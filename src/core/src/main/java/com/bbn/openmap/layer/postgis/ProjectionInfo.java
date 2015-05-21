/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

import java.awt.geom.Point2D;

import com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform;
import com.bbn.openmap.proj.Projection;

/**
 * Handy class that contains different projection information.
 * 
 * @author dietrick
 */
public class ProjectionInfo {

    Projection projection;
    String sridBounds;
    int zoomLevel;

    public ProjectionInfo(Projection proj) {
        this.projection = proj;

        Point2D upperLeft = proj.getUpperLeft();
        Point2D lowerRight = proj.getLowerRight();

        sridBounds = "ST_SetSRID('BOX3D(" + upperLeft.getX() + " " + lowerRight.getY() + ","
                + lowerRight.getX() + " " + upperLeft.getY() + ")'::box3d,4326)";
        
        zoomLevel = -1;
    }

    /**
     * Returns the projection object currently being handled by the layer.
     * 
     * @return the projection
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * Returns the srid bounds string that can be used for PostGIS spatial
     * filtering, looks something like:
     * 
     * <pre>
     * ST_SetSRID('BOX3D(upperLeftLon lowerRightLat,lowerRightLon upperLeftLat)'::box3d,4326)
     * </pre>
     * 
     * @return the sridBounds
     */
    public String getSridBounds() {
        return sridBounds;
    }

    /**
     * Return the zoom level that the projection represents.
     * 
     * @return the zoomLevel
     */
    public int getZoomLevel() {
        if (zoomLevel < 0) {
            zoomLevel = new OSMMapTileCoordinateTransform().getZoomLevelForProj(projection);
        }
        
        return zoomLevel;
    }

}
