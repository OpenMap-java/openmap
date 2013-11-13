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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/CADRG.java,v $
// $RCSfile: CADRG.java,v $
// $Revision: 1.12 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Implements the CADRG projection. This is really an Equal Arc Projection with
 * pixel spacings as dictated by the RPF specification.
 */
public class CADRG extends Cylindrical implements EqualArc {

    private static final long serialVersionUID = 1L;

    /**
     * The CADRG name.
     */
    public final static transient String CADRGName = "CADRG";

    public final static transient double epsilon = 0.0001;

    // HACK -degrees
    private static final double NORTH_LIMIT = ProjMath.degToRad(80.0f);
    private static final double SOUTH_LIMIT = -NORTH_LIMIT;

    private double spps_x, spps_y; // scaled pixels per SCoord
    private static final int CADRG_ARC_A[] = { 369664, 302592, 245760, 199168, 163328, 137216,
            110080, 82432 };
    private static final double CADRG_SCALE_LIMIT = 2000.0;
    private static final int CADRG_get_zone_old_extents[] = { 32, 48, 56, 64, 68, 72, 76, 80, 90 };
    private int /* ox, */oy;
    private double x_pix_constant, y_pix_constant;
    private Point ul;// upper left

    private double[] lower_zone_extents;
    private double[] upper_zone_extents;

    private int zone;

    /**
     * Construct a CADRG projection.
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public CADRG(LatLonPoint center, float scale, int width, int height) {
        super(center, scale, width, height);
        minscale = 1000000 / CADRG_SCALE_LIMIT;
    }

    /**
     * Sets radian latitude to something sane. This is an abstract function
     * since some projections don't deal well with extreme latitudes.
     * 
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * 
     */
    public double normalizeLatitude(double lat) {
        if (lat > NORTH_LIMIT) {
            lat = NORTH_LIMIT;
        } else if (lat < SOUTH_LIMIT) {
            lat = SOUTH_LIMIT;
        }
        return lat;
    }

    // protected void finalize() {
    // Debug.message("proj", "CADRG finialized");
    // }

    /**
     * Return stringified description of this projection.
     * <p>
     * 
     * @return String
     * @see Projection#getProjectionID
     * 
     */
    public String toString() {
        return "CADRG[ spps_x=" + spps_x + " spps_y=" + spps_y + " x_pix=" + x_pix_constant
                + " y_pix=" + y_pix_constant +
                /* " ox=" + ox + */" oy=" + oy + " ul(" + ul.x + "," + ul.y + ")"
                + super.toString();
    }

    /**
     * Returns the current zone of the projection. Zone number starts at 1, goes
     * to 8, per the RPF specification. We don't handle zone 9 (polar).
     * 
     * @return the zone of the projection.
     */
    public int getZone() {
        return zone;
    }

    /**
     * Given a letter for a zone, return the CADRG zone equivalent,
     */
    public static int getProjZone(char asciiZone) {
        int z = (int) asciiZone;

        if (z == 74)
            z--; // Fix J to a zone.
        if (z > 64)
            z -= 64; // Below the equator
        else
            z -= 48; // Above the equator

        // Now we should have a number, of a zone 1-9
        return z;
    }

    /**
     * Get the planet pixel circumference.
     * 
     * @return double circumference of planet in pixels
     */
    public double getPlanetPixelCircumference() {
        // Why this algorithm? Well, the CADRG_ARC_A is a pixel count
        // that needs to be multiplied by 1000000 to normalize it
        // against the 1:1M factor reflected in the array values. The
        // 1.5 factor was tossed in there because it was showing up in
        // other calculations as that 100/150 thing. It works in
        // tests.

        return (1000000 * (double) CADRG_ARC_A[zone - 1]) / 1.5;
        // These are the same things...
        // return (float)getXPixConstant() * scale;

        // This is what the default return value is from the super
        // class.
        // return planetPixelCircumference; // the standard return for
        // projections...
    }

    /**
     * Returns the zone based on the y_pix_constant and a latitude.
     * <p>
     * HACK: latitude in decimal degrees DO THE CONSTANTS DEPEND ON THIS?!!
     * <p>
     * 
     * @param lat latitude
     * @param y_pix_constant pixel constant
     * 
     */
    protected int getZone(double lat, double y_pix_constant) {
        int NOT_SET = -1;
        int ret = NOT_SET;

        for (int x = 0; x < CADRG_get_zone_old_extents.length - 1/* 8 */; x++) {
            double testLat = Math.abs(lat);
            if (testLat <= CADRG_get_zone_old_extents[x]) {
                ret = x + 1;
                break;
            }
        }

        if (ret == NOT_SET)
            ret = CADRG_get_zone_old_extents.length - 1;

        return ret;
    }

