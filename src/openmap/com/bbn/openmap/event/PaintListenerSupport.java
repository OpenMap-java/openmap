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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/PaintListenerSupport.java,v $
// $RCSfile: PaintListenerSupport.java,v $
// $Revision: 1.5 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.util.Debug;
import java.awt.Graphics;
import java.util.Iterator;

/**
 * This is a utility class that can be used by beans that need support
 * for handling PaintListeners and calling the PaintListener.paint()
 * method.  You can use an instance of this class as a member field of
 * your bean and delegate work to it.  
 */
public class PaintListenerSupport extends ListenerSupport {

    /**
     * Construct a PaintListenerSupport.
     */
    public PaintListenerSupport() {
        this(null);
    }

    /**
     * Construct a PaintListenerSupport.
     * @param source source Object
     */
    public PaintListenerSupport(Object source) {
        super(source);
    }

    /**
     * Add a PaintListener.
     * @param l PaintListener
     */
    public void addPaintListener(PaintListener l) {
        addListener(l);
    }

    /**
     * Remove a PaintListener.
     * @param l PaintListener
     */
    public void removePaintListener(PaintListener l) {
        removeListener(l);
    }

    /**
     * Send a Paint event to all registered listeners.
     *
     * @param graphics PaintEvent
     */
    public void paint(Graphics graphics) {

        if (size() == 0) return;
        Iterator it = iterator();

        while (it.hasNext()) {
            PaintListener target = (PaintListener)it.next();
            if (Debug.debugging("paint")) {
                Debug.output("PaintListenerSupport.paint(): target is: " + 
                             target);
            }
            target.listenerPaint(graphics);
        }
    }
}
