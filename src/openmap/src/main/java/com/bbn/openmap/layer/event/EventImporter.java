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

import com.bbn.openmap.omGraphics.time.TemporalOMGraphicList;

/**
 * EventImporter is an object that the EventLayer uses to query a data source
 * for events/records. It creates a TemporalOMGraphicList for the EventLayer to
 * use.
 * 
 * @author dietrick
 */
public interface EventImporter {

    /**
     * Read the data files and construct the TemporalOMGraphics.  There are four things you need to do in this method.
     * <ul>
     * <li>Create an TemporalOMGraphicList, add TemporalOMGraphics, return it.
     * <li>Set a new TimeBounds object on the callback EventLayer when all the timestamp range is known.
     * <li>Add OMEvents to the callback.events list, one for each TemporalPoint created.
     * <li>Add locations to callback's DataBounds (callback.getDataBounds()).
     * </ul>
     */
    TemporalOMGraphicList createData(EventLayer callback);

}
