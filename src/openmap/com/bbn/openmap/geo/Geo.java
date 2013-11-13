/*
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        A Verizon Company
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2001, 2002 All Rights Reserved
 *
 */

package com.bbn.openmap.geo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.Length;

/**
 * A class that represents a point on the Earth as a three dimensional unit
 * length vector, rather than latitude and longitude. For the theory and an
 * efficient implementation using partial evaluation see:
 * http://openmap.bbn.com/~kanderso/lisp/performing-lisp/essence.ps
 * 
 * This implementation matches the theory carefully, but does not use partial
 * evaluation.
 * 
 * <p>
 * For the area calculation see: http://math.rice.edu/~pcmi/sphere/
 * 
 * @author Ken Anderson
 * @author Sachin Date
 * @author Ben Lubin
 * @author Michael Thome
 * @version $Revision: 1.30 $ on $Date: 2007/02/13 20:02:14 $
 */
public class Geo implements Serializable {

    /***************************************************************************
     * Constants for the shape of the earth. see
     * http://www.gfy.ku.dk/%7Eiag/HB2000/part4/groten.htm
     **************************************************************************/
    // Replaced by Length constants.
    // public static final double radiusKM = 6378.13662; // in KM.
    // public static final double radiusNM = 3443.9182; // in NM.
    // Replaced with WGS 84 constants
    // public static final double flattening = 1.0/298.25642;
    public static final double flattening = 1.0 / 298.257223563;
    public static final double FLATTENING_C = (1.0 - flattening) * (1.0 - flattening);

    public static final double METERS_PER_NM = 1852;
    private static final double NPD_LTERM1 = 111412.84 / METERS_PER_NM;
    private static final double NPD_LTERM2 = -93.5 / METERS_PER_NM;
    private static final double NPD_LTERM3 = 0.118 / METERS_PER_NM;

    private double x;
    private double y;
    private double z;

    /**
     * Compute nautical miles per degree at a specified latitude (in degrees).
     * Calculation from NIMA: http://pollux.nss.nima.mil/calc/degree.html
     */
    public final static double npdAtLat(double latdeg) {
        double lat = Math.toRadians(latdeg);
        return (NPD_LTERM1 * Math.cos(lat) + NPD_LTERM2 * Math.cos(3 * lat) + NPD_LTERM3
                * Math.cos(5 * lat));
    }

    /** Convert from geographic to geocentric latitude (radians) */
    public static double geocentricLatitude(double geographicLatitude) {
        return Math.atan((Math.tan(geographicLatitude) * FLATTENING_C));
    }

    /** Convert from geocentric to geographic latitude (radians) */
    public static double geographicLatitude(double geocentricLatitude) {
        return Math.atan(Math.tan(geocentricLatitude) / FLATTENING_C);
    }

    /** Convert from degrees to radians. */
    public static double radians(double degrees) {
        return Length.DECIMAL_DEGREE.toRadians(degrees);
    }

    /** Convert from radians to degrees. */
    public static double degrees(double radians) {
        return Length.DECIMAL_DEGREE.fromRadians(radians);
    }

    /** Convert radians to kilometers. * */
    public static double km(double radians) {
        return Length.KM.fromRadians(radians);
    }

    /** Convert kilometers to radians. * */
    public static double kmToAngle(double km) {
        return Length.KM.toRadians(km);
    }

    /** Convert radians to nauticalMiles. * */
    public static double nm(double radians) {
        return Length.NM.fromRadians(radians);
    }

    /** Convert nautical miles to radians. * */
    public static double nmToAngle(double nm) {
        return Length.NM.toRadians(nm);
    }

    public Geo() {
    }

    /**
     * Construct a Geo from its latitude and longitude.
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     */
    public Geo(double lat, double lon) {
        initialize(lat, lon);
    }

    /**
     * Construct a Geo from its latitude and longitude.
     * 
     * @param lat latitude.
     * @param lon longitude.
     * @param isDegrees should be true if the lat/lon are specified in decimal
     *        degrees, false if they are radians.
     */
    public Geo(double lat, double lon, boolean isDegrees) {
        if (isDegrees) {
            initialize(lat, lon);
        } else {
            initializeRadians(lat, lon);
        }
    }

