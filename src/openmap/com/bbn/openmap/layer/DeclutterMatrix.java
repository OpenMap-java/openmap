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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/DeclutterMatrix.java,v $
// $RCSfile: DeclutterMatrix.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:08 $
// $Author: dietrick $
// 
// **********************************************************************

/*
 * **********************************************************************
 * 
 *  A port to Java/Openmap of the MATT decluttering code.
 *
 *  Created on: Thu Aug 19 14:55:41 1999
 * **********************************************************************
 *
 *  Modification history:
 *  $Log: DeclutterMatrix.java,v $
 *  Revision 1.6  2005/12/09 21:09:08  dietrick
 *  Projection and LatLonPoint Paradigm shift!  Handling preprojected data.  Proj based on Point2D objects, new com.bbn.openmap.proj.coords.LatLonPoint to support that for Projection subclasses.  New Cartesian projection.  All other components seem to be updated and working with the changes.
 *  There will be incompatibilities with OpenMap 4.6 and previous versions of OpenMap, this is a new minor revision.
 *
 *  Revision 1.5  2004/10/14 18:05:52  dietrick
 *  Copyright updates, removed extemporaneous import statements, cleaned up deprecations
 *
 *  Revision 1.4  2004/02/06 19:06:20  dietrick
 *  Fixed harmless lines in DeclutterMatrix, modified DemoLayer to create OMEllipse objects instead of hacking them up, added FullProjectionRenderPolicy which forces clipping area for layers to match the projection, and made RpfLayer call the RenderPolicy
 *
 *  Revision 1.3  2004/01/26 18:18:08  dietrick
 *  Untabified
 *
 *  Revision 1.2  2003/12/23 20:43:25  wjeuerle
 *  no code changes, updated javadoc comments to fix javadoc warnings
 *
 *  Revision 1.1  2003/02/14 21:35:48  dietrick
 *  Initial revision
 *
 *  Revision 1.14  2002/04/02 20:59:41  bmackiew
 *  Updated with revised copyright information.
 *
 *  Revision 1.13  2000/06/21 22:40:29  dietrick
 *  Fixed the runoff of some partial words on the right side when partials weren't allowed.
 *
 *  Added a main to RpfFrame to print out information about the frame file.
 *
 *  Added a string converter to Location.java to translate N340000 to 34.000, etc.
 *
 *  Revision 1.12  2000/05/08 14:22:19  wjeuerle
 *  BBNT Solutions LLC in copyright headers
 *  include Y2K in copyright years
 *
 *  Revision 1.11  2000/01/21 03:44:08  dietrick
 *  Added the capability to set whether you want to allow objects to be
 *  straddling the edge of the map.
 *
 *  Revision 1.10  1999/12/03 18:35:52  dietrick
 *  Changing System.out and System.error to Debug.output and Debug.error.
 *
 *  Revision 1.9  1999/12/02 21:07:35  dietrick
 *  Comments and tweaks.
 *
 *  Revision 1.8  1999/11/30 22:43:17  dietrick
 *  Updates and revamping.
 *
 *  Revision 1.7  1999/10/07 22:15:45  dietrick
 *  Took out the reset function I added, because it's unnecessary.
 *
 *  Revision 1.6  1999/10/07 16:08:51  dietrick
 *  Added a static getGraphics() call, which returns a fake graphics to
 *  use to figure out the dimensions of font metrics at projection time,
 *  as opposed to render time.
 *
 *  Revision 1.5  1999/10/05 17:33:50  gkeith
 *  Cleaned up a bit, but not fully documented yet.
 *  Stay tuned...
 *
 *  Revision 1.4  1999/09/10 17:54:17  pmanghwa
 *  Removed unnecessary checks
 *
 *  Revision 1.3  1999/09/08 20:08:40  pmanghwa
 *  added methods to handle height.
 *  Slow but good looking
 *
 *  Revision 1.2  1999/09/07 21:26:43  pmanghwa
 *  Now its Readable
 *
 *  Revision 1.1  1999/08/24 19:02:35  gkeith
 *  JAVA-port of the old MATT decluttering code.
 *
 *
 **********************************************************************/

