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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkGrid.c,v $
 * $RCSfile: LinkGrid.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkGrid.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"

int WriteLinkGridHeader(LinkSocket *linkSocket)
{  
    int check = OK;
    check = check || WriteChars(linkSocket, GRID_HEADER,
                                lGRID_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_GRID);
    return check;
}

int BufferedWriteLinkGridHeader(char *toBuffer)
{
    int byteswritten = 0;
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       GRID_HEADER,
                                       lGRID_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         GRAPHICTYPE_GRID);
    return byteswritten;
}

/*
  @param LinkSocket: the socket connection on which data is written
*/ 
int BufferedWriteLinkGridLatLon(LinkSocket *linkSocket, 
                                double lat, double lon, 
                                int rows, int columns, double orientation,
                                double vResolution, double hResolution,
                                int major, int *data,
                                LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
    int numberOfBytes = rows*columns*N_BYTES_PER_INTEGER;
    
    buffercount = lGRID_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +  /*Bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +  /*Bytes used by rows and columns*/
        3*N_BYTES_PER_FLOAT + /* bytes for orientation, vResolution, hResolution */
        N_BYTES_PER_INTEGER +  /*Bytes used by major*/
        N_BYTES_PER_INTEGER +  /*Bytes used by length of data*/
        numberOfBytes + /*Bytes used by grid*/   
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */
    buffercount=0;
    
    buffercount += BufferedWriteLinkGridHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON); 
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], rows);
    buffercount += BufferedWriteInteger(&buffer[buffercount], columns);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)orientation);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)vResolution);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)hResolution);
    buffercount += BufferedWriteInteger(&buffer[buffercount], major);
    buffercount += BufferedWriteInteger(&buffer[buffercount], rows*columns);
    buffercount += BufferedWriteChars(&buffer[buffercount], (char*)data, numberOfBytes);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkGridLatLon(LinkSocket *linkSocket, 
                        double lat, double lon, 
                        int rows, int columns, double orientation,
                        double vResolution, double hResolution,
                        int major, int *data,
                        LinkArgs *linkArgs)
{
    int i;
    int numberOfInts;
    int check = OK;

    check = check || WriteLinkGridHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, rows);
    check = check || WriteInteger(linkSocket, columns);
    check = check || WriteFloat(linkSocket,(float)orientation);
    check = check || WriteFloat(linkSocket,(float)vResolution);
    check = check || WriteFloat(linkSocket,(float)hResolution);
    check = check || WriteInteger(linkSocket, major);

    numberOfInts = rows*columns;
    check = check || WriteInteger(linkSocket, numberOfInts);  
    for (i = 0; i < numberOfInts; i++){
        check = check || WriteInteger(linkSocket, data[i]);
    }
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; 
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkGridLatLonS(LinkSocket *linkSocket, 
                         double lat, double lon, 
                         int rows, int columns, double orientation,
                         double vResolution, double hResolution,
                         int major, short *data,
                         LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    int sub;
    int numberOfInts;
    
    check = check || WriteLinkGridHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, rows);
    check = check || WriteInteger(linkSocket, columns);
    check = check || WriteFloat(linkSocket,(float)orientation);
    check = check || WriteFloat(linkSocket,(float)vResolution);
    check = check || WriteFloat(linkSocket,(float)hResolution);
    check = check || WriteInteger(linkSocket, major);

    numberOfInts = rows*columns;
    check = check || WriteInteger(linkSocket, numberOfInts);
    for (i = 0; i < numberOfInts; i++){
        sub = (int)data[i];
        check = check || WriteInteger(linkSocket, sub);
    }
    
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkGridXY(LinkSocket *linkSocket,
                            int x, int y,
                            int rows, int columns, double orientation,
                            double vResolution, double hResolution,
                            int major, int *data,
                            LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
    int numberOfBytes = rows*columns*N_BYTES_PER_INTEGER;
    
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lGRID_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        4*N_BYTES_PER_INTEGER +  /*Bytes used by x, y, rows and columns*/
        3*N_BYTES_PER_FLOAT + /* bytes for orientation, vResolution and hResolution */
        N_BYTES_PER_INTEGER +  /*Bytes used by major*/
        N_BYTES_PER_INTEGER +  /*Bytes used by length of data*/
        numberOfBytes + /*Bytes used by grid*/   
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(sizeof(buffercount));
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkGridHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], rows);
    buffercount += BufferedWriteInteger(&buffer[buffercount], columns);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)orientation);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)vResolution);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)hResolution);
    buffercount += BufferedWriteInteger(&buffer[buffercount], major);
    buffercount += BufferedWriteInteger(&buffer[buffercount], rows*columns);
    buffercount += BufferedWriteChars(&buffer[buffercount], (char*)data, numberOfBytes);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}


