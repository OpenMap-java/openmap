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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkRaster.c,v $
 * $RCSfile: LinkRaster.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkRaster.h"
#include "LinkSocket.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"
 
#define DEBUG_ME "LINKSERVER"
#include <toolLib/debugging.h>
DebugVariable(LINKGRAPHICS, "LINKGRAPHICS", 0x02); /* setenv LINKSERVER "LINKGRAPHICS"*/

int WriteLinkRasterHeader(LinkSocket *linkSocket)
{
    int check = OK;
    check = check || WriteChars(linkSocket, RASTER_HEADER,
                                lRASTER_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_RASTER);
    return check;
}

int BufferedWriteLinkRasterHeader(char *toBuffer)
{
    int byteswritten = 0; 
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       RASTER_HEADER,
                                       lRASTER_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         GRAPHICTYPE_RASTER);
    return byteswritten;
}


/*Direct Images*/
int WriteLinkRasterDirectLatLon(LinkSocket *linkSocket,
                                double lat, double lon, 
                                int width, int height,
                                DirectImage *directimage,
                                LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteInteger(linkSocket, IMAGE_DIRECT_COLOR_MODEL);
    check = check || WriteFloat(linkSocket, (float)lat);
    check = check || WriteFloat(linkSocket, (float)lon);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteDirectImage(linkSocket, directimage);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkRasterDirectLatLon(LinkSocket *linkSocket,
                                        double lat, double lon, 
                                        int width, int height,
                                        DirectImage *directimage,
                                        LinkArgs *linkArgs)
{
      
    int buffercount;
    char* buffer;
    int retval;
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        2*N_BYTES_PER_FLOAT +  /*Bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +  /*Bytes used by width and height*/
        N_BYTES_PER_INTEGER +  /*Bytes used by numberOfpixel(an integer) in directimage.*/
        directimage->numberOfPixels*N_BYTES_PER_INTEGER +
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if(NULL == buffer)
      return -1; /* Memory allocation error */
    buffercount = 0;

    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_DIRECT_COLOR_MODEL);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteDirectImage(&buffer[buffercount], directimage);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}


