//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: SymbolImageMaker.java,v $
//$Revision: 1.1 $
//$Date: 2004/12/08 01:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.ImageIcon;

public interface SymbolImageMaker {
    public ImageIcon getIcon(String code, Dimension di);
    
    public void setBackground(Paint p);
}
