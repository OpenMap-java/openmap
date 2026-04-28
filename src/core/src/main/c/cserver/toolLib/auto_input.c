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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/auto_input.c,v $
 * $RCSfile: auto_input.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* SYSTEM HEADER FILES */
#ifdef _AIX
#include <sys/select.h>
#endif
#include <sys/types.h>
#include <errno.h>
#include <stdio.h> 

/* TOOL HEADER FILES */
#include "compat.h"
#include "style.h"

#define DEBUG_ME        "DEBUG_TOOLLIB"
#include "debugging.h"
#include "error_hand.h"

/* LOCAL HEADER FILES */
#include "plumbing.h"
#include "buffers.h"
#include "auto_input.h"
#include "sockets.h"

DebugVariable(autoinput, "autoinput", 0x01);

/*
 * From sys/types.h
 */

#define MaxFds          FD_SETSIZE
#define LegalFd(fd)     ((fd) >= 0 && (fd) < MaxFds)

typedef enum _InputType
{
    InputType_Unknown = 0,
    InputType_Count = 1,
    InputType_Line  = 2
} InputType;

typedef struct autoInputBuf
{
    char        *data;

    int         fd;
    
    InputType   type;
    int         bytesNeeded;
    int         bytesInBuffer;
    int         bufSize;
    int         resumeFlag;
    char        *clientData;
    
    AutoInputBufCBProc callback;
    const char  *callbackName;
} AutoInputBuf;

static AutoInputBuf ibufs[MaxFds];
static int initialized = 0;

static int AutoFill(
#if NeedFunctionPrototypes
 int fd,
 char *clientData
#endif
);

int InitAutoInputBufs()
{
    int fd;
    
    if(initialized != 0)
        return(NormalReturn);

    initialized = 1;
    
    for(fd = 0; fd < MaxFds; fd++)
    {
        ibufs[fd].fd            = -1;
        ibufs[fd].type          = InputType_Unknown;
        ibufs[fd].data          = (char *) 0;
        ibufs[fd].bytesNeeded   = 0;
        ibufs[fd].bytesInBuffer = 0;
        ibufs[fd].bufSize       = 0;
        ibufs[fd].resumeFlag    = 0;
        ibufs[fd].clientData    = (char *) 0;
        ibufs[fd].callback      = (AutoInputBufCBProc) 0;
        ibufs[fd].callbackName  = "No Input Callback";
    }

    return(NormalReturn);
}

