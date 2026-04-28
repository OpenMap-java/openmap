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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/plumbing.h,v $
 * $RCSfile: plumbing.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

#ifndef _plumbing_h
#define _plumbing_h

#include "compat.h"

BEGIN_extern_C

typedef int (*FdCBProc)(
#if NeedFunctionPrototypes
 int,                           /* fd */
 char *                         /* client_data */
#endif
);              /* pointer to a procedure */
#define NoFdCBProc      ((FdCBProc) 0)

extern void StopLoopOnFds(
#if NeedFunctionPrototypes
#endif
); 

extern int LoopOnFds(
#if NeedFunctionPrototypes
#endif
); 

extern int PrintTrace(
#if NeedFunctionPrototypes
 int,                           /* fd */
 char *,                        /* buf */
 int,                           /* nBytes */
 char                           /* direction */
#endif
);

extern int InitFds(
#if NeedFunctionPrototypes
#endif
); 

extern int InitTkFds(
#if NeedFunctionPrototypes
#endif
); 

extern void WeedOutBadFds(
#if NeedFunctionPrototypes
#endif
); 

extern int ConnectInputFd(
#if NeedFunctionPrototypes
 int,                           /* fd */
 FdCBProc,                      /* callback */
 const char *,                  /* callbackName */
 char *                         /* clientData */
#endif
); 

extern int ConnectOutputFd(
#if NeedFunctionPrototypes
 int,                           /* fd */
 FdCBProc,                      /* callback */
 const char *,                  /* callbackName */
 char *                         /* clientData */
#endif
); 

extern int SetTraceFd(
#if NeedFunctionPrototypes
 int,                           /* fd */
 int,                           /* flag */
 int                            /* direction */
#endif
); 

extern int GetTraceFd(
#if NeedFunctionPrototypes
 int,                           /* fd */
 int                            /* direction */
#endif
); 

extern void DisconnectInputFd(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
); 

extern void DisableInputFd(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
); 

extern void EnableInputFd(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
); 

extern void DisconnectOutputFd(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
); 

extern void DisableOutputFd(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
); 

extern void EnableOutputFd(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
); 

END_extern_C

#endif
