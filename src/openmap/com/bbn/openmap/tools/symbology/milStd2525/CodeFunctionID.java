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
// $Revision: 1.1 $
// $Date: 2003/12/08 18:37:51 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

public class CodeFunctionID extends CodePosition {

    public int position = 5;
    public final static int length = 1;
    public Class nextPosition = CodeFunctionID.class;

    protected CodeFunctionID() {
	// Position == 5
    }

    protected CodeFunctionID(int pos) {
	position = pos;
    }

    protected void parse(String hCode, Properties props, SymbolPart parent) {
	
	List parentList = null;
	int subLevelNumber = 1;

	int pos = getPosition();

	if (pos == 10) {
	    return;
	}

	CodeFunctionID cp = new CodeFunctionID(pos + 1);

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
		    Debug.output("CodeFunctionID.parse: adding " + sp.getPrettyName() + " to " + parent.getPrettyName());
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

    public int getPosition() {
	return position;
    }

    public int getLength() {
	return length;
    }

    public Class getNextPosition() {
	return nextPosition;
    }
}
