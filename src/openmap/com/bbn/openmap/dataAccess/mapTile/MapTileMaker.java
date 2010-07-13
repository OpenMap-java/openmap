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
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import com.bbn.openmap.Environment;
import com.bbn.openmap.Layer;
import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.image.ImageServer;
import com.bbn.openmap.image.SunJPEGFormatter;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The MapTileMaker is an ImageServer extension that knows how to create image
 * tile sets, like the kind of tiles used by Google Maps and OpenStreetMap. It
 * uses ZoomLayerInfo objects to define how tiles are created for different zoom
 * levels. You can run this class as an application. With the -create option, it
 * will create a sample properties file to demonstrate what properties are
 * needed to run it.
 * 
 * @author dietrick
 */
public class MapTileMaker
      extends ImageServer {

   public final static String ROOT_DIRECTORY_PROPERTY = "rootDir";
   public final static String ZOOM_LEVELS_PROPERTY = "zoomLevels";

   protected String rootDir;
   protected List<ZoomLevelInfo> zoomLevels;

   /**
    * Empty constructor that expects to be configured later.
    */
   protected MapTileMaker() {
   }

   /**
    * To create the TileMaker, you hand it a set of properties that let it
    * create an array of layers, and also to set the properties for those
    * layers. The properties file for the ImageServer looks strikingly similar
    * to the openmap.properties file. So, all the layers get set up here...
    */
   public MapTileMaker(Properties props) {
      super(props);
   }

   /**
    * Same as the other constructor, except that the properties can have a
    * prefix in front of them. The format of the prefix has to match how the
    * property is specified the the properties file, which may include the
    * period - i.e server1.imageServer.layers, the server1. is the prefix that
    * should get passed in. The ImageMaster does this.
    */
   public MapTileMaker(String prefix, Properties props) {
      super(prefix, props, null);
   }

   /**
    * Create an TileMaker that should be configured with a Properties file. The
    * prefix given is to scope the ImageServer properties to this instance. The
    * Hashtable is for reusing any layers that may already be instantiated.
    */
   public MapTileMaker(String prefix, Properties props, Map<String, Layer> instantiatedLayers) {
      super(prefix, props, instantiatedLayers);
   }

   /**
    * Create an TileMaker from an array of Layers and an ImageFormatter. It's
    * assumed that the layers are already configured.
    * 
    * @param layers the array of layers.
    * @param formatter the ImageFormatter to use for the output image format.
    */
   public MapTileMaker(Layer[] layers, ImageFormatter formatter) {
      super(layers, formatter);
   }

   @SuppressWarnings("unchecked")
   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      rootDir = props.getProperty(prefix + ROOT_DIRECTORY_PROPERTY, rootDir);
      List<ZoomLevelInfo> zoomLevels =
            (List<ZoomLevelInfo>) PropUtils.objectsFromProperties(props, prefix + ZOOM_LEVELS_PROPERTY,
                                                                  ComponentFactory.ClassNameProperty);
      getZoomLevels().addAll(zoomLevels);
   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);
      String prefix = PropUtils.getScopedPropertyPrefix(this);
      props.put(prefix + ROOT_DIRECTORY_PROPERTY, PropUtils.unnull(rootDir));

      StringBuffer buf = new StringBuffer();
      for (ZoomLevelInfo zfi : getZoomLevels()) {
         buf.append(zfi.getPropertyPrefix()).append(" ");
         zfi.getProperties(props);
      }

      if (buf.length() > 0) {
         props.put(prefix + ZOOM_LEVELS_PROPERTY, buf.toString().trim());
      }

      return props;
   }

   public Properties getPropertyInfo(Properties props) {
      props = super.getPropertyInfo(props);
      PropUtils.setI18NPropertyInfo(Environment.getI18n(), props, com.bbn.openmap.dataAccess.mapTile.MapTileMaker.class,
                                    ROOT_DIRECTORY_PROPERTY, "Tile Directory", "Root directory for holding tile files.",
                                    "com.bbn.openmap.util.propertyEditor.DirectoryPropertyEditor");
      return props;
   }

   public byte[] makeTile(double uvx, double uvy, ZoomLevelInfo zoomInfo, Proj proj) {
      Point2D center = MapTileMaker.tileUVToLatLon(new Point2D.Double(uvx + .5, uvy + .5), zoomInfo.getZoomLevel());
      proj.setScale(zoomInfo.getScale());
      proj.setCenter(center);
      proj.setHeight(TILE_SIZE);
      proj.setWidth(TILE_SIZE);

      return createImage(proj, -1, -1, zoomInfo.getLayers());
   }

   public String makeTileFile(double uvx, double uvy, ZoomLevelInfo zoomInfo, Proj proj)
         throws IOException {
      byte[] imageBytes = makeTile(uvx, uvy, zoomInfo, proj);

      String filePath = zoomInfo.formatImageFilePath(getRootDir(), (int) uvx, (int) uvy);
      return writeImageFile(imageBytes, filePath, true);
   }

   public void makeTiles() {

      if (rootDir != null) {
         File rd = new File(rootDir);
         if (!rd.exists()) {
            rd.mkdir();
         }
      }

      Proj proj = new Mercator(new LatLonPoint.Double(), 10000, TILE_SIZE, TILE_SIZE);

      List<ZoomLevelInfo> zoomLevels = getZoomLevels();
      for (ZoomLevelInfo zfi : zoomLevels) {
         logger.info("writing zoom level " + zfi.getName() + " tiles...");
         for (Rectangle2D bounds : zfi.getUVBounds()) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine(" creating tiles " + bounds);
            }
            int startx = (int) bounds.getX();
            int starty = (int) bounds.getY();
            int xofflimit = (int) bounds.getWidth();
            int yofflimit = (int) bounds.getHeight();

            for (int xoff = 0; xoff < xofflimit; xoff++) {
               int x = startx + xoff;

               // Reset every x loop for first time check through y loop
               String parentDirPath = null;

               for (int yoff = 0; yoff < yofflimit; yoff++) {
                  int y = starty + yoff;

                  if (parentDirPath == null) {
                     parentDirPath = zfi.formatParentDirectoryName(getRootDir(), x, y);
                     File parentDir = new File(parentDirPath);
                     if (!parentDir.exists()) {
                        parentDir.mkdirs();
                     }
                  }

                  try {
                     String outputFile = makeTileFile(x, y, zfi, proj);
                     if (logger.isLoggable(Level.FINER)) {
                        logger.finer("wrote: " + outputFile);
                     }
                  } catch (IOException ioe) {
                     logger.warning("Caught IOException writing " + x + ", " + y + ", " + zfi);
                  }
               }
            }
         }
      }
   }

   public String getRootDir() {
      return rootDir;
   }

   public void setRootDir(String rootDir) {
      this.rootDir = rootDir;
   }

   public List<ZoomLevelInfo> getZoomLevels() {
      if (zoomLevels == null) {
         zoomLevels = new LinkedList<ZoomLevelInfo>();
      }
      return zoomLevels;
   }

   public void setZoomLevels(List<ZoomLevelInfo> zoomLevels) {
      this.zoomLevels = zoomLevels;
   }

   public void createDefaultZoomLevels(int maxZoomLevel) {
      Layer[] layers = getLayers();
      List<ZoomLevelInfo> zoomLevels = getZoomLevels();
      List<String> layerNames = new LinkedList<String>();
      for (int i = 0; i < layers.length; i++) {
         String layerName = layers[i].getPropertyPrefix();
         if (layerName != null) {
            layerNames.add(layerName);
         } else {
            logger.info("no name for layer[" + i + "]");
         }
      }

      zoomLevels.clear();

      for (int i = 0; i <= maxZoomLevel; i++) {
         ZoomLevelInfo zfi = new ZoomLevelInfo();
         zfi.setZoomLevel(i);
         zfi.setLayers(layerNames);
         zfi.setName("ZoomLayerInfo " + i);
         zfi.setDescription("Tiles for zoom level " + i);
         zfi.setPropertyPrefix("zoom" + i);
         zoomLevels.add(zfi);
      }
   }

   /**
    * @param latlon a Point2D whose x component is the longitude and y component
    *        is the latitude
    * @param zoom GoogleMap style zoom level (0-19 usually)
    * @return The "tile number" whose x and y components each are floating point
    *         numbers that represent the distance in number of tiles from the
    *         origin of the whole map at this zoom level. At zoom=0, the lat,lon
    *         point of 0,0 maps to 0.5,0.5 since there is only one tile at zoom
    *         level 0.
    */
   public static Point2D latLonToTileUV(Point2D latlon, int zoom) {
      return latLonToTileUV(latlon, zoom, null);
   }

   public static Point2D latLonToTileUV(Point2D latlon, int zoom, Point2D ret) {
      if (ret == null) {
         ret = new Point2D.Double();
      }

      ret.setLocation(((latlon.getX() + 180) / 360.0 * Math.pow(2, zoom)), ((1.0 - Math.log(Math.tan(latlon.getY() * Math.PI
            / 180.0)
            + (1.0 / Math.cos(latlon.getY() * Math.PI / 180.0)))
            / Math.PI) / 2.0 * (Math.pow(2, zoom))));
      return ret;
   }

   /**
    * @param tileUV a Point2D whose x,y coordinates represent the distance in
    *        number of tiles (each 256x256) from the origin (where the origin is
    *        90lat,-180lon)
    * @param zoom GoogleMap style zoom level (0-19 usually)
    * @return a Point2D whose x coordinate is the longitude and y coordinate is
    *         the latitude
    */
   public static Point2D tileUVToLatLon(Point2D tileUV, int zoom) {
      return tileUVToLatLon(tileUV, zoom, null);
   }

   @SuppressWarnings("unchecked")
   public static <T extends Point2D> T tileUVToLatLon(Point2D tileUV, int zoom, T ret) {
      if (ret == null) {
         ret = (T) new LatLonPoint.Double();
      }

      ret.setLocation(360 / Math.pow(2, zoom) * tileUV.getX() - 180, -90 + 360 / Math.PI
            * Math.atan(Math.exp((-2 * Math.PI * tileUV.getY()) / Math.pow(2, zoom) + Math.PI)));
      return ret;
   }

   public final static int TILE_SIZE = 256;
   public final static Point2D UVUL = new Point2D.Double(0, 0);
   public final static Point2D UVLR = new Point2D.Double(TILE_SIZE, TILE_SIZE);

   public static float getScaleForZoomAndProjection(Projection proj, int zoom) {
      Point2D originLLUL = tileUVToLatLon(new Point2D.Double(0.0, 0.0), zoom);
      Point2D originLLLR = tileUVToLatLon(new Point2D.Double(1.0, 1.0), zoom);
      return proj.getScale(originLLUL, originLLLR, UVUL, UVLR);
   }

   public static float[] getScalesForZoomLevels(Projection proj, int lowZoomLevel, int highZoomLevel) {
      float[] ret = new float[highZoomLevel - lowZoomLevel + 1];
      for (int i = lowZoomLevel; i <= highZoomLevel; i++) {
         ret[i] = getScaleForZoomAndProjection(proj, i);
      }
      return ret;
   }

   public static void main(String[] args) {

      com.bbn.openmap.util.ArgParser ap = new com.bbn.openmap.util.ArgParser("MapTileMaker");

      ap.add("properties", "The properties file to use for image tiles.", 1);
      ap.add("create", "Create a sample properties file at a path", 1);

      if (!ap.parse(args)) {
         ap.printUsage();
         System.exit(0);
      }

      String arg[];

      Properties props = null;
      arg = ap.getArgValues("properties");
      if (arg != null) {
         String ps = arg[0];
         try {

            URL url = PropUtils.getResourceOrFileOrURL(null, ps);
            InputStream inputStream = url.openStream();

            props = new Properties();
            props.load(inputStream);

            MapTileMaker tim = new MapTileMaker(props);
            tim.makeTiles();

         } catch (MalformedURLException murle) {
            Debug.error("TileMaker can't find properties file: " + arg[0]);
         } catch (IOException ioe) {
            Debug.error("TileMaker can't create images: IOException");
         }
      }

      arg = ap.getArgValues("create");
      if (arg != null) {
         String outputFile = arg[0];
         MapTileMaker tim;
         if (props == null) {
            ShapeLayer shapeLayer = new ShapeLayer();
            props = new Properties();
            props.put("shape.prettyName", "Countries");
            props.put("shape.shapeFile", "data/shape/world/cntry02/cntry02.shp");
            props.put("shape.fillColor", "FFBBBBBB");
            shapeLayer.setProperties("shape", props);

            tim = new MapTileMaker(new Layer[] {
               shapeLayer
            }, new SunJPEGFormatter());
            tim.createDefaultZoomLevels(4);

         } else {
            tim = new MapTileMaker(props);
         }

         Properties configurationProps = new Properties();
         configurationProps = tim.getProperties(configurationProps);

         try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            configurationProps.store(fos, "TileMaker Properties");
            fos.flush();
            fos.close();
         } catch (IOException ioe) {
            logger.warning("caught IOException writing property file: " + ioe.getMessage());
         }

      }

      System.exit(0);
   }

}
