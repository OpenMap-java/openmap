# **********************************************************************
# 
#    Use, duplication, or disclosure by the Government is subject to
# 	     restricted rights as set forth in the DFARS.
#  
# 			   BBN Technologies
# 			    A Division of
# 			   BBN Corporation
# 			  10 Moulton Street
# 			 Cambridge, MA 02138
# 			    (617) 873-3000
#  
# 	  Copyright 1998 by BBN Technologies, A Division of
# 		BBN Corporation, all rights reserved.
#  
# **********************************************************************
# 
# $Source: /cvs/distapps/openmap/Attic/openmap.mk,v $
# $RCSfile: openmap.mk,v $
# $Revision: 1.2 $
# $Date: 1998/03/17 18:10:21 $
# $Author: tmitchel $
# 
# **********************************************************************


SRCDIR =	com/bbn/openmap
SRC_PACKAGE =	com.bbn.openmap

allsubdirs:
	@for dir in $(SUBDIRS); do \
	  echo "Making all in $$dir..."; \
	  cd $$dir; \
	  $(MAKE) all; \
	  cd ..; \
	  echo "Done making all in $$dir."; \
	done

cleansubdirs:
	@for dir in $(SUBDIRS); do \
	  echo "Making clean in $$dir..."; \
	  cd $$dir; \
	  $(MAKE) clean; \
	  cd ..; \
	  echo "Done making clean in $$dir."; \
	done

installsubdirs:
	@for dir in $(SUBDIRS); do \
	  echo "Making install in $$dir..."; \
	  cd $$dir; \
	  $(MAKE) install; \
	  cd ..; \
	  echo "Done making install in $$dir."; \
	done
