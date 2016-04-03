package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.bbn.openmap.Environment;
import com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.I18n;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheObject;

import no.ecc.vectortile.VectorTileDecoder;
import no.ecc.vectortile.VectorTileDecoder.Feature;
import no.ecc.vectortile.VectorTileDecoder.FeatureIterable;

/**
 * MapTileFactory that handles mbtiles files containing vector data
 * (http://osm2vectortiles.org), in the MapBox format. Requires JTS and the
 * java-vector-tile project (see maven dependencies). Uses MapBox GL JSON files
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
 * #optional - use parent tiles to fill missing tiles.
 * vectorTileLayer.useParentTiles=true
 * </pre>
 * 
 * @author dietrick
 */
public class VectorMapTileFactory extends RasterMapTileFactory {

	public final static String COMPRESSED_PROPERTY = "compressed";
	public final static String FILL_MISSING_TILES_FROM_PARENTS_PROPERTY = "useParentTiles";

	VectorOMGraphicFactory omGraphicFactory;
	StyleRoot renderStyle;
	/** mvt is compressed, pbf is not. */
	boolean compressed = true;
	boolean useParentTiles = true;

	public VectorMapTileFactory() {
		getLogger().fine("Using VectorTileMapTileFactory");
	}

	/**
	 * @return the omGraphicFactory
	 */
	public VectorOMGraphicFactory getOMGraphicFactory() {
		return omGraphicFactory;
	}

	/**
	 * Set the VectorOMGraphicFactory. Assumes that the styling is set for it.
	 * Resets the cache so new tiles need to be generated with the new style.
	 * 
	 * @param omGraphicFactory
	 *            the omGraphicFactory to set
	 */
	public void setOMGraphicFactory(VectorOMGraphicFactory omGraphicFactory) {
		this.omGraphicFactory = omGraphicFactory;
		reset();
	}

	/**
	 * @return the compressed
	 */
	public boolean isCompressed() {
		return compressed;
	}

	/**
	 * @param compressed
	 *            the compressed to set
	 */
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	/**
	 * @return the useParentTiles
	 */
	public boolean isUseParentTiles() {
		return useParentTiles;
	}

	/**
	 * @param useParentTiles
	 *            the useParentTiles to set
	 */
	public void setUseParentTiles(boolean useParentTiles) {
		this.useParentTiles = useParentTiles;
	}

	/**
	 * @return the renderStyle
	 */
	public StyleRoot getRenderStyle() {
		return renderStyle;
	}

