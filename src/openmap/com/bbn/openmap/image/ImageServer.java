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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageServer.java,v $
// $RCSfile: ImageServer.java,v $
// $Revision: 1.14 $
// $Date: 2007/04/17 20:23:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.Environment;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.plugin.PlugIn;
import com.bbn.openmap.plugin.PlugInLayer;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The image server is the class you want to deal with when creating images. It
 * takes a properties file and creates images based on those properties. It also
 * has this queuing thing going so that requests can stack up while the image
 * generator is working on requests, and it will notify the requestor when the
 * image is ready.
 * <P>
 * 
 * The ImageServer generally has the layers on the map predefined at
 * construction, although you can change the layers that it has. When setting
 * the layer array, do not use the same layer in two different slots of the
 * array - it may not give you the expected map, and may mess around with the
 * timing issues that the ImageGenerator takes care of. If you want to reorder
 * the layers, do so before adding your request to the ImageServer.
 * Additionally, each request has the option of not using certain layers in the
 * ImageServer layer array, by turning off the appropriate bits in the layer
 * mask. Understand that the image for a request will be created based on the
 * layer array contents and the request layer mask at the time the request
 * processing is started, not when it is submitted.
 * <P>
 * Right now, the ImageServer is single threaded - processing requests one after
 * another. The request setup was written to support multi-threaded processing,
 * though, where each image could be generated in it's own thread. That code is
 * not written - maybe someday.
 * <P>
 * <code><pre>
 *                 
 *                  
 *                   
 *                    # If the ImageServer is created and given a prefix (in this example,
 *                    # 'imageServer') the properties file should contain the properties:
 *                    imageServer.layers=&lt;layer1 layer2 ...&gt;
 *                    layer1.className=&lt;classname&gt;
 *                    layer1.prettyName=&lt;pretty name of layer&gt;
 *                    # Add other attributes as required by layer1...
 *                    layer2.className=&lt;classname&gt;
 *                    layer2.prettyName=&lt;pretty name of layer&gt;
 *                    # Add other attributes as required by layer2...
 *                    # First formatter listed is default.
 *                    imageServer.formatters=&lt;formatter1 formatter2 ...&gt;
 *                    formatter1.class=&lt;classname of formatter 1&gt;
 *                    # Add other formatter1 properties
 *                    formatter2.class=&lt;classname of formatter 2&gt;
 *                    
 *                   
 *                  
 * </pre></code>
 * <P>
 * NOTE: If you simply hand the ImageServer a standard openmap.properties file,
 * it works with the addition of the first two attributes except WITHOUT the
 * 'imageServer.' prefix.
 * 
 * New for 4.5: If the layers property is not defined, then the openmap.layers
 * property is used to define which layers are available for the ImageServer.
 */