    /**
     * Returns the x pixel constant of the projection. This was calculated when
     * the projection was created. Represents the number of pixels around the
     * earth (360 degrees).
     */
    public double getXPixConstant() {
        return x_pix_constant;
    }

    /**
     * Returns the y pixel constant of the projection. This was calculated when
     * the projection was created. Represents the number of pixels from 0 to 90
     * degrees.
     */
    public double getYPixConstant() {
        return y_pix_constant;
    }

    /**
     * Returns the upper zone extent for the given zone at the current scale.
     * This only makes sense if the projection is at the same scale as the chart
     * data you are interested in.
     */
    public double getUpperZoneExtent(int zone) {
        if (zone < 1)
            zone = 1;
        if (zone > 8)
            zone = 9;
        return upper_zone_extents[zone - 1];
    }

    /**
     * Returns the lower zone extent for the given zone at the current scale.
     * This only makes sense if the projection is at the same scale as the chart
     * data you are interested in.
     */
    public double getLowerZoneExtent(int zone) {
        if (zone < 1)
            zone = 1;
        if (zone > 8)
            zone = 9;
        return lower_zone_extents[zone - 1];
    }

    /**
     * Return the number of horizontal frame files that will fit around the
     * world in the current zone. This only makes sense if the projection is at
     * the same scale as the chart data you are interested in.
     * 
     * @return number of frame columns in the current zone, to go around the
     *         world.
     */
    public int numHorizontalFrames() {
        return (int) Math.ceil(x_pix_constant / (1536.0));
    }

    /**
     * Return the number of vertical frame files that will fit within the
     * current zone, overlaps included. This only makes sense if the projection
     * is at the same scale as the chart data you are interested in.
     * 
     * @return number of frame rows in the current zone.
     */
    public int numVerticalFrames() {
        return (int) Math.round((upper_zone_extents[zone - 1] - lower_zone_extents[zone - 1])
                * (y_pix_constant / 90.0) / (1536.0));
    }

    /**
     * Figures out the number of pixels around the earth, for 360 degrees.
     * <p>
     * 
     * @param adrgscale The scale adjusted to 1:1M (1M/real scale)
     * @param zone ADRG zone
     * @return The number of pixels around the equator (360 degrees)
     */
    private double CADRG_x_pix_constant(double adrgscale, int zone) {
        // E-W pixel constant
        double x_pix = (double) adrgscale * CADRG_ARC_A[zone - 1] / 512.0;

        // Increase, if necessary, to the next highest integer value
        x_pix = Math.ceil(x_pix);
        x_pix *= 1.33333;// (512*100)/(150*256);

        // Round the final result.
        x_pix = Math.round(x_pix);

        return x_pix * 256.0;
    }

    /**
     * Calculate the maximum allowable scale.
     * <p>
     * 
     * @return float maxscale
     * 
     */
    private float CADRG_calc_maxscale() {
        // Why 1.5? It was 150/100? Why?
        return (float) Math.floor((1000000 * (float) CADRG_ARC_A[0]) / (width * 1.5f));
    }

