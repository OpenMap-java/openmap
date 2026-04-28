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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LatLonPoint.h,v $
 * $RCSfile: LatLonPoint.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LATLONPOINT_H
#define LATLONPOINT_H

/*
  Definition of Geo(Latitude Longitude) Point Object
*/
#include "LinkSocket.h"

/**
 * Defines latitudes and longitudes, generally in decimal degrees.
 *
 * @param Lat The latitude.
 * @param Lon The longitude.
 */

struct LatLonPoint{
    double Lat; 
    double Lon;
};
typedef struct LatLonPoint LatLonPoint;

/**
 * Reads LatLon values from the socket.
 *
 * @param *linkSocket The link socket.
 * @param *point The point to be read.
 * @returns OK if successful.
 */

int ReadLatLonPoint(LinkSocket *linkSocket, LatLonPoint *point);

#endif



