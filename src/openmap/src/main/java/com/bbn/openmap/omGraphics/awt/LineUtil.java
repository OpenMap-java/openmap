package com.bbn.openmap.omGraphics.awt;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import com.bbn.openmap.MoreMath;

/**
 * Various computations about plane geometry.
 * 
 * @author Eric LEPICIER from Pascal HAURIE 1997
 * @version 11 aot 2002
 */
public class LineUtil {

    private static float FLT_EPSILON = 1.192092896E-07F;

    /**
     * Computes the segment square length
     * 
     * @param A the begin point
     * @param B the end point
     * @return double the square distance between A and B
     */
    public static double norm2(Point2D A, Point2D B) {
        return (A.getX() - B.getX()) * (A.getX() - B.getX()) + (A.getY() - B.getY()) * (A.getY() - B.getY());
    }

    /**
     * Computes the segment length
     * 
     * @param A the begin point
     * @param B the end point
     * @return double the distance between A and B
     */
    public static double norm(Point2D A, Point2D B) {
        return Math.sqrt(norm2(A, B));
    }

    /**
     * Interpolates a point on a segment.
     * 
     * @param A the begin point
     * @param B the end point
     * @param d the distance from A to the wanted point
     * @return Point2D the point at distance d from A on the segment AB, the
     *         point B if d>AB
     */
    public static Point2D interpolatedPoint(Point2D A, Point2D B, double d) {

        double r = Math.sqrt(d * d / norm2(A, B));

        if (r < FLT_EPSILON)
            return A;
        if (1 - r < FLT_EPSILON)
            return B;

        Point2D P = new Point2D.Double(r * B.getX() + (1 - r) * A.getX(), r * B.getY() + (1 - r) * A.getY());
        return P;
    }

    /**
     * Extract a length on a polyline.
     * 
     * @param length the curve length to extract from points
     * @param points the original points
     * @param polysegment the returned points
     * @return true if we got the desired length, false otherwise
     */
    protected static boolean retrievePoints(float length, LinkedList points, LinkedList polysegment) {
        polysegment.clear();

        // first point
        Point2D point = (Point2D) points.removeFirst();
        polysegment.add(point);

        double consumedLength = 0.0;
        double norm = 0.0;
        Point2D nextPoint = null;

        while (consumedLength < length && !points.isEmpty()) {
            // consume points while distance is not reached
            nextPoint = (Point2D) points.removeFirst();
            polysegment.add(nextPoint);
            norm = LineUtil.norm(point, nextPoint);
            consumedLength += norm;
            point = nextPoint;
        }

        if (MoreMath.approximately_equal(consumedLength, length)) {
            // we got the exact distance with an existing point.
            // we need to copy the last point back: it will be the
            // first for the next call
            points.addFirst(point);
            return true;
        } else {
            if (consumedLength > length) {
                // we went too far, we need to put back the last point
                points.addFirst(polysegment.removeLast());
                consumedLength -= norm;
                // and interpolate a new point
                point = (Point2D) polysegment.getLast();
                double d = length - consumedLength;
                // between point and nextPoint at distance d
                Point2D interp = LineUtil.interpolatedPoint(point, nextPoint, d);
                polysegment.add(interp);
                points.addFirst(interp);
                return true;
            } else {
                // no more points !
                return false;
            }
        }
    }
}