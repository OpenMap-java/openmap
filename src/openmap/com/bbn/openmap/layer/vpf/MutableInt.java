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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/MutableInt.java,v $
// $RCSfile: MutableInt.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

/** 
 * Implement a wrapper class to allow mutable ints.
 */
public class MutableInt {
    /** our value */
    public int value;

    /**
     * Construct a object with a value
     * @param newval our value
     */
    public MutableInt(int newval) {
        value = newval;
    }
    
    /**
     * Construct an object with the default value.
     */
    public MutableInt() {
    }
}