package com.bbn.openmap.layer;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import com.bbn.openmap.util.Debug;

/**
 * This class represents the screen divided up into sections, and tracks the
 * sections that are marked, for any reason. The pix_intervals are the height
 * and width of the sections. isClear() returns 1 if the space is clear, and
 * setTaken returns true if the space was clear and the space is now marked
 * taken.
 */
public class DeclutterMatrix {

    /**
     * The height of the screen to be covered by the matrix, in pixels. The
     * number of vertical matrix cell is the height/y_pix_interval.
     */
    protected int height = 0;
    /**
     * The width of the screen to be covered by the matrix, in pixels. The
     * number of horizontal matrix cell is the width/x_pix_interval.
     */
    protected int width = 0;
    /** The number of horizontal pixels per matrix cell */
    protected int x_pix_interval = 1;
    /** The number of vertical pixels per matrix cell */
    protected int y_pix_interval = 1;
    /** The matrix itself, width x height. */
    protected boolean[][] matrix = new boolean[0][0];
    /**
     * The maximum index for the horizontal locations within the matrix.
     */
    protected int maxx;
    /**
     * The maximum index for the vertical locations within the matrix.
     */
    protected int maxy;
    /**
     * Whether or not objects are allowed to appear partially off the matrix. If
     * true, cells off the matrix will be automatically counted as clear. The
     * default is true.
     */
    protected boolean allowPartials = true;
    /**
     * A set of matrix indexes that get set for a particular object for a
     * search. This is to limit the number of off matrix indexes used.
     */
    protected MatrixIndexes indexes = new MatrixIndexes();
    /**
     * A flag to force a recreation of the matrix if the dimensions change.
     */
    protected boolean needToRecreate = false;

    /*
     * These are all magic numbers that don't need to be anything in particular,
     * apart from sequential. They are used to calculate where to next search
     * for an opening in the matrix. The order reflects the pattern in which
     * openings are searched for from the original position. In general, the
     * original position is checked, and then alternatives are sought, checking
     * the areas around the original in a search pattern. The search pattern is
     * continued, but the distance from the original location is increased until
     * a clear position is/is not found. As stated in the setNextOpen comments,
     * the search pattern is a square, but the order of these notations let you
     * control which sides/corners of the square are looked at first, in terms
     * of finding an open space. The square expands outward until a place is
     * found, or until no place is found, given some desired limit.
     */
    public final static int DCP_MIDDLE = 20875;
    public final static int DCP_EAST = 20876;
    public final static int DCP_NORTH = 20877;
    public final static int DCP_SOUTH = 20878;
    public final static int DCP_WEST = 20879;
    public final static int DCP_NEAST = 20880;
    public final static int DCP_SEAST = 20881;
    public final static int DCP_SWEST = 20882;
    public final static int DCP_NWEST = 20883;

    /*
     * The Declutter direction variables are general notations for the declutter
     * positions. Within the search square, some positions, like along the side,
     * present an opportunity to check in different places - for instance, if we
     * are on the top part of the square, the search must travers East to West,
     * hence, the direction is noted as DCD_EW. Corners have no search
     * direction, and the left and right sides have North-South search traversal
     * directions.
     */
    public final static int DCD_NS = 20884;
    public final static int DCD_EW = 20885;
    public final static int DCD_NONE = 20886;

    /**
     * This Denotes the parameters of one of the 8 possible positions around a
     * decluttermatrix tile.
     */
    public static class PositionParameters {
        public int position; // should be one of the DCP variables
        public int ewindex;
        public int nsindex;
        public int direction; // should be one of the DCD_ variables

        public PositionParameters(int pos, int ewindx, int nsindx, int direc) {
            position = pos;
            ewindex = ewindx;
            nsindex = nsindx;
            direction = direc;
        }
    }

