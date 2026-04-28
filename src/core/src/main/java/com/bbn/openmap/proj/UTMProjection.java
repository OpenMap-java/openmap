package com.bbn.openmap.proj;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.proj.coords.UTMGCT;
import com.bbn.openmap.proj.coords.UTMPoint;

/**
 * A OpenMap Projection class that uses the {@link UTMPoint} to do its
 * calculation.
 */
public class UTMProjection extends GeoProj {

    /**
     * Center of view as xy coordinates relative to the underlying projection
     */
    // TODO: find a better name?
    protected Point2D.Double xycenter = new Point2D.Double();

    protected double hy, wx;

    /**
     * Pixel per map unit. this is for a projection with a quadratic grid like
     * utm
     */
    protected double ppu;

    // used for calculating wrapping of ArrayList graphics
    // TODO: copied from Cylindrical. may need to change.
    protected Point world; // world width in pixels.

    protected int half_world; // world.x / 2

    protected int zoneNumber;

    protected boolean northern;

    protected Ellipsoid ellps;

    public UTMProjection(LatLonPoint center, float s, int w, int h, int zone_number,
            boolean isnorthern, Ellipsoid ellps) {
        super(center, s, w, h);

        this.zoneNumber = zone_number;
        this.northern = isnorthern;
        this.ellps = ellps;
    }

    protected void computeParameters() {
        // super.computeParameters();

        hy = height / 2;
        wx = width / 2;

        if (xycenter != null) {
            UTMPoint c = UTMPoint.LLtoUTM(getCenter(), ellps, new UTMPoint(), zoneNumber,
                                          northern);
            xycenter.setLocation(c.easting, c.northing);
        }

        // width of the world in pixels at current scale
        // TODO: copied from Cylindrical. may need to change
        if (world == null) {
            world = new Point();
        }
        world.x = (int) (planetPixelCircumference / scale);
        half_world = world.x / 2;

        ppu = (((float) pixelsPerMeter) / getScale());
    }

    public Point2D forward(LatLonPoint llp, Point2D pt) {
        return forward(llp, pt, new UTMPoint());
    }

    @Override
    public Point2D forward(double lat, double lon, Point2D pt, boolean isRadian) {
        LatLonPoint llp = new LatLonPoint.Double(lat, lon, isRadian);
        return forward(llp, pt, new UTMPoint());
    }

    public Point2D forward(double lat, double lon, Point2D pt, boolean isRadian,
                           UTMPoint utmPoint) {
        LatLonPoint llp = new LatLonPoint.Double(lat, lon, isRadian);
        return forward(llp, pt, utmPoint);
    }

    private Point2D forward(LatLonPoint llp, Point2D pt, UTMPoint utmPoint) {
        utmPoint = UTMPoint.LLtoUTM(llp, ellps, utmPoint, zoneNumber, northern);

        pt.setLocation((wx + (ppu * (utmPoint.easting - xycenter.getX()))),
                       (hy - (ppu * (utmPoint.northing - xycenter.getY()))));

        return pt;
    }

    public <T extends Point2D> T inverse(double x, double y, T llpt) {

        double northing = xycenter.getY() + ((hy - y) / ppu);
        double easting = xycenter.getX() + ((x - wx) / ppu);

        if (!(llpt instanceof LatLonPoint)) {
            llpt = (T) new LatLonPoint.Double();
        }

        llpt = (T) UTMPoint.UTMtoLL(ellps, northing, easting, zoneNumber, northern,
                                    (LatLonPoint) llpt);

        return llpt;
    }

    /**
     * Given a couple of points representing a bounding box, find out what the
     * scale should be in order to make those points appear at the corners of
     * the projection.
     * 
     * @param ll1
     *            the upper left coordinates of the bounding box.
     * @param ll2
     *            the lower right coordinates of the bounding box.
     * @param point1
     *            a java.awt.Point reflecting a pixel spot on the projection
     *            that matches the ll1 coordinate, the upper left corner of the
     *            area of interest.
     * @param point2
     *            a java.awt.Point reflecting a pixel spot on the projection
     *            that matches the ll2 coordinate, usually the lower right
     *            corner of the area of interest.
     */
    @Override
    public float getScale(Point2D ll1, Point2D ll2, Point2D point1, Point2D point2) {

        // super does not calculate scale correct for projections that does use
        // the same earth radius up north..

        double widthPX = point2.getX() - point1.getX();

        // float heightPX = point2.y - point1.y;
        
        // Instead of blindly casting, lets just make sure we have the correct object type.
        LatLonPoint llp1 = LatLonPoint.getDouble(ll1);
        LatLonPoint llp2 = LatLonPoint.getDouble(ll2);

        UTMPoint xx1 = UTMPoint.LLtoUTM(llp1, ellps, new UTMPoint(),
                                        zoneNumber, northern);
        UTMPoint xx2 = UTMPoint.LLtoUTM(llp2, ellps, new UTMPoint(),
                                        zoneNumber, northern);

        double widthMap = (xx2.easting - xx1.easting);
        float widthScale = (float) (((double) getPPM()) * (widthMap / widthPX));

        // float heightMap = (xx2.northing - xx1.northing);
        // float heightScale = (float) (((double) getPPM()) * (heightMap /
        // heightPX));

        // TODO: use width-, height- or medium scale? I guess width- and height
        // scale should be equal as the grid inside a single UTM zone is
        // quadratic.

        return widthScale;
    }

