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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/LatLonPoint.java,v $
// $RCSfile: LatLonPoint.java,v $
// $Revision: 1.3 $
// $Date: 2004/09/29 21:43:34 $
// $Author: dietrick $
// 
// **********************************************************************


package  com.bbn.openmap;

import  java.io.*;
import  com.bbn.openmap.proj.*;
import  com.bbn.openmap.util.Assert;


/**
 * Encapsulates latitude and longitude coordinates in decimal degrees.
 * Normalizes the internal representation of latitude and longitude.
 * <p>
 * <strong>Normalized Latitude:</strong><br>
 * &minus;90&deg; &lt;= &phi; &lt;= 90&deg;
 * <p>
 * <strong>Normalized Longitude:</strong><br>
 * &minus;180&deg; &le; &lambda; &le; 180&deg;
 */
public class LatLonPoint implements Cloneable, Serializable {
    // SOUTH_POLE <= phi <= NORTH_POLE
    // -DATELINE <= lambda <= DATELINE
    public final static float NORTH_POLE = 90.0f;
    public final static float SOUTH_POLE = -NORTH_POLE;
    public final static float DATELINE = 180.0f;
    public final static float LON_RANGE = 360.0f;
    // initialize to something sane
    protected float lat_ = 0.0f;
    protected float lon_ = 0.0f;

    public final static float EQUIVALENT_TOLERANCE = 0.00001f;

    /**
     * Construct a default LatLonPoint.
     */
    public LatLonPoint() {
    }

    /**
     * Construct a LatLonPoint from raw float lat/lon in decimal degrees.
     *
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public LatLonPoint(float lat, float lon) {
        setLatLon(lat, lon);
    }

    /**
     * Construct a LatLonPoint from raw float lat/lon in radians.
     *
     * @param lat latitude in radians
     * @param lon longitude in radians
     * @param isRadian placeholder indicates radians
     */
    public LatLonPoint(float lat, float lon, boolean isRadian) {
        setLatLon(lat, lon, isRadian);
    }

    /**
     * Copy construct a LatLonPoint.
     *
     * @param pt LatLonPoint
     */
    public LatLonPoint(LatLonPoint pt) {
        lat_ = pt.lat_;
        lon_ = pt.lon_;
        radlat_ = pt.radlat_;
        radlon_ = pt.radlon_;
    }

    /**
     * Construct a LatLonPoint from raw double lat/lon.
     *
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public LatLonPoint(double lat, double lon) {
        this((float)lat, (float)lon);
    }

    /* uncomment to see how many are being used and thrown away...
     protected void finalize() {
     Debug.output("finalized " + this);
     }
     */

    /**
     * Returns a string representation of the object.
     * @return String representation
     */
    public String toString() {
        return  "LatLonPoint[lat=" + lat_ + ",lon=" + lon_ + "]";
    }

    /**
     * Clone the LatLonPoint.
     * @return clone
     */
    public Object clone() {
        try {
            return  super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.assertExp(false, "LatLonPoint: internal error!");
            return  null;       // statement not reached
        }
    }

    /**
     * Set latitude.
     * @param lat latitude in decimal degrees
     */
    public void setLatitude(float lat) {
        lat_ = normalize_latitude(lat);
        radlat_ = ProjMath.degToRad(lat_);
    }

    /**
     * Set longitude.
     * @param lon longitude in decimal degrees
     */
    public void setLongitude(float lon) {
        lon_ = wrap_longitude(lon);
        radlon_ = ProjMath.degToRad(lon_);
    }

    /**
     * Set latitude and longitude.
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public void setLatLon(float lat, float lon) {
        lat_ = normalize_latitude(lat);
        lon_ = wrap_longitude(lon);
        radlat_ = ProjMath.degToRad(lat_);
        radlon_ = ProjMath.degToRad(lon_);
    }

    /**
     * Set latitude and longitude.
     * @param lat latitude in radians
     * @param lon longitude in radians
     * @param isRadian placeholder indicates radians
     */
    public void setLatLon(float lat, float lon, boolean isRadian) {
        if (isRadian) {
            radlat_ = lat;
            radlon_ = lon;
            lat_ = normalize_latitude(ProjMath.radToDeg(radlat_));
            lon_ = wrap_longitude(ProjMath.radToDeg(radlon_));
        } else {
            setLatLon(lat, lon);
        }
    }

    /**
     * Set LatLonPoint.
     * @param llpt LatLonPoint
     */
    public void setLatLon(LatLonPoint llpt) {
        lat_ = llpt.lat_;
        lon_ = llpt.lon_;
        radlat_ = llpt.radlat_;
        radlon_ = llpt.radlon_;
    }

    /**
     * Get normalized latitude.
     * @return float latitude in decimal degrees
     * (&minus;90&deg; &le; &phi; &le; 90&deg;)
     */
    public float getLatitude() {
        return  lat_;
    }

    /**
     * Get wrapped longitude.
     * @return float longitude in decimal degrees
     * (&minus;180&deg; &le; &lambda; &le; 180&deg;)
     */
    public float getLongitude() {
        return  lon_;
    }

    /**
     * Determines whether two LatLonPoints are equal.
     * @param obj Object
     * @return Whether the two points are equal up to a tolerance of
     * 10<sup>-5</sup> degrees in latitude and longitude.
     */
    public boolean equals(Object obj) {
        if (obj instanceof LatLonPoint) {
            LatLonPoint pt = (LatLonPoint)obj;
            return  (MoreMath.approximately_equal(lat_, pt.lat_, EQUIVALENT_TOLERANCE) && MoreMath.approximately_equal(lon_, pt.lon_, EQUIVALENT_TOLERANCE));
        }
        return  false;
    }

