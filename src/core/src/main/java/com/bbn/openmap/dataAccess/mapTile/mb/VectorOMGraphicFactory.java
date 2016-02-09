package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMShape;
import com.bbn.openmap.omGraphics.SinkGraphic;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import no.ecc.vectortile.VectorTileDecoder.Feature;

public class VectorOMGraphicFactory {

	StyleRoot styles;

	public VectorOMGraphicFactory(StyleRoot styles) {
		this.styles = styles;
	}

	public OMGraphic getBackground() {
		OMGraphic omg = new OMShape.PROJECTED(new Rectangle2D.Double(0, 0, 256, 256));
		styles.getBackgroundRenderer().setTo(omg);
		return omg;
	}
	
	public OMGraphic create(Feature feature) {
		Geometry geometry = feature.getGeometry();

		StyleDrawingAttributes renderer = styles.getRenderer(feature);
		OMGraphic omg = SinkGraphic.getSharedInstance();

		if (renderer != null) {
			switch (FeatureGeometryType.get(geometry.getGeometryType())) {
			case POINT:
			case MULTI_POINT:
				break;
			case LINEAR_RING:
				omg = transformPolyline((LinearRing) geometry);
				break;
			case LINE_STRING:
				omg = transformLineString((LineString) geometry);
				break;
			case MULTI_LINE_STRING:
				omg = transformMultiLineString((MultiLineString) geometry);				
				break;
			case POLYGON:
				omg = transformPolygon((Polygon) geometry);
				break;
			case MULTI_POLYGON:
				omg = transformMultiPolygon((MultiPolygon) geometry);
				break;
			default:
			}
			
			if (getLogger().isLoggable(Level.FINE)) {
				getLogger().fine("++ " + omg.getClass().getName());
			}

			renderer.setTo(omg);
		}

		return omg;
	}

	protected OMGraphic transformPolygon(Polygon polygon) {
		int numInteriorRings = polygon.getNumInteriorRing();
		OMShape shell = new OMShape.PROJECTED(convertCoords(polygon.getExteriorRing().getCoordinates(), true));

		if (numInteriorRings == 0) {
			return shell;
		} else {
			OMGraphicList omgl = new OMGraphicList();
			omgl.add(shell);
			for (int i = 0; i < numInteriorRings; i++) {
				omgl.add(new OMShape.PROJECTED(convertCoords(polygon.getInteriorRingN(i).getCoordinates(), true)));
			}

			return omgl;
		}
	}

	protected OMGraphic transformMultiPolygon(MultiPolygon mPolygon) {
		int numGeometries = mPolygon.getNumGeometries();
		OMGraphicList omgl = new OMGraphicList();
		for (int i = 0; i < numGeometries; i++) {
			omgl.add(new OMShape.PROJECTED(convertCoords(((Polygon) mPolygon.getGeometryN(i)).getCoordinates(), true)));
		}

		return omgl;
	}

	protected OMGraphic transformPolyline(LinearRing linearRing) {
		int numGeometries = linearRing.getNumGeometries();
		if (numGeometries == 1) {
			return new OMShape.PROJECTED(convertCoords(linearRing.getCoordinates(), false));
		} else {
			OMGraphicList omgl = new OMGraphicList();
			for (int i = 0; i < numGeometries; i++) {
				omgl.add(new OMShape.PROJECTED(convertCoords(linearRing.getGeometryN(i).getCoordinates(), false)));
			}

			return omgl;
		}

	}

	protected OMGraphic transformLineString(LineString lineString) {
		return new OMShape.PROJECTED(convertCoords(lineString.getCoordinates(), false));
	}

	protected OMGraphic transformMultiLineString(MultiLineString mlineString) {
		int numGeometries = mlineString.getNumGeometries();
		if (numGeometries == 1) {
			return new OMShape.PROJECTED(convertCoords(mlineString.getCoordinates(), false));
		} else {
			OMGraphicList omgl = new OMGraphicList();
			for (int i = 0; i < numGeometries; i++) {
				omgl.add(new OMShape.PROJECTED(convertCoords(mlineString.getGeometryN(i).getCoordinates(), false)));
			}

			return omgl;
		}
	}

	protected GeneralPath convertCoords(Coordinate[] coords, boolean isPolygon) {
		GeneralPath path = null;
		for (Coordinate c : coords) {
			if (path == null) {
				path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, coords.length);
				path.moveTo(c.x, c.y);
			} else {
				path.lineTo(c.x, c.y);
			}
		}

		if (isPolygon) {
			path.closePath();
		}

		return path;
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
		private static final Logger LOGGER = Logger.getLogger(VectorOMGraphicFactory.class.getName());

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
