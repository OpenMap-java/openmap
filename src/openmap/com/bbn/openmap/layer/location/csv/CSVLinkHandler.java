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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/csv/CSVLinkHandler.java,v $
// $RCSfile: CSVLinkHandler.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 18:17:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location.csv;

/*  Java Core  */
import java.awt.Color;
import java.awt.Component;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import com.bbn.openmap.layer.location.Link;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.DataOrganizer;
import com.bbn.openmap.util.PropUtils;

/**
 * The CSVLinkHandler is designed to let you put data on the map based on
 * information from a Comma Separated Value(CSV) file. It's assumed that the
 * each row in the file refers to two locations, and that a link is to be shown
 * between the two locations.
 * 
 * <P>
 * The individual fields must not have leading whitespace.
 * 
 * <P>
 * The locationFile property should contain a URL referring to the file. This
 * can take the form of file:/myfile.csv for a local file or
 * http://somehost.org/myfile.csv for a remote file
 * 
 * If there is a lat1/lon1 index, and a lat2/lon2 index, then the links'
 * endpoints are in the link file.
 * <P>
 * 
 * The Link CSV file has to have certain fields, and the column number of those
 * fields are set in the properties:
 * 
 * <pre>
 * 
 *          # latitude and longitude indexes of the link end points
 *          linkMarkerName.lat1Index=column_number
 *          linkMarkerName.lon1Index=column_number
 *          linkMarkerName.lat2Index=column_number
 *          linkMarkerName.lon2Index=column_number
 *          # These are optional
 *          linkMarkerName.dashIndex=column_number for true/false (false is default)
 *          linkMarkerName.colorIndex=column_number for color notation
 *          linkMarkerName.thicknessIndex=column_number for pixel thickness of link
 *          linkMarkerName.geoStyleIndex=column_number for link rendertype (STRAIGHT, GC, RHUMB)
 * 
 * </pre>
 * 
 * <p>
 * TODO: update the quadtree used to instead use a
 * com.bbn.openmap.geo.ExtentIndex, so that lines that bisect the map will
 * appear. Right now, one of the endpoints of the line has to be in the map
 * window in order for the link to be displayed, and that's not quite right.
 */
