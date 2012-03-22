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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/CoordFloatString.java,v $
// $RCSfile: CoordFloatString.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.EOFException;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * Encapsulate the VPF Float Coordinate String primitive datatype.
 */
public class CoordFloatString {

    /** the number of tuples */
    public int tcount;
    /** the number of elements in a tuple */
    public int tsize;
    /** where we store the data as x1,y1,z1,x2,y2,z2,... */
    public double vals[];

    /**
     * Construct a CoordFloatString from a file input
     * 
     * @param tuplecount the number of tuples to read from the input
     *        stream
     * @param tuplesize the number of floats in the tuple
     * @param input the input stream to read everything from
     * @exception FormatException if we have IO errors or premature
     *            end-of-file
     */
    public CoordFloatString(int tuplecount, int tuplesize, BinaryFile input)
            throws FormatException {
        tcount = tuplecount;
        tsize = tuplesize;
        int totallen = tcount * tsize;
        vals = new double[totallen];
        try {
            input.readFloatArray(vals, 0, totallen);
        } catch (EOFException e) {
            throw new FormatException("CoordFloatString EOFException");
        }
    }

    /**
     * The maximum indexable tuple value
     * 
     * @return the maximum valid tuple index
     */
    public int maxIndex() {
        return tcount;
    }

    /**
     * A pretty formatter for the floatstring
     * 
     * @return a pretty string of the tuple
     */
    public String toString() {
        boolean singleCoord = (vals.length == tsize);
        StringBuffer retval = new StringBuffer(singleCoord ? "" : "CFS:");
        if (!singleCoord) {
            retval.append(tcount).append("[");
        }

        if (vals.length > 0) {
            retval.append("(").append(vals[0]);
            for (int j = 1; j < tsize; j++) {
                retval.append(", ").append(vals[j]);
            }
            retval.append(")");
        }
        if (vals.length > tsize) {
            retval.append(" ... (").append(vals[vals.length - tsize]);
            for (int j = tsize - 1; j > 0; j--) {
                retval.append(", ").append(vals[vals.length - j]);
            }
            retval.append(")");
        }

        if (!singleCoord) {
            retval.append("]");
        }
        return retval.toString();
    }

    /**
     * Get the first value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the first value of the tuple given by
     *         <code>tuple</code>
     */
    public double getXasFloat(int tuple) {
        return vals[tuple * tsize];
    }

    /**
     * Get the second value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the second value of the tuple given by
     *         <code>tuple</code>
     */
    public double getYasFloat(int tuple) {
        return vals[tuple * tsize + 1];
    }

    /**
     * Get the third value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the third value of the tuple given by
     *         <code>tuple</code>
     */
    public double getZasFloat(int tuple) {
        return vals[tuple * tsize + 2];
    }

    /**
     * Get a tuple
     * 
     * @param tuple the index of the tuple
     * @return the tuple given by <code>tuple</code>
     */
    public double[] getasFloatV(int tuple) {
        double rv[] = new double[tsize];
        for (int i = 0; i < tsize; i++) {
            rv[i] = vals[tsize * tuple + i];
        }
        return rv;
    }

    /**
     * Get a value in a tuple
     * 
     * @param tuple the index of the tuple
     * @param val the index of the value
     * @return the tuple given by <code>tuple</code>
     */
    public double getasFloat(int tuple, int val) {
        return vals[tuple * tsize + val];
    }

}