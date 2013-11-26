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
//$RCSfile: OMGeo.java,v $
//$Revision: 1.6 $
//$Date: 2009/01/21 01:24:42 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.omGraphics.OMGraphicAdapter;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * This is a class (or set of classes) that map GeoExtents to OMGraphics. They
 * can be used directly in a ExtentIndex and will render within OpenMap. This
 * class is not considered to be part of the omgeo.jar package because of its
 * dependencies on OpenMap, but resides in this package because of it's
 * knowledge of it.
 * 
 * @author dietrick
 */
public abstract class OMGeo extends OMGraphicAdapter implements GeoExtent {

    protected GeoExtent extent;
    protected Object id = OMGeo.this;

    protected OMGeo() {
        setRenderType(RENDERTYPE_LATLON);
    }

    protected OMGeo(GeoExtent ge) {
        this();
        setExtent(ge);
    }

    public GeoExtent getExtent() {
        return extent;
    }

    public void setExtent(GeoExtent ge) {
        extent = ge;
    }

    public BoundingCircle getBoundingCircle() {
        return getExtent().getBoundingCircle();
    }

    public Object getID() {
        return id;
    }

    public void setID(Object id) {
        this.id = id;
    }

    public static class Pt extends OMGeo implements GeoPoint {

        protected int radius = OMPoint.DEFAULT_RADIUS;
        protected boolean isOval = true;

        public Pt(GeoPoint gp) {
            super(gp);
        }

        public Pt(Geo g) {
            super(new GeoPoint.Impl(g));
        }

        public Pt(double lat, double lon, boolean isDegrees) {
            super(new GeoPoint.Impl(lat, lon, isDegrees));
        }

        public Geo getGeo() {
            return ((GeoPoint) getExtent()).getPoint();
        }

        public boolean generate(Projection proj) {

            setNeedToRegenerate(true);

            if (proj == null) {
                Debug.message("omgraphic", "GeoOMGraphic.Point: null projection in generate!");
                return false;
            }

            Geo geo = getPoint();
            double lat = geo.getLatitude();
            double lon = geo.getLongitude();

            if (!proj.isPlotable(lat, lon)) {
                setNeedToRegenerate(true);// HMMM not the best flag
                return false;
            }

            Point2D p1 = new Point2D.Double();
            proj.forward(lat, lon, p1);

            double x1 = p1.getX() - radius;
            double y1 = p1.getY() - radius;
            double x2 = p1.getX() + radius;
            double y2 = p1.getY() + radius;

            if (isOval) {
                setShape(new GeneralPath(new Ellipse2D.Float((float) Math.min(x2, x1), (float) Math.min(y2, y1), (float) Math.abs(x2
                        - x1), (float) Math.abs(y2 - y1))));
            } else {
                setShape(createBoxShape((int) Math.min(x2, x1), (int) Math.min(y2, y1), (int) Math.abs(x2
                        - x1), (int) Math.abs(y2 - y1)));
            }

            initLabelingDuringGenerate();
            setLabelLocation(new Point((int) x2, (int) y1));

            setNeedToRegenerate(false);
            return true;
        }

        public boolean isOval() {
            return isOval;
        }

        public void setOval(boolean isOval) {
            this.isOval = isOval;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public Geo getPoint() {
            return ((GeoPoint) getExtent()).getPoint();
        }
    }

    public static class Line extends OMGeo implements GeoSegment {

        public Line(GeoSegment gs) {
            super(gs);
        }

        public Line(Geo[] gs) {
            super(new GeoSegment.Impl(gs));
        }

        public Geo[] getGeoArray() {
            return ((GeoSegment) getExtent()).getSeg();
        }

