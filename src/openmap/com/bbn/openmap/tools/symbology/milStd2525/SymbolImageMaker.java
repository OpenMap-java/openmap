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
//$Revision: 1.2 $
//$Date: 2004/12/10 14:17:11 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.symbology.milStd2525;

import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.ImageIcon;

import com.bbn.openmap.PropertyConsumer;

public interface SymbolImageMaker extends PropertyConsumer {
    public final static String BackgroundPaintProperty = "background";
    public final static String DataPathProperty = "path";
    
    public ImageIcon getIcon(String code, Dimension di);
    
    public void setDataPath(String dataPath);
    
    public void setBackground(Paint p);
}
