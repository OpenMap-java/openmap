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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Mercator.java,v $
// $RCSfile: Mercator.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:14 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.Point;
import java.util.*;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;


/**
 * Implements the Mercator projection.
 */
public class Mercator extends Cylindrical {

    /**
     * The Mercator name.
     */
    public final static transient String MercatorName = "Mercator";

    /**
     * The Mercator type of projection.
     */
    public final static transient int MercatorType = 2;

    // maximum number of segments to draw for rhumblines.
    protected static int MAX_RHUMB_SEGS = 512;

    // HACK epsilon: skirt the edge of the infinite.  If this is too small
    // then we get too close to +-INFINITY when we forward project.  Tweak
    // this if you start getting Infinity or NaN's for forward().
    protected static float epsilon = 0.01f;

    // world<->screen coordinate offsets
    protected int hy, wx;

    // almost constant projection parameters
    protected float tanCtrLat;
    protected float asinh_of_tanCtrLat;

    /**
     * Construct a Mercator projection.
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public Mercator(
        LatLonPoint center, float scale, int width, int height) {

        super(center, scale, width, height, MercatorType);
    }

    public Mercator(
        LatLonPoint center, float scale, int width, int height, int type) {

        super(center, scale, width, height, type);
    }

//    protected void finalize() {
//      Debug.message("mercator", "Mercator finalized");
//    }

    /**
     * Return stringified description of this projection.
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
        return "Mercator[" + super.toString();
    }

    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.<p>
     *
     */
    protected void computeParameters() {
        Debug.message("mercator", "Mercator.computeParameters()");
        super.computeParameters();

        // do some precomputation of stuff
        tanCtrLat = (float)Math.tan(ctrLat);
        asinh_of_tanCtrLat = (float)MoreMath.asinh(tanCtrLat);

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
        if (lat > NORTH_POLE - epsilon) {
            return NORTH_POLE - epsilon;
        } else if (lat < SOUTH_POLE + epsilon) {
            return SOUTH_POLE + epsilon;
        }
        return lat;
    }

//    protected float forward_x(float lambda) {
//      return scaled_radius * wrap_longitude(lambda - ctrLon) + (float)wx;
//    }

//    protected float forward_y(float phi) {
//      return (float)hy - (scaled_radius *
//          (MoreMath.asinh((float)Math.tan(phi)) -
//           asinh_of_tanCtrLat));
//    }

    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * A point is always plot-able in the Mercator projection (even the North
     * and South poles since we normalize latitude).
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(float lat, float lon) {
        return true;
    }

    /**
     * Projects a point from Lat/Lon space to X/Y space.
     * <p>
     * @param pt LatLonPoint
     * @param p Point retval
     * @return Point p
     */
    public Point forward(LatLonPoint pt, Point p) {
        // first convert to radians, and handle infinity
        float phi = normalize_latitude(pt.radlat_);
        float lambda = pt.radlon_;      // already wrapped

        // same as forward_x and forward_y, and convert to screen coords
        p.x = Math.round(scaled_radius * wrap_longitude(lambda - ctrLon)) + wx;
        p.y = hy - Math.round(scaled_radius *
                              (MoreMath.asinh((float)Math.tan(phi)) -
                               asinh_of_tanCtrLat));
        return p;
    }


