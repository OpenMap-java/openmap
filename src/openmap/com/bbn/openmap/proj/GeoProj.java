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
//$RCSfile: GeoProj.java,v $
//$Revision: 1.7 $
//$Date: 2009/02/25 22:34:04 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.proj.coords.LatLonGCT;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * GeoProj is the base class of all Projections that deal with coordinates on
 * the world sphere.
 * <p>
 * You probably don't want to use this class unless you are hacking your own
 * projections, or need extended functionality. To be safe you will want to use
 * the Projection interface.
 * 
 * <h3>Notes:</h3>
 * 
 * <ul>
 * 
 * <li>We deal in radians internally. The outside world usually deals in decimal
 * degrees. If you have data in radians, DON'T bother converting it into DD's
 * since we'll convert it right back into radians for the projection step. For
 * more optimization tips, see the OMPoly class.
 * 
 * <li>We default to projecting our data using the WGS84 datum. You can change
 * the appropriate parameters of the projection after construction if you need
 * to use a different datum. And of course you can derive your own projections
 * from this class as you see fit.
 * 
 * <li>The forward() and inverse() methods are currently implemented using the
 * algorithms given in John Synder's <i>Map Projections --A Working Manual </i>
 * for the sphere. This is sufficient for display purposes, but you should use
 * ellipsoidal algorithms in the GreatCircle class to calculate distance and
 * azimuths on the ellipsoid. See each projection individually for more
 * information.
 * 
 * <li>This class is not thread safe. If two or more threads are using the same
 * Proj, then they could disrupt each other. Occasionally you may need to call a
 * <code>set</code> method of this class. This might interfere with another
 * thread that's using the same projection for <code>forwardPoly</code> or
 * another Projection interface method. In general, you should not need to call
 * any of the <code>set</code> methods directly, but let the MapBean do it for
 * you.
 * 
 * <li>All the various <code>forwardOBJ()</code> methods for ArrayList graphics
 * ultimately go through <code>forwardPoly()</code>.
 * 
 * </ul>
 * 
 * @see Projection
 * @see Cylindrical
 * @see Mercator
 * @see CADRG
 * @see Azimuth
 * @see Orthographic
 * @see Planet
 * @see GreatCircle
 * @see com.bbn.openmap.omGraphics.OMPoly
 * 
 */
