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

package com.bbn.openmap.omGraphics.time;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.Projection;

/**
 * A TemporalOMGraphic object represents an OMGraphic that changes over time.
 * The time is expected to be based on some offset from a time origin, like the
 * starting time of some greater set of events.
 */
public interface TemporalOMGraphic extends OMGraphic {
    
    /**
     * Add a location at a time.
     */
    public void addTimeStamp(TemporalRecord timeStamp);

    /**
     * Remove a location at a certain time.
     */
    public boolean removeTimeStamp(TemporalRecord timeStamp);

    /**
     * Clear all time stamps.
     */
    public void clearTimeStamps();
    
    public void generate(Projection proj, long time);
}
