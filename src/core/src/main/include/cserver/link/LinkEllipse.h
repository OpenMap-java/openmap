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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkEllipse.h,v $
 * $RCSfile: LinkEllipse.h,v $
 * $Revision: 1.2 $
 * $Date: 2006/10/10 22:05:18 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKELLIPSE_H
#define LINKELLIPSE_H

#include "LinkSocket.h"
#include "Request.h"
#include "LinkArgs.h"

/*Prototypes for Writing Ellipse Object*/

/*
  Buffered objects write everything to a chunk of memory and 
  write the entire memory to socket at one short
*/

/**
 * Writes a ellipse to the link socket.
 *
 * @param *link The link socket.
 * @param lat The latitude of the center of the ellipse, in decimal degrees.
 * @param lon The longitude of the center of the ellipse, in decimal degrees.
 * @param majorAxis The length of the major axis of the ellipse.
 * @param minorAxis The length of the minor axis of the ellipse.
 * @param unit The units of the radius of the ellipse, (KM, MILES, or NMILES).  See Graphics.h.
 * @param rotation the angle rotation applied to the ellipse.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkEllipseLatLon(LinkSocket *link, 
			   double lat, double lon, 
			   double majorAxis, double minorAxis, int unit,
			   double rotation,
			   LinkArgs *linkArgs);

/**
 * Writes a ellipse to the link socket in X-Y space.  The X and Y coordinates
 * are offset from the upper left corner of the canvas.
 *
 * @param *link The link socket.
 * @param x The X coordinate of the center of the ellipse, in pixels.
 * @param y the Y coordinate of the center of the ellipse, in pixels.
 * @param majorAxis The length of the major axis of the ellipse, in pixels.
 * @param minorAxis The length of the minor axis of the ellipse, in pixels.
 * @param rotation the angle rotation applied to the ellipse.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkEllipseXY(LinkSocket *link,  
		       int x, int y,
		       int majorAxis, int minorAxis,
		       double rotation,
		       LinkArgs *linkArgs);

/**
 * Writes a ellipse with an offset to the link socket.  The X and Y points are
 * offset from the lat-long anchor point.
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the center of the ellipse, offset from the anchor point, in pixels.
 * @param y The Y coordinate of the center of the ellipse, offset from the anchor point, in pixels.
 * @param majorAxis The length of the major axis of the ellipse, in pixels.
 * @param minorAxis The length of the minor axis of the ellipse, in pixels.
 * @param rotation the angle rotation applied to the ellipse.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkEllipseOffset(LinkSocket *link,
			   double lat, double lon,
			   int x, int y,                    
			   int majorAxis, int minorAxis,
			   double rotation,
			   LinkArgs *linkArgs);

#endif





