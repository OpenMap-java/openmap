// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/darwars/ambush/aar/src/com/bbn/hotwash/event/AAREvent.java,v $
// $RCSfile: AAREvent.java,v $
// $Revision: 1.3 $
// $Date: 2008/11/10 23:57:53 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.bbn.openmap.util.Attributable;

/**
 * An OMEvent represents an event that occurs at a certain time. The time is
 * maintained as an offset from the UNIX epoch marker.
 * 
 * @author dietrick
 */
public class OMEvent implements Attributable {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.event.OMEvent");

    public final static String ATT_KEY_DETAILED_INFORMATION = "DETAILS";
    public final static String ATT_KEY_SELECTED = "SELECTED";
    public final static String ATT_KEY_PLAY_FILTER = "PLAY_FILTER";
    public final static String ATT_KEY_RATING = "RATING";

    public final static String ATT_VAL_GOOD_RATING = "+";
    public final static String ATT_VAL_BAD_RATING = "-";
    public final static String ATT_VAL_SELECTED_START_RANGE = "AVSSR";
    public final static String ATT_VAL_SELECTED_END_RANGE = "AVSER";
    public final static String ATT_VAL_SELECTED = "SELECTED";

    protected String description = "";
    /** Absolute time, milliseconds from the UNIX epoch. */
    protected long timeStamp;
    /**
     * Value to assist the OMEventComparator to order OMEvents with the same
     * timeStamp.
     */
    protected short timeStampComparator = 0;
    protected boolean sorted = false;

    protected Point2D location;
    protected Rectangle range;
    protected Object source;

    protected boolean atCurrentTime = false;
    protected boolean selected = false;

    protected Map attributes;

    public OMEvent(Object src, String desc, long tStamp) {
        this(src, desc, tStamp, null, null);
    }

    public OMEvent(Object src, String desc, long tStamp, Point2D loc) {
        this(src, desc, tStamp, loc, null);
    }

    public OMEvent(Object src, String desc, long tStamp, Point2D loc,
            Rectangle rect) {
        description = desc;
        timeStamp = tStamp;
        location = loc;
        range = rect;
        source = src;

        if (src instanceof Attributable) {
            attributes = ((Attributable) src).getAttributes();
        }
    }

    /**
     * Assumed to be an offset time (millis) from the initial system time.
     */
    public void setTimeStamp(long tStamp) {
        timeStamp = tStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setDescription(String desc) {
        description = desc;
    }

    public String getDescription() {
        return toString();
    }

    public String toString() {
        return description;
    }

    public void setLocation(Point2D loc) {
        location = loc;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setRange(Rectangle rect) {
        range = rect;
    }

    public Rectangle getRange() {
        return range;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object src) {
        source = src;
    }

    public String getDetailedInformation() {
        return (String) getAttribute(ATT_KEY_DETAILED_INFORMATION);
    }

    public void setDetailedInformation(String di) {
        putAttribute(ATT_KEY_DETAILED_INFORMATION, di);
    }

    public boolean isAtCurrentTime() {
        return atCurrentTime;
    }

    public void setAtCurrentTime(boolean atCurrentTime) {
        this.atCurrentTime = atCurrentTime;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Test whether this event should be displayed, given the provided filter
     * list. The filter list are those that are turned on, meaning that things
     * contained in the list should be displayed, and should cause this method
     * to return true. This method should be overridden by subclasses. If it
     * isn't then the event will always be shown.
     * 
     * @param filters
     * @return true if this event passes filters
     */
    public boolean passesMacroFilters(List filters) {
        return true;
    }

    public void putAttribute(Object key, Object value) {
        if (key == null) {
            return;
        }

        if (attributes == null) {
            attributes = new Hashtable();
        }

        if (value != null) {
            attributes.put(key, value);
        } else {
            attributes.remove(key);
        }
    }

    public Object getAttribute(Object key) {
        Object ret = null;
        if (attributes != null && key != null) {
            ret = attributes.get(key);
        }
        return ret;
    }

    public void clearAttributes() {
        if (attributes != null) {
            attributes.clear();
        }
    }

    public Map getAttributes() {
        return attributes;
    }

    /**
     * Doesn't set the attribute map on the source, if the source if
     * Attributable. Just sets the attribute Map on the OMEvent.
     */
    public void setAttributes(Map map) {
        attributes = map;
    }

    public short getTimeStampComparator() {
        return timeStampComparator;
    }

    public void setTimeStampComparator(short timeStampComparator) {
        this.timeStampComparator = timeStampComparator;
    }

    protected boolean isSorted() {
        return sorted;
    }

    protected void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

}