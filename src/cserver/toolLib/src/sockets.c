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
 * $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/sockets.c,v $
 * $RCSfile: sockets.c,v $
 * $Revision: 1.2 $
 * $Date: 2004/01/26 19:07:10 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

/* SYSTEM LEVEL HEADER FILES */
#include <stdio.h>
#include <sys/types.h>
#ifdef _AIX
#include <fcntl.h>
#else
#include <sys/fcntl.h>
#endif
#include <sys/socket.h>
#include <netinet/in.h>         /* Order of these matters under Solaris */
#include <arpa/inet.h>          /* Order of these matters under Solaris */
#include <netinet/tcp.h>
#include <netdb.h>
#if defined(_AIX) || defined(ultrix) || defined(__hpux)
#include <sys/ioctl.h>
#else
#include <sys/filio.h>
#endif
#include <errno.h>
#include <sys/un.h>
#include <string.h>
#include <sys/param.h>

#ifdef SVR4
#include <memory.h>
#define bzero(addr, n)  memset(addr, 0, n)
#define bcopy(from, to, n) memcpy(to, from, n)
#endif

#ifdef c_plusplus
#include <sysent.h>  
#endif

/* OTHER HEADER FILES */
#include "compat.h"
#define DEBUG_ME "DEBUG_TOOLLIB"
#include "debugging.h"
#include "style.h"
#include "sockets.h"
#include "error_hand.h"

DebugVariable(sockets, "sockets", 0x01);
  
/* LOCAL HEADER FILES */

/* LOCAL FUNCTIONS */
static int open_client_socket(
 int *,                         /* sock */
 const char *,                  /* service_name */
 const char *,                  /* host_name */
 int,                           /* domain */
 int                            /* port */
);      

static int  open_server_socket (
 int *,                         /* sock */
 const char *,                  /* service_name */
 int,                           /* domain */
 int                            /* port */
); 

/* Global variables */
  
#define AF              AF_INET
#define SOCK_TYPE       SOCK_STREAM
#define MEDIUM          "tcp"

static int      domain = AF_INET;
static char     message[1024];
static int      hunt_mode = 0;
static int      quiet_mode = 0;


/*
 * Look for a server on the specified host
 *
 *      Given a service name, hostname, unis port, internet port, and
 *      an int pointer to put the socket number into, try to connect
 *      to that service on the host, or on the local host if the
 *      hostname is not given, preferentially by the unix port, then
 *      by the inet port.
 * 
 * RETURNS:
 * -1 on failure, 0 on success
 */           

int LookForServer(
                  const char *service,
                  const char *hostname,
                  int unixPort,
                  int inetPort,
                  int *socketp)
{
    int result;
    char localhost[MAXHOSTNAMELEN + 1];
    
    if(socketp == (int *) 0)
    {
        WARNING_MESSAGE("LookForServer called with NULL socket pointer");
        return(-1);
    }
    
    *socketp = -1;

    if(service == (char *) 0)
    {
        WARNING_MESSAGE("LookForServer called with NULL service name");
        return(-1);
    }
    
    if(Debug(sockets))
    {
        sprintf(message, "LookForServer(%s, %s, %d, %d, 0x%x)",
                service, hostname, unixPort, inetPort, socketp);
        DEBUG_MESSAGE(message);
    }
    
    /*
     * Algorithm:
     *
     * If no hostname is given, try to connect to the local host via
     * unix port, otherwise connect via inetPort.
     *
     * If hostname is given, force connection via inetPort.
     *
     */

    quiet_mode = 1;

    if(strlen(hostname) == 0)
    {
        result = ConnectToServer(service, hostname, AF_UNIX,
                                 unixPort, socketp);
        if(result < 0)
        {
            result = gethostname(localhost, MAXHOSTNAMELEN);
            if(result < 0)
            {
                WARNING_PERROR
                    ("Couldn't get local hostname, using 'localhost'");
                strcpy(localhost, "localhost");
            }
            
            result = ConnectToServer(service, localhost, AF_INET,
                                     inetPort, socketp);
            if(result < 0)
            {
                sprintf(msgBuf, "Could not connect to %s anywhere!",
                        service);
                WARNING_MESSAGE(msgBuf);
                quiet_mode = 0;
                return(-1);
            }
        }
    }
    else
    {
        result = ConnectToServer(service, hostname, AF_INET,
                                 inetPort, socketp);
        if(result < 0)
        {
            sprintf(msgBuf, "Could not connect to %s anywhere!",
                    service);
            WARNING_MESSAGE(msgBuf);
            quiet_mode = 0;
            return(-1);
        }
    }

    quiet_mode = 0;
    return(0);
}


