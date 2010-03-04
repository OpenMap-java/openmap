// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Cartesian.java,v $
// $RCSfile: Cartesian.java,v $
// $Revision: 1.4 $
// $Date: 2006/04/07 15:21:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * The Cartesian projection is a non-wrapping, straight-forward scaling
 * projection drawn in 2D. The simplest projection ever, it can be used for
 * regular plotting.
 */
public class Cartesian extends Proj implements Projection, java.io.Serializable {
    /**
     * The Cartesian name.
     */
    public final static transient String CartesianName = "Cartesian";

    /**
     * The coordinate limit of the left side of the projection. If the left side
     * of the map projection would show coordinates more left than this value,
     * the center of the map will be changed so that this value is on the edge.
     */
    protected double leftLimit;
    /**
     * The coordinate limit of the right side of the projection. If the right
     * side of the map projection would show coordinates more right than this
     * value, the center of the map will be changed so that this value is on the
     * edge.
     */
    protected double rightLimit;
    /**
     * The coordinate limit of the top side of the projection. If the top side
     * of the map projection would show coordinates higher than this value, the
     * center of the map will be changed so that this value is on the edge.
     */
    protected double topLimit;
    /**
     * The coordinate limit of the bottom side of the projection. If the bottom
     * side of the map projection would show coordinates lower than this value,
     * the center of the map will be changed so that this value is on the edge.
     */
    protected double bottomLimit;
    /**
     * A point that can be used for force the projection against the limits. Is
     * only used if the limits are set to be something other than infinity.
     */
    protected Point2D limitAnchorPoint;

    protected double scaleFactor;

    protected transient double hWidth;
    protected transient double hHeight;
    protected transient double SFScale;

    protected transient AffineTransform transform1;
    protected transient AffineTransform transform2;
    // protected transient AffineTransform transform3;
    protected transient AffineTransform transform4;

    /**
     * Create a Cartesian projection that does straight scaling, no wrapping.
     * 
     * @param center the coordinates of the center of the map.
     * @param scale the scale to use for the map, referring to the difference of
     *        the ration between pixels versus coordinate values.
     * @param width the pixel width of the map.
     * @param height the pixel height of the map.
     */
    public Cartesian(Point2D center, float scale, int width, int height) {
        super(center, scale, width, height);
    }

    public void init() {
        scaleFactor = 100000000;

        // Limits are in coordinate space, dictating how much coordinate space
        // can be viewable. The center point and scale should be adjusted as
        // appropriate (during computeParameters).
        leftLimit = Double.NEGATIVE_INFINITY;
        rightLimit = Double.POSITIVE_INFINITY;
        topLimit = Double.POSITIVE_INFINITY;
        bottomLimit = Double.NEGATIVE_INFINITY;
    }

    protected void computeParameters() {
        hWidth = width / 2.0;
        hHeight = height / 2.0;
        SFScale = scaleFactor / scale;

        checkLimits();

        transform1 = AffineTransform.getTranslateInstance(-centerX, -centerY);
        transform2 = AffineTransform.getScaleInstance(SFScale, -SFScale);
        // transform3 = AffineTransform.getScaleInstance(1 / SFScale,
        // -1 / SFScale);
        transform4 = AffineTransform.getTranslateInstance(hWidth, hHeight);
    }

    /**
     * The method you want to call. Checks if there are limits set, and will
     * call setLimits(checkScale) properly.
     */
    protected void checkLimits() {
        if (leftLimit != Double.NEGATIVE_INFINITY
                || rightLimit != Double.POSITIVE_INFINITY
                || topLimit != Double.POSITIVE_INFINITY
                || bottomLimit != Double.NEGATIVE_INFINITY) {
            checkLimits(true);
        }
    }

    /**
     * Should only be called if you've checked there are limits set on the
     * projection.
     * 
     * @param checkScale true the first time through, if the scale changes this
     *        method calls itself with false to prevent a loop.
     */
    protected void checkLimits(boolean checkScale) {

        if (limitAnchorPoint != null) {
            centerX = limitAnchorPoint.getX();
            centerY = limitAnchorPoint.getY();
        }

        // Check limits.
        Point2D p2 = checkUpperLimits();
        if (p2 != null) {
            centerX = p2.getX();
            centerY = p2.getY();
        }

        Point2D p1 = checkLowerLimits();
        if (p1 != null) {
            centerX = p1.getX();
            centerY = p1.getY();
        }

        if (checkScale && p1 != null && p2 != null) {
            double newScale = checkScaleAgainstLimits();
            if (newScale != scale) {
                scale = newScale;
                SFScale = scaleFactor / scale;
                checkLimits(false);
            }
        }
    }

