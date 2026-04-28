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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/SinkLayer.java,v $
// $RCSfile: SinkLayer.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:53 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;

/**
 * SinkLayer is a Layer which does nothing. This can be used primarily
 * as a placeholder.
 */
public class SinkLayer extends Layer {

    // the shared instance
    private static SinkLayer sharedInstance;

    /**
     * Get a shared instance of the SinkLayer.
     * 
     * @return SinkLayer shared instance
     */
    public final static SinkLayer getSharedInstance() {
        if (sharedInstance == null)
            sharedInstance = new SinkLayer();
        return sharedInstance;
    }

    // cannot construct
    private SinkLayer() {}

    /**
     * ProjectionListener interface method.
     * 
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {}
}