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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkRaster.h,v $
 * $RCSfile: LinkRaster.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKRASTER_H
#define LINKRASTER_H

#include "LinkSocket.h"
#include "LinkImage.h"
#include "Request.h"
#include "LinkArgs.h"

/* Prototypes for writing Direct model images */

/**
 * Writes a direct model image to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the image location (upper left corner), in decimal degrees.
 * @param lon The longitude of the image location (upper left corner), in decimal degrees.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *directimage The image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there is a memory allocation error.
 */

int WriteLinkRasterDirectLatLon(LinkSocket *linkSocket,
                                double lat, double lon, 
                                int width, int height,
                                DirectImage *directimage,
                                LinkArgs *linkArgs);

/**
 * Writes a buffered direct model image to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterDirectLatLon().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the image location (upper left corner), in decimal degrees.
 * @param lon The longitude of the image location (upper left corner), in decimal degrees.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *directimage The image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterDirectLatLon(LinkSocket *linkSocket,
                                        double lat, double lon, 
                                        int width, int height,
                                        DirectImage *directimage,
                                        LinkArgs *linkArgs);


/**
 * Writes a direct model image in X-Y space to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the image location from the left side of the canvas, in pixels.
 * @param y The Y coordinate of the image location from the top of the canvas, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *directimage The image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there is a memory allocation error.
 */

int WriteLinkRasterDirectXY(LinkSocket *linkSocket,
                            int x, int y, 
                            int width, int height,
                            DirectImage *directimage,
                            LinkArgs *linkArgs);

/**
 * Writes a buffered direct model image in X-Y space to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterDirectXY().
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the image location from the left side of the canvas, in pixels.
 * @param y The Y coordinate of the image location from the top of the canvas, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *directimage The image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterDirectXY(LinkSocket *linkSocket,
                                    int x, int y, 
                                    int width, int height,
                                    DirectImage *directimage,
                                    LinkArgs *linkArgs);

/** 
 * Writes a direct model image with an offset to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point (upper left corner), in decimal degrees.
 * @param lon The longitude of the anchor point (upper left corner), in decimal degrees.
 * @param x The X coordinate of the image location, offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location, offset from the anchor point, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *directimage The image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there is a memory allocation error.
 */

int WriteLinkRasterDirectOffset(LinkSocket *linkSocket,
                                double lat, double lon, 
                                int x, int y, 
                                int width, int height,
                                DirectImage *directimage,
                                LinkArgs *linkArgs);

/**
 * Writes a buffered direct model image with an offset to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterDirectOffset().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point (upper left corner), in decimal degrees.
 * @param lon The longitude of the anchor point (upper left corner), in decimal degrees.
 * @param x The X coordinate of the image location, offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location, offset from the anchor point, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *directimage The image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterDirectOffset(LinkSocket *linkSocket,
                                        double lat, double lon, 
                                        int x, int y, 
                                        int width, int height,
                                        DirectImage *directimage,
                                        LinkArgs *linkArgs);

/*-----------------------------------------------------------*/
/*Prototypes for writing Indexed model images */

/**
 * Writes an indexed model image to the link socket.
 *
 * @param *linkSocket The link socket
 * @param lat The latitude of the image location (upper left corner), in decimal degrees.
 * @param lon The longitude of the image location (upper left corner), in decimal degrees.
 * @param width The width of the image.
 * @param height The height of the image.
 * @param transparency The transparency of the image, in a range of 0-255.  A transparency of 255 means an opaque image.
 * @param *indeximage The indexed image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkRasterIndexedLatLon(LinkSocket *linkSocket,
                                 double lat, double lon, 
                                 int width, int height,
                                 int transparency,
                                 IndexedImage *indeximage,               
                                 LinkArgs *linkArgs);

/**
 * Writes a buffered indexed model image to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterIndexedLatLon().
 *
 * @param *linkSocket The link socket
 * @param lat The latitude of the image location (upper left corner), in decimal degrees.
 * @param lon The longitude of the image location (upper left corner), in decimal degrees.
 * @param width The width of the image.
 * @param height The height of the image.
 * @param transparency The transparency of the image, in a range of 0-255.  A transparency of 255 means an opaque image.
 * @param *indeximage The indexed image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterIndexedLatLon(LinkSocket *linkSocket,
                                         double lat, double lon, 
                                         int width, int height, 
                                         int transparency,
                                         IndexedImage *indeximage,               
                                         LinkArgs *linkArgs);


/**
 * Writes an indexed model image in X-Y space to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the image location (upper left corner), offset from the top left corner of the canvas, in pixels.
 * @param y The Y coordinate of the image location, offset from the top left corner of the canvas, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param transparency The transparency of the image, in a range of 0-255.  A transparency of 255 means an opaque image.
 * @param *indeximage The indexed image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int WriteLinkRasterIndexedXY(LinkSocket *linkSocket,
                             int x, int y, 
                             int width, int height, 
                             int transparency,
                             IndexedImage *indeximage,
                             LinkArgs *linkArgs);

/**
 * Writes a buffered indexed model image in X-Y space to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterIndexedXY().
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the image location (upper left corner), offset from the top left corner of the canvas, in pixels.
 * @param y The Y coordinate of the image location, offset from the top left corner of the canvas, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param transparency The transparency of the image, in a range of 0-255.  A transparency of 255 means an opaque image.
 * @param *indeximage The indexed image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterIndexedXY(LinkSocket *linkSocket,
                                     int x, int y,
                                     int width, int height, 
                                     int transparency,
                                     IndexedImage *indeximage,
                                     LinkArgs *linkArgs);

/**
 * Writes an indexed model image with an offset to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *indeximage The indexed image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there's a memory allocation error.
 */