int ConnectToServer(
                    const char *serviceName,
                    const char *hostName,
                    int type,
                    int port,
                    int *socketp)
{
    struct servent *serverEntry = (struct servent *) 0;
    char thisHost[MAXHOSTNAMELEN + 1];
    char sockname[1024];
    int result;

    /*
     * UNIX connection:
     *
     * Build a unix socket path 
     * using the service name and the port
     */
        
    if(type == AF_UNIX)
    {
        sprintf(sockname, "%s/%s.%d", UnixSocketPath, serviceName, port);
        result = open_unix_port(socketp, sockname, CLIENT);
        if(result < 0)
        {
            *socketp = -1;
            return(-1);
        }
        return(0);
    }

    /*
     * INET connection:
     *
     *  If the port number is 0, try to find the port in /etc/services, 
     *  use given port otherwise
     */

    if(hostName == (char *) 0)
    {
        result = gethostname(thisHost, MAXHOSTNAMELEN);
        if(result < 0)
        {
            sprintf(message,
                    "ConnectToServer: no host name for internet connection");
            WARNING_PERROR(message);
            *socketp = -1;
            return(-1);
        }
    }
    else
    {
        strncpy(thisHost, hostName, MAXHOSTNAMELEN);
    }
    
    if(serviceName != (char *) 0 && port == 0)
    {
        if ((serverEntry = getservbyname (serviceName, "tcp")) != NULL)
        {
            port = serverEntry->s_port;
        }
    }
    
    /*
     * Either we got a port number or a port was passed in to this function
     * as a fallback if port is > 0 here.
     */

    if(port > 0)
    {
        result = open_socket_port(socketp, port, hostName, CLIENT);
        if(result < 0)
        {
            *socketp = -1;
            return(-1);
        }
    }
    
    return(0);
}

/* ------------------------------------------------------------------------
 * 
 * SetupSockets - Opens the sockets/ports that it will listen to and reports
 *                the result to stderr/stdout.
 * 
 * RETURNS:  
 *           
 * ------------------------------------------------------------------------ */


int SetupSockets(
                 const char *serviceName,
                 int inetPort,
                 int unixPort,
                 int *inetSocketp,
                 int *unixSocketp)
{
    struct servent *serverEntry = (struct servent *) 0;
    char sockname[1024];
    int result;
    Bool doInetSocket = False;
    Bool doUnixSocket = False;
    
    if(inetSocketp != (int *) 0)
        doInetSocket = True;

    if(unixSocketp != (int *) 0)
        doUnixSocket = True;

    if(doInetSocket)
    {
        /*
         * TCP connection:
         *
         * If the inetPort is 0, then look up the service name in
         * /etc/services to try to get a port number, otherwise use the port
         * number given.
         */

        *inetSocketp = -1;
        if(serviceName != (char *) 0 && inetPort == 0)
        {
            if ((serverEntry = getservbyname (serviceName, "tcp")) != NULL)
            {
                inetPort = serverEntry->s_port;
            }
        }
    
        /*
         * Either we got a port number or a port was passed in to this function
         * as a fallback if inetPort is > 0 here.
         */
        
        if(inetPort > 0)
        {
            result = open_socket_port(inetSocketp, inetPort,
                                      (char *) 0, SERVER);
            if(result < 0)
            {
                sprintf(message, "Error opening internet port %d\n", 
                        inetPort);
                WARNING_MESSAGE(message);
                return(-1);
            }
        }
        sprintf(message, "Using TCP Port %d", inetPort);
        INFO_MESSAGE(message);
    }

    if(doUnixSocket)
    {
        /*
         * UNIX connection:
         *
         * Build a unix socket path 
         * using the service name and the port
         */
    
        *unixSocketp = -1;

        sprintf(sockname, "%s/%s.%d", UnixSocketPath, serviceName, unixPort);
        unlink(sockname);
    
        result = open_unix_port(unixSocketp, sockname, SERVER);
        if(result < 0)
        {
            close_socket(*inetSocketp);
        
            sprintf(message, "Error opening unix socket %s\n", sockname);
            WARNING_MESSAGE(message);
            return(-1);
        }

        sprintf(message, "Using UNIX Port '%s'", sockname);
        INFO_MESSAGE(message);
    }    
    return(0);
}


/* ------------------------------------------------------------------------
 * 
 * set_hunt_mode()
 *
 *      Turns on or off error messages when a socket is being opened on
 *      a port that's busy.
 * 
 * RETURNS:  
 *      Nothing
 *           
 * ------------------------------------------------------------------------ */

void set_hunt_mode(int flag)
{
    hunt_mode = flag;
}

/*
 *      set_socket_domain()
 *
 *              Sets the domain to either AF_INET or AF_UNIX.
 *
 *      RETURNS:
 *              Old Domain.
 */

int set_socket_domain(
                      int newDomain)                    /* New domain to use */
{
    int old = domain;
    
    if(newDomain != AF_UNIX && newDomain != AF_INET)
        return(old);
    
    domain = newDomain;
    return(old);
}

