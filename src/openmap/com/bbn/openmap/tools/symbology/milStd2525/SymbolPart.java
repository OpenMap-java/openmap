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
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.dataAccess.cgm.CGM;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class SymbolPart {

    /** Property file property for pretty name 'name' */
    public final static String NameProperty = "name";
    /** Property file property for cgm file too represent the symbol. */
    public final static String CGMProperty = "cgm";

    /**
     * The Object that describes the location of this symbol part in
     * the symbol heirarchy as defined by the 15 digit symbol code.
     */
    protected CodePosition codePosition;
    /**
     * The part of the symbol code unique to this symbol part.
     * Parents and children will make up the other parts of the code.
     */
    protected String code;
    /**
     * The pretty name for a symbol represented by this SymbolPart at
     * this point in the heirarchy.
     */
    protected String prettyName;
    /**
     * The file containing the symbol geometry for this SymbolPart.
     */
    protected String cgmName;
    /**
     * The symbol geometery object for this SymbolPart.
     */
    protected CGM cgm;
    /**
     * A list of children SymbolParts relative to this one.
     */
    protected List subs;
    /**
     * The parent SymbolPart to this one.
     */
    protected SymbolPart parent;
    /** 
     * Some positions need to shift for entries that don't follow the
     * conventions on the specification.
     */
    protected int positionShift = 0;

    protected static boolean DEBUG = false;

    public final char UNUSED = '-';
    public final char WILD = '*';
    
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
	int start = codePosition.getStartIndex();
	int end = codePosition.getEndIndex();
 	this.code = symbolCode.substring(start, end);
	this.codePosition = codePosition;

	boolean debug = DEBUG;

	// This corrects the situation where the symbol code is
	// shorter in the specification than it would seem
	// appropriate for its place in the heirarchy.
	while (code.charAt(0) == UNUSED && start > 1) {
	    code = symbolCode.substring(--start, end);
	    this.positionShift--;
	}

	this.prettyName = props.getProperty(symbolCode + "." + NameProperty);
	this.cgmName = props.getProperty(symbolCode + "." + CGMProperty);
	this.parent = parent;

	String sc = getSymbolCode();

	if (!symbolCode.equals(sc)) {
	    debug = true;
	}

	if (debug) {
	    Debug.output("SymbolPart(): read " + start +
			 " of [" + symbolCode + 
			 "] as [" + sc +
			 "] : " + this.prettyName + 
			 " (" + code + ")");
	}
    }

    public void setCode(String c) {
	code = c;
    }

    public String getCode() {
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
	    int key = codePosition.getStartIndex() + positionShift;
// 	    char sChar = symbolCode.charAt(key);

	    symbolCode = symbolCode.replace(key, codePosition.getEndIndex(), code);

// 	    symbolCode.setCharAt(key, code);
	}

	return symbolCode;
    }
}
