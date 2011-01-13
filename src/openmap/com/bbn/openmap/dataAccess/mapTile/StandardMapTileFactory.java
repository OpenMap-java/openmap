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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.omGraphics.OMWarpingImage;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ClasspathHacker;
import com.bbn.openmap.util.ComponentFactory;
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
 * 
 * <pre>
 * rootDir=the path to the parent directory of the tiles. The factory will construct specific file paths that are appended to this value. 
 * fileExt=the file extension to append to the tile names
 * cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * # default is OSMMapTileCoordinateTransform, but it depends on the source of tiles.  GDAL is TSMMapTileCoordinateTransform
 * mapTileTransform=com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform, or com.bbn.openmap.dataAccess.mapTile.TSMMapTileCoordinateTransform
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
   public final static String MTCTRANSFORM_PROPERTY = "mapTileTransform";
   public final static String EMPTY_TILE_PROPERTY = "emtpyTilePath";

   public final static String TILE_PROPERTIES = "tiles.omp";

   public final static String DEFAULT_EMPTY_TILE_NAME = "EMPTY_TILE";

   protected ZoomLevelInfo zoomLevelInfo = new ZoomLevelInfo();
   protected String rootDir;
   protected String fileExt = ".png";
   protected String rootDirProperty; // For writing out later, if necessary

   protected boolean verbose = false;

   /**
    * A component that is painting the tiles to the screen. If this component is
    * set on this tile factory, it will be told to repaint the OMGraphicList it
    * provides with the current contents. If you want the map to update as tiles
    * are created by the factory, they will pop up on the map when they are
    * ready.
    */
   protected Component repaintCallback;
   /**
    * Flag to tell the factory to create the extra tiles off-map. Tends to cause
    * the layer to do more work than necessary, so it's not used.
    */
   private boolean doExtraTiles = false;
   /**
    * Coordinate transform for the uv coordinates of the tiles. Different
    * sources have different origins for tile coordinates.
    */
   protected MapTileCoordinateTransform mtcTransform = new OSMMapTileCoordinateTransform();

   public StandardMapTileFactory() {
      super(100);
      verbose = logger.isLoggable(Level.FINE);
   }

   public StandardMapTileFactory(Component layer, String rootDir, String tileFileExt) {
      super(100);
      setRootDir(rootDir);
      setFileExt(tileFileExt);
      verbose = logger.isLoggable(Level.FINE);
      this.repaintCallback = layer;
   }

   @Override
   public CacheObject load(Object key) {
      return null;
   }

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
      Point2D tileUL = mtcTransform.tileUVToLatLon(pnt, zoomLevel);
      pnt.setLocation(x + 1, y + 1);
      Point2D tileLR = mtcTransform.tileUVToLatLon(pnt, zoomLevel);
      if (verbose) {
         logger.fine("tile coords: " + tileUL + ", " + tileLR);
      }

      URL imageURL;
      try {
         imageURL = PropUtils.getResourceOrFileOrURL(imagePath);
         if (imageURL != null) {
            ImageIcon ii = new ImageIcon(imageURL);
            if (ii.getIconWidth() > 0) {

               double x1 = Math.min(tileUL.getX(), tileLR.getX());
               double x2 = Math.max(tileUL.getX(), tileLR.getX());
               double y1 = Math.min(tileUL.getY(), tileLR.getY());
               double y2 = Math.max(tileUL.getY(), tileLR.getY());

               OMScalingRaster raster = new OMScalingRaster(y2, x1, y1, x2, ii.getImage());
               return new CacheObject(imagePath, raster);
            }
         } else {
            logger.fine("Can't find resource located at " + imagePath);
         }
      } catch (MalformedURLException e) {
         logger.fine("Can't find resource located at " + imagePath);
      }
      return null;
   }

   protected CacheObject getTileNotMatchingProjectionType(String imagePath, int x, int y, int zoomLevel) {
      URL imageURL;
      try {
         imageURL = PropUtils.getResourceOrFileOrURL(imagePath);
         if (imageURL != null) {
            BufferedImage bi = BufferedImageHelper.getBufferedImage(imageURL);

            if (bi != null) {

               DataBounds dataBounds = new DataBounds(new Point(x, y), new Point(x + 1, y + 1));
               dataBounds.setyDirUp(mtcTransform.isYDirectionUp());

               OMWarpingImage raster = new OMWarpingImage(bi, mtcTransform.getTransform(zoomLevel), dataBounds);

               if (logger.isLoggable(Level.FINER)) {
                  raster.setSelected(true);
               }
               return new CacheObject(imagePath, raster);
            }
         }

      } catch (InterruptedException e) {
         logger.warning(e.getMessage());
         e.printStackTrace();
      } catch (MalformedURLException e1) {
         logger.fine("can't find resource located at: " + imagePath);
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
    * @throws InterruptedException
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
    * @throws InterruptedException
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
    * @throws InterruptedException
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

         int[] uvBounds = mtcTransform.getTileBoundsForProjection(upperLeft, lowerRight, zoomLevel);
         int uvup = uvBounds[0];
         int uvleft = uvBounds[1];
         int uvbottom = uvBounds[2];
         int uvright = uvBounds[3];

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

      int uvleftM = (int) Math.min(uvleft, uvright);
      int uvrightM = (int) Math.max(uvleft, uvright);
      int uvupM = (int) Math.min(uvbottom, uvup);
      int uvbottomM = (int) Math.max(uvbottom, uvup);

      for (int x = uvleftM; x < uvrightM; x++) {
         for (int y = uvupM; y < uvbottomM; y++) {

            // Try to help doing unnecessary work
            if (Thread.currentThread().isInterrupted()) {
               logger.fine("Detected interruption in standard loop, thread " + Thread.currentThread().getName());
               return;
            }

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

      if (verbose)
         logger.fine("found " + list.size() + " frames in cache, loading " + reloads.size() + " others now...");

      if (repaintCallback != null) {
         repaintCallback.repaint();
      }

      /*
       * Load the tiles that we're already in the cache, that need to be fetched
       * from the source.
       */
      for (LoadObj reload : reloads) {
         handleLoad(reload.imagePath, reload.x, reload.y, reload.zoomLevel, proj, list);
      }

      if (verbose) {
         logger.fine("finished loading " + reloads.size() + " frames from source for screen"
               + (doExtraTiles ? ", moving to off-screen frames..." : ""));
      }

      if (!doExtraTiles)
         return;

      // Just for giggles, lets go ahead and walk around the edge of the area
      // and prefetch tiles to load them into memory...

      // int uvleft, int uvright, int uvup, int uvbottom
      int x1 = uvleft;
      int y1 = uvup;
      int x2 = uvright;
      int y2 = uvbottom;
      boolean top = false;
      boolean left = false;
      boolean right = false;
      boolean bottom = false;
      if (x1 > 0) {
         x1--;
         left = true;
      }
      if (y1 > 0) {
         y1--;
         top = true;
      }
      int edgeTileCount = zoomLevelInfo.getEdgeTileCount();
      if (x2 < edgeTileCount - 1) {
         x2++;
         right = true;
      }
      if (y2 < edgeTileCount - 1) {
         y2++;
         bottom = true;
      }

      // Get the corners
      if (top && left) {
         handleLoad(x1, y1, zoomLevel, proj, list);
      }
      if (bottom && left) {
         handleLoad(x1, y2, zoomLevel, proj, list);
      }
      if (bottom && right) {
         handleLoad(x2, y2, zoomLevel, proj, list);
      }
      if (top && right) {
         handleLoad(x2, y1, zoomLevel, proj, list);
      }
      // Now go along the sides
      if (top) {
         for (int x = uvleft; x < uvright; x++) {
            handleLoad(x, y1, zoomLevel, proj, list);
         }
      }

      if (bottom) {
         for (int x = uvleft; x < uvright; x++) {
            handleLoad(x, y2, zoomLevel, proj, list);
         }
      }

      if (right) {
         for (int y = uvup; y < uvbottom; y++) {
            handleLoad(x2, y, zoomLevel, proj, list);
         }
      }

      if (left) {
         for (int y = uvup; y < uvbottom; y++) {
            handleLoad(x1, y, zoomLevel, proj, list);
         }
      }

      if (verbose) {
         logger.fine("finished loading all tiles (" + list.size() + ")");
      }
   }

   /**
    * Handles going to the cache, getting the cache to load the tile, and then
    * manage the resulting OMRaster tile. Adds the tile to the list after
    * generating it with the projection, and calls the repaintCallback if there
    * is one.
    * 
    * @param imagePath the image path for the tile
    * @param x the x uv coordinate of the tile
    * @param y the y uv coordinate of the tile
    * @param zoomLevel the zoomLevel of the tile
    * @param proj the current projection.
    * @param list the OMGraphicList to add the tile to.
    * @throws InterruptedException
    */
   private void handleLoad(String imagePath, int x, int y, int zoomLevel, Projection proj, OMGraphicList list) {

      if (Thread.currentThread().isInterrupted()) {
         return;
      }

      CacheObject ret = load(imagePath, x, y, zoomLevel, proj);
      if (ret != null) {
         replaceLeastUsed(ret);
         OMGraphic raster = (OMGraphic) ret.obj;

         if (raster != null) {
            raster.generate(proj);
            list.add(raster);

            if (logger.isLoggable(Level.FINE)) {
               raster.putAttribute(OMGraphic.TOOLTIP, imagePath);
            }

            if (repaintCallback != null) {
               repaintCallback.repaint();
            }
         }
      }
   }

   /**
    * Handles going to the cache, getting the cache to load the tile, and then
    * manage the resulting OMRaster tile. Adds the tile to the list after
    * generating it with the projection, and calls the repaintCallback if there
    * is one. Handles creating the image file path given the other info.
    * 
    * @param x the x uv coordinate of the tile
    * @param y the y uv coordinate of the tile
    * @param zoomLevel the zoomLevel of the tile
    * @param proj the current projection.
    * @param list the OMGraphicList to add the tile to.
    * @throws InterruptedException
    */
   private void handleLoad(int x, int y, int zoomLevel, Projection proj, OMGraphicList list) {
      String imagePath = zoomLevelInfo.formatImageFilePath(rootDir, x, y) + fileExt;
      handleLoad(imagePath, x, y, zoomLevel, proj, list);
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
         if (diff <= 0 && diff > currentDiff) {
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
      getList.put(prefix + ROOT_DIR_PROPERTY, PropUtils.unnull(rootDirProperty));
      getList.put(prefix + FILE_EXT_PROPERTY, PropUtils.unnull(fileExt));
      getList.put(prefix + CACHE_SIZE_PROPERTY, Integer.toString(getCacheSize()));
      getList.put(prefix + MTCTRANSFORM_PROPERTY, mtcTransform.getClass().toString());
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

      String rootDirectory = setList.getProperty(prefix + ROOT_DIR_PROPERTY);
      if (rootDirectory != null) {
         setRootDir(rootDirectory);
      }

      String fileExt = setList.getProperty(prefix + FILE_EXT_PROPERTY);

      // Add a period if it doesn't exist.
      if (fileExt != null) {
         setFileExt(fileExt);
      }

      String mapTileCoordinateTransform = setList.getProperty(prefix + MTCTRANSFORM_PROPERTY);
      if (mapTileCoordinateTransform != null) {
         Object obj = ComponentFactory.create(mapTileCoordinateTransform);

         if (obj instanceof MapTileCoordinateTransform) {
            setMtcTransform((MapTileCoordinateTransform) obj);
         }
      }

      super.resetCache(PropUtils.intFromProperties(setList, prefix + CACHE_SIZE_PROPERTY, getCacheSize()));
   }

   public void setPropertyPrefix(String prefix) {
      this.prefix = prefix;
   }

   public String getRootDir() {
      return rootDir;
   }

   public void setRootDir(String rootDirectory) {

      if (rootDirectory != null) {
         if (rootDirectory.endsWith("jar")) {

            rootDirProperty = rootDirectory;
            String jarFileNames = rootDirectory;

            Vector<String> jarNames = PropUtils.parseMarkers(jarFileNames, ";");
            for (String jarName : jarNames) {
               try {
                  logger.fine("adding " + jarName + " to classpath");
                  ClasspathHacker.addFile(jarName);

                  JarFile jarFile = new JarFile(jarName);
                  JarEntry jarPropertyFile = (JarEntry) jarFile.getEntry(TILE_PROPERTIES);
                  if (jarPropertyFile != null) {
                     InputStream is = jarFile.getInputStream(jarPropertyFile);
                     configure(is);
                  }

               } catch (IOException ioe) {
                  logger.warning("couldn't add map data jar file: " + jarName);
               }
            }

            // You might notice that we didn't set the rootDir here if a jar
            // file
            // is being used. That's because we just want to use whatever the
            // tile
            // file says, and this method will be called again if needed when
            // the
            // properties get written.

         } else {
            // check for tile.omp file that may describe how to read tiles.
            File tileProps = new File(rootDirectory, TILE_PROPERTIES);
            if (tileProps.exists()) {
               try {
                  // Do this in case other properties are set for the tile set, file ext, transform.
                  configure(tileProps.toURI().toURL().openStream());
               } catch (MalformedURLException murle) {
                  logger.warning("tile file for " + rootDirectory + " couldn't be read: " + tileProps.getAbsolutePath());
               } catch (IOException ioe) {
                  logger.warning("tile file for " + rootDirectory + " couldn't be read");
               }
            }

            this.rootDir = rootDirectory;

            if (rootDirProperty == null) {
               // Assuming a file path being set, not as a result of a jar file
               rootDirProperty = rootDirectory;
            }
         }
         
      } else {
         // nulled out
         this.rootDir = rootDirectory;
         rootDirProperty = rootDirectory;
      }

   }

   protected void configure(InputStream is)
         throws IOException {

      Properties props = new Properties();
      props.load(is);

      String oldPrefix = getPropertyPrefix();
      setProperties(null, props);
      setPropertyPrefix(oldPrefix);
   }

   public String getFileExt() {
      return fileExt;
   }

   public void setFileExt(String fileExt) {
      this.fileExt = (fileExt != null && fileExt.startsWith(".")) ? fileExt : "." + fileExt;
   }

   public MapTileCoordinateTransform getMtcTransform() {
      return mtcTransform;
   }

   /**
    * Set the map tile coordinate transformed used to figure out lat/lon to tile
    * coordinates. Can't be null, if you set it to null an
    * OSMMapTileCoordTransform will be created instead.
    * 
    * @param mtcTransform
    */
   public void setMtcTransform(MapTileCoordinateTransform mtcTransform) {
      if (mtcTransform == null) {
         mtcTransform = new OSMMapTileCoordinateTransform();
      }
      this.mtcTransform = mtcTransform;
   }

}