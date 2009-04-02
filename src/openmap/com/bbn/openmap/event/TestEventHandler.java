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

package com.bbn.openmap.event;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.time.TimeBounds;
import com.bbn.openmap.time.TimeBoundsHandler;
import com.bbn.openmap.time.TimeBoundsProvider;

public class TestEventHandler extends OMEventHandler implements
        TimeBoundsProvider {

    protected TimeBounds timeBounds;
    protected List<TimeBoundsHandler> timeBoundsHandlers = new ArrayList<TimeBoundsHandler>();

    public TestEventHandler() {
        super();
        timeBounds = new TimeBounds();
        createEvents();
    }

    protected void createEvents() {

        addEvent(new OMEvent("Object Source", "Event 1", System.currentTimeMillis()));
        addEvent(new OMEvent("Object Source", "Event 2", System.currentTimeMillis() + 1000l));
        addEvent(new OMEvent("Object Source", "Event 3", System.currentTimeMillis() + 2000l));

        timeBounds.addTimeToBounds(System.currentTimeMillis() - 10000);
        timeBounds.addTimeToBounds(System.currentTimeMillis() + 10000);
        callForTimeBoundsReset();
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
        return true;
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
