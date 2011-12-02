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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolPart.java,v $
// $RCSfile: SymbolPart.java,v $
// $Revision: 1.12 $
// $Date: 2005/08/11 20:39:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.dataAccess.cgm.CGM;
import com.bbn.openmap.util.Debug;

/**
 * The SymbolPart class represents one part in a hierarchy of pieces
 * needed to represent an actual symbol. A symbol may require
 * geometries from its parents, as each piece further down the
 * hierarchy makes each symbol's meaning more specific, or scoped for
 * a particular task. The top-level SymbolPart represents the entire
 * symbology tree. Descending down through the tree, a SymbolPart
 * representing one of the 5 Scheme sections is next, with the lower
 * levels dependent on the Scheme definitions.
 * <P>
 * 
 * The SymbolPart is smart enough to use the hierarchy.properties file
 * that defines the symbol set and create the symbol tree using the
 * appropriate Code classes. Not all Code classes help define the
 * tree, because some aspects of a symbol are flexible, like the
 * Affiliation (enemy, friend, etc). The SymbolPart tree only defines
 * some aspects of the symbol. Other parts of the symbol are dependent
 * on these flexible variations that are provided to the SymbolPart at
 * the time icons are created.
 */
public class SymbolPart {

    public final static String DEFAULT_SYMBOL_CODE = "               ";

    /** Property file property for pretty name 'name' */
    public final static String NameProperty = "name";
    /** Property file property for cgm file too represent the symbol. */
    public final static String CGMProperty = "cgm";

    /**
     * The Object that describes the location of this symbol part in
     * the symbol hierarchy as defined by the 15 digit symbol code.
     */
    protected CodePosition codePosition;
    /**
     * The part of the symbol code unique to this symbol part. Parents
     * and children will make up the other parts of the code.
     */
    protected String code;
    /**
     * The pretty name for a symbol represented by this SymbolPart at
     * this point in the hierarchy.
     */
    protected String prettyName;
    /**
     * The file containing the symbol geometry for this SymbolPart.
     */
    protected String cgmName;
    /**
     * The symbol geometry object for this SymbolPart.
     */
    protected CGM cgm;
    /**
     * A list of children SymbolParts relative to this one.
     */
    protected List subs;
    /**
     * The parent SymbolPart to this one.
     */
    protected SymbolPart parent;
    /**
     * Some positions need to shift for entries that don't follow the
     * conventions on the specification.
     */
    protected int positionShift = 0;

    protected static boolean DEBUG = false;

    public final static char UNUSED = '-';
    public final static char WILD = '*';

    protected SymbolPart() {
        DEBUG = Debug.debugging("symbolpart");
    }

    /**
     * The most-used constructor, used by CodePosition objects to
     * create the different levels of the SymbolPart tree. The
     * SymbolPart uses the parameters and the Properties to define its
     * name and get the cgm file holding the geometry for the symbol.
     * This constructor focuses on the Scheme, Dimension and
     * FunctionID symbol parts.
     * 
     * @param codePosition CodePosition object that corresponds to the
     *        SymbolPart. CodePosition object with lower position
     *        numbers tend to define more general symbols.
     * @param symbolCode the 15 character symbol string that defines
     *        this SymbolPart. This string is associated with a
     *        hierarchy number in the Properties.
     * @param props the Properties object contains all the information
     *        about the symbol tree.
     * @param parent the SymbolPart that is above this one in the
     *        SymbolPart tree.
     */
    public SymbolPart(CodePosition codePosition, String symbolCode,
            Properties props, SymbolPart parent) {
        this(codePosition,
             symbolCode,
             props,
             parent,
             codePosition.getStartIndex(),
             codePosition.getEndIndex(),
             true);
    }

    /**
     * A different constructor used by OptionPositions. The SymbolPart
     * uses the parameters and the Properties to define its name and
     * get the cgm file holding the geometry for the symbol. This
     * constructor focuses on the Scheme, Dimension and FunctionID
     * symbol parts.
     * 
     * @param codePosition CodePosition object that corresponds to the
     *        SymbolPart. CodePosition object with lower position
     *        numbers tend to define more general symbols.
     * @param symbolCode the 15 character symbol string that defines
     *        this SymbolPart. This string is associated with a
     *        hierarchy number in the Properties.
     * @param props the Properties object contains all the information
     *        about the symbol tree.
     * @param parent the SymbolPart that is above this one in the
     *        SymbolPart tree.
     */
    public SymbolPart(CodePosition codePosition, String symbolCode,
            Properties props, SymbolPart parent, int start, int end,
            boolean shiftIfNecessary) {

        this.code = symbolCode.substring(start, end);
        this.codePosition = codePosition;

        // For OptionPositions, we need to have a version where the
        // start and end aren't used for parsing, because the
        // properties are especially designed for them. We just need
        // the indexes for placement into the symbol code later. The
        // new code just needs to read symbolCode from the beginning
        // for the length between the indexes.

        boolean debug = DEBUG;

        // This corrects the situation where the symbol code is
        // shorter in the specification than it would seem
        // appropriate for its place in the hierarchy.
        while (code.charAt(0) == UNUSED && start > 1 && shiftIfNecessary) {
            code = symbolCode.substring(--start, end);
            this.positionShift--;
        }

        this.prettyName = props.getProperty(symbolCode + "." + NameProperty);
        this.cgmName = props.getProperty(symbolCode + "." + CGMProperty);
        this.parent = parent;

        String sc = getSymbolCode();

        if (Debug.debugging("errors") && !symbolCode.equals(sc)) {
            debug = true;
        }

        if (debug) {
            Debug.output("SymbolPart(" + codePosition.getPrettyName()
                    + "): read " + start + " of [" + symbolCode + "] as [" + sc
                    + "] : " + this.prettyName + " (" + code + ")");
        }
    }