	/**
	 * Causes a new VectoryOMGraphicFactory to be created with the new
	 * renderStyle, and the cache is cleared to force new tiles to be created.
	 * 
	 * @param renderStyle
	 *            the renderStyle to set
	 */
	public void setRenderStyle(StyleRoot renderStyle) {
		this.renderStyle = renderStyle;
		if (renderStyle != null) {
			setOMGraphicFactory(new VectorOMGraphicFactory(renderStyle));
		} else {
			setOMGraphicFactory(null);
		}

		createGUIFilters();
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

		if (rootDir == null) {
			getLogger().warning("Tile location (rootDir) not set");
			return null;
		}

		if (omGraphicFactory == null) {

			if (renderStyle == null) {
				renderStyle = new StyleRoot.DEFAULT();
			}

			omGraphicFactory = new VectorOMGraphicFactory(renderStyle);
		}

		VectorTileDecoder decoder = new VectorTileDecoder();
		BufferedImage rasterImage = new BufferedImage(MapTileCoordinateTransform.TILE_SIZE,
				MapTileCoordinateTransform.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g2 = rasterImage.createGraphics();
		RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.addRenderingHints(renderingHints);

		try {

			// What to do if the tile isn't there? We can check smaller zoom
			// levels to see if parent tiles exist. If they do, we can read
			// those and set an AffineTransform on the Graphics object to adjust
			// zoom and offset. The result should look fine, this is vector
			// data.

			TileDataLoader tileDataLoader = new TileDataLoader(key, x, y, zoomLevel);
			byte[] tileData = tileDataLoader.tileData;
			if (tileData != null) {
				FeatureIterable fi = decoder.decode(tileData);

				Map<String, OMGraphicList> featureLists = omGraphicFactory.getFeatureMap();

				omGraphicFactory.setCoordTransform(tileDataLoader.transform);
				for (Feature feature : fi.asList()) {
					omGraphicFactory.createAndSort(feature, zoomLevel, featureLists);
				}

				omGraphicFactory.render(g2, zoomLevel, featureLists);
				filterPanel.revalidate();

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
		compressed = PropUtils.booleanFromProperties(setList, prefix + COMPRESSED_PROPERTY, compressed);
		useParentTiles = PropUtils.booleanFromProperties(setList, prefix + FILL_MISSING_TILES_FROM_PARENTS_PROPERTY,
				useParentTiles);
	}

	public Properties getProperties(Properties getList) {
		getList = super.getProperties(getList);
		String prefix = PropUtils.getScopedPropertyPrefix(this);
		getList.put(prefix + COMPRESSED_PROPERTY, Boolean.toString(compressed));
		getList.put(prefix + FILL_MISSING_TILES_FROM_PARENTS_PROPERTY, Boolean.toString(useParentTiles));
		return getList;
	}

	public Properties getPropertyInfo(Properties list) {
		list = super.getPropertyInfo(list);
		I18n i18n = Environment.getI18n();
		PropUtils.setI18NPropertyInfo(i18n, list, VectorMapTileFactory.class, COMPRESSED_PROPERTY, "Compressed",
				"True if tile data is compressed", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
		PropUtils.setI18NPropertyInfo(i18n, list, VectorMapTileFactory.class, FILL_MISSING_TILES_FROM_PARENTS_PROPERTY,
				"Use Parent Tiles", "Will use parent data for missing tiles.",
				"com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
		return list;
	}

	public String getInitPropertiesOrder() {
		return ROOT_DIR_PROPERTY;
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

	List<JCheckBox> filterCheckBoxes = new ArrayList<JCheckBox>();
	JPanel filterPanel = new JPanel();

	public JComponent getFilterPanel() {
		filterPanel.removeAll();
		JComponent guiPanel = createGUIFilters();
		filterPanel.add(guiPanel);
		return filterPanel;
	}

	protected JComponent createGUIFilters() {
		filterCheckBoxes.clear();

		JPanel panel = new JPanel();
		JScrollPane jsp = new JScrollPane(panel);
		jsp.setMaximumSize(new Dimension(900, 500));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;

		panel.setLayout(gridbag);
		if (renderStyle != null) {
			int countForColumn = 0;
			int countForRow = 0;

			int numColumns = renderStyle.layers.size() / 10;
			if (numColumns > 4) {
				numColumns = 4;
			} else if (numColumns <= 0) {
				numColumns = 1;
			}

			for (StyleLayer layer : renderStyle.layers) {
				JCheckBox jcb = new JCheckBox(layer.id, renderStyle.visibleLayers.contains(layer.id));
				jcb.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						updateVisibilityList();
					}
				});
				filterCheckBoxes.add(jcb);
				c.gridx = countForColumn++ % numColumns;
				c.gridy = countForRow++ / numColumns;
				gridbag.setConstraints(jcb, c);
				panel.add(jcb);
			}
		}

		JButton selectAllButton = new JButton("Select All");
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				updateVisibilityList(true);
			}
		});
		JButton selectNoneButton = new JButton("Select None");
		selectNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				updateVisibilityList(false);
			}
		});

		JPanel parent = new JPanel();
		gridbag = new GridBagLayout();
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		parent.setLayout(gridbag);
		gridbag.setConstraints(jsp, c);
		parent.add(jsp);

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(selectAllButton, c);
		parent.add(selectAllButton);
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(selectNoneButton, c);
		parent.add(selectNoneButton);
		return parent;
	}

	protected void updateVisibilityList(boolean setAllTo) {
		for (JCheckBox jcb : filterCheckBoxes) {
			jcb.setSelected(setAllTo);
		}
		updateVisibilityList();
	}

	protected void updateVisibilityList() {
		List<String> visibleLayers = new ArrayList<String>();
		for (JCheckBox jcb : filterCheckBoxes) {
			if (jcb.isSelected()) {
				visibleLayers.add(jcb.getText());
			}
		}
		renderStyle.visibleLayers = visibleLayers;

		reset();
		if (mapTileRequester instanceof OMGraphicHandlerLayer) {
			((OMGraphicHandlerLayer) mapTileRequester).doPrepare();

		}
	}

	/**
	 * This class loads parent tile data if a tile isn't found. The transform
	 * will handle how to change the projection of the points of the parent to
	 * make them work for the desired tile location and zoom level.
	 * 
	 * <p>
	 * NOTE: this class assumes that the tiles are laid out in MapBox OSM
	 * notation, where 0,0 is upper left for tile location, and zooming out
	 * lowers the zoom level. It does not take into account the
	 * MapTileCoordinateTransform the layer may be using.
	 * 
	 * @author dietrick
	 *
	 */
	class TileDataLoader {
		/**
		 * The transform that might have to be made on the tile data to get it
		 * to work for the requested tile location.
		 */
		AffineTransform transform;
		/**
		 * The tile data to use.
		 */
		byte[] tileData;
		/**
		 * This is the zoom level where a tile was found that covers the
		 * original tile area.
		 */
		int foundDataZoomLevel;

		TileDataLoader(Object key, int x, int y, int zoomLevel) throws Exception {
			tileData = findTileData(key, x, y, zoomLevel);
			if (tileData == null) {
				foundDataZoomLevel = zoomLevel;
			}

			double zoomLevelMultiplier = Math.pow(2.0, zoomLevel - foundDataZoomLevel);

			double xoffset = (x % zoomLevelMultiplier) * MapTileCoordinateTransform.TILE_SIZE;
			double yoffset = (y % zoomLevelMultiplier) * MapTileCoordinateTransform.TILE_SIZE;

			this.transform = AffineTransform.getTranslateInstance(-xoffset, -yoffset);
			this.transform.concatenate(AffineTransform.getScaleInstance(zoomLevelMultiplier, zoomLevelMultiplier));
		}

		/**
		 * Recursive tile search, until the zoom levels run out.
		 * 
		 * @param key
		 * @param x
		 * @param y
		 * @param zoomLevel
		 * @return
		 * @throws Exception
		 */
		byte[] findTileData(Object key, int x, int y, int zoomLevel) throws Exception {
			byte[] tileDataBytes = getTileData(key, x, y, zoomLevel);
			if (tileDataBytes != null || !useParentTiles) {
				foundDataZoomLevel = zoomLevel;
				return tileDataBytes;
			}

			if (zoomLevel == 0) {
				return null;
			}

			// to check parent, divide x, y by 2 and get an integer, chop off
			// the remainder.
			zoomLevel--;
			x = x / 2;
			y = y / 2;

			return findTileData(key, x, y, zoomLevel);
		}
	}
}
