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

import java.util.Iterator;

/**
 * Iterate along a sequence of Ribbons. A Ribbon is a three Geo set, with a
 * RIGHT, CENTER and LEFT Geo in the Ribbon. The location of the LEFT and RIGHT
 * Geo are perpendicular to the great circle path that the CENTER Geo resides
 * on.
 */
public class RibbonIterator
        implements Iterator<Ribbon>, Iterable<Ribbon> {

    protected Geo v1;
    protected Geo v2;
    protected double radius;
    protected Geo gc;
    protected Rotation rotator;
    protected Geo point;
    protected double distance;
    protected boolean hasNext;

    /**
     * Return an iterator that returns Ribbons along the great circle between v1
     * and v2. The Ribbon points are radius radians apart, and each Ribbon is
     * 2*radius apart.
     */
    public RibbonIterator(Geo v1, Geo v2, double radius) {
        this(v1, v2, radius, 2.0 * radius);
    }

    /**
     * Return an iterator that returns Ribbons along the great circle between v1
     * and v2. The Ribbon points are radius radians apart, and each Ribbon is
     * rotationIntervalDist apart.
     * 
     * @param v1 from this Geo
     * @param v2 to this Geo
     * @param radius distance away from great circle lines between geos, in
     *        radians.
     * @param rotationIntervalDist interval distance between ribbons, in
     *        radians. You want this to be smaller than the distance between the
     *        geos, obviously.
     */
    public RibbonIterator(Geo v1, Geo v2, double radius, double rotationIntervalDist) {
        this.v1 = v1;
        this.v2 = v2;
        this.distance = v1.distance(v2);
        this.radius = radius;
        this.gc = v1.crossNormalize(v2);
        this.rotator = new Rotation(gc, rotationIntervalDist);
        this.point = v1;
        this.hasNext = true;
    }

    public Ribbon next() {
        if (point != v2 && v1.distance(point) < distance) {
            Ribbon result = new Ribbon(point, gc, radius);
            point = rotator.rotate(point);
            return result;
        } else {
            point = v2;
            return new Ribbon(point, gc, radius);
        }
    }

    public boolean hasNext() {
        return point != v2;
    }

    public void remove() {
    }

    public Iterator<Ribbon> iterator() {
        return this;
    }
}
