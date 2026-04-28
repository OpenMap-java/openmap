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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrameSubframeInfo.java,v $
// $RCSfile: DTEDFrameSubframeInfo.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:54 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.util.HashCodeUtil;

/**
 * The DTEDFrameSubframeInfo contains information about how a subframe image
 * ought to be presented. It contains the view settings, and the location of the
 * subframe image.
 */
public class DTEDFrameSubframeInfo {
    /**
     * The type of shading to use in the image. DTEDFrameSubframe has the
     * different types of possibilities.
     */
    public int viewType;
    /**
     * The distance (elevation) range that each color section of the band
     * viewtype represents.
     */
    public int bandHeight;
    /** The DTED level of the data. */
    public int dtedLevel;
    /**
     * A user adjusted setting for contrast for the slope shading. Some areas of
     * the earth require different settings, for more or less detail.
     */
    public int slopeAdjust;
    /**
     * The degrees/pixel of the image in the x direction, derived from the scale
     * of the projection.
     */
    public double xPixInterval;
    /**
     * The degrees/pixel of the image in the y direction, derived from the scale
     * of the projection.
     */
    public double yPixInterval;
    /**
     * x number of the subframe within the DTEDFrame, from the left side of the
     * frame (east).
     */
    public int subx;
    /**
     * y number of the subframe within the DTEDFrame, from the bottom side of
     * the frame (south).
     */
    public int suby;
    /** Longitude of the upper left corner of the subframe image. */
    public float lon;
    /** Latitude of the upper left corner of the subframe image. */
    public float lat;
    /** height of the subframe image. */
    public int height;
    /** Width of the subframe image. */
    public int width;
    /**
     * The colormodel of the images. Use OMRasterObject.COLORMODEL_DIRECT
     * (default) or OMRasterObject.COLORMODEL_INDEXED
     */
    public int colorModel = OMRasterObject.COLORMODEL_DIRECT;

    public DTEDFrameSubframeInfo(int Vt, int Bh, int Dl, int Sa) {
        this(DTEDCacheHandler.SF_PIXEL_HW, DTEDCacheHandler.SF_PIXEL_HW, 0.0f, 0.0f, 0, 0, (double) 0.0, (double) 0.0, Vt, Bh, Dl,
             Sa);
    }

    public DTEDFrameSubframeInfo(int Height, int Width, float Lon, float Lat, int Subx, int Suby, double xpi, double ypi, int Vt,
                                 int Bh, int Dl, int Sa) {
        viewType = Vt;
        bandHeight = Bh;
        dtedLevel = Dl;
        slopeAdjust = Sa;
        xPixInterval = xpi;
        yPixInterval = ypi;
        subx = Subx;
        suby = Suby;
        lon = Lon;
        lat = Lat;
        height = Height;
        width = Width;
    }

    public DTEDFrameSubframeInfo makeClone() {
        return new DTEDFrameSubframeInfo(height, width, lon, lat, subx, suby, xPixInterval, yPixInterval, viewType, bandHeight,
                                         dtedLevel, slopeAdjust);
    }

    /**
     * A comparison test to test the drawing parameters, to figure out if the
     * presentation configuration has changed, and that the pixel color values
     * need to be recalculated.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DTEDFrameSubframeInfo newInfo = (DTEDFrameSubframeInfo) obj;
        boolean result = true;

        return viewType != newInfo.viewType && bandHeight != newInfo.bandHeight && dtedLevel != newInfo.dtedLevel
                && slopeAdjust != newInfo.slopeAdjust && xPixInterval != newInfo.xPixInterval
                && yPixInterval != newInfo.yPixInterval && newInfo.subx == subx && newInfo.suby == suby;
    }

    public int hashCode() {
        int result = HashCodeUtil.SEED;
        // collect the contributions of various fields
        result = HashCodeUtil.hash(result, viewType);
        result = HashCodeUtil.hash(result, bandHeight);
        result = HashCodeUtil.hash(result, dtedLevel);
        result = HashCodeUtil.hash(result, slopeAdjust);
        result = HashCodeUtil.hash(result, xPixInterval);
        result = HashCodeUtil.hash(result, yPixInterval);
        result = HashCodeUtil.hash(result, subx);
        result = HashCodeUtil.hash(result, suby);
        return result;
    }
}