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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/OMGraphicHandlerLayer.java,v $
// $RCSfile: OMGraphicHandlerLayer.java,v $
// $Revision: 1.25 $
// $Date: 2004/10/12 17:13:43 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Graphics;
import java.awt.Shape;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.policy.ProjectionChangePolicy;
import com.bbn.openmap.layer.policy.RenderPolicy;
import com.bbn.openmap.layer.policy.StandardPCPolicy;
import com.bbn.openmap.layer.policy.StandardRenderPolicy;
import com.bbn.openmap.omGraphics.FilterSupport;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.event.GestureResponsePolicy;
import com.bbn.openmap.omGraphics.event.MapMouseInterpreter;
import com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.SwingWorker;

/**
 * The OMGraphicHandlerLayer is a layer that provides OMGraphicHandler
 * support. With this support, the OMGraphicHandlerLayer can accept
 * OMAction instructions for managing OMGraphics, and can perform
 * display filtering as supported by the FilterSupport object.
 * <P>
 * 
 * When extending this class for a simple layer, they only method you
 * need to override is the prepare() method. This is a good class to
 * use to start writing your own layers. Start with overriding the
 * prepare() method, having it return an OMGraphicList containing
 * OMGraphics on the map that are appropriate for the current
 * projection.
 * <P>
 * 
 * The OMGraphicHandlerLayer already has an OMGraphicList variable, so
 * if you extend this class you don't have to manage another one. You
 * can add your OMGraphics to the list provided with getList(). If you
 * create a list of OMGraphics that is reused and simply re-projected
 * when the projection changes, do nothing - that's what happens
 * anyway based on the default ProjectionChangePolicy set for the
 * layer (StandardPCPolicy). You can either create an OMGraphicList in
 * the constructor and set it by calling setList(OMGraphicList), or
 * you can test for a null OMGraphicList returned from getList() in
 * prepare() and create one if it needs to be. If the list isn't null,
 * make sure you still call generate on it. The advantage of waiting
 * to create the list in prepare is that the processing time to create
 * the OMGraphics is delayed until the layer is added to the map. If
 * you create OMGraphics in the constructor, you delay the entire
 * program (maybe startup of the map!) while the OMGraphics are
 * created.
 * <P>
 * 
 * If you let prepare() create a new OMGraphicList based on the new
 * projection, then make sure the ProjectionChangePolicy for the layer
 * is set to a com.bbn.openmap.layer.policy.ResetListPCPolicy, or at
 * least clear out the old graphics at some point before adding new
 * OMGraphics to the list in that method. You just have to do one, not
 * both, of those things. If you are managing a lot of OMGraphics and
 * do not null out the list, you may see your layer appear to lag
 * behind the projection changes. That's because another layer with
 * less work to do finishes and calls repaint, and since your list is
 * still set with OMGraphics ready for the old projection, it will
 * just draw what it had, and then draw again when it has finished
 * working. Nulling out the list will prevent your layer from drawing
 * anything on the new projection until it is ready.
 * <P>
 * 
 * The OMGraphicHandlerLayer has support built in for launching a
 * SwingWorker to do work for you in a separate thread. This behavior
 * is controlled by the ProjectionChangePolicy that is set for the
 * layer. Both the StandardPCPolicy and ListResetPCPolicy launch
 * threads by calling doPrepare() on this layer. The StandardPCPolicy
 * only calls this if the number of OMGraphics on its list is greater
 * than some cutoff value.
 * <P>
 * 
 * useLayerWorker variable is true (default), then doPrepare() will be
 * called when a new ProjectionEvent is received in the
 * projectionChanged method. This will cause prepare() to be called in
 * a separate thread. You can use prepare() to create OMGraphics, the
 * projection will have been set in the layer and is available via
 * getProjection(). You should generate() the OMGraphics in prepare.
 * NOTE: You can override the projectionChanged() method to
 * create/manage OMGraphics any way you want. The SwingWorker only
 * gets launched if doPrepare() gets called.
 * <P>
 * 
 * MouseEvents are not handled by a MapMouseInterpreter, with the
 * layer being the GestureResponsePolicy object dictating how events
 * are responded to. The interpreter does the work of fielding
 * MapMouseEvents, figuring out if they concern an OMGraphic, and
 * asking the policy what it should do in certain situations,
 * including providing tooltips, information, or opportunities to edit
 * OMGraphics. The mouseModes property can be set to the MapMouseMode
 * IDs that the interpreter should respond to.
 * <P>
 * 
 * For OMGraphicHandlerLayers, there are several properties that can
 * be set that dictate important behavior:
 * 
 * <pre>
 * 
 * 
 *  layer.projectionChangePolicy=pcp
 *  layer.pcp.class=com.bbn.openmap.layer.policy.StandardPCPolicy
 * 
 *  layer.renderPolicy=srp
 *  layer.srp.class=com.bbn.openmap.layer.policy.StandardRenderPolicy
 *  # or
 *  layer.renderPolicy=ta
 *  layer.ta.class=com.bbn.openmap.layer.policy.RenderingHintsRenderPolicy
 *  layer.ta.renderingHints=KEY_TEXT_ANTIALIASING
 *  layer.ta.KEY_TEXT_ANTIALIASING=VALUE_TEXT_ANTIALIAS_ON
 * 
 *  layer.mouseModes=Gestures
 *  layer.consumeEvents=true
 *  
 * </pre>
 */
