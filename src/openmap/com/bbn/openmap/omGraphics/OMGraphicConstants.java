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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMGraphicConstants.java,v $
// $RCSfile: OMGraphicConstants.java,v $
// $Revision: 1.7 $
// $Date: 2006/10/10 22:05:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;

import com.bbn.openmap.proj.LineType;

/**
 * An interface that contains all the constants associated with OMGraphics.
 */
public interface OMGraphicConstants {

    /** Line type is unknown. */
    public final static int LINETYPE_UNKNOWN = 0;
    /** Line will be drawn straight between window points. */
    public final static int LINETYPE_STRAIGHT = LineType.Straight;
    /**
     * Line will be drawn on a constant bearing between two points.
     */
    public final static int LINETYPE_RHUMB = LineType.Rhumb;
    /**
     * Line will be drawn on the shortest geographical path between two
     * locations.
     */
    public final static int LINETYPE_GREATCIRCLE = LineType.GreatCircle;

    /** Render type is unknown. */
    public final static int RENDERTYPE_UNKNOWN = 0;
    /**
     * The graphic should be projected relative to its lat/lon position.
     */
    public final static int RENDERTYPE_LATLON = 1;
    /**
     * The graphic should be projected relative to its window position.
     */
    public final static int RENDERTYPE_XY = 2;
    /**
     * The graphic should be projected in window space relative to a lat/lon
     * position.
     */
    public final static int RENDERTYPE_OFFSET = 3;

    /**
     * The graphic should not be moved or be considered in the placement of
     * other graphics.
     */
    public final static int DECLUTTERTYPE_NONE = 0;
    /**
     * The graphic should not be moved, but its position should be considered
     * when placing other graphics.
     */
    public final static int DECLUTTERTYPE_SPACE = 1;
    /**
     * The graphic should be moved if it is going to share window space with
     * another graphic.
     */
    public final static int DECLUTTERTYPE_MOVE = 2;
    /**
     * The graphic should be moved if it is going to share window space with
     * another graphic, and a line should be drawn from the new position to the
     * original position.
     */
    public final static int DECLUTTERTYPE_LINE = 3;

    /** The generic graphic type. */
    public final static int GRAPHICTYPE_GRAPHIC = 0;
    /** A bitmap type - OMBitmap. */
    public final static int GRAPHICTYPE_BITMAP = 1;
    /** A text type - OMText. */
    public final static int GRAPHICTYPE_TEXT = 2;
    /** A polygon/polyline type - OMPoly. */
    public final static int GRAPHICTYPE_POLY = 3;
    /** A line type - OMLine. */
    public final static int GRAPHICTYPE_LINE = 4;
    /** A rectangle type - OMRect. */
    public final static int GRAPHICTYPE_RECTANGLE = 5;
    /** A ellipse/circle type - OMCircle. */
    public final static int GRAPHICTYPE_CIRCLE = 6;
    /** A raster type - OMRaster. */
    public final static int GRAPHICTYPE_RASTER = 7;
    /** A grid type - OMGrid. */
    public final static int GRAPHICTYPE_GRID = 8;
    /** A point type - OMPoint */
    public final static int GRAPHICTYPE_POINT = 9;
    /** An arc type - OMArc. */
    public final static int GRAPHICTYPE_ARC = 10;
    /** A ellipse type - OMEllipse. */
    public final static int GRAPHICTYPE_ELLIPSE = 11;

    /**
     * The float coordinates are in decimal degrees. Should not be used -
     * switching over to com.bbn.openmap.proj.Length.
     */
    public final static int DECIMAL_DEGREES = 0;
    /**
     * The float coordinates are in radians. Should not be used - switching over
     * to com.bbn.openmap.proj.Length.
     */
    public final static int RADIANS = 1;

    /** A transparent color. */
    public final static Color clear = com.bbn.openmap.util.ColorFactory.createColor(0,
                                                                                    true);

    /** A Basic Stroke. */
    public final static java.awt.Stroke BASIC_STROKE = new java.awt.BasicStroke();

    /**
     * The default rotation andle to use for java.awt.Graphics2D objects.
     */
    public static final double DEFAULT_ROTATIONANGLE = 0.0;

    /**
     * Graphic action descriptor mask - raise the graphic on top of others.
     */
    public static final int RAISE_TO_TOP_GRAPHIC_MASK = 1 << 0;
    /** Graphic action descriptor mask - lower graphics below others. */
    public static final int LOWER_TO_BOTTOM_GRAPHIC_MASK = 1 << 1;
    /** Graphic action descriptor mask - delete the graphic. */
    public static final int DELETE_GRAPHIC_MASK = 1 << 2;
    /** Graphic action descriptor mask - select the graphic. */
    public static final int SELECT_GRAPHIC_MASK = 1 << 3;
    /** Graphic action descriptor mask - deselect the graphic. */
    public static final int DESELECT_GRAPHIC_MASK = 1 << 4;
    /** Graphic action descriptor mask - deselect all graphics. */
    public static final int DESELECTALL_GRAPHIC_MASK = 1 << 5;
    /** Graphic action descriptor mask - add a graphic. */
    public static final int ADD_GRAPHIC_MASK = 1 << 6;
    /**
     * Graphic action descriptor mask - update the graphic. Really for
     * client/server notification to update.
     */
    public static final int UPDATE_GRAPHIC_MASK = 1 << 7;
    /**
     * Graphic action descriptor mask - raise the graphic relative to others by
     * one.
     */
    public static final int RAISE_GRAPHIC_MASK = 1 << 8;
    /**
     * Graphic action descriptor mask - lower down relative to other graphics by
     * one.
     */
    public static final int LOWER_GRAPHIC_MASK = 1 << 9;
    /**
     * Graphic action descriptor mask - sort the graphics. The sorting criteria
     * depends on local criteria as implemented in the object doing the sorting.
     */
    public static final int SORT_GRAPHICS_MASK = 1 << 10;

    /**
     * A string that can be used for a keyword into the OMGraphic attribute
     * hashtable to designate a tooltip for an OMGraphic. The layer would then
     * look for a value for this keyword to display as a tooltip for an
     * OMGraphic.
     */
    public static final String TOOLTIP = "Tooltip";
    /**
     * A string that can be used for a keyword into the OMGraphic attribute
     * hashtable to designate something that would be displayed on the
     * InformationDelegator's information line for an OMGraphic. The layer would
     * then look for a value for this keyword to display in the information line
     * for an OMGraphic.
     */
    public static final String INFOLINE = "Information Line";
    /**
     * A string that can be used for a keyword into the OMGraphic attribute
     * hashtable to designate something that would be displayed as a label for
     * an OMGraphic.
     */
    public static final String LABEL = "Label";

    /**
     * A string that can be used for a keyword into the OMGraphic attribute
     * hashtable. If the value exists and is false it means graphic can't be
     * removed (there won't be menu item "delete")
     */
    public static final String REMOVABLE = "Removable";

    /**
     * A string that can be used for a keyword into the OMGraphic attribute
     * hashtable. If the value exists and is false it means graphic change
     * appearance can't be changed (there won't be menu item "Change
     * appearance")
     */
    public static final String CHANGE_APPEARANCE = "ChangeAppearance";

    /**
     * A string that can be used in the attributes to make a note that the
     * OMGraphic has changed, and its state should be updated somewhere.
     */
    public static final String UPDATED = "Updated";
    
    public static final String OMGRAPHIC_ELT = "omgraphic";
    
    public static final String OMGRAPHIC_TYPE_ATTR = "type";
    
}
