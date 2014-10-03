//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.time;

/**
 * Contains the message from a TimeBoundsHandler to a TimeBoundsListener. If the
 * new TimeBounds is null, then the TimeBoundsHandler has gone inactive (nothing
 * providing time bounds). If it isn't and the old TimeBounds is null, the
 * TimeBoundsHandler has gone active (something is now providing time bounds).
 * 
 * @author dietrick
 */
public class TimeBoundsEvent {

    protected TimeBoundsHandler source;
    protected TimeBounds newTimeBounds;
    protected TimeBounds oldTimeBounds;
    protected boolean induceGraphicalUpdate;

    public TimeBoundsEvent(TimeBoundsHandler source, TimeBounds ntb, TimeBounds otb) {
        this.source = source;
        this.newTimeBounds = ntb;
        this.oldTimeBounds = otb;
        this.induceGraphicalUpdate = true;
    }

    /**
     * The TimeBoundsHandler that is sending the message.
     * 
     * @return source of time bounds message
     */
    public TimeBoundsHandler getSource() {
        return source;
    }

    /**
     * @return if null, the TimeBoundsHandler is now inactive. Otherwise,
     *         returns the updated TimeBounds.
     */
    public TimeBounds getNewTimeBounds() {
        return newTimeBounds;
    }

    /**
     * Only really used to indicate when a TimeBoundsHandler has gone active, if
     * this is null and the new TimeBounds isn't.
     * 
     * @return old time bounds
     */
    public TimeBounds getOldTimeBounds() {
        return oldTimeBounds;
    }

    /**
     * @return whether this event should cause any graphical changes. (Set to
     *         false if you're firing multiple events and you want only one of
     *         them - generally the last one, naturally - to begin the work of
     *         updating the display.)
     */
    public boolean isInduceGraphicalUpdate() {
        return induceGraphicalUpdate;
    }

    /**
     * @param induceGraphicalUpdate Whether this event should cause any
     *        graphical changes. (Set to false if you're firing multiple events
     *        and you want only one of them - generally the last one, naturally
     *        - to begin the work of updating the display.)
     */
    public void setInduceGraphicalUpdate(boolean induceGraphicalUpdate) {
        this.induceGraphicalUpdate = induceGraphicalUpdate;
    }

}
