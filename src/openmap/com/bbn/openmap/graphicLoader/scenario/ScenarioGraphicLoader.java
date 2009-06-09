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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/ScenarioGraphicLoader.java,v $
// $RCSfile: ScenarioGraphicLoader.java,v $
// $Revision: 1.9 $
// $Date: 2006/03/06 16:13:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.scenario;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingConstants;

import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.event.OMEventHandler;
import com.bbn.openmap.graphicLoader.MMLGraphicLoader;
import com.bbn.openmap.io.CSVFile;
import com.bbn.openmap.layer.location.LocationHandler;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.time.TemporalPoint;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.time.TimeBounds;
import com.bbn.openmap.time.TimeBoundsHandler;
import com.bbn.openmap.time.TimeBoundsProvider;
import com.bbn.openmap.time.TimeEvent;
import com.bbn.openmap.time.TimeEventListener;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.PropUtils;

/**
 * The ScenarioGraphicLoader contains all the ScenarioGraphics and manages the
 * time for the scenario. The different organization objects are represented in
 * a location file that lists a name and an icon URL. An activities file lists
 * the different steps for the organizations - where they are (lat/lon) and
 * when. A timer in the loader positions the organizations for that time,
 * interpolating location for times between time/location definitions. If an
 * organization stops to wait in a position, two activity locations should be
 * defined for that stop, for when the organization arrived to that spot and
 * when then left. Different properties need to be set for the
 * ScenarioGraphicLoader to let it know how the files, Comma Separated Value
 * (CSV) files, should be interpreted.
 * <p>
 * 
 * The ScenarioGraphicLoader also lets you define different steps for how to
 * control the time, i.e. the timer rate. The clock interval for the timer rate
 * is measured in milliseconds, specifying how often the map should be updated.
 * Note that the more often the map is updated, the more unresponsive the map
 * can become. The pace for the timer rate is how much 'senario time' passes for
 * each time the clock updates. You can define those steps in different formats,
 * but the default format for the pace is hh:mm:ss for hours:minutes:seconds.
 * <p>
 * 
 * Sample properties:
 * 
 * <pre>
 * 
 * 
 *    scenario.class=com.bbn.openmap.graphicLoader.scenario.ScenarioGraphicLoader
 *    scenario.prettyName=Test Scenario
 *    scenario.locationFile=org-list.csv
 *    scenario.locationFileHasHeader=true
 *    scenario.nameIndex=0
 *    scenario.iconIndex=5
 *    scenario.activityFile=org-activities.csv
 *    scenario.activityFileHasHeader=true
 *    scenario.activityNameIndex=1
 *    scenario.latIndex=9
 *    scenario.lonIndex=10
 *    scenario.timeFormat=d-MMM-yyyy HH:mm
 *    scenario.timeIndex=7
 *    # If no icon defined, used for org. location markers edge.
 *    scenario.lineColor=aaaaaa33
 *    # If no icon defined, used for org. location markers fill.
 *    scenario.fillColor=aaaaaa33
 *    # Used for lines for total scenario paths
 *    scenario.selectColor=aaaa0000
 *    
 *    scenario.timerRates=vs s m a q f vf
 *    scenario.vs.prettyName=Very Slow
 *    scenario.vs.clockIntervalMillis=2000
 *    scenario.vs.pace=00:06:00
 *    scenario.s.prettyName=Slow
 *    scenario.s.clockIntervalMillis=1000
 *    scenario.s.pace=00:06:00
 *    scenario.m.prettyName=Moderate
 *    scenario.m.clockIntervalMillis=400
 *    scenario.m.pace=00:06:00
 *    scenario.a.prettyName=Average
 *    scenario.a.clockIntervalMillis=200
 *    scenario.a.pace=00:06:00
 *    scenario.q.prettyName=Quick
 *    scenario.q.clockIntervalMillis=100
 *    scenario.q.pace=00:06:00
 *    scenario.f.prettyName=Fast
 *    scenario.f.clockIntervalMillis=40
 *    scenario.f.pace=00:06:00
 *    scenario.vf.prettyName=Very Fast
 *    scenario.vf.clockIntervalMillis=10
 *    scenario.vf.pace=01:00:00
 * 
 * </pre>
 */
