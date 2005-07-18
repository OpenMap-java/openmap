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
//$RCSfile: RegionIndexImpl.java,v $
//$Revision: 1.3 $
//$Date: 2005/07/18 22:18:08 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Separable indexed database for Regional BoundingCircles. This is
 * currently a simple longitude coverage map of the world, broken into
 * buckets covering 1 degrees. A given BoundingCircle will show up in
 * every bucket that it touches or comes within 30nm of.
 */
public class RegionIndexImpl extends java.util.AbstractCollection implements
        RegionIndex {

    /**
     * how many buckets in the longitudinal index - 360 means 1 bucket
     * per degree of longitude. More than 360 doesn't seem to add much
     * search speed, less than 180 makes it slower. The sweet spot on
     * current datasets is somewhere in between.
     */
    public static final int NBUCKETS = 360;

    /**
     * how much of a margin to put around regions for indexing
     * purposes, in nautical miles. This must be at least the largest
     * margin searched for by route (currently 50nmiles) - the larger
     * this value, the larger the average entries/bucket and so, the
     * slower the search.
     */
    public static final double nmMargin = 50.0;

    /** all is a collection of everything successfully indexed. */
    protected final List all = new ArrayList(2000);

    protected final List buckets[] = new List[NBUCKETS];

    /**
     * polar is a bucket for anything that is near enough to either
     * pole to cover more than 1/2 the buckets.
     */
    protected final List polar = new ArrayList();

    protected final List discarded = new ArrayList();

    /**
     * Method to call to add Region object with BoundingCircle to
     * Collection and organize it for later retrieval.
     * 
     * @param region Region to index
     * @return true if object added, false if it's been discarded.
     */
    public boolean addRegion(Region region) {
        boolean ret = false;
        try {

            BoundingCircle bc = region.getBoundingCircle();
            if (bc == null) {
                discarded.add(region);
                return false;
            }

            Geo center = bc.getCenter();
            double clon = center.getLongitude();
            double clat = center.getLatitude();
            double radd = Geo.degrees(bc.getRadius());

            if ((clat == 90.0 && clon == -180.0) || radd >= 90.0) {
                discarded.add(region);
            } else {
                all.add(region); // add to the everything list

                // we need to project the radius away from the
                // center at the latitude, NOT at the equator!
                double latfactor = Geo.npdAtLat(clat);
                if (latfactor == 0) {
                    polar.add(region);
                    ret = true;
                } else {
                    double xd = nmMargin / latfactor; // 50 nm
                    /*
                     * margin = xd "extra degrees" at the center's
                     * latitude
                     */
                    if (xd >= 45) {
                        polar.add(region);
                        ret = true;
                    } else {
                        int lon1 = (int) Math.floor((180 + clon - (xd + radd))
                                * NBUCKETS / 360.0);
                        int lon2 = (int) Math.ceil((180 + clon + (xd + radd))
                                * NBUCKETS / 360.0);
                        for (int i = lon1; i <= lon2; i++) {
                            int x = i;
                            if (x >= NBUCKETS)
                                x = x - NBUCKETS;
                            if (x < 0)
                                x = x + NBUCKETS;
                            List b = buckets[x];
                            if (b == null) {
                                b = new ArrayList(5);
                                buckets[x] = b;
                            }
                            b.add(region);
                            ret = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    /**
     * Called when you want everything in each bucket between the
     * coordinates.
     * 
     * @param left left-most (west) bucket value.
     * @param right right-most (east) bucket value.
     * @return Iterator over regions in buckets that cover range
     *         provided.
     */
    protected Iterator lookup(float left, float right) {
        return lookup(left, right, null);
    }

    /**
     * Called when you want to get the regions in the buckets, but you
     * want to further filter on objects that can intersect based on
     * the bounding circle provided.
     * 
     * @param left left-most (west) bucket value.
     * @param right right-most (east) bucket value.
     * @param bc Bounding circle to do another filter check, if null,
     *        everything in a bucket will be returned.
     * @return Iterator over regions in buckets that cover range
     *         provided that intersect with the BoundingCircle (if one
     *         is provided).
     */
    protected Iterator lookup(float left, float right, BoundingCircle bc) {
        Set s = new HashSet();
        int l = (int) Math.floor((left + 180) * NBUCKETS / 360.0f);
        int r = (int) Math.ceil((right + 180) * NBUCKETS / 360.0f);
        if (r < l) {
            r = r + NBUCKETS;
        }
        for (int i = l; i <= r; i++) {
            int x = i;
            if (x >= NBUCKETS)
                x = x - NBUCKETS;
            if (x < 0)
                x = x + NBUCKETS;
            List b = buckets[x];
            if (b != null) {
                if (bc == null) {
                    s.addAll(b);
                } else {
                    for (Iterator it = b.iterator(); it.hasNext();) {
                        Region region = (Region) it.next();
                        if (bc.intersects(region.getBoundingCircle())) {
                            s.add(region);
                        }
                    }
                }
            }
        }
        s.addAll(polar); // add all the polar regions, just in case
        return s.iterator();
    }

    /**
     * Method to call to remove a region from the index.
     * 
     * @return true if the region was found and removed.
     */
    public boolean removeRegion(Region region) {
        boolean ret = false;
        try {

            BoundingCircle bc = region.getBoundingCircle();
            if (bc == null) {
                discarded.add(region);
                return false;
            }

            Geo center = bc.getCenter();
            double clon = center.getLongitude();
            double clat = center.getLatitude();
            double radd = Geo.degrees(bc.getRadius());

            if ((clat == 90.0 && clon == -180.0) || radd >= 90.0) {
                discarded.remove(region);
            } else {
                all.remove(region); // add to the everything list

                // we need to project the radius away from the
                // center at the latitude, NOT at the equator!
                double latfactor = Geo.npdAtLat(clat);
                if (latfactor == 0) {
                    polar.remove(region);
                    ret = true;
                } else {
                    double xd = nmMargin / latfactor; // 50 nm
                    /*
                     * margin = xd "extra degrees" at the center's
                     * latitude
                     */
                    if (xd >= 45) {
                        polar.remove(region);
                        ret = true;
                    } else {
                        int lon1 = (int) Math.floor((180 + clon - (xd + radd))
                                * NBUCKETS / 360.0);
                        int lon2 = (int) Math.ceil((180 + clon + (xd + radd))
                                * NBUCKETS / 360.0);
                        for (int i = lon1; i <= lon2; i++) {
                            int x = i;
                            if (x >= NBUCKETS)
                                x = x - NBUCKETS;
                            if (x < 0)
                                x = x + NBUCKETS;
                            List b = buckets[x];
                            if (b == null) {
                                b = new ArrayList(5);
                                buckets[x] = b;
                            }
                            b.remove(region);
                            ret = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    /**
     * Method to call to clear out the index.
     */
    public void clearRegions() {
        all.clear();
        polar.clear();
        discarded.clear();
        for (int i = 0; i < buckets.length; i++) {
            buckets[i].clear();
        }
    }

    /**
     * RegionIndex parameter method.
     * 
     * @return horizontal range in nautical miles for matches.
     */
    public double indexHorizontalRange() {
        return nmMargin;
    }

    /**
     * @param segment to check against Collection's BoundingCircles
     *        for Regions.
     * @return Iterator of Regions that pertain to segment.
     */
    protected Iterator segmentMatches(GeoSegment segment) {
        Geo[] pts = segment.getSeg();
        float left = (float) pts[0].getLongitude();
        float right = left;
        float x = (float) pts[1].getLongitude();
        if (x - left > 180) {
            x = x - 360;
        } // if x crossed the dateline going east...
        if (left - x > 180) {
            x = x + 360;
        } // going west
        if (x < left)
            left = x;
        if (x > right)
            right = x;

        return lookup(left, right, new BoundingCircle.Impl(segment.getSeg()));
    }

    /**
     * @param path A Path to check against Collection's
     *        BoundingCircles for Regions.
     * @return Iterator of Regions that pertain to path.
     */
    protected Iterator pathMatches(Path path) {
        Set results = new HashSet();
        Path.SegmentIterator pit = path.segmentIterator();
        while (pit.hasNext()) {
            GeoSegment seg = pit.nextSegment();
            for (Iterator it = segmentMatches(seg); it.hasNext();) {
                results.add(it.next());
            }
        }
        return results.iterator();
    }

    /**
     * @param p a Geo to check for intersections against Collection's
     *        BoundingCircles for Regions.
     * @return Iterator of Regions that pertain to point.
     */
    public Iterator iterator(BoundingCircle bc) {
        float lon = (float) bc.getCenter().getLongitude();
        float radius = (float)Math.toDegrees(bc.getRadius());
        return lookup(lon - radius, lon + radius, bc);
    }

    /**
     * @return an Iterator over BoundingCircle objects in the
     *         Collection where the GExtent may be related to them.
     */
    public Iterator iterator(GeoExtent o) {
        if (o instanceof GeoSegment) {
            return segmentMatches((GeoSegment) o);
        } else if (o instanceof Path) {
            return pathMatches((Path) o);
        } else if (o instanceof GeoPoint) {
            return iterator(new BoundingCircle.Impl(((GeoPoint)o).getPoint(), 0));
        } else {
            return Collections.EMPTY_LIST.iterator();
        }
    }

    /**
     * @return Iterator over all entries in Collection.
     */
    public Iterator iterator() {
        return all.iterator();
    }

    /**
     * @return number of all entries in Collection.
     */
    public int size() {
        return all.size();
    }

    //
    // metrics
    //

    public String toString() {
        int entc = 0;
        int empties = 0;
        for (int i = 0; i < NBUCKETS; i++) {
            List l = buckets[i];
            if (l != null) {
                entc += l.size();
            } else {
                empties++;
            }
        }

        return "RegionIndexImpl[" + size() + " -" + discarded.size() + " E"
                + (entc / ((float) NBUCKETS)) + "]";
    }
}