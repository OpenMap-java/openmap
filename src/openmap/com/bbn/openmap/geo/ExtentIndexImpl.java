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
//$RCSfile: ExtentIndexImpl.java,v $
//$Revision: 1.2 $
//$Date: 2005/07/29 13:09:24 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Separable indexed database for Regional BoundingCircles. This is
 * currently a simple longitude coverage map of the world, broken into
 * buckets covering 1 degrees. A given BoundingCircle will show up in
 * every bucket that it touches or comes within the margin.
 */
public class ExtentIndexImpl extends java.util.AbstractCollection implements
        ExtentIndex {

    /**
     * Default value for #nbuckets if not specified in the call to the
     * constructor.
     */
    public static final int D_NBUCKETS = 360;

    /**
     * Default value for #margin if not specified in the call to the
     * constructor.
     */
    public static final double D_MARGIN = 50.0;

    /**
     * how many buckets in the longitudinal index - 360 means 1 bucket
     * per degree of longitude. More than 360 doesn't seem to add much
     * search speed, less than 180 makes it slower. The sweet spot on
     * current datasets is somewhere in between.
     * 
     * If unspecifed, defaults to #D_NBUCKETS
     */
    public final int nbuckets;

    /**
     * how much of a margin to put around regions for indexing
     * purposes, in nautical miles. This must be at least the largest
     * margin searched for by route (currently 50nmiles) - the larger
     * this value, the larger the average entries/bucket and so, the
     * slower the search.
     * 
     * If unspecfied, defaults to #D_MARGIN
     */
    public final double margin;

    protected final List buckets[];

    /** all is a collection of everything successfully indexed. */
    protected final List all = new ArrayList(2000);
    /**
     * polar is a bucket for anything that is near enough to either
     * pole to cover more than 1/2 the buckets.
     */
    protected final List polar = new ArrayList();

    protected final List discarded = new ArrayList();

    public ExtentIndexImpl() {
        nbuckets = D_NBUCKETS;
        margin = D_MARGIN;
        buckets = new List[nbuckets];
    }

    public ExtentIndexImpl(int nb) {
        nbuckets = nb;
        margin = D_MARGIN;
        buckets = new List[nbuckets];
    }

    public ExtentIndexImpl(int nb, double m) {
        nbuckets = nb;
        margin = m;
        buckets = new List[nbuckets];
    }

    public ExtentIndexImpl(double m) {
        nbuckets = D_NBUCKETS;
        margin = m;
        buckets = new List[nbuckets];
    }

    /**
     * Add an object to the index.
     * 
     * @return true if object is a GeoExtent and was added.
     */
    public boolean add(Object o) {
        if (o instanceof GeoExtent) {
            return addExtent((GeoExtent) o);
        } else {
            return false;
        }
    }

    /**
     * Method to call to add Region object with BoundingCircle to
     * Collection and organize it for later retrieval.
     * 
     * @param extent Region to index
     * @return true if object added, false if it's been discarded.
     */
    public boolean addExtent(GeoExtent extent) {
        boolean ret = false;
        try {

            BoundingCircle bc = extent.getBoundingCircle();
            if (bc == null) {
                discarded.add(extent);
                return false;
            }

            Geo center = bc.getCenter();
            double clon = center.getLongitude();
            double clat = center.getLatitude();
            double rnm = Geo.nm(bc.getRadius());

            if ((clat == 90.0 && clon == -180.0) || rnm >= 90 * 60) {
                discarded.add(extent);
            } else {
                all.add(extent); // add to the everything list

                // we need to project the radius away from the
                // center at the latitude, NOT at the equator!
                double latfactor = Geo.npdAtLat(clat);
                if (latfactor == 0) {
                    polar.add(extent);
                    ret = true;
                } else {
                    double xd = (rnm + margin) / latfactor;
                    /*
                     * margin = xd "extra degrees" at the center's
                     * latitude
                     */
                    if (xd >= 45) {
                        polar.add(extent);
                        ret = true;
                    } else {
                        double[] lons = normalizeLons(new double[] { clon - xd,
                                clon + xd });
                        int lb = bucketFor(lons[0]);
                        int rb = bucketFor(lons[1]);
                        if (rb < lb)
                            rb = rb + nbuckets;
                        for (int i = lb; i <= rb; i++) {
                            int x = i % nbuckets;
                            List b = buckets[x];
                            if (b == null) {
                                b = new ArrayList(5);
                                buckets[x] = b;
                            }
                            b.add(extent);
                            ret = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    /** normalize longitude to be at least 0.0 and less than 360 * */
    protected static final double normalizeLon(double lon) {
        // put it into the range of [-360.0, +360.0]
        double n = lon % 360;
        return (n < 0.0) ? n + 360.0 : n;
        // now n is (0.0,+360]
    }

    /** figure out what bucket a particular longitude goes in * */
    protected final int bucketFor(double lon) {
        return (int) Math.floor(normalizeLon(lon) / nbuckets); // which
        // bucket?
    }

    /*
     * Normalize and sort the argument two element array so that on a
     * north-up globe, a great-circle arc between the points is headed
     * eastward and is less than half-way around the world. @param
     * lons two-element array on longitudes @return the mutated
     * argument.
     */
    protected final static double[] normalizeLons(double[] lons) {
        double a = normalizeLon(lons[0]);
        double b = normalizeLon(lons[1]);
        // if wide and east or narrow and west, swap
        if ((Math.abs(b - a) > 180.0) == (b > a)) {
            lons[0] = b;
            lons[1] = a;
        } else {
            lons[0] = a;
            lons[1] = b;
        }
        return lons;
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
    protected Iterator lookup(double left, double right) {
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
    protected Iterator lookup(double left, double right, BoundingCircle bc) {
        Set s = new HashSet();
        int lb = bucketFor(left);
        int rb = bucketFor(right);
        if (rb < lb)
            rb = rb + nbuckets;
        for (int i = lb; i <= rb; i++) {
            List b = buckets[i % nbuckets];
            if (b != null) {
                if (bc == null) {
                    s.addAll(b);
                } else {
                    for (Iterator it = b.iterator(); it.hasNext();) {
                        GeoRegion region = (GeoRegion) it.next();
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
    public boolean removeExtent(GeoExtent region) {
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
            double rnm = Geo.nm(bc.getRadius());

            if ((clat == 90.0 && clon == -180.0) || rnm >= 90 * 60) {
                discarded.remove(region);
            } else {
                all.remove(region); // remove from the everything list

                // we need to project the radius away from the
                // center at the latitude, NOT at the equator!
                double latfactor = Geo.npdAtLat(clat);
                if (latfactor == 0) {
                    polar.remove(region);
                    ret = true;
                } else {
                    double xd = (rnm + margin) / latfactor;
                    /*
                     * margin = xd "extra degrees" at the center's
                     * latitude
                     */
                    if (xd >= 45) {
                        polar.remove(region);
                        ret = true;
                    } else {
                        double[] lons = normalizeLons(new double[] { clon - xd,
                                clon + xd });
                        int lb = bucketFor(lons[0]);
                        int rb = bucketFor(lons[1]);
                        if (rb < lb)
                            rb = rb + nbuckets;
                        for (int i = lb; i <= rb; i++) {
                            int x = i % nbuckets;
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
    public void clear() {
        all.clear();
        polar.clear();
        discarded.clear();
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null) {
                buckets[i].clear();
            }
        }
    }

    /**
     * RegionIndex parameter method.
     * 
     * @return horizontal range in nautical miles for matches.
     */
    public double indexHorizontalRange() {
        return margin;
    }

    public Iterator lookupBySegment(GeoSegment segment) {
        Geo[] pts = segment.getSeg();
        double[] lons = normalizeLons(new double[] { pts[0].getLongitude(),
                pts[1].getLongitude() });
        return lookup(lons[0], lons[1], segment.getBoundingCircle());
    }

    public Iterator lookupByPath(GeoPath path) {
        Set results = new HashSet();
        GeoPath.SegmentIterator pit = path.segmentIterator();
        while (pit.hasNext()) {
            GeoSegment seg = pit.nextSegment();
            for (Iterator it = lookupBySegment(seg); it.hasNext();) {
                results.add(it.next());
            }
        }
        return results.iterator();
    }

    public Iterator lookupByBoundingCircle(BoundingCircle bc) {
        double cLon = bc.getCenter().getLongitude();
        double rNM = Geo.nm(bc.getRadius()); // radius in nm at
        // equator
        double npd = Geo.npdAtLat(bc.getCenter().getLatitude());
        if (npd == 0) { // avoid divide by zero - polar region
            return iterator();
        } else {
            double rdeg = rNM / npd;
            if (rdeg >= 180) {
                return iterator(); // radius covers the whole world
            } else {
                return lookup(cLon - rdeg, cLon + rdeg, bc);
            }
        }
    }

    /**
     * @return an Iterator over BoundingCircle objects in the
     *         Collection where the GExtent may be related to them.
     */
    public Iterator iterator(GeoExtent o) {
        if (o instanceof GeoSegment) {
            return lookupBySegment((GeoSegment) o);
        } else if (o instanceof GeoPath) {
            return lookupByPath((GeoPath) o);
        } else if (o instanceof GeoPoint) {
            return lookupByBoundingCircle(new BoundingCircle.Impl(((GeoPoint) o).getPoint(), 0));
        } else {
            return lookupByBoundingCircle(o.getBoundingCircle());
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
        for (int i = 0; i < nbuckets; i++) {
            List l = buckets[i];
            if (l != null) {
                entc += l.size();
            } else {
                empties++;
            }
        }

        return "RegionIndexImpl[" + size() + " -" + discarded.size() + " E"
                + (entc / ((float) nbuckets)) + "]";
    }
}