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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/image/ImageTile.java,v $
// $RCSfile: ImageTile.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.image;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * This is an extension to OMRaster that automatically scales itself to match
 * the current projection. It is only lat/lon based, and takes the coordinates
 * of the upper left and lower right corners of the image. It does straight
 * scaling - it does not force the image projection to match the map projection!
 * So, your mileage may vary - you have to understand the projection of the
 * image, and know how it fits the projection type of the map. Of course, at
 * larger scales, it might not matter so much.
 * 
 * This class was inspired by, and created from parts of the ImageLayer
 * submission from Adrian Lumsden@sss, on 25-Jan-2002. Used the scaling and
 * trimming code from that submission. That code was also developed with
 * assistance from Steve McDonald at SiliconSpaceships.com.
 * 
 * @see com.bbn.openmap.omGraphics.OMRaster
 * @see com.bbn.openmap.omGraphics.OMRasterObject
 */
public class ImageTile extends OMScalingRaster implements Serializable {

    protected ImageReader imageDecoder;
    protected CacheHandler cache;

    /**
     * Construct a blank OMRaster, to be filled in with set calls. Make sure you
     * set either a source image or ImageDecoder that knows how to get the
     * image.
     */
    public ImageTile() {
        super();
    }

    // //////////////////////////////////// IMAGEICON

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon.
     * 
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param imageDecoder ImageReader for Image.
     * @param cache CacheHandler holding cached images.
     */
    public ImageTile(double ullat, double ullon, double lrlat, double lrlon,
            ImageReader imageDecoder, CacheHandler cache) {
        super();
        setRenderType(OMGraphic.RENDERTYPE_LATLON);
        setColorModel(COLORMODEL_IMAGEICON);

        lat = ullat;
        lon = ullon;
        lat2 = lrlat;
        lon2 = lrlon;

        this.imageDecoder = imageDecoder;
        this.cache = cache;
    }

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon.
     * 
     * @param ullat latitude of the top of the image.
     * @param ullon longitude of the left side of the image.
     * @param lrlat latitude of the bottom of the image.
     * @param lrlon longitude of the right side of the image.
     * @param image BufferedImage used for the image.
     */
    public ImageTile(double ullat, double ullon, double lrlat, double lrlon, BufferedImage image) {
        super(ullat, ullon, lrlat, lrlon, image);
    }

    protected Boolean realSelection = null;
    public static Color DEFAULT_NON_CLEAR_FILL_PAINT = new Color(200, 200, 200, 100);

    public void setSelected(boolean setting) {
        if (realSelection != null) {
            realSelection = Boolean.valueOf(setting);

            if (setting) {
                displayPaint = getSelectPaint();
            } else {
                displayPaint = getLinePaint();
            }

        } else {
            super.setSelected(setting);
        }
    }

    /**
     * Checking to see of the image needs to be updated for the projection
     * parameters, namely scale.
     * 
     * @param proj current projection
     * @return true if the image scale, as projected, isn't being shrunk down
     *         too much, and the image should be displayed.
     */
    protected boolean shouldFetchForProjection(Projection proj) {
        Point2D anchor1 = new Point2D.Double(lat, lon);
        Point2D anchor2 = new Point2D.Double(lat2, lon2);

        float imageScale = com.bbn.openmap.proj.ProjMath.getScale(anchor1, anchor2, proj);

        float scaleRatio = Cache.DEFAULT_SCALE_RATIO; // Something somewhat
                                                      // reasonable, a default.
        if (cache instanceof Cache) {
            scaleRatio = ((Cache) cache).getCutoffScaleRatio();
        }

        return (imageScale * scaleRatio) <= proj.getScale();
    }

    /**
     * Called from within generate.
     * 
     * @param proj current projection.
     * @return false if the rest of generate() should be skipped, if the image
     *         doesn't need to be formed for the current projection.
     */
    protected boolean updateImageForProjection(Projection proj) {
        // point1 and point2 are not yet set for a changed projection
        position(proj);
        
        if (imageDecoder != null) {
            if (!isOnMap(proj)) {
                bitmap = null;
                sourceImage = null;
                setNeedToRegenerate(true);
                return false;
            }

            // Check the scale against the cache to see if we should do
            // anything.
            if (shouldFetchForProjection(proj)) {
                bitmap = null;

                if (realSelection == null) {
                    if (getFillPaint() == com.bbn.openmap.omGraphics.OMColor.clear) {
                        setFillPaint(DEFAULT_NON_CLEAR_FILL_PAINT);
                    }
                    realSelection = Boolean.valueOf(selected);
                }
                selected = true;
                setShape();
                setNeedToRegenerate(false);
                return false;
            } else if (realSelection != null) {
                if (getFillPaint() == DEFAULT_NON_CLEAR_FILL_PAINT) {
                    setFillPaint(com.bbn.openmap.omGraphics.OMColor.clear);
                }
                setFillPaint(com.bbn.openmap.omGraphics.OMColor.clear);
                selected = realSelection.booleanValue();
                realSelection = null;
            }

            if (sourceImage == null) {
                if (cache != null) {
                    setImage((Image) cache.get(imageDecoder));
                } else {
                    setImage(imageDecoder.getBufferedImage());
                }
            }
        }
        return true;
    }

    public boolean regenerate(Projection p) {
        return generate(p);
    }

    public ImageReader getImageDecoder() {
        return imageDecoder;
    }

    public void setImageDecoder(ImageReader imageDecoder) {
        this.imageDecoder = imageDecoder;
    }

    public static class Cache extends CacheHandler {

        public final static float DEFAULT_SCALE_RATIO = 5f;

        protected float cutoffScaleRatio = DEFAULT_SCALE_RATIO;

        public Cache() {
            super(10);
        }

        public Cache(int maxSize) {
            super(maxSize);
        }

        public void setCutoffScaleRatio(float scale) {
            cutoffScaleRatio = scale;
        }

        public float getCutoffScaleRatio() {
            return cutoffScaleRatio;
        }

        /**
         * Returns a CacheObject that will be loaded into the cache. The key
         * should be an ImageDecoder, and the object in the cache object will be
         * the BufferedImage that will get inserted into the ImageTile.
         */
        public CacheObject load(Object key) {

            try {
                if (key instanceof ImageReader) {
                    // URL imageURL = PropUtils.getResourceOrFileOrURL(key);
                    //
                    // FileCacheImageInputStream fciis = new
                    // FileCacheImageInputStream(imageURL.openStream(), null);
                    // BufferedImage fileImage = ImageIO.read(fciis);

                    BufferedImage fileImage = ((ImageReader) key).getBufferedImage();

                    return new CacheObject(key, fileImage);
                }

            } catch (Exception e) {
            } // Catch errors

            return null;
        }

    }

}
