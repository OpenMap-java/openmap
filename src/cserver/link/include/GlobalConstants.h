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
 * $Source: /cvs/distapps/openmap/src/cserver/link/include/GlobalConstants.h,v $
 * $RCSfile: GlobalConstants.h,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:48 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef GLOBALCONSTANTS_H
#define GLOBALCONSTANTS_H

/*
  Definitions of Constants
*/
#define LINK_TRUE 1
#define LINK_FALSE 0
#define OK 0
#define NOK 1

#define N_CHARS_PER_UNICODE_CHAR 2
#define N_BYTES_PER_INTEGER 4
#define N_BYTES_PER_FLOAT 4
#define N_BIT_PER_CHAR 8

#define COLORBLACK 0xFF000000
#define DEFAULT_LINE_WIDTH 1

#define HEADERSUCCESS 'S'
#define HEADERERROR 'E'
#define MEMORYERROR 'M'
#define MAX_SOCKET_BUFFER_SIZE 4096

#endif
