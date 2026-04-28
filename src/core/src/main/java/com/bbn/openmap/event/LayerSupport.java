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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/LayerSupport.java,v $
// $RCSfile: LayerSupport.java,v $
// $Revision: 1.9 $
// $Date: 2008/10/16 19:33:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.Layer;

/**
 * This is a utility class that can be used by beans that need support for
 * handling LayerListeners and firing LayerEvents. You can use an instance of
 * this class as a member field of your bean and delegate work to it.
 */
public class LayerSupport
      extends ListenerSupport<LayerListener> {

   static Logger logger = Logger.getLogger("com.bbn.openmap.event.LayerSupport");
   protected boolean synchronous = true;

   /**
    * Construct a LayerSupport.
    * 
    * @param sourceBean The bean to be given as the source for any events.
    */
   public LayerSupport(Object sourceBean) {
      super(sourceBean);
      logger.fine("LayerSupport created");
   }

   /**
    * Send a layer event to all registered listeners.
    * 
    * @param type the event type: one of ADD, REMOVE, REPLACE
    * @param layers the list of layers
    * @see LayerEvent
    */
   public void fireLayer(int type, Layer[] layers) {

      if (logger.isLoggable(Level.FINE)) {
         logger.fine("calling setLayers on " + size() + " objects");
      }

      if (isEmpty())
         return;

      LayerEvent evt = new LayerEvent(source, type, layers);
      for (LayerListener listener : this) {
         listener.setLayers(evt);
      }
   }

   /**
    * Used to see if another Thread object needs to be created.
    */
   protected Thread t;
   /**
    * Event information stack.
    */
   protected Vector<SetLayerRunnable> events = new Vector<SetLayerRunnable>();

   /**
    * Pushed the information onto a Vector stack to get executed by a separate
    * thread. Any thread launched is held on to, and if that thread is is null
    * or not active, a new thread is kicked off. The dying thread checks the
    * Vector stack and fires another event if it can.
    * 
    * @param layerEventType
    * @param layers
    */
   public synchronized void pushLayerEvent(int layerEventType, Layer[] layers) {

      if (synchronous) {
         fireLayer(layerEventType, layers);
      } else {

         events.add(new SetLayerRunnable(layerEventType, layers));

         if (t == null || !t.isAlive()) {
            SetLayerRunnable runnable = popLayerEvent();
            if (runnable != null) {
               t = new Thread(runnable);
               t.start();
            }
         }
      }
   }

   /**
    * Return the first event on the stack, may be null if there is nothing to
    * do.
    */
   public synchronized SetLayerRunnable popLayerEvent() {
      try {
         return events.remove(0);
      } catch (ArrayIndexOutOfBoundsException aioobe) {
         return null;
      }
   }

   /**
    * A reusable Runnable used by a thread to notify listeners when layers are
    * turned on/off or shuffled.
    */
   protected class SetLayerRunnable
         implements Runnable {
      protected int layerEventType;
      protected Layer[] layers;

      public SetLayerRunnable(int let, Layer[] lrs) {
         layerEventType = let;
         layers = lrs;
      }

      public int getEventType() {
         return layerEventType;
      }

      public Layer[] getLayers() {
         return layers;
      }

      public void run() {
         doIt(getEventType(), getLayers());
         SetLayerRunnable runnable = popLayerEvent();
         while (runnable != null) {
            doIt(runnable.getEventType(), runnable.getLayers());
            runnable = popLayerEvent();
         }
      }

      public void doIt(int eventType, Layer[] layers) {
         logger.fine("firing LayerEvent on LayerListeners");
         fireLayer(eventType, layers);
      }
   }

   public boolean isSynchronous() {
      return synchronous;
   }

   public void setSynchronous(boolean synchronous) {
      this.synchronous = synchronous;
   }
}