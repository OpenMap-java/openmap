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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/time/TimeConstants.java,v $
// $RCSfile: TimeConstants.java,v $
// $Revision: 1.5 $
// $Date: 2007/09/25 17:31:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import java.io.Serializable;

import com.bbn.openmap.util.HashCodeUtil;

/**
 * TimerStatus objects accompany TimeEvents to describe what's going on with the
 * application clock, or the reason the current time has changed.
 */
public class TimerStatus implements Serializable {

    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_STEP_FORWARD = "Timer step forward";
    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_STEP_BACKWARD = "Timer step backward";
    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_FORWARD = "Timer run forward";
    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_BACKWARD = "Timer run backward";
    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_STOPPED = "Timer stopped";
    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_UPDATED = "Timer updated";
    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_ACTIVE = "Timer active";
    /**
     * Command string for clock controllers.
     */
    public final static String TIMER_INACTIVE = "Timer inactive";

    /**
     * Indicates that the clock has been moved incrementally forward, stepping
     * by a set value controlled by some other mechanism. Not set when the clock
     * is changed due to normal running.
     */
    public final static TimerStatus STEP_FORWARD = new TimerStatus(TIMER_STEP_FORWARD);
    /**
     * Indicates that the clock has been moved incrementally backward, stepping
     * by a set value controlled by some other mechanism. Not set when the clock
     * is changed due to normal running.
     */
    public final static TimerStatus STEP_BACKWARD = new TimerStatus(TIMER_STEP_BACKWARD);
    /**
     * Indicates the clock is running, and time has been incremented by the
     * clock.
     */
    public final static TimerStatus FORWARD = new TimerStatus(TIMER_FORWARD);
    /**
     * Indicates the clock is running, and time has been decremented by the
     * clock.
     */
    public final static TimerStatus BACKWARD = new TimerStatus(TIMER_BACKWARD);
    /**
     * Indicates that the clock is no longer running.
     */
    public final static TimerStatus STOPPED = new TimerStatus(TIMER_STOPPED);
    /**
     * Used when the timer is moved to a specific value, not when the clock is
     * running incrementally. Indicates a jump in time.
     */
    public final static TimerStatus UPDATE = new TimerStatus(TIMER_UPDATED);
    /**
     * Indicates that the clock is active and may be sending events.
     */
    public final static TimerStatus ACTIVE = new TimerStatus(TIMER_ACTIVE);
    /**
     * Indicates that the clock is inactive and will not be sending events.
     */
    public final static TimerStatus INACTIVE = new TimerStatus(TIMER_INACTIVE);

    protected String description;

    public TimerStatus(String description) {
        this.description = description;
    }

    public String toString() {
        return description;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimerStatus ts = (TimerStatus) obj;
        return ts == this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = HashCodeUtil.SEED;
        // collect the contributions of various fields
        result = HashCodeUtil.hash(result, description);
        return result;
    }
}