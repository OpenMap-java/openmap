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
// $Revision: 1.2 $
// $Date: 2003/12/17 00:23:49 $
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
	CodeAffiliation ca = new CodeAffiliation();
	ca.parsePositions("affiliation", positionProperties);

	CodeWarfightingModifier cwm = new CodeWarfightingModifier();
	CodeSizeModifier csm = new CodeSizeModifier();
	CodeMOOTWModifier cmm = new CodeMOOTWModifier();
	cwm.parsePositions("modifiers", positionProperties);
	csm.parsePositions("modifiers", positionProperties);
	cmm.parsePositions("modifiers", positionProperties);

	CodeStatus cstatus = new CodeStatus();
	cstatus.parsePositions("status", positionProperties);

	CodeOrderOfBattle coob = new CodeOrderOfBattle();
	coob.parsePositions("oob", positionProperties);

	List warfightingOptions = new LinkedList();
	warfightingOptions.add(ca);
	warfightingOptions.add(cstatus);
	warfightingOptions.add(cwm);
// 	warfightingOptions.add(cc); // CodeCountry
	warfightingOptions.add(coob);
	// Kind of a hack, I know the number is the heirarchy number
	// of the particular scheme, and that the position.properties
	// file is setting up the options to reflect that for all of
	// these code option settings.
	((CodeScheme)cs.getFromChoices(1)).setCodeOptions(new CodeOptions(warfightingOptions));

	List tacOptions = new LinkedList();
	tacOptions.add(ca);
	tacOptions.add(csm);
// 	tacOptions.add(cc); // CodeCountry
	((CodeScheme)cs.getFromChoices(2)).setCodeOptions(new CodeOptions(tacOptions));

	List intelOptions = new LinkedList();
	intelOptions.add(ca);
	intelOptions.add(cstatus);
// 	intelOptions.add(cc); // CodeCountry
	intelOptions.add(coob);
	((CodeScheme)cs.getFromChoices(4)).setCodeOptions(new CodeOptions(intelOptions));

	List mootwOptions = new LinkedList();
	mootwOptions.add(ca);
	mootwOptions.add(cstatus);
	mootwOptions.add(cmm);
// 	mootwOptions.add(cc); // CodeCountry
	mootwOptions.add(coob);
	((CodeScheme)cs.getFromChoices(5)).setCodeOptions(new CodeOptions(mootwOptions));
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
