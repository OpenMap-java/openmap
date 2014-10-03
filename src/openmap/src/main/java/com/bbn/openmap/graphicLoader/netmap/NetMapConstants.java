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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapConstants.java,v $
// $RCSfile: NetMapConstants.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

/**
 * This interface describes the different constant settings used for
 * the NetMapEvent properties. If you implement this class, you have
 * direct access to the constants.
 */
public interface NetMapConstants {

    public static final String DEFAULT_SERVER = "localhost";
    public static final String DEFAULT_PORT = "5554";

    public static final int ERROR_VALUE_INT = Integer.MIN_VALUE;
    public static final float ERROR_VALUE_FLOAT = Float.MIN_VALUE;
    public static final double ERROR_VALUE_DOUBLE = Double.MIN_VALUE;

    // Actions
    public static final int NODE_MOVE = 0;
    public static final int NODE_DELETE = -1;
    public static final int LINE_DELETE = NODE_DELETE;
    public static final String NODE_MOVE_STRING = "0";
    public static final String NODE_DELETE_STRING = "-1";
    public static final String LINE_DELETE_STRING = NODE_DELETE_STRING;
    public static final String JMAP_VIEW_CMD = "shc cat jmap.views";

    // Command Types
    public static final String NODE_OBJECT = "nobj";
    public static final String NODE_OBJECT_STATUS = "nobjstat";
    public static final String LINK_OBJECT = "lobj";
    public static final String LINK_OBJECT_STATUS = "lobjstat";
    public static final String REFRESH = "refresh";
    public static final String UPDATE = "update";
    public static final String CLEAR = "clear";

    // Properties Fields
    public static final String COMMAND_FIELD = "cmd";
    public static final String INDEX_FIELD = "index";
    public static final String SHAPE_FIELD = "shape";
    public static final String STATUS_FIELD = "status";
    public static final String ICON_FIELD = "icon";
    public static final String LAT_FIELD = "lat";
    public static final String LON_FIELD = "lon";
    public static final String TIME_FIELD = "time";
    public static final String POSX_FIELD = "posx";
    public static final String POSY_FIELD = "posy";
    public static final String HEIGHT_FIELD = "height";
    public static final String WIDTH_FIELD = "width";
    public static final String MENU_FIELD = "menu";
    public static final String LABEL_FIELD = "label";
    public static final String JOFFSET_FIELD = "joffset";
    public static final String LINK_NODE1_FIELD = "lnode1";
    public static final String LINK_NODE2_FIELD = "lnode2";
    public static final String ELEVATION_FIELD = "elev";
    public static final String IP_FIELD = "ip";
    public static final String DATABASE_TAG_FIELD = "dt";
    public static final String NAME_FIELD = "name";
}