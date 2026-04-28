/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.postgis.PGgeometry;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * The object that builds a query for a particular feature type, and also
 * contains information about how to render it.
 * 
 * @author dietrick
 */
public class FeatureQuery {

    String geomSRID = "900913";
    String geomColumnName;
    String geomTableName;

    List<String> columnNames = new ArrayList<String>();
    Map<String, Class> columnTypeMap = new HashMap<String, Class>();
    String queryColumnString = null;
    DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

    List<WhereRule> whereRules = new ArrayList<WhereRule>();

    public FeatureQuery(String geometryTableName, String geometryColumnName, String geometrySrid) {
        this.geomSRID = geometrySrid;
        this.geomColumnName = geometryColumnName;
        this.geomTableName = geometryTableName;
    }

    /**
     * Add a query rule that affects which geometries and attributes are
     * returned.
     * 
     * @param rule FeatureQueryRule
     * @return this FeatureQuery, so addition of rules can be stacked on each
     *         other and on the constructor.
     */
    public FeatureQuery addWhereRule(WhereRule rule) {
        whereRules.add(rule);
        return this;
    }

    /**
     * 
     * @param projInfo which will be in 4326 projection and specified as
     *        such, i.e. something like: ST_SetSRID('BOX3D(" + upperLeft.getX()
     *        + " " + lowerRight.getY() + "," + lowerRight.getX() + " " +
     *        upperLeft.getY() + ")'::box3d,4326)
     * @return the select command for retrieving this feature, for a particular
     *         bounds if specified.
     * @throws QueryException if where clauses detect a problem
     */
    protected String getQuery(ProjectionInfo projInfo) throws QueryException {
        StringBuilder ret = new StringBuilder("select");

        // tell the query to transform successful results back to 4326 for
        // conversion to OMGraphics
        ret.append(" ST_Transform(").append(geomColumnName).append(",4326)");

        if (queryColumnString != null) {
            ret.append(queryColumnString);
        }

        ret.append(" from ").append(geomTableName);

        if (whereRules != null && !whereRules.isEmpty()) {
            StringBuilder ruleString = new StringBuilder();

            for (WhereRule rule : whereRules) {
                String queryCondition = rule.getWhereClauseSegment(this, projInfo);

                if (queryCondition != null && !queryCondition.isEmpty()) {

                    if (ruleString.length() == 0) {
                        ruleString.append(" where ");
                    } else {
                        ruleString.append(" and ");
                    }

                    ruleString.append("(").append(queryCondition).append(")");
                }
            }

            ret.append(ruleString);
        }

        /*
         * When all put together, it should look like this:
         * "select ST_Transform( " + geomColumn + ", 4326), osm_id from " +
         * geomTable + " where (" + geomColumn + " && " + spatialQueryString +
         * " = true) and (NOT planet_osm_roads.highway is NULL);";
         */

        return ret.append(";").toString();
    }

    /**
     * The main call into the FeatureQuery to gather OMGraphics from the
     * database.
     * 
     * @param stmt the sql Statement class;
     * @param projInfo describes the current projection.
     * @return OMGraphicList containing the results from the query.
     * @throws SQLException
     * @throws QueryException if where clauses detect a problem
     */
    public OMGraphicList queryDatabaseAndMakeOMGraphics(Statement stmt, ProjectionInfo projInfo)
            throws SQLException, QueryException {

        String query = getQuery(projInfo);

        System.out.println(query);

        stmt.executeQuery(query);
        ResultSet rs = stmt.getResultSet();
        int resultCount = 0;
        PostGISOMGraphicFactory factory = new PostGISOMGraphicFactory(projInfo.getProjection(), drawingAttributes);
        OMGraphicList results = new OMGraphicList();

        while (rs.next()) {
            PGgeometry geom = (PGgeometry) rs.getObject(1);
            results.add(factory.transformGeometryToOM(geom.getGeometry()));
            resultCount++;
        }

        rs.close();
        System.out.println("feature query resulted in hits: " + resultCount);

        return results;
    }

    /**
     * @return the drawingAttributes used to decorate the results of this
     *         feature query.
     */
    public DrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    /**
     * Set the drawing attributes used to decorate the OMGraphics for this
     * FeatureQuery.
     * 
     * @param drawingAttributes the drawingAttributes to set
     */
    public void setDrawingAttributes(DrawingAttributes drawingAttributes) {
        this.drawingAttributes = drawingAttributes;
    }

}
