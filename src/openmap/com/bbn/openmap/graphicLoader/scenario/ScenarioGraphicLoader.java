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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/ScenarioGraphicLoader.java,v $
// $RCSfile: ScenarioGraphicLoader.java,v $
// $Revision: 1.6 $
// $Date: 2004/03/04 04:14:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.scenario;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.gui.time.RealTimeHandler;
import com.bbn.openmap.gui.time.TimeConstants;
import com.bbn.openmap.gui.time.TimeSliderSupport;
import com.bbn.openmap.gui.time.TimerControlButtonPanel;
import com.bbn.openmap.gui.time.TimerRateComboBox;
import com.bbn.openmap.gui.time.TimerToggleButton;
import com.bbn.openmap.io.CSVFile;
import com.bbn.openmap.layer.location.LocationHandler;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.graphicLoader.MMLGraphicLoader;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;

/**
 * The ScenarioGraphicLoader contains all the ScenarioGraphics and
 * manages the time for the scenario.  The different organization
 * objects are represented in a location file that lists a name and an
 * icon URL.  An activities file lists the different steps for the
 * organizations - where they are (lat/lon) and when.  A timer in the
 * loader positions the organizations for that time, interpolating
 * location for times between time/location definitions.  If an
 * organization stops to wait in a position, two activity locations
 * should be defined for that stop, for when the organization arrived
 * to that spot and when then left. Different properties need to be
 * set for the ScenarioGraphicLoader to let it know how the files,
 * Comma Separated Value (CSV) files, should be interpreted.<p>
 *
 * The ScenarioGraphicLoader also lets you define different steps for
 * how to control the time, i.e. the timer rate.  The clock interval
 * for the timer rate is measured in milliseconds, specifying how
 * often the map should be updated.  Note that the more often the map
 * is updated, the more unresponsive the map can become.  The pace for
 * the timer rate is how much 'senario time' passes for each time the
 * clock updates.  You can define those steps in different formats,
 * but the default format for the pace is hh:mm:ss for
 * hours:minutes:seconds.<p>
 *
 * Sample properties: <pre>
 *
 * scenario.class=com.bbn.openmap.graphicLoader.scenario.ScenarioGraphicLoader
 * scenario.prettyName=Test Scenario
 * scenario.locationFile=org-list.csv
 * scenario.locationFileHasHeader=true
 * scenario.nameIndex=0
 * scenario.iconIndex=5
 * scenario.activityFile=org-activities.csv
 * scenario.activityFileHasHeader=true
 * scenario.activityNameIndex=1
 * scenario.latIndex=9
 * scenario.lonIndex=10
 * scenario.timeFormat=d-MMM-yyyy HH:mm
 * scenario.timeIndex=7
 * # If no icon defined, used for org. location markers edge.
 * scenario.lineColor=aaaaaa33
 * # If no icon defined, used for org. location markers fill.
 * scenario.fillColor=aaaaaa33
 * # Used for lines for total scenario paths
 * scenario.selectColor=aaaa0000
 * 
 * scenario.timerRates=vs s m a q f vf
 * scenario.vs.prettyName=Very Slow
 * scenario.vs.clockIntervalMillis=2000
 * scenario.vs.pace=00:06:00
 * scenario.s.prettyName=Slow
 * scenario.s.clockIntervalMillis=1000
 * scenario.s.pace=00:06:00
 * scenario.m.prettyName=Moderate
 * scenario.m.clockIntervalMillis=400
 * scenario.m.pace=00:06:00
 * scenario.a.prettyName=Average
 * scenario.a.clockIntervalMillis=200
 * scenario.a.pace=00:06:00
 * scenario.q.prettyName=Quick
 * scenario.q.clockIntervalMillis=100
 * scenario.q.pace=00:06:00
 * scenario.f.prettyName=Fast
 * scenario.f.clockIntervalMillis=40
 * scenario.f.pace=00:06:00
 * scenario.vf.prettyName=Very Fast
 * scenario.vf.clockIntervalMillis=10
 * scenario.vf.pace=01:00:00
 * </pre>
 */
