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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapListenerSupport.java,v $
// $RCSfile: NetMapListenerSupport.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.util.Debug;

/**
 * NetMapListenerSupport is used to manage NetMapListeners, and to
 * distribute NetMapEvents to them.
 */
public class NetMapListenerSupport implements java.io.Serializable {

    transient private Vector listeners;
    transient private Object source;

    /**
     * Construct a NetMapListenerSupport.
     */
    public NetMapListenerSupport() {
        this(null);
    }

    /**
     * Construct a NetMapListenerSupport.
     * 
     * @param aSource source Object
     */
    public NetMapListenerSupport(Object aSource) {
        source = aSource;
    }

    /**
     * Set the source object.
     * 
     * @param aSource source Object
     */
    public synchronized void setSource(Object aSource) {
        source = aSource;
    }

    /**
     * Get the source object.
     * 
     * @return Object source
     */
    public synchronized Object getSource() {
        return source;
    }

    /**
     * Add a NetMapListener.
     * 
     * @param l NetMapListener
     */
    public synchronized void addNetMapListener(NetMapListener l) {
        if (listeners == null) {
            listeners = new java.util.Vector();
        }
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    /**
     * Remove a NetMapListener.
     * 
     * @param l NetMapListener
     */
    public synchronized void removeNetMapListener(NetMapListener l) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(l);
    }

    /**
     * Return a cloned list of Listeners.
     * 
     * @return Vector of listeners, null if none have been added.
     */
    public synchronized java.util.Vector getListeners() {
        if (listeners == null) {
            return null;
        }

        return (java.util.Vector) listeners.clone();
    }

    /**
     * Remove all listeners.
     */
    public void clearNetMapListeners() {
        listeners.clear();
    }

    /**
     * Send a center event to all registered listeners.
     */
    public void fireNetMapEvent(Properties eventProperties) {
        java.util.Vector targets;
        NetMapListener target;
        Object theSource = getSource();

        targets = getListeners();

        if (listeners == null) {
            return;
        }

        int nTargets = targets.size();

        if (nTargets == 0)
            return;

        NetMapEvent evt = new NetMapEvent(theSource, eventProperties);

        for (int i = 0; i < nTargets; i++) {
            target = (NetMapListener) targets.elementAt(i);
            if (Debug.debugging("mapbean")) {
                Debug.output("NetMapListenerSupport.fireNetMapEvent(): "
                        + "target is: " + target);
            }

            target.catchEvent(evt);
        }
    }
}