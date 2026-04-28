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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/plumbing.c,v $
 * $RCSfile: plumbing.c,v $
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
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/types.h>
#include <errno.h>
#include <stdio.h> 

#ifdef c_plusplus
#include <sysent.h>
#endif

/* TOOL HEADER FILES */
#include "compat.h"
#define DEBUG_ME        "DEBUG_TOOLLIB"
#include "debugging.h"
#include "style.h"
#include "error_hand.h"
#include "free_mgr.h"
#include "sockets.h"

/* LOCAL HEADER FILES */
#include "plumbing.h"

DebugVariable(plumbing, "plumbing", 0x01);

#ifdef TK
#include <tk.h>
#endif
/*------------------------------------------------------------------------
 *
 *  Deal with another event loop...
 *
 *------------------------------------------------------------------------*/

typedef enum
{
    EventMode_Plumbing,
    EventMode_Tk
} EventMode;

/* Default the event mode to Normal */
EventMode eventMode = EventMode_Plumbing;

/*------------------------------------------------------------------------
 *
 *      File Descriptor support
 *
 *      These functions take care of hooking callback functions up to
 *      file descriptor events.
 *
 *------------------------------------------------------------------------*/

/*
 * From sys/types.h
 */

#define MaxFds          FD_SETSIZE
#define LegalFd(fd)     ((fd) >= 0 && (fd) < MaxFds)

static int      nFds = 0;
static int      maxFd = 0;

static fd_set   readFds;
static fd_set   writeFds;

static int initialized = 0;

#define TraceData       (0x01)

typedef struct _fdSupport
{
    int fd;
    FdCBProc callback;
    const char *callbackName;
    char *clientData;
    int  flag;
} FdSupport;

static FdSupport        ifds[MaxFds];
static FdSupport        ofds[MaxFds];

static char             peekBuf[16 * 1024];

static  Bool    keepLooping = True;

void StopLoopOnFds()
{
    keepLooping = False;
}

/* ------------------------------------------------------------------------
 * 
 * LoopOnFds            Continuously loops on select() of the active
 *                      file descriptors
 * 
 * RETURNS:             -1 on failure
 *                      0 if someone called StopLoopOnFds()
 *           
 * ------------------------------------------------------------------------ */

LoopOnFds()
{
    int result = 0;
    int status;
    int fd;
    int nBytes;
    int none_read;
    struct rlimit rlimit;
    
    struct timeval *forEver = (struct timeval *) 0;
    
    fd_set rFds; 
    fd_set wFds; 

    if(initialized == 0)
        return(-1);
    
    /*
     * Assuming that no one expands the dtable size within the program!
     */

#ifdef RLIMIT_NOFILE
    getrlimit(RLIMIT_NOFILE, &rlimit);
#else
    rlimit.rlim_cur = getdtablesize();
#endif
    maxFd = rlimit.rlim_cur;

    /*
     * Turn the keepLooping flag on. If someone calls StopLoopOnFds(),
     * we'll stop looping and fall out of here.
     */

    keepLooping = True;
    

    while(keepLooping && nFds > 0)
    {
        /*
         * Copy the master set into the local set since select(2) 
         * will be modifying the ones we pass in to it.
         */
        
        rFds = readFds;  
        wFds = writeFds; 
        
#ifdef __hpux
        result = select(maxFd, (int*)&rFds, (int*)&wFds, (int *) 0, forEver);
#else
        result = select(maxFd, &rFds, &wFds, (fd_set *) 0, forEver);
#endif
        
        /*
         * returning from select may be the result of an interrupted
         * system call.
         */
        
        if(result < 0)
        {
            switch(errno)
            {
              case EINTR:
                break;
                
              case EBADF:
                WeedOutBadFds();
                break;
                
              default:
                break;
            }
            continue;
        }

        if(result < 0)
        {
            WARNING_PERROR("Error in select call");
            return(ErrorReturn);
        }

        none_read = 1;
        for(fd = 0; result > 0 && fd < maxFd; fd++)
        {
            if(FD_ISSET(fd, &rFds))
            {
                none_read = 0;
                if(ifds[fd].callback != (FdCBProc) 0)
                {
                    if(Debug(plumbing))
                    {
                        nBytes = socket_count(fd);
                        sprintf(msgBuf, "Calling %s(%d...) ( %05d bytes )", 
                                ifds[fd].callbackName, fd, nBytes);
                        DEBUG_MESSAGE(msgBuf);

                        if(ifds[fd].flag & TraceData)
                        {
                            if(nBytes > sizeof(peekBuf))
                                nBytes = sizeof(peekBuf);
                            socket_peek(fd, peekBuf, nBytes);
                            PrintTrace(fd, peekBuf, nBytes, 'I');
                        }
                    }
                    status = (*(ifds[fd].callback)) (fd, ifds[fd].clientData);
                    if(status == ErrorReturn)
                    {
                        DisconnectInputFd(fd);
                        DisconnectOutputFd(fd);
                    }
                }
                else
                {
                    sprintf(msgBuf, "Uncaught Read Select on fd %d", fd);
                    WARNING_MESSAGE(msgBuf);
                }
                result--;
            }

            if(FD_ISSET(fd, &wFds))
            {
                if (none_read)
                {
                    if(ofds[fd].callback != (FdCBProc) 0)
                    {
                        if(Debug(plumbing))
                        {
                            sprintf(msgBuf, "Calling %s(%d...)", 
                                    ofds[fd].callbackName, fd);
                            DEBUG_MESSAGE(msgBuf);
                        }
                        status = (*(ofds[fd].callback)) 
                            (fd, ofds[fd].clientData);
                        if(status == ErrorReturn)
                        {
                            DisconnectInputFd(fd);
                            DisconnectOutputFd(fd);
                        }
                    }
                    else
                    {
                        sprintf(msgBuf, "Uncaught Write Select on fd %d", fd);
                        WARNING_MESSAGE(msgBuf);
                    }
                }
                result--;
            }
            
        }
    }

    return(result);
}

