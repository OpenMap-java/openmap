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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeAffiliation.java,v $
// $RCSfile: CodeAffiliation.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/11 08:31:52 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

public class CodeAffiliation extends CodePosition {

    public final static CodeAffiliation PENDING = new CodeAffiliation('P', "PENDING");
    public final static CodeAffiliation UNKNOWN = new CodeAffiliation('U', "UNKNOWN");
    public final static CodeAffiliation ASSUMED_FRIEND = new CodeAffiliation('A', "ASSUMED FRIEND");
    public final static CodeAffiliation FRIEND = new CodeAffiliation('F', "FRIEND");
    public final static CodeAffiliation NEUTRAL = new CodeAffiliation('N', "NEUTRAL");
    public final static CodeAffiliation SUSPECT = new CodeAffiliation('S', "SUSPECT");
    public final static CodeAffiliation HOSTILE = new CodeAffiliation('H', "HOSTILE");
    public final static CodeAffiliation JOKER = new CodeAffiliation('J', "JOKER");
    public final static CodeAffiliation FAKER = new CodeAffiliation('K', "FAKER");
    public final static CodeAffiliation NONE_SPECIFIED = new CodeAffiliation('O', "NONE SPECIFIED");

    protected CodeAffiliation(char idChar, String name) {
	super(0, idChar, name, 2, 2, null);
    }

    public static List getList() {
	List list = (List)positions.get(CodeAffiliation.class);
	if (list == null) {
	    list = new ArrayList();
	    list.add(PENDING);
	    list.add(UNKNOWN);
	    list.add(ASSUMED_FRIEND);
	    list.add(FRIEND);
	    list.add(NEUTRAL);
	    list.add(SUSPECT);
	    list.add(HOSTILE);
	    list.add(JOKER);
	    list.add(FAKER);
	    list.add(NONE_SPECIFIED);
	    positions.put(CodeAffiliation.class, list);
	}
	return list;
    }

}
