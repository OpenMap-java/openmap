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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkBitmap.h,v $
 * $RCSfile: LinkBitmap.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKBITMAP_H
#define LINKBITMAP_H

#include "LinkArgs.h"

/*Prototypes for Writing Bitmap Object*/

/*
  Buffered objects write everything to a chunk of memory and 
  write the entire memory to socket at one short
*/

/**
 * Writes a bitmap object to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the image location (top left corner), in decimal degrees.
 * @param lon The longitude of the image location (top left corner) in decimal degrees.
 * @param width The width of the rectangle enclosing the bitmap, in pixels.
 * @param height The height of the rectangle enclosing the bitmap, in pixels.
 * @param numberOfbytes The size of the bitmap object, in bytes.
 * @param *bitmap The bitmap.
 * @param *linkArgs The link arguments.
 * @returns OK if it succeeded, -1 if there was a memory allocation error.
 */

int WriteLinkBitmapLatLon(LinkSocket *linkSocket, 
                          double lat, double lon, 
                          int width, int height,
                          int numberOfbytes, char *bitmap,
                          LinkArgs *linkArgs);

/**
 * Writes a buffered bitmap object to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkBitmapLatLon().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the image location (top left corner), in decimal degrees.
 * @param lon The longitude of the image location (top left corner) in decimal degrees.
 * @param width The width of the rectangle enclosing the bitmap, in pixels.
 * @param height The height of the rectangle enclosing the bitmap, in pixels.
 * @param numberOfbytes The size of the bitmap object, in bytes.
 * @param *bitmap The bitmap.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkBitmapLatLon(LinkSocket *linkSocket, 
                                  double lat, double lon, 
                                  int width, int height,
                                  int numberOfbytes, char *bitmap,
                                  LinkArgs *linkArgs);

/**
 * Writes a bitmap object in X-Y space to the link socket.  The X and Y
 * points are offset from the upper left corner of the canvas.
 *
 * @param linkSocket The link socket.
 * @param x The X coordinate of the image location (upper left corner), in pixels.
 * @param y The Y coordinate of the image location (upper left corner), in pixels.
 * @param width The width of the rectangle enclosing the bitmap, in pixels.
 * @param height The height of the rectangle enclosing the bitmap, in pixels.
 * @param numberOfbytes The size of the bitmap object, in bytes.
 * @param *bitmap The bitmap.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkBitmapXY(LinkSocket *linkSocket,  
                      int x, int y,
                      int width, int height,
                      int numberOfbytes, char *bitmap,
                      LinkArgs *linkArgs);

/**
 * Writes a buffered bitmap object in X-Y space to the link socket.  The X 
 * and Y points are offset from the upper left corner of the canvas.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkBitmapXY().
 *
 * @param linkSocket The link socket.
 * @param x The X coordinate of the image location (upper left corner), in pixels.
 * @param y The Y coordinate of the image location (upper left corner), in pixels.
 * @param width The width of the rectangle enclosing the bitmap, in pixels.
 * @param height The height of the rectangle enclosing the bitmap, in pixels.
 * @param numberOfbytes The size of the bitmap object, in bytes.
 * @param *bitmap The bitmap.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkBitmapXY(LinkSocket *linkSocket,  
                              int x, int y,
                              int width, int height,
                              int numberOfbytes, char *bitmap,
                              LinkArgs *linkArgs);

/**
 * Writes a bitmap object with an offset to the link socket.  The X and Y
 * coordinates are offset from the lat-long anchor point, in pixels.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param width The width of the rectangle enclosing the bitmap, in pixels.
 * @param height The height of the rectangle enclosnig the bitmap, in pixels.
 * @param numberOfbytes The size of the bitmap, in bytes.
 * @param *bitmap The bitmap.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkBitmapOffset(LinkSocket *linkSocket,
                          double lat, double lon,
                          int x, int y,                    
                          int width, int height,
                          int numberOfbytes, char *bitmap,
                          LinkArgs *linkArgs);

/**
 * Writes a buffered bitmap object with an offset to the link socket.  The X 
 * and Y coordinates are offset from the lat-long anchor point, in pixels.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkBimapOffset().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param width The width of the rectangle enclosing the bitmap, in pixels.
 * @param height The height of the rectangle enclosnig the bitmap, in pixels.
 * @param numberOfbytes The size of the bitmap, in bytes.
 * @param *bitmap The bitmap.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkBitmapOffset(LinkSocket *linkSocket,
                                  double lat, double lon,
                                  int x, int y,                    
                                  int width, int height,
                                  int numberOfbytes, char *bitmap,
                                  LinkArgs *linkArgs);

#endif
