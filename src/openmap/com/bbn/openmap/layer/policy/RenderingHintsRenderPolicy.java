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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/RenderingHintsRenderPolicy.java,v $
// $RCSfile: RenderingHintsRenderPolicy.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/04 18:15:21 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.policy;

import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The RenderingHintsRenderPolicy is a StandardRenderPolicy that lets
 * you set RenderingHints on the java.awt.Graphics object before the
 * layer is painted, and then simply paints the current graphic list.
 * No conditions or deviations are considered.  This class can be set
 * with properties.
 * <P>
 * The properties have to look like: <pre>
 *
 * prefix.renderingHints=RENDERINGHINTS_KEY1 RENDERINGHINTS_KEY2
 * prefix.RENDERINGHINTS_KEY1=RENDERINGHINTS_VALUE1
 * prefix.RENDERINGHINTS_KEY2=RENDERINGHINTS_VALUE2
 *
 * </pre> For example, for a GraticuleLayer in the properties file
 * with a 'graticule' prefix: <pre>
 * 
 * graticule.renderPolicy=textAliasing
 * graticule.textAliasing.class=com.bbn.openmap.layer.policy.RenderingHintsRenderPolicy
 * graticule.textAliasing.renderingHints=KEY_TEXT_ANTIALIASING
 * graticule.textAliasing.KEY_TEXT_ANTIALIASING=VALUE_TEXT_ANTIALIAS_ON
 * 
 * </pre> The HintsMapBeanRepaintPolicy uses a
 * RenderingHintsRenderPolicy with properties that look like these,
 * where 'repaintPolicy' is the prefix for the
 * RenderingHintsRenderPolicy: <pre>
 *
 * repaintPolicy.class=com.bbn.openmap.HintsMapBeanRepaintPolicy
 * repaintPolicy.renderingHints=KEY_ANTIALIASING
 * repaintPolicy.KEY_ANTIALIASING=VALUE_ANTIALIAS_ON
 * repaintPolicy.KEY_RENDERING=VALUE_RENDER_SPEED
 * 
 * </pre>
 * See the java.awt.RenderingHints javadocs for the key-value
 * pairs that can be used.
 * 
 */
public class RenderingHintsRenderPolicy extends StandardRenderPolicy {

    public final static String RenderingHintsProperty = "renderingHints";

    protected RenderingHints renderingHints = null;

    /**
     * The layer needs to be set at some point before use.
     */
    public RenderingHintsRenderPolicy() {
	super();
	setRenderingHints(new RenderingHints(null));
    }
    
    public RenderingHintsRenderPolicy(OMGraphicHandlerLayer layer) {
	super(layer);
	setRenderingHints(new RenderingHints(null));
    }

    /**
     * Set the RenderingHints Map that should be used on the MapBean.
     * If null, no modifications will be set on the MapBean's Graphics
     * object.
     */
    public void setRenderingHints(RenderingHints rh) {
	renderingHints = rh;
    }

    /**
     * Get the RenderingHints Map that should be used on the MapBean.
     * May be null (default).
     */
    public RenderingHints getRenderingHints() {
	return renderingHints;
    }

    /**
     * If you are going to change the Graphics object in this method,
     * you should make a copy of it first using the Graphics.create()
     * method so the changes won't affect other layers.
     */
    public void paint(Graphics g) {
	g = g.create(); // Make a copy to use just for this layer.
	setRenderingHints(g);
	super.paint(g);
    }

    public void setRenderingHints(Graphics g) {
	if (renderingHints != null && g instanceof Graphics2D) {
 	    ((Graphics2D)g).setRenderingHints(renderingHints);
	}
    }

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);
	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	String renderingHintsString = 
	    props.getProperty(prefix + RenderingHintsProperty);

	if (DEBUG) {
	    Debug.output("RHRP: decoding rendering hints: " + renderingHintsString);
	}

	Vector renderingHintsVector = 
	    PropUtils.parseSpacedMarkers(renderingHintsString);

	if (renderingHintsVector != null) {
	    Iterator it = renderingHintsVector.iterator();
	    while (it.hasNext()) {
		String renderingHintKeyString = (String)it.next();
		if (renderingHintKeyString != null) {
		    String renderingHintValueString = 
			props.getProperty(prefix + renderingHintKeyString);

		    if (renderingHintValueString != null) {
			Object key = null;
			Object value = null;

			try {
			    key = RenderingHints.class.getField(renderingHintKeyString).get(null);
			    value = RenderingHints.class.getField(renderingHintValueString).get(null);
			} catch (NoSuchFieldException nsfe) {
			} catch (IllegalAccessException iae) {
			}

			if (key != null && value != null) {
			    if (renderingHints == null) {
				renderingHints = new RenderingHints(null);
			    }
			    renderingHints.put(key, value);
			    if (DEBUG) {
				Debug.output("RHRP+++ adding " + renderingHintKeyString + " | " + renderingHintValueString);
			    }
			} else {
			    if (DEBUG) {
				Debug.output("RHRP--- NOT adding " + renderingHintKeyString + " (" + key + ") | " + renderingHintValueString + " (" + value + ")");
			    }
			}
		    } else if (DEBUG) {
			Debug.output("RHRP--- NOT adding " + renderingHintKeyString);
		    }
		}
	    }
	}
    }

    public Properties getProperties(Properties props) {
	props = super.getProperties(props);
	return props;
    }

    public Properties getPropertyInfo(Properties props) {
	props = super.getPropertyInfo(props);
	return props;
    }

}
