package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;

import com.bbn.openmap.proj.Ellipsoid;

/**
 * A {@link CoordinateReferenceSystem} to convert a latlon in a given
 * {@link Ellipsoid} to/from wgs84.
 * <p>
 * The {@link LatLonPoint}-side is in wgs84 and the {@link Point2D}-side in
 * the other {@link Ellipsoid} given by the constructor.
 * <p>
 * Datum shifting is performed using {@link ECEFPoint}
 */
public class DatumShiftGCT extends AbstractGCT {

	private Ellipsoid ellip;

	private HelmertTransformation eceftransf;
	private HelmertTransformation eceftransfInverse;

	private ECEFPoint ecef = new ECEFPoint();

	public DatumShiftGCT(Ellipsoid ellip) {
		this.ellip = ellip;

		eceftransf = HelmertTransformation.find(Ellipsoid.WGS_84, ellip);
		eceftransfInverse = HelmertTransformation.find(ellip, Ellipsoid.WGS_84);
	}

	/**
	 * Convert from a latlon in wgs84 to the {@link Ellipsoid} of this instance
	 */
	public synchronized Point2D forward(double lat, double lon, Point2D ret) {
		ecef.setLatLon(lat, lon, Ellipsoid.WGS_84);
		eceftransf.apply(ecef);
		return ecef.getLatLon(ellip, ret);
	}

	/**
	 * Convert from a latlon in the {@link Ellipsoid} of this instance to wgs84
	 */
	public synchronized LatLonPoint inverse(double x, double y, LatLonPoint ret) {
		ecef.setLatLon(y, x, ellip);
		eceftransfInverse.apply(ecef);
		return ecef.getLatLon(ret);
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + ellip.name + ")";
	}

}
