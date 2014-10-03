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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Orthographic.java,v $
// $RCSfile: Orthographic.java,v $
// $Revision: 1.7 $
// $Date: 2006/04/07 15:21:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Implements the Orthographic projection.
 */
public class Orthographic extends Azimuth {

    /**
     * The Orthographic name.
     */
    public final static transient String OrthographicName = "Orthographic";

    protected double hy, wx;

    // almost constant projection parameters
    protected double cosCtrLat;
    protected double sinCtrLat;

    public final static transient double epsilon = 0.0001f;
    protected final static double NORTH_BOUNDARY = NORTH_POLE - epsilon;
    protected final static double SOUTH_BOUNDARY = -NORTH_BOUNDARY;

    /**
     * Construct an Orthographic projection.
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public Orthographic(LatLonPoint center, float scale, int width, int height) {
        super(center, scale, width, height);
        setMinScale(1000.0f);
    }

    // protected void finalize() {
    // Debug.message("proj", "Orthographic finalized");
    // }

    /**
     * Return stringified description of this projection.
     * 
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
        return "Orthographic[" + super.toString();
    }

    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change. For instance,
     * they may need to recalculate "constant" parameters used in the forward()
     * and inverse() calls.
     * <p>
     */
    protected void computeParameters() {
        Debug.message("proj", "Orthographic.computeParameters()");
        super.computeParameters();

        // do some precomputation of stuff
        cosCtrLat = Math.cos(centerY);
        sinCtrLat = Math.sin(centerY);

        // compute the offsets
        hy = height / 2;
        wx = width / 2;
    }

    /**
     * Sets radian latitude to something sane. This is an abstract function
     * since some projections don't deal well with extreme latitudes.
     * <p>
     * 
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     */
    public double normalizeLatitude(double lat) {
        if (lat > NORTH_BOUNDARY) {
            return NORTH_BOUNDARY;
        } else if (lat < SOUTH_BOUNDARY) {
            return SOUTH_BOUNDARY;
        }
        return lat;
    }

    /**
     * Check if a given lat/lon is within the visible hemisphere.
     * 
     * @param phi1 latitude
     * @param lambda0 longitude
     * @param phi latitude
     * @param lambda longitude
     * @return boolean true if within the visible hemisphere, false if not
     */
    final public static boolean hemisphere_clip(float phi1, float lambda0,
                                                float phi, float lambda) {
        return (GreatCircle.sphericalDistance(phi1, lambda0, phi, lambda)/*-epsilon*/<= MoreMath.HALF_PI);
    }

    final public static boolean hemisphere_clip(double phi1, double lambda0,
                                                double phi, double lambda) {
        return (GreatCircle.sphericalDistance(phi1, lambda0, phi, lambda)/*-epsilon*/<= MoreMath.HALF_PI_D);
    }

    /**
     * Calculate point along edge of hemisphere (using center point and current
     * azimuth).
     * <p>
     * This is invoked for points that aren't visible in the current hemisphere.
     * 
     * @param p Point2D
     * @return Point2D p
     */
    private Point2D edge_point(Point2D p, double current_azimuth) {
        LatLonPoint tmpll = GreatCircle.sphericalBetween(centerY,
                centerX,
                MoreMath.HALF_PI_D/*-epsilon*/,
                current_azimuth);

        double phi = tmpll.getRadLat();
        double lambda = tmpll.getRadLon();
        double cosPhi = Math.cos(phi);
        double lambdaMinusCtrLon = lambda - centerX;

        double x = (scaled_radius * cosPhi * Math.sin(lambdaMinusCtrLon)) + wx;
        double y = hy
                - (scaled_radius * (cosCtrLat * Math.sin(phi) - sinCtrLat
                        * cosPhi * Math.cos(lambdaMinusCtrLon)));
        p.setLocation(x, y);
        return p;
    }

    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * A point is plot-able if it is within the visible hemisphere.
     * 
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(double lat, double lon) {
        lat = normalizeLatitude(ProjMath.degToRad(lat));
        lon = wrapLongitude(ProjMath.degToRad(lon));
        return hemisphere_clip(centerY, centerX, lat, lon);
    }

