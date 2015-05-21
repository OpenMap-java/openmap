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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/buffers.h,v $
 * $RCSfile: buffers.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */


/*
 *      This module handles memory buffer pools where the buffers are
 *      always sized in powers of 2.
 *
 *      GetNewBufferAndSize() returns a pointer to a buffer of at least
 *                            the requested size and returns also the
 *                            actual size of the buffer.
 *
 *      FreeBuffer()          Takes a buffer that was created by
 *                            GetNewBufferAndSize and returns it back to
 *                            the buffer pool.
 *
 *      GetNewBuffer()        Is a macro in buffers.h that simply calls
 *                            GetNewBufferAndSize without requiring the
 *                            actual size argument for convenience.
 *
 *      This module will use malloc() to get more memory to put into
 *      the buffer pool but will never call free(). It always tries to
 *      satisfy new buffer requests from its pool of free buffers.
 *      The constraints are set up so that the buffers are always
 *      between BufMinAlloc and BufMaxAlloc and that a given buffer is
 *      never more than OversizeLimit bigger than the requested size.
 *
 *      That is so that a usefule sized buffer is created even when a tiny
 *      one is requested. In the world of the X Protocol, 32 bytes is
 *      the best minimum size since events and a lot of requests and replies
 *      fit into that size.
 *
 *      The OversizeLimit prevents a small request from chewing up a large
 *      buffer since that could cause a lot of large buffers to be created
 *      and then be underutilized.
 * 
 * ------------------------------------------------------------------------ */

#ifndef buffers_include
#define buffers_include

#include "compat.h"
#include "style.h"

#define BufMinAlloc     32              /* Must be power of 2 */
#define BufMaxAlloc     1024 * 1024     /* Must be power of 2 */

/*
 * The OversizeLimit is a multiplier used to limit the magnitude of
 * the returned buffer size. If a buffer of size N is requested,
 * the returned buffer will be at most
 *   (first power of 2 >=  min(N, BufMinAlloc)) * OversizeLimit
 * bytes long. e.g if you request a buffer of 500 bytes,
 * the biggest you'll get back is 1024 bytes.
 */

#define OversizeLimit   2               /* Must be power of 2 that's >= 2 */

#define GetNewBuffer(size)      GetNewBufferAndSize((size), 0)


BEGIN_extern_C

Bool IsABuffer(
#if NeedFunctionPrototypes
 char *                         /* buf */
#endif
);

int FreeBuffer(
#if NeedFunctionPrototypes
 char *                         /* buf */
#endif
);

char *GetNewBufferAndSize(
#if NeedFunctionPrototypes
 int,                           /* size */
 int *                          /* actual size */
#endif
);

char *IncreaseBufferSize(
#if NeedFunctionPrototypes
 char *,                        /* buffer */
 int,                           /* size */
 int *                          /* actual size */
#endif
);

int DebugFreeBuffer(
#if NeedFunctionPrototypes
 char *,                        /* buf */
 const char *,                  /* file */
 int                            /* line */
#endif
);

char *DebugGetNewBufferAndSize(
#if NeedFunctionPrototypes
 int,                           /* size */
 int *,                         /* actual size */
 const char *,                  /* file */
 int                            /* line */
#endif
);

char *DebugIncreaseBufferSize(
#if NeedFunctionPrototypes
 char *,                        /* buffer */
 int,                           /* size */
 int *,                         /* actual size */
 const char *,                  /* file */
 int                            /* line */
#endif
);

int InitBuffers(
#if NeedFunctionPrototypes
#endif
);

#ifndef BuffersPrivate
#ifdef DebugBufferPrintout
#define FreeBuffer(a)                DebugFreeBuffer((a), __FILE__, __LINE__)
#define GetNewBufferAndSize(a, b)    DebugGetNewBufferAndSize((a), (b), __FILE__, __LINE__)
#define IncreaseBufferSize(a, b, c)  DebugIncreaseBufferSize((a), (b), (c), __FILE__, __LINE__)
#endif
#endif

END_extern_C

#endif
