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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/csv/CSVLinkHandler.java,v $
// $RCSfile: CSVLinkHandler.java,v $
// $Revision: 1.2 $
// $Date: 2003/09/22 23:47:35 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.location.csv;


/*  Java Core  */
import java.awt.Point;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import java.net.URL;
import javax.swing.*;

/*  OpenMap  */
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;
import com.bbn.openmap.util.CSVTokenizer;
import com.bbn.openmap.util.quadtree.QuadTree;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.layer.location.*;
import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.Box;
import javax.swing.SwingUtilities;

/**  
 * The CSVLinkHandler is designed to let you put data on the map based
 * on information from a Comma Separated Value(CSV) file.  It's
 * assumed that the each row in the file refers to two locations, and
 * that a link is to be shown between the two locations.
 *
 * <P>The individual fields must not have leading whitespace.
 *
 * <P>The locationFile property should contain a URL referring to the file.
 * This can take the form of file:/myfile.csv for a local file or
 * http://somehost.org/myfile.csv for a remote file.
 *
 * If there is a lat1/lon1 index, and a lat2/lon2 index, then the links'
 * endpoints are in the link file.
 * <P>
 *
 * The Link CSV file has to have certain fields, and the column number
 * of those fields are set in the properties: 
 * <pre>
 * # latitude and longitude indexes of the link end points
 * linkMarkerName.lat1Index=column_number
 * linkMarkerName.lon1Index=column_number
 * linkMarkerName.lat2Index=column_number
 * linkMarkerName.lon2Index=column_number
 * # These are optional
 * linkMarkerName.dashIndex=column_number for true/false (false is default)
 * linkMarkerName.colorIndex=column_number for color notation
 * linkMarkerName.thicknessIndex=column_number for pixel thickness of link
 * linkMarkerName.geoStyleIndex=column_number for link rendertype (STRAIGHT, GC, RHUMB)
 * </pre>
 */
public class CSVLinkHandler extends CSVLocationHandler {

    ////////////////////////
    // Link Variables

    /** Property to use to designate the column of the link file to use
     * as the latitude of end "1". */
    public static final String Lat1IndexProperty = "lat1Index";
    /** Property to use to designate the column of the link file to use
     * as the longitude of end "1". */
    public static final String Lon1IndexProperty = "lon1Index";
    /** Property to use to designate the column of the link file to use
     * as the latitude of end "2". */
    public static final String Lat2IndexProperty = "lat2Index";
    /** Property to use to designate the column of the link file to use
     * as the longitude of end "2". */
    public static final String Lon2IndexProperty = "lon2Index";
    /** Not used. */
    public static final String LinkTypeIndexProperty = "linkTypeIndex";
    /** Index in file for True/false property to indicate link should
     *  be dashed line. */
    public static final String DashIndexProperty = "dashIndex";
    public static final String ColorIndexProperty = "colorIndex";
    public static final String ThicknessIndexProperty = "thicknessIndex";
    /**
     * Index in CSV file for rendertype of link - STRAIGHT, GC, RHUMB
     */
    public static final String GeoStyleIndexProperty = "geoStyleIndex";

    /** The names of the various link types on the map. Not used. */
    public static final String LinkTypesProperty = "linkTypes";
    
    /** Index of column in CSV to use as latitude1 of link. */
    protected int lat1Index = -1;
    /** Index of column in CSV to use as longitude1 of link. */
    protected int lon1Index = -1;
    /** Index of column in CSV to use as latitude2 of link. */
    protected int lat2Index = -1;
    /** Index of column in CSV to use as longitude2 of link. */
    protected int lon2Index = -1;
    /** Index of column in CSV to use as the type of link. */
    protected int linkTypeIndex = -1;
    /** Index of column in CSV to use as the line-type to draw a link in. */
    protected int geoStyleIndex = -1;
    
    protected int dashIndex = -1;
    protected int colorIndex = -1;
    protected int thicknessIndex = -1;

    /** 
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     */
    public CSVLinkHandler() {}

