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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMScalingRaster.java,v $
// $RCSfile: OMScalingRaster.java,v $
// $Revision: 1.6 $
// $Date: 2004/09/17 19:17:48 $
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
 * This is an extension to OMRaster that automatically scales itelf to
 * match the current projection.  It is only lat/lon based, and takes
 * the coordinates of the upper left and lower right corners of the
 * image.  It does straight scaling - it does not force the image
 * projection to match the map projection!  So, your mileage may vary
 * - you have to understand the projection of the image, and know how
 * it fits the projection type of the map.  Of course, at larger
 * scales, it might not matter so much.
 *
 * This class was inspired by, and created from parts of the
 * ImageLayer submission from Adrian Lumsden@sss, on 25-Jan-2002. Used
 * the scaling and trimming code from that submission.  That code was
 * also developed with assistance from Steve McDonald at
 * SiliconSpaceships.com.
 *
 * @see OMRaster
 * @see OMRasterObject 
 */
public class OMScalingRaster extends OMRaster implements Serializable {

    /**
     * The latitude of the lower right corner for the image, in
     * decimal degrees. 
     */
    protected float lat2 = 0.0f;

    /**
     * The longitude of the lower right corner for the image, in
     * decimal degrees. 
     */
    protected float lon2 = 0.0f;
    
    /**
     * This the original version of the image, which we keep around
     * for rescaling later.
     */
    protected BufferedImage sourceImage = null;

    /**
     * The rectangle in screen co-ordinates that the scaled
     * image projects to after clipping.
     */
    protected Rectangle clipRect;

    /**
     * Constuct a blank OMRaster, to be filled in with set calls.
     */
    public OMScalingRaster() {
        super();
    }
    
    ///////////////////////////////////// INT PIXELS - DIRECT COLORMODEL
    
    /**
     * Creates an OMRaster images, Lat/Lon placement with a direct
     * colormodel image.
     *
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     */
    public OMScalingRaster(float ullat, float ullon, 
                           float lrlat, float lrlon,
                           int w, int h, 
                           int[] pix) {

        super(ullat, ullon, w, h, pix);
        lat2 = lrlat;
        lon2 = lrlon;
    }
  
    ////////////////////////////////////// IMAGEICON

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon.
     *
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param ii ImageIcon used for the image.
     */
    public OMScalingRaster(float ullat, float ullon, 
                           float lrlat, float lrlon,
                           ImageIcon ii) {
        this(ullat, ullon, lrlat, lrlon, ii.getImage());
    }

    /**
     * Create an OMRaster, Lat/Lon placement with an Image.
     *
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param ii Image used for the image.
     */
    public OMScalingRaster(float ullat, float ullon,
                           float lrlat, float lrlon, Image ii) {
        super();
        setRenderType(OMGraphic.RENDERTYPE_LATLON);
        setColorModel(COLORMODEL_IMAGEICON);

        lat = ullat;
        lon = ullon;
        lat2 = lrlat;
        lon2 = lrlon;
        setImage(ii);
    }
  
    ////////////////////////////////////// BYTE PIXELS with COLORTABLE

    /**
     * Lat/Lon placement with a indexed colormodel, which is using a
     * colortable and a byte array to contruct the int[] pixels.  
     *
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     */
    public OMScalingRaster(float ullat, float ullon,
                           float lrlat, float lrlon,
                           int w, int h, 
                           byte[] bytes,
                           Color[] colorTable,
                           int trans) {

        super(ullat, ullon, w, h, bytes, colorTable, trans);
        lat2 = lrlat;
        lon2 = lrlon;
    }

    /**
     * Creates a BufferedImage version of the image.  A new
     * BufferedImage object is created, and the image is copied into
     * it.  You can get rid of the input image after calling this
     * method.  The OMRaster variables height, width and bitmap are
     * set here to the values for the new BufferedImage.
     *
     * @param image the input image.  
     */
    public void setImage(Image image) {
        if (DEBUG) {
            Debug.output("OMScalingRaster.setImage: " + image);
        }

        if (image == null) {
            return;
        }

        if (!(image instanceof BufferedImage)) {
            sourceImage = new BufferedImage(image.getWidth(this),
                                            image.getHeight(this),
                                            BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2D = sourceImage.createGraphics();
            g2D.drawImage(image, 0, 0, this);
        } else {
            sourceImage = (BufferedImage)image;
        }

        width = sourceImage.getWidth();
        height = sourceImage.getHeight();

        // Just in case rendering tries to happen.
        bitmap = sourceImage;
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
            if (DEBUG) {
                Debug.error("OMScalingRaster: null projection in position!");
            }
            return false;
        }

        point1 = proj.forward(lat, lon);
        point2 = proj.forward(lat2, lon2);

        setNeedToReposition(false);
        return true;
    }

