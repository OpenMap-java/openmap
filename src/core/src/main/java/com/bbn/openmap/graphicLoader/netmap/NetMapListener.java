// **********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapGraphicLoader.java,v
// $
//$RCSfile: NetMapListener.java,v $
//$Revision: 1.2 $
//$Date: 2004/10/14 18:05:47 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

public interface NetMapListener {

    public void catchEvent(NetMapEvent event);

}