/*
 *      open_socket()
 *
 *              Given a service and host name open a socket connection
 *              to that service protocol on that host.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if not.
 */

int open_socket (
                 int *sock,             /* handle to socket id to open */
                 const char *service_name, /* find /etc/services */
                 const char *host_name, /* String to use as host name */
                 int mode)              /* SERVER or CLIENT */
{
    int result;
    int optval = 1;
    int optlen;
    struct protoent *tcp_proto;
    
    if (mode == SERVER)
        result = open_server_socket (sock, service_name, domain, 0);
    else
        result = open_client_socket (sock, service_name, host_name, domain, 0);

    if(result != -1 && domain == AF_INET)
    {
        tcp_proto = getprotobyname(MEDIUM);
        if(tcp_proto != NULL)
        {
            optlen = sizeof(optval);
            optval = 1;
            if (setsockopt (*sock, tcp_proto->p_proto,
                            TCP_NODELAY, (char *) &optval, optlen)
                < 0)
            {
                WARNING_PERROR("Error in TCP_NODELAY setsockopt call");
                if(close(*sock) < 0)
                    WARNING_PERROR("Error in socket close");
                return (-1);
            }
        }
    }
    
    if(result != -1)
        set_hunt_mode(0);
    
    return(result);
}

/*
 *      open_socket_port()
 *
 *              Given a port and host name open a socket connection
 *              to that service protocol on that host.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if not.
 */

int open_socket_port (
                      int *sock,        /* handle to socket id to open */
                      int port,         /* port id for service */
                      const char *host_name, /* String to use as host name */
                      int mode)         /* SERVER or CLIENT */
{
    int result;
    int optval = 1;
    int optlen;
    struct protoent *tcp_proto;
    
    if (mode == SERVER)
    {
        result = open_server_socket (sock, (char *) 0, domain, port);
    }
    else
    {
        result = open_client_socket (sock, (char *) 0, host_name,
                                     domain, port);
    }
    
    if(result != -1 && domain == AF_INET)
    {
        tcp_proto = getprotobyname(MEDIUM);
        if(tcp_proto != NULL)
        {
            optlen = sizeof(optval);
            optval = 1;
            if (setsockopt (*sock, tcp_proto->p_proto,
                            TCP_NODELAY, (char *) &optval, optlen)
                < 0)
            {
                WARNING_PERROR("Error in TCP_NODELAY setsockopt call");
                if(close(*sock) < 0)
                    WARNING_PERROR("Error in socket close");
                return (-1);
            }
        }
    }
    
    return(result);
}


/*
 *      open_unix_port()
 *
 *              Given a port and host name open a socket connection
 *              to that service protocol on that host.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if not.
 */
int open_unix_port (
                    int *sock,          /* handle to socket id to open */
                    const char *sockname, /* pathname of port to open */
                    int mode)           /* SERVER or CLIENT */
{
    int result;
    
    if (mode == SERVER)
        result = open_server_socket (sock, sockname, AF_UNIX, 0);
    else
        result = open_client_socket (sock, sockname, "", AF_UNIX, 0);
    
    return(result);
}

/*+------------------------------------------------------------------------
 *      open_client_socket()
 *
 *              Given a service and host name open a socket connection
 *              to that service protocol on that host as a client of the
 *              service.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if not.
 *
 *________________________________________________________________________*/

