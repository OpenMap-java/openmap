# Generated automatically from Makefile.in by configure.
# **********************************************************************
# 
#    Use, duplication, or disclosure by the Government is subject to
# 	     restricted rights as set forth in the DFARS.
#  
# 			   BBNT Solutions LLC
# 			      A Part of  
# 			         GTE      
# 			  10 Moulton Street
# 			 Cambridge, MA 02138
# 			    (617) 873-3000
#  
# 	  Copyright 1998, 2000 by BBNT Solutions LLC,
# 		A part of GTE, all rights reserved.
#  
# **********************************************************************
# 
# $Source: /cvs/distapps/openmap/src/cserver/makefile,v $
# $Revision: 1.1.1.1 $
# $Date: 2003/02/14 21:35:48 $
# $Author: dietrick $
# 
# **********************************************************************



all: 
	cd toolLib/src && $(MAKE) $@
	cd link/src && $(MAKE) $@

linkserver.pure:
	cd link/src && $(MAKE) linkserver.pure

clean distclean::
	cd toolLib/src && $(MAKE) $@
	cd link/src && $(MAKE) $@
	cd link/include && rm -rf *~
	cd bin && rm -rf *
	cd include && rm -rf *
	cd lib && rm -rf *
	rm -rf bin include lib

