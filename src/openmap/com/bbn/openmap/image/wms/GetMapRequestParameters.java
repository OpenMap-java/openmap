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

    public final List layerNames = new ArrayList();

}