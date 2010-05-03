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

import java.awt.Component;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.omGraphics.OMWarpingImage;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.proj.coords.MercatorUVGCT;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * The StandardImageTileFactory is a TileFactory implementation that retrieves
 * image tiles from local storage. These tiles are assumed to be stored in the
 * local file system, at some root directory, and then in some hierarchy like
 * zoom-level/x coord/y coord.file-extension. This class can be extended to
 * allow different tile naming/storing conventions to be used.
 * <p>
 * 
 * This component can be configured using properties:
 * <p>
 * <pre>
 * rootDir=the path to the parent directory of the tiles. The factory will construct specific file paths that are appended to this value. 
 * fileExt=the file extension to append to the tile names, should have a period.
 * cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * </pre>
 * 
 * @author dietrick
 */
public class StandardMapTileFactory
      extends CacheHandler
      implements MapTileFactory, PropertyConsumer {
   protected String prefix = null;

   protected final static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory");

   public final static String ROOT_DIR_PROPERTY = "rootDir";
   public final static String FILE_EXT_PROPERTY = "fileExt";
   public final static String CACHE_SIZE_PROPERTY = "cacheSize";

   protected ZoomLevelInfo zoomLevelInfo = new ZoomLevelInfo();
   protected String rootDir;
   protected String fileExt;

   protected boolean verbose = false;

   protected Component repaintCallback;

   public StandardMapTileFactory() {
      super(100);
      verbose = logger.isLoggable(Level.FINE);
   }

   public StandardMapTileFactory(Layer layer, String rootDir, String tileFileExt) {
      super(100);
      this.rootDir = rootDir;
      this.fileExt = tileFileExt;
      verbose = logger.isLoggable(Level.FINE);
      this.repaintCallback = layer;
   }

   @Override
   public CacheObject load(Object key) {
      return null;
   }

   MercatorUVGCT transform = new MercatorUVGCT(0);

   /**
    * Called to load cache object from data source, when not found in cache.
    * 
    * @param key
    * @param x
    * @param y
    * @param zoomLevel
    * @param proj passed solely to enable checking if the projection of the
    *        tiles matches the rendered projection.
    * @return
    */
   public CacheObject load(Object key, int x, int y, int zoomLevel, Projection proj) {
      if (key instanceof String) {
         String imagePath = (String) key;
         if (verbose) {
            logger.fine("fetching file for cache: " + imagePath);
         }

         if (proj instanceof Mercator) {
            return getTileMatchingProjectionType(imagePath, x, y, zoomLevel);
         } else {
            return getTileNotMatchingProjectionType(imagePath, x, y, zoomLevel);
         }

      }
      return null;
   }

   protected CacheObject getTileMatchingProjectionType(String imagePath, int x, int y, int zoomLevel) {

      Point2D pnt = new Point2D.Double();
      pnt.setLocation(x, y);
      Point2D tileUL = MapTileMaker.tileUVToLatLon(pnt, zoomLevel);
      pnt.setLocation(x + 1, y + 1);
      Point2D tileLR = MapTileMaker.tileUVToLatLon(pnt, zoomLevel);
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("tile coords: " + tileUL + ", " + tileLR);
      }

      ImageIcon ii = new ImageIcon(imagePath);
      if (ii.getIconWidth() > 0) {
         OMScalingRaster raster = new OMScalingRaster(tileUL.getY(), tileUL.getX(), tileLR.getY(), tileLR.getX(), ii.getImage());
         return new CacheObject(imagePath, raster);
      }
      return null;
   }

   protected CacheObject getTileNotMatchingProjectionType(String imagePath, int x, int y, int zoomLevel) {
      URL imageURL;
      try {
         imageURL = PropUtils.getResourceOrFileOrURL(imagePath);
         BufferedImage bi = BufferedImageHelper.getBufferedImage(imageURL);

         if (bi != null) {

            DataBounds dataBounds = new DataBounds(new Point(x, y), new Point(x + 1, y + 1));
            dataBounds.setyDirUp(false);

            transform.setZoomLevel(zoomLevel);
            OMWarpingImage raster = new OMWarpingImage(bi, transform, dataBounds);

            if (logger.isLoggable(Level.FINER)) {
               raster.setSelected(true);
            }
            return new CacheObject(imagePath, raster);
         }

      } catch (InterruptedException e) {
         logger.warning(e.getMessage());
         e.printStackTrace();
      } catch (MalformedURLException e1) {
         logger.warning(e1.getMessage());
         e1.printStackTrace();
      }
      return null;
   }

   /**
    * The main call to retrieve something from the cache, modified to allow load
    * method to do some projection calculations to initialize tile parameters.
    * If the object is not found in the cache, then load is called to get it
    * from the data source.
    * 
    * @param proj passed solely to enable checking if the projection of the
    *        tiles matches the rendered projection.
    */
   public Object get(Object key, int x, int y, int zoomLevel, Projection proj) {
      CacheObject ret = searchCache(key);
      if (ret != null) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("found tile (" + x + ", " + y + ") in cache");
         }
         return ret.obj;
      }

      ret = load(key, x, y, zoomLevel, proj);
      if (ret == null)
         return null;

      replaceLeastUsed(ret);
      return ret.obj;
   }

   /**
    * An auxiliary call to retrieve something from the cache, modified to allow
    * load method to do some projection calculations to initialize tile
    * parameters. If the object is not found in the cache, null is returned.
    */
   public Object getFromCache(Object key, int x, int y, int zoomLevel) {
      CacheObject ret = searchCache(key);
      if (ret != null) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("found tile (" + x + ", " + y + ") in cache");
         }
         return ret.obj;
      }

      return null;
   }

   /**
    * Returns projected tiles for the given projection.
    * 
    * @param proj the projection to fetch tiles for.
    * @return OMGraphicList containing projected OMGraphics.
    */
   public OMGraphicList getTiles(Projection proj) {
      return getTiles(proj, -1, new OMGraphicList());
   }

   /**
    * Returns projected tiles for given projection at specified zoom level.
    * 
    * @param proj projection for query
    * @param zoomLevel zoom level 1-20 for tiles to be returned, -1 for code to
    *        figure out appropriate zoom level.
    * @return OMGraphicList with tiles.
    */
   public OMGraphicList getTiles(Projection proj, int zoomLevel) {
      return getTiles(proj, zoomLevel, new OMGraphicList());
   }

   /**
    * Returns projected tiles for given projection at specified zoom level. Use
    * this call if you are providing a repaint callback component to the
    * factory, so you will have a handle on the OMGraphicList to render to.
    * 
    * @param proj projection for query
    * @param zoomLevel zoom level 1-20 for tiles to be returned, -1 for code to
    *        figure out appropriate zoom level.
    * @param list OMGraphicList that is returned, that will also have tiles
    *        added to it.
    * @return OMGraphicList with tiles.
    */
   public OMGraphicList getTiles(Projection proj, int zoomLevel, OMGraphicList list) {

      if (fileExt == null || rootDir == null) {
         logger.warning("No path to tile files provided (" + rootDir + "), or file extension (" + fileExt + ") not specified");
         return list;
      }

      /**
       * Given a projection, a couple of things have to happen.
       * 
       * - First, we need to figure out what zoom level fits us best if it is
       * not specified.
       * 
       * - Second, we need to figure out the uv bounds that fit the projection.
       * 
       * - Third, we need to grab the images for uv grid, by cycling through the
       * limits in both directions.
       * 
       * The TileMaker static methods let us convert uv to lat/lon and back, a
       * ZoomLevelInfo object can be used to figure out what the file path looks
       * like.
       */

      if (zoomLevel < 0) {
         zoomLevel = getZoomLevelForProj(proj);
         if (verbose) {
            logger.fine("Best zoom level calculated at: " + zoomLevel);
         }
      }

      if (zoomLevel >= 0) {

         if (zoomLevel == 0)
            zoomLevel++;

         zoomLevelInfo.setZoomLevel(zoomLevel);

         Point2D upperLeft = proj.getUpperLeft();
         Point2D lowerRight = proj.getLowerRight();

         Point2D uvul = MapTileMaker.latLonToTileUV(upperLeft, zoomLevel);
         Point2D uvlr = MapTileMaker.latLonToTileUV(lowerRight, zoomLevel);

         int uvleft = (int) Math.floor(uvul.getX());
         int uvright = (int) Math.ceil(uvlr.getX());
         int uvup = (int) Math.floor(uvul.getY());
         if (uvup < 0) {
            uvup = 0;
         }
         int uvbottom = (int) Math.ceil(uvlr.getY());

         if (verbose) {
            logger.fine("for " + proj + ", fetching tiles between x(" + uvleft + ", " + uvright + ") y(" + uvup + ", " + uvbottom
                  + ")");
         }

         // dateline test
         Point2D datelinePnt = proj.forward(new LatLonPoint.Double(upperLeft.getY(), 180d));
         double dlx = datelinePnt.getX();
         boolean dateline = dlx > 0 & dlx < proj.getWidth();
         logger.fine("Long(180) located at " + dlx);

         if (!dateline) {
            getTiles(uvleft, uvright, uvup, uvbottom, zoomLevelInfo, proj, list);
         } else {
            logger.fine("handling DATELINE");
            getTiles(uvleft, (int) Math.pow(2, zoomLevel), uvup, uvbottom, zoomLevelInfo, proj, list);
            getTiles(0, uvright, uvup, uvbottom, zoomLevelInfo, proj, list);
         }

      }
      return list;
   }

   /**
    * A temporary object used to store information about map tiles that are not
    * found in the cache. The caching mechanism has been modified to search for
    * cached tiles first, and using this object to hold information about map
    * tiles that need to be loaded. The cached tiles will be immediately
    * displayed, and then these tiles will be displayed after that as they are
    * loaded.
    * 
    * @author dietrick
    */
   class LoadObj {
      String imagePath;
      int x;
      int y;
      int zoomLevel;

      LoadObj(String p, int x, int y, int z) {
         this.imagePath = p;
         this.x = x;
         this.y = y;
         this.zoomLevel = z;
      }
   }

   protected void getTiles(int uvleft, int uvright, int uvup, int uvbottom, ZoomLevelInfo zoomLevelInfo, Projection proj,
                           OMGraphicList list) {
      if (verbose) {
         logger.fine("for zoom level: " + zoomLevelInfo.getZoomLevel() + ", screen covers uv coords [t:" + uvup + ", l:" + uvleft
               + ", b:" + uvbottom + ", r:" + uvright + "]");
      }

      if (zoomLevelInfo.getZoomLevel() == 0) {
         logger.fine("got one tile, OM can't draw a single tile covering the earth. Sorry.");
      }

      List<LoadObj> reloads = new ArrayList<LoadObj>();
      int zoomLevel = zoomLevelInfo.getZoomLevel();

      boolean isMercator = proj instanceof Mercator;

      for (int x = uvleft; x < uvright; x++) {
         for (int y = uvup; y < uvbottom; y++) {

            String imagePath = zoomLevelInfo.formatImageFilePath(rootDir, x, y) + fileExt;

            /**
             * Need to modify the action of the cache a little to make the map
             * appear more responsive. So, we cycle through the desired tiles,
             * gathering all of the tiles that are immediately available.
             * Generate them, add them to list, and call repaint when they are
             * set.
             * 
             * Keep track of the ones that are not there, and load those
             * one-by-one after, calling repaint as they are added to the list.
             */

            OMGraphic raster = (OMGraphic) getFromCache(imagePath, x, y, zoomLevel);

            boolean rightOMGraphicType =
                  (raster instanceof OMScalingRaster && isMercator) || (raster instanceof OMWarpingImage && !isMercator);

            if (raster != null && rightOMGraphicType) {
               raster.generate(proj);
               list.add(raster);
            } else {
               reloads.add(new LoadObj(imagePath, x, y, zoomLevel));
            }

         }
      }

      if (repaintCallback != null) {
         repaintCallback.repaint();
      }

      for (LoadObj reload : reloads) {

         // OMGraphic raster = (OMGraphic) get(reload.imagePath, reload.x,
         // reload.y, reload.zoomLevel, proj);

         CacheObject ret = load(reload.imagePath, reload.x, reload.y, reload.zoomLevel, proj);
         if (ret != null) {
            replaceLeastUsed(ret);
            OMGraphic raster = (OMGraphic) ret.obj;

            if (raster != null) {
               raster.generate(proj);
               list.add(raster);
               if (repaintCallback != null) {
                  repaintCallback.repaint();
               }
            }
         }

      }

   }

   protected float[] scales;

   /**
    * Given a projection, figure out the appropriate zoom level for it. Right
    * now, 0 is totally zoomed with one tile for the entire earth. But we don't
    * return 0, we start at 1. OM can't handle one tile that covers the entire
    * earth because of the restriction for handling OMGraphics to less than half
    * of the earth.
    * 
    * @param proj
    * @return the zoom level.
    */
   public int getZoomLevelForProj(Projection proj) {
      int low = 0;
      int high = 20;

      if (scales == null) {
         scales = MapTileMaker.getScalesForZoomLevels(proj, low, high);
      }

      float currentScale = proj.getScale();
      float currentDiff = Float.NEGATIVE_INFINITY;
      int ret = low;
      for (int i = low; i <= high; i++) {
         float diff = currentScale - scales[i];
         if (diff < 0 && diff > currentDiff) {
            ret = i;
            currentDiff = diff;
         }
      }

      return ret;
   }

   public Component getRepaintCallback() {
      return repaintCallback;
   }

   public void setRepaintCallback(Component callback) {
      this.repaintCallback = callback;
   }

   public Properties getProperties(Properties getList) {
      String prefix = PropUtils.getScopedPropertyPrefix(this);
      getList.put(prefix + ROOT_DIR_PROPERTY, PropUtils.unnull(rootDir));
      getList.put(prefix + FILE_EXT_PROPERTY, PropUtils.unnull(fileExt));
      getList.put(prefix + CACHE_SIZE_PROPERTY, Integer.toString(getCacheSize()));
      return getList;
   }

   public Properties getPropertyInfo(Properties list) {
      I18n i18n = Environment.getI18n();
      PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class, ROOT_DIR_PROPERTY,
                                    "Tile Directory", "Root directory containing image tiles",
                                    "com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor");
      PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class, FILE_EXT_PROPERTY,
                                    "Image File Extension", "Extension of image files (.jpg, .png, etc)", null);
      PropUtils.setI18NPropertyInfo(i18n, list, com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.class,
                                    CACHE_SIZE_PROPERTY, "Cache Size", "Number of tile images held in memory", null);
      return list;
   }

   public String getPropertyPrefix() {
      return prefix;
   }

   public void setProperties(Properties setList) {
      setProperties(null, setList);
   }

   public void setProperties(String prefix, Properties setList) {
      setPropertyPrefix(prefix);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      rootDir = setList.getProperty(prefix + ROOT_DIR_PROPERTY, rootDir);
      fileExt = setList.getProperty(prefix + FILE_EXT_PROPERTY, fileExt);
      super.resetCache(PropUtils.intFromProperties(setList, prefix + CACHE_SIZE_PROPERTY, getCacheSize()));
   }

   public void setPropertyPrefix(String prefix) {
      this.prefix = prefix;
   }

   public String getRootDir() {
      return rootDir;
   }

   public void setRootDir(String rootDir) {
      this.rootDir = rootDir;
   }

   public String getFileExt() {
      return fileExt;
   }

   public void setFileExt(String fileExt) {
      this.fileExt = (fileExt != null && fileExt.startsWith(".")) ? fileExt : "." + fileExt;
   }
}