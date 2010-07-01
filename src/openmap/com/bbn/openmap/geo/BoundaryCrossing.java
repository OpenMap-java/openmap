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
//$RCSfile: BoundaryCrossing.java,v $
//$Revision: 1.5 $
//$Date: 2007/02/13 20:02:13 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The BoundaryCrossing class represents a location where a path
 * crosses a region. Since a location can represent a region being
 * left and a region being entered, you can ask it for the out
 * GeoRegion and the in GeoRegion. Both won't be null at the same
 * time, but either may be.
 * 
 * @author dietrick
 */
public class BoundaryCrossing {

    /**
     * The Geo location of the crossing.
     */
    protected Geo geo;
    /**
     * The GeoRegion that is being entered.
     */
    protected GeoRegion in;
    /**
     * The GeoRegion that is being exited.
     */
    protected GeoRegion out;

    /**
     * Creates the BoundaryCrossing. The getCrossings() factory method
     * will result in BoundaryCrossings being created.
     * 
     * @param p The Geo location
     * @param r the GeoRegion being entered/exited
     * @param goinin whether the path is goin' in to the region.
     */
    protected BoundaryCrossing(Geo p, GeoRegion r, boolean goinin) {
        geo = p;
        if (goinin) {
            in = r;
        } else {
            out = r;
        }
    }

    public Geo getGeo() {
        return geo;
    }

    public GeoRegion getIn() {
        return in;
    }

    public GeoRegion getOut() {
        return out;
    }

    /**
     * The main factory method to create BoundaryCrossings. Provides a
     * BoundaryCrossing.Collector so that crossing points can be
     * retrieved, as well as an iterator over regions intersected.
     * 
     * @param path GeoPath to travel over
     * @param regions An ExtentIndex filled with GeoRegions.
     * @return BoundaryCrossing.Collector
     */
    public static Collector getCrossings(GeoPath path, Collection regions) {
        Collector collector = new Collector();
        CrossingIntersection crossings = new CrossingIntersection(collector);
        crossings.consider(path, regions);
        return collector;
    }

    /**
     * A Collector is an object that organizes boundary crossings, as
     * discovered by CrossingIntersection class.
     * 
     * @author dietrick
     */
    public static class Collector extends MatchCollector.SetMatchCollector {

        List crossings = new ArrayList(10);
        List lastSegmentCrossingList;
        Geo lastSegmentStartingPoint;

        public Collector() {}

        /**
         * Add a BoundaryCrossing to the collection of crossings.
         * 
         * @param bc
         */
        protected void addCrossing(BoundaryCrossing bc) {
            crossings.add(bc);
        }

