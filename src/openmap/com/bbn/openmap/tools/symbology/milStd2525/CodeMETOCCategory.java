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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeMETOCCategory.java,v $
// $RCSfile: CodeMETOCCategory.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

public class CodeMETOCCategory extends CodePosition {

    public final static CodeMETOCCategory ATMOSPHERIC = 
	new CodeMETOCCategory(1, 'A', "ATMOSPHERIC");
    public final static CodeMETOCCategory OCEANIC = 
	new CodeMETOCCategory(2, 'O', "OCEANIC");
    public final static CodeMETOCCategory SPACE = 
	new CodeMETOCCategory(3, 'S', "SPACE");

    protected CodeMETOCCategory(int heirarchyLevelNumber, char idChar, String name) {
	super(heirarchyLevelNumber, idChar, name, 2, 2, CodeFunctionID.class);
    }

    public static List getList() {
	List list = (List)positions.get(CodeMETOCCategory.class.getName());
	if (list == null) {
	    list = new ArrayList();
	    list.add(ATMOSPHERIC);
	    list.add(OCEANIC);
	    list.add(SPACE);
	    positions.put(CodeMETOCCategory.class.getName(), list);
	}

	return list;
    }

}
