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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import com.bbn.openmap.layer.location.Link;
import com.bbn.openmap.layer.location.Location;
import com.bbn.openmap.omGraphics.OMArrowHead;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.quadtree.QuadTree;
import java.awt.BasicStroke;

/**
 * The CSVLinkHandler is designed to let you put data on the map based on
 * information from a Comma Separated Value(CSV) file. It's assumed that the
 * each row in the file refers to two locations, and that a link is to be shown
 * between the two locations. It has the same basic properties as the
 * CSVLocationHandler, with allowances for extra lat/lon per record to handle
 * the other side of the link/line.
 * 
 * <P>
 * The individual fields must not have leading whitespace.
 * 
 * <P>
 * The locationFile property should contain a URL referring to the file. This
 * can take the form of file:/myfile.csv for a local file or
 * http://somehost.org/myfile.csv for a remote file
 * 
 * If there is a lat/lon index, and a lat2/lon2 index, then the links' endpoints
 * are in the link file.
 * <P>
 * 
 * The Link CSV file has to have certain fields, and the column number of those
 * fields are set in the properties:
 * 
 * <pre>
 * 
 *          # latitude and longitude indexes of the link end points.  The first two are the same as CSVLocationHandler.
 *          linkMarkerName.latIndex=column_number
 *          linkMarkerName.lonIndex=column_number
 *          linkMarkerName.lat2Index=column_number
 *          linkMarkerName.lon2Index=column_number
 *          linkMarkerName.lineWidthIndex=column_number
 *          linkMarkerName.lineColorIndex=column_number
 *          # Line type. One of GREATCIRCLE, STRAIGHT, or RHUMB
 *          linkMarkerName.lineType=GREATCIRCLE
 *          # Arrows on links. One of NONE, FORWARD, BACKWARD, BOTH
 *          linkMarkerName.arrowDirection=NONE
 *          # Number from 0 to 100, indicating percentage along line to show arrow
 *          linkMarkerName.arrowLocation=0
 *          # Arrow dimensions. See documentation for OMArrowHead
 *          linkMarkerName.arrowWingLength=20
 *          linkMarkerName.arrowWingTip=5
 *          
 *          #plus all the other optional properties. The marker parameters will work on the link OMGraphics.
 * 
 * </pre>
 * 
 * <p>
 * TODO: update the quadtree used to instead use a
 * com.bbn.openmap.geo.ExtentIndex, so that lines that bisect the map will
 * appear. Right now, one of the endpoints of the line has to be in the map
 * window in order for the link to be displayed, and that's not quite right.
 */
public class CSVLinkHandler extends CSVLocationHandler {

    // //////////////////////
    // Link Variables

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
    /**
     * Property to use to designate the width of the line, in floating-point-pixels
     */
    public static final String LineWidthIndexProperty = "lineWidthIndex";
    /**
     * Property to use to designate the width of the line, in floating-point-pixels
     */
    public static final String LineColorIndexProperty = "lineColorIndex";
    /**
     * Property to use to designate the line type
     */
    public static final String LineTypeProperty = "lineType";
    
    /** Property to use for Arrow direction. One of NONE, HEAD, TAIL, BOTH indicating arrow direction */
    public static final String ArrowDirectionProperty = "arrowDirection";
    /** Property to use for percentage along line to show arrow, 0..100 */
    public static final String ArrowLocationProperty = "arrowLocation";
    /** Property to use for arrow winglength */
    public static final String ArrowWingLengthProperty = "arrowWingLength";
    /** Property to use for arrow wingtip */
    public static final String ArrowWingTipProperty = "arrowWingTip";
         
    /** The names of the various link types on the map. Not used. */
    /** Index of column in CSV to use as latitude2 of link. */
    protected int lat2Index = -1;
    /** Index of column in CSV to use as longitude2 of link. */
    protected int lon2Index = -1;
    /** Index of column in CSV to use at Line Width of Link. */
    protected int lineWidthIndex = -1;
    /** Index of column in CSV to use at Color for the  Link. */
    protected int lineColorIndex = -1;
    /** Line type */
    protected int lineType = -1;
    protected String lineTypeStr = "STRAIGHT";

