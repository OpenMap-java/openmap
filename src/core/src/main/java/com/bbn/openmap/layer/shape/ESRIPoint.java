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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ESRIPoint.java,v $
// $RCSfile: ESRIPoint.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.text.NumberFormat;

import com.bbn.openmap.dataAccess.shape.ShapeUtils;
import com.bbn.openmap.util.HashCodeUtil;

/**
 * A class representing an x,y coordinate.
 * 
 * @author Ray Tomlinson
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.3 $ $Date: 2004/10/14 18:06:04 $
 */
public class ESRIPoint {

    /** A formatting instance for string conversion. */
    static NumberFormat format = NumberFormat.getInstance();

    /** Static initializer for <code>format</code> */
    static {
        format.setMaximumFractionDigits(6);
        format.setMinimumFractionDigits(6);
    }

    /** The X coordinate. */
    public double x;

    /** The Y coordinate. */
    public double y;

    /** Null constructor. */
    public ESRIPoint() {
    }

    /**
     * Initializes a point with the given coordinates.
     * 
     * @param _x the x coordinate
     * @param _y the y coordinate
     */
    public ESRIPoint(double _x, double _y) {
        x = _x;
        y = _y;
    }

    /**
     * Returns the X coordinate of this point.
     * 
     * @return the x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of this point.
     * 
     * @return the y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Converts this point to an appropriate string representation.
     * 
     * @return a string representing this point
     */
    public String toString() {
        return "ESRIPoint[" + format.format(x) + "," + format.format(y) + "]";
    }

    /**
     *  
     */
    // public int binaryStoreSize() {
    // return 28; // Constant for Point records
    // }
    /**
     * Constructs a point from the given data buffer.
     * 
     * @param b the data buffer
     * @param off the offset into the buffer
     */
    public ESRIPoint(byte b[], int off) {
        x = ShapeUtils.readLEDouble(b, off);
        y = ShapeUtils.readLEDouble(b, off + 8);
    }

    /**
     * Writes this point to the given buffer at the given offset.
     * 
     * @param b the data buffer
     * @param off the offset into the buffer
     * @return then number of bytes written
     */
    public int write(byte[] b, int off) {
        int nBytes = ShapeUtils.writeLEDouble(b, off, x);
        nBytes += ShapeUtils.writeLEDouble(b, off + nBytes, y);
        return nBytes;
    }

    /**
     * Determines equality of this instance with any other instance.
     * 
     * @param obj another instance
     * @return true if equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ESRIPoint pt = (ESRIPoint) obj;
        return (x == pt.x && y == pt.y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = HashCodeUtil.SEED;
        // collect the contributions of various fields
        result = HashCodeUtil.hash(result, x);
        result = HashCodeUtil.hash(result, y);
        return result;
    }
}