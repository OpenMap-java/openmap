package com.bbn.openmap.dataAccess.mapTile.mb;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.fasterxml.jackson.databind.JsonNode;

import no.ecc.vectortile.VectorTileDecoder.Feature;

/**
 * The root node of the style document.  From here, you can get to all info.
 * @author dietrick
 */
public class StyleRoot {

	static String VERSION = "version";
	static String NAME = "name";
	static String METADATA = "metadata";
	static String CONSTANTS = "constants";
	static String CENTER = "center";
	static String ZOOM = "zoom";
	static String BEARING = "bearing";
	static String PITCH = "pitch";
	static String SOURCES = "sources";
	static String SPRITE = "sprite";
	static String GLYPHS = "glyphs";
	static String LAYERS = "layers";

	double version;
	String name;
	Map<String, Object> metadata;
	Map<String, JsonNode> constants;
	LatLonPoint center;
	double zoom;
	double bearing;
	double pitch;
	StyleSource sources;
	String sprites;
	String glyphs;
	List<StyleLayer> layers;

	final HashMap<StyleLayerType, List<StyleLayer>> layerGroupsByType = new HashMap<StyleLayerType, List<StyleLayer>>();
	final HashMap<String, List<StyleLayer>> layerGroupsBySourceLayer = new HashMap<String, List<StyleLayer>>();

	private StyleRoot() {

	}

	public StyleRoot(JsonNode rootNode) {
		version = rootNode.get(VERSION).asDouble();
		name = rootNode.get(NAME).asText();
		zoom = StyleNode.getAsDouble(rootNode, ZOOM, Double.NaN);
		bearing = StyleNode.getAsDouble(rootNode, BEARING, Double.NaN);
		pitch = StyleNode.getAsDouble(rootNode, PITCH, Double.NaN);
		sprites = rootNode.get(SPRITE).asText();
		glyphs = rootNode.get(GLYPHS).asText();

		JsonNode metadataNode = rootNode.get(METADATA);
		if (metadataNode != null) {

		}
		JsonNode sourceNode = rootNode.get(SOURCES);
		if (sourceNode != null) {

		}
		JsonNode layerNode = rootNode.get(LAYERS);
		if (layerNode != null) {
			layers = StyleLayer.getLayerArray(layerNode);
			resetLayerGroups(layers);

			if (getLogger().isLoggable(Level.FINE)) {
				StringBuilder sb = new StringBuilder("StyleLayer ID, SOURCELAYER");
				for (StyleLayer sl : layers) {
					sb.append(sl.id).append(", ").append(sl.sourceLayer);
				}
				getLogger().fine(sb.toString());
			}
		}
	}

	void resetLayerGroups(List<StyleLayer> styleLayers) {
		layerGroupsByType.clear();
		layerGroupsBySourceLayer.clear();

		for (StyleLayer sl : styleLayers) {
			List<StyleLayer> sll = layerGroupsByType.get(sl.type);
			if (sll == null) {
				sll = new ArrayList<StyleLayer>();
				layerGroupsByType.put(sl.type, sll);
			}
			sll.add(sl);

			if (sl.sourceLayer != null) {
				sll = layerGroupsBySourceLayer.get(sl.sourceLayer);
				if (sll == null) {
					sll = new ArrayList<StyleLayer>();
					layerGroupsBySourceLayer.put(sl.sourceLayer, sll);
				}
				sll.add(sl);
			}
		}
	}

	public StyleDrawingAttributes getBackgroundRenderer() {
		List<StyleLayer> backgroundLayers = layerGroupsByType.get(StyleLayerType.BACKGROUND);
		if (backgroundLayers != null && !backgroundLayers.isEmpty()) {
			return backgroundLayers.get(0).renderer;
		}
		return StyleDrawingAttributes.EMPTY;
	}

	public StyleDrawingAttributes getRenderer(Feature feature) { // String
																	// featureSourceLayer,
																	// String
																	// featureClass)
																	// {

		String featureSourceLayer = feature.getLayerName();
		String featureClass = (String) feature.getAttributes().get("class");

		// combos.add(featureClass + ", " + featureSourceLayer);
		List<StyleLayer> matchingSourceLayers = layerGroupsBySourceLayer.get(featureSourceLayer);
		if (matchingSourceLayers != null && !matchingSourceLayers.isEmpty()) {
			StyleDrawingAttributes sda = matchingSourceLayers.get(0).renderer;
			if (sda.visible) {
				return sda;
			}

			/*
			 * for (StyleLayer styleLayer : layers) { String styleSourceLayer =
			 * styleLayer.sourceLayer;
			 * 
			 * if (styleSourceLayer != null &&
			 * styleSourceLayer.equals(featureSourceLayer)) { // We want to
			 * match classes up if we can -
			 * 
			 * return styleLayer.renderer; } }
			 */
		}

		return null;
	}

	/**
	 * Basic styling just to see features.
	 * @author dietrick
	 *
	 */
	public static class DEFAULT extends StyleRoot {

		StyleDrawingAttributes area = StyleDrawingAttributes.get(StyleLayerType.FILL, null);
		StyleDrawingAttributes line = StyleDrawingAttributes.get(StyleLayerType.LINE, null);

		public DEFAULT() {
			version = 1.0;
			name = "OpenMap DEFAULT";
			zoom = Double.NaN;
			bearing = Double.NaN;
			pitch = Double.NaN;
			sprites = "";
			glyphs = "";

			area.setFillPaint(Color.lightGray);
			area.setFillPaint(Color.lightGray);
			line.setLinePaint(Color.darkGray);
		}

		public StyleDrawingAttributes getRenderer(Feature feature) {
			switch (FeatureGeometryType.get(feature.getGeometry().getGeometryType())) {
			case POINT:
			case MULTI_POINT:
			case LINEAR_RING:
			case LINE_STRING:
			case MULTI_LINE_STRING:
				return line;
			case POLYGON:
			case MULTI_POLYGON:
				return area;
			default:
			}
			return null;
		}
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
		private static final Logger LOGGER = Logger.getLogger(StyleRoot.class.getName());

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
