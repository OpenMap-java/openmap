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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageServerConstants.java,v $
// $RCSfile: ImageServerConstants.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

/**
 * This file defines the OpenMap extensions to the WMTConstants for
 * making a web request for a map. They include String attribute
 * keywords that let you request a certain OpenMap projection.
 */
public interface ImageServerConstants extends WMTConstants {

    /**
     * Specific OpenMap projection parameter (PROJTYPE). Should match
     * string identifier for OpenMap projection type.
     */
    public final static String PROJTYPE = "PROJTYPE";
    /**
     * Specific OpenMap projection parameter (SCALE). Should reflect
     * right side of the scale ratio (1:XXXXX).
     */
    public final static String SCALE = "SCALE";
    /**
     * Specific OpenMap projection parameter (LAT). Float value.
     */
    public final static String LAT = "LAT";
    /**
     * Specific OpenMap projection parameter (LON). Float value.
     */
    public final static String LON = "LON";
    /**
     * Which layer number should be included in Image (LayerMask).
     * Integer value. Bit 0 is for layer 0 and so on.
     */
    public final static String LAYERMASK = "LAYERMASK";
    /**
     * Specify OpenMap layers that should be part of Image (LAYERS) an
     * String[] value
     */
    public static final String LAYERS = "LAYERS";
    /**
     * Keyword (pan) for the map server pan request.
     */
    public final static String PAN = "PAN";

    /**
     * Keyword (recenter) for the map server recenter request.
     */
    public final static String RECENTER = "RECENTER";

    /**
     * Specific OpenMap projection parameter (AZIMUTH). Float value.
     */
    public final static String AZIMUTH = "AZIMUTH";

    /**
     * Keyword (query) for the map server query request;
     */
    public final static String QUERY = "QUERY";

    /**
     * Specific OpenMap projection parameter (X). Int value.
     */
    public final static String X = "X";

    /**
     * Specific OpenMap projection parameter (Y). Int value.
     */
    public final static String Y = "Y";

}