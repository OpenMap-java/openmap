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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/mapped_files.c,v $
 * $RCSfile: mapped_files.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* SYSTEM HEADER FILES */
#include <stdio.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

/* #define MAC_OS_X */

#ifdef MAC_OS_X
#define MAXINT INT_MAX
#else
#include <values.h>
#endif


/* TOOL HEADER FILES */
#include "compat.h"
#include "style.h"
#define DEBUG_ME "DEBUG_TOOLLIB"
#include "debugging.h"
#include "error_hand.h"

/* MATT HEADER FILES */
#include "mapped_files.h"
    
DebugVariable(mappedFiles, "MappedFiles", 0x01);

FileEntry lruTable[LRUEntries];
static Bool mappedInitialized = False;
static int lruTime = 0;

static int InitMappedFiles()
{
    int i;
    if(mappedInitialized)
        return(0);
    
    if(Debug(mappedFiles))
    {
        sprintf(msgBuf,
                "InitMappedFiles() LRU Table has %d entries", LRUEntries);
        DEBUG_MESSAGE(msgBuf);
    }
    
    for(i = 0; i < LRUEntries; i++)
    {
        lruTable[i].filename[0] = '\0';
        lruTable[i].address = (char*)0;
        lruTable[i].time = 0;
    }
    mappedInitialized = True;

    return(0);
}

Bool FileExists(char *name)
{
    struct stat statBuf;
    int result;
    
    result = stat(name, &statBuf);
    if(result < 0)
        return(False);
    else
        return(True);
}

void FlushMappedFileTable()
{
    int i;

    InitMappedFiles();
    
    for(i = 0; i < LRUEntries; i++)
    {
        if(lruTable[i].address && (lruTable[i].time != -1))
        {
            munmap(lruTable[i].address, lruTable[i].size);
            lruTable[i].address = (char*)0;
            lruTable[i].filename[0] = '\0';
        }
    }
}

int GetMappedFileSize(char *name)
{
    int i;
    
    InitMappedFiles();
    
    for(i = 0; i < LRUEntries; i++)
    {
        if(strcmp(name, lruTable[i].filename) == 0)
        {
            if(Debug(mappedFiles))
            {
                sprintf(msgBuf, "LRU Time %d Found %s in LRU cache %d bytes",
                        lruTime, name, lruTable[i].size);
                DEBUG_MESSAGE(msgBuf);
            }
            
            return(lruTable[i].size);
        }
    }
    return(-1);
}

void CloseMappedFile(FileEntry* fe) 
{
    if (Debug(mappedFiles))
    {
        sprintf(msgBuf, "Pretending to close %s in LRU cache %d bytes",
                fe->filename, fe->size);
        DEBUG_MESSAGE(msgBuf);
    }
    fe->time = 1;  /* just make it eligible for reuse.  might prevent us from
                      having to reopen the file had we really closed it*/
}

FileEntry *FEOpenMappedFile(char *name, int auto_closable)
{
    int i;
    int fd;
    int result;
    struct stat statBuf;
    int oldestTime = MAXINT;
    int oldestIndex = -1;
    char *address;
    
    InitMappedFiles();

    lruTime++;
    
    for(i = 0; i < LRUEntries; i++)
    {
        if(strcmp(name, lruTable[i].filename) == 0)
        {
            if(Debug(mappedFiles))
            {
                sprintf(msgBuf, "LRU Time %d Found %s in LRU cache",
                        lruTime, name);
                DEBUG_MESSAGE(msgBuf);
            }
            
            if (lruTable[i].time == -1)
            {
                sprintf(msgBuf, "File %s already opened!", name);
                WARNING_MESSAGE(msgBuf);
            } else
              lruTable[i].time = (auto_closable) ? lruTime : -1;
            lruTable[i].currOffset = 0;
            return(&lruTable[i]);
        }

        if ((lruTable[i].time < oldestTime) && (lruTable[i].time != -1))
        {
            oldestTime = lruTable[i].time;
            oldestIndex = i;
        }
    }

    if(Debug(mappedFiles))
    {
        if(lruTable[oldestIndex].address == (char*)0)
        {
            sprintf(msgBuf,
                    "LRU Time %d Entering %s into LRU cache in new slot %d",
                    lruTime, name, oldestIndex);
        }
        else
        {
            sprintf(msgBuf, "LRU Time %d Replacing (t=%d) %s with %s",
                    lruTime, lruTable[oldestIndex].time,
                    lruTable[oldestIndex].filename, name);
        }

        DEBUG_MESSAGE(msgBuf);
    }
    
    if(lruTable[oldestIndex].address)
    {
        munmap(lruTable[oldestIndex].address, lruTable[oldestIndex].size);
        lruTable[oldestIndex].address = (char*)0;
    }

    fd = open(name, O_RDONLY);
    if(fd < 0)
    {
        if (errno != ENOENT) { /* if we fail to access the file for a reason
                                  other than that it doesn't exist */
          sprintf(msgBuf, "Error opening %s", name);
          WARNING_PERROR(msgBuf);
        } else if (Debug(mappedFiles)) {
          sprintf(msgBuf, "Could not map %s", name);
          WARNING_PERROR(msgBuf);
        }
        return((FileEntry *) 0);
    }

    result = fstat(fd, &statBuf);
    if(result < 0)
    {
        if(Debug(mappedFiles))
        {
            sprintf(msgBuf, "Could not access %s", name);
            WARNING_PERROR(msgBuf);
        }
        close(fd);
        return((FileEntry *) 0);
    }
    
    address = (char *) mmap((caddr_t) 0, (int) statBuf.st_size, 
                            PROT_READ, MAP_SHARED, fd, (off_t) 0);
    close(fd);
    if((int) address == -1)
    {
        sprintf(msgBuf, "Failed to mmap %s, %d bytes", 
                name, (int)statBuf.st_size);
        WARNING_PERROR(msgBuf);
        return((FileEntry *) 0);
    }

    lruTable[oldestIndex].size = (int) statBuf.st_size;
    lruTable[oldestIndex].address = address;
    lruTable[oldestIndex].currOffset = 0;
    lruTable[oldestIndex].time = (auto_closable) ? lruTime : -1;
    strcpy(lruTable[oldestIndex].filename, name);
    
    return(&lruTable[oldestIndex]);
}

char *OpenMappedFile(char *name)
{
  FileEntry* mf = FEOpenMappedFile(name, 1);
  if (mf)
    return mf->address;
  return (char*)0;
}

int SeekMappedFile(FileEntry* mf, long offset, int ptrname)
{
  if (ptrname == SEEK_SET)
    mf->currOffset = offset;
  else if (ptrname == SEEK_CUR)
    mf->currOffset += offset;
  else 
    mf->currOffset = mf->size + offset;
  return 0;
}
