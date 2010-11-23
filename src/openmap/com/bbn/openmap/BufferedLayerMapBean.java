// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/BufferedLayerMapBean.java,v $
// $RCSfile: BufferedLayerMapBean.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.event.ContainerEvent;
import java.util.logging.Level;

import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.layer.BufferedLayer;

/**
 * The BufferedLayerMapBean is a BufferedMapBean with an additional image buffer
 * that holds Layers designated as background layers. The additional image
 * buffer is a BufferedLayer that this MapBean manages, and all background
 * layers are added to the BufferedLayer, which is automatically added to the
 * bottom of the map. When layers are added to the MapBean via the setLayers()
 * method, the Layer.getAddAsBackground() flag is checked, and if that is true
 * for a layer, it is added to the BufferedLayer. The background layers do not
 * receive mouse events.
 * <P>
 * 
 * It should be cautioned that the appearance of the map may not match the layer
 * stack as it is delivered to the MapBean because of this flag. If, for
 * example, layers 1 and 4 are marked as background layers, while layers 2 and 3
 * are not (in a 4 layer stack), then the map will show layers 2, 3, 1, 4, with
 * layers 1 and 4 being displayed from the BufferedLayer. Something to think
 * about when it comes to designing GUI elements.
 */
public class BufferedLayerMapBean
      extends BufferedMapBean {

   protected BufferedLayer bufferedLayer;

   protected boolean DEBUG = false;

   /**
    * Construct a MapBean.
    */
   public BufferedLayerMapBean() {
      super();
      DEBUG = logger.isLoggable(Level.FINE);
   }

   public BufferedLayerMapBean(boolean useThreadedNotification) {
      super(useThreadedNotification);
      DEBUG = logger.isLoggable(Level.FINE);
   }

   /**
    * Set the background color of the map. Actually sets the background color of
    * the projection, and calls repaint().
    * 
    * @param color java.awt.Color.
    */
   public void setBackgroundColor(Color color) {
      super.setBackground(color);
      getBufferedLayer().setBackground(color);
   }

   public void setBckgrnd(Paint paint) {
      super.setBckgrnd(paint);
      getBufferedLayer().setBckgrnd(paint);
   }

   public synchronized void setBufferedLayer(BufferedLayer bl) {
      bufferedLayer = bl;
   }

   public synchronized BufferedLayer getBufferedLayer() {
      if (bufferedLayer == null) {
         bufferedLayer = new BufferedLayer();
         addPropertyChangeListener(bufferedLayer);
         bufferedLayer.setName("Background Layers");
      }

      return bufferedLayer;
   }

   /**
    * Set the MapBeanRepaintPolicy used by the MapBean. This method is
    * overridden in order to pass the policy on to the MapBean stored in the
    * internal BufferedLayer.
    */
   public void setMapBeanRepaintPolicy(MapBeanRepaintPolicy mbrp) {
      super.setMapBeanRepaintPolicy(mbrp);

      MapBean mb = getBufferedLayer().getMapBean();
      if (mb != null) {
         if (mbrp == null) {
            mb.setMapBeanRepaintPolicy(mbrp);
         } else {
            MapBeanRepaintPolicy mbrp2 = (MapBeanRepaintPolicy) mbrp.clone();
            mb.setMapBeanRepaintPolicy(mbrp2);
            mbrp2.setMap(mb);
         }
      }
   }

   /**
    * LayerListener interface method. A list of layers will be added, removed,
    * or replaced based on on the type of LayerEvent.
    * 
    * @param evt a LayerEvent
    */
   public void setLayers(LayerEvent evt) {
      bufferDirty = true;
      Layer[] layers = evt.getLayers();
      int type = evt.getType();

      if (type == LayerEvent.ALL) {
         // Don't care about these at all...
         return;
      }

      // @HACK is this cool?:
      if (layers == null) {
         logger.warning("layer[] is null!");
         return;
      }

      boolean oldChange = getDoContainerChange();
      setDoContainerChange(false);

      BufferedLayer bufLayer;

      synchronized (this) {
         bufLayer = getBufferedLayer();
      }

      // use LayerEvent.REPLACE when you want to remove all current
      // layers
      // add a new set
      if (type == LayerEvent.REPLACE) {
         if (DEBUG) {
            debugmsg("Replacing all layers");
         }
         removeAll();
         bufLayer.clearLayers();

         for (int i = 0; i < layers.length; i++) {
            // @HACK is this cool?:
            if (layers[i] == null) {
               logger.warning("layer " + i + " is null");
               continue;
            }

            if (DEBUG) {
               debugmsg("Adding layer[" + i + "]= " + layers[i].getName());
            }

            if (layers[i].getAddAsBackground()) {
               if (DEBUG) {
                  logger.fine("Adding layer[" + i + "]= " + layers[i].getName() + " to background");
               }

               bufLayer.addLayer(layers[i]);
            } else {
               add(layers[i]);
            }

            layers[i].setVisible(true);
         }

         if (bufLayer.hasLayers()) {
            add(bufLayer);
         }
      }

      // use LayerEvent.ADD when adding and/or reshuffling layers
      else if (type == LayerEvent.ADD) {

         remove(bufLayer);

         if (DEBUG) {
            debugmsg("Adding new layers");
         }
         for (int i = 0; i < layers.length; i++) {
            if (DEBUG) {
               debugmsg("Adding layer[" + i + "]= " + layers[i].getName());
            }

            // add(layers[i]);
            layers[i].setVisible(true);

            if (layers[i].getAddAsBackground()) {
               if (DEBUG) {
                  debugmsg("Adding layer[" + i + "]= " + layers[i].getName() + " to background");
               }
               bufLayer.addLayer(layers[i]);
            } else {
               add(layers[i]);
            }
         }

         if (bufLayer.hasLayers()) {
            add(bufLayer);
         }
      }

      // use LayerEvent.REMOVE when you want to delete layers from
      // the map
      else if (type == LayerEvent.REMOVE) {
         if (DEBUG) {
            debugmsg("Removing layers");
         }
         for (int i = 0; i < layers.length; i++) {
            if (DEBUG) {
               debugmsg("Removing layer[" + i + "]= " + layers[i].getName());
            }
            remove(layers[i]);
            bufLayer.removeLayer(layers[i]);
         }
      }

      if (!layerRemovalDelayed) {
         purgeAndNotifyRemovedLayers();
      }

      setDoContainerChange(oldChange);
      repaint();
      revalidate();
   }

   /**
    * ContainerListener Interface method. Should not be called directly. Part of
    * the ContainerListener interface, and it's here to make the MapBean a good
    * Container citizen.
    * 
    * @param e ContainerEvent
    */
   protected void changeLayers(ContainerEvent e) {
      // Container Changes can be disabled to speed adding/removing
      // multiple layers
      if (!doContainerChange) {
         return;
      }

      Component[] comps = this.getComponents();
      int ncomponents = comps.length;
      int nBufLayerComponents = 0;

      BufferedLayer bufLayer;
      synchronized (this) {
         bufLayer = getBufferedLayer();
      }

      if (ncomponents == 0 || comps[ncomponents - 1] != bufLayer) {
         super.changeLayers(e);
         return;
      }

      Component[] bufLayers = bufLayer.getLayers();
      nBufLayerComponents = bufLayers.length;

      // Take 1 off for the bufLayer
      Layer[] newLayers = new Layer[ncomponents + nBufLayerComponents - 1];
      System.arraycopy(comps, 0, newLayers, 0, ncomponents - 1);
      System.arraycopy(bufLayers, 0, newLayers, ncomponents - 1, nBufLayerComponents);

      if (DEBUG)
         debugmsg("changeLayers() - firing change");
      firePropertyChange(LayersProperty, currentLayers, newLayers);

      // Tell the new layers that they have been added
      for (int i = 0; i < addedLayers.size(); i++) {
         ((Layer) addedLayers.elementAt(i)).added(this);
      }
      addedLayers.removeAllElements();

      currentLayers = newLayers;
   }

   /**
    * Call when getting rid of the MapBean, it releases pointers to all
    * listeners and kills the ProjectionSupport thread.
    */
   public void dispose() {
      if (bufferedLayer != null) {
         bufferedLayer.dispose();
      }
      super.dispose();
   }

   /*
    * Not sure why this method was overwritten in this subclass, because it
    * marks the buffered layer as dirty, causing it to be re-created. That
    * defeats the whole point of the BufferedLayer in the BufferedLayerMapBean.
    * The layers that are part of the BufferedLayer know how to get the
    * BLMapBean to refresh itself when needed, so this method is OK as the
    * superclass runs it.
    */

   // /**
   // * Marks the image buffer as dirty if value is true. On the next
   // * <code>paintChildren()</code>, we will call <code>paint()</code> on all
   // * Layer components.
   // *
   // * @param value boolean
   // */
   // public void setBufferDirty(boolean value) {
   // super.setBufferDirty(value);
   // if (bufferedLayer != null && value) {
   // bufferedLayer.setBufferDirty(true);
   // }
   // }

}