public class ScenarioGraphicLoader extends MMLGraphicLoader 
    implements Tool, ComponentListener, DataBoundsProvider, RealTimeHandler, TimeConstants {

    public final static String TOTAL_SCENARIO_MODE = "TOTAL_SCENARIO";
    public final static String SNAPSHOT_SCENARIO_MODE = "SNAPSHOT_SCENARIO";
    public final static String SCENARIO_MODE_CMD = "SCENARIO_MODE_CMD";

    public final static String DefaultTimerIntervalFormat = "HH:mm:ss";
    public final static String DefaultPaceBaselineString = "00:00:00";

    protected String totalScenarioIconName = "totalScenarioTime.png";
    protected String snapshotIconName = "snapshotScenarioTime.png";

    /** locationFile */
    public final static String LocationFileProperty = "locationFile";
    /** locationFileHasHeader */
    public final static String LocationHeaderProperty = "locationFileHasHeader";
    /** iconIndex */
    public final static String IconIndexProperty = "iconIndex";
    /** nameIndex */
    public final static String NameIndexProperty = "nameIndex";

    /** activityFile */
    public final static String ActivityFileProperty = "activityFile";
    /** activityNameIndex */
    public final static String ActivityNameIndexProperty = "activityNameIndex";
    /** activityFileHasHeader */
    public final static String ActivityHeaderProperty = "activityFileHasHeader";
    /** latIndex */
    public final static String LatIndexProperty = "latIndex";
    /** lonIndex */
    public final static String LonIndexProperty = "lonIndex";
    /** timeIndex */
    public final static String TimeIndexProperty = "timeIndex";
    /** eastIsNeg */
    public final static String EastIsNegProperty = "eastIsNeg";
    /** showNames */
    public final static String ShowNamesProperty = LocationHandler.ShowNamesProperty;
    /** defaultURL */
    public final static String DefaultIconURLProperty = "defaultURL";
    /** timeFormat */
    public final static String TimeFormatProperty = "timeFormat";
    /** timerInvervalFormat */
    public final static String TimerIntervalFormatProperty = "timerIntervalFormat";
    /** timerPaceBaseline */
    public final static String TimerPaceBaselineProperty = "timerPaceBaseline";
    /** clockIntervalMillis */
    public final static String ClockIntervalProperty = "clockIntervalMillis";
    /** pace */
    public final static String PaceProperty = "pace";
    /** timerRates */
    public final static String TimerRatesProperty = "timerRates";

    protected String locationFile;
    protected boolean locationHeader = true;
    protected int nameIndex;
    protected int iconIndex;
    protected String activityFile;
    protected boolean activityHeader = true;
    protected int activityNameIndex;
    protected int latIndex;
    protected int lonIndex;
    protected int timeIndex;
    protected boolean eastIsNeg = false;

    /**
     * TimeFormat default is similar to IETF standard date syntax:
     * "Sat, 12 Aug 1995 13:30:00 GMT" represented by (EEE, d MMM yyyy
     * HH:mm:ss z), except for the local timezone.
     */
    protected SimpleDateFormat timeFormat = 
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

    /**
     * The TimerIntervalFormat controls how the pace can be specified
     * for the rate settings.  The default is HH:mm:ss, you can use
     * the timerIntervalFormat property to set it to something else.
     */
    protected SimpleDateFormat timerIntervalFormat = 
        new SimpleDateFormat(DefaultTimerIntervalFormat);

    protected String timerPaceBaselineString = DefaultPaceBaselineString;

    protected long startTime = Long.MAX_VALUE;
    protected long endTime = Long.MIN_VALUE;
    protected long time = 0;
    protected int timeIncrement = 1;
    protected String mode = TOTAL_SCENARIO_MODE;
    protected boolean timeWrap = (mode == SNAPSHOT_SCENARIO_MODE);
    protected int clockDirection = 1;

    /** Icon URL for points to use as default.  May be null. */
    protected String defaultIconURL;
    protected boolean showNames = false;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected ScenarioGraphicList scenarioGraphics = null;
    protected LinkedList timerRates;
    protected DrawingAttributes drawingAttributes = null;
    protected DataBounds dataBounds = null;
    protected Date timeDate = null;

    /// GUI ToolPanel widgets.  Kept here to make their visibility adjustable.
    protected JToggleButton timeWrapToggle;
    protected JLabel timeLabel;
    protected TimerControlButtonPanel timerControl;
    protected TimerRateComboBox timerRateControl;
    protected JSlider timeSlider;
    protected TimeSliderSupport timeSliderSupport;

    public ScenarioGraphicLoader() {
        drawingAttributes = new DrawingAttributes();
        dataBounds = new DataBounds();
        setUpdateInterval(500); // Default 1/2 second.
    }

    /**
     * The main method call in the ScenarioGraphicLoader that actually
     * modifies the OMGraphics and updates the map.
     */
    public synchronized void manageGraphics() {
        Projection p = getProjection();
        OMGraphicHandler receiver = getReceiver();
        boolean DEBUG = Debug.debugging("scenario");
        if (receiver != null && p != null) {
            if (scenarioGraphics == null) {
                scenarioGraphics = createData();

                // Update limits
                if (timeSliderSupport != null) {
                    timeSliderSupport.setStartTime(startTime);
                    timeSliderSupport.setEndTime(endTime);
                }
            }

            if (mode == TOTAL_SCENARIO_MODE) {
                if (DEBUG) {
                    Debug.output("ScenarioGraphicLoader (" + getName() + 
                                 ") generating total scenario ");
                }
                scenarioGraphics.generateTotalScenario(p);
            } else {
                long currentTime = getTime();
                if (DEBUG) {
                    Debug.output("ScenarioGraphicLoader (" + getName() + 
                                 ") snapshot at " + currentTime);
                }
                scenarioGraphics.generateSnapshot(p, currentTime);
            }
            if (DEBUG) {
                Debug.output("ScenarioGraphicLoader (" + getName() + 
                             ") setting list of " + 
                             scenarioGraphics.size() + " scenario graphics");
            }
            receiver.setList(scenarioGraphics);
        } else {
            Debug.output("ScenarioGraphicLoader (" + getName() + 
                         ") doesn't have a connection to the map.");
        }
    }

    // RealTimeHandler methods.

    public void setPace(int pace) {
        timeIncrement = pace;
    }

    public int getPace() {
        return timeIncrement;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long t) {
        time = t;

        if (timeDate != null && timeLabel != null) {
            timeDate.setTime(time);
            timeLabel.setText(timeFormat.format(timeDate));
        }

        if (timeSliderSupport != null) {
            timeSliderSupport.update(time);
        }

        // If the time has been set to be somewhere within the boundaries 
        // of time, make sure we're showing the current location of
        // everything.
        if (time > startTime && time < endTime) {
            mode = SNAPSHOT_SCENARIO_MODE;
        }

        manageGraphics();
    }

    public void startClock() {
        if (!timer.isRunning()) {
            pcs.firePropertyChange(TIMER_RUNNING_STATUS, TIMER_STOPPED, (getClockDirection() > 0?TIMER_FORWARD:TIMER_BACKWARD));
        }
        if (Debug.debugging("scenario")) {
            Debug.output("ScenarioGraphicLoader " + getName() + ": Starting clock");
        }
        timer.restart();
    }

    public void stopClock() {
        if (timer.isRunning()) {
            pcs.firePropertyChange(TIMER_RUNNING_STATUS, (getClockDirection() > 0?TIMER_FORWARD:TIMER_BACKWARD), TIMER_STOPPED);
            timer.stop();
        }
    }

    /**
     * Set whether time increases or decreases when the clock is run.
     * If direction is zero or greater, clock runs forward.  If
     * direction is negative, clock runs backward.
     */
    public void setClockDirection(int direction) {
        String oldDirection = clockDirection > 0?TIMER_FORWARD:TIMER_BACKWARD;
        
        if (direction >= 0) {
            clockDirection = 1;
        } else {
            clockDirection = -1;
        }

        String newDirection = clockDirection > 0?TIMER_FORWARD:TIMER_BACKWARD;

        if (timer.isRunning()) {
            if (oldDirection != newDirection) {
                pcs.firePropertyChange(TIMER_RUNNING_STATUS, oldDirection, newDirection);
            }
        }
    }

    /**
     * Get whether time increases or decreases when the clock is run.
     * If direction is zero or greater, clock runs forward.  If
     * direction is negative, clock runs backward.
     */
    public int getClockDirection() {
        return clockDirection;
    }

    /**
     * Call setTime with the amount given added to the current time.
     * The amount should be negative if you are going backward through
     * time.  You need to make sure manageGraphics is called for the
     * map to update.<p>
     *
     * This method calls changeTimeBy(amount, wrapAroundTimeLimits),
     * with wrapAroundTimeLimits being true of the mode of the
     * ScenarioGraphicLoader is SNAPSHOT_SCENARIO_MODE.
     *
     * @param amount to change the current time by, in milliseconds.
     */
    protected void changeTimeBy(long amount) {
        changeTimeBy(amount, timeWrap);
    }

    /**
     * Call setTime with the amount given added to the current time.
     * The amount should be negative if you are going backward through
     * time.  You need to make sure manageGraphics is called for the
     * map to update.
     *
     * @param amount to change the current time by, in milliseconds.
     * @param wrapAroundTimeLimits if true, the time will be set as if
     * the start and end times ofthe scenario are connected, so that
     * moving the time past the time scale in either direction will
     * put the time at the other end of the scale.
     */
    protected void changeTimeBy(long amount, boolean wrapAroundTimeLimits) {

        long oldTime = getTime();
        long newTime;

        if (oldTime > endTime || oldTime < startTime) {
            if (wrapAroundTimeLimits) {
                if (amount >= 0) {
                    newTime = startTime + amount;
                } else {
                    newTime = endTime + amount;
                }
            } else {
                if (amount >= 0) {
                    newTime = startTime;
                } else {
                    newTime = endTime;
                }

                if (timer.isRunning()) {
                    mode = TOTAL_SCENARIO_MODE;
                    stopClock();
                    setTime(newTime);
                    return;
                }
            }
        } else {
            newTime = oldTime + amount;
        }

        if (Debug.debugging("scenario")) {
            Debug.output("ScenarioGraphicLoader (" + getName() + 
                         ") changing time by [" +
                         amount + "] to (" + newTime + ")");
        }

        mode = SNAPSHOT_SCENARIO_MODE;
        setTime(newTime);
    }

    /**
     * Move the clock forward one clock interval.
     */
    public void stepForward() {
        changeTimeBy(timeIncrement, true);
    }
    
    /**
     * Move the clock back one clock interval.
     */
    public void stepBackward() {
        changeTimeBy(-timeIncrement, true);
    }

    /**
     * ActionListener interface, gets called when the timer goes ping
     * if their isn't a command with the ActionEvent.  Otherwise, the
     * command should be filled in.
     */
    public void actionPerformed(ActionEvent ae) {
        // Will check to see if any GUI commands trigger this 
        // method, otherwise, it should just change the time.
        String cmd = ae.getActionCommand();
        if (cmd == SCENARIO_MODE_CMD) {
            timeWrap = ((JToggleButton)ae.getSource()).isSelected();
        } else {
            changeTimeBy(timeIncrement * clockDirection);
        }
    }

    /**
     * Read the data files and construct the ScenarioPoints.
     */
    public synchronized ScenarioGraphicList createData() {
        ScenarioGraphicList list = new ScenarioGraphicList();
        Hashtable library = new Hashtable();
        // Create location data
        if (locationFile != null &&
            nameIndex != -1) {
            Debug.message("scenario", "Reading location file...");
            try {
                CSVFile locations = new CSVFile(locationFile);
                locations.loadData();
                Iterator records = locations.iterator();
                while (records.hasNext()) {
                    String name = null;
                    String icon = null;
                    Vector record = (Vector) records.next();

                    if (record.size() == 0) continue;

                    name = (String)record.elementAt(nameIndex);

                    if (iconIndex != -1) {
                        icon = (String)record.elementAt(iconIndex);
                    }

                    if (name != null) {
                        ScenarioPoint location = new ScenarioPoint(name, icon);
                        location.setShowName(showNames);
                        drawingAttributes.setTo(location);
                        library.put(name.intern(), location);
                        list.add(location);
                    } else {
                        Debug.error("ScenaroGraphicLoader: no name to use to create location: " + name);
                    }
                }
            } catch (MalformedURLException murle) {
                Debug.error("ScenarioGraphicLoader: problem finding the location file: " + 
                            locationFile);
                return list;
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                Debug.error("ScenarioGraphicLoader: problem with parsing location file: " + locationFile);
                if (Debug.debugging("scenario")) {
                    Debug.output("The problem is with one of the indexes into the file: \n" +
                                 aioobe.getMessage());
                    aioobe.printStackTrace();
                }
            } catch (NullPointerException npe) {
                Debug.error("ScenarioGraphicLoader (" + getName() + ") null pointer exception, most likely a problem finding the organization data file");
            }
        } else {
            Debug.error("ScenarioGraphicLoader(" + getName() + 
                        "): Location file (" + 
                        locationFile + ") not configured.");
            return list;
        }

        // OK, got the locations built up, need to fill up the scenario
        // Create location data
        if (activityFile != null &&
            activityNameIndex != -1 &&
            latIndex != -1 && lonIndex != -1 && timeIndex != -1) {
            Debug.message("scenario", "Reading activity file...");
            try {
                CSVFile activities = new CSVFile(activityFile);
                activities.loadData(); // numbers as strings == false
                Iterator records = activities.iterator();
                while (records.hasNext()) {
                    String name = null;
                    float lat;
                    float lon;
                    long time;

                    Vector record = (Vector) records.next();

                    if (record.size() == 0) continue;

                    name = record.elementAt(activityNameIndex).toString().intern();

                    try {
                        lat = ((Double)record.elementAt(latIndex)).floatValue();
                        lon = ((Double)record.elementAt(lonIndex)).floatValue();

                        // parse time from string, ending up with
                        // milliseconds from time epoch.
                        String timeString = (String)record.elementAt(timeIndex);
                        timeDate = timeFormat.parse(timeString);
                        time = timeDate.getTime();

                        if (time < startTime) {
                            startTime = time;
                        }

                        if (time > endTime) {
                            endTime = time;
                        }

                        dataBounds.add((double)lon, (double)lat);

                        if (name != null) {
                            ScenarioPoint point = (ScenarioPoint)library.get(name);
                            if (point != null) {
                                TimeStamp ts = new TimeStamp(lat, lon, time);
                                point.addTimeStamp(ts);
                            } else {
                                Debug.error("SenaroGraphicLoader: ScenarioPoint not found for " + name + ", entry: " + record);
                            }
                        } else {
                            Debug.error("SenaroGraphicLoader: no name to use to create activity point: " + name);
                        }

                    } catch (ClassCastException cce) {

                        Object obj0 = record.elementAt(activityNameIndex);
                        Object obj1 = record.elementAt(latIndex);
                        Object obj2 = record.elementAt(lonIndex);
                        Object obj3 = record.elementAt(timeIndex);

                        Debug.error(
                            "ScenarioGraphicLoader(" + getName() + 
                            ") has problem with indexes in activity file for " + 
                            obj0 +  " (" + obj0.getClass().getName() + ")" +
                            ":\n\tlat index = " + latIndex + 
                            ", value = " + obj1 +  " (" + obj1.getClass().getName() + 
                            ")\n\t lon index = " + lonIndex + 
                            ", value = " + obj2 + " (" + obj2.getClass().getName() + 
                            ")\n\t time index = " + timeIndex + 
                            ", value = " + obj3 + " (" + obj3.getClass().getName() + 
                            ")");
                    } catch (ParseException pe) {
                        Debug.output("ScenarioGraphicLoader(" + getName() +
                                     ") has problem with time format. " + 
                                     pe.getMessage());
                    }
                }
            } catch (MalformedURLException murle) {
                Debug.error("ScenarioGraphicLoader: problem with activity file: " + 
                            activityFile);
                return list;
            } catch (NullPointerException npe) {
                Debug.error("ScenarioGraphicLoader (" + getName() + ") null pointer exception, most likely a problem finding the activites data file");
            }
        } else {
            Debug.error("ScenarioGraphicLoader(" + getName() + "): Activity file (" + 
                        activityFile + ") not configured.");
            return list;
        }

        this.time = startTime;

        Debug.message("scenario", "Reading files OK");

        return list;
    }

    /** 
     * The properties and prefix are managed and decoded here, for
     * the standard uses of the ScenarioGraphicLoader.
     *
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.  
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        drawingAttributes.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        locationFile = properties.getProperty(
            prefix + LocationFileProperty);
        iconIndex = PropUtils.intFromProperties(
            properties, prefix + IconIndexProperty, -1);
        nameIndex = PropUtils.intFromProperties(
            properties, prefix + NameIndexProperty, -1);
        activityNameIndex = PropUtils.intFromProperties(
            properties, prefix + ActivityNameIndexProperty, -1);
        activityFile = properties.getProperty(
            prefix + ActivityFileProperty);
        latIndex = PropUtils.intFromProperties(
            properties, prefix + LatIndexProperty, -1);
        lonIndex = PropUtils.intFromProperties(
            properties, prefix + LonIndexProperty, -1);
        timeIndex = PropUtils.intFromProperties(
            properties, prefix + TimeIndexProperty, -1);
        eastIsNeg = PropUtils.booleanFromProperties(
            properties, prefix + EastIsNegProperty, eastIsNeg);
        showNames = PropUtils.booleanFromProperties(
            properties, prefix + ShowNamesProperty, showNames);
        defaultIconURL = properties.getProperty(
            prefix + DefaultIconURLProperty);
        locationHeader = PropUtils.booleanFromProperties(
            properties, prefix + LocationHeaderProperty, false);
        activityHeader = PropUtils.booleanFromProperties(
            properties, prefix + ActivityHeaderProperty, false);

        String timeFormatString = properties.getProperty(
                prefix+TimeFormatProperty, 
                ((SimpleDateFormat)timeFormat).toPattern());

        timeFormat = new SimpleDateFormat(timeFormatString);

        String timerIntervalFormatString = properties.getProperty(
            prefix+TimerIntervalFormatProperty, 
            ((SimpleDateFormat)timerIntervalFormat).toPattern());
        
        timerPaceBaselineString = properties.getProperty(
            prefix+TimerPaceBaselineProperty, 
            timerPaceBaselineString);

        timerIntervalFormat = new SimpleDateFormat(timerIntervalFormatString);

        if (Debug.debugging("scenario")) {
            Debug.output("ScenarioGraphicLoader timer rate pace pattern: " + 
                         timerIntervalFormatString);
        }

        String timerRatesString = properties.getProperty(prefix + TimerRatesProperty);
        timerRates = new LinkedList();
        if (timerRatesString != null) {
            if (Debug.debugging("scenario")) {
                Debug.output("ScenarioGraphicLoader reading timer rates: " + 
                             timerRatesString);
            }
            Vector rates = PropUtils.parseSpacedMarkers(timerRatesString);
            Iterator it = rates.iterator();
            while(it.hasNext()) {
                String ratePrefix = (String)it.next();
                TimerRateHolder trh = new TimerRateHolder(timerIntervalFormat, timerPaceBaselineString);
                trh.setProperties(prefix + ratePrefix, properties);
                if (trh.valid) {
                    timerRates.add(trh);
                    if (Debug.debugging("scenario")) {
                        Debug.output("ScenarioGraphicLoader adding " + trh);
                    }
                } else {
                    if (Debug.debugging("scenario")) {
                        Debug.output("ScenarioGraphicLoader NOT adding " + ratePrefix);
                    }
                }
            }
        } else {
            if (Debug.debugging("scenario")) {
                Debug.output("ScenarioGraphicLoader has no timer rate information");
            }
        }
        
        if (Debug.debugging("scenario")) {
            Debug.output("ScenarioGraphicLoader indexes:" +
                         "\n\tlocation file: " + locationFile +
                         "\n\tlocation file has header: " +  locationHeader +
                         "\n\tnameIndex = " + nameIndex + 
                         "\n\ticonIndex = " + iconIndex + 
                         "\n\tactivity file: " + activityFile +
                         "\n\tactivity file has header: " +  activityHeader +
                         "\n\tlatIndex = " + latIndex + 
                         "\n\tlonIndex = " + lonIndex + 
                         "\n\ttimeIndex = " + timeIndex);
        }
    }

    /**
     * PropertyConsumer method, to fill in a Properties object,
     * reflecting the current values of the layer.  If the layer has a
     * propertyPrefix set, the property keys should have that prefix
     * plus a separating '.' prepended to each propery key it uses for
     * configuration.
     *
     * @param props a Properties object to load the PropertyConsumer
     * properties into.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.  
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + LocationFileProperty, PropUtils.unnull(locationFile));
        props.put(prefix + LocationHeaderProperty, new Boolean(locationHeader).toString());
        props.put(prefix + NameIndexProperty, 
                  (nameIndex != -1?Integer.toString(nameIndex):""));

        props.put(prefix + ActivityFileProperty, PropUtils.unnull(activityFile));
        props.put(prefix + ActivityHeaderProperty, new Boolean(activityHeader).toString());
        props.put(prefix + ActivityNameIndexProperty, 
                  (activityNameIndex != -1?Integer.toString(activityNameIndex):""));
        props.put(prefix + EastIsNegProperty, new Boolean(eastIsNeg).toString());
        props.put(prefix + ShowNamesProperty, new Boolean(showNames).toString());
        props.put(prefix + LatIndexProperty, 
                  (latIndex != -1?Integer.toString(latIndex):""));
        props.put(prefix + LonIndexProperty, 
                  (lonIndex != -1?Integer.toString(lonIndex):""));
        props.put(prefix + TimeIndexProperty, 
                  (timeIndex != -1?Integer.toString(timeIndex):""));
        props.put(prefix + IconIndexProperty, 
                  (iconIndex != -1?Integer.toString(iconIndex):""));
        props.put(prefix + DefaultIconURLProperty, PropUtils.unnull(defaultIconURL));
        drawingAttributes.getProperties(props);
        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).  This method takes care of the basic LocationHandler
     * parameters, so any LocationHandlers that extend the
     * AbstractLocationHandler should call this method, too, before
     * adding any specific properties.
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.  
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.put(LocationFileProperty, "URL of file containing location information.");
        list .put(LocationFileProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        list.put(LocationHeaderProperty, "Location file has a header row to be ignored.");
        list.put(LocationHeaderProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(ActivityFileProperty, "URL of file containing scenario activity information.");
        list .put(ActivityFileProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        list.put(ActivityHeaderProperty, "Activity file has a header row to be ignored.");
        list.put(ActivityHeaderProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(EastIsNegProperty, "Flag to note that negative latitude are over the eastern hemisphere.");
        list.put(EastIsNegProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(ShowNamesProperty, "Flag to note that locations should display their names.");
        list.put(ShowNamesProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(NameIndexProperty, "The column index, in the location file, of the location label text.");

        list.put(ActivityNameIndexProperty, "The column index, in the activity file, of the location label text.");

        list.put(LatIndexProperty, "The column index, in the activity file, of the latitudes.");
        list.put(LonIndexProperty, "The column index, in the activity file, of the longitudes.");
        list.put(TimeIndexProperty, "The column index, in the activity file, of the time of the activity.");
        list.put(IconIndexProperty, "The column index, in the location file, of the icon for locations (optional).");
        list.put(DefaultIconURLProperty, "The URL of an image file to use as a default for the location markers (optional).");

        drawingAttributes.getPropertyInfo(list);

        return list;
    }

//     /** 
//      * Tool Method.  The retrieval tool's interface. This is added to the
//      * tool bar.
//      *
//      * @return String The key for this tool.
//      */
//     public Container getFace() {
//      JToolBar jtb = new JToolBar();
//      jtb.setFloatable(false);

// //   TimerToggleButton ttb = new TimerToggleButton(this);
// //   ttb.setToolTipText("Start/Stop Scenario Timer");
// //   jtb.add(ttb);
// //   pcs.addPropertyChangeListener(TIMER_RUNNING_STATUS, ttb);

//      try {
//          URL url = PropUtils.getResourceOrFileOrURL(this, snapshotIconName);
//          ImageIcon snapshotIcon = new ImageIcon(url);

//          url = PropUtils.getResourceOrFileOrURL(this, totalScenarioIconName);
//          ImageIcon totalScenarioIcon = new ImageIcon(url);

//          timeWrapToggle = new JToggleButton(totalScenarioIcon, timeWrap);
//          timeWrapToggle.setSelectedIcon(snapshotIcon);
//          timeWrapToggle.setActionCommand(SCENARIO_MODE_CMD);
//          timeWrapToggle.addActionListener(this);
//          timeWrapToggle.setToolTipText("Wrap Scenario Time Scale");
//          jtb.add(timeWrapToggle);

//      } catch (MalformedURLException murle) {
//          Debug.error("ScenarioGraphicLoader " + getName() + ":" + murle.getMessage());
//      } catch (NullPointerException npe) {
//          Debug.error("ScenarioGraphicLoader " + getName() + ":" + npe.getMessage());
//      }

//      timerControl = new TimerControlButtonPanel(this);
//      jtb.add(timerControl);
//      pcs.addPropertyChangeListener(TIMER_RUNNING_STATUS, timerControl);

//      String runningStatus = timer.isRunning()?(getClockDirection() > 0?TIMER_FORWARD:TIMER_BACKWARD):TIMER_STOPPED;
//      pcs.firePropertyChange(TIMER_RUNNING_STATUS, null,
//                             runningStatus);

//      timerRateControl = new TimerRateComboBox(this);
//      timerRateControl.setToolTipText("Change clock rate for Scenario");

//      Iterator it = timerRates.iterator();
//      while (it.hasNext()) {
//          TimerRateHolder trh = (TimerRateHolder)it.next();
//          timerRateControl.add(trh.label, trh.clock, trh.pace);
//      }

//      int si = timerRates.size()/2;
//      if (si > 0) {
//          timerRateControl.setSelectedIndex(si);
//      }

//      jtb.add(timerRateControl);

//      timeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
//      timeSliderSupport = new TimeSliderSupport(timeSlider, this, startTime, endTime);
//      jtb.add(timeSlider);

//      timeLabel = new JLabel();
//      java.awt.Font defaultFont = timeLabel.getFont();
//      timeLabel.setFont(new java.awt.Font(defaultFont.getName(), defaultFont.getStyle(), 9));
//      jtb.add(timeLabel);

//      return jtb;
//     }

    /** 
     * Tool Method.  The retrieval tool's interface. This is added to the
     * tool bar.
     *
     * @return String The key for this tool.
     */
    public Container getFace() {
        JPanel jtb = new JPanel();

        Box bigBox = Box.createHorizontalBox();
        Box rightBox = Box.createVerticalBox();
        Box leftBox = Box.createVerticalBox();
        Box innerBox = Box.createHorizontalBox();

        try {
            URL url = PropUtils.getResourceOrFileOrURL(this, snapshotIconName);
            ImageIcon snapshotIcon = new ImageIcon(url);

            url = PropUtils.getResourceOrFileOrURL(this, totalScenarioIconName);
            ImageIcon totalScenarioIcon = new ImageIcon(url);

            timeWrapToggle = new JToggleButton(totalScenarioIcon, timeWrap);
            timeWrapToggle.setSelectedIcon(snapshotIcon);
            timeWrapToggle.setActionCommand(SCENARIO_MODE_CMD);
            timeWrapToggle.addActionListener(this);
            timeWrapToggle.setToolTipText("Wrap Scenario Time Scale");
//          jtb.add(timeWrapToggle);
            innerBox.add(timeWrapToggle);

        } catch (MalformedURLException murle) {
            Debug.error("ScenarioGraphicLoader " + getName() + ":" + murle.getMessage());
        } catch (NullPointerException npe) {
            Debug.error("ScenarioGraphicLoader " + getName() + ":" + npe.getMessage());
        }

        timerControl = new TimerControlButtonPanel(this);
//      jtb.add(timerControl);
        innerBox.add(timerControl);
        rightBox.add(innerBox);

        pcs.addPropertyChangeListener(TIMER_RUNNING_STATUS, timerControl);

        String runningStatus = timer.isRunning()?(getClockDirection() > 0?TIMER_FORWARD:TIMER_BACKWARD):TIMER_STOPPED;
        pcs.firePropertyChange(TIMER_RUNNING_STATUS, null,
                               runningStatus);

        timerRateControl = new TimerRateComboBox(this);
        timerRateControl.setToolTipText("Change clock rate for Scenario");

        Iterator it = timerRates.iterator();
        while (it.hasNext()) {
            TimerRateHolder trh = (TimerRateHolder)it.next();
            timerRateControl.add(trh.label, trh.clock, trh.pace);
        }

        int si = timerRates.size()/2;
        if (si > 0) {
            timerRateControl.setSelectedIndex(si);
        }

//      jtb.add(timerRateControl);
        rightBox.add(timerRateControl);

        timeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
        timeSliderSupport = new TimeSliderSupport(timeSlider, this, startTime, endTime);
//      jtb.add(timeSlider);
        leftBox.add(timeSlider);

        timeLabel = new JLabel(" ", SwingConstants.CENTER);
        java.awt.Font defaultFont = timeLabel.getFont();
        timeLabel.setFont(new java.awt.Font(defaultFont.getName(), defaultFont.getStyle(), 10));
//      jtb.add(timeLabel);
        leftBox.add(timeLabel);

        bigBox.add(leftBox);
        bigBox.add(rightBox);
        jtb.add(bigBox);

        return jtb;
    }

    /** 
     * Tool Method.  The retrieval key for this tool.
     *
     * @return String The key for this tool.
     **/
    public String getKey() {
        return getName();
    }
    
    /** 
     * Tool Method.  Set the retrieval key for this tool.
     *
     * @param aKey The key for this tool.
     */
    public void setKey(String aKey) {
        setName(aKey);
    }

    /**
     * DataBoundsProvider method.
     */
    public DataBounds getDataBounds() {
        return dataBounds;
    }

    public void componentResized(ComponentEvent ce) {}
    public void componentMoved(ComponentEvent ce) {}
    public void componentHidden(ComponentEvent ce) {}
    public void componentShown(ComponentEvent ce) {}


    /**
     * An OMGraphicList that knows what a ScenarioGraphic is, and
     * knows when to tell it to draw itself at a particular time, or
     * if it should draw its entire scenario path.
     */
    public class ScenarioGraphicList extends OMGraphicList {
        public ScenarioGraphicList() {
            super();
        }
        
        public void generateTotalScenario(Projection p) {
            synchronized (graphics) {
                Iterator it = iterator();
                while (it.hasNext()) {
                    OMGraphic graphic = (OMGraphic) it.next();
                    if (graphic instanceof ScenarioGraphic) {
                        ((ScenarioGraphic)graphic).generateTotalScenario(p);
                    } else {
                        graphic.generate(p);
                    }
                }
            }
        }

        public void generateSnapshot(Projection p, long time) {
            synchronized (graphics) {
                Iterator it = iterator();
                while (it.hasNext()) {
                    OMGraphic graphic = (OMGraphic) it.next();
                    if (graphic instanceof ScenarioGraphic) {
                        ((ScenarioGraphic)graphic).generateSnapshot(p, time);
                    } else {
                        graphic.generate(p);
                    }
                }
            }
        }
    }

    /**
     * A convenience class that keeps track of a relationship between
     * real-time changes and scenario-time changes.
     */
    public class TimerRateHolder implements PropertyConsumer {
        String label;
        int clock;
        int pace;
        SimpleDateFormat paceFormat;
        String paceZero;
        boolean valid = false;
        String propPrefix;

        /**
         * Create a TimerRateHolder with a date format, and a baseline
         * time.  The default baseline time is "00:00:00", so if you
         * need to change that, use this constructor.  The pace for
         * this TimerRateHolder should be a relative amount of time,
         * and that relativity, taking into account the locale offset
         * to GMT, is given by the baseline time.  The baseline time
         * should match the format given.
         */
        public TimerRateHolder(SimpleDateFormat simpleDateFormat, String dpz) {
            paceFormat = simpleDateFormat;
            paceZero = dpz;
        }

        public String toString() {
            return "ScenarioGraphicLoader.TimerRateHolder [" + label +
                ", clock:" + clock + ", pace:" + pace + "] (" + valid + ")";
        }

        public void setProperties(Properties props) {
            setProperties(null, props);
        }

        public void setProperties(String prefix, Properties props) {

            propPrefix = prefix;
            prefix = PropUtils.getScopedPropertyPrefix(prefix);

            try {
                label = props.getProperty(prefix + Layer.PrettyNameProperty);
                clock = PropUtils.intFromProperties(props, prefix + ClockIntervalProperty, -1);
                String paceString = props.getProperty(prefix + PaceProperty);
                pace = (int) (paceFormat.parse(paceString).getTime() - paceFormat.parse(paceZero).getTime());
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
    }
    
}
