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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/include/sockets.h,v $
 * $RCSfile: sockets.h,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/*
 * sockets.c - general-purpose routines for opening and using sockets
 *              as server or client.
 *
 * Functions are:
 *
 *      open_socket()                   Open a socket connection
 *      open_socket_port()                      Open a socket connection
 *      close_socket()                  Close a socket connection
 *      accept_socket()                 Accept a connection on a socket
 *
 * Usage:
 *      Servers call open_socket() as servers and then call accept_socket()
 *      to get connections from clients.
 *
 *      Clients call open_socket() and use that socket id to talk to servers.
 *
 *      Both servers and clients call close_socket() to terminate.
 *
 * ------------------------------------------------------------------------ */

#ifndef SOCKETS_INCLUDE
#define SOCKETS_INCLUDE

#include <sys/types.h>
#include <sys/socket.h>

#include "compat.h"

BEGIN_extern_C

/* Mode variables for open_socket() */
#define SERVER 1
#define CLIENT 2

#define UnixSocketPath  "/tmp"

#ifndef __C2MAN__

extern int LookForServer(
#if NeedFunctionPrototypes
 const char *,                  /* service */
 const char *,                  /* hostname */
 int,                           /* unixPort */
 int,                           /* inetPort */
 int *                          /* socketp */
#endif
);

extern int ConnectToServer(
#if NeedFunctionPrototypes
 const char *serviceName,
 const char *hostName,
 int type,
 int port,
 int *socketp
#endif
); 

extern int SetupSockets(
#if NeedFunctionPrototypes
 const char *,                  /* serviceName */
 int,                           /* tcpPort */
 int,                           /* unixPort */
 int *,                         /* tcpSocketp */
 int *                          /* unixSocketp */
#endif
); 


extern void set_hunt_mode(
#if NeedFunctionPrototypes
 int                            /* flag */
#endif
); 

extern int set_socket_domain(
#if NeedFunctionPrototypes
 int                            /* new */
#endif
); 

extern int open_socket(
#if NeedFunctionPrototypes
 int *,                         /* sock */
 const char *,                  /* service_name */
 const char *,                  /* host_name */
 int                            /* mode */
#endif
);

extern int open_socket_port(
#if NeedFunctionPrototypes
 int *,                         /* sock */
 int,                           /* port */
 const char *,                  /* host_name */
 int                            /* mode */
#endif
);

extern int open_unix_port(
#if NeedFunctionPrototypes
 int *,                         /* sock */
 const char *,                  /* sockname */
 int                            /* mode */
#endif
);

extern int close_socket(
#if NeedFunctionPrototypes
 int                            /* sock */
#endif
);

extern char *LastHostAccepted(
#if NeedFunctionPrototypes
#endif
);

extern int accept_socket(
#if NeedFunctionPrototypes
 int,                           /* sock */
 int *                          /* new_socket */
#endif
);

extern int socket_receive(
#if NeedFunctionPrototypes
 int,                           /* fd */
 void *,                        /* buf */
 int                            /* nbytes */
#endif
);

extern int socket_nb_receive(
#if NeedFunctionPrototypes
 int,                           /* fd */
 void *,                        /* buf */
 int,                           /* nbytes */
 int *                          /* bytes_received */
#endif
);

extern int socket_peek(
#if NeedFunctionPrototypes
 int,                           /* fd */
 void *,                        /* buf */
 int                            /* nbytes */
#endif
);

extern int socket_test(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
);

extern int socket_count(
#if NeedFunctionPrototypes
 int                            /* fd */
#endif
);

extern int socket_send(
#if NeedFunctionPrototypes
 int,                           /* fd */
 void *,                        /* buf */
 int                            /* nbytes */
#endif
);

extern int socket_nb_send(
#if NeedFunctionPrototypes
 int,                           /* fd */
 void *,                        /* buf */
 int                            /* nbytes */
#endif
);

extern int socket_control(
#if NeedFunctionPrototypes
 int,                           /* fd */
 int,                           /* whichFlag */
 int                            /* what */
#endif
);

extern char *ConnectedInternetAddress(
#if NeedFunctionPrototypes
 int fd
#endif
);

extern char *ConnectedHostname(
#if NeedFunctionPrototypes
 int fd
#endif
);

#endif /* ifndef __C2MAN__ */

END_extern_C

#endif

