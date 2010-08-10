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

package com.bbn.openmap.layer.event;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.io.CSVFile;
import com.bbn.openmap.layer.location.LocationHandler;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.time.TemporalOMGraphic;
import com.bbn.openmap.omGraphics.time.TemporalOMGraphicList;
import com.bbn.openmap.omGraphics.time.TemporalOMPoint;
import com.bbn.openmap.omGraphics.time.TemporalOMScalingIcon;
import com.bbn.openmap.omGraphics.time.TemporalPoint;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.time.TimeBounds;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.PropUtils;

/**
 * A data importer for the EventLayer. The location file should contain
 * information about objects that will be moving on the map. The activity file
 * will contain information about where and when the objects moved. Sample
 * properties:
 * 
 * <pre>
 *    eventLayer.class=com.bbn.openmap.layer.time.EventLayer
 *    eventLayer.importer=com.bbn.openmap.layer.time.CSVEventImporter
 *    eventLayer.prettyName=Test Event
 *    eventLayer.locationFile=org-list.csv
 *    eventLayer.locationFileHasHeader=true
 *    eventLayer.nameIndex=0
 *    eventLayer.iconIndex=5
 *    eventLayer.activityFile=org-activities.csv
 *    eventLayer.activityFileHasHeader=true
 *    eventLayer.activityNameIndex=1
 *    eventLayer.latIndex=9
 *    eventLayer.lonIndex=10
 *    eventLayer.timeFormat=d-MMM-yyyy HH:mm
 *    eventLayer.timeIndex=7
 *    # If no icon defined, used for location markers edge.
 *    eventLayer.lineColor=aaaaaa33
 *    # If no icon defined, used for location markers fill.
 *    eventLayer.fillColor=aaaaaa33
 * </pre>
 * 
 * @author dietrick
 */
public class CSVEventImporter extends OMComponent implements EventImporter {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.event.CSVEventImporter");

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
    /**
     * TimeFormat default is similar to IETF standard date syntax:
     * "Sat, 12 Aug 1995 13:30:00 GMT" represented by (EEE, d MMM yyyy HH:mm:ss
     * z), except for the local timezone.
     */
    protected SimpleDateFormat timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

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

    /** Icon URL for points to use as default. May be null. */
    protected String defaultIconURL;
    protected boolean showNames = false;
    protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

    public CSVEventImporter() {

    }

