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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/Visual.java,v
// $
// $RCSfile: Visual.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

public class Visual {

    /**
     * The visual representation of this Waypoint.
     */
    private RoadGraphic visual;

    protected boolean blinkState = false;

    /**
     * The RoadLayer of which we are a part.
     */
    protected RoadLayer layer;

    /**
     * Mark this Visual as needing to have its visual representation
     * updated. It has moved or otherwise changed its appearance.
     */
    public void update() {
        visual = null;
    }

    public void setVisual(RoadGraphic newVisual) {
        visual = newVisual;
        visual.blink(blinkState);
    }

    public RoadGraphic getVisual() {
        return visual;
    }

    public void blink(boolean newState) {
        blinkState = newState;
        if (visual != null)
            visual.blink(newState);
    }
}