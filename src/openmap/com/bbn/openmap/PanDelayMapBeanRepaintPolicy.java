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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/StandardMapBeanRepaintPolicy.java,v $
// $RCSfile: StandardMapBeanRepaintPolicy.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

/**
 * A PanDelayMapBeanRepaintPolicy has a flag that prevents layer repaint calls
 * from being acted on due to panning action on the MapBean, which tends to
 * upset the look and feel of the pan.
 */
public class PanDelayMapBeanRepaintPolicy extends HintsMapBeanRepaintPolicy {

    protected boolean panning = false;
    protected boolean needRepaint = false;

    public PanDelayMapBeanRepaintPolicy() {
    }

    public PanDelayMapBeanRepaintPolicy(MapBean mb) {
        super(mb);
    }

    /**
     * Take some action based on a repaint request from this particular layer.
     * The StandardMapBeanRepaintPolicy just forwards requests on.
     */
    public void repaint(Layer layer) {
        // No decisions, just forward the repaint() request;
        if (map != null && !panning) {
            needRepaint = false;
            map.repaint();
        } else {
            needRepaint = true;
        }
    }

    public boolean isPanning() {
        return panning;
    }

    public void setPanning(boolean panning) {
        this.panning = panning;
        
        if (!panning && needRepaint && map != null) {
            needRepaint = false;
            map.repaint();
        }
    }

    public Object clone() {
        return new PanDelayMapBeanRepaintPolicy();
    }
}
