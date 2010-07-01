//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: ExtentIndex.java,v $
//$Revision: 1.4 $
//$Date: 2007/02/13 20:02:09 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A Collection of Regions that supports indexed subsets. That is, in addition
 * to acting like a normal collection, it also allows getting an iterator that
 * will return a superset of all intersecting regions that is a subset of the
 * whole collection.
 * 
 * @author mthome@bbn.com
 */
public interface ExtentIndex extends java.util.Collection {
    /**
     * report on the maximum horizontalRange supported by this index.
     */
    double indexHorizontalRange();

    /**
     * Add a extent to the index.
     * 
     * @param region
     * @return true if Region successfully added, false if not.
     */
    boolean addExtent(GeoExtent region);

    /**
     * Remove a region from the index.
     * 
     * @param region
     * @return true if the region was found and removed.
     */
    boolean removeExtent(GeoExtent region);

    /**
     * Resets the index to an empty state.
     */
    void clear();

    /**
     * return an iterator listing a subset of the whole collection that is a
     * superset of the actual matches. A valid (but inefficient) implementation
     * would return an iterator over the whole collection.
     * 
     * Implementation should match anything that is likely to match - this will
     * generally include, for instance, additional space around the actual
     * segment to accommodate buffer zones around the segment.
     */
    Iterator iterator(GeoExtent extent);

