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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkPropertiesConstants.java,v $
// $RCSfile: LinkPropertiesConstants.java,v $
// $Revision: 1.2 $
// $Date: 2003/08/14 22:28:46 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.util.Debug;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;

/**
 * A LinkPropertiesConstants interface defines the well known
 * expected properties that may be coming over the link inside the
 * properties.  Other properties may be carried as well, but these are
 * properties you can kind of expect to see.
 */
public interface LinkPropertiesConstants {

    // Constants that contain graphic attributes

    /** The line color attribute name. */
    public final static String LPC_LINECOLOR = "lineColor"; 
    /** The line style (dash attribute) attribute name. */
    public final static String LPC_LINESTYLE = "lineStyle";
   /** The highlight color attribute name. */
    public final static String LPC_HIGHLIGHTCOLOR = "highlightColor";
    /** The fill color attribute name. */
    public final static String LPC_FILLCOLOR = "fillColor";
    /** The fill pattern attribute name. */
    public final static String LPC_FILLPATTERN = "fillPattern";
    /** The line width attribute name. */
    public final static String LPC_LINEWIDTH = "lineWidth";
    /** The text graphic contents attribute name. */
    public final static String LPC_LINKTEXTSTRING = "textString";
    /** The font representation attribute name. */
    public final static String LPC_LINKTEXTFONT = "textFont";
    /**
     * The location of the text baseline relative to the specified
     * text location.  The property should be BASELINE_BOTTOM,
     * BASELINE_MIDDLE, BASELINE_TOP.  BASELINE_BOTTOM is the
     * default. 
     */
    public final static String LPC_LINKTEXTBASELINE = "textBaseline";
    /** The rotation in degrees clockwise from North. */
    public final static String LPC_LINKROTATION = "rotation";
    /** The url of an image to use in a LinkRaster, for some cases. */
    public final static String LPC_LINKRASTERIMAGEURL = "rasterImageURL";
    /** The graphic identifier attribute name. */
    public final static String LPC_GRAPHICID = "graphicID";

    // These constants are to affect the map as a whole
    /** The latitude of the center of the map in decimal degrees. */
    public final static String LPC_CENTER_LAT = "latitude";
    /** The longitude of the center of the map in decimal degrees. */
    public final static String LPC_CENTER_LONG = "longitude";
    /** The scale or zoom level of the map */
    public final static String LPC_SCALE = "scale";
    /** The width of the map (pixels) */
    public final static String LPC_WIDTH = "width";
    /** The height of the map (pixels) */
    public final static String LPC_HEIGHT = "height";
    /** The projection to use for the map */
    public final static String LPC_PROJECTION = "projection";
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
    public final static String LPC_MESSAGE = "message";
    /** The time in milliseconds since 1/1/1970 00:00:00. */
    public final static String LPC_TIME = "time";

    /** Line styles */
    public final static String LPC_SOLID        = "SOLID";
    public final static String LPC_LONG_DASH    = "LONG_DASH";
    public final static String LPC_DASH         = "DASH";
    public final static String LPC_DOT          = "DOT";
    public final static String LPC_DASH_DOT     = "DASH_DOT";
    public final static String LPC_DASH_DOT_DOT = "DASH_DOT_DOT";

    /** Fill patterns */
    public final static String LPC_SOLID_PATTERN         = "SOLID";
    public final static String LPC_VERTICAL_PATTERN      = "VERTICAL";
    public final static String LPC_HORIZONTAL_PATTERN    = "HORIZONTAL";
    public final static String LPC_CROSS_PATTERN         = "CROSS";
    public final static String LPC_DIAG_CROSS_PATTERN    = "DIAG_CROSS";
    public final static String LPC_FORWARD_DIAG_PATTERN  = "FORWARD_DIAG";
    public final static String LPC_BACKWARD_DIAG_PATTERN = "BACKWARD_DIAG";
}
