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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfSubframe.java,v $
// $RCSfile: RpfSubframe.java,v $
// $Revision: 1.6 $
// $Date: 2006/10/04 14:46:13 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * Some of the ideas for this code is based on source code provided by
 * The MITRE Corporation, through the browse application source code.
 * Many thanks to Nancy Markuson who provided BBN with the software,
 * and to Theron Tock, who wrote the software, and Daniel Scholten,
 * who revised it - (c) 1994 The MITRE Corporation for those parts,
 * and used with permission.  Namely, the subframe caching mechanism
 * is the part that has been modified.
 */

package com.bbn.openmap.layer.rpf;

import java.awt.Color;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.omGraphics.OMWarpingImage;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.util.DataBounds;

/**
 * The RpfSubframe is a holder for images and attributes within the cache.
 */
public class RpfSubframe {
    /**
     * The version of the subframe, used by the cache for marking how recently
     * used the subframe was.
     */
    public byte version;
    /** The pointers to the surrounding subframes in the cache. */
    public int nextSubframe, prevSubframe;
    /** The original pixel size of RPF Subframes. */
    public final static int PIXEL_EDGE_SIZE = 256;
    /** The actual attribute information. */
    protected String data = "";
    /** The opaqueness of the image. */
    protected int opaqueness;
    protected DataBounds bounds = null;
    /**
     * The object that creates the image from either pixels or bytes and colors.
     * Also handles selecting warping or scaling images based on the projection
     * type.
     */
    protected ImageCreator imageCreator = null;

    public RpfSubframe() {
    }

    /**
     * Set the lat/lon of the frame and attribute text.
     * 
     * @param ulat latitude of upper left point, in decimal degrees.
     * @param wlon longitude of upper left point, in decimal degrees.
     * @param llat latitude of lower right point, in decimal degrees.
     * @param elon longitude of lower right point, in decimal degrees.
     */
    public void setLocation(double ulat, double wlon, double llat, double elon) {
        bounds = new DataBounds();
        bounds.add(wlon, ulat);
        bounds.add(elon, llat);
    }

    /**
     * setScalingTo has to be called after this for the changes to take place,
     * or else you need to call the information.setData() methods directly.
     */
    public void setAttributeText(String text) {
        data = text;
    }

    /**
     * getAttributeText retrieves the text that would be displayed as attribute
     * information about the subframe.
     */
    public String getAttributeText() {
        return data;
    }

    public void setPixels(int[] pixels) {
        imageCreator = new Pixels(pixels);
    }

    public void setBitsAndColors(byte[] bits, Color[] colors) {
        imageCreator = new BitsAndColors(bits, colors);
    }

    public void setTransparent(int opaqueness) {
        if (this.opaqueness != opaqueness) {
            this.opaqueness = opaqueness;
            imageCreator.setTransparent(opaqueness);
        }
        // Check new setting vs old - if changed, then set new member variable
        // value, &
        // setNeedToRegenerate(true);
    }

    public OMGraphic getImage(Projection proj) {
        if (imageCreator != null) {
            return imageCreator.getImage(proj);
        }
        return null;
    }

    public abstract class ImageCreator {
        /**
         * The OMGraphic holding the image.
         */
        OMGraphic image;

        /**
         * Get the proper OMGraphic given the projection type.
         * 
         * @param proj
         * @return a projected OMGraphic for the image.
         */
        protected abstract OMGraphic getImage(Projection proj);

        /**
         * Set the transparent color index or opaqueness setting, depending on
         * color model.
         * 
         * @param opaqueness
         */
        protected abstract void setTransparent(int opaqueness);

    }

    /**
     * Direct colormodel implementation.
     * 
     * @author ddietrick
     */
    public class Pixels
            extends ImageCreator {

        int[] pixels = null;

        protected Pixels(int[] pixels) {
            this.pixels = pixels;
        }

        protected void setTransparent(int opaqueness) {
            if (pixels != null) {
                for (int i = 0; i < pixels.length; i++) {
                    pixels[i] = (0x00FFFFFF & pixels[i]) | (opaqueness << 24);
                }
                // image = null; ??
            }
        }

        protected OMGraphic getImage(Projection proj) {

            boolean scaling = proj instanceof EqualArc;

            if (bounds == null || pixels == null) {
                return null;
            }

            if (image == null) {
                if (scaling) {
                    image =
                            new OMScalingRaster(bounds.getMax().getY(), bounds.getMin().getX(), bounds.getMin().getY(),
                                                bounds.getMax().getX(), PIXEL_EDGE_SIZE, PIXEL_EDGE_SIZE, pixels);
                } else {
                    image = new OMWarpingImage(pixels, PIXEL_EDGE_SIZE, PIXEL_EDGE_SIZE, new LatLonGCT(), bounds);
                }
            } else {
                if (scaling) {
                    if (!(image instanceof OMScalingRaster)) {
                        image =
                                new OMScalingRaster(bounds.getMax().getY(), bounds.getMin().getX(), bounds.getMin().getY(),
                                                    bounds.getMax().getX(), PIXEL_EDGE_SIZE, PIXEL_EDGE_SIZE, pixels);
                    }
                } else {
                    if (image instanceof OMScalingRaster) {
                        image = new OMWarpingImage((OMScalingRaster) image, null);
                    }
                }
            }

            image.setSelectPaint(Color.yellow);
            image.generate(proj);
            if (data != null) {
                image.putAttribute(OMGraphic.TOOLTIP, data);
            }

            return image;
        }

    }

    /**
     * Indexed colormodel implementation.
     * 
     * @author ddietrick
     */
    public class BitsAndColors
            extends ImageCreator {

        byte[] bits = null;
        Color[] colors = null;

        protected BitsAndColors(byte[] bits, Color[] colors) {
            this.bits = bits;
            this.colors = colors;
        }

        protected void setTransparent(int opaqueness) {
            // setTransparent has to be set on the resulting OMScalingRaster and
            // regenerated
        }

        protected OMGraphic getImage(Projection proj) {
            boolean scaling = proj instanceof EqualArc;

            if (bounds == null || bits == null || colors == null) {
                return null;
            }

            if (image == null) {
                image =
                        new OMScalingRaster(bounds.getMax().getY(), bounds.getMin().getX(), bounds.getMin().getY(), bounds.getMax()
                                                                                                                          .getX(),
                                            PIXEL_EDGE_SIZE, PIXEL_EDGE_SIZE, bits, colors, opaqueness);
                image.generate(proj);
                if (!scaling) {
                    image = new OMWarpingImage((OMScalingRaster) image, null);
                }
            } else {
                if (scaling) {
                    if (!(image instanceof OMScalingRaster)) {
                        image =
                                new OMScalingRaster(bounds.getMax().getY(), bounds.getMin().getX(), bounds.getMin().getY(),
                                                    bounds.getMax().getX(), PIXEL_EDGE_SIZE, PIXEL_EDGE_SIZE, bits, colors,
                                                    opaqueness);
                    }
                } else {
                    if (image instanceof OMScalingRaster) {
                        image = new OMWarpingImage((OMScalingRaster) image, null);
                    }
                }
            }
            image.setSelectPaint(Color.yellow);
            image.generate(proj);
            if (data != null) {
                image.putAttribute(OMGraphic.TOOLTIP, data);
            }

            return image;
        }
    }
}
