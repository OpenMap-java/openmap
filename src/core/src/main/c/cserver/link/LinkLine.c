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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkLine.c,v $
 * $RCSfile: LinkLine.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkLine.h"
#include "LinkSocket.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"

int WriteLinkLineLatLon(LinkSocket *linkSocket,
                        double lat_1, double lon_1, 
                        double lat_2, double lon_2, 
                        int lineType, int nsegs,
                        LinkArgs *linkArgs)
{
    int check = OK;

    check = check || WriteChars(linkSocket, LINE_HEADER, lLINE_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_LINE); 
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON); 
    check = check || WriteInteger(linkSocket, lineType); 
    check = check || WriteFloat(linkSocket, (float)lat_1);
    check = check || WriteFloat(linkSocket, (float)lon_1);
    check = check || WriteFloat(linkSocket, (float)lat_2); 
    check = check || WriteFloat(linkSocket, (float)lon_2);
    check = check || WriteInteger(linkSocket, nsegs);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int WriteLinkLineXY(LinkSocket *linkSocket,
                    int x1, int y1, 
                    int x2, int y2,
                    LinkArgs *linkArgs)
{
    int check = OK; 
    check = check || WriteChars(linkSocket,LINE_HEADER, lLINE_HEADER); 
    check = check || WriteInteger(linkSocket,GRAPHICTYPE_LINE);  
    check = check || WriteInteger(linkSocket,RENDERTYPE_XY); 
    check = check || WriteInteger(linkSocket, x1); 
    check = check || WriteInteger(linkSocket, y1); 
    check = check || WriteInteger(linkSocket, x2);  
    check = check || WriteInteger(linkSocket, y2);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int WriteLinkLineOffset(LinkSocket *linkSocket,
                        double lat_1, double lon_1, 
                        int x1, int y1, 
                        int x2, int y2, 
                        LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteChars(linkSocket, LINE_HEADER, lLINE_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_LINE); 
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET); 
    check = check || WriteFloat(linkSocket, (float)lat_1);
    check = check || WriteFloat(linkSocket, (float)lon_1);
    check = check || WriteInteger(linkSocket, x1);
    check = check || WriteInteger(linkSocket, y1);
    check = check || WriteInteger(linkSocket, x2);
    check = check || WriteInteger(linkSocket, y2);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int BufferedWriteLinkLineLatLon(LinkSocket *linkSocket,
                                double lat_1, double lon_1, 
                                double lat_2, double lon_2, 
                                int lineType, int nsegs,
                                LinkArgs *linkArgs)
{

    int buffercount;
    char *buffer;
    int retval;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lBITMAP_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        4*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +   /*Bytes used by lineType and nsegs*/
        LinkSizeOfLinkArgs(linkArgs);
               
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteChars(&buffer[buffercount],
                                      LINE_HEADER, lLINE_HEADER);
    buffercount += BufferedWriteInteger(&buffer[buffercount], GRAPHICTYPE_LINE); 
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON); 
    buffercount += BufferedWriteInteger(&buffer[buffercount], lineType); 
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat_1);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon_1);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat_2); 
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon_2);
    buffercount += BufferedWriteInteger(&buffer[buffercount], nsegs);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}


int BufferedWriteLinkLineXY(LinkSocket *linkSocket,
                            int x1, int y1, 
                            int x2, int y2,
                            LinkArgs *linkArgs)
{
  
    int buffercount;
  
    char *buffer;
    int retval;
  
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lBITMAP_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        4*N_BYTES_PER_INTEGER +   /*Bytes used by x,y*/
        LinkSizeOfLinkArgs(linkArgs);

    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0; 

    buffercount += BufferedWriteChars(&buffer[buffercount],
                                      LINE_HEADER, lLINE_HEADER); 
    buffercount += BufferedWriteInteger(&buffer[buffercount],GRAPHICTYPE_LINE);  
    buffercount += BufferedWriteInteger(&buffer[buffercount],RENDERTYPE_XY); 
    buffercount += BufferedWriteInteger(&buffer[buffercount], x1); 
    buffercount += BufferedWriteInteger(&buffer[buffercount], y1); 
    buffercount += BufferedWriteInteger(&buffer[buffercount], x2);  
    buffercount += BufferedWriteInteger(&buffer[buffercount], y2);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer,buffercount);  
 
    free(buffer);
    return retval;
}

int BufferedWriteLinkLineOffset(LinkSocket *linkSocket,
                                double lat_1, double lon_1, 
                                int x1, int y1, 
                                int x2, int y2, 
                                LinkArgs *linkArgs)
{

    int buffercount;
    char *buffer;
    int retval;
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lBITMAP_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        4*N_BYTES_PER_INTEGER +   /*Bytes used by x,y*/
        LinkSizeOfLinkArgs(linkArgs);

    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
  
    buffercount += BufferedWriteChars(&buffer[buffercount],
                                      LINE_HEADER, lLINE_HEADER);
    buffercount += BufferedWriteInteger(&buffer[buffercount], GRAPHICTYPE_LINE); 
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET); 
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat_1);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon_1);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x1);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y1);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x2);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y2);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);

    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}
