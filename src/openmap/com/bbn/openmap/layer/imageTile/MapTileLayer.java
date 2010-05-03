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

import com.bbn.openmap.dataAccess.mapTile.MapTileFactory;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.ListResetPCPolicy;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * A Layer that uses a MapTileFactory to display information (tiles) on the
 * map.
 * 
 * @author dietrick
 */
public class MapTileLayer
      extends OMGraphicHandlerLayer {

   private static final long serialVersionUID = 1L;

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.imageTile.TileLayer");

   public final static String TILE_FACTORY_CLASS_PROPERTY = "tileFactory";

   protected MapTileFactory tileFactory;

   public MapTileLayer() {
      setProjectionChangePolicy(new ListResetPCPolicy(this));
   }

   public MapTileLayer(MapTileFactory tileFactory) {
      this();
      this.tileFactory = tileFactory;
   }

   public synchronized OMGraphicList prepare() {

      Projection projection = getProjection();

      if (projection == null) {
         return null;
      }

      if (tileFactory != null) {
         OMGraphicList newList = new OMGraphicList();
         setList(newList);
         return tileFactory.getTiles(projection, -1, newList);
      } else {
         return null;
      }
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
   }

   public MapTileFactory getTileFactory() {
      return tileFactory;
   }

   public void setTileFactory(MapTileFactory tileFactory) {
      logger.fine("setting tile factory to: " + tileFactory.getClass().getName());
      tileFactory.setRepaintCallback(this);
      this.tileFactory = tileFactory;
   }

}