public class ScenarioGraphicLoader extends MMLGraphicLoader implements
        ComponentListener, DataBoundsProvider, TimeBoundsProvider,
        TimeEventListener, OMEventHandler {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.graphicLoader.scenario.ScenarioGraphicLoader");

    public final static String TOTAL_SCENARIO_MODE = "TOTAL_SCENARIO";
    public final static String SNAPSHOT_SCENARIO_MODE = "SNAPSHOT_SCENARIO";
    public final static String SCENARIO_MODE_CMD = "SCENARIO_MODE_CMD";

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
    protected int orientation = SwingConstants.HORIZONTAL;
    protected String mode;
    protected boolean active = true;

    /** Icon URL for points to use as default. May be null. */
    protected String defaultIconURL;
    protected boolean showNames = false;
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected ScenarioGraphicList scenarioGraphics = null;
    protected DrawingAttributes drawingAttributes = null;
    protected DataBounds dataBounds = null;
    /**
     * TimeFormat default is similar to IETF standard date syntax:
     * "Sat, 12 Aug 1995 13:30:00 GMT" represented by (EEE, d MMM yyyy HH:mm:ss
     * z), except for the local timezone.
     */
    protected SimpleDateFormat timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

    protected TimeBounds timeBounds;
    protected long time;

    public ScenarioGraphicLoader() {
        drawingAttributes = new DrawingAttributes();
        dataBounds = new DataBounds();
        setUpdateInterval(500); // Default 1/2 second.
    }

    /**
     * The main method call in the ScenarioGraphicLoader that actually modifies
     * the OMGraphics and updates the map.
     */
    public synchronized void manageGraphics() {
        Projection p = getProjection();
        OMGraphicHandler receiver = getReceiver();
        boolean DEBUG = logger.isLoggable(Level.FINE);
        if (receiver != null && p != null) {
            if (scenarioGraphics == null) {
                scenarioGraphics = createData();

                // TODO update time Bounds
            }

            long currentTime = getTime();
            if (DEBUG) {
                logger.fine("ScenarioGraphicLoader (" + getName()
                        + ") snapshot at " + currentTime);
            }
            scenarioGraphics.generate(p,
                    currentTime,
                    getMode() == TOTAL_SCENARIO_MODE);

            if (DEBUG) {
                logger.fine("ScenarioGraphicLoader (" + getName()
                        + ") setting list of " + scenarioGraphics.size()
                        + " scenario graphics");
            }
            receiver.setList(scenarioGraphics);
        } else {
            logger.fine("ScenarioGraphicLoader (" + getName()
                    + ") doesn't have a connection to the map.");
        }
    }

    /**
     * Read the data files and construct the ScenarioPoints.
     */
    public synchronized ScenarioGraphicList createData() {
        ScenarioGraphicList list = new ScenarioGraphicList();
        Hashtable library = new Hashtable();
        // Create location data
        if (locationFile != null && nameIndex != -1) {
            logger.fine("Reading location file...");
            try {
                CSVFile locations = new CSVFile(locationFile);
                locations.loadData();
                Iterator records = locations.iterator();
                while (records.hasNext()) {
                    String name = null;
                    String icon = null;
                    Vector record = (Vector) records.next();

                    if (record.size() == 0)
                        continue;

                    name = (String) record.elementAt(nameIndex);

                    if (iconIndex != -1) {
                        icon = (String) record.elementAt(iconIndex);
                    }

                    if (name != null) {
                        ScenarioPoint location = new ScenarioPoint(name, icon);
                        location.setShowName(showNames);
                        drawingAttributes.setTo(location);
                        library.put(name.intern(), location);
                        list.add(location);
                    } else {
                        logger.warning("no name to use to create location: "
                                + name);
                    }
                }
            } catch (MalformedURLException murle) {
                logger.warning("problem finding the location file: "
                        + locationFile);
                return list;
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                logger.warning("problem with parsing location file: "
                        + locationFile);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("The problem is with one of the indexes into the file: \n"
                            + aioobe.getMessage());
                    aioobe.printStackTrace();
                }
            } catch (NullPointerException npe) {
                logger.warning("ScenarioGraphicLoader ("
                        + getName()
                        + ") null pointer exception, most likely a problem finding the organization data file");
            }
        } else {
            logger.warning("ScenarioGraphicLoader(" + getName()
                    + "): Location file (" + locationFile + ") not configured.");
            return list;
        }

        // OK, got the locations built up, need to fill up the
        // scenario
        // Create location data
        if (activityFile != null && activityNameIndex != -1 && latIndex != -1
                && lonIndex != -1 && timeIndex != -1) {
            logger.fine("Reading activity file...");
            timeBounds = new TimeBounds();
            try {
                CSVFile activities = new CSVFile(activityFile);
                activities.loadData(); // numbers as strings == false
                Iterator records = activities.iterator();
                while (records.hasNext()) {
                    String name = null;
                    float lat;
                    float lon;

                    Vector record = (Vector) records.next();

                    if (record.size() == 0)
                        continue;

                    name = record.elementAt(activityNameIndex)
                            .toString()
                            .intern();

                    try {
                        lat = ((Double) record.elementAt(latIndex)).floatValue();
                        lon = ((Double) record.elementAt(lonIndex)).floatValue();

                        // parse time from string, ending up with
                        // milliseconds from time epoch.
                        String timeString = (String) record.elementAt(timeIndex);
                        Date timeDate = timeFormat.parse(timeString);
                        long time = timeDate.getTime();

                        timeBounds.addTimeToBounds(time);
                        dataBounds.add((double) lon, (double) lat);

                        if (name != null) {
                            ScenarioPoint point = (ScenarioPoint) library.get(name);
                            if (point != null) {
                                LatLonPoint location = new LatLonPoint.Double(lat, lon);
                                TemporalPoint ts = new TemporalPoint(location, time);
                                point.addTimeStamp(ts);
                                
                                OMEvent event = new OMEvent(ts, name, time, location);
                                events.add(event);
                                                                
                            } else {
                                logger.warning("ScenarioPoint not found for "
                                        + name + ", entry: " + record);
                            }
                        } else {
                            logger.warning("no name to use to create activity point: "
                                    + name);
                        }

                    } catch (ClassCastException cce) {

                        Object obj0 = record.elementAt(activityNameIndex);
                        Object obj1 = record.elementAt(latIndex);
                        Object obj2 = record.elementAt(lonIndex);
                        Object obj3 = record.elementAt(timeIndex);

                        logger.warning("ScenarioGraphicLoader("
                                + getName()
                                + ") has problem with indexes in activity file for "
                                + obj0 + " (" + obj0.getClass().getName() + ")"
                                + ":\n\tlat index = " + latIndex + ", value = "
                                + obj1 + " (" + obj1.getClass().getName()
                                + ")\n\t lon index = " + lonIndex
                                + ", value = " + obj2 + " ("
                                + obj2.getClass().getName()
                                + ")\n\t time index = " + timeIndex
                                + ", value = " + obj3 + " ("
                                + obj3.getClass().getName() + ")");
                    } catch (ParseException pe) {
                        logger.fine("ScenarioGraphicLoader(" + getName()
                                + ") has problem with time format. "
                                + pe.getMessage());
                    }
                }
            } catch (MalformedURLException murle) {
                logger.warning("problem with activity file: "
                        + activityFile);
                return list;
            } catch (NullPointerException npe) {
                logger.warning("ScenarioGraphicLoader ("
                        + getName()
                        + ") null pointer exception, most likely a problem finding the activites data file");
            }
        } else {
            logger.warning("ScenarioGraphicLoader(" + getName()
                    + "): Activity file (" + activityFile + ") not configured.");
            return list;
        }

        logger.fine("Reading files OK");

        // Time will get updated automatically when the TimeEvent gets sent from
        // the clock.
        callForTimeBoundsReset();
        
        return list;
    }

    /**
     * The properties and prefix are managed and decoded here, for the standard
     * uses of the ScenarioGraphicLoader.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        drawingAttributes.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        locationFile = properties.getProperty(prefix + LocationFileProperty);
        iconIndex = PropUtils.intFromProperties(properties, prefix
                + IconIndexProperty, -1);
        nameIndex = PropUtils.intFromProperties(properties, prefix
                + NameIndexProperty, -1);
        activityNameIndex = PropUtils.intFromProperties(properties, prefix
                + ActivityNameIndexProperty, -1);
        activityFile = properties.getProperty(prefix + ActivityFileProperty);
        latIndex = PropUtils.intFromProperties(properties, prefix
                + LatIndexProperty, -1);
        lonIndex = PropUtils.intFromProperties(properties, prefix
                + LonIndexProperty, -1);
        timeIndex = PropUtils.intFromProperties(properties, prefix
                + TimeIndexProperty, -1);
        eastIsNeg = PropUtils.booleanFromProperties(properties, prefix
                + EastIsNegProperty, eastIsNeg);
        showNames = PropUtils.booleanFromProperties(properties, prefix
                + ShowNamesProperty, showNames);
        defaultIconURL = properties.getProperty(prefix + DefaultIconURLProperty);
        locationHeader = PropUtils.booleanFromProperties(properties, prefix
                + LocationHeaderProperty, false);
        activityHeader = PropUtils.booleanFromProperties(properties, prefix
                + ActivityHeaderProperty, false);

        String timeFormatString = properties.getProperty(prefix
                + TimeFormatProperty,
                ((SimpleDateFormat) timeFormat).toPattern());

        timeFormat = new SimpleDateFormat(timeFormatString);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ScenarioGraphicLoader indexes:"
                    + "\n\tlocation file: " + locationFile
                    + "\n\tlocation file has header: " + locationHeader
                    + "\n\tnameIndex = " + nameIndex + "\n\ticonIndex = "
                    + iconIndex + "\n\tactivity file: " + activityFile
                    + "\n\tactivity file has header: " + activityHeader
                    + "\n\tlatIndex = " + latIndex + "\n\tlonIndex = "
                    + lonIndex + "\n\ttimeIndex = " + timeIndex);
        }
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each propery key it uses for configuration.
     * 
     * @param props a Properties object to load the PropertyConsumer properties
     *        into.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + LocationFileProperty, PropUtils.unnull(locationFile));
        props.put(prefix + LocationHeaderProperty,
                new Boolean(locationHeader).toString());
        props.put(prefix + NameIndexProperty,
                (nameIndex != -1 ? Integer.toString(nameIndex) : ""));

        props.put(prefix + ActivityFileProperty, PropUtils.unnull(activityFile));
        props.put(prefix + ActivityHeaderProperty,
                new Boolean(activityHeader).toString());
        props.put(prefix + ActivityNameIndexProperty,
                (activityNameIndex != -1 ? Integer.toString(activityNameIndex)
                        : ""));
        props.put(prefix + EastIsNegProperty, new Boolean(eastIsNeg).toString());
        props.put(prefix + ShowNamesProperty, new Boolean(showNames).toString());
        props.put(prefix + LatIndexProperty,
                (latIndex != -1 ? Integer.toString(latIndex) : ""));
        props.put(prefix + LonIndexProperty,
                (lonIndex != -1 ? Integer.toString(lonIndex) : ""));
        props.put(prefix + TimeIndexProperty,
                (timeIndex != -1 ? Integer.toString(timeIndex) : ""));
        props.put(prefix + IconIndexProperty,
                (iconIndex != -1 ? Integer.toString(iconIndex) : ""));
        props.put(prefix + DefaultIconURLProperty,
                PropUtils.unnull(defaultIconURL));
        drawingAttributes.getProperties(props);
        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting the
     * properties able to be set on this PropertyConsumer. The key for each
     * property should be the raw property name (without a prefix) with a value
     * that is a String that describes what the property key represents, along
     * with any other information about the property that would be helpful
     * (range, default value, etc.). This method takes care of the basic
     * LocationHandler parameters, so any LocationHandlers that extend the
     * AbstractLocationHandler should call this method, too, before adding any
     * specific properties.
     * 
     * @param list a Properties object to load the PropertyConsumer properties
     *        into. If getList equals null, then a new Properties object should
     *        be created.
     * @return Properties object containing PropertyConsumer property values. If
     *         getList was not null, this should equal getList. Otherwise, it
     *         should be the Properties object created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.put(LocationFileProperty,
                "URL of file containing location information.");
        list.put(LocationFileProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        list.put(LocationHeaderProperty,
                "Location file has a header row to be ignored.");
        list.put(LocationHeaderProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(ActivityFileProperty,
                "URL of file containing scenario activity information.");
        list.put(ActivityFileProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        list.put(ActivityHeaderProperty,
                "Activity file has a header row to be ignored.");
        list.put(ActivityHeaderProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(EastIsNegProperty,
                "Flag to note that negative latitude are over the eastern hemisphere.");
        list.put(EastIsNegProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(ShowNamesProperty,
                "Flag to note that locations should display their names.");
        list.put(ShowNamesProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(NameIndexProperty,
                "The column index, in the location file, of the location label text.");

        list.put(ActivityNameIndexProperty,
                "The column index, in the activity file, of the location label text.");

        list.put(LatIndexProperty,
                "The column index, in the activity file, of the latitudes.");
        list.put(LonIndexProperty,
                "The column index, in the activity file, of the longitudes.");
        list.put(TimeIndexProperty,
                "The column index, in the activity file, of the time of the activity.");
        list.put(IconIndexProperty,
                "The column index, in the location file, of the icon for locations (optional).");
        list.put(DefaultIconURLProperty,
                "The URL of an image file to use as a default for the location markers (optional).");

        drawingAttributes.getPropertyInfo(list);

        return list;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Tool Method. The retrieval key for this tool.
     * 
     * @return String The key for this tool.
     */
    public String getKey() {
        return getName();
    }

    /**
     * Tool Method. Set the retrieval key for this tool.
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
     * An OMGraphicList that knows what a ScenarioGraphic is, and knows when to
     * tell it to draw itself at a particular time, or if it should draw its
     * entire scenario path.
     */
    public class ScenarioGraphicList extends OMGraphicList {
        public ScenarioGraphicList() {
            super();
        }

        public void generate(Projection p, long time, boolean showScenario) {
            synchronized (graphics) {
                Iterator it = iterator();
                while (it.hasNext()) {
                    OMGraphic graphic = (OMGraphic) it.next();
                    if (graphic instanceof ScenarioGraphic) {
                        ((ScenarioGraphic) graphic).generate(p,
                                time,
                                showScenario);
                    } else {
                        graphic.generate(p);
                    }
                }
            }
        }
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    // TimeBoundsProvider methods.
    protected List<TimeBoundsHandler> timeBoundsHandlers = new ArrayList<TimeBoundsHandler>();

    public void addTimeBoundsHandler(TimeBoundsHandler tbh) {
        timeBoundsHandlers.add(tbh);
    }

    public TimeBounds getTimeBounds() {
        return timeBounds;
    }

    public void handleTimeBounds(TimeBounds tb) {
    // NoOp, we don't really care what the time bounds are
    }

    public boolean isActive() {
        return active && getReceiver() != null;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

    public void removeTimeBoundsHandler(TimeBoundsHandler tbh) {
        timeBoundsHandlers.remove(tbh);

    }

    public void updateTime(TimeEvent te) {
        time = te.getSimTime();
        manageGraphics();
    }

    public long getTime() {
        return time;
    }

    protected LinkedList<OMEvent> events = new LinkedList<OMEvent>();
    protected LinkedList<OMEvent> filters = new LinkedList<OMEvent>();
    
    public List<OMEvent> getEventList() {
        return getEventList(null);
    }

    public List<OMEvent> getEventList(List filters) {
        return events;
    }

    public Boolean getFilterState(String filterName) {
        return Boolean.TRUE;
    }

    public List getFilters() {
        return filters;
    }

    public List<OMEvent> getMacroFilteredList(Collection eventCollection) {
        return events;
    }

    public void setFilterState(String filterName, Boolean state) {
    }

    public void callForTimeBoundsReset() {
        for (TimeBoundsHandler tbh : timeBoundsHandlers) {
            tbh.resetTimeBounds();
        }
    }
    
}