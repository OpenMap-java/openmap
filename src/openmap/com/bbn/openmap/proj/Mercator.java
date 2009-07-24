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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Mercator.java,v $
// $RCSfile: Mercator.java,v $
// $Revision: 1.8 $
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
 * Implements the Mercator projection.
 */
public class Mercator extends Cylindrical {

    /**
     * The Mercator name.
     */
    public final static transient String MercatorName = "Mercator";

    // maximum number of segments to draw for rhumblines.
    protected final static int MAX_RHUMB_SEGS = 512;

    /*
     * HACK epsilon: skirt the edge of the infinite. If this is too small then
     * we get too close to +-INFINITY when we forward project. Tweak this if you
     * start getting Infinity or NaN's for forward().
     */
    protected static double epsilon = 0.01f;

    // world<->screen coordinate offsets
    protected transient double hy, wx;

    // almost constant projection parameters
    protected transient double tanCtrLat;
    protected transient double asinh_of_tanCtrLat;

    /**
     * Construct a Mercator projection.
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public Mercator(LatLonPoint center, float scale, int width, int height) {
        super(center, scale, width, height);
    }

    // protected void finalize() {
    // Debug.message("mercator", "Mercator finalized");
    // }

    /**
     * Return stringified description of this projection.
     * 
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
        return "Mercator[" + super.toString();
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
        Debug.message("mercator", "Mercator.computeParameters()");
        super.computeParameters();

        // do some precomputation of stuff
        tanCtrLat = Math.tan(centerY);
        asinh_of_tanCtrLat = MoreMath.asinh(tanCtrLat);

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
        if (lat > NORTH_POLE - epsilon) {
            return NORTH_POLE - epsilon;
        } else if (lat < SOUTH_POLE + epsilon) {
            return SOUTH_POLE + epsilon;
        }
        return lat;
    }

    // protected float forward_x(float lambda) {
    // return scaled_radius * wrap_longitude(lambda - ctrLon) +
    // (float)wx;
    // }

    // protected float forward_y(float phi) {
    // return (float)hy - (scaled_radius *
    // (MoreMath.asinh((float)Math.tan(phi)) -
    // asinh_of_tanCtrLat));
    // }

    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * A point is always plot-able in the Mercator projection (even the North
     * and South poles since we normalize latitude).
     * 
     * @param lat double latitude in decimal degrees
     * @param lon double longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(double lat, double lon) {
        return true;
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point2D.
     * <p>
     * 
     * @param lat double latitude in radians
     * @param lon double longitude in radians
     * @param p Resulting XY Point2D
     * @param isRadian bogus argument indicating that lat,lon arguments are in
     *        radians
     * @return Point2D p
     */
    public Point2D forward(double lat, double lon, Point2D p, boolean isRadian) {
        if (!isRadian) {
            lat = ProjMath.degToRad(lat);
            lon = ProjMath.degToRad(lon);
        }
        // first normalize
        lat = normalizeLatitude(lat);
        lon = wrapLongitude(lon);

        // same as forward_x and forward_y, and convert to screen
        // coords
		double x = (scaled_radius * wrapLongitude(lon - centerX)) + wx;
		double y = hy
				- (scaled_radius * (MoreMath.asinh(Math.tan(lat)) - asinh_of_tanCtrLat));
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

        // inverse project
        double wc = asinh_of_tanCtrLat * scaled_radius;

        llp.setLocation(Math.toDegrees(wrapLongitude(x / scaled_radius + centerX)),
                Math.toDegrees(normalizeLatitude(Math.atan(MoreMath.sinh((y + wc) / scaled_radius)))));

        return llp;
    }