    /**
     * Forward project a point. If the point is not within the viewable
     * hemisphere, return flags in AzimuthVar variable if specified.
     * 
     * @param phi double latitude in radians
     * @param lambda double longitude in radians
     * @param p Point2D
     * @param azVar AzimuthVar or null
     * @return Point2D pt
     */
    protected Point2D _forward(double phi, double lambda, Point2D p,
                               AzimuthVar azVar) {
        double cosPhi = Math.cos(phi);
        double lambdaMinusCtrLon = lambda - centerX;

        // normalize invalid point to the edge of the sphere
        if (!hemisphere_clip(centerY, centerX, phi, lambda)) {
            double az = GreatCircle.sphericalAzimuth(centerY,
                    centerX,
                    phi,
                    lambda);
            if (azVar != null) {
                // set the invalid flag
                azVar.invalid_forward = true;
                // record azimuth of this point
                azVar.current_azimuth = az;
            }
            return edge_point(p, az);
        }

        double x = (scaled_radius * cosPhi * Math.sin(lambdaMinusCtrLon)) + wx;
        double y = hy
                - (scaled_radius * (cosCtrLat * Math.sin(phi) - sinCtrLat
                        * cosPhi * Math.cos(lambdaMinusCtrLon)));
        p.setLocation(x, y);
        return p;
    }

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point2D)
     */
    public <T extends Point2D> T inverse(double x, double y, T llp) {
        
        if (llp == null) {
            llp = (T) new LatLonPoint.Double();
        }
        
        // convert from screen to world coordinates
        x -= wx;
        y = hy - y;

        // Debug.output("Orthographic.inverse: x,y=" + x + "," + y);

        // sqrt(pt . pt)
        double rho = Math.sqrt(x * x + y * y);
        if (rho == 0) {
            Debug.message("proj", "Orthographic.inverse: center!");
            llp.setLocation(Math.toDegrees(centerX), Math.toDegrees(centerY));
            return llp;
        }

        // float c = (float)Math.asin(rho/scaled_radius);
        // float cosC = (float)Math.cos(c);
        // float sinC = (float)Math.sin(c);
        double sinC = rho / scaled_radius;
        double cosC = Math.sqrt(1 - sinC * sinC);

        // calculate latitude
        double lat = Math.asin(cosC * sinCtrLat
                + (y * sinC * (cosCtrLat / rho)));

        // calculate longitude
        double lon;
        if (centerY == NORTH_POLE) {
            lon = centerX + Math.atan2(x, -y);
        } else if (centerY == SOUTH_POLE) {
            lon = centerX + Math.atan2(x, y);
        } else {
            lon = centerX
                    + Math.atan2((x * sinC), (rho * cosCtrLat * cosC - y
                            * sinCtrLat * sinC));
        }
        // Debug.output("Orthographic.inverse: lat,lon=" +
        // ProjMath.radToDeg(lat) + "," +
        // ProjMath.radToDeg(lon));

        // check if point in outer space
        // if (MoreMath.approximately_equal(lat, ctrLat) &&
        // MoreMath.approximately_equal(lon, ctrLon) &&
        // (Math.abs(x-(width/2))<2) &&
        // (Math.abs(y-(height/2))<2))
        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            // Debug.message("proj", "Orthographic.inverse(): outer
            // space!");
            lat = centerY;
            lon = centerX;
        }
        
        llp.setLocation(Math.toDegrees(wrapLongitude(lon)), Math.toDegrees(normalizeLatitude(lat)));
        return llp;
    }

    /**
     * Get the upper left (northernmost and westernmost) point of the
     * projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the projection
     * based on the center point and height and width of screen.
     * 
     * @return LatLonPoint
     */
    public LatLonPoint getUpperLeft() {
        LatLonPoint tmp = new LatLonPoint.Double();
        double lat, lon;

        // over north pole
        if (overNorthPole()) {
            lat = NORTH_POLE;
            lon = -DATELINE;
        }

        // over south pole
        else if (overSouthPole()) {
            lon = -DATELINE;

            // get the left top corner
            inverse(0, 0, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.getRadLon(), centerX, 0.0001f)) {
                lat = centerY + MoreMath.HALF_PI_D;
            } else {
                // northernmost coord is left top
                lat = tmp.getY();
            }
        }

        // view in northern hemisphere
        else if (centerY >= 0f) {
            // get the left top corner
            inverse(0, 0, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.getRadLon(), centerX, 0.0001f)) {
                inverse(width / 2, 0, tmp);
                lat = tmp.getRadLat();
                lon = -DATELINE;
            } else {
                // westernmost coord is left top
                lon = tmp.getRadLon();
                // northernmost coord is center top
                inverse(width / 2, 0, tmp);
                lat = tmp.getRadLat();
            }
        }

        // view in southern hemisphere
        else {
            // get the left top corner
            inverse(0, 0, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.getRadLon(), centerX, 0.0001f)) {
                lat = centerY + MoreMath.HALF_PI_D;
                lon = -DATELINE;
            } else {
                // northernmost coord is left top
                lat = tmp.getRadLat();
                // westernmost coord is left bottom
                inverse(0, height - 1, tmp);
                lon = tmp.getRadLon();
            }
        }
        tmp.setLatLon(lat, lon, true);
        // Debug.output("ul="+tmp);
        return tmp;
    }

    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the projection
     * based on the center point and height and width of screen.
     * <p>
     * This is trivial for most cylindrical projections, but much more
     * complicated for azimuthal projections.
     * 
     * @return LatLonPoint
     */
    public LatLonPoint getLowerRight() {
        LatLonPoint tmp = new LatLonPoint.Double();
        double lat, lon;

        // over north pole
        if (overNorthPole()) {
            lon = DATELINE;

            // get the right bottom corner
            inverse(width - 1, height - 1, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.getRadLon(), centerX, 0.0001f)) {
                lat = centerY - MoreMath.HALF_PI_D;
            } else {
                // southernmost coord is right bottom
                lat = tmp.getRadLat();
            }
        }

        // over south pole
        else if (overSouthPole()) {
            lat = SOUTH_POLE;
            lon = DATELINE;
        }

        // view in northern hemisphere
        else if (centerY >= 0f) {
            // get the right bottom corner
            inverse(width - 1, height - 1, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.getRadLon(), centerX, 0.0001f)) {
                lat = centerY - MoreMath.HALF_PI_D;
                lon = DATELINE;
            } else {
                // southernmost coord is right bottom
                lat = tmp.getRadLat();
                // easternmost coord is right top
                inverse(width - 1, 0, tmp);
                lon = tmp.getRadLon();
            }
        }

        // view in southern hemisphere
        else {
            // get the right bottom corner
            inverse(width - 1, height - 1, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.getRadLon(), centerX, 0.0001f)) {
                inverse(width / 2, height - 1, tmp);
                lat = tmp.getRadLat();
                lon = DATELINE;
            } else {
                // easternmost coord is right bottom
                lon = tmp.getRadLon();
                // southernmost coord is center bottom
                inverse(width / 2, height - 1, tmp);
                lat = tmp.getRadLat();
            }
        }
        tmp.setLatLon(lat, lon, true);
        // Debug.output("lr="+tmp);
        return tmp;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return OrthographicName;
    }

    /*
     * public void testPoint(float lat, float lon) { float x, y; lon =
     * wrap_longitude(ProjMath.degToRad(lon)); lat =
     * normalize_latitude(ProjMath.degToRad(lat)); x = forward_x(lat, lon); y =
     * forward_y(lat, lon);
     * 
     * Debug.output("(lon="+ProjMath.radToDeg(lon)+",lat="+
     * ProjMath.radToDeg(lat)+ ") = (x="+x+",y="+y+")"); lat = inverse_lat(x,
     * y); lon = wrap_longitude(inverse_lon(x, y));
     * Debug.output("(x="+x+",y="+y+") = (lon="+ ProjMath.radToDeg(lon)+",lat="+
     * ProjMath.radToDeg(lat)+")"); }
     * 
     * public static void main (String argv[]) { Orthographic proj=null; proj =
     * new Orthographic(new LatLonPoint(40.0f, 0.0f), 1.0f, 620, 480);
     * 
     * Debug.output("testing"); proj.setEarthRadius(1.0f);
     * Debug.output("setEarthRadius("+proj.getEarthRadius()+")");
     * proj.setPPM(1); Debug.output("setPPM("+proj.getPPM()+")");
     * proj.setMinScale(1.0f);
     * Debug.output("setMinScale("+proj.getMinScale()+")"); try {
     * proj.setScale(1.0f); } catch (java.beans.PropertyVetoException e) { }
     * Debug.output("setScale("+proj.getScale()+")"); Debug.output(proj);
     * Debug.output();
     * 
     * Debug.output("---testing latitude"); proj.testPoint(0.0f, 0.0f);
     * proj.testPoint(10.0f, 0.0f); proj.testPoint(40.0f, 0.0f);
     * proj.testPoint(-80.0f, 0.0f); proj.testPoint(-90.0f, 0.0f);
     * proj.testPoint(100.0f, 0.0f); proj.testPoint(-3272.0f, 0.0f);
     * Debug.output("---testing longitude"); proj.testPoint(0.0f, 10.0f);
     * proj.testPoint(0.0f, -10.0f); proj.testPoint(0.0f, 90.0f);
     * proj.testPoint(0.0f, -90.0f); proj.testPoint(0.0f, 170.0f);
     * proj.testPoint(0.0f, -170.0f); proj.testPoint(0.0f, 180.0f);
     * proj.testPoint(0.0f, -180.0f); proj.testPoint(0.0f, 190.0f);
     * proj.testPoint(0.0f, -190.0f); Debug.output("---testing lat&lon");
     * proj.testPoint(100.0f, 370.0f); proj.testPoint(-30.0f, -370.0f);
     * proj.testPoint(-80.0f, 550.0f); proj.testPoint(0.0f, -550.0f); }
     */
}