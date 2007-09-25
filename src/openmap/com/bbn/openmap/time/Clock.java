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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/time/Clock.java,v $
// $RCSfile: Clock.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:30:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.Timer;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.gui.time.RealTimeHandler;
import com.bbn.openmap.gui.time.TimeConstants;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The Clock is a controller that manages a Timer in order to support the notion
 * of a time range and a list of objects that can contribute to that time range.
 * The clock can count forward and backward, can wrap around the time limits,
 * and can be set to any time between the time range limits. The clock sends out
 * time notifications as PropertyChangeEvents.
 */
public class Clock extends OMComponent implements RealTimeHandler,
        TimeConstants, ActionListener, PropertyChangeListener,
        TimeBoundsListener, Serializable {

    /**
     * Property name fired for time PropertyChangeEvents dealing with general
     * TimeEvents, when the clock is being started, stopped or jumped to a new
     * location.
     */
    public final static String TIMER_STATUS_PROPERTY = "TIMER_STATUS_PROPERTY";

    /**
     * Property placed in TimeEvent object for TIMER_STATUS_PROPERTY events, for
     * those time events where the time is being set by a component that can
     * jump time - NOT the timer running as usual."
     */
    public final static String TIME_SET_STATUS = "TIME_SET_STATUS";

    /**
     * Property fired when the TimeBounds change.
     */
    public final static String TIME_BOUNDS_PROPERTY = "TIME_BOUNDS_PROPERTY";

    /**
     * timeFormat, used for the times listed in properties for rates/pace.
     */
    public final static String TimeFormatProperty = "timeFormat";

    /**
     * TimeFormat default is similar to IETF standard date syntax: "13:30:00"
     * represented by (HH:mm:ss).
     */
    protected SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    protected Timer timer;

    protected long startTime = Long.MAX_VALUE;

    protected long endTime = Long.MIN_VALUE;

    protected long time = 0;

    protected int timeIncrement = 1;

    protected boolean timeWrap = false;

    protected int clockDirection = 1;

    protected List timerRates;

    protected transient List timeBoundsProviders;

    /**
     * The delay between timer pulses, in milliseconds.
     */
    protected int updateInterval = 1000;

    public Clock() {
        createTimer();
        timerRates = new LinkedList();
        timeBoundsProviders = new LinkedList();
    }

    // //////////////////////////
    // RealTimeHandler methods.
    // //////////////////////////

    public void setUpdateInterval(int delay) {
        updateInterval = delay;
        if (timer != null) {
            timer.setDelay(updateInterval);
            if (timer.isRunning()) {
                timer.restart();
            }
        }
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setPace(int pace) {
        timeIncrement = pace;
    }

    public int getPace() {
        return timeIncrement;
    }

    public void setTime(long t) {
        setTime(t, TIME_SET_STATUS);
    }

    protected void setTime(long t, String timeStatus) {

        // Catch interative cycles and other duplications that may be
        // triggered from other gui components.
        if (t == time)
            return;

        if (Debug.debugging("clock")) {
            Debug.output("Clock.setTime: " + t + " for " + timeStatus);
        }

        time = t;
        fireClockUpdate(timeStatus);
    }

    /**
     * The method that delivers the current time status to its
     * PropertyChangeListeners.
     */
    protected void fireClockUpdate(String timerStatus) {
        firePropertyChange(TIMER_STATUS_PROPERTY, null, TimeEvent.create(this,
        // time, time - systemTime, simTime + time - systemTime,
                time,
                systemTime,
                simTime,
                timerStatus));
    }

    /**
     * Get the current time.
     */
    public long getTime() {
        return time;
    }

    /**
     * Method to call to start the timer.
     */
    public void startClock() {
        if (!timer.isRunning()) {
            firePropertyChange(TIMER_RUNNING_STATUS,
                    TIMER_STOPPED,
                    (getClockDirection() > 0 ? TIMER_FORWARD : TIMER_BACKWARD));
            fireClockUpdate(getClockDirection() > 0 ? TIMER_FORWARD
                    : TIMER_BACKWARD);
        }
        if (Debug.debugging("clock")) {
            Debug.output("Clock: Starting clock");
        }
        timer.restart();
    }

    /**
     * Method to call to stop the timer.
     */
    public void stopClock() {
        if (timer.isRunning()) {
            firePropertyChange(TIMER_RUNNING_STATUS,
                    (getClockDirection() > 0 ? TIMER_FORWARD : TIMER_BACKWARD),
                    TIMER_STOPPED);
            fireClockUpdate(TIMER_STOPPED);
            timer.stop();
        }
    }

    /**
     * Set whether time increases or decreases when the clock is run. If
     * direction is zero or greater, clock runs forward. If direction is
     * negative, clock runs backward.
     */
    public void setClockDirection(int direction) {
        String oldDirection = clockDirection > 0 ? TIMER_FORWARD
                : TIMER_BACKWARD;

        if (direction >= 0) {
            clockDirection = 1;
        } else {
            clockDirection = -1;
        }

        String newDirection = clockDirection > 0 ? TIMER_FORWARD
                : TIMER_BACKWARD;

        if (timer.isRunning()) {
            if (oldDirection != newDirection) {
                firePropertyChange(TIMER_RUNNING_STATUS,
                        oldDirection,
                        newDirection);
                fireClockUpdate(newDirection);
            }
        }
    }

    /**
     * Get whether time increases or decreases when the clock is run. If
     * direction is zero or greater, clock runs forward. If direction is
     * negative, clock runs backward.
     */
    public int getClockDirection() {
        return clockDirection;
    }

    /**
     * Move the clock forward one clock interval.
     */
    public void stepForward() {
        changeTimeBy(timeIncrement, timeWrap, TIME_SET_STATUS);
    }

    /**
     * Move the clock back one clock interval.
     */
    public void stepBackward() {
        changeTimeBy(-timeIncrement, timeWrap, TIME_SET_STATUS);
    }

    // ///// Convenience methods for ReadTimeHandler

    /**
     * Call setTime with the amount given added to the current time. The amount
     * should be negative if you are going backward through time. You need to
     * make sure manageGraphics is called for the map to update.
     * <p>
     * 
     * @param amount to change the current time by, in milliseconds.
     */
    protected void changeTimeBy(long amount) {
        changeTimeBy(amount, timeWrap, TIMER_TIME_STATUS);
    }

    /**
     * Call setTime with the amount given added to the current time. The amount
     * should be negative if you are going backward through time. You need to
     * make sure manageGraphics is called for the map to update.
     * 
     * @param amount to change the current time by, in milliseconds.
     * @param wrapAroundTimeLimits if true, the time will be set as if the start
     *        and end times ofthe scenario are connected, so that moving the
     *        time past the time scale in either direction will put the time at
     *        the other end of the scale.
     */
    protected void changeTimeBy(long amount, boolean wrapAroundTimeLimits) {
        changeTimeBy(amount, wrapAroundTimeLimits, TIMER_TIME_STATUS);
    }

    /**
     * Call setTime with the amount given added to the current time. The amount
     * should be negative if you are going backward through time. You need to
     * make sure manageGraphics is called for the map to update.
     * 
     * @param amount to change the current time by, in milliseconds.
     * @param wrapAroundTimeLimits if true, the time will be set as if the start
     *        and end times ofthe scenario are connected, so that moving the
     *        time past the time scale in either direction will put the time at
     *        the other end of the scale.
     * @param timeStatus the string given to the TimeEvent to let everyone know
     *        why the time is changing. Usually TIMER_TIME_STATUS if the timer
     *        went off normally, or TIME_SET_STATUS if the time is being
     *        specifically set to something.
     */
    protected void changeTimeBy(long amount, boolean wrapAroundTimeLimits,
                                String timeStatus) {

        long oldTime = getTime();
        long newTime;
        boolean stopClock = false;

        newTime = oldTime + amount;

        if (newTime > endTime || newTime < startTime) {
            if (wrapAroundTimeLimits) {
                newTime = (amount >= 0 ? startTime : endTime);
            } else {
                newTime = (amount >= 0 ? endTime : startTime);
                stopClock = true;
            }
        }

        if (Debug.debugging("clock")) {
            Debug.output("Clock "
                    + (stopClock ? ("stopping clock at (" + newTime)
                            : ("changing time by [" + amount + "] to (" + newTime))
                    + ") : " + timeStatus);
        }

        if (stopClock) {
            stopClock();
        }

        setTime(newTime, timeStatus);
    }

    // ///////////////////////
    // ActionListener method
    // ///////////////////////

    /**
     * ActionListener interface, gets called when the timer goes ping if there
     * isn't a command with the ActionEvent. Otherwise, the command should be
     * filled in.
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == getTimer()) {

            // Normal time change call.
            // changeTimeBy(timeIncrement * clockDirection);

            // Hacked version, so that the video can get events that
            // they will listen to if the clock is running backwards.
            changeTimeBy(timeIncrement * clockDirection,
                    timeWrap,
                    clockDirection < 0 ? TIME_SET_STATUS : TIMER_TIME_STATUS);
        }
    }

    // //////////////////////////////
    // PropertyChangeListener method
    // //////////////////////////////

    /**
     * PropertyChangeListener method called when the bounds on a
     * TimeBoundsProvider changes, so so that the range of times can be
     * adjusted.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        resetTimeBounds();
    }

    // //////////////////////////////
    // TimeBoundsListener methods
    // //////////////////////////////

    /**
     * Add a TimeBoundsProvider to the clock, so it knows the bounds of it's
     * time range.
     */
    public void addTimeBoundsProvider(TimeBoundsProvider tbp) {
        if (!timeBoundsProviders.contains(tbp)) {
            timeBoundsProviders.add(tbp);
            tbp.addPropertyChangeListener(TimeBoundsProvider.ACTIVE_PROPERTY,
                    this);
            resetTimeBounds();
        }
    }

    public void removeTimeBoundsProvider(TimeBoundsProvider tbp) {
        timeBoundsProviders.remove(tbp);
        tbp.removePropertyChangeListener(TimeBoundsProvider.ACTIVE_PROPERTY,
                this);
        resetTimeBounds();
    }

    public void clearTimeBoundsProviders() {
        timeBoundsProviders.clear();
        resetTimeBounds();
    }

    // //////////////////////////////
    // Generic Clock Methods
    // //////////////////////////////

    /**
     * Method to call when TimeBoundsProviders change, in order to query them
     * and figure out what the new time range is.
     */
    public void resetTimeBounds() {
        TimeBounds oldtb = new TimeBounds(startTime, endTime);

        startTime = Long.MAX_VALUE;
        endTime = Long.MIN_VALUE;
        int activeTimeBoundsProviderCount = 0;

        for (Iterator it = timeBoundsProviders.iterator(); it.hasNext();) {
            TimeBoundsProvider tbp = (TimeBoundsProvider) it.next();
            if (tbp.isActive()) {
                activeTimeBoundsProviderCount++;
                TimeBounds bounds = tbp.getTimeBounds();
                if (bounds != null) {
                    addTime(bounds.getStartTime());
                    addTime(bounds.getEndTime());
                }

                if (Debug.debugging("clock")) {
                    Debug.output("Clock.resetTimeBounds("
                            + tbp.getClass().getName() + ") adding " + bounds);
                }
            } else {
                if (Debug.debugging("clock")) {
                    Debug.output("Clock.resetTimeBounds("
                            + tbp.getClass().getName() + ") not active");
                }
            }
        }

        // system time is startTime, let other components track their
        // relative system time if it is important to them.
        systemTime = startTime;

        TimeBounds tb = new TimeBounds(startTime, endTime);
        for (Iterator it = timeBoundsProviders.iterator(); it.hasNext();) {
            ((TimeBoundsProvider) it.next()).handleTimeBounds(tb);
        }

        long currentTime = time;
        // If the number of activeTimeBoundsProviders is zero, reset the clock
        // to the startTime, which should be Long.MAX_TIME, and this should
        // reset it when some TimeBoundsProvider is made active.x
        if (activeTimeBoundsProviderCount == 0) {
            setTime(startTime);
        } else if (currentTime < startTime || currentTime == Long.MAX_VALUE) {
            setTime(startTime);
        } else if (currentTime > endTime) {
            setTime(endTime);
        }
        firePropertyChange(TIME_BOUNDS_PROPERTY, oldtb, tb);
    }

    /**
     * Add a time to the time range, fire a TIME_BOUNDS_PROPERTY change if the
     * time range changes.
     * 
     * @param timeStamp in milliseconds
     */
    public void addTimeToBounds(long timeStamp) {
        long oldStartTime = startTime;
        long oldEndTime = endTime;

        addTime(timeStamp);

        if (oldStartTime != startTime || oldEndTime != endTime) {
            firePropertyChange(TIME_BOUNDS_PROPERTY,
                    new TimeBounds(oldStartTime, oldEndTime),
                    new TimeBounds(startTime, endTime));
        }
    }

    /**
     * Add a time to the time range.
     * 
     * @param timeStamp in milliseconds
     */
    protected void addTime(long timeStamp) {

        if (timeStamp < startTime) {
            if (Debug.debugging("clock")) {
                Debug.output("Clock: setting startTime: " + timeStamp);
            }

            startTime = timeStamp;
        }

        if (timeStamp > endTime) {
            if (Debug.debugging("clock")) {
                Debug.output("Clock: setting endTime: " + timeStamp);
            }

            endTime = timeStamp;
        }

        // This is actually resetting the time even if it doesn't need to be
        // reset, since startTime and endTime really can be in the middle of
        // being redefined. The time check should happen after all times have
        // been added, the old and new system times have been checked, and then
        // we'll have a better idea of where time should be.
        // if (time < startTime) {
        // time = (long) startTime;
        // } else if (time > endTime) {
        // time = (long) endTime;
        // } // else, leave it alone...

    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    protected long systemTime = 0;

    protected long simTime = 0;

    /**
     * Set the system time and simulation time. These times are used in relation
     * to any offsets. System time usually gets reset when the time bounds are
     * reset.
     * 
     * @param sysTime the system (computer) time used in TimeEvents.
     * @param simulationTime the scenario time used in TimeEvents.
     */
    public void setBaseTimesForTimeEvent(long sysTime, long simulationTime) {
        systemTime = sysTime;
        simTime = simulationTime;
    }

    public long getSimTime() {
        return simTime;
    }

    public long getSystemTime() {
        return systemTime;
    }

    // //////////////////////////
    // Timer management methods
    // //////////////////////////

    /**
     * Get the timer being used for automatic updates. May be null if a timer is
     * not set.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * If you want the layer to update itself at certain intervals, you can set
     * the timer to do that. Set it to null to disable it. If the current timer
     * is not null, the graphic loader is removed as an ActionListener. If the
     * new one is not null, the graphic loader is added as an ActionListener.
     */
    public void setTimer(Timer t) {
        if (timer != null) {
            timer.stop();
            timer.removeActionListener(this);
        }

        timer = t;
        if (timer != null) {
            timer.removeActionListener(this);
            timer.addActionListener(this);
        }
    }

    /**
     * Creates a timer with the current updateInterval and calls setTimer().
     */
    public void createTimer() {
        Timer t = new Timer(updateInterval, this);
        t.setInitialDelay(0);
        setTimer(t);
    }

    /**
     * Get a list of TimerRateHolders.
     */
    public List getTimerRates() {
        return timerRates;
    }

    /**
     * Make sure the List contains TimerRateHolders.
     */
    public void setTimerRates(List rates) {
        timerRates = rates;
    }

    // ////////////////////
    // OMComponent methods
    // ////////////////////

    /**
     * @param prefix string prefix used in the properties file for this
     *        component.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String timeFormatString = properties.getProperty(prefix
                + TimeFormatProperty,
                ((SimpleDateFormat) timeFormat).toPattern());

        timeFormat = new SimpleDateFormat(timeFormatString);
        timerRates = TimerRateHolder.getTimerRateHolders(prefix, properties);
    }

    /**
     * OMComponent method, called when new components are added to the
     * MapHandler. Lets the Clock find TimeBoundsProviders that have been added
     * to the application, so that the Clock can register itself as a
     * PropertyChangeListener.
     */
    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof TimeBoundsProvider) {
            if (Debug.debugging("clock")) {
                Debug.output("Clock.findAndInit(TimeBoundsProvider): "
                        + someObj.getClass().getName());
            }
            addTimeBoundsProvider((TimeBoundsProvider) someObj);
        }
    }

    /**
     * OMComponent method, called when new components are removed from the
     * MapHandler. Lets the Clock unregister itself as PropertyChangeListener to
     * TimeBoundsProviders.
     */
    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        if (someObj instanceof TimeBoundsProvider) {
            removeTimeBoundsProvider((TimeBoundsProvider) someObj);
        }
    }

    /**
     * Adds a PropertyChangeListener to this Clock, so that object can receive
     * time PropertyChangeEvents.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        if (Debug.debugging("clock")) {
            Debug.output("Clock: adding property change listener");
        }

        super.addPropertyChangeListener(TIMER_RUNNING_STATUS, pcl);
        initializePropertyChangeListener(pcl);
    }

    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener pcl) {
        super.addPropertyChangeListener(propertyName, pcl);
        initializePropertyChangeListener(pcl);
    }

    /**
     * Fires Propertyevents to new PropertyChangeListeners so they get the
     * latest info.
     */
    protected void initializePropertyChangeListener(PropertyChangeListener pcl) {
        String runningStatus = timer.isRunning() ? (getClockDirection() > 0 ? TIMER_FORWARD
                : TIMER_BACKWARD)
                : TIMER_STOPPED;
        firePropertyChange(TIMER_RUNNING_STATUS, null, runningStatus);
        fireClockUpdate(runningStatus);
    }

    /**
     * @return true if timer is running.
     */
    public boolean isRunning() {
        return timer.isRunning();
    }
}