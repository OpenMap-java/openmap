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
# $Source: /cvs/distapps/openmap/Attic/jar.mk,v $
# $RCSfile: jar.mk,v $
# $Revision: 1.1 $
# $Date: 1998/03/16 22:41:45 $
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
