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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Orthographic.java,v $
// $RCSfile: Orthographic.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:14 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.Point;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;


/**
 * Implements the Orthographic projection.
 */
public class Orthographic extends Azimuth {

    /**
     * The Orthographic name.
     */
    public final static transient String OrthographicName = "Orthographic";

    /**
     * The Orthographic type of projection.
     */
    public final static transient int OrthographicType = 7;

    protected int hy, wx;

    // almost constant projection parameters
    protected float cosCtrLat;
    protected float sinCtrLat;


    public final static transient float epsilon = 0.0001f;
    protected final static transient float NORTH_BOUNDARY = NORTH_POLE-epsilon;
    protected final static transient float SOUTH_BOUNDARY = -NORTH_BOUNDARY;


    /**
     * Construct an Orthographic projection.
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public Orthographic(
        LatLonPoint center, float scale, int width, int height)
    {
        super(center, scale, width, height, OrthographicType);
        setMinScale(1000.0f);
    }

    /**
     * Construct an Orthographic projection.
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     * @param type subclass's type
     */
    public Orthographic(
        LatLonPoint center, float scale, int width, int height, int type)
    {
        super(center, scale, width, height, type);
        setMinScale(1000.0f);
    }


//    protected void finalize() {
//      Debug.message("proj", "Orthographic finalized");
//    }


    /**
     * Return stringified description of this projection.
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
        return "Orthographic[" + super.toString();
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.<p>
     */
    protected void computeParameters() {
        Debug.message("proj", "Orthographic.computeParameters()");
        super.computeParameters();

        // do some precomputation of stuff
        cosCtrLat = (float)Math.cos(ctrLat);
        sinCtrLat = (float)Math.sin(ctrLat);
        
        // compute the offsets
        hy = height/2;
        wx = width/2;
    }

    
    /**
     * Sets radian latitude to something sane.  This is an abstract
     * function since some projections don't deal well with extreme
     * latitudes.<p>
     *
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see com.bbn.openmap.LatLonPoint#normalize_latitude(float)
     *
     */
    public float normalize_latitude(float lat) {
        if (lat > NORTH_BOUNDARY) {
            return NORTH_BOUNDARY;
        } else if (lat < SOUTH_BOUNDARY) {
            return SOUTH_BOUNDARY;
        }
        return lat;
    }


    /**
     * Check if a given lat/lon is within the visible hemisphere.
     * @param phi1 latitude
     * @param lambda0 longitude
     * @param phi latitude
     * @param lambda longitude
     * @return boolean true if within the visible hemisphere, false if not
     */
    final public static boolean hemisphere_clip(
        float phi1, float lambda0, float phi, float lambda)
    {
        return (GreatCircle.spherical_distance(
            phi1, lambda0, phi, lambda)/*-epsilon*/ <= MoreMath.HALF_PI);
    }


    /**
     * Calculate point along edge of hemisphere (using center point and
     * current azimuth).
     * <p>
     * This is invoked for points that aren't visible in the current
     * hemisphere.
     * @param p Point
     * @return Point p
     */
    private Point edge_point(Point p, float current_azimuth) {
        LatLonPoint tmpll = GreatCircle.spherical_between(
                ctrLat, ctrLon, MoreMath.HALF_PI/*-epsilon*/, current_azimuth);

        float phi = tmpll.radlat_;
        float lambda = tmpll.radlon_;
        float cosPhi = (float)Math.cos(phi);
        float lambdaMinusCtrLon = (float)(lambda-ctrLon);

        p.x = (int)(scaled_radius * cosPhi *
                    (float)Math.sin(lambdaMinusCtrLon)) + wx;
        p.y = hy - (int)(scaled_radius *
                         (cosCtrLat * (float)Math.sin(phi) -
                          sinCtrLat * cosPhi *
                          (float)Math.cos(lambdaMinusCtrLon)));
        return p;
    }


    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * A point is plot-able if it is within the visible hemisphere.
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(float lat, float lon) {
        lat = normalize_latitude(ProjMath.degToRad(lat));
        lon = wrap_longitude(ProjMath.degToRad(lon));
        return hemisphere_clip(ctrLat, ctrLon, lat, lon);
    }


