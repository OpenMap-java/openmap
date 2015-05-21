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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/LinkText.c,v $
 * $RCSfile: LinkText.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdlib.h>
#include <stdio.h>

#include "LinkText.h"
#include "Link.h"
#include "GlobalConstants.h"
#include "Response.h"
#include "Request.h"

int WriteTextHeader(LinkSocket *linkSocket)
{
    int check = OK;
    check = check || WriteChars(linkSocket, TEXT_HEADER, lTEXT_HEADER);
    check = check || WriteInteger(linkSocket, GRAPHICTYPE_TEXT);
    return check;
}


int WriteLinkTextLatLon(LinkSocket *linkSocket, 
                        double lat, double lon,
                        char *String, char *fontAsString,
                        int justification,
                        LinkArgs *linkArgs, int is_unicode, 
                        int num_unicode_chars)
{
    int check = OK;

    /** Add font and string to properties. */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTSTRING, String, is_unicode, num_unicode_chars);
    if (check == -1)
      return -1; /* Memory allocation error */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTFONT, fontAsString, 0, 0);
    if (check == -1)
      return -1; /* Memory allocation error */

    check = check || WriteTextHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_LATLON);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, justification);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int WriteLinkTextXY(LinkSocket *linkSocket,
                    int x, int y, 
                    char *String, char *fontAsString,
                    int justification,
                    LinkArgs *linkArgs, int is_unicode, int num_unicode_chars)
{
    int check = OK;  

    /** Add font and string to properties. */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTSTRING, String, is_unicode, num_unicode_chars);
    if (check == -1)
      return -1; /* Memory allocation error */

    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTFONT, fontAsString, 0, 0);
    if (check == -1)
      return -1; /* Memory allocation error */

    check = check || WriteTextHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_XY);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, justification);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}


int WriteLinkTextOffset(LinkSocket *linkSocket,
                        double lat, double lon,
                        int x, int y,
                        char *String, char *fontAsString,
                        int justification,                 
                        LinkArgs *linkArgs, int is_unicode, 
                        int num_unicode_chars)
{
    int check = OK;

    /** Add font and string to properties. */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTSTRING, String, is_unicode, num_unicode_chars);
    if (check == -1)
      return -1; /* Memory allocation error */
    check = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTFONT, fontAsString, 0, 0);
    if (check == -1)
      return -1; /* Memory allocation error */
    
    check = check || WriteTextHeader(linkSocket);
    check = check || WriteInteger(linkSocket, RENDERTYPE_OFFSET);
    check = check || WriteFloat(linkSocket,(float)lat);
    check = check || WriteFloat(linkSocket,(float)lon);
    check = check || WriteInteger(linkSocket, x);
    check = check || WriteInteger(linkSocket, y);
    check = check || WriteInteger(linkSocket, justification);
    check = check || WriteLinkArgs(linkSocket, linkArgs);
    return check;
    /* Returns -1 if there was a memory allocation error in WriteLinkArgs() */
}

int BufferedWriteTextHeader(char *toBuffer)
{
    int byteswritten = 0;
    
    byteswritten += BufferedWriteChars(&toBuffer[byteswritten],
                                       TEXT_HEADER, lTEXT_HEADER);
    byteswritten += BufferedWriteInteger(&toBuffer[byteswritten], GRAPHICTYPE_TEXT);
    return byteswritten;
}


int BufferedWriteLinkTextLatLon(LinkSocket *linkSocket, 
                                double lat, double lon,
                                char *String, char *fontAsString,
                                int justification,
                                LinkArgs *linkArgs, int is_unicode,
                                int num_unicode_chars)
{
    int buffercount;
    char *buffer;
    int retval;

    /** Add font and string to properties. */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTSTRING, String, 
                                       is_unicode, num_unicode_chars);
    if (retval == -1)
      return -1; /* Memory allocation error */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTFONT, fontAsString, 0, 0);
    if (retval == -1)
      return -1; /* Memory allocation error */

    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lTEXT_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        N_BYTES_PER_INTEGER +   /*Bytes used by justification*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteTextHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_LATLON);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);    
    buffercount += BufferedWriteInteger(&buffer[buffercount], justification);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}


int BufferedWriteLinkTextXY(LinkSocket *linkSocket, int x, int y, 
                            char *String, char *fontAsString,
                            int justification, LinkArgs *linkArgs, 
                            int is_unicode, int num_unicode_chars)
{
    int buffercount;
    char *buffer;
    int retval;

    /** Add font and string to properties. */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTSTRING, String, 
                                       is_unicode, num_unicode_chars);
    if (retval == -1)
      return -1; /* Memory allocation error */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTFONT, fontAsString, 0, 0);
    if (retval == -1)
      return -1; /* Memory allocation error */

    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lTEXT_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        3*N_BYTES_PER_INTEGER +   /*Bytes used by x,y,justification*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteTextHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_XY);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], justification);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}


int BufferedWriteLinkTextOffset(LinkSocket *linkSocket,
                                double lat, double lon, int x, int y,
                                char *String, char *fontAsString,
                                int justification, LinkArgs *linkArgs,
                                int is_unicode, int num_unicode_chars)
{
    int buffercount;
    char *buffer;
    int retval;
    
    /** Add font and string to properties. */
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTSTRING, String, is_unicode, num_unicode_chars);
    if (retval == -1)
      return -1; /* Memory allocation error */
    
    retval = SetKeyValuePairInLinkArgs(linkArgs, LPC_LINKTEXTFONT, fontAsString, 0, 0);
    if (retval == -1)
      return -1; /*Memory allocation error */
    
    /*
      buffercount corresponds to data being written in bytes below.
    */
    buffercount = lTEXT_HEADER +  /* Bytes used by Header*/
        N_BYTES_PER_INTEGER +     /*Bytes used by GRAPHIC_TYPE. It is an Integer*/
        N_BYTES_PER_INTEGER +     /*Bytes used by RENDERTYPE identifier*/
        2*N_BYTES_PER_FLOAT +     /*Bytes used by lat and lon*/
        3*N_BYTES_PER_INTEGER +   /*Bytes used by x,y,justification*/
        LinkSizeOfLinkArgs(linkArgs);
    
    buffer = (char *)malloc(buffercount);
    if (buffer == NULL)
      return -1; /* Memory allocation error */

    buffercount=0;
    
    buffercount += BufferedWriteTextHeader(&buffer[buffercount]);
    buffercount += BufferedWriteInteger(&buffer[buffercount], RENDERTYPE_OFFSET);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lat);
    buffercount += BufferedWriteFloat(&buffer[buffercount],(float)lon);
    buffercount += BufferedWriteInteger(&buffer[buffercount], x);
    buffercount += BufferedWriteInteger(&buffer[buffercount], y);
    buffercount += BufferedWriteInteger(&buffer[buffercount], justification);
    buffercount += BufferedWriteLinkArgs(&buffer[buffercount], linkArgs);
    
    retval = WriteChars(linkSocket, buffer, buffercount);         
    free(buffer);
    return retval;
}
