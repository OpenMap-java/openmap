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
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.event;

import com.bbn.openmap.Layer;
import com.bbn.openmap.util.Debug;

import java.util.Iterator;

/**
 * This is a utility class that can be used by beans that need support
 * for handling LayerListeners and firing LayerEvents.  You can use an
 * instance of this class as a member field of your bean and delegate
 * work to it.
 */
public class LayerSupport extends ListenerSupport {

    /**
     * Construct a LayerSupport.
     * @param sourceBean  The bean to be given as the source for any events.
     */
    public LayerSupport(Object sourceBean) {
        super(sourceBean);
        Debug.message("layersupport","LayerSupport | LayerSupport");
    }

    /**
     * Add a LayerListener to the listener list.
     *
     * @param listener  The LayerListener to be added
     */
    public synchronized void addLayerListener(LayerListener listener) {
        addListener(listener);
    }

    /**
     * Remove a LayerListener from the listener list.
     *
     * @param listener  The LayerListener to be removed
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
    public synchronized void fireLayer(int type, Layer[] layers) {
        Debug.message("layersupport","LayerSupport | fireLayer");

        Iterator it = iterator();
        if (Debug.debugging("layersupport")) {
            Debug.output("LayerSupport calling setLayers on " + 
                         size() + " objects");
        }

        if (size() == 0) return;

        LayerEvent evt = new LayerEvent(source, type, layers);
        while (it.hasNext()) {
            ((LayerListener)it.next()).setLayers(evt);
        }
    }
}