int WriteLinkRasterDirectXY(LinkSocket *linkSocket,
                             int x, int y, 
                             int width, int height,
                             DirectImage *directimage,
                             LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, IMAGE_DIRECT_COLOR_MODEL);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteDirectImage(linkSocket, directimage);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkRasterDirectXY(LinkSocket *linkSocket,
                                    int x, int y, 
                                    int width, int height,
                                    DirectImage *directimage,
                                    LinkArgs *linkArgs)
{
    
    int buffercount;
    char *buffer;
    int retval;
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        
        4*N_BYTES_PER_INTEGER +  /*Bytes used by x,y,width and height*/
        N_BYTES_PER_INTEGER +  /*Bytes used by numberOfpixel(an integer) in directimage.*/
        directimage->numberOfPixels*N_BYTES_PER_INTEGER +
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(sizeof(buffercount));
    if(NULL == buffer)
      return -1; /* Memory allocation error */

    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_DIRECT_COLOR_MODEL);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteDirectImage(&buffer[buffercount], directimage);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkRasterDirectOffset(LinkSocket *linkSocket,
                                double lat, double lon, 
                                int x, int y, 
                                int width, int height,
                                DirectImage *directimage,
                                LinkArgs *linkArgs)
{
    int check = OK;
    
    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteInteger(linkSocket, IMAGE_DIRECT_COLOR_MODEL);
    check = check || WriteFloat(linkSocket, (float)lat);
    check = check || WriteFloat(linkSocket, (float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteDirectImage(linkSocket, directimage);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkRasterDirectOffset(LinkSocket *linkSocket,
                                        double lat, double lon, 
                                        int x, int y, 
                                        int width, int height,
                                        DirectImage *directimage,
                                        LinkArgs *linkArgs)
{
    
    int buffercount;
    char *buffer;
    int retval;
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        2*N_BYTES_PER_INTEGER + /*bytes used by lat and lon*/
        4*N_BYTES_PER_INTEGER +  /*Bytes used by x,y,width and height*/
        N_BYTES_PER_INTEGER +  /*Bytes used by numberOfpixel(an integer) in directimage.*/
        directimage->numberOfPixels*N_BYTES_PER_INTEGER +
        LinkSizeOfLinkArgs(linkArgs);
  
    buffer = (char *)malloc(sizeof(buffercount));
    if(NULL == buffer)
      return -1; /* Memory allocation error */

    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_DIRECT_COLOR_MODEL);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteDirectImage(&buffer[buffercount], directimage);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}

/*----------------------------------------------------------*/
/*Indexed Images*/

int WriteLinkRasterIndexedLatLon(LinkSocket *linkSocket,
                                 double lat, double lon, 
                                 int width, int height, 
                                 int transparency,
                                 IndexedImage *indexedimage,
                                 LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteInteger(linkSocket, IMAGE_INDEX_COLOR_MODEL);
    
    if (Debug(LINKGRAPHICS)) printf("LinkRaster: Writing indexed raster.\n\
lat = %f\nlon = %f\n\
width = %d\nheight = %d\nnumber of colors = %d\nnumber of pixels = %d\n",
                                    lat, lon, width, height,
                                    indexedimage->colorTableSize,
                                    indexedimage->numberOfPixels);
    
    check = check || WriteFloat(linkSocket, (float)lat);
    check = check || WriteFloat(linkSocket, (float)lon);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteIndexedImage(linkSocket, indexedimage);
    check = check || WriteInteger(linkSocket, transparency);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int BufferedWriteLinkRasterIndexedLatLon(LinkSocket *linkSocket,
                                         double lat, double lon, 
                                         int width, int height, 
                                         int transparency,
                                         IndexedImage *indexedimage,
                                         LinkArgs *linkArgs)
{
  
    int buffercount;
    char *buffer;
    int retval;
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        2*N_BYTES_PER_INTEGER + /*bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +  /*Bytes used by width and height*/
        GetIndexedImageSize(indexedimage) +
        N_BYTES_PER_INTEGER + /*transparency */
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if(NULL == buffer)
      return -1; /* Memory allocation error */
    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_INDEX_COLOR_MODEL);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteIndexedImage(&buffer[buffercount], indexedimage);
    buffercount += BufferedWriteInteger(&buffer[buffercount], transparency);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkRasterIndexedXY(LinkSocket *linkSocket,
                             int x, int y, 
                             int width, int height,
                             int transparency,
                             IndexedImage *indexedimage,
                             LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, IMAGE_INDEX_COLOR_MODEL);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteIndexedImage(linkSocket, indexedimage);
    check = check || WriteInteger(linkSocket, transparency);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkRasterIndexedXY(LinkSocket *linkSocket,
                                     int x, int y, 
                                     int width, int height, 
                                     int transparency,
                                     IndexedImage *indexedimage,
                                     LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        4*N_BYTES_PER_INTEGER +  /*Bytes used by x,y,width and height*/
        GetIndexedImageSize(indexedimage) +
        N_BYTES_PER_INTEGER + /*transparency */
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(sizeof(buffercount));
    if(NULL == buffer)
      return -1; /* Memory allocation error */

    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_INDEX_COLOR_MODEL);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteIndexedImage(&buffer[buffercount], indexedimage);
    buffercount += BufferedWriteInteger(&buffer[buffercount], transparency);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkRasterIndexedOffset(LinkSocket *linkSocket,
                                 double lat, double lon, 
                                 int x, int y, 
                                 int width, int height, 
                                 int transparency,
                                 IndexedImage *indexedimage, 
                                 LinkArgs *linkArgs)
{
    int check = OK;
    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteInteger(linkSocket, IMAGE_INDEX_COLOR_MODEL);
    check = check || WriteFloat(linkSocket, (float)lat);
    check = check || WriteFloat(linkSocket, (float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, width);
    check = check || WriteInteger(linkSocket, height);
    check = check || WriteIndexedImage(linkSocket, indexedimage);
    check = check || WriteInteger(linkSocket, transparency);
    check = check || WriteLinkArgs(linkSocket, linkArgs);

    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkRasterIndexedOffset(LinkSocket *linkSocket,
                                         double lat, double lon, 
                                         int x, int y, 
                                         int width, int height, 
                                         int transparency,
                                         IndexedImage *indexedimage, 
                                         LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        2*N_BYTES_PER_INTEGER + /*bytes used by lat and lon*/
        4*N_BYTES_PER_INTEGER +  /*Bytes used by x,y,width and height*/
        GetIndexedImageSize(indexedimage) +
        N_BYTES_PER_INTEGER + /*transparency */
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(sizeof(buffercount));
    if(NULL == buffer)
      return -1; /* Memory allocation error */

    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_INDEX_COLOR_MODEL);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], width);
    buffercount += BufferedWriteInteger(&buffer[buffercount], height);
    buffercount += BufferedWriteIndexedImage(&buffer[buffercount], indexedimage);
    buffercount += BufferedWriteInteger(&buffer[buffercount], transparency);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}
/*-----------------------------------------------------------*/

/*UR: Images*/

int WriteLinkRasterImageURLLatLon(LinkSocket *linkSocket,
                                  double lat, double lon,
                                  char *url,
                                  LinkArgs *linkArgs)
{
    int check = OK;

    /** Add font and string to properties. */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKRASTERIMAGEURL, url, 0, 0);
    if (check == -1)
      return -1; /* Memory allocation error */

    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteInteger(linkSocket, IMAGE_URL);
    check = check || WriteFloat(linkSocket, (float)lat);
    check = check || WriteFloat(linkSocket, (float)lon);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkRasterImageURLLatLon(LinkSocket *linkSocket,
                                          double lat, double lon, 
                                          char *url,
                                          LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;

    /** Add font and string to properties. */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKRASTERIMAGEURL, url, 0, 0);
    if (retval == -1)
      return -1; /* Memory allocation error */
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        2*N_BYTES_PER_INTEGER + /*bytes used by lat and lon*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(sizeof(buffercount));
    if(NULL == buffer)
      return -1; /* Memory allocation error */

    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_URL);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkRasterImageURLXY(LinkSocket *linkSocket,
                              int x, int y, 
                              char *url,
                              LinkArgs *linkArgs)
{
    int check = OK;

    /** Add font and string to properties. */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKRASTERIMAGEURL, url, 0, 0);
    if (check == -1)
      return -1; /* Memory allocation error. */
    
    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, IMAGE_URL);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}  

