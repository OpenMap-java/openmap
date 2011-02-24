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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.OMComponent;

/**
 * OMEventHandler subclasses look for the things that create OMEvents.
 * 
 * @author dietrick
 */
public abstract class OMEventHandlerAdapter extends OMComponent implements
        OMEventHandler {

    protected LinkedList<OMEvent> events;
    protected List filterList;
    protected List macroFilters;
    protected Hashtable filterStates;

    public final static String ShowEventsAtStartupProperty = "showEvents";

    public OMEventHandlerAdapter() {
        events = new LinkedList<OMEvent>();
        filterList = new LinkedList();
        macroFilters = new LinkedList();
        filterStates = new Hashtable();
    }

    public void addEvent(OMEvent me) {
        events.add(me);
    }

    public void removeEvent(OMEvent me) {
        events.remove(me);
    }

    public void clearEvents() {
        events.clear();
    }

    public List<OMEvent> getEventList() {
        return getEventList(null);
    }

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
     * @return List of OMEvents that past filters
     */
    public List<OMEvent> getEventList(List filters) {
        // At this level, we just want to return all events. Let
        // subclasses worry about macro-filtered events...
        // return getMacroFilteredList(events);

        return events;
    }

    public void addMacroFilter(OMEventMacroFilter mf) {
        macroFilters.add(mf);
    }

    public void removeMacroFilter(OMEventMacroFilter mf) {
        macroFilters.remove(mf);
    }

    public void clearMacroFilters() {
        macroFilters.clear();
    }

    public List<OMEvent> getMacroFilteredList(Collection eventCollection) {
        List<OMEvent> ret = new LinkedList<OMEvent>();
        // If there are no macro filters, return a list with all
        // mission events.
        ret.addAll(eventCollection);

        if (macroFilters != null) {
            for (Iterator it = macroFilters.iterator(); it.hasNext();) {
                OMEventMacroFilter mf = (OMEventMacroFilter) it.next();
                // Should get whittled down to a list passing macro
                // filters.
                ret = mf.getMacroFilteredList(ret);
            }
        }

        return ret;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof OMEventMacroFilter) {
            addMacroFilter((OMEventMacroFilter) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof OMEventMacroFilter) {
            removeMacroFilter((OMEventMacroFilter) someObj);
        }
    }

    /**
     * @return List of Strings that serve as pretty names for the gui, and as
     *         filters. OK to return null.
     */
    public List getFilters() {
        return filterList;
    }

    /**
     * Query to find out if a filter should be enabled, based on EventHandler
     * settings and history.
     * 
     * @param filterName the filter string.
     * @return Boolean.TRUE for things that should be display, Boolean.FALSE for
     *         things that shouldn't be displayed, and null for things that
     *         aren't known about.
     */
    public Boolean getFilterState(String filterName) {
        return (Boolean) filterStates.get(filterName);
    }

    /**
     * @param filterName
     * @param state
     */
    public void setFilterState(String filterName, Boolean state) {
        if (filterStates.get(filterName) != null) {
            filterStates.put(filterName, state);
        }
    }
}