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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapMouseEvent.java,v $
// $RCSfile: MapMouseEvent.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.bbn.openmap.MapBean;

/**
 * MouseEvent extension and wrapper that provides the additional capability to
 * get a lat/lon translation for the x,y location for the MouseEvent if the
 * source is a MapBean, and also to get a handle to the MapMouseMode that is
 * currently active and distributing the MouseEvents.
 */
public class MapMouseEvent extends MouseEvent {

    protected MapMouseMode mapMouseMode = null;
    protected MapBean map = null;

    /**
     * Create a MapMouseEvent from a MapMouseMode that is distributing the event
     * and the original MouseEvent delivered from a source component, most
     * likely a MapBean.
     */
    public MapMouseEvent(MapMouseMode mode, MouseEvent me) {
        super((Component) me.getSource(),
              me.getID(),
              me.getWhen(),
              me.getModifiers(),
              me.getX(),
              me.getY(),
              me.getClickCount(),
              me.isPopupTrigger());
        if (me.getSource() instanceof MapBean) {
            map = (MapBean) me.getSource();
        }
        mapMouseMode = mode;
    }

    /**
     * Get the Lat/Lon for the x/y point, in the current projection of the
     * MapBean that sent the MouseEvent. Could be null if the MouseEvent did not
     * originate from a MapBean.
     */
    public Point2D getLatLon() {
        if (map != null) {
            return map.getCoordinates(this);
        } else
            return null;
    }

    /**
     * If the map isn't rotated, this provides the same coordinates as the
     * getX() and getY() methods would. If the map is rotated, this method
     * provides the projected coordinates of the MouseEvent, i.e. the location
     * of the MouseEvent in the non-rotated pixel space of the projection.
     * 
     * @return Point2D coordinates of location of mouse position
     */
    public Point2D getProjectedLocation() {
        if (map != null) {
            return map.getNonRotatedLocation(this);
        } else {
            return new Point2D.Double(getX(), getY());
        }
    }

    /**
     * Get the MapMouseMode that sent this event. This is different than the
     * source of the Event - the MapMouseMode is simply controlling the
     * distribution of the events. May be null if there isn't a MapMouseMode
     * delivering the MapMouseMode.
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
    
    public MapBean getMap() {
        return map;
    }
    
    public boolean mapIsRotated() {
        return (map != null && map.getRotation() != 0.0);
    }

}