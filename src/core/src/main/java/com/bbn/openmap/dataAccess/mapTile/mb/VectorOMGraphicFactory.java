package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMShape;
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
	AffineTransform coordTransform;

	public VectorOMGraphicFactory(StyleRoot styles) {
		this.styles = styles;
	}

	public HashMap<String, OMGraphicList> getFeatureMap() {
		HashMap<String, OMGraphicList> mapLists = new HashMap<String, OMGraphicList>();
		if (styles != null) {
			for (StyleLayer layer : styles.layers) {
				mapLists.put(layer.id, new OMGraphicList());
			}
		}
		return mapLists;
	}

	public OMGraphic getBackground(int zoomLevel) {
		OMGraphic omg = new OMShape.PROJECTED(new Rectangle2D.Double(0, 0, 256, 256));
		styles.getBackgroundRenderer().getRenderer(zoomLevel).setTo(omg);
		return omg;
	}

	/**
	 * @return the coordTransform
	 */
	public AffineTransform getCoordTransform() {
		return coordTransform;
	}

	/**
	 * @param coordTransform
	 *            the coordTransform to set
	 */
	public void setCoordTransform(AffineTransform coordTransform) {
		this.coordTransform = coordTransform;
	}

	public void createAndSort(Feature feature, int currentZoomLevel, Map<String, OMGraphicList> featureLists) {
		Geometry geometry = feature.getGeometry();

		List<StyleDrawingAttributes> renderers = styles.getRenderers(feature, currentZoomLevel);

		// OK, since we know the renderer here, we can determine if we need
		// OMShape.PROJECTED or OMShape.GAPPED at this point when we evaluate
		// the geometry type.

		if (renderers != null && !renderers.isEmpty()) {
			for (StyleDrawingAttributes renderer : renderers) {
				OMGraphicList featureList = featureLists.get(renderer.layerID);

				OMGraphic omg = null;

				switch (FeatureGeometryType.get(geometry.getGeometryType())) {
				case POINT:
				case MULTI_POINT:
					break;
				case LINEAR_RING:
					omg = transformPolyline((LinearRing) geometry, renderer, currentZoomLevel);
					break;
				case LINE_STRING:
					omg = transformLineString((LineString) geometry, renderer, currentZoomLevel);
					break;
				case MULTI_LINE_STRING:
					omg = transformMultiLineString((MultiLineString) geometry, renderer, currentZoomLevel);
					break;
				case POLYGON:
					omg = transformPolygon((Polygon) geometry, renderer, currentZoomLevel);
					break;
				case MULTI_POLYGON:
					omg = transformMultiPolygon((MultiPolygon) geometry, renderer, currentZoomLevel);
					break;
				default:
					System.out.println("unsure of handing " + geometry.getGeometryType());
				}

				if (omg != null) {
					featureList.add(omg);
					if (getLogger().isLoggable(Level.FINE)) {
						getLogger().fine("++ " + omg.getClass().getName());
					}
				}
			}
		}
	}

	public void render(Graphics2D g2, int zoomLevel, Map<String, OMGraphicList> featureLists) {
		StringBuilder sBuilder = new StringBuilder("\n========= New tile\n");
		if (styles != null) {

			for (StyleLayer layer : styles.layers) {

				if (layer.type.equals(StyleLayerType.BACKGROUND)) {
					if (styles.visibleLayers.contains(layer.id)) {
						getBackground(zoomLevel).render(g2);
						sBuilder.append("  rendering ").append(layer.id).append("\n");
					}
					continue;
				}

				OMGraphicList omgl = featureLists.get(layer.id);
				if (omgl != null && styles.visibleLayers.contains(layer.id)) {
					sBuilder.append("  rendering ").append(layer.id).append(" ").append(omgl.size()).append("\n");
					omgl.render(g2);
				}
			}

		}
		sBuilder.append("========= End tile").append("\n");

		if (getLogger().isLoggable(Level.FINE)) {
			getLogger().fine(sBuilder.toString());
		}
	}

	OMGraphic getOMGraphic(Coordinate[] coords, boolean connect, StyleDrawingAttributes renderer, int zoomLevel) {
		return renderer.getOMGraphic(convertCoords(coords, connect), zoomLevel);
	}

	protected OMGraphic transformPolygon(Polygon polygon, StyleDrawingAttributes renderer, int zoomLevel) {
		int numInteriorRings = polygon.getNumInteriorRing();
		OMGraphic shell = getOMGraphic(polygon.getExteriorRing().getCoordinates(), true, renderer, zoomLevel);

		if (numInteriorRings == 0) {
			return shell;
		} else {
			OMGraphicList omgl = new OMGraphicList();
			omgl.add(shell);
			for (int i = 0; i < numInteriorRings; i++) {
				omgl.add(getOMGraphic(polygon.getInteriorRingN(i).getCoordinates(), true, renderer, zoomLevel));
			}

			return omgl;
		}
	}

	protected OMGraphic transformMultiPolygon(MultiPolygon mPolygon, StyleDrawingAttributes renderer, int zoomLevel) {
		int numGeometries = mPolygon.getNumGeometries();
		OMGraphicList omgl = new OMGraphicList();
		for (int i = 0; i < numGeometries; i++) {
			omgl.add(getOMGraphic(((Polygon) mPolygon.getGeometryN(i)).getCoordinates(), true, renderer, zoomLevel));
		}

		return omgl;
	}

	protected OMGraphic transformPolyline(LinearRing linearRing, StyleDrawingAttributes renderer, int zoomLevel) {
		int numGeometries = linearRing.getNumGeometries();
		if (numGeometries == 1) {
			return getOMGraphic(linearRing.getCoordinates(), false, renderer, zoomLevel);
		} else {
			OMGraphicList omgl = new OMGraphicList();
			for (int i = 0; i < numGeometries; i++) {
				omgl.add(getOMGraphic(linearRing.getGeometryN(i).getCoordinates(), false, renderer, zoomLevel));
			}

			return omgl;
		}

	}

	protected OMGraphic transformLineString(LineString lineString, StyleDrawingAttributes renderer, int zoomLevel) {
		return getOMGraphic(lineString.getCoordinates(), false, renderer, zoomLevel);
	}

	protected OMGraphic transformMultiLineString(MultiLineString mlineString, StyleDrawingAttributes renderer,
			int zoomLevel) {
		int numGeometries = mlineString.getNumGeometries();
		if (numGeometries == 1) {
			return getOMGraphic(mlineString.getCoordinates(), false, renderer, zoomLevel);
		} else {
			OMGraphicList omgl = new OMGraphicList();
			for (int i = 0; i < numGeometries; i++) {
				omgl.add(getOMGraphic(mlineString.getGeometryN(i).getCoordinates(), false, renderer, zoomLevel));
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

		if (coordTransform != null) {
			path.transform(coordTransform);
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
