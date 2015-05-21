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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProjectionListener.java,v $
// $RCSfile: ProjectionListener.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * Interface for listening to ProjectionEvents.
 * <p>
 * ProjectionEvent is fired when something fundamental about the
 * MapBean changes (e.g. when width, height, scale, type, center, etc
 * changes).
 */
public interface ProjectionListener extends java.util.EventListener {

    /**
     * Invoked when there has been a fundamental change to the Map.
     * <p>
     * Layers are expected to recompute their graphics (if this makes
     * sense), and then <code>repaint()</code> themselves.
     * 
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e);
}