int WriteLinkGridXY(LinkSocket *linkSocket,
                    int x, int y,
                    int rows, int columns, double orientation,
                    double vResolution, double hResolution,
                    int major, int *data,
                    LinkArgs *linkArgs)
{
    int i;
    int check = OK;
    int numberOfInts = rows*columns;

    check = check || WriteLinkGridHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, rows);
    check = check || WriteInteger(linkSocket, columns);
    check = check || WriteFloat(linkSocket,(float)orientation);
    check = check || WriteFloat(linkSocket,(float)vResolution);
    check = check || WriteFloat(linkSocket,(float)hResolution);
    check = check || WriteInteger(linkSocket, major);
    
    check = check || WriteInteger(linkSocket, numberOfInts);  
    for (i = 0; i < numberOfInts; i++){
        check = check || WriteInteger(linkSocket, data[i]);
    }

    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkGridOffset(LinkSocket *linkSocket,
                                double lat, double lon,
                                int x, int y,                      
                                int rows, int columns, double orientation,
                                double vResolution, double hResolution,
                                int major, int *data,
                                LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
    int numberOfBytes = rows*columns*N_BYTES_PER_INTEGER;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lGRID_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        4*N_BYTES_PER_INTEGER +   /*Bytes used by x,y,rows and columns*/
        /*these are common to all*/
        3*N_BYTES_PER_FLOAT + /* bytes for orientation, vResolution and hResolution */
        N_BYTES_PER_INTEGER +  /*Bytes used by major*/
        N_BYTES_PER_INTEGER +  /*Bytes used by length of data*/
        numberOfBytes + /*Bytes used by grid*/   
        LinkSizeOfLinkArgs(linkArgs);    
    
    buffer = (char *)malloc(sizeof(buffercount));
    if (buffer == NULL)
      return -1; /* Memory allocation error */
    buffercount=0;
    
    buffercount += BufferedWriteLinkGridHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], columns);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)orientation);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)vResolution);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)hResolution);
    buffercount += BufferedWriteInteger(&buffer[buffercount], major);
    buffercount += BufferedWriteInteger(&buffer[buffercount], rows*columns);
    buffercount += BufferedWriteChars(&buffer[buffercount], (char*)data, numberOfBytes);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkGridOffset(LinkSocket *linkSocket,
                        double lat, double lon,
                        int x, int y,                      
                        int rows, int columns, double orientation,
                        double vResolution, double hResolution,
                        int major, int *data,
                        LinkArgs *linkArgs)
{
    int i;
    int numberOfInts = rows*columns;
    int check = OK;
    
    check = check || WriteLinkGridHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, rows);
    check = check || WriteInteger(linkSocket, columns);
    check = check || WriteFloat(linkSocket,(float)orientation);
    check = check || WriteFloat(linkSocket,(float)vResolution);
    check = check || WriteFloat(linkSocket,(float)hResolution);
    check = check || WriteInteger(linkSocket, major);
    
    check = check || WriteInteger(linkSocket, numberOfInts);  
    for (i = 0; i < numberOfInts; i++){
        check = check || WriteInteger(linkSocket, data[i]);
    }
    
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


