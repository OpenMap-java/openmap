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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapMouseMode.java,v $
// $RCSfile: MapMouseMode.java,v $
// $Revision: 1.7 $
// $Date: 2005/12/16 14:14:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.Cursor;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

/**
 * Interface for handling mouse behavior while the mouse is operating over the
 * MapBean. A "MouseMode" object exists to interpret the meaning of mouse
 * events. For instance, you could have a mode where mouse events (click,
 * drag-select) are interpreted as navigation commands, (recenter,
 * zoom-and-recenter). There may be other modes depending on how your
 * application wants to interpret MouseEvents.
 * 
 * @see AbstractMouseMode
 * @see NavMouseMode
 * @see SelectMouseMode
 * @see NullMouseMode
 */
public interface MapMouseMode
      extends MouseListener, MouseMotionListener, MouseWheelListener, PaintListener {

   /**
    * Returns the id (MapMouseMode name). This name should be unique for each
    * MapMouseMode.
    * 
    * @return String ID
    */
   public String getID();

   /**
    * Return a pretty name, suitable for the GUI.
    */
   public String getPrettyName();

   /**
    * Gets the mouse cursor recommended for use when this mouse mode is active.
    * 
    * @return Cursor the mouse cursor recommended for use when this mouse mode
    *         is active.
    */
   public Cursor getModeCursor();

   /**
    * Gets the Icon to represent the Mouse Mode in a GUI.
    */
   public Icon getGUIIcon();

   /**
    * Add a MapMouseListener to the MouseMode.
    * 
    * @param l the MapMouseListener to add.
    */
   public void addMapMouseListener(MapMouseListener l);

   /**
    * Remove a MapMouseListener from the MouseMode.
    * 
    * @param l the MapMouseListener to remove.
    */
   public void removeMapMouseListener(MapMouseListener l);

   /**
    * Remove all MapMouseListeners from the mode.
    */
   public void removeAllMapMouseListeners();

   /**
    * Let the MapMouseMode know if it is active or not. Called by the
    * MouseDelegator.
    * 
    * @param active true if the MapMouseMode has been made the active one, false
    *        if it has been set inactive.
    */
   public void setActive(boolean active);

   /**
    * Lets the MouseDelegator know if the MapMouseMode should be visible in the
    * GUI, in order to create certain mouse modes that may be controlled by
    * other tools.
    */
   public boolean isVisible();

   /**
    * Request to have the MapMouseMode act as a proxy for a MapMouseMode that
    * wants to remain hidden. Can be useful for directing events to one object.
    * With this call, no events will be forwarded to the proxy's target.
    * 
    * @param mmm the hidden MapMouseMode for this MapMouseMode to send events
    *        to.
    * @return true if the proxy setup (essentially a lock) is successful, false
    *         if the proxy is already set up for another listener.
    */
   public boolean actAsProxyFor(MapMouseMode mmm);

   /**
    * Request to have the MapMouseMode act as a proxy for a MapMouseMode that
    * wants to remain hidden. Can be useful for directing events to one object.
    * 
    * @param mmm the hidden MapMouseMode for this MapMouseMode to send events
    *        to.
    * @param pdm the proxy distribution mask to use, which lets this proxy
    *        notify its targets of events.
    * @return true if the proxy setup (essentially a lock) is successful, false
    *         if the proxy is already set up for another listener.
    */
   public boolean actAsProxyFor(MapMouseMode mmm, int pdm);

   /**
    * Can check if the MapMouseMode is acting as a proxy for a MapMouseMode.
    */
   public boolean isProxyFor(MapMouseMode mmm);

   /**
    * Release the proxy lock on the MapMouseMode.
    */
   public void releaseProxy();

   /**
    * @return the mouse mode being proxied.
    */
   public MapMouseMode getProxied();

   /**
    * Set the mask that dictates which events get sent to this support object's
    * targets even if the parent mouse mode is acting as a proxy.
    * 
    * @see MapMouseSupport for definitions of mask bits.
    */
   public void setProxyDistributionMask(int mask);

   /**
    * Get the mask that dictates which events get sent to this support object's
    * targets even if the parent mouse mode is acting as a proxy.
    * 
    * @see MapMouseSupport for definitions of mask bits.
    */
   public int getProxyDistributionMask();

   /**
    * The MapMouseMode should send out notifications when a proxy is added or
    * removed.
    * 
    * @param pcl listener to notify.
    */
   public void addPropertyChangeListener(PropertyChangeListener pcl);

   public void removePropertyChangeListener(PropertyChangeListener pcl);

}