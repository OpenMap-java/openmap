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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkPoly.c,v $
 * $RCSfile: LinkPoly.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkPoly.h"
#include "LinkSocket.h"
#include "Response.h"
#include "Link.h"
#include "GlobalConstants.h"


int WriteLinkPolyHeader(LinkSocket *linkSocket)
{
    int check = OK;
    check = check || WriteChars(linkSocket, POLY_HEADER, lPOLY_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_POLY);
    return check;
}

int WriteLinkPolyLatLon(LinkSocket *linkSocket, int ltype, 
                        int numberOflatlon, double latlon[],
                        int unit, int nsegs, 
                        LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    check = check || WriteLinkPolyHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteInteger(linkSocket, ltype);
    check = check || WriteInteger(linkSocket, numberOflatlon);
    
    for(i=0;i < numberOflatlon; i++)
    {
        check = check || WriteFloat(linkSocket, (float)latlon[i]);
    }
    
    check = check || WriteInteger(linkSocket, unit);
    check = check || WriteInteger(linkSocket, nsegs);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkPolyLatLon2D(LinkSocket *linkSocket, int ltype, 
                          int numberOflatlon, /*size of lat or lon array*/
                          double lat[], double lon[], /*These 2 arrays are of equal size*/
                          int unit, int nsegs, 
                          LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    check = check || WriteLinkPolyHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteInteger(linkSocket, ltype);
    check = check || WriteInteger(linkSocket, numberOflatlon + numberOflatlon);
    
    for(i=0;i < numberOflatlon; i++)
    {
        check = check || WriteFloat(linkSocket, (float)lat[i]);
        check = check || WriteFloat(linkSocket, (float)lon[i]);
    }
    
    check = check || WriteInteger(linkSocket, unit);
    check = check || WriteInteger(linkSocket, nsegs);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkPolyLatLon2F(LinkSocket *linkSocket, int ltype, 
                          int numberOflatlon, /*size of lat or lon array*/
                          float lat[], float lon[], /*These 2 arrays are of equal size*/
                          int unit, int nsegs, 
                          LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    check = check || WriteLinkPolyHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteInteger(linkSocket, ltype);
    check = check || WriteInteger(linkSocket, numberOflatlon + numberOflatlon);
    
    for(i=0;i < numberOflatlon; i++)
    {
        check = check || WriteFloat(linkSocket, (float)lat[i]);
        check = check || WriteFloat(linkSocket, (float)lon[i]);
    }
    
    check = check || WriteInteger(linkSocket, unit);
    check = check || WriteInteger(linkSocket, nsegs);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkPolyXY(LinkSocket *linkSocket,  
                    int numberOfXY, int XY[],                   
                    LinkArgs *linkArgs)
{
    int i;
    int check = OK;

    check = check || WriteLinkPolyHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, numberOfXY);

    for(i=0; i < numberOfXY; i++)
        WriteInteger(linkSocket, XY[i]);
    
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkPolyXY2(LinkSocket *linkSocket,  
                     int numberOfXY, /*count of elements in X or Y array*/
                     int X[], int Y[], /* These 2 arrays are of equal size*/
                     LinkArgs *linkArgs)
{
    int i;
    int check = OK;

    check = check || WriteLinkPolyHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, numberOfXY + numberOfXY);

    for(i=0; i < numberOfXY; i++)
    {
        WriteInteger(linkSocket, X[i]);
        WriteInteger(linkSocket, Y[i]);
    }
    
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkPolyOffset(LinkSocket *linkSocket,
                        double lat, double lon,
                        int numberOfXY, int XY[], int CoordMode,                        
                        LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    
    check = check || WriteLinkPolyHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    
    check = check || WriteFloat(linkSocket, lat);
    check = check || WriteFloat(linkSocket, lon);
    check = check || WriteInteger(linkSocket, numberOfXY);
    
    for(i=0; i < numberOfXY; i++)
        WriteInteger(linkSocket, XY[i]);
    
    check = check || WriteInteger(linkSocket, CoordMode);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkPolyOffset2(LinkSocket *linkSocket,
                         double lat, double lon,
                         int numberOfXY, /*count of elements in X or Y array*/
                         int X[], int Y[],
                         int CoordMode,                 
                         LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    
    check = check || WriteLinkPolyHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    
    check = check || WriteFloat(linkSocket, lat);
    check = check || WriteFloat(linkSocket, lon);
    check = check || WriteInteger(linkSocket, numberOfXY + numberOfXY);
    
    for(i=0; i < numberOfXY; i++)
    {
        WriteInteger(linkSocket, X[i]);
        WriteInteger(linkSocket, Y[i]);
    }
    
    check = check || WriteInteger(linkSocket, CoordMode);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkPolyHeader(char *toBuffer)
{
    int byteswritten = 0;
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       POLY_HEADER, lPOLY_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten], GRAPHICTYPE_POLY);
    return byteswritten;
}

int BufferedWriteLinkPolyLatLon(LinkSocket *linkSocket, int ltype, 
                                int numberOflatlon, double latlon[],
                                int unit, int nsegs, 
                                LinkArgs *linkArgs)
{
    int i;
    
    int buffercount;
    char *buffer;
    int retval;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lPOLY_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/               
        4*N_BYTES_PER_INTEGER +   /*Bytes used by numberOflatlon, unit, nsegs,ltype*/
        N_BYTES_PER_FLOAT*numberOflatlon +
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkPolyHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteInteger(&buffer[buffercount], ltype);
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOflatlon);

    for(i=0;i < numberOflatlon; i++)
    {
        buffercount += BufferedWriteFloat(&buffer[buffercount], (float)latlon[i]);
    }
    
    buffercount += BufferedWriteInteger(&buffer[buffercount], unit);
    buffercount += BufferedWriteInteger(&buffer[buffercount], nsegs);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int BufferedWriteLinkPolyLatLon2(LinkSocket *linkSocket, int ltype, 
                                 int numberOflatlon, /*size of lat or lon array*/
                                 double lat[], double lon[], /*These 2 arrays are of equal size*/
                                 int unit, int nsegs, 
                                 LinkArgs *linkArgs)
{
    int i;
    
    int buffercount;
    char *buffer;
    int retval;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lPOLY_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/               
        4*N_BYTES_PER_INTEGER +   /*Bytes used by numberOflatlon, unit, nsegs,ltype*/
        N_BYTES_PER_FLOAT*numberOflatlon +
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkPolyHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteInteger(&buffer[buffercount], ltype);
    buffercount += BufferedWriteInteger(&buffer[buffercount],
                                        numberOflatlon + numberOflatlon);
    
    for(i=0;i < numberOflatlon; i++)
    {
        buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat[i]);
        buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon[i]);
    }
    
    buffercount += BufferedWriteInteger(&buffer[buffercount], unit);
    buffercount += BufferedWriteInteger(&buffer[buffercount], nsegs);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int BufferedWriteLinkPolyXY(LinkSocket *linkSocket,                      
                            int numberOfXY, int XY[],                   
                            LinkArgs *linkArgs)
{
    
    int buffercount;
    char *buffer;
    int retval,i;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lPOLY_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        N_BYTES_PER_INTEGER +    /*byts used by numberOfXY*/
        2*N_BYTES_PER_INTEGER +   /*Bytes used by numberofxy and coordmode*/
        numberOfXY*N_BYTES_PER_INTEGER + /*bytes used by xy points*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkPolyHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOfXY);
    for(i=0;i < numberOfXY; i++)
    {
        buffercount += BufferedWriteInteger(&buffer[buffercount], XY[i]);
    }
    
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int BufferedWriteLinkPolyXY2(LinkSocket *linkSocket,  
                             int numberOfXY, /*count of elements in X or Y array*/
                             int X[], int Y[], /* These 2 arrays are of equal size*/
                             LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval,i;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lPOLY_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        N_BYTES_PER_INTEGER +    /*byts used by numberOfXY*/
        2*N_BYTES_PER_INTEGER +   /*Bytes used by numberofxy and coordmode*/
        numberOfXY*N_BYTES_PER_INTEGER + /*bytes used by xy points*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkPolyHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOfXY + numberOfXY);

    for(i=0;i < numberOfXY; i++)
    {
        buffercount += BufferedWriteInteger(&buffer[buffercount], X[i]);
        buffercount += BufferedWriteInteger(&buffer[buffercount], Y[i]);
    }
    
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int BufferedWriteLinkPolyOffset(LinkSocket *linkSocket,
                                double lat, double lon,
                                int numberOfXY, int XY[], int CoordMode,
                                LinkArgs *linkArgs)
{

    int buffercount;
    char *buffer;
    int retval,i;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lPOLY_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +   /*Bytes used by numberofxy and coordmode*/
        numberOfXY*N_BYTES_PER_INTEGER + /*bytes used by xy points*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkPolyHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteFloat(&buffer[buffercount], lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOfXY);
    
    for(i=0;i < numberOfXY; i++)
    {
        buffercount += BufferedWriteInteger(&buffer[buffercount], XY[i]);
    }
    
    buffercount += BufferedWriteInteger(&buffer[buffercount], CoordMode);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int BufferedWriteLinkPolyOffset2(LinkSocket *linkSocket,
                                 double lat, double lon,
                                 int numberOfXY, /*count of elements in X or Y array*/
                                 int X[], int Y[],
                                 int CoordMode,                 
                                 LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval,i;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lPOLY_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +   /*Bytes used by numberofxy and coordmode*/
        numberOfXY*N_BYTES_PER_INTEGER + /*bytes used by xy points*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkPolyHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteFloat(&buffer[buffercount], lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOfXY + numberOfXY);
    
    for(i=0;i < numberOfXY; i++)
    {
        buffercount += BufferedWriteInteger(&buffer[buffercount], X[i]);
        buffercount += BufferedWriteInteger(&buffer[buffercount], Y[i]);
    }
    
    buffercount += BufferedWriteInteger(&buffer[buffercount], CoordMode);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

