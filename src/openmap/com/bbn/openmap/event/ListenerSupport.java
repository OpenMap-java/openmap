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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ListenerSupport.java,v $
// $RCSfile: ListenerSupport.java,v $
// $Revision: 1.2 $
// $Date: 2003/10/10 15:40:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.util.Debug;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * This is an utility class that can be used by beans that need
 * support for handling listeners and firing events.  You can use an
 * subclass of this class as a member field of your bean and delegate
 * work to it.  It handles the work for a listener support subclass
 * managing the Vector of listeners.  It knows nothing about firing
 * events to the listeners.
 */
public class ListenerSupport implements java.io.Serializable {

    transient protected Vector listeners;

    protected Object source;

    /**
     * Construct a ListenerSupport object.
     * @param sourceBean  The bean to be given as the source for any events.
     */
    public ListenerSupport(Object sourceBean) {
	setSource(sourceBean);
	Debug.message("listenersupport","ListenerSupport()");
    }

    /**
     * Set the source of the events.
     */
    protected void setSource(Object src) {
	source = src;
    }

    /**
     * Get the source of the events.
     */
    protected Object getSource() {
	return source;
    }

    /**
     * Add an Object to the listener list.
     *
     * @param listener The listener object to be added
     */
    public synchronized void addListener(Object listener) {
	if (listeners == null) {
	    listeners = new Vector();
	}

	if (!listeners.contains(listener)) {
	    listeners.addElement(listener);
	    if (Debug.debugging("listenersupport")) {
		Debug.output("ListenerSupport | addListener " + listener.getClass() + " was added");
	    }
	}
    }

    /**
     * Remove an Object from the listener list.
     *
     * @param listener  The Object to be removed
     */
    public synchronized void removeListener(Object obj) {
	if (listeners != null) {
	    listeners.removeElement(obj);
	}
    }

    /**
     * Clear the listener list.
     */
    public void removeAll() {
	if (listeners != null) {
	    listeners.clear();
	}
    }

    /**
     * Return an iterator over a clone of the listeners.  If listeners
     * is null, it will return an empty iterator.
     */
    public synchronized Iterator iterator() {
	if (listeners != null) {
	    return new ArrayList(listeners).iterator();
	}
	// Failsafe, return an empty iterator
	return new ArrayList().iterator();
    }

    /**
     * Return the number of listeners.
     */
    public int size() {
	int size = 0;
	if (listeners != null) {
	    size = listeners.size();
	}

	return size;
    }

    /**
     * Return a cloned list of Listeners.
     * @return List of listeners, null if none have been added.
     */
    public synchronized List getListeners() {
	if (listeners == null) {
	    return null;
	}

	return (List) listeners.clone();
    }

    /**
     * Write the listeners to a stream.
     */
    public void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

	List v = getListeners();
	if (v != null) {
	    Iterator it = v.iterator();
	    while (it.hasNext()) {
	        Object obj = it.next();
	        if (obj instanceof Serializable) {
	            s.writeObject(obj);
	        }
            }
        }
        s.writeObject(null);
    }


    /**
     * Read the listeners from a stream.
     */
    public void readObject(ObjectInputStream s) 
	throws ClassNotFoundException, IOException {

        s.defaultReadObject();
      
        Object listenerOrNull;
        while (null != (listenerOrNull = s.readObject())) {
	    addListener(listenerOrNull);
        }
    }
}
