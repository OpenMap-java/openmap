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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeFunctionID.java,v $
// $RCSfile: CodeFunctionID.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

public class CodeFunctionID extends CodePosition {

    protected CodeFunctionID() {
	// Code function IDs are not kept around, they are just used
	// to create the SymbolPart Structure.  The first three
	// arguments in the super call are meaningless.
	this(5);
    }

    protected CodeFunctionID(int pos) {
	// Code function IDs are not kept around, they are just used
	// to create the SymbolPart Structure.  The first three
	// arguments in the super call are meaningless.
	super(0, ' ', null, pos, 10, CodeFunctionID.class);
    }

    protected void parse(String hCode, Properties props, SymbolPart parent) {
	
	List parentList = null;
	int subLevelNumber = 1;

	int pos = getStartIndex() + parent.positionShift;
	if (pos < 4) pos = 4;

	if (pos == 10) {
	    return;
	}

	// startIndex is one less that originally specified, need to
	// add an extra 1 to the new position of counteract that.
	CodeFunctionID cp = new CodeFunctionID(pos + 2);  

	while (subLevelNumber > 0) {
	    String hCode2 = hCode + "." + subLevelNumber;
	    String entry = props.getProperty(hCode2);
	    if (entry != null) {

		if (DEBUG) {
		    Debug.output("CodeFunctionID.parse: reading " + 
				 hCode2 + " as " + entry);
		}

		SymbolPart sp = new SymbolPart(cp, entry, props, parent);

		if (parentList == null) {
		    parentList = parent.getSubs();
		    if (parentList == null) {
			parentList = new ArrayList();
			parent.setSubs(parentList);
		    }
		}

		if (DEBUG) {
		    Debug.output("CodeFunctionID.parse: adding " + 
				 sp.getPrettyName() + 
				 " to " + parent.getPrettyName());
		}

		parentList.add(sp);

		if (DEBUG) {
		    Debug.output("CodePosition.parse: looking for children of " + 
			     sp.getPrettyName());
		}

		cp.parse(hCode2, props, sp);
		subLevelNumber++;

	    } else {
		subLevelNumber = -1;
	    }
	}
    }

}