public class ImageServer
        implements
        /* ImageReadyListener, ImageReceiver, */PropertyConsumer {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.image.ImageServer");

    /** The Image formatter for the output image. */
    protected ImageFormatter formatter;

    /**
     * Hashtable of ImageFormatters available to be used.
     */
    protected Map<String, ImageFormatter> imageFormatters;

    /** The array of layers on the map. First is on top. */
    protected Layer[] layers;

    /** Property for space separated layers. */
    public static final String ImageServerLayersProperty = "layers";

    /** OpenMap prefix */
    public static final String OpenMapPrefix = "openmap.";

    /**
     * Property for the image formatter list of available formats. This property
     * should contain a space separated list of marker names.
     */
    public static final String ImageFormattersProperty = "formatters";

    /** Property to turn on anti-aliasing. */
    public static final String AntiAliasingProperty = "antialiasing";

    /**
     * Property to set the background color.
     */
    public static final String BackgroundProperty = "background";

    /** Flag to do graphics and text anti-aliasing on the map image. */
    protected boolean doAntiAliasing = false;

    /**
     * A place to hold on to a property prefix in case one is used. Useful for
     * ImageServer properties files where more than one image server is defined.
     */
    protected String propertiesPrefix = null;

    /**
     * The ProjectionFactory to be used for image projections. If null, the
     * default projection set will be used.
     */
    protected ProjectionFactory projectionFactory;

    private boolean transparent = true;

    /**
     * Empty constructor that expects to be configured later.
     */
    protected ImageServer() {
    }

    /**
     * To create the image server, you hand it a set of properties that let it
     * create an array of layers, and also to set the properties for those
     * layers. The properties file for the ImageServer looks strikingly similar
     * to the openmap.properties file. So, all the layers get set up here...
     */
    public ImageServer(Properties props) {
        setProperties(props);
    }

    /**
     * Same as the other constructor, except that the properties can have a
     * prefix in front of them. The format of the prefix has to match how the
     * property is specified the the properties file, which may include the
     * period - i.e server1.imageServer.layers, the server1. is the prefix that
     * should get passed in. The ImageMaster does this.
     */
    public ImageServer(String prefix, Properties props) {
        this(prefix, props, null);
    }

    /**
     * Create an ImageServer that should be configured with a Properties file.
     * The prefix given is to scope the ImageServer properties to this instance.
     * The Hashtable is for reusing any layers that may already be instantiated.
     */
    public ImageServer(String prefix, Properties props, Map<String, Layer> instantiatedLayers) {
        setProperties(prefix, props, instantiatedLayers);
    }

    /**
     * Create an ImageServer from an array of Layers and an ImageFormatter. It's
     * assumed that the layers are already configured.
     * 
     * @param layers the array of layers.
     * @param formatter the ImageFormatter to use for the output image format.
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
     * Set the layers used on the NEXT request that is processed. Will not
     * affect any image currently being created.
     * 
     * @param newLayers an array of com.bbn.openmap.Layer objects, already
     *        configured and ready to respond to a projectionChanged method
     *        call.
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
     * 
     * @return Layer[]
     */
    public synchronized Layer[] getLayers() {
        return layers;
    }

    /**
     * Use the ProjectionPainter interface of the layers to create an image.
     * This approach avoids some of the timing issues that the thread model of
     * the MapBean and Layers that seem to pop up from time to time. They are
     * Swing components, you know. They were designed to be part of a GUI. So,
     * this is a serialized, safe way to do things.
     * 
     * @param proj projection of map.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj) {
        return createImage(proj, -1, -1, 0xFFFFFFFF);
    }

    /**
     * Use the ProjectionPainter interface of the layers to create an image.
     * This approach avoids some of the timing issues that the thread model of
     * the MapBean and Layers that seem to pop up from time to time. They are
     * Swing components, you know. They were designed to be part of a GUI. So,
     * this is a serialized, safe way to do things.
     * 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image. If you don't want
     *        it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image. If you don't want
     *        it scaled, use -1.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj, int scaledWidth, int scaledHeight) {
        return createImage(proj, scaledWidth, scaledHeight, 0xFFFFFFFF);
    }

    /**
     * Use the ProjectionPainter interface of the layers to create an image.
     * This approach avoids some of the timing issues that the thread model of
     * the MapBean and Layers that seem to pop up from time to time. They are
     * Swing components, you know. They were designed to be part of a GUI. So,
     * this is a serialized, safe way to do things. The background used for the
     * image is the one set in this ImageServer object.
     * 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image. If you don't want
     *        it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image. If you don't want
     *        it scaled, use -1.
     * @param showLayers Layer marker names reflecting the layers that should be
     *        part of this image.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj, int scaledWidth, int scaledHeight, List<String> showLayers) {
        return createImage(proj, scaledWidth, scaledHeight, showLayers, getBackground());
    }

    /**
     * Use the ProjectionPainter interface of the layers to create an image.
     * This approach avoids some of the timing issues that the thread model of
     * the MapBean and Layers that seem to pop up from time to time. They are
     * Swing components, you know. They were designed to be part of a GUI. So,
     * this is a serialized, safe way to do things. The background used for the
     * image is the one set in this ImageServer object.
     * 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image. If you don't want
     *        it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image. If you don't want
     *        it scaled, use -1.
     * @param showLayers Layersthat should be part of this image.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImageFromLayers(Projection proj, int scaledWidth, int scaledHeight, List<Layer> showLayers) {
        return createImageFromLayers(proj, scaledWidth, scaledHeight, showLayers, getBackground());
    }

    /**
     * Use the ProjectionPainter interface of the layers to create an image.
     * This approach avoids some of the timing issues that the thread model of
     * the MapBean and Layers that seem to pop up from time to time. They are
     * Swing components, you know. They were designed to be part of a GUI. So,
     * this is a serialized, safe way to do things. The background used for the
     * image is the one set in this ImageServer object.
     * 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image. If you don't want
     *        it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image. If you don't want
     *        it scaled, use -1.
     * @param showLayers Layer marker names reflecting the layers that should be
     *        part of this image.
     * @param background the Paint to be used for the background of this image.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj, int scaledWidth, int scaledHeight, List<String> showLayers, Paint background) {

        logger.fine("using the new ProjectionPainter interface!  createImage with layer string array.");

        if (formatter == null) {
            logger.warning("no formatter set! Can't create image.");
            return new byte[0];
        }

        ImageFormatter imageFormatter = formatter.makeClone();
        java.awt.Graphics graphics = createGraphics(imageFormatter, proj.getWidth(), proj.getHeight());

        if (graphics == null) {
            return new byte[0];
        }

        ((Proj) proj).drawBackground((Graphics2D) graphics, background);

        if (showLayers != null) {
            int size = showLayers.size();
            for (int j = size - 1; j >= 0; j--) {
                for (int i = layers.length - 1; i >= 0; i--) {
                    String layerName = (String) showLayers.get(j);
                    Layer layer = layers[i];
                    String prefix = layer.getPropertyPrefix();
                    if (prefix == null) {
                        // Just in case the PlugInLayer prefix didn't get set to
                        // the
                        // same as the plugins'
                        if (layer instanceof PlugInLayer) {
                            prefix = ((PlugInLayer) layer).getPlugIn().getPropertyPrefix();
                        }
                    }

                    if (layerName.equals(prefix)) {
                        layer.renderDataForProjection(proj, graphics);
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("image request adding layer graphics from : " + layer.getName());
                        }
                    }
                }
            }
        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine("no layers available for image");
        }

        byte[] formattedImage = getFormattedImage(imageFormatter, scaledWidth, scaledHeight);
        graphics.dispose();
        return formattedImage;
    }

    /**
     * Create an image from a set of layers.
     * 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image. If you don't want
     *        it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image. If you don't want
     *        it scaled, use -1.
     * @param layers A set of layers to paint into the image.
     * @param background the Paint to be used for the background of this image.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImageFromLayers(Projection proj, int scaledWidth, int scaledHeight, List<Layer> layers, Paint background) {

        logger.fine("using the new ProjectionPainter interface!  createImage with layer list.");

        if (formatter == null) {
            logger.warning("no formatter set! Can't create image.");
            return new byte[0];
        }

        ImageFormatter imageFormatter = formatter.makeClone();
        java.awt.Graphics graphics = createGraphics(imageFormatter, proj.getWidth(), proj.getHeight());

        if (graphics == null) {
            return new byte[0];
        }

        ((Proj) proj).drawBackground((Graphics2D) graphics, background);

        if (layers != null && !layers.isEmpty()) {
            for (int i = layers.size() - 1; i >= 0; i--) {
                Layer layer = layers.get(i);

                if (layer != null) {
                    layer.renderDataForProjection(proj, graphics);

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("image request adding layer graphics from : " + layer.getName());
                    }
                }
            }

        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine("no layers available for image");
        }

        byte[] formattedImage = getFormattedImage(imageFormatter, scaledWidth, scaledHeight);
        graphics.dispose();
        return formattedImage;
    }

    /**
     * This method returns a integer representing a mask created from the
     * visibility settings of the layers.
     */
    public int calculateVisibleLayerMask() {
        int ret = 0; // Initialize all the layer bits to zero.
        for (int i = layers.length - 1; i >= 0; i--) {
            if (layers[i].isVisible()) {
                ret |= (0x00000001 << i);
            }
        }
        return ret;
    }

    /**
     * Use the ProjectionPainter interface of the layers to create an image.
     * This approach avoids some of the timing issues that the thread model of
     * the MapBean and Layers that seem to pop up from time to time. They are
     * Swing components, you know. They were designed to be part of a GUI. So,
     * this is a serialized, safe way to do things. Uses the default background
     * set in the ImageServer.
     * 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image. If you don't want
     *        it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image. If you don't want
     *        it scaled, use -1.
     * @param includedLayerMask a mask signifying which of the ImageServer
     *        layers to use in the image. It's assumed that the called knows
     *        which layers are desired. Bit 1 of the mask refers to layer[0],
     *        etc. A bit turned on means the layer will be included.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj, int scaledWidth, int scaledHeight, int includedLayerMask) {
        return createImage(proj, scaledWidth, scaledHeight, includedLayerMask, getBackground());
    }

    /**
     * Use the ProjectionPainter interface of the layers to create an image.
     * This approach avoids some of the timing issues that the thread model of
     * the MapBean and Layers that seem to pop up from time to time. They are
     * Swing components, you know. They were designed to be part of a GUI. So,
     * this is a serialized, safe way to do things.
     * 
     * @param proj projection of map.
     * @param scaledWidth scaled pixel width of final image. If you don't want
     *        it scaled, use -1.
     * @param scaledHeight scaled pixel height of final image. If you don't want
     *        it scaled, use -1.
     * @param includedLayerMask a mask signifying which of the ImageServer
     *        layers to use in the image. It's assumed that the called knows
     *        which layers are desired. Bit 1 of the mask refers to layer[0],
     *        etc. A bit turned on means the layer will be included.
     * @param background the background Paint to use for the image, behind the
     *        layers.
     * @return a byte[] representing the formatted image.
     */
    public byte[] createImage(Projection proj, int scaledWidth, int scaledHeight, int includedLayerMask, Paint background) {

        logger.fine("using the new ProjectionPainter interface!  createImage with layer mask.");

        if (formatter == null) {
            logger.warning("no formatter set! Can't create image.");
            return new byte[0];
        }

        ImageFormatter imageFormatter = formatter.makeClone();

        Graphics graphics = createGraphics(imageFormatter, proj.getWidth(), proj.getHeight());

        if (graphics == null) {
            return new byte[0];
        }

        ((Proj) proj).drawBackground((Graphics2D) graphics, background);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("considering " + layers.length + " for image...");
        }

        if (layers != null) {
            for (int i = layers.length - 1; i >= 0; i--) {
                if ((includedLayerMask & (0x00000001 << i)) != 0) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("image request adding layer graphics from : " + layers[i].getName());
                    }

                    layers[i].renderDataForProjection(proj, graphics);
                } else {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("skipping layer graphics from : " + layers[i].getName());
                    }
                }
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("no layers available");
            }
        }

        byte[] formattedImage = getFormattedImage(imageFormatter, scaledWidth, scaledHeight);
        graphics.dispose();
        return formattedImage;
    }

    /**
     * Create a java.awt.Graphics to use for an image. The Graphics will affect
     * the image contained within the ImageFormatter.
     * 
     * @param formatter the ImageFormatter containing the image.
     * @param width the pixel width of the image.
     * @param height the pixel height of the image.
     */
    protected Graphics createGraphics(ImageFormatter formatter, int width, int height) {

        java.awt.Graphics graphics = null;

        if (formatter == null) {
            logger.warning("ImageServer.createGraphics: Formatter is null, returning null graphics.");
            return null;
        }

        graphics = formatter.getGraphics(width, height, getTransparent());

        if (graphics == null) {
            logger.warning("ImageServer.createGraphics: NOT able to create Graphics!");
            return null;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("graphics is cool");
        }

        if (doAntiAliasing && graphics instanceof java.awt.Graphics2D) {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        return graphics;
    }

    /**
     * Format the image that is contained in the ImageFormatter, scaling to a
     * particular size if the scaledWidth and scaledHeight are greater than 0.
     */
    protected byte[] getFormattedImage(ImageFormatter formatter, int scaledWidth, int scaledHeight) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ready to create formatted image.");
        }
        byte[] formattedImage = null;

        // Now, scale the image, if needed...
        if (scaledWidth > 0 && scaledHeight > 0) {

            formattedImage = formatter.getScaledImageBytes(scaledWidth, scaledHeight);

        } else {
            logger.fine("ImageServer: using full scale image (unscaled).");
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
     * Set the layers and image type in the properties. The properties might
     * have a prefix in the file.
     */
    public void setProperties(String prefix, Properties props) {
        setProperties(prefix, props, (Map<String, Layer>) null);
    }

    /**
     * Set the layers and image type in the properties. The properties might
     * have a prefix in the file.
     */
    public void setProperties(String prefix, Properties props, Map<String, Layer> instantiatedLayers) {
        setPropertyPrefix(prefix);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        layers = getLayers(props, instantiatedLayers);
        formatter = getFormatters(props);
        doAntiAliasing = PropUtils.booleanFromProperties(props, prefix + AntiAliasingProperty, false);

        background = getBackground(props, prefix + BackgroundProperty);
    }

    /**
     * Determine the background color based on property settings. If the
     * property key isn't found, the openmap.BackgroundColor property will be
     * used. If that isn't found, then Color.white will be returned as default.
     * 
     * @param props properties to check
     * @param propertyKey first key to check for.
     * @return Paint used for background, as stated in properties
     */
    public Paint getBackground(Properties props, String propertyKey) {
        String paintString = props.getProperty(propertyKey);
        if (paintString == null) {
            paintString = props.getProperty(Environment.BackgroundColor);
        }

        Paint ret = null;
        if (paintString != null) {
            try {
                ret = PropUtils.parseColor(paintString, transparent);
            } catch (NumberFormatException nfe) {
                // Color set to white below...
            }
        }

        if (ret == null) {
            ret = Color.white;
        }

        return ret;
    }

    /**
     * Part of the PropertyConsumer interface.
     */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        StringBuffer buf = new StringBuffer();
        for (Layer layer : getLayers()) {
            buf.append(layer.getPropertyPrefix()).append(" ");
            layer.getProperties(props);
        }

        props.put(prefix + ImageServerLayersProperty, buf.toString().trim());

        buf = new StringBuffer();
        if (imageFormatters != null) {
            int index = 1;
            for (ImageFormatter formatter : imageFormatters.values()) {

                String className = formatter.getClass().getName();
                String prfx = className.substring(className.lastIndexOf('.') + 1) + index;

                if (formatter instanceof PropertyConsumer) {
                    PropertyConsumer pc = (PropertyConsumer) formatter;
                    String opp = pc.getPropertyPrefix();

                    if (opp != null) {
                        prfx = opp;
                        pc.getProperties(props);
                    } else {
                        pc.setPropertyPrefix(prfx);
                        pc.getProperties(props);
                        // Reset it to whatever it was before we grabbed
                        // properties.
                        pc.setPropertyPrefix(null);
                    }
                }

                buf.append(prfx).append(" ");
                props.put(prfx + ComponentFactory.DotClassNameProperty, formatter.getClass().getName());

                index++;
            }
        }

        props.put(prefix + ImageFormattersProperty, buf.toString().trim());

        props.put(prefix + AntiAliasingProperty, Boolean.toString(doAntiAliasing));

        if (background instanceof Color) {
            String colorString = Integer.toHexString(((Color) background).getRGB());
            props.put(Environment.BackgroundColor, colorString);
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
     * Part of the PropertyConsumer interface. Set the Properties prefix to use
     * to scope the relevant properties passed into the setProperties method.
     */
    public void setPropertyPrefix(String prefix) {
        propertiesPrefix = prefix;
    }

    /**
     * Part of the PropertyConsumer interface. Get the Properties prefix used to
     * scope the relevant properties passed into the setProperties method.
     */
    public String getPropertyPrefix() {
        return propertiesPrefix;
    }

    /**
     * Given a integer that represents, bitwise, the layers that you want out of
     * the current list held by the ImageServer layer array, return an array of
     * those layers.
     * 
     * @param layerMask bit mask for desired layers, bit 0 is layer 0.
     * @return layer[]
     */
    protected synchronized Layer[] getMaskedLayers(int layerMask) {
        if (layerMask == 0xFFFFFFFF || layers == null) {
            // They all want to be there
            if (logger.isLoggable(Level.FINE)) {
                logger.fine((layers != null ? "ImageServer: image request adding all layers."
                        : "ImageServer.getMaskedLayers() null layers"));
            }
            return layers;
        } else {
            // Use the vector as a growable array, and add the layers
            // to it that the mask says should be there.
            Vector<Layer> layerVector = new Vector<Layer>(layers.length);
            for (int i = 0; i < layers.length; i++) {
                if ((layerMask & (0x00000001 << i)) != 0) {
                    layerVector.add(layers[i]);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("image request adding layer: " + layers[i].getName());
                    }
                }
            }
            Layer[] imageLayers = new Layer[layerVector.size()];
            return (Layer[]) layerVector.toArray(imageLayers);
        }
    }

    /**
     * Get the ImageFormatter currently used for the image creation.
     * 
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

        if (imageFormatters == null) {
            imageFormatters = new Hashtable<String, ImageFormatter>();
        }

        if (!imageFormatters.containsKey(formatter.getFormatLabel())) {
            imageFormatters.put(formatter.getFormatLabel(), formatter);
        }
    }

    /**
     * Set the default formatter to the one with the given label. The label can
     * be retrieved from the ImageFormatter.
     * 
     * @param formatterLabel String for a particular formatter.
     * @return true if label matches up with a known formatter, false if no
     *         formatter found.
     */
    public synchronized boolean setFormatter(String formatterLabel) {
        ImageFormatter tmpFormatter = (ImageFormatter) imageFormatters.get(formatterLabel.intern());

        if (tmpFormatter != null) {
            setFormatter(tmpFormatter);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the Hashtable used to hold the ImageFormatters. The label for each
     * one is the lookup for it in the Hashtable.
     * 
     * @return Hashtable of ImageFormatters.
     */
    public synchronized Map<String, ImageFormatter> getFormatters() {
        return imageFormatters;
    }

    /**
     * Set the ImageFormatter Hashtable to set up the possible choices for image
     * formats.
     * 
     * @param iFormatters Hashtable of ImageFormatters
     * @param defaultFormatterKey the key label of the formatter to use for a
     *        default.
     */
    public synchronized void setFormatters(Map<String, ImageFormatter> iFormatters, String defaultFormatterKey) {
        imageFormatters = iFormatters;
        formatter = (ImageFormatter) imageFormatters.get(defaultFormatterKey.intern());
    }

    /**
     * Create an ImageFormatter from the contents of a properties object.
     * 
     * @param p Properties used to initialize the Properties.
     * @return default formatter.
     */
    protected synchronized ImageFormatter getFormatters(Properties p) {
        String formattersString;
        ImageFormatter iFormatter = null;

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        formattersString = p.getProperty(prefix + ImageFormattersProperty);

        // First, look at the formatters string to get a marker list
        // of available formatters.
        if (formattersString != null) {
            Vector<String> markerNames = PropUtils.parseSpacedMarkers(formattersString);
            Vector<?> formatters = ComponentFactory.create(markerNames, p);

            int size = formatters.size();

            if (imageFormatters == null) {
                imageFormatters = new Hashtable<String, ImageFormatter>(size);
            }

            for (int i = 0; i < size; i++) {
                ImageFormatter formatter = (ImageFormatter) formatters.get(i);
                imageFormatters.put(formatter.getFormatLabel(), formatter);

                if (i == 0) {
                    iFormatter = formatter;
                }
            }

        } else {
            logger.fine("no formatters specified");
        }

        return iFormatter;
    }

    /**
     * Create an array of Layers from a properties object.
     */
    protected Layer[] getLayers(Properties p) {
        return getLayers(p, (Map<String, Layer>) null);
    }

    /**
     * Create an array of Layers from a properties object. Reuse the layer from
     * the hashtable if it's there under the same property name. The Hashtable
     * is kept for an ImageServer that is used buy an ImageMaster or another
     * object that is using different layers for it's image. It will reuse the
     * layers it's already created if the marker names are the same.
     * 
     * @param p properties
     * @param instantiatedLayers a hashtable containing layers, with the prefix
     *        layer name used as the key.
     */
    protected Layer[] getLayers(Properties p, Map<String, Layer> instantiatedLayers) {

        String layersValue;
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        layersValue = p.getProperty(prefix + ImageServerLayersProperty);

        if (layersValue == null) {
            // get openmap.layers value
            layersValue = p.getProperty(OpenMapPrefix + ImageServerLayersProperty);

            if (layersValue == null) {
                logger.warning("No property \"" + ImageServerLayersProperty + "\" found in ImageServer properties.");
                return new Layer[0];
            }
        }

        Vector<String> layerNames = PropUtils.parseSpacedMarkers(layersValue);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("OpenMap.getLayers(): " + layerNames);
        }

        int nLayerNames = layerNames.size();
        Vector<Layer> layers = new Vector<Layer>(nLayerNames);

        for (int i = 0; i < nLayerNames; i++) {
            String layerName = (String) layerNames.elementAt(i);

            // Check to see if some other ImageServer has used this
            // layer, and reuse it.
            if (instantiatedLayers != null) {
                Layer iLayer = (Layer) instantiatedLayers.get(layerName);
                if (iLayer != null) {

                    // We might want to consider adding this:
                    // iLayer.setProperties(layerName, p);

                    layers.add(iLayer);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("adding instantiated layer /" + layerName + "/");
                    }
                    continue;
                }
            }

            // Brand new layer, so instantiate it.
            String classProperty = layerName + ".class";
            String className = p.getProperty(classProperty);
            if (className == null) {
                logger.warning("Failed to locate property \"" + classProperty + "\"");
                logger.warning("Skipping layer \"" + layerName + "\"");
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
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Saving /" + layerName + "/ to instantiated layers hashtable.");
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

    public ProjectionFactory getProjectionFactory() {
        if (projectionFactory == null) {
            projectionFactory = ProjectionFactory.loadDefaultProjections();
        }
        return projectionFactory;
    }

    public void setProjectionFactory(ProjectionFactory projFactory) {
        projectionFactory = projFactory;
    }

    /**
     * Takes a byte array and writes it out to a file path.
     * 
     * @param imageBytes the formatted bytes of the image.
     * @param outputPath the path of the image file.
     * @param checkFormatterForExtension if true, will check the current active
     *        formatter for extension that will be added to the path if it
     *        doesn't end with the image type.
     * @return the final file path used, with any extensions added.
     * @throws IOException
     */
    public String writeImageFile(byte[] imageBytes, String outputPath, boolean checkFormatterForExtension)
            throws IOException {
        String appendix = "";

        if (checkFormatterForExtension) {
            ImageFormatter formatter = getFormatter();
            if (formatter == null) {
                appendix = ".jpg";
            } else {
                String fileType = formatter.getFormatLabel();
                if (fileType.equals(WMTConstants.IMAGEFORMAT_JPEG)) {
                    appendix = ".jpg";
                } else {
                    appendix = "." + fileType.toLowerCase();
                }
            }

            // If the file output path already ends properly, don't bother
            // changing
            // it.
            if (outputPath.endsWith(appendix)) {
                appendix = "";
            }
        }

        String finalOutputPath = outputPath + appendix;

        FileOutputStream fos = new FileOutputStream(finalOutputPath);

        fos.write(imageBytes);
        fos.flush();
        fos.close();

        return finalOutputPath;
    }

    /**
     * For convenience, to create an image file based on the contents of a
     * properties file (like an openmap.properties file).
     * 
     * @param prefix The prefix for the ImageServer properties (layers and
     *        formatters) to use in the properties file. If defined, then this
     *        method will look for 'prefix.layers' and prefix.formatters'
     *        properties. If null, then this method will look 'layers' and
     *        'formatters' properties.
     * 
     * @param props The properties to use for defining the layers and plugins to
     *        use on the map image. Standard openmap.properties formats for
     *        layer definitions. See the standard openmap.properties file for
     *        more details on how to define layers and plugins.
     * 
     * @param proj The projection to use for the map. If null, then the
     *        Environment projection properties will be looked for in the
     *        Properties.
     * 
     * @param outputPath The output path for the image file. The image file
     *        should not have an appendix defined. This method will check which
     *        formatter is being used, and will assign one based on the image
     *        format (leave off the ., too).
     * 
     * @return the final path of the written image file, with the chosen
     *         appendix attached.
     */
    public static String createImageFile(String prefix, Properties props, Projection proj, String outputPath)

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

        Color background = MapBean.DEFAULT_BACKGROUND_COLOR;
        background = (Color) PropUtils.parseColorFromProperties(props, Environment.BackgroundColor, background);

        is.setBackground(background);

        // Initialize the map projection, scale, center with
        // user prefs or defaults
        if (proj == null) {
            ProjectionFactory projFactory = is.getProjectionFactory();
            String projName = props.getProperty(Environment.Projection);
            Class<? extends Projection> projClass = projFactory.getProjClassForName(projName);

            if (projClass == null) {
                projClass = Mercator.class;
            }

            Point2D center = null;

            if (GeoProj.class.isAssignableFrom(projClass)) {
                center =
                        new LatLonPoint.Float(PropUtils.floatFromProperties(props, Environment.Latitude, 0f),
                                              PropUtils.floatFromProperties(props, Environment.Longitude, 0f));
            } else {
                center =
                        new Point2D.Float(PropUtils.floatFromProperties(props, Environment.Latitude, 0f),
                                          PropUtils.floatFromProperties(props, Environment.Longitude, 0f));
            }

            proj =
                    projFactory.makeProjection(projClass, center,
                                               PropUtils.floatFromProperties(props, Environment.Scale, MapBean.DEFAULT_SCALE),
                                               PropUtils.intFromProperties(props, Environment.Width, MapBean.DEFAULT_WIDTH),
                                               PropUtils.intFromProperties(props, Environment.Height, MapBean.DEFAULT_HEIGHT));
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("creating image with projection " + proj);
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
     * Paint object used for map backgrounds.
     */
    protected Paint background;

    /**
     * Set the Paint to use for image backgrounds.
     */
    public void setBackground(Paint bg) {
        background = bg;
    }

    /**
     * Get the Paint to use for image backgrounds.
     */
    public Paint getBackground() {
        return background;
    }

    /**
     * Set the transparent flag. Even if this flag is true, the image still may
     * not end up transparent if the {@link ImageFormatter} does not support
     * transparency or the image is completely filled.
     * 
     * @param transparent
     */
    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    /**
     * Get the transparent flag. Even if this flag is true, the image still may
     * not end up transparent if the {@link ImageFormatter} does not support
     * transparency or the image is completely filled.
     * 
     * @return true if set for transparency
     */
    public boolean getTransparent() {
        return transparent;
    }

    /**
     * The ImageServer class main function will create a map image from a
     * modified openmap.properties file.
     * 
     * <pre>
     * java com.bbn.openmap.image.ImageServer -properties (path to properties file) -file (path to output image)
     * </pre>
     * 
     * <P>
     * The path to the output image should not have an appendix on it, that will
     * get assigned depending on what image format is used.
     */
    public static void main(String[] argv) {

        com.bbn.openmap.util.ArgParser ap = new com.bbn.openmap.util.ArgParser("ImageServer");

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

                URL url = PropUtils.getResourceOrFileOrURL(null, ps);
                InputStream inputStream = url.openStream();

                props = new Properties();
                props.load(inputStream);

                Projection proj = null;

                String finalOutputPath = ImageServer.createImageFile(null, props, proj, imagefile);

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Writing image file to: " + finalOutputPath);
                }

            } catch (MalformedURLException murle) {
                logger.warning("ImageServer can't find properties file: " + arg[0]);
            } catch (IOException ioe) {
                logger.warning("ImageServer can't write output image: IOException");
            }
        }

        System.exit(0);
    }
}
