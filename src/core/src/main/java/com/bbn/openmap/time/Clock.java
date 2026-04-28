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
import java.beans.beancontext.BeanContextChildSupport;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Timer;

import com.bbn.openmap.OMComponent;
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
        ActionListener, PropertyChangeListener, TimeBoundsHandler, Serializable {

    public final static int DEFAULT_TIME_INTERVAL = 1000;

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

    /**
     * The timeIncrement is the amount of time that passes for each clock tick.
     * This sets up the ratio for slow and fast motion changes for a set clock
     * update rate. Can be modified with the pace accessors.
     */
    protected int timeIncrement = DEFAULT_TIME_INTERVAL;

    protected boolean timeWrap = false;

    protected int clockDirection = 1;

    protected List<TimerRateHolder> timerRates;

    protected transient List<TimeBoundsProvider> timeBoundsProviders;
    protected transient List<TimeBoundsListener> timeBoundsListeners;
    protected transient List<TimeEventListener> timeEventListeners;

    /**
     * The delay between timer pulses, in milliseconds.
     */
    protected int updateInterval = DEFAULT_TIME_INTERVAL;

    public Clock() {
        // Created again with the peer not set, this allows the Clock to be
        // added to multiple MapHandlers.
        beanContextChildSupport = new BeanContextChildSupport();
        createTimer();
        timerRates = new LinkedList<TimerRateHolder>();
        timeBoundsProviders = new Vector<TimeBoundsProvider>();
        timeBoundsListeners = new Vector<TimeBoundsListener>();
        timeEventListeners = new Vector<TimeEventListener>();
    }

    // //////////////////////////
    // RealTimeHandler methods.
    // //////////////////////////

    /**
     * Set the real time clock interval between clock ticks, in milliseconds.
     */
    public void setUpdateInterval(int delay) {
        updateInterval = delay;
        if (timer != null) {
            timer.setDelay(updateInterval);
            if (timer.isRunning()) {
                timer.restart();
            }
        }
    }

    /**
     * Return the real time interval between clock ticks, in milliseconds.
     */
    public int getUpdateInterval() {
        return updateInterval;
    }

    /**
     * Set the amount of simulation time that passes with each clock tick, in
     * milliseconds.
     */
    public void setPace(int pace) {
        timeIncrement = pace;
    }

    /**
     * Get the amount of simulation time that passes with each clock tick, in
     * milliseconds.
     */
    public int getPace() {
        return timeIncrement;
    }

    /**
     * Called to set the clock to a specific time, usually for jumps.
     * 
     * @param t the time in unix epoch terms
     */
    public void setTime(long t) {
        setTime(t, TimerStatus.UPDATE);
    }

    /**
     * The call to set the clock for all reasons.
     * 
     * @param t the time in unix epoch terms
     * @param timeStatus TimerStatus indicating how the clock is changing.
     */
    protected void setTime(long t, TimerStatus timeStatus) {

        // Catch interactive cycles and other duplications that may be
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
     * The method that delivers the current time status to the
     * TimeEventListeners.
     */
    protected void fireClockUpdate(TimerStatus timerStatus) {
        fireUpdateTime(TimeEvent.create(this,
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
            firePropertyChange(TIMER_STATUS,
                    TimerStatus.STOPPED,
                    (getClockDirection() > 0 ? TimerStatus.FORWARD
                            : TimerStatus.BACKWARD));
            fireClockUpdate(getClockDirection() > 0 ? TimerStatus.FORWARD
                    : TimerStatus.BACKWARD);
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
            firePropertyChange(TIMER_STATUS,
                    (getClockDirection() > 0 ? TimerStatus.FORWARD
                            : TimerStatus.BACKWARD),
                    TimerStatus.STOPPED);
            fireClockUpdate(TimerStatus.STOPPED);
            timer.stop();
        }
    }

    /**
     * Set whether time increases or decreases when the clock is run. If
     * direction is zero or greater, clock runs forward. If direction is
     * negative, clock runs backward.
     */
    public void setClockDirection(int direction) {
        TimerStatus oldDirection = clockDirection > 0 ? TimerStatus.FORWARD
                : TimerStatus.BACKWARD;

        if (direction >= 0) {
            clockDirection = 1;
        } else {
            clockDirection = -1;
        }

        TimerStatus newDirection = clockDirection > 0 ? TimerStatus.FORWARD
                : TimerStatus.BACKWARD;

        if (timer.isRunning()) {
            if (oldDirection != newDirection) {
                firePropertyChange(TIMER_STATUS, oldDirection, newDirection);
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
     * Move the clock forward one time increment.
     */
    public void stepForward() {
        changeTimeBy(timeIncrement, timeWrap, TimerStatus.STEP_FORWARD);
    }

    /**
     * Move the clock back one time increment.
     */
    public void stepBackward() {
        changeTimeBy(-timeIncrement, timeWrap, TimerStatus.STEP_BACKWARD);
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
        changeTimeBy(amount, timeWrap, (amount >= 0 ? TimerStatus.FORWARD
                : TimerStatus.BACKWARD));
    }

    /**
     * Call setTime with the amount given added to the current time. The amount
     * should be negative if you are going backward through time. You need to
     * make sure manageGraphics is called for the map to update.
     * 
     * @param amount to change the current time by, in milliseconds.
     * @param wrapAroundTimeLimits if true, the time will be set as if the start
     *        and end times of the scenario are connected, so that moving the
     *        time past the time scale in either direction will put the time at
     *        the other end of the scale.
     */
    protected void changeTimeBy(long amount, boolean wrapAroundTimeLimits) {
        changeTimeBy(amount,
                wrapAroundTimeLimits,
                (amount >= 0 ? TimerStatus.FORWARD : TimerStatus.BACKWARD));
    }

    /**
     * Call setTime with the amount given added to the current time. The amount
     * should be negative if you are going backward through time. You need to
     * make sure manageGraphics is called for the map to update.
     * 
     * @param amount to change the current time by, in milliseconds.
     * @param wrapAroundTimeLimits if true, the time will be set as if the start
     *        and end times of the scenario are connected, so that moving the
     *        time past the time scale in either direction will put the time at
     *        the other end of the scale.
     * @param timeStatus the string given to the TimeEvent to let everyone know
     *        why the time is changing. Usually TIMER_TIME_STATUS if the timer
     *        went off normally, or TIME_SET_STATUS if the time is being
     *        specifically set to something.
     */
    protected void changeTimeBy(long amount, boolean wrapAroundTimeLimits,
                                TimerStatus timeStatus) {

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

        // Should set the new time before telling everyone the clock is stopped.
        setTime(newTime, timeStatus);

        if (stopClock) {
            stopClock();
        }
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
                    clockDirection < 0 ? TimerStatus.UPDATE
                            : TimerStatus.FORWARD);
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
    // TimeBoundsProvider methods
    // //////////////////////////////

    /**
     * Add a TimeBoundsProvider to the clock, so it knows the bounds of it's
     * time range.
     */
    public void addTimeBoundsProvider(TimeBoundsProvider tbp) {
        if (!timeBoundsProviders.contains(tbp)) {
            timeBoundsProviders.add(tbp);
            resetTimeBounds();
        }
    }

    public void removeTimeBoundsProvider(TimeBoundsProvider tbp) {
        timeBoundsProviders.remove(tbp);
        resetTimeBounds();
    }

    public void clearTimeBoundsProviders() {
        timeBoundsProviders.clear();
        resetTimeBounds();
    }

    // //////////////////////////////
    // TimeBoundsListener methods
    // //////////////////////////////

    /**
     * Add a TimeBoundsListener to the clock, so it knows who to tell when the
     * time bounds change.
     */
    public void addTimeBoundsListener(TimeBoundsListener tbl) {
        if (!timeBoundsListeners.contains(tbl)) {
            timeBoundsListeners.add(tbl);
        }
    }

    public void removeTimeBoundsListener(TimeBoundsListener tbl) {
        timeBoundsListeners.remove(tbl);
    }

    public void clearTimeBoundsListeners() {
        timeBoundsListeners.clear();
    }

    public void fireUpdateTimeBounds(TimeBoundsEvent tbe) {
        if (timeBoundsListeners != null) {
            List<TimeBoundsListener> copy;
            synchronized(timeBoundsListeners) {
                copy = new ArrayList<TimeBoundsListener>(timeBoundsListeners);                
            }
            for (Iterator<TimeBoundsListener> it = copy.iterator(); it.hasNext();) {
                it.next().updateTimeBounds(tbe);
            }
        }
    }

    // //////////////////////////////
    // TimeEventListener methods
    // //////////////////////////////

    /**
     * Add a TimeEventListener to the clock, so it knows who to update when the
     * time changes.
     */
    public void addTimeEventListener(TimeEventListener tel) {
        if (!timeEventListeners.contains(tel)) {
            timeEventListeners.add(tel);
        }
    }

    public void removeTimeEventListener(TimeEventListener tel) {
        timeEventListeners.remove(tel);
    }

    public void clearTimeEventListeners() {
        timeEventListeners.clear();
    }

    public void fireUpdateTime(TimeEvent te) {
        if (timeEventListeners != null) {
            List<TimeEventListener> copy;
            synchronized(timeEventListeners) {
                copy = new ArrayList<TimeEventListener>(timeEventListeners);                
            }
            for (Iterator<TimeEventListener> it = copy.iterator(); it.hasNext();) {
                it.next().updateTime(te);
            }
        }
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

        List<TimeBoundsProvider> copy;
        synchronized(timeBoundsProviders) {
            copy = new ArrayList<TimeBoundsProvider>(timeBoundsProviders);                
        }
        for (Iterator<TimeBoundsProvider> it = copy.iterator(); it.hasNext();) {
            TimeBoundsProvider tbp = it.next();
            if (tbp.isActive()) {
                activeTimeBoundsProviderCount++;
                TimeBounds bounds = tbp.getTimeBounds();
                if (bounds != null && !bounds.isUnset()) {
                    addTime(bounds.getStartTime());
                    addTime(bounds.getEndTime());

                    if (Debug.debugging("clock")) {
                        Debug.output("Clock.resetTimeBounds("
                                + tbp.getClass().getName() + ") adding "
                                + bounds);
                    }
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

        /*
         * First thing, let all the TimeBoundsProviders know what the overall
         * TimeBounds is, in case they need to update their GUI or something.
         */
        TimeBounds tb = new TimeBounds(startTime, endTime);
        for (Iterator<TimeBoundsProvider> it = copy.iterator(); it.hasNext();) {
            it.next().handleTimeBounds(tb);
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

        /*
         * Now, update the TimeBoundsListeners, so they can update their GUIs.
         */
        fireUpdateTimeBounds(new TimeBoundsEvent(this, tb, oldtb));
        
        if (tb.isUnset()) {
            fireUpdateTime(TimeEvent.NO_TIME);
        }

    }

    /**
     * Add a time to the time range, fire a TimeBoundsEvent if the time range
     * changes.
     * 
     * @param timeStamp in milliseconds
     */
    public void addTimeToBounds(long timeStamp) {
        long oldStartTime = startTime;
        long oldEndTime = endTime;

        addTime(timeStamp);

        if (oldStartTime != startTime || oldEndTime != endTime) {
            fireUpdateTimeBounds(new TimeBoundsEvent(this, new TimeBounds(oldStartTime, oldEndTime), new TimeBounds(startTime, endTime)));
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
    public List<TimerRateHolder> getTimerRates() {
        return timerRates;
    }

    /**
     * Make sure the List contains TimerRateHolders.
     */
    public void setTimerRates(List<TimerRateHolder> rates) {
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

        super.addPropertyChangeListener(TIMER_STATUS, pcl);
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
        TimerStatus runningStatus = timer.isRunning() ? (getClockDirection() > 0 ? TimerStatus.FORWARD
                : TimerStatus.BACKWARD)
                : TimerStatus.STOPPED;
        firePropertyChange(TIMER_STATUS, null, runningStatus);
        fireClockUpdate(runningStatus);
    }

    /**
     * @return true if timer is running.
     */
    public boolean isRunning() {
        return timer.isRunning();
    }

}