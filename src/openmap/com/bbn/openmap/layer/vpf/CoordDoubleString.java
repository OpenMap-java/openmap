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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/CoordDoubleString.java,v $
// $RCSfile: CoordDoubleString.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.EOFException;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * Encapsulate the VPF Double Coordinate String primitive datatype.
 */

public class CoordDoubleString implements CoordTupleString {

    /** where we store the data */
    final private double vals[][];

    /**
     * Construct a CoordDoubleString from a file input
     * 
     * @param tuplecount the number of tuples to read from the input
     *        stream
     * @param tuplesize the number of doubles in the tuple
     * @param input the input stream to read everything from
     * @exception FormatException if we have IO errors or premature
     *            end-of-file
     */
    public CoordDoubleString(int tuplecount, int tuplesize, BinaryFile input)
            throws FormatException {
        try {
            vals = new double[tuplecount][tuplesize];
            for (int i = 0; i < tuplecount; i++)
                for (int j = 0; j < tuplesize; j++)
                    vals[i][j] = input.readDouble();
        } catch (EOFException e) {
            throw new FormatException("CoordDoubleString EOFException");
        }
    }

    /**
     * The maximum indexable tuple value
     * 
     * @return the maximum valid tuple index
     */
    public int maxIndex() {
        return vals.length;
    }

    /**
     * Accessor for the underlying primitive type
     * 
     * @return a Double, since that's what we manage
     */
    public Number getPrimitiveType() {
        return new Double(0.0);
    }

    /**
     * A pretty formatter for the doublestring
     * 
     * @return a pretty string of the tuple
     */
    public String toString() {
        StringBuffer retval = new StringBuffer("CDS:");
        retval.append(vals.length).append("[");

        for (int i = 0; i < vals.length; i++) {
            retval.append("(");
            for (int j = 0; j < vals[i].length; j++)
                retval.append(vals[i][j]).append(", ");
            retval.append(") ");
        }
        retval.append("]");
        return retval.toString();
    }

    /**
     * Get the first value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the first value of the tuple given by
     *         <code>tuple</code>
     */
    public float getXasFloat(int tuple) {
        return (float) vals[tuple][0];
    }

    /**
     * Get the first value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the first value of the tuple given by
     *         <code>tuple</code>
     */
    public double getXasDouble(int tuple) {
        return vals[tuple][0];
    }

    /**
     * Get the second value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the second value of the tuple given by
     *         <code>tuple</code>
     */
    public float getYasFloat(int tuple) {
        return (float) vals[tuple][1];
    }

    /**
     * Get the second value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the second value of the tuple given by
     *         <code>tuple</code>
     */
    public double getYasDouble(int tuple) {
        return vals[tuple][1];
    }

    /**
     * Get the third value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the third value of the tuple given by
     *         <code>tuple</code>
     */
    public float getZasFloat(int tuple) {
        if (vals[tuple].length >= 3) {
            return (float) vals[tuple][2];
        }
        return 0.0f;
    }

    /**
     * Get the third value of a tuple
     * 
     * @param tuple the index of the tuple
     * @return the third value of the tuple given by
     *         <code>tuple</code>
     */
    public double getZasDouble(int tuple) {
        if (vals[tuple].length >= 3) {
            return vals[tuple][2];
        }
        return 0.0;
    }

    /**
     * Get a tuple
     * 
     * @param tuple the index of the tuple
     * @return the tuple given by <code>tuple</code>
     */
    public float[] getasFloat(int tuple) {
        int tusize = vals[tuple].length;
        float rv[] = new float[tusize];
        for (int i = 0; i < tusize; i++)
            rv[i] = (float) vals[tuple][i];
        return rv;
    }

    /**
     * Get a tuple
     * 
     * @param tuple the index of the tuple
     * @return the tuple given by <code>tuple</code>
     */
    public double[] getasDouble(int tuple) {
        return vals[tuple];
    }

    /**
     * Get a value in a tuple
     * 
     * @param tuple the index of the tuple
     * @param val the index of the value
     * @return the tuple given by <code>tuple</code>
     */
    public float getasFloat(int tuple, int val) {
        return (float) vals[tuple][val];
    }

    /**
     * Get a value in a tuple
     * 
     * @param tuple the index of the tuple
     * @param val the index of the value
     * @return the tuple given by <code>tuple</code>
     */
    public double getasDouble(int tuple, int val) {
        return vals[tuple][val];
    }
}