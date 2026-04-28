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

package com.bbn.openmap.event;

import java.util.List;

/**
 * A macro filter knows how to take a Collection of objects and return a list of
 * them that pass its filters. This object is generally created to be used for
 * by multiple OMEventHandlers to further filter events based on other
 * parameters that the OMEventHandler may not know about.
 * 
 * @author dietrick
 */
public interface OMEventMacroFilter {
    /**
     * @param listOfEvents a list of events that are currently going to be
     *        displayed in the GUI.
     * @return a filtered list of events that pass the criteria set by this
     *         filter.
     */
    public List<OMEvent> getMacroFilteredList(List<OMEvent> listOfEvents);
}