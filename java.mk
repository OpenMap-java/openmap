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
# $Source: /cvs/distapps/openmap/Attic/java.mk,v $
# $RCSfile: java.mk,v $
# $Revision: 1.7 $
# $Date: 1998/10/14 15:51:42 $
# $Author: tmitchel $
# 
# **********************************************************************


# add some new suffixes for java related files
.SUFFIXES: .java .class

# Java compiler (may be jvc for Microsoft compiler)
JC = javac

# Java compiler flags for optimizing
JFLAGS_OPT = -J-mx128m -O

# Java compiler flags for development
JFLAGS_DEV = -J-mx128m -g -deprecation

# Set depending on development vs. optimization
JFLAGS = $(JFLAGS_OPT)
JFLAGS = $(JFLAGS_DEV)


# *.java to *.class make rule
.java.class:
	$(JC) $(JFLAGS) $<


# GNU install
INSTALL.dir =		install --directory
INSTALL.class =		install --mode=444
INSTALL.image =		install --mode=444
INSTALL.props =		install --mode=444
