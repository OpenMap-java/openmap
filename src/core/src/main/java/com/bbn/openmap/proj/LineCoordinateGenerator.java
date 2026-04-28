package com.bbn.openmap.proj;

import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Generator class that connects simple coordinates with complex lines (great
 * circle or rhumb). If you provide decimal degrees, the answer will be in
 * decimal degrees. If coords are provided in radians, the answer will be in
 * radians.
 * 
 * <pre>
 * Usage:
 * 
 * double[] coords = new double[] { 30.0, -125.0, 30.0, -90.0, 15.0, -90.0, 15, -125.0, 30.0, -125.0 };
 * double[] complexCoords = LineCoordinateGenerator.fromDegrees(coords).withSegmentsPerDegrees(10).greatCircleLineDoubles();
 * 
 * </pre>
 * 
 * @author dietrick
 */
public class LineCoordinateGenerator {

	public final static double DEFAULT_SEGS_PER_DEG = 10;
	double segsPerDeg = DEFAULT_SEGS_PER_DEG;
	final double[] llpts;
	private boolean returnDegrees = false;

	/**
	 * Use static methods to create one, designating radians or degrees.
	 * 
	 * @param radians
	 */
	private LineCoordinateGenerator(double[] radians) {
		this.llpts = radians;
	}

	/**
	 * Create LCG with radian coordinates.
	 * 
	 * @param radians array of coordinates in radians, in lat, lon, lat, lon
	 *            order.
	 * @return LCG
	 */
	public static LineCoordinateGenerator fromRadians(double[] radians) {
		return new LineCoordinateGenerator(radians);
	}

	/**
	 * Create LCG with decimal degree coordinates.
	 * 
	 * @param degrees array of coordinates in degrees, in lat, lon, lat, lon
	 *            order.
	 * @return LCG
	 */
	public static LineCoordinateGenerator fromDegrees(double[] degrees) {
		double[] radians = new double[degrees.length];
		System.arraycopy(degrees, 0, radians, 0, degrees.length);
		ProjMath.arrayDegToRad(radians);
		return new LineCoordinateGenerator(radians).fromDegrees();
	}

	private LineCoordinateGenerator fromDegrees() {
		returnDegrees = true;
		return this;
	}

	/**
	 * Set how complex the line is by setting how many segments per degree are
	 * used to approximate the curve.
	 * 
	 * @param spd the default is 10 segments per degree
	 * @return this
	 */
	public LineCoordinateGenerator withSegmentsPerDegrees(double spd) {
		this.segsPerDeg = spd;
		return this;
	}

	/**
	 * Return the source coordinates connected by great circle lines
	 * 
	 * @return double[] in lat, lon, lat, lon order.
	 */
	public double[] greatCircleLineDoubles() {
		return toDoubles(greatCircleLineShape()).get(0);
	}

	/**
	 * Create a java.awt.Shape object of coordinates connected by great circle
	 * lines.
	 * 
	 * @return java.awt.Shape
	 */
	public Shape greatCircleLineShape() {
		GeneralPath path = null;
		if (llpts != null && llpts.length >= 4 && llpts.length % 2 == 0) {
			double y1 = llpts[0];
			double x1 = llpts[1];

			path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, llpts.length / 2);
			boolean firstCoords = true;

			for (int i = 2; i < llpts.length; i += 2) {
				double y2 = llpts[i];
				double x2 = llpts[i + 1];

				double radDist = GreatCircle.sphericalDistance(y1, x1, y2, x2);
				int nsegs = (int) (ProjMath.radToDeg(radDist) * segsPerDeg); // segs/degree

				if (nsegs == 0) {
					nsegs = 1;
				}

				double[] coords = GreatCircle.greatCircle(y1, x1, y2, x2, nsegs, false);

				if (returnDegrees) {
					ProjMath.arrayRadToDeg(coords);
				}

				for (int j = 0; j <= coords.length - 1; j += 2) {
					if (firstCoords) {
						path.moveTo(coords[j + 1], coords[j]);
						firstCoords = false;
					} else {
						path.lineTo(coords[j + 1], coords[j]);
					}
				}

				x1 = x2;
				y1 = y2;
			}

			// End point, since great circle calc doesn't tack on the last
			// point.
			if (returnDegrees) {
				path.lineTo(ProjMath.radToDeg(x1), ProjMath.radToDeg(y1));
			} else {
				path.lineTo(x1, y1);
			}
		}

