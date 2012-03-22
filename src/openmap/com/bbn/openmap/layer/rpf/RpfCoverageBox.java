// **********************************************************************
//
// <copyright>
//
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
//
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfCoverageBox.java,v $
// $RCSfile: RpfCoverageBox.java,v $
// $Revision: 1.8 $
// $Date: 2006/12/13 16:45:24 $
// $Author: dietrick $
//
// **********************************************************************

/**
 *  Modifications :
 *
 *  1. Changed getId() to make more unique
 *  2. Changed horizontalSubframes() and verticalSubframes(); need to round up
 *     in certain cases.
 */

package com.bbn.openmap.layer.rpf;

import java.awt.Point;

import com.bbn.openmap.util.Debug;

/**
 * The RpfCoverageBox describes the coverage provided by a RpfTocEntry
 * within a table of contents file. This should be enough information
 * that lets you tell what you need to ask for the proper subframes to
 * put on a screen.
 */
public class RpfCoverageBox {
    public double nw_lat;
    public double nw_lon;
    public double se_lat;
    public double se_lon;
    /** Lat degrees/subframe; vertInterval*256. */
    public double subframeLatInterval;
    /** Lon degrees/subframe; horizInterval*256. */
    public double subframeLonInterval;
    /** Two letter code for chart type. */
    public String chartCode;
    /**
     * The starting index for coverage subframes. If it's null, it
     * hasn't been figured out yet.
     */
    public Point startIndexes;
    /**
     * The ending index for coverage subframes. If it's null, it
     * hasn't been figured out yet.
     */
    public Point endIndexes;
    /**
     * For the coverage queries, the CADRG zone becomes important.
     * This is not the zone from the RpfTocEntry - its the translated
     * zone for use with the CADRG projection.
     */
    public int zone;
    /**
     * The TOC number that a frame provider can use to get to the
     * right entry number. Used internally.
     */
    public int tocNumber;
    /**
     * The RpfTocEntry index to use to get more information about the
     * frame files to use to get data for a subframe. Used internally.
     */
    public int entryNumber;
    /** The scale of the maps of this coverage rectangle. */
    public float scale;
    /**
     * Of the number of subframes that can fit on the screen, the
     * percentage of them that are on the screen. Should be set via
     * setPercentCoverage, unless you are copying all values as well.
     */
    public float percentCoverage;
    /** A semi unique string descriptor. */
    protected String id;

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(" nw_lat ").append(nw_lat).append(", nw_lon ").append(nw_lon).append("\n");
        s.append(" se_lat ").append(se_lat).append(", se_lon ").append(se_lon).append("\n");
        s.append(" chart code ").append(chartCode).append("\n");
        s.append(" scale (parsed and decoded) ").append(scale).append("\n");
        s.append(" vertical subframes ").append(verticalSubframes()).append("\n");
        s.append(" horizontal subframes ").append(horizontalSubframes()).append("\n");
        s.append(" percent coverage ").append(percentCoverage);
        return s.toString();
    }

    /**
     * Modified to make the semi-unique ID more unique. The former
     * method of generating these ID numbers was causing some coverage
     * boxes that are geographically close to each other to have the
     * same ID, which resulted in caching problems. In particular, if
     * two non-identical boxes had the same ID, they could invalidate
     * each others' caches in certain regions. Modified these IDs so
     * that the numbers would be more unique. This was a major cause
     * of gaps at zone boundaries.
     * 
     * @return a unique ID for the coverage box
     */
    public String getID() {
        if (id == null) {
            // This should be enough to tell that the box is not the
            // same. Even if it isn't the same, the subframes from
            // this source should be the same.

            // NOTE: added +se_lat+se_lon because of
            // uniqueness problems
            id = tocNumber + entryNumber + nw_lat + nw_lon + se_lat + se_lon
                    + chartCode;

        }
        return id;
    }

    /**
     * The number of subframes vertically within this coverage box.
     * 
     * NOTE: Empirically noticed that # of subframes sometimes
     * underreported because these cast to an int. Rounding would
     * sometimes overreport, however. Observed that a tolerance of
     * .000001 would define when to round up. This appeared to be
     * causing gaps at zone boundaries, since a coverage box could
     * think it doesn't have a certain subframe, when it actually
     * does.
     */
    public int verticalSubframes() {
        double value = Math.abs((nw_lat - se_lat) / subframeLatInterval);
        int lower = (int) value;
        int upper = lower + 1;
        if (upper - value < .000001) {
            return upper;
        } else
            return lower;
    }

    /**
     * The number of subframes horizontally within this coverage box.
     * 
     * NOTE: Empirically noticed that # of subframes sometimes
     * underreported because these cast to an int. Rounding would
     * sometimes overreport, however. Observed that a tolerance of
     * .000001 would define when to round up. This appeared to be
     * causing gaps at zone boundaries, since a coverage box could
     * think it doesn't have a certain subframe, when it actually
     * does.
     */
    public int horizontalSubframes() {
        double value = Math.abs((se_lon - nw_lon) / subframeLonInterval);
        int lower = (int) value;
        int upper = lower + 1;
        if (upper - value < .000001) {
            return upper;
        } else
            return lower;
    }

    /**
     * This is only good for a preliminary check to see of the
     * boundaries are within the range of each other.
     * 
     * @return how many of the edges of this coverage box fall within
     *         the queried box.
     */
    public int setBoundaryHits(float ullat, float ullon, float lrlat,
                               float lrlon) {
        int boundaryHits = 0;
        if (lrlat < nw_lat)
            boundaryHits++;
        if (ullat > se_lat)
            boundaryHits++;
        if (lrlon > nw_lon)
            boundaryHits++;
        if (ullon < se_lon)
            boundaryHits++;

        if (ullat < nw_lat)
            boundaryHits++;
        if (lrlat > se_lat)
            boundaryHits++;
        if (ullon > nw_lon)
            boundaryHits++;
        if (lrlon < se_lon)
            boundaryHits++;
        return boundaryHits;
    }

    /**
     * The percentage of subframes that actually fill the queried
     * rectangle, compared to the number of subframes that could fit.
     * As a bonus, the start and end suframe indexes are set.
     * 
     * @return the percentage of coverage over the queried rectangle.
     */
    public float setPercentCoverage(float ullat, float ullon, float lrlat,
                                    float lrlon) {
        startIndexes = new Point();
        endIndexes = new Point();
        return setPercentCoverage(ullat,
                ullon,
                lrlat,
                lrlon,
                startIndexes,
                endIndexes);
    }

    /**
     * The percentage of subframes that actually fill the queried
     * rectangle, compared to the number of subframes that could fit.
     * As a bonus, the start and end suframe indexes are set.
     * 
     * @return the percentage of coverage over the queried rectangle.
     */
    public float setPercentCoverage(float ullat, float ullon, float lrlat,
                                    float lrlon, Point start, Point end) {

        startIndexes = start;
        endIndexes = end;

        // Set the subframes that are on the screen, in the matrix of
        // subframes that make up the overall boundary rectangle.
        // Upper left is 0, 0
        double tempInterval = (ullon - nw_lon) / subframeLonInterval;
        start.x = (int) tempInterval;
        if (tempInterval < 0 && tempInterval < (double) start.x)
            start.x--;
        tempInterval = (nw_lat - ullat) / subframeLatInterval;
        start.y = (int) tempInterval;
        if (tempInterval < 0 && tempInterval < (double) start.y)
            start.y--;
        tempInterval = (lrlon - nw_lon) / subframeLonInterval;
        end.x = (int) tempInterval;
        if (tempInterval < 0 && tempInterval < (double) end.x)
            end.x--;
        tempInterval = (nw_lat - lrlat) / subframeLatInterval;
        end.y = (int) tempInterval;
        if (tempInterval < 0 && tempInterval < (double) end.y)
            end.y--;
        //(int) (Math.abs(lrlon - ullon)/subframeLonInterval);
        int num_horiz_subframes = horizontalSubframes();
        //(int) (Math.abs(ullat - lrlat)/subframeLatInterval);
        int num_vert_subframes = verticalSubframes();

        if ((start.y >= 0 || end.y >= 0)
                && (start.x >= 0 || end.x >= 0)
                && (start.x < num_horiz_subframes || end.x < num_horiz_subframes)
                && (start.y < num_vert_subframes || end.y < num_vert_subframes)) {

            // So now here, either the lesser side is less than zero,
            // the greater side is greater than the Max.
            int left = start.x < 0 ? 0 : start.x;
            int right = end.x >= num_horiz_subframes ? (num_horiz_subframes - 1)
                    : end.x;
            int top = start.y < 0 ? 0 : start.y;
            int bottom = end.y >= num_vert_subframes ? (num_vert_subframes - 1)
                    : end.y;

            percentCoverage = ((float) ((Math.abs(right - left) + 1f) * (Math.abs(bottom
                    - top) + 1f)) / (float) ((Math.abs(end.x - start.x) + 1f) * (Math.abs(end.y
                    - start.y) + 1))) * 100f;

            if (percentCoverage > 100f)
                percentCoverage = 100f;

            if (Debug.debugging("rpf")) {
                System.out.println("Calculated percentage = "
                        + percentCoverage
                        + " <= "
                        + ((Math.abs(right - left) + 1) * (Math.abs(bottom
                                - top) + 1))
                        + " subframes / "
                        + ((Math.abs(end.x - start.x) + 1) * (Math.abs(end.y
                                - start.y) + 1)) + " subframes\n (" + right
                        + " - " + left + ") * (" + bottom + " - " + top
                        + ") / (" + end.x + " - " + start.x + ") * (" + end.y
                        + " - " + start.y + ")");
            }

        } else {
            percentCoverage = 0f;
        }

        return percentCoverage;
    }

    /** Return the percent coverage of the last queried rectangle. */
    public float getPercentCoverage() {
        return percentCoverage;
    }

    /**
     * Location within box. True if it is;
     */
    public boolean within(float lat, float lon) {
        double y = (double) lat;
        double x = (double) lon;
        return (y < nw_lat && y > se_lat && x < se_lon && x > nw_lon);
    }

    /** Reset the coverage percentage and scale difference. */
    public void reset() {
        percentCoverage = 0f;
        scale = 0;
    }
}