    /** 
     * Prepare the graphics for rendering. For all image types, it
     * positions the image relative to the projection.  For direct and
     * indexed colormodel images, it creates the ImageIcon used for
     * drawing to the window (internal to object).  For indexed
     * colormodel images, it also calls computePixels, to resolve the
     * colortable and the bytes to create the image pixels.
     *
     * @param proj Projection used to position the image on the window.
     * @return true if the image is ready to paint.
     */
    public boolean generate(Projection proj) {

        // Position sets the position for the OMRaster!!!!
        if (!position(proj)) {
            if (DEBUG) {
                Debug.error("OMRaster.generate(): positioning failed!");
            }
            return false;
        }

        if (colorModel != COLORMODEL_IMAGEICON) {
            // If the sourceImage hasn't been created, and needs to
            // be, then just do what we normally do in OMRaster.
            if (sourceImage == null || getNeedToRegenerate()) {
                if (DEBUG) {
                    Debug.output("OMScalingRaster: generating image");
                }
                boolean ret = super.generate(proj);
                // bitmap is set to a BufferedImage
                setImage(bitmap);
            }
        }           

        // point1 and point2 are already set in position()

        // We assume that the image doesn't cross the dateline, and
        // that p1 is upper left corner, and p2 is lower right.
        // scaleTo modifies the internal bitmap image for display.
        scaleTo(proj);

        // generate shape that is a boundary of the generated image.
        // We'll make it a GeneralPath rectangle.
        int w = bitmap.getWidth(this);
        int h = bitmap.getHeight(this);

        shape = createBoxShape(point1.x, point1.y, w, h);

        setNeedToRegenerate(false);
        return true;
    }

    /**
     * Since the OMScalingRaster changes height and width depending on
     * scale, we need to rotate the image over that point and factor
     * in the scaled height and width of the image.  Called from
     * within OMRasterObject.render().
     */
    protected void rotate(Graphics2D g) {
        int rotOffsetX = point1.x + (point2.x - point1.x)/2;
        int rotOffsetY = point1.y + (point2.y - point1.y)/2;
        ((Graphics2D)g).rotate(rotationAngle, rotOffsetX, rotOffsetY);
    }