    /**
     * Sets the part of the SymbolCode that is unique to this
     * SymbolPart.
     */
    public void setCode(String c) {
        code = c;
    }

    /**
     * Gets the part of the SymbolCode that is unique to this
     * SymbolPart.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the descriptive name if this SymbolPart.
     */
    public void setPrettyName(String pn) {
        prettyName = pn;
    }

    /**
     * Sets the descriptive name if this SymbolPart.
     */
    public String getPrettyName() {
        return prettyName;
    }

    /**
     * Sets the SymbolPart's parent in the SymbolPart tree.
     */
    public void setParent(SymbolPart par) {
        parent = par;
    }

    /**
     * Retrieves the SymbolPart's parent in the SymbolPart tree.
     */
    public SymbolPart getParent() {
        return parent;
    }

    /**
     * Sets a list of SymbolPart tree for more specific
     * representations of what this SymbolPart represents.
     */
    public void setSubs(List set) {
        subs = set;
    }

    /**
     * Gets a list of SymbolPart tree for more specific
     * representations of what this SymbolPart represents.
     */
    public List getSubs() {
        return subs;
    }

    /**
     * Get a simple string representation of this SymbolPart,
     * including the 15 digit code and the pretty name.
     */
    public String toString() {
        // return " [" + getSymbolCode() + "] " + prettyName;
        return prettyName;
    }

    /**
     * A method used by the tree to provide a string representation of
     * how all the SymbolParts are connected.
     */
    public String getDescription(int level) {
        StringBuffer sb = new StringBuffer();
        String indicator = "|--> ";
        if (level > 0) {
            sb.append(indicator);
        }

        List subs = getSubs();
        int subSize = 0;
        if (subs != null) {
            subSize = subs.size();
        }

        sb.append(toString());
        if (subSize > 0) {
            sb.append(" with ").append(subSize).append(" subcategor"
                   ).append((subSize == 1 ? "y\n" : "ies\n"));
        } else {
            sb.append("\n");
        }

        if (subs != null) {
            synchronized (this) {
                StringBuffer sb1 = new StringBuffer();

                for (int i = 0; i < level; i++) {
                    sb1.append("     ");
                }

                String spacer = sb1.toString();

                for (Iterator it = subs.iterator(); it.hasNext();) {
                    sb.append(spacer)
                            .append(((SymbolPart) it.next()).getDescription(level + 1));
                }
            }
        }

        return sb.toString();
    }

    /**
     * The starting command for retrieving the description with this
     * SymbolPart being the top of the tree.
     */
    public String getDescription() {
        return getDescription(0);
    }

    /**
     * Retrieves the 15 character symbol code for this SymbolPart.
     * Calling this method will cause the SymbolPart to ask all of
     * it's parents to contribute their part of the code as well.
     */
    public String getSymbolCode() {
        return getSymbolCode(null).toString();
    }

    /**
     * A 15 character string of spaces, where spaces won't overwrite
     * the current character when this symbol writes to a
     * getSymbolCode() string.
     */
    public StringBuffer getSymbolCodeMask() {
        return new StringBuffer(DEFAULT_SYMBOL_CODE);
    }

    /**
     * A SymbolPart tree method that gets the SymbolPart's parents
     * contribution for the symbol code.
     */
    protected StringBuffer getSymbolCode(StringBuffer symbolCode) {

        if (codePosition instanceof CodeScheme) {
            symbolCode = ((CodeScheme) codePosition).getDefaultSymbolCode();
        } else if (parent != null) {
            symbolCode = parent.getSymbolCode(symbolCode);
        } else {
            symbolCode = getSymbolCodeMask();
        }

        if (codePosition != null) {
            int key = codePosition.getStartIndex() + positionShift;
            symbolCode = symbolCode.replace(key,
                    codePosition.getEndIndex(),
                    code);
        }

        return symbolCode;
    }

    public CodePosition getCodePosition() {
        return codePosition;
    }

    public CodeOptions getCodeOptions() {
        CodeScheme cs = getCodeScheme();
        if (cs != null) {
            return cs.getCodeOptions(this);
        } else {
            return null;
        }
    }

    public CodeScheme getCodeScheme() {
        CodeScheme cs = null;
        if (codePosition instanceof CodeScheme) {
            cs = (CodeScheme) codePosition;
        } else if (parent != null) {
            cs = parent.getCodeScheme();
        }
        return cs;
    }

    /**
     * A query method that answers if the given 15 digit code applies
     * to this symbol part.
     * 
     * @param queryCode
     * @return true if the code applies to this position.
     */
    public boolean codeMatches(String queryCode) {
        if (codePosition != null && code != null) {
            int startIndex = codePosition.startIndex;
            int length = code.indexOf('-');
            if (length == -1) {
                length = code.length();
            }

            if (Debug.debugging("symbology.detail")) {
                Debug.output("Checking " + queryCode + " against |" + code
                        + "| starting at " + startIndex + " for " + length);
            }
            return queryCode.regionMatches(true, startIndex, code, 0, length);
        }

        // Nothing set, must be the head and we want a match.
        return true;
    }

    public void paintIcon(Graphics2D g, CodeOptions co, Dimension di) {

    }

}