    /** 
     * The properties and prefix are managed and decoded here, for
     * the standard uses of the CSVLinkHandler.
     *
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.  
     */
    public void setProperties(String prefix,
			      java.util.Properties properties) {
	super.setProperties(prefix, properties);

	String realPrefix = PropUtils.getScopedPropertyPrefix(this);

	String lat1IndexString =
	    properties.getProperty(realPrefix + Lat1IndexProperty);
	String lon1IndexString =
	    properties.getProperty(realPrefix + Lon1IndexProperty);
	String lat2IndexString =
	    properties.getProperty(realPrefix + Lat2IndexProperty);
	String lon2IndexString =
	    properties.getProperty(realPrefix + Lon2IndexProperty);
	
	// This will replace the three properties below it. 
	// Note - I can't remember how - DFD
	String linkTypeIndexString =
	    properties.getProperty(realPrefix + LinkTypeIndexProperty);

	String dashIndexString =
	    properties.getProperty(realPrefix + DashIndexProperty);
	String colorIndexString =
	    properties.getProperty(realPrefix + ColorIndexProperty);
	String thicknessIndexString =
	    properties.getProperty(realPrefix + ThicknessIndexProperty);
	String geoStyleIndexString = 
	    properties.getProperty(realPrefix + GeoStyleIndexProperty);
	
	lat1Index     = -1;
	lon1Index     = -1;
	lat2Index     = -1;
	lon2Index     = -1;
	colorIndex    = -1;
	dashIndex	= -1;
	thicknessIndex	= -1;
	geoStyleIndex	= -1;

	// Check to make sure we have enough indexes specified.
	if (lat1IndexString != null && lon1IndexString != null) {
	    try {
		lat1Index = Integer.valueOf(lat1IndexString).intValue();
		lon1Index = Integer.valueOf(lon1IndexString).intValue();
	    }
	    catch (NumberFormatException e) {
		Debug.error
		    ("CSVLinkHandler: Unable to parse node1Index string.");
	    }
	} else {
	    Debug.error
		("CSVNodeLayer: no Index-1 specified as lat/lon or node");
	}
	
	if (lat2IndexString != null && lon2IndexString != null) {
	    try {
		lat2Index = Integer.valueOf(lat2IndexString).intValue();
		lon2Index = Integer.valueOf(lon2IndexString).intValue();
	    }
	    catch (NumberFormatException e) {
		Debug.error
		    ("CSVLinkHandler: Unable to parse node2Index string.");
	    }
	} else {
	    Debug.error
		("CSVNodeLayer: no Index-2 specified as lat/lon or node");
	}
	
	if (linkTypeIndexString != null) {
	    try {
		linkTypeIndex = Integer.valueOf(linkTypeIndexString).intValue();
	    }
	    catch (NumberFormatException e) {
		Debug.error
		    ("CSVLinkHandler: Unable to parse linkTypeIndex string.");
	    }
	} else {
	    Debug.error ("CSVNodeLayer: no linkTypeIndex specified");
	}   

	if (geoStyleIndexString != null) {
	    try {
		geoStyleIndex = Integer.valueOf(geoStyleIndexString).intValue();
	    }
	    catch (NumberFormatException e) {
		Debug.error
		    ("CSVLinkHandler: Unable to parse geoStyleIndex string.");
	    }
	} else {
	    Debug.error ("CSVNodeLayer: no geoStyleIndex specified");
	}   

	// These will be removed too... 
	if (dashIndexString != null) {
	    try {
		dashIndex = Integer.valueOf(dashIndexString).intValue();
	    }
	    catch (NumberFormatException e) {
		Debug.error
		    ("CSVLinkHandler: Unable to parse dashIndex string.");
	    }
	} else {
	    Debug.error ("CSVNodeLayer: no dashIndex specified");
	}   

	if (colorIndexString != null) {
	    try {
		colorIndex = Integer.valueOf(colorIndexString).intValue();
	    }
	    catch (NumberFormatException e) {
		Debug.error
		    ("CSVLinkHandler: Unable to parse colorIndex string.");
	    }
	} else {
	    Debug.error ("CSVNodeLayer: no colorIndex specified");
	}  
	
	if (thicknessIndexString != null) {
	    try {
		thicknessIndex = Integer.valueOf(thicknessIndexString).intValue();
	    }
	    catch (NumberFormatException e) {
		Debug.error
		    ("CSVLinkHandler: Unable to parse thicknessIndex string.");
	    }
	} else {
	    Debug.error ("CSVNodeLayer: no thicknessIndex specified");
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

	props.put(prefix + Lat1IndexProperty, Integer.toString(lat1Index));
	props.put(prefix + Lon1IndexProperty, Integer.toString(lon1Index));
	props.put(prefix + Lat2IndexProperty, Integer.toString(lat2Index));
	props.put(prefix + Lon2IndexProperty, Integer.toString(lon2Index));

//  	props.put(prefix + LinkTypeIndexProperty, Integer.toString(linkTypeIndex));

	props.put(prefix + DashIndexProperty, Integer.toString(dashIndex));
	props.put(prefix + ColorIndexProperty, Integer.toString(colorIndex));
	props.put(prefix + ThicknessIndexProperty, Integer.toString(thicknessIndex));
	props.put(prefix + GeoStyleIndexProperty, Integer.toString(geoStyleIndex));

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

    /**
     * This is called by the CSVLinkWorker to load the CSV file
     */
    protected QuadTree createData() {
	
	QuadTree qt = new QuadTree(90.0f, -180.0f, -90.0f, 180.0f, 100, 50f);

	try {
	    Object token = null;

	    // This lets the property be specified as a file name
	    // even if it's not specified as file:/<name> in
	    // the properties file.
	    URL csvURL = new URL(new URL("file:"), locationFile); 
	    CSVTokenizer csvt =
	      new CSVTokenizer(new BufferedReader
			       (new InputStreamReader
				(csvURL.openStream())));

	    float lat1 = 0;
	    float lon1 = 0;
	    float lat2 = 0;
	    float lon2 = 0;
	    String linkType = "";
	    int linetype = OMGraphic.LINETYPE_STRAIGHT;
	    Color color = Color.black;
	    boolean dashed = false;
	    float thickness = 0;

	    Link link = null;
	    
	    token = csvt.token();
	    
	    if (Debug.debugging("link")) {
		Debug.output("CSVLinkHandler: Reading File:" + locationFile
			     + " lat1Index: " + lat1Index
			     + " lon1Index: " + lon1Index
			     + " lat2Index: " + lat2Index
			     + " lon2Index: " + lon2Index
			     + " geoStyleIndex: " + geoStyleIndex
			     // + " linkTypeIndex: " + linkTypeIndex
			     + " dashIndex: " + dashIndex
			     + " colorIndex: " + colorIndex
			     + " thicknessIndex: " + thicknessIndex);
	    }
	    
	    
	    while (!csvt.isEOF(token)) {
		int i = 0;
		
		Debug.message("link", "CSVLinkHandler: Starting a line");
		
		while (!csvt.isNewline(token)) {
		    
		    try {
			if (i == lat1Index)
			    lat1 = ((Double)token).floatValue();
			else if (i == lon1Index)
			    lon1 = ((Double)token).floatValue();
			else if (i == lat2Index)
			    lat2 = ((Double)token).floatValue();
			else if (i == lon2Index)
			    lon2 = ((Double)token).floatValue();
			else if (i == geoStyleIndex) 
			    linetype = getLineTypeFromToken(token);
			
			// These are going to go away... 
			else if (i == colorIndex) 
			    color  = getColorFromToken(token);
			else if (i == thicknessIndex)
			    thickness = ((Double)token).floatValue();
			else if (i == dashIndex)
			    dashed = Boolean.valueOf((String)token).booleanValue();
			
		    } catch (Exception e) {
			e.printStackTrace(); 
		    }

		    token = csvt.token();
		    i++;
		}
		
		
		link = new Link(lat1, lon1, lat2, lon2, "no details",
				color, dashed, thickness, linetype);
		link.setLocationHandler(this);
 		Debug.message("link", "CSVLinkHandler: " + link.getDetails());
		
 		qt.put(lat1, lon1, link);
 		qt.put(lat2, lon2, link);
		token = csvt.token();
	    }
	} catch (java.io.IOException ioe) {
	    throw new com.bbn.openmap.util.HandleError(ioe);
	} catch (ArrayIndexOutOfBoundsException aioobe) {
	    throw new com.bbn.openmap.util.HandleError(aioobe);
	} catch (NumberFormatException nfe) {
	    throw new com.bbn.openmap.util.HandleError(nfe);
	} catch (ClassCastException cce) {
	    throw new com.bbn.openmap.util.HandleError(cce);
	}
	Debug.message("link", "CSVLinkHandler: Finished File:" + locationFile);
	return qt;
    }

    /** 
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * <P>
     * In this case, the palette widget only contains one button, 
     * which reloads the data files for the layer. 
     * <p>
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
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

    //----------------------------------------------------------------------
    // ActionListener interface implementation
    //----------------------------------------------------------------------

    /** 
     * The Action Listener method, that reacts to the palette widgets
     * actions.
     */
    public void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	if (cmd == showLocationsCommand) {		
	    JCheckBox linkCheck = (JCheckBox)e.getSource();
	    setShowLocations(linkCheck.isSelected());
	    if(Debug.debugging("location")) {
	    	Debug.output("CSVLinkHandler::actionPerformed showLocations is " + isShowLocations());
	    }
	    getLayer().repaint();
	}else if (cmd == readDataCommand) {
	    Debug.output("Re-reading links file");
	    quadtree = null;
	    getLayer().doPrepare();
	} else {
	    Debug.error("Unknown action command \"" + cmd +
			       "\" in CSVLinkLayer.actionPerformed().");
	}
    }

    /* Utility functions */

    /** 
     * This gets a line-type from a token, and translates it into one of 
     * LINETYPE_STRAIGHT, LINETYPE_GREATCIRCLE, or LINETYPE_RHUMB.
     * @param token the token read from teh CSV file. 
     * @return one of LINETYPE_STRAIGHT, LINETYPE_GREATCIRCLE, or LINETYPE_RHUMB
     */

    protected int getLineTypeFromToken(Object token) {
	int default_lintetype = OMGraphic.LINETYPE_STRAIGHT;
	String tokstring = (String)token;

	if (Debug.debugging("link")) {
	    Debug.output("CSVLinkHandler:getLineTypeFromToken(" + 
			       tokstring + ")");
	}
	
	if (tokstring.equals("STRAIGHT"))
	    return OMGraphic.LINETYPE_STRAIGHT;
	else if (tokstring.equals("GC"))
	    return OMGraphic.LINETYPE_GREATCIRCLE;
	else if (tokstring.equals("RHUMB"))
	    return OMGraphic.LINETYPE_RHUMB;
	else {
	    Debug.error("Don't understand Linetype " 
			       + tokstring + ", using default (STRAIGHT)");
	    return default_lintetype;
	}
    }
    


    /**  
     * This interprets a color value from a token. The color can be
     * one of the standard colors in the java.awt.Color class, or it
     * can be a hexadecimal representation of any other displayable
     * color.
     * <p>
     * @param token the token read from the CSV file. 
     * <p>
     * @return the java.awt.Color described by that token, or Color.black 
     * (if the token cannot be translated into a proper color).
     */

    protected Color getColorFromToken(Object token) {
	String tokstring = (String)token;
	
	Color result = Color.black;
	
	if (Debug.debugging("link")) {
	    Debug.output("CSVLinkHandler: getColorFromToken(" + 
			       tokstring + ")");
	}

	// Thank the heavens for Emacs macros!
	if (tokstring.equals("black"))
	    result =  Color.black;
	else if (tokstring.equals("blue"))
	    result =  Color.blue;
	else if (tokstring.equals("cyan"))
	    result =  Color.cyan;
	else if (tokstring.equals("darkGray"))
	    result =  Color.darkGray;
	else if (tokstring.equals("gray"))
	    result =  Color.gray;
	else if (tokstring.equals("green"))
	    result =  Color.green;
	else if (tokstring.equals("lightGray"))
	    result =  Color.lightGray;
	else if (tokstring.equals("magenta"))
	    result =  Color.magenta;
	else if (tokstring.equals("orange"))
	    result =  Color.orange;
	else if (tokstring.equals("pink"))
	    result =  Color.pink;
	else if (tokstring.equals("red"))
	    result =  Color.red;
	else if (tokstring.equals("white"))
	    result =  Color.white;
	else if (tokstring.equals("yellow"))
	    result =  Color.yellow;
	else
	    // decode a hex color string.
	    result = Color.decode(tokstring);
	
	if (Debug.debugging("link")) {
	    Debug.output("CSVLinkHandler: getColorFromToken returns (" + 
			       result + ")");
	}

	return result;
    }
  
}
