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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkPoint.h,v $
 * $RCSfile: LinkPoint.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKPOINT_H
#define LINKPOINT_H

#include "LinkSocket.h"
#include "Request.h"
#include "LinkArgs.h"


/*Prototypes for Writing Point Object*/

/**
 * Writes a point object in lat-long space to the link socket.
 *
 * @param *link The link socket.
 * @param lat The latitude of the point, in decimal degrees.
 * @param lon The longitude of the point, in decimal degrees.
 * @param radius The pixel radius of the point.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPointLatLon(LinkSocket *link, double lat, double lon, int radius,
                         LinkArgs *linkArgs);

/**
 * Writes a point object in X-Y space to the link socket.  X-Y values are
 * from the top left corner of the canvas.
 *
 * @param link The link socket.
 * @param x The X coordinate of the upper left corner of the point, in pixels.
 * @param y The Y coordinate of the upper left corner of the point, in pixels.
 * @param radius The pixel radius of the point.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPointXY(LinkSocket *link, int x, int y, int radius,
                     LinkArgs *linkArgs);

/**
 * Writes a point object with an offset to the link socket.  X-Y values
 * are offset from the lat-long anchor point, in pixels.
 *
 * @param link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the point, in pixels.
 * @param y The Y coordinate of the point, in pixels.
 * @param radius The pixel radius of the point.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkPointOffset(LinkSocket *link, double lat, double lon,
                         int x, int y, int radius, LinkArgs *linkArgs);

#endif
