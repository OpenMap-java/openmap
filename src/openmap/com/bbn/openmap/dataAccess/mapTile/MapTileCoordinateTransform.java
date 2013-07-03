/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Point2D;

import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * The TileCoordinateTransform is an object that knows how to translate lat/lon
 * coordinates to UV tile coordinates for map tiles. Its an interface because
 * there seem to be two accepted ways to divide up frames over the Earth. The
 * Tile Map Service (TMS) tile system divide the Earth with the origin at the
 * bottom left, OpenStreetMap has the origin at the top left.
 * 
 * @author dietrick
 */
public interface MapTileCoordinateTransform {

   public final static int TILE_SIZE = 256;
   public final static Point2D UVUL = new Point2D.Double(0, 0);
   public final static Point2D UVLR = new Point2D.Double(TILE_SIZE, TILE_SIZE);

   public Point2D latLonToTileUV(Point2D latlon, int zoom);

   public Point2D latLonToTileUV(Point2D latlon, int zoom, Point2D ret);

   /**
    * @param tileUV a Point2D whose x,y coordinates represent the distance in
    *        number of tiles (each 256x256) from the origin (where the origin is
    *        90lat,-180lon)
    * @param zoom Tile Map Service (TMS) style zoom level (0-19 usually)
    * @return a Point2D whose x coordinate is the longitude and y coordinate is
    *         the latitude
    */
   public Point2D tileUVToLatLon(Point2D tileUV, int zoom);

   public LatLonPoint tileUVToLatLon(Point2D tileUV, int zoom, LatLonPoint ret);

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
   public int[] getTileBoundsForProjection(Point2D upperLeft, Point2D lowerRight, int zoomLevel);

   /**
    * @return if y coordinates for tiles increase as pixel values increase.
    */
   public boolean isYDirectionUp();

   /**
    * The coordinate transformation object used for lat/lon uv conversions.
    * 
    * @return transform appropriate for a particular zoom level.
    */
   public GeoCoordTransformation getTransform(int zoomLevel);
   
   /**
    * Return a scale value for the transforming projection, given a discrete zoom level.
    * @param zoom level
    * @return scale value.
    */
   public float getScaleForZoom(int zoom);

   /**
    * Get the scale value for a Projection and discrete zoom level.
    * @param proj the projection to use for scale calculations.
    * @param zoom the discrete zoom level.
    * @return scale value for the given projection.
    */
   public float getScaleForZoomAndProjection(Projection proj, int zoom);

   /**
    * Creates an array of scale values for different zoom levels. Make sure you
    * don't reference the array outside of 0 and high zoom levels. There
    * will be a high zoom level number of items in the array.
    * 
    * @param proj
    * @param highZoomLevel
    * @return array, initialized for the 0 zoom level index to the high zoom
    *         level index.
    */
   public float[] getScalesForZoomLevels(Projection proj, int highZoomLevel);
   
   /**
    * @return the pixel size for tiles for this transform.
    */
   public int getTileSize();
   
}
