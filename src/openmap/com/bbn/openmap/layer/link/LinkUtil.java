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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkUtil.java,v $
// $RCSfile: LinkUtil.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import java.io.*;

public class LinkUtil {

    /** 
     * readString reads an expected number of characters off a
     * DataInput and creates a String from it.
     *
     * @param length the number of characters to be read.. 
     */
    protected static String readString(DataInput dis, int length)
	throws IOException, ArrayIndexOutOfBoundsException {
	String ret = null;
	char[] chars = new char[length];

	for (int i = 0; i < length; i++){
	    chars[i] = dis.readChar();
	}
	ret = new String(chars);
	return ret;
    }

    /** Provided as a readability convenience. */
    public static int setMask(int value, int mask){
	return (value | mask);
    }

    /** Provided as a readability convenience. */
    public static int unsetMask(int value, int mask){
	return (value & ~mask);
    }

    /** Provided as a readability convenience. */
    public static boolean isMask(int value, int mask){
	if ((value & mask) == 0){
	    return false;
	}
	return true;
    }
}
