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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/PanEvent.java,v $
// $RCSfile: PanEvent.java,v $
// $Revision: 1.4 $
// $Date: 2006/02/27 23:19:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * An event to request the map to pan. Event designates the direction
 * and magnitude (relative to map dimensions) to pan the map.
 */
public class PanEvent extends java.util.EventObject {
    /**
     * Marks the first integer id for the range of pan event
     * directions.
     */
    public static final int PAN_FIRST = 1300;

    /**
     * Marks the last integer id for the range of pan event
     * directions.
     */
    public static final int PAN_LAST = 1307;

    /**
     * The possible pan directions
     */
    public static final int NORTH = 1300;
    public static final int NORTH_EAST = 1301;
    public static final int EAST = 1302;
    public static final int SOUTH_EAST = 1303;
    public static final int SOUTH = 1304;
    public static final int SOUTH_WEST = 1305;
    public static final int WEST = 1306;
    public static final int NORTH_WEST = 1307;

    protected float Az;
    protected float c;

    /**
     * Create a PanEvent with source Object and direction.
     * <p>
     * 
     * @param source Object
     * @param direction N, NE, E, SE, S, SW, W, NW
     * @deprecated use new panning semantics
     */
    public PanEvent(Object source, int direction) {
        this(source, dir2Az(direction), Float.NaN);
    }

    /**
     * Create a PanEvent with source Object and direction.
     * <p>
     * 
     * @param source Object
     * @param direction N, NE, E, SE, S, SW, W, NW
     * @param amount 0.0 &lt;= x &lt;= 1.0
     * @deprecated use new panning semantics
     */
    public PanEvent(Object source, int direction, float amount) {
        this(source, dir2Az(direction), Float.NaN);
    }

    /**
     * Create a PanEvent.
     * <ul>
     * <li><code>pan(-180)</code> pan south
     * <li><code>pan(-90)</code> pan west
     * <li><code>pan(0)</code> pan north
     * <li><code>pan(90)</code> pan east
     * </ul>
     * 
     * @param source Object
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     */
    public PanEvent(Object source, float Az) {
        this(source, Az, Float.NaN);
    }

    /**
     * Create a PanEvent.
     * <ul>
     * <li><code>pan(-180, c)</code> pan south `c' degrees
     * <li><code>pan(-90, c)</code> pan west `c' degrees
     * <li><code>pan(0, c)</code> pan north `c' degrees
     * <li><code>pan(90, c)</code> pan east `c' degrees
     * </ul>
     * 
     * @param source Object
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     * @param c arc distance in decimal degrees
     */
    public PanEvent(Object source, float Az, float c) {
        super(source);
        this.Az = Az;
        this.c = c;
    }

    // convert from old style to new
    final static float dir2Az(int direction) {
        switch (direction) {
        case NORTH_WEST:
            return -45f;
        case NORTH:
            return 0f;
        case NORTH_EAST:
            return 45f;
        case EAST:
            return 90f;
        case SOUTH_EAST:
            return 135f;
        case SOUTH:
            return 180f;
        case SOUTH_WEST:
            return -135f;
        case WEST:
            return -90f;
        default:
            return 0f;
        }
    }

    /**
     * Get azimuth of pan.
     * 
     * @return float decimal degrees
     */
    public float getAzimuth() {
        return Az;
    }

    /**
     * Get arc distance of pan.
     * 
     * @return float decimal degrees
     */
    public float getArcDistance() {
        return c;
    }

    /**
     * Get the direction of pan.
     * 
     * @return int direction
     * @deprecated use getAzimuth()
     */
    public int getDirection() {
        return -1;
    }

    /**
     * Get the amount of pan.
     * 
     * @return float 0.0 &lt;= amount &lt;= 1.0
     * @deprecated use getArcDistance()
     */
    public float getAmount() {
        return Float.NaN;
    }

    /**
     * Return stringified object.
     * 
     * @return String
     */
    public String toString() {
        return getClass().getName() + "[Az = " + Az + ", c=" + c + ", source="
                + source + "]";
    }
}