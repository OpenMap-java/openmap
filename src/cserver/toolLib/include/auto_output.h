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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/auto_output.h,v $
 * $RCSfile: auto_output.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef _auto_output_include
#define _auto_output_include

#include "compat.h"


typedef int (*AutoOutputBufCBProc)(
#if NeedFunctionPrototypes
 char *,                        /* clientData */
 int,                           /* bytesAlreadySent */
 char *,                        /* data */
 int                            /* Success */
#endif
);              /* pointer to a procedure */

BEGIN_extern_C

extern int InitAutoOutputBufs(
#if NeedFunctionPrototypes
#endif
);
 
extern int CancelAutoOutput(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
);
 
extern int StartAutoOutput(
#if NeedFunctionPrototypes
 int,                           /* fd */
 char *,                        /* data */
 int,                           /* bytesToSend */
 char *,                        /* clientData */
 AutoOutputBufCBProc,           /* callback */
 const char *                   /* callbackName */
#endif
);

extern void FlushAutoOutput(
#if NeedFunctionPrototypes
 int fd
#endif
);

END_extern_C

#endif