public class OMGraphicHandlerLayer extends Layer implements
        GestureResponsePolicy {

    /**
     * The property that can be set for the ProjectionChangePolicy.
     * This property should be set with a scoping marker name used to
     * define a policy class and any other properties that the policy
     * should use. "projectionChangePolicy"
     * 
     * @see com.bbn.openmap.layer.policy.ProjectionChangePolicy
     * @see com.bbn.openmap.layer.policy.StandardPCPolicy
     * @see com.bbn.openmap.layer.policy.ListResetPCPolicy
     */
    public final static String ProjectionChangePolicyProperty = "projectionChangePolicy";
    /**
     * The property that can be set for the RenderPolicy. This
     * property should be set with a marker name used to define a
     * policy class and any other properties that the policy should
     * use. "renderPolicy"
     * 
     * @see com.bbn.openmap.layer.policy.StandardRenderPolicy
     * @see com.bbn.openmap.layer.policy.BufferedImageRenderPolicy
     * @see com.bbn.openmap.layer.policy.RenderingHintsRenderPolicy
     */
    public final static String RenderPolicyProperty = "renderPolicy";

    /**
     * The property that can be set to tell the layer which mouse
     * modes to listen to. The property should be a space-separated
     * list of mouse mode IDs, which can be specified for a
     * MapMouseMode in the properties file or, if none is specified,
     * the default ID hard-coded into the MapMouseMode. "mouseModes"
     */
    public final static String MouseModesProperty = "mouseModes";

    /**
     * The property that can be set to tell the layer to consume mouse
     * events. The maim reason not to do this is in case you have
     * OMGraphics that you are moving, and you need other layers to
     * respond to let you know when you are over the place you think
     * you need to be.
     */
    public final static String ConsumeEventsProperty = "consumeEvents";

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

    protected String[] mouseModeIDs = null;

    /**
     * A flag to tell the layer to be selfish about consuming
     * MouseEvents it receives. If set to true, it will consume events
     * so that other layers will not receive the events. If false,
     * lower layers will also receive events, which will let them
     * react too. Intended to let other layers provide information
     * about what the mouse is over when editing is occuring.
     */
    protected boolean consumeEvents = false;

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
    public OMGraphicList filter(Shape shapeBoundary, boolean getInsideBoundary) {
        return filter.filter(shapeBoundary, getInsideBoundary);
    }

    /**
     * To find out whether SQL queries are handled.
     * 
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
     * Get the OMGraphicList held by the layer. May be null.
     */
    public OMGraphicList getList() {
        return filter.getList();
    }

    /**
     * Indicates if the OMGraphicHandler can have its OMGraphicList
     * set.
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
     * Don't set to null. This is here to let subclasses put a
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
     * projection changes. Will trigger a repaint().
     * <p>
     * 
     * The ProjectionEvent is passed to the current
     * ProjectionChangePolicy object, which determines what will
     * happen on the layer and how. By default, a StandardPCPolicy is
     * notified with the projection change, and it will test the
     * projection for changes and make sure prepare() is called. It
     * will make the decision whether doPrepare() is called, based on
     * the number of OMGraphics on the list, which may launch a swing
     * worker thread to call prepare(). The StandardPCPolicy does not
     * do anything to the OMGraphicList when the projection changes.
     * <p>
     * 
     * If you need the OMGraphicList cleared out with a new
     * projection, you can substitute a ListRestPCPolicy for the
     * StandardPCPolicy. You would want to do this if your
     * OMGraphicList changes for different projections - The reason
     * the OMGraphicList is nulled out is so if another layer finishes
     * before yours does and gets repainted, your old OMGraphics don't
     * get painted along side their new ones - it's a mismatched
     * situation. You can set the ProjectionChangePolicy directly with
     * the setProjectionChangePolicy, or by overriding the
     * getProjectionChangePolicy method and returning the type you
     * want by default if it is null.
     * 
     * @see com.bbn.openmap.layer.policy.ProjectionChangePolicy
     * @see com.bbn.openmap.layer.policy.StandardPCPolicy
     * @see com.bbn.openmap.layer.policy.ListResetPCPolicy
     */
    public void projectionChanged(ProjectionEvent pe) {
        if (Debug.debugging("layer")) {
            Debug.output("OMGraphicHandlerLayer " + getName()
                    + " projection changed, calling "
                    + getProjectionChangePolicy().getClass().getName());
        }
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
        // Just to make sure,
        pcp.setLayer(this);
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
        // Just to make sure,
        rp.setLayer(this);
    }

    protected void interrupt() {
        try {
            if (layerWorker != null) {
                layerWorker.interrupt();
            }
        } catch (SecurityException se) {
            Debug.output(getName()
                    + " layer caught a SecurityException when something tried to stop work on the worker thread");
        }
    }

    /**
     * Sets the SwingWorker off to call prepare(). If the SwingWorker
     * passed in is not null, start() is called on it.
     * 
     * @param worker null to reset the layerWorker variable, or a
     *        SwingWorker to start up.
     */
    protected void setLayerWorker(SwingWorker worker) {
        synchronized (LAYERWORKER_LOCK) {
            layerWorker = worker;

            if (layerWorker != null) {
                layerWorker.start();
            }
        }
    }

    protected SwingWorker getLayerWorker() {
        return layerWorker;
    }

    /**
     * This method is here to provide a default action for Layers as
     * they act as a ProjectionPainter. Normally, ProjectionPainters
     * are expected to receive the projection, gather/create
     * OMGraphics that apply to the projection, and render them into
     * the Graphics provided. This is supposed to be done in the same
     * thread that calls this function, so the caller knows that when
     * this method returns, everything that the ProjectionPainter
     * needed to do is complete.
     * <P>
     * If the layer doesn't override this method, then the
     * paint(Graphics) method will be called.
     * 
     * @param proj Projection of the map.
     * @param g java.awt.Graphics to draw into.
     */
    public synchronized void renderDataForProjection(Projection proj, Graphics g) {
        if (proj == null) {
            Debug.error("Layer(" + getName()
                    + ").renderDataForProjection: null projection!");
            return;
        } else if (!proj.equals(getProjection())) {
            setProjection(proj.makeClone());
            setList(prepare());
        }
        paint(g);
    }

    /**
     * The default action is to get the OMGraphicList and render it.
     * 
     * @param g java.awt.Graphics object to render OMGraphics into.
     */
    public void paint(Graphics g) {
        getRenderPolicy().paint(g);
    }

    /**
     * A method that will launch a LayerWorker thread to call the
     * prepare method. This method will set in motion all the steps
     * needed to create and render the current OMGraphicList with the
     * current projection. Nothing more needs to be called, because
     * the LayerWorker will be started, it will call prepare(). Inside
     * the prepare() method, the OMGraphicList should be created and
     * the OMGraphics generated for the current projection that can be
     * picked up in the getProjection() method, and the LayerWorker
     * will call workerComplete() which will call repaint() on this
     * layer.
     */
    public void doPrepare() {

        if (isWorking()) {
            if (Debug.debugging("layer")) {
                Debug.output(getName()
                        + " layer already working in prepare(), cancelling");
            }
            setCancelled(true);
            return;
        }

        // If there isn't a worker thread working on a projection
        // changed or other doPrepare call, then create a thread that
        // will do the real work. If there is a thread working on
        // this, then set the cancelled flag in the layer.
        setLayerWorker(new LayerWorker());
    }

    /**
     * A check to see if the SwingWorker is doing something.
     */
    public boolean isWorking() {
        boolean ret = false;

        synchronized (LAYERWORKER_LOCK) {
            ret = (layerWorker != null);
        }

        return ret;
    }

    /**
     * This is the main method you should be concerned with when
     * overriding this class. You have to make sure that this method
     * returns an OMGraphicList that is ready to be rendered. That
     * means they need to be generated with the current projection,
     * which can be retrieved by calling getProjection().
     * <P>
     * 
     * This method will be called in a separate thread if doPrepare()
     * is called on the layer. This will automatically cause repaint()
     * to be called, which lets java know to call paint() on this
     * class.
     * <P>
     * 
     * Note that the default action of this method is to get the
     * OMGraphicList as it is currently set in the layer, reprojects
     * the list with the current projection (calls generate() on
     * them), and then returns the current list.
     * <P>
     * 
     * If your layer needs to change what is on the list based on what
     * the current projection is, you can either clear() the list
     * yourself and add new OMGraphics to it (remember to call
     * generate(Projection) on them), and return the list. You also
     * have the option of setting a ListResetPCPolicy, which will
     * automatically set the list to null when the projection changes
     * before calling this method. The OMGraphicHandlerList will
     * ignore a null OMGraphicList.
     * <P>
     * 
     * NOTE: If you call prepare directly, you may need to call
     * repaint(), too. With all invocations of this method that are
     * cause by a projection change, repaint() will be called for you.
     * 
     * The method is synchronized in case renderDataForProjection()
     * gets called while in the middle of this method. For a different
     * projection, that would be bad.
     */
    public synchronized OMGraphicList prepare() {
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
    protected Object CANCELLED_LOCK = new Object();
    protected Object LAYERWORKER_LOCK = new Object();

    /**
     * Used to set the cancelled flag in the layer. The swing worker
     * checks this once in a while to see if the projection has
     * changed since it started working. If this is set to true, the
     * swing worker quits when it is safe.
     */
    public void setCancelled(boolean set) {
        if (set) {
            interrupt();// if the layerWorker is busy, stop it.
        }

        synchronized (CANCELLED_LOCK) {
            cancelled = set;
        }
    }

    /** Check to see if the cancelled flag has been set. */
    public boolean isCancelled() {
        boolean ret = false;
        synchronized (CANCELLED_LOCK) {
            ret = cancelled;
        }
        return ret;
    }

    /**
     * The LayerWorker calls this method on the layer when it is done
     * working. If the calling worker is not the same as the "current"
     * worker, then a new worker is created.
     * 
     * @param worker the worker that has the graphics.
     */
    protected void workerComplete(LayerWorker worker) {
        if (!isCancelled()) {
            setLayerWorker(null);
            getProjectionChangePolicy().workerComplete((OMGraphicList) worker.get());
            repaint();
        } else {
            setCancelled(false);
            setLayerWorker(new LayerWorker());
        }
    }

    /**
     * Since we can't have the main thread taking up the time to do
     * the work to create OMGraphics, we use this worker thread to do
     * it.
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
            Debug.message("layer", getName() + "|LayerWorker.construct()");
            fireStatusUpdate(LayerStatusEvent.START_WORKING);
            String msg;

            try {

                long start = System.currentTimeMillis();
                OMGraphicList list = getRenderPolicy().prepare();
                long stop = System.currentTimeMillis();
                if (Debug.debugging("layer")) {
                    Debug.output(getName()
                            + "|LayerWorker.construct(): fetched "
                            + (list == null ? "null list "
                                    : (list.size() + " graphics ")) + "in "
                            + (double) ((stop - start) / 1000d) + " seconds");
                }
                return list;

            } catch (OutOfMemoryError e) {
                msg = getName() + "|LayerWorker.construct(): " + e;
                Debug.error(msg);
                e.printStackTrace();
            } catch (Exception e) {
                msg = getName() + "|LayerWorker.construct(): " + e;
                Debug.error(msg);
                e.printStackTrace();
            }

            // This is only called if there is an error.
            if (Debug.debugging("displayLayerErrors")) {
                fireRequestMessage(new InfoDisplayEvent(this, msg));
            }

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

    /**
     * Overrides the Layer setProperties method. Also calls Layer's
     * version. If the ProjectionChangePolicy and RenderPolicy objects
     * are set programmatically and are PropertyConsumers, they will
     * still have access to properties if this method is called. Their
     * property prefix will be scoped as if the OMGraphicHandlerLayer
     * had them created, with their prefix being prefix + . +
     * PropertyChangePolicyProperty and prefix + . +
     * RenderPolicyProperty.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

        // Check to see if the layer wants to set its own projection
        // change policy.
        String pcpString = props.getProperty(realPrefix
                + ProjectionChangePolicyProperty);
        String policyPrefix;
        if (pcpString != null) {
            policyPrefix = realPrefix + pcpString;

            // If the projection change policy is null, try to create
            // it.
            if (projectionChangePolicy == null) {
                String pcpClass = props.getProperty(policyPrefix + ".class");
                if (pcpClass == null) {
                    Debug.error("Layer "
                            + getName()
                            + " has "
                            + policyPrefix
                            + " property defined in properties for PropertyChangePolicy, but "
                            + policyPrefix + ".class property is undefined.");
                } else {
                    Object obj = ComponentFactory.create(pcpClass,
                            policyPrefix,
                            props);
                    if (obj != null) {

                        if (Debug.debugging("layer")) {
                            Debug.output("Layer " + getName()
                                    + " setting ProjectionChangePolicy ["
                                    + obj.getClass().getName() + "]");
                        }

                        try {
                            setProjectionChangePolicy((ProjectionChangePolicy) obj);
                        } catch (ClassCastException cce) {
                            Debug.error("Layer "
                                    + getName()
                                    + " has "
                                    + policyPrefix
                                    + " property defined in properties for ProjectionChangePolicy, but "
                                    + policyPrefix
                                    + ".class property ("
                                    + pcpClass
                                    + ") does not define a valid ProjectionChangePolicy. A "
                                    + obj.getClass().getName()
                                    + " was created instead.");
                        }

                    } else {
                        Debug.error("Layer "
                                + getName()
                                + " has "
                                + policyPrefix
                                + " property defined in properties for PropertyChangePolicy, but "
                                + policyPrefix
                                + ".class property does not define a valid PropertyChangePolicy.");
                    }
                }

            } else { // ProjectionChangePolicy is not null...
                // If the projection change policy is not null and the
                // policy is a PropertyConsumer, pass the properties
                // to the policy. Note that the property prefix for
                // the policy is prefix +
                // ProjectionChangePolicyProperty

                if (projectionChangePolicy instanceof PropertyConsumer) {
                    ((PropertyConsumer) projectionChangePolicy).setProperties(policyPrefix,
                            props);
                }
            }

        } else if (Debug.debugging("layer")) {
            Debug.output("Layer " + getName()
                    + " using default ProjectionChangePolicy ["
                    + getProjectionChangePolicy().getClass().getName() + "]");
        }

        // Check to see if the layer want to set its own rendering
        // policy.
        String rpString = props.getProperty(realPrefix + RenderPolicyProperty);
        if (rpString != null) {
            policyPrefix = realPrefix + rpString;
            if (renderPolicy == null) {
                String rpClass = props.getProperty(policyPrefix + ".class");
                if (rpClass == null) {
                    Debug.error("Layer "
                            + getName()
                            + " has "
                            + policyPrefix
                            + " property defined in properties for RenderPolicy, but "
                            + policyPrefix + ".class property is undefined.");
                } else {

                    Object rpObj = ComponentFactory.create(rpClass,
                            policyPrefix,
                            props);

                    if (rpObj != null) {
                        if (Debug.debugging("layer")) {
                            Debug.output("Layer " + getName()
                                    + " setting RenderPolicy ["
                                    + rpObj.getClass().getName() + "]");
                        }

                        try {
                            setRenderPolicy((RenderPolicy) rpObj);
                        } catch (ClassCastException cce) {
                            Debug.error("Layer "
                                    + getName()
                                    + " has "
                                    + policyPrefix
                                    + " property defined in properties for RenderPolicy, but "
                                    + policyPrefix
                                    + ".class property ("
                                    + rpClass
                                    + ") does not define a valid RenderPolicy. A "
                                    + rpObj.getClass().getName()
                                    + " was created instead.");
                        }
                    } else {
                        Debug.error("Layer "
                                + getName()
                                + " has "
                                + policyPrefix
                                + " property defined in properties for RenderPolicy, but "
                                + policyPrefix + ".class property (" + rpClass
                                + ") isn't being created.");
                    }
                }
            } else { // RenderPolicy is not null...
                // Same thing with renderPolicy as with projection
                // change policy.
                if (renderPolicy instanceof PropertyConsumer) {
                    ((PropertyConsumer) renderPolicy).setProperties(policyPrefix,
                            props);
                }
            }

        } else if (Debug.debugging("layer")) {
            Debug.output("Layer " + getName() + " using default RenderPolicy ["
                    + getRenderPolicy().getClass().getName() + "]");
        }

        String mmString = props.getProperty(realPrefix + MouseModesProperty);
        if (mmString != null) {
            Vector mmv = PropUtils.parseSpacedMarkers(mmString);
            if (mmv.size() > 0) {
                String[] mm = new String[mmv.size()];
                Iterator it = mmv.iterator();
                int i = 0;
                while (it.hasNext()) {
                    mm[i] = (String) it.next();
                }
                setMouseModeIDsForEvents(mm);
            }
        }

        consumeEvents = PropUtils.booleanFromProperties(props, realPrefix
                + ConsumeEventsProperty, consumeEvents);
    }

    /**
     * Overrides Layer getProperties method., also calls that method
     * on Layer. Sets the properties from the policy objects used by
     * this OMGraphicHandler layer.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        String policyPrefix = null;

        ////// ProjectionChangePolicy

        ProjectionChangePolicy pcp = getProjectionChangePolicy();
        if (pcp instanceof PropertyConsumer) {
            policyPrefix = ((PropertyConsumer) pcp).getPropertyPrefix();
            ((PropertyConsumer) pcp).getProperties(props);
        }

        if (policyPrefix == null) {
            policyPrefix = prefix + "pcp";
        }

        //Whoops, need to make sure pcp is valid but removing the
        //OMGHL prefix from the front of the policy prefix (if
        //applicable). Same for RenderPolicy

        props.put(prefix + ProjectionChangePolicyProperty,
                policyPrefix.substring(prefix.length()));
        // This has to come after the above line, or the above
        // property will have a trailing period.
        policyPrefix = PropUtils.getScopedPropertyPrefix(policyPrefix);
        props.put(policyPrefix + "class", pcp.getClass().getName());

        RenderPolicy rp = getRenderPolicy();
        if (rp instanceof PropertyConsumer) {
            policyPrefix = ((PropertyConsumer) rp).getPropertyPrefix();
            ((PropertyConsumer) rp).getProperties(props);
        }

        ///// RenderPolicy

        if (policyPrefix == null) {
            policyPrefix = prefix + "rp";
        }

        props.put(prefix + RenderPolicyProperty,
                policyPrefix.substring(prefix.length()));
        // This has to come after the above line, or the above
        // property will have a trailing period.
        policyPrefix = PropUtils.getScopedPropertyPrefix(policyPrefix);
        props.put(policyPrefix + "class", rp.getClass().getName());

        props.put(prefix + ConsumeEventsProperty,
                new Boolean(consumeEvents).toString());

        String[] mm = getMouseModeIDsForEvents();
        if (mm != null && mm.length > 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mm.length; i++) {
                // Don't need any MouseModes that have been scoped to
                // the pretty name, those are automatically generated.
                if (mm[i].equals(getName())) {
                    continue;
                }
                sb.append(mm[i] + " ");
            }
            props.put(prefix + MouseModesProperty, sb.toString());
        }

        return props;
    }

    /**
     * Overrides Layer getProperties method., also calls that method
     * on Layer. Sets the properties from the policy objects used by
     * this OMGraphicHandler layer.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        String policyPrefix = null;

        ProjectionChangePolicy pcp = getProjectionChangePolicy();
        if (pcp instanceof PropertyConsumer) {
            policyPrefix = ((PropertyConsumer) pcp).getPropertyPrefix();
            if (policyPrefix != null) {
                int index = policyPrefix.indexOf(".");
                if (index != -1) {
                    policyPrefix = policyPrefix.substring(index + 1);
                }

                ((PropertyConsumer) pcp).getPropertyInfo(list);
            }
        }

        if (policyPrefix == null) {
            policyPrefix = "pcp";
        }

        list.put(policyPrefix + ".class",
                "Class name of ProjectionChangePolicy (optional)");

        RenderPolicy rp = getRenderPolicy();
        if (rp instanceof PropertyConsumer) {
            policyPrefix = ((PropertyConsumer) rp).getPropertyPrefix();

            if (policyPrefix != null) {
                int index = policyPrefix.indexOf(".");
                if (index != -1) {
                    policyPrefix = policyPrefix.substring(index + 1);
                }
            }

            ((PropertyConsumer) rp).getPropertyInfo(list);
        } else {
        }

        if (policyPrefix == null) {
            policyPrefix = "rp";
        }

        list.put(policyPrefix + ".class",
                "Class name of RenderPolicy (optional)");

        list.put(ConsumeEventsProperty,
                "Flag that tells the layer to consume MouseEvents, or let others use them as well.");
        list.put(ConsumeEventsProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");

        list.put(MouseModesProperty,
                "Space-separated list of MouseMode IDs to receive events from.");

        return list;
    }

    /**
     * The MapMouseInterpreter used to catch the MapMouseEvents and
     * direct them to layer as referencing certain OMGraphics.
     * Mananges how the layer responds to mouse events.
     */
    protected MapMouseInterpreter mouseEventInterpreter = null;

    /**
     * Set the interpreter used to field and interpret MouseEvents,
     * thereby calling GestureResponsePolicy methods on this layer.
     */
    public synchronized void setMouseEventInterpreter(MapMouseInterpreter mmi) {

        if (mmi instanceof StandardMapMouseInterpreter) {
            String[] modeList = getMouseModeIDsForEvents();
            ((StandardMapMouseInterpreter) mmi).setMouseModeServiceList(modeList);
            ((StandardMapMouseInterpreter) mmi).setConsumeEvents(getConsumeEvents());
        }

        if (mouseEventInterpreter != null) {
            // Remove handle
            mouseEventInterpreter.setGRP(null);
        }

        mmi.setGRP(this);
        mouseEventInterpreter = mmi;
    }

    /**
     * Get the interpreter used to field and interpret MouseEvents,
     * thereby calling GestureResponsePolicy methods on this layer.
     * This method checks to see if any mouse modes ids have been set
     * via the getMouseModeIDsForEvents() method, and if there were
     * and the interpreter hasn't been set, it will create a
     * StandardMapMouseInterpreter. Otherwise, it returns whatever has
     * been set as the interpreter, which could be null.
     */
    public synchronized MapMouseInterpreter getMouseEventInterpreter() {
        if (getMouseModeIDsForEvents() != null && mouseEventInterpreter == null) {
            setMouseEventInterpreter(new StandardMapMouseInterpreter(this));
        }

        return mouseEventInterpreter;
    }

    /**
     * Query asked from the MouseDelegator for interest in receiving
     * MapMouseEvents. This returns a MapMouseInterpreter that has
     * been told to listen for events from the MapMouseModes in
     * setMouseModeIDsForEvents().
     */
    public MapMouseListener getMapMouseListener() {
        MapMouseListener mml = getMouseEventInterpreter();

        if (mml != null) {
            if (Debug.debugging("layer")) {

                String[] modes = mml.getMouseModeServiceList();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < modes.length; i++) {
                    sb.append(modes[i] + ", ");
                }

                Debug.output("Layer " + getName() + " returning "
                        + mml.getClass().getName()
                        + " as map mouse listener that listens to: "
                        + sb.toString());
            }
        }

        return mml;
    }

    /**
     * A flag to tell the layer to be selfish about consuming
     * MouseEvents it receives. If set to true, it will consume events
     * so that other layers will not receive the events. If false,
     * lower layers will also receive events, which will let them
     * react too. Intended to let other layers provide information
     * about what the mouse is over when editing is occuring.
     */
    public void setConsumeEvents(boolean consume) {
        consumeEvents = consume;

        if (mouseEventInterpreter instanceof StandardMapMouseInterpreter) {
            ((StandardMapMouseInterpreter) mouseEventInterpreter).setConsumeEvents(getConsumeEvents());
        }
    }

    public boolean getConsumeEvents() {
        return consumeEvents;
    }

    /**
     * This is the important method call that determines what
     * MapMouseModes the interpreter for this layer responds to. The
     * MapMouseInterpreter calls this so it can respond to
     * MouseDelegator queries. You can programmatically call
     * setMouseModeIDsForEvents with the mode IDs to set these values,
     * or set the mouseModes property for this layer set to a
     * space-separated list of mode IDs.
     */
    public String[] getMouseModeIDsForEvents() {
        return mouseModeIDs;
    }

    /**
     * Use this method to set which mouse modes this layer responds
     * to. The array should contain the mouse mode IDs.
     */
    public void setMouseModeIDsForEvents(String[] mm) {

        if (Debug.debugging("layer")) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mm.length; i++) {
                sb.append(mm[i] + " ");
            }

            Debug.output("For layer " + getName() + ", setting mouse modes to "
                    + sb.toString());
        }

        mouseModeIDs = mm;

        if (mouseEventInterpreter instanceof StandardMapMouseInterpreter) {
            ((StandardMapMouseInterpreter) mouseEventInterpreter).setMouseModeServiceList(mm);
        }
    }

    /**
     * Query asking if OMGraphic is highlightable, which means that
     * something in the GUI should change when the mouse is moved or
     * dragged over the given OMGraphic. Highlighting shows that
     * something could happen, or provides cursory information about
     * the OMGraphic. Responding true to this method may cause
     * getInfoText() and getToolTipTextFor() methods to be called
     * (depends on the MapMouseInterpetor).
     */
    public boolean isHighlightable(OMGraphic omg) {
        return true;
    }

    /**
     * Query asking if an OMGraphic is selectable, or able to be
     * moved, deleted or otherwise modified. Responding true to this
     * method may cause select() to be called (depends on the
     * MapMouseInterpertor) so the meaning depends on what the layer
     * does in select.
     */
    public boolean isSelectable(OMGraphic omg) {
        return false;
    }

    /**
     * A current list of select OMGraphics.
     */
    protected OMGraphicList selectedList;

    /**
     * Retrieve the list of currently selected OMGraphics.
     */
    public OMGraphicList getSelected() {
        return selectedList;
    }

    ////// Reactions

    /**
     * Fleeting change of appearance for mouse movements over an
     * OMGraphic.
     */
    public void highlight(OMGraphic omg) {
        omg.select();
        omg.generate(getProjection());
        repaint();
    }

    /**
     * Notification to set OMGraphic to normal appearance.
     */
    public void unhighlight(OMGraphic omg) {
        omg.deselect();
        omg.generate(getProjection());
        repaint();
    }

    /**
     * Designate a list of OMGraphics as selected.
     */
    public void select(OMGraphicList list) {
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                if (selectedList == null) {
                    selectedList = new OMGraphicList();
                }

                OMGraphic omg = (OMGraphic) it.next();
                if (omg instanceof OMGraphicList
                        && !((OMGraphicList) omg).isVague()) {
                    select((OMGraphicList) omg);
                } else {
                    selectedList.add(omg);
                }
            }
        }
    }

    /**
     * Designate a list of OMGraphics as deselected.
     */
    public void deselect(OMGraphicList list) {
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext() && selectedList != null) {
                OMGraphic omg = (OMGraphic) it.next();
                if (omg instanceof OMGraphicList
                        && !((OMGraphicList) omg).isVague()) {
                    deselect((OMGraphicList) omg);
                } else {
                    selectedList.remove(omg);
                }
            }
        }
    }

    /**
     * Remove OMGraphics from the layer.
     */
    public OMGraphicList cut(OMGraphicList omgl) {
        OMGraphicList list = getList();
        if (list != null && omgl != null) {
            Iterator it = omgl.iterator();
            while (it.hasNext()) {
                list.remove((OMGraphic) it.next());
            }
        }
        return omgl;
    }

    /*****************************************************************
     * Return a copy of an OMGraphic. Not implemented yet.
     */
    public OMGraphicList copy(OMGraphicList omgl) {
        return null;
    }

    /**
     * Add OMGraphics to the Layer.
     */
    public void paste(OMGraphicList omgl) {
        OMGraphicList list = getList();
        if (list != null && omgl != null) {
            Iterator it = omgl.iterator();
            while (it.hasNext()) {
                list.add((OMGraphic) it.next());
            }
        }
    }

    /**
     * If applicable, should return a short, informational string
     * about the OMGraphic to be displayed in the
     * InformationDelegator. Return null if nothing should be
     * displayed.
     */
    public String getInfoText(OMGraphic omg) {
        return null;
    }

    /**
     * If applicable, should return a tool tip for the OMGraphic.
     * Return null if nothing should be shown.
     */
    public String getToolTipTextFor(OMGraphic omg) {
        return null;
    }

    /**
     * Return a JMenu with contents applicable to a popup menu for a
     * location over the map. The popup doesn't concern any
     * OMGraphics, and should be presented for a click on the map
     * background.
     * 
     * @param mme a MapMouseEvent describing the location over where
     *        the menu items should apply, in case different options
     *        are appropriate for different places.
     * @return a JMenu for the map. Return null or empty List if no
     *         input required.
     */
    public List getItemsForMapMenu(MapMouseEvent mme) {
        return null;
    }

    /**
     * Return a java.util.List containing input for a JMenu with
     * contents applicable to a popup menu for a location over an
     * OMGraphic.
     * 
     * @return a List containing options for the given OMGraphic.
     *         Return null or empty list if there are no options.
     */
    public List getItemsForOMGraphicMenu(OMGraphic omg) {
        return null;
    }

    /**
     * A query from the MapMouseInterpreter wondering if the
     * GestureResponsePolicy wants events pertaining to mouse
     * movements over the map that are not over an OMGraphic. If the
     * GestureResponsePolicy responds true, then the mouseOver and
     * leftClick methods will be called on the GestureResponsePolicy
     * by the interpreter. There is no rightClick method that is
     * called, because a right click will always cause a
     * getItemsForMapMenu() method to be called.
     */
    public boolean receivesMapEvents() {
        return false;
    }

    /**
     * A notification that the mouse cursor has been moved over the
     * map, not over any of the OMGraphics on the
     * GestureResponsePolicy. This only gets called if the response to
     * receiveMapEvents is true.
     * 
     * @param mme MapMouseEvent describing the location of the mouse.
     * @return true of this information is to be considered consumed
     *         and should not be passed to anybody else.
     */
    public boolean mouseOver(MapMouseEvent mme) {
        return false;
    }

    /**
     * A notification that the mouse has been clicked with the left
     * mouse button on the map, and not on any of the OMGraphics. This
     * only gets called if the response to receiveMapEvents is true.
     * Right clicks on the map are always reported to the
     * getItemsForMapMenu method.
     * 
     * @param mme MapMouseEvent describing the location of the mouse.
     * @return true of this information is to be considered consumed
     *         and should not be passed to anybody else.
     */
    public boolean leftClick(MapMouseEvent mme) {
        return false;
    }

}