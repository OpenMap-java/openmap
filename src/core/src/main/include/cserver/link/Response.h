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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/Response.h,v $
 * $RCSfile: Response.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef RESPONSE_H
#define RESPONSE_H

#include "Request.h"
#include "LinkArgs.h"

/**
 * Writes the graphics response header to the link socket.
 *
 * @param *link The link socket.
 * @param *linkArgs The link arguments.
 * @returns -1 if there was a memory allocation error.
 */


int WriteMapResponseHeader(LinkSocket *link, LinkArgs *linkArgs);

/**
 * Writes the action response header to the link socket.
 *
 * @param *link The link socket.
 * @param *linkArgs the link arguments.
 * @returns -1 if there was a memory allocation error.
 */

int WriteActionResponseHeader(LinkSocket *link, LinkArgs *linkArgs);

/**
 * Signals the end of a section.
 *
 * @param *link The link socket.
 *
 */


void EndSection(LinkSocket *link);

/**
 * Signals the end of the transmission.
 *
 * @param *link The link socket.
 *
 */

void EndTotal(LinkSocket *link);

#endif