    protected String arrowDirection = "NONE";
    protected int arrowLocation = 100;
    protected int arrowWingLength = 20;
    protected int arrowWingTip = 5;
    
    /** The actual Arrow Head */
    OMArrowHead arrowHead = null;
    
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

        lat2Index = PropUtils.intFromProperties(properties, realPrefix + Lat2IndexProperty, lat2Index);
        lon2Index = PropUtils.intFromProperties(properties, realPrefix + Lon2IndexProperty, lon2Index);
        lineWidthIndex = PropUtils.intFromProperties(properties, realPrefix + LineWidthIndexProperty, lineWidthIndex);
        lineColorIndex = PropUtils.intFromProperties(properties, realPrefix + LineColorIndexProperty, lineColorIndex);
        lineTypeStr = properties.getProperty(realPrefix + LineTypeProperty, lineTypeStr);
        if(lineTypeStr.equals("GREATCIRCLE")) {
            lineType = OMLine.LINETYPE_GREATCIRCLE;
        } else if(lineTypeStr.equals("STRAIGHT")) {
            lineType = OMLine.LINETYPE_STRAIGHT;
        } else if(lineTypeStr.equals("RHUMB")) {
            lineType = OMLine.LINETYPE_RHUMB;
        } else {
            logger.warning("Unknown Line Type " + lineTypeStr + ", making STRAIGHT");
            lineType = OMLine.LINETYPE_STRAIGHT;
            lineTypeStr = "STRAIGHT";
        }
        
        int arrowDirectionType = -1;
        arrowDirection = properties.getProperty(realPrefix + ArrowDirectionProperty, arrowDirection);
        if(!arrowDirection.equals("NONE")) {
            arrowLocation = PropUtils.intFromProperties(properties, realPrefix + ArrowLocationProperty, arrowLocation);
            arrowWingLength = PropUtils.intFromProperties(properties, realPrefix + ArrowWingLengthProperty, arrowWingLength);
            arrowWingTip = PropUtils.intFromProperties(properties, realPrefix + ArrowWingTipProperty, arrowWingTip);
            if(arrowDirection.equals("BOTH")) {
                arrowDirectionType = OMArrowHead.ARROWHEAD_DIRECTION_BOTH;
            } else if(arrowDirection.equals("FORWARD")) {
                arrowDirectionType = OMArrowHead.ARROWHEAD_DIRECTION_FORWARD;
            } else if(arrowDirection.equals("BACKWARD")) {
                arrowDirectionType = OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD;
            } else {
                logger.warning("Unknown arrow direction " + arrowDirection + ", making NONE");
            }
            
            if(arrowDirectionType > -1) {
                arrowHead = new OMArrowHead(arrowDirectionType, arrowLocation, arrowWingTip, arrowWingLength);
            }
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

        props.put(prefix + Lat2IndexProperty, (lat2Index != -1 ? Integer.toString(lat2Index) : ""));
        props.put(prefix + Lon2IndexProperty, (lon2Index != -1 ? Integer.toString(lon2Index) : ""));
        props.put(prefix + LineWidthIndexProperty, (lineWidthIndex != -1 ? Integer.toString(lineWidthIndex) : ""));
        props.put(prefix + LineColorIndexProperty, (lineColorIndex != -1 ? Integer.toString(lineColorIndex) : ""));
        props.put(prefix + LineTypeProperty, lineTypeStr);
        props.put(prefix + ArrowLocationProperty, arrowLocation);
        props.put(prefix + ArrowDirectionProperty, arrowDirection);
        props.put(prefix + ArrowWingLengthProperty, arrowWingLength);
        props.put(prefix + ArrowWingTipProperty, arrowWingTip);

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

        list.put(LatIndexProperty, "The column index, in the location file, of the first node latitude.");
        list.put(LonIndexProperty, "The column index, in the location file, of the first node longitude.");
        list.put(Lat2IndexProperty, "The column index, in the location file, of the second node latitude.");
        list.put(Lon2IndexProperty, "The column index, in the location file, of the second node longitude.");
        list.put(LineWidthIndexProperty, "The column index, in the location file, of the width of the line.");
        list.put(LineColorIndexProperty, "The column index, in the location file, of the color of the line.");
        list.put(LineTypeProperty, "Line Type. One of GREATCIRCLE, STRAIGHT, RHUMB.");
        list.put(ArrowDirectionProperty, "Arrow Direction. One of NONE, BOTH, FORWARD, BACKWARD.");
        list.put(ArrowLocationProperty, "Arrow Location. Percent along line, from 0..100");
        list.put(ArrowWingLengthProperty, "Arrow Wing Length. See OMArrowHead documentation");
        list.put(ArrowWingTipProperty, "Arrow Wing Tip. See OMArrowHead documentation");

        return list;
    }

