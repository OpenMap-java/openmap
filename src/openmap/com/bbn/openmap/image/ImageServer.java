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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageServer.java,v $
// $RCSfile: ImageServer.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.image;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.ImageIcon;

import com.bbn.openmap.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.plugin.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.util.*;
import com.bbn.openmap.layer.util.LayerUtils;

/** 
 * The image server is the class you want to deal with when creating
 * images from the ImageGenerator.  It takes a properties file and
 * sets up the image generator based on those properties. It also has
 * this queuing thing going so that requests can stack up while the
 * image generator is working on requests, and it will notify the
 * requestor when the image is ready.  <P> 
 *
 * The ImageServer generally has the layers on the map predefined at
 * construction, although you can change the layers that it has.  When
 * setting the layer array, do not use the same layer in two different
 * slots of the array - it may not give you the expected map, and may
 * mess around with the timing issues that the ImageGenerator takes
 * care of.  If you want to reorder the layers, do so before adding
 * your request to the ImageServer.  Additionally, each request has
 * the option of not using certain layers in the ImageServer layer
 * array, by turning off the appropriate bits in the layer mask.
 * Understand that the image for a request will be created based on
 * the layer array contents and the request layer mask at the time the
 * request processing is started, not when it is submitted.<P> Right
 * now, the ImageServer is single threaded - processing requests one
 * after another.  The request setup was written to support
 * multi-threaded processing, though, where each image could be
 * generated in it's own thread.  That code is not written - maybe
 * someday.
 * <P><code><pre>
 * # If the ImageServer is created and given a prefix (in this example,
 * # 'imageServer') the properties file should contain the properties:
 * imageServer.layers=<layer1 layer2 ...>
 * layer1.className=<classname>
 * layer1.prettyName=<pretty name of layer>
 * # Add other attributes as required by layer1...
 * layer2.className=<classname>
 * layer2.prettyName=<pretty name of layer>
 * # Add other attributes as required by layer2...
 * # First formatter listed is default.
 * imageServer.formatters=<formatter1 formatter2 ...>
 * formatter1.class=<classname of formatter 1>
 * # Add other formatter1 properties
 * formatter2.class=<classname of formatter 2>
 * </pre></code>
 * <P>
 * NOTE: If you simply hand the ImageServer a standard
 * openmap.properties file, it works with the addition of the first two
 * attributes except WITHOUT the 'imageServer.' prefix.  
 *
 * New for 4.5: If the layers property is not defined, then the
 * openmap.layers property is used to define which layers are
 * available for the ImageServer.
 */
