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
// $Source:
// /cvs/darwars/ambush/aar/src/com/bbn/ambush/time/TimeEvent.java,v $
// $RCSfile: TimeEvent.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:30:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import java.io.Serializable;

/**
 * The heartbeat of the application that indicates what the current display time
 * is, for all other components to react to. It might reflect the current system
 * time, or the time that should be reflected in some recording playback.
 * 
 * @author dietrick
 */
public class TimeEvent implements Serializable {

    /**
     * The source of the TimeEvent.
     */
    protected Object source;
    /**
     * The current system time, millis from unix epoch.
     */
    protected long systemTime;
    /**
     * The current offset time, in millis from the start of the time frame of
     * interest.
     */
    protected long offsetTime;
    /**
     * The current simulation time, if the current system time does not
     * correspond to the time frame of the data.
     */
    protected long simTime;
    /**
     * Description of how/why time changed.
     */
    protected TimerStatus timerStatus;

    public final static TimeEvent NO_TIME = new TimeEvent(null, Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, TimerStatus.INACTIVE);

    /**
     * Create a time event.
     * 
     * @param src the object creating the time event.
     * @param systemTime the current system time, millis from the epoch.
     * @param offsetTime the current offset time, in millis from the start of
     *        the mission or media.
     * @param simTime the current simulation time, in millis from the start of
     *        the simulation clock.
     * @param timerStatus
     */
    public TimeEvent(Object src, long systemTime, long offsetTime,
            long simTime, TimerStatus timerStatus) {
        this.source = src;
        this.systemTime = systemTime;
        this.offsetTime = offsetTime;
        this.simTime = simTime;
        this.timerStatus = timerStatus;
    }

    /**
     * Create a time event, with the option of receiving the NO_TIME event if
     * the time is Long.MIN_VALUE and the system time and simulation time are
     * Long.MAX_VALUE.
     * 
     * @param src the object creating the time event.
     * @param time the current time of the clock, millis from the epoch.
     * @param systemTime the time of the start of the media, in millis from
     *        epoch. Subtracted from the time, it should give the offset time
     *        from the start of the mission or media.
     * @param simTime the starting time within the simulation of the currently
     *        active mission or media, in millis. When the offset time (time -
     *        system time) is added to this time, you should have the current
     *        game time.
     * @param timerStatus to describe what kind of TimeEvent should be
     *        created.
     * @return a TimeEvent, or TimeEvent.NO_TIME object if the time values
     *         indicate that no time has been set on the clock.
     */
    public static TimeEvent create(Object src, long time, long systemTime,
                                   long simTime, TimerStatus timerStatus) {
        if (time == Long.MIN_VALUE || time == Long.MAX_VALUE) {
            return NO_TIME;
        }

        return new TimeEvent(src, time, time - systemTime, simTime + time
                - systemTime, timerStatus);
    }

    public Object getSource() {
        return source;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public long getOffsetTime() {
        return offsetTime;
    }

    public long getSimTime() {
        return simTime;
    }

    /**
     * Returns the String identifying the timer action (TIMER_FORWARD,
     * TIMER_STOPPED, TIMER_TIME_STATUS, ...).
     * 
     * @return String identifying what's going on with the timer.
     */
    public TimerStatus getTimerStatus() {
        return timerStatus;
    }

    public String toString() {
        return "TimeEvent: " + timerStatus + " [" + offsetTime + ", "
                + systemTime + ", " + simTime + "]";
    }
}