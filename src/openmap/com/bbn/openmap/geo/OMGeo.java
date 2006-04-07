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
//$Revision: 1.1 $
//$Date: 2006/04/07 17:23:34 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

public abstract class OMGeo extends OMGraphic {

    protected GeoExtent geos;

    protected OMGeo() {
        setRenderType(RENDERTYPE_LATLON);
    }

    protected OMGeo(GeoExtent ge) {
        this(ge, LINETYPE_GREATCIRCLE);
    }

    protected OMGeo(GeoExtent ge, int lineType) {
        setGeos(ge);
        setLineType(lineType);
    }

    public GeoExtent getGeos() {
        return geos;
    }

    public void setGeos(GeoExtent ge) {
        geos = ge;
    }

    public static class Pt extends OMGeo {

        protected int radius = OMPoint.DEFAULT_RADIUS;
        protected boolean isOval = true;

        public Pt(GeoPoint gp) {
            super(gp);
        }

        public Pt(Geo g) {
            super(new GeoPoint.Impl(g));
        }

        public Geo getGeo() {
            return ((GeoPoint) getGeos()).getPoint();
        }

        public boolean generate(Projection proj) {
            setShape(null);
            if (proj == null) {
                Debug.message("omgraphic",
                        "GeoOMGraphic.Point: null projection in generate!");
                return false;
            }

            Geo geo = ((GeoPoint) getGeos()).getPoint();
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
                shape = new GeneralPath(new Ellipse2D.Float((float) Math.min(x2,
                        x1), (float) Math.min(y2, y1), (float) Math.abs(x2 - x1), (float) Math.abs(y2
                        - y1)));
            } else {
                shape = createBoxShape((int) Math.min(x2, x1),
                        (int) Math.min(y2, y1),
                        (int) Math.abs(x2 - x1),
                        (int) Math.abs(y2 - y1));
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

    }

    public static class Line extends OMGeo {

        public Line(GeoSegment gs) {
            super(gs);
        }

        public Line(GeoSegment gs, int lineType) {
            super(gs, lineType);
        }

        public Line(Geo[] gs, int lineType) {
            super(new GeoSegment.Impl(gs), lineType);
        }

        public Geo[] getGeoArray() {
            return ((GeoSegment) getGeos()).getSeg();
        }

        public boolean generate(Projection proj) {
            setShape(null);

            if (proj == null) {
                Debug.message("omgraphic",
                        "GeoOMGraphic.Line: null projection in generate!");
                return false;
            }

            // reset the internals
            initLabelingDuringGenerate();

            float[] latlons = ((GeoSegment) getGeos()).getSegArray();

            ArrayList lines = null;
            if (proj instanceof GeoProj) {
                lines = ((GeoProj) proj).forwardLine(new LatLonPoint(latlons[0], latlons[1]),
                        new LatLonPoint(latlons[2], latlons[3]),
                        lineType,
                        -1);
            } else {
                lines = proj.forwardLine(new Point2D.Float(latlons[1], latlons[0]),
                        new Point2D.Float(latlons[3], latlons[2]));
            }

            int size = lines.size();

            int[][] xpoints = new int[(int) (size / 2)][0];
            int[][] ypoints = new int[xpoints.length][0];

            for (int i = 0, j = 0; i < size; i += 2, j++) {
                int[] xps = (int[]) lines.get(i);
                int[] yps = (int[]) lines.get(i + 1);

                xpoints[j] = xps;
                ypoints[j] = yps;

                GeneralPath gp = createShape(xps, yps, false);
                if (shape == null) {
                    shape = gp;
                } else {
                    ((GeneralPath) shape).append(gp, false);
                }
            }

            setLabelLocation(shape);
            setNeedToRegenerate(false);
            return true;
        }

    }

    public static class Poly extends OMGeo {

        protected boolean isPolygon;

        public Poly(GeoPath gp) {
            super(gp);
        }

        public Poly(GeoPath gp, int lineType) {
            super(gp, lineType);
        }

        public Poly(Geo[] gs, int lineType, boolean isPolygon) {
            super((isPolygon ? new GeoRegion.Impl(gs) : new GeoPath.Impl(gs)),
                  lineType);
        }

        public Geo[] toGeoArray() {
            return ((GeoPath) getGeos()).toPointArray();
        }

        public boolean generate(Projection proj) {
            int i, j, npts;
            setShape(null);
            setNeedToRegenerate(true);
            isPolygon = getGeos() instanceof GeoRegion;

            if (proj == null) {
                Debug.message("omgraphic",
                        "GeoOMGraphic.Poly: null projection in generate!");
                return false;
            }

            // answer the question now, saving calcuation for future
            // calculations. The set method forces the calculation for
            // the query.

            // The only real new memory here is the array itself. We may want to
            // hold this to facilitate some speed.

            Geo[] geos = ((GeoPath) getGeos()).toPointArray();
            npts = geos.length;
            double[] rawllpts = new double[npts * 2];
            for (i = 0; i < npts; i++) {
                Geo geo = geos[i];
                rawllpts[2 * i] = geo.getLatitudeRadians();
                rawllpts[2 * i + 1] = geo.getLongitudeRadians();
            }

            // isGeometryClosed(rawllpts);

            // polygon/polyline project the polygon/polyline.
            // Vertices should already be in radians.
            ArrayList vector;
            if (proj instanceof GeoProj) {
                vector = ((GeoProj) proj).forwardPoly(rawllpts,
                        lineType,
                        -1,
                        isPolygon);
            } else {
                ProjMath.arrayRadToDeg(rawllpts);
                vector = proj.forwardPoly(rawllpts, isPolygon);
            }

            int size = vector.size();

            int[][] xpoints = new int[(int) (size / 2)][0];
            int[][] ypoints = new int[xpoints.length][0];

            for (i = 0, j = 0; i < size; i += 2, j++) {
                xpoints[j] = (int[]) vector.get(i);
                ypoints[j] = (int[]) vector.get(i + 1);
            }

            initLabelingDuringGenerate();

            size = xpoints.length;

            for (i = 0; i < size; i++) {
                GeneralPath gp = createShape(xpoints[i], ypoints[i], isPolygon);

                if (shape == null) {
                    shape = gp;
                } else {
                    ((GeneralPath) shape).append(gp, false);
                }
            }

            setLabelLocation(xpoints[0], ypoints[0]);

            setNeedToRegenerate(false);
            return true;
        }

        protected boolean isGeometryClosed(float[] rawllpts) {
            boolean geometryClosed = false;

            if (rawllpts != null) {
                int l = rawllpts.length;
                if (l > 4) {
                    geometryClosed = (Math.abs(rawllpts[0] - rawllpts[l - 2]) < 1e-5 && Math.abs(rawllpts[1]
                            - rawllpts[l - 1]) < 1e-5);
                }
            }

            return geometryClosed;
        }
    }

}
