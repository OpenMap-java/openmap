// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/coords/DMSLatLonPoint.java,v $
// $RCSfile: DMSLatLonPoint.java,v $
// $Revision: 1.7 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj.coords;

import com.bbn.openmap.util.Assert;

/**
 * Encapsulates a latitude and longitude coordinate in degrees, minutes and
 * seconds as well as the sign.
 * <p>
 * 
 * Original code contributed by Colin Mummery (colin_mummery@yahoo.com)
 */
public class DMSLatLonPoint implements Cloneable {

    private final static double MINUTE = 1 / 60.0;
    private final static double SECOND = 1 / 3600.0;

    /**
     * Indicates if the latitude is negative, the actual int values are always
     * positive.
     */
    public boolean lat_isnegative;
    /**
     * The number of degrees in the latitude.
     */
    public int lat_degrees;
    /**
     * The number of minutes in the latitude.
     */
    public int lat_minutes;
    /**
     * The number of seconds in the latitude.
     */
    public double lat_seconds;

    /**
     * Indicates if the longitude is negative, the actual int values are always
     * positive.
     */
    public boolean lon_isnegative;
    /**
     * The number of degrees in the longitude.
     */
    public int lon_degrees;
    /**
     * The number of minutes in the longitude.
     */
    public int lon_minutes;
    /**
     * The number of seconds in the longitude.
     */
    public double lon_seconds;

    /**
     * Construct a default LatLonPoint with zero values.
     */
    public DMSLatLonPoint() {}

    /**
     * Construct a DMSLatLonPoint from raw int lat/lon. All parameters are
     * checked for their validity.
     * 
     * @param lat_isnegative boolean value indicating the sign of the latitude
     * @param lat_degrees integer number of degrees in latitude
     * @param lat_minutes integer number of minutes in latitude
     * @param lat_seconds float number of seconds in latitude
     * @param lon_isnegative boolean value indicating the sign of the longitude
     * @param lon_degrees integer number of degrees in longitude
     * @param lon_minutes integer number of minutes in longitude
     * @param lon_seconds float number of seconds in longitude
     */
    public DMSLatLonPoint(boolean lat_isnegative, int lat_degrees,
            int lat_minutes, double lat_seconds, boolean lon_isnegative,
            int lon_degrees, int lon_minutes, double lon_seconds) {

        this.lat_isnegative = lat_isnegative;
        this.lat_degrees = (int) LatLonPoint.normalizeLatitude(lat_degrees);
        if (this.lat_degrees < 0) {
            // can't have a negative value
            this.lat_degrees = -this.lat_degrees;
        }
        this.lat_minutes = normalize_value(lat_minutes);
        this.lat_seconds = normalize_value(lat_seconds);

        this.lon_isnegative = lon_isnegative;
        this.lon_degrees = (int) LatLonPoint.wrapLongitude(lon_degrees);
        if (this.lon_degrees < 0) {
            // can't have a negative value
            this.lon_degrees = -this.lon_degrees;
        }
        this.lon_minutes = normalize_value(lon_minutes);
        this.lon_seconds = normalize_value(lon_seconds);
    }

    /**
     * Constructs a new DMSLatLonPoint given a LatLonPoint instance
     * 
     * @param llp A LatLonPoint instance
     */
    public DMSLatLonPoint(LatLonPoint llp) {
        getDMSLatLonPoint(llp, this);
    }

    /**
     * A static method which takes an instance of a LatLongPoint and sets the
     * correct values into an instance of DMSLatLonPoint.
     * 
     * @param llp A LatLonPoint instance.
     * @param dllp A DMSLatLonPoint instance.
     */
    static void getDMSLatLonPoint(LatLonPoint llp, DMSLatLonPoint dllp) {

        // set everything to zero
        dllp.lat_degrees = 0;
        dllp.lat_minutes = 0;
        dllp.lat_seconds = 0f;
        dllp.lat_isnegative = false;
        dllp.lon_degrees = 0;
        dllp.lon_minutes = 0;
        dllp.lon_seconds = 0f;
        dllp.lon_isnegative = false;

        // First do the latitude
        double val = llp.getY();

        if (val < 0) {
            dllp.lat_isnegative = true;
            val = -val;
        } // remove the sign but remember it

        dllp.lat_degrees = (int) Math.floor((double) val);

        if (val >= SECOND) {
            // If it's less then a second then we assume zero...I
            // guess
            // we could round up
            int deg = (int) val;
            // take out the whole degrees
            double rem = val - deg;
            // Do we have anything left to convert to a minute
            if (rem >= MINUTE) { // get the minutes
                int min = (int) (rem * 60);
                dllp.lat_minutes = min;
                rem -= (min * MINUTE);
            }
            // Any seconds left?
            if (rem >= SECOND) { // get the seconds
                double sec = (rem * 3600.0);
                dllp.lat_seconds = sec;
                rem -= (sec * SECOND);
            }
        } else {
            dllp.lat_isnegative = false; // we don't want a negative
            // zero
        }

        // Next repeat the code for longitude, easiest just to repeat
        // it
        val = llp.getX();
        if (val < 0) {
            dllp.lon_isnegative = true;
            val = -val;
        }

        dllp.lon_degrees = (int) Math.floor((double) val);

        if (val >= SECOND) {
            int deg = (int) val;
            double rem = val - deg;
            if (rem >= MINUTE) {
                int min = (int) (rem * 60.0);
                dllp.lon_minutes = min;
                rem -= (min * MINUTE);
            }
            if (rem >= SECOND) {
                double sec = rem * 3600.0;
                dllp.lon_seconds = sec;
                rem -= (sec * SECOND);
            }
        } else {
            dllp.lon_isnegative = false;
        }
    }

