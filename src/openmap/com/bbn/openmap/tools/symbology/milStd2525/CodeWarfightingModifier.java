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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeWarfightingModifier.java,v $
// $RCSfile: CodeWarfightingModifier.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/16 01:08:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Properties;

import com.bbn.openmap.util.PropUtils;

public class CodeWarfightingModifier extends OptionPosition {

    public CodeWarfightingModifier() {
	super("Modifier", 11, 12);
    }

    /**
     * Starts looking for property 1, creates SymbolParts until the
     * numbers run out.  If there are limits to what properties should
     * be read, this method should be overriden.
     */
    public void parsePositions(String prefix, Properties props) {
	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	for (int spCount = 1; spCount > 0 && spCount < 128; spCount++) {
	    String entry = props.getProperty(prefix + Integer.toString(spCount));

	    if (entry != null) {
		addSymbolPart(prefix + entry, prefix, props);
	    } else {
		spCount = -1;
	    }
	}
    }

}
