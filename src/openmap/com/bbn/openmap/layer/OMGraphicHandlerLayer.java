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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/OMGraphicHandlerLayer.java,v $
// $RCSfile: OMGraphicHandlerLayer.java,v $
// $Revision: 1.4 $
// $Date: 2003/03/10 22:04:54 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer;

import java.awt.Graphics;
import java.awt.Shape;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.policy.ProjectionChangePolicy;
import com.bbn.openmap.layer.policy.StandardPCPolicy;
import com.bbn.openmap.layer.policy.RenderPolicy;
import com.bbn.openmap.layer.policy.StandardRenderPolicy;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;

/**
 * The OMGraphicHandlerLayer is a layer that provides OMGraphicHandler
 * support.  With this support, the OMGraphicHandlerLayer can accept
 * OMAction instructions for managing OMGraphics, and can perform
 * display filtering as supported by the FilterSupport object.<P>
 *
 * The OMGraphicHandlerLayer already has an OMGraphicList variable, so
 * if you extend this class you don't have to manage another one.  You
 * can add your OMGraphics to the list provided. If you create a list
 * of OMGraphics that is reused and simply re-projected when the
 * projection changes, do nothing - that's what happens anyway based
 * on the default ProjectionChangePolicy set for the layer
 * (StandardPCPolicy).  If you let prepare() create a new
 * OMGraphicList based on the new projection, then make sure the
 * ProjectionChangePolicy for the layer is set to a
 * com.bbn.openmap.layer.policy.ResetListPCPolicy, or at least clear
 * out the old graphics at some point. You just have to do one, not
 * both, of those things.  If you are managing a lot of OMGraphics and
 * do not null out the list, you may see your layer appear to lag
 * behind the projection changes.  That's because another layer with
 * less work to do finishes and calls repaint, and since your list is
 * still set with OMGraphics ready for the old projection, it will
 * just draw what it had, and then draw again when it has finished
 * working.  Nulling out the list will prevent your layer from drawing
 * anything on the new projection until it is ready.<P>
 *
 * The OMGraphicHandlerLayer has support built in for launching a
 * SwingWorker to do work for you in a separate thread.  This behavior
 * is controlled by the ProjectionChangePolicy that is set for the
 * layer.  Both the StandardPCPolicy and ListResetPCPolicy launch
 * threads by calling doPrepare() on this layer.  The StandardPCPolicy
 * only calls this if the number of OMGraphics on its list is greater
 * than some cutoff value. <P>
 * 
 * useLayerWorker variable is true (default), then doPrepare() will be
 * called when a new ProjectionEvent is received in the
 * projectionChanged method.  This will cause prepare() to be called
 * in a separate thread.   You can use prepare() to create OMGraphics,
 * the projection will have been set in the layer and is available via
 * getProjection().  You should generate() the OMGraphics in prepare.
 * NOTE: You can override the projectionChanged() method to
 * create/manage OMGraphics any way you want.  The SwingWorker only
 * gets launched if doPrepare() gets called.
 */
public class OMGraphicHandlerLayer extends Layer {

    /**
     * Filter support that can be used to manage OMGraphics.
     */
    protected FilterSupport filter = new FilterSupport();

    /** 
     * The ProjectionChangePolicy object that determines how a layer
     * reacts and sets up the OMGraphicList to be rendered for the
     * layer when the projection changes. 
     */
    protected ProjectionChangePolicy projectionChangePolicy = null;

    /**
     * The RenderPolicy object that determines how a layer's
     * OMGraphicList is rendered in the layer.paint() method.
     */
    protected RenderPolicy renderPolicy = null;

    /**
     * A SwingWorker that can be used for gathering OMGraphics or
     * doing other work in a different thread.
     */
    protected SwingWorker layerWorker;
    
    // OMGraphicHandler methods, deferred to FilterSupport...

    /**
     * Sets all the OMGraphics outside of this shape to be invisible.
     * Also returns another OMGraphicList containing OMGraphics that
     * are contained within the Shape provided.
     */
    public OMGraphicList filter(Shape withinThisShape) {
	return filter.filter(withinThisShape);
    }

    /**
     * @see OMGraphicHandler#filter(Shape, boolean).
     */
    public OMGraphicList filter(Shape shapeBoundary, 
				boolean getInsideBoundary) {
	return filter.filter(shapeBoundary, getInsideBoundary);
    }

    /**
     * To find out whether SQL queries are handled.
     * @see OMGraphicHandler#supportsSQL().
     */
    public boolean supportsSQL() {
	return filter.supportsSQL();
    }

    /**
     * Depending on the filter's SQL support, returns an OMGraphicList
     * that fit the query.
     */
    public OMGraphicList filter(String SQLQuery) {
	return filter.filter(SQLQuery);
    }

    /**
     * Perform the OMAction on the OMGraphic, within the OMGraphicList
     * contained in the layer.
     */
    public boolean doAction(OMGraphic graphic, OMAction action) {
	return filter.doAction(graphic, action);
    }

