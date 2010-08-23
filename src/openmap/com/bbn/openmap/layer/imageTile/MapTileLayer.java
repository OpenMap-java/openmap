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

import java.util.Properties;
import java.util.logging.Logger;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.dataAccess.mapTile.MapTileFactory;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * A Layer that uses a MapTileFactory to display information (tiles) on the map.
 * 
 * @author dietrick
 */
public class MapTileLayer
      extends OMGraphicHandlerLayer {

   private static final long serialVersionUID = 1L;

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.imageTile.TileLayer");

   public final static String TILE_FACTORY_CLASS_PROPERTY = "tileFactory";
   public final static String INCREMENTAL_UPDATES_PROPERTY = "incrementalUpdates";

   protected MapTileFactory tileFactory;
   protected boolean incrementalUpdates = false;

   public MapTileLayer() {
      setRenderPolicy(new com.bbn.openmap.layer.policy.BufferedImageRenderPolicy(this));
   }

   public MapTileLayer(MapTileFactory tileFactory) {
      this();
      this.tileFactory = tileFactory;
   }

   /**
    * OMGraphicHandlerLayer method, called with projection changes or whenever
    * else doPrepare() is called.
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
