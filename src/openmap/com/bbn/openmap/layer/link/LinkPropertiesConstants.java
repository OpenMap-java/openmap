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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
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
    /** The highlight color attribute name. */
    public final static String LPC_HIGHLIGHTCOLOR = "highlightColor";
    /** The fill color attribute name. */
    public final static String LPC_FILLCOLOR = "fillColor";
    /** The line width attribute name. */
    public final static String LPC_LINEWIDTH = "lineWidth";
    /** The text graphic contents attribute name. */
    public final static String LPC_LINKTEXTSTRING = "textString";
    /** The font representation attribute name. */
    public final static String LPC_LINKTEXTFONT = "textFont";
    /** The url of an image to use in a LinkRaster, for some cases. */
    public final static String LPC_LINKRASTERIMAGEURL = "rasterImageURL";
    /** The graphic identifier attribute name. */
    public final static String LPC_GRAPHICID = "graphicID";

    // Constants that can fire a information delegator action.

    /** The URL text attribute name. */
    public final static String LPC_URL = "url";
    /** The HTML text (displayed in a browser) attribute name. */
    public final static String LPC_HTML = "html";
    /** The Information Line (status line) attribute name. */
    public final static String LPC_INFO = "info";
    /** The Message text (in a pop-up window) attribute name. */
    public final static String LPC_MESSAGE = "message";
}