        public boolean generate(Projection proj) {

            setNeedToRegenerate(true);
            
            if (proj == null) {
                Debug.message("omgraphic", "GeoOMGraphic.Line: null projection in generate!");
                return false;
            }

            // reset the internals
            initLabelingDuringGenerate();

            double[] latlons = getSegArray();

            ArrayList<float[]> lines = null;
            if (proj instanceof GeoProj) {
                lines = ((GeoProj) proj).forwardLine(new LatLonPoint.Double(latlons[0], latlons[1]), new LatLonPoint.Double(latlons[2], latlons[3]), lineType, -1);
            } else {
                lines = proj.forwardLine(new Point2D.Double(latlons[1], latlons[0]), new Point2D.Double(latlons[3], latlons[2]));
            }

            int size = lines.size();

            float[][] xpoints = new float[(int) (size / 2)][0];
            float[][] ypoints = new float[xpoints.length][0];

            GeneralPath projectedShape = null;
            
            for (int i = 0, j = 0; i < size; i += 2, j++) {
                float[] xps = (float[]) lines.get(i);
                float[] yps = (float[]) lines.get(i + 1);

                xpoints[j] = xps;
                ypoints[j] = yps;

                GeneralPath gp = createShape(xps, yps, false);
                if (projectedShape == null) {
                    projectedShape = gp;
                } else {
                    projectedShape.append(gp, false);
                }
            }
            
            setShape(projectedShape);
            setLabelLocation(projectedShape);
            setNeedToRegenerate(false);
            return true;
        }

        public Geo[] getSeg() {
            return ((GeoSegment) getExtent()).getSeg();
        }

        public double[] getSegArray() {
            return ((GeoSegment) getExtent()).getSegArray();
        }

    }

    public static class Polyline extends OMGeo implements GeoPath {

        public Polyline(GeoPath gp) {
            super(gp);
        }

        public Polyline(Geo[] gs) {
            super(new GeoPath.Impl(gs));
        }

        public Polyline(GeoArray points) {
            super(new GeoPath.Impl(points));
        }

        public GeoArray getPoints() {
            return ((GeoPath) getExtent()).getPoints();
        }

        public boolean generate(Projection proj) {

            setNeedToRegenerate(true);            
            
            boolean isPolygon = getExtent() instanceof GeoRegion;

            if (proj == null) {
                Debug.message("omgraphic", "GeoOMGraphic.Poly: null projection in generate!");
                return false;
            }

            // answer the question now, saving calculation for future
            // calculations. The set method forces the calculation for
            // the query.

            // polygon/polyline project the polygon/polyline.
            // Vertices should already be in radians.

            // We might want to cache the latlon points retrieved from the
            // GeoArray at some point.
            ArrayList<float[]> vector;
            if (proj instanceof GeoProj) {
                vector = ((GeoProj) proj).forwardPoly(getPoints().toLLRadians(), lineType, -1, isPolygon);
            } else {
                vector = proj.forwardPoly(getPoints().toLLDegrees(), isPolygon);
            }

            int size = vector.size();

            float[][] xpoints = new float[(int) (size / 2)][0];
            float[][] ypoints = new float[xpoints.length][0];

            for (int i = 0, j = 0; i < size; i += 2, j++) {
                xpoints[j] = (float[]) vector.get(i);
                ypoints[j] = (float[]) vector.get(i + 1);
            }

            initLabelingDuringGenerate();

            size = xpoints.length;
            GeneralPath projectedShape = null;
            for (int i = 0; i < size; i++) {
                GeneralPath gp = createShape(xpoints[i], ypoints[i], isPolygon);

                projectedShape = appendShapeEdge(projectedShape, gp, false);
            }

            setShape(projectedShape);
            setLabelLocation(xpoints[0], ypoints[0]);

            setNeedToRegenerate(false);
            return true;
        }

        protected boolean isGeometryClosed(float[] rawllpts) {
            boolean geometryClosed = false;

            if (rawllpts != null) {
                int l = rawllpts.length;
                if (l > 4) {
                    geometryClosed = (MoreMath.approximately_equal(rawllpts[0], rawllpts[l - 2]) && MoreMath.approximately_equal(rawllpts[1], rawllpts[l - 1]));
                }
            }

            return geometryClosed;
        }

        public boolean isSegmentNear(GeoSegment s, double epsilon) {
            return ((GeoPath) getExtent()).isSegmentNear(s, epsilon);
        }

        public int length() {
            return ((GeoPath) getExtent()).length();
        }

        public PointIterator pointIterator() {
            return ((GeoPath) getExtent()).pointIterator();
        }

        public SegmentIterator segmentIterator() {
            return ((GeoPath) getExtent()).segmentIterator();
        }
    }

    public static class Polygon extends Polyline implements GeoRegion {

        public Polygon(GeoPath gp) {
            super(gp);
        }

        public Polygon(Geo[] gs) {
            super(new GeoRegion.Impl(gs));
        }

        public Polygon(GeoArray points) {
            super(new GeoRegion.Impl(points));
        }

        public boolean isPointInside(Geo point) {
            return ((GeoRegion) getExtent()).isPointInside(point);
        }
    }

}