    /** Construct a Geo from its parts. */
    public Geo(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Construct a Geo from another Geo. */
    public Geo(Geo geo) {
        this(geo.x, geo.y, geo.z);
    }

    public static final Geo makeGeoRadians(double latr, double lonr) {
        double rlat = geocentricLatitude(latr);
        double c = Math.cos(rlat);
        return new Geo(c * Math.cos(lonr), c * Math.sin(lonr), Math.sin(rlat));
    }

    public static final Geo makeGeoDegrees(double latd, double lond) {
        return makeGeoRadians(radians(latd), radians(lond));
    }

    public static final Geo makeGeo(double x, double y, double z) {
        return new Geo(x, y, z);
    }

    public static final Geo makeGeo(Geo p) {
        return new Geo(p.x, p.y, p.z);
    }

    /**
     * Initialize this Geo to match another.
     * 
     * @param g
     */
    public void initialize(Geo g) {
        x = g.x;
        y = g.y;
        z = g.z;
    }

    /**
     * Initialize this Geo with new parameters.
     * 
     * @param x
     * @param y
     * @param z
     */
    public void initialize(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Initialize this Geo with to represent coordinates.
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     */
    public void initialize(double lat, double lon) {
        initializeRadians(radians(lat), radians(lon));
    }

    /**
     * Initialize this Geo with to represent coordinates.
     * 
     * @param lat latitude in radians.
     * @param lon longitude in radians.
     */
    public void initializeRadians(double lat, double lon) {
        double rlat = geocentricLatitude(lat);
        double c = Math.cos(rlat);
        x = c * Math.cos(lon);
        y = c * Math.sin(lon);
        z = Math.sin(rlat);
    }

    /**
     * Find the midpoint Geo between this one and another on a Great Circle line
     * between the two. The result is undefined of the two points are antipodes.
     * 
     * @param g2
     * @return midpoint Geo.
     */
    public Geo midPoint(Geo g2) {
        return add(g2).normalize();
    }

    /**
     * Find the midpoint Geo between this one and another on a Great Circle line
     * between the two. The result is undefined of the two points are antipodes.
     * 
     * @param g2
     * @param ret a Geo value to set returned values in. Do not pass in a null
     *        value.
     * @return midpoint Geo.
     */
    public Geo midPoint(Geo g2, Geo ret) {
        return add(g2).normalize(ret);
    }

    public Geo interpolate(Geo g2, double x) {
        return scale(x).add(g2.scale(1 - x)).normalize();
    }

    /**
     * 
     * @param g2
     * @param x
     * @param ret Do not pass in a null value.
     * @return ret, or new Geo set at distance between g2 and this one.
     */
    public Geo interpolate(Geo g2, double x, Geo ret) {
        return scale(x).add(g2.scale(1 - x, ret), ret).normalize(ret);
    }

    public String toString() {
        return "Geo[" + getLatitude() + "," + getLongitude() + "]";
    }

    public double getLatitude() {
        return degrees(geographicLatitude(Math.atan2(z, Math.sqrt(x * x + y * y))));
    }

    public double getLongitude() {
        return degrees(Math.atan2(y, x));
    }

    public double getLatitudeRadians() {
        return geographicLatitude(Math.atan2(z, Math.sqrt(x * x + y * y)));
    }

    public double getLongitudeRadians() {
        return Math.atan2(y, x);
    }

    /**
     * Reader for x, in internal axis representation (positive to the right side
     * of screen).
     * 
     * @return x
     */
    public final double x() {
        return this.x;
    }

    /**
     * Reader for y in internal axis representation (positive into screen).
     * 
     * @return y
     */
    public final double y() {
        return this.y;
    }

    /**
     * Reader for z in internal axis representation (positive going to top of
     * screen).
     * 
     * @return z
     */
    public final double z() {
        return this.z;
    }

    public void setLength(double r) {
        // It's tempting to call getLatitudeRadians() here, but it changes the
        // angle. I think we want to keep the angles the same, and just extend
        // x, y, z, and then let the latitudes get refigured out for the
        // ellipsoid when they are asked for.
        double rlat = Math.atan2(z, Math.sqrt(x * x + y * y));
        double rlon = getLongitudeRadians();

        double c = r * Math.cos(rlat);
        x = c * Math.cos(rlon);
        y = c * Math.sin(rlon);
        z = r * Math.sin(rlat);
    }

    /** North pole. */
    public static final Geo north = new Geo(0.0, 0.0, 1.0);

    /** Dot product. Gives you the cos of the angle between this point and b. */
    public double dot(Geo b) {
        return (this.x() * b.x() + this.y() * b.y() + this.z() * b.z());
    }

    /** Dot product. Gives you the cos of the angle between the two points. */
    public static double dot(Geo a, Geo b) {
        return (a.x() * b.x() + a.y() * b.y() + a.z() * b.z());
    }

    /** Euclidian length. */
    public double length() {
        return Math.sqrt(this.dot(this));
    }

    /** Multiply this by s. * */
    public Geo scale(double s) {
        return scale(s, new Geo());
    }

    /**
     * Multiply this by s.
     * 
     * @return ret that was passed in, filled in with scaled values. Do not pass
     *         in a null value.
     */
    public Geo scale(double s, Geo ret) {
        ret.initialize(this.x() * s, this.y() * s, this.z() * s);
        return ret;
    }

    /** Returns a unit length vector parallel to this. */
    public Geo normalize() {
        return this.scale(1.0 / this.length());
    }

    /**
     * Returns a unit length vector parallel to this.
     * 
     * @return ret with normalized values. Do not pass in a null value.
     */
    public Geo normalize(Geo ret) {
        return this.scale(1.0 / this.length(), ret);
    }

    /** Vector cross product. */
    public Geo cross(Geo b) {
        return cross(b, new Geo());
    }

    /**
     * Vector cross product. Gives you the point 90 degrees from the great
     * circle line between this point and b. The side of the line depends on the
     * right hand rule.
     * 
     * @return ret Do not pass in a null value.
     */
    public Geo cross(Geo b, Geo ret) {
        ret.initialize(this.y() * b.z() - this.z() * b.y(), this.z() * b.x() - this.x() * b.z(), this.x()
                * b.y() - this.y() * b.x());
        return ret;
    }

    /** Equivalent to this.cross(b).length(). */
    public double crossLength(Geo b) {
        double x = this.y() * b.z() - this.z() * b.y();
        double y = this.z() * b.x() - this.x() * b.z();
        double z = this.x() * b.y() - this.y() * b.x();
        return Math.sqrt(x * x + y * y + z * z);
    }

    /** Equivalent to <code>this.cross(b).normalize()</code>. */
    public Geo crossNormalize(Geo b) {
        return crossNormalize(b, new Geo());
    }

    /**
     * Equivalent to <code>this.cross(b).normalize()</code>.
     * 
     * @return ret Do not pass in a null value.
     */
    public Geo crossNormalize(Geo b, Geo ret) {
        double x = this.y() * b.z() - this.z() * b.y();
        double y = this.z() * b.x() - this.x() * b.z();
        double z = this.x() * b.y() - this.y() * b.x();
        double L = Math.sqrt(x * x + y * y + z * z);

        ret.initialize(x / L, y / L, z / L);
        return ret;
    }

    /**
     * Equivalent to <code>this.cross(b).normalize()</code>.
     * 
     * @return ret Do not pass in a null value.
     */
    public static Geo crossNormalize(Geo a, Geo b, Geo ret) {
        return a.crossNormalize(b, ret);
    }

    /** Returns this + b. */
    public Geo add(Geo b) {
        return add(b, new Geo());
    }

    /**
     * @return ret Do not pass in a null value.
     */
    public Geo add(Geo b, Geo ret) {
        ret.initialize(this.x() + b.x(), this.y() + b.y(), this.z() + b.z());
        return ret;
    }

    /** Returns this - b. */
    public Geo subtract(Geo b) {
        return subtract(b, new Geo());
    }

    /**
     * Returns this - b. *
     * 
     * @return ret Do not pass in a null value.
     */
    public Geo subtract(Geo b, Geo ret) {
        ret.initialize(this.x() - b.x(), this.y() - b.y(), this.z() - b.z());
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Geo)) {
            return false;
        }

        Geo v2 = (Geo) obj;
        return MoreMath.approximately_equal(this.x, v2.x)
                && MoreMath.approximately_equal(this.y, v2.y)
                && MoreMath.approximately_equal(this.z, v2.z);
    }

