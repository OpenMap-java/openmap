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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkRectangle.c,v $
 * $RCSfile: LinkRectangle.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkRectangle.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"

int WriteRectangleHeader(LinkSocket *linkSocket)
{
    int check = OK;
    check = check || WriteChars(linkSocket, RECTANGLE_HEADER,
                                lRECTANGLE_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_RECTANGLE);
    return check;
}

int WriteLinkRectangleLatLon(LinkSocket *linkSocket, int ltype,
                             double NWlat, double NWlon, /*NW = North West*/
                             double SElat, double SElon, /*SE = South East*/
                             int nsegs,
                             LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteRectangleHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteInteger(linkSocket, ltype);
    check = check || WriteFloat(linkSocket,(float)NWlat);
    check = check || WriteFloat(linkSocket,(float)NWlon);
    check = check || WriteFloat(linkSocket,(float)SElat);
    check = check || WriteFloat(linkSocket,(float)SElon);
    check = check || WriteInteger(linkSocket, nsegs);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int WriteLinkRectangleXY(LinkSocket *linkSocket,
                         int ulx, int uly, 
                         int lrx, int lry,
                         LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteRectangleHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, ulx);
    check = check || WriteInteger(linkSocket, uly);
    check = check || WriteInteger(linkSocket, lrx);
    check = check || WriteInteger(linkSocket, lry);  
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int WriteLinkRectangleOffset(LinkSocket *linkSocket,
                             double lat, double lon,
                             int ulx, int uly,
                             int lrx, int lry,                     
                             LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteRectangleHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, ulx);
    check = check || WriteInteger(linkSocket, uly);
    check = check || WriteInteger(linkSocket, lrx);
    check = check || WriteInteger(linkSocket, lry);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteRectangleHeader(char *toBuffer)
{
    int byteswritten = 0;
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       RECTANGLE_HEADER,
                                       lRECTANGLE_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         GRAPHICTYPE_RECTANGLE);
    return byteswritten;
}


int BufferedWriteLinkRectangleLatLon(LinkSocket *linkSocket, int ltype,
                                     double NWlat, double NWlon, /*NW = North West*/
                                     double SElat, double SElon, /*SE = South East*/
                                     int nsegs,
                                     LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lRECTANGLE_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        4*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +   /*Bytes used by ltype and nsegs*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteRectangleHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteInteger(&buffer[buffercount], ltype);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)NWlat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)NWlon);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)SElat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)SElon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], nsegs);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}


int BufferedWriteLinkRectangleXY(LinkSocket *linkSocket,
                                 int ulx, int uly, 
                                 int lrx, int lry,
                                 LinkArgs *linkArgs)
{

    int buffercount;
    char *buffer;
    int retval;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lRECTANGLE_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        4*N_BYTES_PER_INTEGER +   /*Bytes used by bounding co-ordinates of rectangle*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteRectangleHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], ulx);
    buffercount += BufferedWriteInteger(&buffer[buffercount], uly);
    buffercount += BufferedWriteInteger(&buffer[buffercount], lrx);
    buffercount += BufferedWriteInteger(&buffer[buffercount], lry);  
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}


int BufferedWriteLinkRectangleOffset(LinkSocket *linkSocket,
                                     double lat, double lon,
                                     int ulx, int uly,
                                     int lrx, int lry,                     
                                     LinkArgs *linkArgs)
{

    int buffercount;
    char *buffer;
    int retval;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lRECTANGLE_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        4*N_BYTES_PER_INTEGER +   /*Bytes used by bounding co-ordinates of rectangle*/
        LinkSizeOfLinkArgs(linkArgs);
               

    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;

    buffercount += BufferedWriteRectangleHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], ulx);
    buffercount += BufferedWriteInteger(&buffer[buffercount], uly);
    buffercount += BufferedWriteInteger(&buffer[buffercount], lrx);
    buffercount += BufferedWriteInteger(&buffer[buffercount], lry);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}