int WriteLinkRasterIndexedOffset(LinkSocket *linkSocket,
                                 double lat, double lon, 
                                 int x, int y,
                                 int width, int height, 
                                 int transparency,
                                 IndexedImage *indeximage, 
                                 LinkArgs *linkArgs);

/**
 * Writes a buffered indexed model image with an offset to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterIndexedOffset().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param width The width of the image, in pixels.
 * @param height The height of the image, in pixels.
 * @param *indeximage The indexed image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterIndexedOffset(LinkSocket *linkSocket,
                                         double lat, double lon, 
                                         int x, int y,
                                         int width, int height, 
                                         int transparency,
                                         IndexedImage *indeximage, 
                                         LinkArgs *linkArgs);

/*--------------------------------------------------------*/

/*Prototypes for writing Images that are refered as a URL*/

/**
 * Writes an image that is referred as a URL to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the image location (upper left corner), in decimal degrees.
 * @param lon The longitude of the image location (upper left corner), in decimal degrees.
 * @param *url URL of the image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int WriteLinkRasterImageURLLatLon(LinkSocket *linkSocket,
                                  double lat, double lon,
                                  char *url,
                                  LinkArgs *linkArgs);

/**
 * Writes a buffered image that is referred as a URL to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterImageURLLatLon().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the image location (upper left corner), in decimal degrees.
 * @param lon The longitude of the image location (upper left corner), in decimal degrees.
 * @param *url URL of the image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterImageURLLatLon(LinkSocket *linkSocket,
                                          double lat, double lon,
                                          char *url,
                                          LinkArgs *linkArgs);

/**
 * Writes an image in X-Y space that is referred as a URL, to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the image location (upper left corner), offset from the upper left corner of the canvas, in pixels.
 * @param y The Y coordinate of the image location, offset from the upper left corner of the canvas, in pixels.
 * @param *url The URL of the image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkRasterImageURLXY(LinkSocket *linkSocket,
                              int x, int y,
                              char *url,
                              LinkArgs *linkArgs);

/**
 * Writes a buffered image in X-Y space that is referred as a URL, to the 
 * link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterImageURLXY().
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the image location (upper left corner), offset from the upper left corner of the canvas, in pixels.
 * @param y The Y coordinate of the image location, offset from the upper left corner of the canvas, in pixels.
 * @param *url The URL of the image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterImageURLXY(LinkSocket *linkSocket,
                                      int x, int y,
                                      char *url,
                                      LinkArgs *linkArgs);
/**
 * Writes an image with an offset that is referred as a URL, to
 * the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location, offset from the anchor point, in pixels.
 * @param *url The URL of the image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkRasterImageURLOffset(LinkSocket *linkSocket,
                                  double lat, double lon, 
                                  int x, int y,
                                  char *url,
                                  LinkArgs *linkArgs);

/**
 * Writes an image with an offset that is referred as a URL, to
 * the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkRasterImageURLOffset().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of the image location (upper left corner), offset from the anchor point, in pixels.
 * @param y The Y coordinate of the image location, offset from the anchor point, in pixels.
 * @param *url The URL of the image.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkRasterImageURLOffset(LinkSocket *linkSocket,
                                          double lat, double lon, 
                                          int x, int y,
                                          char *url,
                                          LinkArgs *linkArgs);

#endif
