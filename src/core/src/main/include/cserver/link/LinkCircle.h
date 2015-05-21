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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkCircle.h,v $
 * $RCSfile: LinkCircle.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKCIRCLE_H
#define LINKCIRCLE_H

#include "LinkSocket.h"
#include "Request.h"
#include "LinkArgs.h"

/*Prototypes for Writing Circle Object*/

/*
  Buffered objects write everything to a chunk of memory and 
  write the entire memory to socket at one short
*/

/**
 * Writes a circle to the link socket.
 *
 * @param *link The link socket.
 * @param lat The latitude of the center of the circle, in decimal degrees.
 * @param lon The longitude of the center of the circle, in decimal degrees.
 * @param radius The radius of the circle.
 * @param unit The units of the radius of the circle, (KM, MILES, or NMILES).  See Graphics.h.
 * @param nvertices The number of vertices that should be used to approximate the circle.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkCircleLatLon(LinkSocket *link, 
                          double lat, double lon, 
                          double radius, int unit,
                          int nvertices,
                          LinkArgs *linkArgs);

/**
 * Writes a circle to the link socket in X-Y space.  The X and Y coordinates
 * are offset from the upper left corner of the canvas.
 *
 * @param *link The link socket.
 * @param x The X coordinate of the center of the circle, in pixels.
 * @param y the Y coordinate of the center of the circle, in pixels.
 * @param width The width of the bounding rectangle of the circle, in pixels.
 * @param height The height of the bounding rectangle of the circle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkCircleXY(LinkSocket *link,  
                      int x, int y,
                      int width, int height,                   
                      LinkArgs *linkArgs);

/**
 * Writes a circle with an offset to the link socket.  The X and Y points are
 * offset from the lat-long anchor point.
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the center of the circle, offset from the anchor point, in pixels.
 * @param y The Y coordinate of the center of the circle, offset from the anchor point, in pixels.
 * @param width The width of the bounding rectangle of the circle, in pixels.
 * @param height The height of the bounding rectangle of the circle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int WriteLinkCircleOffset(LinkSocket *link,
                          double lat, double lon,
                          int x, int y,                    
                          int width, int height,                           
                          LinkArgs *linkArgs);

/**
 * Writes a buffered circle to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkCircleLatLon().
 *
 * @param *link The link socket.
 * @param lat The latitude of the center of the circle, in decimal degrees.
 * @param lon The longitude of the center of the circle, in decimal degrees.
 * @param radius The radius of the circle.
 * @param unit The units of the radius of the circle, (KM, MILES, or NMILES).  See Graphics.h.
 * @param nvertices The number of vertices that should be used to approximate the circle.
 * @param *linkArgs The link arguments.
 * @returns -1 if there is a memory allocation error.
 */

int BufferedWriteLinkCircleLatLon(LinkSocket *link, 
                                  double lat, double lon, 
                                  double radius, int unit,
                                  int nvertices,
                                  LinkArgs *linkArgs);

/**
 * Writes a buffered circle to the link socket in X-Y space.  The X and Y 
 * coordinates are offset from the upper left corner of the canvas.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkCircleXY().
 *
 * @param *link The link socket.
 * @param x The X coordinate of the center of the circle, in pixels.
 * @param y the Y coordinate of the center of the circle, in pixels.
 * @param width The width of the bounding rectangle of the circle, in pixels.
 * @param height The height of the bounding rectangle of the circle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkCircleXY(LinkSocket *link,
                              int x, int y,
                              int width, int height,                   
                              LinkArgs *linkArgs);

/**
 * Writes a buffered circle with an offset to the link socket.  The X and Y 
 * points are offset from the lat-long anchor point. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkCircleOffset().
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the center of the circle, offset from the anchor point, in pixels.
 * @param y The Y coordinate of the center of the circle, offset from the anchor point, in pixels.
 * @param width The width of the bounding rectangle of the circle, in pixels.
 * @param height The height of the bounding rectangle of the circle, in pixels.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int BufferedWriteLinkCircleOffset(LinkSocket *link,
                                  double lat, double lon,
                                  int x, int y,                    
                                  int width, int height,                           
                                  LinkArgs *linkArgs);

#endif