public class ImageServer 
    implements /*ImageReadyListener, ImageReceiver,*/ PropertyConsumer {

    /** The Image formatter for the output image. */
    protected ImageFormatter formatter;

    /**
     * Hashtable of ImageFormatters available to be used.
     */
    protected Hashtable imageFormatters;

    /** The array of layers on the map.  First is on top. */
    protected Layer[] layers;

    /** Property for space separated layers. */
    public static final String ImageServerLayersProperty = "layers";
    
    /** OpenMap prefix */
    public static final String OpenMapPrefix = "openmap.";

    /**
     * Property for the image formatter list of available
     * formats. This propery should contain a space separated list of
     * marker names.
     */
    public static final String ImageFormattersProperty = "formatters";

    /** Property to turn on anti-aliasing. */
    public static final String AntiAliasingProperty = "antialiasing";

    /** Flag to do graphics and text anti-aliasing on the map image. */
    protected boolean doAntiAliasing = false;

    /**
     * A place to hold on to a property prefix in case one is
     * used. Useful for ImageServer properties files where more than
     * one image server is defined. 
     */
    protected String propertiesPrefix = null;

    /**
     * Empty constructor that expects to be configured later.
     */
    protected ImageServer(){}

    /**
     * To create the image server, you hand it a set of properties
     * that let it create an array of layers, and also to set the
     * properties for those layers.  The properties file for the
     * ImageServer looks strikingly similar to the openmap.properties
     * file.  So, all the layers get set up here...
     */
    public ImageServer(Properties props) {
	setProperties(props);
    }

    /**
     * Same as the other constructor, except that the properties can
     * have a prefix in front of them.  The format of the prefix has
     * to match how the property is specified the the properties file,
     * which may include the period - i.e server1.imageServer.layers,
     * the server1. is the prefix that should get passed in.  The
     * ImageMaster does this.
     */
    public ImageServer(String prefix, Properties props) {
	this(prefix, props, null);
    }

    /**
     * Create an ImageServer that should be configured with a
     * Properties file.  The prefix given is to scope the ImageServer
     * properties to this instance.  The Hashtable is for reusing any
     * layers that may already be instantiated.
     */
    public ImageServer(String prefix, Properties props, 
		       Hashtable instantiatedLayers) {
	setProperties(prefix, props, instantiatedLayers);
    }

    /**
     * Create an ImageServer from an array of Layers and an
     * ImageFormatter.  It's assumed that the layers are already
     * configured.
     * @param layers the array of layers.
     * @param formatter the ImageFormatter to use for the output image
     * format.  
     */
    public ImageServer(Layer[] layers, ImageFormatter formatter) {
	this.layers = layers;
	this.formatter = formatter;
    }

    /**
     * Set whether anti-aliasing is used when creating the image.
     */
    public void setDoAntiAliasing(boolean set) {
	doAntiAliasing = set;
    }

    /**
     * Find out whether anti-aliasing is used when creating the image.
     */
    public boolean getDoAntiAliasing() {
	return doAntiAliasing;
    }

    /**
     * Set the layers used on the NEXT request that is processed.
     * Will not affect any image currently being created.
     * @param newLayers an array of com.bbn.openmap.Layer objects,
     * already configured and ready to respond to a projectionChanged
     * method call.
     */
    public synchronized void setLayers(Layer[] newLayers) {
	if (newLayers == null) {
	    layers = new Layer[0];
	} else {
	    layers = newLayers;
	}
    }

    /**
     * Retrieve the current set of layers used for requests.
     * @return Layer[]
     */
    public synchronized Layer[] getLayers() {
	return layers;
    }

    /**
     *  Use the ProjectionPainter interface of the layers to create an
     *  image.   This approach avoids  some of the timing  issues that
     *  the thread model of the MapBean and Layers that seem to pop up
     *  from time to time.  They are Swing components, you know.  They
     *  were designed to be part  of a GUI.  So, this is a serialized,
     *  safe way to do things. 
     * @param proj projection of map.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj) {
	return createImage(proj, -1, -1, 0xFFFFFFFF);
    }

    /**
     *  Use the ProjectionPainter interface of the layers to create an
     *  image.   This approach avoids  some of the timing  issues that
     *  the thread model of the MapBean and Layers that seem to pop up
     *  from time to time.  They are Swing components, you know.  They
     *  were designed to be part  of a GUI.  So, this is a serialized,
     *  safe way to do things. 
     * @param proj projection of map.     
     * @param scaledWidth scaled pixel width of final image.  If you
     * don't want it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image.  If you
     * don't want it scaled, use -1.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj, int scaledWidth, int scaledHeight) {
	return createImage(proj, scaledWidth, scaledHeight, 0xFFFFFFFF);
    }
  
    /**
     * Use the ProjectionPainter interface of the layers to create an
     * image.   This approach avoids  some of the timing  issues that
     * the thread model of the MapBean and Layers that seem to pop up
     * from time to time.  They are Swing components, you know.  They
     * were designed to be part  of a GUI.  So, this is a serialized,
     * safe way to do things. 
     *
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image.  If you
     * don't want it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image.  If you
     * don't want it scaled, use -1.
     * @param showLayers Layer marker names reflecting the layers that
     * should be part of this image.
     * @return a byte[] representing the formatted image.  
     */
    public byte[] createImage(Projection proj, int scaledWidth, int scaledHeight, 
			      Vector showLayers) {
      
	Debug.message("imageserver", "ImageServer: using the new ProjectionPainter interface!  createImage with layer string array. ");

	if (formatter == null) {
	    Debug.error("ImageServer.createImage: no formatter set! Can't create image.");
	    return new byte[0];
	}

	ImageFormatter imageFormatter = formatter.makeClone();
	java.awt.Graphics graphics = createGraphics(imageFormatter,
						    proj.getWidth(),
						    proj.getHeight());

	if (graphics == null) {
	    return new byte[0];
	}
      
	((Proj)proj).drawBackground(graphics);
	int size = showLayers.size();

	if (showLayers != null) {
	    for (int j=size-1; j >= 0; j--) {      	
		for (int i = layers.length - 1; i >= 0; i--) {
		    String layerName = (String)showLayers.elementAt(j);
		    Layer layer = layers[i];
		    if (layerName.equals(layer.getPropertyPrefix())) {
			layer.renderDataForProjection(proj, graphics);
			if (Debug.debugging("imageserver")) {
			    Debug.output("ImageServer: image request adding layer graphics from : " + layer.getName());
			}
		    }			    
		}
	    }
	}

	byte[] formattedImage = getFormattedImage(imageFormatter,
						  scaledWidth, scaledHeight);
	graphics.dispose();
	return formattedImage;
    }

    /**
     * This method returns a integer representing a mask created from
     * the visibility settings of the layers.
     */
    public int calculateVisibleLayerMask() {
	int ret = 0; // Initialize all the layer bits to zero.
	for (int i = layers.length - 1; i >= 0; i--) {
	    if (layers[i].isVisible()) {
		ret = ret | (0x00000001 << i);
	    }
	}
	return ret;
    }

    /**
     *  Use the ProjectionPainter interface of the layers to create an
     *  image.   This approach avoids  some of the timing  issues that
     *  the thread model of the MapBean and Layers that seem to pop up
     *  from time to time.  They are Swing components, you know.  They
     *  were designed to be part  of a GUI.  So, this is a serialized,
     *  safe way to do things. 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image.  If you
     * don't want it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image.  If you
     * don't want it scaled, use -1.
     * @param includedLayerMask a mask signifying which of the
     * ImageServer layers to use in the image.  It's assumed that the
     * called knows which layers are desired.  Bit 1 of the mask
     * refers to layer[0], etc.  A bit turned on means the layer will
     * be included.
     * @return a byte[] representing the formatted image.  
     */
    public byte[] createImage(Projection proj, 
			      int scaledWidth, int scaledHeight,
			      int includedLayerMask) {

	Debug.message("imageserver", "ImageServer: using the new ProjectionPainter interface!  createImage with layer mask.");

	if (formatter == null) {
	    Debug.error("ImageServer.createImage: no formatter set! Can't create image.");
	    return new byte[0];
	}

	ImageFormatter imageFormatter = formatter.makeClone();

	java.awt.Graphics graphics = createGraphics(imageFormatter, 
						    proj.getWidth(),
						    proj.getHeight());

	if (graphics == null) {
	    return new byte[0];
	}

	((Proj)proj).drawBackground(graphics);
	for (int i = layers.length - 1; i >= 0; i--) {
	    
	    if ((includedLayerMask & (0x00000001 << i)) != 0) {
		if (Debug.debugging("imageserver")) {
		    Debug.output("ImageServer: image request adding layer graphics from : "
				 + layers[i].getName());
		}

		layers[i].renderDataForProjection(proj, graphics);
	    } else {
		if (Debug.debugging("imageserver")) {
		    Debug.output("ImageServer: skipping layer graphics from : "
				 + layers[i].getName());
		}
	    }
	}
	
	byte[] formattedImage = getFormattedImage(imageFormatter,
						  scaledWidth, scaledHeight);
	graphics.dispose();
	return formattedImage;
    }

    /**
     * Create a java.awt.Graphics to use for an image.  The Graphics
     * will affect the image contained within the ImageFormatter.
     *
     * @param formatter the ImageFormatter containing the image.
     * @param width the pixel width of the image.
     * @param height the pixel height of the image.
     */
    protected Graphics createGraphics(ImageFormatter formatter, 
				      int width, int height) {

 	java.awt.Graphics graphics = null;
      
	if (formatter != null) {
	    graphics = formatter.getGraphics(width, height);
	} else {
	    Debug.error("ImageServer.createGraphics: Formatter is null, returning null graphics.");
	    return null;
	}
      
	if (graphics == null) {
	    Debug.error("ImageServer.createGraphics: NOT able to create Graphics!");
	    return null;
	}
      
	if (Debug.debugging("imageserver")) {
	    Debug.output("ImageServer.createGraphics: graphics is cool");
	}
      
	if (doAntiAliasing && graphics instanceof java.awt.Graphics2D) {
	    java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
	    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				 RenderingHints.VALUE_ANTIALIAS_ON);
	}
      
	return graphics;
    }

    /**
     * Format the image that is contained in the ImageFormatter,
     * scaling to a particular size if the scaledWidth and
     * scaledHeight are greater than 0.
     */
    protected byte[] getFormattedImage(ImageFormatter formatter, 
				       int scaledWidth, int scaledHeight) {

	if (Debug.debugging("imageserver")) {
	    Debug.output("ImageServer: ready to create formatted image.");
	}
	byte[] formattedImage = null;

	// Now, scale the image, if needed...
	if (scaledWidth > 0 && scaledHeight > 0) {
	    
	    formattedImage = 
		formatter.getScaledImageBytes(scaledWidth, scaledHeight);
	
	} else {
	    Debug.message("imageserver", "ImageServer: using full scale image (unscaled).");
	    formattedImage = formatter.getImageBytes();
	}
      	return formattedImage;
    }

    /**
     * Set the layers and image type in the properties.
     */
    public void setProperties(Properties props) {
	setProperties((String) null, props);
    }

    /**
     * Set the layers and image type in the properties.  The
     * properties might have a prefix in the file.  
     */
    public void setProperties(String prefix, Properties props) {
	setProperties(prefix, props, (Hashtable) null);
    }

    /**
     * Set the layers and image type in the properties.  The
     * properties might have a prefix in the file.  
     */
    public void setProperties(String prefix, Properties props, 
			      Hashtable instantiatedLayers) {
	setPropertyPrefix(prefix);
	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	layers = getLayers(props, instantiatedLayers);
	formatter = getFormatters(props);
	doAntiAliasing = LayerUtils.booleanFromProperties(props, prefix+AntiAliasingProperty, false);
    }

    /**
     * Part of the PropertyConsumer interface.  Doesn't do anything yet.
     */
    public Properties getProperties(Properties props) {
	if (props == null) {
	    props = new Properties();
	}
	return props;
    }

    /**
     * Part of the PropertyConsumer interface.
     */
    public Properties getPropertyInfo(Properties list) {
	if (list == null) {
	    list = new Properties();
	}

	list.put(ImageServerLayersProperty, "A list of marker names (space-separated) for layer definitions");
	list.put(ImageFormattersProperty, "A list of marker names (space-separated) for ImageFormatter definitions");
	list.put(AntiAliasingProperty, "Whether to use anti-aliasing for the image");
	return list;
    }

    /**
     * Part of the PropertyConsumer interface.  Set the Properties
     * prefix to use to scope the relevant properties passed into the
     * setProperties method.
     */
    public void setPropertyPrefix(String prefix) {
	propertiesPrefix = prefix;
    }

    /**
     * Part of the PropertyConsumer interface.  Get the Properties
     * prefix used to scope the relevant properties passed into the
     * setProperties method.
     */
    public String getPropertyPrefix() {
	return propertiesPrefix;
    }

    /** 
     * Given a integer that represents, bitwise, the layers that you
     * want out of the current list held by the ImageServer layer
     * array, return an array of those layers.  
     * @param layerMask bit mask for desired layers, bit 0 is layer 0.
     * @return layer[]     
     */
    protected synchronized Layer[] getMaskedLayers(int layerMask) {
	if (layerMask == 0xFFFFFFFF) {
	    //  They all want to be there
	    Debug.message("imageserver",
			  "ImageServer: image request adding all layers.");
	    return layers;
	} else {
	    //  Use the vector as a growable array, and add the layers
	    //  to it that the mask says should be there.
	    Vector layerVector = new Vector(layers.length);
	    for (int i = 0; i < layers.length; i++) {
		if ((layerMask & (0x00000001 << i)) != 0) {
		    layerVector.add(layers[i]);
		    if (Debug.debugging("imageserver")) {
			Debug.output("ImageServer: image request adding layer: " + layers[i].getName());
		    }
		}
	    }
	    Layer[] imageLayers = new Layer[layerVector.size()];
	    return (Layer[])layerVector.toArray(imageLayers);
	}
    }

    /**
     * Get the ImageFormatter currently used for the image creation.
     * @return ImageFormatter.
     */
    public synchronized ImageFormatter getFormatter() {
	return formatter;
    }

    /**
     * Set the ImageFormatter to be used for ImageCreation.
     */
    public synchronized void setFormatter(ImageFormatter f) {
	formatter = f;
    }

    /**
     * Set the default formatter to the one with the given label.  The
     * label can be retrieved from the ImageFormatter.
     *
     * @param formatterLabel String for a particular formatter.
     * @return true if label matches up with a known formatter, false
     * if no formatter found.
     */
    public synchronized boolean setFormatter(String formatterLabel) {
	ImageFormatter tmpFormatter = 
	    (ImageFormatter)imageFormatters.get(formatterLabel.intern());

	if (tmpFormatter != null) {
	    setFormatter(tmpFormatter);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Get the Hashtable used to hold the ImageFormatters.  The label
     * for each one is the lookup for it in the Hashtable.
     *
     * @return Hashtable of ImageFormatters.
     */
    public synchronized Hashtable getFormatters() {
	return imageFormatters;
    }

    /**
     * Set the ImageFormatter Hashtable to set up the possible choices
     * for image formats.
     *
     * @param iFormatters Hashtable of ImageFormatters
     * @param defaultFormatterKey the key label of the formatter to
     * use for a default.
     */
    public synchronized void setFormatters(Hashtable iFormatters,
					   String defaultFormatterKey) {
	imageFormatters = iFormatters;
	formatter = (ImageFormatter)imageFormatters.get(defaultFormatterKey.intern());
    }

    /**
     * Create an ImageFormatter from the contents of a properties object.
     * @param p Properties used to initialize the Properties.
     * @return default formatter.
     */
    protected ImageFormatter getFormatters(Properties p) {
	String formatterString, formattersString;
	ImageFormatter iFormatter = null;

	String prefix = PropUtils.getScopedPropertyPrefix(this);
	formattersString = p.getProperty(prefix + ImageFormattersProperty);

	// First, look at the formatters string to get a marker list
	// of available formatters.
	if (formattersString != null) {
	    Vector markerNames = PropUtils.parseSpacedMarkers(formattersString);
	    Vector formatters = ComponentFactory.create(markerNames, p);

	    int size = formatters.size();

	    if (imageFormatters == null) {
		imageFormatters = new Hashtable(size);
	    }

	    for (int i = 0; i < size; i++) {
		ImageFormatter formatter = (ImageFormatter) formatters.get(i);
		imageFormatters.put(formatter.getFormatLabel(), formatter);

		if (i == 0) {
		    iFormatter = formatter;
		}
	    }

	} else {
	    Debug.message("imageserver", "ImageServer.getFormatters: no formatters specified");
	}

	return iFormatter;
    }

    /**
     * Create an array of Layers from a properties object.
     */
    protected Layer[] getLayers(Properties p) {
	return getLayers(p, (Hashtable)null);
    }

    /**
     * Create an array of Layers from a properties object.  Reuse the
     * layer from the hashtable if it's there under the same property
     * name.  The Hashtable is kept for an ImageServer that is used
     * buy an ImageMaster or another object that is using different
     * layers for it's image.  It will reuse the layers it's already
     * created if the marker names are the same.
     *
     * @param p properties
     * @param instantiatedLayers a hashtable containing layers, with
     * the prefix layer name used as the key.  
     */
    protected Layer[] getLayers(Properties p, Hashtable instantiatedLayers) {

	String layersValue;
	String prefix = PropUtils.getScopedPropertyPrefix(this);
	layersValue = p.getProperty(prefix + ImageServerLayersProperty);
        
        if (layersValue == null) {
            // get openmap.layers value
            layersValue = p.getProperty(OpenMapPrefix + ImageServerLayersProperty);
    
            if (layersValue == null) {
                Debug.error("ImageServer: No property \"" + ImageServerLayersProperty + "\" found in ImageServer properties.");
                return new Layer[0];
            }
        }

	Vector layerNames = PropUtils.parseSpacedMarkers(layersValue);

	if (Debug.debugging("imageserver")) {
	    Debug.output("OpenMap.getLayers(): "+ layerNames);
	}

	int nLayerNames = layerNames.size();
	Vector layers = new Vector(nLayerNames);

	for (int i = 0; i < nLayerNames; i++) {
	    String layerName = (String)layerNames.elementAt(i);

	    // Check to see if some other ImageServer has used this
	    // layer, and reuse it.
	    if (instantiatedLayers != null) {
		Layer iLayer = (Layer) instantiatedLayers.get(layerName);
		if (iLayer != null) {

		    // We might want to consider adding this:
// 		    iLayer.setProperties(layerName, p);

		    layers.add(iLayer);
		    if (Debug.debugging("imageserver")) {
			Debug.output("ImageServer: adding instantiated layer /"
				     + layerName + "/");
		    }
		    continue;
		}
	    }

	    // Brand new layer, so instantiate it.
	    String classProperty = layerName + ".class";
	    String className = p.getProperty(classProperty);
	    if (className == null) {
		Debug.error("Failed to locate property \""
			    + classProperty + "\"");
		Debug.error("Skipping layer \"" + layerName + "\"");
		continue;
	    }

	    Object obj = ComponentFactory.create(className, layerName, p);
	    if (obj instanceof Layer || obj instanceof PlugIn) {
		Layer l = null;

		if (obj instanceof PlugIn) {
		    PlugIn pi = (PlugIn) obj;
		    PlugInLayer pil = new PlugInLayer();
		    pil.setPlugIn(pi);
		    pil.setName(p.getProperty(PropUtils.getScopedPropertyPrefix(pi) + Layer.PrettyNameProperty));
		    l = pil;
		} else {
		    l = (Layer) obj;
		}

		layers.addElement(l);

		if (instantiatedLayers != null) {
		    instantiatedLayers.put(layerName, l);
		    if (Debug.debugging("imageserver")) {
			Debug.output("ImageServer: Saving /" + layerName +
				     "/ to instantiated layers hashtable.");
		    }
		}
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

//      protected void finalize() {
//  	if (Debug.debugging("gc")) {
//  	    Debug.output("ImageServer: GC'd.");
//  	}
//      }

    ///////////////////////////////////////////////////////////////
    // Start of methods that use the ImageGenerator stuff.
    ///////////////////////////////////////////////////////////////

//      /** A queue for image requests. */
//      protected Vector requests = new Vector();
//      /** The ImageGenerator. */
//      protected ImageGenerator generator = null;
//      /** Used by the ImageServer when it acts as ImageReceiver. */
//      protected boolean imageIsReady = false;
//      /** Used by the ImageServer when it acts as ImageReceiver. */
//      protected byte[] imageDataBuf = null;
//      /** Used by the ImageServer when it acts as ImageReceiver.
//       * Dictates how long, in milliseconds the ImageServer will wait
//       * for the layers before returning an error image when
//       * getMapImage() is called.  */
//      protected long timeout = 0;

//      /**
//       * Set the timeout for the ImageServer when it is acting like an
//       * ImageReceiver, to tell it how long it should wait for the
//       * layers.  
//       * @param time max time to wait, in milliseconds (0-999999).
//       */
//      public void setTimeout(long time) {
//  	if (time < 0 || time > 999999) {
//  	    timeout = 0;
//  	} else {
//  	    timeout = time;
//  	}
//      }

//      /**
//       * Get the timeout that the ImageServer is using for it's
//       * ImageReceiver role.
//       * @return timeout, in milliseconds.  
//       */
//      public long getTimeout() {
//  	return timeout;
//      }

//      /** 
//       * The ImageServer's basic function is to take projections, and
//       * to give back an image of that projection with the current
//       * layers.  
//       *
//       * @return a long signifying a timestamp of request.
//       */
//      public long createImage(Projection proj, ImageReceiver ir) {
//  	return createImage(proj, -1, -1, ir);
//      }

//      /** 
//       * The ImageServer's basic function is to take projections, and
//       * to give back an image of that projection with the current
//       * layers. 
//       *
//       * @param proj projection of map.
//       * @param scaledWidth scaled pixel width of final image.
//       * @param scaledHeight scaled pixel height of final image.
//       * @param ir ImageReceiver to get image bytes. 
//       * @return a long signifying a timestamp of request.
//       */
//      public long createImage(Projection proj, int scaledWidth, int scaledHeight,
//  			    ImageReceiver ir) {
//  	return createImage(proj, scaledWidth, scaledHeight, ir, 0xFFFFFFFF);
//      }

//      /** 
//       * The ImageServer's basic function is to take projections, and to
//       * give back an image of that projection with the current layers.
//       * Uses an ImageGenerator to create the image.  Careful!  The
//       * timing on this may not work right under heavy loads.
//       *
//       * @param proj projection of map.
//       * @param scaledWidth scaled pixel width of final image.
//       * @param scaledHeight scaled pixel height of final image.
//       * @param ir ImageReceiver to get image bytes. 
//       * @param includedLayerMask a mask signifying which of the
//       * ImageServer layers to use in the iamge.  It's assumed that the
//       * called knows which layers are desired.  Bit 1 of the mask
//       * refers to layer[0], etc.  A bit turned on means the layer will
//       * be included.
//       * @return a long signifying a timestamp of request.  
//       */
//      public long createImage(Projection proj, int scaledWidth, int scaledHeight,
//  			    ImageReceiver ir, int includedLayerMask) {

//  	Request request = new Request(proj.makeClone(), 
//  				      scaledWidth, scaledHeight, 
//  				      ir, System.currentTimeMillis(),
//  				      includedLayerMask);
//  	addRequest(request);
//  	return request.requestID;
//      }

//      /**
//       * Call this function to get map image bytes.  The projection
//       * should describe the parameters of the map view.  Use this
//       * method if you want all the layers, and the map at the size
//       * dictated by the projection.
//       *
//       * @param proj the map projection.  
//       * @return bytes representing the formatted image, depending on
//       * the image formatter set into the ImageServer. Returns byte[0]
//       * if something goes wrong, or if the timeout was reached.
//       */
//      public byte[] getMapImage(Projection proj) {
//  	return getMapImage(proj, -1, -1, 0xFFFFFFFF);
//      }
	
//      /**
//       * Call this function to get map image bytes.  The projection
//       * should describe the parameters of the map view.  This method
//       * allows you to scale the map to a different size than the one
//       * dictated by the projection, and also use a bit mask to turn
//       * individual layers on/off.  For the bit mask, bit 0 = layer[0] =
//       * layer on top.
//       *
//       * @param proj the map projection.  
//       * @param scaledWidth pixel width of final image.
//       * @param scaledHeight pixel height of final image.
//       * @param includedLayerMask bit mask to turn specific layers
//       * on/off (1 = on).
//       * @return bytes representing the formatted image, depending on
//       * the image formatter set into the ImageServer.  Returns byte[0]
//       * if something goes wrong, or if the timeout was reached.
//       */
//      public byte[] getMapImage(Projection proj, 
//  			      int scaledWidth, int scaledHeight,
//  			      int includedLayerMask) {

//  	Debug.message("imageserver", "ImageServer.getMapImage()");
//  	createImage(proj, scaledWidth, scaledHeight, this, includedLayerMask);
	
//  	synchronized(this) {
//  	    try {
//  		if (imageIsReady) {
//  		    Debug.message("imageserver", 
//  				  "ImageServer: Image is ready, no need to wait");
//  		} else {
//  		    Debug.message("imageserver", 
//  				  "ImageServer waiting for layers...");
//  		    wait(timeout);
//  		    if (!imageIsReady) {
//  			// If the image is not ready, then the timeout
//  			// kicked it out of wait status, and we need
//  			// to proceed.
//  			Debug.message("imageserver", "ImageServer reached timeout limit waiting for layers, returning zero length image.");
//  			imageDataBuf = null;
//  		    }
//  		}
//  	    } catch (InterruptedException ie) {
//  		Debug.error("ImageServer caught interrupted exception waiting for layers");
//  	    }

//  	    // reset 'ready' flag for next time
//  	    imageIsReady = false; 
//  	}

//  	if (imageDataBuf == null) {
//  	    return new byte[0];
//  	}
	 
//  	return imageDataBuf;
//      }
    
//      /** 
//       * Add an image request to the list.
//       * @deprecated unreliable
//       */
//      protected synchronized void addRequest(Request request) {
//  	requests.add(request);

//  	// If the request is the first in the queue, go ahead and
//  	// start it.
//  	if (request == (Request) requests.get(0)) {
//  	    launchGenerator(request, getMaskedLayers(request.layerMask));
//  	}
//      }

//      /**
//       * Assign an ImageGenerator to the ImageServer generator variable.
//       *
//       * @param proj Projection for the image.
//       * @param layers the layer array to use on the image.
//       * @param request the overall request for the image.
//       * @deprecated unreliable
//       */
//      protected synchronized void launchGenerator(Request request, 
//  						Layer[] layers) {
//  	if (generator == null) {
//  	    generator =  new ImageGenerator(request.proj, layers,
//  					    request.requestID, this);
//  	} else {
//  	    generator.generateImage(request.proj, layers,
//  				    request.requestID, this);
//  	}
//      }

//      /** 
//       * Remove the request from the list, and start working on the
//       * next request, if there is one.  The ImageReceiver is given the data.
//       * @deprecated unreliable
//       */
//      protected synchronized void requestComplete(long requestID, 
//  						byte[] formattedImage) {

//  	Request request = getRequest(requestID);

//  	if (request != null) {
//  	    requests.remove(request);
//  	    request.receiver.receiveImageData(formattedImage);
//  	    request.proj = null;
//  	    request.receiver = null;
//  	}

//  	// Start the image generator working on the next one.
//  	if (requests.size() >= 1) {
//  	    request = (Request) requests.get(0);
//  	    launchGenerator(request, getMaskedLayers(request.layerMask));
//  	}
//      }

//      /**
//       * Given a request ID, find the request from the list of requests.
//       * @return null if not found.
//       * @deprecated unreliable
//       */
//      protected synchronized Request getRequest(long requestID) {
//  	Request request;
//  	int index = -1;
//  	for (int i = 0; i < requests.size(); i++) {
//  	    request = (Request)requests.get(i);
//  	    if (requestID == request.requestID) {
//  		return request;
//  	    }
//  	}
//  	return null;
//      }

//      /**  
//       * Part of the ImageReadyListener interface.  Called by the
//       * ImageGenerator when all the layers have their graphics.  We
//       * have to create an image buffer to draw into, and then format
//       * the image using the ImageFormatter.
//       * @deprecated unreliable image creation path. Use createImage() instead.
//       */
//      public void imageReady(ImageGenerator imageGenerator, long requestID) {
//  	if (Debug.debugging("imageserver")) {
//  	    Debug.output("ImageServer: handling image.");
//  	}

//  	java.awt.Graphics graphics = null;
//  	byte[] formattedImage = null;

//  	if (formatter != null) {
//  	    com.bbn.openmap.proj.Projection proj = imageGenerator.getProjection();
//  	    graphics = formatter.getGraphics(proj.getWidth(), proj.getHeight());
//  	} else {
//  	    Debug.error("ImageServer: Formatter is null, returning zero length image.");
//  	    formattedImage = new byte[0];
//  	}

//  	if (graphics == null) {
//  	    Debug.error("ImageServer: NOT able to create Graphics!");
//  	    formattedImage = new byte[0];
//  	}

//  	if (formattedImage != null) {
//  	    // No point continuing...
//  	    requestComplete(requestID, formattedImage);
//  	    return;
//  	}	    

//  	if (Debug.debugging("imageserver")) {
//  	    Debug.output("ImageServer: graphics is cool");
//  	}

//  	if (doAntiAliasing && graphics instanceof java.awt.Graphics2D) {
//  	    java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
//  	    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//  				 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//  	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//  				 RenderingHints.VALUE_ANTIALIAS_ON);
//  	}

//  	imageGenerator.paintLayers(graphics);

//  	if (Debug.debugging("imageserver")) {
//  	    Debug.output("ImageServer: ready to create formatted image.");
//  	}

//  	// Now, scale the image, if needed...
//  	Request request = getRequest(requestID);
//  	if (request != null && 
//  	    request.scaledWidth > 0 && 
//  	    request.scaledHeight > 0) {
	    
//  	    formattedImage = 
//  		formatter.getScaledImageBytes(request.scaledWidth, request.scaledHeight);

//  	} else {
//  	    if (Debug.debugging("imageserver")) {
//  		if (request != null) {
//  		    Debug.output("ImageServer: using full scale image (unscaled).");
//  		}
//  	    }
//  	    formattedImage = formatter.getImageBytes();
//  	}
	
//  	graphics.dispose();
//  	requestComplete(requestID, formattedImage);
//      }

//      /*
//       * This is the function called by the (internal) ImageServer
//       * when it has completed image-generation.
//       * @deprecated unreliable image creation path.
//       */
//      public void receiveImageData(byte[] bytes) {
//  	if (Debug.debugging("imageserver")) {
//  	    Debug.output("ImageServer.receiveImageData(), bytes.length = " +
//  			 bytes.length);
//  	}

//  	synchronized (this) {
//  	    imageDataBuf = bytes;
//  	    imageIsReady = true;
//  	    notifyAll();
//  	}
//      }

//      /**
//       * A class to track image requests.  Generated internally.
//       */
//      public class Request {
//  	/** What the map should look like for this image. */
//  	public Projection proj;
//  	/** Who should be notified with the image bytes. */
//  	public ImageReceiver receiver;
//  	/** A timestamp ID for the request. */
//  	public long requestID;
//  	/** The output width size of the image, scaled from the width
//  	 *  of the actual image created by the image generator. */
//  	public int scaledWidth;
//  	/** The output height size of the image, scaled from the width
//  	 *  of the actual image created by the image generator. */
//  	public int scaledHeight;
//  	/** Limit the layers given to the ImageGenerator. The lower
//  	 *  bits refer to upper layers.*/
//  	public int layerMask;

//  	/** 
//  	 * Create a image request, with no adjustment to the image
//  	 * size after creation. Use all the layers stored in the
//  	 * ImageServer.  
//  	 */
//  	public Request(Projection p, ImageReceiver ir, long rid) {
//  	    this(p, -1, -1, ir, rid, 0xFFFFFFFF);
//  	}

//  	/**
//  	 * Create a image request, with the size to scale the image
//  	 * after creation.  Use all the images stored in the ImageServer.
//  	 */
//  	public Request(Projection p, int sWidth, int sHeight, 
//  		       ImageReceiver ir, long rid) {
//  	    this(p, sWidth, sHeight, ir, rid, 0xFFFFFFFF);
//  	}
	
//  	/**
//  	 * Create a image request, with the size to scale the image
//  	 * after creation.  Use all the images stored in the ImageServer.
//  	 * @param p com.bbn.openmap.proj.Projection
//  	 * @param sWidth the scaled pixel width of the final image.
//  	 * @param sHeight the scaled pixel hwight of the final image.
//  	 * @param ir ImageReceiver to get the image bytes.
//  	 * @param rid the request ID unique to this request.
//  	 * @param mask the mask to use when adding layers to the
//  	 * ImageGenerator.  Bit 1 refers to layer[0], etc. A bit
//  	 * turned on means to include the layer in the image - so
//  	 * 0xFFFFFFFF means to use all of them.
//  	 */
//  	public Request(Projection p, int sWidth, int sHeight, 
//  		       ImageReceiver ir, long rid, int mask) {

//  	    scaledWidth = sWidth;
//  	    scaledHeight = sHeight;

//  	    proj = p;
//  	    receiver = ir;
//  	    requestID = rid;

//  	    layerMask = mask;
//  	}
//      }

//      ///////////////////////////////////////////////////////////////
//      // End of methods that use the ImageGenerator stuff.
//      ///////////////////////////////////////////////////////////////

    /**
     * For convenience, to create an image file based on the contents
     * of a properties file (like an openmap.properties file).
     *
     * @param prefix The prefix for the ImageServer properties (layers
     * and formatters) to use in the properties file.  If defined,
     * then this method will look for 'prefix.layers' and
     * prefix.formatters' properties. If null, then this method will
     * look 'layers' and 'formatters' properties.
     *
     * @param props The properties to use for defining the layers and
     * plugins to use on the map image.  Standard openmap.properties
     * formats for layer definitions.  See the standard
     * openmap.properties file for more details on how to define
     * layers and plugins.
     *
     * @param proj The projection to use for the map.  If null, then
     * the Environment projection properties will be looked for in the
     * Properties.
     *
     * @param outputPath The output path for the image file.  The
     * image file should not have an appendix defined.  This method
     * will check which formatter is being used, and will assign one
     * based on the image format (leave off the ., too).
     *
     * @return the final path of the written image file, with the
     * chosen appendix attached.
     */
    public static String createImageFile(String prefix, 
					 Properties props, 
					 Projection proj,
					 String outputPath) 

	throws MalformedURLException, IOException {

	String appendix = "";
	
	ImageServer is = new ImageServer(props);

	ImageFormatter formatter = is.getFormatter();
	if (formatter == null) {
	    is.setFormatter(new SunJPEGFormatter());
	    appendix = ".jpg";
	} else {
	    String fileType = formatter.getFormatLabel();
	    if (fileType.equals(WMTConstants.IMAGEFORMAT_JPEG)) {
		appendix = ".jpg";
	    } else {
		appendix = "." + fileType.toLowerCase();
	    }
	}
	
	// Initialize the map projection, scale, center with
	// user prefs or defaults
	if (proj == null) {
	    String projName = props.getProperty(Environment.Projection);
	    if (projName == null) {
		projName = Mercator.MercatorName;
	    }
	    int projType = ProjectionFactory.getProjType(projName);
	    
	    proj = ProjectionFactory.makeProjection(
		projType,
		LayerUtils.floatFromProperties(props, Environment.Latitude, 0f),
		LayerUtils.floatFromProperties(props, Environment.Longitude, 0f),
		LayerUtils.floatFromProperties(props, Environment.Scale,
					       MapBean.DEFAULT_SCALE),
		LayerUtils.intFromProperties(props, Environment.Width,
					     MapBean.DEFAULT_WIDTH),
		LayerUtils.intFromProperties(props, Environment.Height,
					     MapBean.DEFAULT_HEIGHT));
	    ((Proj)proj).setBackgroundColor(
		(Color)LayerUtils.parseColorFromProperties(
		    props, 
		    Environment.BackgroundColor, 
		    ((Proj)proj).getBackgroundColor()));
	    
	}
	
	if (Debug.debugging("imageserver")) {
	    Debug.output("ImageServer: creating image with projection " + 
			 proj);
	}
	
	byte[] imageBytes = is.createImage(proj);
	String finalOutputPath = outputPath + appendix;
	FileOutputStream fos = new FileOutputStream(finalOutputPath);

	fos.write(imageBytes);
	fos.flush();
	fos.close();

	return finalOutputPath;
    }

    /**
     * The ImageServer class main function will create a map image
     * from a modified openmap.properties file.
     *
     * <pre>java com.bbn.openmap.image.ImageServer -properties (path
     * to properties file) -file (path to output image) </pre> 
     *
     * <P> The path to the output image should not have an appendix on
     * it, that will get assigned depending on what image format is
     * used.  
     */
    public final static void main(String[] argv) {

	Debug.init();
	Debug.put("imageserver");
	Debug.put("image");

	com.bbn.openmap.util.ArgParser ap = new 
	    com.bbn.openmap.util.ArgParser("ImageServer");

	ap.add("properties", "The properties file to use for the image.", 1);
	ap.add("file", "The output image file, without appendix (default is 'image').", 1);

	if (!ap.parse(argv)) {
	    ap.printUsage();
	    System.exit(0);
	}

	String imagefile = "image";
	String arg[];

	arg = ap.getArgValues("file");
	if (arg != null) {
	    imagefile = arg[0];
	}

	Properties props = null;
	arg = ap.getArgValues("properties");
	if (arg != null) {
	    String ps = arg[0];
	    try {

		URL url = LayerUtils.getResourceOrFileOrURL(null, ps);
		InputStream inputStream = url.openStream();

		props = new Properties();
		props.load(inputStream);

		Projection proj = null;

		String finalOutputPath = ImageServer.createImageFile(null, props, proj, imagefile);

		if (Debug.debugging("imageserver")) {
		    Debug.output("Writing image file to: " + finalOutputPath);
		}


	    } catch (MalformedURLException murle) {
		Debug.error("ImageServer can't find properties file: " +
			    arg[0]);
	    } catch (IOException ioe) {
		Debug.error("ImageServer can't write output image: IOException");
	    }
	}

	System.exit(0);
    }
}