int CancelAutoInput(int fd)
{
    int bytesInBuffer;

    if(initialized == 0) InitAutoInputBufs();
    
    if(Debug(autoinput))
    {
        sprintf(msgBuf, "CancelAutoInput(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }

    if(!LegalFd(fd))
    {
        if(Debug(autoinput))
        {
            sprintf(msgBuf, "fd %d out of range (0, %d)", fd, MaxFds);
            DEBUG_MESSAGE(msgBuf);
        }
        
        return(ErrorReturn);
    }
    
    bytesInBuffer = ibufs[fd].bytesInBuffer;

    DisconnectInputFd(fd);
    
    ibufs[fd].fd = -1;
    
    return(bytesInBuffer);
}

void FlushAutoInput(int fd)
{
    CancelAutoInput(fd);
    FreeBuffer(ibufs[fd].data);
    ibufs[fd].data = (char *) 0;
}


int ResumeAutoInput(
                    int fd,
                    int bytesNeeded,
                    char *clientData,
                    AutoInputBufCBProc callback,
                    const char *callbackName)
{
    if(initialized == 0) InitAutoInputBufs();

    if(Debug(autoinput))
    {
        sprintf(msgBuf, "ResumeAutoInput(%d, %d, 0x%x, 0x%x, %s)",
                fd, bytesNeeded, clientData, callback,
                callbackName);
        DEBUG_MESSAGE(msgBuf);
    }
    
    if(!LegalFd(fd))
    {
        if(Debug(autoinput))
        {
            sprintf(msgBuf, "fd %d out of range (0, %d)", fd, MaxFds);
            DEBUG_MESSAGE(msgBuf);
        }
        
        return(ErrorReturn);
    }
    
    if(ibufs[fd].fd == -1)
    {
        sprintf(msgBuf, "ResumeAutoInput fd % is not active", fd);
        DEBUG_MESSAGE(msgBuf);
        return(ErrorReturn);
    }
    
    ibufs[fd].bytesNeeded   = bytesNeeded;
    ibufs[fd].clientData    = clientData;
    ibufs[fd].callback      = callback;
    ibufs[fd].callbackName  = callbackName;
    ibufs[fd].resumeFlag    = 1;
    
    ConnectInputFd(fd, AutoFill, "AutoFill", (char *) 0);

    return(fd);
}

int AutoInputLine(
                  int fd,
                  char *clientData,
                  AutoInputBufCBProc callback,
                  const char *callbackName)
{
    char *data;
    int bytesNeeded = 1;
    
    if(initialized == 0) InitAutoInputBufs();

    if(Debug(autoinput))
    {
        sprintf(msgBuf, "AutoInputLine(%d, 0x%x, 0x%x, %s)",
                fd, clientData, callback, callbackName);
        DEBUG_MESSAGE(msgBuf);
    }
    
    if(!LegalFd(fd))
    {
        if(Debug(autoinput))
        {
            sprintf(msgBuf, "fd %d out of range (0, %d)", fd, MaxFds);
            DEBUG_MESSAGE(msgBuf);
        }
        
        return(ErrorReturn);
    }

    data = GetNewBufferAndSize(bytesNeeded, &(ibufs[fd].bufSize));
    if(data == (char *) 0)
    {
        NoMemory("StartAutoInput", bytesNeeded);
        return(ErrorReturn);
    }
    
    ibufs[fd].fd            = fd;
    ibufs[fd].type          = InputType_Line;
    ibufs[fd].data          = data;
    ibufs[fd].bytesNeeded   = bytesNeeded;
    ibufs[fd].bytesInBuffer = 0;
    ibufs[fd].clientData    = clientData;
    ibufs[fd].callback      = callback;
    ibufs[fd].callbackName  = callbackName;
    ibufs[fd].resumeFlag    = 0;

    ConnectInputFd(fd, AutoFill, "AutoFill", (char *) 0);
    
    return(NormalReturn);
}

int StartAutoInput(
                   int fd,
                   int bytesNeeded,
                   char *clientData,
                   AutoInputBufCBProc callback,
                   const char *callbackName)
{
    char *data;

    if(initialized == 0) InitAutoInputBufs();

    if(Debug(autoinput))
    {
        sprintf(msgBuf, "StartAutoInput(%d, %d, 0x%x, 0x%x, %s)",
                fd, bytesNeeded, clientData, callback,
                callbackName);
        DEBUG_MESSAGE(msgBuf);
    }
    
    if(!LegalFd(fd))
    {
        if(Debug(autoinput))
        {
            sprintf(msgBuf, "fd %d out of range (0, %d)", fd, MaxFds);
            DEBUG_MESSAGE(msgBuf);
        }
        
        return(ErrorReturn);
    }

    data = GetNewBufferAndSize(bytesNeeded, &(ibufs[fd].bufSize));
    if(data == (char *) 0)
    {
        NoMemory("StartAutoInput", bytesNeeded);
        return(ErrorReturn);
    }
    
    ibufs[fd].fd            = fd;
    ibufs[fd].type          = InputType_Count;
    ibufs[fd].data          = data;
    ibufs[fd].bytesNeeded   = bytesNeeded;
    ibufs[fd].bytesInBuffer = 0;
    ibufs[fd].clientData    = clientData;
    ibufs[fd].callback      = callback;
    ibufs[fd].callbackName  = callbackName;
    ibufs[fd].resumeFlag    = 0;

    ConnectInputFd(fd, AutoFill, "AutoFill", (char *) 0);
    
    return(NormalReturn);
}

/* ARGSUSED */
static int AutoFill(
                    int fd,
                    char *clientData)
{
    int status;
    int result;
    int bytesStillNeeded;
    int bytesToRead;
    int bytes_received;
    
    do
    {
        /*
         * Make sure the buffer is big enough
         */

        if(ibufs[fd].bytesNeeded > ibufs[fd].bufSize)
        {
            ibufs[fd].data = IncreaseBufferSize(ibufs[fd].data, 
                                                ibufs[fd].bytesNeeded,
                                                &(ibufs[fd].bufSize));
            if(ibufs[fd].data == (char *) 0)
            {
                if(Debug(autoinput))
                {
                    sprintf(msgBuf,
                            "AutoFill(%d ...) NoMem for %d bytes, calling %s",
                            fd, ibufs[fd].bytesNeeded, ibufs[fd].callbackName);
                    DEBUG_MESSAGE(msgBuf);
                }
                
                if(ibufs[fd].callback)
                {
                    status = (* (ibufs[fd].callback))(ibufs[fd].clientData,
                                                      ibufs[fd].data,
                                                      ibufs[fd].bytesInBuffer,
                                                      ibufs[fd].bufSize,
                                                      False);
                }
                else
                {
                    status = ErrorReturn;
                }
                
                CancelAutoInput(fd);
                
                return(status);
            }
        }
        
        /*
         * How much data do we still need to get?
         */
        
        bytesStillNeeded = (ibufs[fd].bytesNeeded - ibufs[fd].bytesInBuffer);
        
        /*
         * Try to get it.
         */
        
        if(Debug(autoinput))
        {
            result = socket_count(fd);
            
            sprintf(msgBuf, 
                    "AutoFill(%d ...) want %d, have %d, need %d, %d available",
                    fd, ibufs[fd].bytesNeeded, ibufs[fd].bytesInBuffer, 
                    bytesStillNeeded, result);
            DEBUG_MESSAGE(msgBuf);
        }
        
        bytesToRead = bytesStillNeeded;
        bytes_received = 0;
        
        result = socket_nb_receive(fd,
                                   &(ibufs[fd].data[ibufs[fd].bytesInBuffer]),
                                   bytesToRead,
                                   &bytes_received);
        
        if (result >= 0)
        {
            ibufs[fd].bytesInBuffer += bytes_received;
        }

        /*
         * Check for problems
         */
        if (result <= 0)
        {
            /* 0 = EOF, negative = errors. */
            if(Debug(autoinput))
            {
                if (result == 0)
                    sprintf(msgBuf, "AutoFill(%d ...) socket EOF, calling %s",
                            fd, ibufs[fd].callbackName);
                else
                    sprintf(msgBuf, "AutoFill(%d ...) socket died, calling %s",
                            fd, ibufs[fd].callbackName);

                DEBUG_MESSAGE(msgBuf);
            }

            if(ibufs[fd].callback)
            {
                status = (* (ibufs[fd].callback))(ibufs[fd].clientData,
                                                  ibufs[fd].data,
                                                  ibufs[fd].bytesInBuffer,
                                                  ibufs[fd].bufSize,
                                                  False);
            }
            else
            {
                status = ErrorReturn;
            }
                
            CancelAutoInput(fd);
                
            return(status);
        }
        
        
        /*
         * Is the buffer full yet?
         */
        
        if(ibufs[fd].type == InputType_Line)
        {
            if(ibufs[fd].data[ibufs[fd].bytesInBuffer-1] != '\n')
                ibufs[fd].bytesNeeded++;
            else
                ibufs[fd].data[ibufs[fd].bytesInBuffer-1] = '\0';
        }
        
        do
        {
            if(ibufs[fd].bytesInBuffer >= ibufs[fd].bytesNeeded)
            {
                if(Debug(autoinput))
                {
                    sprintf(msgBuf, "AutoFill(%d ...) filled, calling %s",
                            fd, ibufs[fd].callbackName);
                    DEBUG_MESSAGE(msgBuf);
                }
                
                bytesStillNeeded = 0;
                
                ibufs[fd].resumeFlag = 0;
                
                if(ibufs[fd].callback)
                {
                    status = (* (ibufs[fd].callback))(ibufs[fd].clientData,
                                                      ibufs[fd].data,
                                                      ibufs[fd].bytesInBuffer,
                                                      ibufs[fd].bufSize,
                                                      True);
                    
                    if(status == ErrorReturn)
                        return(status);
                }
                
                /*
                 * Check to see if the consumer has
                 * requested more data. If not, cancel this auto input.
                 */
                
            }    
        } while(ibufs[fd].resumeFlag
                && ibufs[fd].bytesInBuffer >= ibufs[fd].bytesNeeded);
        
        if(ibufs[fd].bytesInBuffer >= ibufs[fd].bytesNeeded)
            CancelAutoInput(fd);

#ifdef DEBUG
        if(Debug(autoinput))
            fprintf(stderr, "%02d %d\n", fd, socket_count(fd));
#endif
        
    } while(ibufs[fd].resumeFlag &&
            ibufs[fd].bytesInBuffer < ibufs[fd].bytesNeeded);
            
    return(NormalReturn);
}

