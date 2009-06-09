//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.proj;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class ProjectionRotation implements Projection {

    protected Proj proj;
    protected double theta;
    protected AffineTransform transform;
    protected AffineTransform transformInverse;

    public ProjectionRotation(Proj proj, double theta) {
        update(proj, theta);
    }

    public ProjectionRotation(Proj proj, AffineTransform transform) {
        this.proj = proj;
        this.transform = transform;
    }

    public void update(Proj proj) {
        update(proj, theta);
    }

    public void update(Proj proj, double theta) {
        this.proj = proj;
        this.theta = theta;

        if (theta != 0) {
            double centerx = proj.getWidth() / 2.0;
            double centery = proj.getHeight() / 2.0;

            this.transform = AffineTransform.getRotateInstance(theta,
                    centerx,
                    centery);

            try {
                this.transformInverse = transform.createInverse();
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }

        } else {
            transform = null;
            transformInverse = null;
        }
    }

    protected Point2D transform(Point2D pt) {
        if (transform != null) {
            transform.transform(pt, pt);
        }
        return pt;
    }
    
    protected <T extends Point2D> T inverseTransform(T  pt) {
        if (transformInverse != null) {
            transformInverse.transform(pt, pt);
        }
        return pt;
    }
    
    public Point2D forward(Point2D coord) {
        return transform(proj.forward(coord));
    }

    public Point2D forward(Point2D llp, Point2D pt) {
        return transform(proj.forward(llp, pt));
    }

    public Point2D forward(float lat, float lon) {
        return transform(proj.forward(lat, lon));
    }

    public Point2D forward(double lat, double lon) {
        return transform(proj.forward(lat, lon));
    }

    public Point2D forward(float lat, float lon, Point2D pt) {
        return transform(proj.forward(lat, lon, pt));
    }

    public Point2D forward(double lat, double lon, Point2D pt) {
        return transform(proj.forward(lat, lon, pt));
    }

    public ArrayList<int[]> forwardLine(Point2D ll1, Point2D ll2) {
        return proj.forwardLine(ll1, ll2);
    }

    public ArrayList<int[]> forwardPoly(float[] rawllpts, boolean isFilled) {
        return proj.forwardPoly(rawllpts, isFilled);
    }

    public ArrayList<int[]> forwardPoly(double[] rawllpts, boolean isFilled) {
        return proj.forwardPoly(rawllpts, isFilled);
    }

    public boolean forwardRaw(float[] rawllpts, int rawoff, int[] xcoords,
                              int[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        return proj.forwardRaw(rawllpts,
                rawoff,
                xcoords,
                ycoords,
                visible,
                copyoff,
                copylen);
    }

    public boolean forwardRaw(double[] rawllpts, int rawoff, int[] xcoords,
                              int[] ycoords, boolean[] visible, int copyoff,
                              int copylen) {
        return proj.forwardRaw(rawllpts,
                rawoff,
                xcoords,
                ycoords,
                visible,
                copyoff,
                copylen);
    }

    public ArrayList<int[]> forwardRect(Point2D ll1, Point2D ll2) {
        return proj.forwardRect(ll1, ll2);
    }

    public Shape forwardShape(Shape shape) {
        return proj.forwardShape(shape);
    }

    public <T extends Point2D> T getCenter() {
        return (T) proj.getCenter();
    }

    public <T extends Point2D> T getCenter(T fillInThis) {
        return proj.getCenter(fillInThis);
    }

    public int getHeight() {
        return proj.getHeight();
    }

    public <T extends Point2D> T getLowerRight() {
        return (T) proj.getLowerRight();
    }

    public float getMaxScale() {
        return proj.getMaxScale();
    }

    public float getMinScale() {
        return proj.getMinScale();
    }

    public String getName() {
        return proj.getName();
    }

    public String getProjectionID() {
        return proj.getProjectionID();
    }

    public float getScale() {
        return proj.getScale();
    }

    public <T extends Point2D> float getScale(T ll1, T ll2, Point2D point1,
                                              Point2D point2) {
        return proj.getScale(ll1, ll2, point1, point2);
    }

    public Length getUcuom() {
        return proj.getUcuom();
    }

    public <T extends Point2D> T getUpperLeft() {
        return (T) proj.getUpperLeft();
    }

    public int getWidth() {
        return proj.getWidth();
    }

    public <T extends Point2D> T inverse(Point2D point) {
        return (T) proj.inverse(point);
    }

    public <T extends Point2D> T inverse(Point2D point2D, T llpt) {
        return proj.inverse(point2D, llpt);
    }

    public <T extends Point2D> T inverse(double x, double y) {
        return (T) proj.inverse(x, y);
    }

    public <T extends Point2D> T inverse(double x, double y, T llpt) {
        return proj.inverse(x, y, llpt);
    }

    public boolean isPlotable(Point2D point) {
        return proj.isPlotable(point);
    }

    public boolean isPlotable(float lat, float lon) {
        return proj.isPlotable(lat, lon);
    }

    public boolean isPlotable(double lat, double lon) {
        return proj.isPlotable(lat, lon);
    }

    public Projection makeClone() {
        return null;
    }

    public void pan(float Az, float c) {
        proj.pan(Az, c);
    }

    public void pan(float Az) {
        proj.pan(Az);
    }

}
