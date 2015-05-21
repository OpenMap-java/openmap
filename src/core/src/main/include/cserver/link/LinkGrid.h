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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkGrid.h,v $
 * $RCSfile: LinkGrid.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKGRID_H
#define LINKGRID_H

#include "LinkArgs.h"
#include "LinkSocket.h"
#include "Request.h"

/*Prototypes for Writing Grid Object*/

/*
  Buffered objects write everything to a chunk of memory and 
  write the entire memory to socket at one short
*/

/**
 * Writes a grid object in lat-long space to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param orientation
 * @param vResolution
 * @param HResolution
 * @param major
 * @param data
 * @param linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkGridLatLon(LinkSocket *linkSocket, 
                        double lat, double lon, 
                        int rows, int columns, double orientation,
                        double vResolution, double hResolution,
                        int major, int* data,
                        LinkArgs *linkArgs);

/**
 * Writes a grid object in lat-long space with the data passed as a short
 * to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param *lat The latitude of the anchor point, in degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param orientation
 * @param vResolution
 * @param HResolution
 * @param major
 * @param data
 * @param linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int WriteLinkGridLatLonS(LinkSocket *linkSocket, 
                         double lat, double lon, 
                         int rows, int columns, double orientation,
                         double vResolution, double hResolution,
                         int major, short* data,
                         LinkArgs *linkArgs);

/**
 * Writes a buffered grid object in lat-long space to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkGridLatLon().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param orientation
 * @param vResolution
 * @param HResolution
 * @param major
 * @param data
 * @param linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int BufferedWriteLinkGridLatLon(LinkSocket *linkSocket, 
                                double lat, double lon, 
                                int rows, int columns, double orientation,
                                double vResolution, double hResolution,
                                int major, int* data,
                                LinkArgs *linkArgs);

/**
 * Writes a grid object in X-Y space to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the anchor point, in pixels from the top left corner of the canvas.
 * @param y The Y coordinate of the anchor point, in pixels from the top left corner of the canvas.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param orientation
 * @param vResolution
 * @param HResolution
 * @param major
 * @param data
 * @param linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkGridXY(LinkSocket *linkSocket,  
                    int x, int y,
                    int rows, int columns, double orientation,
                    double vResolution, double hResolution,
                    int major, int* data,
                    LinkArgs *linkArgs);

/**
 * Writes a buffered grid object in X-Y space to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkGridXY().
 *
 * @param *linkSocket The link socket.
 * @param x The X coordinate of the anchor point, in pixels from the top left corner of the canvas.
 * @param y The Y coordinate of the anchor point, in pixels from the top left corner of the canvas.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param orientation
 * @param vResolution
 * @param HResolution
 * @param major
 * @param data
 * @param linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkGridXY(LinkSocket *linkSocket,  
                            int x, int y,
                            int rows, int columns, double orientation,
                            double vResolution, double hResolution,
                            int major, int* data,
                            LinkArgs *linkArgs);


/**
 * Writes a grid object with an offset to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point.
 * @param lon The longitude of the anchor point.
 * @param x The X coordinate of the top left corner of the grid, offset from the anchor point, in pixels.
 * @param y The Y coordinate of the top left corner of the grid, offset from the anchor point, in pixels.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param orientation
 * @param vResolution
 * @param hResolution
 * @param major
 * @param data
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error. 
 */

 
int WriteLinkGridOffset(LinkSocket *linkSocket,
                        double lat, double lon,
                        int x, int y,                      
                        int rows, int columns, double orientation,
                        double vResolution, double hResolution,
                        int major, int* data,
                        LinkArgs *linkArgs);
/**
 * Writes a buffered grid object with an offset to the link socket.
 * <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkGridOffset().
 *
 * @param *linkSocket The link socket.
 * @param lat The latitude of the anchor point.
 * @param lon The longitude of the anchor point.
 * @param x The X coordinate of the top left corner of the grid, offset from the anchor point, in pixels.
 * @param y The Y coordinate of the top left corner of the grid, offset from the anchor point, in pixels.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param orientation
 * @param vResolution
 * @param hResolution
 * @param major
 * @param data
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int BufferedWriteLinkGridOffset(LinkSocket *linkSocket,
                                double lat, double lon,
                                int x, int y,                      
                                int rows, int columns, double orientation,
                                double vResolution, double hResolution,
                                int major, int* data,
                                LinkArgs *linkArgs);

#endif
