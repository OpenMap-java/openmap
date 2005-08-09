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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/ShapeConstants.java,v
// $
// $RCSfile: ShapeConstants.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 17:21:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.omGraphics.DrawingAttributes;

/*
 * Constants used in reading shape files.
 */
public interface ShapeConstants {

    /** The length of a shape file header in bytes, value of 100. */
    public static final int SHAPE_FILE_HEADER_LENGTH = 100;

    /** The length of a shape file record header in bytes, value of 8. */
    public static final int SHAPE_FILE_RECORD_HEADER_LENGTH = 8;

    /** The indicator for a null shape type, value of 0 */
    public static final int SHAPE_TYPE_NULL = 0;

    /** The indicator for a point shape type, value of 1. */
    public static final int SHAPE_TYPE_POINT = 1;

    /** The indicator for an arc (polyline) shape type, value of 3. */
    public static final int SHAPE_TYPE_ARC = 3;

    /**
     * The indicator for a polyline (arc) shape type, value of 3.
     * NOTE: ESRI decided to rename the `arc' type as the `polyline'
     * type as of their July 1998 Shapefile Technical Description
     * paper. Maybe they should rename Arc/Info as Polyline/Info?...
     */
    public static final int SHAPE_TYPE_POLYLINE = 3;

    /** The indicator for a polygon shape type, value of 5. */
    public static final int SHAPE_TYPE_POLYGON = 5;

    /** The indicator for a multipoint shape type, value of 8. */
    public static final int SHAPE_TYPE_MULTIPOINT = 8;

    public static final Byte DBF_TYPE_CHARACTER = new Byte((byte) 67);
    public static final String DBF_CHARACTER = "Character";
    public static final Byte DBF_TYPE_DATE = new Byte((byte) 68);
    public static final String DBF_DATE = "Date";
    public static final Byte DBF_TYPE_NUMERIC = new Byte((byte) 78);
    public static final String DBF_NUMERIC = "Number";
    public static final Byte DBF_TYPE_LOGICAL = new Byte((byte) 76);
    public static final String DBF_LOGICAL = "Boolean";
    public static final Byte DBF_TYPE_MEMO = new Byte((byte) 77);
    public static final String DBF_MEMO = "Memo";

    public static final String PARAM_DBF = "dbf";
    public static final String PARAM_SHX = "shx";
    public static final String PARAM_SHP = "shp";

    public static final String SHAPE_DBF_DESCRIPTION = "Description";
    public static final String SHAPE_DBF_LINECOLOR = DrawingAttributes.linePaintProperty;
    public static final String SHAPE_DBF_FILLCOLOR = DrawingAttributes.fillPaintProperty;
    public static final String SHAPE_DBF_SELECTCOLOR = DrawingAttributes.selectPaintProperty;
    public static final String SHAPE_DBF_LINEWIDTH = DrawingAttributes.lineWidthProperty;
    public static final String SHAPE_DBF_DASHPATTERN = DrawingAttributes.dashPatternProperty;
    public static final String SHAPE_DBF_DASHPHASE = DrawingAttributes.dashPhaseProperty;

    /**
     * Attribute Key for DbfTableModel stored in an EsriGraphicList.
     * or for a row of attribute information (ArrayList) from the DBF
     * file on a Shape feature.
     */
    public static final String DBF_ATTRIBUTE = "DBF_ATTRIBUTE";
    /**
     * Attribute key for row of attribute information (ArrayList) from
     * the DBF file on a Shape feature.
     */
    public static final String SHAPE_DBF_INFO_ATTRIBUTE = "SHAPE_DBF_INFO_ATTRIBUTE";
    /**
     * Attribute key for index Integer for a shape feature, indicating
     * the shape index into the file for the feature. Caution! Shape
     * indexes start at 1, not zero!
     */
    public static final String SHAPE_INDEX_ATTRIBUTE = "SHAPE_INDEX_ATTRIBUTE";

}