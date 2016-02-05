package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import no.ecc.vectortile.VectorTileDecoder.Feature;

public class StyleFilter {
	/**
	 * Optional filter. A expression specifying conditions on source features.
	 * Only features that match the filter are displayed.
	 */
	static String FILTER = "filter";

	StyleFilterOperation op;

	StyleFilter(StyleFilterOperation op) {
		this.op = op;
	}

	public static StyleFilter getForLayerNode(JsonNode layerNode) {
		JsonNode filterNode = layerNode.get(FILTER);
		if (filterNode != null) {

			JsonNode opNode = filterNode.get(0);
			StyleFilterOperation op = StyleFilterOperation.getForFilterNode(opNode);

			switch (op) {
			case EQUALS:
			case NOT_EQUALS:
			case GREATER_THAN:
			case GREATER_THAN_EQUALS:
			case LESS_THAN:
			case LESS_THAN_EQUALS:
				return new StyleFilter.KEY_VALUE(op, filterNode);
			case IN:
			case NOT_IN:
				return new StyleFilter.KEY_LIST(op, filterNode);
			case ALL:
			case ANY:
			case NONE:
				return new StyleFilter.COMPOUND(op, filterNode);
			default:
			}
	
		}
		return new StyleFilter(StyleFilterOperation.NOTHING);				
	}

	public boolean passes(Feature feature) {
		return true;
	}

	public static class KEY_VALUE extends StyleFilter {
		String key;
		String value;

		public KEY_VALUE(StyleFilterOperation op, JsonNode filterNode) {
			super(op);
			key = filterNode.get(1).asText();
			value = filterNode.get(2).asText();
		}

		public boolean passes(Feature feature) {
			return true;
		}

	}

	public static class KEY_LIST extends StyleFilter {
		String key;
		HashSet<String> values;

		public KEY_LIST(StyleFilterOperation op, JsonNode filterNode) {
			super(op);
		}

		public boolean passes(Feature feature) {
			return true;
		}
	}

	public static class COMPOUND extends StyleFilter {
		List<StyleFilter> filters;

		public COMPOUND(StyleFilterOperation op, JsonNode filterNode) {
			super(op);
		}

		public boolean passes(Feature feature) {
			return true;
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
		private static final Logger LOGGER = Logger.getLogger(StyleFilter.class.getName());

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
