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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/LayerHandler.java,v $
// $RCSfile: LayerHandler.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.event.*;
import com.bbn.openmap.plugin.*;

import java.awt.event.*;
import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import java.beans.*;
import java.beans.beancontext.*;

/**
 * The LayerHandler is a component that keeps track of all Layers for
 * the MapBean, whether or not they are currently part of the map or
 * not.  It is able to dynamically add and remove layers from the list
 * of available layers.  Whether a layer is added to the MapBean
 * depends on the visibility setting of the layer.  If
 * Layer.isVisible() is true, the layer will be added to the MapBean.
 * There are methods within the LayerHandler that let you change the
 * visibility setting of a layer.  <P> 
 *
 * The LayerHandler is able to take a Properties object, and create
 * layers that are defined within it.  The key property is "layers",
 * which may or may not have a prefix for it.  If that property does
 * have a prefix (prefix.layers, i.e. openmap.layers), then that
 * prefix has to be known and passed in to the contructor or init
 * method.  This layers property should fit the general openmap marker
 * list paradigm, where the marker names are listed in a space
 * separated list, and then each marker name is used as a prefix for
 * the properties for a particular layer.  As a minimum, each layer
 * needs to have the class and prettyName properties defined.  The
 * class property should define the class name to use for the layer,
 * and the prettyName property needs to be a name for the layer to be
 * used in the GUI.  Any other property that the particular layer can
 * use should be listed in the Properties, with the applicable marker
 * name as a prefix.  Each layer should have its available properties
 * defined in its documentation.  For example:<P><pre>
 *
 * openmap.layers=marker1 marker2 (etc)
 * marker1.class=com.bbn.openmap.layer.GraticuleLayer
 * marker1.prettyName=Graticule Layer
 * # false is default
 * marker1.addToBeanContext=false
 *
 * marker2.class=com.bbn.openmap.layer.shape.ShapeLayer
 * marker2.prettyName=Political Boundaries
 * marker2.shapeFile=pathToShapeFile
 * marker2.spatialIndex=pathToSpatialIndexFile
 * marker2.lineColor=FFFFFFFF
 * marker2.fillColor=FFFF0000
 * </pre><P>
 *
 * The LayerHandler is a SoloMapComponent, which means that for a
 * particular map, there should only be one of them.  When a
 * LayerHandler is added to a BeanContext, it will look for a MapBean
 * to connect to itself as a LayerListener so that the MapBean will
 * receive LayerEvents - this is the mechanism that adds and removes
 * layers on the map.  If more than one MapBean is added to the
 * BeanContext, then the last MapBean added will be added as a
 * LayerListener, with any prior MapBeans added as a LayerListener
 * removed from the LayerHandler.  The MapHandler controls the
 * behavior of multiple SoloMapComponent addition to the BeanContext.
 */
