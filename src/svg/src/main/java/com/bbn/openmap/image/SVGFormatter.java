/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                            A Part of  
 *                               GTE      
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                          (617) 873-3000
 *  
 *        Copyright 1999-2000 by BBNT Solutions LLC,
 *              A part of GTE, all rights reserved.
 *  
 * **********************************************************************
 * 
 * $Source: /cvs/distapps/openmap/src/svg/com/bbn/openmap/image/SVGFormatter.java,v $
 * $Revision: 1.8 $
 * $Date: 2009/02/23 22:37:33 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

package com.bbn.openmap.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.util.Debug;

/**
 * The SVGFormatter lets you create SVG documents from the MapBean and
 * it's layers. Tested with the Batik-1.1.1 package available at:
 * http://xml.apache.org/batik/dist/batik-1.1.1.zip
 * <P>
 * Code initially provided by Sebastien Prud'homme, modified by
 * dietrick.
 * <P>
 */
public class SVGFormatter extends AbstractImageFormatter {

    SVGGraphics2D svgGenerator = null;

    public SVGFormatter() {}

    /** Set the properties of the image formatter. */
    public void setProperties(String prefix, Properties props) {}

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
    public ImageFormatter makeClone() {
        return new SVGFormatter();
    }

    /**
     * Convert a BufferedImage to a image file format. Not the same as
     * other ImageFormatters. Returns a SVG document which refers to
     * the image.
     * 
     * @param bi BufferedImage
     * @return null. The SVGFormatter returns the SVG file containing
     *         information about the image. Probably not what you
     *         expected.
     */
    public byte[] formatImage(BufferedImage bi) {
        return null;
    }

    /**
     * Take a MapBean, and get the SVG document that represents what's
     * on it.
     * 
     * @param map the MapBean.
     * @return byte[] representing an SVG of the map in it's current
     *         state.
     */
    public byte[] getImageFromMapBean(com.bbn.openmap.MapBean map) {

        Proj proj = (Proj) map.getProjection();
        java.awt.Graphics graphics = getGraphics(proj.getWidth(),
                proj.getHeight());

        // This should actually be getting the layers, and rendering
        // each one. It turns out that calling paintChildren() will
        // render a buffered image if the MapBean is buffered, and
        // that's not what we really want here.
        //      map.paintChildren(graphics);

        map.addPropertyChangeListener(this);

        // Layers should be set...
        proj.drawBackground((Graphics2D) graphics, map.getBckgrnd());

        if (layers != null) {
            for (int i = layers.length - 1; i >= 0; i--) {
                layers[i].renderDataForProjection(proj, graphics);
            }
        } else {
            Debug.error("SVGFormatter can't get layers from map!");
        }

        map.removePropertyChangeListener(this);
        layers = null;

        return getImageBytes();
    }

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
    public Graphics getGraphics(int width, int height) {
        if (svgGenerator == null) {
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument(null, "svg", null);
            svgGenerator = new SVGGraphics2D(document);
            svgGenerator.setClip(0, 0, width, height);
        }
        return svgGenerator;
    }

    /**
     * Return the BufferedImage contained within the formatter.
     * 
     * @return null - there isn't a BufferedImage in this formatter.
     */
    public BufferedImage getBufferedImage() {
        return null;
    }

    /**
     * For ImageFormatters, returns the BufferedImage contained within
     * the formatter. Doesn't do anything for the SVGFormatter.
     * 
     * @param bi the BufferedImage to use in this formatter.
     */
    public void setBufferedImage(BufferedImage bi) {}

    /**
     * Scale the internal BufferedImage to the pixel dimensions, and
     * then return it. Not implemented.
     * 
     * @param scaledWidth the desired pixel width of the image.
     * @param scaledHeight the desired pixel height of the image.
     * @return null.
     */
    public BufferedImage getScaledBufferedImage(int scaledWidth,
                                                int scaledHeight) {
        return null;
    }

    /**
     * Return the string bytes for the SVG document.
     * 
     * @return byte[] representing the image.
     */
    public byte[] getImageBytes() {
        if (svgGenerator != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(stream, SVGGraphics2D.DEFAULT_XML_ENCODING);
                svgGenerator.stream(writer, false);
                return stream.toByteArray();
            } catch (java.io.IOException ioe) {
                Debug.error("SVGFormatter caught IOException formatting svg!");
            }
        }
        return new byte[0];
    }

    /**
     * Scale the internal BufferedImage, then return the image bytes
     * of the formatted image. Not implemented. Unscaled image
     * returned.
     * 
     * @param scaledWidth the desired pixel width of the image.
     * @param scaledHeight the desired pixel height of the image.
     * @return byte[] representing the image.
     */
    public byte[] getScaledImageBytes(int scaledWidth, int scaledHeight) {
        return getImageBytes();
    }

    /**
     * Get the Image Type created by the SVGFormatter. These responses
     * should adhere to the OGC WMT standard format labels. Some are
     * listed in the WMTConstants interface file.
     */
    public String getFormatLabel() {
        return WMTConstants.IMAGEFORMAT_SVG;
    }

    public String getContentType() {
        return "image/svg+xml";
    }

    @Override
    protected boolean imageFormatSupportAlphaChannel() {
        return false;
    }

    @Override
    protected boolean imageFormatSupportTransparentPixel() {
        return false;
    }
}