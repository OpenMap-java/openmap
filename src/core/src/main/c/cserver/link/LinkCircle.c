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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkCircle.c,v $
 * $RCSfile: LinkCircle.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkCircle.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"

int WriteCircleHeader(LinkSocket *linkSocket)
{
    int check = OK;
    check = check || WriteChars(linkSocket, CIRCLE_HEADER,
                                lCIRCLE_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_CIRCLE);
    return check;
}

int BufferedWriteCircleHeader(char *toBuffer)
{
    int byteswritten =0;
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       CIRCLE_HEADER,
                                       lCIRCLE_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         GRAPHICTYPE_CIRCLE);
    return byteswritten;
}

int WriteLinkCircleLatLon(LinkSocket *linkSocket, 
                          double lat, double lon, 
                          double radius, int unit,
                          int nvertices,
                          LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteCircleHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteFloat(linkSocket,(float)radius);
    check = check || WriteInteger(linkSocket, unit);
    check = check || WriteInteger(linkSocket, nvertices);    
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error in WriteLinkArgs*/

}


int  WriteLinkCircleXY(LinkSocket *linkSocket,
                       int x, int y,
                       int width, int height,
                       LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteCircleHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);  
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error in WriteLinkArgs() */

}


int  WriteLinkCircleOffset(LinkSocket *linkSocket,
                           double lat, double lon,
                           int x, int y,                           
                           int width, int height,
                           LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteCircleHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error in WriteLinkArgs() */

}

int BufferedWriteLinkCircleLatLon(LinkSocket *linkSocket, 
                                  double lat, double lon, 
                                  double radius, int unit,
                                  int nvertices,
                                  LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
  
    buffercount = lCIRCLE_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /*Bytes used by rendertype identifier*/
        3*N_BYTES_PER_FLOAT +  /*Bytes used by lat,lon and radius*/
        2*N_BYTES_PER_INTEGER + /*bytes used by unit and vertices*/  
        LinkSizeOfLinkArgs(linkArgs);

    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */
    buffercount=0;

    buffercount += BufferedWriteCircleHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)radius);
    buffercount += BufferedWriteInteger(&buffer[buffercount], unit);
    buffercount += BufferedWriteInteger(&buffer[buffercount], nvertices);    
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}


int BufferedWriteLinkCircleXY(LinkSocket *linkSocket,  
                              int x, int y,
                              int width, int height,                   
                              LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
  
    buffercount = lCIRCLE_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /*Bytes used by rendertype identifier*/
        4*N_BYTES_PER_INTEGER +  /*Bytes used by x,y,width and height*/
        LinkSizeOfLinkArgs(linkArgs);
  
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */
    buffercount=0;
  
    buffercount += BufferedWriteCircleHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);  
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}


int BufferedWriteLinkCircleOffset(LinkSocket *linkSocket,
                                  double lat, double lon,
                                  int x, int y,                    
                                  int width, int height,                           
                                  LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
  
    buffercount = lCIRCLE_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /*Bytes used by rendertype identifier*/
        2*N_BYTES_PER_FLOAT +  /*Bytes used by lat and lon*/
        4*N_BYTES_PER_INTEGER +  /*Bytes used by x,y,width and height*/
        LinkSizeOfLinkArgs(linkArgs);

    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */
    buffercount=0;

    buffercount += BufferedWriteCircleHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
  
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}
