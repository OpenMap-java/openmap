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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/PropertyStringFormatException.java,v $
// $RCSfile: PropertyStringFormatException.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util;

/** 
 */
public class PropertyStringFormatException extends RuntimeException {
    /**
     * Constructs a <code>MapRequestFormatException</code> with <tt>null</tt> 
     * as its error message string.
     */
    public PropertyStringFormatException() {
	super();
    }

    /**
     * Constructs a <code>MapRequestFormatException</code>, saving a reference 
     * to the error message string <tt>s</tt> for later retrieval by the 
     * <tt>getMessage</tt> method.
     *
     * @param s the detail message.
     */
    public PropertyStringFormatException(String s) {
	super(s);
    }
}

