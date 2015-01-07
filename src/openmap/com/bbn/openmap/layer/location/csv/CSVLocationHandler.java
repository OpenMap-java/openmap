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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/csv/CSVLocationHandler.java,v $
// $RCSfile: CSVLocationHandler.java,v $
// $Revision: 1.12 $
// $Date: 2005/08/09 18:17:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location.csv;

/*  Java  */
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import com.bbn.openmap.layer.location.AbstractLocationHandler;
import com.bbn.openmap.layer.location.Location;
import com.bbn.openmap.layer.location.LocationCBMenuItem;
import com.bbn.openmap.layer.location.LocationHandler;
import com.bbn.openmap.layer.location.LocationLayer;
import com.bbn.openmap.layer.location.LocationMenuItem;
import com.bbn.openmap.layer.location.URLRasterLocation;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.CSVTokenizer;
import com.bbn.openmap.util.DataOrganizer;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.quadtree.QuadTree;

/**
 * The CSVLocationLayer is a LocationHandler designed to let you put data on the
 * map based on information from a Comma Separated Value(CSV) file. It's assumed
 * that the each row in the file refers to a certain location, and that location
 * contains a name label, a latitude and a longitude (both in decimal degrees).
 * 
 * <P>
 * The individual fields must not have leading whitespace.
 * 
 * <P>
 * The CSVLocationLayer gives you some basic functionality. The properties file
 * lets you set defaults on whether to draw the locations and the names by
 * default. For crowded layers, having all the names displayed might cause a
 * cluttering problem. In gesture mode, OpenMap will display the name of each
 * location as the mouse is passed over it. Pressing the left mouse button over
 * a location brings up a popup menu that lets you show/hide the name label, and
 * also to display the entire row contents of the location CSV file in a Browser
 * window that OpenMap launches.
 * 
 * <P>
 * If you want to extend the functionality of this LocationHandler, there are a
 * couple of methods to focus your changes: The setProperties() method lets you
 * add properties to set from the properties file. The createData() method, by
 * default, is a one-time method that creates the graphic objects based on the
 * CSV data. By modifying these methods, and creating a different combination
 * graphic other than the CSVLocation, you can create different layer effects
 * pretty easily.
 * 
 * <P>
 * The locationFile property should contain a URL referring to the file. This
 * can take the form of file:/myfile.csv for a local file or
 * http://somehost.org/myfile.csv for a remote file.
 * 
 * <P>
 * In the openmap.properties file (for instance): <BR>
 * 
 * <pre>
 * 
 * 
 * 
 *       # In the section for the LocationLayer:
 *       locationLayer.locationHandlers=csvlocationhandler
 *       
 *       csvlocationhandler.class=com.bbn.openmap.layer.location.csv.CSVLocationHandler
 *       csvlocationhandler.locationFile=/data/worldpts/WorldLocs_point.csv
 *       csvlocationhandler.csvFileHasHeader=true
 *       csvlocationhandler.showNames=false
 *       csvlocationhandler.showLocations=true
 *       csvlocationhandler.nameIndex=0
 *       csvlocationhandler.latIndex=8
 *       csvlocationhandler.lonIndex=10
 *       # Optional property, if you have a column in the file for URLs of
 *       # images to use for an icon.
 *       csvlocationhandler.iconIndex=11
 *       # Optional property, URL of image to use as marker for all entries in
 *       # csv file without a URL listed at the iconIndex.
 *       csvlocationhandler.defaultIconURL=/data/symbols/default.gif
 *       # Optional property, if the eastern hemisphere longitudes are negative.  False by default.
 *       csvlocationhandler.eastIsNeg=false
 *       
 *       # CSVLocationHandler has been updated to have regular DrawingAttribute properties for both name and location.
 *      csvlocationhandler.name.lineColor=FF008C54
 *      csvlocationhandler.location.lineColor=FFFF0000
 *      csvlocationhandler.location.fillColor=FFaaaaaa
 *      csvlocationhandler.location.pointRadius=3
 *      csvlocationhandler.location.pointOval=true
 *      
 *      # optional, can be used if you override createLocation and need access to varying rendering attributes.
 *      # ra1, ra2 and ra3 would be used as keys in renderAttributes map.  All GraphicAttributes properties are available, not
 *      # just lineColor.
 *     
 *      csvlocationhandler.renderAttributesList=ra1 ra2 ra3
 *      csvlocationhandler.ra1.lineColor=0xFFFF0000
 *      csvlocationhandler.ra2.lineColor=0xFF00FF00
 *      csvlocationhandler.ra3.lineColor=0xFF00FFFF
 * 
 * </pre>
 */
