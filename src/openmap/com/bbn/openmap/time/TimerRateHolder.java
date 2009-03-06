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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/time/TimerRateHolder.java,v $
// $RCSfile: TimerRateHolder.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:30:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * A convenience class that keeps track of a relationship between
 * real-time changes and scenario-time changes.
 */
public class TimerRateHolder implements PropertyConsumer, Serializable {
    /**
     * The string used for the default pace baseline ("00:00:00").
     * Needs to match the pace format.
     */
    public final static String DEFAULT_PACE_BASELINE_VALUE = "00:00:00";
    /**
     * The string used for the default pace format ("HH:mm:ss").
     */
    public final static String DEFAULT_PACE_FORMAT = "HH:mm:ss";

    /**
     * Property used to describe a list of properties for
     * TimerRateHolders (timerRates)
     */
    public final static String TimerRatesProperty = "timerRates";

    /**
     * PaceFormatProperty represents the property used for describing
     * the pace format (paceFormat).
     */
    public final static String PaceFormatProperty = "paceFormat";
    /**
     * PaceBaselineProperty represents the property used for
     * baselining the pace value (paceBaseline).
     */
    public final static String PaceBaselineProperty = "paceBaseline";
    /**
     * Clock that describes how often the pace value is applied to the
     * time value (clockIntervalMillis).
     */
    public final static String ClockIntervalProperty = "clockIntervalMillis";
    /** The property used for the pace setting (pace). */
    public final static String PaceProperty = "pace";

    /**
     * Display label for this Timer rate.
     */
    protected String label;
    /**
     * The number of milliseconds that the timer should wait between
     * updates.
     */
    protected long clockInterval;
    /**
     * The pace is the amount of time that should pass, in simulation
     * time, when the timer gets updated. If the clock is updating
     * every second, and you have the pace set for 1 hour, then the
     * situation controlled by the application's clock will be updated
     * 1 hour every second.
     */
    protected long pace;
    /**
     * The format used in the properties to note the pace. Kept in
     * order to write out the format if that's needed.
     */
    protected SimpleDateFormat paceFormat;
    /**
     * The baseline used to normalize the pace setting. Defaults to
     * "OO:OO:OO".
     */
    protected String paceZero;
    /**
     * Flag that denotes whether the TimerRateHolder has be
     * successfully set.
     */
    protected boolean valid = false;
    /**
     * The prefix used to set the properties, saved in case the
     * properties are retrieved.
     */
    protected String propPrefix;

    /**
     * Create a TimerRateHolder with the default pace format and value
     * (00:00:00).
     */
    public TimerRateHolder() {
        paceFormat = new SimpleDateFormat(DEFAULT_PACE_FORMAT);
        paceZero = DEFAULT_PACE_BASELINE_VALUE;
    }

    /**
     * Create a TimerRateHolder with a date format, and a baseline
     * time. The default baseline time is "00:00:00", so if you need
     * to change that, use this constructor. The pace for this
     * TimerRateHolder should be a relative amount of time, and that
     * relativity, taking into account the locale offset to GMT, is
     * given by the baseline time. The baseline time should match the
     * format given.
     */
    public TimerRateHolder(SimpleDateFormat simpleDateFormat, String dpz) {
        paceFormat = simpleDateFormat;
        paceZero = dpz;
    }

    public void setLabel(String lab) {
        label = lab;
    }

    public String getLabel() {
        return label;
    }

    public void setPace(long pace) {
        this.pace = pace;
    }

    public long getPace() {
        return pace;
    }

    public void setClockInterval(long cli) {
        clockInterval = cli;
    }

    public long getClockInterval() {
        return clockInterval;
    }

    public String toString() {
        return "TimePanel.TimerRateHolder [" + label + ", clock:"
                + clockInterval + ", pace:" + pace + "] (" + valid + ")";
    }

    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    public void setProperties(String prefix, Properties props) {

        propPrefix = prefix;
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        try {
            label = props.getProperty(prefix + Layer.PrettyNameProperty);
            clockInterval = PropUtils.longFromProperties(props, prefix
                    + ClockIntervalProperty, -1L);
            String paceString = props.getProperty(prefix + PaceProperty);
            pace = paceFormat.parse(paceString).getTime()
                    - paceFormat.parse(paceZero).getTime();
            valid = true;

        } catch (NullPointerException npe) {
            Debug.error("TimerRateHolder caught NPE: " + npe.getMessage());
        } catch (ParseException pe) {
            Debug.error("TimerRateHolder parse exception: " + pe.getMessage());
        }
    }

    public Properties getProperties(Properties props) {
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        return props;
    }

    public String getPropertyPrefix() {
        return propPrefix;
    }

    public void setPropertyPrefix(String p) {
        propPrefix = p;
    }

    public static List<TimerRateHolder> getTimerRateHolders(String prefix, Properties properties) {

        List<TimerRateHolder> timerRateHolders = new LinkedList<TimerRateHolder>();
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        String paceFormatString = properties.getProperty(prefix
                + PaceFormatProperty, DEFAULT_PACE_FORMAT);
        String paceBaselineString = properties.getProperty(prefix
                + PaceBaselineProperty, DEFAULT_PACE_BASELINE_VALUE);
        SimpleDateFormat paceFormat = new SimpleDateFormat(paceFormatString);

        if (Debug.debugging("timerrateholder")) {
            Debug.output("TimerRateHolder timer rate pace pattern: "
                    + paceFormatString);
        }

        String timerRatesString = properties.getProperty(prefix
                + TimerRatesProperty);

        if (timerRatesString != null) {
            if (Debug.debugging("timerrateholder")) {
                Debug.output("TimerRateHolder reading timer rates: "
                        + timerRatesString);
            }

            Vector<String> rates = PropUtils.parseSpacedMarkers(timerRatesString);
            Iterator<String> it = rates.iterator();
            while (it.hasNext()) {
                String ratePrefix = (String) it.next();
                TimerRateHolder trh = new TimerRateHolder(paceFormat, paceBaselineString);
                trh.setProperties(prefix + ratePrefix, properties);

                if (trh.valid) {
                    timerRateHolders.add(trh);
                    if (Debug.debugging("timerrateholder")) {
                        Debug.output("TimerRateHolder adding " + trh);
                    }
                } else {
                    if (Debug.debugging("timerrateholder")) {
                        Debug.output("TimerRateHolder NOT adding " + ratePrefix);
                    }
                }
            }
        }

        return timerRateHolders;
    }

}

