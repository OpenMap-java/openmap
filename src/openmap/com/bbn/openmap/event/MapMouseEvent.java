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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapMouseEvent.java,v $
// $RCSfile: MapMouseEvent.java,v $
// $Revision: 1.3 $
// $Date: 2004/05/10 20:40:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;

import java.awt.Component;
import java.awt.event.MouseEvent;

/**
 * MouseEvent extension and wrapper that provides the additional
 * capability to get a lat/lon translation for the x,y location for
 * the MouseEvent if the source is a MapBean, and also to get a handle
 * to the MapMouseMode that is currently active and distributing the
 * MouseEvents.
 */
public class MapMouseEvent extends MouseEvent {

    protected MapMouseMode mapMouseMode = null;
    protected MapBean map = null;

    /**
     * Create a MapMouseEvent from a MapMouseMode that is distributing
     * the event and the original MouseEvent delivered from a source
     * component, most likely a MapBean.
     */
    public MapMouseEvent(MapMouseMode mode, MouseEvent me) {
        super((Component)me.getSource(), me.getID(), me.getWhen(), me.getModifiers(),
              me.getX(), me.getY(), me.getClickCount(), me.isPopupTrigger());
        if (me.getSource() instanceof MapBean) {
            map = (MapBean) me.getSource();
        }
        mapMouseMode = mode;
    }
        
    /**
     * Get the Lat/Lon for the x/y point, in the current projection of
     * the MapBean that sent the MouseEvent.  Could be null if the
     * MouseEvent did not originate from a MapBean.
     */
    public LatLonPoint getLatLon() {
        if (map != null) {
            return map.getCoordinates(this);
        } else return null;
    }

    /**
     * Get the MapMouseMode that sent this event.  This is different
     * than the source of the Event - the MapMouseMode is simply
     * controlling the distribution of the events.  May be null if
     * there isn't a MapMouseMode delivering the MapMouseMode.
     */
    public MapMouseMode getMapMouseMode() {
        return mapMouseMode;
    }

    /**
     * Returns a String representation of this object.
     */
    public String paramString() {
        return super.paramString() + " " + getLatLon();
    }


}
