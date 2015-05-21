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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/LinkText.h,v $
 * $RCSfile: LinkText.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:09 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef LINKTEXT_H
#define LINKTEXT_H

#include "LinkSocket.h"
#include "Request.h"
#include "LinkArgs.h"

#define DEFAULT_FONT "-*-new century schoolbook-*-*-*-*-14-*-*-*-*-*-*-*"

/*Prototypes for Writing Text Object*/

/** 
 * Writes text to the link socket.
 *
 * @param *link The link socket.
 * @param lat The latitude of where the text should be drawn, in decimal degrees.
 * @param lon The longitude of where the text should be drawn, in decimal degrees.
 * @param numberOfCharsInString The number of characters in the string.
 * @param *string The string of text.
 * @param *fontAsString
 * @param justification The justification of the string.  Possible values are JUSTIFY_LEFT, JUSTIFY_CENTER, and JUSTIFY_RIGHT.  (See Link.h)
 * @param *linkArgs The link arguments.
 * @param is_unicode 1 if the string is already in Unicode.
 * @param num_unicode_chars If the string is in Unicode, the number of characters the string takes.
 * @returns OK if successful.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkTextLatLon(LinkSocket *link,
                        double lat, double lon,
                        char *String, char *fontAsString,
                        int justification, LinkArgs *linkArgs, int is_unicode,
                        int num_unicode_chars);

/**
 * Writes text in X-Y space to the link socket.
 *
 * @param *link The link socket.
 * @param x The X coordinate of where the text should be drawn, offset from the upper left corner of the canvas, in pixels.
 * @param y The Y coordinate of where the text should be drawn, offset from the upper left corner of the canvas, in pixels.
 * @param numberOfCharsInString The number of characters in the string.
 * @param fontAsString
 * @param justification The justification of the string.  Possible values are JUSTIFY_LEFT, JUSTIFY_CENTER, and JUSTIFY_RIGHT.  (See Link.h)
 * @param *linkArgs The link arguments.
 * @param is_unicode 1 if the string is already in Unicode.
 * @param num_unicode_chars If the string is Unicode, how many characters the string takes up.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkTextXY(LinkSocket *link, int x, int y, 
                    char *String, char *fontAsString,
                    int justification, LinkArgs *linkArgs, int is_unicode,
                    int num_unicode_chars);

/**
 * Writes text with an offset to the link socket.
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of where the text should be drawn, offset from the anchor point, in pixels.
 * @param y The Y coordinate of where the text should be drawn, offset from the anchor point, in pixels.
 * @param numberOfCharsInString The number of characters in the string.
 * @param *fontAsString
 * @param justification The justification of the string.  Possible values are JUSTIFY_LEFT, JUSTIFY_CENTER, and JUSTIFY_RIGHT.  (See Link.h)
 * @param *linkArgs The link arguments.
 * @param is_unicode 1 if the string is already in Unicode.
 * @param num_unicode_chars If the string is in Unicode, the number of characters the string takes.
 * @returns -1 if there was a memory allocation error.
 */

int WriteLinkTextOffset(LinkSocket *link,
                        double lat, double lon, int x, int y,
                        char *String, char *fontAsString,
                        int justification, LinkArgs *linkArgs, int is_unicode,
                        int num_unicode_chars);

/**
 * Writes buffered text to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkTextLatLon().
 *
 * @param *link The link socket.
 * @param lat The latitude of where the text should be drawn, in decimal degrees.
 * @param lon The longitude of where the text should be drawn, in decimal degrees.
 * @param numberOfCharsInString The number of characters in the string.
 * @param *string The string of text.
 * @param *fontAsString
 * @param justification The justification of the string.  Possible values are JUSTIFY_LEFT, JUSTIFY_CENTER, and JUSTIFY_RIGHT.  (See Link.h)
 * @param *linkArgs The link arguments.
 * @param is_unicode 1 if the string is already in Unicode.
 * @param num_unicode_chars If the string is in Unicode, the number of characters the string takes.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkTextLatLon(LinkSocket *link,
                                double lat, double lon,
                                char *String, char *fontAsString,
                                int justification, LinkArgs *linkArgs,
                                int is_unicode, int num_unicode_chars);


/**
 * Writes buffered text in X-Y space to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkTextXY().
 *
 * @param *link The link socket.
 * @param x The X coordinate of where the text should be drawn, offset from the upper left corner of the canvas, in pixels.
 * @param y The Y coordinate of where the text should be drawn, offset from the upper left corner of the canvas, in pixels.
 * @param numberOfCharsInString The number of characters in the string.
 * @param fontAsString
 * @param justification The justification of the string.  Possible values are JUSTIFY_LEFT, JUSTIFY_CENTER, and JUSTIFY_RIGHT.  (See Link.h)
 * @param *linkArgs The link arguments.
 * @param is_unicode 1 if the string is already in Unicode.
 * @param num_unicode_chars If the string is in Unicode, the number of characters the string takes.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkTextXY(LinkSocket *link, int x, int y, 
                            char *String, char *fontAsString,
                            int justification, LinkArgs *linkArgs, 
                            int is_unicode, int num_unicode_chars);


/**
 * Writes buffered text with an offset to the link socket. <b>DEPRECATED</b>
 *
 * <b>Deprecated</b>: Use WriteLinkTextOffset().
 *
 * @param *link The link socket.
 * @param lat The latitude of the anchor point, in decimal degrees.
 * @param lon The longitude of the anchor point, in decimal degrees.
 * @param x The X coordinate of where the text should be drawn, offset from the anchor point, in pixels.
 * @param y The Y coordinate of where the text should be drawn, offset from the anchor point, in pixels.
 * @param numberOfCharsInString The number of characters in the string.
 * @param *fontAsString
 * @param justification The justification of the string.  Possible values are JUSTIFY_LEFT, JUSTIFY_CENTER, and JUSTIFY_RIGHT.  (See Link.h)
 * @param *linkArgs The link arguments.
 * @param is_unicode 1 if the string is already in Unicode.
 * @param num_unicode_chars If the string is in Unicode, the number of characters it takes.
 * @returns -1 if there was a memory allocation error.
 */

int BufferedWriteLinkTextOffset(LinkSocket *link,
                                double lat, double lon, int x, int y,
                                char *String, char *fontAsString,
                                int justification, LinkArgs *linkArgs,
                                int is_unicode, int num_unicode_chars);

#endif
