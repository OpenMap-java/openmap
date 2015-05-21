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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeScheme.java,v $
// $RCSfile: CodeScheme.java,v $
// $Revision: 1.9 $
// $Date: 2004/10/14 18:06:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The CodeScheme represents the options presented in the first
 * character of the 15 character symbol code. This character
 * represents the scheme, or symbology set, of the top-most branches
 * of the MIL-STD-2525B symbol tree. The layout and meaning of the 15
 * characters depend on the scheme, and the CodeScheme can figure out
 * some of them when it parses the position properties to see what
 * other CodePositions are fundamental for a particular instance of a
 * CodeScheme. There are other CodePositions that present choices for
 * a particular scheme type, and the CodeScheme needs to be told what
 * those options are. The CodePositionTree handles setting up the
 * CodeScheme with its optional CodePositions.
 */
public class CodeScheme extends CodePosition {

    /**
     * The base 15 character code for a symbol under a scheme. This
     * code has wild-cards and unused charaters in them where
     * appropriate for the scheme.
     */
    protected String defaultSymbolCode = SymbolPart.DEFAULT_SYMBOL_CODE;
    /**
     * For parsing the hierarchy, most schemes have some characters
     * added to their hierarchy index number. This can be specified in
     * the position properties.
     */
    protected String hierarchyAddition;

    /**
     * Property keyword for the default symbol code
     * 'defaultSymbolCode'.
     */
    public final static String DefaultSymbolCodeProperty = "defaultSymbolCode";

    /**
     * Property keyword for the hierarchy addition string
     * 'hierarchyCodeAddition'.
     */
    public final static String HierarchyCodeAdditionProperty = "hierarchyCodeAddition";

    public CodeScheme() {
        super("Scheme", 1, 1);
    }

    /**
     * The method needs more information from the properties than the
     * CodePosition version of this method provides, like getting the
     * base symbol code for the scheme and the hierarchy addition.
     */
    public CodePosition addPositionChoice(int index, String entry,
                                          String prefix, Properties props) {

        CodeScheme cs = (CodeScheme) super.addPositionChoice(index,
                entry,
                prefix,
                props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix) + entry + ".";

        String next = props.getProperty(prefix + NextProperty);

        if (next != null) {
            String nextClassName = props.getProperty(next + ".class");
            if (nextClassName != null) {
                CodePosition cp = (CodePosition) ComponentFactory.create(nextClassName);
                if (DEBUG) {
                    Debug.output("CodeScheme created next class(" + next
                            + "), " + nextClassName);
                }
                if (cp != null) {
                    cs.nextPosition = cp;
                    cp.parsePositions(next, props);
                }
            } else {
                if (DEBUG) {
                    Debug.output("CodeScheme couldn't create next class("
                            + next + "), " + nextClassName);
                }
            }
        }

        cs.defaultSymbolCode = props.getProperty(prefix
                + DefaultSymbolCodeProperty);
        cs.hierarchyAddition = props.getProperty(prefix
                + HierarchyCodeAdditionProperty, "");
        // Don't need to add to choices, already done in super class
        // method.
        return cs;
    }

    /**
     * Parse the hierarchy properties to create SymbolParts for those
     * parts under a particular scheme represented by this instance of
     * CodeScheme.
     * 
     * @param props the hierarchy properties.
     * @param parent the SymbolPart parent that the new SymbolPart
     *        tree falls under.
     */
    public SymbolPart parseHierarchy(Properties props, SymbolPart parent) {
        String hCode = getHierarchyNumber() + hierarchyAddition;
        String entry = props.getProperty(hCode);
        SymbolPart sp = null;

        if (entry != null) {
            sp = new SymbolPart(this, entry, props, parent);
            parseHierarchy(hCode, props, sp);
        }

        return sp;
    }

    /**
     * Parse the hierarchy properties to create SymbolParts for those
     * parts under a particular scheme represented by this instance of
     * CodeScheme.
     * 
     * @param hCode the hierarchy code of this scheme, used to grow
     *        the tree for subsequent generations.
     * @param props the hierarchy properties.
     * @param parent the SymbolPart parent that the new SymbolPart
     *        tree falls under.
     */
    public void parseHierarchy(String hCode, Properties props, SymbolPart parent) {

        List codePositionList = null;

        if (nextPosition != null) {
            codePositionList = nextPosition.getPositionChoices();
        }

        if (codePositionList == null || codePositionList.isEmpty()) {
            Debug.output(prettyName
                    + ".parseHierarchy(): codePositionList.size = 0");
            return;
        }

        List parentList = null;

        for (Iterator it = codePositionList.iterator(); it.hasNext();) {
            CodePosition cp = (CodePosition) it.next();
            String newHCode = hCode + "." + cp.getHierarchyNumber();
            if (DEBUG) {
                Debug.output("CodeScheme.parse: " + newHCode + " with "
                        + cp.getPrettyName());
            }

            String entry = props.getProperty(newHCode);
            if (entry != null) {
                SymbolPart sp = new SymbolPart(cp, entry, props, parent);

                if (parentList == null) {
                    parentList = parent.getSubs();
                    if (parentList == null) {
                        parentList = new ArrayList();
                        parent.setSubs(parentList);
                    }
                }

                if (DEBUG) {
                    Debug.output("CodeScheme.parse: adding "
                            + sp.getPrettyName() + " to "
                            + parent.getPrettyName());
                }

                parentList.add(sp);

                if (DEBUG) {
                    Debug.output("CodeScheme.parse: handling "
                            + cp.getPrettyName() + " children for "
                            + sp.getPrettyName());
                }

                cp.parseHierarchy(newHCode, props, sp);

            } else {
                if (DEBUG) {
                    Debug.output("CodeScheme.parse: no entry found for "
                            + newHCode);
                }
            }
        }
    }

    /**
     * Return the default 15 character symbol code for this instance
     * of a scheme. Pretty much all of the symbols below this node in
     * the SymbolPart tree will have the same base code, with their
     * parameters written on top of it.
     */
    public StringBuffer getDefaultSymbolCode() {
        return new StringBuffer(defaultSymbolCode);
    }

    /**
     * A set of CodePostitions that can be set with on this scheme.
     * It's different from the choices, which is a list of
     * instantiated CodePositions for a particular CodePosition. The
     * options are a set of CodePositions, containing choices. For
     * instance, for a warfighing code scheme, there would be code
     * positions for affiliation, status, order of battle and
     * modifiers. The metoc code scheme wouldn't have options. Each
     * CodePosition returned in the options can represent a setting
     * for the position (its choices will be null), or can represent a
     * suite of choices if there is a list of other CodePositions in
     * its choices parameter.
     */
    protected CodeOptions options;

    /**
     * Set the code options for this scheme.
     */
    public void setCodeOptions(CodeOptions co) {
        options = co;
    }

    /**
     * Get the code options set for this scheme.
     */
    public CodeOptions getCodeOptions() {
        return options;
    }

    /**
     * Get the code options for the scheme as it relates to the symbol
     * part. The symbol part may have some restrictions set on it by
     * having one of the option CodePositions set within it. If that's
     * the case, then the CodeOptions returned will have the
     * CodePosition object for that aspect of the symbol represented
     * by a CodePosition object without choices.
     */
    public CodeOptions getCodeOptions(SymbolPart sp) {
        // Check with the symbol part first to see of there are any
        // options for the particular positions established and
        // limiting for the particular symbol, and then substitute
        // defaults for any other positions.

        return options;
    }
}