    /**
     * Hash the lat/lon value.
     * <p>
     * @return int hash value
     */
    public int hashCode() {
        return  ProjMath.hashLatLon(lat_, lon_);
    }

    /**
     * Write object.
     * @param s DataOutputStream
     */
    public void write(DataOutputStream s) throws IOException {
        // Write my information
        s.writeFloat(lat_);
        s.writeFloat(lon_);
    }

    /**
     * Read object.  Assumes that the floats read off the stream will
     * be in decimal degrees.  Latitude read off the stream first,
     * then longitude.
     * @param s DataInputStream
     */
    public void read(DataInputStream s) throws IOException {
        setLatLon(s.readFloat(), s.readFloat());
    }

    /**
     * Read object.  Latitude read off the stream first, then longitude.
     * @param s DataInputStream
     * @param inRadians if true, the floats read off stream will be
     * considered to be radians.  Otherwise, they will be considered
     * to be decimal degrees.
     */
    public void read(DataInputStream s, boolean inRadians) throws IOException {
        if (inRadians) {
            setLatLon(s.readFloat(), s.readFloat(), inRadians);
        } else {
            read(s);
        }
    }

    /**
     * Sets latitude to something sane.
     * @param lat latitude in decimal degrees
     * @return float normalized latitude in decimal degrees
     * (&minus;90&deg; &le; &phi; &le; 90&deg;)
     */
    final public static float normalize_latitude(float lat) {
        if (lat > NORTH_POLE) {
            lat = NORTH_POLE;
        }
        if (lat < SOUTH_POLE) {
            lat = SOUTH_POLE;
        }
        return  lat;
    }

    /**
     * Sets longitude to something sane.
     * @param lon longitude in decimal degrees
     * @return float wrapped longitude in decimal degrees
     * (&minus;180&deg; &le; &lambda; &le; 180&deg;)
     */
    final public static float wrap_longitude(float lon) {
        if ((lon < -DATELINE) || (lon > DATELINE)) {
            //System.out.print("LatLonPoint: wrapping longitude " + lon);
            lon += DATELINE;
            lon = lon%LON_RANGE;
            lon = (lon < 0) ? DATELINE + lon : -DATELINE + lon;
            //Debug.output(" to " + lon);
        }
        return  lon;
    }

    /**
     * Check if latitude is bogus.
     * Latitude is invalid if lat &gt; 90&deg; or if lat &lt; &minus;90&deg;.
     * @param lat latitude in decimal degrees
     * @return boolean true if latitude is invalid
     */
    public static boolean isInvalidLatitude(float lat) {
        return  ((lat > NORTH_POLE) || (lat < SOUTH_POLE));
    }

    /**
     * Check if longitude is bogus.
     * Longitude is invalid if lon &gt; 180&deg; or if lon &lt; &minus;180&deg;.
     * @param lon longitude in decimal degrees
     * @return boolean true if longitude is invalid
     */
    public static boolean isInvalidLongitude(float lon) {
        return  ((lon < -DATELINE) || (lon > DATELINE));
    }

    /**
     * Calculate the <code>radlat_</code> and <code>radlon_</code>
     * instance variables upon deserialization.
     * Also, check <code>lat_</code> and <code>lon_</code> for safety;
     * someone may have tampered with the stream.
     * @param stream Stream to read <code>lat_</code> and <code>lon_</code> from.
     */
    private void readObject(java.io.ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        lat_ = normalize_latitude(lat_);
        lon_ = wrap_longitude(lon_);
        radlat_ = ProjMath.degToRad(lat_);
        radlon_ = ProjMath.degToRad(lon_);
    }
    
	/**
	 * Find a LatLonPoint a distance and direction away from this
	 * point, based on the sphercal earth model.
	 * @param dist distance, in radians.
	 * @param az radians of azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
	 * @return LatLonPoint result
	 */
    public LatLonPoint getPoint(float dist, float az) {
		return GreatCircle.spherical_between(radlat_, radlon_, dist, az);
    }

    /**
     * Find the distance to another LatLonPoint, based on a earth spherical model.
     * @param toPoint LatLonPoint
     * @return distance, in radians.  You can use an com.bbn.openmap.proj.Length 
     * to convert the radians to other units. 
     */
    	public float distance(LatLonPoint toPoint) {
    		return GreatCircle.spherical_distance(radlat_, radlon_, toPoint.radlat_, toPoint.radlon_);
    	}
    	
    /**
     * Find the azimuth to another point, based on the sphercal earth model.
     * @param toPoint LatLonPoint
     * @return the azimuth `Az' east of north from this point 
     * bearing toward the one provided as an argument.(-PI &lt;= Az &lt;= PI).
     * 
     */
    public float azimuth(LatLonPoint toPoint) {
    		return GreatCircle.spherical_azimuth(radlat_, radlon_, toPoint.radlat_, toPoint.radlon_);
    }
    
    /**
     * Used by the projection code for read-only quick access.
     * This is meant for quick backdoor access by the projection library.
     * Modify at your own risk!
     * @see #lat_
     */
    public transient float radlat_ = 0.0f;
    /**
     * Used by the projection code for read-only quick access.
     * This is meant for quick backdoor access by the projection library.
     * Modify at your own risk!
     * @see #lon_
     */
    public transient float radlon_ = 0.0f;
}
