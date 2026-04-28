package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Helper functions for dealing with nodes.
 * 
 * @author dietrick
 *
 */
public class StyleNode {

	public static String getAsText(JsonNode node, String fieldName, String dflt) {
		try {
			return node.get(fieldName).asText();
		} catch (NullPointerException npe) {
			return dflt;
		}
	}

	public static int getAsInt(JsonNode node, String fieldName, int dflt) {
		try {
			return node.get(fieldName).asInt();
		} catch (NullPointerException npe) {
			return dflt;
		}
	}

	public static double getAsDouble(JsonNode node, String fieldName, double dflt) {
		try {
			return node.get(fieldName).asDouble();
		} catch (NullPointerException npe) {
			return dflt;
		}
	}

	public static boolean getAsBoolean(JsonNode node, String fieldName, boolean dflt) {
		try {
			return node.get(fieldName).asBoolean();
		} catch (NullPointerException npe) {
			return dflt;
		}
	}

	public static long getAsLong(JsonNode node, String fieldName, long dflt) {
		try {
			return node.get(fieldName).asLong();
		} catch (NullPointerException npe) {
			return dflt;
		}
	}

	public static void prettyOut(JsonNode node, String prefix) {
		Iterator<Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> field = fields.next();
			String key = field.getKey();
			if (key.equalsIgnoreCase("id")) {
				System.out.println("");
			}

			JsonNode value = field.getValue();
			System.out.println(prefix + key + ": " + value);
			if (value.isArray()) {
				prettyOutArray(value, prefix + "  ");
			} else {
				prettyOut(value, prefix + "  ");
			}
		}
	}

	public static void prettyOutArray(JsonNode node, String prefix) {
		Iterator<JsonNode> children = node.elements();
		while (children.hasNext()) {
			JsonNode child = children.next();
			if (child.isArray()) {
				prettyOutArray(child, prefix + "  ");
			} else {
				prettyOut(child, prefix + "  ");
			}
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
		private static final Logger LOGGER = Logger.getLogger(StyleNode.class.getName());

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
