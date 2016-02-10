package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import no.ecc.vectortile.VectorTileDecoder.Feature;

public class StyleFilter {
	/**
	 * Optional filter. A expression specifying conditions on source features.
	 * Only features that match the filter are displayed.
	 */
	static String FILTER = "filter";
	static boolean fineLogging = getLogger().isLoggable(Level.FINE);

	/**
	 * Filter key to check feature geometry type.
	 */
	static String SPECIAL_CLASS_KEY = "$type";

	StyleFilterOperation op;

	StyleFilter(StyleFilterOperation op) {
		this.op = op;
	}

	public static StyleFilter getForLayerNode(JsonNode layerNode) {
		JsonNode filterNode = layerNode.get(FILTER);
		if (filterNode != null) {
			return getForFilterNode(filterNode);
		}
		return new StyleFilter(StyleFilterOperation.NOTHING);
	}

	public static StyleFilter getForFilterNode(JsonNode filterNode) {
		JsonNode opNode = filterNode.get(0);
		if (fineLogging) {
			getLogger().fine("eval " + filterNode + ", have first: " + opNode);
		}
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
		return new StyleFilter(StyleFilterOperation.NOTHING);
	}

	public boolean passes(Feature feature) {
		return true;
	}

	public static class KEY_VALUE extends StyleFilter {
		String key;
		Object value;

		public KEY_VALUE(StyleFilterOperation op, JsonNode filterNode) {
			super(op);
			key = filterNode.get(1).asText();

			JsonNode valNode = filterNode.get(2);
			if (valNode.isBoolean()) {
				value = valNode.asBoolean();
			} else if (valNode.isInt()) {
				value = valNode.asInt();
			} else if (valNode.isDouble() || valNode.isNumber()) {
				value = valNode.asDouble();
			} else {
				value = valNode.asText();
			}
		}

		public boolean passes(Feature feature) {
			Object featureVal;
			if (key.equals(SPECIAL_CLASS_KEY)) {
				featureVal = feature.getGeometry().getGeometryType();
			} else {
				featureVal = feature.getAttributes().get(key);
			}

			boolean ret = op.passes(featureVal, value);

			if (fineLogging) {
				getLogger().fine("checking " + featureVal + " vs " + value + ":" + ret);
			}

			return ret;
		}

	}

	public static class KEY_LIST extends StyleFilter {
		String key;
		HashSet<String> values;

		public KEY_LIST(StyleFilterOperation op, JsonNode filterNode) {
			super(op);

			values = new HashSet<String>();

			if (filterNode.isArray()) {
				Iterator<JsonNode> listStuff = filterNode.elements();
				// The original op is the first thing, skip it.				
				listStuff.next();
				key = listStuff.next().asText();
				while (listStuff.hasNext()) {
					String listThing = listStuff.next().asText();
					if (fineLogging) {
						getLogger().fine("KEYLIST: adding " + listThing);
					}
					values.add(listThing);
				}
			}
		}

		public boolean passes(Feature feature) {
			Object featureVal;
			if (key.equals(SPECIAL_CLASS_KEY)) {
				featureVal = feature.getGeometry().getGeometryType();
			} else {
				featureVal = feature.getAttributes().get(key);
			}

			boolean ret = op.passes(featureVal, values);

			if (fineLogging) {
				getLogger().fine("checking " + featureVal + " in " + values + ": " + ret);
			}

			return ret;
		}
	}

	public static class COMPOUND extends StyleFilter {
		Collection<StyleFilter> filters;

		public COMPOUND(StyleFilterOperation op, JsonNode filterNode) {
			super(op);
			if (fineLogging) {
				getLogger().fine("COMPOUND " + op + ", " + filterNode);
			}
			filters = new ArrayList<StyleFilter>();

			if (filterNode.isArray()) {
				Iterator<JsonNode> subFilters = filterNode.elements();
				// The original op is the first thing, skip it.
				subFilters.next();
				while (subFilters.hasNext()) {
					JsonNode subFilter = subFilters.next();
					if (fineLogging) {
						getLogger().fine(subFilter.toString());
					}
					filters.add(getForFilterNode(subFilter));
				}
			}
		}

		public boolean passes(Feature feature) {
			return op.passes(feature, filters);
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
