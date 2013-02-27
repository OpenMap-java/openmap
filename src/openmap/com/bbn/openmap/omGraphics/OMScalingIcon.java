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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMScalingIcon.java,v $
// $RCSfile: OMScalingIcon.java,v $
// $Revision: 1.10 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * This is an extension to OMScalingRaster that scales an icon. The icon is
 * automatically centered over the lat/lon location. The offsets push the icon
 * away from the lat/lon.
 * 
 * @see OMScalingRaster
 */
public class OMScalingIcon extends OMScalingRaster implements Serializable {

    protected float baseScale;
    protected float maxScale = Float.MAX_VALUE;
    protected float minScale = 0f;

    /**
     * Construct a blank OMRaster, to be filled in with set calls.
     */
    public OMScalingIcon() {
        super();
    }

    // /////////////////////////////////// INT PIXELS - DIRECT
    // COLORMODEL

    /**
     * Creates an OMRaster images, Lat/Lon placement with a direct colormodel
     * image.
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
    public OMScalingIcon(double centerLat, double centerLon, int offsetX, int offsetY, int w,
            int h, int[] pix, float baseScale) {

        super(centerLat, centerLon, 0f, 0f, w, h, pix);
        setX(offsetX);
        setY(offsetY);
        this.baseScale = baseScale;
    }

    // //////////////////////////////////// IMAGEICON

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
    public OMScalingIcon(double centerLat, double centerLon, int offsetX, int offsetY,
            ImageIcon ii, float baseScale) {
        this(centerLat, centerLon, offsetX, offsetY, ii.getImage(), baseScale);
    }

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon. Doesn't scale,
     * because baseScale, minScale and maxScale are all set to the same number
     * (4000000).
     * 
     * @param centerLat latitude of the top of the image.
     * @param centerLon longitude of the left side of the image.
     * @param ii ImageIcon used for the image.
     */
    public OMScalingIcon(double centerLat, double centerLon, ImageIcon ii) {
        this(centerLat, centerLon, ii.getImage());
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
    public OMScalingIcon(double centerLat, double centerLon, int offsetX, int offsetY, Image ii,
            float baseScale) {
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

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon. Doesn't scale,
     * because baseScale, minScale and maxScale are all set to the same number
     * (4000000).
     * 
     * @param centerLat latitude of the top of the image.
     * @param centerLon longitude of the left side of the image.
     * @param image ImageIcon used for the image.
     */
    public OMScalingIcon(double centerLat, double centerLon, Image image) {
        this(centerLat, centerLon, 0, 0, image, 4000000);
        setMaxScale(4000000);
        setMinScale(4000000);
    }

    // //////////////////////////////////// BYTE PIXELS with
    // COLORTABLE

    /**
     * Lat/Lon placement with a indexed colormodel, which is using a colortable
     * and a byte array to construct the int[] pixels.
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
    public OMScalingIcon(float centerLat, float centerLon, int offsetX, int offsetY, int w, int h,
            byte[] bytes, Color[] colorTable, int trans, float baseScale) {

        super(centerLat, centerLon, 0f, 0f, w, h, bytes, colorTable, trans);
        setX(offsetX);
        setY(offsetY);
        this.baseScale = baseScale;
    }

    /**
     * Since the image doesn't necessarily need to be regenerated when it is
     * merely moved, raster objects have this function, called from generate()
     * and when a placement attribute is changed.
     * 
     * @return true if enough information is in the object for proper placement.
     * @param proj projection of window.
     */
    protected boolean position(Projection proj) {

        if (proj == null) {
            Debug.error("OMScalingIcon: null projection in position!");
            return false;
        }

        if (sourceImage == null) {
            // Debug.error("OMScalingIcon: null sourceImage in position!");
            // XXX: For now fail silently.
            return false;
        }

        float shrinkScale = proj.getScale();

        if (shrinkScale > maxScale) {
            shrinkScale = maxScale;
        }
        if (shrinkScale < minScale) {
            shrinkScale = minScale;
        }

        float scaleFactor = baseScale / shrinkScale;

        point1 = (Point) proj.forward(lat, lon, new Point());
        point2 = (Point) proj.forward(lat, lon, new Point());

        int halfImageWidth = sourceImage.getWidth() / 2;
        int halfImageHeight = sourceImage.getHeight() / 2;
        int myX = getX();
        int myY = getY();

        double p1x = point1.getX();
        double p1y = point1.getY();
        double newP1x = p1x + (scaleFactor * (myX - halfImageWidth));
        double newP1y = p1y + (scaleFactor * (myY - halfImageHeight));
        point1.setLocation((int) newP1x, (int) newP1y);

        double p2x = point2.getX();
        double p2y = point2.getY();
        double newP2x = p2x + (scaleFactor * (myX + halfImageWidth));
        double newP2y = p2y + (scaleFactor * (myY + halfImageHeight));
        point2.setLocation((int) newP2x, (int) newP2y);

        setNeedToReposition(false);
        return true;
    }

    public boolean isOnMap(Projection proj) {
        generate(proj); // Should only generate if needed...

        Shape shape = getShape();
        if (shape == null) {
            return false;
        }

        Point2D p1 = proj.forward(proj.getUpperLeft());
        Point2D p2 = proj.forward(proj.getLowerRight());
        int h = (int) (p2.getY() - p1.getY());
        int w = (int) (p2.getX() - p1.getX());

        Rectangle mapRect = new Rectangle((int) p1.getX(), (int) p1.getY(), w, h);

        return mapRect.intersects(shape.getBounds());
    }

    /**
     * Overridding this so we don't clip rotated icons near the edge of the map.  Just display icons as whole.
     */
    protected void scaleTo(Projection thisProj) {

        if (DEBUG)
            logger.fine("OMScalingRaster: scaleTo()");

        if (sourceImage == null) {
            if (DEBUG) {
                logger.fine("OMScalingRaster.scaleTo() sourceImage is null");
            }
            return;
        }

        // Get image projection rectangle
        Rectangle projRect = new Rectangle();
        projRect.setLocation(point1);
        projRect.setSize(point2.x - point1.x, point2.y - point1.y);

        Rectangle sourceRect = new Rectangle();
        sourceRect.width = sourceImage.getWidth();
        sourceRect.height = sourceImage.getHeight();

        // Now we have everything we need to sort out this new projection.
        // boolean currentVisibility = isVisible();

        if (!projRect.isEmpty()) {

            // Assume will need whole image, set the clipRect so it's
            // on the map, somewhere.

            // If big enough to see
            if ((projRect.width >= 1) && (projRect.height >= 1)) {

                // check width and height of clipRect, in case it got
                // rounded down to zero.
                if (sourceRect.width <= 0) {
                    sourceRect.width = 1;
                }
                if (sourceRect.height <= 0) {
                    sourceRect.height = 1;
                }
                // Now we can grab the bit we want out of the source
                // and scale it to fit the intersection.

                // Calc width adjustment
                double widthAdj = (double) projRect.width / (double) sourceRect.width;
                // Calc height adjustment
                double heightAdj = (double) projRect.height / (double) sourceRect.height;
                // Create the transform
                AffineTransform xform = new AffineTransform();
                // Specify scaling
                xform.setToScale(widthAdj, heightAdj);

                // Create the transform op.
                // AffineTransformOp xformOp = new AffineTransformOp(xform,
                // AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                AffineTransformOp xformOp = new AffineTransformOp(xform, getScaleTransformType());
                // Scale clip area -> newImage
                // extract sub-image
                try {
                    BufferedImage newImage = xformOp.filter(sourceImage, null);

                    bitmap = newImage;
                    point1.setLocation(projRect.x, projRect.y);
                    // setVisible(currentVisibility);
                } catch (IllegalArgumentException iae) {
                    // This has been kicked off when the dimensions of the
                    // filter get too big. Treat it like the height and width
                    // being set to -1.
                    logger.fine("Caught IllegalArgumentException: " + iae.getMessage());
                    bitmap = null;
                }
            }
        } else {
            bitmap = null;
        }
    }

    public void setBaseScale(float bs) {
        baseScale = bs;
    }

    public float getBaseScale() {
        return baseScale;
    }

    /**
     * Set the scale that limits how small an icon will shrink. Should be a
     * number larger than the base scale. If the map scale gets larger than this
     * number, the icon will stop shrinking.
     */
    public void setMaxScale(float ms) {
        maxScale = ms;
    }

    public float getMaxScale() {
        return maxScale;
    }

    /**
     * Set the scale that limits how big an icon should grow. Should be a number
     * smaller than the base scale. If the map scale gets smaller than this
     * number, the icon will stop growing.
     */
    public void setMinScale(float ms) {
        minScale = ms;
    }

    public float getMinScale() {
        return minScale;
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMScalingIcon) {
            OMScalingIcon icon = (OMScalingIcon) source;
            this.baseScale = icon.baseScale;
            this.maxScale = icon.maxScale;
            this.minScale = icon.minScale;
        }
    }

}
