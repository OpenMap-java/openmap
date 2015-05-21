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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/free_mgr.c,v $
 * $RCSfile: free_mgr.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
/*
 *      CodeCenter/ObjectCenter compile and runtime warning suppression
 */
/* SUPPRESS 112 *//* Retrieving a <xxx> from <yyy> ... */
/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

/* System Header Files */
#include <stdio.h>
#include <string.h>

/* local header files */
#include "compat.h"
#include "style.h"
#include "error_hand.h"
#define FreeListPrivate
#include "free_mgr.h"

#define DEBUG_ME        "DEBUG_TOOLLIB"
#include "debugging.h"

DebugVariable(freelist, "freelist", 0x01);

static char freeListMagicNumber1[] = "FreeListMagic1";
static char freeListMagicNumber2[] = "FreeListMagic2";

static FreeList freeLists = 
{ 
    freeListMagicNumber1,               /* magic1 */
    "MasterFreeList",                   /* name */
    (char *) 0,                         /* list */
    OffsetInStruct(FreeList, next),     /* offsetToNext */
    10,                                 /* reallocSize */
    sizeof(FreeList),                   /* itemSize */
    0,                                  /* freeItems */
    0,                                  /* inUseItems */
    NoFreeList,                         /* next */
    freeListMagicNumber2                /* magic2 */
};

static int ReallocateFreeList(
#if NeedFunctionPrototypes
 FreeList *                     /* freeList */
#endif
);

static char errorString[256];
static char debugMsg[256];
static int seq = 0;
static int initialized = 0;
static int checkFree = 1;

char *PrintFreeList(FreeList *freeList)
{
    static char buf[80];
    sprintf(buf, "%-20s free %-6d used %-6d size %-5d bytes %-6d", 
            freeList->name, freeList->freeItems,
            freeList->inUseItems, freeList->itemSize,
            freeList->inUseItems * freeList->itemSize);
    return(buf);
}

void DumpFreeLists()
{
    FreeList *list;
    
    for(list = &freeLists; list; list = list->next)
    {
        printf("%s\n", PrintFreeList(list));
    }
}


/*+------------------------------------------------------------------------
 *      ManageFreeList()
 *
 *              Tells the FreeList manager about a list type to manage.
 *
 *      RETURNS:
 *              FreeList handle to use for later reference to the list if ok,
 *              NULL if not.
 *________________________________________________________________________*/

FreeList *DebugManageFreeList(
                              unsigned long offsetToNext,
                              int reallocSize,
                              int itemSize,
                              const char *name,
                              const char *file,
                              int line)
{
    char *fileName;

    /*
     * Find the file name minus the preceding path name
     */

    fileName = strrchr(file, '/');
    if(fileName == NULL)
        fileName = (char *) file;
    else
        fileName++;

    initialized = 1;
    sprintf(debugMsg, "%14s, %04d", fileName, line);
    return(ManageFreeList(offsetToNext, reallocSize, itemSize, name));
}


FreeList *ManageFreeList(
                         unsigned long offsetToNext,
                         int reallocSize,
                         int itemSize,
                         const char *name)
{
    int result;
    FreeList *freeList;
    
    if(!initialized)
        debugMsg[0] = '\0';
    
    if ( CorruptedFreeList(&freeLists) )
    {
        FATAL_MESSAGE("Corrupted free list");
        return(NoFreeList);
    }
    
    freeList = (FreeList *)GetFromFreeList(&freeLists);
    if(freeList == NoFreeList)
    {
        FATAL_MESSAGE("Can't get item from master free list");
        return(NoFreeList);
    }

    freeList->list         = (char *) 0;
    freeList->name         = (char *) name;
    freeList->offsetToNext = offsetToNext;
    freeList->reallocSize  = reallocSize;
    freeList->itemSize     = itemSize;
    freeList->freeItems    = 0;
    freeList->inUseItems   = 0;
    freeList->next         = freeLists.next;
    freeList->magic1       = freeListMagicNumber1;
    freeList->magic2       = freeListMagicNumber2;
    freeLists.next         = freeList;

    if(Debug(freelist))
    {
        seq++;
        sprintf(msgBuf, 
                "FreeList %06d MANAGE  %20s                          %s", 
                seq, freeList->name,
                debugMsg);
        DEBUG_MESSAGE(msgBuf);
    }
    

    result = ReallocateFreeList(freeList);
    if(result == -1)
    {
        FATAL_MESSAGE("Can't get initial entry for new free list");
        return(NoFreeList);
    }
    
    return(freeList);
}

/*+---------------------------------------------------------------------
 *    GetFromFreeList()
 * 
 *    RETURNS:
 *      Pointer to object of the right size for the type given
 *      NULL if there is an error
 *---------------------------------------------------------------------*/

