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
# $Source: /cvs/distapps/openmap/Attic/jar.mk,v $
# $RCSfile: jar.mk,v $
# $Revision: 1.3 $
# $Date: 1998/07/22 19:06:29 $
# $Author: rmcneil $
# 
# **********************************************************************


#############
# variables #
#############
SRCDIR =	com/bbn/openmap


# jar up OpenMap, Java libCspec, Java libCspec sample, IDL generated
# code
OPENMAP.JAR =	$(SRCDIR)/*.class $(SRCDIR)/app/*.class \
		$(SRCDIR)/corba/*.class $(SRCDIR)/dcwSpecialist/*.class \
		$(SRCDIR)/event/*.class \
		$(SRCDIR)/gui/*.class \
		$(SRCDIR)/html/*.class \
		$(SRCDIR)/http/*.class \
		$(SRCDIR)/omGraphics/*.class \
		$(SRCDIR)/overlay/*.class \
		$(SRCDIR)/pluginLayer/*.class \
		$(SRCDIR)/proj/*.class \
		$(SRCDIR)/specialist/*.class $(SRCDIR)/util/*.class \
		com/bbn/util/*.class \
		$(SRCDIR)/gui/*.gif

APPLET.JAR =	$(SRCDIR)/*.class $(SRCDIR)/awt/*.class \
		$(SRCDIR)/client/*.class $(SRCDIR)/proj/*.class \
		$(SRCDIR)/spec/*.class $(SRCDIR)/kfc/*.class \
		$(SRCDIR)/CSpecialist/*.class \
		$(SRCDIR)/CSpecialist/*/*.class
