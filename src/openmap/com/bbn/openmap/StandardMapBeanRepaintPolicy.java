// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/StandardMapBeanRepaintPolicy.java,v $
// $RCSfile: StandardMapBeanRepaintPolicy.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/10 21:57:22 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

/**
 * A MapBeanRepaintPolicy that just forwards layer repaint requests normally.
 */
public class StandardMapBeanRepaintPolicy implements MapBeanRepaintPolicy {

    protected MapBean map;

    public StandardMapBeanRepaintPolicy(MapBean mb) {
	map = mb;
    }

    public void repaint(Layer layer) {
	// No decisions, just forward the repaint() request;
	if (map != null) {
	    map.repaint();
	}
    }
}
