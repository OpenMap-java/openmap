package com.bbn.openmap.layer.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import com.bbn.openmap.Environment;
import com.bbn.openmap.dataAccess.mapTile.mb.StyleRoot;
import com.bbn.openmap.dataAccess.mapTile.mb.VectorMapTileFactory;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.I18n;
import com.bbn.openmap.util.PropUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VectorMapTileLayer extends MapTileLayer {

	private static final long serialVersionUID = 1L;

	public final static String STYLE_LOCATION_PROPERTY = "style";

	String styleLocation;
	StyleRoot renderStyle;

	public VectorMapTileLayer() {
		super();
		setTileFactory(new VectorMapTileFactory());
	}

	/**
	 * OMGraphicHandlerLayer method, called with projection changes or whenever
	 * else doPrepare() is called. Calls getTiles on the map tile factory.
	 * 
	 * @return OMGraphicList that contains tiles to be displayed for the current
	 *         projection.
	 */
	public synchronized OMGraphicList prepare() {

		Projection projection = getProjection();

		if (projection == null) {
			return null;
		}

		if (renderStyle == null && styleLocation != null) {
			renderStyle = loadStyleJSON(styleLocation);
			if (tileFactory instanceof VectorMapTileFactory) {
				getLogger().fine("setting new render style on tile factory");
				((VectorMapTileFactory) tileFactory).setRenderStyle(renderStyle);
			}
		}

		if (tileFactory != null) {
			OMGraphicList newList = new OMGraphicList();

			OMText attrib = getAttributionGraphic();
			if (attrib != null) {
				newList.add(attrib);
			}

			return tileFactory.getTiles(projection, zoomLevel, newList);
		}

		return null;
	}

	public StyleRoot loadStyleJSON(String urlString) {
		if (urlString != null) {
			try {

				URL input = PropUtils.getResourceOrFileOrURL(urlString);
				InputStream inputStream = input.openStream();
				return new StyleRoot(new ObjectMapper().readTree(inputStream));

			} catch (MalformedURLException e) {
				getLogger().warning(urlString + " style couldn't be found, " + e.getMessage());
			} catch (IOException e) {
				getLogger().warning(urlString + " style couldn't be loaded property, " + e.getMessage());
			}
		}

		return null;
	}

	/**
	 * @return the styleLocation
	 */
	public String getStyleLocation() {
		return styleLocation;
	}

	/**
	 * Will cause a reset of the tile factory.
	 * 
	 * @param styleLocation
	 *            the styleLocation to set
	 */
	public void setStyleLocation(String styleLocation) {
		this.styleLocation = styleLocation;
		renderStyle = null;

		doPrepare();
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		setPropertyPrefix(prefix);
		prefix = PropUtils.getScopedPropertyPrefix(prefix);
		setStyleLocation(props.getProperty(prefix + STYLE_LOCATION_PROPERTY, getStyleLocation()));
	}

	public Properties getProperties(Properties props) {
		props = super.getProperties(props);
		String prefix = PropUtils.getScopedPropertyPrefix(this);
		props.put(prefix + STYLE_LOCATION_PROPERTY, PropUtils.unnull(getStyleLocation()));
		return props;
	}

	public Properties getPropertyInfo(Properties props) {
		props = super.getPropertyInfo(props);
		I18n i18n = Environment.getI18n();
		PropUtils.setI18NPropertyInfo(i18n, props, this.getClass(), STYLE_LOCATION_PROPERTY, "Style",
				"Location of JSON Style file", "com.bbn.openmap.util.propertyEditor.FilePropertyEditor");

		props.put(initPropertiesProperty,
				PropUtils.unnull(props.getProperty(initPropertiesProperty)) + " " + STYLE_LOCATION_PROPERTY);
		return props;
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
		private static final Logger LOGGER = Logger.getLogger(VectorMapTileLayer.class.getName());

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
