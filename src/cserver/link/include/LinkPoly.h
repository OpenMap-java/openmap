/* **********************************************************************
 * 
 * <copyright>
 * 
 *  BBN Technologies, a Verizon Company
 *  10 Moulton Street
 *  Cambridge, MA 02138
 *  (617) 873-8000
 * 
 *  Copyright (C) BBNT Solutions LLC. All rights reserved.
 * 
 * </copyright>
 * **********************************************************************
 * 
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkPoly.h,v $
 * $RCSfile: LinkPoly.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKPOLY_H
#define LINKPOLY_H

#include "LinkSocket.h"
#include "LinkArgs.h"
#include "Request.h"

/*Prototypes for Writing PolyGon Object*/

/**
 * Writes a polygon to the link socket.
 *
 * @param *link The link socket.
 * @param latlon[] The array of lat-long points defining the polygon
 * @param unit The units of the lat-long points (either DECIMAL_DEGREES or RADIANS).  See Graphics.h.
 * @param nsegs The number of segments used to approximate the polygon.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPolyLatLon(LinkSocket *link, int ltype, 
                        int numberOflatlon, double latlon[],
                        int unit, int nsegs, 
                        LinkArgs *linkArgs);

/**
 * Writes a polygon to the link socket.  This function allows you to specify
 * the latitude and longitude as floats in two separate arrays, of size
 * numberOflatlon.
 *
 * @param *link The link socket.
 * @param ltype The line type.
 * @param numberOflatlon The number of points in the polygon.
 * @param lat[] Array of latitudes for the points, of size numberOflatlon.
 * @param lon[] Array of longitudes for the points, of size numberOflatlon.
 * @param unit The units of the lat-long points (either DECIMAL_DEGREES or RADIANS).  See Graphics.h.
 * @param nsegs The number of segments used to approximate the polygon.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPolyLatLon2F(LinkSocket *link, int ltype, 
                          int numberOflatlon, /*size of lat or lon array*/
                          float lat[], float lon[], /*These 2 arrays are of equal size*/
                          int unit, int nsegs, 
                          LinkArgs *linkArgs);


/**
 * Writes a polygon to the link socket.  This function allows you to specify
 * the latitude and longitude as doubles in two separate arrays, of size
 * numberOflatlon.
 *
 * @param *link The link socket.
 * @param ltype The line type.
 * @param numberOflatlon The number of points in the polygon.
 * @param lat[] Array of latitudes for the points, of size numberOflatlon.
 * @param lon[] Array of longitudes for the points, of size numberOflatlon.
 * @param unit The units of the lat-long points (either DECIMAL_DEGREES or RADIANS).  See Graphics.h.
 * @param nsegs The number of segments used to approximate the polygon.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPolyLatLon2D(LinkSocket *link, int ltype, 
                          int numberOflatlon, /*size of lat or lon array*/
                          double lat[], double lon[], /*These 2 arrays are of equal size*/
                          int unit, int nsegs, 
                          LinkArgs *linkArgs);

/**
 * Writes a polygon in XY space to the link socket.
 *
 * @param *link The link socket.
 * @param numberOfXY The number of points in the polygon.
 * @param XY[] Array of X and Y points, offset from the upper left corner of the canvas, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPolyXY(LinkSocket *link,  
                    int numberOfXY, int XY[],                   
                    LinkArgs *linkArgs);

/**
 * Writes a polygon in XY space to the link socket, allowing the X and Y
 * coordinates to be sent in separate arrays.
 *
 * @param *link The link socket.
 * @param numberOfXY The number of points in the polygon.
 * @param X[] Array of X points, offset from the upper left corner of the canvas, in pixels.
 * @param Y[] Array of Y points, offset from the upper left corner of the canvas, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPolyXY2(LinkSocket *link,  
                     int numberOfXY, /*count of elements in X or Y array*/
                     int X[], int Y[], /* These 2 arrays are of equal size*/
                     LinkArgs *linkArgs);

