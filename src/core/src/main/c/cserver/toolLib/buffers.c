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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/buffers.c,v $
 * $RCSfile: buffers.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* SYSTEM HEADER FILES */
#include <stdio.h>
#include <string.h>

/* #define MAC_OS_X */

#ifndef MAC_OS_X
#include <malloc.h>
#endif

/* TOOL HEADER FILES */
#include "compat.h"
#include "style.h"
#include "error_hand.h"
#include "Magic.h"
#define DEBUG_ME        "DEBUG_TOOLLIB"
#include "debugging.h"

#ifdef SVR4
#include <memory.h>
#define bzero(addr, n)  memset(addr, 0, n)
#define bcopy(from, to, n) memcpy(to, from, n)
#endif

DebugVariable(buffers, "buffers", 0x01);

/* LOCAL HEADER FILES */
#define BuffersPrivate
#include "buffers.h"

static int initialized = 0;

/*
 * NOTE: This struct should be kept to be a multiple of 4 bytes long to
 * ensure decent memory alignment of the data portion that is returned
 * to the user. Otherwise, bad things will happen. InitBuffers watches for
 * that, and, thankfully (maybe) some compilers pad structs anyway.
 *
 */

typedef struct _BufHeader
{
    Magic               magic1;
    int                 size;
    struct _BufHeader   *next;
    char                *data;
    Magic               *magic3;
    Magic               magic2;
} BufHeader;

static int seq = 0;

static Magic bufMagic1;
static Magic bufMagic2;
static Magic bufMagic3;

#define BufHeaderOk(b)  ((b)->magic1 == bufMagic1 && (b)->magic2 == bufMagic2 \
                         && *((b)->magic3) == bufMagic3)

#define NoBufHeader ((BufHeader *) 0)

static BufHeader *freeBufList = NoBufHeader;
static BufHeader *inUseList   = NoBufHeader;

/*
 * Keep some stats that we can get at with saber or dbx
 */

static int freeBufs   = 0;
static int inUseBufs  = 0;
static int freeBytes  = 0;
static int inUseBytes = 0;

char bufferMsg[1024];

void DumpBuffers()
{
    printf("Buffers: count %d/%d, size %d/%d\n",
           inUseBufs, (freeBufs + inUseBufs),
           inUseBytes,(freeBytes + inUseBytes));
}

/* ------------------------------------------------------------------------
 * 
 * PutBufOnFreeList()
 *
 *      Given a BufHeader pointer, insert the buffer into the
 *      free list. Right now the free list is ordered in terms
 *      of increasing size but there is nothing saying that there
 *      should not be separate free lists for each buffer size or
 *      some other scheme which could be more efficient.
 * 
 * RETURNS:  Nothing.
 *           
 * ------------------------------------------------------------------------ */

static void PutBufOnFreeList(BufHeader *buf)
{
    BufHeader **temp;
    BufHeader *entry;

    if(initialized == 0) InitBuffers();
    
    entry = freeBufList;
    temp  = &freeBufList;
    

    freeBufs++;
    inUseBufs--;
    freeBytes  += buf->size;
    inUseBytes -= buf->size;

    if(Debug(buffers))
    {
        seq++;
        sprintf(msgBuf,
        "Buffers  %06d FREE 0x%08x %06d: F %02d, U %02d, Fb %05d, Ub %05d %s",
                seq, buf,
                buf->size, freeBufs, inUseBufs, freeBytes, inUseBytes, bufferMsg);
        DEBUG_MESSAGE(msgBuf);
    }
    
    while(entry)
    {
        if(entry->size >= buf->size)
        {
            *temp     = buf;
            buf->next = entry;
            return;
        }
        temp  = &(entry->next);
        entry = entry->next;
    }

    *temp     = buf;
    buf->next = NoBufHeader;
}


/* ------------------------------------------------------------------------
 * 
 * FreeBuffer()
 *
 *      Given a buffer pointer, figure out if it really originated from
 *      this module and if so, put it back on the free list.
 * 
 * RETURNS:  -1 on error
 *           
 * ------------------------------------------------------------------------ */

int DebugFreeBuffer(
                    char *buf,
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

    sprintf(bufferMsg, "%14s, %04d", fileName, line);
    return(FreeBuffer(buf));
}

/*------------------------------------------------------------------------
 * IsABuffer()
 *
 * Note that this is likely to return the right answer since three
 * magic numbers get checked, each of which is a 32 bit unique number.
 * Two of the numbers are at fixed locations above the buffer's storage,
 * one is at the end of the storage.
 *
 * RETURN:
 *      False   definitely not a buffer, ok to use free(2) on it
 *              assuming the choices where whether this was malloc'ed storage
 *              or buffer space.
 *
 *      True    Highly likely it's a buffer.
 *------------------------------------------------------------------------*/

