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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/CoordTupleString.java,v $
// $RCSfile: CoordTupleString.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

/**
 * Describe a common schema for accessing either double or float based
 * coordinate tuple strings (arrays of tuples).
 */
public interface CoordTupleString {

    /**
     * Accessor for the max tuple index
     * 
     * @return the max tuple index
     */
    public abstract int maxIndex();

    /**
     * Accessor to determine the actual type managed.
     * 
     * @return a Number the most directly corresponds to the
     *         underlying managed type (Float for a float tuplestring,
     *         Double for a double tuplestring, etc)
     */
    public abstract Number getPrimitiveType();

    /**
     * Accessor to retrieve a tuple
     * 
     * @param tuple the tuple to retrieve (the first tuple is index 0)
     * @return the tuple at index <code>tuple</code>, coerced into
     *         a float[]
     */
    public abstract float[] getasFloat(int tuple);

    /**
     * Accessor to retrieve a tuple
     * 
     * @param tuple the tuple to retrieve (the first tuple is index 0)
     * @return the tuple at index <code>tuple</code>, coerced into
     *         a double[]
     */
    public abstract double[] getasDouble(int tuple);

    /**
     * Accessor to retrieve a single value in a tuple
     * 
     * @param tuple the tuple to retrieve (the first tuple is index 0)
     * @param val the index of the value in the tuple (the first val
     *        is index 0)
     * @return the tuple at index <code>tuple</code>, coerced into
     *         a float
     */
    public abstract float getasFloat(int tuple, int val);

    /**
     * Accessor to retrieve a single value in a tuple
     * 
     * @param tuple the tuple to retrieve (the first tuple is index 0)
     * @param val the index of the value in the tuple (the first val
     *        is index 0)
     * @return the tuple at index <code>tuple</code>, coerced into
     *         a double
     */
    public abstract double getasDouble(int tuple, int val);
}