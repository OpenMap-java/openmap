# **********************************************************************
# 
#  BBN Corporation
#  10 Moulton St.
#  Cambridge, MA 02138
#  (617) 873-2000
# 
#  Copyright (C) 1997
#  This software is subject to copyright protection under the laws of 
#  the United States and other countries.
# 
# **********************************************************************
# 
# $Source: /cvs/distapps/openmap/Attic/openmap.mk,v $
# $RCSfile: openmap.mk,v $
# $Revision: 1.1 $
# $Date: 1998/03/16 22:41:45 $
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
