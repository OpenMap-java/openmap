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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/policy/StandardPCPolicy.java,v $
// $RCSfile: StandardPCPolicy.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/10 22:03:57 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.policy;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.proj.Projection;

/**
 * ProjectionChangePolicy that uses a Layer SwingWorker to kick off a
 * thread to call layer.prepare() and doesn't delete the OMGraphicList
 * between projection changes.  The standard behavior for simple
 * layers that maintain a constant list of OMGraphics.
 */
public class StandardPCPolicy implements ProjectionChangePolicy {

    /**
     * The OMGraphicHandlerLayer using this policy.  Don't let this be
     * null.
     */
    protected OMGraphicHandlerLayer layer;
    /**
     * The number of OMGraphics that have to be on the layer's
     * OMGraphicList before a thread is spawned.  The default is 50;
     */
    protected int graphicCutoff = 50;

    /**
     * Don't pass in a null layer.
     */
    public StandardPCPolicy(OMGraphicHandlerLayer layer) {
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
	    // Some criteria can decide whether
	    // starting another thread is worth it...
	    if (shouldSpawnThreadForPrepare()) {
		layer.doPrepare();
		return;
	    } else {
		layer.prepare();
		layer.repaint();
	    }
	} else {
	    layer.repaint();
	}
	layer.fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
    }

    /**
     * We're assuming that the list is somewhat constant for the
     * layer.  If the number of OMGraphics on the layer's list is more
     * than the cutoff number, thena thread should be launched to
     * generate them with the new projection.  You can extend this
     * method so that different criteria may be considered.
     */
    protected boolean shouldSpawnThreadForPrepare() {
	if (layer != null) {
	    com.bbn.openmap.omGraphics.OMGraphicList list = layer.getList();
	    if (list != null) {
		return layer.getList().size() > graphicCutoff;
	    }
	}
	// If we have to create a list, might as well assume that 
	// is should be done in a new thread.
	return true;
    }

    /**
     * Set the number of OMGraphics that have to be on the list before
     * a thread gets spawned to call generate() on them.
     */
    public void setGraphicCutoff(int number) {
	graphicCutoff = number;
    }

    public int getGraphicCutoff() {
	return graphicCutoff;
    }

}