public class LayerHandler extends OMComponent
    implements SoloMapComponent, Serializable {

    /** 
     * Property for space separated layers. If a prefix is needed,
     * just use the methods that let you use the prefix - don't worry
     * about the period, it will be added automatically.
     */
    public static final String layersProperty = "layers";
    /**
     * Property for space separated layers to be displayed at
     * startup. If a prefix is needed, just use the methods that let
     * you use the prefix - don't worry about the period, it will be
     * added automatically. 
     */
    public static final String startUpLayersProperty = "startUpLayers";
    /**
     * The thing that's interested in the layer arrangement.
     */
    protected transient LayerSupport listeners = new LayerSupport(this);
    /**
     * The list of all layers, even the ones that are not part of the
     * map. 
     */
    protected Layer[] allLayers = new Layer[0];
    /**
     * This handle is only here to keep it appraised of layer prefix
     * names. 
     */
    private PropertyHandler propertyHandler;

    /** 
     * If you use this constructor, the LayerHandler expects that the
     * layers will be created and added later, either by addLayer() or
     * init().
     */
    public LayerHandler() {}

    /**
     * Start the LayerHandler, and have it create all the layers as
     * defined in a properties file.
     * @param props properties as defined in an openmap.properties file.
     */
    public LayerHandler(Properties props) {
	init(null, props);
    }

    /**
     * Start the LayerHandler, and have it create all the layers as
     * defined in a properties file.
     * @param prefix the prefix for the layers and startUpLayers
     * properties, as if they are listed as prefix.layers, and
     * prefix.startUpLayers.
     * @param props properties as defined in an openmap.propertites file.  
     */
    public LayerHandler(String prefix, Properties props) {
	init(prefix, props);
    }

    /**
     * Start the LayerHandler with configured layers.
     */
    public LayerHandler(Layer[] layers) {
	init(layers);
    }

    /**
     * Extension of the OMComponent.  If the LayerHandler is created
     * by the ComponentFactory (via the PropertyHandler), this method
     * will be called automatically.  For the OpenMap applications,
     * this method is rigged to handle the openmap.layers property by
     * calling init("openmap", props).  If you are using the
     * LayerHandler in a different setting, then you might want to
     * just call init() directly, or extend this class and have
     * setProperties do what you want.
     */
    public void setProperties(String prefix, Properties props) {
	init(Environment.OpenMapPrefix, props);
    }

    /**
     * Initialize the LayerHandler by having it construct it's layers
     * from a properties object.  The properties should be created
     * from an openmap.properties file.
     * @param prefix the prefix to use for the layers and
     * startUpLayers properties.
     * @param props properties as defined in an openmap.properties
     * file.
     */
    public void init(String prefix, Properties props) {
	init(getLayers(prefix, props));
    }

    /**
     * Initialize the LayerHandler by having it construct it's layers
     * from a URL containing an openmap.properties file.
     * @param url a url for a properties file.
     */
    public void init(java.net.URL url) {
	init(null, url);
    }

    /**
     * Initialize the LayerHandler by having it construct it's layers
     * from a URL containing an openmap.properties file.
     * @param prefix the prefix to use for the layers and
     * startUpLayers properties.
     * @param url a url for a properties file.  
     */
    public void init(String prefix, java.net.URL url) {
	try {
	    java.io.InputStream in = url.openStream();
	    Properties props = new Properties();
	    props.load(in);
	    init(getLayers(prefix, props));
	} catch (java.net.MalformedURLException murle) {
	    Debug.error("LayerHandler.init(URL): " + url + " is not a valid URL");
	} catch (java.io.IOException e) {
	    Debug.error("LayerHandler.init(URL): Caught an IOException");
	}
    }

    /**
     * Initialize from an array of layers. This will cause the
     * LayerListeners, if they exist, to update themselves    
     * with the current list of layers.
     *
     * @param layers the initial array of layers. 
     */
    public void init(Layer[] layers) {
	setLayers(layers);
    }

    /**
     * This is the method that gets used to parse the layer properties
     * from an openmap.properties file, where the layer marker names
     * are listed under a layers property, and each layer is then
     * represented by a marker.class property, and a maker.prettyName
     * property.
     * @param p properties containing layers property, the
     * startupLayers property listing the layers to make visible
     * immediately, and the layer properties as well. 
     * @return Layer[] of layers created from the properties.  
     */
    protected Layer[] getLayers(Properties p) {
	return getLayers(null, p);
    }

    /**
     * This is the method that gets used to parse the layer
     * properties from an openmap.properties file, where the layer
     * marker names are listed under a prefix.layers property, and
     * each layer is then represented by a marker.class property, and
     * a maker.prettyName property.
     * @param prefix the prefix to use to use for the layer list
     * (layers) property and the startUpLayers property.  If it is
     * not null, this will cause the method to looke for
     * prefix.layers and prefix.startUpLayers.
     * @param p the properties to build the layers from.  
     * @return Layer[]
     */
    protected Layer[] getLayers(String prefix, Properties p) {
	Debug.message("layerhandler", 
		      "LayerHandler: Getting new layers from properties...");

	// First, load the layer marker names into a vector for later use
 	Vector startuplayers;
	Vector layersValue;

	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	startuplayers = PropUtils.parseSpacedMarkers(p.getProperty(prefix + startUpLayersProperty));
	layersValue = PropUtils.parseSpacedMarkers(p.getProperty(prefix + layersProperty));

	if (startuplayers.isEmpty()) {
	    Debug.message("layerhandler", "LayerHandler: No layers on startup list");	    
	}

	if (layersValue.isEmpty()) {
	    Debug.error("LayerHandler.getLayers(): No property \"" + layersProperty + "\" found in properties.");
	    return new Layer[0];
	} else {
	    if (Debug.debugging("layerhandler")) {
		Debug.output("LayerHandler: Layer markers found = " + layersValue);
	    }
	}
	
	Layer[] layers = getLayers(layersValue, startuplayers, p);

	// You don't want to do this, it sets up a cycle...
//    	addLayersToBeanContext(layers);
//  	loadLayers(null);

	return layers;
    }

    /**
     * A static method that lets you pass in a Properties object,
     * along with two Vectors of strings, each Vector representing
     * marker names for layers contained in the Properties.
     * <P>
     * If a PlugIn is listed in the properties, the LayerHandler will
     * create a PlugInLayer for it and set the PlugIn in that layer.
     *
     * @param layerList Vector of marker names to use to inspect the
     * properties with.
     * @param visibleLayerList Vector of marker names representing the
     * layers that should initially be set to visible when created, so
     * that those layers are initially added to the map.
     * @param p Properties object containing the layers properties.
     * @return Layer[] 
     */
    public static Layer[] getLayers(Vector layerList, 
				    Vector visibleLayerList, 
				    Properties p) {

	int nLayerNames = layerList.size();
	Vector layers = new Vector(nLayerNames);

	for (int i = 0; i < nLayerNames; i++) {
	    String layerName = (String)layerList.elementAt(i);
	    String classProperty = layerName + ".class";
	    String className = p.getProperty(classProperty);
	    if (className == null) {
		Debug.error("LayerHandler.getLayers(): Failed to locate property \"" + classProperty + "\"\n  Skipping layer \"" + layerName + "\"");
		continue;
	    }

	    Object obj = ComponentFactory.create(className, layerName, p);
	    Layer l;

	    if (obj instanceof Layer) {
		l = (Layer) obj;
	    } else if (obj instanceof PlugIn) {

		PlugInLayer pl = new PlugInLayer();
		pl.setProperties(layerName, p);
		pl.setPlugIn((PlugIn)obj);
		l = pl;
	    } else {
		Debug.error("LayerHandler: Skipped \"" + layerName + "\" " +
			    (obj==null?" - unable to create ":", type " + obj.getClass().getName()));
		continue;
	    }
	    
	    // Figure out of the layer is on the startup list,
	    // and make it visible if it is...
	    l.setVisible(visibleLayerList.contains(layerName));
	    // The ComponentFactory does this now
	    //  		l.setProperties(layerName, p);
	    
	    layers.addElement(l);
	    
	    if (Debug.debugging("layerhandler")) {
		Debug.output("LayerHandler: layer " + l.getName() +
			     (l.isVisible()?" is visible":" is not visible"));
	    }
	}

	int nLayers = layers.size();
	if (nLayers == 0) {
	    return new Layer[0];
	} else {
	    Layer[] value = new Layer[nLayers];
	    layers.copyInto(value);
	    return value;
	}
    }

    /** 
     * Add a LayerListener to the LayerHandler, in order to be told
     * about layers that need to be added to the map.  The new
     * LayerListener will receive two events, one telling it all the
     * layers available, and one telling it which layers are active
     * (visible).
     * @param ll LayerListener, usually the MapBean or other GUI
     * components interested in providing layer controls.  
     */
    public void addLayerListener(LayerListener ll) {
	Debug.message("layerhandler", "LayerHandler: adding layer listener");
	listeners.addLayerListener(ll);
	// Usually, the listeners are interested in one type of event
	// or the other.  So fire both, and let the listener hash it
	// out.
	ll.setLayers(new LayerEvent(this, LayerEvent.ALL, allLayers));
	ll.setLayers(new LayerEvent(this, LayerEvent.ADD, getMapLayers()));
    }

    /** 
     * Add a LayerListener to the LayerHandler, in order to be told
     * about layers that need to be added to the map.
     * @param ll LayerListener, usually the MapBean or other GUI
     * components interested in providing layer controls.
     */
    public void removeLayerListener(LayerListener ll) {
	if (listeners != null) {
	    listeners.removeLayerListener(ll);
	}
    }

    /** 
     * Set all the layers held by the LayerHandler.  The visible
     * layers will be sent to listeners interested in visible layers
     * (LayerEvent.REPLACE), and the list of all layers will be sent
     * to listeners interested in LayerEvent.ALL events.
     * @param layers Layer array of all the layers to be held by the
     * LayerHandler.  
     */
    public void setLayers(Layer[] layers) {
	allLayers = layers;

	if (Debug.debugging("layerhandler")) {
	    Debug.output("LayerHandler.setLayers: " + layers);
	}
	
	if (listeners != null) {
	    Debug.message("layerhandler","firing LayerEvent.ALL on LayerListeners");
	    listeners.fireLayer(LayerEvent.ALL, allLayers);
	    Debug.message("layerhandler","firing LayerEvent.REPLACE on LayerListeners");
	    listeners.fireLayer(LayerEvent.REPLACE, getMapLayers());
	} else {
	    Debug.message("layerhandler","LayerListeners object is null");
	}
    }

    /**
     * If you are futzing with the layer visibility outside the
     * perview of the LayerHandler (not using the turnLayerOn()
     * methods) then you can call this to get all the listeners using
     * the current set of visible layers.
     */
    public void setLayers() {
	setLayers(allLayers);
    }

    /**  
     * Tell anyone interested in the layers to update the layer pretty
     * names.  Same as setLayers().
     * @deprecated. Replaced by setLayers().
     */
    public void updateLayerLabels() {
	setLayers(allLayers);
    }

    /**
     * Get a layer array, of potential layers that CAN be added to the
     * map, not the ones that are active on the map.  A new array is
     * returned, containing the current layers.
     *
     * @return new Layer[] containing new layers.
     */
    public synchronized Layer[] getLayers() {
	if (allLayers == null) {
	    return new Layer[0];    
 	} else {
	    Layer[] layers = new Layer[allLayers.length];
	    System.arraycopy(allLayers, 0, layers, 0, allLayers.length);
	    return layers;
	}
    }

    /** 
     * Get the layers that are currently part of the Map - the ones
     * that are visible.
     * @return an Layer[] of visible Layers. 
     */
    public Layer[] getMapLayers() {
	Debug.message("layerhandler", "LayerHandler.getMapLayers()");

	int numEnabled = 0;
	int cakeIndex = 0;
	Layer[] cake = null;
	Layer[] layers = getLayers();
	
	// First loop finds out how many visible layers there are,
	// Second loop creates the layer cake of visible layers.
	for (int j = 0; j < 2; j++) {
	    for (int i = 0; i < layers.length; i++) {
		if (layers[i] != null && layers[i].isVisible()) {
		    if (j==0) {
			numEnabled++;
		    } else {
			cake[cakeIndex++] = layers[i];
		    }
		}
	    }
	    if (j == 0) {
		cake = new Layer[numEnabled];
	    }
	}
	return cake;
    }
    
    /**
     * Move a layer to a certain position.  Returns true if the layer
     * exists in the LayerHandler, false if is doesn't.  No action is
     * taken if the layer isn't already added to the LayerHandler
     * stack. If the position is 0 or less the layer is moved on
     * top. If the position is greater or equal to the number of
     * layers, the layer is moved to the bottom of the pile.
     *
     * @param layer the layer to move.
     * @param position the array index to place it, shifting the other
     * layers up or down, depending on where the layer is originally.
     * @return true if the layer is already contained in the
     * LayerHandler, false if not.
     */
    public boolean moveLayer(Layer layer, int toPosition) {
	boolean found = false;
      
	if (allLayers == null) {
	    return false;
	}
     
	int i = 0;
	for (i = 0; i < allLayers.length; i++) {
	    if (layer == allLayers[i]) {
		found = true;
		break;
	    }
	}
     
	if (found) {
	    // i should be set to the index of the layer.
	    int pos = toPosition;
	
	    if (pos < 0) {
		pos = 0;
	    } else if (pos >= allLayers.length) {
		pos = allLayers.length - 1;
	    }
	
	    if (pos == i) {
		return true;
	    }
	
	    Layer movedLayer = allLayers[i];
	
	    int direction;
	    if (i > pos) {
		direction = -1;
	    } else {
		direction = 1;
	    }
	
	    while (i != pos) {
		allLayers[i] = allLayers[i + direction];
		i+=direction;
	    }
	    allLayers[pos] = movedLayer;
	    setLayers(allLayers);
	}
      
	return found;
    }

   /** 
     * Add a layer to the bottom of the layer stack.  If the layer is
     * already part of the layer stack, nothing is done.
     *
     * @param layer the layer to add.  
     */
    public void addLayer(Layer layer) {
	if (allLayers == null) {
	    addLayer(layer, 0);
	    return;
	}
     
	int i = 0;
	for (i = 0; i < allLayers.length; i++) {
	    if (layer == allLayers[i]) {
		return;
	    }
	}

	addLayer(layer, allLayers.length + 1);
    }

    /** 
     * Add a layer to a certain position in the layer array.  If the
     * position is 0 or less, the layer is put up front (on top).  If
     * the position is greater than the length of the current array,
     * the layer is put at the end, (on the bottom).  The layer is     
     * placed on the map if it's visiblity is true.  A Layer can only
     * be added once.  If you add a layer that is already added to the
     * LayerHandler, it will be moved to the requested postition.     
     *
     * @param layer the layer to add.
     * @param position the array index to place it.  
     */
    public void addLayer(Layer layer, int position) {
	if (moveLayer(layer, position)) {
	    return;
	}
      
	if (allLayers == null) {
	    allLayers = new Layer[0];
	}
      
	Layer[] newLayers = new Layer[allLayers.length +1];
      
	if (position >= allLayers.length) {
	    // Put the new layer on the bottom
	    System.arraycopy(allLayers, 0, newLayers, 0, allLayers.length);
	    newLayers[allLayers.length] = layer;
	} else if (position <= 0) {
	    // Put the new layer on top
	    System.arraycopy(allLayers, 0, newLayers, 1, allLayers.length);
	    newLayers[0] = layer;
	} else {
	    newLayers[position] = layer;
	    System.arraycopy(allLayers, 0, newLayers, 0, position);
	    System.arraycopy(allLayers, position, newLayers, position + 1,
			     allLayers.length - position);
	}

	if (propertyHandler != null) {
	    String pre = layer.getPropertyPrefix();
	    if (pre != null && pre != "") {
		propertyHandler.addUsedPrefix(layer.getPropertyPrefix());
	    }
	}

	// Add the layer to the BeanContext, if it wants to be.	
	BeanContext bc = getBeanContext();
	if (bc != null && layer.getAddToBeanContext()) {
	    bc.add(layer);
	}
      
	setLayers(newLayers);
    }
  
    /** 
     * Add a layer to a certain position in the layer array.  If the
     * position is 0 or less, the layer is put up front (on top).  If
     * the position is greater thatn the length of the current array,
     * the layer is put at the end, (on the bottom).
     *
     * @param layer the layer to add.
     * @param position the array index to place it. 
     * @param addedLayerTurnedOn turn the layer on.
     * @deprecated the layer will be turned on if its visibility is true.
     */
    public void addLayer(Layer layer, int position, 
			 boolean addedLayerTurnedOn) {
	layer.setVisible(addedLayerTurnedOn);
	addLayer(layer, position);
    }

    /**
     * Remove a layer from the list of potentials.
     *
     * @param layer to remove.
     */
    public void removeLayer(Layer layer) {
	int index = -1;
	for (int i = 0; i < allLayers.length; i++) {
	    if (layer == allLayers[i]) {
		index = i;
		break;
	    }
	}
	// If the layer is actually there...
	if (index != -1) {
	    removeLayer(allLayers, index);
	}
    }

    /**
     * Remove a layer from the list of potentials.
     *
     * @param index of layer in the layer array.  Top-most is first.  
     */
    public void removeLayer(int index) {
	if (index >=0 && index < allLayers.length) {
	    removeLayer(allLayers, index);
	}
    }

    public boolean hasLayer(Layer l) {
	Layer[] layers = allLayers;
	for (int i = 0; i < layers.length; i++) {
	    if (layers[i] == l) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Remove all the layers.
     */
    public void removeAll() {
	BeanContext bc = getBeanContext();
	Layer[] oldLayers = allLayers;

	allLayers = new Layer[0];
	for (int i = 0; i < oldLayers.length; i++) {
	    turnLayerOn(false, oldLayers[i]);
	    oldLayers[i].clearListeners();
	}

	setLayers(allLayers);

	for (int i = 0; i < oldLayers.length; i++) {
	    // Remove the layer to the BeanContext, if it wants to be.	
	    if (bc != null) {
		bc.remove(oldLayers[i]);
	    }
	    oldLayers[i] = null;
	}

	System.gc();
    }

    /** 
     * The version that does the work.  The other two functions do
     * sanity checks. Calls setLayers(), and removes the layer from
     * the BeanContext.
     * 
     * @param currentLayers the current layers handled in the LayersMenu.
     * @param the validated index of the layer to remove.  
     */
    protected void removeLayer(Layer[] currentLayers, int index) {
	Layer rLayer = currentLayers[index];
	rLayer.setVisible(false);

	Layer[] newLayers = new Layer[currentLayers.length - 1];
	System.arraycopy(currentLayers, 0, newLayers, 0, index);
	System.arraycopy(currentLayers, index + 1, newLayers, index,
			 currentLayers.length - index - 1);

	// Remove the layer to the BeanContext, if it wants to be.	
	BeanContext bc = getBeanContext();
	if (bc != null) {
	    bc.remove(rLayer);
	}
	turnLayerOn(false, rLayer);
	rLayer.clearListeners();
	rLayer = null;

	// Shouldn't call this, but it's the only thing that seems to
	// make it work...
	if (Debug.debugging("helpgc")) {
	    System.gc();
	}

	setLayers(newLayers);
    }

    /**
     * Take a layer that the LayersMenu knows about, that may or may
     * not be a part of the map, and change its visibility by
     * adding/removing it from the MapBean.
     *
     * @param setting true to add layer to the map.
     * @param layer the index of the layer to turn on/off.
     * @return true of index represented a layer, false if not or if
     * something went wrong.
     */
    public boolean turnLayerOn(boolean setting, int index) {
	try {
	    turnLayerOn(setting, allLayers[index]);
	    return true;
	} catch (ArrayIndexOutOfBoundsException aoobe) {
	    // Do nothing...
	} catch (NullPointerException npe) {
	    // Do nothing...
	}
	return false;
    }

    /**
     * Take a layer that the LayersMenu knows about, that may or may
     * not be a part of the map, and change its visibility by
     * adding/removing it from the MapBean.  If the layer is not
     * found, it's added and the visibility depends on the setting
     * parameter.
     *
     * @param setting true to add layer to the map.
     * @param layer the layer to turn on.  
     * @return true if the layer was found, false if not or if
     * something went wrong.
     */
    public boolean turnLayerOn(boolean setting, Layer layer) {
	
	if ((setting && !layer.isVisible()) || 
	    (!setting && layer.isVisible())) {
	    if (Debug.debugging("layerhandler")) {
		Debug.output("LayerHandler: turning " +
			     layer.getName() + (setting?" on":" off"));
	    }
	    
	    layer.setVisible(setting);
	    if (listeners != null) {
		listeners.fireLayer(LayerEvent.REPLACE, getMapLayers());
	    }
	    return true;
	}	    
  	return false;
    }
    
    /**
     * Called from childrenAdded(), when a new component is added to
     * the BeanContext, and from setBeanContext() when the
     * LayerHandler is initially added to the BeanContext.  This
     * method takes the iterator provided when those methods are
     * called, and looks for the objects that the LayerHandler is
     * interested in, namely, the MapBean, the PropertyHandler, or
     * any other LayerListeners.  The LayerHandler handles multiple
     * LayerListeners, and if one is found, it is added to the
     * LayerListener list.  If a PropertyHandler is found, then
     * init() is called, effectively resetting the layers held by the
     * LayerHandler.
     * 
     * @param it Iterator with objects to look through.  
     */
    public void findAndInit(Iterator it) {
	while(it.hasNext()) {
	    Object someObj = it.next();
	    if (someObj instanceof com.bbn.openmap.event.LayerListener) {
		Debug.message("layerhandler","LayerHandler found a LayerListener.");	   
		addLayerListener((LayerListener)someObj);	      
	    }

	    if (someObj instanceof Layer) {
		if (Debug.debugging("layerhandler")) {
		    Debug.output("LayerHandler found a Layer |" + 
				 ((Layer)someObj).getName() + "|");
		}
		if (!hasLayer((Layer)someObj)) {
		    addLayer((Layer)someObj);
		}
	    }

	    if (someObj instanceof PropertyHandler) {
		// Used to notify the PropertyHandler of used property
		// prefix names.
		propertyHandler = (PropertyHandler)someObj;
	    }
	}
    }

    /**
     * A BeanContextMembershipListener interface method, which is
     * called when new objects are removed from the BeanContext. If a
     * LayerListener or Layer is found on this list, it is removed from the
     * list of LayerListeners.
     * @param bcme an event containing an Iterator containing removed
     * objects.
     */
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
	Iterator it = bcme.iterator();
	while(it.hasNext()){
	    Object someObj = it.next();
	    if (someObj instanceof com.bbn.openmap.event.LayerListener){	      
		Debug.message("layerhandler","LayerListener object is being removed");	  
		removeLayerListener((LayerListener)someObj);	      
	    }

	    if (someObj instanceof Layer) {
		removeLayer((Layer)someObj);       
	    }

	    if (someObj instanceof PropertyHandler &&
		propertyHandler == someObj) {
		propertyHandler = null;
	    }
	}
    }

    /**
     * Add layers to the BeanContext, if they want to be.  Since the
     * BeanContext is a Collection, it doesn't matter if a layer is
     * already there because duplicates aren't allowed.
     *
     * @param layers layers to add, if they want to be.
     */
    public void addLayersToBeanContext(Layer[] layers) {
	BeanContext bc = getBeanContext();
	if (bc == null || layers == null) {
	    return;
	}

	for (int i = 0; i < layers.length; i++) {
	    if (layers[i].getAddToBeanContext()) {
		bc.add(layers[i]);
	    }
	}
    }

    /**
     * Called when the LayerHandler is added to a BeanContext.  This
     * method calls findAndInit() to hook up with any objects that may
     * already be added to the BeanContext.  A BeanContextChild
     * method.
     * @param in_bc  BeanContext.  
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
	if (in_bc != null) {
	    if (Debug.debugging("layerhandler")) {
		Debug.output("LayerHandler.setBeanContext()");
	    }
	    in_bc.addBeanContextMembershipListener(this);
	    beanContextChildSupport.setBeanContext(in_bc);

	    // findAndInit should be called after the layers and
	    // plugins are added to the MapHandler, so they can find
	    // the components they need before they get added to the
	    // map (if they are to be added at startup).
	    addLayersToBeanContext(getLayers());

	    // Calling this here may (will) cause the MapBean to get
	    // loaded with its initial layers, since it is a
	    // LayerListener.
	    findAndInit(in_bc.iterator());
	}
    }
}