int BufferedWriteLinkRasterImageURLXY(LinkSocket *linkSocket,
                                      int x, int y,
                                      char *url,
                                      LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;

    /** Add font and string to properties. */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKRASTERIMAGEURL, url, 0, 0);
    if (retval == -1)
      return -1; /* Memory allocation error */
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        2*N_BYTES_PER_INTEGER +  /*Bytes used by x,y*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(sizeof(buffercount));
    if(NULL == buffer)
      return -1; /* Memory allocation error */

    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_URL);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}

int WriteLinkRasterImageURLOffset(LinkSocket *linkSocket,
                                  double lat, double lon, 
                                  int x , int y , 
                                  char *url,
                                  LinkArgs *linkArgs)
{
    int check = OK;

    /** Add font and string to properties. */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKRASTERIMAGEURL, url, 0, 0);
    if (check == -1)
      return -1; /* Memory allocation error */

    check = check || WriteLinkRasterHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteInteger(linkSocket, IMAGE_URL);
    
    check = check || WriteFloat(linkSocket, (float)lat);
    check = check || WriteFloat(linkSocket, (float)lon);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteLinkRasterImageURLOffset(LinkSocket *linkSocket,
                                          double lat, double lon, 
                                          int x, int y,
                                          char *url,
                                          LinkArgs *linkArgs)
{
    int buffercount;
    char *buffer;
    int retval;

    /** Add font and string to properties. */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKRASTERIMAGEURL, url, 0, 0);
    if (retval == -1)
      return -1; /* Memory allocation error */
    
    buffercount = lRASTER_HEADER + /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +  /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER + /* Bytes used by RENDERTYPE indentifier. It is an integer*/
        N_BYTES_PER_INTEGER +  /*Bytes used bu Image format indentifier. It is an Integer*/
        2*N_BYTES_PER_INTEGER + /*bytes used by lat and lon*/
        2*N_BYTES_PER_INTEGER +  /*Bytes used by x,y*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if(NULL == buffer)
      return -1; /* Memory allocation error */

    buffercount = 0;
    
    buffercount += BufferedWriteLinkRasterHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteInteger(&buffer[buffercount], IMAGE_URL);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount], (float)lon);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
                
    retval = WriteChars(linkSocket, buffer,buffercount);         
    free(buffer);
    return retval;
}






