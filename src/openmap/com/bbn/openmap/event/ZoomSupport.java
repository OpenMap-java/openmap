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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ZoomSupport.java,v $
// $RCSfile: ZoomSupport.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


/**
 * This is a utility class that can be used by beans that need support
 * for handling ZoomListeners and firing ZoomEvents.  You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class ZoomSupport implements java.io.Serializable {

    /**
     * Construct a ZoomSupport.
     * @param sourceBean The bean to be given as the source for any events.
     */
    public ZoomSupport(Object sourceBean) {
	source = sourceBean;
    }

    /**
     * Add a ZoomListener to the listener list.
     * @param listener The ZoomListener to be added
     */
    public synchronized void addZoomListener(ZoomListener listener) {
	if (listeners == null) {
	    listeners = new java.util.Vector();
	}
	listeners.addElement(listener);
    }

    /**
     * Remove a ZoomListener from the listener list.
     * @param listener The ZoomListener to be removed
     */
    public synchronized void removeZoomListener(ZoomListener listener) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(listener);
    }

    /**
     * Send a zoom event to all registered listeners.
     * @param zoomType Either ZoomEvent.RELATIVE or ZoomEvent.ABSOLUTE
     * @param amount The new scale if ABSOLUTE, the multiplier if RELATIVE
     */
    public void fireZoom(int zoomType, float amount) {

	if (! ((zoomType == ZoomEvent.RELATIVE) ||
	       (zoomType == ZoomEvent.ABSOLUTE))) {
	    throw new IllegalArgumentException("Bad value, " + zoomType +
					       " for zoomType in " +
					       "ZoomSupport.fireZoom()");
	}

	java.util.Vector targets;
	synchronized (this) {
	    if (listeners == null) {
	    	return;
	    }
	    targets = (java.util.Vector) listeners.clone();
	}
        ZoomEvent evt = new ZoomEvent(source, zoomType, amount);

	for (int i = 0; i < targets.size(); i++) {
	    ZoomListener target = (ZoomListener)targets.elementAt(i);
	    target.zoom(evt);
	}
    }


    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

	java.util.Vector v = null;
	synchronized (this) {
	    if (listeners != null) {
	        v = (java.util.Vector) listeners.clone();
            }
	}

	if (v != null) {
	    for(int i = 0; i < v.size(); i++) {
	        ZoomListener l = (ZoomListener)v.elementAt(i);
	        if (l instanceof Serializable) {
	            s.writeObject(l);
	        }
            }
        }
        s.writeObject(null);
    }


    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
      
        Object listenerOrNull;
        while(null != (listenerOrNull = s.readObject())) {
	  addZoomListener((ZoomListener)listenerOrNull);
        }
    }

    transient private java.util.Vector listeners;
    private Object source;
    private int zoomSupportSerializedDataVersion = 1;
}