public class CSVLocationHandler extends AbstractLocationHandler implements LocationHandler {

    /** The path to the primary CSV file holding the locations. */
    protected String locationFile;
    /** The property describing the locations of location data. */
    public static final String LocationFileProperty = "locationFile";
    /** Set if the CSVFile has a header record. Default is false. */
    public final static String csvHeaderProperty = "csvFileHasHeader";
    /** The storage mechanism for the locations. */
    protected QuadTree<Location> quadtree = null;

    /** The property describing whether East is a negative value. */
    public static final String eastIsNegProperty = "eastIsNeg";
    /** Are east values really negative with this file? */
    protected boolean eastIsNeg = false;
    /**
     * Flag that specifies that the first line consists of header information,
     * and should not be mapped to a graphic.
     */
    protected boolean csvHasHeader = false;

    // /////////////////////
    // Name label variables

    /** Index of column in CSV to use as name of location. */
    protected int nameIndex = -1;
    /**
     * Property to use to designate the column of the CSV file to use as a name.
     */
    public static final String NameIndexProperty = "nameIndex";

    // //////////////////////
    // Location Variables

    /**
     * Property to use to designate the column of the CSV file to use as the
     * latitude.
     */
    public static final String LatIndexProperty = "latIndex";
    /**
     * Property to use to designate the column of the CSV file to use as the
     * longitude.
     */
    public static final String LonIndexProperty = "lonIndex";
    /**
     * Property to use to designate the column of the CSV file to use as an icon
     * URL
     */
    public static final String IconIndexProperty = "iconIndex";
    /**
     * Property to set an URL for an icon image to use for all the locations
     * that don't have an image defined in the csv file, or if there isn't an
     * icon defined in the csv file for any of the locations and you want them
     * all to have the same icon.
     */
    public static final String DefaultIconURLProperty = "defaultIconURL";

    /** Index of column in CSV to use as latitude of location. */
    protected int latIndex = -1;
    /** Index of column in CSV to use as longitude of location. */
    protected int lonIndex = -1;
    /** Index of column in CSV to use as URL of the icon. */
    protected int iconIndex = -1;

    protected String defaultIconURL = null;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public CSVLocationHandler() {
    }

