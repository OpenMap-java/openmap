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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Gnomonic.java,v $
// $RCSfile: Gnomonic.java,v $
// $Revision: 1.10 $
// $Date: 2006/04/07 15:21:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Implements the Gnomonic projection.
 */
public class Gnomonic extends Azimuth {

    /**
     * The Gnomonic name.
     */
    public final static transient String GnomonicName = "Gnomonic";

    protected double hy, wx;

    // almost constant projection parameters
    protected double cosCtrLat;
    protected double sinCtrLat;

    public final static transient double epsilon = 0.0001;
    public final static transient double HEMISPHERE_EDGE = ((Math.PI / 180d) * 80d);// 80degrees
    public final static transient double hPrime = 1d / Math.pow(Math.cos(HEMISPHERE_EDGE),
            2d);

    protected final static float NORTH_BOUNDARY = (float) (NORTH_POLE - epsilon);
    protected final static float SOUTH_BOUNDARY = -NORTH_BOUNDARY;

    /**
     * Construct a Mercator projection.
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     * 
     */
    public Gnomonic(LatLonPoint center, float scale, int width, int height) {
        super(center, scale, width, height);
        setMinScale(1000.0f);
    }

    /**
     * Return stringified description of this projection.
     * 
     * @return String
     * @see Projection#getProjectionID
     * 
     */
    public String toString() {
        return "Gnomonic[" + super.toString();
    }

    protected void init() {
        super.init();

        // minscale is the minimum scale allowable (before integer
        // wrapping can occur)
        minscale = (float) Math.ceil((2 * hPrime * planetPixelRadius)
                / (int) Integer.MAX_VALUE);
        if (minscale < 1)
            minscale = 1;

        // calculate cutoff scale for XWindows workaround
        XSCALE_THRESHOLD = (int) ((planetPixelRadius * 2 * hPrime) / 64000);// fudge

    }

    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change. For instance,
     * they may need to recalculate "constant" parameters used in the forward()
     * and inverse() calls.
     * <p>
     * 
     */
    protected void computeParameters() {
        Debug.message("proj", "Gnomonic.computeParameters()");
        super.computeParameters();

        // maxscale = scale at which a world hemisphere fits in the
        // window
        maxscale = (width < height) ? (float) (planetPixelRadius * 2 * hPrime)
                / (float) width : (float) (planetPixelRadius * 2 * hPrime)
                / (float) height;
        if (maxscale < minscale) {
            maxscale = minscale;
        }

        if (scale > maxscale) {
            scale = maxscale;
        }

        scaled_radius = planetPixelRadius / scale;

        // width of the world in pixels at current scale. We see only
        // one hemisphere.
        world.x = (int) ((planetPixelRadius * 2 * hPrime) / scale);

        // do some precomputation of stuff
        cosCtrLat = Math.cos(centerY);
        sinCtrLat = Math.sin(centerY);

        // compute the offsets
        hy = height / 2;
        wx = width / 2;
    }

