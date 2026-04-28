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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/MapRequest.c,v $
 * $RCSfile: MapRequest.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "MapRequest.h"
#include "Link.h"
#include "GlobalConstants.h"
/*for debugging*/
#define DEBUG_ME "LINKSERVER"
#include "toolLib/debugging.h"
DebugVariable(LINK, "LINK", 0x01); /* setenv LINKSERVER "LINK"*/

/*Reads MAP Object off socket */
int ReadMapRequest(Link* link)
{
    int check = OK;
    link->mapRequest = (MapRequest*) malloc(sizeof(MapRequest));

    if(NULL == link->mapRequest){
      return -1; /* Memory allocation error */
    }

    check = check || ReadFloat(link->socket, &(link->mapRequest->version));
    check = check || ReadFloat(link->socket, &(link->mapRequest->centerlatitude));
    check = check || ReadFloat(link->socket, &(link->mapRequest->centerlongitude));
    check = check || ReadFloat(link->socket, &(link->mapRequest->scale));
    check = check || ReadInteger(link->socket, &(link->mapRequest->height));
    check = check || ReadInteger(link->socket, &(link->mapRequest->width));

    if (Debug(LINK)){
        printf(" version read %f\n", link->mapRequest->version);
        printf(" center latitude %f\n", link->mapRequest->centerlatitude);
        printf(" center longitude %f\n", link->mapRequest->centerlongitude);
        printf(" scale %f \n", link->mapRequest->scale);
        printf(" height %d\n", link->mapRequest->height);
        printf(" width %d\n", link->mapRequest->width);
    }
    
    check = check || ReadBoundingPolygons(link->socket,
                                          &(link->mapRequest->boundingPolygon));
    if (check == -1)
      return -1; /* Memory allocation error */
    
    check = check || ReadLinkArgs(link->socket, &(link->mapRequest->linkargs));
    return check; /* If -1, it's a memory allocation error */

}

void FreeMapRequest(MapRequest *map)
{
    FreeBoundingPolygons(&map->boundingPolygon);
    FreeLinkArgs(&map->linkargs); 
}
