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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkPoint.c,v $
 * $RCSfile: LinkPoint.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkPoint.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"

int WritePointHeader(LinkSocket *linkSocket)
{
    int check = OK;
    check = check || WriteChars(linkSocket, POINT_HEADER,
                                lPOINT_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_POINT);
    return check;
}

int WriteLinkPointLatLon(LinkSocket *linkSocket, 
                         double lat, double lon,
                         int radius, LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WritePointHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, radius);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int WriteLinkPointXY(LinkSocket *linkSocket,
                     int x, int y, int radius, LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WritePointHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, radius);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int WriteLinkPointOffset(LinkSocket *linkSocket,
                         double lat, double lon,
                         int x, int y, int radius,                         
                         LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WritePointHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, radius);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