    /**
     * Returns the number of pixels from the equator to a pole.
     * <p>
     * 
     * @param adrgscale scale adjusted to 1:1M (1M/real scale)
     * @return number of pixels from 0 to 90 degrees
     * 
     */
    private double CADRG_y_pix_constant(double adrgscale) {
        final int CADRG_ARC_B = 400384;

        double y_pix = (double) adrgscale * CADRG_ARC_B / 512.0;

        // Increase, if necessary, to the next highest integer value
        y_pix = Math.ceil(y_pix);

        y_pix *= 0.33333;// (512*100)/(4*150*256);

        // Round the final result.
        y_pix = Math.round(y_pix);

        return y_pix * 256.0;
    }

    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * A point is plot-able in the CADRG projection if it is within the North
     * and South zone limits.
     * <p>
     * 
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(double lat, double lon) {
        /**
         * I don't think what we are doing here with the latitude is right,
         * normalizing the latitude. We're trying to answer a question about
         * whether given values are valid, and we're changing those values to be
         * valid before answering. Duh. So, no normalizingLatitude before the
         * check.
         */
        lat = ProjMath.degToRad(lat);
        return ((lat < NORTH_LIMIT) && (lat > SOUTH_LIMIT));
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point2D.
     * 
     * @param lat float latitude in radians
     * @param lon float longitude in radians
     * @param ret_val Resulting XY Point2D
     * @return Point2D ret_val
     */
    public Point2D forward(double lat, double lon, Point2D ret_val, boolean isRadians) {
        if (!isRadians) {
            lon = Math.toRadians(lon);
            lat = Math.toRadians(lat);
        }

        double lon_ = wrapLongitude(lon - centerX);
        double lat_ = normalizeLatitude(lat);

        int x = (int) ProjMath.roundAdjust(spps_x * lon_) - ul.x;
        int y = (int) ProjMath.roundAdjust(-spps_y * lat_) + ul.y + oy;
        ret_val.setLocation(x, y);
        return ret_val;
    }

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * <p>
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param ret_val LatLonPoint
     * @return LatLonPoint ret_val
     * @see Proj#inverse(Point2D)
     * 
     */
    @SuppressWarnings("unchecked")
    public <T extends Point2D> T inverse(double x, double y, T ret_val) {
        // Debug.output("CADRG.inverse");

        if (ret_val == null) {
            ret_val = (T) new LatLonPoint.Double();
        }

        /* offset back into pixel space from Drawable space */
        double px = x + ul.x/* - ox */;
        double py = -y + ul.y + oy;

        // Check bounds on the call (P Space). Mutate if needed.
        if (px > ProjMath.roundAdjust(world.x / 2.0)) {
            px = ProjMath.roundAdjust(world.x / 2.0);
        } else if (px < ProjMath.roundAdjust(-world.x / 2.0)) {
            px = ProjMath.roundAdjust(-world.x / 2.0);
        }
        if (py > ProjMath.roundAdjust(world.y / 2.0)) {
            py = ProjMath.roundAdjust(world.y / 2.0);
        } else if (py < ProjMath.roundAdjust(-world.y / 2.0)) {
            py = ProjMath.roundAdjust(-world.y / 2.0);
        }

        // normalize_latitude on the way out.
        double lat_ = normalizeLatitude(py / spps_y);
        double lon_ = wrapLongitude((px / spps_x) + centerX);
        ret_val.setLocation(Math.toDegrees(lon_), Math.toDegrees(lat_));

        return ret_val;
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
    protected synchronized void computeParameters() {
        int w, h;

        if (ul == null)
            ul = new Point(0, 0); // HACK

        // quick calculate the maxscale
        maxscale = CADRG_calc_maxscale();
        if (scale > maxscale)
            scale = maxscale;

        // Compute the "ADRG" scale, which gets used below.
        double adrgscale = 1000000.0 / scale; // 1 million (from
        // ADRG
        // spec)
        if (adrgscale > CADRG_SCALE_LIMIT) {
            Debug.message("proj", "CADRG: adrgscale > CADRG_SCALE_LIMIT");
            adrgscale = CADRG_SCALE_LIMIT;
        }

        // Compute the y pixel constant based on scale.
        y_pix_constant = CADRG_y_pix_constant(adrgscale);
        if (Debug.debugging("proj")) {
            Debug.output("Y pix constant = " + y_pix_constant);
        }

        // ////

        /** Pixels per degree */
        double ppd = y_pix_constant / 90.0;
        if (upper_zone_extents == null || lower_zone_extents == null) {
            upper_zone_extents = new double[CADRG_get_zone_old_extents.length];
            lower_zone_extents = new double[CADRG_get_zone_old_extents.length + 1];

            lower_zone_extents[0] = 0f;
            lower_zone_extents[8] = 80f;
            upper_zone_extents[8] = 90f;

            // figure out new extents - from CADRG spec
            for (int x = 0; x < CADRG_get_zone_old_extents.length - 1/* 8 */; x++) {
                double pivot = Math.floor(ppd * CADRG_get_zone_old_extents[x] / 1536.0);
                lower_zone_extents[x + 1] = pivot * 1536.0 / ppd;
                // Can't go further than the equator.
                // if (x == 0) lower_zone_extents[x] = 0;
                pivot++;
                upper_zone_extents[x] = pivot * 1536.0 / ppd;
                Debug.message("proj", "lower_zone_extents[" + x + "] = " + lower_zone_extents[x]);
                Debug.message("proj", "upper_zone_extents[" + x + "] = " + upper_zone_extents[x]);
            }
        }
        // ////

        // What zone are we in? To try to reduce pixel spacing jumping when
        // zoomed
        // out, just set the zone level to one when zoomed out past 1:60M. There
        // aren't any charts available at those scales in this projection type.
    
        if (scale > 60000000) {
            zone = 1;
        } else {
            zone = getZone(ProjMath.radToDeg(centerY), y_pix_constant);
        }
        if (Debug.debugging("proj")) {
            Debug.output("Zone = " + zone);
        }

        // Compute the x pixel constant, based on scale and zone.
        x_pix_constant = CADRG_x_pix_constant(adrgscale, zone);

        // If the x_pix_constant, or number of pixels around the earth is less
        // than or equal to the width of the map window, then the corner
        // coordinates become equal or inverted (ul vs lr).
        if (width >= x_pix_constant) {
            x_pix_constant = width + 1;
        }

        if (Debug.debugging("proj")) {
            Debug.output("x_pix_constant = " + x_pix_constant);
        }
        // Now I can compute the world coordinate.
        if (world == null)
            world = new Point(0, 0);
        world.x = (int) ProjMath.roundAdjust(x_pix_constant);
        world.y = (int) ProjMath.roundAdjust(y_pix_constant * 4.0 / 2.0);
        Debug.message("proj", "world = " + world.x + "," + world.y);

        // Compute scaled pixels per RADIAN, not SCOORD
        spps_x = (double) x_pix_constant / MoreMath.TWO_PI/*
                                                           * MoreMath.DEG_TO_SC(360
                                                           * )
                                                           */;
        spps_y = (double) y_pix_constant / MoreMath.HALF_PI/*
                                                            * MoreMath.DEG_TO_SC(
                                                            * 90 )
                                                            */;
        Debug.message("proj", "spps = " + spps_x + "," + spps_y);

        // Fix the "small world" situation, computing ox, oy.
        if (width > world.x) {
            Debug.message("proj", "CADRG: fixing small world");
            w = world.x;
            // ox = (int) ProjMath.roundAdjust((width - w) / 2.0);
        } else {
            w = width;
            // ox = 0;
        }
        if (height > world.y) {
            h = (int) world.y;
            oy = (int) ProjMath.roundAdjust((height - h) / 2.0);
        } else {
            h = height;
            oy = 0;
        }

        // compute the "upper left" adjustment.
        long temp = (long) ProjMath.roundAdjust(spps_y * centerY);
        if (Debug.debugging("proj")) {
            Debug.output("CADRG.temp = " + temp);
        }
        if (ul == null)
            ul = new Point(0, 0);
        ul.x = (int) ProjMath.roundAdjust(-w / 2.0);
        if ((temp != 0) && (oy != 0)) {
            ul.y = (int) ProjMath.roundAdjust(h / 2.0);
        } else {
            ul.y = (int) temp + (int) ProjMath.roundAdjust(h / 2.0);
        }

        if (Debug.debugging("proj")) {
            Debug.output("CADRG: ul = " + ul.x + "," + ul.y);
            Debug.output(/* "ox = " + ox + */" oy = " + oy);
        }

        // Finally compute some useful cylindrical projection
        // parameters
        // maxscale = (CADRG_ARC_A[0] * (1000000/width));// HACK!!!
        half_world = world.x / 2;

        if (scale > maxscale) {
            scale = maxscale;
        }
        // scaled_radius = planetPixelRadius/scale;
        Debug.message("proj", "CADRG.computeParameters(): maxscale: " + maxscale);
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return CADRGName;
    }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a Point2D reflecting a pixel spot on the projection that
     *        matches the ll1 coordinate, the upper left corner of the area of
     *        interest.
     * @param point2 a Point2D reflecting a pixel spot on the projection that
     *        matches the ll2 coordinate, usually the lower right corner of the
     *        area of interest.
     */
    public float getScale(Point2D ll1, Point2D ll2, Point2D point1, Point2D point2) {
        return getScale(ll1, ll2, point1, point2, 0);
    }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a Point2D reflecting a pixel spot on the projection that
     *        matches the ll1 coordinate, the upper left corner of the area of
     *        interest.
     * @param point2 a Point2D reflecting a pixel spot on the projection that
     *        matches the ll2 coordinate, usually the lower right corner of the
     *        area of interest.
     * @param recursiveCount a protective count to keep this method from getting
     *        in a recursive death spiral.
     */
    private float getScale(Point2D ll1, Point2D ll2, Point2D point1, Point2D point2,
                           int recursiveCount) {

        try {

            double deltaDegrees;
            double pixPerDegree;
            int deltaPix;
            double ret;
            double dx = Math.abs(point2.getX() - point1.getX());
            double dy = Math.abs(point2.getY() - point1.getY());

            double nCenterLat = Math.min(ll1.getY(), ll2.getY())
                    + Math.abs(ll1.getY() - ll2.getY()) / 2;
            double nCenterLon = Math.min(ll1.getX(), ll2.getX())
                    + Math.abs(ll1.getX() - ll2.getX()) / 2;

            if (dx < dy) {
                double dlat = Math.abs(ll1.getX() - ll2.getY());
                deltaDegrees = dlat;
                deltaPix = getHeight();
                pixPerDegree = getScale() * getYPixConstant() / 90;
            } else {
                double dlon;
                double lat1, lon1, lon2;

                // point1 is to the right of point2. switch the
                // LatLonPoints so that ll1 is west (left) of ll2.
                if (point1.getX() > point2.getX()) {
                    lat1 = ll1.getY();
                    lon1 = ll1.getX();
                    ll1.setLocation(ll2);
                    // Remember for setLocation the order of args is reversed
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
                deltaPix = getWidth();
                pixPerDegree = getPlanetPixelCircumference() / 360;
            }

            // The new scale...
            ret = pixPerDegree / (deltaPix / deltaDegrees);

            // OK, now given the new scale at the apparent new center
            // location, we need to test if the zone changes, because
            // if it does, the values don't work out right because the
            // pixel spacings are different. If the zones are
            // different, we need to recalculate the scale based on
            // the
            // new zone.
            CADRG newcadrg = new CADRG(new LatLonPoint.Double(nCenterLat, nCenterLon), (float) ret, getWidth(), getHeight());

            // Use the recursiveCount to prevent extended recalls. A
            // couple rounds should suffice.
            if (newcadrg.getZone() != zone && recursiveCount < 2) {
                ret = newcadrg.getScale(ll1, ll2, newcadrg.forward(ll1), newcadrg.forward(ll2), recursiveCount + 1);
            }

            return (float) ret;
        } catch (NullPointerException npe) {
            Debug.error("ProjMath.getScale(): caught null pointer exception.");
            return Float.MAX_VALUE;
        }
    }

    public static CADRG convertProjection(Projection proj) {
        if (proj instanceof CADRG) {
            return (CADRG) proj;
        }

        CADRG cadrg = new CADRG((LatLonPoint) proj.getCenter(new LatLonPoint.Float()), proj.getScale(), proj.getWidth(), proj.getHeight());

        Point2D ulp = cadrg.forward(proj.getUpperLeft());
        Point2D lrp = cadrg.forward(proj.getLowerRight());

        int w = (int) Math.abs(lrp.getX() - ulp.getX());
        int h = (int) Math.abs(lrp.getY() - ulp.getY());

        return new CADRG((LatLonPoint) proj.getCenter(new LatLonPoint.Float()), proj.getScale(), w, h);
    }

    /*
     * public static void main (String argv[]) { CADRG proj= new CADRG(new
     * LatLonPoint(42.0f, 0.0f), 18000000.0f, 620,480);
     * 
     * Debug.output("---testing latitude"); proj.testPoint(0.0f, 0.0f);
     * proj.testPoint(10.0f, 0.0f); proj.testPoint(-10.0f, 0.0f);
     * proj.testPoint(23.1234f, 0.0f); proj.testPoint(-23.1234f, 0.0f);
     * proj.testPoint(90.0f, 0.0f); proj.testPoint(-100.0f, 0.0f);
     * 
     * Debug.output("---testing longitude"); proj.testPoint(0.0f, 10.0f);
     * proj.testPoint(0.0f, -10.0f); proj.testPoint(0.0f, 86.45f);
     * proj.testPoint(0.0f, -86.45f); proj.testPoint(0.0f, 375.0f);
     * proj.testPoint(0.0f, -375.0f); }
     * 
     * private void testPoint(float lat, float lon) { LatLonPoint llpoint = new
     * LatLonPoint(ProjMath.radToDeg(
     * normalize_latitude(ProjMath.degToRad(lat))), lon); Point point =
     * forward(llpoint);
     * 
     * Debug.output("(lon="+llpoint.getLongitude()+
     * ",lat="+llpoint.getLatitude()+ ") = (x="+point.x+",y="+point.y+")");
     * 
     * llpoint = inverse(point);
     * 
     * Debug.output("(x="+point.x+",y="+point.y+") = (lon="+
     * llpoint.getLongitude()+",lat="+ llpoint.getLatitude()+")"); }
     */
}