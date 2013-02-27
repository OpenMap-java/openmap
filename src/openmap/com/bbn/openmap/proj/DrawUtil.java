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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/DrawUtil.java,v $
// $RCSfile: DrawUtil.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.bbn.openmap.MoreMath;

/**
 * Drawing utility functions.
 */
public class DrawUtil {

    // cannot construct
    private DrawUtil() {}

    /**
     * Generate additional vertices between two points.
     * <p>
     * 
     * @param x1 x coord
     * @param y1 y coord
     * @param x2 x coord
     * @param y2 y coord
     * @param n num segments
     * @param include_last include the last one?
     * @param ret_val the array to put them in
     * @return int[] ret_val
     */
    public final static int[] lineSegments(int x1, int y1, int x2, int y2,
                                           int n, boolean include_last,
                                           int[] ret_val) {
        if (n <= 0) {
            ret_val = new int[2];
            ret_val[0] = x1;
            ret_val[1] = y1;
            return ret_val;
        }
        float dx = x2 - x1;
        float dy = y2 - y1;
        int end = include_last ? n + 1 : n;
        end <<= 1;
        float inc = 1f / (float) n;
        float t = inc;

        // add all the vertices in x,y order
        ret_val[0] = x1;
        ret_val[1] = y1;
        for (int i = 2; i < end; i += 2, t += inc) {
            ret_val[i] = x1 + (int) (dx * t);
            ret_val[i + 1] = y1 + (int) (dy * t);
        }
        return ret_val;
    }

    /**
     * Returns n or n+1 points along a line.
     * <p>
     * 
     * @param pt1 point
     * @param pt2 point
     * @param n count
     * @param include_last boolean
     * @return Point[]
     */
    public final static Point[] lineSegments(Point pt1, Point pt2, int n,
                                             boolean include_last) {

        Point v = new Point(pt2.x - pt1.x, pt2.y - pt1.y);
        int end = include_last ? n + 1 : n;
        Point[] ret_val = new Point[end];
        float inc = 1f / (float) n;
        float t = inc;

        ret_val[0] = pt1;
        for (int i = 1; i < end; i++, t += inc) {
            ret_val[i] = new Point(pt1.x + (int) ((float) v.x * t), pt1.y
                    + (int) ((float) v.y * t));
        }
        return ret_val;
    }

    /**
     * Bresenham's line algorithm.
     * <p>
     * Returns an array of points to draw.
     * <p>
     * 
     * @param pt1 point
     * @param pt2 point
     * @return Point[]
     * 
     */
    public final static Point[] bresenham_line(Point pt1, Point pt2) {
        return bresenham_line(pt1.x, pt1.y, pt2.x, pt2.y);
    }

    /**
     * Bresenham's line algorithm.
     * <p>
     * 
     * @param x1 horizontal pixel window location of first point.
     * @param y1 vertical pixel window location of first point.
     * @param x2 horizontal pixel window location of second point.
     * @param y2 vertical pixel window location of second point.
     * @return Point[]
     * 
     */
    public final static Point[] bresenham_line(int x1, int y1, int x2, int y2) {
        // This is actually NOT bresenhams algorithm. It is faster!
        // -rmf

        // Debug.output("DrawUtil.bresenham_line(" +
        // x1 + "," + y1 + ")->(" + x2 + "," + y2 + ")");
        int i;
        int d, x, y, ax, ay, sx, sy, dx, dy, t;

        dx = x2 - x1;
        ax = Math.abs(dx) << 1;
        sx = MoreMath.sign(dx);
        dy = y2 - y1;
        ay = Math.abs(dy) << 1;
        sy = MoreMath.sign(dy);

        t = Math.max(Math.abs(dx), Math.abs(dy)) + 1;
        Point[] ret_val = new Point[t];

        x = x1;
        y = y1;
        if (ax > ay) { /* x dominant */
            d = ay - (ax >> 1);
            for (i = 0;;) {
                ret_val[i++] = new Point(x, y);
                // ret_val[i].x = x; ret_val[i++].y = y;
                if (x == x2)
                    return ret_val;
                if (d >= 0) {
                    y += sy;
                    d -= ax;
                }
                x += sx;
                d += ay;
            }
        } else { /* y dominant */
            d = ax - (ay >> 1);
            for (i = 0;;) {
                ret_val[i++] = new Point(x, y);
                // ret_val[i].x = x; ret_val[i++].y = y;
                if (y == y2)
                    return ret_val;
                if (d >= 0) {
                    x += sx;
                    d -= ay;
                }
                y += sy;
                d += ax;
            }
        }
    }

