/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

/**
 * A where clause segment, describing parameters for an attribute to pass a select test.
 * 
 * @author dietrick
 */
public interface WhereRule {

    /**
     * Return a String to be passed into the Statement query as a condition for evaluating geometries or attributes.
     * @param featureQuery can be used to get srid, geometry tables or geometry column names.
     * @param projInfo can be used to get information about the projection.
     * @return the String to be inserted into the where clause.
     * @throws QueryException if the where clause fails based on the featureQuery or projInfo settings. 
     */
    String getWhereClauseSegment(FeatureQuery featureQuery, ProjectionInfo projInfo) throws QueryException;

}
