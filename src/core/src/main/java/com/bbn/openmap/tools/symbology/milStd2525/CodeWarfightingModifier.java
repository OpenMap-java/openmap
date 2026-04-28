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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeWarfightingModifier.java,v $
// $RCSfile: CodeWarfightingModifier.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Properties;

import com.bbn.openmap.util.PropUtils;

/**
 * The CodeWarfightingModifier CodePosition defines different
 * modifiers used for symbols in the warfighting scheme. It is used in
 * position 11 and 12 of the symbol code.
 */
public class CodeWarfightingModifier extends CodePosition {

    public CodeWarfightingModifier() {
        super("Modifier", 11, 12);
    }

    /**
     * Starts looking for property 1, creates SymbolParts until the
     * numbers run out. If there are limits to what properties should
     * be read, this method should be overriden.
     */
    public void parsePositions(String prefix, Properties props) {
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        for (int index = 1; index > 0 && index < 128; index++) {
            String entry = props.getProperty(prefix + Integer.toString(index));

            if (entry != null) {
                addPositionChoice(index, entry, prefix, props);
            } else {
                index = -1;
            }
        }
    }

}