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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeScheme.java,v $
// $RCSfile: CodeScheme.java,v $
// $Revision: 1.3 $
// $Date: 2003/12/16 01:08:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class CodeScheme extends CodePosition {

    protected String defaultSymbolCode;
    protected String heirarchyAddition;

    public final static String DefaultSymbolCodeProperty = "defaultSymbolCode";
    public final static String HeirarchyCodeAdditionProperty = "heirarchyCodeAddition";

    public CodeScheme() {
	super("Scheme", 1, 1);
    }

    public CodePosition addPositionChoice(int index, String entry,
					  String prefix, Properties props) {
	
	CodeScheme cs = (CodeScheme)super.addPositionChoice(index, entry, prefix, props);
	prefix = PropUtils.getScopedPropertyPrefix(prefix) + entry + ".";

	String next = props.getProperty(prefix + NextProperty);

	if (next != null) {
	    String nextClassName = props.getProperty(next + ".class");
	    if (nextClassName != null) {
		CodePosition cp = (CodePosition)ComponentFactory.create(nextClassName);
		if (DEBUG) {
		    Debug.output("CodeScheme created next class(" + 
				 next + "), " + nextClassName);
		}
		if (cp != null) {
		    cs.nextPosition = cp;
		    cp.parsePositions(next, props);
		}
	    } else {
		if (DEBUG) {
		    Debug.output("CodeScheme couldn't create next class(" + 
				 next + "), " + nextClassName);
		}
	    }
	}

	cs.defaultSymbolCode = props.getProperty(prefix + DefaultSymbolCodeProperty);
	cs.heirarchyAddition = props.getProperty(prefix + HeirarchyCodeAdditionProperty, "");
	// Don't need to add to choices, already done in super class method.
	return cs;
    }

    public SymbolPart parseHeirarchy(Properties props, SymbolPart parent) {
	String hCode = getHeirarchyNumber() + heirarchyAddition;
	String entry = props.getProperty(hCode);
	SymbolPart sp = null;

	if (entry != null) {
	    sp = new SymbolPart(this, entry, props, parent);
	    parseHeirarchy(hCode, props, sp);
	}

	return sp;
    }

    public void parseHeirarchy(String hCode, Properties props, SymbolPart parent) {
	
 	List codePositionList = null;

	if (nextPosition != null) {
	    codePositionList = nextPosition.getPositionChoices();
	}

	if (codePositionList == null || codePositionList.size() == 0) {
	    Debug.output(prettyName + ".parseHeirarchy(): codePositionList.size = 0");
	    return;
	}

	List parentList = null;

	for (Iterator it = codePositionList.iterator(); it.hasNext();) {
	    CodePosition cp = (CodePosition)it.next();
	    String newHCode = hCode + "." + cp.getHeirarchyNumber();
	    if (DEBUG) {
		Debug.output("CodeScheme.parse: " + 
			     newHCode + " with " + 
			     cp.getPrettyName());
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
		    Debug.output("CodeScheme.parse: adding " + sp.getPrettyName() + " to " + parent.getPrettyName());
		}

		parentList.add(sp);

		if (DEBUG) {
		    Debug.output("CodeScheme.parse: handling " + 
				 cp.getPrettyName() + 
				 " children for " + sp.getPrettyName());
		}

		cp.parseHeirarchy(newHCode, props, sp);
	    
	    } else {
		if (DEBUG) {
		    Debug.output("CodeScheme.parse: no entry found for " + newHCode);
		}
	    }
	}
    }

    public StringBuffer getDefaultSymbolCode() {
	return new StringBuffer(defaultSymbolCode);
    }

    public CodeOptions getCodeOptions(SymbolPart sp) {
	// Check with the symbol part first to see of there are any
	// options for the particular positions established and
	// limiting for the particular symbol, and then subsitute
	// defaults for any other positions.
	return null;
    }
}