    /**
     * This is an ordering of the possible positions around a matrix tile.
     */
    public final static PositionParameters dcPos[] = new PositionParameters[9];

    static { // Now initialize it.
        dcPos[0] = new PositionParameters(DCP_EAST, 1, 0, DCD_NS);
        dcPos[1] = new PositionParameters(DCP_NORTH, 0, -1, DCD_EW);
        dcPos[2] = new PositionParameters(DCP_SOUTH, 0, 1, DCD_EW);
        dcPos[3] = new PositionParameters(DCP_WEST, -1, 0, DCD_NS);
        dcPos[4] = new PositionParameters(DCP_NEAST, 1, -1, DCD_NONE);
        dcPos[5] = new PositionParameters(DCP_SEAST, 1, 1, DCD_NONE);
        dcPos[6] = new PositionParameters(DCP_SWEST, -1, 1, DCD_NONE);
        dcPos[7] = new PositionParameters(DCP_NWEST, -1, -1, DCD_NONE);
        dcPos[8] = new PositionParameters(DCP_MIDDLE, 0, 0, DCD_NONE);
    }

    public class MatrixIndexes {

        public boolean withinMatrix = false;
        public boolean partial = false;
        public int xStart = 0;
        public int yStart = 0;
        public int xEnd = 0;
        public int yEnd = 0;

        public int origXIndex = -1;
        public int origYIndex = -1;
        public int origIndexLength = 1;
        public int origIndexHeight = 1;

        public MatrixIndexes() {}

        public boolean setFromPixels(int pixelXLocation, int pixelYLocation,
                                     int pixelLength, int pixelHeight) {
            int objXIndex = pixelXLocation / x_pix_interval;
            int objYIndex = pixelYLocation / y_pix_interval;
            int objLength = (int) Math.ceil((double) pixelLength
                    / (double) x_pix_interval);
            int objHeight = (int) Math.ceil((double) pixelHeight
                    / (double) y_pix_interval);
            return set(objXIndex, objYIndex, objLength, objHeight);
        }

        public boolean setFromPixels(int pixelXLocation, int pixelYLocation) {
            int objXIndex = pixelXLocation / x_pix_interval;
            int objYIndex = pixelYLocation / y_pix_interval;
            return set(objXIndex, objYIndex, origIndexLength, origIndexHeight);
        }

        public boolean set(int objXIndex, int objYIndex) {
            return set(objXIndex, objYIndex, origIndexLength, origIndexHeight);
        }

        public boolean set(int objXIndex, int objYIndex, int objIndexLength,
                           int objIndexHeight) {

            // Save as reference
            origXIndex = objXIndex;
            origYIndex = objYIndex;

            // This may be redundant, but so what. It's too hard to
            // tell...
            origIndexLength = objIndexLength;
            origIndexHeight = objIndexHeight;

            withinMatrix = objOnMatrix(objXIndex,
                    objYIndex,
                    objIndexLength,
                    objIndexHeight);
            if (!withinMatrix) {
                return false;
            }

            // End variables refer to an end index
            partial = false;
            // Set the end of the check - if the end sticks out past
            // the
            // matrix, just check the part that's on the matrix. If
            // it's
            // left of the matrix, don't bother going into the loop by
            // setting the end point to 0.
            if ((objXIndex + objIndexLength) <= maxx) {
                xEnd = objXIndex + objIndexLength;
            } else {
                xEnd = maxx;
                partial = true;
            }
            if (xEnd < 0)
                xEnd = 0;

            // Now do the vertical version of the same thing...
            if ((objYIndex + objIndexHeight) <= maxy) {
                yEnd = objYIndex + objIndexHeight;
            } else {
                yEnd = maxy;
                partial = true;
            }
            if (yEnd < 0)
                yEnd = 0;

            // And, figure out what the good starting index point is
            // to
            // check for vertical conflicts - This is all to make the
            // checkMatrixLocation methods run as efficiently as
            // possible.
            if (objYIndex >= 0) {
                yStart = objYIndex;
            } else {
                objYIndex = 0;
                partial = true;
            }
            // And horizontal starting index
            if (objXIndex >= 0) {
                xStart = objXIndex;
            } else {
                xStart = 0;
                partial = true;
            }

            return true;
        }