    /**
     * Assume that the Graphics has been set with the Paint/Color needed, just
     * render the shape of the background.
     */
    public void drawBackground(Graphics g) {
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Sets radian latitude to something sane. This is an abstract function
     * since some projections don't deal well with extreme latitudes.
     * <p>
     * 
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * 
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
     * Get the distance c of the point from the center of the hemisphere.
     * 
     * @param phi1 latitude
     * @param lambda0 longitude
     * @param phi latitude
     * @param lambda longitude
     * @return float c angular distance in radians
     * 
     */
    final public static float hemisphere_distance(float phi1, float lambda0,
                                                  float phi, float lambda) {
        return GreatCircle.sphericalDistance(phi1, lambda0, phi, lambda)/*-epsilon*/;
    }

    final public static double hemisphere_distance(double phi1, double lambda0,
                                                   double phi, double lambda) {
        return GreatCircle.sphericalDistance(phi1, lambda0, phi, lambda)/*-epsilon*/;
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
        return (GreatCircle.sphericalDistance(phi1, lambda0, phi, lambda)/*-epsilon*/<= HEMISPHERE_EDGE);
    }

    final public static boolean hemisphere_clip(double phi1, double lambda0,
                                                double phi, double lambda) {
        return (GreatCircle.sphericalDistance(phi1, lambda0, phi, lambda)/*-epsilon*/<= HEMISPHERE_EDGE);
    }

    /**
     * Calculate point along edge of hemisphere (using center point and current
     * azimuth).
     * <p>
     * This is invoked for points that aren't visible in the current hemisphere.
     * 
     * @param p Point2D
     * @return Point2D p
     * 
     */
    private Point2D edge_point(Point2D p, double current_azimuth) {
        double c = HEMISPHERE_EDGE;
        LatLonPoint tmpll = GreatCircle.sphericalBetween(centerY,
                centerX,
                c/*-epsilon*/,
                current_azimuth);
        double phi = tmpll.getRadLat();
        double lambda = tmpll.getRadLon();

        double kPrime = 1f / Math.cos(c);
        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);
        double lambdaMinusCtrLon = lambda - centerX;
        double cosLambdaMinusCtrLon = Math.cos(lambdaMinusCtrLon);
        double sinLambdaMinusCtrLon = Math.sin(lambdaMinusCtrLon);

        double x = (scaled_radius * kPrime * cosPhi * sinLambdaMinusCtrLon)
                + wx;
        double y = hy
                - (scaled_radius * kPrime * (cosCtrLat * sinPhi - sinCtrLat
                        * cosPhi * cosLambdaMinusCtrLon));
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
     * @param phi float latitude in radians
     * @param lambda float longitude in radians
     * @param p Point2D
     * @param azVar AzimuthVar or null
     * @return Point2D pt
     */
    protected Point2D _forward(float phi, float lambda, Point2D p,
                               AzimuthVar azVar) {
        return _forward((double) phi, (double) lambda, p, azVar);
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
        double c = hemisphere_distance(centerY, centerX, phi, lambda);
        // normalize invalid point to the edge of the sphere
        if (c > HEMISPHERE_EDGE) {
            double az = GreatCircle.sphericalAzimuth(centerY,
                    centerX,
                    phi,
                    lambda);
            if (azVar != null) {
                azVar.invalid_forward = true; // set the invalid
                // flag
                azVar.current_azimuth = (float) az; // record azimuth
                // of this
                // point
            }
            return edge_point(p, az);
        }

        double kPrime = 1 / Math.cos(c);
        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);
        double lambdaMinusCtrLon = lambda - centerX;
        double cosLambdaMinusCtrLon = Math.cos(lambdaMinusCtrLon);
        double sinLambdaMinusCtrLon = Math.sin(lambdaMinusCtrLon);

        double x = (scaled_radius * kPrime * cosPhi * sinLambdaMinusCtrLon)
                + wx;
        double y = hy
                - (scaled_radius * kPrime * (cosCtrLat * sinPhi - sinCtrLat
                        * cosPhi * cosLambdaMinusCtrLon));
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
     * 
     */
    public <T extends Point2D> T inverse(double x, double y, T llp) {
        
        if (llp == null) {
            llp = (T) new LatLonPoint.Double();
        }
        
        // convert from screen to world coordinates
        x -= wx;
        y = hy - y;

        // Debug.output("Gnomonic.inverse: x,y=" + x + "," + y);

        double rho = Math.sqrt(x * x + y * y);
        if (rho == 0) {
            Debug.message("proj", "Gnomonic.inverse: center!");
            llp.setLocation(ProjMath.radToDeg(centerX),
                    ProjMath.radToDeg(centerY));
            return llp;
        }

        double c = Math.atan2(rho, scaled_radius);
        double cosC = Math.cos(c);
        double sinC = Math.sin(c);

        // calculate latitude
        double lat = Math.asin(cosC * sinCtrLat
                + (y * sinC * (cosCtrLat / rho)));

        // calculate longitude
        double lon = centerX
                + Math.atan2((x * sinC), (rho * cosCtrLat * cosC - y
                        * sinCtrLat * sinC));
        // Debug.output("Gnomonic.inverse: lat,lon=" +
        // ProjMath.radToDeg(lat) + "," +
        // ProjMath.radToDeg(lon));

        // check if point in outer space
        // if (MoreMath.approximately_equal(lat, ctrLat) &&
        // MoreMath.approximately_equal(lon, ctrLon) &&
        // (Math.abs(x-(width/2))<2) &&
        // (Math.abs(y-(height/2))<2))

        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            Debug.message("proj", "Gnomonic.inverse(): outer space!");
            lat = centerY;
            lon = centerX;
        }

        llp.setLocation(Math.toDegrees(wrapLongitude(lon)), Math.toDegrees(normalizeLatitude(lat)));
        return llp;
    }

    /**
     * Check if equator is visible on screen.
     * 
     * @return boolean
     */
    public boolean overEquator() {
        LatLonPoint llN = new LatLonPoint.Float();
        inverse(width / 2, 0, llN);
        LatLonPoint llS = new LatLonPoint.Float();
        inverse(width / 2, height, llS);
        return MoreMath.sign(llN.getY()) != MoreMath.sign(llS.getY());
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
            if (overEquator()) {
                // get top center for latitude
                inverse(width / 2, 0, tmp);
                lat = tmp.getRadLat();
            } else {
                // get left top corner for latitude
                inverse(0, 0, tmp);
                lat = tmp.getRadLat();
            }
        }

        // view in northern hemisphere
        else if (tmp.getRadLat() >= 0) {
            // get left top corner for longitude
            inverse(0, 0, tmp);
            lon = tmp.getRadLon();
            // get top center for latitude
            inverse(width / 2, 0, tmp);
            lat = tmp.getRadLat();
        }

        // view in southern hemisphere
        else {
            // get left bottom corner for longitude
            inverse(0, height, tmp);
            lon = tmp.getRadLon();

            if (overEquator()) {
                // get top center (for latitude)
                inverse(width / 2, 0, tmp);
                lat = tmp.getRadLat();
            } else {
                // get left top corner (for latitude)
                inverse(0, 0, tmp);
                lat = tmp.getRadLat();
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
            if (overEquator()) {
                // get bottom center for latitude
                inverse(width / 2, height, tmp);
                lat = tmp.getRadLat();
            } else {
                // get bottom right corner for latitude
                inverse(width, height, tmp);
                lat = tmp.getRadLat();
            }
        }

        // over south pole
        else if (overSouthPole()) {
            lat = SOUTH_POLE;
            lon = DATELINE;
        }

        // view in northern hemisphere
        else if (tmp.getRadLat() >= 0f) {
            // get the right top corner for longitude
            inverse(width, 0, tmp);
            lon = tmp.getRadLon();

            if (overEquator()) {
                // get the bottom center (for latitude)
                inverse(width / 2, height, tmp);
                lat = tmp.getRadLat();
            } else {
                // get the right bottom corner (for latitude)
                inverse(width, height, tmp);
                lat = tmp.getRadLat();
            }
        }

        // view in southern hemisphere
        else {
            // get the right bottom corner for longitude
            inverse(width, height, tmp);
            lon = tmp.getRadLon();
            // get bottom center for latitude
            inverse(width / 2, height, tmp);
            lat = tmp.getRadLat();
        }
        tmp.setLatLon(lat, lon, true);
        // Debug.output("lr="+tmp);
        return tmp;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return GnomonicName;
    }

}