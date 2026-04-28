/**
 * 
 */
package com.bbn.openmap.geo;

public interface MatchFilter {

    /**
     * do inexpensive comparisons to determine if the two elements might
     * intersect.
     * 
     * @return true iff further checks might yield intersection.
     */
    boolean preConsider(GeoExtent seg, GeoExtent region);

    /**
     * the distance (in radians) to consider two objects to be interacting, that
     * is, intersecting for our purposes.
     * 
     * @return the maximum distance to consider touching. Must be non-negative.
     */
    double getHRange();

    //
    // implementations
    //

    public static class MatchParametersMF
            implements MatchFilter {
        protected double hrange = 0.0;

        public MatchParametersMF(MatchParameters params) {
            // initialize search parameters from method calls
            hrange = params.horizontalRange();
        }

        public MatchParametersMF(double hrange, int[] vrange, long[] trange) {
            this.hrange = hrange;
        }

        public double getHRange() {
            return hrange;
        }

        public boolean preConsider(GeoExtent seg, GeoExtent region) {
            return true;
        }
    }

    public static class ExactMF
            implements MatchFilter {
        public double getHRange() {
            return 0.0;
        }

        public boolean preConsider(GeoExtent seg, GeoExtent region) {
            return true;
        }
    }
}