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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/Attic/OptionPosition.java,v $
// $RCSfile: OptionPosition.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/16 01:08:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class OptionPosition extends CodePosition {

    /**
     * A list of CodePosition choices for this position.
     */
    protected List options = new ArrayList();

    public OptionPosition(String name, int start, int end)  {
	super(name, start, end);
    }

    /**
     * Starts looking for property 1, creates SymbolParts until the
     * numbers run out.  If there are limits to what properties should
     * be read, this method should be overriden.
     */
    protected void parsePositions(String prefix, Properties props) {
	prefix = PropUtils.getScopedPropertyPrefix(prefix);
	for (int spCount = 1; spCount > 0; spCount++) {
	    String entry = props.getProperty(prefix + Integer.toString(spCount));
	    if (entry != null) {
		addSymbolPart(prefix + entry, prefix, props);
	    } else {
		spCount = -1;
	    }
	}
    }

    protected void addSymbolPart(String entry, String prefix, Properties props) {
	int offset = prefix.length();
	options.add(new SymbolPart(this, entry, props, null, offset, offset + endIndex - startIndex, false));
    }

    /**
     * Get the location of this position, and look through the list of
     * SymbolParts to see which has matching codes in the position
     * represented by the OptionPosition, and return that SymbolPart.
     * May return null if there are no known parts or if there is no
     * match for the string.
     *
     * @param code a 15 character symbol code to check to see if this
     * list contains any applicable parts.
     */
    public SymbolPart getSymbolPart(String code) {
	try {
	    String relevant = code.substring(getStartIndex(), getEndIndex());

	    for (Iterator it = options.iterator(); it.hasNext();) {

		SymbolPart sp = (SymbolPart)it.next();

		if (relevant.equals(sp.getCode())) {
		    if (DEBUG) {
			Debug.output("OptionPosition found [" + sp.getPrettyName() + "] for " + code);
		    }
		    return sp;
		}
	    }
	} catch (ArrayIndexOutOfBoundsException aioobe) {

	}
	return null;
    }

    /**
     * Retrieve the list of SymbolPart options for this position.
     */
    public List getOptions() {
	return options;
    }

    /**
     * Set the list of SymbolPart options for this position.
     */
    public void setOptions(List list) {
	options = list;
    }

}