    /**
     * Forward project a point.
     * If the point is not within the viewable hemisphere, return flags in
     * AzimuthVar variable if specified.
     * @param phi float latitude in radians
     * @param lambda float longitude in radians
     * @param p Point
     * @param azVar AzimuthVar or null
     * @return Point pt
     */
    protected Point _forward (
            float phi, float lambda, Point p, AzimuthVar azVar)
    {
        float cosPhi = (float)Math.cos(phi);
        float lambdaMinusCtrLon = (float)(lambda-ctrLon);

        // normalize invalid point to the edge of the sphere
        if (!hemisphere_clip(ctrLat, ctrLon, phi, lambda)) {
            float az = 
                GreatCircle.spherical_azimuth(ctrLat, ctrLon, phi, lambda);
            if (azVar != null) {
                azVar.invalid_forward = true;   // set the invalid flag
                azVar.current_azimuth = az;     // record azimuth of this point
            }
            return edge_point(p, az);
        }

        p.x = (int)(scaled_radius * cosPhi *
                    (float)Math.sin(lambdaMinusCtrLon)) + wx;
        p.y = hy - (int)(scaled_radius *
                         (cosCtrLat * (float)Math.sin(phi) -
                          sinCtrLat * cosPhi *
                          (float)Math.cos(lambdaMinusCtrLon)));
        return p;
    }


    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point)
     */
    public LatLonPoint inverse(int x, int y, LatLonPoint llp) {
        // convert from screen to world coordinates
        x = x - wx;
        y = hy - y;

//      Debug.output("Orthographic.inverse: x,y=" + x + "," + y);

        // sqrt(pt . pt)
        float rho = (float)Math.sqrt(x*x + y*y);
        if (rho == 0f) {
            Debug.message("proj", "Orthographic.inverse: center!");
            llp.setLatLon(
                    ProjMath.radToDeg(ctrLat),
                    ProjMath.radToDeg(ctrLon));
            return llp;
        }

        //float c = (float)Math.asin(rho/scaled_radius);
        //float cosC = (float)Math.cos(c);
        //float sinC = (float)Math.sin(c);
        float sinC = rho/scaled_radius;
        float cosC = (float)Math.sqrt(1 - sinC * sinC);

        // calculate latitude 
        float lat = (float)Math.asin(
            cosC * sinCtrLat + (y * sinC * (cosCtrLat/rho)));

        // calculate longitude
        float lon;
        if (ctrLat == NORTH_POLE) {
            lon = ctrLon + (float)Math.atan2(x, -y);
        } else if (ctrLat == SOUTH_POLE) {
            lon = ctrLon + (float)Math.atan2(x, y);
        } else {
            lon = ctrLon + (float)Math.atan2(
                (x * sinC),
                (rho * cosCtrLat * cosC - y * sinCtrLat * sinC));
        }
//      Debug.output("Orthographic.inverse: lat,lon=" +
//                         ProjMath.radToDeg(lat) + "," +
//                         ProjMath.radToDeg(lon));

        // check if point in outer space
//      if (MoreMath.approximately_equal(lat, ctrLat) &&
//             MoreMath.approximately_equal(lon, ctrLon) &&
//             (Math.abs(x-(width/2))<2) &&
//             (Math.abs(y-(height/2))<2))
        if (Float.isNaN(lat) || Float.isNaN(lon))
        {
//          Debug.message("proj", "Orthographic.inverse(): outer space!");
            lat = ctrLat;
            lon = ctrLon;
        }
        llp.setLatLon(
                ProjMath.radToDeg(lat),
                ProjMath.radToDeg(lon));
        return llp;
    }


    /**
     * Inverse project a Point.
     * @param pt x,y Point
     * @param llp resulting LatLonPoint
     * @return LatLonPoint llp
     */
    public LatLonPoint inverse(Point pt, LatLonPoint llp) {
        return inverse(pt.x, pt.y, llp);
    }


    /**
     * Get the upper left (northernmost and westernmost) point of the
     * projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * @return LatLonPoint
     */
    public LatLonPoint getUpperLeft() {
        LatLonPoint tmp = new LatLonPoint();
        float lat, lon;

        // over north pole
        if (overNorthPole()) {
            lat = NORTH_POLE;
            lon = -DATELINE;
        }

        // over south pole
        else if (overSouthPole()) {
            lon = -DATELINE;

            // get the left top corner
            tmp = inverse(0, 0, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.radlon_, ctrLon, 0.0001f)) {
                lat = ctrLat + MoreMath.HALF_PI;
            } else {
                // northernmost coord is left top
                lat = tmp.radlat_;
            }
        }

        // view in northern hemisphere
        else if (ctrLat >= 0f) {
            // get the left top corner
            tmp = inverse(0,0,tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.radlon_, ctrLon, 0.0001f)) {
                lat = inverse(width/2, 0, tmp).radlat_;
                lon = -DATELINE;
            } else {
                // westernmost coord is left top
                lon = tmp.radlon_;
                // northernmost coord is center top
                lat = inverse(width/2, 0, tmp).radlat_;
            }
        }

        // view in southern hemisphere
        else {
            // get the left top corner
            tmp = inverse(0, 0, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.radlon_, ctrLon, 0.0001f)) {
                lat = ctrLat + MoreMath.HALF_PI;
                lon = -DATELINE;
            } else {
                // northernmost coord is left top
                lat = tmp.radlat_;
                // westernmost coord is left bottom
                lon = inverse(0, height-1, tmp).radlon_;
            }
        }
        tmp.setLatLon(lat, lon, true);