Bool IsABuffer(char *buf)
{
    BufHeader *thisBuf;

    if(initialized == 0) InitBuffers();

    if(buf == (char *) 0)
    {
        return(False);
    }

    /*
     * buf points to the location inside the BufHeader struct. Back up
     * to the beginning of the struct and then test to see that
     * all the magic numbers are there. If they are not, then
     * either 'buf' was not generated in this module or the user of buf
     * trashed it by using more than was allowed.
     */

    thisBuf = (BufHeader *)((unsigned long) buf - sizeof(BufHeader));

    if(!BufHeaderOk(thisBuf))
        return(False);

    return(True);
}

int FreeBuffer(char *buf)
{
    BufHeader *thisBuf;
    BufHeader **temp;
    BufHeader *entry;

    if(initialized == 0) InitBuffers();

    if(buf == (char *) 0)
    {
        return(0);
    }

    /*
     * buf points to the location inside the BufHeader struct. Back up
     * to the beginning of the struct and then test to see that
     * all the magic numbers are there. If they are not, then
     * either 'buf' was not generated in this module or the user of buf
     * trashed it by using more than was allowed.
     */

    thisBuf = (BufHeader *)((unsigned long) buf - sizeof(BufHeader));

    if(!BufHeaderOk(thisBuf))
    {
        sprintf(msgBuf, "FreeBuf(0x%x) corrupted buffer found", buf);
        WARNING_MESSAGE(msgBuf);
        return(-1);
    }
    
    entry = inUseList;
    temp  = &inUseList;
    
    while(entry)
    {
        if(entry->data == buf)
        {
            *temp = entry->next;
            PutBufOnFreeList(entry);
            return(entry->size);
        }
        temp  = &(entry->next);
        entry = entry->next;
    }

    return(0);
}


/* ------------------------------------------------------------------------
 * 
 * IncreaseBufferSize()
 *
 *      Given a pointer to a buffer, and a new size request, decide
 *      whether the buffer really was big enough all along (in which case
 *      just hand it back to the caller) or whether a new one has to
 *      be found (in which case the old contents are copied into the 
 *      new one) and handed back.
 * 
 * RETURNS:  The pointer to the new buffer and also the actual size if
 *           the actualSizePtr is non-null.
 *           
 * ------------------------------------------------------------------------ */

char *DebugIncreaseBufferSize(
                              char *buf,
                              int size,
                              int *asp,
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

    sprintf(bufferMsg, "%14s, %04d", fileName, line);
    return(IncreaseBufferSize(buf, size, asp));
}

char *IncreaseBufferSize(
                         char *buffer,
                         int size,
                         int *actualSizePtr)
{
    BufHeader *thisBuf;
    char *newBuffer;

    if(initialized == 0) InitBuffers();

    /*
     * Check to see if there is a real buffer here
     */

    if(buffer)
    {
        thisBuf = (BufHeader *)((unsigned long) buffer - sizeof(BufHeader));

        if(!BufHeaderOk(thisBuf))
        {
            sprintf(msgBuf,
                    "IncreaseBufferSize(0x%x) corrupted buffer found", buffer);
            WARNING_MESSAGE(msgBuf);
            return((char *) 0);
        }

        /*
         * It turns out that this one is big enough.
         */
        if(thisBuf->size >= size)
        {
            if(actualSizePtr)
                *actualSizePtr = thisBuf->size;
            return(buffer);
        }
    }
    
    /*
     * Either there was not original buffer or the original was too small
     */

    newBuffer = GetNewBufferAndSize(size, actualSizePtr);

    /*
     * If we actually got a new one, and there actually was an old one,
     * copy the contents of the old one into the new one. Free the old one.
     *
     * Note: Don't free the old one if we failed to get a new one.
     * Our callers might get miffed it we don't give them back a new
     * buffer AND we blow away their old data!
     */

    if(newBuffer && buffer)
    {
        bcopy(buffer, newBuffer, thisBuf->size);
        FreeBuffer(buffer);
    }

    /*
     * return the new one. If it was bogus, this will return the
     * NULL on up to the caller
     */

    return(newBuffer);
}


/* ------------------------------------------------------------------------
 * 
 * GetNewBufferAndSize()
 *
 *      Note the companion macro in buffers.h called
 *      GetNewBuffer() which is the usual one to call.
 * 
 *      Looks for the first buffer that is at least big enough
 *      to handle the size needed and returns the data portion
 *      of that buffer.
 * 
 * RETURNS:  pointer to new buffer and size of buffer if actualSizePtr
 *           is non-null.
 *           
 * ------------------------------------------------------------------------ */

