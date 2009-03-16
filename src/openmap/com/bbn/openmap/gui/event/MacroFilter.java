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
//$RCSfile: MacroFilter.java,v $
//$Revision: 1.1 $
//$Date: 2007/08/16 22:15:20 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui.event;

import java.util.List;

/**
 * A Macro filter knows how to take a Collection of objects and return a list of
 * them that pass its filters.
 * 
 * @author dietrick
 */
public interface MacroFilter extends FilterPresenter {
    public List getMacroFilteredList(List listOfEvents);
}