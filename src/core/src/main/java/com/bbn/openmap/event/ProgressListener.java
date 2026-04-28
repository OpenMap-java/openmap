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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProgressListener.java,v $
// $RCSfile: ProgressListener.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * Listens for ProgressEvents from something doing some work.
 */
public interface ProgressListener extends java.util.EventListener {
    /**
     * Intermediate updates.
     */
    public void updateProgress(ProgressEvent evt);

}