    /**
     * A basic implementation of ExtentIndex that uses Collection-typed buckets.
     * Extending classes must implement #makeBucket(int) to specify an
     * alternative Collection implementation.
     */
    abstract class AbstractExtentIndex extends java.util.AbstractCollection
            implements ExtentIndex {

        /**
         * Default value for #nbuckets if not specified in the call to the
         * constructor.
         */
        public static final int D_NBUCKETS = 360;

        /**
         * Default value for #margin if not specified in the call to the
         * constructor.
         */
        public static final double D_MARGIN = 0.0;

        /**
         * how many buckets in the longitudinal index - 360 means 1 bucket per
         * degree of longitude. More than 360 doesn't seem to add much search
         * speed, less than 180 makes it slower. The sweet spot on current
         * datasets is somewhere in between.
         * 
         * If unspecified, defaults to #D_NBUCKETS
         */
        public final int nbuckets;

        /**
         * how much of a margin to put around regions for indexing purposes, in
         * nautical miles. This must be at least the largest margin searched for
         * by route (currently 50nmiles) - the larger this value, the larger the
         * average entries/bucket and so, the slower the search.
         * 
         * If unspecified, defaults to #D_MARGIN
         */
        public final double margin;

        protected final Collection buckets[];

        /** all is a collection of everything successfully indexed. */
        protected final Collection all;
        /**
         * polar is a bucket for anything that is near enough to either pole to
         * cover more than 1/2 the buckets.
         */
        protected final Collection polar;

        protected final Collection discarded;

        public AbstractExtentIndex() {
            this(D_NBUCKETS, D_MARGIN);
        }

        public AbstractExtentIndex(int nb) {
            this(nb, D_MARGIN);
        }

        public AbstractExtentIndex(double m) {
            this(D_NBUCKETS, m);
        }

        public AbstractExtentIndex(int nb, double m) {
            nbuckets = nb;
            margin = m;
            buckets = new Collection[nbuckets];
            all = makeBucket(2000);
            polar = makeBucket();
            discarded = makeBucket();
        }

        protected final Collection makeBucket() {
            return makeBucket(0);
        }

        /**
         * implement to specify the factory to use to create Bucket storage.
         * 
         * @param sizeHint a guess at the number of elements that are likely to
         *        be stored in this bucket or 0 if unknown.
         * @return A Collection instance suitable for use as a bucket
         * 
         */
        abstract protected Collection makeBucket(int sizeHint);

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
         * Method to call to add Region object with BoundingCircle to Collection
         * and organize it for later retrieval.
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
                         * margin = xd "extra degrees" at the center's latitude
                         */
                        if (xd >= 45) {
                            polar.add(extent);
                            ret = true;
                        } else {
                            double[] lons = normalizeLons(new double[] {
                                    clon - xd, clon + xd });
                            int lb = bucketFor(lons[0]);
                            int rb = bucketFor(lons[1]);
                            if (rb < lb)
                                rb += nbuckets;
                            for (int i = lb; i <= rb; i++) {
                                int x = i % nbuckets;
                                Collection b = buckets[x];
                                if (b == null) {
                                    b = makeBucket(5);
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

        /**
         * figure out what bucket a particular longitude goes in.
         */
        protected final int bucketFor(double lon) {
            return (int) Math.floor(normalizeLon(lon) / 360.0
                    * (double) nbuckets);
        }

        /*
         * Normalize and sort the argument two element array so that on a
         * north-up globe, a great-circle arc between the points is headed
         * eastward and is less than half-way around the world. @param lons
         * two-element array on longitudes @return the mutated argument.
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
         * @return Iterator over regions in buckets that cover range provided.
         */
        protected Iterator lookup(double left, double right) {
            return lookup(left, right, null);
        }

        /**
         * Called when you want to get the regions in the buckets, but you want
         * to further filter on objects that can intersect based on the bounding
         * circle provided.
         * 
         * @param left left-most (west) bucket value.
         * @param right right-most (east) bucket value.
         * @param bc Bounding circle to do another filter check, if null,
         *        everything in a bucket will be returned.
         * @return Iterator over regions in buckets that cover range provided
         *         that intersect with the BoundingCircle (if one is provided).
         */
        protected Iterator lookup(double left, double right, BoundingCircle bc) {
            Collection s = null;
            int lb = bucketFor(left);
            int rb = bucketFor(right);
            if (rb < lb)
                rb += nbuckets;
            for (int i = lb; i <= rb; i++) {
                Collection b = buckets[i % nbuckets];
                if (b != null) {
                    if (bc == null) {
                        if (s == null) {
                            s = new HashSet();
                        }
                        s.addAll(b);
                    } else {
                        for (Iterator it = b.iterator(); it.hasNext();) {
                            GeoExtent region = (GeoExtent) it.next();
                            if (bc.intersects(region.getBoundingCircle())) {
                                if (s == null) {
                                    s = new HashSet();
                                }
                                s.add(region);
                            }
                        }
                    }
                }
            }
            if (!polar.isEmpty()) {
                if (s == null) {
                    s = new HashSet();
                }
                s.addAll(polar); // add all the polar regions, just in case
            }
            if (s == null) {
                return Collections.EMPTY_SET.iterator();
            } else {
                return s.iterator();
            }
        }

        /**
         * Method to call to remove a region from the index.
         * 
         * @return true if the region was found and removed.
         */
        public boolean removeExtent(GeoExtent region) {
            boolean ret = false;
            BoundingCircle bc = region.getBoundingCircle();
            if (bc == null) {
                return discarded.remove(region);
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
                    ret = ret || polar.remove(region);
                } else {
                    double xd = (rnm + margin) / latfactor;
                    /*
                     * margin = xd "extra degrees" at the center's latitude
                     */
                    if (xd >= 45) {
                        ret = ret || polar.remove(region);
                    } else {
                        double[] lons = normalizeLons(new double[] { clon - xd,
                                clon + xd });
                        int lb = bucketFor(lons[0]);
                        int rb = bucketFor(lons[1]);
                        if (rb < lb)
                            rb += nbuckets;
                        for (int i = lb; i <= rb; i++) {
                            int x = i % nbuckets;
                            Collection b = buckets[x];
                            if (b != null) {
                                ret = ret || b.remove(region);
                                if (b.isEmpty()) {
                                    buckets[x] = null;
                                }
                            }
                        }
                    }
                }
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
            Collection results = null;
            GeoPath.SegmentIterator pit = path.segmentIterator();
            while (pit.hasNext()) {
                GeoSegment seg = pit.nextSegment();
                for (Iterator it = lookupBySegment(seg); it.hasNext();) {
                    if (results == null) {
                        results = new HashSet();
                    }
                    results.add(it.next());
                }
            }
            if (results == null) {
                return Collections.EMPTY_SET.iterator();
            } else {
                return results.iterator();
            }
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
         * @return an Iterator over BoundingCircle objects in the Collection
         *         where the GExtent may be related to them.
         */
        public Iterator iterator(GeoExtent o) {
            if (o instanceof GeoSegment) {
                return lookupBySegment((GeoSegment) o);
            } else if (o instanceof GeoRegion) {
                // It's important that GeoRegion be tested before GeoPath,
                // because the GeoPath will catch GeoRegions, and doing the
                // lookup by path for a region may cause the lookup to fail for
                // extents near the center of the region (outside of the
                // bounding circles of the region's segments).
                return lookupByBoundingCircle(o.getBoundingCircle());
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
                Collection l = buckets[i];
                if (l != null) {
                    entc += l.size();
                } else {
                    empties++;
                }
            }

            return this.getClass().getName() + "[" + size() + " -"
                    + discarded.size() + " E" + (entc / ((float) nbuckets))
                    + "]";
        }
    }

    class HashSetExtentIndexImpl extends AbstractExtentIndex {
        public HashSetExtentIndexImpl() {
            this(D_NBUCKETS, D_MARGIN);
        }

        public HashSetExtentIndexImpl(int nb) {
            this(nb, D_MARGIN);
        }

        public HashSetExtentIndexImpl(double m) {
            this(D_NBUCKETS, m);
        }

        public HashSetExtentIndexImpl(int nb, double m) {
            super(nb, m);
        }

        protected Collection makeBucket(int sizeHint) {
            if (sizeHint != 0) {
                return new HashSet();
            } else {
                return new HashSet(sizeHint);
            }
        }
    }

    class ArrayListExtentIndexImpl extends AbstractExtentIndex {
        public ArrayListExtentIndexImpl() {
            this(D_NBUCKETS, D_MARGIN);
        }

        public ArrayListExtentIndexImpl(int nb) {
            this(nb, D_MARGIN);
        }

        public ArrayListExtentIndexImpl(double m) {
            this(D_NBUCKETS, m);
        }

        public ArrayListExtentIndexImpl(int nb, double m) {
            super(nb, m);
        }

        protected Collection makeBucket(int sizeHint) {
            if (sizeHint != 0) {
                return new ArrayList();
            } else {
                return new ArrayList(sizeHint);
            }
        }
    }

}
