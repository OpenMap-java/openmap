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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/PanSupport.java,v $
// $RCSfile: PanSupport.java,v $
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
 * for handling PanListeners and firing PanEvents.  You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class PanSupport implements java.io.Serializable {

    /**
     * Construct a PanSupport.
     * @param sourceBean The bean to be given as the source for any events.
     */
    public PanSupport(Object sourceBean) {
	source = sourceBean;
    }

    /**
     * Add a PanListener to the listener list.
     * @param listener The PanListener to be added
     */
    public synchronized void addPanListener(PanListener listener) {
	if (listeners == null) {
	    listeners = new java.util.Vector();
	}
	listeners.addElement(listener);
    }

    /**
     * Remove a PanListener from the listener list.
     * @param listener The PanListener to be removed
     */
    public synchronized void removePanListener(PanListener listener) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(listener);
    }

    /**
     * Send a pan event to all registered listeners.
     * @param direction PanEvent.NORTH ... PanEvent.NORTH_WEST
     * @see PanEvent
     * @deprecated use firePan(azimuth)
     */
    public void firePan(int direction) {
	firePan(direction, 1f);
    }

    /**
     * Send a pan event to all registered listeners.
     *
     * @param direction PanEvent.NORTH ... PanEvent.NORTH_WEST
     * @param amount (0.0 &lt;= amount) in decimal degrees.
     * @see PanEvent
     * @deprecated use firePan(azimuth, arc)
     */
    public void firePan(int direction, float amount) {

	if (direction < PanEvent.PAN_FIRST || direction > PanEvent.PAN_LAST) {
	    throw new IllegalArgumentException("Bad value, " + direction +
					       " for direction in " +
					       "PanSupport.firePan()");
	}

	float az = PanEvent.dir2Az(direction);
	firePan(az);
    }

    public void firePan(float Az) {
	firePan(Az, Float.NaN);
    }

    /**
     * Fire a pan event.
     * @param Az azimuth "east of north" in decimal degrees:
     * <code>-180 &lt;= Az &lt;= 180</code>
     * @param c arc distance in decimal degrees.
     */
    public void firePan(float az, float c) {
	java.util.Vector targets;
	synchronized (this) {
	    if (listeners == null) {
	    	return;
	    }
	    targets = (java.util.Vector) listeners.clone();
	}
        PanEvent evt = new PanEvent(source, az, c);

	for (int i = 0; i < targets.size(); i++) {
	    PanListener target = (PanListener)targets.elementAt(i);
	    target.pan(evt);
	}
    }

    private void writeObject(ObjectOutputStream s)
	throws IOException {

        s.defaultWriteObject();

	java.util.Vector v = null;
	synchronized (this) {
	    if (listeners != null) {
	        v = (java.util.Vector) listeners.clone();
            }
	}

	if (v != null) {
	    for(int i = 0; i < v.size(); i++) {
	        PanListener l = (PanListener)v.elementAt(i);
	        if (l instanceof Serializable) {
	            s.writeObject(l);
	        }
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) 
	throws ClassNotFoundException, IOException {

        s.defaultReadObject();
      
        Object listenerOrNull;
        while(null != (listenerOrNull = s.readObject())) {
	  addPanListener((PanListener)listenerOrNull);
        }
    }

    transient private java.util.Vector listeners;
    private Object source;
    private int panSupportSerializedDataVersion = 1;
}




