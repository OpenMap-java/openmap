package com.bbn.openmap.dataAccess.mapTile.mb;

import java.util.Collection;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import no.ecc.vectortile.VectorTileDecoder.Feature;

public enum StyleFilterOperation {

	EQUALS("==") {
		public boolean passes(Object ths, Object tht) {
			return (ths == null && tht == null) || (ths != null && ths.equals(tht));
		}
	},
	NOT_EQUALS("!=") {
		public boolean passes(Object ths, Object tht) {
			return (ths == null && tht != null) || (ths != null && tht == null) || (!ths.equals(tht));
		}
	},
	GREATER_THAN(">") {
		public boolean passes(Object number1, Object number2) {
			try {
				return ((Number) number1).doubleValue() > ((Number) number2).doubleValue();
			} catch (ClassCastException cce) {
				getLogger().warning("Non-numerical classes used as input");
				return false;
			}
		}
	},
	GREATER_THAN_EQUALS(">=") {
		public boolean passes(Object ths, Object tht) {
			return GREATER_THAN.passes(ths, tht) || EQUALS.passes(ths, tht);
		}
	},
	LESS_THAN("<") {
		public boolean passes(Object number1, Object number2) {
			try {
				return ((Number) number1).doubleValue() < ((Number) number2).doubleValue();
			} catch (ClassCastException cce) {
				getLogger().warning("Non-numerical classes used as input");				
				return false;
			}
		}
	},
	LESS_THAN_EQUALS("<=") {
		public boolean passes(Object ths, Object tht) {
			return LESS_THAN.passes(ths, tht) || EQUALS.passes(ths, tht);
		}
	},
	IN("in") {
		public boolean passes(Object ths, Object tht) {
			try {
				return tht != null && ((Collection<String>) tht).contains(ths);
			} catch (ClassCastException cce) {
				getLogger().warning("Non-collection classes used as input");				
				return false;
			}
		}
	},
	NOT_IN("!in") {
		public boolean passes(Object ths, Object tht) {
			try {
				return tht == null || (ths != null && !((Collection<String>) tht).contains(ths));
			} catch (ClassCastException cce) {
				getLogger().warning("Non-collection classes used as input");				
				return true;
			}
		}
	},
	ALL("all") {
		public boolean passes(Object feature, Object collectionOfStyleFeatures) {
			try {
				boolean pass = true;
				Collection<StyleFilter> lsf = (Collection<StyleFilter>) collectionOfStyleFeatures;
				for (StyleFilter op : lsf) {
					pass &= op.passes(((Feature) feature));
				}
				return pass;
			} catch (ClassCastException cce) {
				getLogger().warning("Non-collection classes used as input");
				return false;
			}
		}
	},
	ANY("any") {
		public boolean passes(Object feature, Object collectionOfStyleFeatures) {
			try {
				boolean pass = false;
				Collection<StyleFilter> lsf = (Collection<StyleFilter>) collectionOfStyleFeatures;
				for (StyleFilter op : lsf) {
					pass |= op.passes(((Feature) feature));
				}
				return pass;
			} catch (ClassCastException cce) {
				getLogger().warning("Non-collection classes used as input");								
				return false;
			}
		}
	},
	NONE("none") {
		public boolean passes(Object feature, Object listOfStyleFeatures) {
			return !ANY.passes(feature, listOfStyleFeatures);
		}
	},
	NOTHING("") {
		public boolean passes(Object ths, Object tht) {
			return true;
		}
	};

	private String name;

	private StyleFilterOperation(String name) {
		this.name = name;
	}

	protected static StyleFilterOperation getForFilterNode(JsonNode filterNode) {
		return getForOpString(filterNode.asText());
	}

	protected static StyleFilterOperation getForOpString(String nm) {
		for (StyleFilterOperation st : StyleFilterOperation.values()) {
			if (st.name.equalsIgnoreCase(nm)) {
				return st;
			}
		}
		return NOTHING;
	}

	public String getName() {
		return name;
	}
	
	protected abstract boolean passes(Object ths, Object tht);

	/*
	 * ["==",key,value]equality:key=value["!=",key,value]inequality:key≠value[
	 * ">",key,value]
	 * 
	 * greater than:key>value[">=",key,value] greater than or
	 * equal:key≥value["<",key,value] less than:key<value["<=",key,value] less
	 * than or equal:key≤value["in",key,v0,...,vn] set inclusion:key∈
	 * 
	 * {v0, ..., vn}["!in",key,v0,...,vn]
	 * 
	 * set exclusion:key∉
	 * 
	 * {v0, ..., vn}["all",f0,...,fn]
	 * 
	 * logical AND:f0∧...∧fn["any",f0,...,fn] logical
	 * OR:f0∨...∨fn["none",f0,...,fn] logical NOR:¬f0∧...∧¬fn
	 */
	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(StyleFilterOperation.class.getName());

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
