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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/PaintListenerSupport.java,v $
// $RCSfile: PaintListenerSupport.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:05:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.Graphics;

import com.bbn.openmap.util.Debug;

/**
 * This is a utility class that can be used by beans that need support
 * for handling PaintListeners and calling the PaintListener.paint()
 * method. You can use an instance of this class as a member field of
 * your bean and delegate work to it.
 */
public class PaintListenerSupport extends ListenerSupport<PaintListener> {

    /**
     * Construct a PaintListenerSupport.
     */
    public PaintListenerSupport() {
        this(null);
    }

    /**
     * Construct a PaintListenerSupport.
     * 
     * @param source source Object
     */
    public PaintListenerSupport(Object source) {
        super(source);
    }

    /**
     * Send a Paint event to all registered listeners.
     * 
     * @param graphics PaintEvent
     */
    public void paint(Graphics graphics) {

        if (isEmpty())
            return;

        for (PaintListener target : this) {
            if (Debug.debugging("paint")) {
                Debug.output("PaintListenerSupport.paint(): target is: "
                        + target);
            }
            target.listenerPaint(graphics);
        }
    }
}