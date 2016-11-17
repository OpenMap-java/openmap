/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Point2D;

import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * An abstraction of MapTileCoordinateTransform that combines the identical
 * methods of OSM and TMS MapTileCoordinateTransforms.
 * 
 * @author dietrick
 */
public abstract class AbstractMapTileCoordinateTransform implements MapTileCoordinateTransform {

    int tileSize = TILE_SIZE;
    /**
     * The zoom level tile size is used by the factory to determine when it
     * needs to get tiles for a different zoom level. The default value is 350.
     * That is, when the factory is figuring out what zoom level to use, if the
     * pixel size of a tile is greater than or equal to 350 x 350, it decides to
     * check the next zoom level for retrieving tiles. This is used instead of
     * just comparing projection scales.
     */
    int DEFAULT_ZOOM_LEVEL_TILE_SIZE = 350;

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
     * Given a projection, figure out the appropriate zoom level for it. Right
     * now, 0 is totally zoomed with one tile for the entire earth. But we don't
     * return 0, we start at 1. OM can't handle one tile that covers the entire
     * earth because of the restriction for handling OMGraphics to less than
     * half of the earth.
     * 
     * @param proj
     * @return the zoom level.
     */
    public int getZoomLevelForProj(Projection proj) {
        return getZoomLevelForProj(proj, DEFAULT_ZOOM_LEVEL_TILE_SIZE);
    }
    
    /**
     * Given a projection, figure out the appropriate zoom level for it. Right
     * now, 0 is totally zoomed with one tile for the entire earth. But we don't
     * return 0, we start at 1. OM can't handle one tile that covers the entire
     * earth because of the restriction for handling OMGraphics to less than
     * half of the earth.
     * 
     * @param proj
     * @param zoomLevelTileSize used for determining zoom levels, a kind of
     *        buffer around true zoom levels since the OpenMap layers scale
     *        images.
     * @return the zoom level.
     */
    public int getZoomLevelForProj(Projection proj, int zoomLevelTileSize) {
        int low = getMinZoomLevelForProj();
        int high = getMaxZoomLevelForProj();

        int ret = low;
        for (int currentZoom = low; currentZoom <= high; currentZoom++) {
            // nearest tile to center
            Point2D nttc = latLonToTileUV(proj.getCenter(), currentZoom);

            double nttcX = Math.floor(nttc.getX());
            double nttcY = Math.floor(nttc.getY());
            Point2D originLLUL = tileUVToLatLon(new Point2D.Double(nttcX, nttcY), currentZoom);
            Point2D originLLLR = tileUVToLatLon(new Point2D.Double(nttcX + 1, nttcY + 1), currentZoom);

            Point2D projUVUL = proj.forward(originLLUL);
            Point2D projLLLR = proj.forward(originLLLR);

            if (Math.abs(projUVUL.getX() - projLLLR.getX()) <= zoomLevelTileSize) {
                return currentZoom;
            }

            /*
             * Used to try to do this with scale comparisons, now just look at
             * tile sizes. float diff = currentScale - scales[currentZoom]; if
             * (diff > 0) { return currentZoom + 1; }
             */
        }

        return ret;
    }

    /**
     * Returns the minimum zoom level for calculating the appropriate zoom
     * level.
     * 
     * @return minimum zoom level
     */
    protected int getMinZoomLevelForProj() {
        return 1;
    }

    /**
     * Returns the maximum zoom level for calculating the appropriate zoom
     * level.
     * 
     * @return maximum zoom level
     */
    protected int getMaxZoomLevelForProj() {
        return 20;
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
