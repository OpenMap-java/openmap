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
# $Source: /cvs/distapps/openmap/Attic/Makefile,v $
# $RCSfile: Makefile,v $
# $Revision: 1.1 $
# $Date: 2002/07/11 19:26:50 $
# $Author: dietrick $
# 
# **********************************************************************


default: all

all:
	cd ${OPENMAP_HOME} && ant -emacs $@

classes:
	cd ${OPENMAP_HOME} && ant -emacs $@

clean:
	cd ${OPENMAP_HOME} && ant -emacs $@
