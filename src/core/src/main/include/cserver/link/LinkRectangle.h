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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkRectangle.h,v $
 * $RCSfile: LinkRectangle.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKRECTANGLE_H
#define LINKRECTANGLE_H

#include "LinkSocket.h"
#include "Request.h"
#include "LinkArgs.h"


/*Prototypes for Writing Rectangle Object*/

/**
 * Writes a rectangle object in lat-long space to the link socket.
 *
 * @param *link The link socket.
 * @param ltype The line type.
 * @param NWlat The latitude of the top left corner of the rectangle, in decimal degrees.
 * @param NWlon The longitude of the top left corner of the rectangle, in decimal degrees.
 * @param SElat The latitude of the lower right corner of the rectangle, in decimal degrees.
 * @param SElon The longitude of the lower right corner of the rectangle, in decimal degrees.
 * @param nsegs The number of segments used to approximate the rectangle.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkRectangleLatLon(LinkSocket *link, int ltype,
                             double NWlat, double NWlon, /*NW = North West*/
                             double SElat, double SElon, /*SE = South East*/
                             int nsegs,
                             LinkArgs *linkArgs);

/**
 * Writes a rectangle object in X-Y space to the link socket.  X-Y values are
 * from the top left corner of the canvas.
 *
 * @param link The link socket.
 * @param ulx The X coordinate of the upper left corner of the rectangle, in pixels.
 * @param uly The Y coordinate of the upper left corner of the rectangle, in pixels.
 * @param lrx The X coordinate of the lower right corner of the rectangle, in pixels.
 * @param lry The Y coordinate of the lower right corner of the rectangle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkRectangleXY(LinkSocket *link,  
                         int ulx, int uly, /*ul = upper left */
                         int lrx, int lry,                     
                         LinkArgs *linkArgs);

/**
 * Writes a rectangle object with an offset to the link socket.  X-Y values
 * are offset from the lat-long anchor point, in pixels.
 *
 * @param link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param ulx The X coordinate of the upper left corner of the rectangle, in pixels.
 * @param uly The Y coordinate of the upper left corner of the rectangle, in pixels.
 * @param lrx The X coordinate of the lower right corner of the rectangle, in pixels.
 * @param lry The Y coordinate of the lower right corner of the rectangle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkRectangleOffset(LinkSocket *link,
                             double lat, double lon,
                             int ulx, int uly,
                             int lrx, int lry,                     
                             LinkArgs *linkArgs);

/**
 * Writes a buffered rectangle object in lat-long space to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRectangleLatLon().
 *
 * @param *link The link socket.
 * @param ltype The line type.
 * @param NWlat The latitude of the top left corner of the rectangle, in decimal degrees.
 * @param NWlon The longitude of the top left corner of the rectangle, in decimal degrees.
 * @param SElat The latitude of the lower right corner of the rectangle, in decimal degrees.
 * @param SElon The longitude of the lower right corner of the rectangle, in decimal degrees.
 * @param nsegs The number of segments used to approximate the rectangle.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRectangleLatLon(LinkSocket *link, int ltype,
                                     double NWlat, double NWlon, /*NW = North West*/
                                     double SElat, double SElon, /*SE = South East*/
                                     int nsegs,
                                     LinkArgs *linkArgs);

/**
 * Writes a buffered rectangle object in X-Y space to the link socket.  X-Y 
 * values are from the top left corner of the canvas. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRectangleXY().
 *
 * @param link The link socket.
 * @param ulx The X coordinate of the upper left corner of the rectangle, in pixels.
 * @param uly The Y coordinate of the upper left corner of the rectangle, in pixels.
 * @param lrx The X coordinate of the lower right corner of the rectangle, in pixels.
 * @param lry The Y coordinate of the lower right corner of the rectangle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRectangleXY(LinkSocket *link,  
                                 int ulx, int uly, /*ul = upper left */
                                 int lrx, int lry,                     
                                 LinkArgs *linkArgs);

/**
 * Writes a buffered rectangle object with an offset to the link socket.  X-Y 
 * values are offset from the lat-long anchor point, in pixels. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRectangleOffset().
 *
 * @param link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param ulx The X coordinate of the upper left corner of the rectangle, in pixels.
 * @param uly The Y coordinate of the upper left corner of the rectangle, in pixels.
 * @param lrx The X coordinate of the lower right corner of the rectangle, in pixels.
 * @param lry The Y coordinate of the lower right corner of the rectangle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRectangleOffset(LinkSocket *link,
                                     double lat, double lon,
                                     int ulx, int uly,
                                     int lrx, int lry,                     
                                     LinkArgs *linkArgs);

#endif
