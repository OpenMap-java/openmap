# **********************************************************************
# 
# <copyright>
# 
#  BBN Technologies, a Verizon Company
#  10 Moulton Street
#  Cambridge, MA 02138
#  (617) 873-8000
# 
#  Copyright (C) BBNT Solutions LLC. All rights reserved.
# 
# </copyright>
# **********************************************************************
# 
# $Source: /cvs/distapps/openmap/Attic/corba.mk,v $
# $RCSfile: corba.mk,v $
# $Revision: 1.3 $
# $Date: 2002/05/08 22:32:36 $
# $Author: dietrick $
# 
# **********************************************************************

# This file sets a new rule for building java files that need a
# different CORBA implementation than was included with the jdk.  You
# can set the CLASSPATH to reflect whatever you want, and if you
# reference this file in a certain Makefile, after the reference to
# openmap.mk, the classpath will be adjusted accordingly for all
# classes requiring a CORBA implementation.  The Visibroker 3.4 path
# included here is for an example, or in our case, it's what we use.

# *.java to *.class make rule
.java.class:
	$(JAVAC) -bootclasspath ${bootclasspath} $<

# Our specifics
JAVA_HOME = /usr/local/java
VISIBROKER_HOME = /usr/local/vbroker

CORBA_CLASSES = ${VISIBROKER_HOME}/lib/vbjapp.jar:${VISIBROKER_HOME}/lib/vbjcosnm.jar:${VISIBROKER_HOME}/lib/vbjtools.jar:${VISIBROKER_HOME}/lib/vbjorb.jar

# bootclasspath = <path to the corba jars (like Visibroker);	\
# development path to openmap; path to runtime jar (rt.jar)>
bootclasspath =${CORBA_CLASSES}:${CLASSPATH}:${JAVA_HOME}/jre/lib/rt.jar

