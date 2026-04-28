/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

/**
 * A simple fixed where clause rule that returns the String used in the
 * constructor.
 * 
 * @author dietrick
 */
public class StringWhereRule implements WhereRule {

    String stringRule;

    public StringWhereRule(String stringRule) {
        this.stringRule = stringRule;
    }

    public String getWhereClauseSegment(FeatureQuery featureQuery, ProjectionInfo projInfo)
            throws QueryException {
        return stringRule;
    }
}