    /**
     * Tests if a point is inside a polygon.
     * <p>
     * 
     * @param xpts horizontal pixel window points of polygon.
     * @param ypts vertical pixel window points of polygon.
     * @param ptx horizontal pixel window points of location
     * @param pty vertical pixel window points of location.
     * @return boolean
     */
    public final static boolean inside_polygon(float[] xpts, float[] ypts,
                                               double ptx, double pty) {

        int j, inside_flag = 0;
        int numverts = xpts.length;
        if (numverts <= 2)
            return false;
        Point2D vtx0 = new Point2D.Float(), vtx1 = new Point2D.Float();
        double dv0; // prevents OVERFLOW!!
        int crossings = 0;
        boolean xflag0 = false, yflag0 = false, yflag1 = false;

        vtx0.setLocation(xpts[numverts - 1], ypts[numverts - 1]);
        // get test bit for above/below Y axis
        yflag0 = ((dv0 = vtx0.getY() - pty) >= 0);

        for (j = 0; j < numverts; j++) {
            if ((j & 0x1) != 0) { // HACK - slightly changed
                vtx0.setLocation(xpts[j], ypts[j]);
                yflag0 = ((dv0 = vtx0.getY() - pty) >= 0);
            } else {
                vtx1.setLocation(xpts[j], ypts[j]);
                yflag1 = (vtx1.getY() >= pty);
            }

            /*
             * check if points not both above/below X axis - can't hit ray
             */
            if (yflag0 != yflag1) {
                /* check if points on same side of Y axis */
                if ((xflag0 = (vtx0.getX() >= ptx)) == (vtx1.getX() >= ptx)) {
                    if (xflag0)
                        crossings++;
                } else {
                    crossings += ((vtx0.getX() - dv0 * (vtx1.getX() - vtx0.getX())
                            / (vtx1.getY() - vtx0.getY())) >= ptx) ? 1 : 0;
                }
            }
            inside_flag = crossings & 0x01;
        }
        return (inside_flag != 0);
    }

    /**
     * Returns the distance from Point (x,y) to the closest line segment in the
     * Poly (int[] xpts, int[] ypts).
     * <p>
     * This procedure assumes that xpts.length == ypts.length.
     * <p>
     * 
     * @param xpts X points of the polygon
     * @param ypts Y points of the polygon
     * @param ptx x location of the point
     * @param pty y location of the point
     * @param connected polyline or polygon
     */
    public final static float closestPolyDistance(float[] xpts, float[] ypts,
                                                  double ptx, double pty,
                                                  boolean connected) {
        if (xpts.length == 0)
            return Float.POSITIVE_INFINITY;
        if (xpts.length == 1)
            return (float) distance(xpts[0], ypts[0], ptx, pty);

        float temp, distance = Float.POSITIVE_INFINITY;
        int i, j;

        for (i = 0, j = 1; j < xpts.length; i++, j++) {
            temp = (float) distance_to_line(xpts[i],
                    ypts[i],
                    xpts[j],
                    ypts[j],
                    ptx,
                    pty);
            // Debug.output(
            // "\tdistance from line (" + from.x + "," + from.y +
            // "<->" +
            // to.x + "," + to.y + ") to point (" + ptx + "," + pty +
            // ")=" +
            // temp);
            if (temp < distance)
                distance = temp;
        }

        // connect
        if (connected) {
            temp = (float) distance_to_line(xpts[i],
                    ypts[i],
                    xpts[0],
                    ypts[0],
                    ptx,
                    pty);
            if (temp < distance)
                distance = temp;
        }
        return distance;
    }

    /**
     * 2D distance formula.
     * <p>
     * 
     * @param x1 x coord
     * @param y1 y coord
     * @param x2 x coord
     * @param y2 y coord
     * @return float distance
     */
    public final static float distance(float x1, float y1, float x2, float y2) {
        double xdiff = x2 - x1;
        double ydiff = y2 - y1;
        return (float) Math.sqrt((xdiff * xdiff + ydiff * ydiff));
    }

