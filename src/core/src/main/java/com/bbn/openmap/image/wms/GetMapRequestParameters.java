package com.bbn.openmap.image.wms;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.coords.CoordinateReferenceSystem;
import com.bbn.openmap.proj.coords.LatLonPoint;

class GetMapRequestParameters extends WmsRequestParameters implements FormatRequestParameter,
		WidthAndHeightRequestParameters {

	public int width;

	public int height;

	public CoordinateReferenceSystem crs;

	public LatLonPoint bboxLatLonLowerLeft;

	public LatLonPoint bboxLatLonUpperRight;

	public LatLonPoint bboxLatLonCenter;

	public ImageFormatter formatter;

	private boolean transparent = true;

	public Paint background = OMColor.clear;

	/**
	 * All wms layer names as they appear in the LAYERS-element of the request.
	 * Some of the layer names may be to a nested layer.
	 */
	public final List<String> layerNames = new ArrayList<String>();

	/**
	 * All top level layer names for each of the layer names. So, if the request
	 * has two sub level layer names with the same top level layer name, this
	 * list will only have that single top level layer name.
	 */
	public final List<String> topLayerNames = new ArrayList<String>();

	public ImageFormatter getFormatter() {
		return formatter;
	}

	public void setFormatter(ImageFormatter formatter) {
		this.formatter = formatter;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}

	public boolean getTransparent() {
		return transparent;
	}

}