        /**
         * Test to see if the object is on the matrix. Assumes that the matrix
         * is not null.
         * 
         * @return true if object is on matrix.
         */
        public boolean objOnMatrix(int objXIndex, int objYIndex,
                                   int objIndexLength, int objIndexHeight) {
            if (objXIndex + objIndexLength < 0 || // left off-matrix
                    objYIndex + objIndexHeight < 0 || // below matrix
                    // right off-matrix and above matrix
                    objXIndex > maxx || objYIndex > maxy) {
                return false;
            }
            return true;
        }
    }

    /**
     * Set whether names can appear partially on/off the map. True means they
     * can, i.e., the spaces off the map are by default, clear.
     */
    public void setAllowPartials(boolean value) {
        allowPartials = value;
    }

    /**
     * Find out whether the spaces off the map are counted as clear and
     * available. If they are, then objects can appear partially on the map.
     */
    public boolean isAllowPartials() {
        return allowPartials;
    }

    /** ******************************************************************* */

    /**
     * Construct a new DeclutterMatrix, given the screen dimensions and the size
     * of the matrix cells
     */
    public DeclutterMatrix(int width, int height, int x_pix_interval,
            int y_pix_interval) {
        this.width = width;
        this.height = height;
        if (x_pix_interval != 0) {
            this.x_pix_interval = x_pix_interval;
        } else {
            x_pix_interval = 1;
        }

        if (y_pix_interval != 0) {
            this.y_pix_interval = y_pix_interval;
        } else {
            y_pix_interval = 1;
        }

        this.matrix = null;

        this.maxx = (this.width / this.x_pix_interval) - 1;
        this.maxy = (this.height / this.y_pix_interval) - 1;
        create();
        Debug.message("declutter", "Decluttering matrix created."
                + "  Width = " + width + " Height = " + height);
    }

    /**
     * Construct a new matrix, given the screen dimensions, and using the
     * default matrix cell size
     */
    public DeclutterMatrix(int width, int height) {
        this(width, height, 1, 1);
    }

    /** Create a new matrix, with null dimensions */
    public DeclutterMatrix() {
        this(0, 0);
    }

    /*
     * Any of these delete the current matrix if it exists and resets the
     * variable. create() needs to be called to recreate a new matrix after all
     * the changes.
     */

    public void setXInterval(int x_pix_interval) {
        if (x_pix_interval != 0) {
            this.x_pix_interval = x_pix_interval;
        } else {
            this.x_pix_interval = 1; // To avoid DBZ error?
        }
        this.maxx = (this.width / this.x_pix_interval) - 1;
        needToRecreate = true;
        Debug.message("declutter",
                "Decluttering matrix: x_pix_interval changed to "
                        + x_pix_interval);
    }

    public void setYInterval(int y_pix_interval) {
        if (y_pix_interval != 0) {
            this.y_pix_interval = y_pix_interval;
        } else {
            this.y_pix_interval = 1;
        }
        this.maxy = (this.height / this.y_pix_interval) - 1;
        needToRecreate = true;
        Debug.message("declutter",
                "Decluttering matrix: y_pix_interval changed to "
                        + y_pix_interval);
    }

    public void setWidth(int width) {
        this.width = width;
        this.maxx = (this.width / this.x_pix_interval) - 1;
        needToRecreate = true;

        Debug.message("declutter", "Decluttering matrix: Width reset to "
                + width);
    }

