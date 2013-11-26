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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMScalingRaster.java,v $
// $RCSfile: OMScalingRaster.java,v $
// $Revision: 1.16 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.util.ImageWarp;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.util.DataBounds;

/**
 * This is an extension to OMRaster that automatically scales itelf to match the
 * current projection. It is only lat/lon based, and takes the coordinates of
 * the upper left and lower right corners of the image. It does straight scaling
 * - it does not force the image projection to match the map projection! So,
 * your mileage may vary - you have to understand the projection of the image,
 * and know how it fits the projection type of the map. Of course, at larger
 * scales, it might not matter so much.
 * 
 * This class was inspired by, and created from parts of the ImageLayer
 * submission from Adrian Lumsden@sss, on 25-Jan-2002. Used the scaling and
 * trimming code from that submission. That code was also developed with
 * assistance from Steve McDonald at SiliconSpaceships.com.
 * 
 * @see OMRaster
 * @see OMRasterObject
 */
public class OMScalingRaster extends OMRaster implements Serializable {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    /**
     * The latitude of the lower right corner for the image, in decimal degrees.
     */
    protected double lat2 = 0.0f;

    /**
     * The longitude of the lower right corner for the image, in decimal
     * degrees.
     */
    protected double lon2 = 0.0f;

    /**
     * This the original version of the image, which we keep around for
     * rescaling later.
     */
    protected transient BufferedImage sourceImage = null;

    /**
     * The rectangle in screen co-ordinates that the scaled image projects to
     * after clipping.
     */
    protected transient Rectangle clipRect;

    protected transient ArrayList<float[]> corners;

    protected int scaleTransformType = AffineTransformOp.TYPE_BILINEAR;

    /**
     * This lastProjection is used to keep track of the last projection used to
     * warp or scale the image, an used during the rendering process to check if
     * we should rework the image to be displayed.
     */
    protected Projection lastProjection = null;

    /**
     * Construct a blank OMRaster, to be filled in with set calls.
     */
    public OMScalingRaster() {
        super();
    }

    // /////////////////////////////////// INT PIXELS - DIRECT
    // COLORMODEL

    /**
     * Creates an OMRaster images, Lat/Lon placement with a direct colormodel
     * image.
     * 
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     */
    public OMScalingRaster(double ullat, double ullon, double lrlat, double lrlon, int w, int h,
            int[] pix) {

        super(ullat, ullon, w, h, pix);
        lat2 = lrlat;
        lon2 = lrlon;
    }

