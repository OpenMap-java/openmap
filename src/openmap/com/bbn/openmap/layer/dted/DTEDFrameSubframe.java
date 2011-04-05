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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrameSubframe.java,v
// $
// $RCSfile: DTEDFrameSubframe.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:54 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

import java.awt.Color;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMScalingRaster;
import com.bbn.openmap.omGraphics.OMWarpingImage;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.util.DataBounds;

public class DTEDFrameSubframe {
    // Types of slope shading
    /** Empty image. */
    public static final int NOSHADING = 0;
    /** Gray scale slope shading, sun from the Northwest. */
    public static final int SLOPESHADING = 1;
    /** Banded contour coloring, contour based on meters. */
    public static final int METERSHADING = 2;
    /** Banded contour coloring, contour based on feet. */
    public static final int FEETSHADING = 3;
    /** Test markings, for the boundary of the subframe. */
    public static final int BOUNDARYSHADING = 4;
    /**
     * Colorized slope shading. Color bands are based on elevation, and are
     * accented by shaded indications.
     */
    public static final int COLOREDSHADING = 5;
    /** DTED LEVEL 0, 1km posts. */
    public static final int LEVEL_0 = 0;
    /** DTED LEVEL 1, 100m posts. */
    public static final int LEVEL_1 = 1;
    /** DTED LEVEL 2, 30m posts. */
    public static final int LEVEL_2 = 2;
    /** Default height between bands in band views. */
    public static final int DEFAULT_BANDHEIGHT = 25;
    /** Default contrast setting for slope shading. */
    public static final int DEFAULT_SLOPE_ADJUST = 3;

    public DTEDFrameSubframeInfo dfsi;
    protected ImageCreator imageCreator = null;

    public DTEDFrameSubframe(DTEDFrameSubframeInfo info) {
        dfsi = info.makeClone();
    }

    public OMGraphic getImageIfCurrent(Projection proj, DTEDFrameSubframeInfo dfsi) {
        if (dfsi.equals(this.dfsi) && imageCreator != null) {
            return imageCreator.getImage(proj);
        }

        return null;
    }

    // public void finalize(){
    // Debug.message("gc", " DTEDFrameSubframe: getting GC'd");
    // }

    public OMGraphic getImage(Projection proj) {
        if (imageCreator != null) {
            return imageCreator.getImage(proj);
        }
        return null;
    }

    public void setPixels(int[] pixels) {
        imageCreator = new Pixels(pixels);
    }

    public void setBitsAndColors(byte[] bits, Color[] colors) {
        imageCreator = new BitsAndColors(bits, colors);
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

            if (pixels == null || dfsi == null) {
                return null;
            }

            Point2D projOrigin = proj.forward(dfsi.lat, dfsi.lon);
            Point2D otherCorner = new Point2D.Double();
            if (proj instanceof EqualArc) {
                projOrigin.setLocation(projOrigin.getX() + dfsi.width, projOrigin.getY() + dfsi.height);
                proj.inverse(projOrigin, otherCorner);
            } else {
                /*
                 * Working with DTEDCacheHandler to work around non-EqualArc
                 * projection subframe bounds location problems. For those
                 * projection types, making one subframe per frame that covers
                 * the entire degree x degree area.
                 */
                otherCorner.setLocation(dfsi.lon + 1, dfsi.lat - 1);
            }

            if (image == null) {
                if (scaling) {
                    image =
                            new OMScalingRaster(dfsi.lat, dfsi.lon, otherCorner.getY(), otherCorner.getX(), dfsi.width,
                                                dfsi.height, pixels);
                } else {
                    DataBounds bounds =
                            new DataBounds(otherCorner.getX(), otherCorner.getY(), (double) dfsi.lon, (double) dfsi.lat);
                    image = new OMWarpingImage(pixels, dfsi.width, dfsi.height, new LatLonGCT(), bounds);
                }
            } else {
                if (scaling) {
                    if (!(image instanceof OMScalingRaster)) {
                        image =
                                new OMScalingRaster(dfsi.lat, dfsi.lon, otherCorner.getY(), otherCorner.getX(), dfsi.width,
                                                    dfsi.height, pixels);
                    }
                } else {
                    if (image instanceof OMScalingRaster) {
                        image = new OMWarpingImage((OMScalingRaster) image, null);
                    }
                }
            }

            image.generate(proj);

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

            if (bits == null || colors == null || dfsi == null) {
                return null;
            }

            Point2D projOrigin = proj.forward(dfsi.lat, dfsi.lon);
            projOrigin.setLocation(projOrigin.getX() + dfsi.width, projOrigin.getY() + dfsi.height);
            Point2D otherCorner = proj.inverse(projOrigin);

            if (image == null) {
                image =
                        new OMScalingRaster(dfsi.lat, dfsi.lon, otherCorner.getY(), otherCorner.getX(), dfsi.width, dfsi.height,
                                            bits, colors, 255);
                image.generate(proj);
                if (!scaling) {
                    image = new OMWarpingImage((OMScalingRaster) image, null);
                }
            } else {
                if (scaling) {
                    if (!(image instanceof OMScalingRaster)) {
                        image =
                                new OMScalingRaster(dfsi.lat, dfsi.lon, otherCorner.getY(), otherCorner.getX(), dfsi.width,
                                                    dfsi.height, bits, colors, 255);
                    }
                } else {
                    if (image instanceof OMScalingRaster) {
                        image = new OMWarpingImage((OMScalingRaster) image, null);
                    }
                }
            }
            image.generate(proj);

            return image;
        }
    }
}