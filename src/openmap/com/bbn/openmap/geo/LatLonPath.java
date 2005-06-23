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
//$RCSfile: LatLonPath.java,v $
//$Revision: 1.1 $
//$Date: 2005/06/23 22:57:40 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

/**
 * An implementation of Path that takes an alternating lat/lon array
 * and (optionally) an array of altitudes.
 */
public class LatLonPath implements Path {
    protected Geo[] pts;
    protected int length;

    /**
     * Create a path of LatLon pairs.
     * @param lls alternating lat/lon in decimal degrees.
     */
    public LatLonPath(float[] lls) {
        this(lls, true);
    }
    
    /**
     * Create a path of LatLon pairs.
     * @param lls alternating lat/lon values.
     * @param isDegrees true if lat/lon are in degrees, false if in radians.
     */
    public LatLonPath(float[] lls, boolean isDegrees) {
        int al = lls.length;
        length = al / 2;
        pts = new Geo[length];
        for (int i = 0, p = 0; i < al; i = i + 2) {
            pts[p] = new Geo(lls[i], lls[i + 1], isDegrees);
            p++;
        }
    }
    
    public int length() {
        return length;
    }

    public Path.SegmentIterator segmentIterator() {
        return new SegIt();
    }

    public Path.PointIterator pointIterator() {
        return new PointIt();
    }

    protected class SegIt implements Path.SegmentIterator, GSegment {
        int i = -1;
        Geo[] seg = new Geo[2];

        public SegIt() {
            seg[1] = pts[0];
        }

        public boolean hasNext() {
            return i < (length - 2);
        }

        public Object next() {
            return nextSegment();
        }

        public GSegment nextSegment() {
            i++;
            seg[0] = seg[1];
            seg[1] = pts[i + 1];
            return this;
        }

        public void remove() {
            throw new UnsupportedOperationException("Path.Iterator doesn't support remove");
        }

        /**
         * GSegment method.
         * 
         * @return the current segment as a two-element array of Geo
         *         The first point is the "current point" and the
         *         second is the next. TODO If there isn't another
         *         point available, will throw an indexOutOfBounds
         *         exception.
         */
        public Geo[] getSeg() {
            return seg;
        }

        /**
         * @return the current segment as a float[]. The first point
         *         is the "current point" and the second is the next.
         *         TODO If there isn't another point available, will
         *         throw an indexOutOfBounds exception.
         */
        public float[] getSegArray() {
            return new float[] { (float) seg[0].getLatitude(),
                    (float) seg[0].getLongitude(),
                    (float) seg[1].getLatitude(), (float) seg[1].getLongitude() };
        }

        /**
         * Return Object ID for current segment.
         */
        public Object getSegId() {
            return new Integer(i);
        }
    }

    protected class PointIt implements Path.PointIterator, GPoint {
        int i = -1;
        Geo[] seg = new Geo[2];

        public PointIt() {
            seg[1] = pts[0];
        }

        public boolean hasNext() {
            return i < length;
        }

        public Object next() {
            return nextPoint();
        }

        public GPoint nextPoint() {
            i++;
            return this;
        }

        public void remove() {
            throw new UnsupportedOperationException("Path.Iterator doesn't support remove");
        }

        public Geo getPoint() {
            return seg[i];
        }

        public Object getPointId() {
            return new Integer(i);
        }
    }
}
