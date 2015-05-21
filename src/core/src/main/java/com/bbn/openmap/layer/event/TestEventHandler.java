//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer.event;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.event.OMEventHandlerAdapter;
import com.bbn.openmap.time.TimeBounds;
import com.bbn.openmap.time.TimeBoundsHandler;
import com.bbn.openmap.time.TimeBoundsProvider;

/**
 * Sample OMEventHandler to demonstrate being a TimeBoundsProvider and TimeBoundsHandler.
 * 
 * @author dietrick
 */
public class TestEventHandler extends OMEventHandlerAdapter implements
        TimeBoundsProvider {

    protected TimeBounds timeBounds;
    protected List<TimeBoundsHandler> timeBoundsHandlers = new ArrayList<TimeBoundsHandler>();
    protected boolean active = true;

    public TestEventHandler() {
        super();
        timeBounds = new TimeBounds();
        createEvents();
    }

    protected void createEvents() {

        
        addEvent(new OMEvent("Object Source", "Event 1", System.currentTimeMillis()));
        addEvent(new OMEvent("Object Source", "Event 2", System.currentTimeMillis() + 1000l));
        addEvent(new OMEvent("Object Source", "Event 3", System.currentTimeMillis() + 2000l));
        addEvent(new OMEvent("Object Source", "Event 4", System.currentTimeMillis() + 3500l));
        addEvent(new OMEvent("Object Source", "Event 5", System.currentTimeMillis() + 5500l));
        addEvent(new OMEvent("Object Source", "Event 6", System.currentTimeMillis() + 10000l));
        addEvent(new OMEvent("Object Source", "Event 7", System.currentTimeMillis() + 20000l));
        addEvent(new OMEvent("Object Source", "Event 8", System.currentTimeMillis() + 25600l));
        addEvent(new OMEvent("Object Source", "Event 9", System.currentTimeMillis() + 45000l));
        addEvent(new OMEvent("Object Source", "Event 10", System.currentTimeMillis() + 60900l));

        callForTimeBoundsReset();
    }

    public void addEvent(OMEvent event) {
        super.addEvent(event);
        timeBounds.addTimeToBounds(event.getTimeStamp());
    }
    
    public List<OMEvent> getEventList(List filters) {
        // At this level, we just want to return all events. Let
        // subclasses worry about macro-filtered events...
        // return getMacroFilteredList(events);
        if (active) {
            return events;
        } else {
            return new ArrayList<OMEvent>();
        }
    }

    public void addTimeBoundsHandler(TimeBoundsHandler tbh) {
        timeBoundsHandlers.add(tbh);
    }

    public TimeBounds getTimeBounds() {
        return timeBounds;
    }

    public void handleTimeBounds(TimeBounds tb) {
    // NO-OP
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        callForTimeBoundsReset();
    }

    public void removeTimeBoundsHandler(TimeBoundsHandler tbh) {
        timeBoundsHandlers.remove(tbh);
    }

    public void callForTimeBoundsReset() {
        for (TimeBoundsHandler tbh : timeBoundsHandlers) {
            tbh.resetTimeBounds();
        }
    }

}
