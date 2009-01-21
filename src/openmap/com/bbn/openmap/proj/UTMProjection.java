package com.bbn.openmap.proj;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.proj.coords.LatLonPoint;
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

    protected int hy, wx;

    /**
     * Pixel per map unit. this is for a projection with a quadratic grid like
     * utm
     */
    protected float ppu;

    // used for calculating wrapping of ArrayList graphics
    // TODO: copied from Cylindrical. may need to change.
    protected Point world; // world width in pixels.

    protected int half_world; // world.x / 2

    private int zone_number;

    private boolean isnorthern;

    private Ellipsoid ellps;

    /**
     * The UTMType number. Just a random int not used by any other.
     */
    public final static transient int UTMType = 993387;

    public UTMProjection(LatLonPoint center, float s, int w, int h, int type,
            int zone_number, boolean isnorthern, Ellipsoid ellps) {
        super(center, s, w, h);

        this.zone_number = zone_number;
        this.isnorthern = isnorthern;
        this.ellps = ellps;
    }

    protected void computeParameters() {
        // super.computeParameters();

        hy = height / 2;
        wx = width / 2;

        if (xycenter != null) {
            UTMPoint c = UTMPoint.LLtoUTM((LatLonPoint) getCenter(),
                    ellps,
                    new UTMPoint(),
                    zone_number,
                    isnorthern);
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

    public float normalize_latitude(float lat) {
        if (lat > NORTH_POLE) {
            return NORTH_POLE;
        } else if (lat < SOUTH_POLE) {
            return SOUTH_POLE;
        }
        return lat;
    }

    public Point2D forward(LatLonPoint llp, Point2D pt) {
        return forward(llp, pt, new UTMPoint());
    }

    @Override
    public Point2D forward(double lat, double lon, Point2D pt, boolean isRadian) {
        LatLonPoint llp = new LatLonPoint.Double(lat, lon, isRadian);
        return forward(llp, pt, new UTMPoint());
    }

    public Point2D forward(double lat, double lon, Point2D pt,
                           boolean isRadian, UTMPoint utmPoint) {
        LatLonPoint llp = new LatLonPoint.Double(lat, lon, isRadian);
        return forward(llp, pt, utmPoint);
    }

    private Point2D forward(LatLonPoint llp, Point2D pt, UTMPoint utmPoint) {
        utmPoint = UTMPoint.LLtoUTM(llp,
                ellps,
                utmPoint,
                zone_number,
                isnorthern);

        pt.setLocation((wx + Math.round(ppu
                * (utmPoint.easting - xycenter.getX()))), (hy - Math.round(ppu
                * (utmPoint.northing - xycenter.getY()))));

        return pt;
    }

    public Point2D inverse(double x, double y, Point2D llpt) {

        double northing = xycenter.getY() + ((hy - y) / ppu);
        double easting = xycenter.getX() + ((x - wx) / ppu);

        if (!(llpt instanceof LatLonPoint)) {
            llpt = new LatLonPoint.Double();
        }

        llpt = UTMPoint.UTMtoLL(ellps,
                (float) northing,
                (float) easting,
                zone_number,
                isnorthern,
                (LatLonPoint) llpt);

        return llpt;
    }

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
    public float getScale(LatLonPoint ll1, LatLonPoint ll2, Point point1,
                          Point point2) {

        // super does not calculate scale correct for projections that does use
        // the same earth radius up north..

        float widthPX = point2.x - point1.x;

        // float heightPX = point2.y - point1.y;

        UTMPoint xx1 = UTMPoint.LLtoUTM(ll1,
                ellps,
                new UTMPoint(),
                zone_number,
                isnorthern);
        UTMPoint xx2 = UTMPoint.LLtoUTM(ll2,
                ellps,
                new UTMPoint(),
                zone_number,
                isnorthern);

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

    public boolean isPlotable(float lat, float lon) {
        return true;
    }

    protected ArrayList<int[]> _forwardPoly(float[] rawllpts, int ltype,
                                            int nsegs, boolean isFilled) {
        // TODO: copied from Cylindrical. may need to change.
        int n, k, flag = 0, min = 0, max = 0, xp, xadj = 0;

        // determine length of pairs list
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<int[]>(0);

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype)) {
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);
        }

        // determine when to stop
        Point temp = new Point(0, 0);
        int[] xs = new int[len];
        int[] ys = new int[len];

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
        ArrayList<int[]> ret_val = null;
        ret_val = new ArrayList<int[]>(2 + 2 * (max + min));
        ret_val.add(xs);
        ret_val.add(ys);
        int[] altx = null;

        /*
         * if (Debug.debugging("proj")) { dumpPoly(rawllpts, xs, ys); }
         */

        // add the extra left-wrap polys
        for (int i = 1; i <= min; i++) {
            altx = new int[xs.length];
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
            altx = new int[xs.length];
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
    protected ArrayList<int[]> _forwardPoly(double[] rawllpts, int ltype,
                                            int nsegs, boolean isFilled) {
        // TODO: copied from Cylindrical. may need to change.
        int n, k, flag = 0, min = 0, max = 0, xp, xadj = 0;

        // determine length of pairs list
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<int[]>(0);

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype)) {
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);
        }

        // determine when to stop
        Point temp = new Point(0, 0);
        int[] xs = new int[len];
        int[] ys = new int[len];

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
        ArrayList<int[]> ret_val = null;
        ret_val = new ArrayList<int[]>(2 + 2 * (max + min));
        ret_val.add(xs);
        ret_val.add(ys);
        int[] altx = null;

        /*
         * if (Debug.debugging("proj")) { dumpPoly(rawllpts, xs, ys); }
         */

        // add the extra left-wrap polys
        for (int i = 1; i <= min; i++) {
            altx = new int[xs.length];
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
            altx = new int[xs.length];
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
     * @param g Graphics2D
     * @param paint java.awt.Paint to use for the background
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

    public boolean forwardRaw(float[] rawllpts, int rawoff, int[] xcoords,
                              int[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
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

    public Point2D getLowerRight() {
        return inverse(width - 1, height - 1);
    }

    public Point2D getUpperLeft() {
        return inverse(0, 0);
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
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see com.bbn.openmap.LatLonPoint#normalizeLatitude(float)
     * 
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

}
