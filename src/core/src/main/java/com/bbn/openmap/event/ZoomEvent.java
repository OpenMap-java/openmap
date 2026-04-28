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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ZoomEvent.java,v $
// $RCSfile: ZoomEvent.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 17:37:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * An event to request that the map zoom in or out. Event specifies
 * the type and amount of zoom of the map.
 */
public class ZoomEvent extends java.util.EventObject implements
        java.io.Serializable {
    /**
     * Type that specifies that the amount should be used as a multiplier to the
     * current scale.
     */
    public transient static final int RELATIVE = 301;

    /**
     * Type that specifies that the amount should be used as the new scale.
     */
    public transient static final int ABSOLUTE = 302;

    /**
     * The type of zooming.
     */
    protected int type;

    /**
     * The zoom factor.
     */
    protected float amount;

    /**
     * Construct a ZoomEvent.
     * 
     * @param source the creator of the ZoomEvent.
     * @param type the type of the event, referring to how to use the
     *        amount.
     * @param amount the value of the ZoomEvent.
     */
    public ZoomEvent(Object source, int type, float amount) {
        super(source);
        switch (type) {
        case RELATIVE:
        case ABSOLUTE:
            break;
        default:
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        this.type = type;
        this.amount = amount;
    }

    /**
     * Check if the type is RELATIVE.
     * 
     * @return boolean
     */
    public boolean isRelative() {
        return (type == RELATIVE);
    }

    /**
     * Check if the type is ABSOLUTE.
     * 
     * @return boolean
     */
    public boolean isAbsolute() {
        return (type == ABSOLUTE);
    }

    /**
     * Get the amount of zoom.
     * 
     * @return float
     */
    public float getAmount() {
        return amount;
    }

    /**
     * Stringify the object.
     * 
     * @return String
     */
    public String toString() {
        return "#<ZoomEvent " + (isRelative() ? "Relative " : "")
                + (isAbsolute() ? "Absolute " : "") + amount + ">";
    }
}