/**
 * Writes a polygon with an offset to the link socket.
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param XY[] The array of X-Y points defining the polygon, offset from the anchor point, in pixels.
 * @param CoordMode See the protocol for more information.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPolyOffset(LinkSocket *link,
                        double lat, double lon,
                        int numberOfXY, int XY[], int CoordMode,                        
                        LinkArgs *linkArgs);

/**
 * Writes a polygon with an offset to the link socket, using two arrays for the
 * X and Y coordinates.
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param XY[] The array of X-Y points defining the polygon, offset from the anchor point, in pixels.
 * @param CoordMode See the protocol for more information.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPolyOffset2(LinkSocket *link,
                         double lat, double lon,
                         int numberOfXY, /*count of elements in X or Y array*/
                         int X[], int Y[],
                         int CoordMode,                 
                         LinkArgs *linkArgs);
/**
 * Writes a buffered polygon to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkPolyLatLon().
 *
 * @param *link The link socket.
 * @param latlon[] The array of lat-long points defining the polygon
 * @param unit The units of the lat-long points (either DECIMAL_DEGREES or RADIANS).  See Graphics.h.
 * @param nsegs The number of segments used to approximate the polygon.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int BufferedWriteLinkPolyLatLon(LinkSocket *link, int ltype, 
                                int numberOflatlon, double latlon[],
                                int unit, int nsegs, 
                                LinkArgs *linkArgs);

/**
 * Writes a buffered polygon to the link socket.  This function allows you to 
 * specify the latitude and longitude as doubles in two separate arrays, of 
 * size numberOflatlon. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkPolyLatLon2().
 *
 * @param *link The link socket.
 * @param ltype The line type.
 * @param numberOflatlon The number of points in the polygon.
 * @param lat[] Array of latitudes for the points, of size numberOflatlon.
 * @param lon[] Array of longitudes for the points, of size numberOflatlon.
 * @param unit The units of the lat-long points (either DECIMAL_DEGREES or RADIANS).  See Graphics.h.
 * @param nsegs The number of segments used to approximate the polygon.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkPolyLatLon2(LinkSocket *link, int ltype, 
                                 int numberOflatlon, /*size of lat or lon array*/
                                 double lat[], double lon[], /*These 2 arrays are of equal size*/
                                 int unit, int nsegs, 
                                 LinkArgs *linkArgs);

/**
 * Writes a buffered polygon in XY space to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkPolyXY().
 *
 * @param *link The link socket.
 * @param numberOfXY The number of points in the polygon.
 * @param XY[] Array of X and Y points, offset from the upper left corner of the canvas, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int BufferedWriteLinkPolyXY(LinkSocket *link,  
                            int numberOfXY, int XY[],                   
                            LinkArgs *linkArgs);

/**
 * Writes a buffered polygon in XY space to the link socket, allowing the X 
 * and Y coordinates to be sent in separate arrays. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkPolyXY2().
 *
 * @param *link The link socket.
 * @param numberOfXY The number of points in the polygon.
 * @param X[] Array of X points, offset from the upper left corner of the canvas, in pixels.
 * @param Y[] Array of Y points, offset from the upper left corner of the canvas, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int BufferedWriteLinkPolyXY2(LinkSocket *link,  
                             int numberOfXY, /*count of elements in X or Y array*/
                             int X[], int Y[], /* These 2 arrays are of equal size*/
                             LinkArgs *linkArgs);
/**
 * Writes a buffered polygon with an offset to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkPolyOffset().
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param XY[] The array of X-Y points defining the polygon, offset from the anchor point, in pixels.
 * @param CoordMode See the protocol for more information.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkPolyOffset(LinkSocket *link,
                                double lat, double lon,
                                int numberOfXY, int XY[], int CoordMode,                        
                                LinkArgs *linkArgs);

/**
 * Writes a buffered polygon with an offset to the link socket, using two 
 * arrays for the X and Y coordinates. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: WriteLinkPolyOffset2().
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param XY[] The array of X-Y points defining the polygon, offset from the anchor point, in pixels.
 * @param CoordMode See the protocol for more information.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkPolyOffset2(LinkSocket *linkSocket,
                                 double lat, double lon,
                                 int numberOfXY, /*count of elements in X or Y array*/
                                 int X[], int Y[],
                                 int CoordMode,                 
                                 LinkArgs *linkArgs);

#endif 
