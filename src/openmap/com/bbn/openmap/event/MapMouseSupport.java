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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapMouseSupport.java,v $
// $RCSfile: MapMouseSupport.java,v $
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

import com.bbn.openmap.util.Debug;

/**
 * This is a utility class that can be used by beans that need support
 * for handling MapMouseListeners and firing MapMouseEvents.  You can
 * use an instance of this class as a member field of your bean and
 * delegate work to it.
 * <p>
 * You can set the behavior of how MouseEvents are propagated by
 * setting whether to "consume" events.  If the MouseMode is consuming
 * events, then the event is not propagated further than the first
 * listener to successfully process it.  Otherwise the event is
 * propagated to all listeners.  The default is to consume events.
 */
public class MapMouseSupport implements java.io.Serializable {

    /**
     * The flag that dictates whether the events should be passed to
     * all the listeners or just limited to the first listener that
     * can deal with it. The default value is set to true, which means
     * the event will be consumed by the first layer that can handle
     * it. 
     */
    protected boolean consumeEvents = true;

    transient private java.util.Vector listeners = null;
    private int MapMouseSupportSerializedDataVersion = 1;

    /**
     * The priority MapMouseListener will be guaranteed to receive
     * events that go hand in hand (pressed - released, etc.).
     */
    protected MapMouseListener priorityListener = null;

    /**
     * Used to determine whether a release should reset the
     * priorityListener on a mouse release.
     */
    protected boolean clickHappened = false;

    /**
     * Construct a default MapMouseSupport.
     * The default value of consumeEvents is set to true.
     */
    public MapMouseSupport() {
	this(true);
    }

    /**
     * Construct a MapMouseSupport.
     * @param shouldConsumeEvents if true, events are propagated to
     * the first MapMouseListener that successfully processes the
     * event, if false, events are propagated to all MapMouseListeners
     */
    public MapMouseSupport(boolean shouldConsumeEvents) {
	consumeEvents = shouldConsumeEvents;
    }

    /**
     * Sets how the mouse support passes out events.  If the value
     * passed in is true, the mouse support will only pass the event
     * to the first listener that can respond to the event.  If false,
     * the mouse support will pass the event on to all its listeners.
     * @param shouldConsumeEvents true for limited distribution.
     */
    public void setConsumeEvents(boolean shouldConsumeEvents) {
	consumeEvents = shouldConsumeEvents;
    }

   /**
    * Returns how the mouse support is set up to distribute events.
    * @return true if only one listner gets to act on an event.
    */
    public boolean isConsumeEvents() {
	return consumeEvents;
    }

    /**
     * Add a MapMouseListener to the listener list.
     * @param listener The MapMouseListener to be added
     */
    public synchronized void addMapMouseListener(MapMouseListener listener) {
	if (listeners == null) {
	    listeners = new java.util.Vector();
	}
	listeners.addElement(listener);
    }

    /**
     * Remove a MapMouseListener from the listener list.
     * @param listener The MapMouseListener to be removed
     */
    public synchronized void removeMapMouseListener(MapMouseListener listener) {
	if (listeners == null) {
	    return;
	}
	listeners.removeElement(listener);
    }

    /**
     * Remove all MapMouseListeners from the listener list.
     */
    public synchronized void removeAllMapMouseListeners() {
	if (listeners == null) {
	    return;
	}
	listeners.removeAllElements();
    }

    /**
     * Get a reference to the listeners.
     */
    protected java.util.Vector getTargets() {
	java.util.Vector targets;
	synchronized (this) {
	    if (listeners == null) {
	    	return null;
	    }
	    return (java.util.Vector) listeners.clone();
	}
    }

    /**
     * Handle a mousePressed MouseListener event.
     * @param evt MouseEvent to be handled
     */
    public boolean fireMapMousePressed(java.awt.event.MouseEvent evt) {
	if (Debug.debugging("gestures")) {
	    System.out.println("MapMouseSupport.fireMapMousePressed()");
	}
	java.util.Vector targets = getTargets();
	if (targets == null) return false;

	for (int i = 0; i < targets.size(); i++) {
	    MapMouseListener target = 
		(MapMouseListener)targets.elementAt(i);
	    if (target.mousePressed(evt) && consumeEvents) {
		priorityListener = target;
		return true;
	    }
	}
	return false;
    }

