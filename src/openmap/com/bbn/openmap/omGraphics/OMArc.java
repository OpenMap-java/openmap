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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMArc.java,v $
// $RCSfile: OMArc.java,v $
// $Revision: 1.13 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.proj.Cylindrical;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * Graphic object that represents an arc.
 * <p>
 * <h3>NOTE:</h3>
 * See the <a href="com.bbn.openmap.proj.Projection.html#poly_restrictions">
 * RESTRICTIONS </a> on Lat/Lon polygons/polylines which apply to arcs as well.
 * Not following the guidelines listed may result in ambiguous/undefined shapes!
 * Similar assumptions apply to the other vector graphics that we define: polys,
 * rects, lines.
 * <p>
 * These assumptions are virtually the same as those on the more generic OMPoly
 * graphic type.
 * <p>
 * 
 * @see OMPoly
 */
public class OMArc extends OMGraphicAdapter implements OMGraphic {

    /** Horizontal pixel location of the center. */
    protected double x1 = 0;
    /** Vertical pixel location of the center. */
    protected double y1 = 0;

    /** Horizontal pixel offset. */
    protected double off_x = 0;
    /** Vertical pixel offset. */
    protected double off_y = 0;
    /**
     * Center point.
     */
    protected Point2D center;
    /**
     * Radius of arc in radians. For LATLON arc. Note that the methods for this
     * class use Decimal Degrees, or ask for a Length object to use for units.
     * The radius is converted to radians for internal use.
     */
    protected double radius = 0.0f;
    /**
     * The pixel horizontal diameter of the arc. For XY and OFFSET arcs.
     */
    protected double width = 0;
    /**
     * The pixel vertical diameter of the arc. For XY and OFFSET arcs.
     */
    protected double height = 0;

    /**
     * The starting angle of the arc in decimal degrees. This is defined in
     * decimal degrees because the java.awt.geom.Arc object wants it in decimal
     * degrees. 0 is North?
     */
    protected double start = 0.0f;

    /**
     * The angular extent of the arc in decimal degrees.
     */
    protected double extent = 360.0f;

    /**
     * For Arcs, how the arc should be closed when rendered. Arc2D.OPEN is the
     * default, Arc2D.PIE and Arc2D.CHORD are options.
     * 
     * @see java.awt.geom.Arc2D
     */
    protected int arcType = Arc2D.OPEN;

    /**
     * Used to render arc in Cylindrical projections when the arc encompasses a
     * pole.
     */
    private transient GeneralPath polarShapeLine = null;
    /**
     * Indicates that the polarShapeLine should be used for rendering.
     */
    private transient boolean correctFill = false;
    /** Force the correct polar hack. */
    private transient boolean correctPolar = false;

    /**
     * Number of vertices to draw for lat/lon poly-arcs.
     */
    protected int nverts;

    /**
     * The angle by which the circle/ellipse is to be rotated, in radians
     */
    protected double rotationAngle = DEFAULT_ROTATIONANGLE;

    /**
     * The simplest constructor for an OMArc, and it expects that all fields
     * will be filled in later. Rendertype is RENDERTYPE_UNKNOWN.
     */
    public OMArc() {
        super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
    }

    /**
     * Create an OMArc, positioned with a lat-lon center and x-y axis.
     * Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param w horizontal diameter of arc, pixels
     * @param h vertical diameter of arc, pixels
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees
     */
    public OMArc(double latPoint, double lonPoint, int w, int h, double s, double e) {
        this(latPoint, lonPoint, 0, 0, w, h, s, e);
    }

    /**
     * Create a OMArc, positioned with a x-y center with x-y axis. Rendertype is
     * RENDERTYPE_XY.
     * 
     * @param x1 window position of center point from left of window, in pixels
     * @param y1 window position of center point from top of window, in pixels
     * @param w horizontal diameter of arc, pixels
     * @param h vertical diameter of arc, pixels
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees. For XY rendertype arcs,
     *        positive extents go in the counter-clockwise direction, matching
     *        the java.awt.geom.Arc2D convention.
     */
    public OMArc(int x1, int y1, int w, int h, double s, double e) {
        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);

