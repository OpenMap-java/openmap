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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/ScaleFilterLayer.java,v $
// $RCSfile: ScaleFilterLayer.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer;

import java.awt.Graphics;
import java.awt.event.*;
import java.util.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * An OpenMap Layer that encapsulates other layers and acts as a scale
 * filter.  It will delegate responsibility to one of several layers
 * depending on the scale.  Repainting is undefined unless the
 * encapsulated layers broadcast LayerStatusEvents to indicate when
 * they're finished working (ready with graphics).
 * <p>
 * <h3>HACK TODO</h3>
 * <ul>
 * <li>Need to implement more listener types.
 * <li>Need to override more base-class methods and delegate to the
 * appropriate layer.
 * </ul>
 * <P>
 * To use this layer, list it as a layer in the openmap.properties
 * file in the openmap.layers properties, as you would add any other
 * layer. Then, add these properties to the openmap.properties
 * file. The layers added to the ScaleFilterLayer do not get added to
 * the openmap.layers property, but instead get added to the
 * scaledFilterLayer.layers property listed here.  Then, the
 * properties for these layers are added to the openmap.properties
 * file like any other layer.
 * <BR>
 * The properties for this layer look like this:<BR>
 * <BR>
 * <code><pre>
 * #######################################
 * # Properties for ScaleFilterLayer
 * #######################################
 * scaledFilterLayer.class=com.bbn.openmap.layer.ScaleFilterLayer
 * scaledFilterLayer.prettyName=&ltPretty name used on menu&ge
 * # List 2 or more layers, larger scale layers first
 * scaledFilterLayer.layers=layer_1 layer_2 layer_3 ...
 * # List the transition scales to switch between layers
 * scaledFilterLayer.transitionScales= (transition scale from layer 1 to 2) (transition scale from layer 2 to 3) (...)
 * #######################################
 * </pre></code>
 */
