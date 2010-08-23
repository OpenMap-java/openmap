package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;

/**
 * Convert between mercator uv coordinates that are used for map tiles and
 * lat/lon degrees. Setting the zoom level sets the number of expected pixels in
 * the Mercator uv projection in both directions.
 */
public abstract class MercatorUVGCT
      extends AbstractGCT
      implements GeoCoordTransformation {

   int zoomLevel;

   protected MercatorUVGCT(int zoomLevel) {
      this.zoomLevel = zoomLevel;
   }

   public abstract Point2D forward(double lat, double lon, Point2D ret);

   public abstract LatLonPoint inverse(double uvx, double uvy, LatLonPoint ret);

   public int getZoomLevel() {
      return zoomLevel;
   }

   public void setZoomLevel(int zoomLevel) {
      this.zoomLevel = zoomLevel;
   }

   public static class OSM
         extends MercatorUVGCT {

      public OSM(int zoomLevel) {
         super(zoomLevel);
      }

      public Point2D forward(double lat, double lon, Point2D ret) {
         if (ret == null) {
            ret = new Point2D.Double();
         }

         ret.setLocation(((lon + 180.0) / 360.0 * Math.pow(2.0, zoomLevel)),
                         ((1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + (1.0 / Math.cos(lat * Math.PI / 180.0))) / Math.PI) / 2.0 * (Math.pow(2.0,
                                                                                                                                                  zoomLevel))));
         return ret;
      }

      public LatLonPoint inverse(double uvx, double uvy, LatLonPoint ret) {
         if (ret == null) {
            ret = new LatLonPoint.Double();
         }

         ret.setLocation(360.0 / Math.pow(2.0, zoomLevel) * uvx - 180.0,
                         -90.0 + 360.0 / Math.PI * Math.atan(Math.exp((-2.0 * Math.PI * uvy) / Math.pow(2.0, zoomLevel) + Math.PI)));
         return ret;
      }

   }

   public static class TMS
         extends MercatorUVGCT {

      public TMS(int zoomLevel) {
         super(zoomLevel);
      }

      public Point2D forward(double lat, double lon, Point2D ret) {
         if (ret == null) {
            ret = new Point2D.Double();
         }

         ret.setLocation(((lon + 180.0) / 360.0 * Math.pow(2, zoomLevel)),
                         Math.pow(2.0, zoomLevel)
                               - ((1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + (1.0 / Math.cos(lat * Math.PI / 180.0)))
                                     / Math.PI) / 2.0 * (Math.pow(2.0, zoomLevel))));
         return ret;
      }

      public LatLonPoint inverse(double uvx, double uvy, LatLonPoint ret) {
         if (ret == null) {
            ret = new LatLonPoint.Double();
         }

         ret.setLocation(360 / Math.pow(2, zoomLevel) * uvx - 180.0,
                         -90.0
                               + 360.0
                               / Math.PI
                               * Math.atan(Math.exp((-2.0 * Math.PI * (-(uvy - Math.pow(2.0, zoomLevel))))
                                     / Math.pow(2.0, zoomLevel) + Math.PI)));

         return ret;
      }

   }

}
