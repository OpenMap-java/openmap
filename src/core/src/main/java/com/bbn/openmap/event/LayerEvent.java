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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/LayerEvent.java,v $
// $RCSfile: LayerEvent.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import com.bbn.openmap.Layer;

/**
 * An event to request that layers be added, removed, or shuffled
 * around.
 */
public class LayerEvent extends java.util.EventObject {

    public transient static final int ADD = 400;
    public transient static final int REMOVE = 401;
    public transient static final int REPLACE = 402;
    public transient static final int ALL = 403;

    private transient Layer[] layers;
    private transient int type;

    /**
     * Construct a LayerEvent.
     * 
     * @param source Object
     * @param type type of LayerEvent
     * @param layers Layer[]
     */
    public LayerEvent(Object source, int type, Layer[] layers) {
        super(source);
        this.layers = layers;
        this.type = type;
    }

    /**
     * Get the Layers affected by this event.
     * 
     * @return Layer[]
     */
    public Layer[] getLayers() {
        return layers;
    }

    /**
     * Get the type of LayerEvent.
     * 
     * @return int type
     */
    public int getType() {
        return type;
    }
}