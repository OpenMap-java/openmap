// **********************************************************************
//
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/LambertConformal.java,v $
// $RCSfile: LambertConformal.java,v $
// $Revision: 1.10 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Implements the LambertConformalConic projection. <br>
 * <br>
 * <b>NOTE: </b> This implementation only works for the northern hemisphere. <br>
 * <br>
 * Needs to be modified for use in the southern hemisphere.
 * <p>
 * 
 * @author David J. Ward
 * @author Chris van Lith
 * @see http://www.epsg.org/guides/docs/G7-2.pdf
 */
public class LambertConformal extends GeoProj {

    /**
     * The LambertCC name.
     */
    public final static transient String LambertConformalName = "Lambert Conformal";

    private static final int MODE_2SP = 1;
    private static final int MODE_BELGIUM = 2;

    private int mode = MODE_2SP;

    private double lambert_sp_one;
    private double lambert_sp_two;
    private double centralMeridian;

    double locationCenterXPixel = 0;
    double locationCenterYPixel = 0;
    double locationCenterXLambert = 0.0;
    double locationCenterYLambert = 0.0;
    double locationPixelsPerLambert = 0.0;

    double locationOriginX = 0.0;
    double locationOriginY = 0.0;

    double referenceLatitude = 0.0;
    double falseEasting = 0.0;
    double falseNorthing = 0.0;

    // EPSG Guidance Note number 7, part 2 - November 2005
    // Lambert Conic Conformal 9802
    double n = 0.0;
    double F = 0.0;
    double rf = 0.0;
    double lamdaf = 0.0;
    double alpha = 0.0;

    private Ellipsoid ellps = Ellipsoid.WGS_84;

    /**
     * Construct a Lambert projection.
     * <p>
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    protected LambertConformal(LatLonPoint center, float scale, int width,
            int height) {
        super(center, scale, width, height);
    }

    /**
     * Constructor for the lambert conformal projection.
     * 
     * @param center center location for projections
     * @param scale scale of projection
     * @param width width of projection
     * @param height height of projection
     * @param centralMeridian the Central Meridian in degrees.
     * @param sp_one Standard Parallel One in degrees.
     * @param sp_two Standard Parallel Two in degrees.
     */
    protected LambertConformal(LatLonPoint center, float scale, int width,
            int height, float centralMeridian, float sp_one, float sp_two,
            Ellipsoid ellps) {
        this(center,
             scale,
             width,
             height,
             centralMeridian,
             sp_one,
             sp_two,
             0f,
             0,
             0,
             ellps);
    }