    // //////////////////////////////////// IMAGEICON

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon.
     * 
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param ii ImageIcon used for the image.
     */
    public OMScalingRaster(double ullat, double ullon, double lrlat, double lrlon, ImageIcon ii) {
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
    public OMScalingRaster(double ullat, double ullon, double lrlat, double lrlon, Image ii) {
        super();
        setRenderType(OMGraphic.RENDERTYPE_LATLON);
        setColorModel(COLORMODEL_IMAGEICON);

        lat = ullat;
        lon = ullon;
        lat2 = lrlat;
        lon2 = lrlon;
        setImage(ii);
    }

    // //////////////////////////////////// BYTE PIXELS with
    // COLORTABLE

    /**
     * Lat/Lon placement with a indexed colormodel, which is using a colortable
     * and a byte array to construct the int[] pixels.
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
    public OMScalingRaster(double ullat, double ullon, double lrlat, double lrlon, int w, int h,
            byte[] bytes, Color[] colorTable, int trans) {

        super(ullat, ullon, w, h, bytes, colorTable, trans);
        lat2 = lrlat;
        lon2 = lrlon;
    }

    /**
     * Creates a BufferedImage version of the image. A new BufferedImage object
     * is created, and the image is copied into it. You can get rid of the input
     * image after calling this method. The OMRaster variables height, width and
     * bitmap are set here to the values for the new BufferedImage.
     * 
     * @param image the input image.
     */
    public void setImage(Image image) {
        if (DEBUG) {
            logger.fine("OMScalingRaster.setImage: " + image);
        }

        /**
         * Oh, don't do this. The image is created from the colortable and pixel
         * version, too, and setting the color model to IMAGEICON will cause any
         * updates to not take hold.
         */
        // setColorModel(COLORMODEL_IMAGEICON);

        if (image == null) {
            bitmap = null;
            sourceImage = null;
            return;
        }

        if (!(image instanceof BufferedImage)) {
            int w = image.getWidth(this);
            int h = image.getHeight(this);
            if (w <= 0) {
                w = width;
            }
            if (h <= 0) {
                h = height;
            }

            sourceImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2D = sourceImage.createGraphics();
            g2D.drawImage(image, 0, 0, this);
        } else {

            // null check above
            if (image.equals(sourceImage)) {
                // Nothing needs to be done.
                return;
            }

            sourceImage = (BufferedImage) image;
        }

        width = sourceImage.getWidth();
        height = sourceImage.getHeight();

        setNeedToRegenerate(true);
        // Just in case rendering tries to happen.
        bitmap = sourceImage;
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
            if (DEBUG) {
                logger.fine("OMScalingRaster: null projection in position!");
            }
            return false;
        }

        point1 = (Point) proj.forward(lat, lon, new Point());
        point2 = (Point) proj.forward(lat2, lon2, new Point());

        corners = null;

        if (point1.x > point2.x) {

            double[] coords = new double[] { lat, lon, lat, lon2, lat2, lon2, lat2, lon, lat, lon };

            if (proj instanceof GeoProj) {
                corners = ((GeoProj) proj).forwardPoly(ProjMath.arrayDegToRad(coords), OMGraphic.LINETYPE_STRAIGHT, -1, true);
            } else {
                corners = proj.forwardPoly(coords, true);
            }

            if (corners != null && corners.size() > 2) {
                float[] xs = corners.get(0);
                float[] ys = corners.get(1);
                point1.setLocation(xs[0], ys[0]);
                point2.setLocation(xs[2], ys[2]);
            }
        }

        setNeedToReposition(false);
        return true;
    }

    /**
     * Prepare the graphics for rendering. For all image types, it positions the
     * image relative to the projection. For direct and indexed colormodel
     * images, it creates the ImageIcon used for drawing to the window (internal
     * to object). For indexed colormodel images, it also calls computePixels,
     * to resolve the colortable and the bytes to create the image pixels.
     * 
     * @param proj Projection used to position the image on the window.
     * @return true if the image is ready to paint.
     */
    public boolean generate(Projection proj) {

        if (!updateImageForProjection(proj)) {

            if (getNeedToReposition()) {
                position(proj);
                setShape();
            } else {
                // Nothing changed with image placement, image is ready, we can
                // return at this point.
                setShape();
                setNeedToRegenerate(false);
                return true;
            }
        }

        setShape(null);

        // Position sets the position for the OMRaster!!!!
        if (!position(proj)) {
            if (DEBUG) {
                logger.fine("OMRaster.generate(): positioning failed!");
            }
            return false;
        }

        if (colorModel != COLORMODEL_IMAGEICON) {
            // If the sourceImage hasn't been created, and needs to
            // be, then just do what we normally do in OMRaster.
            if (sourceImage == null || getNeedToRegenerate()) {
                if (DEBUG) {
                    logger.fine("OMScalingRaster: generating image");
                }
                super.generate(proj);
                // bitmap is set to a BufferedImage
                setImage(bitmap);

                // Since we have a source image that is going to be reused,
                // let's get rid of the memory that we won't use anymore.
                pixels = null;
                bits = null;
            }
        }

        // point1 and point2 are already set in position()

        // We assume that the image doesn't cross the dateline, and
        // that p1 is upper left corner, and p2 is lower right.
        // scaleTo modifies the internal bitmap image for display.
        scaleTo(proj);

        if (bitmap != null) {
            if (corners == null) {
                int w = bitmap.getWidth(this);
                int h = bitmap.getHeight(this);
                setShape(createBoxShape(point1.x, point1.y, w, h));
            } else {
                int numRects = corners.size();
                GeneralPath projectedShape = null;
                for (int i = 0; i < numRects; i += 2) {
                    GeneralPath gp = createShape(corners.get(i), corners.get(i + 1), true);

                    projectedShape = appendShapeEdge(projectedShape, gp, false);
                }
                setShape(projectedShape);
            }
            
            setNeedToRegenerate(false);
        } else {
            // Make the label go away if it is off-screen.
            hasLabel = false;
        }
        return true;
    }

