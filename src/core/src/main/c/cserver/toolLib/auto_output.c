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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/auto_output.c,v $
 * $RCSfile: auto_output.c,v $
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

#define DEBUG_ME        "DEBUG_TOOLLIB"
#include "debugging.h"
#include "style.h"
#include "error_hand.h"
#include "buffers.h"
#include "sockets.h"

/* LOCAL HEADER FILES */
#include "plumbing.h"
#include "auto_output.h"

DebugVariable(autooutput, "autooutput", 0x01);

/*
 * From sys/types.h
 */

#define MaxFds          FD_SETSIZE
#define LegalFd(fd)     ((fd) >= 0 && (fd) < MaxFds)

static int initialized = 0;

typedef struct autoOutputBuf
{
    char        *data;

    int         fd;
    int         bytesToSend;
    int         bytesAlreadySent;
    int         restartFlag;
    
    char        *clientData;
    
    AutoOutputBufCBProc callback;
    const char  *callbackName;
} AutoOutputBuf;

static AutoOutputBuf obufs[MaxFds];

static int AutoEmpty(int fd, char *clientData);

char *obufinfo(int fd)
{
    static char foo[128];

    if(initialized == 0) InitAutoOutputBufs();

    sprintf(foo, "%d, %d, %d, %s", 
            obufs[fd].fd,
            obufs[fd].bytesToSend,
            obufs[fd].bytesAlreadySent,
            obufs[fd].callbackName);
    return(foo);
}

int InitAutoOutputBufs()
{
    int fd;
    
    if(initialized != 0)
        return(NormalReturn);
    initialized = 1;
    
    for(fd = 0; fd < MaxFds; fd++)
    {
        obufs[fd].fd               = -1;
        obufs[fd].data             = (char *) 0;
        obufs[fd].bytesToSend      = 0;
        obufs[fd].bytesAlreadySent = 0;
        obufs[fd].restartFlag      = 0;
        obufs[fd].clientData       = (char *) 0;
        obufs[fd].callback         = (AutoOutputBufCBProc) 0;
        obufs[fd].callbackName     = "No Output Callback";
    }

    return(NormalReturn);
}

int CancelAutoOutput(int fd)
{
    int bytesAlreadySent;
    
    if(initialized == 0) InitAutoOutputBufs();

    if(Debug(autooutput))
    {
        sprintf(msgBuf, "CancelAutoOutput(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }

    if(!LegalFd(fd))
    {
        if(Debug(autooutput))
        {
            sprintf(msgBuf, "fd %d out of range (0, %d)", fd, MaxFds);
            DEBUG_MESSAGE(msgBuf);
        }
        
        return(ErrorReturn);
    }

    bytesAlreadySent = obufs[fd].bytesAlreadySent;

    DisconnectOutputFd(fd);
    
    obufs[fd].fd = -1;
    
    return(bytesAlreadySent);
}

void FlushAutoOutput(int fd)
{
    CancelAutoOutput(fd);
    FreeBuffer(obufs[fd].data);
    obufs[fd].data = (char *) 0;
}


int StartAutoOutput(
                    int fd,
                    char *data,
                    int bytesToSend,
                    char *clientData,
                    AutoOutputBufCBProc callback,
                    const char *callbackName)
{
    if(initialized == 0) InitAutoOutputBufs();

    if(Debug(autooutput))   
    {
        sprintf(msgBuf, "StartAutoOutput(%d, 0x%x, %d, 0x%x, 0x%x, %s)",
                fd, data, bytesToSend, clientData, callback, callbackName);
        DEBUG_MESSAGE(msgBuf);
    }
    
    if(!LegalFd(fd))
    {
        if(Debug(autooutput))
        {
            sprintf(msgBuf, "fd %d out of range (0, %d)", fd, MaxFds);
            DEBUG_MESSAGE(msgBuf);
        }
        
        return(ErrorReturn);
    }
    
    if(obufs[fd].fd != -1)
        obufs[fd].restartFlag = 1;
    else
        obufs[fd].restartFlag = 0;
    
    obufs[fd].fd               = fd;
    obufs[fd].data             = data;
    obufs[fd].bytesToSend      = bytesToSend;
    obufs[fd].bytesAlreadySent = 0;
    obufs[fd].clientData       = clientData;
    obufs[fd].callback         = callback;
    obufs[fd].callbackName     = callbackName;

    ConnectOutputFd(fd, AutoEmpty, "AutoEmpty", (char *) 0);
    
    /*
     * Took this out 1/18/93
     * It causes mucho recursion if it's in
     */
    /* AutoEmpty(fd, (char *) 0); */
    
    return(fd);
}

/* ARGSUSED */
static int AutoEmpty(int fd, char *clientData)
{
    int status;
    int result;
    int bytesStillToSend;
    
    /*
     * What's left to send out?
     */

    bytesStillToSend = (obufs[fd].bytesToSend - obufs[fd].bytesAlreadySent);

    /*
     * Send as much as possible
     */

    result = socket_nb_send(fd,
                            &(obufs[fd].data[obufs[fd].bytesAlreadySent]),
                            bytesStillToSend);

    /*
     * Check to make sure there was no grievous error
     */

    if(result < 0)
    {
        if(errno != EPIPE)
            result = socket_test(fd);

        if(result < 0)
        {
            if(Debug(autooutput))
            {
                sprintf(msgBuf, "AutoEmpty(%d ...) socket died, calling %s",
                        fd, obufs[fd].callbackName);
                DEBUG_MESSAGE(msgBuf);
            }

            if(obufs[fd].callback)
            {
                status = (* (obufs[fd].callback))(obufs[fd].clientData,
                                                  obufs[fd].bytesAlreadySent,
                                                  obufs[fd].data,
                                                  False);
            }
            else
            {
                status = ErrorReturn;
            }
            
            if(obufs[fd].restartFlag == 0)
                CancelAutoOutput(fd);
            
            return(status);
        }
    }
    else
    {
        obufs[fd].bytesAlreadySent += result;
    } 

    /*
     * Are we done yet?
     */

    if(obufs[fd].bytesAlreadySent >= obufs[fd].bytesToSend)
    {
        if(Debug(autooutput))
        {
            sprintf(msgBuf, "AutoEmpty(%d ...) emptied, calling %s",
                    fd, obufs[fd].callbackName);
            DEBUG_MESSAGE(msgBuf);
        }

        if(obufs[fd].callback)
        {
            status = (* (obufs[fd].callback))(obufs[fd].clientData,
                                              obufs[fd].bytesAlreadySent,
                                              obufs[fd].data,
                                              True);

            if(status == ErrorReturn)
                return(status);
        }
        
        if(obufs[fd].restartFlag == 0 
           || obufs[fd].bytesAlreadySent >= obufs[fd].bytesToSend)
            CancelAutoOutput(fd);
    }    

    return(NormalReturn);
}
