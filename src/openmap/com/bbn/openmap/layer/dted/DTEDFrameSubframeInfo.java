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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrameSubframeInfo.java,v $
// $RCSfile: DTEDFrameSubframeInfo.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.dted;

import java.io.*;
import com.bbn.openmap.omGraphics.OMRasterObject;

/** The DTEDFrameSubframeInfo contains information about how a
* subframe image ought to be presented.  It contains the view
* settings, and the location of the subframe image.
* */
public class DTEDFrameSubframeInfo{
    /** The type of shading to use in the image.  DTEDFrameSubframe
     * has the different types of possibilities.  
     * */
    public int viewType;
    /** The distance (elevation) range that each color section of the
     * band viewtype represents. 
     * */
    public int bandHeight;
    /** The DTED level of the data. */
    public int dtedLevel;
    /** A user adjusted setting for contrast for the slope shading.
     * Some areas of the earth require different settings, for more
     * or less detail. 
     * */
    public int slopeAdjust;
    /** The degrees/pixel of the image in the x direction, derived
     * from the scale of the projection.  
     * */
    public double xPixInterval;
    /** The degrees/pixel of the image in the y direction, derived
     * from the scale of the projection.  
     * */
    public double yPixInterval;
    /** x number of the subframe within the DTEDFrame, from the left
     * side of the frame (east).
     * */
    public int subx;
    /** y number of the subframe within the DTEDFrame, from the bottom
     * side of the frame (south).
     * */
    public int suby;
    /** Longitude of the upper left corner of the subframe image. */
    public float lon;
    /** Latitude of the upper left corner of the subframe image. */
    public float lat;
    /** height of the subframe image. */
    public int height;
    /** Width of the subframe image. */
    public int width;
    /** The colormodel of the images. Use
     * OMRasterObject.COLORMODEL_DIRECT (default) or
     * OMRasterObject.COLORMODEL_INDEXED*/
    public int colorModel = OMRasterObject.COLORMODEL_DIRECT;

    public DTEDFrameSubframeInfo(int Vt, int Bh, int Dl, int Sa){
	this(DTEDCacheHandler.SF_PIXEL_HW, 
	     DTEDCacheHandler.SF_PIXEL_HW, 
	     0.0f, 0.0f, 0, 0, 
	     (double)0.0, (double)0.0, 
	     Vt, Bh, Dl, Sa);
    }

    public DTEDFrameSubframeInfo(int Height, int Width, 
				 float Lon, float Lat,
				 int Subx, int Suby,
				 double xpi, double ypi,
				 int Vt, int Bh,
				 int Dl, int Sa){
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

    public DTEDFrameSubframeInfo makeClone(){
	return new DTEDFrameSubframeInfo(height, width, 
					 lon, lat, 
					 subx, suby, 
					 xPixInterval, yPixInterval, 
					 viewType, bandHeight, 
					 dtedLevel, slopeAdjust);
    }

    /** A comparision test to test the drawing parameters, to figure
     * out if the presentation configuration has changed, and that the
     * pixel color values need to be recalculated.
     * */
    public boolean equals(DTEDFrameSubframeInfo newInfo){
        boolean result = true;
	if(viewType != newInfo.viewType) result = false;
	if(bandHeight != newInfo.bandHeight) result = false;
	if(dtedLevel != newInfo.dtedLevel) result = false;
	if(slopeAdjust != newInfo.slopeAdjust) result = false;
	if(xPixInterval != newInfo.xPixInterval) result = false;
	if(yPixInterval != newInfo.yPixInterval) result = false;
	return result;
    }
}
