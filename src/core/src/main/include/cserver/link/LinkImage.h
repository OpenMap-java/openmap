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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkImage.h,v $
 * $RCSfile: LinkImage.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKIMAGE_H
#define LINKIMAGE_H

#include "LinkSocket.h"

/*
  Definations of Objects for sending Images/pics in different ways to client
*/

/**
 * A raw image object.
 *
 * @param numberOfPixels The number of pixels in the image.
 * @param *image The image.
 */

struct DirectImage 
{
    int numberOfPixels;
    int *image;
};
typedef struct DirectImage DirectImage;

/**
 * Sends images (pixel values) directly as integers to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param *directimage The direct image.
 */

int WriteDirectImage(LinkSocket *linkSocket, DirectImage *directimage);

/**
 * Sends buffered images (pixel values) directly as integers to the link 
 * socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteDirectImage().
 *
 * @param *linkSocket The link socket.
 * @param *directimage The direct image.
 */

int BufferedWriteDirectImage(char *toBuffer, DirectImage *directimage);

/**
 * Returns the memory required to store this direct image, in bytes.
 *
 * @param *directimage The direct image.
 * @returns Memory required to store the direct image, in bytes.
 */

int GetDirectImageSize(DirectImage *directimage);

/*---------------------------------------------------------------------------*/
/*
  Sends Pixel values in a color table and send image as a series of bytes,
  each byte containing an index on color table that contains actual pixel value
*/

/**
 * The indexed image object.
 *
 * @param numberOfPixels The number of pixels in the object.
 * @param *image An array of bytes, each byte contains an index for a color table.
 * @param colorTableSize Size of the color table.
 * @param *colorTable The color table.
 * @param transparency The transparency of the image.
 */


struct IndexedImage
{
    int numberOfPixels;
    char *image;    /*An array of bytes, each byte contains an index for color table*/
    
    int colorTableSize;
    int *colorTable; /*Each entry contains a pixel value*/

    int transparency;
};
typedef struct IndexedImage IndexedImage;

/**
 * Writes an indexed image to the link socket.
 *
 * @param *linkSocket The link socket.
 * @param *indexedimage The indexed image.
 */
int WriteIndexedImage(LinkSocket *linkSocket, IndexedImage *indexedimage);

/**
 * Writes a buffered, indexed image to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteIndexedImage().
 *
 * @param *linkSocket The link socket.
 * @param *indexedimage The indexed image.
 */

int BufferedWriteIndexedImage(char *toBuffer, IndexedImage *indexedimage);

/**
 * Returns memory size in bytes required to store the direct image.
 *
 * @param *indexedimage The indexed image.
 * @returns Memory required to store the direct image, in bytes.
 */

int GetIndexedImageSize(IndexedImage *indexedimage);

#endif 
