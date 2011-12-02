// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
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
// $Revision: 1.12 $
// $Date: 2005/08/11 20:39:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The CodePosition class is a base class used to interpret and
 * organize role settings for a symbol. The SymbolCode describes a
 * symbol and its role, with different characters having defined
 * meanings and optional values, depending on the symbol. The
 * CodePosition object defines a character and meaning for a character
 * in a certain place. A SymbolPart refers to a particlar CodePosition
 * that uniquely defines it, giving it some organizational meaning.
 * SymbolParts that share a parent can get to the parent's CodePositin
 * to see that meaning as well.
 * <P>
 * 
 * CodePositions have some intelligence for parsing position
 * properties and hierarchy properties, which allow the whole symbol
 * tree to be defined.
 * <P>
 * 
 * CodePositions server a couple of different roles. Some CodePosition
 * objects organize the kinds of set values that may be applicable for
 * a certain character position, and can offer those choices. These
 * organizational CodePositions won't have a SymbolPart to represent
 * itself. Other CodePositions, including the choices and those tied
 * directly to SymbolParts in the SymbolPart tree, don't offer choices
 * but can provide SymbolParts to represent themselves in the symbol.
 */
public class CodePosition {

    public final static char NO_CHAR = ' ';
    public final static int NO_NUMBER = -1;

    protected int hierarchyNumber;
    protected String id;
    protected String prettyName;
    protected int startIndex;
    protected int endIndex;
    protected CodePosition nextPosition = null;
    protected SymbolPart symbolPart = null;

    public boolean DEBUG = false;

    /** Property file property for pretty name 'name' */
    public final static String NameProperty = "name";

    /**
     * Property file property for a classname representing the next
     * position in the position tree 'next'.
     */
    public final static String NextProperty = "next";

    /**
     * A list of CodePosition choices for this position. This is only
     * used for a single instance of the CodePosition that in turn
     * holds this list of possible versions.
     */
    protected List choices;

    public CodePosition() {
        DEBUG = Debug.debugging("codeposition");
    }

    public CodePosition(String name, int start, int end) {
        this();
        startIndex = start - 1;
        endIndex = end;
        prettyName = name;
    }

    /**
     * A query method that answers of the given 15 digit code applies
     * to this symbol part.
     * 
     * @param queryCode
     * @return true if the code applies to this position.
     */
    public boolean codeMatches(String queryCode) {
        int length = id.length();

        if (Debug.debugging("symbology.detail")) {
            Debug.output("Checking " + queryCode + " against |" + id
                    + "| starting at " + startIndex + " for " + length);
        }
        return queryCode.regionMatches(true, startIndex, id, 0, length);
    }

    /**
     * Get the current list of CodePosition possibilities. Only returns
     * a list for the CodePositions used to parse the position
     * properties.
     */
    public List getPositionChoices() {
        return choices;
    }

    /**
     * Get a CodePosition from this list of available possibilities
     * given the hierarchy number for the position. Not all positions
     * have a hierarchy number, but the number given in the positions
     * properties will probably suffice.
     */
    public CodePosition getFromChoices(int hierarchyNumber) {
        List aList = getPositionChoices();
        if (aList != null) {
            for (Iterator it = aList.iterator(); it.hasNext();) {
                CodePosition cp = (CodePosition) it.next();
                if (hierarchyNumber == cp.getHierarchyNumber()) {
                    return cp;
                }
            }
        }
        return null;
    }

