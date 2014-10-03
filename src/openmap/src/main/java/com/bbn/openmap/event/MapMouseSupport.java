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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapMouseSupport.java,v $
// $RCSfile: MapMouseSupport.java,v $
// $Revision: 1.9 $
// $Date: 2008/10/16 19:33:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a utility class that can be used by beans that need support for
 * handling MapMouseListeners and firing MapMouseEvents. You can use an instance
 * of this class as a member field of your bean and delegate work to it.
 * <p>
 * You can set the behavior of how MouseEvents are propagated by setting whether
 * to "consume" events. If the MouseMode is consuming events, then the event is
 * not propagated further than the first listener to successfully process it.
 * Otherwise the event is propagated to all listeners. The default is to consume
 * events.
 */
public class MapMouseSupport
      extends ListenerSupport<MapMouseListener> {

   private static final long serialVersionUID = 1L;

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.event.MapMouseSupport");

   /**
    * The flag that dictates whether the events should be passed to all the
    * listeners or just limited to the first listener that can deal with it. The
    * default value is set to true, which means the event will be consumed by
    * the first layer that can handle it.
    */
   protected boolean consumeEvents = true;

   /**
    * The priority MapMouseListener will be guaranteed to receive events that go
    * hand in hand (pressed - released, etc.).
    */
   protected MapMouseListener priorityListener = null;

   /**
    * Used to determine whether a release should reset the priorityListener on a
    * mouse release.
    */
   protected boolean clickHappened = false;

   /**
    * A MapMouseMode that may be using the parent of this support object as a
    * proxy.
    */
   protected transient MapMouseMode proxy = null;

   protected transient int proxyDistributionMask = 0;

   public final static int PROXY_DISTRIB_MOUSE_PRESSED = 1 << 0;
   public final static int PROXY_ACK_CONSUMED_MOUSE_PRESSED = 1 << 1;
   public final static int PROXY_DISTRIB_MOUSE_RELEASED = 1 << 2;
   public final static int PROXY_ACK_CONSUMED_MOUSE_RELEASED = 1 << 3;
   public final static int PROXY_DISTRIB_MOUSE_CLICKED = 1 << 4;
   public final static int PROXY_ACK_CONSUMED_MOUSE_CLICKED = 1 << 5;
   public final static int PROXY_DISTRIB_MOUSE_MOVED = 1 << 6;
   public final static int PROXY_ACK_CONSUMED_MOUSE_MOVED = 1 << 7;
   public final static int PROXY_DISTRIB_MOUSE_DRAGGED = 1 << 8;
   public final static int PROXY_ACK_CONSUMED_MOUSE_DRAGGED = 1 << 9;
   public final static int PROXY_DISTRIB_MOUSE_ENTERED = 1 << 10;
   public final static int PROXY_DISTRIB_MOUSE_EXITED = 1 << 11;

   protected boolean DEBUG = false;
   protected boolean DEBUG_DETAIL = false;

   /**
    * Construct a default MapMouseSupport. The default value of consumeEvents is
    * set to true.
    */
   public MapMouseSupport() {
      this(null, true);
   }

   /**
    * Construct a default MapMouseSupport. The default value of consumeEvents is
    * set to true.
    * 
    * @param mode the parent MapMouseMode to use with creating the
    *        MapMouseEvent.
    */
   public MapMouseSupport(MapMouseMode mode) {
      this(mode, true);
   }

   /**
    * Construct a MapMouseSupport.
    * 
    * @param shouldConsumeEvents if true, events are propagated to the first
    *        MapMouseListener that successfully processes the event, if false,
    *        events are propagated to all MapMouseListeners
    */
   public MapMouseSupport(boolean shouldConsumeEvents) {
      this(null, shouldConsumeEvents);
   }

   /**
    * Construct a MapMouseSupport.
    * 
    * @param mode the parent MapMouseMode to use with creating the
    *        MapMouseEvent.
    * @param shouldConsumeEvents if true, events are propagated to the first
    *        MapMouseListener that successfully processes the event, if false,
    *        events are propagated to all MapMouseListeners
    */
   public MapMouseSupport(MapMouseMode mode, boolean shouldConsumeEvents) {
      super(mode);

      consumeEvents = shouldConsumeEvents;
      DEBUG = logger.isLoggable(Level.FINE);
      DEBUG_DETAIL = logger.isLoggable(Level.FINER);
   }

   /**
    * Set the parent MapMouseMode to use in constructing MapMouseEvents.
    */
   public void setParentMode(MapMouseMode mode) {
      setSource(mode);
   }

   public MapMouseMode getParentMode() {
      return (MapMouseMode) getSource();
   }

   /**
    * Sets how the mouse support passes out events. If the value passed in is
    * true, the mouse support will only pass the event to the first listener
    * that can respond to the event. If false, the mouse support will pass the
    * event on to all its listeners.
    * 
    * @param shouldConsumeEvents true for limited distribution.
    */
   public void setConsumeEvents(boolean shouldConsumeEvents) {
      consumeEvents = shouldConsumeEvents;
   }

   /**
    * Returns how the mouse support is set up to distribute events.
    * 
    * @return true if only one listener gets to act on an event.
    */
   public boolean isConsumeEvents() {
      return consumeEvents;
   }

   /**
    * Handle a mousePressed MouseListener event.
    * 
    * @param evt MouseEvent to be handled
    */
   public boolean fireMapMousePressed(MouseEvent evt) {
      if (DEBUG) {
         logger.fine("MapMouseSupport.fireMapMousePressed()");
      }

      boolean consumed = false;

      if (DEBUG) {
         logger.fine("  -- has proxy (" + (proxy != null) + ") -- shift used (" + evt.isShiftDown() + ")");
      }

      if (proxy == null || evt.isShiftDown() || (proxyDistributionMask & PROXY_DISTRIB_MOUSE_PRESSED) > 0) {

         evt = new MapMouseEvent(getParentMode(), evt);

         if (DEBUG && proxy != null && evt.isShiftDown()) {
            logger.fine("MMS.fireMapMousePressed(): proxy enabled, but side stepping to send event to primary listeners");
         }

         Iterator<MapMouseListener> it = iterator();
         while (it.hasNext() && !consumed) {
            MapMouseListener target = it.next();
            consumed = target.mousePressed(evt) && consumeEvents;

            if (consumed) {
               priorityListener = target;
            }
         }
      }

      boolean ignoreConsumed = !consumed || (consumed && ((proxyDistributionMask & PROXY_ACK_CONSUMED_MOUSE_PRESSED) == 0));

      if (proxy != null && ignoreConsumed && !evt.isShiftDown()) {
         proxy.mousePressed(evt);
         consumed = true;
      } else {
         if (DEBUG && evt.isShiftDown()) {
            logger.fine("MMS.fireMapMousePressed(): side-stepped proxy");
         }
      }

      return consumed;
   }

   /**
    * Handle a mouseReleased MouseListener event. Checks to see if there is a
    * priorityListener, and will direct the event to that listener. The
    * priorityListener variable will be reset to null. If there is not a
    * priorityListener, the event is passed through the listeners, subject to
    * the consumeEvents mode.
    * 
    * @param evt MouseEvent to be handled.
    */
   public boolean fireMapMouseReleased(MouseEvent evt) {
      if (DEBUG) {
         logger.fine("MapMouseSupport: fireMapMouseReleased");
      }

      boolean consumed = false;

      evt = new MapMouseEvent(getParentMode(), evt);

      if (priorityListener != null) {
         priorityListener.mouseReleased(evt);
         if (!clickHappened) {
            priorityListener = null;
         }
         consumed = true;
      }

      if (proxy == null || evt.isShiftDown() || (proxyDistributionMask & PROXY_DISTRIB_MOUSE_RELEASED) > 0) {

         Iterator<MapMouseListener> it = iterator();
         while (it.hasNext() && !consumed) {
            consumed = it.next().mouseReleased(evt) && consumeEvents;
         }
      }

      boolean ignoreConsumed = !consumed || (consumed && ((proxyDistributionMask & PROXY_ACK_CONSUMED_MOUSE_RELEASED) == 0));

      if (proxy != null && ignoreConsumed && !evt.isShiftDown()) {
         proxy.mouseReleased(evt);
         consumed = true;
      }

      return consumed;
   }

   /**
    * Handle a mouseClicked MouseListener event. If the priorityListener is set,
    * it automatically gets the clicked event. If it is not set, the other
    * listeners get a shot at the event according to the consumeEvent mode.
    * 
    * @param evt MouseEvent to be handled.
    */
   public boolean fireMapMouseClicked(MouseEvent evt) {
      if (DEBUG) {
         logger.fine("MapMouseSupport: fireMapMouseClicked");
      }

      clickHappened = true;
      boolean consumed = false;

      evt = new MapMouseEvent(getParentMode(), evt);

      if (priorityListener != null && evt.getClickCount() > 1) {
         priorityListener.mouseClicked(evt);
         consumed = true;
      }

      priorityListener = null;

      if (proxy == null || evt.isShiftDown() || (proxyDistributionMask & PROXY_DISTRIB_MOUSE_CLICKED) > 0) {

         Iterator<MapMouseListener> it = iterator();

         while (it.hasNext() && !consumed) {
            MapMouseListener target = it.next();
            consumed = target.mouseClicked(evt) && consumeEvents;

            if (consumed) {
               priorityListener = target;
            }
         }
      }

      boolean ignoreConsumed = !consumed || (consumed && ((proxyDistributionMask & PROXY_ACK_CONSUMED_MOUSE_CLICKED) == 0));

      if (proxy != null && ignoreConsumed && !evt.isShiftDown()) {
         proxy.mouseClicked(evt);
         consumed = true;
      }

      return consumed;
   }

   /**
    * Handle a mouseEntered MouseListener event.
    * 
    * @param evt MouseEvent to be handled
    * @return true if there was a target to send the event to.
    */
   public boolean fireMapMouseEntered(MouseEvent evt) {
      if (DEBUG) {
         logger.fine("MapMouseSupport: fireMapMouseEntered");
      }

      boolean consumed = false;

      if (proxy == null || evt.isShiftDown() || (proxyDistributionMask & PROXY_DISTRIB_MOUSE_ENTERED) > 0) {

         evt = new MapMouseEvent(getParentMode(), evt);

         for (MapMouseListener listener : this) {
            listener.mouseEntered(evt);
            consumed = true;
         }
      }

      if (proxy != null && !evt.isShiftDown()) {
         proxy.mouseEntered(evt);
         consumed = true;
      }

      return consumed;
   }

   /**
    * Handle a mouseExited MouseListener event.
    * 
    * @param evt MouseEvent to be handled
    * @return true if there was a target to send the event to.
    */
   public boolean fireMapMouseExited(MouseEvent evt) {
      if (DEBUG) {
         logger.fine("MapMouseSupport: fireMapMouseExited");
      }

      boolean consumed = false;

      if (proxy == null || evt.isShiftDown() || (proxyDistributionMask & PROXY_DISTRIB_MOUSE_EXITED) > 0) {

         evt = new MapMouseEvent(getParentMode(), evt);

         for (MapMouseListener listener : this) {
            listener.mouseExited(evt);
            consumed = true;
         }
      }

      if (proxy != null && !evt.isShiftDown()) {
         proxy.mouseExited(evt);
         consumed = true;
      }

      return consumed;
   }

   /**
    * Handle a mouseDragged MouseListener event.
    * 
    * @param evt MouseEvent to be handled
    * @return false.
    */
   public boolean fireMapMouseDragged(MouseEvent evt) {
      if (DEBUG_DETAIL) {
         logger.finer("MapMouseSupport: fireMapMouseDragged");
      }

      clickHappened = false;
      boolean consumed = false;

      if (proxy == null || evt.isShiftDown() || (proxyDistributionMask & PROXY_DISTRIB_MOUSE_DRAGGED) > 0) {

         evt = new MapMouseEvent(getParentMode(), evt);

         Iterator<MapMouseListener> it = iterator();

         while (it.hasNext() && !consumed) {
            consumed = it.next().mouseDragged(evt) && consumeEvents;
         }
      }

      boolean ignoreConsumed = !consumed || (consumed && ((proxyDistributionMask & PROXY_ACK_CONSUMED_MOUSE_DRAGGED) == 0));

      if (proxy != null && ignoreConsumed && !evt.isShiftDown()) {
         proxy.mouseDragged(evt);
         consumed = true;
      }

      return consumed;
   }

   /**
    * Handle a mouseMoved MouseListener event. If the moved event is consumed,
    * the rest of the listeners that didn't have a chance to respond get called
    * in the mouse moved method without arguments.
    * 
    * @param evt MouseEvent to be handled
    * @return true if the event was consumed.
    */
   public boolean fireMapMouseMoved(MouseEvent evt) {
      if (DEBUG_DETAIL) {
         logger.fine("MapMouseSupport: fireMapMouseMoved");
      }

      boolean consumed = false;

      if (proxy == null || evt.isShiftDown() || (proxyDistributionMask & PROXY_DISTRIB_MOUSE_MOVED) > 0) {

         evt = new MapMouseEvent(getParentMode(), evt);

         Iterator<MapMouseListener> it = iterator();

         while (it.hasNext()) {
            MapMouseListener target = it.next();
            if (consumed) {
               target.mouseMoved();
            } else {
               consumed = target.mouseMoved(evt);
            }
         }
      }

      // consumed was used above to figure out whether to send
      // mouseMoved(evt) or mouseMoved(), now we have to set it
      // based on whether the MouseMode should be consuming events.
      consumed &= consumeEvents;

      boolean ignoreConsumed = !consumed || (consumed && ((proxyDistributionMask & PROXY_ACK_CONSUMED_MOUSE_MOVED) == 0));

      if (proxy != null && ignoreConsumed && !evt.isShiftDown()) {
         proxy.mouseMoved(evt);
         consumed = true;
      }

      return consumed;
   }

   /**
    * Request to have the parent MapMouseMode act as a proxy for a MapMouseMode
    * that wants to remain hidden. Can be useful for directing events to one
    * object.
    * 
    * @param mmm the hidden MapMouseMode for this MapMouseMode to send events
    *        to.
    * @param pdm the proxy distribution mask to use, which lets this support
    *        object notify its targets of events if the parent is acting as a
    *        proxy.
    * @return true if the proxy setup (essentially a lock) is successful, false
    *         if the proxy is already set up for another listener.
    */
   protected synchronized boolean setProxyFor(MapMouseMode mmm, int pdm) {
      proxyDistributionMask = pdm;
      if (proxy == null || proxy == mmm) {
         proxy = mmm;
         return true;
      }
      return false;
   }

   /**
    * Can check if the MapMouseMode is acting as a proxy for another
    * MapMouseMode.
    */
   public synchronized boolean isProxyFor(MapMouseMode mmm) {
      return proxy == mmm;
   }

   /**
    * Release the proxy lock on the MapMouseMode. Resets the proxy distribution
    * mask.
    */
   protected synchronized void releaseProxy() {
      proxy = null;
      proxyDistributionMask = 0;
   }

   /**
    * @return MapMouseMode being proxied (the hidden MapMouseMode that the
    *         parent mode is providing events to).
    */
   public synchronized MapMouseMode getProxied() {
      return proxy;
   }

   /**
    * Set the mask that dictates which events get sent to this support object's
    * targets even if the parent mouse mode is acting as a proxy.
    */
   protected void setProxyDistributionMask(int mask) {
      proxyDistributionMask = mask;
   }

   /**
    * Get the mask that dictates which events get sent to this support object's
    * targets even if the parent mouse mode is acting as a proxy.
    */
   protected int getProxyDistributionMask() {
      return proxyDistributionMask;
   }
}