char *DebugGetFromFreeList(
                           FreeList *freeList,
                           const char *file,
                           int line)
{
    char *fileName;

    /*
     * Find the file name minus the preceding path name
     */

    fileName = strrchr(file, '/');
    if(fileName == NULL)
        fileName = (char *) file;
    else
        fileName++;

    sprintf(debugMsg, "%14s, %04d", fileName, line);
    return(GetFromFreeList(freeList));
}

char *GetFromFreeList(FreeList *freeList)
{
    int result;
    unsigned long base;
    char **next;
    
    if(freeList == NoFreeList)
    {
        WARNING_MESSAGE("GetFromFreeList() -- Null freeList argument");
        return((char *) 0);
    }

    if( CorruptedFreeList(freeList) )
    {
        sprintf(errorString, "Corrupted free list at 0x%x", freeList);
        FATAL_MESSAGE(errorString);
        return((char *) 0);
    }    

    if (freeList->list == (char *) 0)
    {
        result = ReallocateFreeList(freeList);
        if(result == -1)
        {
            WARNING_MESSAGE("Can't realloc free list");
            return((char *) 0);
        }
    }

    /* 
     * base will point to the base of an 'itemSize' chunk of the newly
     *      allocated memory area,
     * next will point to the place where the structure element called
     *      'next' would be if we were dealing with an actual user
     *      structure rather than a hunk of memory 
     */

    base = (unsigned long)freeList->list;
    next = (char **)(base + freeList->offsetToNext);
    freeList->list = *next;
    *next = (char *) 0;

    freeList->inUseItems++;
    freeList->freeItems--;
    
    if(Debug(freelist))
    {
        seq++;
        sprintf(msgBuf, "FreeList %06d GET     %20s 0x%08x %06d %06d %s",
                seq, freeList->name, base, freeList->inUseItems,
                freeList->freeItems, debugMsg);
        DEBUG_MESSAGE(msgBuf);
    }
    
    return((char *)base);
}

/*+------------------------------------------------------------------------
 *      ReturnListToFreeList()
 *
 *              Puts an item or list of items back onto the free list.
 *              The last item's next pointer must be NULL.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if the given type was not found or the list was NULL
 *________________________________________________________________________*/
int DebugReturnListToFreeList(
                              char *list,
                              FreeList *freeList,
                              const char *file,
                              int line)
{
    char *fileName;

    /*
     * Find the file name minus the preceding path name
     */

    fileName = strrchr(file, '/');
    if(fileName == NULL)
        fileName = (char *) file;
    else
        fileName++;

    sprintf(debugMsg, "%14s, %04d", fileName, line);
    return(ReturnListToFreeList(list, freeList));
}

int ReturnListToFreeList(char *list, FreeList *freeList)
{
    unsigned long base;
    char **next;
    
    if(freeList == NoFreeList)
        return(-1);
    
    if( CorruptedFreeList(freeList) )
    {
        sprintf(errorString, "Corrupted free list at 0x%x", freeList);
        FATAL_MESSAGE(errorString);
        return(-1);
    }

    if(list == (char *) 0)
        return(-1);
    
    /* 
     * base will point the the base of an 'itemSize' chunk of the 
     *      returned list
     * next will point to the place where the structure element called
     *      'next' would be if we were dealing with an actual user
     *      structure rather than a hunk of memory 
     */

    base = (unsigned long)list;
    next = (char **)(base + freeList->offsetToNext);

    /*
     * Find the end of the user's list
     */

    while(*next != (char *) 0)
    {
        if(checkFree && CheckIfAlreadyFree((char *) base, freeList))
            continue;

        freeList->inUseItems--;
        freeList->freeItems++;
        
        if(Debug(freelist))
        {
            seq++;
            sprintf(msgBuf, 
                    "FreeList %06d FREE    %20s 0x%08x %06d %06d %s",
                    seq, freeList->name, base, freeList->inUseItems,
                    freeList->freeItems, debugMsg);
            DEBUG_MESSAGE(msgBuf);
        }
        
        base = (unsigned long) *next;
        next = (char **)(base + freeList->offsetToNext);
    }

    if(!(checkFree && CheckIfAlreadyFree((char *) base, freeList)))
    {
        freeList->inUseItems--;
        freeList->freeItems++;

        if(Debug(freelist))
        {
            seq++;
            sprintf(msgBuf, "FreeList %06d FREE    %20s 0x%08x %06d %06d %s",
                    seq, freeList->name, base, freeList->inUseItems,
                    freeList->freeItems, debugMsg);
            DEBUG_MESSAGE(msgBuf);
        }
        
        *next = freeList->list;
        freeList->list = list;
    }
    return(0);
}