    /**
     * Get the OMGraphicList held by the layer.  May be null.
     */
    public OMGraphicList getList() {
	return filter.getList();
    }

    /**
     * Indicates if the OMGraphicHandler can have its OMGraphicList set.
     */
    public boolean canSetList() {
	return filter.canSetList();
    }

    /**
     * Set the OMGraphicList within this OMGraphicHandler. Works if
     * canSetGraphicList == true.
     */
    public void setList(OMGraphicList omgl) {
	filter.setList(omgl);
    }
    
    /**
     * Remove all filters, and reset all graphics to be visible.
     */
    public void resetFiltering() {
	filter.resetFiltering();
    }

    /**
     * Don't set to null.  This is here to let subclasses put a
     * more/less capable FilterSupport in place.
     */
    public void setFilter(FilterSupport fs) {
	filter = fs;
    }

    /**
     * Get the FilterSupport object that is handling the
     * OMGraphicHandler methods.
     */
    public FilterSupport getFilter() {
	return filter;
    }

    /**
     * From the ProjectionListener interface. The method gets called
     * when the layer is part of the map, and whenever the map
     * projection changes.  Will trigger a repaint().<p>
     *
     * The ProjectionEvent is passed to the current
     * ProjectionChangePolicy object, which determines what will
     * happen on the layer and how.  By default, a StandardPCPolicy is
     * notified with the projection change, and it will test the
     * projection for changes and make sure prepare() is called.  It
     * will make the decision whether doPrepare() is called, based on
     * the number of OMGraphics on the list, which may launch a swing
     * worker thread to call prepare().  The StandardPCPolicy does not
     * do anything to the OMGraphicList when the projection
     * changes.<p>
     *
     * If you need the OMGraphicList cleared out with a new
     * projection, you can substitute a ListRestPCPolicy for the
     * StandardPCPolicy.  You would want to do this if your
     * OMGraphicList changes for different projections - The reason
     * the OMGraphicList is nulled out is so if another layer finishes
     * before yours does and gets repainted, your old OMGraphics don't
     * get painted along side their new ones - it's a mismatched
     * situation.  You can set the ProjectionChangePolicy directly
     * with the setProjectionChangePolicy, or by overriding the
     * getProjectionChangePolicy method and returning the type you
     * want by default if it is null.
     *
     * @see com.bbn.openmap.layer.policy.ProjectionChangePolicy
     * @see com.bbn.openmap.layer.policy.StandardPCPolicy
     * @see com.bbn.openmap.layer.policy.ListResetPCPolicy
     */
    public void projectionChanged(ProjectionEvent pe) {    
	getProjectionChangePolicy().projectionChanged(pe);
    }

    /**
     * Get the ProjectionChangePolicy that determines how a layer
     * reacts and gathers OMGraphics for a projection change.
     */
    public ProjectionChangePolicy getProjectionChangePolicy() {
	if (projectionChangePolicy == null) {
	    projectionChangePolicy = new StandardPCPolicy(this);
	}
	return projectionChangePolicy;
    }

    /**
     * Set the ProjectionChangePolicy that determines how a layer
     * reacts and gathers OMGraphics for a projection change.
     */
    public void setProjectionChangePolicy(ProjectionChangePolicy pcp) {
	projectionChangePolicy = pcp;
    }

    /**
     * Get the RenderPolicy that determines how an OMGraphicList is
     * rendered.
     */
    public RenderPolicy getRenderPolicy() {
	if (renderPolicy == null) {
	    renderPolicy = new StandardRenderPolicy(this);
	}
	return renderPolicy;
    }

    /**
     * Set the RenderPolicy that determines how the OMGraphicList is
     * rendered.
     */
    public void setRenderPolicy(RenderPolicy rp) {
	renderPolicy = rp;
    }

    public void interrupt() {
	try {
	    if (layerWorker != null) {
		layerWorker.interrupt();
	    }
	} catch (SecurityException se) {
	    Debug.output(getName() + " layer caught a SecurityException when something tried to stop work on the worker thread");
	}
    }

    protected void setLayerWorker(SwingWorker worker) {
	layerWorker = worker;
    }

    protected SwingWorker getLayerWorker() {
	return layerWorker;
    }

    /**
     * This method is here to provide a default action for Layers as
     * they act as a ProjectionPainter.  Normally, ProjectionPainters
     * are expected to receive the projection, gather/create
     * OMGraphics that apply to the projection, and render them into
     * the Graphics provided.  This is supposed to be done in the
     * same thread that calls this function, so the caller knows that
     * when this method returns, everything that the
     * ProjectionPainter needed to do is complete.<P> If the layer
     * doesn't override this method, then the paint(Graphics) method
     * will be called.
     *
     * @param proj Projection of the map.
     * @param g java.awt.Graphics to draw into.  
     */
    public synchronized void renderDataForProjection(Projection proj, Graphics g) {
	if (proj == null) {
	    Debug.error("Layer(" + getName() + 
			").renderDataForProjection: null projection!");
	    return;
	} else if (!proj.equals(getProjection())) {
	    setProjection(proj.makeClone());
	    setList(prepare());
	}
	paint(g);
    }