static int
open_client_socket(
                   int *sock,           /* handle to socket id to open */
                   const char *service_name,/* find in /etc/services */
                   const char *host_name,   /* String containing host name */
                   int domain,          /* AF_UNIX or AF_INET */
                   int port)            /* Port # if service name is NULL */
{
    int optval;
    int optlen;
    struct hostent *host_entry   = (struct hostent *) 0;
    struct servent *server_entry = (struct servent *) 0;
    struct sockaddr_in connect_addr;
    struct sockaddr_un unix_addr;
    int result;

    if(sock == NULL)
    {
        WARNING_MESSAGE("open_client_socket called with NULL sock");
        return(-1);
    }

    if(service_name == NULL && port == 0)
    {
        WARNING_MESSAGE("open_client_socket called with NULL service_name and bad port number");
        return(-1);
    }

    if(host_name == NULL)
    {
        WARNING_MESSAGE("open_client_socket called with NULL host_name");
        return(-1);
    }
    
    if(domain == AF_INET)
    {
        if(service_name != (char *) 0)
        {
            if ((server_entry = getservbyname (service_name, "tcp")) == NULL)
            {
                sprintf(message, 
                        "Couldn't get service entry for %s", service_name);
                WARNING_PERROR(message);
                return (-1);
            }
        }

        if ((host_entry = gethostbyname (host_name)) == NULL)
        {
            sprintf(message, "Couldn't get host entry for %s", host_name);
            WARNING_PERROR(message);
            return (-1);
        }
        
        /* Check the host entry for validity */
        if (host_entry->h_addrtype != AF_INET)
        {
            sprintf(message,
                    "Got unexpected address type %d in host entry.",
                     host_entry->h_addrtype);
            WARNING_PERROR(message);
            return (-1);
        }
        

        *sock = socket (domain, SOCK_TYPE, getprotobyname (MEDIUM)->p_proto);
    
        if (*sock < 0)
        {
            WARNING_PERROR("Error in socket call");
            return (-1);
        }
        
        optlen = sizeof(optval);
        if (getsockopt (*sock, SOL_SOCKET, SO_REUSEADDR, 
                        (char *) &optval, &optlen) < 0)
        {
            WARNING_PERROR("Error in REUSEADDR  getsockopt call");
            if(close(*sock) < 0)
                WARNING_PERROR("Error in socket close");
            return (-1);
        }

        if(optval == 0)
        {
            optval = 1;
            if (setsockopt (*sock, SOL_SOCKET, SO_REUSEADDR,
                            (char *) &optval, optlen)
                < 0)
            {
                WARNING_PERROR("Error in REUSEASSR setsockopt call");
                if(close(*sock) < 0)
                    WARNING_PERROR("Error in socket close");
                return (-1);
            }
        }
        
        
#ifdef PLEASE_DONT_ROUTE
        optlen = sizeof(optval);
        if (getsockopt (*sock, SOL_SOCKET, SO_DONTROUTE, &optval, &optlen) < 0)
        {
            WARNING_PERROR("Error in DONTROUTE  getsockopt call");
            if(close(*sock) < 0)
                WARNING_PERROR("Error in socket close");
            return (-1);
        }

        if(optval == 0)
        {
            optval = 1;
            if (setsockopt (*sock, SOL_SOCKET, SO_DONTROUTE, &optval, optlen)
                < 0)
            {
                WARNING_PERROR("Error in DONTROUTE setsockopt call");
                if(close(*sock) < 0)
                    WARNING_PERROR("Error in socket close");
                return (-1);
            }
        }
#endif
        
        /* Get ready to establish the socket and bind to it */
        bzero ((char *) &connect_addr, sizeof (connect_addr));
        bcopy (host_entry->h_addr, (char *) &connect_addr.sin_addr,
               host_entry->h_length);
        connect_addr.sin_family = host_entry->h_addrtype;
        if(server_entry != (struct servent *) 0)
        {
            /* Don't know if this is net or host order */
            connect_addr.sin_port = server_entry->s_port; 
        }
        else
        {
            connect_addr.sin_port = htons((short)port);
        }
        
        do
        {
            result = connect (*sock,
                              (struct sockaddr *) &connect_addr, 
                              sizeof (connect_addr));
        } while(result == -1 && (errno == EINTR));
        
        if(result == -1)
        {
            WARNING_PERROR("Error in connect call");
            if(close(*sock) < 0)
                WARNING_PERROR("Error in socket close");
            return (-1);
        }
    }
    else
    {
        *sock = socket (AF_UNIX, SOCK_STREAM, 0);
        if (*sock < 0)
        {
            WARNING_PERROR("Error creating UNIX socket");
            return(-1);
        }

        unix_addr.sun_family = AF_UNIX;
        if(service_name == (char *) 0)
        {
            sprintf(unix_addr.sun_path, "%d", port);
        }
        else
        {
            strncpy(unix_addr.sun_path, service_name, 
                    sizeof(unix_addr.sun_path) - 1);
            unix_addr.sun_path[sizeof(unix_addr.sun_path) - 1] = '\0';
        }
        
        if(connect(*sock, (struct sockaddr *) &unix_addr, 
                   sizeof(unix_addr)) < 0)
        {
            if(quiet_mode == 0)
                WARNING_PERROR("Error connecting to socket");
            if(close(*sock) < 0 && quiet_mode == 0)
                WARNING_PERROR("Error in socket close");
            return(-1);
        }
    }
    
    return (0);
}

/*+------------------------------------------------------------------------
 *      open_server_socket()
 *
 *              Given a service and host name open a socket connection
 *              to that service protocol on that host as a provider of the
 *              service.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if not.
 *
 *________________________________________________________________________*/

