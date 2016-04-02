package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import no.ecc.vectortile.VectorTileDecoder.Feature;

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
	StyleLayer refLayer = null;

	public StyleLayer(JsonNode layerNode, List<StyleLayer> currentLayers, JsonNode constants) {
		id = layerNode.get(ID).asText();

		// This is handy for debugging, when you want to trap the thread on a
		// desired entry. Look for the layer id here, as defined in the style
		// file.
		/*
		 * if (id.equals("water_offset")) { System.out.println("water_pattern");
		 * }
		 */

		ref = StyleNode.getAsText(layerNode, REF, null);
		if (ref != null && currentLayers != null) {
			refLayer = resolveRef(currentLayers);
		}

		if (refLayer != null) {
			renderer = StyleDrawingAttributes.get(type, layerNode, constants);
			// And also layout parameters.
			if (renderer != null && refLayer.renderer != null) {
				renderer.configureFromReference(refLayer.renderer);
			} else {
				System.out.println("StyleLayer " + id + " can't configure renderer this:"
						+ (renderer != null ? "OK" : "NOPE") + " that:" + (refLayer.renderer != null ? "OK" : "NOPE"));
			}
		} else {
			type = StyleLayerType.getFromLayerNode(layerNode);
			source = StyleNode.getAsText(layerNode, SOURCE, null);
			sourceLayer = StyleNode.getAsText(layerNode, SOURCE_LAYER, null);
			filter = StyleFilter.getForLayerNode(layerNode);
			minZoom = StyleNode.getAsDouble(layerNode, MIN_ZOOM, Double.NaN);
			maxZoom = StyleNode.getAsDouble(layerNode, MAX_ZOOM, Double.NaN);
			renderer = StyleDrawingAttributes.getForType(layerNode, constants);
		}

		renderer.layerID = this.id;

		interactive = StyleNode.getAsBoolean(layerNode, INTERACTIVE, false);

		if (ref != null) {
			getLogger().warning(id + ": REF linked: " + ref);
		}

		if (layerNode.get(METADATA) != null) {
			getLogger().warning(id + ": metadata is being ignored for now.");
		}

	}

	StyleLayer resolveRef(List<StyleLayer> layers) {
		for (StyleLayer layer : layers) {
			if (layer.id.equals(ref)) {
				if (getLogger().isLoggable(Level.INFO)) {
					getLogger().info("StyleLayer (" + id + ") found referenced layer: " + layer.id);
				}
				return configureFromReference(layer);
			}
		}
		return null;
	}

	StyleLayer configureFromReference(StyleLayer reference) {
		// According to the specification, this is what gets copied from the
		// reference.
		this.type = reference.type;
		this.source = reference.source;
		this.sourceLayer = reference.sourceLayer;
		this.minZoom = reference.minZoom;
		this.maxZoom = reference.maxZoom;
		this.filter = reference.filter;

		// and layout, but we hook that up explicitly in the constructor after
		// this method is called.

		return reference;
	}

	public boolean passes(Feature feature, int zoomLevel) {

		if ((!Double.isNaN(minZoom) && zoomLevel < minZoom) || (!Double.isNaN(maxZoom) && zoomLevel > maxZoom)) {
			return false;
		}

		if (filter != null) {
			return filter.passes(feature);
		}
		return true;
	}

	public static List<StyleLayer> getLayerArray(JsonNode node, JsonNode constants) {
		List<StyleLayer> layers = new ArrayList<StyleLayer>();
		Iterator<JsonNode> layerNodes = node.elements();
		while (layerNodes.hasNext()) {
			layers.add(new StyleLayer(layerNodes.next(), new ArrayList<StyleLayer>(layers), constants));
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