    /**
     * Return a LatLonPoint from this DMSLatLonPoint. The LatLonPoint is
     * allocated here.
     * 
     * @return LatLonPoint, full of decimal degrees.
     */
    public LatLonPoint getLatLonPoint() {
        return getLatLonPoint(null);
    }

    /**
     * Return a LatLonPoint from this DMSLatLonPoint. The LatLonPoint is
     * allocated here if the llp is null. If it's not null, then the llp is
     * loaded with the proper values.
     * 
     * @param llp the LatLonPoint to load up.
     * @return LatLonPoint, full of decimal degrees.
     */
    public LatLonPoint getLatLonPoint(LatLonPoint llp) {
        double lat = getDecimalLatitude();
        double lon = getDecimalLongitude();
        if (llp == null) {
            return new LatLonPoint.Double(lat, lon);
        } else {
            llp.setLatLon(lat, lon);
            return llp;
        }
    }

    /**
     * Returns the latitude as decimal degrees.
     * 
     * @return A float value for the latitude
     */
    public double getDecimalLatitude() {
        double val = (lat_degrees + (lat_minutes * MINUTE) + (lat_seconds * SECOND));
        return (lat_isnegative) ? -val : val;
    }

    /**
     * Returns the longitude as decimal degrees.
     * 
     * @return A float value for the longitude
     */
    public double getDecimalLongitude() {
        double val = (lon_degrees + (lon_minutes * MINUTE) + (lon_seconds * SECOND));
        return (lon_isnegative) ? -val : val;
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return String representation
     */
    public String toString() {
        return "DMSLatLonPoint[lat_isnegative = " + lat_isnegative
                + ", lat_degrees = " + lat_degrees + ", lat_minutes = "
                + lat_minutes + ", lat_seconds = " + lat_seconds
                + ", lon_isnegative = " + lon_isnegative + ", lon_degrees = "
                + lon_degrees + ", lon_minutes = " + lon_minutes
                + ", lon_seconds = " + lon_seconds + "]";
    }

    /**
     * Set DMSLatLonPoint. Sets the current instance values to be the same as
     * another DMSLatLonPoint instance.
     * 
     * @param llpt DMSLatLonPoint
     */
    public void setDMSLatLon(DMSLatLonPoint llpt) {
        lat_isnegative = llpt.lat_isnegative;
        lat_degrees = llpt.lat_degrees;
        lat_minutes = llpt.lat_minutes;
        lat_seconds = llpt.lat_seconds;
        lon_isnegative = llpt.lon_isnegative;
        lon_degrees = llpt.lon_degrees;
        lon_minutes = llpt.lon_minutes;
        lon_seconds = llpt.lon_seconds;
    }

    /**
     * Clone the DMSLatLonPoint.
     * 
     * @return clone
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.assertExp(false, "DMSLatLonPoint: internal error!");
            return null;// statement not reached
        }
    }

    /**
     * Determines whether two DMSLatLonPoints are exactly equal.
     * 
     * @param obj Object
     * @return boolean
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DMSLatLonPoint pt = (DMSLatLonPoint) obj;
        return (pt.lat_isnegative == lat_isnegative
                && pt.lat_degrees == lat_degrees
                && pt.lat_minutes == lat_degrees
                && pt.lat_seconds == lat_seconds
                && pt.lon_isnegative == lon_isnegative
                && pt.lon_degrees == lon_degrees
                && pt.lon_minutes == lon_minutes
                && pt.lon_seconds == lon_seconds);
    }

    /**
     * Sets the minutes and seconds to something sane.
     * 
     * @param val an int value for the minutes or seconds
     * @return int value normalized
     */
    final public static int normalize_value(int val) {
        val %= 60;
        if (val < 0) {
            val += 60;
        }
        return val;
    }

    /**
     * Sets the minutes and seconds to something sane.
     * 
     * @param val an double value for the minutes or seconds
     * @return float value normalized
     */
    final public static double normalize_value(double val) {
        val %= 60f;
        if (val < 0f) {
            val += 60f;
        }
        return val;
    }

    /**
     * Generate a hash value for the point. Hash by spreading the values across
     * a 32 bit int, ignoring the sign allow 8 bits (max 255) for degrees, 7
     * bits (max 127) for (minutes + seconds) so the total is 30 bits.
     * 
     * @return An int hash value representing the point.
     */
    public int hashCode() {
        return (lat_degrees | lon_degrees << 8
                | (lat_minutes + (int) lat_seconds) << 16 | (lon_minutes + (int) lon_seconds) << 23);
    }
}