package com.bbn.openmap.image.wms;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.proj.coords.CoordinateReferenceSystem;
import com.bbn.openmap.proj.coords.LatLonPoint;

class GetMapRequestParameters {

    public int width;

    public int height;

    public CoordinateReferenceSystem crs;

    public LatLonPoint bboxLatLonMinXY;

    public LatLonPoint bboxLatLonMaxXY;

    public LatLonPoint bboxLatLonCenter;

    public ImageFormatter formatter;

    public Paint background;
    
    /**
     * All wms layer names as they appear in the LAYERS-element of the request.
     * Some of the layer names may be to a nested layer.
     */
    public final List layerNames = new ArrayList();

    /**
     * All top level layer names for each of the layer names. So, if the request
     * has two sub level layer names with the same top level layer name, this
     * list will only have that single top level layer name.
     */
    public final List topLayerNames = new ArrayList();
    
}