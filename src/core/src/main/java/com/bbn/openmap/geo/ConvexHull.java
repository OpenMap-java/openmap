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
//$RCSfile: ConvexHull.java,v $
//$Revision: 1.5 $
//$Date: 2008/04/15 21:51:01 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

/**
 * This class contains static methods that can be used to create convex hull
 * GeoRegions from arrays of Geos. The only algorithm implemented is Graham's,
 * where the highest point is selected (called the pivot), the other points are
 * sorted according to their relative azimuths from the pivot, and then a path
 * is created around the other points. Any right turn encountered traversing the
 * points means that point should be skipped when creating the convex hull.
 * 
 * @author dietrick
 */
public class ConvexHull {
    private ConvexHull() {}

    /**
     * Using Graham's scan.
     * 
     * @param geos
     * @return GeoRegion outlining the convex hull of the geos
     */
    public static final GeoRegion getRegion(Geo[] geos) {
        Geo[] regionGeos = hull(geos);
        return new GeoRegion.Impl(regionGeos);
    }

    /**
     * Using Graham's scan.
     * 
     * @param geos
     * @return a convex hull of the geos
     */
    public static final Geo[] hull(Geo[] geos) {
        return hull(geos, 0);
    }

    /**
     * Using Graham's scan.
     * 
     * @param geos
     * @param tolerance the distance between points where they would be
     *        considered equals, in radians.
     * @return a convex hull of the geos
     */
    public static final Geo[] hull(Geo[] geos, double tolerance) {
        Geo pivot = findHighest(geos);
        TreeSet sortedGeos = new TreeSet(new PivotAngleComparator(pivot));
        for (int i = 0; i < geos.length; i++) {
            Geo g = geos[i];
            if (g != pivot) {
                sortedGeos.add(g);
            }
        }

        Stack hullStack = new Stack();
        hullStack.push(pivot);

        Geo gCross, midCross = null;
        Geo geo = null, endGeo = null, midGeo = null;

        Iterator sortedGeoIt = sortedGeos.iterator();
        if (sortedGeoIt.hasNext()) {
            midGeo = (Geo) sortedGeoIt.next();

            while (midGeo.distance(pivot) == 0 && sortedGeoIt.hasNext()) {
                midGeo = (Geo) sortedGeoIt.next();
            }
        }

        Geo lastGeoRead = midGeo;

        while (sortedGeoIt.hasNext() && midGeo != null) {
            geo = (Geo) sortedGeoIt.next();
            double dist = geo.distance(lastGeoRead);
            if (dist <= tolerance) {
                // Debug.output("Skipping duplicate geo");
                continue;
            }

            endGeo = (Geo) hullStack.peek();

            midCross = endGeo.crossNormalize(midGeo);
            gCross = midGeo.crossNormalize(geo);
            Geo i = gCross.crossNormalize(midCross).antipode();

            // Debug.output("Evaluating:\n\tendGeo: " + endGeo + "\n\tmidGeo: "
            // + midGeo + "\n\tto " + geo
            // + "\n ****** intersection point: " + i);

            if (midGeo.distance(i) < Math.PI / 2) {
                // Debug.output("+++++++++++++ midGeo to hull");

                // left turn, OK for hull
                hullStack.push(midGeo);
                endGeo = midGeo;
                midGeo = geo;

            } else {

                // right turn, need to backtrack
                while (hullStack.size() > 1) {

                    // Debug.output("-------- midGeo dropped");

                    midGeo = (Geo) hullStack.pop();
                    endGeo = (Geo) hullStack.peek();

                    midCross = endGeo.crossNormalize(midGeo);
                    gCross = midGeo.crossNormalize(geo);
                    i = gCross.crossNormalize(midCross).antipode();

                    // Debug.output("Evaluating:\n\tendGeo: " + endGeo
                    // + "\n\tmidGeo: " + midGeo + "\n\tto " + geo
                    // + "\n ****** intersection point: " + i);

                    if (midGeo.distance(i) < Math.PI / 2) {

                        // Debug.output("+++++++++++++ midGeo to hull");

                        hullStack.push(midGeo);
                        midGeo = geo;
                        break;
                    }
                }
            }

            lastGeoRead = geo;
        }

        if (midGeo != null) {
            hullStack.push(midGeo);
        }

        hullStack.push(pivot);

        Geo[] regionGeos = new Geo[hullStack.size()];

        int i = 0;
        // Need to reverse order to get inside of poly on the right side of
        // line.
        while (!hullStack.isEmpty()) {
            regionGeos[i++] = (Geo) hullStack.pop();
        }

        return regionGeos;
    }

    protected static Geo findHighest(Geo[] geos) {
        Geo ret = null;
        double highest = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < geos.length; i++) {
            double lat = geos[i].getLatitude();
            if (lat > highest) {
                highest = lat;
                ret = geos[i];
            }
        }
        return ret;
    }

    // XXX: does this need to be serializable?
    protected static final class PivotAngleComparator implements Comparator,
            Serializable {
        private Geo pivot;

        public PivotAngleComparator(Geo pivot) {
            this.pivot = pivot;
        }

        public int compare(Object obj1, Object obj2) {
            double ang1 = Double.MAX_VALUE, ang2 = Double.MAX_VALUE;
            int ret = 0;

            if (obj1 instanceof Geo) {
                ang1 = Math.toDegrees(pivot.azimuth((Geo) obj1));
            }

            if (obj2 instanceof Geo) {
                ang2 = Math.toDegrees(pivot.azimuth((Geo) obj2));
            }

            // ts1 is the one being tested/added to the TreeSet, so we
            // want later items with the same time being added after
            // previous items in the file with the same time.

            if (ang1 < ang2) {
                ret = 1;
            } else if (ang1 >= ang2) {
                ret = -1;
            }

            return ret;
        }

        public Geo getPivot() {
            return pivot;
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            if (obj instanceof PivotAngleComparator) {
                return pivot.equals(((PivotAngleComparator) obj).pivot);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return pivot.hashCode();
        }
    }

}
