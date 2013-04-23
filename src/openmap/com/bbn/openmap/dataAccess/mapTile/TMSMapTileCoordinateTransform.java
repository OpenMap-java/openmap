/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Point2D;

import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.proj.coords.MercatorUVGCT;

/**
 * The implementation of TileCoordinateTransformation for Tile Map Service (TMS)
 * tile coordinate notations.
 * 
 * @author dietrick
 */
public class TMSMapTileCoordinateTransform extends AbstractMapTileCoordinateTransform {

    /**
     * @param latlon a Point2D whose x component is the longitude and y
     *        component is the latitude
     * @param zoom Tile Map Service (TMS) style zoom level (0-19 usually)
     * @param ret LatLonPoint to fill and return, to avoid allocating return
     *        object.
     * @return The "tile number" whose x and y components each are floating
     *         point numbers that represent the distance in number of tiles from
     *         the origin of the whole map at this zoom level. At zoom=0, the
     *         lat,lon point of 0,0 maps to 0.5,0.5 since there is only one tile
     *         at zoom level 0.
     */
    public Point2D latLonToTileUV(Point2D latlon, int zoom, Point2D ret) {
        if (ret == null) {
            ret = new Point2D.Double();
        }

        ret.setLocation(((latlon.getX() + 180.0) / 360.0 * Math.pow(2.0, zoom)), Math.pow(2.0, zoom)
                - ((1.0 - Math.log(Math.tan(latlon.getY() * Math.PI / 180.0)
                        + (1.0 / Math.cos(latlon.getY() * Math.PI / 180.0)))
                        / Math.PI) / 2.0 * (Math.pow(2.0, zoom))));

        return ret;
        // Much slower!
        // transform.setZoomLevel(zoom);
        // return transform.forward(latlon.getY(), latlon.getX(), ret);
    }

    /**
     * @param tileUV a Point2D whose x,y coordinates represent the distance in
     *        number of tiles (each 256x256) from the origin (where the origin
     *        is -90lat,-180lon)
     * @param zoom Tile Map Service (TMS) style zoom level (0-19 usually)
     * @param ret LatLonPoint to fill and return, to avoid allocating return
     *        object.
     * @return a Point2D whose x coordinate is the longitude and y coordinate is
     *         the latitude
     */
    public LatLonPoint tileUVToLatLon(Point2D tileUV, int zoom, LatLonPoint ret) {
        if (ret == null) {
            ret = new LatLonPoint.Double();
        }

        ret.setLocation(360.0 / Math.pow(2.0, zoom) * tileUV.getX() - 180.0, -90.0
                + 360.0
                / Math.PI
                * Math.atan(Math.exp((-2.0 * Math.PI * (-(tileUV.getY() - Math.pow(2.0, zoom))))
                        / Math.pow(2.0, zoom) + Math.PI)));

        return ret;

        // Much slower!
        // transform.setZoomLevel(zoom);
        // return transform.inverse(tileUV.getX(), tileUV.getY(), ret);
    }

    /**
     * Given a projection, provide the upper, lower, left and right tile
     * coordinates that cover the projection area.
     * 
     * @param upperLeft lat/lon coordinate of upper left corner of bounding box.
     * @param lowerRight lat/lon coordinate of lower right corner of bounding
     *        box.
     * @param zoomLevel zoom level of desired tiles.
     * @return int[], in top, left, bottom and right order.
     */
    public int[] getTileBoundsForProjection(Point2D upperLeft, Point2D lowerRight, int zoomLevel) {

        Point2D uvul = latLonToTileUV(upperLeft, zoomLevel);
        Point2D uvlr = latLonToTileUV(lowerRight, zoomLevel);

        int[] ret = new int[4];

        int uvleft = (int) Math.floor(uvul.getX());
        int uvright = (int) Math.ceil(uvlr.getX());
        int uvbottom = (int) Math.floor(uvlr.getY()) - 1;
        if (uvbottom < 0) {
            uvbottom = 0;
        }
        int uvup = (int) Math.ceil(uvul.getY()) + 1;

        ret[0] = uvup;
        ret[1] = uvleft;
        ret[2] = uvbottom;
        ret[3] = uvright;

        return ret;
    }

    public static void main(String[] args) {
        for (int i = 9; i < 15; i++) {
            System.out.println("Zoom Level " + i);
            TMSMapTileCoordinateTransform tms = new TMSMapTileCoordinateTransform();

            LatLonPoint llp = new LatLonPoint.Double(0.0, 0.0);
            Point2D uv = tms.latLonToTileUV(llp, i);
            System.out.println(" " + llp + " transformed to " + uv);
            llp = tms.tileUVToLatLon(uv, i, llp);
            System.out.println(" " + uv + " transformed to " + llp);

            llp = new LatLonPoint.Double(41.389, 2.169);
            uv = tms.latLonToTileUV(llp, i);
            System.out.println(" " + llp + " transformed to " + uv);
            llp = tms.tileUVToLatLon(uv, i, llp);
            System.out.println(" " + uv + " transformed to " + llp);
        }

    }

    /**
     * @return if y coordinates for tiles increase as pixel values increase.
     */
    public boolean isYDirectionUp() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform#getTransform
     * ()
     */
    public GeoCoordTransformation getTransform(int zoomLevel) {
        return new MercatorUVGCT.TMS(zoomLevel);
    }
}
