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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMScalingIcon.java,v $
// $RCSfile: OMScalingIcon.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.Serializable;
import javax.swing.ImageIcon;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.proj.Projection;

/**
 * This is an extension to OMScalingRaster that scales an icon.  The
 * icon is automatically centered over the lat/lon location.  The
 * offsets push the icon away from the lat/lon.
 * @see OMScalingRaster
 */
public class OMScalingIcon extends OMScalingRaster implements Serializable {

    protected float baseScale;
    protected float maxScale = Float.MAX_VALUE;
    protected float minScale = 0f;
    
    /**
     * Constuct a blank OMRaster, to be filled in with set calls.
     */
    public OMScalingIcon() {
        super();
    }
    
    ///////////////////////////////////// INT PIXELS - DIRECT COLORMODEL
    
    /**
     * Creates an OMRaster images, Lat/Lon placement with a direct
     * colormodel image.
     *
     * @param centerLat latitude of the top of the image.
     * @param centerLon longitude of the left side of the image.
     * @param offsetX horizontal pixel offset of icon (positive pushes east).
     * @param offsetY vertical pixel offset of icon (positive pushes south).
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @param baseScale the scale where the icon will be show regular size.
     * @see #setPixel 
     */
    public OMScalingIcon(float centerLat, float centerLon, int offsetX, int offsetY,
                         int w, int h, int[] pix, float baseScale) {

        super(centerLat, centerLon, 0f, 0f, w, h, pix);
        setX(offsetX);
        setY(offsetY);
        this.baseScale = baseScale;
    }
  
    ////////////////////////////////////// IMAGEICON

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon.
     *
     * @param centerLat latitude of the top of the image.
     * @param centerLon longitude of the left side of the image.
     * @param offsetX horizontal pixel offset of icon (positive pushes east).
     * @param offsetY vertical pixel offset of icon (positive pushes south).
     * @param ii ImageIcon used for the image.
     * @param baseScale the scale where the icon will be show regular size.
     */
    public OMScalingIcon(float centerLat, float centerLon, 
                         int offsetX, int offsetY,
                         ImageIcon ii, float baseScale) {
        this(centerLat, centerLon, offsetX, offsetY, ii.getImage(), baseScale);
    }

    /**
     * Create an OMRaster, Lat/Lon placement with an Image.
     *
     * @param centerLat latitude of the top of the image.
     * @param centerLon longitude of the left side of the image.
     * @param offsetX horizontal pixel offset of icon (positive pushes east).
     * @param offsetY vertical pixel offset of icon (positive pushes south).
     * @param ii Image used for the image.
     * @param baseScale the scale where the icon will be show regular size.
     */
    public OMScalingIcon(float centerLat, float centerLon,
                         int offsetX, int offsetY,
                         Image ii, float baseScale) {
        super();
        setRenderType(OMGraphic.RENDERTYPE_LATLON);
        setColorModel(COLORMODEL_IMAGEICON);

        lat = centerLat;
        lon = centerLon;
        setImage(ii);
        setX(offsetX);
        setY(offsetY);
        this.baseScale = baseScale;
    }
  
    ////////////////////////////////////// BYTE PIXELS with COLORTABLE

    /**
     * Lat/Lon placement with a indexed colormodel, which is using a
     * colortable and a byte array to contruct the int[] pixels.  
     *
     * @param centerLat latitude of the top of the image.
     * @param centerLon longitude of the left side of the image.
     * @param offsetX horizontal pixel offset of icon (positive pushes east).
     * @param offsetY vertical pixel offset of icon (positive pushes south).
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @param baseScale the scale where the icon will be show regular size.
     * @see #setPixel 
     */
    public OMScalingIcon(float centerLat, float centerLon,
                         int offsetX, int offsetY,
                         int w, int h, 
                         byte[] bytes,
                         Color[] colorTable,
                         int trans, float baseScale) {

        super(centerLat, centerLon, 0f, 0f, w, h, bytes, colorTable, trans);
        setX(offsetX);
        setY(offsetY);
        this.baseScale = baseScale;
    }

    /**
     * Since the image doesn't necessarily need to be regenerated
     * when it is merely moved, raster objects have this function,
     * called from generate() and when a placement attribute is
     * changed.
     *
     * @return true if enough information is in the object for proper
     * placement.
     * @param proj projection of window.
     */
    protected boolean position(Projection proj) {

        if (proj == null) {
            Debug.error("OMScalingIcon: null projection in position!");
            return false;
        }
        
        float shrinkScale = proj.getScale();

        if (shrinkScale > maxScale) {
            shrinkScale = maxScale;
        }
        if (shrinkScale < minScale) {
            shrinkScale = minScale;
        }
        
        float scaleFactor = baseScale/shrinkScale;

        point1 = proj.forward(lat, lon);
        point2 = proj.forward(lat, lon);

        point1.setLocation((int)(point1.getX() + (scaleFactor * (getX() - sourceImage.getWidth()/2))),
                          (int)(point1.getY() + (scaleFactor * (getY() - sourceImage.getHeight()/2))));

        point2.setLocation((int)(point2.getX() + (scaleFactor * (getX() + sourceImage.getWidth()/2))),
                           (int)(point2.getY() + (scaleFactor * (getY() + sourceImage.getHeight()/2))));

        setNeedToReposition(false);
        return true;
    }

    public boolean isOnMap(Projection proj) {
        generate(proj); // Should only generate if needed...

        Shape shape = getShape();
        if (shape == null) {
            return false;
        }

        Point p1 = proj.forward(proj.getUpperLeft());
        Point p2 = proj.forward(proj.getLowerRight());
        int h = (int)(p2.getY() - p1.getY());
        int w = (int)(p2.getX() - p1.getX());

        Rectangle mapRect = new Rectangle((int)p1.getX(), (int)p1.getY(), w, h);
        
        return mapRect.intersects(shape.getBounds());
    }

    public void setBaseScale(float bs) {
        baseScale = bs;
    }

    public float getBaseScale() {
        return baseScale;
    }

    /**
     * Set the scale that limits how big an icon will grow.
     */
    public void setMaxScale(float ms) {
        maxScale = ms;
    }

    public float getMaxScale() {
        return maxScale;
    }

    /**
     * Set the scale that limits how small an icon will shrink.
     */
    public void setMinScale(float ms) {
        minScale = ms;
    }

    public float getMinScale() {
        return minScale;
    }

    

    
}


