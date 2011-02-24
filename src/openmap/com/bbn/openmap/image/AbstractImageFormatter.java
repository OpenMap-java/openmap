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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/AbstractImageFormatter.java,v $
// $RCSfile: AbstractImageFormatter.java,v $
// $Revision: 1.11 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The abstract implementation of the ImageFormatter. The ImageFormatter deals
 * with most of the image meanderings of Java, while letting you create an image
 * in a specific format. The ImageFormatter's responsibility has grown slightly,
 * since it now contains the BufferedImage that it will be formatting. Thisis to
 * make things go smoother for different uses of the formatter - some image
 * formats, for instance, really need to utilize a special implementation of a
 * Graphics in order to create the data file they want. The new definition
 * allows for that. Generally, however, you'll want to either hand the MapBean
 * to the formatter to get the image bytes, or, as in the case of the
 * ImageServer, get a Graphics from the formatter, paint the map into it, then
 * retrieve the image bytes after that.
 */
public abstract class AbstractImageFormatter
        implements ImageFormatter, PropertyConsumer, PropertyChangeListener {

    protected BufferedImage bufferedImage;
    protected String propertiesPrefix;

    public AbstractImageFormatter() {
    }

    /** Set the properties of the image formatter. */
    public void setProperties(String prefix, Properties props) {
    }

    /**
     * Convert a BufferedImage to a image file format...
     * 
     * @param bi a BufferedImage..
     */
    public abstract byte[] formatImage(BufferedImage bi);

    /**
     * Create a new instance of the same type of formatter. If you are running
     * in a multi-threaded environment, you'll need to provide a new instance of
     * the formatter to each thread, since the image and graphics that are being
     * drawn into for each thread are contained within.
     * 
     * @return a new instance of this type of formatter, with the same
     *         properties set.
     */
    public abstract ImageFormatter makeClone();

    /**
     * Return true if the image format support fully transparent pixels. The
     * returned value represent the capability of the image format, not the
     * current color model.
     * 
     * @return true of transparent pixels supported
     */
    protected abstract boolean imageFormatSupportTransparentPixel();

    /**
     * Return true if the image format support alpha channel. The returned value
     * represent the capability of the image format, not the current color
     * model.
     * 
     * @return true if alpha supported
     */
    protected abstract boolean imageFormatSupportAlphaChannel();

    /**
     * Take a MapBean, and get the image bytes that represent the current state.
     * 
     * @param map the MapBean.
     * @return byte[] representing an image of the map in it's current state.
     */
    public byte[] getImageFromMapBean(MapBean map) {
        return getImageFromMapBean(map, -1, -1, false);
    }

    /**
     * Take a MapBean, and get the image bytes that represent the current state.
     * 
     * @param map the MapBean.
     * @param width the pixel width of the desired image.
     * @param height the pixel height of the desired image.
     * @return byte[] representing an image of the map in it's current state.
     */
    public byte[] getImageFromMapBean(MapBean map, int width, int height) {
        return getImageFromMapBean(map, width, height, true);
    }

    /**
     * Take a MapBean, and get the image bytes that represent the current state.
     * 
     * @param map the MapBean.
     * @param width the pixel width of the desired image.
     * @param height the pixel height of the desired image.
     * @param scaleImage true to resize image based on scale
     * @return byte[] representing an image of the map in it's current state.
     */
    public byte[] getImageFromMapBean(MapBean map, int width, int height, boolean scaleImage) {
        if (map == null) {
            return new byte[0];
        }

        Proj proj = (Proj) map.getProjection();

        boolean needToScale = (width != proj.getWidth() || height != proj.getHeight());

        if (Debug.debugging("formatter")) {
            Debug.output("AIF: called with w:" + width + ", h:" + height + ", need to scale (" + needToScale + ")"
                    + " and scaleImage (" + scaleImage + ")");
        }

        if (width == -1)
            width = proj.getWidth();
        if (height == -1)
            height = proj.getHeight();

        Graphics graphics = getGraphics(width, height);

        if (!needToScale) {
            if (Debug.debugging("formatter")) {
                Debug.output("AIF: don't need to scale, painting normally.");
            }
            // This way just paints what the MapBean is displaying.
            map.paintAll(graphics);
        } else {
            // One problem with this approach is that it will
            // use the ProjectionPainter interface on the layers. So,
            // you may not get the same image that is on the map. All
            // layers on the map will get painted in the image - so if
            // a layer hasn't painted itself on the map window, you
            // will see it in the image.

            // This lets us know what the layers are
            map.addPropertyChangeListener(this);

            // Layers should be set...
            Point2D cp = map.getCenter();

            double scaleMod = 1f;// scale factor for image scale
            // If we need to scale the image,
            // figure out the scale factor.
            if (scaleImage) {
                if (Debug.debugging("formatter")) {
                    Debug.output("AIF: scaling image to w:" + width + ", h:" + height);
                }
                double area1 = (double) proj.getHeight() * (double) proj.getWidth();
                double area2 = (double) height * (double) width;
                scaleMod = Math.sqrt(area1 / area2);
            }

            Proj tp =
                    (Proj) map.getProjectionFactory().makeProjection(map.getProjection().getClass(), cp,
                                                                     map.getScale() * (float) scaleMod, width, height);

            tp.drawBackground((Graphics2D) graphics, map.getBckgrnd());

            if (layers != null) {
                for (int i = layers.length - 1; i >= 0; i--) {
                    Projection oldProj = layers[i].getProjection();
                    layers[i].renderDataForProjection(tp, graphics);
                    if (Debug.debugging("formatter")) {
                        Debug.output("AbstractImageFormatter: rendering " + layers[i].getName());
                    }
                    // Need to set the old Projection object on the
                    // Layer, not the current MapBean Proj object. If
                    // you set the MapBean Proj object, make sure you
                    // clone it first. The Layer will do a check on
                    // the Projection object it has against any new
                    // ones it receives. If it has the original from
                    // the MapBean, the check it does will return a
                    // false negative, and the layer will think it
                    // doesn't have to do anything.

                    if (oldProj != null && oldProj == map.getProjection()) {
                        // Seems like a lot of users are getting
                        // burned by manually setting the same
                        // projection on the MapBean as they are on
                        // the layers, and the layers are freezing up
                        // after they are used to create an image.

                        // I don't see how this problem is manifesting
                        // itself, but this code section is an attempt
                        // to help.
                        oldProj = oldProj.makeClone();
                    }

                    layers[i].setProjection(oldProj);
                }

            } else {
                Debug.output("AbstractImageFormatter can't get layers from map!");
            }

            map.removePropertyChangeListener(this);
            layers = null;
        }

        return getImageBytes();
    }

    /**
     * Return the applicable Graphics to use to paint the layers into. If the
     * internal BufferedImage hasn't been created yet, or has been set to null,
     * then a new buffered Image is created, set to the size specified by the
     * height and width. The ImageGenerator extends MapBean. Remember to dispose
     * of the graphics object when you are done with it. Uses the default
     * BufferedImage.TYPE_INT_RGB colormodel.
     * 
     * @param width pixel width of Graphics.
     * @param height pixel height of Graphics.
     * @return Graphics object to use.
     * @see java.awt.image.BufferedImage
     */
    public Graphics getGraphics(int width, int height) {
        return getGraphics(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public java.awt.Graphics getGraphics(int width, int height, boolean alpha) {
        int imageFormat = BufferedImage.TYPE_INT_RGB;
        if (alpha && (imageFormatSupportAlphaChannel() || imageFormatSupportTransparentPixel())) {
            imageFormat = BufferedImage.TYPE_INT_ARGB;
        }
        return getGraphics(width, height, imageFormat);
    }

    /**
     * Return the applicable Graphics to use to paint the layers into. If the
     * internal BufferedImage hasn't been created yet, or has been set to null,
     * then a new buffered Image is created, set to the size specified by the
     * height and width. The ImageGenerator extends MapBean. Remember to dispose
     * of the graphics object when you are done with it. Lets you select the
     * image type.
     * 
     * @param width pixel width of Graphics.
     * @param height pixel height of Graphics.
     * @param imageType image type - see BufferedImage
     * @return java.awt.Graphics object to use.
     * @see java.awt.image.BufferedImage
     */
    public Graphics getGraphics(int width, int height, int imageType) {
        bufferedImage = new BufferedImage(width, height, imageType);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Graphics g = ge.createGraphics(bufferedImage);
        g.setClip(0, 0, width, height);
        return g;
    }

    /**
     * Return the BufferedImage contained within the formatter.
     * 
     * @return the BufferedImage.
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    /**
     * Return the BufferedImage contained within the formatter.
     * 
     * @param bi the BufferedImage to use in this formatter.
     */
    public void setBufferedImage(BufferedImage bi) {
        bufferedImage = bi;
    }

    /**
     * Scale the internal BufferedImage to the pixel dimensions, and then return
     * it.
     * 
     * @param scaledWidth the desired pixel width of the image.
     * @param scaledHeight the desired pixel height of the image.
     * @return the scaled BufferedImage.
     */
    public BufferedImage getScaledBufferedImage(int scaledWidth, int scaledHeight) {

        if (bufferedImage == null) {
            return null;
        }

        if (Debug.debugging("formatter")) {
            Debug.output("Formatter: scaling image to : " + scaledWidth + ", " + scaledHeight);
        }

        java.awt.Image image = ImageScaler.getOptimalScalingImage(bufferedImage, scaledWidth, scaledHeight);

        if (Debug.debugging("formatter")) {
            Debug.output("Formatter: creating scaled image...");
        }

        try {

            BufferedImage buffi = BufferedImageHelper.getBufferedImage(image, 0, 0, -1, -1);

            // Do this here, in case something bad happens in the
            // buffered image creation, so at least the original image
            // is retained.
            bufferedImage = buffi;
        } catch (InterruptedException ie) {
            Debug.error("Formatter: Something bad happened during scaling! \n" + ie);
        }

        if (Debug.debugging("formatter")) {
            Debug.output("Formatter: image successfully scaled");
        }

        return bufferedImage;
    }

    /**
     * Return the image bytes of the formatted image.
     * 
     * @return byte[] representing the image.
     */
    public byte[] getImageBytes() {
        BufferedImage bi = getBufferedImage();
        if (bi == null) {
            return new byte[0];
        } else {
            Debug.message("formatter", "Formatter: creating formatted image bytes...");
            return formatImage(bi);
        }
    }

    /**
     * Scale the internal BufferedImage, then return the image bytes of the
     * formatted image.
     * 
     * @param scaledWidth the desired pixel width of the image.
     * @param scaledHeight the desired pixel height of the image.
     * @return byte[] representing the image.
     */
    public byte[] getScaledImageBytes(int scaledWidth, int scaledHeight) {
        BufferedImage bi = getScaledBufferedImage(scaledWidth, scaledHeight);
        if (bi == null) {
            return new byte[0];
        } else {
            Debug.message("formatter", "Formatter: creating formatted image bytes...");
            return formatImage(bi);
        }
    }

    /**
     * Set the layers and image type in the properties.
     */
    public void setProperties(Properties props) {
        setProperties((String) null, props);
    }

    /**
     * Part of the PropertyConsumer interface. Doesn't do anything yet.
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
     * Used when the layers from the MapBean are needed, in order to use the
     * renderDataForProjection method.
     */
    protected Layer[] layers = null;

    /**
     * Used when the layers from the MapBean are needed, in order to use the
     * renderDataForProjection method. Sets the Layer[] by adding the formatter
     * as a PropertyChangeListener to the MapBean. Remember to remove the
     * formatter from the MapBean as a PropertyChangeListener.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        String propName = pce.getPropertyName();
        if (propName == MapBean.LayersProperty) {
            layers = (Layer[]) pce.getNewValue();
        }
    }
}