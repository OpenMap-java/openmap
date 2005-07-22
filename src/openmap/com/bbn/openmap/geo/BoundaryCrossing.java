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
//$Revision: 1.2 $
//$Date: 2005/07/22 21:22:48 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BoundaryCrossing {

    protected Geo geo;
    protected GeoRegion in;
    protected GeoRegion out;

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

    public static Collector getCrossings(GeoPath path, Collection regions) {
        Collector collector = new Collector();
        CrossingIntersection crossings = new CrossingIntersection(collector);
        crossings.consider(path, regions);
        return collector;
    }

    public static class Collector extends MatchCollector.SetMatchCollector {

        List crossings = new ArrayList(10);
        List lastSegmentCrossingList;
        Geo lastSegmentStartingPoint;
        
        public Collector() {}

        protected void addCrossing(BoundaryCrossing bc) {
            crossings.add(bc);
        }
        
        protected void addCrossing(Collection c, GeoSegment segment,
                                   GeoRegion region) {

            // We need to get all the BorderCrossings from the current
            // segment all together, across regions and place them in
            // order. So if the segments matche with the previous
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

            LinkedList orderedList = new LinkedList();

            orderedList.addAll(lastSegmentCrossingList);
            lastSegmentCrossingList.clear();

            for (Iterator it = c.iterator(); it.hasNext();) {
                Geo current = (Geo) it.next();
                double curDist = start.distance(current);
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
                    region.toPointArray());
            for (Iterator it = orderedList.iterator(); it.hasNext();) {
                BoundaryCrossing bc = (BoundaryCrossing) it.next();

                if (region == bc.in && !goinin) {
                    bc.out = bc.in;
                    bc.in = null;
                }

                lastSegmentCrossingList.add(bc);
                goinin = !goinin;
            }

            lastSegmentStartingPoint = start;
        }

        protected void compact() {
            if (lastSegmentCrossingList != null
                    && lastSegmentCrossingList.size() > 0) {
                crossings.addAll(lastSegmentCrossingList);
                lastSegmentCrossingList.clear();
                lastSegmentCrossingList = null;
            }

            Object[] bc = crossings.toArray();
            crossings = new ArrayList(bc.length);

            BoundaryCrossing current, previous = null;

            for (int i = 0; i < bc.length; i++) {
                current = (BoundaryCrossing) bc[i];

                if (previous != null && previous.geo.distance(current.geo) == 0.0) {
                    if (previous.in == null)
                        previous.in = current.in;
                    if (previous.out == null) {
                        previous.out = current.out;
                    }
                    
                    continue;
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
     * Used by the intersection methods to control the behavior of the
     * geometric matches and to call back into external code, for
     * instance, to collect results. A new instance is (and must be)
     * used each time.
     * <p>
     * This implementation requires that setMatchParameters be called
     * prior to starting the match.
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
        public boolean considerSegmentXRegion(GeoSegment segment, GeoRegion region) {
            List hits = Intersection.segmentNearPoly(segment,
                    region.toPointArray(),
                    0.0);

            if (hits != null) {
                ((Collector)collector).addCrossing(hits, segment, region);
                return true;
            }

            return false;
        }

        /**
         * Does a reverse check to see if any point of region is
         * within r, giving an indication if region is entirely within
         * r. All other intersection situations should be caught by
         * consider(segment, region).
         */
        public boolean consider(GeoRegion r, GeoRegion region) {
            // since we just want crossings, this check is moot. The
            // consider(segment, region) method should be used.
            return false;
        }
    }

}
