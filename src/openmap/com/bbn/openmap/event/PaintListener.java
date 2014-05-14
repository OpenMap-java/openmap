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

import java.awt.Graphics;

/**
 * A PaintListener is an object that wants to know when another object
 * is painted with a java.awt.Graphics object.
 */
public interface PaintListener {

    /**
     * Method called when the source object is painted.
     * @param source the source object, may be null, you need to check.
     * @param graphics the graphics to paint into.
     */
    public void listenerPaint(Object source, Graphics graphics);

}