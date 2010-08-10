//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * The ServerMapTileFactory is an extension to the StandardMapTileFactory that
 * can go to a http server to retrieve image tiles. You provide it with a root
 * URL that points to the parent directory of the tiles, and then this component
 * will add on the zoom/x/y.extension to that directory path to make the call
 * for a specific tile. Please make sure you have the permission of the server's
 * owner before hammering away at retrieving tiles from it.
 * 
 * This component can be configured using properties:
 * <p>
 * 
 * <pre>
 * # Inherited from StandardMapTileFactory
 * rootDir=the URL to the parent directory of the tiles on a server. The factory will construct specific file paths that are appended to this value. 
 * fileExt=the file extension to append to the tile names, should have a period.
 * cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * 
 * # Additional properties
 * localCacheRootDir=if specified, the factory will store tiles locally at this root directory.  This directory is checked before going to the server, too.
 * </pre>
 * 
 * @author dietrick
 */
public class ServerMapTileFactory
      extends StandardMapTileFactory
      implements MapTileFactory, PropertyConsumer {

   public final static String LOCAL_CACHE_ROOT_DIR_PROPERTY = "localCacheRootDir";

   protected String localCacheDir = null;

   public ServerMapTileFactory() {
      this(null);
   }

   public ServerMapTileFactory(String rootDir) {
      this.rootDir = rootDir;
      this.fileExt = ".png";
      verbose = logger.isLoggable(Level.FINE);
   }

   /**
    * An auxiliary call to retrieve something from the cache, modified to allow
    * load method to do some projection calculations to initialize tile
    * parameters. If the object is not found in the cache, null is returned.
    */
   public Object getFromCache(Object key, int x, int y, int zoomLevel) {
      String localLoc = null;

      if (localCacheDir != null && zoomLevelInfo != null) {
         localLoc = zoomLevelInfo.formatImageFilePath(localCacheDir, x, y) + fileExt;
         /**
          * If a local cache is defined, then the cache will always use the
          * string for the local file as the key.
          */
         CacheObject ret = searchCache(localLoc);
         if (ret != null) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("found tile (" + x + ", " + y + ") in cache");
            }
            return ret.obj;
         }
         /**
          * Return null if the localized version isn't found in cache when local
          * version is defined.
          */
         return null;
      }

      // Assuming that the localCacheDir is not defined, so the cache objects
      // will be using the server location as key

      CacheObject ret = searchCache(key);
      if (ret != null) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("found tile (" + x + ", " + y + ") in cache");
         }
         return ret.obj;
      }

      return null;
   }

   public CacheObject load(Object key, int x, int y, int zoomLevel, Projection proj) {
      if (key instanceof String) {
         String imagePath = (String) key;
         if (verbose) {
            logger.fine("fetching file for cache: " + imagePath);
         }

         java.net.URL url = null;
         ImageIcon ii = null;

         String localLoc = null;
         if (localCacheDir != null && zoomLevelInfo != null) {
            localLoc = zoomLevelInfo.formatImageFilePath(localCacheDir, x, y) + fileExt;

            CacheObject localVersion = super.load(localLoc, x, y, zoomLevel, proj);

            if (localVersion != null) {
               logger.fine("found version of tile in local cache: " + localLoc);
               return localVersion;
            }
         }

         try {
            url = new java.net.URL(imagePath);
            java.net.HttpURLConnection urlc = (java.net.HttpURLConnection) url.openConnection();

            if (logger.isLoggable(Level.FINER)) {
               logger.finer("url content type: " + urlc.getContentType());
            }

            if (urlc == null || urlc.getContentType() == null) {
               logger.warning("unable to connect to " + imagePath);
               return null;
            }

            // text
            if (urlc.getContentType().startsWith("text")) {
               java.io.BufferedReader bin = new java.io.BufferedReader(new java.io.InputStreamReader(urlc.getInputStream()));
               String st;
               String message = "";
               while ((st = bin.readLine()) != null) {
                  message += st;
               }

               // Debug.error(message);
               // How about we toss the message out to the user
               // instead?
               logger.warning(message);

               // image
            } else if (urlc.getContentType().startsWith("image")) {

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

               if (localCacheDir != null) {
                  File localFile = new File(localLoc);

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

         if (ii != null) {

            Point2D pnt = new Point2D.Double();
            pnt.setLocation(x, y);
            Point2D tileUL = mtcTransform.tileUVToLatLon(pnt, zoomLevel);
            pnt.setLocation(x + 1, y + 1);
            Point2D tileLR = mtcTransform.tileUVToLatLon(pnt, zoomLevel);
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("tile coords: " + tileUL + ", " + tileLR);
            }

            if (ii.getIconWidth() > 0) {
               // image found
               OMScalingRaster raster =
                     new OMScalingRaster(tileUL.getY(), tileUL.getX(), tileLR.getY(), tileLR.getX(), ii.getImage());
               if (logger.isLoggable(Level.FINER)) {
                  raster.setSelected(true);
               }

               // Again, create a CacheObject based on the local name if the
               // local dir is defined.
               if (localLoc != null) {
                  key = localLoc;
               }

               return new CacheObject(key, raster);
            }
         }
      }

      return null;
   }

   public Properties getProperties(Properties getList) {
      getList = super.getProperties(getList);
      getList.put(prefix + LOCAL_CACHE_ROOT_DIR_PROPERTY, PropUtils.unnull(localCacheDir));
      return getList;
   }

   public Properties getPropertyInfo(Properties list) {
      list = super.getPropertyInfo(list);
      I18n i18n = Environment.getI18n();
      PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class,
                                    LOCAL_CACHE_ROOT_DIR_PROPERTY, "Local Cache Tile Directory",
                                    "Root directory containing image tiles retrieved from image server.",
                                    "com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor");
      return list;
   }

   public void setProperties(String prefix, Properties setList) {
      super.setProperties(prefix, setList);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      localCacheDir = setList.getProperty(prefix + LOCAL_CACHE_ROOT_DIR_PROPERTY, localCacheDir);
   }

}