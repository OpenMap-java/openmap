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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/LayerSupport.java,v $
// $RCSfile: LayerSupport.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.util.Debug;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * This is a utility class that can be used by beans that need support
 * for handling LayerListeners and firing LayerEvents.  You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class LayerSupport implements java.io.Serializable {

    /**
     * Construct a LayerSupport.
     * @param sourceBean  The bean to be given as the source for any events.
     */
    public LayerSupport(Object sourceBean) {
	source = sourceBean;
	Debug.message("layersupport","LayerSupport | LayerSupport");
    }

    /**
     * Add a LayerListener to the listener list.
     *
     * @param listener  The LayerListener to be added
     */
    public synchronized void addLayerListener(LayerListener listener) {
	if (listeners == null) {
	    listeners = new java.util.Vector();
	}
	if (!listeners.contains(listener)) {
	    listeners.addElement(listener);
	    if (Debug.debugging("layersupport")) {
		Debug.output("LayerSupport | addLayerListener " + listener.getClass() + " was added");
	    }
	}
    }

    /**
     * Remove a LayerListener from the listener list.
     *
     * @param listener  The LayerListener to be removed
     */
    public synchronized void removeLayerListener(LayerListener listener) {
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
     * @param type the event type: one of ADD, REMOVE, REPLACE
     * @param layers the list of layers
     * @see LayerEvent
     */
    public void fireLayer(int type, com.bbn.openmap.Layer[] layers) {
	Debug.message("layersupport","LayerSupport | fireLayer");
	java.util.Vector targets;
	synchronized (this) {
	    if (listeners == null) {
	    	return;
	    }
	    Debug.message("layersupport","LayerSupport | fireLayer has " + listeners.size() + " listeners");
	    targets = (java.util.Vector) listeners.clone();
	    Debug.message("layersupport","LayerSupport | fireLayer has " + targets.size() + " listeners after cloning");
	}
        LayerEvent evt = new LayerEvent(source, type, layers);
	Debug.message("layersupport","calling setLayers on " + targets.size() + " objects");
	for (int i = 0; i < targets.size(); i++) {
	    LayerListener target = (LayerListener)targets.elementAt(i);
	    target.setLayers(evt);
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
	        LayerListener l = (LayerListener)v.elementAt(i);
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
	  addLayerListener((LayerListener)listenerOrNull);
        }
    }

    transient private java.util.Vector listeners;
    private Object source;
    private int layerSupportSerializedDataVersion = 1;
}
