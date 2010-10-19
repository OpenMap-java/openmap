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

package com.bbn.openmap.layer.imageTile;

import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.dataAccess.mapTile.MapTileFactory;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ClasspathHacker;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * A Layer that uses a MapTileFactory to display information (tiles) on the map.
 * Properties for this layer look like this:
 * 
 * <pre>
 * 
 * tiles.class=com.bbn.openmap.layer.imageTile.MapTileLayer
 * tiles.prettyName=TILES
 * tiles.tileFactory=com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory
 * tiles.jar=mapTilesInJar.jar (optional, for runtime jar loading)
 * tiles.rootDir=root_directory_of_tiles
 * tiles.fileExt=.png
 * tiles.cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * # transform for naming convention of tiles default is OSMMapTileCoordinateTransform, but it depends on the source of tiles.  GDAL is TSMMapTileCoordinateTransform
 * mapTileTransform=com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform, or com.bbn.openmap.dataAccess.mapTile.TSMMapTileCoordinateTransform
 * 
 * </pre>
 * 
 * You can also have:
 * 
 * <pre>
 * 
 * tiles.class=com.bbn.openmap.layer.imageTile.MapTileLayer
 * tiles.prettyName=TILES
 * tiles.tileFactory=com.bbn.openmap.dataAccess.mapTile.ServerMapTileFactory
 * tiles.rootDir=URL root directory of tiles
 * # a local location to cache tiles, to reduce load on server.
 * tiles.localCacheRootDir=/data/tiles/osmtiles
 * 
 * # other properties are the same.
 * tiles.fileExt=.png
 * tiles.cacheSize=the number of mapTiles the factory should hold on to. The default is 100.
 * # transform for naming convention of tiles default is OSMMapTileCoordinateTransform, but it depends on the source of tiles.  GDAL is TSMMapTileCoordinateTransform
 * mapTileTransform=com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform, or com.bbn.openmap.dataAccess.mapTile.TSMMapTileCoordinateTransform
 * 
 * </pre>
 * 
 * @author dietrick
 */
public class MapTileLayer
      extends OMGraphicHandlerLayer {

   private static final long serialVersionUID = 1L;

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.imageTile.TileLayer");

   /**
    * Property that sets the class name of the MapTileFactory to use for this
    * layer.
    */
   public final static String TILE_FACTORY_CLASS_PROPERTY = "tileFactory";
   /**
    * Property to allow the MapTileFactory to call repaint on this layer as map
    * tiles become available. Default is false, enabling it will not allow this
    * layer to be used with an ImageServer (renderDataForProjection won't work).
    */
   public final static String INCREMENTAL_UPDATES_PROPERTY = "incrementalUpdates";
   /**
    * Property to allow jar files to be dynamically added to the classpath at
    * runtime. Each path to a jar file should be separated by a semi-colon.
    */
   public final static String MAP_DATA_JAR_PROPERTY = "jar";

   /**
    * The MapTileFactory that knows how to fetch image files and create
    * OMRasters for them.
    */
   protected MapTileFactory tileFactory;
   /**
    * Flag to allow this layer to set itself as a repaint callback object on the
    * tile factory.
    */
   protected boolean incrementalUpdates = false;
   /**
    * The path to jar files as set in the properties. The jar files, if valid,
    * will be added to the classpath at runtime. If a jar file is set, then any
    * path to image tiles should be specified from the top-level directory
    * inside the jar file as a relative path. We only keep track of this if we
    * write the properties back out again, so it's basically whatever the
    * property was set to when the layer was configured.
    */
   protected String jarFileNames = null;

   public MapTileLayer() {
      setRenderPolicy(new com.bbn.openmap.layer.policy.BufferedImageRenderPolicy(this));
   }

   public MapTileLayer(MapTileFactory tileFactory) {
      this();
      this.tileFactory = tileFactory;
   }

   /**
    * OMGraphicHandlerLayer method, called with projection changes or whenever
    * else doPrepare() is called. Calls getTiles on the map tile factory.
    * 
    * @return OMGraphicList that contains tiles to be displayed for the current
    *         projection.
    */
   public synchronized OMGraphicList prepare() {

      Projection projection = getProjection();

      if (projection == null) {
         return null;
      }

      if (tileFactory != null) {
         OMGraphicList newList = new OMGraphicList();
         setList(newList);
         return tileFactory.getTiles(projection, -1, newList);
      }
      return null;
   }
   
   public String getToolTipTextFor(OMGraphic omg) {
      return (String) omg.getAttribute(OMGraphic.TOOLTIP);
   }

   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      String tileFactoryClassString = props.getProperty(prefix + TILE_FACTORY_CLASS_PROPERTY);
      if (tileFactoryClassString != null) {
         MapTileFactory itf = (MapTileFactory) ComponentFactory.create(tileFactoryClassString, prefix, props);
         if (itf != null) {
            setTileFactory(itf);
         }
      }

      incrementalUpdates = PropUtils.booleanFromProperties(props, prefix + INCREMENTAL_UPDATES_PROPERTY, incrementalUpdates);

      String jarString = props.getProperty(prefix + MAP_DATA_JAR_PROPERTY);
      if (jarString != null) {
         jarFileNames = jarString;

         Vector<String> jarNames = PropUtils.parseMarkers(jarFileNames, ";");
         for (String jarName : jarNames) {
            try {
               logger.fine("adding " + jarName + " to classpath");
               ClasspathHacker.addFile(jarName);
            } catch (IOException ioe) {
               logger.warning("couldn't add map data jar file: " + jarName);
            }
         }
      }

   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);

      String prefix = PropUtils.getScopedPropertyPrefix(this);
      if (tileFactory != null) {
         props.put(prefix + TILE_FACTORY_CLASS_PROPERTY, tileFactory.getClass().getName());
         if (tileFactory instanceof PropertyConsumer) {
            ((PropertyConsumer) tileFactory).getProperties(props);
         }
      }

      props.put(prefix + INCREMENTAL_UPDATES_PROPERTY, Boolean.toString(incrementalUpdates));
      if (jarFileNames != null) {
         props.put(prefix + MAP_DATA_JAR_PROPERTY, jarFileNames);
      }

      return props;
   }

   public MapTileFactory getTileFactory() {
      return tileFactory;
   }

   public void setTileFactory(MapTileFactory tileFactory) {
      logger.fine("setting tile factory to: " + tileFactory.getClass().getName());
      // This allows for general faster response, but causes the map to jump
      // around a little bit when used with the BufferedImageRenderPolicy and
      // when the projection changes occur rapidly, like when zooming and
      // panning several times in a second. The generation/positioning can't
      // keep up. It'll settle out, but it might be better to be slower and
      // less confusing to the user.

      if (incrementalUpdates) {
         tileFactory.setRepaintCallback(this);
      }

      this.tileFactory = tileFactory;
   }

   public boolean isIncrementalUpdates() {
      return incrementalUpdates;
   }

   public void setIncrementalUpdates(boolean incrementalUpdates) {
      this.incrementalUpdates = incrementalUpdates;
      if (tileFactory != null) {
         if (!incrementalUpdates) {
            tileFactory.setRepaintCallback(null);
         } else {
            tileFactory.setRepaintCallback(this);
         }
      }
   }

}
