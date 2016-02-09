package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.bbn.openmap.Environment;
import com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.I18n;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.ecc.vectortile.VectorTileDecoder;
import no.ecc.vectortile.VectorTileDecoder.Feature;
import no.ecc.vectortile.VectorTileDecoder.FeatureIterable;

/**
 * MapTileFactory that handles mbtiles files containing vector data
 * (http://osm2vectortiles.org), in the MapBox format. Requires JTS and the
 * java-vector-tile project (see maven dependecies). Uses MapBox GL JSON files
 * for styling (https://www.mapbox.com/mapbox-gl-style-spec/).
 * 
 * <pre>
 *  
 * vectorTileLayer.class=com.bbn.openmap.layer.image.MapTileLayer
 * vectorTileLayer.prettyName=Vector Tiles (USA)
 * vectorTileLayer.tileFactory=com.bbn.openmap.dataAccess.mapTile.mb.VectorMapTileFactory
 * vectorTileLayer.style=styles/basic-v8.json
 * vectorTileLayer.rootDir=jdbc:sqlite:/data/tiles/united_states_of_america.mbtiles
 * 
 * #optional - mbtiles and mvt files have compressed data, pbf data is not compressed.
 * vectorTileLayer.compressed=true
 * </pre>
 * 
 * @author dietrick
 */
public class VectorMapTileFactory extends RasterMapTileFactory {

	public final static String STYLE_LOCATION_PROPERTY = "style";
	public final static String COMPRESSED_PROPERTY = "compressed";

	VectorOMGraphicFactory factory;
	String styleLocation;
	StyleRoot styles;
	/** mvt is compressed, pbf is not.*/
	boolean compressed = true;

	public VectorMapTileFactory() {
		getLogger().fine("Using VectorTileMapTileFactory");
	}

	public StyleRoot loadStyleJSON(String urlString) {
		if (urlString != null) {
			try {

				URL input = PropUtils.getResourceOrFileOrURL(urlString);
				InputStream inputStream = input.openStream();
				return new StyleRoot(new ObjectMapper().readTree(inputStream));

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	boolean disabled = false;

	/**
	 * Fetches a new tile from the database.
	 */
	public CacheObject load(Object key, int x, int y, int zoomLevel, Projection proj) {

		if (!jdbcLoaded || disabled) {
			if (!disabled) {
				logger.log(Level.INFO, "jbdc not loaded, disabling VectorMapTileFactory");
				disabled = true;
			}
			return null;
		}

		if (factory == null) {
			if (styleLocation != null) {
				styles = loadStyleJSON(styleLocation);
			}

			if (styles == null) {
				styles = new StyleRoot.DEFAULT();
			}

			factory = new VectorOMGraphicFactory(styles);
		}

		VectorTileDecoder decoder = new VectorTileDecoder();
		BufferedImage rasterImage = new BufferedImage(MapTileCoordinateTransform.TILE_SIZE,
				MapTileCoordinateTransform.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g2 = rasterImage.createGraphics();
		RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.addRenderingHints(renderingHints);
		factory.getBackground().render(g2);

		try {
			byte[] tileData = getTileData(key, x, y, zoomLevel);
			if (tileData != null) {
				FeatureIterable fi = decoder.decode(tileData);
				for (Feature feature : fi.asList()) {
					factory.create(feature).render(g2);
				}
			} else {
				if (getLogger().isLoggable(Level.FINER)) {
					getLogger().finer("tile for " + zoomLevel + "|" + x + "|" + y + " is missing.");
				}
			}
		} catch (Exception e) {
			getLogger().warning("something went wrong fetching image from database: " + e.getMessage());
			e.printStackTrace();
		}

		/**
		 * At this point, we have a image of rendered tile data. Prepare it for
		 * OpenMap layer.
		 */

		try {

			OMGraphic raster = createOMGraphicFromBufferedImage(rasterImage, x, y, zoomLevel, proj);

			/*
			 * Again, create a CacheObject based on the local name if the local
			 * dir is defined.
			 */
			if (raster != null) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("building" + key);
				}

				return new CacheObject(key, raster);
			}

		} catch (InterruptedException ie) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("factory interrupted fetching " + key);
			}
		}

		return null;
	}

