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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolPart.java,v $
// $RCSfile: SymbolPart.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/08 18:37:51 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class SymbolPart {

    public final static String NameProperty = "name";
    public final static String IconFileProperty = "iconFile";

    protected CodePosition codePosition;
    protected char code;
    protected String prettyName;
//     protected CGM symbolPart;
    protected List subs;
    protected SymbolPart parent;

    protected static boolean DEBUG = false;

    /**
     * A special constructor for the head.
     */
    public SymbolPart(Properties props) {
	this.prettyName = "MIL-STD-2525B Symbology";

	DEBUG = Debug.debugging("symbolpart");
	if (DEBUG) {
	    Debug.output("SymbolPart head");
	}

	String schemeCode;
	SymbolPart symbolSet = null;
	subs = new Vector();

	List schemes = CodeScheme.getList();
	if (DEBUG) {
	    Debug.output("SymbolPart head: loading schemes");
	}
	
	for (Iterator it = schemes.iterator(); it.hasNext();) {
	    CodeScheme cs = (CodeScheme)it.next();

	    if (DEBUG) {
		Debug.output("SymbolPart head: loading " + cs.getPrettyName());
	    }

	    symbolSet = cs.parse(props, this);
	    if (symbolSet != null) {
		subs.add(symbolSet);
	    }
	}
    }

    public SymbolPart(CodePosition codePosition, String symbolCode,
		      Properties props, SymbolPart parent) {
	int pos = codePosition.getPosition();
 	this.code = symbolCode.charAt(pos - 1); // off by 1
	this.prettyName = props.getProperty(symbolCode + "." + NameProperty);
	this.parent = parent;
	this.codePosition = codePosition;

	if (DEBUG) {
	    Debug.output("SymbolPart(): read " + pos +
			 " of [" + symbolCode + 
			 "] as [" + getSymbolCode() +
			 "] : " + this.prettyName);
	}
    }

    public void setCode(char c) {
	code = c;
    }

    public char getCode() {
	return code;
    }
   
    public void setPrettyName(String pn) {
	prettyName = pn;
    }

    public String getPrettyName() {
	return prettyName;
    }

    public void setParent(SymbolPart par) {
	parent = par;
    }

    public SymbolPart getParent() {
	return parent;
    }

    public void setSubs(List set) {
	subs = set;
    }

    public List getSubs() {
	return subs;
    }

    public String toString() {
	return " [" + getSymbolCode() + "] " + prettyName;
    }

    /**
     */
    public String getDescription(int level) {
	StringBuffer sb = new StringBuffer();
	String indicator = "|--> ";
	if (level > 0) {
	    sb.append(indicator);
	}

	List subs = getSubs();
	int subSize = 0;
	if (subs != null) {
	    subSize = subs.size();
	}

	sb.append(toString());
	if (subSize > 0) {
	    sb.append (" with " + subSize + " subcategor" + 
		       (subSize == 1?"y\n":"ies\n"));
	} else {
	    sb.append("\n");
	}

	if (subs != null) {
	    synchronized (this) {
		StringBuffer sb1 = new StringBuffer();

		for (int i = 0; i < level; i++) {
		    sb1.append("     ");
		}

		String spacer = sb1.toString();
	
		for (Iterator it = subs.iterator();it.hasNext();) {
		    sb.append(spacer + ((SymbolPart)it.next()).getDescription(level+1));
		}
	    }
	}

	return sb.toString();
    }
    
    /**
     */
    public String getDescription() {
	return getDescription(0);
    }

    public String getSymbolCode() {
	return getSymbolCode(null).toString();
    }

    protected StringBuffer getSymbolCode(StringBuffer symbolCode) {

	if (codePosition instanceof CodeScheme) {
	    symbolCode = ((CodeScheme)codePosition).getDefaultSymbolCode();
	} else if (parent != null) {
	    symbolCode = parent.getSymbolCode(symbolCode);
	} else {
	    Debug.output(prettyName + ": No parent and no scheme to use for setting up default symbol code");
	    symbolCode = new StringBuffer("---------------");
	}

	if (codePosition != null) {
	    symbolCode.setCharAt(codePosition.getPosition() - 1, code);
	}

	return symbolCode;
    }
}
