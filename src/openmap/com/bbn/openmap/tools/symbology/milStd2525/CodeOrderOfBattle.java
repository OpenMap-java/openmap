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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeOrderOfBattle.java,v $
// $RCSfile: CodeOrderOfBattle.java,v $
// $Revision: 1.6 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

/**
 * The CodeOrderOfBattle CodePosition handles the 15th character in
 * most symbol codes.  It provides additional information about the
 * role of a symbol in the battlespace.
 */
public class CodeOrderOfBattle extends CodePosition {

    public CodeOrderOfBattle() {
        super("Order of Battle", 15, 15);
    }
}
