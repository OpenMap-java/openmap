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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkConstants.java,v $
// $RCSfile: LinkConstants.java,v $
// $Revision: 1.5 $
// $Date: 2007/02/26 17:12:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

/**
 * The LinkConstants object is an interface that describes all the
 * constants that relate directly to the Link object, and that are
 * defined by the Link Protocol. They are kept here in the
 * LinkConstants interface to keep the Link object less cluttered.
 */
public interface LinkConstants {
    /** The value used for an unknown or invalid setting. */
    public final static int UNKNOWN = -1;
    /** The symbol used to end a request or response. */
    public final static String END_TOTAL = "\r";
    /** The symbol used to end a section/part of a request/response. */
    public final static String END_SECTION = "\n";
    /** The Link Protocol version. Latest version is 0.6. */
    public static final float LINK_VERSION = 0.6f;

    /** Graphics request header. */
    public static final String MAP_REQUEST_HEADER = "<MR>";
    /** Gesture request header. */
    public static final String ACTION_REQUEST_HEADER = "<AR>";
    /** GUI request header. */
    public static final String GUI_REQUEST_HEADER = "<GUIR>";
    /** Graphics response header. */
    public static final String GRAPHICS_HEADER = "<G>";
    /** Gesture response header. */
    public static final String ACTIONS_HEADER = "<A>";
    /** GUI response header. */
    public static final String GUI_HEADER = "<GUI>";
    /** Close Link header. */
    public static final String CLOSE_LINK_HEADER = "<Q>";
    /** No Action header. */
    public static final String HUH_HEADER = "<?>";
    /** Ping request header. */
    public static final String PING_REQUEST_HEADER = "<PRQ>";
    /** Ping response header */
    public static final String PING_RESPONSE_HEADER = "<PR>";
    /** Header to instruct the other other side to quit. */
    public static final String SHUTDOWN_HEADER = "<SDWN>";

    /** Bitmap graphic object header. */
    public static final String BITMAP_HEADER = "<B>";
    /** Text graphic object header. */
    public static final String TEXT_HEADER = "<T>";
    /** Poly graphic object header. */
    public static final String POLY_HEADER = "<PY>";
    /** Line graphic object header. */
    public static final String LINE_HEADER = "<L>";
    /** Rectangle graphic object header. */
    public static final String RECTANGLE_HEADER = "<RE>";
    /** Circle graphic object header. */
    public static final String CIRCLE_HEADER = "<C>";
    /** Raster graphic object header. */
    public static final String RASTER_HEADER = "<RA>";
    /** Grid graphic object header. */
    public static final String GRID_HEADER = "<GD>";
    /** Generic graphic object header. */
    public static final String GRAPHIC_HEADER = "<GR>";
    /** Point graphic object header. */
    public static final String POINT_HEADER = "<PO>";
    /** Arc graphic object header. */
    public static final String ARC_HEADER = "<A>";
    /** Ellipse graphic object header. */
    public static final String ELLIPSE_HEADER = "<E>";

    /** Graphics action object header. */
    public static final String UPDATE_GRAPHICS = "<UG>";
    /** GUI action object header. */
    public static final String UPDATE_GUI = "<UGUI>";
    /** Map action object header. */
    public static final String UPDATE_MAP = "<UM>";

}
