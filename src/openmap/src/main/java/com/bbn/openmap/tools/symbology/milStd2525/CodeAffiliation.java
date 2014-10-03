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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeAffiliation.java,v $
// $RCSfile: CodeAffiliation.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

/**
 * The CodeAffiliation CodePosition presents options for symbols,
 * noting that the symbol represents a friend, foe or unknown. This
 * notation is made in the second character of some symbol code
 * strings (those that allow affiliations).
 */
public class CodeAffiliation extends CodePosition {

    public CodeAffiliation() {
        super("Affiliation", 2, 2);
    }
}