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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodePositionTree.java,v $
// $RCSfile: CodePositionTree.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/16 01:08:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

public class CodePositionTree extends CodeScheme {

    public CodePositionTree(Properties positionProperties) {
	// Read CodeSchemes, build position tree.  Then, this
	// CodePosition can be used to build the heirarchal symbol
	// tree from the head SymbolPart
	CodeScheme cs = new CodeScheme();
	cs.parsePositions("scheme", positionProperties);
	choices = cs.getPositionChoices();

	// Read Optional Flag Positions
    }

    public SymbolPart parseHeirarchy(String name, Properties heirarchyProperties) {
	List positions = getPositionChoices();

	SymbolPartTree head = new SymbolPartTree(name);
	List subs = new LinkedList();
	head.setSubs(subs);

	for (Iterator it = positions.iterator(); it.hasNext();) {
	    CodeScheme cs = (CodeScheme)it.next();

	    if (Debug.debugging("symbolpart")) {
		Debug.output("CodePositionTree: loading " + cs.getPrettyName());
	    }

	    SymbolPart sp = cs.parseHeirarchy(heirarchyProperties, head);
	    if (sp != null) {
		subs.add(sp);
	    }
	}

	return head;
    }
}