static int
open_server_socket (
                    int *sock,          /* handle to socket id to open */
                    const char *service_name, /* find in /etc/services */
                    int domain,         /* AF_UNIX or AF_INET */
                    int port)           /* Port number */
{
    int optval;
    int optlen;
    
    struct servent *server_entry = (struct servent *) 0;
    struct sockaddr_in connect_addr;
    struct sockaddr_un unix_addr;
    
    if(sock == NULL)
    {
        WARNING_MESSAGE("open_server_socket called with NULL sock");
        return(-1);
    }

    if(service_name == NULL && port == 0)
    {
        WARNING_MESSAGE("open_server_socket called with NULL service_name and bad port");
        return(-1);
    }

    if(domain == AF_INET)
    {
        if(service_name != (char *) 0)
        {
            server_entry = getservbyname (service_name, "tcp");
            if (server_entry == NULL)
            {
                sprintf(message, 
                        "Couldn't get service entry for %s", service_name);
                WARNING_PERROR(message);
                return (-1);
            }
        }
        /* Get ready to establish the socket and bind to it */

        /* Clear the structure first */
        bzero ((char *) &connect_addr, sizeof (connect_addr));

        /* Fill in the port entry */
        if(server_entry == (struct servent *) 0)
        {
            connect_addr.sin_port = htons((short)port);
        }
        else
        {
            /* Don't know if this is host or net order */
            connect_addr.sin_port = server_entry->s_port; 
        }
        
        *sock = socket (domain, SOCK_TYPE, getprotobyname (MEDIUM)->p_proto);

        if (*sock < 0)
        {
            WARNING_PERROR("Error in socket call");
            return (-1);
        }

        optlen = sizeof(optval);
        if (getsockopt (*sock, SOL_SOCKET, SO_REUSEADDR,
                        (char *) &optval, &optlen) < 0)
        {
            WARNING_PERROR("Error in getsockopt call");
            if(close(*sock) < 0)
                WARNING_PERROR("Error in socket close");
            return (-1);
        }

        if(optval == 0)
        {
            optval = 1;
            if (setsockopt (*sock, SOL_SOCKET, SO_REUSEADDR, 
                            (char *) &optval, optlen)
                < 0)
            {
                WARNING_PERROR("Error in setsockopt call");
                if(close(*sock) < 0)
                    WARNING_PERROR("Error in socket close");
                return (-1);
            }
        }
        
        if (bind (*sock, (struct sockaddr *) &connect_addr,
                  sizeof (connect_addr)) < 0)
        {
            if(!hunt_mode)
                WARNING_PERROR("Error in bind call");
            if(close(*sock) < 0)
                WARNING_PERROR("Error in socket close");
            return (-1);
        }
        
    }
    else    
    {
        *sock = socket (AF_UNIX, SOCK_STREAM, 0);
        if(*sock < 0)
        {
            WARNING_PERROR("Error opening UNIX socket");
            return(-1);
        }
        
        unix_addr.sun_family = AF_UNIX;
        if(service_name == (char *) 0)
        {
            sprintf(unix_addr.sun_path, "%d", port);
        }
        else
        {
            strncpy(unix_addr.sun_path, service_name, 
                    sizeof(unix_addr.sun_path) - 1);
            unix_addr.sun_path[sizeof(unix_addr.sun_path) - 1] = '\0';
        }
        
        if (bind (*sock, (struct sockaddr *) &unix_addr, 
                  sizeof (unix_addr)) < 0)
        {
            if(!hunt_mode)
                WARNING_PERROR("Error in bind call");
            if(close(*sock) < 0)
                WARNING_PERROR("Error in socket close");
            return(-1);
        }
    }
    
    if (listen (*sock, 5) < 0)
    {
        WARNING_PERROR("Error in listen call");
        close_socket(*sock);
        return (-1);
    }

    return (0);
}

/*+------------------------------------------------------------------------
 *      close_socket()
 *
 *              shut down a socket connection
 *
 *      RETURNS:
 *              0 if it closes OK
 *              -1 if not.
 *________________________________________________________________________*/

int close_socket (int sock)             /* Socket ID to close */

{
    int result;
    
    if (shutdown (sock, 2) < 0)
    {
        DEBUG_MESSAGE("Error in socket shutdown");
        /* This is ok, still has to be closed */
    }

    do
    {
        result = close(sock);
    } while(result == -1 && errno == EINTR);
    
    if(result < 0)
    {
        WARNING_PERROR("Error in socket close");
        return(-1);
    }
    
    return (0);
}

/*+------------------------------------------------------------------------
 *      accept_socket()
 *
 *              Given a socket id, accept a connection to that socket and
 *              return a new socket id specific to that connection.
 *
 *      RETURNS:
 *              0 if all went ok
 *              -1 if not.
 *________________________________________________________________________*/

static char lastHostAccepted[256];
static int lastHostInitialized = 0;

char *LastHostAccepted()
{
    if(lastHostInitialized)
        return(lastHostAccepted);
    else
        return("NoConnectionAcceptedYet");
}

