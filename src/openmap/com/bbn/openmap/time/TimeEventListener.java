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
 * A TimeEventListener is interested in the current time setting, and why the
 * time has changed to that particular value. The Clock sends out TimeEvents, so
 * anything that should be listening to the Clock should be a TimeEventListener.
 * 
 * @author dietrick
 */
public interface TimeEventListener {
    void updateTime(TimeEvent te);
}
