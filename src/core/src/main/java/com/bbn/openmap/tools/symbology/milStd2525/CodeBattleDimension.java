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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeBattleDimension.java,v $
// $RCSfile: CodeBattleDimension.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

/**
 * The CodeBattleDimension CodePositions notes whether the symbol
 * represents an object in space, in the air, on the ground, surface
 * of the water or underneath the water. The battle dimension usually
 * changes the background shape of the symbol, and is noted on the
 * third character of the symbol codes of those symbols that have
 * battle dimensions.
 */
public class CodeBattleDimension extends CodePosition {

    public CodeBattleDimension() {
        super("Battle Dimension", 3, 3);
    }
}