    /**
     * Checks the lower limits and returns new center coordinates to keep the
     * limits at the edge if necessary.
     */
    protected Point2D checkLowerLimits() {
        Point2D p1 = new Point2D.Double();
        Point2D p2 = null;
        double moveX = 0;
        double moveY = 0;
        inverse(0, height, p1);// LL corner
        if (p1.getX() < leftLimit) {
            p2 = forward(centerY, leftLimit, p2);
            moveX = p2.getX();
        }

        if (p1.getY() < bottomLimit) {
            p2 = forward(bottomLimit, centerX, p2);
            moveY = height - p2.getY();
        }

        if (p2 != null) {
            inverse(hWidth + moveX, hHeight - moveY, p2);
        }

        return p2;
    }

    /**
     * Checks the upper limits and returns new center coordinates to keep the
     * limits at the edge if necessary.
     */
    protected Point2D checkUpperLimits() {
        Point2D p1 = new Point2D.Double();
        Point2D p2 = null;
        double moveX = 0;
        double moveY = 0;
        inverse(width, 0, p1);// UR corner
        if (p1.getX() > rightLimit) {
            p2 = forward(centerY, rightLimit, p2);
            moveX = width - p2.getX();
        }

        if (p1.getY() > topLimit) {
            p2 = forward(topLimit, centerX, p2);
            moveY = p2.getY();
        }

        if (p2 != null) {
            inverse(hWidth - moveX, hHeight + moveY, p2);
        }

        return p2;
    }

    /**
     * Checks the corner values against the limits and returns the right scale
     * to keep limits at the edge if necessary.
     */
    protected double checkScaleAgainstLimits() {
        Point2D p1 = getUpperLeft();
        Point2D p2 = getLowerRight();

        double x = p1.getX();
        double y = p1.getY();
        if (topLimit != Double.POSITIVE_INFINITY) {
            y = topLimit;
        }

        if (leftLimit != Double.NEGATIVE_INFINITY) {
            x = leftLimit;
        }
        p1.setLocation(x, y);

        x = p2.getX();
        y = p2.getY();
        if (bottomLimit != Double.NEGATIVE_INFINITY) {
            y = bottomLimit;
        }
        if (rightLimit != Double.POSITIVE_INFINITY) {
            x = rightLimit;
        }

        p2.setLocation(x, y);
        // second p1, p2 meaningless
        return getScale(p1, p2, p1, p2);
    }

    /**
     * Forward project a world coordinate into screen space.
     * 
     * @param wy vertical coordinate component in world units.
     * @param wx horizontal coordinate component in world units.
     * @param mapPoint screen point to load result into. OK if null, a new one
     *        will be created and returned.
     * @return Point2D provided or new one created containing map coordinate.
     */
    public Point2D forward(double wy, double wx, Point2D mapPoint) {

        double x = ((wx - centerX) * SFScale) + hWidth;
        double y = hHeight - ((wy - centerY) * SFScale);

        if (mapPoint == null) {
            mapPoint = new Point2D.Double(x, y);
        } else {
            mapPoint.setLocation(x, y);
        }
        /*
         * fPoint1.setLocation(wx, wy); Point2D tmp =
         * transform1.transform(fPoint1, fPoint2); tmp =
         * transform2.transform(tmp, fPoint1); tmp = transform4.transform(tmp,
         * fPoint2); mapPoint.setLocation(tmp.getX(), tmp.getY());
         */
        return mapPoint;
    }

    // Used for AffineTransform forward and inverse methods, to save
    // allocation expense.
    // Point2D fPoint1 = new Point2D.Double();
    // Point2D fPoint2 = new Point2D.Double();
    // Point2D iPoint1 = new Point2D.Double();
    // Point2D iPoint2 = new Point2D.Double();

