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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeStatus.java,v $
// $RCSfile: CodeStatus.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/08 18:37:51 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

public class CodeStatus extends CodePosition {

    protected final static int position = 4;
    protected final static int length = 1;

    public final static CodeStatus ANTICIPATED_PLANNED = 
	new CodeStatus('A', "ANTICIPATED/PlANNED");
    public final static CodeStatus PRESENT = 
	new CodeStatus('P', "PRESENT");

    protected CodeStatus(char idChar, String name) {
	super(0, idChar, name);
    }

    public static List getList() {
	List list = (List)positions.get(CodeStatus.class);
	if (list == null) {
	    list = new ArrayList();
	    list.add(ANTICIPATED_PLANNED);
	    list.add(PRESENT);
	    positions.put(CodeStatus.class, list);
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
	return null;
    }
}