    /**
     * Called from within generate. Some render buffering calls generate to make
     * sure the latest projection is called on an OMGraphic before it's put into
     * a buffer. We're keeping track of the last projection used to generate the
     * warped image, and if it's the same, don't bother regenerating, use the
     * raster we have. This method is a question: do we need to update the image
     * because of a projection change?
     * 
     * @param proj current projection.
     * @return false if the projection shouldn't cause anything to change for
     *         the image.
     */
    protected boolean updateImageForProjection(Projection proj) {
        boolean projUnchanged = proj.equals(lastProjection);
        boolean ret = bitmap != null && projUnchanged && !getNeedToRegenerate();
        if (!projUnchanged) {
            lastProjection = proj.makeClone();
        }
        return !ret;
    }

    /**
     * Since the OMScalingRaster changes height and width depending on scale, we
     * need to rotate the image over that point and factor in the scaled height
     * and width of the image. Called from within OMRasterObject.render().
     */
    protected void rotate(Graphics2D g) {
        int rotOffsetX = point1.x + (point2.x - point1.x) / 2;
        int rotOffsetY = point1.y + (point2.y - point1.y) / 2;
        ((Graphics2D) g).rotate(rotationAngle, rotOffsetX, rotOffsetY);
    }

    /**
     * Take the current projection and the sourceImage, and make the image that
     * gets displayed fit the projection. If the source image isn't over the
     * map, then this OMGraphic is set to be invisible. If part of the image is
     * on the map, only that part is used. The OMRaster bitmap variable is set
     * with an image that is created from the source image, and the point1
     * variable is set to the point where the image should be placed. For
     * instance, if the source image upper left corner is off the map to the
     * NorthWest, then the OMRaster bitmap is set to a image, clipped from the
     * source, that is entirely on the map. The OMRaster point1 is set to 0, 0,
     * since that is where the clipped image should be placed.
     * 
     * @param thisProj the projection that the image should be scaled to.
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

        // Get the projection window rectangle in pix
        Rectangle winRect = new Rectangle(thisProj.getWidth(), thisProj.getHeight());
        // Get image projection rectangle
        Rectangle projRect = new Rectangle();
        projRect.setLocation(point1);
        projRect.setSize(point2.x - point1.x, point2.y - point1.y);

        Rectangle sourceRect = new Rectangle();
        sourceRect.width = sourceImage.getWidth();
        sourceRect.height = sourceImage.getHeight();

        // Now we have everything we need to sort out this new projection.
        // boolean currentVisibility = isVisible();

        // Assume we will not see it, in order to see if any part of
        // the image will appear on map. If so, then reset visibility to
        // what's needed.
        // setVisible(false);
        clipRect = null;

        Rectangle iRect = projRect;
        // <= 2 is limiting this intersection to regular world - small world
        // will have multiple rects, corners We don't want to clip the bitmap
        // if we have to draw it on different parts of the map window (if it
        // wraps).
        if (corners == null || corners.size() <= 2) {
            iRect = winRect.intersection(projRect);
        }

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
                    // calc X scale factor
                    double xScaleFactor = (double) sourceRect.width / (double) projRect.width;
                    // and Y scale factor
                    double yScaleFactor = (double) sourceRect.height / (double) projRect.height;
                    // and the x offset
                    int xOffset = iRect.x - projRect.x;
                    // the y offset
                    int yOffset = iRect.y - projRect.y;
                    // Scale the x position
                    clipRect.x = (int) Math.floor(xOffset * xScaleFactor);
                    // scale the y position
                    clipRect.y = (int) Math.floor(yOffset * yScaleFactor);

                    // Do Math.ceil because the icon was getting
                    // clipped a little if it started to move off the
                    // screen a little.
                    clipRect.width = (int) Math.ceil(iRect.width * xScaleFactor); // scale
                    // the width
                    clipRect.height = (int) Math.ceil(iRect.height * yScaleFactor); // scale
                    // the height

                    // Make sure the rounding doesn't exceed the
                    // original icon bounds
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
                // Now we can grab the bit we want out of the source
                // and scale it to fit the intersection.

                // Calc width adjustment
                double widthAdj = (double) iRect.width / (double) clipRect.width;
                // Calc height adjustment
                double heightAdj = (double) iRect.height / (double) clipRect.height;
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
                    BufferedImage newImage = xformOp.filter(sourceImage.getSubimage(clipRect.x, clipRect.y, clipRect.width, clipRect.height), null);

                    bitmap = newImage;
                    point1.setLocation(iRect.x, iRect.y);
                    // setVisible(currentVisibility);
                } catch (IllegalArgumentException iae) {
                    // This has been kicked off when the dimensions of the
                    // filter get too big. Treat it like the height and width
                    // being set to -1.
                    logger.fine("Caught IllegalArgumentException: " + iae.getMessage());
                    bitmap = null;
                } catch (OutOfMemoryError oome) {
                    // This sometimes happens on startup, but rarely. The size
                    // of the DataBuffer created from the filter causes the
                    // error to be thrown. We never see the effect of the error
                    // in the application, however - the application continues
                    // and recovers. I have a feeling its a result of the
                    // startup order, and the projection isn't quite right yet.
                    logger.fine("Caught OutOfMemoryException, setting bitmap to null");
                    bitmap = null;
                } catch (NegativeArraySizeException nase) {
                    logger.fine("Caught OutOfMemoryException, setting bitmap to null");
                    bitmap = null;
                } catch (NullPointerException npe) {
                    logger.fine("Caught NPE, setting bitmap to null");
                    bitmap = null;
                } catch (RasterFormatException rfe) {
                    logger.fine("Caught RasterFormatException, setting bitmap to null");
                }
            }
        } else {
            bitmap = null;
        }
    }

    /**
     * Render the raster on the java.awt.Graphics. Overrides the raster method
     * because it checks to see if the raster is in a small-world situation,
     * where the image must wrap around the world.
     * 
     * @param graphics java.awt.Graphics to draw the image on.
     */
    public void render(Graphics graphics) {

        if (getNeedToRegenerate() || getNeedToReposition() || !isVisible()) {
            return;
        }

        boolean smallWorld = bitmap != null && corners != null && corners.size() >= 4;

        if (smallWorld) {
            float[] xs = corners.get(2);
            float[] ys = corners.get(3);
            Point point1 = new Point();
            point1.setLocation((double) xs[0], (double) ys[0]);
            Point point2 = new Point();
            point2.setLocation((double) xs[2], (double) ys[2]);

            // copy the graphic, so our transform doesn't cascade to
            // others...
            Graphics g = graphics.create();
            if (g instanceof Graphics2D && rotationAngle != DEFAULT_ROTATIONANGLE) {
                // rotate about our image center point
                rotate((Graphics2D) g);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("OMRasterObject.render() | drawing " + width + "x" + height
                        + " image at " + point1.x + ", " + point1.y);
            }

            if (g instanceof Graphics2D && bitmap instanceof RenderedImage) {
                // Affine translation for placement...
                ((Graphics2D) g).drawRenderedImage((RenderedImage) bitmap, new AffineTransform(1f, 0f, 0f, 1f, point1.x, point1.y));
            } else {
                g.drawImage(bitmap, point1.x, point1.y, this);
            }
        }

        // render the location that is always set.
        super.render(graphics);
    }

