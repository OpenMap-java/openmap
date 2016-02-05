package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class StyleLayer {

	/** Required string. Unique layer name. */
	static String ID = "id";

	/**
	 * Optional Arbitrary properties useful to track with the layer, but do not
	 * influence rendering. Properties should be prefixed to avoid collisions,
	 * like ‘mapbox:’.
	 */
	static String METADATA = "metadata";
	/**
	 * Optional string. References another layer to copy type, source,
	 * source-layer, minzoom, maxzoom, filter, and layout properties from. This
	 * allows the layers to share processing and be more efficient.
	 */
	static String REF = "ref";
	/**
	 * Optional string. Name of a source description to be used for this layer.
	 */
	static String SOURCE = "source";
	/**
	 * Optional string. Layer to use from a vector tile source. Required if the
	 * source supports multiple layers.
	 */
	static String SOURCE_LAYER = "source-layer";
	/**
	 * Optional number. The minimum zoom level on which the layer gets parsed
	 * and appears on.
	 */
	static String MIN_ZOOM = "minzoom";
	/**
	 * Optional number. The maximum zoom level on which the layer gets parsed
	 * and appears on.
	 */
	static String MAX_ZOOM = "maxzoom";
	/**
	 * Optional boolean. Defaults to false. Enable querying of feature data from
	 * this layer for interactivity.
	 */
	static String INTERACTIVE = "interactive";
	/**
	 * Optional paint. Class-specific paint properties for this layer. The class
	 * name is the part after the first dot.
	 */
	static String PAINT_DOT = "paint.";

	String id;
	StyleLayerType type;
	Map<String, Object> metadata;
	String ref;
	String source;
	String sourceLayer;
	double minZoom;
	double maxZoom;
	boolean interactive;
	StyleFilter filter;
	StyleDrawingAttributes renderer;

	public StyleLayer(JsonNode layerNode) {
		id = layerNode.get(ID).asText();
		type = StyleLayerType.getFromLayerNode(layerNode);
		renderer = StyleDrawingAttributes.getForType(layerNode);
		
		source = StyleNode.getAsText(layerNode, SOURCE, null);
		sourceLayer = StyleNode.getAsText(layerNode, SOURCE_LAYER, null);
		interactive = StyleNode.getAsBoolean(layerNode, INTERACTIVE, false);
		
		filter = StyleFilter.getForLayerNode(layerNode);
		minZoom = StyleNode.getAsDouble(layerNode, MIN_ZOOM, Double.NaN);
		maxZoom = StyleNode.getAsDouble(layerNode, MAX_ZOOM, Double.NaN);
		
		if (layerNode.get(REF) != null) {
			System.out.println(id + ": REF links aren't ready");
		}
		
		if (layerNode.get(METADATA) != null) {
			System.out.println(id + ": metadata is being ignored for now.");
		}
		
	}

	public static List<StyleLayer> getLayerArray(JsonNode node) {
		List<StyleLayer> layers = new ArrayList<StyleLayer>();
		Iterator<JsonNode> layerNodes = node.elements();
		while (layerNodes.hasNext()) {
			layers.add(new StyleLayer(layerNodes.next()));
		}
		return layers;
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
		private static final Logger LOGGER = Logger.getLogger(StyleLayer.class.getName());

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
