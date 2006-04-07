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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/LayerSupport.java,v $
// $RCSfile: LayerSupport.java,v $
// $Revision: 1.8 $
// $Date: 2006/04/07 17:27:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.Layer;
import com.bbn.openmap.util.Debug;

/**
 * This is a utility class that can be used by beans that need support
 * for handling LayerListeners and firing LayerEvents. You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class LayerSupport extends ListenerSupport {

    protected boolean synchronous = true;

    /**
     * Construct a LayerSupport.
     * 
     * @param sourceBean The bean to be given as the source for any
     *        events.
     */
    public LayerSupport(Object sourceBean) {
        super(sourceBean);
        Debug.message("layersupport", "LayerSupport | LayerSupport");
    }

    /**
     * Add a LayerListener to the listener list.
     * 
     * @param listener The LayerListener to be added
     */
    public synchronized void addLayerListener(LayerListener listener) {
        addListener(listener);
    }

    /**
     * Remove a LayerListener from the listener list.
     * 
     * @param listener The LayerListener to be removed
     */
    public synchronized void removeLayerListener(LayerListener listener) {
        removeListener(listener);
    }

    /**
     * Send a layer event to all registered listeners.
     * 
     * @param type the event type: one of ADD, REMOVE, REPLACE
     * @param layers the list of layers
     * @see LayerEvent
     */
    public void fireLayer(int type, Layer[] layers) {
        Debug.message("layersupport", "LayerSupport | fireLayer");

        Iterator it = iterator();
        if (Debug.debugging("layersupport")) {
            Debug.output("LayerSupport calling setLayers on " + size()
                    + " objects");
        }

        if (size() == 0)
            return;

        LayerEvent evt = new LayerEvent(source, type, layers);
        while (it.hasNext()) {
            ((LayerListener) it.next()).setLayers(evt);
        }
    }
    /**
     * Used to see if another Thread object needs to be created.
     */
    protected Thread t;
    /**
     * Event information stack.
     */
    protected Vector events = new Vector();

    /**
     * Pushed the information onto a Vector stack to get executed by a
     * separate thread. Any thread launched is held on to, and if that
     * thread is is null or not active, a new thread is kicked off.
     * The dying thread checks the Vector stack and fires another
     * event if it can.
     * 
     * @param layerEventType
     * @param layers
     */
    public synchronized void pushLayerEvent(int layerEventType, Layer[] layers) {

        if (synchronous) {
            fireLayer(layerEventType, layers);
        } else {

            events.add(new SetLayerRunnable(layerEventType, layers));

            if (t == null || !t.isAlive()) {
                SetLayerRunnable runnable = popLayerEvent();
                if (runnable != null) {
                    t = new Thread(runnable);
                    t.start();
                }
            }
        }
    }

    /**
     * Return the first event on the stack, may be null if there is
     * nothing to do.
     */
    public synchronized SetLayerRunnable popLayerEvent() {
        try {
            return (SetLayerRunnable) events.remove(0);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return null;
        }
    }

    /**
     * A reusable Runnable used by a thread to notify listeners when
     * layers are turned on/off or shuffled.
     */
    protected class SetLayerRunnable implements Runnable {
        protected int layerEventType;
        protected Layer[] layers;

        public SetLayerRunnable(int let, Layer[] lrs) {
            layerEventType = let;
            layers = lrs;
        }

        public int getEventType() {
            return layerEventType;
        }

        public Layer[] getLayers() {
            return layers;
        }

        public void run() {
            doIt(getEventType(), getLayers());
            SetLayerRunnable runnable = popLayerEvent();
            while (runnable != null) {
                doIt(runnable.getEventType(), runnable.getLayers());
                runnable = popLayerEvent();
            }
        }

        public void doIt(int eventType, Layer[] layers) {
            Debug.message("layerhandler",
                    "LayerSupport: firing LayerEvent on LayerListeners");
            fireLayer(eventType, layers);
        }
    };

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }
}