int PrintTrace(
               int fd,
               char *buf,
               int nBytes,
               char direction)
{
    int i;

    if(initialized == 0) InitFds();
    
    printf("Trace (%c) fd %02d:", direction, fd);
    for(i = 0; i < nBytes; i++)
    {
        /* SUPPRESS 112 *//* CodeCenter Retrieving x from y object is z */
        printf("%02x ", (buf[i] & 0xFF));
    }
    printf("\n");
    return(nBytes);
}


/* ------------------------------------------------------------------------
 * 
 * InitFds      Init the file descriptor handling stuff.
 * 
 * RETURNS:     -1 on failure
 *           
 * ------------------------------------------------------------------------ */

int InitFds()
{
    int fd;
    
    if(initialized != 0)
        return(NormalReturn);
    initialized = 1;
    
    maxFd = 0;
    nFds = 0;
    
    FD_ZERO(&readFds);
    FD_ZERO(&writeFds);
    
    for(fd = 0; fd < MaxFds; fd++)
    {
        ifds[fd].fd           = -1;
        ifds[fd].callback     = (FdCBProc) 0;
        ifds[fd].callbackName = "No Callback Defined";
        ifds[fd].clientData   = (char *) 0;
        ifds[fd].flag         = 0;

        ofds[fd].fd           = -1;
        ofds[fd].callback     = (FdCBProc) 0;
        ofds[fd].callbackName = "No Callback Defined";
        ofds[fd].clientData   = (char *) 0;
        ofds[fd].flag         = 0;
    }

    return(0);
}

int InitTkFds ()
{
    int result;
    
    result = InitFds();
#ifdef TK
    eventMode = EventMode_Tk;
#endif
    return result;
}

void WeedOutBadFds()
{
    int fd;
    
    if(initialized == 0) InitFds();

    for(fd = 0; fd < MaxFds; fd++)
    {
        if(ifds[fd].fd == -1)
        {
            DisconnectInputFd(fd);
        }

        if(ofds[fd].fd == -1)
        {
            DisconnectOutputFd(fd);
        }

        if(socket_test(fd) < 0)
        {
            DisconnectOutputFd(fd);
            DisconnectInputFd(fd);
        }
    }
}

void Plumbing_TkInputEvent (ClientData data, int mask)
{
#ifdef TK
    int fd, nBytes, status;

    fd = (int)data;
    if (Debug(plumbing))
    {
        sprintf(msgBuf, "Tk Input Event on fd %d", fd);
        DEBUG_MESSAGE(msgBuf);
    }

    if(ifds[fd].callback != (FdCBProc) 0)
    {
        if(Debug(plumbing))
        {
            nBytes = socket_count(fd);
            sprintf(msgBuf, "Calling %s(%d...) ( %05d bytes )", 
                    ifds[fd].callbackName, fd, nBytes);
            DEBUG_MESSAGE(msgBuf);

            if(ifds[fd].flag & TraceData)
            {
                if(nBytes > sizeof(peekBuf))
                    nBytes = sizeof(peekBuf);
                socket_peek(fd, peekBuf, nBytes);
                PrintTrace(fd, peekBuf, nBytes, 'I');
            }
        }
        status = (*(ifds[fd].callback)) (fd, ifds[fd].clientData);
        if(status == ErrorReturn)
        {
            DisconnectInputFd(fd);
            DisconnectOutputFd(fd);
        }
    }
#endif
}

/* ------------------------------------------------------------------------
 * 
 * ConnectInputFd()     Allows user to hook a callback to input ready
 *                      events on a file descriptor.
 * 
 * RETURNS:             The file descriptor.
 *
 * NOTE:                Clobbers the old input fd to callback association.
 *                      This is how you change what the callback or clientdata
 *                      that's hung off a fd is.
 * ------------------------------------------------------------------------ */

