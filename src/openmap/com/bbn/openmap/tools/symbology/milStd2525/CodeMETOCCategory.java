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
// $Revision: 1.4 $
// $Date: 2003/12/18 19:11:11 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

/**
 * A CodeMETOCCategory is similar to the Tactical Graphics
 * CodeCategory, but it applies to the METOC symbol set instead.  This
 * CodePosition notes the second character in METOC symbol codes, and
 * represents whether a METOC event is in space, the atmosphere or
 * ocean.
 */
public class CodeMETOCCategory extends CodePosition {

    public CodeMETOCCategory() {
	super("METOC Catetory", 2, 2);
    }
}
