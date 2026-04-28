//**********************************************************************
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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/coords/UPSPoint.java,v $
// $RCSfile: UPSPoint.java,v $
// $Revision: 1.9 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.proj.coords;

/**
 * Class UPSPoint.
 * <p>
 * This class encapsulates a UPS point.
 * <p>
 * UPS is known as (Universal Polar Stereographic).
 * <p>
 * UPS is a coordinate system of Earth's polar regions north of
 * <p>
 * 84 degrees north, and south of 80 degrees south.
 * <p>
 * This class defaults to WGS-1984.
 * <p>
 * 
 * @author Bob Hayes
 */
public class UPSPoint {

    /** Easting */
    protected double easting;
    /** Northing */
    protected double northing;
    /** Hemisphere */
    // protected boolean southernHemisphere;
    private double Degree = Math.PI / 180.0;

    // WGS-1984: 6378137.0, 298.257223563 0.00669438d

    /** Constructor for the UPSPoint object */
    public UPSPoint() {
        this.easting = 0;
        this.northing = 0;
        // southernHemisphere = false;
    }

    /**
     * Constructor for the UPSPoint object
     * 
     * @param easting easting
     * @param northing northing
     */
    public UPSPoint(double easting, double northing) {
        this.easting = easting;
        this.northing = northing;
        // southernHemisphere = southern;
    }

    /**
     * Static method to create a UPSPoint object from lat/lon coordinates.
     * Method avoids conflict with (double, double) constructor.
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public static UPSPoint createUPSPoint(double lat, double lon) {
        UPSPoint ups = new UPSPoint();
        ups.toUPS(lat, lon);
        return ups;
    }

    /**
     * Constructor for the UPSPoint object
     * 
     * @param llpt LatLonPoint
     */
    public UPSPoint(LatLonPoint llpt) {
        this.toUPS(llpt.getY(), llpt.getX());
    }

    /**
     * Converts a lat-lon pair to UPS point
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public void toUPS(double lat, double lon) {
        double a = 0;
        double t = 0;
        double e = 0;
        double es = 0;
        double rho = 0;
        double x;
        double y;
        final double k0 = 0.994;

        double lambda = lon * Degree;
        double phi = Math.abs(lat * Degree);
        // double phi = (lat * Degree);

        a = 6378137.0;
        es = 0.00669438d;

        e = Math.sqrt(es);
        t = Math.tan(Math.PI / 4.0 - phi / 2.0)
                / Math.pow((1.0 - e * Math.sin(phi))
                        / (1.0 + e * Math.sin(phi)), (e / 2.0));
        rho = 2.0
                * a
                * k0
                * t
                / Math.sqrt(Math.pow(1.0 + e, 1.0 + e)
                        * Math.pow(1.0 - e, 1.0 - e));
        x = rho * Math.sin(lambda);
        y = rho * Math.cos(lambda);

        if (lat > 0.0) {
            // Northern hemisphere
            y = -(y);
            // southernHemisphere = false;
        }
        x += 2.0e6;
        // Add in false easting and northing
        y += 2.0e6;

        easting = x;
        northing = y;
    }

    /*
     * ---------------------------------------------------------------------------------
     */

    /**
     * Convert a UPSPoint to a LatLonPoint
     * 
     * @return returns a LatLonPoint
     */
    public LatLonPoint toLatLonPoint(boolean southernHemisphere) {
        LatLonPoint llp = new LatLonPoint.Double();
        double lon = 0;
        double lat = 0;
        double a = 0;
        double es = 0;
        double e = 0;
        double t = 0;
        double rho = 0;
        double x = easting;
        double y = northing;
        final double k0 = 0.994;

        a = 6378137.0;
        es = 0.00669438d;

        e = Math.sqrt(es);

        x -= 2.0e6;
        // Remove false easting and northing
        y -= 2.0e6;

        rho = Math.sqrt(x * x + y * y);
        t = rho
                * Math.sqrt(Math.pow(1.0 + e, 1.0 + e)
                        * Math.pow(1.0 - e, 1.0 - e)) / (2.0 * a * k0);

        lat = calcPhi(e, t);
        lat /= Degree;

        if (y != 0.0) {
            t = Math.atan(Math.abs(x / y));
        } else {
            t = Math.PI / 2.0;
            if (x < 0.0) {
                t = -t;
            }
        }

        if (southernHemisphere == false) {
            y = -y;
        } else {
            lat = -lat;
        }

        if (y < 0.0) {
            t = Math.PI - t;
        }

        if (x < 0.0) {
            t = -t;
        }

        lon = t / Degree;
        llp.setLatitude((float) lat);
        llp.setLongitude((float) lon);
        return llp;
    }

    /*
     * ---------------------------------------------------------------------------------
     */

    /**
     * Sets the northing attribute
     * 
     * @param northing The new northing value
     */
    public void setNorthing(double northing) {
        this.northing = northing;
    }

    /**
     * Sets the easting attribute
     * 
     * @param easting The new easting value
     */
    public void setEasting(double easting) {
        this.easting = easting;
    }

    /**
     * Gets the easting attribute
     * 
     * @return The easting value
     */
    public double getNorthing() {
        return northing;
    }

    /**
     * Gets the easting attribute
     * 
     * @return The easting value
     */
    public double getEasting() {
        return easting;
    }

    /**
     * Description of the Method
     * 
     * @return returns a string representation of the object
     */
    public String toString() {
        return "Easting:" + easting + " Northing:" + northing;
    }

    /**
     * Calculate phi (latitude)
     * 
     * @param e
     * @param t
     * @return phi
     */
    static double calcPhi(double e, double t) {
        double phi = 0;
        double old = Math.PI / 2.0 - 2.0 * Math.atan(t);
        short maxIterations = 20;

        while ((Math.abs((phi - old) / phi) > 1.0e-8) && (maxIterations != 0)) {
            old = phi;
            phi = Math.PI
                    / 2.0
                    - 2.0
                    * Math.atan(t
                            * Math.pow((1.0 - e * Math.sin(phi))
                                    / ((1.0 + e * Math.sin(phi))), (e / 2.0)));
            maxIterations--;
        }
        return phi;
    }

    /**
     * Tested against the NIMA calculator
     */
    public static void main(String[] args) {
        // TEST1 - NORTH & WEST
        LatLonPoint llpt1 = new LatLonPoint.Double(87.00, -74.50);
        System.out.println(llpt1.toString());
        UPSPoint ups = new UPSPoint(llpt1);
        System.out.println(ups.toString());
        LatLonPoint llpt2 = ups.toLatLonPoint(false);
        System.out.println(llpt2.toString());
        // TEST2 - SOUTH & EAST
        System.out.println("--------------------------------------------");
        llpt1 = new LatLonPoint.Double(-89.00, 110.50);
        System.out.println(llpt1.toString());
        ups = new UPSPoint(llpt1);
        System.out.println(ups.toString());
        llpt2 = ups.toLatLonPoint(true);
        System.out.println(llpt2.toString());
    }
}
