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
 * $Source: /cvs/distapps/openmap/src/cserver/link/src/Response.c,v $
 * $RCSfile: Response.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#include <stdio.h>
#include <stdlib.h>

#include "Response.h"
#include "Request.h"
#include "GlobalConstants.h"
#include "Link.h"

/*for debugging*/
#define DEBUG_ME "LINKSERVER"
#include <toolLib/debugging.h>
DebugVariable(LINK, "LINK", 0x01); /* setenv LINKSERVER "LINK"*/

int WriteMapResponseHeader(LinkSocket *link, LinkArgs *linkArgs){
    if (Debug(LINK))
        printf("WriteMapResponseHeader: writing MAP Response header %s\n",
               GRAPHICS_RESPONSE_HEADER);
    WriteChars(link, GRAPHICS_RESPONSE_HEADER, lGRAPHICS_RESPONSE_HEADER); 

    if (Debug(LINK))
        printf("WriteMapResponseHeader: writing version %f\n", VERSION);
    WriteFloat(link, VERSION); /*writing version 0.2*/
    if (WriteLinkArgs(link, linkArgs) == -1)
      return -1; /* Memory allocation error */
    if (Debug(LINK))
        printf("WriteMapResponseHeader: All done writing header...\n");
    return OK;
}

int WriteActionResponseHeader(LinkSocket *link, LinkArgs *linkArgs){
    if (Debug(LINK))
        printf("WriteActionResponseHeader: writing ACTION Response header %s\n",
               GESTURE_RESPONSE_HEADER);
    WriteChars(link, GESTURE_RESPONSE_HEADER, lGESTURE_RESPONSE_HEADER); 

    if (Debug(LINK))
        printf("WriteActionResponseHeader: writing version %f\n", VERSION);
    WriteFloat(link, VERSION); /*writing version 0.2*/
    if (WriteLinkArgs(link, linkArgs) == -1)
      return -1; /* Memory allocation error */
    return OK;
}

void EndSection(LinkSocket *link){
    WriteChars(link, END_SECTION_STRING, 1); /*nothing more for now*/
    Socketflush(link);

    if (Debug(LINK))
        printf("EndSection: Wrote section end symbol and flushed socket...\n");
}

void EndTotal(LinkSocket *link){
    WriteChars(link, END_TOTAL_STRING, 1); /*nothing more for now*/
    Socketflush(link);

    if (Debug(LINK))
        printf("EndSection: Wrote total end symbol and flushed socket...\n");
}