    /**
     * Return the rectangle in screen co-ordinates that the scaled image has
     * been clipped to. This may return a null rectangle (i.e. the image is out
     * of the window). Otherwise the returned rectangle should always at least
     * partially lie within the bounds of the window.
     */
    public Rectangle getClippedRectangle() {
        return clipRect;
    }

    /**
     * Change the upper latitude attribute.
     * 
     * @param value latitude in decimal degrees.
     */
    public void setULLat(double value) {
        setLat(value);
    }

    /**
     * Get the upper latitude.
     * 
     * @return the latitude in decimal degrees.
     */
    public double getULLat() {
        return getLat();
    }

    /**
     * Change the western longitude attribute.
     * 
     * @param value the longitude in decimal degrees.
     */
    public void setULLon(double value) {
        setLon(value);
    }

    /**
     * Get the western longitude.
     * 
     * @return longitude in decimal degrees.
     */
    public double getULLon() {
        return getLon();
    }

    /**
     * Change the southern latitude attribute.
     * 
     * @param value latitude in decimal degrees.
     */
    public void setLRLat(double value) {
        if (lat2 == value)
            return;
        lat2 = value;
        setNeedToReposition(true);
    }

    /**
     * Get the southern latitude.
     * 
     * @return the latitude in decimal degrees.
     */
    public double getLRLat() {
        return lat2;
    }

