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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/LayerStatusEvent.java,v $
// $RCSfile: LayerStatusEvent.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import com.bbn.openmap.Layer;

/**
 * An event to describe the computational status of a Layer.
 */
public class LayerStatusEvent extends java.util.EventObject {

    public final static transient int START_WORKING = 8342;
    public final static transient int STATUS_UPDATE = 8350;
    public final static transient int FINISH_WORKING = 8359;
    public final static transient int DISTRESS = 8360;

    protected int status = -1;

    // unused for the time
    protected int percentageComplete = -1;
    protected int timeRemaining = -1;

    /**
     * Construct a LayerStatusEvent with a status.
     * 
     * @param source Source Object
     * @param status the working status
     *  
     */
    public LayerStatusEvent(Layer source, int status) {
        super(source);
        this.status = status;
    }

    /**
     * Get the status of the layer.
     * 
     * @return int status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Get the associated Layer.
     * 
     * @return Layer
     */
    public Layer getLayer() {
        return (Layer) getSource();
    }
}