    /**
     * Handle a mouseReleased MouseListener event.  Checks to see if
     * there is a priorityListener, and will direct the event to that
     * listener.  The priorityListener variable will be reset to null.
     * If there is not a priorityListener, the event is passed through
     * the listeners, subject to the consumeEvents mode.
     * @param evt MouseEvent to be handled.
     */
    public boolean fireMapMouseReleased(java.awt.event.MouseEvent evt) {
//  	System.out.println("MapMouseSupport: fireMapMouseReleased");

	if (priorityListener != null) {
	    priorityListener.mouseReleased(evt);
	    if (!clickHappened)
		priorityListener = null;
	    return true;
	}

	java.util.Vector targets = getTargets();
	if (targets == null) return false;
	
	for (int i = 0; i < targets.size(); i++) {
	    MapMouseListener target = 
		(MapMouseListener)targets.elementAt(i);
	    if (target.mouseReleased(evt) && consumeEvents) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Handle a mouseClicked MouseListener event.  If the
     * priorityListener is set, it automatically gets the clicked
     * event.  If it is not set, the other listeners get a shot at the
     * event according to the consumeEvent mode.
     * @param evt MouseEvent to be handled.
     */
    public boolean fireMapMouseClicked(java.awt.event.MouseEvent evt) {
//  	System.out.println("MapMouseSupport: fireMapMouseClicked");
	clickHappened = true;
	if (priorityListener != null && evt.getClickCount() > 1){
	    priorityListener.mouseClicked(evt);
	    return true;
	}

	priorityListener = null;

	java.util.Vector targets = getTargets();
	if (targets == null) return false;

	for (int i = 0; i < targets.size(); i++) {
	    MapMouseListener target = 
		(MapMouseListener)targets.elementAt(i);
	    if (target.mouseClicked(evt) && consumeEvents) {
		priorityListener = target;
		return true;
	    }
	}
	return false;
    }

    /**
     * Handle a mouseEntered MouseListener event.
     * @param evt MouseEvent to be handled
     */
    public boolean fireMapMouseEntered(java.awt.event.MouseEvent evt) {
//  	System.out.println("MapMouseSupport: fireMapMouseEntered");
	java.util.Vector targets = getTargets();
	if (targets == null) return false;

	for (int i = 0; i < targets.size(); i++) {
	    MapMouseListener target = 
		(MapMouseListener)targets.elementAt(i);
	    target.mouseEntered(evt);
	}
	return false;
    }

    /**
     * Handle a mouseExited MouseListener event.
     * @param evt MouseEvent to be handled
     * @return false.
     */
    public boolean fireMapMouseExited(java.awt.event.MouseEvent evt) {
//  	System.out.println("MapMouseSupport: fireMapMouseExited");
	java.util.Vector targets = getTargets();
	if (targets == null) return false;

	for (int i = 0; i < targets.size(); i++) {
	    MapMouseListener target = 
		(MapMouseListener)targets.elementAt(i);
	    target.mouseExited(evt);
	}
	return false;
    }

    /**
     * Handle a mouseDragged MouseListener event.
     * @param evt MouseEvent to be handled
     * @return false.
     */
    public boolean fireMapMouseDragged(java.awt.event.MouseEvent evt) {
//  	System.out.println("MapMouseSupport: fireMapMouseDragged");
	clickHappened = false;
	
	if (priorityListener != null){
	    priorityListener.mouseDragged(evt);
	    return true;
	}

	java.util.Vector targets = getTargets();
	if (targets == null) return false;

	for (int i = 0; i < targets.size(); i++) {
	    MapMouseListener target = 
		(MapMouseListener)targets.elementAt(i);
	    if (target.mouseDragged(evt) && consumeEvents) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Handle a mouseMoved MouseListener event.  If the moved event
     * is consumed, the rest of the listeners that didn't have a
     * chance to respond get called in the mouse moved method without
     * arguments.
     * @param evt MouseEvent to be handled
     * @return true if the event was consumed.
     */
    public boolean fireMapMouseMoved(java.awt.event.MouseEvent evt) {
//  	System.out.println("MapMouseSupport: fireMapMouseMoved");
	boolean movedConsumed = false;

	java.util.Vector targets = getTargets();
	if (targets == null) return false;

	for (int i = 0; i < targets.size(); i++) {
	    MapMouseListener target = 
		(MapMouseListener)targets.elementAt(i);
	    if (movedConsumed) {
		target.mouseMoved();
	    } else {
		movedConsumed = target.mouseMoved(evt);
	    }
	}
	return movedConsumed;
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
	        MapMouseListener l = (MapMouseListener)v.elementAt(i);
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
	  addMapMouseListener((MapMouseListener)listenerOrNull);
        }
    }
}