		return path;
	}

	/**
	 * Return the source coordinates connected by rhumb lines
	 * 
	 * @return double[] in lat, lon, lat, lon order.
	 */
	public double[] rhumbLineDoubles() {
		return toDoubles(rhumbLineShape()).get(0);
	}

	/**
	 * Create a java.awt.Shape object of coordinates connected by rhumb lines.
	 * 
	 * @return java.awt.Shape
	 */
	public Shape rhumbLineShape() {
		GeneralPath path = null;
		if (llpts != null && llpts.length >= 4 && llpts.length % 2 == 0) {
			LatLonPoint ll1 = new LatLonPoint.Double(llpts[0], llpts[1], true);

			path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, llpts.length / 2);
			boolean firstCoords = true;

			for (int i = 2; i < llpts.length - 1; i += 2) {
				LatLonPoint ll2 = new LatLonPoint.Double(llpts[i], llpts[i + 1], true);

				if (firstCoords) {
					moveTo(path, ll1);
					firstCoords = false;
				} else {
					lineTo(path, ll1);
				}

				double radDist = RhumbCalculator.getDistanceBetweenPoints(ll1, ll2);
				double angle = RhumbCalculator.getAzimuthBetweenPoints(ll1, ll2);

				double segDistIncrease = radDist / ProjMath.degToRad(ProjMath.radToDeg(radDist) * segsPerDeg); // segs/degree

				double segDist = segDistIncrease;

				while (segDist < radDist) {
					LatLonPoint llp = RhumbCalculator.calculatePointOnRhumbLine(ll1, angle, segDist);
					lineTo(path, llp);
					segDist += segDistIncrease;
				}

				ll1 = ll2;
			}

			// Make sure last coordinate is on return shape
			lineTo(path, ll1);

		}

		return path;
	}

	private void moveTo(GeneralPath path, LatLonPoint llp) {
		if (returnDegrees) {
			path.moveTo(llp.getX(), llp.getY());
		} else {
			path.moveTo(llp.getRadLon(), llp.getRadLat());
		}
	}

	private void lineTo(GeneralPath path, LatLonPoint llp) {
		if (returnDegrees) {
			path.lineTo(llp.getX(), llp.getY());
		} else {
			path.lineTo(llp.getRadLon(), llp.getRadLat());
		}
	}

	/**
	 * Creates a Shape object from provided coordinates.
	 * 
	 * @return java.awt.Shape
	 */
	public Shape straightLineShape() {
		GeneralPath path = null;
		if (llpts != null && llpts.length >= 4 && llpts.length % 2 == 0) {
			double y1 = llpts[0];
			double x1 = llpts[1];

			path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, llpts.length / 2);
			if (returnDegrees) {
				path.moveTo(ProjMath.radToDeg(x1), ProjMath.radToDeg(y1));
			} else {
				path.moveTo(x1, y1);
			}

			for (int i = 2; i < llpts.length - 1; i += 2) {
				x1 = llpts[i + 1];
				y1 = llpts[i];

				if (returnDegrees) {
					path.lineTo(ProjMath.radToDeg(x1), ProjMath.radToDeg(y1));
				} else {
					path.lineTo(x1, y1);
				}
			}
		}
		return path;
	}

	/**
	 * Convert a Shape object into a List of double[]. Separate double[] are
	 * created in case there's a moveTo in the order of coordinates in Shape.
	 * 
	 * @param s java.awt.Shape
	 * @return a List of double[]
	 */
	public static List<double[]> toDoubles(Shape s) {
		List<double[]> coordLists = new ArrayList<double[]>();

		PathIterator pi2 = s.getPathIterator(null);
		FlatteningPathIterator pi = new FlatteningPathIterator(pi2, .25);
		double[] coords = new double[6];

		double lastMovedToPntX = Double.NaN;
		double lastMovedToPntY = Double.NaN;

		List<Double> curCoord = null;

		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);

			if (type == PathIterator.SEG_LINETO) {

				// Moved to the next point
				curCoord = assertList(curCoord);
				// lat/lon order
				curCoord.add(coords[1]);
				curCoord.add(coords[0]);

			} else if (type == PathIterator.SEG_MOVETO) {

				if (curCoord != null && !curCoord.isEmpty()) {
					coordLists.add(toArray(curCoord));
					curCoord = null;
				}

				// Usually the first set of coords
				lastMovedToPntX = coords[0];
				lastMovedToPntY = coords[1];

				curCoord = assertList(curCoord);
				curCoord.add(lastMovedToPntY);
				curCoord.add(lastMovedToPntX);

			} else if (type == PathIterator.SEG_CLOSE) {

				// The last set of coords, if they should go back to the first
				// set of coords.
				final double x = coords[0];
				final double y = coords[1];

				if (x != lastMovedToPntX && y != lastMovedToPntY) {
					curCoord = assertList(curCoord);
					curCoord.add(y);
					curCoord.add(x);
				}

				if (curCoord != null && !curCoord.isEmpty()) {
					coordLists.add(toArray(curCoord));
					curCoord = null;
				}
			}

			pi.next();
		}

		if (curCoord != null && !curCoord.isEmpty()) {
			coordLists.add(toArray(curCoord));
		}

		return coordLists;
	}

	private static List<Double> assertList(List<Double> list) {
		if (list != null) {
			return list;
		}
		return new ArrayList<Double>();
	}

	private static double[] toArray(List<Double> coords) {
		double[] ret = new double[coords.size()];
		int i = 0;
		for (double d : coords) {
			ret[i++] = d;
		}
		return ret;
	}
}