    public void setHeight(int height) {
        this.height = height;
        this.maxy = (this.height / this.y_pix_interval) - 1;
        needToRecreate = true;

        Debug.message("declutter", "Decluttering matrix: height reset to "
                + height);
    }

    /**
     * Allocate the matrix.
     * 
     * @return true if successful, and if the height and width settings were
     *         valid (>0).
     */
    public boolean create() {
        if ((height > 0) && (width > 0)) {
            matrix = new boolean[maxx + 1][maxy + 1];
            needToRecreate = false;
            return true;
        }
        needToRecreate = true;
        return false;
    }

    /**
     * Query whether the matrix is clear, given a set of indexes.
     * 
     * @param indexes the set of indexes
     * @param markAsTaken mark the spaces as used if they are previously clear.
     * @return true if they were clear previously.
     */
    protected boolean isClear(MatrixIndexes indexes, boolean markAsTaken) {

        Debug.message("declutterdetail",
                "DeclutterMatrix: Checking space for clear.");

        if (this.matrix == null) {
            return false;
        }

        if (!indexes.withinMatrix) {
            // But, withinMatrix doesn't tell you if there is a
            // partial. It only tells you if any part of the object
            // is over the matrix. So, if it's not within the Matrix,
            // the answer should be yes, all the time, because you
            // don't have to declutter what you can't see...

            // return allowPartials;
            return true;
        }

        if (!allowPartials && indexes.partial) {
            return false;
        }

        // OK - the above check should verify that some part of the
        // object is on the matrix - so there is a reason to set the
        // limits for the matrix search below, and not worry about
        // dealing with funky index values.

        boolean notClear = false;

        // Since we have the matrix index limits, have two loops, the
        // first to check for the open cells, the other to mark the
        // cells as occupied. The second loop only gets run if the
        // markAsTaken flag is set by the caller.
        for (int taken = 0; taken < 2; taken++) {

            // Check to see if the horizontal indexes are on the
            // matrix - i should be at least greater than zero here,
            // as should j.
            for (int i = indexes.xStart; i <= indexes.xEnd; i++) {

                // Check for loop - the first loop is to see if
                // the spaces are open.
                if (taken == 0) {
                    notClear = isMatrixLocationTaken(i,
                            indexes.yStart,
                            indexes.yEnd - indexes.yStart + 1);
                    if (notClear) {
                        return false;
                    }
                } else {
                    // The second loop is to mark the cells as
                    // taken
                    setTaken(i, indexes.yStart, indexes.yEnd - indexes.yStart
                            + 1);
                }
            }

            // This will prevent the second loop from occurring if it's
            // not supposed to - the caller just wanted to check the
            // spaces, rather than check and mark.
            if (!markAsTaken) {
                return true;
            }
        }
        return true;
    }

