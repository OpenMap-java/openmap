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
// $Revision: 1.4 $
// $Date: 2003/12/17 00:23:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
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
    protected SymbolPart symbolPart = null;

    public static boolean DEBUG = false;

    /** Property file property for pretty name 'name' */
    public final static String NameProperty = "name";

    /** 
     * Property file property for a classname representing the next
     * position in the position tree 'next'. 
     */
    public final static String NextProperty = "next";

    /**
     * A list of CodePosition choices for this position.  This is only
     * used for a single instance of the CodePosition that in turn
     * holds this list of possible versions.
     */
    protected List choices;

    public CodePosition() {
	DEBUG = Debug.debugging("codeposition");
    };

    public CodePosition(String name, int start, int end) {
	this();
	startIndex = start - 1;
	endIndex = end;
	prettyName = name;
    }

    /**
     * Get the current list of CodePosition possibilies.  Only returns
     * a list for the CodePositions used to parse the position
     * properties.
     */
    public List getPositionChoices() {
	return choices;
    }

    /**
     * Get a CodePosition from this list of available possibilities
     * given the heirarchy number for the position.  Not all positions
     * have a heirarchy number, but the number given in the positions
     * properties will probably suffice.
     */
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
     * Method to add a position to the choices for this particular code position.
     * @param index the heirarhical index for this position choice.
     * This really only becomes important for those CodePositions
     * which are used for interpreting the heirarchy properties.
     * Other positions can use them for convenience, and this value
     * will probably be just an ordering number for this choice out of
     * all the other choices for the position.
     * @param entry this should be character or characters used in the
     * symbol code for this particular position choice.
     * @param prefix the scoping property prefix used for all the
     * properties.  The entry is discovered by looking in the
     * properties for this 'prefix.index'.  Then other properties are
     * discovered by looking for 'prefix.entry.propertyKey' properties.
     * @param props the position properties.
     */
    protected CodePosition addPositionChoice(int index, String entry, 
					     String prefix, Properties props) {
	String className = this.getClass().getName();
	CodePosition cp = (CodePosition)ComponentFactory.create(className);
	if (cp != null) {
	    if (DEBUG) {
		Debug.output("CodePosition:  created position (" + className + ")");
	    }

	    // Before prefix is modified
	    cp.symbolPart = getSymbolPart(prefix + entry, prefix, props);

	    prefix = PropUtils.getScopedPropertyPrefix(prefix) + entry + ".";

	    // Might not mean anything for option-type positions
	    cp.heirarchyNumber = index;
	    cp.id = entry.charAt(0);  // ASSUMED
	    cp.prettyName = props.getProperty(prefix + NameProperty);
	    addPositionChoice(cp);
	} else {
	    if (DEBUG) {
		Debug.output("CodePosition: couldn't create position (" + className + ")");
	    }
	}
	return cp;
    }

    /**
     * Add the CodePosition to the choices, creating the choices List
     * if needed.
     */
    public void addPositionChoice(CodePosition cp) {
	if (choices == null) {
	    choices = new LinkedList();
	}
	choices.add(cp);
    }

    /**
     * This method reads Properties to add choices to this class as
     * options for what values are valid in this position.
     */
    protected void parsePositions(String prefix, Properties props) {
	int index = 1;
	prefix = PropUtils.getScopedPropertyPrefix(prefix);
	String entry = props.getProperty(prefix + Integer.toString(index));
	while (entry != null) {
	    addPositionChoice(index, entry, prefix, props);
	    entry = props.getProperty(prefix + Integer.toString(++index));
	}
    }

    /**
     * A method called when parsing position properties.
     * @param entry should be prefix of the overall position class
     * along with the symbol representation for that position.
     * @param prefix should just be the prefix for the overall
     * position class, including the period before the symbol
     * representation for that position.
     * @param props the position properties.
     */
    protected SymbolPart getSymbolPart(String entry, String prefix, Properties props) {
	int offset = prefix.length();
	return new SymbolPart(this, entry, props, null, offset, 
			      offset + endIndex - startIndex, false);
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
			parentList = new LinkedList();
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

    public String toString() {
	return getPrettyName() + " [" + getID() + "] at " + 
	    getStartIndex() + ", " + getEndIndex();
    }
}