    protected boolean checkIndexSettings() {

        if (latIndex == -1 || lonIndex == -1 || lat2Index == -1 || lon2Index == -1) {
            logger.warning("CSVLocationHandler: createData(): Index properties for Lat/Lon/Name are not set properly! lat index:"
                    + latIndex + ", lon index:" + lonIndex);
            return false;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLinkHandler: Reading File:" + locationFile + " lat1Index: " + latIndex
                    + " lon1Index: " + lonIndex + " lat2Index: " + lat2Index + " lon2Index: "
                    + lon2Index);
        }

        return true;
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
        showCSVLinkCheck.addActionListener(new ActionListener() {
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

        rereadFilesButton = new JButton("Re-Read Data File");
        rereadFilesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Re-reading Locations file");
                }
                quadtree = null;
                getLayer().doPrepare();
            }
        });
        Box box = Box.createVerticalBox();
        box.add(showCSVLinkCheck);
        box.add(rereadFilesButton);
        return box;
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
        float lineWidth = 1.0f;
        if(lineWidthIndex != -1) {
            lineWidth = (float) tokenToDouble(recordList, lineWidthIndex, lineWidth);
        }
        final BasicStroke stroke = new BasicStroke(lineWidth);
        
        Color lineColor = Color.BLACK;
        if(lineColorIndex != -1) {
            String testCol = tokenToString(recordList, lineColorIndex, lineColor.toString());
            if(testCol.contains(".")) {
                // Sometimes these things get temporarily translated to doubles somewhere
                testCol = testCol.split("\\.")[0];
            }
            if(!(testCol.startsWith("0x") || testCol.startsWith("#"))) {
                testCol = "0x" + testCol;
            }
            try {
                lineColor = Color.decode(testCol);
            } catch(NumberFormatException ex) {
                logger.warning("Cannot decode color " + testCol);
            }
        }
        String iconURL = tokenToString(recordList, iconIndex, defaultIconURL);

        double lat2 = tokenToDouble(recordList, lat2Index, 0.0);
        double lon2 = tokenToDouble(recordList, lon2Index, 0.0, eastIsNeg);

        Link link = new Link(lat, lon, lat2, lon2, "No details");
        getLocationDrawingAttributes().setTo(link);

        link.setLocationHandler(CSVLinkHandler.this);

        // What we really want to do is get the
        // locationDrawingAttributes and set them on the link.

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CSVLinkHandler: " + link.getDetails());
        }

        qt.put(lat, lon, link);
        qt.put(lat2, lon2, link);

        qt.put(lat, lon, createLocation(lat, lon, name, iconURL, recordList));
        
        final OMGraphic line = link.getLocationMarker();
        
        line.setStroke(stroke);
        line.setLineType(lineType);
        line.setLinePaint(lineColor);
        if(null != arrowHead && line instanceof OMLine) {
            final OMLine l = (OMLine)line;
            l.addArrowHead(arrowHead.getArrowDirectionType(),
                    arrowHead.getLocation(),
                    arrowHead.getWingTip(),
                    arrowHead.getWingLength());
        }
    }
}
