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
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

public class CodeScheme extends CodePosition {

    protected String defaultSymbolCode;

    public final static CodeScheme WARFIGHTING = 
	new CodeScheme(1, 'S', "WARFIGHTING", 
		       CodeBattleDimension.class, 
		       "****------*****");

    public final static CodeScheme TACTICAL_GRAPHICS = 
	new CodeScheme(2, 'G', "TACTICAL GRAPHICS", 
		       CodeBattleDimension.class,
		       "****------****X");

    public final static CodeScheme METOC = 
	new CodeScheme.METOC(3, 'W', "METOC", 
			     CodeMETOCCategory.class, 
			    "---------------");

    public final static CodeScheme INTELLIGENCE = 
	new CodeScheme(4, 'I', "INTELLIGENCE", 
		       CodeBattleDimension.class, 
		       "****--------***");

    public final static CodeScheme MOOTW = 
	new CodeScheme(5, 'O', "Military Operations Other Than War (MOOTW)", 
		       CodeMOOTWCategory.class, 
		       "****------*****");
    public final static CodeScheme MAPPING = 
	new CodeScheme(6, 'M', "Mapping (reserved - under development)", 
		       null, "               ");

    protected CodeScheme(int heirarchyLevelNumber, 
			 char heirarchyLevelChar, String name,
			 Class nextCodePosition,
			 String defaultSymbolCode) {
	super(heirarchyLevelNumber, heirarchyLevelChar, name, 1, 1, nextCodePosition);
	this.defaultSymbolCode = defaultSymbolCode;
    }

    public static List getList() {
	List list = (List)positions.get(CodeScheme.class);
	if (list == null) {
	    list = new ArrayList();
	    list.add(WARFIGHTING);
	    list.add(TACTICAL_GRAPHICS);
	    list.add(METOC);
	    list.add(INTELLIGENCE);
	    list.add(MOOTW);
	    positions.put(CodeScheme.class, list);
	}
	return list;
    }

    protected SymbolPart parse(Properties props, SymbolPart parent) {
	String hCode = getHeirarchyNumber() + ".X";
	String entry = props.getProperty(hCode);
	SymbolPart sp = null;

	if (entry != null) {
	    sp = new SymbolPart(this, entry, props, parent);
	    parse(hCode, props, sp);
	}

	return sp;
    }

    protected void parse(String hCode, Properties props, SymbolPart parent) {
	
	List codePositionList = getNextPositionList();

	if (codePositionList == null || codePositionList.size() == 0) {
	    Debug.output(prettyName + ".parse(): codePositionList.size = 0");
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

		cp.parse(newHCode, props, sp);
	    
	    } else {
		if (DEBUG) {
		    Debug.output("CodeScheme.parse: no entry found for " + newHCode);
		}
	    }
	}
    }

    /**
     */
    protected List getNextPositionList() {

	List nextLevelList = null;

	if (nextPosition != null) {

	    if (DEBUG) {
		Debug.output(getClass().getName() + " looking to " + 
			     nextPosition.getName());
	    }

	    try {
		Method npm = nextPosition.getMethod("getList", null);
		nextLevelList = (List)npm.invoke(nextPosition, null);
	    } catch (NoSuchMethodException nsme) {
	    } catch (SecurityException se) {
	    } catch (IllegalAccessException iae) {
	    } catch (IllegalArgumentException iae2) {
	    } catch (InvocationTargetException ite) {
	    }
	}

	return nextLevelList;
    }

    public StringBuffer getDefaultSymbolCode() {
	return new StringBuffer(defaultSymbolCode);
    }

    public static class METOC extends CodeScheme {

	public METOC(int heirarchyLevelNumber, 
		     char heirarchyLevelChar, String name,
		     Class nextCodePosition,
		     String defaultSymbolCode) {
	    super(heirarchyLevelNumber, heirarchyLevelChar, name,
		  nextCodePosition, defaultSymbolCode);
	}

	protected SymbolPart parse(Properties props, SymbolPart parent) {

	    String hCode = Integer.toString(getHeirarchyNumber());
	    String entry = props.getProperty(hCode);
	    SymbolPart sp = null;

	    if (entry != null) {
		sp = new SymbolPart(this, entry, props, parent);
		parse(hCode, props, sp);
	    }

	    return sp;
	}

    }

}
