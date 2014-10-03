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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeFunctionID.java,v $
// $RCSfile: CodeFunctionID.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

/**
 * The CodeFunctionID CodePosition represents a single level of the
 * part of the tree that starts to really scope in on a symbol's
 * purpose. CodeFunctionIDs are used to represent the 5-10 characters
 * of a symbol code. The CodePositions are linked together in a tree
 * format when the hierarchy tree is read and interpreted, with like
 * features grouped together under a common SymbolPart by a
 * CodeFunctionID.
 */
public class CodeFunctionID extends CodePosition {

    public CodeFunctionID() {
        // Code function IDs are not kept around, they are just used
        // to create the SymbolPart Structure. The first three
        // arguments in the super call are meaningless.
        this(5);
    }

    public CodeFunctionID(int pos) {
        // Code function IDs are not kept around, they are just used
        // to create the SymbolPart Structure. The first three
        // arguments in the super call are meaningless.
        super("Function ID", pos, 10);
    }

    public void parseHierarchy(String hCode, Properties props, SymbolPart parent) {

        List parentList = null;
        int subLevelNumber = 1;

        int pos = getStartIndex() + parent.positionShift;
        if (pos < 4)
            pos = 4;

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
                    Debug.output("CodeFunctionID.parse: reading " + hCode2
                            + " as " + entry);
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
                    Debug.output("CodeFunctionID.parse: adding "
                            + sp.getPrettyName() + " to "
                            + parent.getPrettyName());
                }

                parentList.add(sp);

                if (DEBUG) {
                    Debug.output("CodePosition.parse: looking for children of "
                            + sp.getPrettyName());
                }

                cp.parseHierarchy(hCode2, props, sp);
                subLevelNumber++;

            } else {
                subLevelNumber = -1;
            }
        }
    }

}