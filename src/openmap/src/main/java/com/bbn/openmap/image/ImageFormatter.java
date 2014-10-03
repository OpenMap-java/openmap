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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/ImageFormatter.java,v $
// $RCSfile: ImageFormatter.java,v $
// $Revision: 1.3 $
// $Date: 2008/02/20 01:41:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.image.BufferedImage;
import java.util.Properties;

/**
 * The ImageFormatter deals with most of the image meanderings of
 * Java, while letting you create an image in a specific format.
 */
public interface ImageFormatter {

    /** Set the properties of the image formatter. */
    public void setProperties(String prefix, Properties props);

    /**
     * Create a new instance of the same type of formatter. If you are
     * running in a multi-threaded environment, you'll need to provide
     * a new instance of the formatter to each thread, since the image
     * and graphics that are being drawn into for each thread are
     * contained within.
     * 
     * @return a new instance of this type of formatter, with the same
     *         properties set.
     */
    public abstract ImageFormatter makeClone();

    /**
     * Convert a BufferedImage to a image file format...
     * 
     * @param bi a BufferedImage..
     */
    public byte[] formatImage(BufferedImage bi);

    /**
     * Take a MapBean, and get the image bytes that represent the
     * current state.
     * 
     * @param map the MapBean.
     * @return byte[] representing an image of the map in it's current
     *         state.
     */
    public byte[] getImageFromMapBean(com.bbn.openmap.MapBean map);

    /**
     * Return the applicable Graphics to use to paint the layers into.
     * If the internal BufferedImage hasn't been created yet, or has
     * been set to null, then a new buffered Image is created, set to
     * the size specified by the height and width given. The
     * ImageGenerator extends MapBean. Remember to dispose of the
     * graphics object when you are done with it.
     * 
     * @param width pixel width of Graphics.
     * @param height pixel height of Graphics.
     * @return java.awt.Graphics object to use.
     */
    public java.awt.Graphics getGraphics(int width, int height);

    public java.awt.Graphics getGraphics(int width, int height, boolean alpha);
    
    /**
     * Return the BufferedImage contained within the formatter.
     * 
     * @return the BufferedImage.
     */
    public BufferedImage getBufferedImage();

    /**
     * Return the BufferedImage contained within the formatter.
     * 
     * @param bi the BufferedImage to use in this formatter.
     */
    public void setBufferedImage(BufferedImage bi);

    /**
     * Scale the internal BufferedImage to the pixel dimensions, and
     * then return it.
     * 
     * @param scaledWidth the desired pixel width of the image.
     * @param scaledHeight the desired pixel height of the image.
     * @return the scaled BufferedImage.
     */
    public BufferedImage getScaledBufferedImage(int scaledWidth,
                                                int scaledHeight);

    /**
     * Return the image bytes of the formatted image.
     * 
     * @return byte[] representing the image.
     */
    public byte[] getImageBytes();

    /**
     * Scale the internal BufferedImage, then return the image bytes
     * of the formatted image.
     * 
     * @param scaledWidth the desired pixel width of the image.
     * @param scaledHeight the desired pixel height of the image.
     * @return byte[] representing the image.
     */
    public abstract byte[] getScaledImageBytes(int scaledWidth, int scaledHeight);

    /**
     * Get the Image Type created by the ImageFormatter. These
     * responses should adhere to the OGC WMT standard format labels.
     * Some are listed in the WMTConstants interface file.
     */
    public String getFormatLabel();
    
    /**
     * Get the Mime Content Type created by the ImageFormatter.
     */
    public String getContentType();
    
}