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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/ListResetPCPolicy.java,v $
// $RCSfile: ListResetPCPolicy.java,v $
// $Revision: 1.2 $
// $Date: 2003/08/28 22:25:04 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.policy;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * ProjectionChangePolicy that uses a Layer SwingWorker to kick off a
 * thread to call layer.prepare() and deletes the current
 * OMGraphicList between projection changes.  The standard behavior
 * for layers that gather new OMGraphics for new projections.
 */
public class ListResetPCPolicy implements ProjectionChangePolicy {

    /**
     * Don't let this be null.
     */
    protected OMGraphicHandlerLayer layer;

    /**
     * Don't pass in a null layer.
     */
    public ListResetPCPolicy(OMGraphicHandlerLayer layer) {
	this.layer = layer;
    }

    public OMGraphicHandlerLayer getLayer() {
	return layer;
    }

    public void projectionChanged(ProjectionEvent pe) {

	Projection proj = layer.setProjection(pe);
	// proj will be null if the projection hasn't changed, a 
	// signal that work does not need to be done.
	if (proj != null) {
	    if (Debug.debugging("layer")) {
		Debug.output(getLayer().getName() + ": ListResetPCPolicy projectionChanged with NEW projection, resetting list.");
	    }
	    layer.setList(null);
	    layer.doPrepare();
	} else {
	    if (Debug.debugging("layer")) {
		Debug.output(getLayer().getName() + ": ListResetPCPolicy projectionChanged with OLD projection, repainting.");
	    }
	    layer.repaint();
	    layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	}
    }

    /**
     * This is a subtle call, that dictates what should happen when
     * the LayerWorker has completed working in it's thread.  The
     * LayerWorker.get() method returns whatever was returned in the
     * OMGraphicHandler.prepare() method, an OMGraphicList.  In most
     * cases, this object should be set as the Layer's list at this
     * time.  Some Layers, working asynchronously with their data
     * sources, might want nothing to happen and should use a policy
     * that overrides this method so that nothing does.
     */
    public void workerComplete(OMGraphicList aList) {
	if (layer != null) {
	    layer.setList(aList);
	}
    }
}
