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

import java.awt.Graphics;

import com.bbn.openmap.util.Debug;

/**
 * A MapBeanRepaintPolicy that just forwards layer repaint requests normally,
 * and does nothing for java.awt.Graphics before painting. If this class or any
 * subclass of it is added to the MapHandler, it will find the MapBean and set
 * itself on it.
 * 
 * <P>
 * A StandardMapBeanRepaintPolicy is automatically set in the MapBean, so you
 * don't have to add one of these to the MapHandler. The OMComponent inheritance
 * is here to make it easier for subclasses to be added and for properties to be
 * set.
 */
public class StandardMapBeanRepaintPolicy extends OMComponent implements MapBeanRepaintPolicy,
        SoloMapComponent, Cloneable {

    protected MapBean map;

    protected boolean DEBUG = false;

    public StandardMapBeanRepaintPolicy() {
        DEBUG = Debug.debugging("policy");
    }

    public StandardMapBeanRepaintPolicy(MapBean mb) {
        super();
        setMap(mb);
    }

    public void setMap(MapBean mb) {
        map = mb;
    }

    public MapBean getMap() {
        return map;
    }

    /**
     * Take some action based on a repaint request from this particular layer.
     * The StandardMapBeanRepaintPolicy just forwards requests on.
     */
    public void repaint(Layer layer) {
        // No decisions, just forward the repaint() request;
        if (map != null) {
            if (DEBUG) {
                Debug.output("SMBRP: forwarding repaint request for " + layer.getName());
            }
            map.repaint();
        } else {
            Debug.error("SMBRP: MapBean is null in repaint(" + layer.getName() + ")");
        }
    }

    /**
     * A hook for the RepaintPolicy to make any adjustments to the
     * java.awt.Graphics object before sending the Graphics object to the layers
     * for painting. Gives the policy a chance to make rendering hint changes on
     * Graphic2D objects, setting anti-aliasing configurations, etc. No
     * modifications are made.
     */
    public Graphics modifyGraphicsForPainting(Graphics graphics) {
        return graphics;
    }

    /**
     * If a MapBean is passed to this StandardMapBeanRepaintPolicy, it will set
     * itself on it.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MapBean) {
            ((MapBean) someObj).setMapBeanRepaintPolicy(this);
            setMap((MapBean) someObj);
            if (DEBUG) {
                Debug.output(getClass().getName() + " found MapBean");
            }
        }
    }

    /**
     * If a MapBean is passed to this StandardMapBeanRepaintPolicy, it will
     * check if it is the repaint policy set on the MapBean and if so, remove
     * itself from it.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapBean && ((MapBean) someObj).getMapBeanRepaintPolicy() == this) {
            ((MapBean) someObj).setMapBeanRepaintPolicy(null);
            setMap(null);
        }
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