    protected ArrayList<float[]> _forwardPoly(float[] rawllpts, int ltype, int nsegs,
                                            boolean isFilled) {
        // TODO: copied from Cylindrical. may need to change.
        int n, k, flag = 0, min = 0, max = 0, xp, xadj = 0;

        // determine length of pairs list
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<float[]>(0);

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype)) {
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);
        }

        // determine when to stop
        Point temp = new Point(0, 0);
        float[] xs = new float[len];
        float[] ys = new float[len];

        // more temp objects to limit number of new objects that needs to be
        // created
        UTMPoint tempUtm = new UTMPoint();
        LatLonPoint tempLL = new LatLonPoint.Double();

        // forward project the first point
        tempLL.setLatLon(rawllpts[0], rawllpts[1], true);
        forward(tempLL, temp, tempUtm);
        // forward(rawllpts[0], rawllpts[1], temp, true, tempUtm);
        xp = temp.x;
        xs[0] = temp.x;
        ys[0] = temp.y;
        // forward project the other points
        for (n = 1, k = 2; n < len; n++, k += 2) {
            tempLL.setLatLon(rawllpts[k], rawllpts[k + 1], true);
            forward(tempLL, temp, tempUtm);
            // forward(rawllpts[k], rawllpts[k + 1], temp, true, tempUtm);
            xs[n] = temp.x;
            ys[n] = temp.y;
            // segment crosses longitude along screen edge
            if (Math.abs(xp - xs[n]) >= half_world) {
                flag += (xp < xs[n]) ? -1 : 1;// inc/dec the wrap
                // count
                min = (flag < min) ? flag : min;// left wrap count
                max = (flag > max) ? flag : max;// right wrap count
                xadj = flag * world.x;// adjustment to x coordinates
                // Debug.output("flag=" + flag + " xadj=" + xadj);
            }
            xp = temp.x;// save previous unshifted x coordinate
            if (flag != 0) {
                xs[n] += xadj;// adjust x coordinates
            }
        }
        min *= -1;// positive magnitude

        // now create the return list
        ArrayList<float[]> ret_val = null;
        ret_val = new ArrayList<float[]>(2 + 2 * (max + min));
        ret_val.add(xs);
        ret_val.add(ys);
        float[] altx = null;

        /*
         * if (Debug.debugging("proj")) { dumpPoly(rawllpts, xs, ys); }
         */

        // add the extra left-wrap polys
        for (int i = 1; i <= min; i++) {
            altx = new float[xs.length];
            xadj = i * world.x;// shift opposite
            for (int j = 0; j < altx.length; j++) {
                altx[j] = xs[j] + xadj;
            }
            ret_val.add(altx);
            ret_val.add(ys);
            /*
             * if (Debug.debugging("proj")) { dumpPoly(rawllpts, altx, ys); }
             */
        }

        // add the extra right-wrap polys
        for (int i = 1; i <= max; i++) {
            altx = new float[xs.length];
            xadj = -i * world.x;// shift opposite
            for (int j = 0; j < altx.length; j++) {
                altx[j] = xs[j] + xadj;
            }
            ret_val.add(altx);
            ret_val.add(ys);
            /*
             * if (Debug.debugging("proj")) { dumpPoly(rawllpts, altx, ys); }
             */
        }

        return ret_val;
    }// _forwardPoly()

    @Override
    protected ArrayList<float[]> _forwardPoly(double[] rawllpts, int ltype, int nsegs,
                                            boolean isFilled) {
        // TODO: copied from Cylindrical. may need to change.
        int n, k, flag = 0, min = 0, max = 0, xp, xadj = 0;

        // determine length of pairs list
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<float[]>(0);

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype)) {
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);
        }

        // determine when to stop
        Point temp = new Point(0, 0);
        float[] xs = new float[len];
        float[] ys = new float[len];

        // more temp objects to limit number of new objects that needs to be
        // created
        UTMPoint tempUtm = new UTMPoint();
        LatLonPoint tempLL = new LatLonPoint.Double();

        // forward project the first point
        tempLL.setLatLon(rawllpts[0], rawllpts[1], true);
        forward(tempLL, temp, tempUtm);
        // forward(rawllpts[0], rawllpts[1], temp, true, tempUtm);
        xp = temp.x;
        xs[0] = temp.x;
        ys[0] = temp.y;
        // forward project the other points
        for (n = 1, k = 2; n < len; n++, k += 2) {
            tempLL.setLatLon(rawllpts[k], rawllpts[k + 1], true);
            forward(tempLL, temp, tempUtm);
            // forward(rawllpts[k], rawllpts[k + 1], temp, true, tempUtm);
            xs[n] = temp.x;
            ys[n] = temp.y;
            // segment crosses longitude along screen edge
            if (Math.abs(xp - xs[n]) >= half_world) {
                flag += (xp < xs[n]) ? -1 : 1;// inc/dec the wrap
                // count
                min = (flag < min) ? flag : min;// left wrap count
                max = (flag > max) ? flag : max;// right wrap count
                xadj = flag * world.x;// adjustment to x coordinates
                // Debug.output("flag=" + flag + " xadj=" + xadj);
            }
            xp = temp.x;// save previous unshifted x coordinate
            if (flag != 0) {
                xs[n] += xadj;// adjust x coordinates
            }
        }
        min *= -1;// positive magnitude

        // now create the return list
        ArrayList<float[]> ret_val = null;
        ret_val = new ArrayList<float[]>(2 + 2 * (max + min));
        ret_val.add(xs);
        ret_val.add(ys);
        float[] altx = null;

        /*
         * if (Debug.debugging("proj")) { dumpPoly(rawllpts, xs, ys); }
         */

        // add the extra left-wrap polys
        for (int i = 1; i <= min; i++) {
            altx = new float[xs.length];
            xadj = i * world.x;// shift opposite
            for (int j = 0; j < altx.length; j++) {
                altx[j] = xs[j] + xadj;
            }
            ret_val.add(altx);
            ret_val.add(ys);
            /*
             * if (Debug.debugging("proj")) { dumpPoly(rawllpts, altx, ys); }
             */
        }

        // add the extra right-wrap polys
        for (int i = 1; i <= max; i++) {
            altx = new float[xs.length];
            xadj = -i * world.x;// shift opposite
            for (int j = 0; j < altx.length; j++) {
                altx[j] = xs[j] + xadj;
            }
            ret_val.add(altx);
            ret_val.add(ys);
            /*
             * if (Debug.debugging("proj")) { dumpPoly(rawllpts, altx, ys); }
             */
        }

        return ret_val;
    }

    /**
     * Draw the background for the projection.
     * 
     * @param g
     *            Graphics2D
     * @param paint
     *            java.awt.Paint to use for the background
     */
    public void drawBackground(Graphics2D g, java.awt.Paint paint) {
        g.setPaint(paint);
        drawBackground(g);
    }

    /**
     * Assume that the Graphics has been set with the Paint/Color needed, just
     * render the shape of the background.
     */
    public void drawBackground(Graphics g) {
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public boolean forwardRaw(float[] rawllpts, int rawoff, int[] xcoords, int[] ycoords,
                              boolean[] visible, int copyoff, int copylen) {
        // TODO: copied from Cylindrical. may need a change
        Point temp = new Point();
        UTMPoint tempUtm = new UTMPoint();
        LatLonPoint tempLL = new LatLonPoint.Double();
        int end = copylen + copyoff;
        for (int i = copyoff, j = rawoff; i < end; i++, j += 2) {
            tempLL.setLatLon(rawllpts[j], rawllpts[j + 1], true);
            forward(tempLL, temp, tempUtm);
            // forward(rawllpts[j], rawllpts[j + 1], temp, true, tempUtm);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;
            visible[i] = true;
        }
        // everything is visible
        return true;
    }

    public LatLonPoint getLowerRight() {
        return inverse(width - 1, height - 1, new LatLonPoint.Double());
    }

    public LatLonPoint getUpperLeft() {
        return inverse(0, 0, new LatLonPoint.Double());
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
        computeParameters();
    }

    public boolean isNorthern() {
        return northern;
    }

    public void setNorthern(boolean northern) {
        this.northern = northern;
        computeParameters();
    }

    public Ellipsoid getEllps() {
        return ellps;
    }

    public void setEllps(Ellipsoid ellps) {
        this.ellps = ellps;
        computeParameters();
    }

    /*
     * HACK epsilon: skirt the edge of the infinite. If this is too small then
     * we get too close to +-INFINITY when we forward project. Tweak this if you
     * start getting Infinity or NaN's for forward().
     */
    protected static double epsilon = 0.01f;

    /**
     * Sets radian latitude to something sane. This is an abstract function
     * since some projections don't deal well with extreme latitudes.
     * 
     * @param lat
     *            float latitude in radians
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

    public boolean isPlotable(double lat, double lon) {
        // TODO need to figure out how to calculate this
        return true;
    }

    /**
     * Convenience method to create a GCT for this projection. For projections
     * that start with lat/lon coordinates, this will return a LatLonGCT. For
     * projections that have world coordinates in meters, the GCT will provide a
     * way to get to those meter coordinates. For instance, a UTMProjection will
     * return a UTMGCT.
     * 
     * @return UTMGCT for current projection
     */
    public UTMGCT getGCTForProjection() {
        return new UTMGCT(getZoneNumber(), isNorthern() ? 'N' : 'S');
    }
}
