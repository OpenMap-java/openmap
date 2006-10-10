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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkEllipse.c,v $
 * $RCSfile: LinkEllipse.c,v $
 * $Revision: 1.2 $
 * $Date: 2006/10/10 22:05:18 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkEllipse.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"

int WriteEllipseHeader(LinkSocket *linkSocket)
{
    int check = OK;
    check = check || WriteChars(linkSocket, ELLIPSE_HEADER,
                                lELLIPSE_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_ELLIPSE);
    return check;
}

int BufferedWriteEllipseHeader(char *toBuffer)
{
    int byteswritten =0;
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       ELLIPSE_HEADER,
                                       lELLIPSE_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         GRAPHICTYPE_ELLIPSE);
    return byteswritten;
}

int WriteLinkEllipseLatLon(LinkSocket *linkSocket, 
			   double lat, double lon, 
			   double majorAxis, double minorAxis, int unit,
			   double rotationAngle,
			   LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteEllipseHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteFloat(linkSocket, (float)majorAxis);
    check = check || WriteFloat(linkSocket, (float)minorAxis);
    check = check || WriteInteger(linkSocket, unit);
    check = check || WriteFloat(linkSocket, (float)rotationAngle);    
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error in WriteLinkArgs*/

}


int  WriteLinkEllipseXY(LinkSocket *linkSocket,
			int x, int y,
			int width, int height,
			double rotationAngle,
			LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteEllipseHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);  
    check = check || WriteFloat(linkSocket, (float)rotationAngle);    
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error in WriteLinkArgs() */

}


int  WriteLinkEllipseOffset(LinkSocket *linkSocket,
			    double lat, double lon,
			    int x, int y,                           
			    int width, int height,
			    double rotationAngle,
			    LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteEllipseHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteFloat(linkSocket, (float)rotationAngle);    
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error in WriteLinkArgs() */

}

