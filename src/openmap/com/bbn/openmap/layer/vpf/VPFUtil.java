
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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFUtil.java,v $
// $RCSfile: VPFUtil.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import java.util.*;

/**
 * Miscellaneous utility functions in dealing with VPF data.
 */
public class VPFUtil {
    /**
     * all methods are static, no reason to construct
     */
    private VPFUtil() {};
  
    /**
     * returns a string with the elements of l separated by spaces
     * @param l the list to stringize
     * @return the string version of the list
     * @deprecated use listToString(List) instead
     */
    public static final String vectorToString(List l) {
	return listToString(l);
    }

    /**
     * returns a string with the elements of l separated by spaces
     * @param l the list to stringize
     * @return the string version of the list
     */
    public static final String listToString(List l) {
	StringBuffer row = new StringBuffer();
	ListIterator li = l.listIterator();
	while (li.hasNext()) {
	    row.append(li.next().toString()).append(" ");
	}
	return(row.toString());
    }
    
    /**
     * get the value contained in the object.
     * @param val returns the value of Shorts and Integers as an int.  VPF
     * null values get returned as Integer.MIN_VALUE, as do all other types
     * @return the value contained in val
     */
    public static final int objectToInt(Object val) {
	int v = Integer.MIN_VALUE;
	if (val instanceof Integer) {
	    v = ((Integer)val).intValue();
	    if (v == Integer.MIN_VALUE+1) {
		v = Integer.MIN_VALUE;
	    }
	} else if (val instanceof Short) {
	    v = ((Short)val).shortValue();
	    if (v == Short.MIN_VALUE+1) {
		v = Integer.MIN_VALUE;
	    }
	}
	return v;
    }

    /** some strings */
    public final static String Edge = "Edge";
    public final static String Edges = "Edges";
    public final static String Text = "Text";
    public final static String Area = "Area";
    //public final static String Point = "Point";
    public final static String EPoint = "EPoint";
    public final static String CPoint = "CPoint";

    /**
     * Parses dynamic args passed by specialist client.  A
     * <code>Hashtable</code> is returned as a unified holder
     * of all dynamic arguments.
     */
    public static Hashtable parseDynamicArgs(String args) {
	Hashtable dynArgs = new Hashtable();
	if (args != null) {
	    String lowerArgs = args.toLowerCase();

	    dynArgs.put(Edges, new Boolean(lowerArgs.indexOf(Edges) != -1));
	    dynArgs.put(Text, new Boolean(lowerArgs.indexOf(Text) != -1));
	    dynArgs.put(Area, new Boolean(lowerArgs.indexOf(Area) != -1));
	    dynArgs.put(EPoint, new Boolean(lowerArgs.indexOf(EPoint) != -1));
	    dynArgs.put(CPoint, new Boolean(lowerArgs.indexOf(CPoint) != -1));
	}
	return dynArgs;
    }

    /**
     * If <code>arg</code> maps to a <code>Boolean</code> in the Hashtable,
     * that value is returned,  <code>false</code> otherwise.
     * @param dynArgs the Hashtable to look in
     * @param arg the argument to return
     */
    public static boolean getHashedValueAsBoolean(Hashtable dynArgs,
						  String arg) {
	Object obj = dynArgs.get(arg);
	if (obj == null) {
	    return false;
	} else if (obj instanceof Boolean) {
	    return ((Boolean)obj).booleanValue();
	} else {
	    return false;
	}
    }
}