    /**
     * Change the eastern longitude attribute.
     * 
     * @param value the longitude in decimal degrees.
     */
    public void setLRLon(double value) {
        if (lon2 == value)
            return;
        lon2 = value;
        setNeedToReposition(true);
    }

    /**
     * Get the eastern longitude.
     * 
     * @return longitude in decimal degrees.
     */
    public double getLRLon() {
        return lon2;
    }

    /**
     * Set the rectangle, based on the location and size of the image after
     * scaling.
     */
    public void setShape() {
        if (point2 != null && point1 != null) {
            // generate shape that is a boundary of the generated image.
            // We'll make it a GeneralPath rectangle.
            int w = point2.x - point1.x;
            int h = point2.y - point1.y;

            setShape(createBoxShape(point1.x, point1.y, w, h));
        }
    }

    public boolean isOnMap(Projection proj) {
        Point2D p1 = proj.forward(lat, lon);
        Point2D p2 = proj.forward(lat2, lon2);
        int h = (int) Math.abs(p2.getY() - p1.getY());
        int w = (int) Math.abs(p2.getX() - p1.getX());

        Rectangle imageRect = new Rectangle((int) p1.getX(), (int) p1.getY(), w, h);

        proj.forward(proj.getUpperLeft(), p1);
        proj.forward(proj.getLowerRight(), p2);
        h = (int) Math.abs(p2.getY() - p1.getY());
        w = (int) Math.abs(p2.getX() - p1.getX());

        Rectangle mapRect = new Rectangle((int) p1.getX(), (int) p1.getY(), w, h);

        return mapRect.intersects(imageRect);
    }

    public int getScaleTransformType() {
        return scaleTransformType;
    }

    /**
     * Set the AffineTransformOp used for scaling images. Default is
     * AffineTransformOp.TYPE_BILINEAR. Can also be
     * AffineTransformOp.TYPE_BICUBIC or
     * AffineTransformOp.TYPE_NEAREST_NEIGHBOR.
     * 
     * @param scaleTransformType
     */
    public void setScaleTransformType(int scaleTransformType) {
        if (scaleTransformType == AffineTransformOp.TYPE_BILINEAR
                || scaleTransformType == AffineTransformOp.TYPE_BICUBIC
                || scaleTransformType == AffineTransformOp.TYPE_NEAREST_NEIGHBOR) {
            this.scaleTransformType = scaleTransformType;
        }
    }

    /**
     * Creates an ImageWarp object from the contents of the OMScalingRaster.
     * This can be used in an OMWarpingImage to be used for display in
     * projections that don't match the raster's projection.
     * 
     * @param transform the OMScalingImage assumes that the coordinates/pixel
     *        transformation of the image is equal arc. If it's not, the correct
     *        transformation should be provided for this query. The
     *        OMScalingRaster doesn't really know what it is, it just plots the
     *        corner coordinates and scales the image accordingly.
     * @return ImageWarp an ImageWarp if all the required information was
     *         provided, null if not.
     */
    public ImageWarp getImageWarp(GeoCoordTransformation transform) {
        ImageWarp imageWarp = null;
        Image image = sourceImage;

        if (image != null) {
            DataBounds imageBounds = new DataBounds();
            imageBounds.add(lon, lat);
            imageBounds.add(lon2, lat2);

            if (transform == null) {
                transform = new LatLonGCT();
            }

            BufferedImage bi = null;
            if (image instanceof BufferedImage) {
                bi = (BufferedImage) image;
            } else {
                bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                bi.getGraphics().drawImage(image, 0, 0, null);
            }

            imageWarp = new ImageWarp(bi, transform, imageBounds);
        }

        return imageWarp;
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMScalingRaster) {
            OMScalingRaster omsr = (OMScalingRaster) source;
            this.lat2 = omsr.lat2;
            this.lon2 = omsr.lon2;
            this.scaleTransformType = omsr.scaleTransformType;
            // OK, OK, I know this isn't a deep copy. TODO
            this.sourceImage = omsr.sourceImage;
        }
    }
}
