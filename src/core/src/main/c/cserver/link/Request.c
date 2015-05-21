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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/Request.c,v $
 * $RCSfile: Request.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "Request.h"
#include "GlobalConstants.h"

int ReadBoundingPolygons(LinkSocket *linkSocket, BoundingPolygon *bpoly)
{
  
    int i;
    int check = OK; 
    /*how many polygons are coming*/
    check = check || ReadInteger(linkSocket, &(bpoly->numberOfPolygons));

    /*allocate memory */
    bpoly->polygons = (Polygon *)malloc(sizeof(Polygon) * bpoly->numberOfPolygons);
    if(NULL == bpoly->polygons)
    {
      return -1; /* Memory allocation error */
    }

    /*Read them*/
    for(i=0; i < bpoly->numberOfPolygons; i++)
    {
        check = check || ReadPolygons(linkSocket, &bpoly->polygons[i]);
        if (check == -1)
          return -1; /* Memory allocation error */
    }
    return check;
}


int ReadPolygons(LinkSocket *linkSocket, Polygon *poly)
{
    int i;    
    int check = OK;

    /* How many Lat Lon's are coming (nLat + nLon)*/
    check = check || ReadInteger(linkSocket, &(poly->numberOfPoints));
    poly->numberOfPoints = poly->numberOfPoints/2;  /*a Lat and Lon make 1 point*/
    
    /*Allocate memory*/
    poly->points = (LatLonPoint *)malloc(sizeof(LatLonPoint) * poly->numberOfPoints);
    if(NULL == poly->points)
    {
      return -1; /* Memory allocation error */
    }

    /*Read Points*/
    for(i=0;i < poly->numberOfPoints; i++)
    {
        check = check || ReadLatLonPoint(linkSocket, &poly->points[i]);        
    }
    
    return check;
}

void FreePolygons(Polygon *poly)
{
    free(poly->points);
    poly->points = NULL;  
}

void FreeBoundingPolygons(BoundingPolygon *bpoly)
{
    int i;
    for(i=0; i < bpoly->numberOfPolygons; i++ )
    {
        FreePolygons(&bpoly->polygons[i]);   
    }

   free(bpoly->polygons);
}

int ReadDescriptor(LinkSocket *linkSocket, Descriptor *descriptor)
{
    int *temp;
    temp = (int *)descriptor;
    return ReadInteger(linkSocket, temp);  
}

int ReadModifier(LinkSocket *linkSocket, Modifier *modifier)
{
    int *temp;
    temp = (int *)modifier;
    return ReadInteger(linkSocket, temp);
}
