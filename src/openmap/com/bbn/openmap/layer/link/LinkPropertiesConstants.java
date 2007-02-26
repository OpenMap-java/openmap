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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkPropertiesConstants.java,v $
// $RCSfile: LinkPropertiesConstants.java,v $
// $Revision: 1.4 $
// $Date: 2007/02/26 17:12:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

/**
 * A LinkPropertiesConstants interface defines the well known expected
 * properties that may be coming over the link inside the properties. Other
 * properties may be carried as well, but these are properties you can kind of
 * expect to see.
 */
public interface LinkPropertiesConstants {

    // Constants that contain graphic attributes
    /**
     * The property, transmitted first, that dictates how LinkProperty memory is
     * managed when a LinkProperties object is read. The value for this key
     * dictates the policy.
     */
    public final static String LPC_PROPERY_MANAGEMENT_POLICY = "p";
    public final static char LPC_PROPERY_MANAGEMENT_POLICY_CHAR = 'p';
    /** The line color attribute name. */
    public final static String LPC_LINECOLOR = "lc";
    /** The line style (dash attribute) attribute name. */
    public final static String LPC_LINESTYLE = "ls";
    /** The highlight color attribute name. */
    public final static String LPC_HIGHLIGHTCOLOR = "hc";
    /** The fill color attribute name. */
    public final static String LPC_FILLCOLOR = "fc";
    /** The fill pattern attribute name. */
    public final static String LPC_FILLPATTERN = "fp";
    /** The line width attribute name. */
    public final static String LPC_LINEWIDTH = "lw";
    /** The text graphic contents attribute name. */
    public final static String LPC_LINKTEXTSTRING = "ts";
    /** The font representation attribute name. */
    public final static String LPC_LINKTEXTFONT = "tf";
    
    /** The line color attribute name. */
    public final static String LPC_OLD_LINECOLOR = "lineColor";
    /** The line style (dash attribute) attribute name. */
    public final static String LPC_OLD_LINESTYLE = "lineStyle";
    /** The highlight color attribute name. */
    public final static String LPC_OLD_HIGHLIGHTCOLOR = "highlightColor";
    /** The fill color attribute name. */
    public final static String LPC_OLD_FILLCOLOR = "fillColor";
    /** The fill pattern attribute name. */
    public final static String LPC_OLD_FILLPATTERN = "fillPattern";
    /** The line width attribute name. */
    public final static String LPC_OLD_LINEWIDTH = "lineWidth";
    /** The text graphic contents attribute name. */
    public final static String LPC_OLD_LINKTEXTSTRING = "textString";
    /** The font representation attribute name. */
    public final static String LPC_OLD_LINKTEXTFONT = "textFont";
    
    /**
     * The location of the text baseline relative to the specified text
     * location. The property should be BASELINE_BOTTOM, BASELINE_MIDDLE,
     * BASELINE_TOP. BASELINE_BOTTOM is the default.
     */
    public final static String LPC_LINKTEXTBASELINE = "tbl";
    /** The rotation in degrees clockwise from North. */
    public final static String LPC_LINKROTATION = "rot";
    /** The url of an image to use in a LinkRaster, for some cases. */
    public final static String LPC_LINKRASTERIMAGEURL = "riu";
    /** The graphic identifier attribute name. */
    public final static String LPC_GRAPHICID = "gid";

    // These constants are to affect the map as a whole
    /** The latitude of the center of the map in decimal degrees. */
    public final static String LPC_CENTER_LAT = "lat";
    /** The longitude of the center of the map in decimal degrees. */
    public final static String LPC_CENTER_LONG = "lon";
    /** The scale or zoom level of the map */
    public final static String LPC_SCALE = "s";
    /** The width of the map (pixels) */
    public final static String LPC_WIDTH = "w";
    /** The height of the map (pixels) */
    public final static String LPC_HEIGHT = "h";
    /** The projection to use for the map */
    public final static String LPC_PROJECTION = "p";
    /** The lower left latitude */
    public final static String LPC_LATMIN = "latmin";
    /** The lower left longitude */
    public final static String LPC_LONMIN = "lonmin";
    /** The upper right latitude */
    public final static String LPC_LATMAX = "latmax";
    /** The upper right longitude */
    public final static String LPC_LONMAX = "lonmax";

    // Constants that can fire a information delegator action.

    /** The URL text attribute name. */
    public final static String LPC_URL = "url";
    /** The HTML text (displayed in a browser) attribute name. */
    public final static String LPC_HTML = "html";
    /** The Information Line (status line) attribute name. */
    public final static String LPC_INFO = "info";
    /** The Message text (in a pop-up window) attribute name. */
    public final static String LPC_MESSAGE = "mess";
    /** The time in milliseconds since 1/1/1970 00:00:00. */
    public final static String LPC_TIME = "time";

    /** Line styles */
    public final static String LPC_SOLID = "S";
    public final static String LPC_LONG_DASH = "LD";
    public final static String LPC_DASH = "DA";
    public final static String LPC_DOT = "D";
    public final static String LPC_DASH_DOT = "DAD";
    public final static String LPC_DASH_DOT_DOT = "DADD";

    /** Fill patterns */
    public final static String LPC_SOLID_PATTERN = "S";
    public final static String LPC_VERTICAL_PATTERN = "V";
    public final static String LPC_HORIZONTAL_PATTERN = "H";
    public final static String LPC_CROSS_PATTERN = "C";
    public final static String LPC_DIAG_CROSS_PATTERN = "DC";
    public final static String LPC_FORWARD_DIAG_PATTERN = "FD";
    public final static String LPC_BACKWARD_DIAG_PATTERN = "BD";

    public final static String LPC_CLEAR_PROPERTIES = "C";
    public final static char LPC_CLEAR_PROPERTIES_CHAR = 'C';
    public final static String LPC_REUSE_PROPERTIES = "R";
    public final static char LPC_REUSE_PROPERTIES_CHAR = 'R';

}
