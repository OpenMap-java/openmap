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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeBattleDimension.java,v $
// $RCSfile: CodeBattleDimension.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

public class CodeBattleDimension extends CodePosition {

    public final static CodeBattleDimension SPACE = 
	new CodeBattleDimension(1, 'P', "SPACE");
    public final static CodeBattleDimension AIR = 
	new CodeBattleDimension(2, 'A', "AIR");
    public final static CodeBattleDimension GROUND = 
	new CodeBattleDimension(3, 'G', "GROUND");
    public final static CodeBattleDimension SEA_SURFACE = 
	new CodeBattleDimension(4, 'S', "SEA SURFACE");
    public final static CodeBattleDimension SEA_SUBSURFACE = 
	new CodeBattleDimension(5, 'U', "SEA SUBSURFACE");
    public final static CodeBattleDimension SOF = 
	new CodeBattleDimension(6, 'F', "SOF");
    public final static CodeBattleDimension OTHER = 
	new CodeBattleDimension(0, 'X', "OTHER (No frame)");

    protected CodeBattleDimension(int heirarchyLevelNumber, char idChar, String name) {
	super(heirarchyLevelNumber, idChar, name, 3, 3, null);
    }

    public static List getList() {
	List list = (List)positions.get(CodeBattleDimension.class);
	if (list == null) {
	    list = new ArrayList();
	    list.add(SPACE);
	    list.add(AIR);
	    list.add(GROUND);
	    list.add(SEA_SURFACE);
	    list.add(SEA_SUBSURFACE);
	    list.add(SOF);
	    list.add(OTHER);
	    positions.put(CodeMETOCCategory.class, list);
	}
	return list;
    }

}
