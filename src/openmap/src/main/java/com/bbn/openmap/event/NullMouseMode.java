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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/NullMouseMode.java,v $
// $RCSfile: NullMouseMode.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * The NullMouseMode takes no action on mouse events and does not keep
 * a list of map mouse listeners. It is intended to be used when you
 * need a mouse mode that does nothing as an alternative to one that
 * does something. If you don't have one that does something you don't
 * need any MouseModes at all.
 */
public class NullMouseMode extends AbstractMouseMode {

    /**
     * Mouse Mode identifier, which is "None".
     */
    public final static transient String modeID = "None";

    /**
     * Construct a NullMouseMode. Default constructor sets the ID to
     * the modeID string and the consume events parameter to true.
     */
    public NullMouseMode() {
        this(modeID, true);
    }

    /**
     * Construct a NullMouseMode. Constructor that lets you set the
     * name and the consume mode.
     * 
     * @param id the ID name.
     * @param consumeEvents the consume mode.
     */
    public NullMouseMode(String id, boolean consumeEvents) {
        super(id, consumeEvents);
    }

    /**
     * IGNORED.
     */
    public void addMapMouseListener(MapMouseListener mml) {}

    /**
     * IGNORED.
     */
    public void removeMapMouseListener(MapMouseListener mml) {}
}