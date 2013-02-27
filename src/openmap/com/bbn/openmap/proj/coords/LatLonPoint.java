//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: LatLonPoint.java,v $
//$Revision: 1.2 $
//$Date: 2007/12/03 23:39:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.ProjMath;

/**
 * A Point2D representation of LatLonPoints, used integrate with the
 * Projections. These LatLonPoints wrap their internal decimal degree values so
 * the latitude are between -90 and 90 and the longitudes are between -180 and
 * 180. Radian values are precalculated and held within the object.
 * <P>
 * 
 * The LatLonPoint is an abstract class and can't be instantiated directly. You
 * need to create a Float or Double version of a LatLonPoint, much like the
 * Point2D object.
 * <P>
 * 
 * @author dietrick
 */
public abstract class LatLonPoint extends Point2D implements Cloneable, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4416029542303298672L;
    public final static double NORTH_POLE = 90.0;
    public final static double SOUTH_POLE = -NORTH_POLE;
    public final static double DATELINE = 180.0;
    public final static double LON_RANGE = 360.0;

    protected LatLonPoint() {
    }

    /**
     * Factory method that will create a LatLonPoint.Float from a Point2D
     * object. If pt2D is already a LatLonPoint.Float object, it is simply
     * returned.
     * 
     * @param pt2D
     * @return a LatLonPoint.Float object.
     */
    public static LatLonPoint getFloat(Point2D pt2D) {
        if (pt2D instanceof Float) {
            return (Float) pt2D;
        } else {
            return new Float(pt2D);
        }
    }

    /**
     * Factory method that will create a LatLonPoint.Double from a Point2D
     * object. If pt2D is already a LatLonPoint.Double object, it is simply
     * returned.
     * 
     * @param pt2D
     * @return a LatLonPoint.Double object.
     */
    public static LatLonPoint getDouble(Point2D pt2D) {
        if (pt2D instanceof Double) {
            return (Double) pt2D;
        } else {
            return new Double(pt2D);
        }
    }

    /**
     * Set the latitude, longitude for this point.
     * 
     * @param lat decimal degree latitude
     * @param lon decimal degree longitude.
     */
    public abstract void setLatLon(double lat, double lon);

    /**
     * Set the latitude, longitude for this point, with the option of noting
     * whether the values are in degrees or radians.
     * 
     * @param lat latitude
     * @param lon longitude.
     * @param isRadians true of values are radians.
     */
    public abstract void setLatLon(double lat, double lon, boolean isRadians);

    /**
     * @return decimal degree longitude as a float.
     */
    public abstract float getLongitude();

    /**
     * @return decimal degree latitude as a float.
     */
    public abstract float getLatitude();

    /**
     * @return radian longitude value.
     */
    public abstract double getRadLon();

    /**
     * @return radian latitude value.
     */
    public abstract double getRadLat();

    /**
     * Set decimal degree latitude.
     */
    public abstract void setLatitude(double lat);

    /**
     * Set decimal degree longitude.
     */
    public abstract void setLongitude(double lon);

    /**
     * The Float version of a LatLonPoint, where coordinates are held to float
     * precision.
     */
    public static class Float extends LatLonPoint {

        /**
         * 
         */
        private static final long serialVersionUID = -2447464428275551182L;
        protected float lat;
        protected float lon;
        protected transient float radLat;
        protected transient float radLon;

        /**
         * Default constructor, values set to 0, 0.
         */
        public Float() {
        }

        /**
         * @param lat decimal degree latitude.
         * @param lon decimal degree longitude.
         */
        public Float(float lat, float lon) {
            setLatLon(lat, lon, false);
        }

        /**
         * @param lat latitude
         * @param lon longitude
         * @param isRadian true if values are radians, false if decimal degrees.
         */
        public Float(float lat, float lon, boolean isRadian) {
            setLatLon(lat, lon, isRadian);
        }

        /**
         * Create Float version from another LatLonPoint.
         * 
         * @param llp
         */
        public Float(LatLonPoint llp) {
            setLatLon(llp.getLatitude(), llp.getLongitude(), false);
        }

        /**
         * Create Float version from Point2D object, where the x, y values are
         * expected to be decimal degrees.
         * 
         * @param pt2D
         */
        public Float(Point2D pt2D) {
            setLatLon(pt2D.getY(), pt2D.getX(), false);
        }

        /**
         * Point2D method, inheriting signature!!
         * 
         * @param x longitude value in decimal degrees.
         * @param y latitude value in decimal degrees.
         */
        public void setLocation(float x, float y) {
            setLatLon(y, x, false);
        }

        /**
         * Point2D method, inheriting signature!!
         * 
         * @param x longitude value in decimal degrees.
         * @param y latitude value in decimal degrees.
         */
        public void setLocation(double x, double y) {
            setLatLon((float) y, (float) x, false);
        }

        /**
         * Set lat/lon values.
         * 
         * @param lat decimal degree latitude.
         * @param lon decimal degree longitude.
         */
        public void setLatLon(float lat, float lon) {
            setLatLon(lat, lon, false);
        }

        /**
         * Set lat/lon values.
         * 
         * @param lat decimal degree latitude.
         * @param lon decimal degree longitude.
         */
        public void setLatLon(double lat, double lon) {
            setLatLon((float) lat, (float) lon, false);
        }

        /**
         * Set lat/lon values.
         * 
         * @param lat latitude.
         * @param lon longitude.
         * @param isRadians true if values are radians.
         */
        public void setLatLon(double lat, double lon, boolean isRadians) {
            if (isRadians) {
                radLat = (float) lat;
                radLon = (float) lon;
                this.lat = (float) ProjMath.radToDeg(lat);
                this.lon = (float) ProjMath.radToDeg(lon);
            } else {
                this.lat = (float) Double.normalizeLatitude(lat);
                this.lon = (float) Double.wrapLongitude(lon);
                radLat = (float) ProjMath.degToRad(lat);
                radLon = (float) ProjMath.degToRad(lon);
            }
        }

        /**
         * Set lat/lon values.
         * 
         * @param lat latitude.
         * @param lon longitude.
         * @param isRadians true if values are radians.
         */
        public void setLatLon(float lat, float lon, boolean isRadians) {
            if (isRadians) {
                radLat = lat;
                radLon = lon;
                this.lat = ProjMath.radToDeg(lat);
                this.lon = ProjMath.radToDeg(lon);
            } else {
                this.lat = normalizeLatitude(lat);
                this.lon = wrapLongitude(lon);
                radLat = ProjMath.degToRad(lat);
                radLon = ProjMath.degToRad(lon);
            }
        }

        /**
         * Point2D method.
         * 
         * @return decimal degree longitude.
         * 
         * @see java.awt.geom.Point2D#getX()
         */
        public double getX() {
            return (double) lon;
        }

        /**
         * Point2D method
         * 
         * @return decimal degree latitude.
         */
        public double getY() {
            return (double) lat;
        }

        /**
         * @return decimal degree longitude.
         */
        public float getLongitude() {
            return lon;
        }

        /**
         * @return decimal degree latitude.
         */
        public float getLatitude() {
            return lat;
        }

        /**
         * @return radian longitude.
         */
        public double getRadLon() {
            return (double) radLon;
        }

        /**
         * @return radian latitude.
         */
        public double getRadLat() {
            return (double) radLat;
        }

        /**
         * Set latitude.
         * 
         * @param lat latitude in decimal degrees
         */
        public void setLatitude(float lat) {
            this.lat = normalizeLatitude(lat);
            radLat = ProjMath.degToRad(lat);
        }

        /**
         * Set latitude.
         * 
         * @param lat latitude in decimal degrees
         */
        public void setLatitude(double lat) {
            setLatitude((float) lat);
        }

        /**
         * Set longitude.
         * 
         * @param lon longitude in decimal degrees
         */
        public void setLongitude(float lon) {
            this.lon = wrapLongitude(lon);
            radLon = ProjMath.degToRad(lon);
        }

        /**
         * Set longitude.
         * 
         * @param lon longitude in decimal degrees
         */
        public void setLongitude(double lon) {
            setLongitude((float) lon);
        }

        /**
         * Find a LatLonPoint a distance and direction away from this point,
         * based on the spherical earth model.
         * 
         * @param dist distance, in radians.
         * @param az radians of azimuth (direction) east of north (-PI &lt;= Az
         *        &lt; PI)
         * @return LatLonPoint result
         */
        public LatLonPoint getPoint(float dist, float az) {
            return GreatCircle.sphericalBetween(radLat, radLon, dist, az);
        }

        /**
         * Write object.
         * 
         * @param s DataOutputStream
         */
        public void write(DataOutputStream s) throws IOException {
            // Write my information
            s.writeFloat(lat);
            s.writeFloat(lon);
        }

        /**
         * Read object. Assumes that the floats read off the stream will be in
         * decimal degrees. Latitude read off the stream first, then longitude.
         * 
         * @param s DataInputStream
         */
        public void read(DataInputStream s) throws IOException {
            setLatLon(s.readFloat(), s.readFloat(), false);
        }

        /**
         * Read object. Latitude read off the stream first, then longitude.
         * 
         * @param s DataInputStream
         * @param inRadians if true, the floats read off stream will be
         *        considered to be radians. Otherwise, they will be considered
         *        to be decimal degrees.
         */
        public void read(DataInputStream s, boolean inRadians) throws IOException {
            setLatLon(s.readFloat(), s.readFloat(), inRadians);
        }

        /**
         * Calculate the <code>radlat_</code> and <code>radlon_</code> instance
         * variables upon deserialization. Also, check <code>lat_</code> and
         * <code>lon_</code> for safety; someone may have tampered with the
         * stream.
         * 
         * @param stream Stream to read <code>lat_</code> and <code>lon_</code>
         *        from.
         */
        private void readObject(java.io.ObjectInputStream stream)
                throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            lat = normalizeLatitude(lat);
            lon = wrapLongitude(lon);
            radLat = ProjMath.degToRad(lat);
            radLon = ProjMath.degToRad(lon);
        }

        public String toString() {
            return "LatLonPoint.Float[lat=" + lat + ",lon=" + lon + "]";
        }
    }

    /**
     * Double precision version of LatLonPoint.
     * 
     * @author dietrick
     */
    public static class Double extends LatLonPoint {
        /**
         * 
         */
        private static final long serialVersionUID = -7463055211717523471L;

        protected double lat;
        protected double lon;
        protected transient double radLat;
        protected transient double radLon;

        /**
         * Default constructor, values set to 0, 0.
         */
        public Double() {
        }

        /**
         * Set the latitude, longitude for this point in decimal degrees.
         * 
         * @param lat latitude
         * @param lon longitude.
         */
        public Double(double lat, double lon) {
            setLatLon(lat, lon, false);
        }

        /**
         * Set the latitude, longitude for this point, with the option of noting
         * whether the values are in degrees or radians.
         * 
         * @param lat latitude
         * @param lon longitude.
         * @param isRadian true of values are radians.
         */
        public Double(double lat, double lon, boolean isRadian) {
            setLatLon(lat, lon, isRadian);
        }

        /**
         * Create Double version from another LatLonPoint.
         * 
         * @param llp
         */
        public Double(LatLonPoint llp) {
            setLatLon(llp.getY(), llp.getX(), false);
        }

        /**
         * Create Double version from Point2D object, where the x, y values are
         * expected to be decimal degrees.
         * 
         * @param pt2D
         */
        public Double(Point2D pt2D) {
            setLatLon(pt2D.getY(), pt2D.getX(), false);
        }

        /**
         * Point2D method, inheriting signature!!
         * 
         * @param x longitude value in decimal degrees.
         * @param y latitude value in decimal degrees.
         */
        public void setLocation(double x, double y) {
            setLatLon(y, x, false);
        }

        /**
         * Set latitude and longitude.
         * 
         * @param lat latitude in decimal degrees.
         * @param lon longitude in decimal degrees.
         */
        public void setLatLon(double lat, double lon) {
            setLatLon(lat, lon, false);
        }

        /**
         * Set latitude and longitude.
         * 
         * @param lat latitude.
         * @param lon longitude.
         * @param isRadians true if lat/lon values are radians.
         */
        public void setLatLon(double lat, double lon, boolean isRadians) {
            if (isRadians) {
                radLat = lat;
                radLon = lon;
                this.lat = ProjMath.radToDeg(lat);
                this.lon = ProjMath.radToDeg(lon);
            } else {
                this.lat = normalizeLatitude(lat);
                this.lon = wrapLongitude(lon);
                radLat = ProjMath.degToRad(lat);
                radLon = ProjMath.degToRad(lon);
            }
        }

        /**
         * @return longitude in decimal degrees.
         */
        public double getX() {
            return lon;
        }

        /**
         * @return latitude in decimal degrees.
         */
        public double getY() {
            return lat;
        }

        /**
         * @return float latitude in decimal degrees.
         */
        public float getLatitude() {
            return (float) lat;
        }

        /**
         * @return float longitude in decimal degrees.
         */
        public float getLongitude() {
            return (float) lon;
        }

        /**
         * @return radian longitude.
         */
        public double getRadLon() {
            return radLon;
        }

        /**
         * @return radian latitude.
         */
        public double getRadLat() {
            return radLat;
        }

        /**
         * Set latitude.
         * 
         * @param lat latitude in decimal degrees
         */
        public void setLatitude(double lat) {
            this.lat = normalizeLatitude(lat);
            radLat = ProjMath.degToRad(lat);
        }

        /**
         * Set longitude.
         * 
         * @param lon longitude in decimal degrees
         */
        public void setLongitude(double lon) {
            this.lon = wrapLongitude(lon);
            radLon = ProjMath.degToRad(lon);
        }

        /**
         * Find a LatLonPoint a distance and direction away from this point,
         * based on the spherical earth model.
         * 
         * @param dist distance, in radians.
         * @param az radians of azimuth (direction) east of north (-PI &lt;= Az
         *        &lt; PI)
         * @return LatLonPoint result
         */
        public LatLonPoint getPoint(double dist, double az) {
            return GreatCircle.sphericalBetween(radLat, radLon, dist, az);
        }

        /**
         * Write object.
         * 
         * @param s DataOutputStream
         */
        public void write(DataOutputStream s) throws IOException {
            // Write my information
            s.writeDouble(lat);
            s.writeDouble(lon);
        }

        /**
         * Read object. Assumes that the floats read off the stream will be in
         * decimal degrees. Latitude read off the stream first, then longitude.
         * 
         * @param s DataInputStream
         */
        public void read(DataInputStream s) throws IOException {
            setLatLon(s.readDouble(), s.readDouble(), false);
        }

        /**
         * Read object. Latitude read off the stream first, then longitude.
         * 
         * @param s DataInputStream
         * @param inRadians if true, the floats read off stream will be
         *        considered to be radians. Otherwise, they will be considered
         *        to be decimal degrees.
         */
        public void read(DataInputStream s, boolean inRadians) throws IOException {
            setLatLon(s.readDouble(), s.readDouble(), inRadians);
        }

        /**
         * Calculate the <code>radLat</code> and <code>radLon</code> instance
         * variables upon deserialization. Also, check <code>lat</code> and
         * <code>lon</code> for safety; someone may have tampered with the
         * stream.
         * 
         * @param stream Stream to read <code>lat</code> and <code>lon</code>
         *        from.
         */
        private void readObject(java.io.ObjectInputStream stream)
                throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            lat = normalizeLatitude(lat);
            lon = wrapLongitude(lon);
            radLat = ProjMath.degToRad(lat);
            radLon = ProjMath.degToRad(lon);
        }

        public String toString() {
            return "LatLonPoint.Double[lat=" + lat + ",lon=" + lon + "]";
        }

    }

    /**
     * Set location values from another lat/lon point.
     * 
     * @param llp
     */
    public void setLatLon(LatLonPoint llp) {
        setLatLon(llp.getY(), llp.getX(), false);
    }

    /**
     * Ensure latitude is between the poles.
     * 
     * @param lat
     * @return latitude greater than or equal to -90 and less than or equal to
     *         90.
     */
    public final static float normalizeLatitude(float lat) {
        return (float) normalizeLatitude((double) lat);
    }

    /**
     * Sets latitude to something sane.
     * 
     * @param lat latitude in decimal degrees
     * @return float normalized latitude in decimal degrees (&minus;90&deg; &le;
     *         &phi; &le; 90&deg;)
     */
    public final static double normalizeLatitude(double lat) {
        if (lat > NORTH_POLE) {
            lat = NORTH_POLE;
        }
        if (lat < SOUTH_POLE) {
            lat = SOUTH_POLE;
        }
        return lat;
    }

    /**
     * Ensure the longitude is between the date line.
     * 
     * @param lon
     * @return longitude that is smaller than or equal to 180 and greater than
     *         or equal to -180
     */
    public final static float wrapLongitude(float lon) {
        return (float) wrapLongitude((double) lon);
    }

    /**
     * Sets longitude to something sane.
     * 
     * @param lon longitude in decimal degrees
     * @return float wrapped longitude in decimal degrees (&minus;180&deg; &le;
     *         &lambda; &le; 180&deg;)
     */
    public final static double wrapLongitude(double lon) {
        if ((lon < -DATELINE) || (lon > DATELINE)) {
            // System.out.print("LatLonPoint: wrapping longitude "
            // +
            // lon);
            lon += DATELINE;
            lon = lon % LON_RANGE;
            lon = (lon < 0) ? DATELINE + lon : -DATELINE + lon;
            // Debug.output(" to " + lon);
        }
        return lon;
    }

    /**
     * Check if latitude is bogus. Latitude is invalid if lat &gt; 90&deg; or if
     * lat &lt; &minus;90&deg;.
     * 
     * @param lat latitude in decimal degrees
     * @return boolean true if latitude is invalid
     */
    public static boolean isInvalidLatitude(double lat) {
        return ((lat > NORTH_POLE) || (lat < SOUTH_POLE));
    }

    /**
     * Check if longitude is bogus. Longitude is invalid if lon &gt; 180&deg; or
     * if lon &lt; &minus;180&deg;.
     * 
     * @param lon longitude in decimal degrees
     * @return boolean true if longitude is invalid
     */
    public static boolean isInvalidLongitude(double lon) {
        return ((lon < -DATELINE) || (lon > DATELINE));
    }

    /**
     * Determines whether two LatLonPoints are equal.
     * 
     * @param obj Object
     * @return Whether the two points are equal up to a tolerance of 10 <sup>-5
     *         </sup> degrees in latitude and longitude.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LatLonPoint pt = (LatLonPoint) obj;
        return (MoreMath.approximately_equal(getY(), pt.getY()) && MoreMath.approximately_equal(getX(), pt.getX()));
    }

    /**
     * Find the distance to another LatLonPoint, based on a earth spherical
     * model.
     * 
     * @param toPoint LatLonPoint
     * @return distance, in radians. You can use an com.bbn.openmap.proj.Length
     *         to convert the radians to other units.
     */
    public double distance(LatLonPoint toPoint) {
        return GreatCircle.sphericalDistance(getRadLat(), getRadLon(), toPoint.getRadLat(), toPoint.getRadLon());
    }

    /**
     * Find the azimuth to another point, based on the spherical earth model.
     * 
     * @param toPoint LatLonPoint
     * @return the azimuth `Az' east of north from this point bearing toward the
     *         one provided as an argument.(-PI &lt;= Az &lt;= PI).
     * 
     */
    public double azimuth(LatLonPoint toPoint) {
        return GreatCircle.sphericalAzimuth(getRadLat(), getRadLon(), toPoint.getRadLat(), toPoint.getRadLon());
    }

    /**
     * Get a new LatLonPoint a distance and azimuth from another point, based on
     * the spherical earth model.
     * 
     * @param distance radians
     * @param azimuth radians
     * @return LatLonPoint that is distance and azimuth away from this one.
     */
    public LatLonPoint getPoint(double distance, double azimuth) {
        return GreatCircle.sphericalBetween(getRadLat(), getRadLon(), distance, azimuth);
    }

}
