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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/ProgressSupport.java,v $
// $RCSfile: ProgressSupport.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.util.Debug;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * This is a utility class that can be used by beans that need support
 * for handling ProgressListeners and firing ProgressEvents.  You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class ProgressSupport implements Serializable {

    /**
     * Construct a ProgressSupport.
     * @param sourceBean  The bean to be given as the source for any events.
     */
    public ProgressSupport(Object sourceBean) {
	source = sourceBean;
	Debug.message("progresssupport","ProgressSupport | ProgressSupport");
    }

    /**
     * Add a ProgressListener to the listener list.
     *
     * @param listener  The ProgressListener to be added
     */
    public synchronized void addProgressListener(ProgressListener listener) {
	if (listeners == null) {
	    listeners = new Vector();
	}
	if (!listeners.contains(listener)) {
	    listeners.addElement(listener);
	    if (Debug.debugging("progresssupport")) {
		Debug.output("ProgressSupport | addProgressListener " + listener.getClass() + " was added");
	    }
	}
    }

    /**
     * Remove a ProgressListener from the listener list.
     *
     * @param listener  The ProgressListener to be removed
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(listener);
    }

    public void removeAll() {
	listeners.clear();
    }

    /**
     * Send a layer event to all registered listeners.
     *
     * @param taskname the description of the task
     * @param finishedValue the completed value
     * @param currentValue the currentValue
     */
    public void fireUpdate(int type, String taskname,
			   float finishedValue, 
			   float currentValue) {
	Debug.message("progresssupport","ProgressSupport | fireUpdate");
	Vector targets;

	boolean DEBUG = Debug.debugging("progresssupport");

	synchronized (this) {
	    if (listeners == null) {
	    	return;
	    }

	    if (DEBUG) {
		Debug.output("ProgressSupport | fireUpdate has " + listeners.size() + " listeners");
	    }
	    targets = (Vector) listeners.clone();

	    if (DEBUG) {
		Debug.output("ProgressSupport | fireUpdate has " + targets.size() + " listeners after cloning");
	    }
	}

        ProgressEvent evt = new ProgressEvent(source, type, taskname,
					      finishedValue, currentValue);

	if (DEBUG) {
	    Debug.output("calling updateProgress on " + targets.size() + " objects");
	}

	for (int i = 0; i < targets.size(); i++) {
	    ProgressListener target = (ProgressListener)targets.elementAt(i);
	    target.updateProgress(evt);
	}
    }

    private void writeObject(ObjectOutputStream s) 
	throws IOException {

        s.defaultWriteObject();

	Vector v = null;
	synchronized (this) {
	    if (listeners != null) {
	        v = (Vector) listeners.clone();
            }
	}

	if (v != null) {
	    for(int i = 0; i < v.size(); i++) {
	        ProgressListener l = (ProgressListener)v.elementAt(i);
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
	    addProgressListener((ProgressListener)listenerOrNull);
        }
    }

    transient private Vector listeners;
    private Object source;
    private int progresssupportSerializedDataVersion = 1;
}
