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
 * See http://www.epsg.org/guides/docs/G7-2.pdf
 * 
 * @author David J. Ward
 * @author Chris van Lith
 * 
 */
public class LambertConformal
        extends GeoProj {

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
    double locationOriginXfPixel = 0.0;
    double locationOriginYfPixel = 0.0;

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

    private transient Ellipsoid ellps = Ellipsoid.WGS_84;

    /**
     * A small number 10^(-10) This number can be re-factored into MoreMath
     */
    final public static double EPS10 = 1.0e-10;

    /**
     * Construct a Lambert projection.
     * <p>
     * 
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    protected LambertConformal(LatLonPoint center, float scale, int width, int height) {
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
    protected LambertConformal(LatLonPoint center, float scale, int width, int height, float centralMeridian, float sp_one,
                               float sp_two, Ellipsoid ellps) {
        this(center, scale, width, height, centralMeridian, sp_one, sp_two, 0f, 0, 0, ellps);
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
    public LambertConformal(LatLonPoint center, float scale, int width, int height, double centralMeridian, double sp_one,
                            double sp_two, double reference_latitude, double falseEasting, double falseNorthing, Ellipsoid ellps) {

        super(center, scale, width, height);

        if (Math.abs(sp_one + sp_two) < EPS10) {
            // can not form a corn.
            throw new RuntimeException("Unable to create Lambert Cornic");
        }
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

        double phi1 = ProjMath.degToRad(lambert_sp_one);
        double phi2 = ProjMath.degToRad(lambert_sp_two);
        double phif = ProjMath.degToRad(referenceLatitude);
        double e = ellps.ecc;
        double sinphi;

        sinphi = Math.sin(phi1);
        double m1 = lambMsfn(sinphi, Math.cos(phi1), e);
        double t1 = lambTsfn(phi1, sinphi, e);

        if (MoreMath.approximately_equal(phi1, phi2, EPS10)) {
            n = sinphi;
        } else {
            sinphi = Math.sin(phi2);
            double m2 = lambMsfn(sinphi, Math.cos(phi2), e);
            double t2 = lambTsfn(phi2, sinphi, e);
            n = Math.log(m1 / m2) / Math.log(t1 / t2);
        }

        F = m1 / (n * Math.pow(t1, n));

        if (MoreMath.approximately_equal(phi1, phi2, EPS10)) {
            rf = 0.0d;
        } else {
            rf = ellps.radius * F * Math.pow(lambTsfn(phif, Math.sin(phif), e), n);
        }

        lamdaf = ProjMath.degToRad(centralMeridian);

        locationCenterXPixel = ((double) getWidth() / 2d);
        locationCenterYPixel = ((double) getHeight() / 2d);

        locationPixelsPerLambert = (double) getPPM() / getScale();

        LatLonPoint origin = new LatLonPoint.Double(referenceLatitude, centralMeridian);
        Point2D lp = LLToWorld(origin.getY(), origin.getX(), new Point2D.Double());
        locationOriginX = lp.getX();
        locationOriginY = lp.getY();

        LatLonPoint center = getCenter();
        lp = LLToWorld(center.getY(), center.getX(), lp);
        locationCenterXLambert = lp.getX();
        locationCenterYLambert = lp.getY();

        // calculate un-truncated true origin pixel value, refer to method
        // worldToPixel.
        locationOriginXfPixel = locationCenterXPixel + (locationOriginX - locationCenterXLambert) * locationPixelsPerLambert;
        locationOriginYfPixel = locationCenterYPixel - (locationOriginY - locationCenterYLambert) * locationPixelsPerLambert;

        if (Debug.debugging("Lambert")) {
            Debug.output("Creating LambertConformal: center x = " + locationCenterXLambert + ", center y = "
                    + locationCenterYLambert);
            Debug.output("Creating LambertConformal: origin x = " + locationOriginX + ", origin y = " + locationOriginY);
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
     * <li><code>pan(180, c)</code> pan south
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
            // double newLat = inverse(width / 2, height).getY(); //tLatitude();
            // setCenter(newLat, getCenter().getY()); //Longitude());
        } else if (MoreMath.approximately_equal(Az, -135f, 0.01f)) {
            setCenter(inverse(0, height));// southwest
        } else if (MoreMath.approximately_equal(Az, -90f, 0.01f)) {
            setCenter(inverse(0, height / 2));// west
            // double newCtrLon = inverse(0, (n > 0 ? 0 : height)).getX();
            // setCenter(getCenter().getY(), newCtrLon);
        } else if (MoreMath.approximately_equal(Az, -45f, 0.01f)) {
            setCenter(inverse(0, 0));// northwest
        } else if (MoreMath.approximately_equal(Az, 0f, 0.01f)) {
            setCenter(inverse(width / 2, 0));// north
            // double newCtrLat = inverse(width / 2, (n>0 ? 0 : height)).getY();
            // setCenter(newCtrLat, getCenter().getX());
        } else if (MoreMath.approximately_equal(Az, 45f, 0.01f)) {
            setCenter(inverse(width, 0));// northeast
        } else if (MoreMath.approximately_equal(Az, 90f, 0.01f)) {
            setCenter(inverse(width, height / 2));// east
            // double newCtrLon = inverse(width, (n>0 ? 0 : height)).getX();
            // setCenter(getCenter().getY(), newCtrLon);
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
     * Modified By:  steve C. Tang,  02/20/2010
     * DESCRIPTION:  This function converts lat, lon coordinate to lambert
     *               coordinate.
     *--------------------------------------------------------------------------*/
    public Point2D LLToWorld(double lat, double lon, Point2D lp) {
        if (lp == null) {
            lp = new Point2D.Double();
        }

        LLToWorldReturningLon(lat, lon, lp);
        return lp;
    }

    /**
     * LLToWorld that returns normalized longitude in radians, to be used for
     * more calculations in some methods. Do not provide a NULL lp here if you
     * want the world coordinates provided back to you.
     * 
     * @param lat latitude in degrees
     * @param lon longitude in degrees.
     * @param lp world coordinates are provided back in this object, make sure
     *        it's not null if you care about them.
     * @return normalized longitude in radians, radians lon - radians lat.
     */
    protected double LLToWorldReturningLon(double lat, double lon, Point2D lp) {

        double phi_deg = lat;
        double phi = ProjMath.degToRad(phi_deg);
        double lamba_deg = lon;
        double lamba = ProjMath.degToRad(lamba_deg);

        // normalized longitude
        double dlamda = lamba - lamdaf;
        if (dlamda > Math.PI) {
            dlamda -= MoreMath.TWO_PI_D;
        } else if (dlamda < -Math.PI) {
            dlamda += MoreMath.TWO_PI_D;
        }

        double e = ellps.ecc;
        double r = 0.0d;
        if (!MoreMath.approximately_equal(Math.abs(phi), MoreMath.HALF_PI, EPS10)) {
            double t = lambTsfn(phi, Math.sin(phi), e);
            r = ellps.radius * F * Math.pow(t, n);
        }
        double theta = n * dlamda;

        double easting = falseEasting + r * Math.sin(theta - alpha);
        double northing = falseNorthing + rf - r * Math.cos(theta - alpha);

        lp.setLocation(easting, northing);

        return dlamda;
    } /* end of function LLToWorld */

    /*----------------------------------------------------------------------------
     * FUNCTION:     lat_lon_to_pixel
     * DATE CREATE:  28 February 1995
     * CREATED BY:   David J. Ward
     * Modified by:  Steve C. Tang,  02/20/2010
     * DESCRIPTION:  This function converts lat, lon coordinate to pixel
     *               coordinate.
     *--------------------------------------------------------------------------*/

    public double LLToPixel(double lat, double lon, Point2D p) {

        double dlamda;

        Point2D lp = new Point2D.Double();
        dlamda = LLToWorldReturningLon(lat, lon, lp);

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

        // In Openmap 4.6.5 and before, the float-2-int truncation in
        // World-to-Pixel transform can cause
        // the displayed position on the other side if the projection side line
        // runs across the pixel.
        // For example, a true World coordinate of 678.9 is truncated to (int)
        // 678.9=678. while 678.9
        // can correspond to dlamda=180.1 that means -179.9 on the other side,
        // 678
        // can correspond to
        // dlamda=179.9 during later map projection. We approximate this
        // situation
        // by forcing the swap.
        // Increase PixelsPerLambert and projection size can reduce the error.
        if ((dlamda > 3.1415 && p.getX() < locationOriginXfPixel) || (-dlamda > 3.1415 && p.getX() > locationOriginXfPixel)) {
            dlamda = -dlamda;
        }

        return dlamda;
    } /* end of function LLToPixel */

    //
    public Point worldToPixel(Point2D lp, Point p) {
        double x = locationCenterXPixel + (lp.getX() - locationCenterXLambert) * locationPixelsPerLambert;
        double y = locationCenterYPixel - (lp.getY() - locationCenterYLambert) * locationPixelsPerLambert;
        if (p == null)
            p = new Point();
        p.setLocation((int) x, (int) y);
        return p;
    }

    //
    public Point2D pixelToWorld(Point p, Point2D lp) {
        double x = locationCenterXLambert + (p.getX() - locationCenterXPixel) / locationPixelsPerLambert;
        double y = locationCenterYLambert - (p.getY() - locationCenterYPixel) / locationPixelsPerLambert;
        if (lp == null)
            lp = new Point2D.Double();
        lp.setLocation(x, y);
        return lp;
    }

    /*----------------------------------------------------------------------------
     * FUNCTION:     worldToLL
     * DATE CREATE:  13-4-2006
     * CREATED BY:   Chris van Lith
     * Modified By:  Steve C. Tang,   02/20/2010
     * DESCRIPTION:  This function converts lambert projections to lat, lon
     *               coordinate.  It is given the lambert projections as the
     *               input, and the lat, lon coordinate as the output.
     *--------------------------------------------------------------------------*/
    public Point2D worldToLL(double x, double y, Point2D llp) {

        x -= falseEasting;
        y = rf - (y - falseNorthing);

        double rR = Math.sqrt(x * x + y * y);
        if (rR < EPS10) {
            llp.setLocation(0.0, n > 0.0 ? 90.0 : -90.0);
            return llp;
        }

        if (n < 0.0) {
            rR = -rR;
            x = -x;
            y = -y;
        }

        double tR = Math.pow(rR / (ellps.radius * F), 1 / n);
        double halfe = 0.5 * ellps.ecc;

        double phiT1 = 0.0;
        double phiT2 = MoreMath.HALF_PI - 2 * Math.atan(tR);
        int iIter = 0, nIter = 10;
        double halfesinphi;
        do {
            phiT1 = phiT2;
            halfesinphi = halfe * Math.sin(phiT1);
            phiT2 = MoreMath.HALF_PI - 2 * Math.atan(tR * Math.pow((0.5 - halfesinphi) / (0.5 + halfesinphi), halfe));
        } while ((Math.abs(phiT2 - phiT1) > EPS10) && (iIter++ < nIter));

        double lamda = ((Math.atan2(x, y) + alpha) / n) + lamdaf;

        double lamda_deg = ProjMath.radToDeg(lamda);
        double phi_deg = ProjMath.radToDeg(phiT2);

        if (llp == null) {
            llp = new LatLonPoint.Double();
        }

        // the LatLonPoint.setLocation(double Lon, double Lat) method specifies
        // Lat-lon in reversed order compared to other methods such as
        // LatLonPoint.setLocation(double Lat, double lon, boolean isRadian).
        // This is very confusing.
        llp.setLocation(lamda_deg, phi_deg); // Lon, Lat

        return llp;
    } /* end of function worldToLL */

    /*----------------------------------------------------------------------------
     * FUNCTION:     pixelToLL
     * DATE CREATE:  28 February 1995
     * CREATED BY:   David J. Ward
     * Modified By:  Steve C. Tang   02/20/2010
     * DESCRIPTION:  This function converts pixel coordinate into lat, lon
     *               coordinate.
     *--------------------------------------------------------------------------*/
    public Point2D pixelToLL(double xabs, double yabs, Point2D llp) {

        double x = locationCenterXLambert + ((xabs - locationCenterXPixel) / locationPixelsPerLambert);
        double y = locationCenterYLambert + ((locationCenterYPixel - yabs) / locationPixelsPerLambert);

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

        if (lat < -70d)
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
     * Forward projects lat,lon into XY space and sets the results in the p
     * provided.
     * <p>
     * 
     * @return Point2D p
     * @param lat latitude
     * @param lon longitude
     * @param p Resulting XY Point2D
     * @param isRadian indicates that lat,lon arguments are in radians
     */
    public Point2D forward(double lat, double lon, Point2D p, boolean isRadian) {
        if (p == null) {
            p = new Point2D.Double();
        }
        _forward(lat, lon, p, isRadian);
        return p;
    }

    protected double _forward(double lat, double lon, Point2D p, boolean isRadian) {

        // Figure out the point for screen coordinates. Need to take
        // into account that the origin point of the projection may be
        // off screen, so we need to take the calculated world
        // coordinates of the center of the screen and subtract the
        // screen offset from that.
        if (isRadian) {
            return LLToPixel(ProjMath.radToDeg(lat), ProjMath.radToDeg(lon), p);
        } else {
            return LLToPixel(lat, lon, p);
        }
    }

    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * <p>
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
        // unless at relatively small scales.
        // Return 90.0 -180 until someone figures out a better way.
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
        // unless at relatively small scales.
        // Return 90.0 -180 until someone figures out a better way.
        return new LatLonPoint.Double(-90.0, 180.0);
    }

    public double getReferenceLon() {
        return centralMeridian;
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
    public boolean forwardRaw(float[] rawllpts, int rawoff, float[] xcoords, float[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {

        double[] drawllpts = new double[rawllpts.length];
        System.arraycopy(drawllpts, 0, rawllpts, 0, rawllpts.length);

        return forwardRaw(drawllpts, rawoff, xcoords, ycoords, visible, copyoff, copylen);
    }

    public boolean forwardRaw(double[] rawllpts, int rawoff, float[] xcoords, float[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        boolean visibleTotal = false;
        // HACK grabbed from Cylindrical. Might need fixing.
        Point temp = new Point();
        int end = copylen + copyoff;
        for (int i = copyoff, j = rawoff; i < end; i++, j += 2) {
            forward(rawllpts[j], rawllpts[j + 1], temp, true);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;

            visible[i] = (0 <= temp.x && temp.x <= width) && (0 <= temp.y && temp.y <= height);

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
     * @return ArrayList<float[]> of x[], y[], x[], y[], ... projected poly
     */
    protected ArrayList<float[]> _forwardPoly(float[] rawllpts, int ltype, int nsegs, boolean isFilled) {

        double[] drawllpts = new double[rawllpts.length];
        System.arraycopy(drawllpts, 0, rawllpts, 0, rawllpts.length);

        return _forwardPoly(drawllpts, ltype, nsegs, isFilled);
    }

    public ArrayList<float[]> _forwardPoly(double[] rawllpts, int ltype, int nsegs, boolean isFilled) {

        int i, j, k;

        // determine length of pairs
        int len = rawllpts.length >> 1; // len/2, chop off extra
        if (len < 2)
            return new ArrayList<float[]>(0);

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
            return new ArrayList<float[]>(0);
        }

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype))
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);

        Point temp = new Point();
        int[] xa = new int[len];
        float[] xs = new float[len];
        float[] ys = new float[len];
        double dlamda1, dlamda2;

        // forward project the points
        k = 0;
        xa[k] = 0;
        i = 0;
        dlamda1 = _forward(rawllpts[i], rawllpts[i + 1], temp, true);
        xs[i] = temp.x;
        ys[i] = temp.y;
        for (i = 1, j = 2; i < len; i++, j += 2) {
            dlamda2 = _forward(rawllpts[j], rawllpts[j + 1], temp, true);
            if (Math.abs(dlamda2 - dlamda1) >= Math.PI) {
                xa[++k] = i;
            }
            xs[i] = temp.x;
            ys[i] = temp.y;
            dlamda1 = dlamda2;
        }

        if (xa[k] < len)
            xa[++k] = len;

        ArrayList<float[]> ret_val = new ArrayList<float[]>(2);

        for (i = 0; i < k; i++) {
            len = xa[i + 1] - xa[i];
            if (len > 0) {
                float[] xf = new float[len];
                float[] yf = new float[len];
                for (j = 0; j < len; j++) {
                    xf[j] = xs[j + xa[i]];
                    yf[j] = ys[j + xa[i]];
                }
                if (i > 0 && i == k - 1 && (xs[0] == xs[xs.length - 1] && ys[0] == ys[ys.length - 1])) {
                    int len0 = ((float[]) ret_val.get(0)).length;
                    float[] x0 = new float[len0 + len];
                    float[] y0 = new float[len0 + len];
                    System.arraycopy(xf, 0, x0, 0, len);
                    System.arraycopy(yf, 0, y0, 0, len);
                    System.arraycopy(((float[]) ret_val.get(0)), 0, x0, len, len0);
                    System.arraycopy(((float[]) ret_val.get(1)), 0, y0, len, len0);
                    ret_val.set(0, x0);
                    ret_val.set(1, y0);
                } else {
                    ret_val.add(xf);
                    ret_val.add(yf);
                }
            }
        }

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
    public float getScale(Point2D ll1, Point2D ll2, Point2D point1, Point2D point2) {

        // super does not calculate scale correct for projections that does use
        // the same earth radius up north..

        double widthPX = point2.getX() - point1.getX();

        Point2D xx1 = LLToWorld(ll1.getY(), ll1.getX(), new Point2D.Double());
        Point2D xx2 = LLToWorld(ll2.getY(), ll2.getX(), new Point2D.Double());

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
     * Special function m = cos(phi) / sqrt(1.0 - (e*sin(phi))**2)
     * 
     * @param sinphi double
     * @param cosphi double
     * @param e double
     * @return double
     */
    public static double lambMsfn(double sinphi, double cosphi, double e) {
        sinphi *= e;
        return cosphi / Math.sqrt(1.d - sinphi * sinphi);
    }

    /**
     * Special function t = tan(PI/4-phi/2) / pow((1-sinphi)/(1+sinphi), .5*e)
     * 
     * @param phi double
     * @param sinphi double
     * @param e double
     * @return double
     */
    public static double lambTsfn(double phi, double sinphi, double e) {
        sinphi *= e;
        return Math.tan((MoreMath.HALF_PI - phi) * 0.5d) * Math.pow((1.d + sinphi) / (1.d - sinphi), 0.5d * e);
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
                                    -49.833333109f, // lambert_sp_one
                                    -51.166666321f, // lambert_sp_two
                                    90.0f, // referenceLatitude
                                    150000.01f, // falseEasting
                                    5400088.44f, // falseNorthing
                                    Ellipsoid.WGS_84);

        Debug.message("Lambert", "(1)" + proj.inverse(310, 240));

        LatLonPoint llp = new LatLonPoint.Double(0.0f, 0.0f);
        Debug.message("Lambert", "(2)" + proj.worldToLL(251763.20f, 153034.13f, llp));

        LatLonPoint pt = new LatLonPoint.Double(50.679572292f, 5.807370150f);
        Point2D lp = proj.LLToWorld(pt.getY(), pt.getX(), new Point2D.Double());
        Debug.message("Lambert", "(3)" + lp);
    }

}
