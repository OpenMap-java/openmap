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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/CenterSupport.java,v $
// $RCSfile: CenterSupport.java,v $
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
 * for handling CenterListeners and firing CenterEvents  You can use
 * an instance of this class as a member field of your bean and
 * delegate work to it.
 * <p>
 * A center event is one that sets the center of a map by specifying
 * latitude and longitude.
 */
public class CenterSupport implements java.io.Serializable {

    /**
     * @sourceBean  The bean to be given as the source for any events.
     */
    public CenterSupport(Object sourceBean) {
	source = sourceBean;
    }

    /**
     * Add a CenterListener to the listener list.
     *
     * @param listener  The CenterListener to be added
     */
    public synchronized void addCenterListener(CenterListener listener) {
	if (listeners == null) {
	    listeners = new java.util.Vector();
	}
	listeners.addElement(listener);
    }

    /**
     * Remove a CenterListener from the listener list.
     *
     * @param listener  The CenterListener to be removed
     */
    public synchronized void removeCenterListener(CenterListener listener) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(listener);
    }

    /**
     * Send a center event to all registered listeners.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @see CenterEvent
     */
    public void fireCenter(float latitude, float longitude) {

	java.util.Vector targets;
	synchronized (this) {
	    if (listeners == null) {
	    	return;
	    }
	    targets = (java.util.Vector) listeners.clone();
	}
        CenterEvent evt = new CenterEvent(source, latitude, longitude);

	for (int i = 0; i < targets.size(); i++) {
	    CenterListener target = (CenterListener)targets.elementAt(i);
	    target.center(evt);
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
	        CenterListener l = (CenterListener)v.elementAt(i);
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
	  addCenterListener((CenterListener)listenerOrNull);
        }
    }

    transient private java.util.Vector listeners;
    private Object source;
    private int centerSupportSerializedDataVersion = 1;
}
