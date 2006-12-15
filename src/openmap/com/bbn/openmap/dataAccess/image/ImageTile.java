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
// $Revision: 1.2 $
// $Date: 2006/12/15 18:28:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.image;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.cacheHandler.CacheHandler;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * This is an extension to OMRaster that automatically scales itelf to match the
 * current projection. It is only lat/lon based, and takes the coordinates of
 * the upper left and lower right corners of the image. It does straight scaling -
 * it does not force the image projection to match the map projection! So, your
 * mileage may vary - you have to understand the projection of the image, and
 * know how it fits the projection type of the map. Of course, at larger scales,
 * it might not matter so much.
 * 
 * This class was inspired by, and created from parts of the ImageLayer
 * submission from Adrian Lumsden@sss, on 25-Jan-2002. Used the scaling and
 * trimming code from that submission. That code was also developed with
 * assistance from Steve McDonald at SiliconSpaceships.com.
 * 
 * @see OMRaster
 * @see OMRasterObject
 */
public class ImageTile extends OMScalingRaster implements Serializable {

    protected ImageDecoder imageDecoder;
    protected CacheHandler cache;

    /**
     * Constuct a blank OMRaster, to be filled in with set calls. Make sure you
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
     * @param ii ImageIcon used for the image.
     */
    public ImageTile(float ullat, float ullon, float lrlat, float lrlon,
            ImageDecoder imageDecoder, CacheHandler cache) {
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
     * @param ii ImageIcon used for the image.
     */
    public ImageTile(float ullat, float ullon, float lrlat, float lrlon,
            BufferedImage image) {
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
     * No op for this class, can be use to manage image use for subclasses.
     * Called from within generate.
     * 
     * @param proj current projection.
     * @return false if the rest of generate() should be skipped.
     */
    protected boolean updateImageForProjection(Projection proj) {
        // point1 and point2 are already set in position()

        if (imageDecoder != null) {
            if (!isOnMap(proj)) {
                bitmap = null;
                sourceImage = null;
                setNeedToRegenerate(true);
                setShape(null);
                return false;
            }

            // Check the scale against the cache to see if we should do anything.
            if (cache instanceof Cache
                    && !((Cache) cache).shouldFetchForScale(proj.getScale())) {
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

    public ImageDecoder getImageDecoder() {
        return imageDecoder;
    }

    public void setImageDecoder(ImageDecoder imageDecoder) {
        this.imageDecoder = imageDecoder;
    }

    public static class Cache extends CacheHandler {

        protected float cutoffScale = 1000000;

        public Cache() {
            super(10);
        }

        public Cache(int maxSize) {
            super(maxSize);
        }

        public void setCutoffScale(float scale) {
            cutoffScale = scale;
        }

        public float getCutoffScale() {
            return cutoffScale;
        }

        public boolean shouldFetchForScale(float scale) {
            return scale <= cutoffScale;
        }

        /**
         * Returns a CacheObject that will be loaded into the cache. The key
         * should be an ImageDecoder, and the object in the cache object will be
         * the BufferedImage that will get inserted into the ImageTile.
         */
        public CacheObject load(Object key) {

            try {
                if (key instanceof ImageDecoder) {
                    // URL imageURL = PropUtils.getResourceOrFileOrURL(key);
                    //
                    // FileCacheImageInputStream fciis = new
                    // FileCacheImageInputStream(imageURL.openStream(), null);
                    // BufferedImage fileImage = ImageIO.read(fciis);

                    BufferedImage fileImage = ((ImageDecoder) key).getBufferedImage();

                    return new CacheObject(key, fileImage);
                }

            } catch (Exception e) {
            } // Catch errors

            return null;
        }

    }

}
