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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeCategory.java,v $
// $RCSfile: CodeCategory.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

public class CodeCategory extends CodePosition {

    public final static CodeCategory TASKS = 
	new CodeCategory(1, 'T', "TASKS");
    public final static CodeCategory C2_GENERAL_MANEUVER = 
	new CodeCategory(2, 'G', "C2 & GENERAL MANEUVER");
    public final static CodeCategory MOBILITY_SURVIVABILITY = 
	new CodeCategory(3, 'M', "MOBILITY/SURVIVABILITY");
    public final static CodeCategory FIRE_SUPPORT = 
	new CodeCategory(4, 'F', "FIRE SUPPORT");
    public final static CodeCategory COMBAT_SERVICE_SUPPORT = 
	new CodeCategory(5, 'S', "COMBAT SERVICE SUPPORT");
    public final static CodeCategory OTHER = 
	new CodeCategory(6, 'O', "OTHER");

    protected CodeCategory(int heirarchyLevelNumber, char idChar, String name) {
	super(heirarchyLevelNumber, idChar, name, 3, 3, null);
    }

    public static List createList() {
	List list = new ArrayList();
	list.add(TASKS);
	list.add(C2_GENERAL_MANEUVER);
	list.add(MOBILITY_SURVIVABILITY);
	list.add(FIRE_SUPPORT);
	list.add(COMBAT_SERVICE_SUPPORT);
	list.add(OTHER);
	return list;
    }

}
