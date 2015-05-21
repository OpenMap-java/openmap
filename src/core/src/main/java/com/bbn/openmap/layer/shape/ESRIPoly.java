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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ESRIPoly.java,v $
// $RCSfile: ESRIPoly.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import com.bbn.openmap.dataAccess.shape.ShapeUtils;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;

/**
 * A representation of a shape file polygon, subordinate to an
 * <code>ESRIPolygonRecord</code>. This is an abstract class,
 * please use one of the fully derived internal classes to do your
 * bidding.
 * 
 * <h3>TODO</h3>
 * <ul>
 * <li>Write double-floating point representation of an ESRIPoly.
 * <li>Make sure to abstract float point vs double float in other
 * objects of the ShapeFile reading code.
 * </ul>
 * 
 * @author Ray Tomlinson
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @author HACK-author blame it on aculline
 * @version $Revision: 1.5 $ $Date: 2009/01/21 01:24:42 $
 */
public abstract class ESRIPoly extends ShapeUtils {

    /**
     * The number of (y,x) pairs of the ESRIPoly. This value should be
     * the <code>points.length&gt;&gt;1</code>.
     */
    public int nPoints;

    /**
     * Poly with data stored in floating point format. This ESRIPoly
     * has been optimized for reading data in the OpenMap environment.
     * By default, The internal points are stored in y,x,y,x
     * (lat,lon,...) order as radian values. There have been
     * allowances made to allow internal coordinates to be stored as
     * decimal degree values, but this is not the best way to do
     * things. The projection libraries are tuned for polygons holding
     * radian point coordinates.
     */
    public static class ESRIFloatPoly extends ESRIPoly {

        /**
         * A vector of vertices, stored as RADIAN y,x,y,x,
         * (lat,lon,...). This is to allow for optimized processing by
         * OpenMap.
         */
        protected double[] radians;

        /**
         * Flag noting if the internal representation of coordinates
         * is in RADIANS, or DECIMAL_DEGREES. By default, the
         * coordinates are in radians. But, a constructor is available
         * to let them be noted as DECIMAL_DEGREES. Also, if you ask
         * for the coordinates as radians, they get converted to
         * radians internally, and this flag gets set accordingly.
         * Likewise, if you as for recimal degrees coordinates, the
         * coordinates internally will be changed to reflect that.
         */
        protected boolean isRadians = true;

        /**
         * Construct a poly with the given number of points. Remember
         * to `+2' if you want all vertices for a polygon.
         * 
         * @param nPts the number of (y,x) pairs
         */
        public ESRIFloatPoly(int nPts) {
            if (Debug.debugging("shape") && (nPts > 50000)) {
                Debug.output("ESRIPoly w/" + nPts + " points");
            }
            nPoints = nPts;
            radians = new double[nPoints * 2];
        }

        /**
         * Construct an ESRIFloatPoly. Remember to `+2' if you want
         * all vertices for a polygon.
         * 
         * @param radians float[] coordinates: y,x,y,x,... (lat,lon)
         *        order in RADIANS!
         */
        public ESRIFloatPoly(double[] radians) {
            this.radians = radians;
            this.isRadians = true;
            nPoints = radians.length / 2;
        }

        /**
         * Construct an ESRIFloatPoly. Remember to `+2' if you want
         * all vertices for a polygon.
         * 
         * @param radians float[] coordinates: y,x,y,x,... (lat,lon)
         *        order in RADIANS!
         */
        public ESRIFloatPoly(double[] radians, boolean isRadians) {
            this.radians = radians;
            nPoints = radians.length / 2;
            this.isRadians = isRadians;
        }

        /**
         * Get the internal points array. If the internal points are
         * not presently stored as radians, they will be permanently
         * converted.
         * 
         * @return float[] RADIAN y,x,y,x,... (lat,lon)
         */
        public double[] getRadians() {
            if (!isRadians) {
                ProjMath.arrayDegToRad(radians);
                isRadians = true;
            }
            return radians;
        }

        /**
         * Get the internal points array. If the internal points are
         * not presently stored as decimal degree values, the will be
         * permanently.
         * 
         * @return float[] DECIMAL_DEGREES y,x,y,x,... (lat,lon)
         */
        public double[] getDecimalDegrees() {
            if (isRadians) {
                ProjMath.arrayRadToDeg(radians);
                isRadians = false;
            }
            return radians;
        }

        /**
         * Reads a polygon from the given buffer starting at the given
         * offset.
         * 
         * @param b the buffer
         * @param off the offset
         * @param connect connect the points (polygon)
         * @return the number of bytes read
         */
        public int read(byte b[], int off, boolean connect) {
            int i, ptr = off;
            int end = (connect) ? radians.length - 2 : radians.length;
            for (i = 0; i < end; i += 2) {
                // REMEMBER: y,x order (lat,lon order)
                radians[i + 1] = ProjMath.degToRad((float) readLEDouble(b, ptr));//x
                                                                                 // (lon)
                ptr += 8;

                radians[i] = ProjMath.degToRad((float) readLEDouble(b, ptr));//y
                                                                             // (lat)
                ptr += 8;
            }
            // cap the points if polygon, assuming enough space in
            // array...
            if (connect) {
                radians[i] = radians[0];
                radians[i + 1] = radians[1];
            }
            return ptr - off;
        }

        /**
         * Returns the x coordinate of the indicated vertex.
         * 
         * @param index the ordinal of the vertex of interest
         * @return the x (longitude) coordinate in decimal degrees
         */
        public double getX(int index) {
            // REMEMBER: y,x order (lat,lon order)
            return ProjMath.radToDeg(radians[(index * 2) + 1]);//x
                                                               // (lon)
        }

        /**
         * Returns the y coordinate of the indicated vertex.
         * 
         * @param index the ordinal of the vertex of interest
         * @return the y (latitude) coordinate in decimal degrees
         */
        public double getY(int index) {
            // REMEMBER: y,x order (lat,lon order)
            return ProjMath.radToDeg(radians[(index * 2)]);//y (lat)
        }
    }

    /**
     * Reads a polygon from the given buffer starting at the given
     * offset.
     * 
     * @param b the buffer
     * @param off the offset
     * @return the number of bytes read
     */
    public abstract int read(byte b[], int off, boolean connect);
}