    /**
     * The properties and prefix are managed and decoded here, for the standard
     * uses of the CSVLocationHandler.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        locationFile = properties.getProperty(prefix + LocationFileProperty);

        latIndex = PropUtils.intFromProperties(properties, prefix + LatIndexProperty, -1);
        lonIndex = PropUtils.intFromProperties(properties, prefix + LonIndexProperty, -1);
        iconIndex = PropUtils.intFromProperties(properties, prefix + IconIndexProperty, -1);
        nameIndex = PropUtils.intFromProperties(properties, prefix + NameIndexProperty, -1);
        eastIsNeg = PropUtils.booleanFromProperties(properties, prefix + eastIsNegProperty, false);
        defaultIconURL = properties.getProperty(prefix + DefaultIconURLProperty);
        if (defaultIconURL != null && defaultIconURL.trim().length() == 0) {
            // If it's empty, it should be null, otherwise it causes
            // confusion later when the empty string can't be
            // interpreted as a valid URL to an image file.
            defaultIconURL = null;
        }

        csvHasHeader = PropUtils.booleanFromProperties(properties, prefix + csvHeaderProperty, false);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLocationHandler indexes:\n  latIndex = " + latIndex + "\n  lonIndex = "
                    + lonIndex + "\n  nameIndex = " + nameIndex + "\n  has header = "
                    + csvHasHeader);
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

        props.put(prefix + "class", this.getClass().getName());
        props.put(prefix + LocationFileProperty, PropUtils.unnull(locationFile));

        props.put(prefix + eastIsNegProperty, new Boolean(eastIsNeg).toString());
        props.put(prefix + csvHeaderProperty, new Boolean(csvHasHeader).toString());
        props.put(prefix + NameIndexProperty, (nameIndex != -1 ? Integer.toString(nameIndex) : ""));
        props.put(prefix + LatIndexProperty, (latIndex != -1 ? Integer.toString(latIndex) : ""));
        props.put(prefix + LonIndexProperty, (lonIndex != -1 ? Integer.toString(lonIndex) : ""));
        props.put(prefix + IconIndexProperty, (iconIndex != -1 ? Integer.toString(iconIndex) : ""));
        props.put(prefix + DefaultIconURLProperty, PropUtils.unnull(defaultIconURL));

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

        list.put("class" + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.NonEditablePropertyEditor");
        list.put(LocationFileProperty, "URL of file containing location information.");
        list.put(LocationFileProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");
        list.put(eastIsNegProperty, "Flag to note that negative latitude are over the eastern hemisphere.");
        list.put(eastIsNegProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        list.put(NameIndexProperty, "The column index, in the location file, of the location label text.");
        list.put(LatIndexProperty, "The column index, in the location file, of the latitudes.");
        list.put(LonIndexProperty, "The column index, in the location file, of the longitudes.");
        list.put(IconIndexProperty, "The column index, in the location file, of the icon for locations (optional).");
        list.put(DefaultIconURLProperty, "The URL of an image file to use as a default for the location markers (optional).");
        list.put(csvHeaderProperty, "Flag to note that the first line in the csv file is a header line and should be ignored.");

        return list;
    }

    public void reloadData() {
        quadtree = createData();
    }

    protected boolean checkIndexSettings() {
        if (latIndex == -1 || lonIndex == -1) {
            logger.warning("CSVLocationHandler: createData(): Index properties for Lat/Lon/Name are not set properly! lat index:"
                    + latIndex + ", lon index:" + lonIndex);
            return false;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLocationHandler: Reading File:" + locationFile + " NameIndex: "
                    + nameIndex + " latIndex: " + latIndex + " lonIndex: " + lonIndex
                    + " iconIndex: " + iconIndex + " eastIsNeg: " + eastIsNeg);
        }

        return true;
    }

    /**
     * Look at the CSV file and create the QuadTree holding all the Locations.
     */
    protected QuadTree<Location> createData() {

        QuadTree<Location> qt = new QuadTree<Location>(90.0f, -180.0f, -90.0f, 180.0f, 100, 50f);

        if (!checkIndexSettings()) {
            return null;
        }

        int lineCount = 0;
        Object token = null;
        // TokenDecoder tokenHandler = getTokenDecoder();

        // readHeader should be set to true if the first line has
        // been read, or if the csvHasHeader is false.
        boolean readHeader = !csvHasHeader;

        try {

            // This lets the property be specified as a file name
            // even if it's not specified as file:/<name> in
            // the properties file.

            URL csvURL = PropUtils.getResourceOrFileOrURL(null, locationFile);
            if (csvURL != null) {

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(csvURL.openStream()));
                CSVTokenizer csvt = new CSVTokenizer(streamReader);

                token = csvt.token();

                List recordList = Collections.synchronizedList(new ArrayList(10));

                while (!csvt.isEOF(token)) {
                    int i = 0;

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("CSVLocationHandler| Starting a line | have"
                                + (readHeader ? " " : "n't ") + "read header");
                    }

                    // Prepare it for the new row/record
                    recordList.clear();

                    while (!csvt.isNewline(token) && !csvt.isEOF(token)) {

                        if (readHeader) {
                            // tokenHandler.handleToken(token, i);
                            recordList.add(token);
                        }

                        token = csvt.token();
                        // For some reason, the check above doesn't always
                        // work
                        if (csvt.isEOF(token)) {
                            break;
                        }
                        i++;
                    }

                    if (!readHeader) {
                        readHeader = true;
                    } else {
                        lineCount++;
                        createLocation(recordList, qt);
                        // tokenHandler.createAndAddObjectFromTokens(qt);
                    }
                    token = csvt.token();
                }

                csvt.close();
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("couldn't figure out file: " + locationFile);
                }
            }
        } catch (java.io.IOException ioe) {
            throw new com.bbn.openmap.util.HandleError(ioe);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            throw new com.bbn.openmap.util.HandleError(aioobe);
        } catch (NumberFormatException nfe) {
            throw new com.bbn.openmap.util.HandleError(nfe);
        } catch (ClassCastException cce) {
            logger.warning("Problem reading entries in " + locationFile
                    + ", check your index settings, first column = 0.");
            throw new com.bbn.openmap.util.HandleError(cce);
        } catch (NullPointerException npe) {
            logger.warning("Problem reading location file, check " + locationFile);
            throw new com.bbn.openmap.util.HandleError(npe);
        } catch (java.security.AccessControlException ace) {
            throw new com.bbn.openmap.util.HandleError(ace);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLocationHandler | Finished File:" + locationFile + ", read "
                    + lineCount + " locations");
        }

        if (lineCount == 0 && readHeader) {
            logger.fine("CSVLocationHandler has read file, but didn't find any data.\n  Check file for a header line, and make sure that the\n  properties (csvFileHasHeader) is set properly for this CSVLocationHandler. Trying again without header...");
            csvHasHeader = !csvHasHeader;
            return createData();
        }

        return qt;
    }

    /**
     * This is the method called by create data with a row's worth of
     * information stuffed in the record List. The indexes set in the properties
     * should describe what each entry is.
     * 
     * @param recordList a record/row of data from the csv file.
     * @param qt the Quadtree to add the Location object, created from the row
     *        contents.
     */
    protected void createLocation(List recordList, QuadTree<Location> qt) {

        String name = tokenToString(recordList, nameIndex, "");
        double lat = tokenToDouble(recordList, latIndex, 0.0);
        double lon = tokenToDouble(recordList, lonIndex, 0.0, eastIsNeg);
        String iconURL = tokenToString(recordList, iconIndex, defaultIconURL);

        qt.put(lat, lon, createLocation(lat, lon, name, iconURL, recordList));
    }

    /**
     * When a new Location object needs to be created from data read in the CSV
     * file, this method is called. This method lets you extend the
     * CSVLocationLayer and easily set what kind of Location objects to use
     * based on file contents. The lat/lon/name/icon path have already been
     * decoded from the record List.
     * 
     * @param lat latitude of location, decimal degrees.
     * @param lon longitude of location, decimal degrees.
     * @param name the label of the location.
     * @param iconURL the String for a URL for an icon. Can be null.
     * @param recordList the original List of Objects in case other entries in
     *        the row should affect how the Location object is configured.
     * @return Location object for lat/lon/name/iconURL.
     */
    protected Location createLocation(double lat, double lon, String name, String iconURL,
                                      List recordList) {

        // This will turn into a regular location if iconURL is null.
        Location loc = new URLRasterLocation(lat, lon, name, iconURL);

        // let the layer handler default set these initially...
        loc.setShowName(isShowNames());
        loc.setShowLocation(isShowLocations());

        loc.setLocationHandler(this);
        getLocationDrawingAttributes().setTo(loc.getLocationMarker());
        getNameDrawingAttributes().setTo(loc.getLabel());

        loc.setDetails(name + " is at lat: " + lat + ", lon: " + lon);

        if (iconURL != null) {
            loc.setDetails(loc.getDetails() + " icon: " + iconURL);
        }

        logger.fine("CSVLocationHandler " + loc.getDetails());

        return loc;
    }

    /**
     * Scope object to String. If anything goes wrong the default is returned.
     * 
     * @param recordList the List for the record.
     * @param index the index of the object to fetch.
     * @param def default value
     * @return String value
     */
    protected String tokenToString(List recordList, int index, String def) {
        try {
            Object obj = recordList.get(index);
            if (obj != null) {
                return obj.toString();
            }
        } catch (Exception e) {
            // just return default
        }
        return def;
    }

    /**
     * Scope object to double if it's a number, or return default. If anything
     * goes wrong the default is returned.
     * 
     * @param recordList the List for the record.
     * @param index the index of the object to fetch.
     * @param def default value
     * @return double value
     */
    protected double tokenToDouble(List recordList, int index, double def) {
        try {
            Object obj = recordList.get(index);
            if (obj instanceof Double) {
                return ((Double) obj).doubleValue();
            }
        } catch (Exception e) {

        }

        return def;
    }

    /**
     * Scope object to double if it's a number, or return default. Swap the sign
     * if needed, if east is supposed to be a negative number. If anything goes
     * wrong the default is returned.
     * 
     * @param recordList the List for the record.
     * @param index the index of the object to fetch.
     * @param def default value
     * @param swapSign multiply value by -1, say, if you know that the file has
     *        negative values for eastern hemisphere longitudes (hey, I've seen
     *        it).
     * @return double value
     */
    protected double tokenToDouble(List recordList, int index, double def, boolean swapSign) {
        Double ret = tokenToDouble(recordList, index, def);
        return swapSign ? -1 * ret : ret;
    }

    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method calls to build the OMGraphicList to draw.
     */
    public OMGraphicList get(double nwLat, double nwLon, double seLat, double seLon,
                             OMGraphicList graphicList) {

        if (graphicList == null) {
            graphicList = new OMGraphicList();
            graphicList.setTraverseMode(OMGraphicList.FIRST_ADDED_ON_TOP);
        }

        // IF the quadtree has not been set up yet, do it!
        if (quadtree == null) {
            logger.fine("CSVLocationHandler: Figuring out the locations and names! (This is a one-time operation!)");
            quadtree = createData();
        }

        if (quadtree != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("CSVLocationHandler|CSVLocationHandler.get() ul.lon = " + nwLon
                        + " lr.lon = " + seLon + " delta = " + (seLon - nwLon));
            }

            List<Location> hits = new ArrayList<Location>();
            quadtree.get(nwLat, nwLon, seLat, seLon, hits);

            graphicList.addAll(hits);
        }

        return graphicList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.layer.location.LocationHandler#getItemsForPopupMenu(com
     * .bbn.openmap.layer.location.Location)
     */
    public List<Component> getItemsForPopupMenu(Location loc) {
        List<Component> menuItems = new ArrayList<Component>();
        menuItems.add(new LocationCBMenuItem(LocationHandler.showname, loc));
        menuItems.add(new LocationMenuItem(showdetails, loc));
        return menuItems;
    }

    protected Box box = null;

    /**
     * Provides the palette widgets to control the options of showing maps, or
     * attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public Component getGUI() {
        if (box == null) {
            JCheckBox showLocationCheck, showNameCheck, forceGlobalCheck;
            JButton rereadFilesButton;

            showLocationCheck = new JCheckBox("Show Locations", isShowLocations());
            showLocationCheck.setActionCommand(showLocationsCommand);
            showLocationCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox locationCheck = (JCheckBox) ae.getSource();
                    setShowLocations(locationCheck.isSelected());
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("CSVLocationHandler::actionPerformed showLocations is "
                                + isShowLocations());
                    }
                    getLayer().repaint();
                }
            });
            showLocationCheck.setToolTipText("<HTML><BODY>Show location markers on the map.</BODY></HTML>");

            showNameCheck = new JCheckBox("Show Location Names", isShowNames());
            showNameCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox namesCheck = (JCheckBox) ae.getSource();
                    setShowNames(namesCheck.isSelected());
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("CSVLocationHandler::actionPerformed showNames is "
                                + isShowNames());
                    }

                    LocationLayer ll = getLayer();
                    if (namesCheck.isSelected() && ll.getDeclutterMatrix() != null
                            && ll.getUseDeclutterMatrix()) {
                        ll.doPrepare();
                    } else {
                        ll.repaint();
                    }
                }
            });
            showNameCheck.setToolTipText("<HTML><BODY>Show location names on the map.</BODY></HTML>");

            forceGlobalCheck = new JCheckBox("Override Location Settings", isForceGlobal());
            forceGlobalCheck.setActionCommand(forceGlobalCommand);
            forceGlobalCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox forceGlobalCheck = (JCheckBox) ae.getSource();
                    setForceGlobal(forceGlobalCheck.isSelected());
                    getLayer().repaint();
                }
            });
            forceGlobalCheck.setToolTipText("<HTML><BODY>Make these settings override those set<BR>on the individual map objects.</BODY></HTML>");

            rereadFilesButton = new JButton("Reload Data From Source");
            rereadFilesButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Re-reading Locations file");
                    }
                    quadtree = null;
                    getLayer().doPrepare();
                }
            });
            rereadFilesButton.setToolTipText("<HTML><BODY>Reload the data file, and put these settings<br>on the individual map objects.</BODY></HTML>");

            box = Box.createVerticalBox();
            box.add(showLocationCheck);
            box.add(showNameCheck);
            box.add(forceGlobalCheck);
            box.add(rereadFilesButton);
        }
        return box;
    }

}
