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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkGraphicConstants.java,v $
// $RCSfile: LinkGraphicConstants.java,v $
// $Revision: 1.4 $
// $Date: 2006/10/10 22:05:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

/**
 * The LinkGraphicConstants is an interface that defines the constants
 * associated with link graphic objects. These constants are defined in
 * the Link Protocol.
 */
public interface LinkGraphicConstants {

    /**
     * The direct colormodel, for OMRasters, means the integer values
     * passed in as pixels, already reflect the RGB color values each
     * pixel should display.
     */
    public final static int COLORMODEL_DIRECT = 0;
    /**
     * The indexed colormodel, for OMRasters, means that the byte
     * array passed in for the pixels has to be resolved with a
     * colortable in order to create a integer array of RGB pixels.
     */
    public final static int COLORMODEL_INDEXED = 1;
    /**
     * The ImageIcon colormode used that means that the image has to
     * be downloaded from an URL. Right now, the IMAGEICON colormodel
     * does this. At some point, it will be changed.
     */
    public static final int COLORMODEL_URL = 2;

    /** Line type is unknown. */
    public final static int LINETYPE_UNKNOWN = 0;
    /** Line will be drawn straight between window points. */
    public final static int LINETYPE_STRAIGHT = 1;
    /**
     * Line will be drawn on a constant bearing between two points.
     */
    public final static int LINETYPE_RHUMB = 2;
    /**
     * Line will be drawn on the shortest geographical path between
     * two locations.
     */
    public final static int LINETYPE_GREATCIRCLE = 3;

    /** Render type is unknown. */
    public final static int RENDERTYPE_UNKNOWN = 0;
    /**
     * The graphic should be projected relative to its lat/lon
     * position.
     */
    public final static int RENDERTYPE_LATLON = 1;
    /**
     * The graphic should be projected relative to its window
     * position.
     */
    public final static int RENDERTYPE_XY = 2;
    /**
     * The graphic should be projected in window space relative to a
     * lat/lon position.
     */
    public final static int RENDERTYPE_OFFSET = 3;

    /** The generic graphic type. */
    public final static int GRAPHICTYPE_GRAPHIC = 0;
    /** A bitmap type. */
    public final static int GRAPHICTYPE_BITMAP = 1;
    /** A text type. */
    public final static int GRAPHICTYPE_TEXT = 2;
    /** A polygon/polyline type. */
    public final static int GRAPHICTYPE_POLY = 3;
    /** A line type. */
    public final static int GRAPHICTYPE_LINE = 4;
    /** A rectangle type. */
    public final static int GRAPHICTYPE_RECTANGLE = 5;
    /** A circle type. */
    public final static int GRAPHICTYPE_CIRCLE = 6;
    /** A raster type. */
    public final static int GRAPHICTYPE_RASTER = 7;
    /** A grid type. */
    public final static int GRAPHICTYPE_GRID = 8;
    /** A point type. */
    public final static int GRAPHICTYPE_POINT = 9;
    /** An arc type. */
    public final static int GRAPHICTYPE_ARC = 10;
    /** An ellipse type. */
    public final static int GRAPHICTYPE_ELLIPSE = 11;

    /** The float coordinates are in decimal degrees. */
    public final static int DECIMAL_DEGREES = 0;
    /** The float coordinates are in radians. */
    public final static int RADIANS = 1;

    /** Unit notation - kilometers. */
    final public static int KM = 0;
    /** Unit notation - miles. */
    final public static int MILES = 1;
    /** Unit notation - nautical miles. */
    final public static int NMILES = 2;

    /**
     * Polygon Translation offsets. For RENDERTYPE_OFFSET in a Poly,
     * the xy points are relative to the position of fixed latlon
     * point.
     */
    public final static int COORDMODE_ORIGIN = 0;
    /**
     * Polygon Delta offsets. For RENDERTYPE_OFFSET in a Poly, each xy
     * point in the array is relative to the previous point, and the
     * first point is relative to the fixed latlon point.
     */
    public final static int COORDMODE_PREVIOUS = 1;
    /** Align the text to the right of the location. */
    public final static int JUSTIFY_LEFT = 0;
    /** Align the text centered on the location. */
    public final static int JUSTIFY_CENTER = 1;
    /** Align the text to the left of the location. */
    public final static int JUSTIFY_RIGHT = 2;
    /** Align the baseline of text to the location. */
    public final static int BASELINE_BOTTOM = 0;
    /** Align the middle of text to the location. */
    public final static int BASELINE_MIDDLE = 1;
    /** Align the top of text to the location. */
    public final static int BASELINE_TOP = 2;

    /**
     * For Grids, note that the columns are the primary dimension of
     * the data.
     */
    public final static int COLUMN_MAJOR = 0;
    /**
     * For Grids, note that the rows are the primary dimension of the
     * data.
     */
    public final static int ROW_MAJOR = 1;

    /**
     * The hex string representation of a black color, used as a
     * default.
     */
    public final static String BLACK_COLOR_STRING = "FF000000";
    /**
     * The hex string representation of a clear black color, used as a
     * default.
     */
    public final static String CLEAR_COLOR_STRING = "00000000";
}
