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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/PaintListener.java,v $
// $RCSfile: PaintListener.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * A PaintListener is an object that wants to know when another object
 * is painted();
 */
public interface PaintListener {

    public void listenerPaint(java.awt.Graphics graphics);

}