public abstract class GeoProj
        extends Proj {
    // Used for generating segments of ArrayList objects
    protected static transient int NUM_DEFAULT_CIRCLE_VERTS = 64;

    // SOUTH_POLE <= phi <= NORTH_POLE (radians)
    // -DATELINE <= lambda <= DATELINE (radians)

    /**
     * North pole latitude in radians.
     */
    public final static transient float NORTH_POLE = ProjMath.NORTH_POLE_F;

    /**
     * South pole latitude in radians.
     */
    public final static transient float SOUTH_POLE = ProjMath.SOUTH_POLE_F;

    /**
     * Dateline longitude in radians.
     */
    public final static transient float DATELINE = ProjMath.DATELINE_F;

    // Used for generating segments of ArrayList objects
    protected static transient int NUM_DEFAULT_GREAT_SEGS = 512;

    // pixels per meter (an extra scaling factor).
    protected double pixelsPerMeter; // PPM
    protected double planetRadius;// EARTH_RADIUS
    protected double planetPixelRadius; // EARTH_PIX_RADIUS
    protected double planetPixelCircumference; // EARTH_PIX_CIRCUMFERENCE
    protected double scaled_radius;

    protected Mercator mercator = null; // for rhumbline calculations

    public GeoProj(LatLonPoint center, float s, int w, int h) {
        super(center, s, w, h);

        // for rhumbline projecting
        if (!(this instanceof Mercator)) {
            mercator = new Mercator(center, (float) scale, width, height);
        }
    }

    protected void init() {

        centerX = wrapLongitude(Math.toRadians(centerX));
        centerY = normalizeLatitude(Math.toRadians(centerY));

        // pixels per meter (an extra scaling factor).
        pixelsPerMeter = Planet.defaultPixelsPerMeter; // PPM
        planetRadius = Planet.wgs84_earthEquatorialRadiusMeters_D;// EARTH_RADIUS
        planetPixelRadius = planetRadius * pixelsPerMeter; // EARTH_PIX_RADIUS
        planetPixelCircumference = MoreMath.TWO_PI_D * planetPixelRadius; // EARTH_PIX_CIRCUMFERENCE
        // the scaled_radius should also be set in computeParameters with the
        // scale that has been checked against min/max scale
        scaled_radius = planetPixelRadius / scale;
        /* good for cylindrical */
        maxscale = planetPixelCircumference / width;
    }

    /**
     * Set the pixels per meter constant.
     * 
     * @param ppm int Pixels Per Meter scale-factor constant
     */
    public void setPPM(double ppm) {
        pixelsPerMeter = ppm;
        if (pixelsPerMeter < 1) {
            pixelsPerMeter = 1;
        }
        computeParameters();
    }

    /**
     * Get the pixels-per-meter constant.
     * 
     * @return int Pixels Per Meter scale-factor constant
     */
    public double getPPM() {
        return pixelsPerMeter;
    }

    /**
     * Set the planet radius.
     * 
     * @param radius float planet radius in meters
     */
    public void setPlanetRadius(double radius) {
        planetRadius = radius;
        if (planetRadius < 1.0f) {
            planetRadius = 1.0f;
        }
        computeParameters();
    }

    /**
     * Get the planet radius.
     * 
     * @return float radius of planet in meters
     */
    public double getPlanetRadius() {
        return planetRadius;
    }

    /**
     * Get the planet pixel radius.
     * 
     * @return float radius of planet in pixels
     */
    public double getPlanetPixelRadius() {
        return planetPixelRadius;
    }

    /**
     * Get the planet pixel circumference.
     * 
     * @return float circumference of planet in pixels
     */
    public double getPlanetPixelCircumference() {
        return planetPixelCircumference;
    }

    /**
     * Set center point of projection.
     * 
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     */
    public void setCenter(float lat, float lon) {
        setCenter((double) lat, (double) lon, false);
    }

    /**
     * Set center point of projection.
     * 
     * @param lat double latitude in decimal degrees
     * @param lon double longitude in decimal degrees
     */
    public void setCenter(double lat, double lon) {
        setCenter(lat, lon, false);
    }

    /**
     * Set center point of projection.
     * 
     * @param lat double latitude in decimal degrees
     * @param lon double longitude in decimal degrees
     */
    public void setCenter(double lat, double lon, boolean isRadians) {
        if (!isRadians) {
            lat = Math.toRadians(lat);
            lon = Math.toRadians(lon);
        }

        centerX = wrapLongitude(lon);
        centerY = normalizeLatitude(lat);
        computeParameters();
        projID = null;
    }

    /**
     * Get center point of projection.
     * 
     * @return LatLonPoint center of projection, created just for you.
     */
    public LatLonPoint getCenter() {
        return new LatLonPoint.Double(centerY, centerX, true);
    }

    /**
     * Returns a center LatLonPoint that was provided, with the location filled
     * into the LatLonPoint object. Calls Point2D.setLocation(x, y).
     */
    public <T extends Point2D> T getCenter(T center) {
        center.setLocation(Math.toDegrees(centerX), Math.toDegrees(centerY));
        return center;
    }

    /**
     * Sets radian latitude to something sane.
     * <p>
     * Normalizes the latitude according to the particular projection.
     * 
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see ProjMath#normalizeLatitude(float, float)
     * @see LatLonPoint#normalizeLatitude(float)
     */
    public float normalizeLatitude(float lat) {
        return (float) normalizeLatitude((double) lat);
    }

    abstract public double normalizeLatitude(double lat);

    /**
     * Sets radian longitude to something sane.
     * 
     * @param lon float longitude in radians
     * @return float longitude (-PI &lt;= x &lt; PI)
     * @see ProjMath#wrapLongitude(float)
     * @see LatLonPoint#wrapLongitude(float)
     */
    public final static float wrapLongitude(float lon) {
        return ProjMath.wrapLongitude(lon);
    }

    public final static double wrapLongitude(double lon) {
        return ProjMath.wrapLongitude(lon);
    }

    public final static double wrapLongitudeDeg(double lon) {
        return ProjMath.wrapLongitudeDeg(lon);
    }

    /**
     * @deprecated use normalizeLatitude() instead.
     */
    public final double normalize_latitude(double lat) {
        return normalizeLatitude(lat);
    }

    /**
     * @deprecated use wrapLongitude() instead.
     */
    public final static double wrap_longitude(double lon) {
        return wrapLongitude(lon);
    }

    /**
     * Pan the map/projection.
     * <p>
     * Example pans:
     * <ul>
     * <li><code>pan(180, c)</code> pan south `c' degrees
     * <li><code>pan(-90, c)</code> pan west `c' degrees
     * <li><code>pan(0, c)</code> pan north `c' degrees
     * <li><code>pan(90, c)</code> pan east `c' degrees
     * </ul>
     * 
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     * @param c arc distance in decimal degrees
     */
    public void pan(float Az, float c) {
        setCenter(GreatCircle.sphericalBetween(centerY, centerX, ProjMath.degToRad(c), ProjMath.degToRad(Az)));
    }

    /**
     * Stringify the projection.
     * 
     * @return stringified projection
     * @see #getProjectionID
     */
    public String toString() {
        return (" radius=" + planetRadius + " ppm=" + pixelsPerMeter + " center(" + centerY + ":" + centerX + ") scale=" + scale
                + " maxscale=" + maxscale + " minscale=" + minscale + " width=" + width + " height=" + height + "]");
    }

    /**
     * Copies this projection.
     * 
     * @return a copy of this projection.
     */
    public Object clone() {
        GeoProj proj = (GeoProj) super.clone();
        if (mercator != null) {
            proj.mercator = (Mercator) mercator.clone();
        }
        return proj;
    }

    public boolean isPlotable(Point2D point) {
        if (point instanceof LatLonPoint) {
            return isPlotable(point.getY(), point.getX());
        }

        // Well, then, what is it?
        return false;
    }

    /**
     * Forward project a rhumbline poly.
     * <p>
     * Draws rhumb lines between vertices of poly. Remember to specify vertices
     * in radians! Check in-code comments for details about the algorithm.
     * 
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param nsegs number of segments to draw for greatcircle or rhumb lines
     *        (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList<float[]> of x[], y[], x[], y[], ... projected poly
     */
    protected ArrayList<float[]> forwardRhumbPoly(float[] rawllpts, int nsegs, boolean isFilled) {

        // IDEA:
        // Rhumblines are straight in the Mercator projection.
        // So we can use the Mercator projection to calculate
        // vertices along the rhumbline between two points. But
        // if there's a better way to calculate loxodromes,
        // someone please chime in (openmap@bbn.com)...
        //
        // ALG:
        // Project pairs of vertices through the Mercator
        // projection into screen XY space, pick intermediate
        // segment points along the straight XY line, then
        // convert all vertices back into LatLon space. Pass the
        // augmented vertices to _forwardPoly() to be drawn as
        // straight segments.
        //
        // WARNING:
        // The algorithm fixes the Cylindrical-wrapping
        // problem, and thus duplicates some code in
        // Cylindrical._forwardPoly()

        if (this instanceof Mercator) {// simple
            return _forwardPoly(rawllpts, LineType.Straight, nsegs, isFilled);
        }

        int i, n, xp, flag = 0, xadj = 0, totalpts = 0;
        Point from = new Point(0, 0);
        Point to = new Point(0, 0);
        int len = rawllpts.length;

        float[][] augllpts = new float[len >>> 1][0];

        // lock access to object global, since this is probably not
        // cloned and different layers may be accessing this.
        // synchronized (mercator) {

        // we now create a clone of the mercator variable in
        // makeClone(), so since different objects should be using
        // their clone instead of the main projection, the
        // synchronization should be unneeded.

        // use mercator projection to calculate rhumblines.
        // mercator.setParms(
        // new LatLonPoint(ctrLat, ctrLon, true),
        // scale, width, height);

        // Unnecessary to set parameters !! ^^^^^

        // project everything through the Mercator projection,
        // building up lat/lon points along the original rhumb
        // line between vertices.
        mercator.forward(rawllpts[0], rawllpts[1], from, true);
        xp = from.x;
        for (i = 0, n = 2; n < len; i++, n += 2) {
            mercator.forward(rawllpts[n], rawllpts[n + 1], to, true);
            // segment crosses longitude along screen edge
            if (Math.abs(xp - to.x) >= mercator.half_world) {
                flag += (xp < to.x) ? -1 : 1;// inc/dec the wrap
                // count
                xadj = flag * mercator.world.x;// adjustment to x
                // coordinates
                // Debug.output("flag=" + flag + " xadj=" + xadj);
            }
            xp = to.x;
            if (flag != 0) {
                to.x += xadj;// adjust x coordinate
            }

            augllpts[i] = mercator.rhumbProject(from, to, false, nsegs);
            totalpts += augllpts[i].length;
            from.x = to.x;
            from.y = to.y;
        }

        LatLonPoint llp = new LatLonPoint.Double();
        mercator.inverse(from.x, from.y, llp);
        // }// end synchronized around mercator

        augllpts[i] = new float[2];
        augllpts[i][0] = (float) llp.getRadLat();
        augllpts[i][1] = (float) llp.getRadLon();
        totalpts += 2;

        // put together all the points
        float[] newllpts = new float[totalpts];
        int pos = 0;
        for (i = 0; i < augllpts.length; i++) {
            // Debug.output("copying " + augllpts[i].length + "
            // floats");
            System.arraycopy(
            /* src */augllpts[i], 0,
            /* dest */newllpts, pos, augllpts[i].length);
            pos += augllpts[i].length;
        }
        // Debug.output("done copying " + totalpts + " total floats");

        // free unused variables
        augllpts = null;

        // now delegate the work to the regular projection code.
        return _forwardPoly(newllpts, LineType.Straight, -1, isFilled);
    }

    /**
     * Forward project a rhumbline poly.
     * <p>
     * Draws rhumb lines between vertices of poly. Remember to specify vertices
     * in radians! Check in-code comments for details about the algorithm.
     * 
     * @param rawllpts double[] of lat,lon,lat,lon,... in RADIANS!
     * @param nsegs number of segments to draw for greatcircle or rhumb lines
     *        (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList<float[]> of x[], y[], x[], y[], ... projected poly
     */
    protected ArrayList<float[]> forwardRhumbPoly(double[] rawllpts, int nsegs, boolean isFilled) {

        // IDEA:
        // Rhumblines are straight in the Mercator projection.
        // So we can use the Mercator projection to calculate
        // vertices along the rhumbline between two points. But
        // if there's a better way to calculate loxodromes,
        // someone please chime in (openmap@bbn.com)...
        //
        // ALG:
        // Project pairs of vertices through the Mercator
        // projection into screen XY space, pick intermediate
        // segment points along the straight XY line, then
        // convert all vertices back into LatLon space. Pass the
        // augmented vertices to _forwardPoly() to be drawn as
        // straight segments.
        //
        // WARNING:
        // The algorithm fixes the Cylindrical-wrapping
        // problem, and thus duplicates some code in
        // Cylindrical._forwardPoly()

        if (this instanceof Mercator) {// simple
            return _forwardPoly(rawllpts, LineType.Straight, nsegs, isFilled);
        }

        int i, n, xp, flag = 0, xadj = 0, totalpts = 0;
        Point from = new Point(0, 0);
        Point to = new Point(0, 0);
        int len = rawllpts.length;

        double[][] augllpts = new double[len >>> 1][0];

        // lock access to object global, since this is probably not
        // cloned and different layers may be accessing this.
        // synchronized (mercator) {

        // we now create a clone of the mercator variable in
        // makeClone(), so since different objects should be using
        // their clone instead of the main projection, the
        // synchronization should be unneeded.

        // use mercator projection to calculate rhumblines.
        // mercator.setParms(
        // new LatLonPoint(ctrLat, ctrLon, true),
        // scale, width, height);

        // Unnecessary to set parameters !! ^^^^^

        // project everything through the Mercator projection,
        // building up lat/lon points along the original rhumb
        // line between vertices.
        mercator.forward(rawllpts[0], rawllpts[1], from, true);
        xp = from.x;
        for (i = 0, n = 2; n < len; i++, n += 2) {
            mercator.forward(rawllpts[n], rawllpts[n + 1], to, true);
            // segment crosses longitude along screen edge
            if (Math.abs(xp - to.x) >= mercator.half_world) {
                flag += (xp < to.x) ? -1 : 1;// inc/dec the wrap
                // count
                xadj = flag * mercator.world.x;// adjustment to x
                // coordinates
                // Debug.output("flag=" + flag + " xadj=" + xadj);
            }
            xp = to.x;
            if (flag != 0) {
                to.x += xadj;// adjust x coordinate
            }

            augllpts[i] = mercator.rhumbProjectDouble(from, to, false, nsegs);
            totalpts += augllpts[i].length;
            from.x = to.x;
            from.y = to.y;
        }

        LatLonPoint llp = new LatLonPoint.Double();
        mercator.inverse(from, llp);
        // }// end synchronized around mercator

        augllpts[i] = new double[2];
        augllpts[i][0] = llp.getRadLat();
        augllpts[i][1] = llp.getRadLon();
        totalpts += 2;

        // put together all the points
        double[] newllpts = new double[totalpts];
        int pos = 0;
        for (i = 0; i < augllpts.length; i++) {
            // Debug.output("copying " + augllpts[i].length + "
            // floats");
            System.arraycopy(
            /* src */augllpts[i], 0,
            /* dest */newllpts, pos, augllpts[i].length);
            pos += augllpts[i].length;
        }
        // Debug.output("done copying " + totalpts + " total floats");

        // free unused variables
        augllpts = null;

        // now delegate the work to the regular projection code.
        return _forwardPoly(newllpts, LineType.Straight, -1, isFilled);
    }

    /**
     * Forward project a greatcircle poly.
     * <p>
     * Draws great circle lines between vertices of poly. Remember to specify
     * vertices in radians!
     * 
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param nsegs number of segments to draw for greatcircle or rhumb lines
     *        (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList<float[]> of x[], y[], x[], y[], ... projected poly
     */
    protected ArrayList<float[]> forwardGreatPoly(float[] rawllpts, int nsegs, boolean isFilled) {
        int i, j, k, totalpts = 0;

        Point from = new Point();
        Point to = new Point();

        int end = rawllpts.length >>> 1;
        float[][] augllpts = new float[end][0];
        end -= 1;// stop before last segment

        // calculate extra vertices between all the original segments.
        forward(rawllpts[0], rawllpts[1], from, true);
        for (i = 0, j = 0, k = 2; i < end; i++, j += 2, k += 2) {
            forward(rawllpts[k], rawllpts[k + 1], to, true);
            augllpts[i] = getGreatVertices(rawllpts[j], rawllpts[j + 1], rawllpts[k], rawllpts[k + 1], from, to, false, nsegs);
            from.x = to.x;
            from.y = to.y;
            totalpts += augllpts[i].length;
        }
        augllpts[i] = new float[2];
        augllpts[i][0] = rawllpts[j];
        augllpts[i][1] = rawllpts[j + 1];
        totalpts += 2;

        // put together all the points
        float[] newllpts = new float[totalpts];
        int pos = 0;
        for (i = 0; i < augllpts.length; i++) {
            System.arraycopy(
            /* src */augllpts[i], 0,
            /* dest */newllpts, pos, augllpts[i].length);
            pos += augllpts[i].length;
        }

        // free unused variables
        augllpts = null;

        // now delegate the work to the regular projection code.
        return _forwardPoly(newllpts, LineType.Straight, -1, isFilled);
    }

    /**
     * Forward project a greatcircle poly.
     * <p>
     * Draws great circle lines between vertices of poly. Remember to specify
     * vertices in radians!
     * 
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param nsegs number of segments to draw for greatcircle or rhumb lines
     *        (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList<float[]> of x[], y[], x[], y[], ... projected poly
     */
    protected ArrayList<float[]> forwardGreatPoly(double[] rawllpts, int nsegs, boolean isFilled) {
        int i, j, k, totalpts = 0;

        Point from = new Point();
        Point to = new Point();

        int end = rawllpts.length >>> 1;
        double[][] augllpts = new double[end][0];
        end -= 1;// stop before last segment

        // calculate extra vertices between all the original segments.
        forward(rawllpts[0], rawllpts[1], from, true);
        for (i = 0, j = 0, k = 2; i < end; i++, j += 2, k += 2) {
            forward(rawllpts[k], rawllpts[k + 1], to, true);
            augllpts[i] = getGreatVertices(rawllpts[j], rawllpts[j + 1], rawllpts[k], rawllpts[k + 1], from, to, false, nsegs);
            from.x = to.x;
            from.y = to.y;
            totalpts += augllpts[i].length;
        }
        augllpts[i] = new double[2];
        augllpts[i][0] = rawllpts[j];
        augllpts[i][1] = rawllpts[j + 1];
        totalpts += 2;

        // put together all the points
        double[] newllpts = new double[totalpts];
        int pos = 0;
        for (i = 0; i < augllpts.length; i++) {
            System.arraycopy(
            /* src */augllpts[i], 0,
            /* dest */newllpts, pos, augllpts[i].length);
            pos += augllpts[i].length;
        }

        // free unused variables
        augllpts = null;

        // now delegate the work to the regular projection code.
        return _forwardPoly(newllpts, LineType.Straight, -1, isFilled);
    }

    /**
     * Get the vertices along the great circle between two points.
     * 
     * @param latp previous float latitude
     * @param lonp previous float longitude
     * @param latn next float latitude
     * @param lonn next float longitude
     * @param from Point
     * @param to Point
     * @param include_last include n or n+1 points of the n segments?
     * @return float[] lat/lon points in RADIANS!
     * 
     */
    private float[] getGreatVertices(float latp, float lonp, float latn, float lonn, Point from, Point to, boolean include_last,
                                     int nsegs) {
        if (nsegs < 1) {
            // calculate pixel distance
            int dist = DrawUtil.pixel_distance(from.x, from.y, to.x, to.y);

            /*
             * determine what would be a decent number of segments to draw.
             * HACK: this is hardcoded calculated by what might look ok on
             * screen. We also put a cap on the number of extra segments we
             * draw.
             */
            nsegs = dist >> 3;// dist/8
            if (nsegs == 0) {
                nsegs = 1;
            } else if (nsegs > NUM_DEFAULT_GREAT_SEGS) {
                nsegs = NUM_DEFAULT_GREAT_SEGS;
            }

            // Debug.output(
            // "("+from.x+","+from.y+")("+to.x+","+to.y+")
            // dist="+dist+" nsegs="+nsegs);
        }

        // both of these return float[] radian coordinates!
        return GreatCircle.greatCircle(latp, lonp, latn, lonn, nsegs, include_last);
    }

    /**
     * Get the vertices along the great circle between two points.
     * 
     * @param latp previous double latitude
     * @param lonp previous double longitude
     * @param latn next double latitude
     * @param lonn next double longitude
     * @param from Point
     * @param to Point
     * @param include_last include n or n+1 points of the n segments?
     * @param nsegs number of segments to create, or -1 to let algorithm figure
     *        it out
     * @return double[] lat/lon points in RADIANS!
     * 
     */
    private double[] getGreatVertices(double latp, double lonp, double latn, double lonn, Point from, Point to,
                                      boolean include_last, int nsegs) {
        if (nsegs < 1) {
            // calculate pixel distance
            int dist = DrawUtil.pixel_distance(from.x, from.y, to.x, to.y);

            /*
             * determine what would be a decent number of segments to draw.
             * HACK: this is hardcoded calculated by what might look ok on
             * screen. We also put a cap on the number of extra segments we
             * draw.
             */
            nsegs = dist >> 3;// dist/8
            if (nsegs == 0) {
                nsegs = 1;
            } else if (nsegs > NUM_DEFAULT_GREAT_SEGS) {
                nsegs = NUM_DEFAULT_GREAT_SEGS;
            }

            // Debug.output(
            // "("+from.x+","+from.y+")("+to.x+","+to.y+")
            // dist="+dist+" nsegs="+nsegs);
        }

        // both of these return float[] radian coordinates!
        return GreatCircle.greatCircle(latp, lonp, latn, lonn, nsegs, include_last);
    }

    /**
     * Check for complicated linetypes.
     * <p>
     * This depends on the line and this projection.
     * 
     * @param ltype int LineType
     * @return boolean
     */
    public boolean isComplicatedLineType(int ltype) {
        switch (ltype) {
            case LineType.Straight:
                return false;
            case LineType.Rhumb:
                return (getClass() == Mercator.class) ? false : true;
            case LineType.GreatCircle:
                return true/*
                            * (getProjectionType() == Gnomonic.GnomonicType) ?
                            * false : true
                            */;
            default:
                Debug.error("Proj.isComplicatedLineType: invalid LineType!");
                return false;
        }
    }

    /**
     * Generates a complicated poly.
     * 
     * @param rawllpts LatLonPofloat[]
     * @param ltype line type
     * @param nsegs number of segments to draw for greatcircle or rhumb lines
     *        (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList
     */
    protected ArrayList<float[]> doPolyDispatch(float[] rawllpts, int ltype, int nsegs, boolean isFilled) {
        switch (ltype) {
            case LineType.Rhumb:
                return forwardRhumbPoly(rawllpts, nsegs, isFilled);
            case LineType.GreatCircle:
                return forwardGreatPoly(rawllpts, nsegs, isFilled);
            case LineType.Straight:
                Debug.error("Proj.doPolyDispatch: Bad Dispatch!\n");
                return new ArrayList<float[]>(0);
            default:
                Debug.error("Proj.doPolyDispatch: Invalid LType!\n");
                return new ArrayList<float[]>(0);
        }
    }

    /**
     * Generates a complicated poly.
     * 
     * @param rawllpts LatLonPofloat[]
     * @param ltype line type
     * @param nsegs number of segments to draw for greatcircle or rhumb lines
     *        (if &lt; 1, this value is generated internally).
     * @param isFilled filled poly?
     * @return ArrayList<int[]>
     */
    protected ArrayList<float[]> doPolyDispatch(double[] rawllpts, int ltype, int nsegs, boolean isFilled) {
        switch (ltype) {
            case LineType.Rhumb:
                return forwardRhumbPoly(rawllpts, nsegs, isFilled);
            case LineType.GreatCircle:
                return forwardGreatPoly(rawllpts, nsegs, isFilled);
            case LineType.Straight:
                Debug.error("Proj.doPolyDispatch: Bad Dispatch!\n");
                return new ArrayList<float[]>(0);
            default:
                Debug.error("Proj.doPolyDispatch: Invalid LType!\n");
                return new ArrayList<float[]>(0);
        }
    }

    // /**
    // * Given a couple of points representing a bounding box, find out what the
    // * scale should be in order to make those points appear at the corners of
    // * the projection.
    // *
    // * @param ll1 the upper left coordinates of the bounding box.
    // * @param ll2 the lower right coordinates of the bounding box.
    // * @param point1 a java.awt.Point reflecting a pixel spot on the
    // projection
    // * that matches the ll1 coordinate, the upper left corner of the area
    // * of interest.
    // * @param point2 a java.awt.Point reflecting a pixel spot on the
    // projection
    // * that matches the ll2 coordinate, usually the lower right corner of
    // * the area of interest.
    // */
    // public float getScale(Point2D ll1, Point2D ll2, Point2D point1,
    // Point2D point2) {
    // if (ll1 instanceof LatLonPoint && ll2 instanceof LatLonPoint) {
    // return getScale(LatLonPoint.getDouble(ll1),
    // LatLonPoint.getDouble(ll2),
    // point1,
    // point2);
    // }
    //
    // return getScale();
    // }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.Point reflecting a pixel spot on the projection
     *        that matches the ll1 coordinate, the upper left corner of the area
     *        of interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the projection
     *        that matches the ll2 coordinate, usually the lower right corner of
     *        the area of interest.
     */
    public float getScale(Point2D ll1, Point2D ll2, Point2D point1, Point2D point2) {

        try {

            double deltaDegrees;
            double pixPerDegree;
            double deltaPix;
            double dx = Math.abs(point2.getX() - point1.getX());
            double dy = Math.abs(point2.getY() - point1.getY());

            // TODO: mercator getScale is wrong for screens in portrait mode,
            // that is dx<dy. why does this handle portrait different than
            // landscape?
            /*
             * if (dx < dy) { double dlat = Math.abs(ll1.getY() - ll2.getY());
             * deltaDegrees = dlat; deltaPix = dy;
             * 
             * // This might not be correct for all projection types
             * pixPerDegree = getPlanetPixelCircumference() / 360.0; } else {
             */
            double dlon;
            double lat1, lon1, lon2;

            // point1 is to the right of point2. switch the
            // LatLonPoints so that ll1 is west (left) of ll2.
            if (point1.getX() > point2.getX()) {
                lat1 = ll1.getY();
                lon1 = ll1.getX();
                ll1.setLocation(ll2);
                ll2.setLocation(lon1, lat1);
            }

            lon1 = ll1.getX();
            lon2 = ll2.getX();

            // allow for crossing dateline
            if (lon1 > lon2) {
                dlon = (180 - lon1) + (180 + lon2);
            } else {
                dlon = lon2 - lon1;
            }

            deltaDegrees = dlon;
            deltaPix = dx;

            // This might not be correct for all projection types
            pixPerDegree = getPlanetPixelCircumference() / 360.0;
            // }

            // The new scale, need it to match the current projection width.
            return (float) (pixPerDegree / (getWidth() / deltaDegrees));
        } catch (NullPointerException npe) {
            com.bbn.openmap.util.Debug.error("ProjMath.getScale(): caught null pointer exception.");
            return Float.MAX_VALUE;
        }
    }

    /**
     * Forward project a point.
     */
    public Point2D forward(Point2D llp, Point2D pt) {
        return forward(llp.getY(), llp.getX(), pt, false);
    }

    /**
     * Forward project a LatLonPoint.
     * <p>
     * Forward projects a LatLon point into XY space. Returns a Point.
     * 
     * @param llp LatLonPoint to be projected
     * @return Point (new)
     */
    public Point2D forward(Point2D llp) {
        return forward(llp.getY(), llp.getX(), new Point2D.Double(), false);
    }

    /**
     * Project the point into view space.
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitue in decimal degrees.
     */
    public Point2D forward(double lat, double lon, Point2D pt) {
        return forward(lat, lon, pt, false);
    }

    /**
     * Project the point into view space.
     * 
     * @param lat latitude
     * @param lon longitude
     * @param pt return point
     * @param isRadian true if lat/lon are radians instead of decimal degrees
     * @return Point2D for projected point
     */
    public Point2D forward(float lat, float lon, Point2D pt, boolean isRadian) {
        return forward((double) lat, (double) lon, pt, isRadian);
    }

    /**
     * Project the point into view space.
     * 
     * @param lat latitude
     * @param lon longitude
     * @param pt return point
     * @param isRadian true if lat/lon are radians instead of decimal degrees
     * @return Point2D for projected point
     */
    abstract public Point2D forward(double lat, double lon, Point2D pt, boolean isRadian);

    /**
     * Inverse project a Point from x,y space to LatLon space.
     * 
     * @param point x,y Point
     * @return LatLonPoint (new)
     */
    public LatLonPoint inverse(Point2D point) {
        return inverse(point.getX(), point.getY(), new LatLonPoint.Double());
    }

    /**
     * Inverse project x,y coordinates.
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @return LatLonPoint (new)
     * @see #inverse(Point2D)
     */
    public LatLonPoint inverse(double x, double y) {
        return inverse(x, y, new LatLonPoint.Double());
    }

    /**
     * Returns the Point2D provided if it is a LatLonPoint, otherwise it creates
     * a LatLonPoint.Double and transfers the values from the provided Point2D
     * object.
     */
    protected LatLonPoint assertLatLonPoint(Point2D p2d) {
        if (p2d instanceof LatLonPoint) {
            return (LatLonPoint) p2d;
        } else {
            return new LatLonPoint.Double(p2d.getY(), p2d.getX());
        }
    }

    /**
     * Forward project a LatLon Line.
     * <p>
     * Returns a ArrayList of (x[], y[]) coordinate pair(s) of the projected
     * line(s).
     * 
     * <a name="line_restrictions">
     * <h4>RESTRICTIONS:</h4>
     * </a> A line segment must be less than 180 degrees of arc (half the
     * circumference of the world). If you need to draw a longer line, then draw
     * several several individual segments of less than 180 degrees, or draw a
     * single polyline of those segments.
     * <p>
     * We make this restriction because from any point on a sphere, you can
     * reach any other point with a maximum traversal of 180degrees of arc.
     * <p>
     * Furthermore, for the Cylindrical family of projections, a line must be
     * &lt; 180 degrees of arc in longitudinal extent. In other words, the
     * difference of longitudes between both vertices must be &lt; 180 degrees.
     * Same as above: if you need a long line, you must break it into several
     * segments.
     * 
     * @param ll1 LatLonPoint
     * @param ll2 LatLonPoint
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @return ArrayList<int[]>
     * @see LineType#Straight
     * @see LineType#Rhumb
     * @see LineType#GreatCircle
     * 
     */
    public ArrayList<float[]> forwardLine(LatLonPoint ll1, LatLonPoint ll2, int ltype, int nsegs) {
        double[] rawllpts = {
            ll1.getRadLat(),
            ll1.getRadLon(),
            ll2.getRadLat(),
            ll2.getRadLon()
        };
        return forwardPoly(rawllpts, ltype, nsegs, false);
    }

    /**
     * Forward project a lat/lon Line.
     * 
     * @see #forwardLine(LatLonPoint, LatLonPoint, int, int)
     */
    public ArrayList<float[]> forwardLine(LatLonPoint ll1, LatLonPoint ll2, int ltype) {
        return forwardLine(ll1, ll2, ltype, -1);
    }

    /**
     * Forward project a rectangle defined by an upper left point and a lower
     * right point.
     * <p>
     * Returns a ArrayList of (x[], y[]) coordinate pairs of the projected
     * points.
     * <p>
     * Rects have the same restrictions as <a href="#poly_restrictions"> polys
     * <a>and <a href="#line_restrictions">lines </a>.
     * 
     * @param ll1 LatLonPoint of northwest corner
     * @param ll2 LatLonPoint of southeast corner
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @see #forwardPoly
     * @return ArrayList<int[]>
     */
    public ArrayList<float[]> forwardRect(LatLonPoint ll1, LatLonPoint ll2, int ltype, int nsegs, boolean isFilled) {
        double[] rawllpts = {
            ll1.getRadLat(),
            ll1.getRadLon(),
            ll1.getRadLat(),
            ll2.getRadLon(),
            ll2.getRadLat(),
            ll2.getRadLon(),
            ll2.getRadLat(),
            ll1.getRadLon(),
            // connect:
            ll1.getRadLat(),
            ll1.getRadLon()
        };
        return forwardPoly(rawllpts, ltype, nsegs, isFilled);
    }

    /**
     * Forward project a lat/lon Rectangle.
     * 
     * @see #forwardRect(LatLonPoint, LatLonPoint, int, int)
     */
    public ArrayList<float[]> forwardRect(LatLonPoint ll1, LatLonPoint ll2, int ltype) {
        return forwardRect(ll1, ll2, ltype, -1, false);
    }

    /**
     * Forward project a lat/lon Rectangle. *
     * 
     * @param ll1 LatLonPoint of northwest corner
     * @param ll2 LatLonPoint of southeast corner
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @see #forwardRect(LatLonPoint, LatLonPoint, int, int)
     */
    public ArrayList<float[]> forwardRect(LatLonPoint ll1, LatLonPoint ll2, int ltype, int nsegs) {
        return forwardRect(ll1, ll2, ltype, nsegs, false);
    }

    /**
     * Forward project an arc.
     * 
     * @param c LatLonPoint center
     * @param radians boolean radius in radians?
     * @param radius radius in radians or decimal degrees
     * @param start the starting angle of the arc, zero being North up. Units
     *        are dependent on radians parameter - the start parameter is in
     *        radians if radians equals true, decimal degrees if not.
     * @param extent the angular extent angle of the arc, zero being no length.
     *        Units are dependent on radians parameter - the extent parameter is
     *        in radians if radians equals true, decimal degrees if not.
     */
    public ArrayList<float[]> forwardArc(LatLonPoint c, boolean radians, double radius, double start, double extent) {
        return forwardArc(c, radians, radius, -1, start, extent, java.awt.geom.Arc2D.OPEN);
    }

    public ArrayList<float[]> forwardArc(LatLonPoint c, boolean radians, double radius, int nverts, double start, double extent) {
        return forwardArc(c, radians, radius, nverts, start, extent, java.awt.geom.Arc2D.OPEN);
    }

    /**
     * Forward project a Lat/Lon Arc.
     * <p>
     * Arcs have the same restrictions as <a href="#poly_restrictions"> polys
     * </a>.
     * 
     * @param c LatLonPoint center of circle
     * @param radians radius in radians or decimal degrees?
     * @param radius radius of circle (0 &lt; radius &lt; 180)
     * @param nverts number of vertices of the circle poly.
     * @param start the starting angle of the arc, zero being North up. Units
     *        are dependent on radians parameter - the start parameter is in
     *        radians if radians equals true, decimal degrees if not.
     * @param extent the angular extent angle of the arc, zero being no length.
     *        Units are dependent on radians parameter - the extent parameter is
     *        in radians if radians equals true, decimal degrees if not.
     * @param arcType type of arc to create - see java.awt.geom.Arc2D for (OPEN,
     *        CHORD, PIE). Arc2D.OPEN means that the just the points for the
     *        curved edge will be provided. Arc2D.PIE means that addition lines
     *        from the edge of the curve to the center point will be added.
     *        Arc2D.CHORD means a single line from each end of the curve will be
     *        drawn.
     */
    public ArrayList<float[]> forwardArc(LatLonPoint c, boolean radians, double radius, int nverts, double start, double extent,
                                         int arcType) {
        // HACK-need better decision for number of vertices.
        if (nverts < 3)
            nverts = NUM_DEFAULT_CIRCLE_VERTS;

        double[] rawllpts;

        switch (arcType) {
            case Arc2D.PIE:
                rawllpts = new double[(nverts << 1) + 4];// *2 for pairs +4
                // connect
                break;
            case Arc2D.CHORD:
                rawllpts = new double[(nverts << 1) + 2];// *2 for pairs +2
                // connect
                break;
            default:
                rawllpts = new double[(nverts << 1)];// *2 for pairs, no
                // connect
        }

        GreatCircle.earthCircle(c.getRadLat(), c.getRadLon(), (radians) ? radius : ProjMath.degToRad(radius), (radians) ? start
                : ProjMath.degToRad(start), (radians) ? extent : ProjMath.degToRad(extent), nverts, rawllpts);

        int linetype = LineType.Straight;
        boolean isFilled = false;

        switch (arcType) {
            case Arc2D.PIE:
                rawllpts[rawllpts.length - 4] = c.getRadLat();
                rawllpts[rawllpts.length - 3] = c.getRadLon();
                // Fall through...
            case Arc2D.CHORD:
                rawllpts[rawllpts.length - 2] = rawllpts[0];
                rawllpts[rawllpts.length - 1] = rawllpts[1];
                // Need to do this for the sides, not the arc part.
                linetype = LineType.GreatCircle;
                isFilled = true;
                break;
            default:
                // Don't need to do anything, defaults are already set.
        }

        // forward project the arc-poly.
        return forwardPoly(rawllpts, linetype, -1, isFilled);
    }

    /**
     * Forward project a circle.
     * 
     * @param c LatLonPoint center
     * @param radians boolean radius in radians?
     * @param radius radius in radians or decimal degrees
     */
    public ArrayList<float[]> forwardCircle(LatLonPoint c, boolean radians, double radius) {
        return forwardCircle(c, radians, radius, -1, false);
    }

    /**
     * Forward project a Lat/Lon Circle.
     * <p>
     * Circles have the same restrictions as <a href="#poly_restrictions">
     * polys. </a>.
     * 
     * @param c LatLonPoint center of circle
     * @param radians radius in radians or decimal degrees?
     * @param radius radius of circle (0 &lt; radius &lt; 180)
     * @param nverts number of vertices of the circle poly.
     */
    public ArrayList<float[]> forwardCircle(LatLonPoint c, boolean radians, double radius, int nverts) {
        return forwardCircle(c, radians, radius, nverts, false);
    }

    /**
     * Forward project a Lat/Lon Circle.
     * <p>
     * Circles have the same restrictions as <a href="#poly_restrictions">
     * polys. </a>.
     * 
     * @param c LatLonPoint center of circle
     * @param radians radius in radians or decimal degrees?
     * @param radius radius of circle (0 &lt; radius &lt; 180)
     * @param nverts number of vertices of the circle poly.
     * @param isFilled filled poly?
     */
    public ArrayList<float[]> forwardCircle(LatLonPoint c, boolean radians, double radius, int nverts, boolean isFilled) {
        // HACK-need better decision for number of vertices.
        if (nverts < 3)
            nverts = NUM_DEFAULT_CIRCLE_VERTS;

        double[] rawllpts = new double[(nverts << 1) + 2];// *2 for
        // pairs +2
        // connect
        GreatCircle.earthCircle(c.getRadLat(), c.getRadLon(), (radians) ? radius : ProjMath.degToRad(radius), nverts, rawllpts);
        // connect the vertices.
        rawllpts[rawllpts.length - 2] = rawllpts[0];
        rawllpts[rawllpts.length - 1] = rawllpts[1];

        // forward project the circle-poly
        return forwardPoly(rawllpts, LineType.Straight, -1, isFilled);
    }

    // HACK
    protected transient static int XTHRESHOLD = 16384;// half range
    protected transient int XSCALE_THRESHOLD = 1000000;// dynamically

    /**
     * Forward project a LatLon Poly.
     * <p>
     * Returns a ArrayList of (x[], y[]) coordinate pair(s) of the projected
     * poly. <a name="poly_restrictions">
     * <h4>RESTRICTIONS:</h4>
     * <br>
     * </a> All the following restrictions apply to LatLon polygons (either
     * filled or non-filled). Many of these restrictions apply to other
     * poly-like ArrayList graphics (Lines, Rectangles, Circles, Ellipses, ...).
     * See also <a href="#line_restrictions">restrictions on LatLon lines. </a>
     * <p>
     * <a name="antarctica_anomaly"> </a> For the cylindrical projections, (e.g.
     * Mercator), your polygons should not include or touch the poles. This is
     * because a polygon or polyline that includes a pole becomes a
     * non-continuous straight line on the map. "So what about Antarctica", you
     * say, "after all it's a polygon that is draped over the South Pole". Well,
     * if you want to see it in a cylindrical projection, you will need to
     * "augment" the vertices to turn it into a valid x-y polygon. You could do
     * this by removing the segment which crosses the dateline, and instead add
     * two extra edges down along both sides of the dateline to very near the
     * south pole and then connect these ends back the other way around the
     * world (not across the dateline) with a few extra line segments (remember
     * the <a href="#line_restrictions">line length restrictions </a>). This way
     * you've removed the polar anomaly from the data set. On the screen, all
     * you see is a sliver artifact down along the dateline. This is the very
     * method that our DCW data server shows Antarctica.
     * <p>
     * There is a fundamental ambiguity with filled polygons on a sphere: which
     * side do you draw the fill-color? The Cylindrical family will draw the
     * polygon as if it were in x-y space. For the Azimuthal projections, (e.g.
     * Orthographic), you can have polygons that cover the pole, but it's
     * important to specify the vertices in a clockwise order so that we can do
     * the correct clipping along the hemisphere edge. We traverse the vertices
     * assuming that the fill will be to the right hand side if the polygon
     * straddles the edge of the projection. (This default can be changed).
     * <p>
     * <h3>To Be (Mostly) Safe:</h3>
     * <ul>
     * <li>Polygons should not touch or encompass the poles unless you will be
     * viewing them with azimuthal projections, such as Orthographic. <br>
     * <li>Polygons should not encompass more area than one hemisphere. <br>
     * <li>Polygon vertices should be specified in "clockwise", fill-on-right
     * order to ensure proper filling. <br>
     * <li>Polygon edges are also restricted by the <a
     * href="#line_restrictions">restrictions on LatLon lines </a>.
     * </ul>
     * <p>
     * <h3>Optimization Notes:</h3>
     * The projection library deals internally in radians, and so you're
     * required to pass in an array of radian points. See <a
     * href="com.bbn.openmap.proj.ProjMath.html#arrayDegToRad">
     * ProjMath.arrayDegToRad(float[]) </a> for an efficient in-place
     * conversion.
     * <p>
     * For no-frills, no-assumptions, fast and efficient projecting, see <a
     * href="#forwardRaw">forwardRaw() </a>.
     * 
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @param isFilled poly is filled? or not
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     * @see #forwardRaw
     * @see LineType#Straight
     * @see LineType#Rhumb
     * @see LineType#GreatCircle
     */
    public ArrayList<float[]> forwardPoly(float[] rawllpts, int ltype, int nsegs, boolean isFilled) {
        ArrayList<float[]> stuff = _forwardPoly(rawllpts, ltype, nsegs, isFilled);
        // @HACK: workaround XWindows bug. simple clip to a boundary.
        // this is ugly.
        if (Environment.doingXWindowsWorkaround && (scale <= XSCALE_THRESHOLD)) {
            int i, j, size = stuff.size();
            float[] xpts, ypts;
            for (i = 0; i < size; i += 2) {
                xpts = (float[]) stuff.get(i);
                ypts = (float[]) stuff.get(i + 1);
                for (j = 0; j < xpts.length; j++) {
                    if (xpts[j] <= -XTHRESHOLD) {
                        xpts[j] = -XTHRESHOLD;
                    } else if (xpts[j] >= XTHRESHOLD) {
                        xpts[j] = XTHRESHOLD;
                    }
                    if (ypts[j] <= -XTHRESHOLD) {
                        ypts[j] = -XTHRESHOLD;
                    } else if (ypts[j] >= XTHRESHOLD) {
                        ypts[j] = XTHRESHOLD;
                    }
                }
                stuff.set(i, xpts);
                stuff.set(i + 1, ypts);
            }
        }
        return stuff;
    }

    /**
     * Forward project a lat/lon Poly.
     * <p>
     * Delegates to _forwardPoly(), and may do additional clipping for Java
     * XWindows problem. Remember to specify vertices in radians!
     * 
     * @param rawllpts double[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     * @see #forwardRaw
     * @see LineType#Straight
     * @see LineType#Rhumb
     * @see LineType#GreatCircle
     */
    public ArrayList<float[]> forwardPoly(double[] rawllpts, int ltype, int nsegs, boolean isFilled) {
        ArrayList<float[]> stuff = _forwardPoly(rawllpts, ltype, nsegs, isFilled);
        // @HACK: workaround XWindows bug. simple clip to a boundary.
        // this is ugly.
        if (Environment.doingXWindowsWorkaround && (scale <= XSCALE_THRESHOLD)) {
            int i, j, size = stuff.size();
            float[] xpts, ypts;
            for (i = 0; i < size; i += 2) {
                xpts = stuff.get(i);
                ypts = stuff.get(i + 1);
                for (j = 0; j < xpts.length; j++) {
                    if (xpts[j] <= -XTHRESHOLD) {
                        xpts[j] = -XTHRESHOLD;
                    } else if (xpts[j] >= XTHRESHOLD) {
                        xpts[j] = XTHRESHOLD;
                    }
                    if (ypts[j] <= -XTHRESHOLD) {
                        ypts[j] = -XTHRESHOLD;
                    } else if (ypts[j] >= XTHRESHOLD) {
                        ypts[j] = XTHRESHOLD;
                    }
                }
                stuff.set(i, xpts);
                stuff.set(i + 1, ypts);
            }
        }
        return stuff;
    }

    /**
     * Forward project a lat/lon Poly defined as decimal degree lat/lons.
     * <p>
     * Delegates to _forwardPoly(), and may do additional clipping for Java
     * XWindows problem. Remember to specify vertices in decimal degrees. If you
     * have radians, use them and call forwardPoly, it's faster. This method
     * will convert the coords to radians before calling the fowardPoly method.
     * 
     * @param llpts double[] of lat,lon,lat,lon,... in decimal degree lat/lon!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     * @see #forwardRaw
     * @see LineType#Straight
     * @see LineType#Rhumb
     * @see LineType#GreatCircle
     */
    public ArrayList<float[]> forwardLLPoly(double[] llpts, int ltype, int nsegs, boolean isFilled) {
        double[] rawllpts = new double[llpts.length];
        System.arraycopy(llpts, 0, rawllpts, 0, llpts.length);
        ProjMath.arrayDegToRad(rawllpts);
        return forwardPoly(rawllpts, ltype, nsegs, isFilled);
    }

    /**
     * Forward project a lat/lon Poly. Remember to specify vertices in radians!
     * 
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     */
    protected abstract ArrayList<float[]> _forwardPoly(float[] rawllpts, int ltype, int nsegs, boolean isFilled);

    /**
     * Forward project a lat/lon Poly. Remember to specify vertices in radians!
     * 
     * @param rawllpts double[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?
     * @return ArrayList of x[], y[], x[], y[], ... projected poly
     */
    protected abstract ArrayList<float[]> _forwardPoly(double[] rawllpts, int ltype, int nsegs, boolean isFilled);

    /**
     * Get the unprojected coordinates units of measure.
     * 
     * @return Length.DECIMAL_DEGREE
     */
    public Length getUcuom() {
        return Length.DECIMAL_DEGREE;
    }

    /**
     * Can't set the unprojected coordinates units of measure for a GeoProj,
     * it's always Length.DECIMAL_DEGREE.
     * 
     * @param ucuom
     */
    public void setUcuom(Length ucuom) {
        // no-op
    }

    /**
     * Convenience method to create a GCT for this projection. For projections
     * that start with lat/lon coordinates, this will return a LatLonGCT. For
     * projections that have world coordinates in meters, the GCT will provide a
     * way to get to those meter coordinates. For instance, a UTMProjection will
     * return a UTMGCT.
     * 
     * @return GeoCoordTransformation for this projection
     */
    @SuppressWarnings("unchecked")
    public <T extends GeoCoordTransformation> T getGCTForProjection() {
        return (T) new LatLonGCT();
    }

    /**
     * @return the reference longitude of the projection. For most projections,
     *         it'll just be the center point longitude. For LLC, it'll be the
     *         reference meridian.
     */
    public double getReferenceLon() {
        return getCenter().getX();
    }

}
