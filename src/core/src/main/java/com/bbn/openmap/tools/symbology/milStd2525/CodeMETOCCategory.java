// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
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
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

/**
 * A CodeMETOCCategory is similar to the Tactical Graphics
 * CodeCategory, but it applies to the METOC symbol set instead. This
 * CodePosition notes the second character in METOC symbol codes, and
 * represents whether a METOC event is in space, the atmosphere or
 * ocean.
 */
public class CodeMETOCCategory extends CodePosition {

    public CodeMETOCCategory() {
        super("METOC Catetory", 2, 2);
    }
}