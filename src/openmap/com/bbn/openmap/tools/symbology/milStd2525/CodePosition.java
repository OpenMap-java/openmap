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
// $Revision: 1.3 $
// $Date: 2003/12/16 01:08:49 $
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

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class CodePosition {

    public final static char NO_CHAR = ' ';
    public final static int NO_NUMBER = -1;

    protected int heirarchyNumber;
    protected char id;
    protected String prettyName;
    protected int startIndex;
    protected int endIndex;
    protected CodePosition nextPosition = null;

    public static boolean DEBUG = false;

    /** Property file property for pretty name 'name' */
    public final static String NameProperty = "name";

    /** 
     * Property file property for a classname representing the next
     * position in the position tree 'next'. 
     */
    public final static String NextProperty = "next";

    /**
     * A list of CodePosition choices for this position.
     */
    protected List choices = new ArrayList();

    protected CodePosition() {};

    public CodePosition(String name, int start, int end) {
	DEBUG = Debug.debugging("codeposition");
	startIndex = start - 1;
	endIndex = end;
	prettyName = name;
    }

    public List getPositionChoices() {
	return choices;
    }

    public CodePosition getFromChoices(int heirarchyNumber) {
	List aList = getPositionChoices();
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

    /**
     */
    protected void parsePositions(String prefix, Properties props) {
	int index = 1;
	String entry = props.getProperty(PropUtils.getScopedPropertyPrefix(prefix) +
					 Integer.toString(index));
	while (entry != null) {
	    addPositionChoice(index, entry, prefix, props);
	    entry = props.getProperty(PropUtils.getScopedPropertyPrefix(prefix) +
				      Integer.toString(++index));
	}
    }

    protected CodePosition addPositionChoice(int index, String entry, 
					     String prefix, Properties props) {
	String className = this.getClass().getName();
	CodePosition cp = (CodePosition)ComponentFactory.create(className);
	if (cp != null) {
	    if (DEBUG) {
		Debug.output("CodePosition:  created position (" + className + ")");
	    }

	    prefix = PropUtils.getScopedPropertyPrefix(prefix) + entry + ".";
	    cp.heirarchyNumber = index;
	    cp.id = entry.charAt(0);  // ASSUMED
	    cp.prettyName = props.getProperty(prefix + NameProperty);
	    choices.add(cp);
	} else {
	    if (DEBUG) {
		Debug.output("CodePosition: couldn't create position (" + className + ")");
	    }
	}
	return cp;
    }

    protected void parseHeirarchy(String hCode, Properties props, SymbolPart parent) {
	
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

		cp.parseHeirarchy(heirarchyCode, props, sp);

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

    public CodePosition getNextPosition() {
	return nextPosition;
    }

}
