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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/StandardRenderPolicy.java,v $
// $RCSfile: StandardRenderPolicy.java,v $
// $Revision: 1.2 $
// $Date: 2003/08/28 22:25:05 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.policy;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;
import java.awt.Graphics;

/**
 * The StandardRenderPolicy is a RenderPolicy that simply paints the
 * current graphic list.  No conditions or deviations are considered.
 */
public class StandardRenderPolicy implements RenderPolicy {

    /**
     * Don't let this be null.
     */
    protected OMGraphicHandlerLayer layer;

    protected boolean DEBUG = false;

    /**
     * Don't pass in a null layer.
     */
    public StandardRenderPolicy(OMGraphicHandlerLayer layer) {
	this.layer = layer;
	DEBUG = Debug.debugging("layer");
    }

    public OMGraphicHandlerLayer getLayer() {
	return layer;
    }

    public OMGraphicList prepare() {
	return layer.prepare();
    }

    public void paint(Graphics g) {
	OMGraphicList list = layer.getList();
	if (list != null) {
	    list.render(g);
	} else if (DEBUG) {
	    Debug.output(layer.getName() + ".paint(): NULL list, skipping...");
	}
    }
}
