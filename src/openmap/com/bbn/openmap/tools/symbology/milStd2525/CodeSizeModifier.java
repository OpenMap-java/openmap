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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeSizeModifier.java,v $
// $RCSfile: CodeSizeModifier.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/16 01:08:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Properties;
import com.bbn.openmap.util.PropUtils;

public class CodeSizeModifier extends OptionPosition {

    public CodeSizeModifier() {
	super("Echelon/Size", 11, 12);
    }

    /**
     * The CodeSizeModifier is more particular.  It wants the first 14
     * code in modifers.properties, and then the last one.
     */
    protected void parsePositions(String prefix, Properties props) {
	String entry = null;
	prefix = PropUtils.getScopedPropertyPrefix(prefix);
	for (int spCount = 1; spCount > 0 && spCount < 15; spCount++) {
	    entry = props.getProperty(prefix + Integer.toString(spCount));
	    if (entry != null) {
		addSymbolPart(prefix + entry, prefix, props);
	    } else {
		spCount = -1;
	    }
	}

	// HACK I know that 'K-' is at 128 in the modifiers.properties file.
	entry = props.getProperty(prefix + "128");
	if (entry != null) {
	    addSymbolPart(prefix + entry, prefix, props);
	}
    }

}
