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
// $Revision: 1.4 $
// $Date: 2003/12/17 00:23:49 $
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

    /**
     * The base 15 character code for a symbol under a scheme.  This
     * code has wild-cards and unused charaters in them where
     * appropriate for the scheme.
     */
    protected String defaultSymbolCode = SymbolPart.DEFAULT_SYMBOL_CODE;
    /**
     * For parsing the heirarchy, most schemes have some characters
     * added to their heirarchy index number.  This can be specified
     * in the position properties.
     */
    protected String heirarchyAddition;

    /**
     * Property keyword for the default symbol code 'defaultSymbolCode'.
     */
    public final static String DefaultSymbolCodeProperty = "defaultSymbolCode";

    /**
     * Property keyword for the heirarchy addition string 'heirarchyCodeAddition'.
     */
    public final static String HeirarchyCodeAdditionProperty = "heirarchyCodeAddition";

    public CodeScheme() {
	super("Scheme", 1, 1);
    }

    /**
     * The method needs more information from the properties than the
     * CodePosition version of this method provides, like getting the
     * base symbol code for the scheme and the heirarchy addition.
     */
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

    protected CodeOptions options;

    public void setCodeOptions(CodeOptions co) {
	options = co;
    }

    public CodeOptions getCodeOptions(SymbolPart sp) {
	// Check with the symbol part first to see of there are any
	// options for the particular positions established and
	// limiting for the particular symbol, and then subsitute
	// defaults for any other positions.
	

	return options;


    }
}