int ConnectInputFd(
                   int fd,
                   FdCBProc callback,
                   const char *callbackName,
                   char *clientData)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "ConnectInputFd(%d, %s, 0x%08x)",
                fd, callbackName, clientData);
        DEBUG_MESSAGE(msgBuf);
    }
    
    /*
     * If it's a new slot, bump up the nFds counter.
     * If it's an old slot, the old guy loses but nFds stays
     * the same.
     */

    if(ifds[fd].fd == -1)
        nFds++;
    
    /*
     * Keep track of the biggest one we have on hand
     */

    if(fd > maxFd)
        maxFd = fd;
    
    /*
     * Init the various fields
     */

    ifds[fd].fd           = fd;
    ifds[fd].callback     = callback;
    ifds[fd].callbackName = callbackName;
    ifds[fd].clientData   = clientData;
    
    FD_SET(fd, &readFds);

#ifdef TK
    if (eventMode == EventMode_Tk)
    {
        if (Debug(plumbing))
        {
            sprintf(msgBuf, "Adding fd %d to TK Event list", fd);
            DEBUG_MESSAGE(msgBuf);
        }
        Tk_CreateFileHandler(fd, TK_READABLE, Plumbing_TkInputEvent,
                             (ClientData)fd);
    }
#endif

    return(fd);
}

/* ------------------------------------------------------------------------
 * 
 * ConnectOutputFd()    Allows user to hook a callback to output ready
 *                      events on a file descriptor.
 * 
 * RETURNS:             The file descriptor.
 *
 * NOTE:                Clobbers the old output fd to callback association.
 *                      This is how you change what the callback or clientdata
 *                      that's hung off a fd is.
 * ------------------------------------------------------------------------ */

int ConnectOutputFd(
                    int fd,
                    FdCBProc callback,
                    const char *callbackName,
                    char *clientData)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "ConnectOuputFd(%d, %s, 0x%08x)",
                fd, callbackName, clientData);
        DEBUG_MESSAGE(msgBuf);
    }
    
    /*
     * If it's a new slot, bump up the nFds counter.
     * If it's an old slot, the old guy loses but nFds stays
     * the same.
     */

    if(ofds[fd].fd == -1)
        nFds++;
    
    /*
     * Keep track of the biggest one we have on hand
     */

    if(fd > maxFd)
        maxFd = fd;
    
    /*
     * Init the various fields
     */

    ofds[fd].fd           = fd;
    ofds[fd].callback     = callback;
    ofds[fd].callbackName = callbackName;
    ofds[fd].clientData   = clientData;
    
    FD_SET(fd, &writeFds);

    return(fd);
}

int SetTraceFd(
               int fd,
               int direction,
               int flag)
{
    if(initialized == 0) InitFds();

    if(flag == True)
    {
        if(direction)
            ifds[fd].flag |= TraceData;
        else
            ofds[fd].flag |= TraceData;
    }
    else
    {
        if(direction)
            ifds[fd].flag &= ~TraceData;
        else
            ofds[fd].flag &= ~TraceData;
    }
    
    return(fd);
}

int GetTraceFd(
               int fd,
               int direction)
{
    if(initialized == 0) InitFds();

    if(direction)
        return(ifds[fd].flag & TraceData);
    else
        return(ofds[fd].flag & TraceData);
}


/* ------------------------------------------------------------------------
 * 
 * DisconnectFd()       Detaches the callback from a file descriptor.
 * 
 * RETURNS:     The file descriptor.
 *           
 * ------------------------------------------------------------------------ */

void DisconnectInputFd(int fd)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "DisconnectInputFd(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }
    FD_CLR(fd, &readFds);
    
    ifds[fd].fd       = -1;
    ifds[fd].callback = (FdCBProc) 0;

#ifdef TK
    if (eventMode == EventMode_Tk)
    {
        if (Debug(plumbing))
        {
            sprintf(msgBuf, "Removing fd %d from TK Event list", fd);
            DEBUG_MESSAGE(msgBuf);
        }
        Tk_DeleteFileHandler(fd);
    }
#endif

}

void DisconnectOutputFd(int fd)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "DisconnectOutputFd(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }
    
    FD_CLR(fd, &writeFds);
    
    ofds[fd].fd       = -1;
    ofds[fd].callback = (FdCBProc) 0;
}

void DisableInputFd(int fd)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "DisableInputFd(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }
    FD_CLR(fd, &readFds);
}

void EnableInputFd(int fd)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "EnableInputFd(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }
    FD_SET(fd, &readFds);
}

void DisableOutputFd(int fd)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "DisableOutputFd(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }
    FD_CLR(fd, &writeFds);
}

void EnableOutputFd(int fd)
{
    if(initialized == 0) InitFds();

    if(Debug(plumbing))
    {
        sprintf(msgBuf, "EnableOutputFd(%d)", fd);
        DEBUG_MESSAGE(msgBuf);
    }
    FD_SET(fd, &writeFds);
}