int    accept_socket (
                      int sock,         /* Socket to accept from */
                      int *new_socket)  /* Put new socket number here */
{
    struct sockaddr_in socket_address;
    int         socket_address_length;
    struct hostent *connectingHost = (struct hostent *) 0;
    
    if(new_socket == NULL)
    {
        WARNING_MESSAGE("accept_socket called with NULL new_socket");
        return(-1);
    }
    
    socket_address_length = sizeof (socket_address);

    do
    {
        *new_socket = accept (sock, 
                              (struct sockaddr *) &socket_address,
                              &socket_address_length);
    } while (*new_socket ==-1 && errno == EINTR);
    
    if (*new_socket < 0)
    {
        WARNING_PERROR("Accept failed on socket");
        return (-1);
    }

    if(socket_address.sin_family == AF_INET)
    {
        connectingHost = gethostbyaddr((char *) &socket_address.sin_addr,
                                       sizeof(struct in_addr), AF_INET);
    

        if(connectingHost != (struct hostent *) 0)
        {
            if(connectingHost->h_name != (char *) 0)
            {
                strncpy(lastHostAccepted, connectingHost->h_name,
                        sizeof(lastHostAccepted) - 1);
                lastHostAccepted[sizeof(lastHostAccepted) - 1] = '\0';
                lastHostInitialized = 1;
            }
            else
            {
                strcpy(lastHostAccepted, "UnknownHostName");
                lastHostInitialized = 1;
            }
        }
        else
        {
            strcpy(lastHostAccepted, "UnknownInternetHost");
            lastHostInitialized = 1;
        }
    }
    else if(socket_address.sin_family == AF_UNIX)
    {
        gethostname(lastHostAccepted, sizeof(lastHostAccepted) - 1);
        lastHostAccepted[sizeof(lastHostAccepted) - 1] = '\0';
        lastHostInitialized = 1;
    }
    else
    {
        strcpy(lastHostAccepted, "UnknownNetwork/Host");
        lastHostInitialized = 1;
    }
    
    return (0);
}


/*------------------------------------------------------------------------
 *      socket_receive()
 *
 *              Tries to receive a buffer full of stuff from a file descriptor
 *
 *      RETURNS:
 *              -1 if error
 *              number of bytes received if no error
 *________________________________________________________________________*/

int socket_receive(
                   int fd,                      /* File descriptor */
                   void *buf,                   /* Data buffer to use */
                   int nbytes)                  /* # of bytes to get */
{
    int bytes_to_receive = nbytes;
    int bytes_received = 0;
    char *receive_pointer = buf;
    int result;

    if(buf == NULL)
    {
        /*
         * Fake an invalid address system error.
         */
        errno = EINVAL;
        WARNING_MESSAGE("socket_receive called with NULL buf");
        return(-1);
    }

    do
    {
        do
        {
            result = read(fd, receive_pointer, bytes_to_receive);
        } while(result == -1 && errno == EINTR);
            
        if(result != -1)
        {
            receive_pointer += result;
            bytes_received += result;
            bytes_to_receive -= result;
        }
        /*
         * Why did I have this here???
         *
         * if(result == -1 && errno == EINTR)
         *    fprintf(stderr, "receive_remote_data EINTR\n");
         */
    } while(bytes_received < nbytes && result > 0);
    /*
     * And why was this part of the while condition???
     *      || (result == -1 && errno == EPIPE));
     */
    
    if(result <= 0)
    {
        if (Debug(sockets))
        {
            sprintf(message,
                    "Error trying to receive %d bytes, received %d, result was %d",
                    nbytes, bytes_received, result);
            if(result == -1)
                WARNING_PERROR(message);
            else
                WARNING_MESSAGE(message);
        }
        return(result);
    }
    return(bytes_received);
}

/*------------------------------------------------------------------------
 *      socket_nb_receive()
 *
 *              Tries to receive a buffer full of stuff from a file descriptor
 *
 *      RETURNS:
 *              negative if error, 0 if EOF, positive if success.
 *
 *              AND, in bytes_received, the number of bytes read on the file.
 *________________________________________________________________________*/

int socket_nb_receive(
                      int fd,           /* File descriptor */
                      void *buf,        /* Data buffer to use */
                      int nbytes,       /* # of bytes to get */
                      int *bytes_received)    /* # of bytes read */
{
    int bytes_to_receive = nbytes;
    char *receive_pointer = buf;
    int result;

    if (bytes_received == (int *)0)
    {
        /* Null pointer!  Error! */
        errno = EINVAL;
        WARNING_PERROR("socket_nb_receive called with bad bytes_recvd ptr.");
        return(-1);
    }

    (*bytes_received) = 0;

    if(buf == NULL)
    {
        /*
         * Fake an invalid address system error.
         */
        errno = EINVAL;
        WARNING_PERROR("socket_nb_receive called with NULL buf");
        return(-1);
    }

    do
    {
        do
        {
            result = read(fd, receive_pointer, bytes_to_receive);
        } while(result == -1 && errno == EINTR);
            
        if(result > 0)
        {
            receive_pointer += result;
            (*bytes_received) += result;
            bytes_to_receive -= result;
        }

        if (result == 0)
            return 0;

    } while((*bytes_received) < nbytes && result > 0);
    
    if(result < 0 && errno != EWOULDBLOCK)
    {
        sprintf(message,
                "Error trying to receive %d bytes, received %d, result was %d",
                nbytes, (*bytes_received), result);
        if(result == -1)
            WARNING_PERROR(message);
        else
            WARNING_MESSAGE(message);
        
        return(result);
    }

    return(1);
}

