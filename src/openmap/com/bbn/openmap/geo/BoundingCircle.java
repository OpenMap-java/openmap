// **********************************************************************
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
//$RCSfile: BoundingCircle.java,v $
//$Revision: 1.2 $
//$Date: 2005/07/05 23:08:29 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;


/**
 * Interface that describes a bounding circle around a given point on
 * a sphere.
 */
public interface BoundingCircle {

    /**
     * Returns a Geo representing the center of the circle.
     * 
     * @return Geo
     */
    public Geo getCenter();

    /**
     * @return the radius of the bounding circle in radians.
     */
    public double getRadius();

    /**
     * A simple implementation of BoundingCircle, storing the center
     * and radius as data members.
     */
    public static class Impl implements BoundingCircle {
        private Geo center; // center of circle
        private double radius; // angle (in radians) to edge

        public Impl(Geo center, double radius) {
            init(center, radius);
        }

        public Impl(Region region) {
            init(region.getBoundary());
        }

        public Impl(LatLonPath llPath) {

            Geo[] region = new Geo[llPath.length()];

            int i = 0;
            for (Path.PointIterator pi = llPath.pointIterator(); pi.hasNext(); i++) {
                region[i] = (Geo) pi.next();
            }
            
            init(region);
        }
        
        /**
         * Works by computing the centroid, then finding the
         * largest radius. this will not, in general, produce the
         * minimal bounding circle.
         */
        protected void init(Geo[] region) {
            Geo c = Intersection.center(region); // centroid

            double r = 0.0;
            int length = region.length;
            for (int i = 0; i < length; i++) {
                double pr = c.distance(region[i]);
                if (pr > r) {
                    r = pr;
                }
            }

            init(c, r);
        }

        protected void init(Geo center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        public final Geo getCenter() {
            return center;
        }

        public final double getRadius() {
            return radius;
        }
    }
}