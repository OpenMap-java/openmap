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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/db/DBLocationHandler.java,v $
// $RCSfile: DBLocationHandler.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:00 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location.db;

/*  Java  */
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import com.bbn.openmap.layer.location.AbstractLocationHandler;
import com.bbn.openmap.layer.location.ByteRasterLocation;
import com.bbn.openmap.layer.location.Location;
import com.bbn.openmap.layer.location.LocationCBMenuItem;
import com.bbn.openmap.layer.location.LocationHandler;
import com.bbn.openmap.layer.location.LocationMenuItem;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.quadtree.QuadTree;

/**
 * The DBLocationLayer is a LocationHandler designed to let you put data on the
 * map based on information from a Database. The properties file lets you set
 * defaults on whether to draw the locations and the names by default. For
 * crowded layers, having all the names displayed might cause a cluttering
 * problem. In gesture mode, OpenMap will display the name of each location as
 * the mouse is passed over it. Pressing the left mouse button over a location
 * brings up a popup menu that lets you show/hide the name label, and also to
 * display attributes of the location in a Browser window that OpenMap launches.
 * 
 * <P>
 * If you want to extend the functionality of this LocationHandler, there are a
 * couple of methods to focus your changes: The setProperties() method lets you
 * add properties to set from the properties file. The createData() method, by
 * default, is a one-time method that creates the graphic objects based on the
 * data. By modifying these methods, and creating a different combination
 * graphic other than the default LocationDataRecordSet, you can create
 * different layer effects pretty easily.
 * 
 * <P>
 * In the openmap.properties file (for instance):
 * 
 * <pre>
 * 
 *  # In the section for the LocationLayer:
 *  locationLayer.locationHandlers=dblocationhandler
 *  
 *  dblocationhandler.class=com.bbn.openmap.layer.location.db.DBLocationHandler
 *  dblocationhandler.locationColor=FF0000
 *  dblocationhandler.nameColor=008C54
 *  dblocationhandler.showNames=false
 *  dblocationhandler.showLocations=true
 *  dblocationhandler.jdbcDriver=oracle.jdbc.driver.OracleDriver
 *  dblocationhandler.jdbcString=jdbc login string
 *  dblocationhandler.userName=username
 *  dblocationhandler.userPassword=password
 *  dblocationhandler.locationQueryString=select statement the data
 *   object needs.  See each Data object (like LocationData) to see what
 *   kind of select statement it needs.
 * 
 * </pre>
 * 
 * In addition, this particular location handler is using the LocationData
 * object to handle the results from the location.
 */