    /**
     * Constructor for the lambert conformal projection.
     * 
     * @param center center location for projections
     * @param scale scale of projection
     * @param width width of projection
     * @param height height of projection
     * @param centralMeridian the Central Meridian in degrees.
     * @param sp_one Standard Parallel One in degrees.
     * @param sp_two Standard Parallel Two in degrees.
     * @param reference_latitude the latitude for the origin of the projection
     * @param falseEasting number of meters added as buffer to origin E/W.
     * @param falseNorthing number of meters added as buffer to origin for N/S.
     * @param ellps the {@link Ellipsoid} used for the projection
     */
    public LambertConformal(LatLonPoint center, float scale, int width,
            int height, double centralMeridian, double sp_one, double sp_two,
            double reference_latitude, double falseEasting,
            double falseNorthing, Ellipsoid ellps) {

        super(center, scale, width, height);

        this.centralMeridian = centralMeridian;
        this.lambert_sp_one = sp_one;
        this.lambert_sp_two = sp_two;
        this.referenceLatitude = reference_latitude;
        this.falseEasting = falseEasting;
        this.falseNorthing = falseNorthing;
        this.ellps = ellps;

        computeParameters();
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
    public void computeParameters() {

        // work around problem caused by Proj calling this method before
        // constructor is done
        if (ellps == null) {
            ellps = Ellipsoid.WGS_84;
        }

        if (mode == MODE_BELGIUM) {
            // Belgium EPSG 9803, adjustment 1972
            alpha = 0.00014204d;
        } else {
            alpha = 0d;
        }

        double phi1 = lambert_sp_one / 180.0 * Math.PI;
        double phi2 = lambert_sp_two / 180.0 * Math.PI;
        double phif = referenceLatitude / 180.0 * Math.PI;
        double e = ellps.ecc;

        double m1 = Math.cos(phi1)
                / Math.pow(1 - Math.pow(e * Math.sin(phi1), 2.0), 0.5);
        double m2 = Math.cos(phi2)
                / Math.pow(1 - Math.pow(e * Math.sin(phi2), 2.0), 0.5);

        double t1 = Math.tan(Math.PI / 4.0 - phi1 / 2.0)
                / Math.pow((1.0 - e * Math.sin(phi1))
                        / (1.0 + e * Math.sin(phi1)), e / 2.0);
        double t2 = Math.tan(Math.PI / 4.0 - phi2 / 2.0)
                / Math.pow((1.0 - e * Math.sin(phi2))
                        / (1.0 + e * Math.sin(phi2)), e / 2.0);
        double tf = Math.tan(Math.PI / 4.0 - phif / 2.0)
                / Math.pow((1.0 - e * Math.sin(phif))
                        / (1.0 + e * Math.sin(phif)), e / 2.0);

        n = (Math.log(m1) - Math.log(m2)) / (Math.log(t1) - Math.log(t2));
        F = m1 / (n * Math.pow(t1, n));
        rf = ellps.radius * F * Math.pow(tf, n);
        lamdaf = centralMeridian / 180.0 * Math.PI;

        locationCenterXPixel = ((double) getWidth() / 2d);
        locationCenterYPixel = ((double) getHeight() / 2d);

        locationPixelsPerLambert = (double) getPPM() / getScale();

        Point2D lp = new Point2D.Double();
        LatLonPoint origin = new LatLonPoint.Double(referenceLatitude, centralMeridian);
        LLToWorld(origin.getY(), origin.getX(), lp);
        locationOriginX = lp.getX();
        locationOriginY = lp.getY();

        Point2D center = getCenter();
        LLToWorld(center.getY(), center.getX(), lp);
        locationCenterXLambert = lp.getX();
        locationCenterYLambert = lp.getY();

        if (Debug.debugging("Lambert")) {
            Debug.output("Creating LambertConformal: center x = "
                    + locationCenterXLambert + ", center y = "
                    + locationCenterYLambert);
            Debug.output("Creating LambertConformal: origin x = "
                    + locationOriginX + ", origin y = " + locationOriginY);
        }

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
        if (lat > NORTH_POLE) {
            return NORTH_POLE;
        } else if (lat < SOUTH_POLE) {
            return SOUTH_POLE;
        }
        return lat;
    }

    /**
     * Pan the map/projection.
     * <ul>
     * <li><code>pan(±180, c)</code> pan south
     * <li><code>pan(-90, c)</code> pan west
     * <li><code>pan(0, c)</code> pan north
     * <li><code>pan(90, c)</code> pan east
     * </ul>
     * 
     * @param Az azimuth "east of north" in decimal degrees:
     *        <code>-180 &lt;= Az &lt;= 180</code>
     */
    public void pan(float Az) {
        if (MoreMath.approximately_equal(Math.abs(Az), 180f, 0.01f)) {
            setCenter(inverse(width / 2, height));// south
        } else if (MoreMath.approximately_equal(Az, -135f, 0.01f)) {
            setCenter(inverse(0, height));// southwest
        } else if (MoreMath.approximately_equal(Az, -90f, 0.01f)) {
            setCenter(inverse(0, height / 2));// west
        } else if (MoreMath.approximately_equal(Az, -45f, 0.01f)) {
            setCenter(inverse(0, 0));// northwest
        } else if (MoreMath.approximately_equal(Az, 0f, 0.01f)) {
            setCenter(inverse(width / 2, 0));// north
        } else if (MoreMath.approximately_equal(Az, 45f, 0.01f)) {
            setCenter(inverse(width, 0));// northeast
        } else if (MoreMath.approximately_equal(Az, 90f, 0.01f)) {
            setCenter(inverse(width, height / 2));// east
        } else if (MoreMath.approximately_equal(Az, 135f, 0.01f)) {
            setCenter(inverse(width, height));// southeast
        } else {
            super.pan(Az);
        }
    }

    /*----------------------------------------------------------------------------
     * FUNCTION:     LLToWorld
     * DATE CREATE:  13-4-2006
     * CREATED BY:   Chris van Lith
     * DESCRIPTION:  This function converts lat, lon coordinate to lambert
     *               coordinate.
     *--------------------------------------------------------------------------*/
    public Point2D LLToWorld(double lat, double lon, Point2D lp) {

        // projectie
        double phi_deg = lat;
        double phi = phi_deg / 180.0 * Math.PI;
        double lamba_deg = lon;
        double lamba = lamba_deg / 180.0 * Math.PI;

        double e = ellps.ecc;

        double t = Math.tan(Math.PI / 4.0 - phi / 2.0)
                / Math.pow((1.0 - e * Math.sin(phi))
                        / (1.0 + e * Math.sin(phi)), e / 2.0);
        double r = ellps.radius * F * Math.pow(t, n);
        double theta = n * (lamba - lamdaf);

        double easting = falseEasting + r * Math.sin(theta - alpha);
        double northing = falseNorthing + rf - r * Math.cos(theta - alpha);

        lp.setLocation(easting, northing);

        return lp;
    } /* end of function LLToWorld */

    /*----------------------------------------------------------------------------
     * FUNCTION:     lat_lon_to_pixel
     * DATE CREATE:  28 February 1995
     * CREATED BY:   David J. Ward
     * DESCRIPTION:  This function converts lat, lon coordinate to pixel
     *               coordinate.
     *--------------------------------------------------------------------------*/

    public Point2D LLToPixel(double lat, double lon, Point2D p) {
        Point2D lp = new Point2D.Double();

        LLToWorld(lat, lon, lp);

        double xrel = lp.getX() - locationCenterXLambert;
        double yrel = lp.getY() - locationCenterYLambert;

        xrel = (xrel * locationPixelsPerLambert);
        yrel = (yrel * locationPixelsPerLambert);

        xrel = locationCenterXPixel + xrel;
        yrel = locationCenterYPixel - yrel;

        if (p == null) {
            p = new Point2D.Double();
        }

        p.setLocation(xrel, yrel);

        return p;
    } /* end of function LLToPixel */

    /*----------------------------------------------------------------------------
     * FUNCTION:     worldToLL
     * DATE CREATE:  13-4-2006
     * CREATED BY:   Chris van Lith
     * DESCRIPTION:  This function converts lambert projections to lat, lon
     *               coordinate.  It is given the lambert projections as the
     *               input, and the lat, lon coordinate as the output.
     *--------------------------------------------------------------------------*/
    public Point2D worldToLL(double x, double y, Point2D llp) {

        double thetaR = Math.atan((x - falseEasting)
                / (rf - (y - falseNorthing)));
        double rR = Math.sqrt(Math.pow(x - falseEasting, 2)
                + Math.pow(rf - (y - falseNorthing), 2));
        double tR = Math.pow(rR / (ellps.radius * F), 1 / n);

        double lamda = ((thetaR + alpha) / n) + lamdaf;

        double e = ellps.ecc;

        double phiT1 = 0.0;
        double phiT2 = (Math.PI / 2 - 2 * Math.atan(tR));
        double significance = 1.0e-8;
        do {
            phiT1 = phiT2;
            phiT2 = (Math.PI / 2 - 2 * Math.atan(tR
                    * Math.pow((1.0 - e * Math.sin(phiT1))
                            / (1.0 + e * Math.sin(phiT1)), e / 2.0)));
        } while (Math.abs(phiT2 - phiT1) > significance);

        double lamda_deg = lamda * 180.0 / Math.PI;
        double phi_deg = phiT2 * 180.0 / Math.PI;

        if (llp == null) {
            llp = new LatLonPoint.Double();
        }

        llp.setLocation(lamda_deg, phi_deg);

        return llp;
    } /* end of function worldToLL */

    /*----------------------------------------------------------------------------
     * FUNCTION:     pixelToLL
     * DATE CREATE:  28 February 1995
     * CREATED BY:   David J. Ward
     * DESCRIPTION:  This function converts pixel coordinate into lat, lon
     *               coordinate.
     *--------------------------------------------------------------------------*/
    public Point2D pixelToLL(double xabs, double yabs, Point2D llp) {

        double x = locationCenterXLambert
                + ((xabs - locationCenterXPixel) / locationPixelsPerLambert);
        double y = locationCenterYLambert
                + ((locationCenterYPixel - yabs) / locationPixelsPerLambert);

        worldToLL(x, y, llp);

        return llp;
    } /* end of function pixelToLL */

    protected Point2D plotablePoint = new Point2D.Double();

    /**
     * Determine if the location is plotable on the screen. The Lambert
     * Conformal projection does not lend its self to a simple determination.
     * This method invokes forward to obtain the screen coordinates. If the
     * screen coordinates are visible returns true otherwise returns false.
     * 
     * @param lat latitude in degrees
     * @param lon longitude in degrees
     * @return true is plotable, otherwise false
     */
    public boolean isPlotable(double lat, double lon) {
        // It is almost impossible to determine it the location
        // is plotable without calling forward() for the Point
        // and checking if the point is in bounds.
        // Be lazy and return true.

        if (lat < -55d)
            return false;
        forward(lat, lon, plotablePoint);
        double x = plotablePoint.getX();
        double y = plotablePoint.getY();

        return (x >= 0 && x < this.width && y >= 0 && y < height);
    }

    /**
     * Determine if the location is plotable
     * 
     * @param llpoint location to check
     * @return returns true is plotable, otherwise false
     */
    public boolean isPlotable(LatLonPoint llpoint) {
        return isPlotable(llpoint.getY(), llpoint.getX());
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point2D.
     * <p>
     * 
     * @return Point2D p
     * @param lat latitude
     * @param lon longitude
     * @param p Resulting XY Point2D
     * @param isRadian indicates that lat,lon arguments are in radians
     */
    public Point2D forward(double lat, double lon, Point2D p, boolean isRadian) {

        // Figure out the point for screen coordinates. Need to take
        // into account that the origin point of the projection may be
        // off screen, so we need to take the calculated world
        // coordinates of the center of the screen and subtract the
        // screen offset from that.
        if (isRadian) {
            LLToPixel(ProjMath.radToDeg(lat), ProjMath.radToDeg(lon), p);
        } else {
            LLToPixel(lat, lon, p);
        }
        return p;
    }

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * <p>
     * 
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point)
     */
    public <T extends Point2D> T inverse(double x, double y, T llp) {
        if (llp == null) {
            llp = (T) new LatLonPoint.Float();
        }
        // convert from screen to world coordinates
        pixelToLL(x, y, llp);
        return llp;
    }

    /**
     * Get the upper left (northwest) point of the projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the projection
     * based on the center point and height and width of screen.
     * <p>
     * 
     * @return LatLonPoint
     */
    public LatLonPoint getUpperLeft() {
        // In a conic projection the upper left is meaningless
        // unless at realitively small scales.
        // Return 90.0 -180 until someone fugures out a better way.
        return new LatLonPoint.Double(90.0, -180.0);
    }

    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the projection
     * based on the center point and height and width of screen.
     * <p>
     * 
     * @return LatLonPoint
     */
    public LatLonPoint getLowerRight() {
        // In a conic projection the upper left is meaningless
        // unless at realitively small scales.
        // Return 90.0 -180 until someone fugures out a better way.
        return new LatLonPoint.Double(-90.0, 180.0);
    }

    /**
     * Get the name string of the projection.
     * 
     * @return the projection name
     */
    public String getName() {
        return LambertConformalName;
    }

    /**
     * Forward project a raw array of radian points. This assumes nothing about
     * the array of coordinates. In no way does it assume the points are
     * connected or that the composite figure is to be filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * <p>
     * 
     * @param rawllpts array of lat,lon,... in radians
     * @param rawoff offset into rawllpts
     * @param xcoords x coordinates
     * @param ycoords y coordinates
     * @param visible coordinates visible?
     * @param copyoff offset into x,y,visible arrays
     * @param copylen number of coordinates (coordinate arrays should be at
     *        least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     *         visible.
     */
    public boolean forwardRaw(float[] rawllpts, int rawoff, int[] xcoords,
                              int[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        boolean visibleTotal = false;
        // HACK grabbed from Cylindrical. Might need fixing.
        Point temp = new Point();
        int end = copylen + copyoff;
        for (int i = copyoff, j = rawoff; i < end; i++, j += 2) {
            forward(rawllpts[j], rawllpts[j + 1], temp, true);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;

            visible[i] = (0 <= temp.x && temp.x <= width)
                    && (0 <= temp.y && temp.y <= height);

            if (visible[i] == true && visibleTotal == false) {
                visibleTotal = true;
            }

        }
        // if everything is visible
        return visibleTotal;
    }

    public boolean forwardRaw(double[] rawllpts, int rawoff, int[] xcoords,
                              int[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        boolean visibleTotal = false;
        // HACK grabbed from Cylindrical. Might need fixing.
        Point temp = new Point();
        int end = copylen + copyoff;
        for (int i = copyoff, j = rawoff; i < end; i++, j += 2) {
            forward(rawllpts[j], rawllpts[j + 1], temp, true);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;

            visible[i] = (0 <= temp.x && temp.x <= width)
                    && (0 <= temp.y && temp.y <= height);

            if (visible[i] == true && visibleTotal == false) {
                visibleTotal = true;
            }

        }
        // if everything is visible
        return visibleTotal;
    }

    /**
     * Forward project a lat/lon Poly. Remember to specify vertices in radians!
     * 
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or rhumbline
     *        line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly? this is currently ignored for cylindrical
     *        projections.
     * @return ArrayList<int[]> of x[], y[], x[], y[], ... projected poly
     */
    protected ArrayList<int[]> _forwardPoly(float[] rawllpts, int ltype,
                                            int nsegs, boolean isFilled) {

        int i, j;

        // determine length of pairs
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<int[]>(0);

        // Not concerned with any polygons that are completely below
        // 60S

        float minlat = ProjMath.degToRad(-60f);
        boolean allBelowMinLat = true;
        for (i = 0, j = 0; i < len; i++, j += 2) {
            float l = rawllpts[j + 1];
            while (l < 0f)
                l += Math.PI * 2f;
            if (rawllpts[j] > minlat) {
                allBelowMinLat = false;
            }
        }
        if (allBelowMinLat) {
            return new ArrayList<int[]>(0);
        }

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype))
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);

        Point temp = new Point();
        int[] xs = new int[len];
        int[] ys = new int[len];

        // forward project the points
        for (i = 0, j = 0; i < len; i++, j += 2) {

            forward(rawllpts[j], rawllpts[j + 1], temp, true);
            xs[i] = temp.x;
            ys[i] = temp.y;
        }

        ArrayList<int[]> ret_val = new ArrayList<int[]>(2);
        ret_val.add(xs);
        ret_val.add(ys);

        return ret_val;
    }

