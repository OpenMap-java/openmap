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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeOptions.java,v $
// $RCSfile: CodeOptions.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/17 00:23:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Iterator;
import java.util.List;

public class CodeOptions {
    protected List options;

    public CodeOptions(List opts) {
	options = opts;
    }
    
    public String toString() {
	StringBuffer sb = new StringBuffer("CodeOptions:\n");
	if (options != null) {
	    for (Iterator it = options.iterator(); it.hasNext();) {
		sb.append(((CodePosition)it.next()).toString() + "\n");
	    }
	}
	return sb.toString();
    }


}
