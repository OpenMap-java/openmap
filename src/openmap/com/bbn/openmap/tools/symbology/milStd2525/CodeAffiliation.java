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
// $Revision: 1.6 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

/**
 * The CodeAffiliation CodePosition presents options for symbols,
 * noting that the symbol represents a friend, foe or unknown.  This
 * notation is made in the second character of some symbol code
 * strings (those that allow affilications).
 */
public class CodeAffiliation extends CodePosition {

    public CodeAffiliation() {
        super("Affiliation", 2, 2);
    }
}