    /**
     * The default action is to get the OMGraphicList and render it.
     * @param g java.awt.Graphics object to render OMGraphics into.
     */
    public void paint(Graphics g) {
	getRenderPolicy().paint(g);
    }

    /**
     * A method that will launch a LayerWorker thread to call the
     * prepare method.  This method will set in motion all the steps
     * needed to create and render the current OMGraphicList with the
     * current projection.  Nothing more needs to be called, because
     * the LayerWorker will be started, it will call prepare().
     * Inside the prepare() method, the OMGraphicList should be
     * created and the OMGraphics generated for the current projection
     * that can be picked up in the getProjection() method, and the
     * LayerWorker will call workerComplete() which will call
     * repaint() on this layer.
     */
    public void doPrepare() {
	// If there isn't a worker thread working on a projection
	// changed or other doPrepare call, then create a thread that
	// will do the real work. If there is a thread working on
	// this, then set the cancelled flag in the layer.
	if (layerWorker == null) {
	    layerWorker = new LayerWorker();
	    layerWorker.execute();
	}
	else setCancelled(true);
    }

    /**
     * The method that gets called by the swing worker thread to get
     * something done.  Returns an OMGraphicList that is the fruit of
     * all the labours.  This method, for the OMGraphicHandler class,
     * just returns the current OMGraphicList.  You should generate
     * the OMGraphics returned on the list, and the current projection
     * is available by calling getProjection(); If you call prepare
     * directly, you may need to call repaint(), too.  If the
     * SwingWorker calls prepare, it will call repaint().
     */
    public OMGraphicList prepare() {
	OMGraphicList currentList = getList();
	Projection proj = getProjection(); 

	// if the layer hasn't been added to the MapBean 
	// the projection could be null.
	if (currentList != null && proj != null) {
	    currentList.generate(proj);
	}

	return currentList;
    }

    /**
     * Set when the something has changed while a swing worker is
     * gathering graphics, and we want it to stop early. 
     */
    protected boolean cancelled = false;

    /**
     * Used to set the cancelled flag in the layer.  The swing worker
     * checks this once in a while to see if the projection has
     * changed since it started working.  If this is set to true, the
     * swing worker quits when it is safe. 
     */
    public synchronized void setCancelled(boolean set) {
	if (set) {
	    interrupt();// if the layerWorker is busy, stop it.
	}
	cancelled = set;
    }

    /** Check to see if the cancelled flag has been set. */
    public synchronized boolean isCancelled() {
	return cancelled;
    }

    /** 
     * The LayerWorker calls this method on the layer when it is
     * done working.  If the calling worker is not the same as the
     * "current" worker, then a new worker is created.
     *
     * @param worker the worker that has the graphics.
     */
    protected synchronized void workerComplete(LayerWorker worker) {
	if (!isCancelled()) {
	    layerWorker = null;
	    setList((OMGraphicList)worker.get());
	    repaint();
	}
	else{
	    setCancelled(false);
	    layerWorker = new LayerWorker();
	    layerWorker.execute();
	}
    }

    /**
     * Since we can't have the main thread taking up the time to
     * do the work to create OMGraphics, we use this worker thread to do it.
     */
    class LayerWorker extends SwingWorker {
	/** Constructor used to create a worker thread. */
	public LayerWorker() {
	    super();
	}

	/**
	 * Compute the value to be returned by the <code>get</code>
	 * method. 
	 */
	public Object construct() {
	    Debug.message("layer", getName()+"|LayerWorker.construct()");
	    fireStatusUpdate(LayerStatusEvent.START_WORKING);
	    String msg;

	    try {

		long start = System.currentTimeMillis();
		OMGraphicList list = getRenderPolicy().prepare();
		long stop = System.currentTimeMillis();
		if (Debug.debugging("layer")) {
		    int size = list.size();
		    Debug.output(getName() + "|LayerWorker.construct(): fetched "+
				 size + " graphics in " + 
				 (double)((stop-start)/1000d) + " seconds");
		}
		return list;

	    } catch (OutOfMemoryError e) {
		msg = getName() +  "|LayerWorker.construct(): " + e;
		Debug.error(msg);
		e.printStackTrace();
	    } catch (Exception e) {
		msg = getName() + "|LayerWorker.construct(): " + e;
		Debug.error(msg);
		e.printStackTrace();
	    }

	    fireRequestMessage(new InfoDisplayEvent(this, msg));
	    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	    return null;
	}

	/**
	 * Called on the event dispatching thread (not on the worker
	 * thread) after the <code>construct</code> method has
	 * returned. 
	 */
	public void finished() {
	    workerComplete(this);
	    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	}
    }
}