    /** Angular distance, in radians between this and v2. */
    public double distance(Geo v2) {
        return Math.atan2(v2.crossLength(this), v2.dot(this));
    }

    /** Angular distance, in radians between v1 and v2. */
    public static double distance(Geo v1, Geo v2) {
        return v1.distance(v2);
    }

    /** Angular distance, in radians between the two lat lon points. */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        return Geo.distance(new Geo(lat1, lon1), new Geo(lat2, lon2));
    }

    /** Distance in kilometers. * */
    public double distanceKM(Geo v2) {
        return km(distance(v2));
    }

    /** Distance in kilometers. * */
    public static double distanceKM(Geo v1, Geo v2) {
        return v1.distanceKM(v2);
    }

    /** Distance in kilometers. * */
    public static double distanceKM(double lat1, double lon1, double lat2, double lon2) {
        return Geo.distanceKM(new Geo(lat1, lon1), new Geo(lat2, lon2));
    }

    /** Distance in nautical miles. * */
    public double distanceNM(Geo v2) {
        return nm(distance(v2));
    }

    /** Distance in nautical miles. * */
    public static double distanceNM(Geo v1, Geo v2) {
        return v1.distanceNM(v2);
    }

    /** Distance in nautical miles. * */
    public static double distanceNM(double lat1, double lon1, double lat2, double lon2) {
        return Geo.distanceNM(new Geo(lat1, lon1), new Geo(lat2, lon2));
    }

    /**
     * Azimuth in radians from this to v2.
     * 
     * @param v2 other Geo
     * @return radian angle between this and v2. Will also return NaN for
     *         identical points, or other Math conventions for resulting math
     *         results.
     */
    public double strictAzimuth(Geo v2) {
        /*
         * n1 is the great circle representing the meridian of this. n2 is the
         * great circle between this and v2. The azimuth is the angle between
         * them but we specialized the cross product.
         */
        // Geo n1 = north.cross(this);
        // Geo n2 = v2.cross(this);
        // crossNormalization is needed to geos of different length.
        Geo n1 = north.crossNormalize(this);
        Geo n2 = v2.crossNormalize(this);
        double az = Math.atan2(-north.dot(n2), n1.dot(n2));
        return (az > 0.0) ? az : 2.0 * Math.PI + az;
    }

    /**
     * Azimuth in radians from this to v2.
     * 
     * @param v2 other Geo
     * @return radian angle between this and v2, and zero instead of NaN if the
     *         two geos are identical.
     */
    public double azimuth(Geo v2) {
        double ret = strictAzimuth(v2);
        if (Double.isNaN(ret)) {
            return 0;
        }
        return ret;
    }

    /**
     * Given 3 points on a sphere, p0, p1, p2, return the angle between them in
     * radians.
     */
    public static double angle(Geo p0, Geo p1, Geo p2) {
        return Math.PI - p0.cross(p1).distance(p1.cross(p2));
    }

    /**
     * Computes the area of a polygon on the surface of a unit sphere given an
     * enumeration of its point. For a non unit sphere, multiply this by the
     * radius of sphere squared. Make sure the first point doesn't equal the
     * last.
     */
    public static double area(Enumeration vs) {
        int count = 0;
        double area = 0;
        Geo v0 = (Geo) vs.nextElement();
        Geo v1 = (Geo) vs.nextElement();
        Geo p0 = v0;
        Geo p1 = v1;
        Geo p2 = null;
        while (vs.hasMoreElements()) {
            count++;
            p2 = (Geo) vs.nextElement();
            area += angle(p0, p1, p2);
            p0 = p1;
            p1 = p2;
        }

        count++;
        p2 = v0;
        area += angle(p0, p1, p2);
        p0 = p1;
        p1 = p2;

        count++;
        p2 = v1;
        area += angle(p0, p1, p2);

        return area - (count - 2) * Math.PI;
    }

    /**
     * Is the point, p, within radius radians of the great circle segment
     * between this and v2?
     */
    public boolean isInside(Geo v2, double radius, Geo p) {
        // Allocate a Geo to be reused for all of these calculations, instead of
        // creating 3 of them that are just thrown away. There's one more we
        // still need to allocate, for dp below.
        Geo tmp = new Geo();

        /*
         * gc is a unit vector perpendicular to the plane defined by v1 and v2
         */
        Geo gc = this.crossNormalize(v2, tmp);

        /*
         * |gc . p| is the size of the projection of p onto gc (the normal of
         * v1,v2) cos(pi/2-r) is effectively the size of the projection of a
         * vector along gc of the radius length. If the former is larger than
         * the latter, than p is further than radius from arc, so must not be
         * isInside
         */
        if (Math.abs(gc.dot(p)) > Math.cos((Math.PI / 2.0) - radius))
            return false;

        /*
         * If p is within radius of either endpoint, then we know it isInside
         */
        if (this.distance(p) <= radius || v2.distance(p) <= radius)
            return true;

        /* d is the vector from the v2 to v1 */
        Geo d = v2.subtract(this, tmp);

        /* L is the length of the vector d */
        double L = d.length();

        /* n is the d normalized to length=1 */
        Geo n = d.normalize(tmp);

        /* dp is the vector from p to v1 */
        Geo dp = p.subtract(this, new Geo());

        /* size is the size of the projection of dp onto n */
        double size = n.dot(dp);

        /* p is inside iff size>=0 and size <= L */
        return (0 <= size && size <= L);
    }

    /**
     * do the segments v1-v2 and p1-p2 come within radius (radians) of each
     * other?
     */
    public static boolean isInside(Geo v1, Geo v2, double radius, Geo p1, Geo p2) {
        return v1.isInside(v2, radius, p1) || v1.isInside(v2, radius, p2)
                || p1.isInside(p2, radius, v1) || p1.isInside(p2, radius, v2);
    }

    /**
     * Static version of isInside uses conventional (decimal degree)
     * coordinates.
     */
    public static boolean isInside(double lat1, double lon1, double lat2, double lon2,
                                   double radius, double lat3, double lon3) {
        return (new Geo(lat1, lon1)).isInside(new Geo(lat2, lon2), radius, new Geo(lat3, lon3));
    }

    /**
     * Is Geo p inside the time bubble along the great circle segment from this
     * to v2 looking forward forwardRadius and backward backwardRadius.
     */
    public boolean inBubble(Geo v2, double forwardRadius, double backRadius, Geo p) {
        return distance(p) <= ((v2.subtract(this).normalize().dot(p.subtract(this)) > 0.0) ? forwardRadius
                : backRadius);
    }

    /** Returns the point opposite this point on the earth. */
    public Geo antipode() {
        return this.scale(-1.0, new Geo());
    }

    /**
     * Returns the point opposite this point on the earth. *
     * 
     * @return ret Do not pass in a null value.
     */
    public Geo antipode(Geo ret) {
        return this.scale(-1.0, ret);
    }

    /**
     * Find the intersection of the great circle between this and q and the
     * great circle normal to r.
     * <p>
     * 
     * That is, find the point, y, lying between this and q such that
     * 
     * <pre>
     * 
     *  y = [x*this + (1-x)*q]*c
     *  where c = 1/y.dot(y) is a factor for normalizing y.
     *  y.dot(r) = 0
     *  substituting:
     *  [x*this + (1-x)*q]*c.dot(r) = 0 or
     *  [x*this + (1-x)*q].dot(r) = 0
     *  x*this.dot(r) + (1-x)*q.dot(r) = 0
     *  x*a + (1-x)*b = 0
     *  x = -b/(a - b)
     * 
     * </pre>
     * 
     * We assume that this and q are less than 180 degrees appart. When this and
     * q are 180 degrees appart, the point -y is also a valid intersection.
     * <p>
     * Alternatively the intersection point, y, satisfies y.dot(r) = 0
     * y.dot(this.crossNormalize(q)) = 0 which is satisfied by y =
     * r.crossNormalize(this.crossNormalize(q));
     * 
     */
    public Geo intersect(Geo q, Geo r) {
        return intersect(q, r, new Geo());
    }

    /**
     * Find the intersection of the great circle between this and q and the
     * great circle normal to r.
     * <p>
     * 
     * That is, find the point, y, lying between this and q such that
     * 
     * <pre>
     * 
     *  y = [x*this + (1-x)*q]*c
     *  where c = 1/y.dot(y) is a factor for normalizing y.
     *  y.dot(r) = 0
     *  substituting:
     *  [x*this + (1-x)*q]*c.dot(r) = 0 or
     *  [x*this + (1-x)*q].dot(r) = 0
     *  x*this.dot(r) + (1-x)*q.dot(r) = 0
     *  x*a + (1-x)*b = 0
     *  x = -b/(a - b)
     * 
     * </pre>
     * 
     * We assume that this and q are less than 180 degrees apart. When this and
     * q are 180 degrees apart, the point -y is also a valid intersection.
     * <p>
     * Alternatively the intersection point, y, satisfies y.dot(r) = 0
     * y.dot(this.crossNormalize(q)) = 0 which is satisfied by y =
     * r.crossNormalize(this.crossNormalize(q));
     * 
     * @return ret Do not pass in a null value.
     */
    public Geo intersect(Geo q, Geo r, Geo ret) {

        // There used to be code in here that broke the intersection code. It
        // was inserted into the 5.1 code, but I can't find a record of why.
        // Reverting to the old code that still works, at least for the test
        // cases we have.

        double a = this.dot(r);
        double b = q.dot(r);
        double x = -b / (a - b);
        // This still results in one Geo being allocated and lost, in the
        // q.scale call.
        return this.scale(x, ret).add(q.scale(1.0 - x), ret).normalize(ret);

    }

    /** alias for computeCorridor(path, radius, radians(10), true) * */
    public static Geo[] computeCorridor(Geo[] path, double radius) {
        return computeCorridor(path, radius, radians(10.0), true);
    }

    /**
     * Wrap a fixed-distance corridor around an (open) path, as specified by an
     * array of Geo.
     * 
     * @param path Open path, must not have repeated points or consecutive
     *        antipodes.
     * @param radius Distance from path to widen corridor, in angular radians.
     * @param err maximum angle of rounded edges, in radians. If 0, will
     *        directly cut outside bends.
     * @param capp iff true, will round end caps
     * @return a closed polygon representing the specified corridor around the
     *         path.
     * 
     */
    public static Geo[] computeCorridor(Geo[] path, double radius, double err, boolean capp) {
        if (path == null || radius <= 0.0) {
            return new Geo[] {};
        }
        // assert path!=null;
        // assert radius > 0.0;

        int pl = path.length;
        if (pl < 2)
            return null;

        // final polygon will be right[0],...,right[n],left[m],...,left[0]
        ArrayList right = new ArrayList((int) (pl * 1.5));
        ArrayList left = new ArrayList((int) (pl * 1.5));

        Geo g0 = null; // previous point
        Geo n0 = null; // previous normal vector
        Geo l0 = null;
        Geo r0 = null;

        Geo g1 = path[0]; // current point

        for (int i = 1; i < pl; i++) {
            Geo g2 = path[i]; // next point
            Geo n1 = g1.crossNormalize(g2); // n is perpendicular to the vector
            // from g1 to g2
            n1 = n1.scale(radius); // normalize to radius
            // these are the offsets on the g2 side at g1
            Geo r1b = g1.add(n1);
            Geo l1b = g1.subtract(n1);

            if (n0 == null || g0 == null) {
                if (capp && err > 0) {
                    // start cap
                    Geo[] arc = approximateArc(g1, l1b, r1b, err);
                    for (int j = arc.length - 1; j >= 0; j--) {
                        right.add(arc[j]);
                    }
                } else {
                    // no previous point - we'll just be square
                    right.add(l1b);
                    left.add(r1b);
                }
                // advance normals
                l0 = l1b;
                r0 = r1b;
            } else {
                // otherwise, compute a more complex shape

                // these are the right and left on the g0 side of g1
                Geo r1a = g1.add(n0);
                Geo l1a = g1.subtract(n0);

                double handed = g0.cross(g1).dot(g2); // right or left handed
                // divergence
                if (handed > 0) { // left needs two points, right needs 1
                    if (err > 0) {
                        Geo[] arc = approximateArc(g1, l1b, l1a, err);
                        for (int j = arc.length - 1; j >= 0; j--) {
                            right.add(arc[j]);
                        }
                    } else {
                        right.add(l1a);
                        right.add(l1b);
                    }
                    l0 = l1b;

                    Geo ip = Intersection.segmentsIntersect(r0, r1a, r1b, g2.add(n1));
                    // if they intersect, take the intersection, else use the
                    // points and punt
                    if (ip != null) {
                        left.add(ip);
                    } else {
                        left.add(r1a);
                        left.add(r1b);
                    }
                    r0 = ip;
                } else {
                    Geo ip = Intersection.segmentsIntersect(l0, l1a, l1b, g2.subtract(n1));
                    // if they intersect, take the intersection, else use the
                    // points and punt
                    if (ip != null) {
                        right.add(ip);
                    } else {
                        right.add(l1a);
                        right.add(l1b);
                    }
                    l0 = ip;
                    if (err > 0) {
                        Geo[] arc = approximateArc(g1, r1a, r1b, err);
                        for (int j = 0; j < arc.length; j++) {
                            left.add(arc[j]);
                        }
                    } else {
                        left.add(r1a);
                        left.add(r1b);
                    }
                    r0 = r1b;
                }
            }

            // advance points
            g0 = g1;
            n0 = n1;
            g1 = g2;
        }

        // finish it off
        Geo rn = g1.subtract(n0);
        Geo ln = g1.add(n0);
        if (capp && err > 0) {
            // end cap
            Geo[] arc = approximateArc(g1, ln, rn, err);
            for (int j = arc.length - 1; j >= 0; j--) {
                right.add(arc[j]);
            }
        } else {
            right.add(rn);
            left.add(ln);
        }

        int ll = right.size();
        int rl = left.size();
        Geo[] result = new Geo[ll + rl];
        for (int i = 0; i < ll; i++) {
            result[i] = (Geo) right.get(i);
        }
        int j = ll;
        for (int i = rl - 1; i >= 0; i--) {
            result[j++] = (Geo) left.get(i);
        }
        return result;
    }

    /** simple vector angle (not geocentric!) */
    static double simpleAngle(Geo p1, Geo p2) {
        return Math.acos(p1.dot(p2) / (p1.length() * p2.length()));
    }

    /**
     * compute a polygonal approximation of an arc centered at pc, beginning at
     * p0 and ending at p1, going clockwise and including the two end points.
     * 
     * @param pc center point
     * @param p0 starting point
     * @param p1 ending point
     * @param err The maximum angle between approximates allowed, in radians.
     *        Smaller values will look better but will result in more returned
     *        points.
     * @return Geo array of arc points
     */
    public static final Geo[] approximateArc(Geo pc, Geo p0, Geo p1, double err) {

        double theta = angle(p0, pc, p1);
        // if the rest of the code is undefined in this situation, just skip it.
        if (Double.isNaN(theta)) {
            return new Geo[] { p0, p1 };
        }

        int n = (int) (2.0 + Math.abs(theta / err)); // number of points
        // (counting the end
        // points)
        Geo[] result = new Geo[n];
        result[0] = p0;
        double dtheta = theta / (n - 1);

        double rho = 0.0; // angle starts at 0 (directly at p0)

        for (int i = 1; i < n - 1; i++) {
            rho += dtheta;
            // Rotate p0 around this so it has the right azimuth.
            result[i] = Rotation.rotate(pc, 2.0 * Math.PI - rho, p0, new Geo());
        }
        result[n - 1] = p1;

        return result;
    }

    public final Geo[] approximateArc(Geo p0, Geo p1, double err) {
        return approximateArc(this, p0, p1, err);
    }

    /** @deprecated use </b>#offset(double, double) */
    public Geo geoAt(double distance, double azimuth) {
        return offset(distance, azimuth);
    }

    /**
     * Returns a Geo that is distance (radians), and azimuth (radians) away from
     * this. this is undefined at the north pole, at which point "azimuth" is
     * undefined.
     * 
     * @param distance distance of this to the target point in radians.
     * @param azimuth Direction of target point from this, in radians, clockwise
     *        from north.
     * @return Geo at distance
     */
    public Geo offset(double distance, double azimuth) {
        return offset(distance, azimuth, new Geo());
    }

    /**
     * Returns a Geo that is distance (radians), and azimuth (radians) away from
     * this. This is undefined at the north pole, at which point "azimuth" is
     * undefined.
     * 
     * @param distance distance of this to the target point in radians.
     * @param azimuth Direction of target point from this, in radians, clockwise
     *        from north.
     * @return ret Do not pass in a null value.
     */
    public Geo offset(double distance, double azimuth, Geo ret) {
        // m is normal the the meridian through this.
        Geo m = this.crossNormalize(north, ret);
        // p is a point on the meridian distance <tt>distance</tt> from this.
        // Geo p = (new Rotation(m, distance)).rotate(this);
        Geo p = Rotation.rotate(m, distance, this, ret);
        // Rotate p around this so it has the right azimuth.
        return Rotation.rotate(this, 2.0 * Math.PI - azimuth, p, ret);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 17;
        long lx = Double.doubleToLongBits(x);
        long ly = Double.doubleToLongBits(y);
        long lz = Double.doubleToLongBits(z);
        result = 31 * result + (int) (lx ^ (lx >>> 32));
        result = 31 * result + (int) (ly ^ (ly >>> 32));
        result = 31 * result + (int) (lz ^ (lz >>> 32));
        return result;
    }

    public static Geo offset(Geo origin, double distance, double azimuth) {
        return origin.offset(distance, azimuth);
    }

    /**
     * 
     * @param origin
     * @param distance
     * @param azimuth
     * @param ret
     * @return ret Do not pass in a null value.
     */
    public static Geo offset(Geo origin, double distance, double azimuth, Geo ret) {
        return origin.offset(distance, azimuth, ret);
    }

    /*
     * //same as offset, except using trig instead of vector mathematics public
     * Geo trig_offset(double distance, double azimuth) { double latr =
     * getLatitudeRadians(); double lonr = getLongitudeRadians();
     * 
     * double coslat = Math.cos(latr); double sinlat = Math.sin(latr); double
     * cosaz = Math.cos(azimuth); double sinaz = Math.sin(azimuth); double sind
     * = Math.sin(distance); double cosd = Math.cos(distance);
     * 
     * return makeGeoRadians(Math.asin(sinlat * cosd + coslat * sind * cosaz),
     * Math.atan2(sind * sinaz, coslat * cosd - sinlat * sind * cosaz) + lonr);
     * }
     */

    //
    // Follows are a series of Geo array operations as useful utilities
    //
    /**
     * convert a String containing space-separated pairs of comma-separated
     * decimal lat-lon pairs into a Geo array.
     */
    public static Geo[] posToGa(String coords) {
        return posToGa(coords.split(" "));
    }

    /**
     * Convert an array of strings with comma-separated decimal lat,lon pairs
     * into a Geo array
     */
    public static Geo[] posToGa(String[] coords) {
        // convert to floating lat/lon degrees
        Geo[] ga = new Geo[coords.length];
        for (int i = 0; i < coords.length; i++) {
            String[] ll = coords[i].split(",");
            ga[i] = Geo.makeGeoDegrees(Double.parseDouble(ll[0]), Double.parseDouble(ll[1]));
        }
        return ga;
    }

    /**
     * Convert a Geo array into a floating point lat lon array (alternating lat
     * and lon values).
     * 
     * @return the ll array provided, or a new array of lla is null.
     */
    public static double[] GaToLLa(Geo[] ga, double[] lla) {
        if (lla == null) {
            lla = new double[2 * ga.length];
        }

        for (int i = 0; i < ga.length; i++) {
            Geo g = ga[i];
            lla[i * 2] = g.getLatitude();
            lla[i * 2 + 1] = g.getLongitude();
        }
        return lla;
    }

    /**
     * Convert a Geo array into a floating point lat lon array (alternating lat
     * and lon values).
     * 
     * @return the ll array provided, or a new array of lla is null.
     */
    public static float[] GaToLLa(Geo[] ga, float[] lla) {
        if (lla == null) {
            lla = new float[2 * ga.length];
        }

        for (int i = 0; i < ga.length; i++) {
            Geo g = ga[i];
            lla[i * 2] = (float) g.getLatitude();
            lla[i * 2 + 1] = (float) g.getLongitude();
        }
        return lla;
    }

    /**
     * Convert a Geo array into a floating point lat lon array (alternating lat
     * and lon values)
     */
    public static float[] GaToLLa(Geo[] ga) {
        return GaToLLa(ga, new float[2 * ga.length]);
    }

    /**
     * Return a Geo array with the duplicates removed. May arbitrarily mutate
     * the input array.
     */
    public static Geo[] removeDups(Geo[] ga) {
        Geo[] r = new Geo[ga.length];
        int p = 0;
        for (int i = 0; i < ga.length; i++) {
            if (p == 0 || !(r[p - 1].equals(ga[i]))) {
                r[p] = ga[i];
                p++;
            }
        }
        if (p != ga.length) {
            Geo[] x = new Geo[p];
            System.arraycopy(r, 0, x, 0, p);
            return x;
        } else {
            return ga;
        }
    }

    /**
     * Convert a float array of alternating lat and lon pairs into a Geo array.
     */
    public static Geo[] LLaToGa(float[] lla) {
        return LLaToGa(lla, true);
    }

    /**
     * Convert a float array of alternating lat and lon pairs into a Geo array.
     */
    public static Geo[] LLaToGa(float[] lla, boolean isDegrees) {
        Geo[] r = new Geo[lla.length / 2];
        for (int i = 0; i < lla.length / 2; i++) {
            if (isDegrees) {
                r[i] = Geo.makeGeoDegrees(lla[i * 2], lla[i * 2 + 1]);
            } else {
                r[i] = Geo.makeGeoRadians(lla[i * 2], lla[i * 2 + 1]);
            }
        }
        return r;
    }

    /**
     * Convert a double array of alternating lat and lon pairs into a Geo array.
     */
    public static Geo[] LLaToGa(double[] lla) {
        return LLaToGa(lla, true);
    }

    /**
     * Convert a double array of alternating lat and lon pairs into a Geo array.
     */
    public static Geo[] LLaToGa(double[] lla, boolean isDegrees) {
        Geo[] r = new Geo[lla.length / 2];
        for (int i = 0; i < lla.length / 2; i++) {
            if (isDegrees) {
                r[i] = Geo.makeGeoDegrees(lla[i * 2], lla[i * 2 + 1]);
            } else {
                r[i] = Geo.makeGeoRadians(lla[i * 2], lla[i * 2 + 1]);
            }
        }
        return r;
    }

    /**
     * return a float array of alternating lat lon pairs where the first and
     * last pair are the same, thus closing the path, by adding a point if
     * needed. Does not mutate the input.
     */
    public static float[] closeLLa(float[] lla) {
        int l = lla.length;
        int s = (l / 2) - 1;
        if (lla[0] == lla[s * 2] && lla[1] == lla[s * 2 + 1]) {
            return lla;
        } else {
            float[] llx = new float[l + 2];
            System.arraycopy(lla, 0, llx, 0, l);
            llx[l] = lla[0];
            llx[l + 1] = lla[1];
            return llx;
        }
    }

    /**
     * return a Geo array where the first and last elements are the same, thus
     * closing the path, by adding a point if needed. Does not mutate the input.
     */
    public static Geo[] closeGa(Geo[] ga) {
        int l = ga.length;
        if (ga[0].equals(ga[l - 1])) {
            return ga;
        } else {
            Geo[] x = new Geo[l + 1];
            System.arraycopy(ga, 0, x, 0, l);
            x[l] = ga[0];
            return x;
        }
    }
}