//      Debug.output("ul="+tmp);
        return tmp;
    }


    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * <p>
     * This is trivial for most cylindrical projections, but much more
     * complicated for azimuthal projections.
     * @return LatLonPoint
     */
    public LatLonPoint getLowerRight() {
        LatLonPoint tmp = new LatLonPoint();
        float lat, lon;

        // over north pole
        if (overNorthPole()) {
            lon = DATELINE;

            // get the right bottom corner
            tmp = inverse(width-1, height-1, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.radlon_, ctrLon, 0.0001f)) {
                lat = ctrLat - MoreMath.HALF_PI;
            } else {
                // southernmost coord is right bottom
                lat = tmp.radlat_;
            }
        }

        // over south pole
        else if (overSouthPole()) {
            lat = SOUTH_POLE;
            lon = DATELINE;
        }

        // view in northern hemisphere
        else if (ctrLat >= 0f) {
            // get the right bottom corner
            tmp = inverse(width-1, height-1, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.radlon_, ctrLon, 0.0001f)) {
                lat = ctrLat - MoreMath.HALF_PI;
                lon = DATELINE;
            } else {
                // southernmost coord is right bottom
                lat = tmp.radlat_;
                // easternmost coord is right top
                lon = inverse(width-1, 0, tmp).radlon_; 
            }
        }

        // view in southern hemisphere
        else {
            // get the right bottom corner
            tmp = inverse(width-1, height-1, tmp);

            // check for invalid
            if (MoreMath.approximately_equal(tmp.radlon_, ctrLon, 0.0001f)) {
                lat = inverse(width/2, height-1, tmp).radlat_;
                lon = DATELINE;
            } else {
                // easternmost coord is right bottom
                lon = tmp.radlon_;
                // southernmost coord is center bottom
                lat = inverse(width/2, height-1, tmp).radlat_;
            }
        }
        tmp.setLatLon(lat, lon, true);
//      Debug.output("lr="+tmp);
        return tmp;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return OrthographicName;
    }

    /*
    public void testPoint(float lat, float lon) {
        float x, y;
        lon = wrap_longitude(ProjMath.degToRad(lon));
        lat = normalize_latitude(ProjMath.degToRad(lat));
        x = forward_x(lat, lon);
        y = forward_y(lat, lon);

        Debug.output("(lon="+ProjMath.radToDeg(lon)+",lat="+
                ProjMath.radToDeg(lat)+
                           ") = (x="+x+",y="+y+")");
        lat = inverse_lat(x, y);
        lon = wrap_longitude(inverse_lon(x, y));
        Debug.output("(x="+x+",y="+y+") = (lon="+
                           ProjMath.radToDeg(lon)+",lat="+
                           ProjMath.radToDeg(lat)+")");
    }

    public static void main (String argv[]) {
        Orthographic proj=null;
        proj = new Orthographic(new LatLonPoint(40.0f, 0.0f), 1.0f, 620, 480);

        Debug.output("testing");
        proj.setEarthRadius(1.0f);
        Debug.output("setEarthRadius("+proj.getEarthRadius()+")");
        proj.setPPM(1);
        Debug.output("setPPM("+proj.getPPM()+")");
        proj.setMinScale(1.0f);
        Debug.output("setMinScale("+proj.getMinScale()+")");
        try {
            proj.setScale(1.0f);
        } catch (java.beans.PropertyVetoException e) {
        }
        Debug.output("setScale("+proj.getScale()+")");
        Debug.output(proj);
        Debug.output();

        Debug.output("---testing latitude");
        proj.testPoint(0.0f, 0.0f);
        proj.testPoint(10.0f, 0.0f);
        proj.testPoint(40.0f, 0.0f);
        proj.testPoint(-80.0f, 0.0f);
        proj.testPoint(-90.0f, 0.0f);
        proj.testPoint(100.0f, 0.0f);
        proj.testPoint(-3272.0f, 0.0f);
        Debug.output("---testing longitude");
        proj.testPoint(0.0f, 10.0f);
        proj.testPoint(0.0f, -10.0f);
        proj.testPoint(0.0f, 90.0f);
        proj.testPoint(0.0f, -90.0f);
        proj.testPoint(0.0f, 170.0f);
        proj.testPoint(0.0f, -170.0f);
        proj.testPoint(0.0f, 180.0f);
        proj.testPoint(0.0f, -180.0f);
        proj.testPoint(0.0f, 190.0f);
        proj.testPoint(0.0f, -190.0f);
        Debug.output("---testing lat&lon");
        proj.testPoint(100.0f, 370.0f);
        proj.testPoint(-30.0f, -370.0f);
        proj.testPoint(-80.0f, 550.0f);
        proj.testPoint(0.0f, -550.0f);
    }
    */
}