        this.x1 = x1;
        this.y1 = y1;
        width = w;
        height = h;
        start = s;
        extent = e;
    }

    /**
     * Create a OMArc, positioned at a Lat-lon location, x-y offset, x-y axis.
     * Rendertype is RENDERTYPE_OFFSET.
     * 
     * @param latPoint latitude of center of arc.
     * @param lonPoint longitude of center of arc.
     * @param offset_x1 # pixels to the right the center will be moved from
     *        lonPoint.
     * @param offset_y1 # pixels down that the center will be moved from
     *        latPoint.
     * @param w horizontal diameter of arc, pixels.
     * @param h vertical diameter of arc, pixels.
     * @param s starting angle of arc, decimal degrees.
     * @param e angular extent of arc, decimal degrees. For Offset rendertype
     *        arcs, positive extents go in the counter-clockwise direction,
     *        matching the java.awt.geom.Arc2D convention.
     */
    public OMArc(double latPoint, double lonPoint, int offset_x1, int offset_y1, int w, int h,
            double s, double e) {
        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);

        center = new LatLonPoint.Double(latPoint, lonPoint);
        off_x = offset_x1;
        off_y = offset_y1;
        width = w;
        height = h;
        start = s;
        extent = e;
    }

    /**
     * Creates an OMArc with a Lat-lon center and a lat-lon axis. Rendertype is
     * RENDERTYPE_LATLON.
     * 
     * @param latPoint latitude of center point, decimal degrees
     * @param lonPoint longitude of center point, decimal degrees
     * @param radius distance in decimal degrees (converted to radians
     *        internally).
     * @param s starting angle of arc, decimal degrees
     * @param e angular extent of arc, decimal degrees. For LATLON rendertype
     *        arcs, positive extents go in the clockwise direction, matching the
     *        OpenMap convention in coordinate space.
     */
    public OMArc(double latPoint, double lonPoint, double radius, double s, double e) {

        this(new LatLonPoint.Double(latPoint, lonPoint), radius, Length.DECIMAL_DEGREE, -1, s, e);
    }

    /**
     * Create an OMArc with a lat/lon center and a physical distance radius.
     * Rendertype is RENDERTYPE_LATLON.
     * 
     * @param latPoint latitude of center of arc in decimal degrees
     * @param lonPoint longitude of center of arc in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object.
     * @param s starting angle of arc, decimal degrees.
     * @param e angular extent of arc, decimal degrees. For LATLON rendertype
     *        arcs, positive extents go in the clockwise direction, matching the
     *        OpenMap convention in coordinate space.
     */
    public OMArc(double latPoint, double lonPoint, double radius, Length units, double s, double e) {
        this(new LatLonPoint.Double(latPoint, lonPoint), radius, units, -1, s, e);
    }

    /**
     * Create an OMArc with a lat/lon center and a physical distance radius.
     * Rendertype is RENDERTYPE_LATLON.
     * 
     * @param latPoint latitude of center of arc in decimal degrees
     * @param lonPoint longitude of center of arc in decimal degrees
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying units.
     * @param nverts number of vertices for the poly-arc (if &lt; 3, value is
     *        generated internally)
     * @param s starting angle of arc, decimal degrees.
     * @param e angular extent of arc, decimal degrees. For LATLON rendertype
     *        arcs, positive extents go in the clockwise direction, matching the
     *        OpenMap convention in coordinate space.
     */
    public OMArc(double latPoint, double lonPoint, double radius, Length units, int nverts,
            double s, double e) {
        this(new LatLonPoint.Double(latPoint, lonPoint), radius, units, nverts, s, e);
    }

    /**
     * Create an OMArc with a lat/lon center and a physical distance radius.
     * Rendertype is RENDERTYPE_LATLON.
     * 
     * @param center LatLon center of arc
     * @param radius distance
     * @param units com.bbn.openmap.proj.Length object specifying units for
     *        distance.
     * @param nverts number of vertices for the poly-arc(if &lt; 3, value is
     *        generated internally)
     * @param s starting angle of arc, decimal degrees.
     * @param e angular extent of arc, decimal degrees. For LATLON rendertype
     *        arcs, positive extents go in the clockwise direction, matching the
     *        OpenMap convention in coordinate space.
     */
    public OMArc(LatLonPoint center, double radius, Length units, int nverts, double s, double e) {

        super(RENDERTYPE_LATLON, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        this.radius = units.toRadians(radius);
        this.center = center;
        this.nverts = nverts;
        this.start = s;
        this.extent = e;
    }

    /**
     * Get the x position of the center. This is always meaningful only if the
     * render type is RENDERTYPE_XY or RENDERTYPE_OFFSET, and meaningful after
     * generation if the RENDERTYPE_LATLON.
     * 
     * @return x position of center.
     */
    public int getX() {
        return (int) x1;
    }

    /**
     * Get the y position of the center. This is always meaningful only if the
     * render type is RENDERTYPE_XY or RENDERTYPE_OFFSET, and meaningful after
     * generation if the RENDERTYPE_LATLON.
     * 
     * @return y position of center.
     */
    public int getY() {
        return (int) y1;
    }

    /**
     * Get the x offset from the center. This is meaningful only if the render
     * type is RENDERTYPE_OFFSET.
     * 
     * @return x offset from center.
     */
    public int getOffX() {
        return (int) off_x;
    }

    /**
     * Get the y position of the center. This is meaningful only if the render
     * type is RENDERTYPE_OFFSET.
     * 
     * @return y offset from center.
     */
    public int getOffY() {
        return (int) off_y;
    }

    /**
     * Get the center LatLonPoint. This is meaningful only if the rendertype is
     * RENDERTYPE_LATLON or RENDERTYPE_OFFSET.
     * 
     * @return LatLonPoint position of center.
     */
    public LatLonPoint getLatLon() {
        return LatLonPoint.getDouble(center);
    }

    /**
     * Get the radius. This is meaningful only if the render type is
     * RENDERTYPE_LATLON.
     * 
     * @return double radius in decimal degrees
     */
    public double getRadius() {
        return Length.DECIMAL_DEGREE.fromRadians(radius);
    }

    /**
     * Get the horizontal pixel diameter of the arc. This is meaningful only if
     * the render type is RENDERTYPE_XY or RENDERTYPE_OFFSET.
     * 
     * @return the horizontal pixel diameter of the arc.
     */
    public int getWidth() {
        return (int) width;
    }

    /**
     * Get the vertical pixel diameter of the arc. This is meaningful only if
     * the render type is RENDERTYPE_XY or RENDERTYPE_OFFSET.
     * 
     * @return the vertical pixel diameter of the arc.
     */
    public int getHeight() {
        return (int) height;
    }

    /**
     * Get the starting angle of the arc.
     * 
     * @return the starting angle of the arc in decimal degrees.
     */
    public double getStartAngle() {
        return start;
    }

    /**
     * Get the extent angle of the arc.
     * 
     * @return the angular extent of the arc in decimal degrees. For LATLON
     *         rendertype arcs, positive extents go in the clockwise direction,
     *         matching the OpenMap convention in coordinate space. For XY and
     *         OFFSET rendertype arcs, positive extents go in the clockwise
     *         direction, matching the java.awt.geom.Arc2D convention.
     */
    public double getExtentAngle() {
        return extent;
    }

    /**
     * Get the number of vertices of the lat/lon arc. This will be meaningful
     * only if the render type is RENDERTYPE_XY or RENDERTYPE_OFFSET and for
     * LINETYPE_GREATARC or LINETYPE_RHUMB line types.
     * 
     * @return int number of segment points
     */
    public int getNumVerts() {
        return nverts;
    }

    /**
     * Set the x position of the center. This will be meaningful only if the
     * render type is RENDERTYPE_XY.
     * 
     * @param value the x position of center.
     */
    public void setX(int value) {
        if (((int) x1) == value)
            return;
        x1 = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the y position of the center. This will be meaningful only if the
     * render type is RENDERTYPE_XY.
     * 
     * @param value the y position of center.
     */
    public void setY(int value) {
        if (((int) y1) == value)
            return;
        y1 = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the x offset from the center. This will be meaningful only if the
     * render type is RENDERTYPE_OFFSET.
     * 
     * @param value the x position of center.
     */
    public void setOffX(int value) {
        if (off_x == value)
            return;
        off_x = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the y offset from the center. This will be meaningful only if the
     * render type is RENDERTYPE_OFFSET.
     * 
     * @param value the y position of center.
     */
    public void setOffY(int value) {
        if (off_y == value)
            return;
        off_y = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the latitude and longitude of the center point. This is meaningful
     * only if the rendertype is RENDERTYPE_LATLON or RENDERTYPE_OFFSET.
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public void setLatLon(double lat, double lon) {
        setCenter(new LatLonPoint.Double(lat, lon));
    }

    /**
     * Set the latitude and longitude of the center point. This is meaningful
     * only if the rendertype is RENDERTYPE_LATLON or RENDERTYPE_OFFSET.
     * 
     * @param p LatLonPoint of center.
     */
    public void setCenter(LatLonPoint p) {
        if (p.equals(center))
            return;
        center = p;
        setNeedToRegenerate(true);
    }

    /**
     * Get the center Point.
     */
    public Point2D getCenter() {
        return center;
    }

    /**
     * Set the radius. This is meaningful only if the render type is
     * RENDERTYPE_LATLON. Note that while the radius is specified as decimal
     * degrees, it only means the distance along the ground that that number of
     * degrees represents at the equator, *NOT* a radius of a number of degrees
     * around a certain location. There is a difference.
     * 
     * @param radius float radius in decimal degrees
     */
    public void setRadius(double radius) {
        this.radius = Length.DECIMAL_DEGREE.toRadians(radius);
        setNeedToRegenerate(true);
    }

    /**
     * Set the radius with units. This is meaningful only if the render type is
     * RENDERTYPE_LATLON.
     * 
     * @param radius float radius
     * @param units Length specifying unit type.
     */
    public void setRadius(double radius, Length units) {
        this.radius = units.toRadians(radius);
        setNeedToRegenerate(true);
    }

    /**
     * Set the horizontal pixel diameter of the arc. This is meaningful only if
     * the render type is RENDERTYPE_XY or RENDERTYPE_OFFSET.
     * 
     * @param value the horizontal pixel diameter of the arc.
     */
    public void setWidth(int value) {
        if (width == value)
            return;
        width = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the vertical pixel diameter of the arc. This is meaningful only if
     * the render type is RENDERTYPE_XY or RENDERTYPE_OFFSET.
     * 
     * @param value the vertical pixel diameter of the arc.
     */
    public void setHeight(int value) {
        if (height == value)
            return;
        height = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the starting angle the arc.
     * 
     * @param value the starting angle of the arc in decimal degrees.
     */
    public void setStart(double value) {
        if (start == value)
            return;
        start = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the angular extent of the arc.
     * 
     * @param value the angular extent of the arc in decimal degrees. For LATLON
     *        rendertype arcs, positive extents go in the clockwise direction,
     *        matching the OpenMap convention in coordinate space. For XY and
     *        OFFSET rendertype arcs, positive extents go in the clockwise
     *        direction, matching the java.awt.geom.Arc2D convention.
     */
    public void setExtent(double value) {
        if (extent == value)
            return;
        extent = value;
        setNeedToRegenerate(true);
    }

    /**
     * Set the number of vertices of the lat/lon arc. This is meaningful only if
     * the render type is RENDERTYPE_LATLON and for LINETYPE_GREATARC or
     * LINETYPE_RHUMB line types. If &lt; 1, this value is generated internally.
     * 
     * @param nverts number of segment points
     */
    public void setNumVerts(int nverts) {
        this.nverts = nverts;
    }

    /**
     * Set the ArcType, either Arc2D.OPEN (default), Arc2D.PIE or Arc2D.CHORD.
     * 
     * @see java.awt.geom.Arc2D
     */
    public void setArcType(int type) {
        if (type == Arc2D.PIE || type == Arc2D.CHORD) {
            arcType = type;
        } else {
            arcType = Arc2D.OPEN;
        }
    }

    /**
     * Get the ArcType.
     * 
     * @see java.awt.geom.Arc2D
     */
    public int getArcType() {
        return arcType;
    }

    /**
     * Set the angle by which the arc is to rotated.
     * 
     * @param angle the number of radians the arc is to be rotated. Measured
     *        clockwise from horizontal. Positive numbers move the positive x
     *        axis toward the positive y axis.
     */
    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
        setNeedToRegenerate(true);
    }

    /**
     * Get the current rotation of the arc.
     * 
     * @return the arc rotation.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setNeedToRegenerate(boolean value) {
        super.setNeedToRegenerate(value);
        if (value) {
            polarShapeLine = null;
            correctFill = false;
        }
    }

    /**
     * Get the polar-fill-correction-flag.
     * 
     * @return boolean
     * @see #setPolarCorrection
     */
    public boolean getPolarCorrection() {
        return correctPolar;
    }

    /**
     * Set the polar-fill-correction-flag. We don't correctly render *filled*
     * arcs/polygons which encompass a pole in Cylindrical projections. This
     * method will toggle support for correcting this problem. You should only
     * set this on arcs that encompass a pole and are drawn with a fill color.
     * You do not need to set this if you're only drawing the arc outline.
     * 
     * @param value boolean
     * @see OMGraphic#setLinePaint
     * @see OMGraphic#setFillPaint
     */
    public void setPolarCorrection(boolean value) {
        correctPolar = value;
        setNeedToRegenerate(true);
    }

    /**
     * Helper function that helps the generate method figure out if the center
     * point should be in the generate shape - if it's not, the code knows that
     * there is a problem with the poles, and the polar correction code needs to
     * be run.
     */
    protected boolean shouldCenterBeInShape() {
        // It won't be for CHORD or OPEN arcs
        return arcType == Arc2D.PIE;
    }

    /**
     * Prepare the arc for rendering.
     * 
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {
        polarShapeLine = null;
        correctFill = false;

        setNeedToRegenerate(true);

        if (proj == null) {
            Debug.message("omgraphic", "OMArc: null projection in generate!");
            return false;
        }

        GeneralPath projectedShape = null;

        switch (renderType) {
        case RENDERTYPE_OFFSET:
            if (!proj.isPlotable(center)) {
                setNeedToRegenerate(true);// HMMM not the best flag
                return false;
            }

            Point2D p1 = proj.forward(center, new Point2D.Double());

            x1 = p1.getX() + off_x;
            y1 = p1.getY() + off_y;
            // Fall through...
        case RENDERTYPE_XY:
            double x = x1 - width / 2d;
            double y = y1 - height / 2d;

            Shape arcShape = createArcShape(x, y, width, height);

            AffineTransform af = null;
            if (rotationAngle != DEFAULT_ROTATIONANGLE) {
                af = new AffineTransform();
                af.rotate(rotationAngle, x1, y1);
            }
            PathIterator pi = arcShape.getPathIterator(af);
            projectedShape = new GeneralPath();
            projectedShape.append(pi, false);

            break;

        case RENDERTYPE_LATLON:

            GeneralPath specialCaseShape = null;

            if (proj instanceof GeoProj) {

                LatLonPoint llCenter = LatLonPoint.getDouble(center);

                Point2D p = proj.forward(llCenter.getY(), llCenter.getX(), new Point2D.Double());

                x1 = p.getX();
                y1 = p.getY();

                ArrayList<float[]> coordLists = getCoordLists(((GeoProj) proj), llCenter, radius, nverts);
                int size = coordLists.size();

                for (int i = 0; i < size; i += 2) {
                    float[] xpoints = (float[]) coordLists.get(i);
                    float[] ypoints = (float[]) coordLists.get(i + 1);

                    GeneralPath gp = createShape(xpoints, ypoints, (arcType != Arc2D.OPEN || (arcType == Arc2D.OPEN && !isClear(fillPaint))));

                    projectedShape = appendShapeEdge(projectedShape, gp, false);

                    correctFill = proj instanceof Cylindrical
                            && ((shouldCenterBeInShape() && projectedShape != null && !projectedShape.contains(x1, y1)) || correctPolar);

                    if (correctFill) {
                        float[][] alts = doPolarFillCorrection(xpoints, ypoints, (llCenter.getRadLat() > 0f) ? -1
                                : proj.getWidth() + 1);

                        int gp2length = alts[0].length - 2;

                        GeneralPath gp1 = createShape(alts[0], alts[1], true);
                        GeneralPath gp2 = createShape(alts[0], alts[1], 0, gp2length, false);

                        if (specialCaseShape == null || polarShapeLine == null) {
                            specialCaseShape = gp1;
                            polarShapeLine = gp2;
                        } else {
                            specialCaseShape.append(gp1, false);
                            polarShapeLine.append(gp2, false);
                        }
                    }
                }

            } else {
                double degRadius = Math.toDegrees(radius);
                // Create shape for non-GeoProj in lat/lon space...
                specialCaseShape = new GeneralPath(proj.forwardShape(new Arc2D.Double(center.getX()
                        - degRadius, center.getY() - degRadius, 2 * degRadius, 2 * degRadius, start, extent, arcType)));
            }

            if (specialCaseShape != null) {
                projectedShape = specialCaseShape;
            }

            break;
        case RENDERTYPE_UNKNOWN:
            System.err.println("OMArc.generate(): invalid RenderType");
            return false;
        }

        setShape(projectedShape);

        setNeedToRegenerate(false);
        return true;
    }

    /**
     * An internal method designed to fetch the Shape to be used for an XY or
     * OFFSET OMArc. This method is smart enough to take the calculated position
     * information and make a call to Arc2D.Double with start, extent and
     * arcType information.
     */
    protected Shape createArcShape(double x, double y, double fwidth, double fheight) {
        return new Arc2D.Double(x, y, fwidth, fheight, start, extent, arcType);
    }

    /**
     * An internal method designed to fetch the ArrayList for LATLON OMArcs.
     * This method is smart enough to take the calculated position information
     * and make a call to Projection.forwardArc() with start, extent and arcType
     * information.
     */
    protected ArrayList<float[]> getCoordLists(GeoProj proj, LatLonPoint center, double radius,
                                               int nverts) {

        int at = (arcType == Arc2D.OPEN && !isClear(fillPaint) ? Arc2D.CHORD : arcType);

        return proj.forwardArc(center, /* radians */
                true, radius, nverts, ProjMath.degToRad(start), ProjMath.degToRad(extent), at);
    }

    /**
     * Return the java.awt.Shape (GeneralPath) that reflects a arc that
     * encompasses a pole. Used when the projection is Cylindrical.
     * 
     * @return a GeneralPath object, or null if it's not needed (which is
     *         probably most of the time, if the arc doesn't include a pole or
     *         the projection isn't Cylindrical).
     */
    public GeneralPath getPolarShapeLine() {
        return polarShapeLine;
    }

    /**
     * Create alternate x,y coordinate arrays for rendering graphics the
     * encompass a pole in the Cylindrical projection.
     * 
     * @return a two dimensional array of points. The [0] array is the x points,
     *         the [1] is the y points.
     */
    private float[][] doPolarFillCorrection(float[] xpoints, float[] ypoints, int y1) {
        float[][] ret = new float[2][];

        int len = xpoints.length;
        float[] alt_xpts = new float[len + 2];
        float[] alt_ypts = new float[len + 2];
        System.arraycopy(xpoints, 0, alt_xpts, 0, len);
        System.arraycopy(ypoints, 0, alt_ypts, 0, len);
        alt_xpts[len] = alt_xpts[len - 1];
        alt_xpts[len + 1] = alt_xpts[0];
        alt_ypts[len] = y1;
        alt_ypts[len + 1] = alt_ypts[len];

        ret[0] = alt_xpts;
        ret[1] = alt_ypts;
        return ret;
    }

    /**
     * Paint the arc.
     * 
     * @param g Graphics context to render into
     */
    public void render(Graphics g) {

        if (!correctFill) {
            // super will catch a null shape...
            super.render(g);
        } else {

            Shape s = getShape();

            if (!isRenderable(s)) {
                return;
            }

            // The polarShapeLine will be there only if a shape was
            // generated.
            // This is getting kicked off because the arc is
            // encompassing a pole, so we need to handle it a little
            // differently.
            if (shouldRenderFill()) {
                setGraphicsForFill(g);
                fill(g, s);
                // draw texture
                if (textureMask != null && textureMask != fillPaint) {
                    setGraphicsColor(g, textureMask);
                    fill(g, s);
                }
            }

            // BUG There is still a bug apparent when, in a
            // cylindrical projection, and drawing a arc around the
            // south pole. If the center of the arc is below any
            // part of the edge of the arc, with the left lower dip
            // of the arc on the screen, you get a line drawn from
            // the right dip to the left dip. Not sure why. It's an
            // unusual case, however.
            if (shouldRenderEdge()) {
                setGraphicsForEdge(g);
                ((Graphics2D) g).draw(polarShapeLine);
            }
        }
    }

    /**
     * @see com.bbn.openmap.omGraphics.OMGraphic#clone()
     */
    public Object clone() {
        OMArc clone = (OMArc) super.clone();
        clone.setNeedToRegenerate(true);
        if (center != null)
            clone.center = (LatLonPoint) center.clone();
        return clone;
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMArc) {
            OMArc arc = (OMArc) source;
            this.x1 = arc.x1;
            this.y1 = arc.y1;
            this.off_x = arc.off_x;
            this.off_y = arc.off_y;
            Point2D center = arc.getCenter();
            if (center != null) {
                this.center = new LatLonPoint.Double(center);
            }
            this.radius = arc.radius;
            this.width = arc.width;
            this.height = arc.height;
            this.start = arc.start;
            this.extent = arc.extent;
            this.arcType = arc.arcType;
            this.nverts = arc.nverts;
            this.rotationAngle = arc.rotationAngle;
        }
    }

}