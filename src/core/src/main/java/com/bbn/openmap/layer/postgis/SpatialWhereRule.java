/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

/**
 * Creates a spatial query part of a PostGIS where clause rule in the select statement. Assumes the
 * {@link FeatureQuery#geomSRID} and {@link FeatureQuery#geomColumnName} are
 * not null.
 * 
 * @author dietrick
 */
public class SpatialWhereRule implements WhereRule {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.layer.postgis.FeatureQueryRule#getQueryCondition(com.
     * bbn.openmap.layer.postgis.FeatureQuery,
     * com.bbn.openmap.layer.postgis.ProjectionInfo)
     */
    public String getWhereClauseSegment(FeatureQuery featureQuery, ProjectionInfo projInfo) throws QueryException {
        String sridBounds = projInfo.getSridBounds();

        if (sridBounds != null && !featureQuery.geomSRID.equalsIgnoreCase("4326")) {
            // If the database contents are in a different coordinate
            // system, we need to transform the bounding box to match.
            StringBuilder boundsBuilder = new StringBuilder("ST_Transform(");
            boundsBuilder.append(sridBounds);
            boundsBuilder.append(",").append(featureQuery.geomSRID).append(")");
            sridBounds = boundsBuilder.toString();
        }

        if (sridBounds != null) {
            // geometry column contents && (overridden operator matching
            // bounds), this does the PostGIS bounds filtering
            return new StringBuilder(featureQuery.geomColumnName).append(" && ").append(sridBounds).append(" = true").toString();
        }

        return null;
    }

}
