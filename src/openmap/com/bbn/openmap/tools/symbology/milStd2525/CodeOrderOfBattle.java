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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeOrderOfBattle.java,v $
// $RCSfile: CodeOrderOfBattle.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/08 18:37:51 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

public class CodeOrderOfBattle extends CodePosition {

    public final static int position = 15;
    public final static int length = 1;
    protected final static Class nextPosition = null;

    public final static CodeOrderOfBattle AIR_OB = 
	new CodeOrderOfBattle('A', "AIR OB");
    public final static CodeOrderOfBattle ELECTRONIC_OB = 
	new CodeOrderOfBattle('E', "ELECTRONIC OB");
    public final static CodeOrderOfBattle CIVILIAN_OB = 
	new CodeOrderOfBattle('C', "CIVILIAN OB");
    public final static CodeOrderOfBattle GROUND_OB = 
	new CodeOrderOfBattle('G', "GROUND OB");
    public final static CodeOrderOfBattle MARITIME_OB = 
	new CodeOrderOfBattle('N', "MARITIME OB");
    public final static CodeOrderOfBattle STRATEGIC_FORCE_RELATED = 
	new CodeOrderOfBattle('S', "STRATEGIC FORCE RELATED");
    public final static CodeOrderOfBattle CONTROL_MARKINGS = 
	new CodeOrderOfBattle('X', "CONTROL MARKINGS");

    protected CodeOrderOfBattle(char idChar, String name) {
	super(0, idChar, name);
    }

    public static List getList() {
	List list = (List)positions.get(CodeOrderOfBattle.class);
	if (list == null) {
	    list = new ArrayList();
	    list.add(AIR_OB);
	    list.add(ELECTRONIC_OB);
	    list.add(CIVILIAN_OB);
	    list.add(GROUND_OB);
	    list.add(MARITIME_OB);
	    list.add(STRATEGIC_FORCE_RELATED);
	    positions.put(CodeOrderOfBattle.class, list);
	}
	return list;
    }

    public int getPosition() {
	return position;
    }

    public int getLength() {
	return length;
    }

    public Class getNextPosition() {
	return nextPosition;
    }

}
