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
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

/**
 * The LinkConstants object is an interface that describes all the
 * constants that relate directly to the Link object, and that are
 * defined by the Link Protocol. They are kept here in the
 * LinkContants interface to keep the Link object less cluttered.
 */
public interface LinkConstants {
    /** The value used for an unknown or invalid setting. */
    public final static int UNKNOWN = -1;
    /** The symbol used to end a request or response. */
    public final static String END_TOTAL = "\r";
    /** The symbol used to end a section/part of a request/response. */
    public final static String END_SECTION = "\n";
    /** The Link Protocol version. Latest version is 0.3. */
    public static final float LINK_VERSION = 0.3f;

    /** Graphics request header. */
    public static final String MAP_REQUEST_HEADER = "<OMLINK:MAP_REQUEST>";
    /** Gesture request header. */
    public static final String ACTION_REQUEST_HEADER = "<OMLINK:ACTION_REQUEST>";
    /** GUI request header. */
    public static final String GUI_REQUEST_HEADER = "<OMLINK:GUI_REQUEST>";
    /** Graphics response header. */
    public static final String GRAPHICS_HEADER = "<OMLINK:GRAPHICS>";
    /** Gesture response header. */
    public static final String ACTIONS_HEADER = "<OMLINK:ACTIONS>";
    /** GUI response header. */
    public static final String GUI_HEADER = "<OMLINK:GUI>";
    /** Close Link header. */
    public static final String CLOSE_LINK_HEADER = "<OMLINK:CLOSE_LINK>";
    /** No Action header. */
    public static final String HUH_HEADER = "<OMLINK:HUH?>";
    /** Ping request header. */
    public static final String PING_REQUEST_HEADER = "<OMLINK:PING_REQUEST>";
    /** Ping response header */
    public static final String PING_RESPONSE_HEADER = "<OMLINK:PING_RESPONSE>";
    /** Header to instruct the other other side to quit. */
    public static final String SHUTDOWN_HEADER = "<OMLINK:SHUTDOWN>";

    /** Bitmap graphic object header. */
    public static final String BITMAP_HEADER = "<OMLINK:BITMAP>";
    /** Text graphic object header. */
    public static final String TEXT_HEADER = "<OMLINK:TEXT>";
    /** Poly graphic object header. */
    public static final String POLY_HEADER = "<OMLINK:POLY>";
    /** Line graphic object header. */
    public static final String LINE_HEADER = "<OMLINK:LINE>";
    /** Rectangle graphic object header. */
    public static final String RECTANGLE_HEADER = "<OMLINK:RECTANGLE>";
    /** Circle graphic object header. */
    public static final String CIRCLE_HEADER = "<OMLINK:CIRCLE>";
    /** Raster graphic object header. */
    public static final String RASTER_HEADER = "<OMLINK:RASTER>";
    /** Grid graphic object header. */
    public static final String GRID_HEADER = "<OMLINK:GRID>";
    /** Graphic graphic object header. */
    public static final String GRAPHIC_HEADER = "<OMLINK:GRAPHIC>";
    /** Point graphic object header. */
    public static final String POINT_HEADER = "<OMLINK:POINT>";
    /** Arc graphic object header. */
    public static final String ARC_HEADER = "<OMLINK:ARC>";

    /** Graphics action object header. */
    public static final String UPDATE_GRAPHICS = "<OMLINK:UPDATE_GRAPHICS>";
    /** GUI action object header. */
    public static final String UPDATE_GUI = "<OMLINK:UPDATE_GUI>";
    /** Map action object header. */
    public static final String UPDATE_MAP = "<OMLINK:UPDATE_MAP>";

}