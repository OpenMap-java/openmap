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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/InvalidCharException.java,v $
// $RCSfile: InvalidCharException.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.io;

/**
 * An invalid character occured on in input stream.
 */
public class InvalidCharException extends FormatException {

    /** the invalid character that we found */
    final public char c;

    /**
     * Construct an object with no detail message
     * @param val the character encountered
     */
    public InvalidCharException(char val) {
	super();
	c = val;
    }

    /**
     * Construct an object with a detail message
     * @param s the detail message
     * @param val the character encountered
     */
    public InvalidCharException(String s, char val) {
	super(s);
	c = val;
    }
}
