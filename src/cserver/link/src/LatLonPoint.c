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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LatLonPoint.c,v $
 * $RCSfile: LatLonPoint.c,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include "LatLonPoint.h"
#include "GlobalConstants.h"

/*
  @param LinkSocket: The socket object from where LatLon 
                     Point values will be read.
*/
int ReadLatLonPoint(LinkSocket *linkSocket, LatLonPoint *point)
{
  int check = OK;
  check = check || ReadFloat(linkSocket, &(point->Lat));
  check = check || ReadFloat(linkSocket, &(point->Lon));   
  return check;
}
