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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/terrain/LOSGenerator.java,v $
// $RCSfile: LOSGenerator.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.terrain;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Planet;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * A Class that can do Line-Of-Sight calculations between two points. Uses the
 * DTEDFrameCache to get elevations.
 */
public class LOSGenerator {

    // These are used to control the algorithm type. Right now, the
    // first two are eliminated, since the azimuth algorithm is
    // faster
    // and more precise.
    final static int PRECISE = 0;
    final static int GOODENOUGH = 1;
    final static int AZIMUTH = 2;

    // The colors of pixels
    final static int DEFAULT_INVISIBLE = new Color(0, 0, 0, 0).getRGB();
    final static int DEFAULT_VISIBLE = new Color(0, 255, 0, 255).getRGB();
    final static int DEFAULT_MAYBEVISIBLE = new Color(255, 255, 0, 255).getRGB();

    protected int INVISIBLE = DEFAULT_INVISIBLE;
    protected int VISIBLE = DEFAULT_VISIBLE;
    protected int MAYBEVISIBLE = DEFAULT_MAYBEVISIBLE;

    DTEDFrameCache dtedCache = null;

    public static Logger logger = Logger.getLogger("com.bbn.openmap.tools.terrain.LOSGenerator");

    /**
     * Not the preferred way to create one of these. It's full of defaults.
     */
    public LOSGenerator() {
    }

    public LOSGenerator(DTEDFrameCache cache) {
        setDtedCache(cache);
    }

    public void setDtedCache(DTEDFrameCache cache) {
        dtedCache = cache;
    }

    public DTEDFrameCache getDtedCache() {
        return dtedCache;
    }

    /**
     * Check to see if two points are within line of sight of each other, taking
     * into account their elevations above Mean Sea Level as retrieved by a DTED
     * database, and any other addition height of each object.
     * 
     * @param startLLP location of point 1.
     * @param startObjHeight the elevation of point 1 above the surface, in
     *        meters. The surface elevation of the point will be looked up and
     *        added to this value.
     * @param addStartElevation true if startObjHeight also needs DTED lookup
     *        added to it.
     * @param endLLP location of point 2.
     * @param endObjHeight the elevation of point 2 above the surface, in
     *        meters. The surface elevation of the point will be looked up and
     *        added to this value.
     * @param numPoints number of sample points to check between the two end
     *        points. Can be dependent on the Projection of the current map, and
     *        based on the number of pixels between the projected points. Could
     *        also be based on the number of elevation posts between the two
     *        points in the DTED database.
     * @return true of their is a line-of-sight path between the two points.
     */
    public boolean isLOS(LatLonPoint startLLP, int startObjHeight, boolean addStartElevation,
                         LatLonPoint endLLP, int endObjHeight, int numPoints) {

        boolean ret = false;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("LOSGenerator.isLOS: " + startLLP + " at height:" + startObjHeight + ", "
                    + endLLP + " at height:" + endObjHeight + ", numPoints = " + numPoints);
        }

        if (dtedCache == null) {
            return ret;
        }

        int startTotalHeight = startObjHeight
                + (addStartElevation ? dtedCache.getElevation((float) startLLP.getLatitude(), (float) startLLP.getLongitude())
                        : 0);

        double[] llpoints = GreatCircle.greatCircle(startLLP.getRadLat(), startLLP.getRadLon(), endLLP.getRadLat(), endLLP.getRadLon(), numPoints, true);
        LatLonPoint llp = new LatLonPoint.Double();
        int gcPointListSize = llpoints.length;
        double smallestSlopeValue = -Math.PI;
        // Start at a couple of points away from origin
        for (int i = 4; i < gcPointListSize; i += 2) {
            llp.setLatLon(llpoints[i], llpoints[i + 1], true);
            int heightAboveGround = 0;
            // Only add height to end point
            if (i >= gcPointListSize - 2) {
                heightAboveGround = endObjHeight;
            }
            double slopeOfCurrentPoint = calculateLOSSlope(startLLP, startTotalHeight, llp, heightAboveGround);

            // if the slope is smaller than the max slope yet seen, it is
            // visible.
            if (slopeOfCurrentPoint > smallestSlopeValue) {
                smallestSlopeValue = slopeOfCurrentPoint;
                ret = true;

                // If the last point has the largest slope, then the
                // point is LOS.
            } else {
                ret = false;
            }

            if (logger.isLoggable(Level.FINER)) {
                logger.finer("   LOS:" + (i / 2) + " - slope = "
                        + Length.DECIMAL_DEGREE.fromRadians(slopeOfCurrentPoint)
                        + " at height of point: " + heightAboveGround + (ret ? " *" : " -"));
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("LOSGenerator - points " + (ret ? "" : " NOT ") + " in LOS");
        }

        return ret;
    }

