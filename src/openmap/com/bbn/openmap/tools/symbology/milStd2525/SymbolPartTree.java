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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolPartTree.java,v $
// $RCSfile: SymbolPartTree.java,v $
// $Revision: 1.1 $
// $Date: 2003/12/16 01:08:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

public class SymbolPartTree extends SymbolPart {

    public SymbolPartTree(String name) {
	prettyName = name;
    }

    /**
     * Get a simple string representation of this SymbolPart,
     * including the 15 digit code and the pretty name.
     */
    public String toString() {
	return "[" + prettyName + "] Symbol Set";
    }
}
