/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Point2D;

import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * An abstraction of MapTileCoordinateTransform that combines the identical
 * methods of OSM and TMS MapTileCoordinateTransforms.
 * 
 * @author dietrick
 */
public abstract class AbstractMapTileCoordinateTransform implements MapTileCoordinateTransform {

    int tileSize = TILE_SIZE;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform#latLonToTileUV
     * (java.awt.geom.Point2D, int)
     */
    public Point2D latLonToTileUV(Point2D latlon, int zoom) {
        return latLonToTileUV(latlon, zoom, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform#tileUVToLatLon
     * (java.awt.geom.Point2D, int)
     */
    public Point2D tileUVToLatLon(Point2D tileUV, int zoom) {
        return tileUVToLatLon(tileUV, zoom, null);
    }

    /**
     * Return a scale value for the transforming projection, given a discrete
     * zoom level.
     * 
     * @param zoom level
     * @return scale value.
     */
    public float getScaleForZoom(int zoom) {
        Projection proj = new Mercator(new LatLonPoint.Double(), 1000000, TILE_SIZE, TILE_SIZE);
        return getScaleForZoomAndProjection(proj, zoom);
    }

    /**
     * Get the scale value for a Projection and discrete zoom level.
     * 
     * @param proj the projection to use for scale calculations.
     * @param zoom the discrete zoom level.
     * @return scale value for the given projection.
     */
    public float getScaleForZoomAndProjection(Projection proj, int zoom) {
        MapTileCoordinateTransform mtct = new OSMMapTileCoordinateTransform();
        Point2D originLLUL = mtct.tileUVToLatLon(new Point2D.Double(0.0, 0.0), zoom);
        Point2D originLLLR = mtct.tileUVToLatLon(new Point2D.Double(1.0, 1.0), zoom);
        return proj.getScale(originLLUL, originLLLR, UVUL, UVLR);
    }

    /**
     * Creates an array of scale values for different zoom levels. Make sure you
     * don't reference the array outside of 0 and high zoom levels. There will
     * be a high zoom level number of items in the array.
     * 
     * @param proj
     * @param highZoomLevel
     * @return array, initialized for the low zoom level index to the high zoom
     *         level index.
     */
    public float[] getScalesForZoomLevels(Projection proj, int highZoomLevel) {
        float[] ret = new float[highZoomLevel + 1];
        for (int i = 0; i <= highZoomLevel; i++) {
            ret[i] = getScaleForZoomAndProjection(proj, i);
        }
        return ret;
    }
    
    /**
     * Returns the tile size of the transform.
     */
    public int getTileSize() {
        return tileSize;
    }
}
