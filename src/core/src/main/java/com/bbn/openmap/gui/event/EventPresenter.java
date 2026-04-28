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
// $Source: /cvs/darwars/ambush/aar/src/com/bbn/hotwash/gui/EventPresenter.java,v $
// $RCSfile: EventPresenter.java,v $
// $Revision: 1.1 $
// $Date: 2007/08/16 22:15:20 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.event;

import java.awt.Component;
import java.util.Iterator;

import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.omGraphics.DrawingAttributes;

/**
 * An EventPresenter is a empty interface that marks a component to be picked up
 * by the EventPanel.
 */
public interface EventPresenter extends FilterPresenter {

    /**
     * A property string to use for PropertyChangeListeners listening for when
     * the presenter's contents have changed, either due to filtering or the
     * availability of new EventHandlers.
     */
    public final static String ActiveEventsProperty = "activeEvents";
    /**
     * A property string to use for PropertyChangeListeners interested in
     * knowing what events are currently selected by the user.
     */
    public final static String SelectedEventsProperty = "selectedEvents";
    /**
     * A property string used when event attributes (ratings, play filter
     * settings) have been changed.
     */
    public final static String EventAttributesUpdatedProperty = "eventAttributesUpdated";

    /**
     * @return the main event display.
     */
    Component getComponent();

    /**
     * Return a list of active events.
     */
    Iterator<OMEvent> getActiveEvents();

    /**
     * Return a list of all events.
     */
    Iterator<OMEvent> getAllEvents();

    /**
     * Return a set of drawing attributes that match what the presenter is using
     * for selection;
     * 
     * @return DrawingAttributes containing rendering info for selected items.
     */
    DrawingAttributes getSelectionDrawingAttributes();

}