int CheckIfAlreadyFree(char *element, FreeList *freeList)
{
    unsigned long base;
    char **next;
    
    if(freeList == NoFreeList)
        return(-1);
    
    if( CorruptedFreeList(freeList) )
    {
        sprintf(errorString, "Corrupted free list at 0x%x", freeList);
        FATAL_MESSAGE(errorString);
        return(-1);
    }

    if(element == (char *) 0)
        return(-1);
    
    base = (unsigned long)(freeList->list);
    next = (char **)(base + freeList->offsetToNext);

    /*
     * Find the end of the user's list
     */

    while(base)
    {
        if((char *) base == element)
            return(1);
        
        base = (unsigned long) *next;
        next = (char **)(base + freeList->offsetToNext);
    }

    return(0);
}

/*+------------------------------------------------------------------------
 *      ReturnToFreeList()
 *
 *              Puts an item back onto the free list.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if the given type was not found or the list was NULL
 *________________________________________________________________________*/
int DebugReturnToFreeList(
                          char *listElement,
                          FreeList *freeList,
                          const char *file,
                          int line)
{
    char *fileName;

    /*
     * Find the file name minus the preceding path name
     */

    fileName = strrchr(file, '/');
    if(fileName == NULL)
        fileName = (char *) file;
    else
        fileName++;

    sprintf(debugMsg, "%14s, %04d", fileName, line);
    return(ReturnToFreeList(listElement, freeList));
}

int ReturnToFreeList(
                     char *listElement,
                     FreeList *freeList)
{
    char **next;
    
    if(freeList == NoFreeList)
        return(-1);
    
    if( CorruptedFreeList(freeList) )
    {
        sprintf(errorString, "Corrupted free list at 0x%x", freeList);
        FATAL_MESSAGE(errorString);
        return(-1);
    }

    if(listElement == (char *) 0)
        return(-1);
    
    /* 
     * next will point to the place where the structure element called
     *      'next' would be if we were dealing with an actual user
     *      structure rather than a hunk of memory 
     */

    if(!(checkFree && CheckIfAlreadyFree(listElement, freeList)))
    {
        freeList->inUseItems--;
        freeList->freeItems++;

        if(Debug(freelist))
        {
            seq++;
            sprintf(msgBuf,
                    "FreeList %06d FREE    %20s 0x%08x %06d %06d %s",
                    seq, freeList->name, listElement, freeList->inUseItems,
                    freeList->freeItems, debugMsg);
            DEBUG_MESSAGE(msgBuf);
        }
    

        next = (char **)(listElement + freeList->offsetToNext);
        *next = freeList->list;
        freeList->list = listElement;
    }

    return(0);
}

/*+---------------------------------------------------------------------
 *    ReallocateFreeList()
 * 
 *    RETURNS:
 *            0 if successful
 *            -1 if unsuccessful
 *---------------------------------------------------------------------*/

static int ReallocateFreeList(FreeList *freeList)
{
    int i;
    unsigned long base;
    char **next;
    
    if(freeList == NoFreeList)
        return(-1);

    if( CorruptedFreeList(freeList) )
    {
        sprintf(errorString, "Corrupted free list at 0x%x", freeList);
        FATAL_MESSAGE(errorString);
        return(-1);
    }

    if (freeList->list == (char *) 0)
    {
        freeList->list = 
             malloc((int)(freeList->reallocSize) *
                    (int)(freeList->itemSize));
        if (freeList->list == (char *) 0)
        {
            sprintf(errorString,
                    "ReallocateFreeList: Failed to allocate for list 0x%x",
                    freeList);
            FATAL_MESSAGE(errorString);
            return(-1);
        }

        /* 
         * base will point the the base of an 'itemSize' chunk of the newly
         *      allocated memory area,
         * next will point to the place where the structure element called
         *      'next' would be if we were dealing with an actual user
         *      structure rather than a hunk of memory 
         */

        base = (unsigned long)freeList->list;
        for (i = 0 ; i < freeList->reallocSize; i++)
        {
            freeList->freeItems++;
            
            if(Debug(freelist))
            {
                seq++;
                sprintf(msgBuf,
                        "FreeList %06d REALLOC %20s 0x%08x %06d %06d %s",
                        seq, freeList->name, base, freeList->inUseItems,
                        freeList->freeItems, debugMsg);
                DEBUG_MESSAGE(msgBuf);
            }
        
            next = (char **)(base + freeList->offsetToNext);
            base = (base + freeList->itemSize);
            *next = (char *) base;
        }
        *next = (char *) 0;

        return(0);
    }
    return(0);
}
