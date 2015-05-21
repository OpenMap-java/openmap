/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

/**
 * A where clause segment, describing parameters for an attribute to pass a
 * select test.
 * 
 * @author dietrick
 */
public class WhereRuleImpl implements WhereRule {

    public String getWhereClauseSegment(FeatureQuery featureQuery, ProjectionInfo projInfo)
            throws QueryException {
        return null;
    }
}
