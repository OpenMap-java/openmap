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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/Taskable.java,v $
// $RCSfile: Taskable.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.awt.event.ActionListener;

/**
 * Taskables are ActionListeners that respond to events from Timers.
 * If a Taskable has been added to a Timer's queue, it will receive
 * <code>actionPerformed()</code> notifications each time the timer
 * expires.
 * <p>
 * Some OpenMap Layers are Taskables. These layers expect to refresh
 * their graphics at a certain rate.
 * 
 * @see javax.swing.Timer
 */
public interface Taskable extends ActionListener {

    /**
     * Get the sleep hint in milliseconds. The Taskable implementation
     * should determine the sleep (delay) interval between invocations
     * of its <code>actionPerformed()</code>.
     * <p>
     * NOTE: this is only a hint for the timer. It's the Taskable's
     * responsibility to determine if too little or too much time has
     * elapsed between invocations of <code>actionPerformed()</code>.
     * 
     * @return int milliseconds of sleep interval
     */
    public int getSleepHint();
}