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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/HintsMapBeanRepaintPolicy.java,v $
// $RCSfile: HintsMapBeanRepaintPolicy.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.layer.policy.RenderingHintsRenderPolicy;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 */
public class HintsMapBeanRepaintPolicy extends StandardMapBeanRepaintPolicy implements Cloneable {

    protected RenderingHintsRenderPolicy hints;

    public HintsMapBeanRepaintPolicy() {
        super();
        setHints(new RenderingHintsRenderPolicy());
    }

    public HintsMapBeanRepaintPolicy(MapBean mb) {
        super(mb);
        setHints(new RenderingHintsRenderPolicy());
    }

    public void setHints(RenderingHintsRenderPolicy rhrp) {
        hints = rhrp;
    }

    public RenderingHintsRenderPolicy getHints() {
        return hints;
    }

    /**
     * A hook for the RepaintPolicy to make any adjustments to the
     * java.awt.Graphics object before sending the Graphics object to
     * the layers for painting.  Gives the policy a chance to make
     * rendering hint changes on Graphic2D objects, setting
     * anti-aliasing configurations, etc.  No modifications are made.
     */
    public Graphics modifyGraphicsForPainting(Graphics graphics) {
        if (hints != null) {
            hints.setRenderingHints(graphics);
        }
        return graphics;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        if (hints != null) {
            hints.setProperties(prefix, props);
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        if (hints != null) {
            props = hints.getProperties(props);
        }
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        if (hints != null) {
            props = hints.getPropertyInfo(props);
        }
        return props;
    }

    public Object clone() {
        HintsMapBeanRepaintPolicy bmbrp = new HintsMapBeanRepaintPolicy();
        bmbrp.setHints(getHints());
        return bmbrp;
    }
}

