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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/auto_input.h,v $
 * $RCSfile: auto_input.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */


/* 
 *      This module provides an automated socket input facility.
 *
 *      Callers specify how much data they want to get and what
 *      callback function to call when that amount of data has been
 *      gotten for them.
 *
 *      These functions allocate the data buffers with the xxbufs
 *      module but DO NOT FREE the data buffers.
 *
 *      It is up to the client to free the buffers. There will always
 *      only be one buffer per input file descriptor, and its address
 *      is always passed to the callback functions.
 *
 *      Callback functions are called with a success flag so they
 *      can figure out if there was a socket error or not.
 *
 * ------------------------------------------------------------------------ */

#ifndef _auto_input_include
#define _auto_input_include

#include "compat.h"


typedef int (*AutoInputBufCBProc)(
#if NeedFunctionPrototypes
 char *,                        /* clientData */
 char *,                        /* data */
 int,                           /* bytesInBuffer */
 int,                           /* bufferSize */
 int                            /* Success */
#endif
);              /* pointer to a procedure */

BEGIN_extern_C

extern int InitAutoInputBufs(
#if NeedFunctionPrototypes
#endif
);
 
extern int CancelAutoInput(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
);
 
extern int ResumeAutoInput(
#if NeedFunctionPrototypes
 int,                           /* fd */
 int,                           /* bytesNeeded */
 char *,                        /* clientData */
 AutoInputBufCBProc,            /* callback */
 const char *                   /* callbackName */
#endif
);

extern int AutoInputLine(
#if NeedFunctionPrototypes
 int,                           /* fd */
 char *,                        /* clientData */
 AutoInputBufCBProc,            /* callback */
 const char *                   /* callbackName */
#endif
);

extern int StartAutoInput(
#if NeedFunctionPrototypes
 int,                           /* fd */
 int,                           /* bytesNeeded */
 char *,                        /* clientData */
 AutoInputBufCBProc,            /* callback */
 const char *                   /* callbackName */
#endif
);

extern void FlushAutoInput(
#if NeedFunctionPrototypes
 int fd
#endif
);

END_extern_C
#endif
