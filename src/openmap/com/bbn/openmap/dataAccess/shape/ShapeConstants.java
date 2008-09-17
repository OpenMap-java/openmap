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
// $Revision: 1.9 $
// $Date: 2008/09/17 20:47:51 $
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
     * The indicator for a polyline (arc) shape type, value of 3. NOTE: ESRI
     * decided to rename the `arc' type as the `polyline' type as of their July
     * 1998 Shapefile Technical Description paper. Maybe they should rename
     * Arc/Info as Polyline/Info?...
     */
    public static final int SHAPE_TYPE_POLYLINE = 3;

    /** The indicator for a polygon shape type, value of 5. */
    public static final int SHAPE_TYPE_POLYGON = 5;

    /** The indicator for a multipoint shape type, value of 8. */
    public static final int SHAPE_TYPE_MULTIPOINT = 8;

    public static final int SHAPE_TYPE_POINTZ = 11;
    public static final int SHAPE_TYPE_POLYLINEZ = 13;
    public static final int SHAPE_TYPE_POLYGONZ = 15;
    public static final int SHAPE_TYPE_MULTIPOINTZ = 18;
    public static final int SHAPE_TYPE_POINTM = 21;
    public static final int SHAPE_TYPE_POLYLINEM = 23;
    public static final int SHAPE_TYPE_POLYGONM = 25;
    public static final int SHAPE_TYPE_MULTIPOINTM = 28;
    public static final int SHAPE_TYPE_MULTIPATCH = 31;

    public static final Byte DBF_TYPE_BINARY = new Byte((byte) 'B');
    public static final String DBF_BINARY = "binary";
    public static final Byte DBF_TYPE_CHARACTER = new Byte((byte) 'C');
    public static final String DBF_CHARACTER = "character";
    public static final Byte DBF_TYPE_DATE = new Byte((byte) 'D');
    public static final String DBF_DATE = "date";
    public static final Byte DBF_TYPE_NUMERIC = new Byte((byte) 'N');
    public static final String DBF_NUMERIC = "numeric";
    public static final Byte DBF_TYPE_LOGICAL = new Byte((byte) 'L');
    public static final String DBF_LOGICAL = "boolean";
    public static final Byte DBF_TYPE_MEMO = new Byte((byte) 'M');
    public static final String DBF_MEMO = "Memo";
    public static final Byte DBF_TYPE_TIMESTAMP = new Byte((byte) '@');
    public static final String DBF_TIMESTAMP = "timestamp";
    public static final Byte DBF_TYPE_LONG = new Byte((byte) 'I');
    public static final String DBF_LONG = "long";
    public static final Byte DBF_TYPE_AUTOINCREMENT = new Byte((byte) '+');
    public static final String DBF_AUTOINCREMENT = "autoincrement";
    public static final Byte DBF_TYPE_FLOAT = new Byte((byte) 'F');
    public static final String DBF_FLOAT = "float";
    public static final Byte DBF_TYPE_DOUBLE = new Byte((byte) 'O');
    public static final String DBF_DOUBLE = "double";
    public static final Byte DBF_TYPE_OLE = new Byte((byte) 'G');
    public static final String DBF_OLE = "OLE";
    
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
     * Attribute Key for DbfTableModel stored in an EsriGraphicList. or for a
     * row of attribute information (ArrayList) from the DBF file on a Shape
     * feature.
     */
    public static final String DBF_ATTRIBUTE = "DBF_ATTRIBUTE";
    /**
     * Attribute key for row of attribute information (ArrayList) from the DBF
     * file on a Shape feature.
     */
    public static final String SHAPE_DBF_INFO_ATTRIBUTE = "SHAPE_DBF_INFO_ATTRIBUTE";
    /**
     * Attribute key for index Integer for a shape feature, indicating the shape
     * index into the file for the feature. Caution! Shape indexes start at 1,
     * not zero!
     */
    public static final String SHAPE_INDEX_ATTRIBUTE = "SHAPE_INDEX_ATTRIBUTE";

    /**
     * For Z and M shape types, the measure value will be stored as a Double in
     * the attribute table in the EsriGraphic under this key.
     */
    public static final String SHAPE_MEASURE_ATTRIBUTE = "SMA";
    /**
     * For multi-part Z and M shape types, the minimum SHAPE_MEASURE_ATTRIBUTE
     * value will be stored under this key in the multi-part graphic attribute
     * table, as a Double.
     */
    public static final String SHAPE_MIN_MEASURE_ATTRIBUTE = "MIN_SMA";
    /**
     * For multi-part Z and M shape types, the maximum SHAPE_MEASURE_ATTRIBUTE
     * value will be stored under this key in the multi-part graphic attribute
     * table, as a Double.
     */
    public static final String SHAPE_MAX_MEASURE_ATTRIBUTE = "MAX_SMA";

    /**
     * For Z shape types, the z value will be stored as a Double in the
     * attribute table in the EsriGraphic under this key.
     */
    public static final String SHAPE_Z_ATTRIBUTE = "SZA";
    /**
     * For multi-part Z shape types, the minimum SHAPE_Z_ATTRIBUTE value will be
     * stored under this key in the multi-part graphic attribute table, as a
     * Double.
     */
    public static final String SHAPE_MIN_Z_ATTRIBUTE = "MIN_Z";
    /**
     * For multi-part Z shape types, the maximum SHAPE_Z_ATTRIBUTE value will be
     * stored under this key in the multi-part graphic attribute table, as a
     * Double.
     */
    public static final String SHAPE_MAX_Z_ATTRIBUTE = "MAX_Z";

    /**
     * Attribute key for storing a bounding box in an attribute map for a
     * record.
     */
    public static final String SHAPE_BOUNDS_ATTRIBUTE = "BOUNDS";

}