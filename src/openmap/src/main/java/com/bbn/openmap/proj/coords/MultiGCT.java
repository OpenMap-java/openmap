package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;

/**
 * A {@link GeoCoordTransformation} consisting of an ordered list of other
 * {@link GeoCoordTransformation}.
 * <p>
 * The forward methods perform the {@link GeoCoordTransformation}s in the given
 * order. The inverse methods in the opposite order.
 */
public class MultiGCT extends AbstractGCT {

	private GeoCoordTransformation[] gcts;

	public MultiGCT(GeoCoordTransformation[] gcts) {
		this.gcts = gcts;
	}

	public Point2D forward(double lat, double lon, Point2D ret) {
		for (int i = 0; i < gcts.length; i++) {
			ret = gcts[i].forward(lat, lon, ret);
			lat = ret.getY();
			lon = ret.getX();
		}
		return ret;
	}

	public LatLonPoint inverse(double x, double y, LatLonPoint ret) {
		for (int i = gcts.length - 1; i >= 0; i--) {
			ret = gcts[i].inverse(x, y, ret);
			x = ret.getX();
			y = ret.getY();
		}
		return ret;
	}

}
