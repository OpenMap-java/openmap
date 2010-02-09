package com.bbn.openmap.proj;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A Projection that wraps another projection, but stretch the image to another
 * aspect ratio.
 * <p>
 * The motivation for this projection is to support the following clause in wms
 * 1.1.1.
 * <p>
 * OGC 01-068r3 (wms 1.1.1) 7.2.3.8. "In the case where the aspect ratio of the
 * BBOX and the ratio width/height are different, the WMS shall stretch the
 * returned map so that the resulting pixels could themselves be rendered in the
 * aspect ratio of the BBOX"
 * 
 * @author halset
 */
public class AspectRatioProjection extends GeoProj {

    private GeoProj wrappedProjection;

    private float xFactor;

    private float yFactor;

    private float halfWidth;

    private float halfWrappedWidth;

    private float halfHeight;

    private float halfWrappedHeight;

    private boolean xHasFactor;

    private boolean yHasFactor;

    /**
     * Constructor that takes a projection and the new width/height.
     * 
     * @param proj a projection to wrap
     * @param w a int with the new width
     * @param h a int with the new height
     */
    public AspectRatioProjection(GeoProj proj, int w, int h) {
        super(proj.getCenter(), proj.getScale(), w, h);
        wrappedProjection = proj;
        xHasFactor = proj.getWidth() != w;
        yHasFactor = proj.getHeight() != h;
        computeParameters();
    }

    private Point2D fromWrapped(Point2D pt) {
        pt.setLocation(xFromWrapped((float) pt.getX()),
                yFromWrapped((float) pt.getY()));
        return pt;
    }

    private float xFromWrapped(float x) {
        if (!xHasFactor)
            return x;
        return ((x - halfWrappedWidth) * xFactor + halfWidth);
    }

    private float yFromWrapped(float y) {
        if (!yHasFactor)
            return y;
        return ((y - halfWrappedHeight) * yFactor + halfHeight);
    }

    private float xToWrapped(float x) {
        if (!xHasFactor)
            return x;
        return ((x - halfWidth) / xFactor + halfWrappedWidth);
    }

    private float yToWrapped(float y) {
        if (!yHasFactor)
            return y;
        return ((y - halfHeight) / yFactor + halfWrappedHeight);
    }

    protected ArrayList<float[]> _forwardPoly(float[] rawllpts, int ltype, int nsegs,
                                     boolean isFilled) {
        ArrayList<float[]> stuff = wrappedProjection._forwardPoly(rawllpts,
                ltype,
                nsegs,
                isFilled);
        int size = stuff.size();
        for (int i = 0; i < size; i += 2) {
            float[] xpts = stuff.get(i);
            if (xHasFactor) {
                for (int j = 0; j < xpts.length; j++)
                    xpts[j] = xFromWrapped(xpts[j]);
            }
            if (yHasFactor) {
                float[] ypts = stuff.get(i + 1);
                for (int j = 0; j < ypts.length; j++)
                    ypts[j] = yFromWrapped(ypts[j]);
            }
        }
        return stuff;
    }

    protected void computeParameters() {
        if (wrappedProjection != null) {
            wrappedProjection.computeParameters();
            xFactor = (float) ((double) getWidth() / (double) wrappedProjection.getWidth());
            yFactor = (float) ((double) getHeight() / (double) wrappedProjection.getHeight());
            halfWidth = (float) getWidth() / 2.0F;
            halfHeight = (float) getHeight() / 2.0F;
            halfWrappedWidth = (float) wrappedProjection.getWidth() / 2.0F;
            halfWrappedHeight = (float) wrappedProjection.getHeight() / 2.0F;
        }
    }

    public void drawBackground(Graphics2D g, Paint p) {
        wrappedProjection.drawBackground(g, p);
    }

    public void drawBackground(Graphics g) {
        wrappedProjection.drawBackground(g);
    }

    public double normalizeLatitude(double lat) {
        if (wrappedProjection == null) {
            if (lat > NORTH_POLE) {
                return NORTH_POLE;
            } else if (lat < SOUTH_POLE) {
                return SOUTH_POLE;
            }
            return lat;
        }
        return wrappedProjection.normalizeLatitude(lat);
    }

    @Override
    public Point2D forward(Point2D llp, Point2D pt) {
        return fromWrapped(wrappedProjection.forward(llp, pt));
    }

    @Override
    public Point2D forward(double lat, double lon, Point2D pt) {
        return fromWrapped(wrappedProjection.forward(lat, lon, pt));
    }

    public boolean forwardRaw(float[] rawllpts, int rawoff, float[] xcoords,
                              float[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        boolean r = wrappedProjection.forwardRaw(rawllpts,
                rawoff,
                xcoords,
                ycoords,
                visible,
                copyoff,
                copylen);
        int end = copylen + copyoff;
        for (int i = copyoff; i < end; i++) {
            if (xHasFactor)
                xcoords[i] = xFromWrapped(xcoords[i]);
            if (yHasFactor)
                ycoords[i] = yFromWrapped(ycoords[i]);
        }
        return r;
    }

    public Point2D getLowerRight() {
        return wrappedProjection.getLowerRight();
    }

    public Point2D getUpperLeft() {
        return wrappedProjection.getUpperLeft();
    }

    @Override
    public Point2D inverse(Point2D point, Point2D llpt) {
        return inverse(point.getX(), point.getY(), llpt);
    }

    @Override
    public boolean isPlotable(float lat, float lon) {
        return wrappedProjection.isPlotable(lat, lon);
    }

    @Override
    protected ArrayList<float[]> _forwardPoly(double[] rawllpts, int ltype, int nsegs,
                                     boolean isFilled) {
        ArrayList<float[]> stuff = wrappedProjection._forwardPoly(rawllpts,
                ltype,
                nsegs,
                isFilled);
        int size = stuff.size();
        for (int i = 0; i < size; i += 2) {
            float[] xpts = stuff.get(i);
            if (xHasFactor) {
                for (int j = 0; j < xpts.length; j++)
                    xpts[j] = xFromWrapped(xpts[j]);
            }
            if (yHasFactor) {
                float[] ypts = stuff.get(i + 1);
                for (int j = 0; j < ypts.length; j++)
                    ypts[j] = yFromWrapped(ypts[j]);
            }
        }
        return stuff;
    }

    @Override
    public Point2D forward(double lat, double lon, Point2D pt, boolean isRadian) {
        return fromWrapped(wrappedProjection.forward(lat, lon, pt, isRadian));
    }

    @Override
    public Point2D inverse(double x, double y, Point2D llpt) {
        return wrappedProjection.inverse(xToWrapped((float) x),
                yToWrapped((float) y),
                llpt);
    }

    public boolean isPlotable(double lat, double lon) {
        return wrappedProjection.isPlotable(lat, lon);
    }
}