/*------------------------------------------------------------------------
 *      socket_peek()
 *
 *              Tries to receive a buffer full of stuff from a client.
 *              Does not actually take the data off the socket.
 *
 *      RETURNS:
 *              -1 if error
 *              number of bytes received if no error
 *________________________________________________________________________*/

int socket_peek(
                int fd,                         /* Socket to peek at */
                void *buf,                      /* Data buffer to use */
                int nbytes)                     /* # of bytes to get */
{
    int result;

    if(buf == NULL)
    {
        /*
         * Fake an invalid address system error.
         */
        errno = EINVAL;
        WARNING_PERROR("socket_peek called with NULL buf");
        return(-1);
    }

    do
    {
        result = recv(fd, buf, nbytes, MSG_PEEK);
    } while (result == -1 && errno == EINTR);

    if(result == -1)
    {
        sprintf(message, 
                "socket_peek(): socket connection error, fd = %d ",
                fd);
        WARNING_PERROR(message);
    }

    return(result);
}

/*------------------------------------------------------------------------
 *      socket_test()
 *
 *              Tries to see if the socket is still open
 *              Must have a SIGPIPE handler installed for this to work.
 *
 *      RETURNS:
 *               1 if socket is open
 *              -1 if error
 *________________________________________________________________________*/

int socket_test(
                int fd)                         /* Socket to test */
{
    int result;
    char dummy;
    
    do
    {
        result = recv(fd, &dummy, 1, MSG_PEEK);
    } while(result == -1 && errno == EINTR);
    
    if(result > 0 || result == -1 && errno == EWOULDBLOCK)
        return(1);

    
    return(-1);
}

/*------------------------------------------------------------------------
 *      socket_count()
 *
 *              Tries to return the number of bytes waiting at the socket.
 *
 *      RETURNS:
 *              -1 if error
 *              number of bytes ready if no error
 *________________________________________________________________________*/

int socket_count(int fd)        /* Socket to count bytes on */
{
    int result;
    int nbytes;
    
    result = ioctl(fd, FIONREAD, &nbytes);
    if(result == -1)
    {
        sprintf(message,
                "socket_count(): ioctl FIONREAD error, fd = %d ",
                fd);
        WARNING_PERROR(message);
        return(-1);
    }

    return(nbytes);
}

/*------------------------------------------------------------------------
 *      socket_send()
 *
 *              Tries to send a buffer full of stuff to a file descriptor
 *              If a SIGPIPE error occurs and there is no handler for it,
 *              the program exits without warning!!!
 *
 *      RETURNS:
 *              -1 if error
 *              number of bytes sent if no error
 *________________________________________________________________________*/

int socket_send(
                int fd,                         /* File descriptor */
                void *buf,                      /* Data buffer to use */
                int nbytes)                     /* # of bytes to get */
{
    int bytes_to_send = nbytes;
    int bytes_sent = 0;
    char *send_pointer = buf;
    int result;
    int chunk_size;
    
    if(buf == NULL)
    {
        /*
         * Fake an invalid address system error.
         */
        errno = EINVAL;
        WARNING_PERROR("socket_send called with NULL buf");
        return(-1);
    }

    do
    {
        if(bytes_to_send > 4096)
            chunk_size = 4096;
        else
            chunk_size = bytes_to_send;
        
        do
        {
            result = write(fd, send_pointer, chunk_size);
        } while(result == -1 && errno == EINTR);
            
        if(result != -1)
        {
            send_pointer += result;
            bytes_sent += result;
            bytes_to_send -= result;
        }

    } while(bytes_sent < nbytes && result > 0 );

    if(result <= 0)
    {
        sprintf(message,
                "Error trying to send %d bytes, sent %d, result was %d",
                nbytes, bytes_sent, result);
        if(result == -1)
            WARNING_PERROR(message);
        else
            WARNING_MESSAGE(message);
        
        return(result);
    }
    return(bytes_sent);
}


/*------------------------------------------------------------------------
 *      socket_nb_send()
 *
 *              Tries to send a buffer full of stuff to a file descriptor
 *              If a SIGPIPE error occurs and there is no handler for it,
 *              the program exits without warning!!!
 *
 *              Non-blocking version of socket_send
 *
 *      RETURNS:
 *              -1 if error
 *              number of bytes sent if no error
 *________________________________________________________________________*/

