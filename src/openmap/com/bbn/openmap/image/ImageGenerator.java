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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/Attic/ImageGenerator.java,v $
// $RCSfile: ImageGenerator.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.image;

import java.awt.*;
import java.awt.event.ContainerEvent;
import java.util.*;
import com.bbn.openmap.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.util.Debug;

/**
 * Provides a way to paint a map into a java.awt.Image.  Give the
 * ImageGenerator an array of Layers and a Projection and it will
 * notify an ImageReadyListener when the java.awt.Image is ready.
 * @deprecated because the ImageReadyListener is deprecated.  Use the
 * other ImageServer methods instead.
 */
public class ImageGenerator extends BufferedMapBean 
    implements LayerStatusListener {

    /**
     * The Hashtable that keeps track of the status of the 
     * layers.
     */
    protected Hashtable layerStatus;
    /** 
     * The flag for the GIFMapBean to automatically draw after it
     * hears from all the layers that it will hear from.  That is,
     * when it gets a LayerStatusEvent.FINISH_WORKING from all the
     * layers that have first sent a LayerStatusEvent.START_WORKING.
     * If false, it will wait for something else to tell it when to
     * paintLayers().  Default is true.
     * */
    protected boolean autoPaint = true;
    /** The number of layers added.  Used to keep track of when all
     * the layers are done gathering objects to draw on the image.
     * */
    protected int numLayers = 0;
    /** The object to notify when the image is ready. */
    protected ImageReadyListener imageReadyListener;
    /** A timestamp ID of the current image request. */
    protected long currentRequestID;

    /**
     * The constructor to generate an Image from the given layers.
     * The ImageListener will have its handleGraphics(graphics)
     * methods called when all's done.
     *
     * @param proj the projection of the map.
     * @param layers an array of layers to drawn on the map.
     * @param requestID a unique identifier to use for the caller.
     * The ImageGenerator doesn't use this number, but provides it to
     * the listener as a reference.
     * @param listener the object to notify when the image is complete.  
     */
    public ImageGenerator(Projection proj, Layer[] layers, long requestID,
			  ImageReadyListener listener){
	suppressCopyright = true;
	generateImage(proj, layers, requestID, listener);
    }

    /**
     * The image to generate an Image from the given layers.  The
     * ImageListener will have its handleGraphics(graphics) methods
     * called when all's done.  This is the method to call if you are
     * reusing the ImageGenerator.
     *
     * @param proj the projection of the map.  If the projection is
     * null, the listener is called.  The projection must be set here
     * for an image to be created.
     * @param layers an array of layers to drawn on the map.  If
     * layers == null, the ImageGenerator will wait until addLayers()
     * is called to do anything.  If layers.length = 0, the
     * ImageReadyListener is called because there is nothing to do.
     * @param requestID a unique identifier to use for the caller.
     * The ImageGenerator doesn't use this number, but provides it to
     * the listener as a reference.
     * @param listener the object to notify when the image is
     * complete.  This should not be set to null, unless you don't
     * want anything to happen.
     */
    public void generateImage(Projection proj, Layer[] layers, long requestID,
			      ImageReadyListener listener) {

	boolean allGood = true;
	suppressCopyright = true;
	layerStatus = new Hashtable();
	imageReadyListener = listener;
	currentRequestID = requestID;
	// The layers will get the projection when they are added as
	// components
	if (proj != null) {
	    projection = (Proj)proj;
	    super.setSize(proj.getWidth(), proj.getHeight());	
	    
	    if (layers != null && layers.length != 0) {
		addLayers(layers);
	    } else {
		if (layers == null) {
		    Debug.message("generator", "ImageGenerator: null layers added to ImageGenerator - nothing happens without layers.");
		} else {
		    Debug.message("generator", "ImageGenerator: no layers added to ImageGenerator - nothing to do.");
		    allGood = false;
		}
	    }
	} else {
	    Debug.error("ImageGenerator: Created without projection.");
	}

	if (!allGood) {
	    if (imageReadyListener != null) {
		imageReadyListener.imageReady(this, requestID);
	    }
	}
    }

    /**  
     * The Component method add() has to be changed to be able to use
     * any Graphics correctly.  The layers have to be told that they
     * are being used by the ImageGenerator, which changes how they
     * repaint themselves.  The ImageGenerator has to add itself as a
     * LayerStatusListener, too, so it can tell when each layer is
     * done collecting graphic objects, and therefore is ready to be
     * painted.
     *
     * @param comp the Component to add.
     * @return the component added.
     */
    public Component add(Component comp) {
	if (comp instanceof Layer) {
	    Layer layer = (Layer) comp;
	    
	    Layer.setAWTAvailable(false);
	    layer.addLayerStatusListener(this);
	    layerStatus.put(layer, Boolean.FALSE);
	    if (Debug.debugging("generator")) {
		Debug.output("ImageGenerator: adding layer " + 
			     layer);
	    }
	}
	return super.add(comp);
    }

    /**  
     * Adds the layers as a group, which is absolutely necessary for
     * running the ImageGenerator.  Adding them this way lets the
     * ImageGenerator know how many layers there are, so it can handle
     * waiting for all of them to return in a multi-threaded
     * environment. Make sure the projection is all set before making
     * this call.
     *
     * @param layers the array of layers to add to the map.  Top layer
     * is first. 
     */
    public void addLayers(Layer[] layers) {
	numLayers = layers.length;
	int nullLayers = 0;

    	setLayers(new LayerEvent(this, LayerEvent.REPLACE, layers));

	for (int i=0; i < numLayers; i++) {
	    if (layers[i] == null) {
		nullLayers++;
	    } else {
		addProjectionListener(layers[i]);
	    }
	}
	numLayers -= nullLayers;
    }

    /**
     * ContainerListener Interface method.
     * Should not be called directly.  Part of the ContainerListener
     * interface, and it's here to make the MapBean a good Container
     * citizen. 
     * @param e ContainerEvent
     */
    public void componentAdded(ContainerEvent e) {
	// Blindly cast.  addImpl has already checked to be
	// sure the child is a Layer.

	// This is the difference between this class, and the MapBean
	// - we don't want to hand the projection off to the component
	// until all the layers have been added (as components).  So
	// wait, and do this later....
	// addProjectionListener((Layer)e.getChild());
	changeLayers(e);

	// If the new layer is in the queue to have removed() called on it
	// take it off the queue, and don't add it to the added() queue.
	// Otherwise, add it to the queue to have added() called on it.
 	if (!removedLayers.removeElement(e.getChild())) 
 	    addedLayers.addElement(e.getChild());
    }

    /**
     * Get the ImageReadyListener that wants to know when all the
     * layers are done.
     *
     * @return ImageReadyListener 
     */
    public ImageReadyListener getImageReadyListener() {
	return imageReadyListener;
    }

    /** 
     * Update the Layer status, per the LayerStatusListener interface
     * implementation.  When this is called, the ImageGenerator looks
     * at the status of all the layers it has, and if they are done,
     * it will call paintLayers() itself.  Assumes that the layer
     * array (children) are not null.
     *
     * @param evt LayerStatusEvent 
     */
    public synchronized void updateLayerStatus(LayerStatusEvent evt) {
	if (Debug.debugging("generator")) {
	    Debug.output("ImageGenerator: Getting layer update from " +
			 evt.getLayer().getName());
	}

	Boolean currentStatus;
	int status = evt.getStatus();
	Layer layer = evt.getLayer();
	currentStatus = (Boolean)layerStatus.get(layer);
	if (currentStatus == null) layerStatus.put(layer, Boolean.FALSE);
	if (status == LayerStatusEvent.START_WORKING) return;
	else if (status == LayerStatusEvent.FINISH_WORKING) {
	    layerStatus.remove(layer);
	    layerStatus.put(layer, Boolean.TRUE);

	    // Remove the ImageGenerator as a LayerStatusListener for
	    // the layer that is complete, since we don't care about
	    // what it is up to anymore.
  	    layer.removeLayerStatusListener(this);
	    removeProjectionListener(layer);

	    boolean allReady = true;
	    for (int i = 0; i < numLayers; i++) {
		if (i < currentLayers.length) {
		    layer = (Layer)currentLayers[i];
		    currentStatus = (Boolean)layerStatus.get(layer);
		    if (currentStatus == Boolean.FALSE) {
			allReady = false;
		    }
		} else if (i >= numLayers - 1) {
		    // Something is probably messed up, but let's do
		    // our best to handle it.  Since we may have
		    // checked to see if all the so-called layer
		    // statuses have been checked, if they have, we
		    // should go with it.  If allReady == true, then
		    // the image will be made correcty for all the
		    // layers that the MapBean thinks are added.
		    // Otherwise, we'll return, hoping that the layers
		    // that we think are outstanding will eventually
		    // call in....

		    //  Fall through, all layers added to MapBean
		    //  called in ...
		    Debug.error("ImageGenerator: Number of layers sent in do not match what the MapBean thinks it contains.");
		} else {
		    return;
		}
	    }

	    //  If all the layers are done, paint them, and call the
	    //  listener
	    if (allReady) {
		Debug.message("generator", "ImageGenerator.updateLayerStatus():  all layers ready, notifying imageReadyListener.");
		// Tells the ImageReadyListener that the Image is
		// ready.
		if (imageReadyListener != null) {
		    imageReadyListener.imageReady(this, currentRequestID);

		    // Cleanup...
		    imageReadyListener = null;
		    layerStatus.clear();
		}
	    }
	}
    }

    /** 
     * Uses the given Graphics to paint all the layers. Assumes that
     * the child layers are not null.
     *  
     * @param Graphics to paint the layers to.  
     */
    public void paintLayers(java.awt.Graphics g) {
	// Paint the water!
	projection.drawBackground(g);
	for (int i = currentLayers.length - 1; i >= 0; i--) {
	    if (Debug.debugging("generator")) {
		Debug.output("ImageGenerator: painting layer " + 
			     (Layer)currentLayers[i]);
	    }
	    ((Layer)currentLayers[i]).paint(g);
	}
    }

//      protected void finalize() {
//  	if (Debug.debugging("gc")) {
//  	    Debug.output("ImageGenerator: GC'd");
//  	}
//      }
}






