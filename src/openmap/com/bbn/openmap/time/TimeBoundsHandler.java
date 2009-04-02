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

package com.bbn.openmap.time;

/**
 * Describes a component that listens to TimeBoundsProviders, for the purpose
 * of figuring out the overall time bounds when contributions to the bounds will
 * be made from several other objects, some of which may or may not be active. A
 * TimeBoundsHandler is the go-between object between TimeBoundsProviders and
 * TimeBoundsListeners. The TimeBoundsHandler will look for TimeBoundsListeners
 * in the MapHandler and will notify them when the overall time bounds change.
 * 
 * @author dietrick
 */
public interface TimeBoundsHandler {

    public void addTimeBoundsProvider(TimeBoundsProvider tbp);

    public void removeTimeBoundsProvider(TimeBoundsProvider tbp);

    public void clearTimeBoundsProviders();
    
    public void resetTimeBounds();
}
