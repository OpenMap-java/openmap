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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/CenterListener.java,v $
// $RCSfile: CenterListener.java,v $
// $Revision: 1.2 $
// $Date: 2003/08/21 20:24:35 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

/**
 * Listens for requests to recenter the map.
 */
public interface CenterListener extends java.util.EventListener {
    public void center(CenterEvent evt);
}
