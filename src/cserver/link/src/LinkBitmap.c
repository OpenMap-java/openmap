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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkBitmap.c,v $
 * $RCSfile: LinkBitmap.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkBitmap.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"

int WriteLinkBitmapHeader(LinkSocket *linkSocket)
{  
    int check = OK;
    check = check || WriteChars(linkSocket, BITMAP_HEADER, lBITMAP_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_BITMAP);
    return check;
}

int BufferedWriteLinkBitmapHeader(char *toBuffer)
{
    int byteswritten = 0;
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       BITMAP_HEADER,
                                       lBITMAP_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         GRAPHICTYPE_BITMAP);
    return byteswritten;
}

/*
  @param LinkSocket: the socket connection on which data is written
*/ 
int BufferedWriteLinkBitmapLatLon(LinkSocket *linkSocket, 
                                  double lat, double lon, 
                                  int width, int height,
                                  int numberOfbytes, char *bitmap,
                                  LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
    
    buffercount = lBITMAP_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +  /*Bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +  /*Bytes used by width and height*/
        N_BYTES_PER_INTEGER +  /*Bytes used by numberOfbytes*/
        numberOfbytes +        /*Bytes used by bitmap*/   
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkBitmapHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON); 
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOfbytes);  
    buffercount += BufferedWriteChars(&buffer[buffercount],bitmap, numberOfbytes);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkBitmapLatLon(LinkSocket *linkSocket, 
                          double lat, double lon, 
                          int width, int height,
                          int numberOfbytes, char *bitmap,
                          LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteLinkBitmapHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteInteger(linkSocket, numberOfbytes);  
    check = check || WriteChars(linkSocket,bitmap, numberOfbytes);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* If -1, memory allocation error in WriteLinkArgs */
}

int BufferedWriteLinkBitmapXY(LinkSocket *linkSocket,
                              int x, int y,
                              int width, int height,
                              int numberOfbytes, char *bitmap,
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
        4*N_BYTES_PER_INTEGER +   /*Bytes used by x,y,width and height*/
        N_BYTES_PER_INTEGER +  /*Bytes used by numberOfbytes*/
        numberOfbytes +        /*Bytes used by bitmap*/   
        LinkSizeOfLinkArgs(linkArgs);
    
    
    buffer = (char *)malloc(sizeof(buffercount));
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;

    buffercount += BufferedWriteLinkBitmapHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOfbytes);  
    buffercount += BufferedWriteChars(&buffer[buffercount],bitmap, numberOfbytes);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}


int WriteLinkBitmapXY(LinkSocket *linkSocket,
                      int x, int y,
                      int width, int height,
                      int numberOfbytes, char *bitmap,
                      LinkArgs *linkArgs)
{
    int check = OK;
    
    check = check || WriteLinkBitmapHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteInteger(linkSocket, numberOfbytes);  
    check = check || WriteChars(linkSocket,bitmap, numberOfbytes);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error in WriteLinkArgs*/
}

int BufferedWriteLinkBitmapOffset(LinkSocket *linkSocket,
                                  double lat, double lon,
                                  int x, int y,                    
                                  int width, int height,
                                  int numberOfbytes, char *bitmap,
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
        4*N_BYTES_PER_INTEGER +   /*Bytes used by x,y,width and height*/
        N_BYTES_PER_INTEGER +  /*Bytes used by numberOfbytes*/
        numberOfbytes +        /*Bytes used by bitmap*/   
        LinkSizeOfLinkArgs(linkArgs);
    
    
    buffer = (char *)malloc(sizeof(buffercount));
    if (buffer == NULL)
      return -1;  /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteLinkBitmapHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteInteger(&buffer[buffercount], numberOfbytes);  
    buffercount += BufferedWriteChars(&buffer[buffercount],bitmap, numberOfbytes);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkBitmapOffset(LinkSocket *linkSocket,
                          double lat, double lon,
                          int x, int y,                    
                          int width, int height,
                          int numberOfbytes, char *bitmap,
                          LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteLinkBitmapHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteInteger(linkSocket, numberOfbytes);  
    check = check || WriteChars(linkSocket,bitmap, numberOfbytes);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check; /* -1 if there was a memory allocation error */
}