    /**
     * Take the current projection and the sourceImage, and make the
     * image that gets displayed fit the projection.  If the source
     * image isn't over the map, then this OMGraphic is set to be
     * invisible.  If part of the image is on the map, only that part
     * is used.  The OMRaster bitmap variable is set with an image
     * that is created from the source image, and the point1 variable
     * is set to the point where the image should be placed.  For
     * instance, if the source image upper left corner is off the map
     * to the NorthWest, then the OMRaster bitmap is set to a image,
     * clipped from the source, that is entirely on the map.  The
     * OMRaster point1 is set to 0, 0, since that is where the clipped
     * image should be placed.
     *
     * @param thisProj the projection that the image should be scaled
     * to.  
     */
    protected void scaleTo(Projection thisProj) {

        if (DEBUG) Debug.output("OMScalingRaster: scaleTo()");

        if (sourceImage == null) {
            if (DEBUG) {
                Debug.output("OMScalingRaster.scaleTo() sourceImage is null");
            }
            return;
        }
        
        // Get the projection window rectangle in pix
        Rectangle winRect = new Rectangle(thisProj.getWidth(),  
                                          thisProj.getHeight());
        // Get image projection rectangle
        Rectangle projRect = new Rectangle();
        projRect.setLocation(point1);
        projRect.setSize(point2.x - point1.x, point2.y - point1.y);
        
        Rectangle sourceRect = new Rectangle();
        sourceRect.width = sourceImage.getWidth();
        sourceRect.height = sourceImage.getHeight();
        
        // Now we have everything we need to sort out this new
        // projection.
        setVisible(false); // Assume we wont see it
        clipRect = null;

        Rectangle iRect = winRect.intersection(projRect);
        if (!iRect.isEmpty()) {
            // Now we have the visible rectangle of the projected
            // image we need to figure out which pixels from the
            // source image get scaled to produce it.
            
            // Assume will need whole image, set the clipRect so it's
            // on the map, somewhere.
            clipRect = new Rectangle();
            clipRect.setBounds(sourceRect);

            // If big enough to see
            if ((iRect.width >= 1) && (iRect.height >= 1)) {

                // If it didn't all fit
                if (!winRect.contains(projRect)) {
                    //   calc X scale factor
                    float xScaleFactor = (float)sourceRect.width / (float)projRect.width;
                    //   and Y scale factor
                    float yScaleFactor = (float)sourceRect.height / (float)projRect.height;
                    int xOffset = (int)((iRect.x - projRect.x)); //   and x offset
                    int yOffset = (int)((iRect.y - projRect.y)); //   and y offset
                    clipRect.x = (int)(xOffset * xScaleFactor); //   scale the x position
                    clipRect.y = (int)(yOffset * yScaleFactor); //   scale the y position

                    // Do Math.ceil because the icon was getting
                    // clipped a little if it started to move off the
                    // screen a little.
                    clipRect.width = (int)Math.ceil(iRect.width * xScaleFactor); //   scale the width
                    clipRect.height = (int)Math.ceil(iRect.height * yScaleFactor); //   scale the height

                    // Make sure the rounding doesn't exceed the original icon bounds
                    if (clipRect.width + clipRect.x > sourceRect.width)
                        clipRect.width = sourceRect.width - clipRect.x;
                    if (clipRect.height + clipRect.y > sourceRect.height)
                        clipRect.height = sourceRect.height - clipRect.y;
                }

                // check width and height of clipRect, in case it got
                // rounded down to zero.
                if (clipRect.width <= 0) {
                    clipRect.width = 1;
                }
                if (clipRect.height <= 0) {
                    clipRect.height = 1;
                }
                // Now we can grab the bit we want out of the source and
                // scale it to fit the intersection.

                // Calc width adjustment
                double widthAdj = (double)iRect.width / (double)clipRect.width;
                // Calc height adjustment
                double heightAdj = (double)iRect.height / (double)clipRect.height;
                // Create the transform
                AffineTransform xform = new AffineTransform();
                // Specify scaling
                xform.setToScale(widthAdj, heightAdj);
                
                // Create the transform op.
                AffineTransformOp xformOp = new AffineTransformOp(xform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                // Scale clip area -> newImage
                //   extract sub-image
                BufferedImage newImage = xformOp.filter(sourceImage.getSubimage(clipRect.x, clipRect.y, clipRect.width, clipRect.height), null);
                
                bitmap = newImage;
                point1.setLocation(iRect.x, iRect.y);
                setVisible(true);
            }
        }
    }
    
    /**
     * Return the rectangle in screen co-ordinates that the scaled
     * image has been clipped to. This may return a null rectangle
     * (i.e. the image is out of the window).  Otherwise the returned
     * rectangle should always at least partially lie within the
     * bounds of the window.
     */
    public Rectangle getClippedRectangle() {
        return clipRect;
    }

    /**
     * Change the upper latitude attribute.
     *
     * @param value latitude in decimal degrees.  
     */
    public void setULLat(float value) {
        setLat(value);
    }

    /**
     * Get the upper latitude.
     *
     * @return the latitude in decimal degrees.
     */
    public float getULLat() {
        return getLat();
    }

    /**
     * Change the western longitude attribute.
     *
     * @param value the longitude in decimal degrees.  
     */
    public void setULLon(float value) {
        setLon(value);
    }

    /**
     * Get the western longitude.
     *
     * @return longitude in decimal degrees.
     */
    public float getULLon() {
        return getLon();
    }

    /**
     * Change the southern latitude attribute.
     *
     * @param value latitude in decimal degrees.  
     */
    public void setLRLat(float value) {
        if (lat2 == value) return;
        lat2 = value;
        setNeedToReposition(true);
    }

    /**
     * Get the southern latitude.
     *
     * @return the latitude in decimal degrees.
     */
    public float getLRLat() {
        return lat2;
    }

    /**
     * Change the eastern longitude attribute.
     *
     * @param value the longitude in decimal degrees.  
     */
    public void setLRLon(float value) {
        if (lon2 == value) return;
        lon2 = value;
        setNeedToReposition(true);
    }

    /**
     * Get the eastern longitude.
     *
     * @return longitude in decimal degrees.
     */
    public float getLRLon() {
        return lon2;
    }

    /**
     * Set the rectangle, based on the location and size of the image
     * after scaling.
     */
    public void setShape() {

        // generate shape that is a boundary of the generated image.
        // We'll make it a GeneralPath rectangle.
        int w = point2.x - point1.x;
        int h = point2.y - point1.y;

        shape = createBoxShape(point1.x, point1.y, w, h);
    }

    public boolean isOnMap(Projection proj) {
        Point p1 = proj.forward(lat, lon);
        Point p2 = proj.forward(lat2, lon2);
        int h = (int)(p2.getY() - p1.getY());
        int w = (int)(p2.getX() - p1.getX());

        Rectangle imageRect = new Rectangle((int)p1.getX(), (int)p1.getY(), w, h);

        proj.forward(proj.getUpperLeft(), p1);
        proj.forward(proj.getLowerRight(), p2);
        h = (int)(p2.getY() - p1.getY());
        w = (int)(p2.getX() - p1.getX());

        Rectangle mapRect = new Rectangle((int)p1.getX(), (int)p1.getY(), w, h);
        
        return mapRect.intersects(imageRect);
    }

}


