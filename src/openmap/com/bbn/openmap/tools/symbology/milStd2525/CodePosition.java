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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodePosition.java,v $
// $RCSfile: CodePosition.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

public class CodePosition {

    protected int heirarchyNumber;
    protected char id;
    protected String prettyName;
    protected int startIndex;
    protected int endIndex;
    protected Class nextPosition = null;

    public static boolean DEBUG = false;

    protected static Hashtable positions = new Hashtable();

    protected CodePosition(int heirarchyLevelNumber, char idChar, String name, 
			   int start, int end, Class np) {

	DEBUG = Debug.debugging("codeposition");
	startIndex = start - 1;
	endIndex = end;
	nextPosition = np;
	heirarchyNumber = heirarchyLevelNumber;
	id = idChar;
	prettyName = name;
    }

    public static List getList() {
	List list = (List)positions.get(CodePosition.class);
	if (list == null) {
	    list = new ArrayList();
	    positions.put(CodePosition.class, list);
	}
	return list;
    }

    public static CodePosition getFromList(int heirarchyNumber) {
	List aList = getList();
	if (aList != null) {
	    for (Iterator it = aList.iterator(); it.hasNext();) {
		CodePosition cp = (CodePosition)it.next();
		if (heirarchyNumber == cp.getHeirarchyNumber()) {
		    return cp;
		}
	    }
	}
	return null;
    }

    protected void parse(String hCode, Properties props, SymbolPart parent) {
	
	List parentList = null;
	int levelCounter = 1;

	while (levelCounter > 0) {

	    String heirarchyCode = hCode + "." + levelCounter;

	    if (DEBUG) {
		Debug.output("CodePosition.parse: " + heirarchyCode + 
			     " with " + getPrettyName());
	    }

	    String entry = props.getProperty(heirarchyCode);

	    if (entry != null) {
		CodeFunctionID cp = new CodeFunctionID();
		SymbolPart sp = new SymbolPart(cp, entry, props, parent);

		if (parentList == null) {
		    parentList = parent.getSubs();
		    if (parentList == null) {
			parentList = new ArrayList();
			parent.setSubs(parentList);
		    }
		}

		if (DEBUG) {
		    Debug.output("CodePosition.parse: adding " + 
				 sp.getPrettyName() + 
				 " to " + parent.getPrettyName());
		}

		parentList.add(sp);

		if (DEBUG) {
		    Debug.output("CodePosition.parse: looking for children of " + 
				 sp.getPrettyName());
		}

		cp.parse(heirarchyCode, props, sp);

		levelCounter++;

	    } else {
		levelCounter = -1; // Flag to punch out of loop
	    }
	}
    }

    public int getHeirarchyNumber() {
	return heirarchyNumber;
    }

    public String getHeirarchyNumberString() {
	return Integer.toString(heirarchyNumber);
    }

    public char getID() {
	return id;
    }

    public String getPrettyName() {
	return prettyName;
    }

    public int getStartIndex() {
	return startIndex;
    }

    public int getEndIndex() {
	return endIndex;
    }

    public Class getNextPosition() {
	return nextPosition;
    }

}