    /**
     * CalculateLOSslope figures out the slope from one point to another. The
     * arc_dist is in radians, and is the radian arc distance of the point from
     * the center point of the image, on the earth. This slope calculation does
     * take the earth's curvature into account, based on the spherical model.
     * The slope returned is the angle from the end point to the beginning
     * point, relative to the vertical of the end point to the center of the
     * earth - i.e. starting at the axis pointing straight down into the earth,
     * how many radians do you have to angle up until you hit the starting
     * point. The DTED elevation of the end point is gathered from the dted
     * cache.
     * 
     * @param startLLP the coordinates of point 1.
     * @param startTotalHeight the total height of point 1, from the Mean Sea
     *        Level - so it's the elevation of the point plus altitude above the
     *        surface, in meters.
     * @param endLLP the coordinates of point 2.
     * @param endObjHeight the elevation of point 2 above the surface, in
     *        meters. The surface elevation of the point will be looked up and
     *        added to this value.
     * @return slope of line between the two points, with zero pointing straight
     *         down, in radians.
     */
    public double calculateLOSSlope(LatLonPoint startLLP, int startTotalHeight, LatLonPoint endLLP,
                                    int endObjHeight) {

        if (dtedCache == null) {
            return 0;
        }

        double arc_dist = startLLP.distance(endLLP);

        int endTotalHeight = endObjHeight
                + dtedCache.getElevation(endLLP.getLatitude(), endLLP.getLongitude());
        return calculateLOSSlope(startTotalHeight, endTotalHeight, arc_dist);
    }

    /**
     * Calculate the slope of a line between two points across a spherical model
     * of the earth. A slope of zero is pointing to the sky from the starting
     * point. 90 degrees is perpendicular from that start point vector, which
     * would be the slope of two points next to each other of the same height.
     * Any angle greater than than and you're rolling downhill.
     * 
     * @param startTotalHeight total height of one point, in meters. Should
     *        represent elevation of point which is the surface elevation above
     *        MSL, and the height above the surface.
     * @param endTotalHeight total height of one the other point, in meters.
     *        Should represent elevation of point which is the surface elevation
     *        above MSL, and the height above the surface.
     * @param arc_dist the surface angle, in radians, across the spherical model
     *        of the earth that separates the two points.
     */
    public static double calculateLOSSlope(int startTotalHeight, int endTotalHeight, double arc_dist) {
        double ret = 0;
        double P = Math.sin(arc_dist) * (endTotalHeight + Planet.wgs84_earthEquatorialRadiusMeters);

        double xPrime = Math.cos(arc_dist)
                * (endTotalHeight + Planet.wgs84_earthEquatorialRadiusMeters);

        double cutoff = startTotalHeight + Planet.wgs84_earthEquatorialRadiusMeters;

        // Suggested changes, submitted by Mark Wigmore. Introduces
        // use of doubles, and avoidance of PI/2 tan() calculations.

        double bottom = cutoff - xPrime;
        ret = MoreMath.HALF_PI_D - Math.atan(bottom / P);
        return ret;
    }

    // public static void main(String[] args) {
    //
    // float dist = Length.FEET.toRadians(1000);
    //
    // System.out.println("slope = "
    // + Length.DECIMAL_DEGREE.fromRadians(LOSGenerator.calculateLOSSlope(10,
    // 10, dist)));
    // System.out.println("slope = "
    // + Length.DECIMAL_DEGREE.fromRadians(LOSGenerator.calculateLOSSlope(5, 10,
    // dist)));
    // System.out.println("slope = "
    // + Length.DECIMAL_DEGREE.fromRadians(LOSGenerator.calculateLOSSlope(15,
    // 10, dist)));
    //
    // }
}
