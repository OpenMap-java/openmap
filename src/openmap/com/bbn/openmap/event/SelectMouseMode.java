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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/SelectMouseMode.java,v $
// $RCSfile: SelectMouseMode.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * The SelectMouseMode delegates handling of mouse events to the
 * listeners. This MouseMode type is ideal for Layers that want to
 * receive MouseEvents. The simplest way to set this up is for the
 * Layer to implement the MapMouseListener interface, and indicate
 * that it wants to receive events when the mouse mode is the
 * SelectMouseMode. Here's a code snippet for a Layer that would do
 * this: <code><pre>
 * public MapMouseListener getMapMouseListener() {
 *     return this;
 * }
 * 
 * public String[] getMouseModeServiceList() {
 *     return new String[] { SelectMouseMode.modeID };
 * }
 * </pre></code>
 * <p>
 * This class is functionally the same as the AbstractMouseMode,
 * except that it actually calls the fire methods in the
 * MapMouseSupport object to propagate the events.
 */
public class SelectMouseMode extends CoordMouseMode {

    /**
     * Mouse Mode identifier, which is "Gestures". This is returned on
     * getID()
     */
    public final static transient String modeID = "Gestures";

    /**
     * Construct a SelectMouseMode. Default constructor. Sets the ID
     * to the modeID, and the consume mode to true.
     */
    public SelectMouseMode() {
        this(true);
    }

    /**
     * Construct a SelectMouseMode. The constructor that lets you set
     * the consume mode.
     * 
     * @param consumeEvents the consume mode setting.
     */
    public SelectMouseMode(boolean consumeEvents) {
        this(modeID, consumeEvents);
    }

    /**
     * Construct a SelectMouseMode. The constructor that lets you set
     * the consume mode.
     * 
     * @param id the id for the mouse mode.
     * @param consumeEvents the consume mode setting.
     */
    public SelectMouseMode(String id, boolean consumeEvents) {
        super(id, consumeEvents);
    }
}