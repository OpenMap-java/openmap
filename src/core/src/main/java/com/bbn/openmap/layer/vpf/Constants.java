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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/Constants.java,v $
// $RCSfile: Constants.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

/**
 * This class provides numerous string constants (typically column
 * names for VPF tables) from the VPF Specification.
 */
public interface Constants {

    /** name of the VPF integer value description table */
    public final static String intVDTTableName = "int.vdt";
    /** name of the VPF character value description table */
    public final static String charVDTTableName = "char.vdt";
    /** the name of VPF Face Tables */
    public static final String faceTableName = "fac";
    /** the name of VPF Ring tables */
    public static final String ringTableName = "rng";
    /** the name of VPF entity node tables */
    public static final String endTableName = "end";
    /** the name of VPF connected node tables */
    public static final String cndTableName = "cnd";

    /** the id column name for any VPF table */
    public static final String ID = DcwRecordFile.ID_COLUMN_NAME;

    /** the ring file start_edge column name */
    public static final String RNG_STARTEDGE = "start_edge";
    /** the ring file face_id column name */
    public static final String RNG_FACEID = "face_id";
    /** the face file ring_ptr column name */
    public static final String FAC_RINGPTR = "ring_ptr";

    /** the edge file start_node column name */
    public static final String EDG_START_NODE = "start_node";
    /** the edge file end_node column name */
    public static final String EDG_END_NODE = "end_node";
    /** the edge file right_face column name */
    public static final String EDG_RIGHT_FACE = "right_face";
    /** the edge file left_face column name */
    public static final String EDG_LEFT_FACE = "left_face";
    /** the edge file right_edge column name */
    public static final String EDG_RIGHT_EDGE = "right_edge";
    /** the edge file left_edge column name */
    public static final String EDG_LEFT_EDGE = "left_edge";
    /** the edge file coordinates column name */
    public static final String EDG_COORDINATES = "coordinates";

    /* the node file (end or cnd) containing_face column name */
    public static final String ND_CONTAININGFACE = "containing_face";
    /* the node file (end or cnd) first_edge column name */
    public static final String ND_FIRSTEDGE = "first_edge";
    /* the node file (end or cnd) coordinate column name */
    public static final String ND_COORDINATE = "coordinate";

    /** int.vdt and char.vdt table column */
    public static final String VDT_TABLE = "table";
    /** int.vdt and char.vdt attribute column */
    public static final String VDT_ATTRIBUTE = "attribute";
    /** int.vdt and char.vdt value column */
    public static final String VDT_VALUE = "value";
    /** int.vdt and char.vdt description column */
    public static final String VDT_DESC = "description";

    /** coverage attribute table coverage_name column */
    public static final String CAT_COVNAME = "coverage_name";
    /** coverage attribute table description column */
    public static final String CAT_DESC = "description";
    /** coverage attribute table (topology) level column */
    public static final String CAT_LEVEL = "level";

    /** face bounding rectangle xmin column */
    public static final String FBR_XMIN = "xmin";
    /** face bounding rectangle ymin column */
    public static final String FBR_YMIN = "ymin";
    /** face bounding rectangle xmax column */
    public static final String FBR_XMAX = "xmax";
    /** face bounding rectangle ymax column */
    public static final String FBR_YMAX = "ymax";

    /** library attribute table library_name column */
    public static final String LAT_LIBNAME = "library_name";
    /** library attribute table xmin column */
    public static final String LAT_XMIN = FBR_XMIN;
    /** library attribute table ymin column */
    public static final String LAT_YMIN = FBR_YMIN;
    /** library attribute table xmax column */
    public static final String LAT_XMAX = FBR_XMAX;
    /** library attribute table ymax column */
    public static final String LAT_YMAX = FBR_YMAX;

    /** the feature class schema (fcs) table feature_class column */
    public static final String FCS_FEATURECLASS = "feature_class";
    /** the feature class schema (fcs) table table1 column */
    public static final String FCS_TABLE1 = "table1";
    /** the feature class schema (fcs) table table1_key column */
    public static final String FCS_TABLE1KEY = "table1_key";
    /** the feature class schema (fcs) table table2 column */
    public static final String FCS_TABLE2 = "table2";
    /** the feature class schema (fcs) table table2_key column */
    public static final String FCS_TABLE2KEY = "table2_key";

    /** the feature class schema (fcs) table table1_key column for DCW */
    public static final String DCW_FCS_TABLE1KEY = "foreign_key";
    /** the feature class schema (fcs) table table2_key column for DCW */
    public static final String DCW_FCS_TABLE2KEY = "primary_key";

    /** A string array with 0 elements */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
}