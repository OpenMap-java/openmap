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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeMOOTWModifier.java,v $
// $RCSfile: CodeMOOTWModifier.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Properties;

import com.bbn.openmap.util.PropUtils;

/**
 * A CodeMOOTWCategory is similar to the CodeModifier CodeCategory,
 * but it applies to the MOOTW symbol set instead. This CodePosition
 * notes the 11 and 12 characters in MOOTW symbol codes.
 */
public class CodeMOOTWModifier extends CodePosition {

    public CodeMOOTWModifier() {
        super("MOOTW Modifiers", 11, 12);
    }

    /**
     * Starts looking for property 1, creates SymbolParts until the
     * numbers run out. If there are limits to what properties should
     * be read, this method should be overriden.
     */
    public void parsePositions(String prefix, Properties props) {
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        for (int index = 1; index > 0 && index < 126; index++) {
            String entry = props.getProperty(prefix + Integer.toString(index));

            if (entry != null) {
                addPositionChoice(index, entry, prefix, props);
            } else {
                index = -1;
            }
        }
    }
}