    /**
     * Check a vertical portion of the matrix, to see if it has already been
     * taken. If a query occurs that is outside the matrix, this returns false.
     * 
     * @param horizontalIndex the horizontal index of the matrix to check.
     * @param verticalIndex the vertical starting index of the matrix to check.
     * @param numCellsToCheck the number of matrix cells to check for taken.
     * @return true if taken, false if available.
     */
    protected boolean isMatrixLocationTaken(int horizontalIndex,
                                            int verticalIndex,
                                            int numCellsToCheck) {
        try {
            for (int i = numCellsToCheck - 1; i >= 0; i--) {
                if (matrix[horizontalIndex][verticalIndex + i]) {
                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return allowPartials;
        }
        return false;
    }

    /**
     * Mark a vertical portion of the matrix as taken.
     * 
     * @param horizontalIndex the horizontal index of the matrix to mark.
     * @param verticalIndex the vertical starting index of the matrix to mark.
     * @param numCellsToMark the number of matrix cells to mark as taken.
     */
    protected void setTaken(int horizontalIndex, int verticalIndex,
                            int numCellsToMark) {
        try {
            for (int i = numCellsToMark - 1; i >= 0; i--) {
                matrix[horizontalIndex][verticalIndex + i] = true;
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }

    /**
     * SetTaken returns true if the space was clear before the it was taken,
     * false if it was not. Either way, the spaces are marked. Except if the
     * matrix is not built, in which case false is returned anyway.
     * 
     * @param indexes the start and end matrix indexes for an object.
     * @return true if successful.
     */
    protected boolean setTaken(MatrixIndexes indexes) {
        if (this.matrix == null) {
            return false;
        }

        if (!indexes.withinMatrix) {
            return allowPartials;
        }

        for (int i = indexes.xStart; i < indexes.xEnd; i++) {
            setTaken(i, indexes.yStart, indexes.yEnd - indexes.yStart + 1);
        }
        return true;
    }

    /**
     * Set an area as taken, given a point and a length of pixels. The length is
     * checked from left to right.
     */
    public boolean setTaken(Point point, int pixelLength) {
        return setTaken(point, pixelLength, y_pix_interval);
    }

    /**
     * Set an area as taken, given a point, a length of pixels and a height of
     * pixels. The length is from left to right, the height from the bottom to
     * top (NOT like screen coordinates)
     */
    public boolean setTaken(Point point, int pixelLength, int pixelHeight) {
        if (needToRecreate)
            create();

        indexes.setFromPixels(point.x, point.y, pixelLength, pixelHeight);
        return setTaken(indexes);
    }

    /**
     * The method to call if you are trying to set something in an open place,
     * anywhere on the map.
     * 
     * @param point the window point
     * @param pixelLength the pixel length of space from left to right.
     * @param pixelHeight the pixel height from bottom to top.
     * @return Point of closest open space.
     */
    public Point2D setNextOpen(Point2D point, int pixelLength, int pixelHeight) {
        return setNextOpen(point, pixelLength, pixelHeight, -1);
    }

    /**
     * The method to call if you are trying to set something in an open place,
     * but want to limit how far away the object could be placed.
     * 
     * @param point the window point
     * @param pixelLength the pixel length of space from left to right.
     * @param pixelHeight the pixel height from bottom to top.
     * @param pixelAwayLimit the pixel distance away from the original location
     *        that where an object will be discarded if it's not at least that
     *        close. -1 means find anywhere on the map where the object will
     *        fit.
     * @return Point of closest open space.
     */
    public Point2D setNextOpen(Point2D point, int pixelLength, int pixelHeight,
                               int pixelAwayLimit) {

        Debug.message("declutterdetail",
                "DeclutterMatrix: Trying to find an open space.");

        if (needToRecreate)
            create();

        int pointx = (int) point.getX();
        int pointy = (int) point.getY();
        boolean set = false;

        // mark the original spot. These are indexes, not pixels.
        int windex = pointx / x_pix_interval;
        int hindex = pointy / y_pix_interval;

        // intermediate values used for ew/ns spanning. This are not
        // pixels, they are indexes into the matrix
        int xpoint;
        int ypoint;

        // The point to be returned with the new, declutter postition
        // for the object.
        Point ret = null;
        // Test point, for memory allocation savings.
        Point testPoint = new Point();

        // A round is a cycle through the positions in the search
        // algorithm. With every round the distance from the original
        // position increases. The search pattern looks like an
        // expanding square, and it's broken into different pieces:
        // Each side, not including the corners, and each corner.

        int round = 0; // Round 1, round 2 - get it!?!?
        int pos, i;

        // Set up the indexes for the original spot.
        indexes.setFromPixels(pointx, pointy, pixelLength, pixelHeight);

        // Make sure the graphic is on the visible screen
        if (matrix == null || !indexes.withinMatrix) {
            return point;
        }

        // Do check for the center here, before looping. Who knows,
        // the space might be open.
        ret = isAreaClearR(windex, hindex, testPoint);
        int round_limit;
        if (pixelAwayLimit < 0) {
            round_limit = Math.abs(maxy / 2 - indexes.yStart) + (maxy / 2);
        } else {
            round_limit = pixelAwayLimit;
        }

        // Now that we know it is not open, move on and start
        // searching for
        // the right place.
        while (ret == null) {

            Debug.message("declutterdetail", "DeclutterMatrix: round " + round
                    + "\n");
            round++;
            for (pos = 0; (dcPos[pos].position != DCP_MIDDLE) && (ret == null); pos++) {

                // Need to do this based on the length, so as to skip
                // unnecessary checks. The xpoint and ypoint are the
                // starting point for each little incremental search.
                // This point, in every round, spirals outward from
                // the original desired location.
                xpoint = windex + (round * dcPos[pos].ewindex);
                ypoint = hindex + (round * dcPos[pos].nsindex);

                // checks to keep starting point away from being
                // offscreen
                if (xpoint <= maxx && ypoint <= maxy && xpoint >= 0
                        && ypoint >= 0) {

                    // Start at handling the parts of the search
                    // square that make up the sides of the square -
                    // DCD_NS refers to the fact that the search
                    // pattern is traversing up the side of the
                    // square, for either side.

                    // for the east and west checks, look
                    // a north and south variations
                    if (dcPos[pos].direction == DCD_NS) {
                        for (i = -1 * (round - 1); (i < round - 1) && !set; i++) {

                            // isAreaClearR used to be used here in
                            // order to prevent a name from being
                            // written over an icon. You know, if the
                            // icon shouldn't be written over, let it
                            // be added to the matrix before the name.
                            // Then it won't be covered.

                            // // Don't look both ways if directly to
                            // the
                            // // east, you'll write over the icon
                            // if(dcPos[pos].position == DCP_EAST &&
                            // i <= pixelHeight) {
                            // ret = isAreaClearR(xpoint,
                            // ypoint+i,
                            // testPoint);
                            // } else {
                            ret = isAreaClearBW(xpoint, ypoint + i, testPoint);
                            // }

                            // If we've found a clear spot, jump out.
                            if (ret != null)
                                break;
                        }
                    } else {

                        // Now we're checking the top and bottom of
                        // the search square, which moves East-West

                        // for the north and south check, look
                        // at EW variations
                        if (dcPos[pos].direction == DCD_EW) {
                            for (i = round - 1; (i >= -1 * (round - 1)) && !set; i--) {
                                ret = isAreaClearBW(xpoint + i,
                                        ypoint,
                                        testPoint);

                                // If we've found a clear spot, jump
                                // out.
                                if (ret != null)
                                    break;
                            }

                        } else {

                            // This part of the code handles the
                            // corners of the square.

                            // looking at the corners for the
                            // search pattern
                            ret = isAreaClearBW(xpoint, ypoint, testPoint);
                        }
                    }
                }
            }

            if (round > round_limit) {
                break;
            }
        } // while (ret == null)

        if (ret != null) {
            if (Debug.debugging("declutter")) {
                Debug.output("Placing object at " + ret.x + " | " + ret.y);
            }
        } else {
            Debug.message("declutter", "Decluttering: No space for entry.");
            // If you got here, space not found, toss
            // oject offscreen
            ret = testPoint;
            ret.x = -1 * windex;
            ret.y = -1 * hindex;
        }

        return ret;
    }

    /*****************************************************************
     * Protected Methods
     ****************************************************************/

    /**
     * Check to see if there is space to the right of the desired place.
     * 
     * @return point for a good clear space, null if not.
     */
    protected Point isAreaClearR(int xPoint, int yPoint, Point point) {
        Debug.message("declutterdetail",
                "Decluttering: Checking to the right...");
        if (!indexes.setFromPixels(xPoint, yPoint)) {
            return null;
        }

        if (isClear(indexes, true)) {
            point.x = xPoint;
            point.y = yPoint;
            Debug.message("declutterdetail",
                    "*******Decluttering: found a spot");

            return point;
        }
        return null;
    }

    /**
     * Check to see if there is space to the left and right. This just checks
     * for all the way to the right, and all the way to the left.
     * 
     * @return Point for a good space, null if not.
     */
    protected Point isAreaClearBWT(int xPoint, int yPoint, Point point) {
        Debug.message("declutterdetail", "Decluttering: Checking both ways...");
        if (!indexes.setFromPixels(xPoint, yPoint)) {
            return null;
        }

        if (isClear(indexes, true)) {
            point.x = xPoint;
            point.y = yPoint;
            Debug.message("declutterdetail",
                    "*******Decluttering: found a spot");

            return point;
        }

        int leftMostIndex = indexes.origXIndex - indexes.origIndexLength;

        if (!indexes.set(leftMostIndex, indexes.origYIndex)) {
            return null;
        }

        if (isClear(indexes, true)) {
            point.x = leftMostIndex * x_pix_interval;
            point.y = yPoint;
            Debug.message("declutterdetail",
                    "*******Decluttering: found a spot");

            return point;
        }
        return null;
    }

    /**
     * Looks both ways for a clear space BW = Both ways = look both ways = check
     * left and right. This method will look all the way to the rigth, and then
     * check incrementally to the left until it's looking all the way there.
     * 
     * @return point of some good place is found, null if not.
     */
    protected Point isAreaClearBW(int xPoint, int yPoint, Point point) {

        Debug.message("declutterdetail", "Decluttering: Checking both ways...");

        // Check to see if it's totally offscreen. If it is, keep it
        // there.. Also check to see if the first location is good
        // (isClear)
        if (!indexes.setFromPixels(xPoint, yPoint) || isClear(indexes, true)) {
            point.x = xPoint;
            point.y = yPoint;
            return point;
        }

        // Guess not. Step our way to the left to see if anything is
        // available...

        // We're going to test the right, and then work our way left
        // until the original spot is the rightmost spot.
        int leftMostIndex = indexes.origXIndex - indexes.origIndexLength;

        // Start by keeping track of clear vertical cells. If we get
        // a run of them that equals the origIndexLength, then we have
        // space right there.
        int count = 0;
        int currentXIndex = indexes.origXIndex + indexes.origIndexLength;

        while (count < indexes.origIndexLength && currentXIndex > leftMostIndex) {

            if (!indexes.set(currentXIndex, indexes.origYIndex)) {
                // Still off the matrix
                count = 0;
                currentXIndex--;
                continue;
            }

            if (currentXIndex >= 0 && currentXIndex <= maxx) {
                if (!isMatrixLocationTaken(currentXIndex,
                        indexes.yStart,
                        indexes.yEnd - indexes.yStart + 1)) {
                    count++; // clear column
                } else {
                    count = 0; // Start counting again
                }
            } else {
                // If we are off the matrix, check and see if we want
                // to consider those space as open autmatically.
                if (allowPartials) {
                    count++;
                } else {
                    // This will not, Because it makes it look like
                    // we ran out of space.
                    count = 0;
                }
            }

            if (count < indexes.origIndexLength) {
                currentXIndex--;
            }

        }

        // So, either we ran out of space, or we found a space big
        // enough for the text.
        if (count >= indexes.origIndexLength) {
            point.x = currentXIndex * x_pix_interval;
            point.y = yPoint;

            indexes.xStart = currentXIndex;
            setTaken(indexes);
            Debug.message("declutterdetail", "Decluttering: found a spot");
            return point;
        }
        // Ran out of space.
        return null;
    }

    private static java.awt.Graphics2D workingGraphics = null;

    /**
     * This is a graphics that is only available to fiddle around with text and
     * fonts, in order to get pre-measurements. DO NOT write anything into this
     * thing.
     * 
     * @return java.awt.Graphics2D
     */
    public static java.awt.Graphics2D getGraphics() {
        if (workingGraphics == null) {
            BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            workingGraphics = ge.createGraphics(bi);
        }

        return workingGraphics;
    }

}