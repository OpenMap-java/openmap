/* 
 * <copyright>
 *  Copyright 2015 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.omGraphics.rule;

import java.util.List;
import java.util.Map;

import com.bbn.openmap.util.PropUtils;

/**
 * Implementation of Rule that uses a Map for attributes.
 *
 * @author dietrick
 */
public class MapRule extends Rule<Map<?,?>> {

    /**
     * Asks the Op class to evaluate the provided value against the Rules value.
     * 
     * @param record
     * @return true of the operation passed
     */
    public boolean evaluate(Map<?,?> record) {
        Object recVal = record.get(keyField);
        return op.evaluate(this.val, recVal);
    }

    /**
     * Returns a String of concatenated record values.
     * 
     * @param fieldNames column names
     * @param record Map to use to fetch values
     * @return String of content
     */
    public String getContent(List<String> fieldNames, Map<?,?> record) {
        StringBuffer buf = new StringBuffer();
        if (fieldNames != null) {
            for (String field : fieldNames) {
                buf.append(PropUtils.unnull(record.get(field))).append(" ");
            }
        }
        // Might be more than just that last ""
        return buf.toString().trim();
    }
}
