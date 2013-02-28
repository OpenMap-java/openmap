package com.bbn.openmap.proj;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.proj.coords.DatumShiftGCT;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * This projection wraps another projection and adds datum handling. The
 * motivation for this projection is to be able to serve wms clients requesting
 * maps in a datum different from wgs84.
 * <p>
 * The underlying data is supposed to be wgs4. The given {@link DatumShiftGCT}
 * convert this to another datum before using the wrapped projection to
 * calculate the screen coordinates.
 * <p>
 * {@link LatLonPoint} as input or output of any of the methods in this class is
 * in wgs84. Internally, each method will convert datums as needed.
 * <p>
 * A user (like wms) of this projection will use a
 * {@link com.bbn.openmap.proj.coords.CoordinateReferenceSystem} to convert coordinates to wgs84 before
 * using this method.
 */
public class DatumShiftProjection extends GeoProj {

    private GeoProj wrappedProjection;

    private DatumShiftGCT datum;

    public DatumShiftProjection(GeoProj proj, DatumShiftGCT datum) {
        super(proj.getCenter(),
              proj.getScale(),
              proj.getWidth(),
              proj.getHeight());
        this.datum = datum;
        this.wrappedProjection = proj;

        setCenter(proj.getCenter());
    }

    @Override
    public void setCenter(double lat, double lon) {
        super.setCenter(lat, lon);

        Point2D centerInDifferentDatum = datum.forward(lat,
                lon,
                new Point2D.Double());
        wrappedProjection.setCenter((float) centerInDifferentDatum.getY(),
                (float) centerInDifferentDatum.getX());
    }

    public ArrayList<float[]> _forwardPoly(float[] rawllpts, int ltype,
                                         int nsegs, boolean isFilled) {

        float[] rawllptsInDifferentDatum = new float[rawllpts.length];

        Point2D tmpll = new Point2D.Double();

        for (int i = 0; i < rawllpts.length; i += 2) {
            tmpll = datum.forward(Math.toDegrees(rawllpts[i]),
                    Math.toDegrees(rawllpts[i + 1]),
                    tmpll);
            rawllptsInDifferentDatum[i] = (float) Math.toRadians(tmpll.getY());
            rawllptsInDifferentDatum[i + 1] = (float) Math.toRadians(tmpll.getX());
        }

        return wrappedProjection._forwardPoly(rawllptsInDifferentDatum,
                ltype,
                nsegs,
                isFilled);
    }

    protected ArrayList<float[]> _forwardPoly(double[] rawllpts, int ltype,
                                            int nsegs, boolean isFilled) {

        double[] rawllptsInDifferentDatum = new double[rawllpts.length];

        Point2D tmpll = new Point2D.Double();

        for (int i = 0; i < rawllpts.length; i += 2) {
            tmpll = datum.forward(Math.toDegrees(rawllpts[i]),
                    Math.toDegrees(rawllpts[i + 1]),
                    tmpll);
            rawllptsInDifferentDatum[i] = Math.toRadians(tmpll.getY());
            rawllptsInDifferentDatum[i + 1] = Math.toRadians(tmpll.getX());
        }

        return wrappedProjection._forwardPoly(rawllptsInDifferentDatum,
                ltype,
                nsegs,
                isFilled);
    }

    protected void computeParameters() {
        if (wrappedProjection != null) {

            wrappedProjection.width = width;
            wrappedProjection.height = height;
            wrappedProjection.scale = scale;
            // wrappedProjection.ctrLat = ctrLat;
            // wrappedProjection.ctrLon = ctrLon;

            wrappedProjection.computeParameters();
        }
    }

    public void drawBackground(Graphics2D g, Paint p) {
        wrappedProjection.drawBackground(g, p);
    }

    public void drawBackground(Graphics g) {
        wrappedProjection.drawBackground(g);
    }

    public double normalizeLatitude(double lat) {
        if (lat > NORTH_POLE) {
            return NORTH_POLE;
        } else if (lat < SOUTH_POLE) {
            return SOUTH_POLE;
        }
        return lat;
    }

    public Point2D forward(double lat, double lon, Point2D pt, boolean isRadian) {
        Point2D t;
        if (isRadian) {
            t = datum.forward(Math.toDegrees(lat), Math.toDegrees(lon));
        } else {
            t = datum.forward(lat, lon);
        }
        return wrappedProjection.forward((float) t.getY(), (float) t.getX(), pt);
    }

    public boolean forwardRaw(float[] rawllpts, int rawoff, float[] xcoords,
                              float[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {

        float[] rawllptsInDifferentDatum = new float[rawllpts.length];

        Point2D tmpll = new Point2D.Double();

        int end = copylen + copyoff;
        for (int i = copyoff, j = rawoff; i < end; i++, j += 2) {
            tmpll = datum.forward(Math.toDegrees(rawllpts[j]),
                    Math.toDegrees(rawllpts[j + 1]),
                    tmpll);
            rawllptsInDifferentDatum[j] = (float) Math.toRadians(tmpll.getY());
            rawllptsInDifferentDatum[j + 1] = (float) Math.toRadians(tmpll.getX());
        }

        return wrappedProjection.forwardRaw(rawllptsInDifferentDatum,
                rawoff,
                xcoords,
                ycoords,
                visible,
                copyoff,
                copylen);
    }

    public LatLonPoint getLowerRight() {
        Point2D llp = wrappedProjection.getLowerRight();
        return datum.inverse(llp.getX(), llp.getY());
    }

    public LatLonPoint getUpperLeft() {
        Point2D llp = wrappedProjection.getUpperLeft();
        return datum.inverse(llp.getX(), llp.getY());
    }

    public LatLonPoint inverse(double x, double y, Point2D pt) {
        LatLonPoint llpt = assertLatLonPoint(pt);
        llpt = (LatLonPoint) wrappedProjection.inverse(x, y, llpt);
        return datum.inverse(llpt.getX(), llpt.getY(), llpt);
    }

    public boolean isPlotable(double lat, double lon) {
        Point2D t = datum.forward(lat, lon);
        return wrappedProjection.isPlotable(t.getY(), t.getX());
    }

    @Override
	public float getScale(Point2D ll1, Point2D ll2, Point2D point1,
			Point2D point2) {
		return wrappedProjection.getScale(ll1, ll2, point1, point2);
	}

}
