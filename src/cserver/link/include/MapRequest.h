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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/MapRequest.h,v $
 * $RCSfile: MapRequest.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef MAPREQUEST_H
#define MAPREQUEST_H

#include "Request.h"
#include "LinkArgs.h"

/*#include "Link.h"*/

/**
 * The MapRequest, which holds the Map (graphics query) information sent by
 * the client.
 *
 * @param version The version.
 * @param centerlatitude In decimal degrees.
 * @param centerlongitude In decimal degrees.
 * @param scale
 * @param height
 * @param width
 * @param boundingPolygon
 * @param linkargs THe link arguments.
 */

struct MapRequest {
  double version;
  double centerlatitude; /*in decimal degrees*/
  double centerlongitude; /* --"-- */
  double scale;
  int height;
  int width;
  BoundingPolygon boundingPolygon;
  LinkArgs linkargs;
};
typedef struct MapRequest MapRequest; 

struct Link;

/**
 * Reads the map object off the socket.
 *
 * @param link The link.
 * @returns -1 if there was a memory allocation error.
 *
 */

int ReadMapRequest(struct Link* link);

/**
 * Frees all of the memory from a map request object.
 *
 * @param *map The map request to free memory from.
 */


void FreeMapRequest(MapRequest *map);

#endif 


