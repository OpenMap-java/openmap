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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/HintsMapBeanRepaintPolicy.java,v $
// $RCSfile: HintsMapBeanRepaintPolicy.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import java.awt.Graphics;
import java.util.Properties;

import com.bbn.openmap.layer.policy.RenderingHintsRenderPolicy;

/**
 * The class lets you set RenderingHints on the MapBean, to set anti-aliasing,
 * etc. This class can be added to the OpenMap application via the
 * openmap.components property in the openmap.properties file.
 */
public class HintsMapBeanRepaintPolicy
        extends StandardMapBeanRepaintPolicy
        implements Cloneable {

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
     * Set RenderingHint on this object.
     * 
     * @param key RenderingHint KEY
     * @param value RenderingHint VALUE
     */
    public void put(Object key, Object value) {
        if (hints != null) {
            hints.put(key, value);
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