char *DebugGetNewBufferAndSize(
                               int size,
                               int *asp,
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

    sprintf(bufferMsg, "%14s, %04d", fileName, line);
    return(GetNewBufferAndSize(size, asp));
}

char *GetNewBufferAndSize(
                          int size,
                          int *actualSizePtr)
{
    int allocSize;
    
    BufHeader **temp;
    BufHeader *entry = NoBufHeader;
    
    if(initialized == 0) InitBuffers();

    entry = freeBufList;
    temp = &freeBufList;
        
    if(size > BufMaxAlloc)
    {
        sprintf(msgBuf, "GetNewBufferAndSize(%d) exceeds max of %d",
                size, BufMaxAlloc);
        return((char *) 0);
    }
    
    while(entry)
    {
        if(entry->size >= size)
        {
            /*
             * Let's make sure that we don't give a ridiculously
             * large buffer out just because it's the next one
             * available!
             */

            if(entry->size > BufMinAlloc && entry->size > size * OversizeLimit)
            {
                break;
            }
            
            *temp       = entry->next;
            entry->next = inUseList;
            inUseList   = entry;
            
            if(actualSizePtr)
                *actualSizePtr = entry->size;
            
            freeBufs--;
            inUseBufs++;
            freeBytes  -= entry->size;
            inUseBytes += entry->size;

            if(Debug(buffers))
            {
                seq++;
                sprintf(msgBuf,
          "Buffers  %06d GET  0x%08x %06d: F %02d, U %02d, Fb %05d, Ub %05d %s",
                        seq, entry, entry->size, 
                        freeBufs, inUseBufs, freeBytes, inUseBytes, bufferMsg);
                DEBUG_MESSAGE(msgBuf);
            }
            return(entry->data);
        }
        temp  = &(entry->next);
        entry = entry->next;
    }

    /*
     * Try to figure out the next power of 2 that will satisfy
     * this size request.
     */
    for(allocSize = BufMinAlloc; allocSize <= BufMaxAlloc; allocSize <<= 1)
    {
        if(allocSize >= size)
            break;
    }
    
    entry = (BufHeader *) malloc(sizeof(BufHeader) + sizeof(Magic) +
                                 allocSize);
    
    if(entry == NoBufHeader)
    {
        NoMemory("GetNewBufferAndSize", 
                 sizeof(BufHeader) + sizeof(Magic) + allocSize);
        return((char *) 0);
    }

    entry->size   = allocSize;
    entry->data   = &((char *) entry)[sizeof(BufHeader)];
    entry->magic3 = (Magic *) &((char *) entry)[sizeof(BufHeader) + allocSize];

    entry->magic1    = bufMagic1;
    entry->magic2    = bufMagic2;
    *(entry->magic3) = bufMagic3;

    entry->next = inUseList;
    inUseList   = entry;

    inUseBufs++;
    inUseBytes += entry->size;

    if(actualSizePtr)
        *actualSizePtr = entry->size;

    if(Debug(buffers))
    {
        seq++;
        sprintf(msgBuf, 
        "Buffers  %06d GET  0x%08x %06d: F %02d, U %02d, Fb %05d, Ub %05d %s",
                seq, entry, entry->size, 
                freeBufs, inUseBufs, freeBytes, inUseBytes, bufferMsg);
        DEBUG_MESSAGE(msgBuf);
    }
    
    return(entry->data);
}


/* ------------------------------------------------------------------------
 * 
 * InitBuffers()
 *
 *      Call this first!
 * 
 * RETURNS:  -1 if failure for some reason.
 *           
 * ------------------------------------------------------------------------ */

int InitBuffers()
{
    
    if(initialized)
        return(0);
    
    initialized = 1;
    
    /*
     * This little test is here to warn against a compiler that
     * does not automatically pad structs to a quad byte alignment.
     */

    /* SUPPRESS 558 *//* CodeCenter conditional expression always false */
    if(sizeof(BufHeader) % 4)
    {
        /* NOTREACHED *//* Since the compiler can figure this out */
        sprintf(msgBuf, "sizeof(BufHeader) is %d, it MUST be a multiple of 4",
                sizeof(BufHeader));
        
        FATAL_MESSAGE(msgBuf);
        return(-1);
    }
    
    bufMagic1 = NewMagicNumber("Buf Magic 1");
    bufMagic2 = NewMagicNumber("Buf Magic 2");
    bufMagic3 = NewMagicNumber("Buf Magic 3");

    bufferMsg[0] = '\0';
    
    return(0);
}