        /**
         * Add a BoundaryCrossing associated with a segment and
         * region. This is the main thinkin' method, called by the
         * CrossingInspector with lists of crossings. This method
         * organizes and orders BorderCrossings according to the
         * segment order of the path that caused the crossings.
         * 
         * @param c A list of Geos that a segement intersects with a
         *        region.
         * @param segment GeoSegment
         * @param region GeoRegion
         */
        protected void addCrossing(Collection c, GeoSegment segment,
                                   GeoRegion region) {

            // We need to get all the BorderCrossings from the current
            // segment all together, across regions and place them in
            // order. So if the segments matches with the previous
            // segment, we need to hold off add them to the crossings
            // list until a segment is complete.

            // Compact will probably have to look a the
            // currentSegmentList to add any leftovers.
            Geo start = segment.getSeg()[0];

            if (lastSegmentCrossingList != null
                    && lastSegmentStartingPoint.distance(start) > 0) {
                // The last segment is complete, need to order the BC
                // on the list and add them to the crossings.
                crossings.addAll(lastSegmentCrossingList);
                lastSegmentCrossingList.clear();
            } else if (lastSegmentCrossingList == null) {
                lastSegmentCrossingList = new ArrayList(10);
            }

            // The ordered list is for temporarily holding points for
            // the current segment as they are placed in the right
            // order.
            LinkedList orderedList = new LinkedList();
            // Everything in the lastSegmentCrossingList has already
            // be ordered relative to what's already been searched, so
            // we can just add them now to place the new points around
            // them accordingly. Also, the lastSegmentCrossingList
            // holds BoundaryCrossing objects, and the collection
            // passed into this function doesn't have those yet.
            orderedList.addAll(lastSegmentCrossingList);
            // Clear out the lastSegmetnCrossingList, we'll replenish
            // it with the ordered list at the end, so it will be
            // ready for the next cycle.
            lastSegmentCrossingList.clear();

            for (Iterator it = c.iterator(); it.hasNext();) {
                Geo current = (Geo) it.next();
                double curDist = start.distance(current);
                // We just assume that crossing point is going into
                // the current region, we'll check later to make sure.
                BoundaryCrossing currentBC = new BoundaryCrossing(current, region, true);

                int lastCheckedIndex = 0;
                BoundaryCrossing lastChecked = null;

                for (Iterator it2 = orderedList.iterator(); it2.hasNext(); lastCheckedIndex++) {
                    lastChecked = (BoundaryCrossing) it2.next();
                    if (curDist < start.distance(lastChecked.geo)) {
                        break;
                    } else {
                        lastChecked = null;
                    }
                }

                if (lastChecked != null) {
                    orderedList.add(lastCheckedIndex, currentBC);
                } else {
                    orderedList.add(currentBC);
                }
            }

            boolean goinin = !Intersection.isPointInPolygon(start,
                    region.getPoints());
            for (Iterator it = orderedList.iterator(); it.hasNext();) {
                BoundaryCrossing bc = (BoundaryCrossing) it.next();

                boolean sameRegion = (bc.in == region);

                if (sameRegion) {
                    if (!goinin)
                        bc.out = bc.in;
                    goinin = !goinin;
                }

                lastSegmentCrossingList.add(bc);
            }

            // OK, remember at this point that the BoundaryCrossing
            // Objects have be.in set to their region, and some have
            // region.out set if the boundary reflects an outward
            // motion. We're going to resolve this in compact();

            lastSegmentStartingPoint = start;
        }

        /**
         * An important method that cleans up the segment/region
         * BoundaryCrossing order, and also resolves the
         * BoundaryCrossing.in/out settings for regions. Must be
         * called before the crossing iterator is retrieved, but the
         * BoundaryCrossing method does that.
         */
        protected void compact() {
            if (lastSegmentCrossingList != null
                    && !lastSegmentCrossingList.isEmpty()) {
                crossings.addAll(lastSegmentCrossingList);
                lastSegmentCrossingList.clear();
                lastSegmentCrossingList = null;
            }

            Object[] bc = crossings.toArray();
            crossings = new ArrayList(bc.length);

            BoundaryCrossing current, previous = null;

            for (int i = 0; i < bc.length; i++) {
                current = (BoundaryCrossing) bc[i];

                if (previous != null
                        && previous.geo.distance(current.geo) == 0.0) {
                    // If the distances between points are zero, it's
                    // a border crossing and we want to merge these
                    // into one point, with the bc.out pointer marking
                    // the region being left, and the bc.in marking
                    // the receiving region.
                    if (previous.out != null)
                        previous.in = current.in;
                    if (current.out != null) {
                        previous.out = current.out;
                    }

                    continue;
                } else {
                    // First point or if the distance between points
                    // is not zero, if the bc.out is set, then we
                    // should set the bc.in to null, just to indicate
                    // that the point reflects a crossing from outside
                    // any other region into that particular region.
                    if (current.out != null) {
                        current.in = null;
                    }
                }

                crossings.add(current);
                previous = current;
            }
        }

        public Iterator getCrossings() {
            compact();
            return crossings.iterator();
        }

    }

    /**
     * A Intersection class that knows how to call
     * BoundaryCrossing.Collector to keep track of the path's
     * relationship with the regions.
     */
    public static class CrossingIntersection extends Intersection {

        public CrossingIntersection(Collector collector) {
            super(new MatchFilter.ExactMF(), collector);
        }

        /**
         * Calls Intersection.isSegmentNearRegion() to see if segment
         * is near the region. We're going to assume that this is
         * going to be called with segments ordered as they appear in
         * the path, in case the path is kinda crazy, and then we can
         * keep track of the crossings in the right order.
         */
        public boolean considerSegmentXRegion(GeoSegment segment,
                                              GeoRegion region) {
            List hits = Intersection.segmentNearPoly(segment,
                    region.getPoints(),
                    0.0);

            if (hits != null) {
                ((Collector) collector).addCrossing(hits, segment, region);
                return true;
            }

            return false;
        }

    }

}