    /**
     * Computes the best stepping factor for a rhumbline.
     * <p>
     * Computes the best stepping factor between two x,y points in order to
     * interpolate points on a rhumb line. (We calculate rhumb lines by forward
     * projecting the line in the Mercator projection, and then calculating
     * segments along the straight line between them.)
     * 
     * @param pt1 Point2D
     * @param pt2 Point2D
     * @return int number of points to use
     */
    protected final static int rhumbStep(Point2D pt1, Point2D pt2) {
        int step = (int) DrawUtil.distance(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());

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
     * Loxodromes are straight in the Mercator projection. Calculate a bunch of
     * extra points between the two points, inverse project back into LatLons
     * and return all the vertices.
     * 
     * @param from Point2D
     * @param to Point2D
     * @param include_last include the very last point?
     * @param nsegs number of segments
     * @return float[] of lat, lon, lat, lon, ... in RADIANS!
     */
    protected float[] rhumbProject(Point2D from, Point2D to, boolean include_last,
                                   int nsegs) {

        // calculate pixel distance
        if (nsegs < 1) {
            // dynamically calculate the number of segments to draw
            nsegs = DrawUtil.pixel_distance((int)from.getX(), (int)from.getY(), (int)to.getX(), (int)to.getY()) >> 3;// /8
            if (nsegs == 0)
                nsegs = 1;
            else if (nsegs > MAX_RHUMB_SEGS)
                nsegs = MAX_RHUMB_SEGS;
        }

        // Debug.output(
        // "from=("+from.x+","+from.y+")to=("+to.x+","+to.y+")");
        LatLonPoint llp = new LatLonPoint.Double();
        // inverse(from.x, from.y, llp);
        // LatLonPoint llp2 = inverse(to.x, to.y, new LatLonPoint());
        // Debug.output(
        // "invfrom=("+llp.getLatitude()+","+llp.getLongitude()+
        // ")invto=("+llp2.getLatitude()+","+llp2.getLongitude()+")");
        // Debug.output("nsegs="+nsegs);
        // calculate nsegs(+1) extra vertices between endpoints
        int[] xypts = DrawUtil.lineSegments((int)from.getX(), (int)from.getY(), // coords
                (int)to.getX(), (int)to.getY(), nsegs, // number of segs between
                include_last, // include last point
                new int[nsegs << 1] // retval
                );

        float[] llpts = new float[xypts.length];
        for (int i = 0; i < llpts.length; i += 2) {
            // System.out.print("("+xypts[i]+","+xypts[i+1]+")");
            inverse(xypts[i], xypts[i + 1], llp);
            llpts[i] = (float) llp.getRadLat();
            llpts[i + 1] = (float) llp.getRadLon();
        }
        // Debug.output("");
        return llpts;
    }

    /**
     * Calculates the points along a rhumbline between two XY points.
     * <p>
     * Loxodromes are straight in the Mercator projection. Calculate a bunch of
     * extra points between the two points, inverse project back into LatLons
     * and return all the vertices.
     * 
     * @param from Point2D
     * @param to Point2D
     * @param include_last include the very last point?
     * @param nsegs number of segments
     * @return double[] of lat, lon, lat, lon, ... in RADIANS!
     */
    protected double[] rhumbProjectDouble(Point2D from, Point2D to,
                                          boolean include_last, int nsegs) {

        // calculate pixel distance
        if (nsegs < 1) {
            // dynamically calculate the number of segments to draw
            nsegs = DrawUtil.pixel_distance((int)from.getX(), (int)from.getY(), (int)to.getX(), (int)to.getY()) >> 3;// /8
            if (nsegs == 0)
                nsegs = 1;
            else if (nsegs > MAX_RHUMB_SEGS)
                nsegs = MAX_RHUMB_SEGS;
        }

        // Debug.output(
        // "from=("+from.x+","+from.y+")to=("+to.x+","+to.y+")");
        LatLonPoint llp = new LatLonPoint.Double();
        // inverse(from.x, from.y, llp);
        // LatLonPoint llp2 = inverse(to.x, to.y, new LatLonPoint());
        // Debug.output(
        // "invfrom=("+llp.getLatitude()+","+llp.getLongitude()+
        // ")invto=("+llp2.getLatitude()+","+llp2.getLongitude()+")");
        // Debug.output("nsegs="+nsegs);
        // calculate nsegs(+1) extra vertices between endpoints
        int[] xypts = DrawUtil.lineSegments((int)from.getX(), (int)from.getY(), // coords
                (int)to.getX(), (int)to.getY(), nsegs, // number of segs between
                include_last, // include last point
                new int[nsegs << 1] // retval
                );

        double[] llpts = new double[xypts.length];
        for (int i = 0; i < llpts.length; i += 2) {
            // System.out.print("("+xypts[i]+","+xypts[i+1]+")");
            inverse(xypts[i], xypts[i + 1], llp);
            llpts[i] = llp.getRadLat();
            llpts[i + 1] = llp.getRadLon();
        }
        // Debug.output("");
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
     * public void testPoint(float lat, float lon) { float x, y; lon =
     * wrap_longitude(ProjMath.degToRad(lon)); lat =
     * normalize_latitude(ProjMath.degToRad(lat)); x = forward_x(lon) -
     * (float)wx; y = (float)hy - forward_y(lat);
     * 
     * Debug.output( "(lon="+ProjMath.radToDeg(lon)+",lat="+
     * ProjMath.radToDeg(lat)+ ") = (x="+x+",y="+y+")"); lat =
     * inverse_lat((float)hy-y); lon = wrap_longitude(inverse_lon(x+(float)wx));
     * Debug.output( "(x="+x+",y="+y+") = (lon="+
     * ProjMath.radToDeg(lon)+",lat="+ProjMath.radToDeg(lat)+")"); }
     * 
     * public static void main (String argv[]) { Mercator proj=null; // proj =
     * new Mercator(new LatLonPoint(0.0f, -180.0f), 1.0f, 620, 480); proj = new
     * Mercator(new LatLonPoint(0.0f, 0.0f), 1.0f, 620, 480); // test on unit
     * circle proj.setEarthRadius(1.0f);
     * Debug.output("setEarthRadius("+proj.getEarthRadius()+")");
     * proj.setPPM(1); Debug.output("setPPM("+proj.getPPM()+")");
     * proj.setScale(1.0f); Debug.output("setScale("+proj.getScale()+")");
     * Debug.output(proj);
     * 
     * Debug.output("---testing latitude"); proj.testPoint(0.0f, 0.0f);
     * proj.testPoint(10.0f, 0.0f); proj.testPoint(40.0f, 0.0f);
     * proj.testPoint(-85.0f, 0.0f); proj.testPoint(-80.0f, 0.0f);
     * proj.testPoint(-90.0f, 0.0f); proj.testPoint(100.0f, 0.0f);
     * proj.testPoint(-3272.0f, 0.0f); Debug.output("---testing longitude");
     * proj.testPoint(0.0f, 10.0f); proj.testPoint(0.0f, -10.0f);
     * proj.testPoint(0.0f, 90.0f); proj.testPoint(0.0f, -90.0f);
     * proj.testPoint(0.0f, 175.0f); proj.testPoint(0.0f, -175.0f);
     * proj.testPoint(0.0f, 180.0f); proj.testPoint(0.0f, -180.0f);
     * proj.testPoint(0.0f, 190.0f); proj.testPoint(0.0f, -190.0f);
     * proj.testPoint(0.0f, 370.0f); proj.testPoint(0.0f, -370.0f);
     * proj.testPoint(0.0f, 550.0f); proj.testPoint(0.0f, -550.0f);
     * 
     * float LAT_RANGE = (float)Math.PI; float LON_RANGE = (float)Math.PI*2f;
     * float HALF_LAT = (float)Math.PI/2f; float HALF_LON = (float)Math.PI;
     * 
     * System.out.print("timing forward: "); long start =
     * System.currentTimeMillis(); for (int i = 0; i < 100000; i++) {
     * proj.forward_x((float)Math.random()*LON_RANGE-HALF_LON);
     * proj.forward_y((float)Math.random()*LAT_RANGE-HALF_LAT); } long stop =
     * System.currentTimeMillis(); Debug.output((stop - start)/1000.0d + "
     * seconds."); }
     */
}