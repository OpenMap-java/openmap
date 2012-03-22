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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/MapMouseInterpreter.java,v $
// $RCSfile: MapMouseInterpreter.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * A MapMouseInterpreter is an extension of the MapMouseListener that
 * has some understanding how MouseEvents on a Map relate to an
 * OMGraphicList, and what kind of actions should be taken on the
 * OMGraphics on a list. This interface describes specific actions
 * being taken on OMGraphics over a map, and the implementing class
 * can then take this information to make more abstract calls on its
 * GestureResponsePolicy.
 */
public interface MapMouseInterpreter extends MapMouseListener {

    /**
     * A method for the GestureResponsePolicy to find out what the
     * last MouseEvent was, in case it needs it to react to the GRP
     * notifications.
     */
    public MouseEvent getCurrentMouseEvent();

    /**
     * Notification that the background was left-clicked upon.
     */
    public boolean leftClick(MouseEvent me);

    /**
     * Notification that a particular OMGraphic was left-clicked upon.
     */
    public boolean leftClick(OMGraphic omg, MouseEvent me);

    /**
     * Notification that a particular OMGraphic, previously
     * left-clicked upon, has been un-clicked. Most likely due to the
     * user clicking on another OMGraphic, using a different mouse
     * button to click on this same OMGraphic, or when the background
     * map was clicked upon.
     */
    public boolean leftClickOff(OMGraphic omg, MouseEvent me);

    /**
     * Notification that the background was right-clicked upon.
     */
    public boolean rightClick(MouseEvent me);

    /**
     * Notification that a particular OMGraphic was right-clicked
     * upon.
     */
    public boolean rightClick(OMGraphic omg, MouseEvent me);

    /**
     * Notification that a particular OMGraphic, previously
     * right-clicked upon, has been un-clicked. Most likely due to the
     * user clicking on another OMGraphic, using a different mouse
     * button to click on this same OMGraphic, or when the background
     * map was clicked upon.
     */
    public boolean rightClickOff(OMGraphic omg, MouseEvent me);

    /**
     * Notification that the mouse is being moved over the map at a
     * certain location, and is not over any OMGraphics.
     */
    public boolean mouseOver(MouseEvent me);

    /**
     * Notification that the mouse is over a particluar OMGraphic.
     */
    public boolean mouseOver(OMGraphic omg, MouseEvent me);

    /**
     * Notification that the mouse has moved off of an OMGraphic it
     * was previously over.
     */
    public boolean mouseNotOver(OMGraphic omg);

    /**
     * Set the GestureResponsePolicy to notify when MouseEvents have
     * been interpreted.
     */
    public void setGRP(GestureResponsePolicy urp);

    /**
     * Get the GestureResponsePolicy to notify when MouseEvents have
     * been interpreted.
     */
    public GestureResponsePolicy getGRP();
    
    /**
     * Check whether the MapMouseInterpreter is responding to events.
     * @return true if willing to respond to MouseEvents.
     */
    public boolean isActive();

    /**
     * Set whether the MapMouseInterpreter responds to mouse events.
     * @param active true if it should respond to mouse events.
     */
    public void setActive(boolean active);

}