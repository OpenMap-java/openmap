// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/TerrainTool.java,v $
// $RCSfile: TerrainTool.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.stateMachine.State;

/**
 * This is a interface that defines common functionality among tools
 * (generators) used by the TerrainLayer.
 */
public interface TerrainTool {

    final static int MAX_SPACE_BETWEEN_PIXELS = 5;

    /** Tell the tool to initialize. */
    public void init();

    /** Tell the tool to reset. */
    public void reset();

    /** Get the current list of graphics from the tool. */
    public OMGraphicList getGraphics();

    /** Let the tool know what the screen looks like. */
    public void setScreenParameters(Projection p);

    /** Get the current state from the state machine of the tool. */
    public State getState();
}

