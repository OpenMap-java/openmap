package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.bbn.openmap.Environment;
import com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.I18n;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
 * </pre>
 * 
 * @author dietrick
 */
public class VectorMapTileFactory extends RasterMapTileFactory {

	public final static String STYLE_LOCATION_PROPERTY = "style";

	VectorOMGraphicFactory factory;
	String styleLocation;
	StyleRoot styles;

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

	public OMGraphicList getTiles(Projection proj, int zoomLevel, OMGraphicList list) {
		OMGraphicList ret = super.getTiles(proj, zoomLevel, list);
		// factory.dumpCombos();
		return ret;
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

		try {
			Connection conn = DriverManager.getConnection(rootDir);
			Statement stat = conn.createStatement();

			// "select zoom_level, tile_column, tile_row, tile_data from map,
			// images where map.tile_id = images.tile_id";
			StringBuilder statement = new StringBuilder("select tile_data from images, map where");
			statement.append(" zoom_level = ").append(zoomLevel);
			statement.append(" and tile_column = ").append(x);
			statement.append(" and tile_row = ").append(Math.pow(2, zoomLevel) - y - 1);
			statement.append(" and map.tile_id = images.tile_id;");

			ResultSet rs = stat.executeQuery(statement.toString());

			while (rs.next()) {

				byte[] compressedTileData = rs.getBytes("tile_data");
				byte[] inflateBuffer = new byte[4096];
				ByteArrayOutputStream inflateBufStream = new ByteArrayOutputStream(inflateBuffer.length);

				///////// unGZIP the byte data ///
				ByteArrayInputStream bais = new ByteArrayInputStream(compressedTileData);
				GZIPInputStream gzipIS = new GZIPInputStream(bais);
				while (gzipIS.available() > 0) {
					int readCount = gzipIS.read(inflateBuffer);
					if (readCount > 0) {
						inflateBufStream.write(inflateBuffer, 0, readCount);
					}
				}
				gzipIS.close();
				inflateBufStream.close();
				byte[] tileData = inflateBufStream.toByteArray();
				///////////////////////////////////////////////////////////

				try {
					FeatureIterable fi = decoder.decode(tileData);
					factory.getBackground().render(g2);
					for (Feature feature : fi.asList()) {
						factory.create(feature).render(g2);
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

			}
			rs.close();
			conn.close();

			try {

				OMGraphic raster = createOMGraphicFromBufferedImage(rasterImage, x, y, zoomLevel, proj);

				/*
				 * Again, create a CacheObject based on the local name if the
				 * local dir is defined.
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

		} catch (Exception e) {
			getLogger().warning("something went wrong fetching image from database: " + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	protected BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
			throws InterruptedException {

		return BufferedImageHelper.getBufferedImage(origImage, 0, 0, 256, 256, BufferedImage.TYPE_INT_ARGB);
	}

	public void setProperties(String prefix, Properties setList) {
		super.setProperties(prefix, setList);
		prefix = PropUtils.getScopedPropertyPrefix(prefix);
		styleLocation = setList.getProperty(prefix + STYLE_LOCATION_PROPERTY, styleLocation);
	}

	public Properties getProperties(Properties getList) {
		getList = super.getProperties(getList);
		String prefix = PropUtils.getScopedPropertyPrefix(this);
		getList.put(prefix + STYLE_LOCATION_PROPERTY, PropUtils.unnull(STYLE_LOCATION_PROPERTY));
		return getList;
	}

	public Properties getPropertyInfo(Properties list) {
		list = super.getPropertyInfo(list);
		I18n i18n = Environment.getI18n();
		PropUtils.setI18NPropertyInfo(i18n, list, VectorMapTileFactory.class, STYLE_LOCATION_PROPERTY, "Style",
				"Location of JSON Style file", "com.bbn.openmap.util.propertyEditor.FilePropertyEditor");
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

	public static void main(String[] args) {

		try {
			Class.forName(DEFAULT_TEST_CLASS);
		} catch (Exception e) {
			System.out.println("can't locate sqlite JDBC components");
			System.exit(-1);
		}

		int zoomLevel = 1;
		int x = 0;
		int y = 1;

		try {
			VectorTileDecoder decoder = new VectorTileDecoder();
			// Connection conn =
			// DriverManager.getConnection("jdbc:sqlite:/Users/dietrick/Downloads/trails.mbtiles");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/data/tiles/united_states_of_america.mbtiles");
			RandomAccessFile raf = new RandomAccessFile("/Users/dietrick/Desktop/output.log", "rw");
			Statement stat = conn.createStatement();

			StringBuilder statement = new StringBuilder(
					"select zoom_level, tile_column, tile_row, tile_id from map where");

			/*
			 * statement.append(" zoom_level = ").append(zoomLevel);
			 * statement.append(" and tile_column = ").append(x);
			 * statement.append(" and tile_row = ").append(y); statement.append(
			 * " and ");
			 */
			statement.append(" map.tile_id = images.tile_id;");

			ResultSet rs = stat.executeQuery(statement.toString());
			while (rs.next()) {

				String header = new String("\nZoom: " + rs.getDouble("zoom_level") + ", x: "
						+ rs.getDouble("tile_column") + ", y: " + rs.getDouble("tile_row") + "\n\n");
				raf.write(header.getBytes());

				byte[] compressedTileData = rs.getBytes("tile_data");
				byte[] inflateBuffer = new byte[4096];
				ByteArrayOutputStream inflateBufStream = new ByteArrayOutputStream(inflateBuffer.length);

				///////// unGZIP the byte data ///
				ByteArrayInputStream bais = new ByteArrayInputStream(compressedTileData);
				GZIPInputStream gzipIS = new GZIPInputStream(bais);
				while (gzipIS.available() > 0) {
					int readCount = gzipIS.read(inflateBuffer);
					if (readCount > 0) {
						inflateBufStream.write(inflateBuffer, 0, readCount);
					}
				}
				gzipIS.close();
				inflateBufStream.close();
				byte[] tileData = inflateBufStream.toByteArray();
				///////////////////////////////////////////////////////////

				try {
					FeatureIterable fi = decoder.decode(tileData);
					for (Feature feature : fi.asList()) {

						Map<String, Object> attributes = feature.getAttributes();

						Geometry geom = feature.getGeometry();
						int numGeometries = geom.getNumGeometries();
						StringBuilder fout = new StringBuilder();
						fout.append("Feature ").append(feature.getLayerName()).append("\n");
						fout.append("  type: ").append(geom.getGeometryType()).append("\n");

						Coordinate[] coords = geom.getCoordinates();
						StringBuilder sb = null;
						if (coords != null && coords.length > 1) {
							for (Coordinate c : coords) {
								if (sb == null) {
									sb = new StringBuilder("  # geometries(").append(numGeometries).append(") coords(")
											.append(coords.length).append(")[");
								} else {
									sb.append("|");
								}
								sb.append(c.x + "," + c.y);
							}

							sb.append("]\n");
							fout.append(sb);
						}

						for (String attName : attributes.keySet()) {
							fout.append("  ").append(attName).append(", ").append(attributes.get(attName).toString())
									.append("\n");
						}

						raf.write(fout.toString().getBytes());
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

			}

			raf.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			// } catch (DataFormatException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

	}

}
