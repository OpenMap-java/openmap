package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class StyleSource {
	
	StyleSourceType type;
	String url;
	
	public StyleSource(JsonNode sourceNode) {
		
	}
	
	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(StyleSource.class.getName());

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
