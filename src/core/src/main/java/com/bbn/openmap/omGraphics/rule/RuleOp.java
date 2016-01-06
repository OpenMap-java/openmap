/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.omGraphics.rule;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * The RuleOp class (operation) is used by the Rules to evaluate a rule key
 * value against a OMGraphics key value.
 * 
 * @author dietrick
 */
public enum RuleOp {

    /**
     * equals: equals
     */
    EQUALS("equals", "equals") {
        public boolean compare(int kvcr) {
            return kvcr == 0;
        }
    },
    /**
     * lt: less than
     */
    LESS_THAN("less than", "lt") {
        public boolean compare(int kvcr) {
            return kvcr > 0;
        }
    },
    /**
     * lte: less than or equals
     */
    LESS_THAN_EQUALS("less than or equals", "lte") {
        public boolean compare(int kvcr) {
            return kvcr == 0 || kvcr > 0;
        }
    },
    /**
     * gt: greater than
     */
    GREATER_THAN("greater than", "gt") {
        public boolean compare(int kvcr) {
            return kvcr < 0;
        }
    },
    /**
     * gte: greater than or equals
     */
    GREATER_THAN_EQUALS("greater than or equals", "gte") {
        public boolean compare(int kvcr) {
            return kvcr == 0 || kvcr < 0;
        }
    },
    /**
     * ne: not equals
     */
    NOT_EQUALS("not equals", "ne") {
        public boolean compare(int kvcr) {
            return kvcr != 0;
        }
    },
    /**
     * noop: no-op (nothing passes rule)
     */
    NONE("no-op", "noop") {
        public boolean compare(int kvcr) {
            return false;
        }
    },
    /**
     * all: all (everything passes rule)
     */
    ALL("all", "all") {
        public boolean compare(int kvcr) {
            return true;
        }
    },
    /**
     * starts: starts with
     */
    STARTS_WITH("starts with", "starts") {
        public boolean compare(int kvcr) {
            return kvcr == 0;
        }

        public boolean evaluate(Object key, Object val) {
            return (val.toString()).startsWith(key.toString());
        }
    },
    /**
     * ends: ends with
     */
    ENDS_WITH("ends with", "ends") {
        public boolean compare(int kvcr) {
            return kvcr == 0;
        }

        public boolean evaluate(Object key, Object val) {
            return (val.toString()).endsWith(key.toString());
        }
    };

    protected String description;
    protected String propertyNotation;

    private RuleOp(String desc, String propNotation) {
        this.description = desc;
        this.propertyNotation = propNotation;
    }

    public boolean evaluate(Object key, Object val) {
        if (key == null) {
            return compare(-1);
        }

        if (val instanceof Number) {
            if (!(key instanceof Double)) {
                java.text.DecimalFormat df = new java.text.DecimalFormat();
                DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);
                df.setDecimalFormatSymbols(dfs);
                try {
                    key = new Double(df.parse(key.toString()).doubleValue());
                } catch (java.text.ParseException pe) {
                    return compare(-1);
                }
            }

            return compare(((Double) key).compareTo(((Number) val).doubleValue()));
        }

        return compare(((String) key.toString()).compareTo(val.toString()));
    }

    public abstract boolean compare(int keyValcompareResult);

    public static RuleOp resolve(String opString) {
        if (opString != null) {
            for (RuleOp op : values()) {
                if (op.propertyNotation.equalsIgnoreCase(opString)) {
                    return op;
                }
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public String getPropertyNotation() {
        return propertyNotation;
    }
}
