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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkLine.h,v $
 * $RCSfile: LinkLine.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKLINE_H
#define LINKLINE_H

#include "LinkSocket.h"
#include "LinkArgs.h"
#include "Request.h"

/**
 * Writes a line between two points in lat-long space to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat_1 The latitude of the first point, in decimal degrees.
 * @param lon_1 The longitude of the first point, in decimal degrees.
 * @param lat_2 The latitude of the second point, in decimal degrees.
 * @param lat_2 The longitude of the second point, in decimal degrees.
 * @param lineType The line type.
 * @param nsegs The number of segments to approximate the line.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkLineLatLon(LinkSocket *linkSocket,
                        double lat_1, double lon_1, 
                        double lat_2, double lon_2, 
                        int lineType, int nsegs,
                        LinkArgs *linkArgs);

/**
 * Writes a line between two points in X-Y space to the link socket.  The
 * X and Y values are offset from the upper left corner of the canvas.
 *
 * @param *linkSocket The link socket.
 * @param x1 The X coordinate of the first point, in pixels.
 * @param y1 The Y coordinate of the first point, in pixels.
 * @param x2 The X coordinate of the second point, in pixels.
 * @param y2 The Y coordinate of the second point, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkLineXY(LinkSocket *linkSocket,
                    int x1, int y1, 
                    int x2, int y2,
                    LinkArgs *linkArgs);

/**
 * Writes a line between two X-Y points, offset from a lat-long point, to 
 * the link socket.  The X and Y values are offset from the lat-long point,
 * in pixels.
 *
 * @param *linkSocket The link socket.
 * @param lat_1 The latitude of the anchor point, in decimal degrees.
 * @param lon_1 The longitude of the anchor point, in decimal degrees.
 * @param x1 The X coordinate of the first point, offset from the anchor point, in pixels.
 * @param y1 The Y coordinate of the first point, offset from the anchor point, in pixels.
 * @param x2 The X coordinate of the second point, offset from the anchor point, in pixels.
 * @param y2 The Y coordinate of the second point, offset from the anchor point, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkLineOffset(LinkSocket *linkSocket,
                        double lat_1, double lon_1, 
                        int x1, int y1, 
                        int x2, int y2, 
                        LinkArgs *linkArgs);

/**
 * Writes a buffered line between two points in lat-long space to the link 
 * socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkLineLatLon().
 *
 * @param *linkSocket The link socket.
 * @param lat_1 The latitude of the first point, in decimal degrees.
 * @param lon_1 The longitude of the first point, in decimal degrees.
 * @param lat_2 The latitude of the second point, in decimal degrees.
 * @param lat_2 The longitude of the second point, in decimal degrees.
 * @param lineType The line type.
 * @param nsegs The number of segments to approximate the line.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkLineLatLon(LinkSocket *linkSocket,
                                double lat_1, double lon_1, 
                                double lat_2, double lon_2, 
                                int lineType, int nsegs,
                                LinkArgs *linkArgs);
/**
 * Writes a buffered line between two points in X-Y space to the link socket.
 * The X and Y values are offset from the upper left corner of the canvas.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkLineXY().
 *
 * @param *linkSocket The link socket.
 * @param x1 The X coordinate of the first point, in pixels.
 * @param y1 The Y coordinate of the first point, in pixels.
 * @param x2 The X coordinate of the second point, in pixels.
 * @param y2 The Y coordinate of the second point, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkLineXY(LinkSocket *linkSocket,
                            int x1, int y1, 
                            int x2, int y2,
                            LinkArgs *linkArgs);

/**
 * Writes a buffered line between two X-Y points, offset from a lat-long 
 * point, to the link socket.  The X and Y values are offset from the 
 * lat-long point, in pixels. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkLineOffset().
 *
 * @param *linkSocket The link socket.
 * @param lat_1 The latitude of the anchor point, in decimal degrees.
 * @param lon_1 The longitude of the anchor point, in decimal degrees.
 * @param x1 The X coordinate of the first point, offset from the anchor point, in pixels.
 * @param y1 The Y coordinate of the first point, offset from the anchor point, in pixels.
 * @param x2 The X coordinate of the second point, offset from the anchor point, in pixels.
 * @param y2 The Y coordinate of the second point, offset from the anchor point, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkLineOffset(LinkSocket *linkSocket,
                                double lat_1, double lon_1, 
                                int x1, int y1, 
                                int x2, int y2, 
                                LinkArgs *linkArgs);

#endif