int socket_nb_send(
                   int fd,              /* File descriptor */
                   void *buf,           /* Data buffer to use */
                   int nbytes)          /* # of bytes to get */
{
    int bytes_to_send = nbytes;
    int bytes_sent = 0;
    char *send_pointer = buf;
    int result;
    int chunk_size;
    
    if(buf == NULL)
    {
        /*
         * Fake an invalid address system error.
         */
        errno = EINVAL;
        WARNING_PERROR("socket_send called with NULL buf");
        return(-1);
    }

    do
    {
        if(bytes_to_send > 4096)
            chunk_size = 4096;
        else
            chunk_size = bytes_to_send;
        
        do
        {
            result = write(fd, send_pointer, chunk_size);
        } while(result == -1 && errno == EINTR);
            
        if(result != -1)
        {
            send_pointer += result;
            bytes_sent += result;
            bytes_to_send -= result;
            if(result < chunk_size)
            {
                return(bytes_sent);
            }
        }

        if(result < 0 && errno == EWOULDBLOCK)
        {
            return(bytes_sent);
        }
        
    } while(bytes_sent < nbytes && result > 0 );

    if(result <= 0)
    {
        sprintf(message,
                "Error trying to send %d bytes, sent %d, result was %d",
                nbytes, bytes_sent, result);
        if(result == -1)
            WARNING_PERROR(message);
        else
            WARNING_MESSAGE(message);
        
        return(result);
    }
    return(bytes_sent);
}


/* ------------------------------------------------------------------------
 * 
 * socket_control()
 *
 *      Set or clear the control flags available via the ioctl SETFL/GETFL
 *      calls
 * 
 * RETURNS:  -1 on error
 *           
 * ------------------------------------------------------------------------ */

int socket_control(
                   int fd,              /* Socket to control */
                   int whichFlag,       /* Which flag to set/clear */
                   int what)            /* True = set, False = clear */
{
    int result;
    int oldFlags;
    
    oldFlags = fcntl(fd, F_GETFL, 0);

    if(oldFlags < 0)
    {
        sprintf(message, "Error getting fcntl flags for fd %d", fd);
        WARNING_PERROR(message);
        return(oldFlags);
    }
    
    if(what == True)
    {
        oldFlags |= whichFlag;
    }
    else
    {
        oldFlags &= ~whichFlag;
    }

    result = fcntl(fd, F_SETFL, oldFlags);
    if(result < 0)
    {
        sprintf(message, "Error setting fcntl flags for fd %d", fd);
        WARNING_PERROR(message);
        return(result);
    }

    return(oldFlags);
}

char *ConnectedInternetAddress(int fd)
{
    struct sockaddr_in name;
    int namelen = sizeof(name);
    char hostname[256];
    int result;
    
    result = getpeername(fd, (struct sockaddr *) &name, &namelen);
    if(result < 0)
    {
        sprintf(msgBuf, "Error getting internet address of socket %d", fd);
        WARNING_PERROR(msgBuf);
        return((char *) 0);
    }

    if(name.sin_family == AF_INET)
    {
        return(inet_ntoa(name.sin_addr));
    }
    else
    {
#if !defined(READY_FOR_PRIME_TIME)
        return("127.0.0.1");
#else
        result = gethostname(hostname, sizeof(hostname));
        if(result < 0)
        {
            WARNING_PERROR("Error getting host name of local host");
            return((char *) 0);
        }
        
        if ((host_entry = gethostbyname (hostname)) == NULL)
        {
            sprintf(message, "Couldn't get host entry for %s", hostname);
            WARNING_PERROR(message);
            return ((char *) 0);
        }
        
        /* Check the host entry for validity */
        if (host_entry->h_addrtype != AF_INET)
        {
            sprintf(message,
                    "Got unexpected address type %d in host entry.",
                     host_entry->h_addrtype);
            WARNING_PERROR(message);
            return ((char *) 0);
        }
        
        if(host_entry->h_addr_list && *(host_entry->h_addr_list))
        {
            return(inet_ntoa(host_entry->h_addr_list[0].sin_addr))
        }
#endif
    }
}

char *ConnectedHostname(int fd)
{
    struct sockaddr_in name;
    struct hostent *host_entry;
    int namelen = sizeof(name);
    int result;
    
    result = getpeername(fd, (struct sockaddr *) &name, &namelen);
    if(result < 0)
    {
        sprintf(msgBuf, "Error getting internet address of socket %d", fd);
        WARNING_PERROR(msgBuf);
        return((char *) 0);
    }

    if(name.sin_family == AF_INET)
    {
        host_entry = gethostbyaddr((char *) &name.sin_addr,
                                   sizeof(struct in_addr), AF_INET);
        if(host_entry)
        {
            return(host_entry->h_name);
        }
        else
        {
            sprintf(msgBuf, "Error getting internet name of socket %d", fd);
            WARNING_MESSAGE(msgBuf);
            return((char *) 0);
        }
    }
    else
    {
        return("localhost");
    }
}