    /**
     * Method to add a position to the choices for this particular
     * code position.
     * 
     * @param index the hierarchical index for this position choice.
     *        This really only becomes important for those
     *        CodePositions which are used for interpreting the
     *        hierarchy properties. Other positions can use them for
     *        convenience, and this value will probably be just an
     *        ordering number for this choice out of all the other
     *        choices for the position.
     * @param entry this should be character or characters used in the
     *        symbol code for this particular position choice.
     * @param prefix the scoping property prefix used for all the
     *        properties. The entry is discovered by looking in the
     *        properties for this 'prefix.index'. Then other
     *        properties are discovered by looking for
     *        'prefix.entry.propertyKey' properties.
     * @param props the position properties.
     */
    protected CodePosition addPositionChoice(int index, String entry,
                                             String prefix, Properties props) {
        String className = this.getClass().getName();
        CodePosition cp = (CodePosition) ComponentFactory.create(className);
        if (cp != null) {
            if (DEBUG) {
                Debug.output("CodePosition:  created position (" + className
                        + ")");
            }

            // Before prefix is modified
            cp.symbolPart = getSymbolPart(prefix + entry, prefix, props);

            prefix = PropUtils.getScopedPropertyPrefix(prefix) + entry + ".";

            // Might not mean anything for option-type positions
            cp.hierarchyNumber = index;
            //cp.id = entry.charAt(0); // ASSUMED, but breaks
            // multi-character codes
            cp.id = entry;
            cp.prettyName = props.getProperty(prefix + NameProperty);
            addPositionChoice(cp);
        } else {
            if (DEBUG) {
                Debug.output("CodePosition: couldn't create position ("
                        + className + ")");
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
     * 
     * @param entry should be prefix of the overall position class
     *        along with the symbol representation for that position.
     * @param prefix should just be the prefix for the overall
     *        position class, including the period before the symbol
     *        representation for that position.
     * @param props the position properties.
     */
    protected SymbolPart getSymbolPart(String entry, String prefix,
                                       Properties props) {
        int offset = prefix.length();
        return new SymbolPart(this, entry, props, null, offset, offset
                + endIndex - startIndex, false);
    }

    protected void parseHierarchy(String hCode, Properties props,
                                  SymbolPart parent) {

        List parentList = null;
        int levelCounter = 1;

        while (levelCounter > 0) {

            String hierarchyCode = hCode + "." + levelCounter;

            if (DEBUG) {
                Debug.output("CodePosition.parse: " + hierarchyCode + " with "
                        + getPrettyName());
            }

            String entry = props.getProperty(hierarchyCode);

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
                    Debug.output("CodePosition.parse: adding "
                            + sp.getPrettyName() + " to "
                            + parent.getPrettyName());
                }

                parentList.add(sp);

                if (DEBUG) {
                    Debug.output("CodePosition.parse: looking for children of "
                            + sp.getPrettyName());
                }

                cp.parseHierarchy(hierarchyCode, props, sp);

                levelCounter++;

            } else {
                levelCounter = -1; // Flag to punch out of loop
            }
        }
    }

    /**
     * The SymbolPart tree can be represented by a hierarchy number
     * system, and this system is what is used in the hierarchy
     * properties file to build the symbol tree.
     */
    public int getHierarchyNumber() {
        return hierarchyNumber;
    }

    /**
     * Return a string version of the hierarchy number.
     */
    public String getHierarchyNumberString() {
        return Integer.toString(hierarchyNumber);
    }

    /**
     * Get the character, in the symbol code, that this position
     * represents.
     */
    public String getID() {
        return id;
    }

    /**
     * Get the pretty name that states what this position and
     * character represents.
     */
    public String getPrettyName() {
        return prettyName;
    }

    /**
     * Get the starting index of the span that this position
     * represents. This value is a java index value starting at 0.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Get the ending index of the span that this position represents.
     * This value is a java index value starting at 0.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Return the next CodePosition. An organizational tool to help
     * build the SymbolPart tree when parsing the hierarchy
     * properties.
     */
    public CodePosition getNextPosition() {
        return nextPosition;
    }

    public String toString() {
        //      return getPrettyName() + " [" + getID() + "] at " +
        //          getStartIndex() + ", " + getEndIndex();
        return getPrettyName();
    }

    protected CodePosition getNULLCodePosition() {
        String className = this.getClass().getName();
        CodePosition cp = (CodePosition) ComponentFactory.create(className);
        StringBuffer idbuf = new StringBuffer();
        for (int i = startIndex; i < endIndex; i++) {
            idbuf.append("*");
        }
        cp.id = idbuf.toString();
        cp.prettyName = "- Unspecified -";
        if (Debug.debugging("symbology")) {
            Debug.output("CodePosition: creating *unspecified* version of (" + className + ") with "
                    + cp.id + ", " + cp.prettyName);
        }
        return cp;
    }

}