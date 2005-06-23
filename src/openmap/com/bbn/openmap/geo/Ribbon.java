/**
 *                     RESTRICTED RIGHTS LEGEND
 *
 *                        BBNT Solutions LLC
 *                        A Verizon Company
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                         (617) 873-3000
 *
 * Copyright BBNT Solutions LLC 2001, 2002 All Rights Reserved
 *
 */

package com.bbn.openmap.geo;

/**
 * Represents 3 points across great circle, see RibbonIterator.
 * <p>
 * The three points, are
 * <ul>
 * <li>LEFT, a point radius radians to the left of the great circle.
 * <li>CENTER, a point on the great circle.
 * <li>RIGHT, a point radius radians to the right of the great
 * circle.
 * </ul>
 */
public class Ribbon {

    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;

    private Geo[] point;

    /**
     * Get the Geo for the Ribbon.
     * 
     * @param which LEFT, RIGHT or CENTER
     * @return Geo
     */
    public Geo get(int which) {
        return point[which];
    }

    /**
     * @param which LEFT, RIGHT or CENTER
     * @return latitude of Geo.
     */
    public double getLatitude(int which) {
        return point[which].getLatitude();
    }

    /**
     * @param which LEFT, RIGHT or CENTER
     * @return longitude of Geo
     */
    public double getLongitude(int which) {
        return point[which].getLongitude();
    }

    /**
     * Create a Ribbon at the point x along the great circle who's
     * normal is gc, and radius is the distance appart in radians.
     */
    public Ribbon(Geo x, Geo gc, double radius) {
        Geo v = x.crossNormalize(gc);
        Rotation r = new Rotation(v, radius);
        Geo left = r.rotate(x);
        point = new Geo[] { left, x, x.add(x.subtract(left)) };
    }
}
