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
# $Source: /cvs/distapps/openmap/Attic/java.mk,v $
# $RCSfile: java.mk,v $
# $Revision: 1.1 $
# $Date: 1998/03/16 22:41:45 $
# $Author: tmitchel $
# 
# **********************************************************************

# add some new suffixes for java related files
.SUFFIXES: .java .class

# Java compiler (may be jvc for Microsoft compiler)
JC = javac

# Java compiler flags for optimizing
JFLAGS_OPT = -O

# Java compiler flags for development
JFLAGS_DEV = -g -deprecation

# Set depending on development vs. optimization
JFLAGS = $(JFLAGS_OPT)
JFLAGS = $(JFLAGS_DEV)


# *.java to *.class make rule
.java.class:
	$(JC) $(JFLAGS) $<
