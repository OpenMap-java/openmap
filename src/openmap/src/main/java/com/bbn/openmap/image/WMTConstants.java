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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/WMTConstants.java,v $
// $RCSfile: WMTConstants.java,v $
// $Revision: 1.4 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

/**
 * This file contains all the String constants that are defined in the
 * OpenGIS WMT specification. We are trying to get to a point where we
 * use all of them.
 */
public interface WMTConstants {
    /**
     * After WMS version 1.0, use this parameter to identify the
     * version
     */
    public final static String VERSION = "VERSION";
    /**
     * After WMS version 1.0, use this parameter to identify the service
     */
    public final static String SERVICE = "SERVICE";
    /**
     * After WMS version 1.0, use this parameter to identify the feature info
     * request
     */
    public final static String GETMAP = "GetMap";
    /**
     * After WMS version 1.0, use this parameter to identify the capabilities
     * request
     */
    public final static String GETCAPABILITIES = "GetCapabilities";
    /**
     * After WMS version 1.0, use this parameter to identify the feature info
     * request
     */
    public final static String GETFEATUREINFO = "GetFeatureInfo";
    
    /**
     * Use this request parameter to to get a image with legend
     */
    public final static String GETLEGENDGRAPHIC = "GetLegendGraphic";
    
    /**
     * WMT Version (WMTVER), floating point industry. Required for WMT
     * requests.
     */
    public final static String WMTVER = "WMTVER";
    /** Request (REQUEST) type. Required for WMT requests. */
    public final static String REQUEST = "REQUEST";
    
    /**
     * Layer list (LAYERS), comma separated layer names. Required for
     * WMS GetMap and GetFeatureInfo request.
     */
    public final static String LAYERS = "LAYERS";
    
    /**
     * Layer name (LAYER)Required for WMS GetLegendGraphic request.
     */
    public final static String LAYER = "LAYER";
    
    /**
     * Query Layer list (QUERY_LAYERS), comma separated layer names. Required for
     * WMS GetFeatureInfo requests.
     */
    public final static String QUERY_LAYERS = "QUERY_LAYERS";
    
    /**
     * (STYLES) Comma separated list for one rendering style per
     * requested layer. Required for WMT requests.
     */
    public final static String STYLES = "STYLES";
    
    /**
     * Style name (STYLE) parameter name. Used by WMS GetLegendGraphic
     */
    public final static String STYLE = "STYLE";
    
    /**
     * Spatial Reference System identifier (SRS). Required for WMT
     * requests.
     */
    public final static String SRS = "SRS";
    /**
     * Coordinate Reference System identifier (CRS). Required for WMS
     * GetMap requests.
     */
    public final static String CRS = "CRS";
    /**
     * Bounding Box (BBOX) consisting of xmin, ymin, xmax, ymax list,
     * in SRS units. Required for WMT requests.
     */
    public final static String BBOX = "BBOX";
    /**
     * Pixel height of requested image (HEIGHT). Required for WMT
     * requests.
     */
    public final static String HEIGHT = "HEIGHT";
    /**
     * Pixel width of requested image (WIDTH). Required for WMT
     * requests.
     */
    public final static String WIDTH = "WIDTH";
    /**
     * Output format for image (FORMAT). Required for WMT requests.
     */
    public final static String FORMAT = "FORMAT";
    
    /**
     * Output format for GetFeatureInfo request (INFO_FORMAT). Required for WMS
     * GetFeatureInfo requests.
     */
    public final static String INFO_FORMAT = "INFO_FORMAT";
    
    /**
     * (TRANSPARENT) true if the background color should be rendered
     * as transparent, if the image format supports transparency, or
     * false otherwise. Optional for WMT requests.
     */
    public final static String TRANSPARENT = "TRANSPARENT";
    /**
     * (BGCOLOR) The hexidecimal RGB value to use for the background
     * color, if transparency is not desired or not supported by the
     * format type. Default is BGCOLOR=0xFFFFFF. Optional for WMT
     * requests.
     */
    public final static String BGCOLOR = "BGCOLOR";
    /**
     * (EXCEPTIONS) The format where exceptions are to be reported
     * from the map server. Default is EXCEPTIONS=INIMAGE. Optional
     * for WMT requests.
     */
    public final static String EXCEPTIONS = "EXCEPTIONS";

    /**
     * Keyword (map) for a Map request.
     */
    public final static String MAP = "map";

    /**
     * Keyword (capabilities) for a map server capabilities request.
     */
    public final static String CAPABILITIES = "capabilities";

    public final static String IMAGEFORMAT_JPEG = "JPEG";
    public final static String IMAGEFORMAT_GIF = "GIF";
    public final static String IMAGEFORMAT_PNG = "PNG";
    public final static String IMAGEFORMAT_TIFF = "TIFF";
    public final static String IMAGEFORMAT_GEOTIFF = "GeoTIFF";
    public final static String IMAGEFORMAT_PPM = "PPM";
    public final static String IMAGEFORMAT_WBMP = "WBMP";
    public final static String IMAGEFORMAT_SVG = "SVG";

}