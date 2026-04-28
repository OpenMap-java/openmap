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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/free_mgr.h,v $
 * $RCSfile: free_mgr.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* 
 *      General Purpose free list management functions. This module 
 *      takes care of pre-allocating a block of memory and then handing
 *      it out in the size chunks specified. This allows more efficient use
 *      of malloc and prevents memory fragmentation. The drawback is that
 *      the memory involved is never freed again. When items are handed
 *      back to the free list, they are made available to be used again.
 *
 *
 * Functions consist of 
 *
 *      FreeList *ManageFreeList(offsetToNext, reallocSize, itemSize, name)
 *      unsigned long offsetToNext;
 *      int reallocSize;
 *      int itemSize;
 *      char *name;
 *
 *      Tells the free list manager to manage a pool of memory to be
 *      parcelled out in 'itemSize' chunks. The pool will contain
 *      'reallocSize' elements and when the pool runs out, that many
 *      more elements will be allocated. The 'offsetToNext' argument
 *      is used if you have linked lists of structures and want to return
 *      a list rather than single elements using ReturnListToFreeList()
 *      
 *
 *      char *GetFromFreeList(freeList)
 *      FreeList *freeList;
 *
 *      Returns a pointer to a properly sized element. The element is not
 *      zeroed before you get it.
 *
 *
 *      int ReturnToFreeList(listElement, freeList)
 *      char *listElement;
 *      FreeList *freeList;
 *
 *      Hand a single element back to the free list pool.
 *
 *
 *      int ReturnListToFreeList(list, freeList)
 *      char *list;
 *      FreeList *freeList;
 *
 *      Hand a list of elements back to the free list pool. This only
 *      works if you specified a valid 'offsetToNext' in the
 *      ManageFreeList() call. The last pointer must be NULL!
 *
 * Macros consist of
 *
 *      OffsetInStruct(cast, element)
 *      cast is any 'typedef' or structure tag
 *      element is a member of the structure.
 *
 * ------------------------------------------------------------------------ */

#ifndef free_mgr_include
#define free_mgr_include

#include "compat.h"

BEGIN_extern_C

#ifndef OffsetInStruct
#define OffsetInStruct(cast, element) \
    ((unsigned long)(&(((cast *)(0))->element)))
#endif

#define CorruptedFreeList(listp) ((listp)->magic1 != freeListMagicNumber1 \
                                  ||(listp)->magic2 != freeListMagicNumber2)

typedef struct freeListStruct
{
    char                *magic1;
    char                *name;
    char                *list;
    unsigned long       offsetToNext;
    int                 reallocSize;
    int                 itemSize;
    int                 freeItems;
    int                 inUseItems;
    struct freeListStruct *next;
    char                *magic2;
} FreeList;
#define NoFreeList ((FreeList *) 0)

extern FreeList *DebugManageFreeList(
#if NeedFunctionPrototypes
 unsigned long,                 /* offsetToNext */
 int,                           /* reallocSize */
 int,                           /* itemSize */
 const char *,                  /* name */
 const char *,                  /* file */
 int                            /* line */
#endif
);
 
extern FreeList *ManageFreeList(
#if NeedFunctionPrototypes
 unsigned long,                 /* offsetToNext */
 int,                           /* reallocSize */
 int,                           /* itemSize */
 const char *                   /* name */
#endif
);
 
extern char *DebugGetFromFreeList(
#if NeedFunctionPrototypes
 FreeList *,                    /* freeList */
 const char *,                  /* file */
 int                            /* line */
#endif
);

extern char *GetFromFreeList(
#if NeedFunctionPrototypes
 FreeList *                     /* freeList */
#endif
);

extern int DebugReturnListToFreeList(
#if NeedFunctionPrototypes
 char *,                        /* list */
 FreeList *,                    /* freeList */
 const char *,                  /* file */
 int                            /* line */
#endif
);

extern int ReturnListToFreeList(
#if NeedFunctionPrototypes
 char *,                        /* list */
 FreeList *                     /* freeList */
#endif
);

extern int DebugReturnToFreeList(
#if NeedFunctionPrototypes
 char *,                        /* listElement */
 FreeList *,                    /* freeList */
 const char *,                  /* file */
 int                            /* line */
#endif
);

extern int ReturnToFreeList(
#if NeedFunctionPrototypes
 char *,                        /* listElement */
 FreeList *                     /* freeList */
#endif
);

extern int CheckIfAlreadyFree(
#if NeedFunctionPrototypes
 char *element, 
 FreeList *freeList
#endif
);


/*
 *
 * Always name free lists by prepending the type name to _FreeList
 * for these macros to work. E.g.
 *
 * FreeList *Foo_FreeList;      for free lists with Foo type elements.
 */


#ifndef FreeListPrivate
#ifdef DebugFreeListPrintout
#define NewListPool(type, n, next) \
    DebugManageFreeList(OffsetInStruct(type, next), (n), sizeof(type), \
                        "type", __FILE__, __LINE__);
#define NewPool(type, n) \
    DebugManageFreeList(0L, (n), sizeof(type), "type", __FILE__, __LINE__);
#define New(type) \
    (type *) DebugGetFromFreeList(STRCAT(type,_FreeList), __FILE__, __LINE__);
#define Delete(elt, type) \
    DebugReturnToFreeList((char *) elt,  STRCAT(type,_FreeList), \
                          __FILE__, __LINE__);
#define DeleteList(elt, type) \
    DebugReturnListToFreeList((char *) elt, STRCAT(type,_FreeList), \
                              __FILE__, __LINE__);
#else

#define NewListPool(type, n, next) \
    ManageFreeList(OffsetInStruct(type, next), (n), sizeof(type), "type");
#define NewPool(type, n) ManageFreeList(0L, (n), sizeof(type), "type");
    
#define New(type)       \
    (type *) GetFromFreeList(STRCAT(type,_FreeList))
#define Delete(elt, type) \
         ReturnToFreeList((char *) elt, STRCAT(type,_FreeList))
#define DeleteList(elt, type) \
         ReturnListToFreeList((char *) elt, STRCAT(type,_FreeList))

#endif
#endif

END_extern_C

#endif