    public ArrayList<int[]> _forwardPoly(double[] rawllpts, int ltype,
                                         int nsegs, boolean isFilled) {

        int i, j;

        // determine length of pairs
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<int[]>(0);

        // Not concerned with any polygons that are completely below
        // 60S

        double minlat = ProjMath.degToRad(-60f);
        boolean allBelowMinLat = true;
        for (i = 0, j = 0; i < len; i++, j += 2) {
            double l = rawllpts[j + 1];
            while (l < 0f)
                l += Math.PI * 2f;
            if (rawllpts[j] > minlat) {
                allBelowMinLat = false;
            }
        }
        if (allBelowMinLat) {
            return new ArrayList<int[]>(0);
        }

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype))
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);

        Point temp = new Point();
        int[] xs = new int[len];
        int[] ys = new int[len];

        // forward project the points
        for (i = 0, j = 0; i < len; i++, j += 2) {

            forward(rawllpts[j], rawllpts[j + 1], temp, true);
            xs[i] = temp.x;
            ys[i] = temp.y;
        }

        ArrayList<int[]> ret_val = new ArrayList<int[]>(2);
        ret_val.add(xs);
        ret_val.add(ys);

        return ret_val;
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
    @Override
    public float getScale(Point2D ll1, Point2D ll2, Point2D point1,
                          Point2D point2) {

        // super does not calculate scale correct for projections that does use
        // the same earth radius up north..

        double widthPX = point2.getX() - point1.getX();

        Point2D xx1 = LLToWorld(ll1.getY(),
                ll1.getX(),
                new Point2D.Double());
        Point2D xx2 = LLToWorld(ll2.getY(),
                ll2.getX(),
                new Point2D.Double());

        double widthMap = (xx2.getX() - xx1.getX());
        double widthScale = (((double) getPPM()) * (widthMap / widthPX));

        // TODO: use width-, height- or medium scale?
        // Until then, this isn't needed:

        // double heightPX = point2.y - point1.y;
        // double heightMap = (xx2.getY() - xx1.getY());
        // double heightScale = (((double) getPPM()) * (heightMap / heightPX));

        return (float) widthScale;
    }

    /**
     * Draw the background for the projection.
     * 
     * @param g Graphics2D
     * @param paint java.awt.Paint to use for the background
     */
    public void drawBackground(java.awt.Graphics2D g, java.awt.Paint paint) {
        g.setPaint(paint);
        drawBackground(g);
    }

    /**
     * Draw the background for the projection.
     * 
     * @param g Graphics
     */
    public void drawBackground(java.awt.Graphics g) {
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * test method
     * 
     * @param argv command line parameters
     */
    public static void main(String argv[]) {
        Debug.init();
        Debug.put("Lambert");
        LambertConformal proj = null;

        proj = new LambertConformal(new LatLonPoint.Double(50.679572292f, 5.807370150f), 100000.0f, 620, 480, 4.3569395237f, // centralMeridian
        49.833333109f, // lambert_sp_one
        51.166666321f, // lambert_sp_two
        90.0f, // referenceLatitude
        150000.01f, // falseEasting
        5400088.44f, // falseNorthing
        Ellipsoid.WGS_84);

        Debug.message("Lambert", "(1)" + proj.inverse(310, 240));

        LatLonPoint llp = new LatLonPoint.Double(0.0f, 0.0f);
        Debug.message("Lambert", "(2)"
                + proj.worldToLL(251763.20f, 153034.13f, llp));

        Point2D lp = new Point2D.Double();
        LatLonPoint pt = new LatLonPoint.Double(50.679572292f, 5.807370150f);
        Debug.message("Lambert", "(3)"
                + proj.LLToWorld(pt.getY(), pt.getX(), lp));
    }

}