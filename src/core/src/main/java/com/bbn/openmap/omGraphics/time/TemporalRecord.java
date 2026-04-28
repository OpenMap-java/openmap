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

/**
 * A TemporalRecord object represents something that happened at a given time.
 * The time is expected to be based on some offset from a time origin, like the
 * starting time of some greater set of events. The TemporalRecord is the basis
 * for comparing whether something happened before another.
 * 
 * @author dietrick
 */
public interface TemporalRecord {
    long getTime();

    void setTime(long t);
}