    /**
     * 2D distance formula.
     * <p>
     * 
     * @param x1 x coord
     * @param y1 y coord
     * @param x2 x coord
     * @param y2 y coord
     * @return float distance
     */
    public final static float distance(int x1, int y1, int x2, int y2) {
        double xdiff = x2 - x1;
        double ydiff = y2 - y1;
        return (float) Math.sqrt((xdiff * xdiff + ydiff * ydiff));
    }

    /**
     * 2D distance formula.
     * <p>
     * 
     * @param x1 x coord
     * @param y1 y coord
     * @param x2 x coord
     * @param y2 y coord
     * @return double distance
     */
    public final static double distance(double x1, double y1, double x2,
                                        double y2) {
        double xdiff = x2 - x1;
        double ydiff = y2 - y1;
        return Math.sqrt((xdiff * xdiff + ydiff * ydiff));
    }

    /**
     * Calculate the "pixel distance" between two points (squaring not
     * involved).
     * <p>
     * 
     * @param x1 x coord
     * @param y1 y coord
     * @param x2 x coord
     * @param y2 y coord
     * @return int pixel distance
     */
    public final static int pixel_distance(int x1, int y1, int x2, int y2) {

        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx > dy) ? dx : dy;
    }

    /**
     * Distance to closest endpoint.
     * <p>
     * 
     * @param x1 x coord
     * @param y1 y coord
     * @param x2 x coord
     * @param y2 y coord
     * @param x x coord of point
     * @param y y coord of point
     * @return float distance to endpoint
     */
    public final static float distance_to_endpoint(int x1, int y1, int x2,
                                                   int y2, int x, int y) {

        return (float) Math.min(distance(x1, y1, x, y), distance(x2, y2, x, y));
    }

    /**
     * Compute distance from point to line segment.
     * <p>
     * Compute the distance from point (x,y) to a line by computing the
     * perpendicular line from (x,y) to the line and finding the intersection of
     * this perpendicular and the line. If the intersection is on the line
     * segment, then the distance is the distance from the mouse to the
     * intersection, otherwise it is the distance from (x,y) to the nearest
     * endpoint.
     * <p>
     * Equations used to compute distance: <br>
     * <ul>
     * <li>m = (y2-y1)/(x2-x1) slope of the line
     * <li>y = mx + b equation of the line
     * <li>c = -1/m slope of line perpendicular to it
     * <li>y = cx + d equation of perpendicular line
     * <li>xi = (d-b)/(m-c) x-intersection, from equating the 2 line equations
     * <li>y1 = c* xi + d y-intersection
     * <li>distance = sqrt(sqr(x-xi) + sqr(y-yi)) distance between two points
     * </ul>
     * 
     * @param x1 line x coord1
     * @param y1 line y coord1
     * @param x2 line x coord2
     * @param y2 line y coord2
     * @param x point x coord
     * @param y point y coord
     * @return float distance to line segment
     * @deprecated USE THE NEW FUNCTION
     * 
     */
    public final static float OLD_distance_to_line(int x1, int y1, int x2,
                                                   int y2, int x, int y) {

        float m; /* slope of the line */
        float c; /* slope of a line perpendicular to the line */
        float b; /* y intercept of line */
        float d; /* y intercept of a line perpendicular to the line */
        int xi, yi; /* intersection of line and perpendicular */

        /* vertical line */
        if (x2 == x1) {
            if (y1 <= y && y <= y2 || y2 <= y && y <= y1)
                return (float) Math.abs(x - x1); // mouse is alongside
            // line
            return distance_to_endpoint(x1, y1, x2, y2, x, y);
        }

        /* horizontal line */
        if (y2 == y1) {
            if (x1 <= x && x <= x2 || x2 <= x && x <= x1)
                return (float) Math.abs(y - y1); // mouse is alongside
            // line
            return distance_to_endpoint(x1, y1, x2, y2, x, y);
        }

        m = ((float) (y2 - y1)) / ((float) (x2 - x1)); /*
                                                        * slope of the line
                                                        */
        c = -1.0f / m; /* slope of perpendicular line */
        d = (float) y - c * (float) x;/*
                                       * perpendicular line through mouse
                                       */
        b = (float) y1 - m * (float) x1; /* the line in the drawing */

        // NOTE: round error
        xi = (int) ProjMath.qint((d - b) / (m - c));// x intersection
        yi = (int) ProjMath.qint(c * (float) xi + d);// y intersection

        /*
         * If intersection is on the line segment distance is distance from
         * mouse to it.
         */
        if ((x1 <= xi && xi <= x2 || x2 <= xi && xi <= x1)
                && (y1 <= yi && yi <= y2 || y2 <= yi && yi <= y1))
            return distance(xi, yi, x, y);

        /* distance is distance from mouse to nearest endpt */
        return distance_to_endpoint(x1, y1, x2, y2, x, y);
    }

    /**
     * Compute perpendicular distance from point to line.
     * <p>
     * 
     * @param x1 line x coord1
     * @param y1 line y coord1
     * @param x2 line x coord2
     * @param y2 line y coord2
     * @param x3 point x coord
     * @param y3 point y coord
     * @return float distance to line
     * 
     */
    public final static float perpendicular_distance_to_line(int x1, int y1,
                                                             int x2, int y2,
                                                             int x3, int y3) {

        int x12 = x2 - x1;
        int y12 = y2 - y1;
        int x13 = x3 - x1;
        int y13 = y3 - y1;
        float D12 = distance(x1, y1, x2, y2);

        return Math.abs((/* Math.abs */(x12 * y13) - /* Math.abs */(x13 * y12))
                / D12);
    }

    /**
     * Computes the distance from a point to a line segment.
     * <p>
     * Variable usage as follows:
     * <p>
     * <ul>
     * <li>x12 x distance from the first endpoint to the second.
     * <li>y12 y distance from the first endpoint to the second.
     * <li>x13 x distance from the first endpoint to point being tested.
     * <li>y13 y distance from the first endpoint to point being tested.
     * <li>x23 x distance from the second endpoint to point being tested.
     * <li>y23 y distance from the second endpoint to point being tested.
     * <li>D12 Length of the line segment.
     * <li>pp distance along the line segment to the intersection of the
     * perpendicular from the point to line extended.
     * </ul>
     * 
     * Procedure:
     * <p>
     * 
     * Compute D12, the length of the line segment. Compute pp, the distance to
     * the perpendicular. If pp is negative, the intersection is before the
     * start of the line segment, so return the distance from the start point.
     * If pp exceeds the length of the line segment, then the intersection is
     * beyond the end point so return the distance of the point from the end
     * point. Otherwise, return the absolute value of the length of the
     * perpendicular line. The sign of the length of the perpendicular line
     * indicates whether the point lies to the right or left of the line as one
     * travels from the start point to the end point.
     * <p>
     * 
     * @param x1 line x coord1
     * @param y1 line y coord1
     * @param x2 line x coord2
     * @param y2 line y coord2
     * @param x3 point x coord
     * @param y3 point y coord
     * @return float distance to line segment
     * 
     */
    public final static double distance_to_line(double x1, double y1,
                                                double x2, double y2,
                                                double x3, double y3) {

        // algorithm courtesy of Ray 1/16/98
        double x12 = x2 - x1;
        double y12 = y2 - y1;
        double x13 = x3 - x1;
        double y13 = y3 - y1;
        double D12 = Math.sqrt(x12 * x12 + y12 * y12);
        double pp = (x12 * x13 + y12 * y13) / D12;
        if (pp < 0.0) {
            return (float) Math.sqrt(x13 * x13 + y13 * y13);
        }
        if (pp > D12) {
            double x23 = x3 - x2;
            double y23 = y3 - y2;
            return Math.sqrt(x23 * x23 + y23 * y23);
        }
        return Math.abs(((x12 * y13 - y12 * x13) / D12));
    }

    /**
     * Generates a line with width lw, returns an ArrayList of 4 x-y coords.
     * <p>
     * 
     * @param lw line width
     * @param x1 line x coord1
     * @param y1 line y coord1
     * @param x2 line x coord2
     * @param y2 line y coord2
     * @return ArrayList<int[]> of x[], y[]
     */
    public static ArrayList<int[]> generateWideLine(int lw, int x1, int y1,
                                                    int x2, int y2) {

        ArrayList<int[]> ret_val = new ArrayList<int[]>(2);
        int[] x = new int[4];
        int[] y = new int[4];

        // calculate the offsets
        // lw = lw -1;
        int off1 = (int) lw / 2;
        int off2 = (lw % 2 != 0) ? (int) lw / 2 + 1 : (int) lw / 2;

        // slope <= 1
        if (Math.abs((float) (y2 - y1) / (float) (x2 - x1)) <= 1f) {
            x[0] = x[3] = x1;
            x[1] = x[2] = x2;

            y[0] = y1 + off1;
            y[1] = y2 + off1;
            y[2] = y2 - off2;
            y[3] = y1 - off2;

            ret_val.add(x);
            ret_val.add(y);
        }

        // slope > 1
        else {
            x[0] = x1 + off1;
            x[1] = x2 + off1;
            x[2] = x2 - off2;
            x[3] = x1 - off2;

            y[0] = y[3] = y1;
            y[1] = y[2] = y2;

            ret_val.add(x);
            ret_val.add(y);
        }

        return ret_val;
    }

    /**
     * Generates a polygon or polyline with positive width lw.
     * <p>
     * Returns ArrayList of x-y array pairs of coordinates of polygon segments.
     * the parameter altx must either be null, or an alternate array of points
     * to draw.
     * <p>
     * 
     * @param lw line width
     * @param xpts int[] x coords
     * @param ypts int[] y coords
     * @param altx int[] altx coords
     * @param connect polygon or polyline?
     * @return ArrayList<int[]> of x[], y[]
     * 
     */
    final public static ArrayList<int[]> generateWidePoly(int lw, int[] xpts,
                                                          int[] ypts,
                                                          int[] altx,
                                                          boolean connect) {

        return generateWidePoly(lw, xpts.length, xpts, ypts, altx, connect);
    }

    /**
     * Generates a polygon or polyline with positive width lw.
     * <p>
     * Returns ArrayList of x-y array pairs of coordinates of polygon segments.
     * the parameter altx must either be null, or an alternate array of points
     * to draw.
     * <p>
     * 
     * @param lw line width
     * @param len numcoords
     * @param xpts int[] x coords
     * @param ypts int[] y coords
     * @param altx int[] altx coords
     * @param connect polygon or polyline?
     * @return ArrayList<int[]> of x[], y[]
     * 
     */
    final public static ArrayList<int[]> generateWidePoly(int lw, int len,
                                                          int[] xpts,
                                                          int[] ypts,
                                                          int[] altx,
                                                          boolean connect) {

        // HACK - altx deprecated?
        ArrayList<int[]> ret_val = new ArrayList<int[]>(len * 4);
        int off1 = 0, off2 = 0;
        int[] x = null, y = null, a_x = null;
        float slope;

        int end = (connect) ? len : len - 1;
        if (len <= 1)
            return new ArrayList<int[]>();
        // lw = lw -1;

        // calculate the offsets - HACK: +1 side not consistent...
        off1 = (int) lw / 2;
        off2 = (int) Math.ceil((float) lw / 2f);

        // System.out.print("DrawUtil.generateWidePoly Points for
        // lw="+lw);
        // for (int i=0;i<len;i++) {
        // System.out.print("(" + xpts[i] + "," + ypts[i] + ")");
        // }
        // Debug.output("");

        for (int i = 0, j = (i + 1) % len; i < end; i++, j = (i + 1) % len) {
            x = new int[4];
            y = new int[4];

            // handle division by zero
            if (xpts[i] == xpts[j])
                slope = Float.POSITIVE_INFINITY;
            else
                slope = Math.abs((float) (ypts[j] - ypts[i])
                        / (float) (xpts[j] - xpts[i]));

            // slope <= 1
            if (slope <= 1f) {
                x[0] = x[3] = xpts[i];
                x[1] = x[2] = xpts[j];

                y[0] = ypts[i] + off1;
                y[1] = ypts[j] + off1;
                y[2] = ypts[j] - off2;
                y[3] = ypts[i] - off2;

                ret_val.add(x);
                ret_val.add(y);

                if (altx != null) {
                    a_x = new int[4];
                    a_x[0] = a_x[3] = altx[i];
                    a_x[1] = a_x[2] = altx[j];
                    ret_val.add(a_x);
                    ret_val.add(y);
                }
            }

            // slope > 1
            else {
                x[0] = xpts[i] + off1;
                x[1] = xpts[j] + off1;
                x[2] = xpts[j] - off2;
                x[3] = xpts[i] - off2;

                y[0] = y[3] = ypts[i];
                y[1] = y[2] = ypts[j];

                ret_val.add(x);
                ret_val.add(y);

                if (altx != null) {
                    a_x = new int[4];
                    a_x[0] = altx[i] + off1;
                    a_x[1] = altx[j] + off1;
                    a_x[2] = altx[j] - off2;
                    a_x[3] = altx[i] - off2;
                    ret_val.add(a_x);
                    ret_val.add(y);
                }
            }
        }
        return ret_val;
    }

    /*
     * public static void main(String[] args) {
     * 
     * 
     * Debug.output("distance_to_line(0,0,4,4, 2,0): " +
     * distance_to_line(0,0,4,4, 2,0));
     * Debug.output("OLD_distance_to_line(0,0,4,4, 2,0): " +
     * OLD_distance_to_line(0,0,4,4, 2,0));
     * Debug.output("distance_to_line(0,0,4,4, 50,50): " +
     * distance_to_line(0,0,4,4, 50,50));
     * Debug.output("OLD_distance_to_line(0,0,4,4, 50,50): " +
     * OLD_distance_to_line(0,0,4,4, 50,50));
     * Debug.output("distance_to_line(-34,12,44,104, -44,-50): " +
     * distance_to_line(-34,12,44,104, -44,-50));
     * Debug.output("OLD_distance_to_line(-34,12,44,104, -44,-50): " +
     * OLD_distance_to_line(-34,12,44,104, -44,-50)); System.exit(0); // 3-4-5
     * triangle Debug.output(distance(0,0,3,4));
     * Debug.output(distance(0,0,-3,4)); Debug.output(distance(0,0,-3,-4));
     * Debug.output(distance(0,0,3,-4)); Debug.output();
     * 
     * Debug.output(distance_to_line(0,0,2,2, 0,2)); // root 2
     * Debug.output(distance_to_line(0,0,2,0, 0,2)); // 2
     * Debug.output(distance_to_line(0,0,2,0, -1,-1)); // root 2
     * Debug.output(distance_to_line(0,0,2,0, 1,0)); // 0
     * Debug.output(distance_to_line(0,0,2,2, 1,0)); // rounded! Debug.output();
     * 
     * int[] xpts = new int[3]; int[] ypts = new int[3]; xpts[0] = 0; ypts[0] =
     * 0; xpts[1] = 3; ypts[1] = 0; xpts[2] = 3; ypts[2] = 4;
     * 
     * Debug.output(closestPolyDistance(xpts, ypts, 0,4, true));
     * Debug.output(closestPolyDistance(xpts, ypts, 0,4, false));//3
     * 
     * xpts[0] = 0; ypts[0] = 0; xpts[1] = 2; ypts[1] = 0; xpts[2] = 2; ypts[2]
     * = 2; Debug.output(closestPolyDistance(xpts, ypts, 0,1, true));//round
     * Debug.output(closestPolyDistance(xpts, ypts, 0,1, false));//1 //
     * linewidth testing
     * 
     * Debug.output(""); ArrayList vec = generateWideLine(3, 0, 0, 5, 5);
     * 
     * int[] x = (int[])vec.elementAt(0); int[] y = (int[])vec.elementAt(1);
     * System.out.print("wide line: "); for (int i = 0; i <x.length; i++) {
     * System.out.print(x[i] + "," + y[i] + " "); } Debug.output("");
     * 
     * Debug.output(""); vec = generateWideLine(4, 0, 0, -5, -3);
     * 
     * x = (int[])vec.elementAt(0); y = (int[])vec.elementAt(1);
     * System.out.print("wide line: "); for (int i = 0; i <x.length; i++) {
     * System.out.print(x[i] + "," + y[i] + " "); } Debug.output("");
     * Debug.output("");
     * 
     * xpts = new int[4]; ypts = new int[4]; xpts[0] = 0; ypts[0] = 0; xpts[1] =
     * 5; ypts[1] = 2; xpts[2] = 4; ypts[2] = 8; xpts[3] = -2; ypts[3] = 6; vec
     * = generateWidePoly(3, xpts, ypts, null, false); int size = vec.size();
     * for (int j = 0; j < size; j+=2) { x = (int[])vec.elementAt(j); y =
     * (int[])vec.elementAt(j+1); System.out.print("wide poly: "); for (int i =
     * 0; i <x.length; i++) { System.out.print(x[i] + "," + y[i] + " "); }
     * Debug.output(""); } }
     */
}