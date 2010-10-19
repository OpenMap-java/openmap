/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ArgParser;

/**
 * A utility class to help manage tile trees. Use the builders to configure and
 * launch the MapTileUtil.
 * 
 * @author dietrick
 */
public class MapTileUtil {

   static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile");

   public final static String SOURCE_PROPERTY = "source";
   public final static String BOUNDS_PROPERTY = "bounds";
   public final static String DESTINATION_PROPERTY = "destination";
   public final static String IMAGEFORMAT_PROPERTY = "format";
   public final static String ZOOMLEVEL_PROPERTY = "zoom";

   public final static int ZOOM_LEVELS = 21;

   MapTileCoordinateTransform mtcTransform;
   String source;
   String destination;
   String format;
   List<double[]> boundsList;
   boolean[] zoomLevels;

   protected MapTileUtil(Builder builder) {
      source = builder.source;
      destination = builder.destination;
      format = builder.format;
      boundsList = builder.boundsList;
      zoomLevels = builder.zoomLevels;
      mtcTransform = builder.mtcTransform;
   }

   /**
    * Figure out which tiles need action, based on settings. Then call action
    * for each tile on the builder.
    * 
    * @param builder
    */
   protected void grabTiles(Builder builder) {

      if (boundsList == null) {
         boundsList = new ArrayList<double[]>();
         boundsList.add(new double[] {
            80,
            -180,
            -80,
            180
         });
      }

      for (int i = 0; i < ZOOM_LEVELS; i++) {

         // Check the zoom level. If they aren't specified, only do 0-14
         if (zoomLevels == null) {
            if (i > 14)
               continue;
         } else {
            if (!zoomLevels[i]) {
               continue;
            }
         }

         for (double[] bounds : boundsList) {

            int[] uvBounds =
                  mtcTransform.getTileBoundsForProjection(new LatLonPoint.Double(bounds[0], bounds[1]),
                                                          new LatLonPoint.Double(bounds[2], bounds[3]), i);
            int uvup = uvBounds[0];
            int uvleft = uvBounds[1];
            int uvbottom = uvBounds[2];
            int uvright = uvBounds[3];

            int uvleftM = (int) Math.min(uvleft, uvright);
            int uvrightM = (int) Math.max(uvleft, uvright);
            int uvupM = (int) Math.min(uvbottom, uvup);
            int uvbottomM = (int) Math.max(uvbottom, uvup);

            for (int x = uvleftM; x < uvrightM; x++) {
               for (int y = uvupM; y < uvbottomM; y++) {
                  builder.action(x, y, i, this);
               }
            }
         }
      }
   }

   /**
    * An action method that will fetch a tile from a URL and copy it to the
    * destination directory.
    * 
    * @param x
    * @param y
    * @param zoomLevel
    */
   protected void grabURLTile(int x, int y, int zoomLevel) {

      java.net.URL url = null;
      ImageIcon ii = null;

      String imagePath = source + "/" + zoomLevel + "/" + x + "/" + y + (format.startsWith(".") ? format : "." + format);

      try {

         url = new java.net.URL(imagePath);
         java.net.HttpURLConnection urlc = (java.net.HttpURLConnection) url.openConnection();

         if (logger.isLoggable(Level.FINER)) {
            logger.finer("url content type: " + urlc.getContentType());
         }

         if (urlc == null) {
            logger.warning("unable to connect to " + imagePath);
            return;
         }

         if (urlc.getContentType().startsWith("image")) {

            InputStream in = urlc.getInputStream();
            // ------- Testing without this
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int buflen = 2048; // 2k blocks
            byte buf[] = new byte[buflen];
            int len = -1;
            while ((len = in.read(buf, 0, buflen)) != -1) {
               out.write(buf, 0, len);
            }
            out.flush();
            out.close();

            byte[] imageBytes = out.toByteArray();
            ii = new ImageIcon(imageBytes);

            if (destination != null) {
               File localFile =
                     new File(destination + "/" + zoomLevel + "/" + x + "/" + y + (format.startsWith(".") ? format : "." + format));

               File parentDir = localFile.getParentFile();
               parentDir.mkdirs();

               FileOutputStream fos = new FileOutputStream(localFile);
               fos.write(imageBytes);
               fos.flush();
               fos.close();
            }

         } // end if image
      } catch (java.net.MalformedURLException murle) {
         logger.warning("WebImagePlugIn: URL \"" + imagePath + "\" is malformed.");
      } catch (java.io.IOException ioe) {
         logger.warning("Couldn't connect to " + imagePath + "Connection Problem");
      }

   }

   /**
    * For instance...
    * @param args
    */
   public static void main(String[] args) {

      new URLGrabber("http://tah.openstreetmap.org/Tiles/tile", "/data/tiles").addZoomRange(0, 14).go();
   }

   /**
    * A generic Builder that handles most configuration issues for the
    * MapTileUtil. Extend to make MTU do what you want by overriding go and
    * action.
    * 
    * @author dietrick
    */
   public abstract static class Builder {
      String source;
      String destination;

      // Optional
      String format = "png";
      List<double[]> boundsList;
      boolean[] zoomLevels; // 0-20
      MapTileCoordinateTransform mtcTransform = new OSMMapTileCoordinateTransform();

      public Builder(String source, String destination) {
         this.source = source;
         this.destination = destination;
      }

      public Builder addBounds(double ulat, double llon, double llat, double rlon) {
         if (boundsList == null) {
            boundsList = new ArrayList<double[]>();
         }

         double[] bnds = new double[] {
            ulat,
            llon,
            llat,
            rlon
         };

         boundsList.add(bnds);
         return this;
      }

      public Builder addZoom(int zoom) {
         if (zoomLevels == null) {
            zoomLevels = new boolean[ZOOM_LEVELS];
         }

         try {
            zoomLevels[zoom] = true;
         } catch (ArrayIndexOutOfBoundsException aioobe) {
            logger.warning("zoom level invalid, ignoring: " + zoom);
         }
         return this;
      }

      public Builder addZoomRange(int zoom1, int zoom2) {
         int min = Math.min(zoom1, zoom2);
         int max = Math.max(zoom1, zoom2);
         for (int z = min; z <= max; z++) {
            addZoom(z);
         }
         return this;
      }

      public Builder format(String format) {
         this.format = format;
         return this;
      }

      public Builder transform(MapTileCoordinateTransform transform) {
         mtcTransform = transform;
         return this;
      }

      public abstract void go();

      /**
       * Called from within grabTiles, with the tile info. You can use this
       * information to make a method call on mtu.
       * 
       * @param x tile coordinate
       * @param y tile coordinate
       * @param zoomLevel tile zoom level
       * @param mtu callback
       */
      public abstract void action(int x, int y, int zoomLevel, MapTileUtil mtu);
   }

   /**
    * A Builder that knows how to get the MTU to download files from a website.
    * 
    * @author dietrick
    */
   public static class URLGrabber
         extends Builder {
      public URLGrabber(String source, String destination) {
         super(source, destination);
      }

      public void go() {
         if (source != null && destination != null) {
            new MapTileUtil(this).grabTiles(this);
         } else {
            logger.warning("Need a source and destination for tile locations");
         }
      }

      public void action(int x, int y, int zoomLevel, MapTileUtil mtu) {
         mtu.grabURLTile(x, y, zoomLevel);
      }
   }

}