public class DBLocationHandler
        extends AbstractLocationHandler
        implements LocationHandler, ActionListener {

    /** The storage mechanism for the locations. */
    protected QuadTree quadtree = null;

    // Database variables.
    /*
     * This String should be completely specified based on which Database is
     * being used including username and password. Alternately, username and
     * password can be specified by in properties file as jdbc.user=USERNAME
     * jdbc.password=PASSWORD
     */
    /** String that would be used for making a connection to Database */
    protected String jdbcString = null;
    /** Property that should be specified for setting jdbc String */
    public static final String jdbcStringProperty = "jdbcString";
    /**
     * This String would be used to load the driver. If this string is null,
     * jdbc Connection Manager would try to load the appropriate driver.
     */
    protected String jdbcDriver = null;
    /** Property to specify jdbc driver to loaded. */
    public static final String jdbcDriverProperty = "jdbcDriver";
    /** User name to be used for connecting to DB. */
    protected String userName = null;
    /** Password to be used with for connecting to DB. */
    protected String userPassword = null;
    /** Property to specify userName for connecting to Database */
    public static final String userNameProperty = "userName";
    /** Property to specify password for connecting to Database */
    public static final String userPasswordProperty = "userPassword";
    /** Property to specify the query string passed to the database. */
    public static final String locationQueryStringProperty = "locationQueryString";

    /**
     * The string used to query the database for the location information.
     */
    protected String locationQueryString = null;

    /** A copy of properties used to construct this Layer */
    protected Properties props;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public DBLocationHandler() {
    }

    /**
     * The properties and prefix are managed and decoded here, for the standard
     * uses of the DBLocationHandler.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        prefix = getPropertyPrefix();
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        props = properties; // Save it now. We would need it in future

        jdbcString = properties.getProperty(prefix + jdbcStringProperty);
        jdbcDriver = properties.getProperty(prefix + jdbcDriverProperty);
        userName = properties.getProperty(prefix + userNameProperty);
        userPassword = properties.getProperty(prefix + userPasswordProperty);

        locationQueryString = properties.getProperty(prefix + locationQueryStringProperty);
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

        String prefix = getPropertyPrefix();
        if (prefix != null) {
            prefix = prefix + ".";
        } else {
            prefix = "";
        }

        props.put(prefix + "class", this.getClass().getName());
        props.put(prefix + jdbcStringProperty, PropUtils.unnull(jdbcString));
        props.put(prefix + jdbcDriverProperty, PropUtils.unnull(jdbcDriver));
        props.put(prefix + userNameProperty, PropUtils.unnull(userName));
        props.put(prefix + userPasswordProperty, PropUtils.unnull(userPassword));
        props.put(prefix + locationQueryStringProperty, PropUtils.unnull(locationQueryString));

        // Put the properties in here for the RawDataRecordSet, which
        // gets images that can be used for the locations.
        props.put(prefix + RawDataRecordSet.tableNameProperty,
                  PropUtils.unnull(props.getProperty(prefix + RawDataRecordSet.tableNameProperty)));
        props.put(prefix + RawDataRecordSet.rawDataColumnNameProperty,
                  PropUtils.unnull(props.getProperty(prefix + RawDataRecordSet.rawDataColumnNameProperty)));
        props.put(prefix + RawDataRecordSet.rawDataKeyColumnNameProperty,
                  PropUtils.unnull(props.getProperty(prefix + RawDataRecordSet.rawDataKeyColumnNameProperty)));

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
        list.put(jdbcStringProperty, "JDBC login string");
        list.put(jdbcDriverProperty, "JDBC driver class name");
        list.put(userNameProperty, "User name");
        list.put(userPasswordProperty, "User password");
        list.put(locationQueryStringProperty, "Select statement that the data object needs.");

        list.put(RawDataRecordSet.tableNameProperty, "The name of the table in the database that holds the images.");
        list.put(RawDataRecordSet.rawDataColumnNameProperty,
                 "The name of the column in the table in the database that holds the name (key) of the image.");
        list.put(RawDataRecordSet.rawDataKeyColumnNameProperty,
                 "The name of the column in the table in the database that holds the raw image bytes.");

        return list;
    }

    public void reloadData() {
        quadtree = createData();
    }

    /**
     * Look in the database and create the QuadTree holding all the Locations.
     */
    protected QuadTree createData() {

        QuadTree qt = new QuadTree(90.0f, -180.0f, -90.0f, 180.0f, 100, 50f);
        ByteRasterLocation loc;
        byte bytearr[];

        if (locationQueryString == null) {
            return qt;
        }

        // Code for reading from DB and pushing it into QuadTree.
        try {
            if (jdbcDriver != null) {
                Class.forName(getJdbcDriver());
            }

            Connection connection = DriverManager.getConnection(getJdbcString(), getUserName(), getUserPassword());

            RawDataRecordSet gifdataRS = new RawDataRecordSet(connection, getPropertyPrefix(), props);

            RecordSet locationdataRS = new RecordSet(connection, locationQueryString);

            while (locationdataRS.next()) {

                LocationData ld = new LocationData(locationdataRS);

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("DBLocationHandler:  location information:\n" + ld);
                }

                bytearr = gifdataRS.getRawData(ld.getGraphicName());

                float lat = ld.getLatitude();
                float lon = ld.getLongitude();

                loc = new ByteRasterLocation(lat, lon, ld.getCityName(), bytearr);

                loc.setLocationHandler(this);

                // let the layer handler default set these
                // initially...
                loc.setShowName(isShowNames());
                loc.setShowLocation(isShowLocations());

                loc.setLocationPaint(getLocationColor());
                loc.getLabel().setLinePaint(getNameColor());
                loc.setDetails(ld.getCityName() + " is at lat: " + lat + ", lon: " + lon);

                qt.put(lat, lon, loc);

            }

            locationdataRS.close();
            connection.close();

        } catch (SQLException sqlE) {
            logger.warning("DBLocationHandler:SQL Exception: " + sqlE.getMessage());
            sqlE.printStackTrace();
        } catch (ClassNotFoundException cnfE) {
            logger.warning("DBLocationHandler: Class not found Exception: " + cnfE);
        }

        return qt;
    }

    public String getJdbcString() {
        return jdbcString;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public Properties getLocationProperties() {
        return props;
    }

    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method call is made on the location.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this happens,
     * the map will set the cancel bit in the LayerThread, (the thread that is
     * running the prepare). If this Layer needs to do any cleanups during the
     * abort, it should do so, but return out of the prepare asap.
     * 
     */
    public OMGraphicList get(float nwLat, float nwLon, float seLat, float seLon, OMGraphicList graphicList) {

        if (graphicList == null) {
            graphicList = new OMGraphicList();
            graphicList.setTraverseMode(OMGraphicList.FIRST_ADDED_ON_TOP);
        }

        // IF the quadtree has not been set up yet, do it!
        if (quadtree == null) {
            logger.fine("DBLocationHandler: Figuring out the locations and names! (This is a one-time operation!)");
            quadtree = createData();
        }

        if (quadtree != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("DBLocationHandler|DBLocationHandler.get() ul.lon = " + nwLon + " lr.lon = " + seLon + " delta = "
                        + (seLon - nwLon));
            }

            Vector vec = new Vector<OMGraphic>();
            quadtree.get(nwLat, nwLon, seLat, seLon, vec);

            graphicList.addAll(vec);
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

    /** Box used for constructing the palette widgets */
    protected Box box = null;

    /**
     * Provides the palette widgets to control the options of showing maps, or
     * attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public Component getGUI() {
        if (box == null) {
            JCheckBox showDBLocationCheck, showNameCheck;
            JButton rereadFilesButton;

            showDBLocationCheck = new JCheckBox("Show Locations", isShowLocations());
            showDBLocationCheck.setActionCommand(showLocationsCommand);
            showDBLocationCheck.addActionListener(this);

            showNameCheck = new JCheckBox("Show Location Names", isShowNames());
            showNameCheck.setActionCommand(showNamesCommand);
            showNameCheck.addActionListener(this);

            rereadFilesButton = new JButton("Reload Data From Source");
            rereadFilesButton.setActionCommand(readDataCommand);
            rereadFilesButton.addActionListener(this);

            box = Box.createVerticalBox();
            box.add(showDBLocationCheck);
            box.add(showNameCheck);
            box.add(rereadFilesButton);
        }
        return box;
    }

    // ----------------------------------------------------------------------
    // ActionListener interface implementation
    // ----------------------------------------------------------------------

    /**
     * The Action Listener method, that reacts to the palette widgets actions.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == showLocationsCommand) {
            JCheckBox locationCheck = (JCheckBox) e.getSource();
            setShowLocations(locationCheck.isSelected());
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("DBLocationHandler::actionPerformed showLocations is " + isShowLocations());
            }
            getLayer().repaint();
        } else if (cmd == showNamesCommand) {
            JCheckBox namesCheck = (JCheckBox) e.getSource();
            setShowNames(namesCheck.isSelected());
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("DBLocationHandler::actionPerformed showNames is " + isShowNames());
            }
            getLayer().repaint();
        } else if (cmd == readDataCommand) {
            logger.fine("DBLocationHandler: Re-reading Locations file");
            quadtree = null;
            getLayer().doPrepare();
        } else {
            logger.warning("DBLocationHandler: Unknown action command \"" + cmd + "\" in actionPerformed().");
        }
    }

}