    /**
     * Read the data files and construct the TemporalOMGraphics. You also need
     * to create TimeBounds, keep track of the time stamps from the data source,
     * and set the new TimeBounds on the EventLayer before returning from this
     * method. If you want to set the DataBounds on the layer, in order for the
     * view menu to have a selection for the area of interest, fetch the
     * DataBounds object and set it accordingly while you are in this method.
     * <p>
     * Read the data files and construct the TemporalOMGraphics. There are four
     * things you need to do in this method.
     * <ul>
     * <li>Create an TemporalOMGraphicList, add TemporalOMGraphics, return it.
     * <li>Set a new TimeBounds object on the callback EventLayer when all the
     * timestamp range is known.
     * <li>Add OMEvents to the callback.events list, one for each TemporalPoint
     * created.
     * <li>Add locations to callback's DataBounds (callback.getDataBounds()).
     * </ul>
     */
    public synchronized TemporalOMGraphicList createData(EventLayer callback) {
        TemporalOMGraphicList list = new TemporalOMGraphicList();
        Hashtable<String, TemporalOMGraphic> library = new Hashtable<String, TemporalOMGraphic>();
        Hashtable<String, ImageIcon> iconLibrary = new Hashtable<String, ImageIcon>();

        // BOTH IMPORTANT
        DataBounds dataBounds = callback.getDataBounds();
        TimeBounds timeBounds = new TimeBounds();

        // Create TemporalOMGraphics, to associate events to
        if (locationFile != null && nameIndex != -1) {
            logger.fine("Reading location file...");
            try {
                CSVFile locations = new CSVFile(locationFile);
                locations.loadData();
                Iterator<Vector<Object>> records = locations.iterator();
                while (records.hasNext()) {
                    String name = null;
                    String iconName = null;
                    ImageIcon icon = null;
                    Vector<?> record = records.next();

                    if (record.isEmpty())
                        continue;

                    name = (String) record.elementAt(nameIndex);

                    if (iconIndex != -1) {
                        iconName = (String) record.elementAt(iconIndex);

                        icon = iconLibrary.get(iconName);

                        if (icon == null) {
                            URL icURL = PropUtils.getResourceOrFileOrURL(iconName);
                            if (icURL != null) {
                                icon = new ImageIcon(icURL);
                                if (icon != null) {
                                    iconLibrary.put(iconName, icon);
                                }
                            }
                        }
                    }

                    if (name != null) {
                        TemporalOMGraphic location;
                        if (icon == null) {
                            location = new TemporalOMPoint(name, OMGraphic.RENDERTYPE_LATLON, true);
                        } else {
                            location = new TemporalOMScalingIcon(name, OMGraphic.RENDERTYPE_LATLON, true, icon, 4000000);
                        }
                        // location.setShowName(showNames);
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
                logger.warning("null pointer exception, most likely a problem finding the organization data file");
            }
        } else {
            logger.warning("Location file (" + locationFile
                    + ") not configured.");
            return list;
        }

        // OK, got the TemporalOMGraphics built up, need to fill up the
        // events
        // 
        if (activityFile != null && activityNameIndex != -1 && latIndex != -1
                && lonIndex != -1 && timeIndex != -1) {
            logger.fine("Reading activity file...");

            try {
                CSVFile activities = new CSVFile(activityFile);
                activities.loadData(); // numbers as strings == false
                Iterator<Vector<Object>> records = activities.iterator();
                while (records.hasNext()) {
                    String name = null;
                    float lat;
                    float lon;

                    Vector<?> record = records.next();

                    if (record.isEmpty())
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

                        // BOTH IMPORTANT
                        timeBounds.addTimeToBounds(time);
                        dataBounds.add((double) lon, (double) lat);

                        if (name != null) {
                            TemporalOMGraphic point = library.get(name);
                            if (point != null) {
                                LatLonPoint location = new LatLonPoint.Double(lat, lon);
                                TemporalPoint ts = new TemporalPoint(location, time);
                                point.addTimeStamp(ts);

                                OMEvent event = new OMEvent(ts, name
                                        + " moving", time, location);

                                // IMPORTANT
                                callback.events.add(event);

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

                        logger.warning("Problem with indexes in activity file for "
                                + obj0
                                + " ("
                                + obj0.getClass().getName()
                                + ")"
                                + ":\n\tlat index = "
                                + latIndex
                                + ", value = "
                                + obj1
                                + " ("
                                + obj1.getClass().getName()
                                + ")\n\t lon index = "
                                + lonIndex
                                + ", value = "
                                + obj2
                                + " ("
                                + obj2.getClass().getName()
                                + ")\n\t time index = "
                                + timeIndex
                                + ", value = "
                                + obj3
                                + " ("
                                + obj3.getClass().getName() + ")");
                    } catch (ParseException pe) {
                        logger.fine("Problem with time format. "
                                + pe.getMessage());
                    }
                }
            } catch (MalformedURLException murle) {
                logger.warning("problem with activity file: " + activityFile);
                return list;
            } catch (NullPointerException npe) {
                logger.warning("null pointer exception, most likely a problem finding the activites data file");
            }
        } else {
            logger.warning("Activity file (" + activityFile
                    + ") not configured.");
            return list;
        }

        logger.fine("Reading files OK");

        // IMPORTANT!
        callback.setTimeBounds(timeBounds);

        // Time will get updated automatically when the TimeEvent gets sent from
        // the clock.
        return list;
    }

    /**
     * The properties and prefix are managed and decoded here, for the standard
     * uses of the EventLayer.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

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
            logger.fine("EventLayer indexes:" + "\n\tlocation file: "
                    + locationFile + "\n\tlocation file has header: "
                    + locationHeader + "\n\tnameIndex = " + nameIndex
                    + "\n\ticonIndex = " + iconIndex + "\n\tactivity file: "
                    + activityFile + "\n\tactivity file has header: "
                    + activityHeader + "\n\tlatIndex = " + latIndex
                    + "\n\tlonIndex = " + lonIndex + "\n\ttimeIndex = "
                    + timeIndex);
        }
    }

    /**
     * PropertyConsumer method, to fill in a Properties object, reflecting the
     * current values of the layer. If the layer has a propertyPrefix set, the
     * property keys should have that prefix plus a separating '.' prepended to
     * each property key it uses for configuration.
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

        return list;
    }

}
