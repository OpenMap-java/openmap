// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ZoomListener.java,v $
// $RCSfile: ZoomListener.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * Listens for requests to zoom the map in and out.
 */
public interface ZoomListener extends java.util.EventListener {
    public void zoom(ZoomEvent evt);
}