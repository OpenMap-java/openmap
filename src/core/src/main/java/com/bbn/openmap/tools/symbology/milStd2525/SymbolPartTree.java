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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/SymbolPartTree.java,v $
// $RCSfile: SymbolPartTree.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

/**
 * The SymbolPartTree is a SymbolPart that serves as the top node of
 * the SymbolPart tree.
 */
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