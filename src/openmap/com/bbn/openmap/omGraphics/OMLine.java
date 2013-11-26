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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMLine.java,v $
// $RCSfile: OMLine.java,v $
// $Revision: 1.15 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

import com.bbn.openmap.omGraphics.geom.NonRegional;
import com.bbn.openmap.omGraphics.util.ArcCalc;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.DeepCopyUtil;

/**
 * Graphic object that represents a simple line.
 * <p>
 * The OMLine is used to create simple lines, from one point on the window to
 * the other. If you want to have a line with several parts, use OMPoly as a
 * polyline with no fillColor.
 * <h3>NOTE:</h3>
 * See the <a href="com.bbn.openmap.proj.Projection.html#line_restrictions">
 * RESTRICTIONS </a> on Lat/Lon lines. Not following the guidelines listed may
 * result in ambiguous/undefined shapes! Similar assumptions apply to the other
 * vector graphics that we define: circles, ellipses, rects, polys.
 * <p>
 * 
 * @see OMPoly
 */
public class OMLine
        extends OMAbstractLine
        implements Serializable, NonRegional {

    /**
     * Figured out after generation, based on what's going on with the map.
     */
    protected transient boolean isPolyline = false;

    /** latlons is a array of 4 doubles - lat1, lon1, lat2, lon2. */
    protected double[] latlons = null;

    /** pts is an array of 4 ints - px1, py1, px2, py2. */
    protected int[] pts = null;

    /**
     * For x-y and offset lines, there is the ability to put a curve in the
     * line. This setting is the amount of an angle, limited to a semi-circle
     * (PI) that the curve will represent. In other words, the arc between the
     * two end points is going to look like a 0 degrees of a circle (straight
     * line, which is the default), or 180 degrees of a circle (full
     * semi-circle). Given in radians, though, not degrees. The ArcCalc object
     * handles all the details.
     */
    protected ArcCalc arc = null;

    public final static int STRAIGHT_LINE = 0;
    public final static int CURVED_LINE = 1;

    /** Generic constructor, attributes need to filled later. */
    public OMLine() {
        super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
    }

    /**
     * Create a line from lat lon points.
     * 
     * @param lat_1 latitude of first point, decimal degrees.
     * @param lon_1 longitude of first point, decimal degrees.
     * @param lat_2 latitude of second point, decimal degrees.
     * @param lon_2 longitude of second point, decimal degrees.
     * @param lineType a choice between LINETYPE_STRAIGHT, LINETYPE_GREATCIRCLE
     *        or LINETYPE_RHUMB.
     */
    public OMLine(double lat_1, double lon_1, double lat_2, double lon_2, int lineType) {
        this(lat_1, lon_1, lat_2, lon_2, lineType, -1);
    }

    /**
     * Create a line from lat lon points.
     * 
     * @param lat_1 latitude of first point, decimal degrees.
     * @param lon_1 longitude of first point, decimal degrees.
     * @param lat_2 latitude of second point, decimal degrees.
     * @param lon_2 longitude of second point, decimal degrees.
     * @param lineType a choice between LINETYPE_STRAIGHT, LINETYPE_GREATCIRCLE
     *        or LINETYPE_RHUMB.
     * @param nsegs number of segment points (only for LINETYPE_GREATCIRCLE or
     *        LINETYPE_RHUMB line types, and if &lt; 1, this value is generated
     *        internally)
     */
    public OMLine(double lat_1, double lon_1, double lat_2, double lon_2, int lineType, int nsegs) {
        super(RENDERTYPE_LATLON, lineType, DECLUTTERTYPE_NONE);
        latlons = new double[4];
        latlons[0] = lat_1;
        latlons[2] = lat_2;
        latlons[1] = lon_1;
        latlons[3] = lon_2;
        this.nsegs = nsegs;
    }

    /**
     * Create a line between two xy points on the window.
     * 
     * @param x1 the x location of the first point, in pixels from the left of
     *        the window.
     * @param y1 the y location of the first point, in pixels from the top of
     *        the window.
     * @param x2 the x location of the second point, in pixels from the left of
     *        the window.
     * @param y2 the y location of the second point, in pixels from the top of
     *        the window.
     */
    public OMLine(int x1, int y1, int x2, int y2) {
        super(RENDERTYPE_XY, LINETYPE_STRAIGHT, DECLUTTERTYPE_NONE);
        pts = new int[4];
        pts[0] = x1;
        pts[1] = y1;
        pts[2] = x2;
        pts[3] = y2;
    }

    /**
     * Create a line between two x-y points on the window, where the x-y points
     * are offsets from a lat-lon point. It assumes that you'll want a straight
     * window line between the points, so if you don't, use the setLineType()
     * method to change it.
     * 
     * @param lat_1 the latitude of the reference point of the line, in decimal
     *        degrees.
     * @param lon_1 the longitude of the reference point of the line, in decimal
     *        degrees.
     * @param x1 the x location of the first point, in pixels from the longitude
     *        point.
     * @param y1 the y location of the first point, in pixels from the latitude
     *        point.
     * @param x2 the x location of the second point, in pixels from the
     *        longitude point.
     * @param y2 the y location of the second point, in pixels from the latitude
     *        point.
     */
    public OMLine(double lat_1, double lon_1, int x1, int y1, int x2, int y2) {

        super(RENDERTYPE_OFFSET, LINETYPE_STRAIGHT, DECLUTTERTYPE_NONE);
        latlons = new double[4];
        pts = new int[4];
        latlons[0] = lat_1;
        latlons[1] = lon_1;
        pts[0] = x1;
        pts[1] = y1;
        pts[2] = x2;
        pts[3] = y2;
    }

    /**
     * Set the lat lon values of the end points of the line from an array of
     * doubles - lat1, lon1, lat2, lon2. This does not look at the line render
     * type, so it acts accordingly. LL1 is only used in RENDERTYPE_LATLON,
     * RENDERTYPE_OFFSET, and LL2 is only used in RENDERTYPE_LATLON.
     * 
     * @param lls array of doubles - lat1, lon1, lat2, lon2
     */
    public void setLL(double[] lls) {
        latlons = lls;
        setNeedToRegenerate(true);
    }

    /**
     * Get the lat lon values of the end points of the line in an array of
     * doubles - lat1, lon1, lat2, lon2. Again, this does not look at the line
     * render type, so it acts accordingly. LL1 is only used in
     * RENDERTYPE_LATLON, RENDERTYPE_OFFSET, and LL2 is only used in
     * RENDERTYPE_LATLON.
     * 
     * @return the lat lon array, and all are decimal degrees.
     */
    public double[] getLL() {
        return latlons;
    }

    /**
     * Set the xy values of the end points of the line from an array of ints -
     * x1, y1, x2, y2 . This does not look at the line render type, so it acts
     * accordingly. p1 and p2 are only used in RENDERTYPE_XY, RENDERTYPE_OFFSET.
     * 
     * @param xys array of ints for the points - x1, y1, x2, y2
     */
    public void setPts(int[] xys) {
        pts = xys;
        setNeedToRegenerate(true);
    }

    /**
     * Get the xy values of the end points of the line in an array of ints - x1,
     * y1, x2, y2 . This does not look at the line render type, so it acts
     * accordingly. p1 and p2 are only used in RENDERTYPE_XY, RENDERTYPE_OFFSET.
     * 
     * @return the array of x-y points, and all are pixel values
     */
    public int[] getPts() {
        return pts;
    }

    /**
     * Check to see if this line is a polyline. This is a polyline if it is
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB for RENDERTYPE_LATLON polys.
     * 
     * @return true if polyline false if not
     */
    public boolean isPolyline() {
        return isPolyline;
    }

    /**
     * Set the number of segments of the lat/lon line. (This is only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types, and if &lt; 1, this
     * value is generated internally).
     * 
     * @param nsegs number of segment points
     */
    public void setNumSegs(int nsegs) {
        this.nsegs = nsegs;
    }

    /**
     * Get the number of segments of the lat/lon line. (This is only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types).
     * 
     * @return int number of segment points
     */
    public int getNumSegs() {
        return nsegs;
    }

    /**
     * Set the arc that is drawn between the points of a x-y or offset line.
     */
    public void setArc(ArcCalc ac) {
        arc = ac;
    }

    /**
     * Return the arc angle set for this line. Will only be set if it was set
     * externally.
     * 
     * @return arc angle in radians.
     */
    public ArcCalc getArc() {
        return arc;
    }

    /**
     * Prepare the line for rendering.
     * 
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {

        setNeedToRegenerate(true);
        
        if (proj == null) {
            Debug.message("omgraphic", "OMLine: null projection in generate!");
            return false;
        }

        // reset the internals
        isPolyline = false;
        initLabelingDuringGenerate();
        GeneralPath projectedShape = null;

        switch (renderType) {
            case RENDERTYPE_XY:

                if (pts == null) {
                    return false;
                }
                
                if (arc != null) {
                    xpoints = new float[1][];
                    ypoints = new float[1][];
                    arc.generate(pts[0], pts[1], pts[2], pts[3]);
                    xpoints[0] = arc.getXPoints();
                    ypoints[0] = arc.getYPoints();
                } else {
                    xpoints = new float[1][2];
                    ypoints = new float[1][2];

                    xpoints[0][0] = pts[0];
                    ypoints[0][0] = pts[1];
                    xpoints[0][1] = pts[2];
                    ypoints[0][1] = pts[3];
                }
                projectedShape = createShape(xpoints[0], ypoints[0], false);
                break;
            case RENDERTYPE_OFFSET:
                if (pts == null || latlons == null || !proj.isPlotable(latlons[0], latlons[1])) {
                    setNeedToRegenerate(true);
                    return false;
                }
                
                Point p1 = (Point) proj.forward(latlons[0], latlons[1], new Point());
                if (arc != null) {
                    xpoints = new float[1][];
                    ypoints = new float[1][];
                    arc.generate(p1.x + pts[0], p1.y + pts[1], p1.x + pts[2], p1.y + pts[3]);

                    xpoints[0] = arc.getXPoints();
                    ypoints[0] = arc.getYPoints();
                } else {
                    xpoints = new float[1][2];
                    ypoints = new float[1][2];

                    xpoints[0][0] = p1.x + pts[0];
                    ypoints[0][0] = p1.y + pts[1];
                    xpoints[0][1] = p1.x + pts[2];
                    ypoints[0][1] = p1.y + pts[3];
                }
                projectedShape = createShape(xpoints[0], ypoints[0], false);
                break;
            case RENDERTYPE_LATLON:
                if (latlons == null) {
                    setNeedToRegenerate(true);
                    return false;
                }
                
                if (arc != null) {
                    p1 = (Point) proj.forward(latlons[0], latlons[1], new Point());
                    Point p2 = (Point) proj.forward(latlons[2], latlons[3], new Point());
                    xpoints = new float[1][];
                    ypoints = new float[1][];
                    arc.generate(p1.x, p1.y, p2.x, p2.y);

                    xpoints[0] = arc.getXPoints();
                    ypoints[0] = arc.getYPoints();

                    projectedShape = createShape(xpoints[0], ypoints[0], false);

                    isPolyline = true;

                } else {
                    ArrayList<float[]> lines = null;
                    if (proj instanceof GeoProj) {
                        lines =
                                ((GeoProj) proj).forwardLine(new LatLonPoint.Double(latlons[0], latlons[1]),
                                                             new LatLonPoint.Double(latlons[2], latlons[3]), lineType, nsegs);
                    } else {
                        lines =
                                proj.forwardLine(new Point2D.Double(latlons[1], latlons[0]), new Point2D.Double(latlons[3],
                                                                                                                latlons[2]));
                    }

                    int size = lines.size();

                    xpoints = new float[(int) (size / 2)][0];
                    ypoints = new float[xpoints.length][0];
                    
                    for (int i = 0, j = 0; i < size; i += 2, j++) {
                        float[] xps = (float[]) lines.get(i);
                        float[] yps = (float[]) lines.get(i + 1);

                        xpoints[j] = xps;
                        ypoints[j] = yps;

                        GeneralPath gp = createShape(xps, yps, false);

                        projectedShape = appendShapeEdge(projectedShape, gp, false);
                    }
                    
                    isPolyline = (lineType != LINETYPE_STRAIGHT);
                }
                break;
            case RENDERTYPE_UNKNOWN:
                System.err.println("OMLine.generate: invalid RenderType");
                setNeedToRegenerate(true);
                return false;
        }

        setShape(projectedShape);
        setLabelLocation(projectedShape);

        if (arrowhead != null) {
            arrowhead.generate(this);
        }

        if (arc != null) {
            // This will only do something if debugging is on.
            arc.generate(proj);
        }

        setNeedToRegenerate(false);
        return true;
    }

    /**
     * Paint the line.
     * 
     * @param g Graphics context to render into
     */
    public void render(Graphics g) {

        if (!isRenderable(getShape())) {
            return;
        }

        // Just to draw the matting for the arrowhead. The matting
        // for the rest of the line will be taken care of in
        // super.render().
        if (arrowhead != null && isMatted() && g instanceof Graphics2D && stroke instanceof BasicStroke) {
            ((Graphics2D) g).setStroke(new BasicStroke(((BasicStroke) stroke).getLineWidth() + 2f));
            setGraphicsColor(g, Color.black);
            arrowhead.render(g);
        }

        super.render(g);

        if (arrowhead != null) {
            setGraphicsForEdge(g);
            arrowhead.render(g);
        }

        if (arc != null) {
            // This is a debugging thing, most times does nothing.
            arc.render(g);
        }

    }

    /**
     * The OMLine should never render fill. It can think it does, if the
     * geometry turns out to be curved. Returning false affects distance() and
     * contains() methods.
     */
    public boolean shouldRenderFill() {
        return false;
    }

    /**
     * This takes the area out of OMLines that may look like they have area,
     * depending on their shape. Checks to see what shouldRenderFill() returns
     * (false by default) to decide how to measure this. If shouldRenderFill ==
     * true, the super.contains() method is returned, which assumes the line
     * shape has area if it is curved. Otherwise, it returns true if the point
     * is on the line.
     */
    public boolean contains(double x, double y) {
        if (shouldRenderFill()) {
            return super.contains(x, y);
        } else {
            return (distance(x, y) == 0);
        }
    }

    public void restore(OMGeometry source) {
        super.restore(source);
        if (source instanceof OMLine) {
            OMLine line = (OMLine) source;

            this.latlons = DeepCopyUtil.deepCopy(line.latlons);
            this.pts = DeepCopyUtil.deepCopy(line.pts);
            if (line.arc != null) {
                this.arc = new ArcCalc(line.arc.getArcAngle(), line.arc.isArcUp());
            } else {
                this.arc = null;
            }
        }
    }

}