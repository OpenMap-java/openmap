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
// $Revision: 1.2 $
// $Date: 2003/12/17 00:23:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Properties;
import com.bbn.openmap.util.PropUtils;

public class CodeSizeModifier extends CodePosition {

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
	for (int index = 1; index > 0 && index < 15; index++) {
	    entry = props.getProperty(prefix + Integer.toString(index));
	    if (entry != null) {
		addPositionChoice(index, entry, prefix, props);
	    } else {
		index = -1;
	    }
	}

	// HACK I know that 'K-' is at 128 in the modifiers.properties file.
	entry = props.getProperty(prefix + "128");
	if (entry != null) {
	    addPositionChoice(128, entry, prefix, props);
	}
    }

}
