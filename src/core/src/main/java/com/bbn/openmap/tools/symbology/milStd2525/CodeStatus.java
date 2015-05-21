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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeStatus.java,v $
// $RCSfile: CodeStatus.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

/**
 * The CodeStatus CodePosition is used to mark and anticipated or
 * planned object, or one that is currently present. It is used on
 * position 4 in the symbol code.
 */
public class CodeStatus extends CodePosition {

    public CodeStatus() {
        super("Status", 4, 4);
    }

}