// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;
import java.io.EOFException;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.io.BinaryFile;

/**
 * Encapsulate the VPF Float Coordinate String primitive datatype.
 */
public class CoordFloatString {

    /** the number of tuples */
    public int tcount;
    /** the number of elements in a tuple */
    public int tsize;
    /** where we store the data as x1,y1,z1,x2,y2,z2,... */
    public float vals[];

    /**
     * Construct a CoordFloatString from a file input
     *
     * @param tuplecount the number of tuples to read from the input stream
     * @param tuplesize the number of floats in the tuple
     * @param input the input stream to read everything from
     * @exception FormatException if we have IO errors or premature end-of-file
     */
    public CoordFloatString(int tuplecount, int tuplesize, BinaryFile input)
	throws FormatException {
	tcount = tuplecount;
	tsize = tuplesize;
	int totallen = tcount * tsize;
	vals = new float[totallen];
	try {
	    input.readFloatArray(vals, 0, totallen);
	} catch (EOFException e) {
	    throw new FormatException("CoordFloatString EOFException");
	}
    }

    /** The maximum indexable tuple value
     * @return the maximum valid tuple index */
    public int maxIndex() {
	return tcount;
    }

    /** A pretty formatter for the floatstring
     * @return a pretty string of the tuple */
    public String toString() {
	StringBuffer retval = new StringBuffer("CFS:");
	retval.append(vals.length).append("[");

	if (vals.length > 0) {
	    retval.append("(");
	    for (int j=0; j < tsize; j++) {
		retval.append(vals[j]).append(", ");
	    }
	    retval.append(") ");
	}
	if (vals.length > 1) {
	    retval.append("... (");
	    for (int j=tsize; j > 0; j--) {
		retval.append(vals[vals.length-j]).append(", ");
	    }
	    retval.append(") ");
	}

	retval.append("]");
	return retval.toString();
    }
 
    /** Get the first value of a tuple
     * @param tuple the index of the tuple
     * @return the first value of the tuple given by <code>tuple</code> */
    public float getXasFloat(int tuple) {
	return vals[tuple*tsize];
    }

    /** Get the second value of a tuple
     * @param tuple the index of the tuple
     * @return the second value of the tuple given by <code>tuple</code> */
    public float getYasFloat(int tuple) {
	return vals[tuple*tsize+1];
    }
  
    /** Get the third value of a tuple
     * @param tuple the index of the tuple
     * @return the third value of the tuple given by <code>tuple</code> */
    public float getZasFloat(int tuple) {
	return vals[tuple*tsize+2];
    }

    /** Get a tuple
     * @param tuple the index of the tuple
     * @return the tuple given by <code>tuple</code> */
    public float[] getasFloatV(int tuple) {
	float rv[] = new float[tsize];
	for (int i = 0 ; i < tsize; i++) {
	    rv[i] = vals[tsize * tuple + i];
	}
	return rv;
    }

    /** Get a value in a tuple
     * @param tuple the index of the tuple
     * @param val the index of the value
     * @return the tuple given by <code>tuple</code> */
    public float getasFloat(int tuple, int val) {
	return vals[tuple*tsize + val];
    }

}