public class CSVLinkHandler
        extends CSVLocationHandler {

    // //////////////////////
    // Link Variables

    /**
     * Property to use to designate the column of the link file to use as the
     * latitude of end "1".
     */
    public static final String Lat1IndexProperty = "lat1Index";
    /**
     * Property to use to designate the column of the link file to use as the
     * longitude of end "1".
     */
    public static final String Lon1IndexProperty = "lon1Index";
    /**
     * Property to use to designate the column of the link file to use as the
     * latitude of end "2".
     */
    public static final String Lat2IndexProperty = "lat2Index";
    /**
     * Property to use to designate the column of the link file to use as the
     * longitude of end "2".
     */
    public static final String Lon2IndexProperty = "lon2Index";
    /** Not used. */
    public static final String LinkTypeIndexProperty = "linkTypeIndex";
    /**
     * Index in file for True/false property to indicate link should be dashed
     * line.
     */
    public static final String DashIndexProperty = "dashIndex";
    public static final String ColorIndexProperty = "colorIndex";
    public static final String ThicknessIndexProperty = "thicknessIndex";
    /**
     * Index in CSV file for rendertype of link - STRAIGHT, GC, RHUMB
     */
    public static final String GeoStyleIndexProperty = "geoStyleIndex";

    /** The names of the various link types on the map. Not used. */
    // public static final String LinkTypesProperty = "linkTypes";
    /** Index of column in CSV to use as latitude1 of link. */
    protected int lat1Index = -1;
    /** Index of column in CSV to use as longitude1 of link. */
    protected int lon1Index = -1;
    /** Index of column in CSV to use as latitude2 of link. */
    protected int lat2Index = -1;
    /** Index of column in CSV to use as longitude2 of link. */
    protected int lon2Index = -1;
    /** Index of column in CSV to use as the type of link. */
    // protected int linkTypeIndex = -1;
    /**
     * Index of column in CSV to use as the line-type to draw a link in.
     */
    protected int geoStyleIndex = -1;

    protected int dashIndex = -1;
    protected int colorIndex = -1;
    protected int thicknessIndex = -1;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public CSVLinkHandler() {
    }

    /**
     * The properties and prefix are managed and decoded here, for the standard
     * uses of the CSVLinkHandler.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);

        String realPrefix = PropUtils.getScopedPropertyPrefix(this);

        lat1Index = PropUtils.intFromProperties(properties, realPrefix + Lat1IndexProperty, lat1Index);
        lon1Index = PropUtils.intFromProperties(properties, realPrefix + Lon1IndexProperty, lon1Index);
        lat2Index = PropUtils.intFromProperties(properties, realPrefix + Lat2IndexProperty, lat2Index);
        lon2Index = PropUtils.intFromProperties(properties, realPrefix + Lon2IndexProperty, lon2Index);
        geoStyleIndex = PropUtils.intFromProperties(properties, realPrefix + GeoStyleIndexProperty, geoStyleIndex);

        // linkTypeIndex = PropUtils.intFromProperties(properties,
        // realPrefix
        // + LinkTypeIndexProperty, linkTypeIndex);

        // This will replace the three properties below it.
        // Note - I can't remember how - DFD - Oh, yeah, it's not
        // implemented.

        colorIndex = PropUtils.intFromProperties(properties, realPrefix + ColorIndexProperty, colorIndex);
        dashIndex = PropUtils.intFromProperties(properties, realPrefix + DashIndexProperty, dashIndex);
        thicknessIndex = PropUtils.intFromProperties(properties, realPrefix + ThicknessIndexProperty, thicknessIndex);
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
        // Start JDJ Changes
        props.put(prefix + "class", this.getClass().getName());
        props.put(prefix + LocationFileProperty, PropUtils.unnull(locationFile));
        props.put(prefix + csvHeaderProperty, new Boolean(csvHasHeader).toString());
        // End JDJ Changes
        props.put(prefix + Lat1IndexProperty, Integer.toString(lat1Index));
        props.put(prefix + Lon1IndexProperty, Integer.toString(lon1Index));
        props.put(prefix + Lat2IndexProperty, Integer.toString(lat2Index));
        props.put(prefix + Lon2IndexProperty, Integer.toString(lon2Index));

        // props.put(prefix + LinkTypeIndexProperty,
        // Integer.toString(linkTypeIndex));

        props.put(prefix + DashIndexProperty, Integer.toString(dashIndex));
        props.put(prefix + ColorIndexProperty, Integer.toString(colorIndex));
        props.put(prefix + ThicknessIndexProperty, Integer.toString(thicknessIndex));
        props.put(prefix + GeoStyleIndexProperty, Integer.toString(geoStyleIndex));

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

        list.remove(LatIndexProperty);
        list.remove(LonIndexProperty);

        list.put(Lat1IndexProperty, "The column index, in the location file, of the first node latitude.");
        list.put(Lon1IndexProperty, "The column index, in the location file, of the first node longitude.");
        list.put(Lat2IndexProperty, "The column index, in the location file, of the second node latitude.");
        list.put(Lon2IndexProperty, "The column index, in the location file, of the second node longitude.");

        list.put(DashIndexProperty, "The column index, in the location file, of the true/false dash indicator.");
        list.put(ColorIndexProperty, "The column index, in the location file, of the color string.");
        list.put(ThicknessIndexProperty, "The column index, in the location file, of the pixel thickness of the link.");
        list.put(GeoStyleIndexProperty, "The column index, in the location file, of the render type of the link.");
        return list;
    }

    protected boolean checkIndexSettings() {

        if (lat1Index == -1 || lon1Index == -1 || lat2Index == -1 || lon2Index == -1) {
            logger.warning("CSVLocationHandler: createData(): Index properties for Lat/Lon/Name are not set properly! lat index:"
                    + latIndex + ", lon index:" + lonIndex);
            return false;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLinkHandler: Reading File:" + locationFile + " lat1Index: " + lat1Index + " lon1Index: " + lon1Index
                    + " lat2Index: " + lat2Index + " lon2Index: " + lon2Index + " geoStyleIndex: " + geoStyleIndex
                    // + " linkTypeIndex: " + linkTypeIndex
                    + " dashIndex: " + dashIndex + " colorIndex: " + colorIndex + " thicknessIndex: " + thicknessIndex);
        }

        return true;
    }

    protected TokenDecoder getTokenDecoder() {
        return new LinkDecoder();
    }

    /**
     * Provides the palette widgets to control the options of showing maps, or
     * attribute text.
     * <P>
     * In this case, the palette widget only contains one button, which reloads
     * the data files for the layer.
     * <p>
     * 
     * @return Component object representing the palette widgets.
     */
    public Component getGUI() {
        JButton rereadFilesButton;
        JCheckBox showCSVLinkCheck;

        showCSVLinkCheck = new JCheckBox("Show Links", isShowLocations());
        showCSVLinkCheck.setActionCommand(showLocationsCommand);
        showCSVLinkCheck.addActionListener(this);

        rereadFilesButton = new JButton("Re-Read Data File");
        rereadFilesButton.setActionCommand(readDataCommand);
        rereadFilesButton.addActionListener(this);
        Box box = Box.createVerticalBox();
        box.add(showCSVLinkCheck);
        box.add(rereadFilesButton);
        return box;
    }

    /* Utility functions */

    /**
     * This gets a line-type from a token, and translates it into one of
     * LINETYPE_STRAIGHT, LINETYPE_GREATCIRCLE, or LINETYPE_RHUMB.
     * 
     * @param token the token read from the CSV file.
     * @return one of LINETYPE_STRAIGHT, LINETYPE_GREATCIRCLE, or LINETYPE_RHUMB
     */
    protected int getLineTypeFromToken(Object token) {
        int default_lintetype = OMGraphic.LINETYPE_STRAIGHT;
        String tokstring = ((String) token).trim().toLowerCase();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLinkHandler:getLineTypeFromToken(" + tokstring + ")");
        }

        if (tokstring.startsWith("s"))
            return OMGraphic.LINETYPE_STRAIGHT;
        else if (tokstring.startsWith("g"))
            return OMGraphic.LINETYPE_GREATCIRCLE;
        else if (tokstring.startsWith("r"))
            return OMGraphic.LINETYPE_RHUMB;
        else {
            logger.warning("Don't understand Linetype " + tokstring + ", using default (STRAIGHT)");
            return default_lintetype;
        }
    }

    /**
     * This interprets a color value from a token. The color can be one of the
     * standard colors in the java.awt.Color class, or it can be a hexadecimal
     * representation of any other displayable color.
     * <p>
     * 
     * @param token the token read from the CSV file.
     *        <p>
     * @return the java.awt.Color described by that token, or Color.black (if
     *         the token cannot be translated into a proper color).
     */
    protected Color getColorFromToken(Object token) {
        String tokstring = (String) token;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLinkHandler: getColorFromToken(" + tokstring + ")");
        }

        Color c = ColorFactory.getNamedColor(tokstring, null);

        if (c == null) {
            // decode a hex color string.
            c = Color.decode(tokstring);
            if (c == null) {
                c = Color.BLACK;
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLinkHandler: getColorFromToken returns (" + c + ")");
        }

        return c;
    }

    public class LinkDecoder
            implements TokenDecoder {

        float lat1;
        float lon1;
        float lat2;
        float lon2;
        int linetype;
        Color color;
        boolean dashed;
        float thickness;

        public LinkDecoder() {
            reset();
        }

        public void reset() {
            lat1 = 0;
            lon1 = 0;
            lat2 = 0;
            lon2 = 0;
            linetype = OMGraphic.LINETYPE_GREATCIRCLE;
            color = (Color) getLocationDrawingAttributes().getLinePaint();
            dashed = false;
            thickness = 1f;
        }

        public void handleToken(Object token, int i) {
            try {
                if (i == lat1Index)
                    lat1 = ((Double) token).floatValue();
                else if (i == lon1Index)
                    lon1 = ((Double) token).floatValue();
                else if (i == lat2Index)
                    lat2 = ((Double) token).floatValue();
                else if (i == lon2Index)
                    lon2 = ((Double) token).floatValue();
                else if (i == geoStyleIndex)
                    linetype = getLineTypeFromToken(token);

                // These are going to go away...
                else if (i == colorIndex)
                    color = getColorFromToken(token);
                else if (i == thicknessIndex)
                    thickness = ((Double) token).floatValue();
                else if (i == dashIndex)
                    dashed = Boolean.valueOf((String) token).booleanValue();
            } catch (NumberFormatException nfe) {

            }
        }

        public void createAndAddObjectFromTokens(DataOrganizer organizer) {
            // Original Lines added to this else block to fix
            // reading a data file with a header in it.
            Link link = new Link(lat1, lon1, lat2, lon2, "No details", color, dashed, thickness, linetype);
            link.setLocationHandler(CSVLinkHandler.this);

            // What we really want to do is get the
            // locationDrawingAttributes and set them on the link.

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("CSVLinkHandler: " + link.getDetails());
            }

            organizer.put(lat1, lon1, link);
            organizer.put(lat2, lon2, link);
            reset();
        }
    }
}