public class ScaleFilterLayer extends Layer
    implements LayerStatusListener {

    /**
     * The layers property.
     */
    public final static transient String layersProperty = ".layers";

    /**
     * The transition scales property.
     */
    public final static transient String transitionScalesProperty = ".transitionScales";

    /**
     * The layers.
     */
    protected Vector layers;

    /**
     * The transition scales.
     */
    protected float[] transitionScales;

    /**
     * The default transition scale.
     */
    protected float defaultTransitionScale = 40000000f;

    /**
     * The index of the currently selected layer.
     */
    protected int targetIndex = -1;

    /**
     * HACK.
     * This is only being used so that layers can prompt a repaint.
     * Layers which reference the MapBean for anything else will be
     * screwed up by the current implementation.  We need to make this
     * a fully-fledged clone of the "real" MapBean, or find a
     * different solution to the repaint() conundrum...
     */
    private static class PseudoMapBean extends MapBean {
	protected ScaleFilterLayer layer;

	static {
	    suppressCopyright = true;
	}

	public PseudoMapBean(ScaleFilterLayer layer) {
	    super();
	    this.layer = layer;
	}

	public void repaint(long tm, int x, int y, int width, int height) {
	    layer.repaint(tm, x, y, width, height);
	}
    }
    private PseudoMapBean pseudoMB;


    /**
     * Initializes an empty layer.
     */
    public ScaleFilterLayer() {
	pseudoMB = new PseudoMapBean(this);
    }

    /**
     * Initializes this layer from the given properties.
     *
     * @param props the <code>Properties</code> holding settings for this layer
     */
    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);
	parseLayers(prefix, props);
	parseScales(prefix, props);
    }

    /**
     * Get the layer that's appropriate at the current scale.  The
     * targetedIndex needs to be set before this is called.  The
     * targetedIndex is the index to the layers array representing the
     * current layer.
     *
     * @return Layer 
     */
    protected Layer getAppropriateLayer() {
	Vector target = layers;
	if (target == null) {
	    return SinkLayer.getSharedInstance();
	}

	if ((targetIndex < 0) || (targetIndex > target.size())) {
	    return SinkLayer.getSharedInstance();
	}

	Layer l = (Layer)target.elementAt(targetIndex);
	return l;
    }

    /**
     * Create the Layers from a property value string.
     * @param prefix String
     * @param props Properties
     */
    protected void parseLayers(String prefix, Properties props) {
	String layersString = props.getProperty(prefix + layersProperty);
	layers = new Vector();
	if (layersString == null || layersString.equals("")) {
	    Debug.error("ScaleFilterLayer(): null layersString!");
	    return;
	}
	StringTokenizer tok = new StringTokenizer(layersString);
	while (tok.hasMoreTokens()) {
	    Object obj;
	    String layerName = tok.nextToken();
	    String classProperty = layerName + ".class";
	    String className = props.getProperty(classProperty);
	    if (className == null) {
		Debug.error(
			"ScaleFilterLayer.parseLayers(): " +
			"Failed to locate property \""
			+ classProperty + "\"");
		Debug.error(
			"ScaleFilterLayer.parseLayers(): " +
			"Skipping layer \"" + layerName + "\"");
		className = SinkLayer.class.getName();
	    }

	    try {
		if (className.equals(SinkLayer.class.getName()))
		    obj = SinkLayer.getSharedInstance();
		else
		    obj = Class.forName(className).newInstance();
		if (Debug.debugging("ScaleFilterLayer")) {
		    Debug.output(
			    "ScaleFilterLayer.parseLayers(): " +
			    "Instantiated " + className);
		}
	    } catch (Exception e) {
		Debug.error(
			"ScaleFilterLayer.parseLayers(): " +
			"Failed to instantiate \""
			+ className + "\": " + e);
		obj = SinkLayer.getSharedInstance();
	    }

	    // create the layer and set its properties
	    if (obj instanceof Layer) {
		Layer l = (Layer) obj;
		l.setProperties(layerName, props);
		l.addLayerStatusListener(this);
		layers.addElement(l);
	    }
	}
    }

    /**
     * Create the transition scales from a property value string.  If
     * there are N layers, there should be N-1 transition scales.
     * @param prefix String
     * @param props Properties
     */
    protected void parseScales(String prefix, Properties props) {
	StringTokenizer tok = null;
	int size = layers.size();
	if (size > 0)
	    --size;
	transitionScales = new float[size];
	String scales = props.getProperty(prefix + transitionScalesProperty);
	if (scales == null) {
	    Debug.error(
			"ScaleFilterLayer.parseScales(): " +
			"Failed to locate property \""
			+ transitionScalesProperty + "\"");
	    if (transitionScales.length > 0)
		transitionScales[0] = defaultTransitionScale;
	    for (int i=1; i<transitionScales.length; i++) {
		transitionScales[i] = transitionScales[i-1]/3;
	    }
	    return;
	}

	try {
	    tok = new StringTokenizer(scales);
	    transitionScales[0] = (tok.hasMoreTokens())
		? new Float(tok.nextToken()).floatValue()
		: defaultTransitionScale;
	} catch (NumberFormatException e) {
	    Debug.error(
		    "ScaleFilterLayer.parseScales()1: " + e);
	    transitionScales[0] = defaultTransitionScale;
	}

	for (int i=1; i<transitionScales.length; i++) {
	    try {
		transitionScales[i] = (tok.hasMoreTokens())
		    ? new Float(tok.nextToken()).floatValue()
		    : transitionScales[i-1]/3;
	    } catch (NumberFormatException e) {
		Debug.error(
			"ScaleFilterLayer.parseScales()2: " + e);
		transitionScales[i] = transitionScales[i-1]/3;
	    }
	}
    }

    /** 
     * Implementing the ProjectionPainter interface.
     */
    public synchronized void renderDataForProjection(Projection proj, java.awt.Graphics g){
	if (proj == null){
	    Debug.error("ScaleFilterLayer.renderDataForProjection: null projection!");
	    return;
	} else {
	    boolean changed = setTargetIndex(proj.getScale());
	    
	    Layer layer = getAppropriateLayer();
	    // Try to keep the layer and the Psudeo MB up to date.
	    if (changed) {
		pseudoMB.setLayers(new LayerEvent(
		    this, LayerEvent.REPLACE, new Layer [] { layer }));
	    }
	    layer.renderDataForProjection(proj, g);
	}
    }

    /**
     * Calculate the index of the target layer.  If there are N
     * layers, there are N-1 transitionScales.  The ith layer is
     * chosen if the scale is greater than the ith transitionScale.
     * @param scale the current map scale
     * @return true if the targetIndex has changed as a result of the
     * new scale.  
     */
    public boolean setTargetIndex(float scale) {
	boolean changed = false;

	float[] target = transitionScales;
	int i;
	for (i=0; i<target.length; i++) {
	    if (scale > target[i]) {
		if (targetIndex != i)
		    changed = true;
		targetIndex = i;
		break;
	    }
	}
	if (i == target.length) {
	    if (targetIndex != i)
		changed = true;
	    targetIndex = i;
	}
	if (Debug.debugging("ScaleFilterLayer")) {
	    Debug.output("layer targetIndex="+targetIndex);
	}
	return changed;
    }

    /**
     * Handles projection change notification events.  Throws out
     * old graphics, and requests new graphics from the spatial index
     * based on the bounding rectangle of the new <code>Projection</code>.
     *
     * @param ev the new projection event */
    public void projectionChanged(ProjectionEvent ev) {
	Projection proj = ev.getProjection();
	boolean changed = setTargetIndex(proj.getScale());

	// get the appropriate layer and invoke projectionChanged
	Layer layer = getAppropriateLayer();
	if (changed) {
	    pseudoMB.setLayers(new LayerEvent(
			this, LayerEvent.REPLACE, new Layer [] { layer }));
	}

	fireStatusUpdate(LayerStatusEvent.START_WORKING);
	layer.projectionChanged(ev);
    }

    /**
     * Renders the layer on the map.
     *
     * @param g a graphics context
     */
    public void paint(Graphics g) {
	Layer layer = getAppropriateLayer();
	layer.paint(g);
	fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
    }

    //----------------------------------------------------------------
    // Component Listener implementation
    //----------------------------------------------------------------

    /**
     * Repaint the map when we get a FINISH_WORKING notification.
     * @param evt LayerStatusEvent
     */
    public void updateLayerStatus(LayerStatusEvent evt) {
	int status = evt.getStatus();
	if (status == LayerStatusEvent.FINISH_WORKING) {
	    repaint();
	}
	fireStatusUpdate(evt);
    }
}
