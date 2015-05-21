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
// $Source:
// /cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionEventHandler.java,v
// $
// $RCSfile: AAREventHandler.java,v $
// $Revision: 1.1 $
// $Date: 2007/08/16 22:15:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.util.Collection;
import java.util.List;

/**
 * An OMEventHandler manages OMEvents, creating them from TemporalRecords.
 * 
 * @author dietrick
 */
public interface OMEventHandler {

    List<OMEvent> getEventList();

    /**
     * This is the main call to return OMEvents based on filters set in the
     * GUI. In subclasses, you can make the call to
     * getMacroFilterList(Collection) from here to check against other filters
     * that are being set across all OMEventhandlers.
     * 
     * @param filters A List of Strings. If your OMEventHandler provides
     *        entries into the filterList, you should check the entries on that
     *        list to see if they are in this provided list. If they are, you
     *        should return the OMEvents that fall under that filter String's
     *        jurisdiction.
     * @return List of OMEvents that pass filters
     */
    List<OMEvent> getEventList(List filters);

    List<OMEvent> getMacroFilteredList(Collection eventCollection);

    /**
     * @return List of Strings that serve as pretty names for the gui, and as
     *         filters. OK to return null.
     */
    List getFilters();

    /**
     * Query to find out if a filter should be enabled, based on EventHandler
     * settings and history.
     * 
     * @param filterName the filter string.
     * @return Boolean.TRUE for things that should be display, Boolean.FALSE for
     *         things that shouldn't be displayed, and null for things that
     *         aren't known about.
     */
    Boolean getFilterState(String filterName);

    /**
     * @param filterName
     * @param state
     */
    void setFilterState(String filterName, Boolean state);
}