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
# $Revision: 1.2 $
# $Date: 1998/03/17 18:10:20 $
# $Author: tmitchel $
# 
# **********************************************************************


#############
# variables #
#############
SRCDIR =	com/bbn/openmap


# jar up OpenMap, Java libCspec, Java libCspec sample, IDL generated
# code
OPENMAP.JAR =	$(SRCDIR)/*.class $(SRCDIR)/awt/*.class \
		$(SRCDIR)/client/*.class $(SRCDIR)/html/*.class \
		$(SRCDIR)/http/*.class $(SRCDIR)/proj/*.class \
		$(SRCDIR)/spec/*.class $(SRCDIR)/kfc/*.class \
		$(SRCDIR)/streamlayer/*.class \
		$(SRCDIR)/streamlayer/sample/*.class \
		$(SRCDIR)/specialist/*.class \
		$(SRCDIR)/specialist/sample/*.class \
		$(SRCDIR)/CSpecialist/*.class \
		$(SRCDIR)/CSpecialist/*/*.class

APPLET.JAR =	$(SRCDIR)/*.class $(SRCDIR)/awt/*.class \
		$(SRCDIR)/client/*.class $(SRCDIR)/proj/*.class \
		$(SRCDIR)/spec/*.class $(SRCDIR)/kfc/*.class \
		$(SRCDIR)/CSpecialist/*.class \
		$(SRCDIR)/CSpecialist/*/*.class
