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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/MapBeanRepaintPolicy.java,v $
// $RCSfile: MapBeanRepaintPolicy.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/10 21:57:22 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

/**
 * A MapBeanRepaintPolicy is a policy object that makes decisions on
 * how a MapBean handles repaint requests from a layer.  It can
 * forward them on to the Swing thread by calling MapBean.repaint(),
 * or ignore them until conditions that it considers valuable are met.
 */
public interface MapBeanRepaintPolicy {

    /**
     * Take some action based on a repaint request from this
     * particular layer.
     */
    public void repaint(Layer layer);

}