    /**
     * Inverse projection a map coordinate into world space.
     * 
     * @param x horizontal map coordinate from left side of map.
     * @param y vertical map coordinate from top of map.
     * @param worldPoint a Point2D object to load result into. OK if null, a new
     *        one will be created if necessary.
     * @return Point2D provided or new one if created, containing the result.
     */
    public Point2D inverse(double x, double y, Point2D worldPoint) {
        double worldPointX = (x - hWidth) / SFScale + centerX;
        double worldPointY = (hHeight - y) / SFScale + centerY;

        if (worldPoint == null) {
            worldPoint = new Point2D.Double(worldPointX, worldPointY);
        } else {
            worldPoint.setLocation(worldPointX, worldPointY);
        }
        /*
         * try { iPoint1.setLocation(x, y); Point2D tmp =
         * transform4.inverseTransform(iPoint1, iPoint2); tmp =
         * transform3.transform(tmp, iPoint1); transform1.inverseTransform(tmp,
         * worldPoint); } catch (NoninvertibleTransformException e) {
         * e.printStackTrace(); } catch (Exception e) { e.printStackTrace(); }
         */
        return worldPoint;
    }

    /**
     * @param Az direction, 0 is north, positive is clockwise.
     * @param c number of world coordinates to pan.
     */
    public void pan(float Az, float c) {
        double currentX = centerX;
        double currentY = centerY;

        currentX -= c * Math.sin(Math.toRadians(Az) + Math.PI);
        currentY -= c * Math.cos(Math.toRadians(Az) + Math.PI);

        setCenter(new Point2D.Double(currentX, currentY));
    }

    /**
     * Pan half a view.
     */
    public void pan(float Az) {
        pan(Az, (float) (getUpperLeft().distance(getLowerRight()) / 4.0));
    }

    /**
     * Takes a java.awt.Shape object and re-projects it for a the current view.
     * Returns a GeneralPath.
     */
//    public Shape forwardShape(Shape shape) {
//        return super.forwardShape(shape);
//        // return
//        // transform4.createTransformedShape(transform2.createTransformedShape(transform1.createTransformedShape(shape)));
//
//        // Set Proj.java for the iterator way of doing this.
//    }

    /**
     */
    public String getName() {
        return CartesianName;
    }

    /**
     */
    public float getScale(Point2D ulWorldPoint, Point2D lrWorldPoint,
                          Point2D point1, Point2D point2) {
        try {

            double worldCoords;
            double deltaPix;
            double dx = Math.abs(lrWorldPoint.getX() - ulWorldPoint.getX());
            double dy = Math.abs(lrWorldPoint.getY() - ulWorldPoint.getY());

            if (dx <= dy) {
                worldCoords = dx;
                deltaPix = Math.abs(point2.getX() - point1.getX());
            } else {
                worldCoords = dy;
                deltaPix = Math.abs(point2.getY() - point1.getY());
            }

            // The new scale...
            return (float) (worldCoords / deltaPix * scaleFactor);
        } catch (NullPointerException npe) {
            com.bbn.openmap.util.Debug.error("CartesianProjection.getScale(): caught null pointer exception.");
            return Float.MAX_VALUE;
        }
    }

    public boolean isPlotable(double lat, double lon) {
        return true;
    }

    public Point2D getCenter() {
        return new Point2D.Double(centerX, centerY);
    }

    public double getBottomLimit() {
        return bottomLimit;
    }

    public void setBottomLimit(double bottomLimit) {
        this.bottomLimit = bottomLimit;
        computeParameters();
    }

    public double getLeftLimit() {
        return leftLimit;
    }

    public void setLeftLimit(double leftLimit) {
        this.leftLimit = leftLimit;
        computeParameters();
    }

    public Point2D getLimitAnchorPoint() {
        return limitAnchorPoint;
    }

    public void setLimitAnchorPoint(Point2D limitAnchorPoint) {
        this.limitAnchorPoint = limitAnchorPoint;
        computeParameters();
    }

    public double getRightLimit() {
        return rightLimit;
    }

    public void setRightLimit(double rightLimit) {
        this.rightLimit = rightLimit;
        computeParameters();
    }

    public double getTopLimit() {
        return topLimit;
    }

    public void setTopLimit(double topLimit) {
        this.topLimit = topLimit;
        computeParameters();
    }

    public void setLimits(double top, double bottom, double left, double right,
                          Point2D anchor) {
        this.topLimit = top;
        this.bottomLimit = bottom;
        this.leftLimit = left;
        this.rightLimit = right;
        this.limitAnchorPoint = anchor;
        computeParameters();
    }

}