    /**
     * Forward projects a lat,lon coordinates.
     * <p>
     * @param lat raw latitude in decimal degrees
     * @param lon raw longitude in decimal degrees
     * @param p Resulting XY Point
     * @return Point p
     */
    public Point forward(float lat, float lon, Point p) {
        // first convert to radians, and normalize
        float phi = normalize_latitude(ProjMath.degToRad(lat));
        float lambda = wrap_longitude(ProjMath.degToRad(lon));

        // same as forward_x and forward_y, and convert to screen coords
        p.x = Math.round(scaled_radius * wrap_longitude(lambda - ctrLon)) + wx;
        p.y = hy - Math.round(scaled_radius *
                              (MoreMath.asinh((float)Math.tan(phi)) -
                               asinh_of_tanCtrLat));
        return p;
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point.
     * <p>
     * @param lat float latitude in radians
     * @param lon float longitude in radians
     * @param p Resulting XY Point
     * @param isRadian bogus argument indicating that lat,lon
     * arguments are in radians
     * @return Point p
     */
    public Point forward(float lat, float lon, 
                         Point p, boolean isRadian)
    {
        // first normalize
        float phi = normalize_latitude(lat);
        float lambda = wrap_longitude(lon);

        // same as forward_x and forward_y, and convert to screen coords
        p.x = Math.round(scaled_radius * wrap_longitude(lambda - ctrLon)) + wx;
        p.y = hy - Math.round(scaled_radius *
                              (MoreMath.asinh((float)Math.tan(phi)) -
                               asinh_of_tanCtrLat));
        return p;
    }

    /**
     * Inverse project a Point.
     * @param pt x,y Point
     * @param llp resulting LatLonPoint
     * @return LatLonPoint llp
     */
    public LatLonPoint inverse(Point pt, LatLonPoint llp) {
        // convert from screen to world coordinates
        int x = pt.x - wx;
        int y = hy - pt.y;

        // inverse project
        // See if you can take advantage of the precalculated array.
        float wc = asinh_of_tanCtrLat * scaled_radius;
        llp.setLatitude(ProjMath.radToDeg(
            (float)Math.atan(MoreMath.sinh((y + wc)/scaled_radius))));
        llp.setLongitude(ProjMath.radToDeg(
            (float)x / scaled_radius + ctrLon));
        
        return llp;
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
        x -= wx;
        y = hy - y;

        // inverse project
        float wc = asinh_of_tanCtrLat * scaled_radius;
        llp.setLatitude(ProjMath.radToDeg(
                (float)Math.atan(MoreMath.sinh((y + wc)/scaled_radius))));
        llp.setLongitude(ProjMath.radToDeg(
                (float)x / scaled_radius + ctrLon));

        return llp;
    }


    /**
     * Computes the best stepping factor for a rhumbline.
     * <p>
     * Computes the best stepping factor between two x,y points in
     * order to interpolate points on a rhumb line.  (We calculate
     * rhumb lines by forward projecting the line in the Mercator
     * projection, and then calculating segments along the straight
     * line between them.)
     * @param pt1 Point
     * @param pt2 Point
     * @return int number of points to use
     */
    protected final static int rhumbStep(Point pt1, Point pt2) {
        int step = (int)DrawUtil.distance(pt1.x, pt1.y, pt2.x, pt2.y);

        if (step > 8192) {
            step = 512;
        } else {
            step >>= 3;// step/8
        }
        return (step == 0) ? 1 : step;
    }


    /**
     * Calculates the points along a rhumbline between two XY points.
     * <p>
     * Loxodromes are straight in the Mercator projection.  Calculate
     * a bunch of extra points between the two points, inverse project
     * back into LatLons and return all the vertices.
     * @param from Point
     * @param to Point
     * @param include_last include the very last point?
     * @param nsegs number of segments
     * @return float[] of lat, lon, lat, lon, ... in RADIANS!
     */
    protected float[] rhumbProject(
        Point from, Point to, boolean include_last, int nsegs) {

        // calculate pixel distance
        if (nsegs < 1) {
            // dynamically calculate the number of segments to draw
            nsegs = DrawUtil.pixel_distance(
                    from.x, from.y, to.x, to.y)>>3;// /8
            if (nsegs == 0)
                nsegs = 1;
            else if (nsegs > MAX_RHUMB_SEGS)
                nsegs = MAX_RHUMB_SEGS;
        }

//      Debug.output(
//              "from=("+from.x+","+from.y+")to=("+to.x+","+to.y+")");
        LatLonPoint llp = inverse(from.x, from.y, new LatLonPoint());
        LatLonPoint llp2 = inverse(to.x, to.y, new LatLonPoint());
//      Debug.output(
//              "invfrom=("+llp.getLatitude()+","+llp.getLongitude()+
//              ")invto=("+llp2.getLatitude()+","+llp2.getLongitude()+")");
//      Debug.output("nsegs="+nsegs);
        // calculate nsegs(+1) extra vertices between endpoints
        int[] xypts =
            DrawUtil.lineSegments(
                    from.x, from.y,     // coords
                    to.x, to.y,
                    nsegs,              // number of segs between
                    include_last,       // include last point
                    new int[nsegs<<1]   // retval
                    );

        float[] llpts = new float[xypts.length];
        for (int i=0; i<llpts.length; i+=2) {
//          System.out.print("("+xypts[i]+","+xypts[i+1]+")");
            inverse(xypts[i], xypts[i+1], llp);
            llpts[i] = llp.radlat_;
            llpts[i+1] = llp.radlon_;
        }
//      Debug.output("");
        return llpts;
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return MercatorName;
    }

    // -------------------------------------------------------------

    /*
    public void testPoint(float lat, float lon) {
        float x, y;
        lon = wrap_longitude(ProjMath.degToRad(lon));
        lat = normalize_latitude(ProjMath.degToRad(lat));
        x = forward_x(lon) - (float)wx;
        y = (float)hy - forward_y(lat);

        Debug.output(
                "(lon="+ProjMath.radToDeg(lon)+",lat="+ ProjMath.radToDeg(lat)+
                ") = (x="+x+",y="+y+")");
        lat = inverse_lat((float)hy-y);
        lon = wrap_longitude(inverse_lon(x+(float)wx));
        Debug.output(
                "(x="+x+",y="+y+") = (lon="+
                ProjMath.radToDeg(lon)+",lat="+ProjMath.radToDeg(lat)+")");
    }

    public static void main (String argv[]) {
        Mercator proj=null;
//      proj = new Mercator(new LatLonPoint(0.0f, -180.0f), 1.0f, 620, 480);
        proj = new Mercator(new LatLonPoint(0.0f, 0.0f), 1.0f, 620, 480);

        // test on unit circle
        proj.setEarthRadius(1.0f);
        Debug.output("setEarthRadius("+proj.getEarthRadius()+")");
        proj.setPPM(1);
        Debug.output("setPPM("+proj.getPPM()+")");
        proj.setScale(1.0f);
        Debug.output("setScale("+proj.getScale()+")");
        Debug.output(proj);

        Debug.output("---testing latitude");
        proj.testPoint(0.0f, 0.0f);
        proj.testPoint(10.0f, 0.0f);
        proj.testPoint(40.0f, 0.0f);
        proj.testPoint(-85.0f, 0.0f);
        proj.testPoint(-80.0f, 0.0f);
        proj.testPoint(-90.0f, 0.0f);
        proj.testPoint(100.0f, 0.0f);
        proj.testPoint(-3272.0f, 0.0f);
        Debug.output("---testing longitude");
        proj.testPoint(0.0f, 10.0f);
        proj.testPoint(0.0f, -10.0f);
        proj.testPoint(0.0f, 90.0f);
        proj.testPoint(0.0f, -90.0f);
        proj.testPoint(0.0f, 175.0f);
        proj.testPoint(0.0f, -175.0f);
        proj.testPoint(0.0f, 180.0f);
        proj.testPoint(0.0f, -180.0f);
        proj.testPoint(0.0f, 190.0f);
        proj.testPoint(0.0f, -190.0f);
        proj.testPoint(0.0f, 370.0f);
        proj.testPoint(0.0f, -370.0f);
        proj.testPoint(0.0f, 550.0f);
        proj.testPoint(0.0f, -550.0f);

        float LAT_RANGE = (float)Math.PI;
        float LON_RANGE = (float)Math.PI*2f;
        float HALF_LAT = (float)Math.PI/2f;
        float HALF_LON = (float)Math.PI;

        System.out.print("timing forward: ");
        long start = System.currentTimeMillis();
        for (int i = 0; i< 100000; i++) {
            proj.forward_x((float)Math.random()*LON_RANGE-HALF_LON);
            proj.forward_y((float)Math.random()*LAT_RANGE-HALF_LAT);
        }
        long stop = System.currentTimeMillis();
        Debug.output((stop - start)/1000.0d + " seconds.");
    }
    */
}