	protected BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
			throws InterruptedException {

		return BufferedImageHelper.getBufferedImage(origImage, 0, 0, 256, 256, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Fetch tile data. Creates a SQL statement matching the MBTiles file schema
	 * and pulls gzipped binary data from the tile_data. If you want to fetch
	 * vector tiles differently, override this method!
	 * 
	 * @param key
	 *            the path to the tile built up from rootDir and the
	 *            TilePathBuilder. source location
	 * @param x
	 *            horizontal tile column
	 * @param y
	 *            vertical tile row
	 * @param zoomLevel
	 *            tile zoom level
	 * @return uncompressed tile data, ready for decoding
	 */
	public byte[] getTileData(Object key, int x, int y, int zoomLevel) throws Exception {

		// For the default implementation of this class, the rootDir should
		// point at the mbtiles file. That's what we use to establish a
		// connection.
		Connection conn = DriverManager.getConnection(rootDir);
		Statement stat = conn.createStatement();

		// "select zoom_level, tile_column, tile_row, tile_data from map,
		// images where map.tile_id = images.tile_id";
		StringBuilder statement = new StringBuilder("select tile_data from images, map where");
		statement.append(" zoom_level = ").append(zoomLevel);
		statement.append(" and tile_column = ").append(x);
		statement.append(" and tile_row = ").append(Math.pow(2, zoomLevel) - y - 1);
		statement.append(" and map.tile_id = images.tile_id;");

		if (getLogger().isLoggable(Level.FINE)) {
			getLogger().fine(statement.toString());
		}

		ResultSet rs = stat.executeQuery(statement.toString());
		byte[] tileData = null;
		if (rs.next()) {
			tileData = rs.getBytes("tile_data");
			rs.close();
			conn.close();
		}

		if (tileData != null) {
			if (compressed) {
				return inflate(tileData);
			} else {
				return tileData;
			}
		}

		return null;
	}

	/**
	 * Decompressed the tile data (un-gzip)
	 * 
	 * @param tileData
	 * @return tile data, uncompressed.
	 * @throws IOException
	 */
	protected byte[] inflate(byte[] tileData) throws IOException {
		byte[] inflateBuffer = new byte[4096];
		ByteArrayOutputStream inflateBufStream = new ByteArrayOutputStream(inflateBuffer.length);

		///////// unGZIP the byte data ///
		ByteArrayInputStream bais = new ByteArrayInputStream(tileData);
		GZIPInputStream gzipIS = new GZIPInputStream(bais);
		while (gzipIS.available() > 0) {
			int readCount = gzipIS.read(inflateBuffer);
			if (readCount > 0) {
				inflateBufStream.write(inflateBuffer, 0, readCount);
			}
		}
		gzipIS.close();
		inflateBufStream.close();
		return inflateBufStream.toByteArray();
		///////////////////////////////////////////////////////////
	}

	public void setProperties(String prefix, Properties setList) {
		super.setProperties(prefix, setList);
		prefix = PropUtils.getScopedPropertyPrefix(prefix);
		styleLocation = setList.getProperty(prefix + STYLE_LOCATION_PROPERTY, styleLocation);
		compressed = PropUtils.booleanFromProperties(setList, prefix + COMPRESSED_PROPERTY, compressed);
	}

	public Properties getProperties(Properties getList) {
		getList = super.getProperties(getList);
		String prefix = PropUtils.getScopedPropertyPrefix(this);
		getList.put(prefix + STYLE_LOCATION_PROPERTY, PropUtils.unnull(STYLE_LOCATION_PROPERTY));
		getList.put(prefix + COMPRESSED_PROPERTY, Boolean.toString(compressed));
		return getList;
	}

	public Properties getPropertyInfo(Properties list) {
		list = super.getPropertyInfo(list);
		I18n i18n = Environment.getI18n();
		PropUtils.setI18NPropertyInfo(i18n, list, VectorMapTileFactory.class, STYLE_LOCATION_PROPERTY, "Style",
				"Location of JSON Style file", "com.bbn.openmap.util.propertyEditor.FilePropertyEditor");
		PropUtils.setI18NPropertyInfo(i18n, list, VectorMapTileFactory.class, COMPRESSED_PROPERTY, "Compressed",
				"True if tile data is compressed", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
		return list;
	}

	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(VectorMapTileFactory.class.getName());

		/**
		 * Prevent instantiation
		 */
		private LoggerHolder() {
			throw new AssertionError("The LoggerHolder should never be instantiated");
		}
	}

	/**
	 * Get the logger for this class.
	 *
	 * @return logger for this class
	 */
	private static Logger getLogger() {
		return LoggerHolder.LOGGER;
	}
	// </editor-fold>
}
