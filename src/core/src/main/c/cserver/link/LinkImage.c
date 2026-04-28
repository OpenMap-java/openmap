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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkImage.c,v $
 * $RCSfile: LinkImage.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include "LinkImage.h"
#include "LinkSocket.h"
#include "GlobalConstants.h"

int WriteDirectImage(LinkSocket *linkSocket, DirectImage *directimage)
{

    int check= OK,i; 
    /*Write number of pixels a client is supposed to read*/ 
    check = check || WriteInteger(linkSocket, directimage->numberOfPixels);
  
    /*Write the values*/

    /*An optimized version. Write the entire buffer instead of writing every integer*/
    /* check = check || WriteChars(linkSocket,(char *)directimage->image, 
       directimage->numberOfPixels*N_BYTES_PER_INTEGER); */
    for(i=0;i<directimage->numberOfPixels;i++)
    {
        check = check || WriteInteger(linkSocket, directimage->image[i]);
    }
    return check;
}

int BufferedWriteDirectImage(char *toBuffer, DirectImage *directimage)
{
    int byteswritten = 0,i;
    /*Write number of pixels a client is supposed to read*/ 
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         directimage->numberOfPixels);
  
    /*Write the values*/
    /*An optimized version. Write the entire buffer instead of writing every integer*/
    /*byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
      (char *)directimage->image, 
      directimage->numberOfPixels*N_BYTES_PER_INTEGER);*/

    for(i=0;i<directimage->numberOfPixels;i++)
    {
        byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                             directimage->image[i]);
    }
    return byteswritten;
}

int GetDirectImageSize(DirectImage *directimage)
{
    return N_BYTES_PER_INTEGER + /*Bytes used by nmberOfPixels(an Integer)*/
        N_BYTES_PER_INTEGER*directimage->numberOfPixels; /*Bytes used by pixels*/
}

int WriteIndexedImage(LinkSocket *linkSocket, IndexedImage *indexedimage)
{
    int check = OK,i;
    /* Write number of pixels*/
    check = check || WriteInteger(linkSocket, indexedimage->numberOfPixels);

    /*Write image*/
    check = check || WriteChars(linkSocket, indexedimage->image,
                                indexedimage->numberOfPixels);

    /*Write color table size*/
    check = check || WriteInteger(linkSocket, indexedimage->colorTableSize);
    /*Write Color table*/
    /*The optimized version*/
    /*check = check || WriteChars(linkSocket, indexedimage->colorTable,
      indexedimage->colorTableSize*N_BYTES_PER_INTEGER);*/

    for(i=0;i<indexedimage->colorTableSize;i++)
    {
        check = check || WriteInteger(linkSocket, indexedimage->colorTable[i]);
    }
    return check;
}


int BufferedWriteIndexedImage(char *toBuffer, IndexedImage *indexedimage)
{
    int byteswritten = 0,i;

    /* Write number of pixels*/
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         indexedimage->numberOfPixels);
    /*Write image*/
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       indexedimage->image, indexedimage->numberOfPixels);
    /*Write color table size*/
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                         indexedimage->colorTableSize);
    /*Write Color table*/
    /*The optimized version*/
    /*byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
      (char *)indexedimage->colorTable,
      indexedimage->colorTableSize*N_BYTES_PER_INTEGER);*/

    for(i=0;i<indexedimage->colorTableSize;i++)
    {
        byteswritten += BufferedWriteInteger(&toBuffer[byteswritten],
                                             indexedimage->colorTable[i]);
    }
    return byteswritten;
}

int GetIndexedImageSize(IndexedImage *indexedimage)
{
    return N_BYTES_PER_INTEGER + /*Bytes used by numberOfPixels(an Integer)*/
        indexedimage->numberOfPixels + /*each pixel is 1 byte */
        N_BYTES_PER_INTEGER + /* Bytes used by colorTableSize */
        N_BYTES_PER_INTEGER*indexedimage->colorTableSize + 
        N_BYTES_PER_INTEGER; /*Bytes used by transparency(an integer)*/
}





