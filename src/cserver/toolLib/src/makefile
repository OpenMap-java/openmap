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
#  $Source: /cvs/distapps/openmap/src/cserver/toolLib/src/makefile,v $
#  $RCSfile: makefile,v $
#  $Revision: 1.1.1.1 $
#  $Date: 2003/02/14 21:35:48 $
#  $Author: dietrick $
# ------------------------------------------------------------------------

all:	libtoolLib.a

SYSTEM_TYPE = solaris2

#COMPILER_HOME	=	/opt/SUNWspro
COMPILER_HOME	=	/usr/local/gnu
LDLIBS 	 	=	-Bstatic -lsocket -lnsl
CXX_AR		=	$(COMPILER_HOME)/bin/ar rcv

# MAC OS X ------
#COMPILER_HOME	=	/usr
#LDLIBS 	=	-Bstatic
#CXX_AR		=	$(COMPILER_HOME)/bin/ar -rsv
# ----------------

#CC		=	$(COMPILER_HOME)/bin/cc
CC		=	$(COMPILER_HOME)/bin/gcc

CXXFLAGS  	=	-I../include

LDFLAGS 	=	-L. -L$(COMPILER_HOME)/lib

OS_CXXFLAGS	=	$(OS_CPPFLAGS) -DSOLARIS=1  -DMULTICAST=1 \
			-DSTDC_HEADERS=1  -DHAVE_FCNTL_H=1  -DHAVE_ERRNO_H=1 \
			-DHAVE_STDLIB_H=1  -DHAVE_LIMITS_H=1  -DHAVE_UNISTD_H=1 \
			-DHAVE_SYS_STREAM_H=1  -DHAVE_SYS_UN_H=1  -DHAVE_SYS_TIME_H=1 \
			-DHAVE_TOLOWER=1  -DHAVE_STRING_H=1  -DTIME_WITH_SYS_TIME=1

CXXFLAGS  	=	-I../include -I/usr/openwin/include $(OS_CXXFLAGS)

LDFLAGS 	=	-L. -L/usr/dt/lib -L/usr/openwin/lib

TOOLLIB.c =	auto_input.c auto_output.c buffers.c error_hand.c \
		free_mgr.c sockets.c stringutil.c \
		Magic.c mapped_files.c compress.c CTag.c

TOOLLIB.h =	auto_input.h auto_output.h buffers.h compat.h \
		debugging.h error_hand.h free_mgr.h listtools.h \
		plumbing.h sockets.h stringutil.h style.h \
		Magic.h mapped_files.h compress.h

TOOLLIB.o =	${TOOLLIB.c:.c=.o}

libtoolLib.a:	$(TOOLLIB.o)
		$(CXX_AR) libtoolLib.a $(TOOLLIB.o)
		mkdir -p ../../lib
		mkdir -p ../../include
		cp libtoolLib.a ../../lib
		cp -r ../include ../../include/toolLib

.c:
	$(CC)  -o $(LDFLAGS) $(CXXFLAGS) $* $*.c
.c.o:
	$(CC) -c $(LDFLAGS) $(CXXFLAGS) $*.c

# what gets removed on a "make clean"
CLEANUPS =	linkserver *.o

RM =		/bin/rm -rf

clean:
	$(RM) $(CLEANUPS)



