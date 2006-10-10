# ------------------------------------------------------------------------
# 
#     Use, duplication, or disclosure by the Government is subject to
#  	     restricted rights as set forth in the DFARS.
#   
#  			   BBNT Solutions LLC
#  			      A Part of  
#  			         GTE      
#  			  10 Moulton Street
#  			 Cambridge, MA 02138
#  			    (617) 873-3000
#   
#  	  Copyright 1999-2000 by BBNT Solutions LLC,
#  		A part of GTE, all rights reserved.
#   
#  **********************************************************************
#  
#  $Source: /cvs/distapps/openmap/src/cserver/link/src/makefile,v $
#  $RCSfile: makefile,v $
#  $Revision: 1.2 $
#  $Date: 2006/10/10 22:05:18 $
#  $Author: dietrick $
# ------------------------------------------------------------------------

all:	liblink.a linkserver

SYSTEM_TYPE = solaris2

#COMPILER_HOME	=	/opt/SUNWspro
COMPILER_HOME	=	/usr/local/gnu
LDLIBS 	 	=	-ltoolLib -lsocket -lnsl
CXX_AR		=	$(COMPILER_HOME)/bin/ar rcv

# MAC OS X ------
#COMPILER_HOME	=	/usr
#LDLIBS 	=	-ltoolLib
#CXX_AR		=	$(COMPILER_HOME)/bin/ar -rsv
# ----------------

#CC		=	$(COMPILER_HOME)/bin/cc
CC		=	$(COMPILER_HOME)/bin/gcc -Wall

CXXFLAGS  	=	-I../include -I../../include -DSOLARIS=1 \
			-DMULTICAST=1 -DSTDC_HEADERS=1  -DHAVE_FCNTL_H=1  -DHAVE_ERRNO_H=1 \
			-DHAVE_STDLIB_H=1  -DHAVE_LIMITS_H=1  -DHAVE_UNISTD_H=1 \
			-DHAVE_SYS_STREAM_H=1  -DHAVE_SYS_UN_H=1  -DHAVE_SYS_TIME_H=1 \
			-DHAVE_TOLOWER=1  -DHAVE_STRING_H=1  -DTIME_WITH_SYS_TIME=1

LDFLAGS 	=	-L../../lib -L$(COMPILER_HOME)/lib

SERVERM.c 	=	LinkServer.c
SERVERM.o 	=	$(SERVERM.c:.c=.o)  

SERVER.c 	=	LatLonPoint.c Response.c \
			LinkBitmap.c LinkCircle.c LinkEllipse.c LinkRectangle.c LinkText.c\
			LinkSocket.c LinkArgs.c LinkPoly.c LinkGrid.c\
			Request.c LinkLine.c LinkRaster.c LinkImage.c \
			MapRequest.c ActionRequest.c Link.c LinkPoint.c

SERVER.o 	=	$(SERVER.c:.c=.o)

linkserver:	$(SERVER.o) $(SERVERM.o)
		$(CC) $(CXXFLAGS) $(LDFLAGS) -o $@ \
		$(SERVERM.o) $(SERVER.o) $(LDLIBS)
		mkdir -p ../../bin
		cp linkserver ../../bin

liblink.a:	$(SERVER.o)
		$(CXX_AR) liblink.a $(SERVER.o)
		mkdir -p ../../lib
		mkdir -p ../../include
		cp liblink.a ../../lib
		cp -r ../include ../../include/link

# compile using Purify Software's profiler
linkserver.pure:	$(SERVER.o) $(SERVERM.o)
			purify -best-effort $(CC)  $(CXXFLAGS) $(LDFLAGS) -o $@ \
			$(SERVERM.o) $(SERVER.o) $(LDLIBS)

.c:
	$(CC)  -o $(LDFLAGS) $(CXXFLAGS) $* $*.c
.c.o:
	$(CC) -c $(LDFLAGS) $(CXXFLAGS) $*.c

# what gets removed on a "make clean"
CLEANUPS =	linkserver *.o *.a *~

RM =		/bin/rm -rf

clean:
	$(RM) $(CLEANUPS)



