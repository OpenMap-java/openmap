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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/CenterEvent.java,v $
// $RCSfile: CenterEvent.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

/**
 * An event to request the map should recenter to a new latitude and
 * longitude.
 */
public class CenterEvent extends java.util.EventObject {

    private transient float latitude;
    private transient float longitude;

    /**
     * Construct a CenterEvent.
     * 
     * @param source the source bean
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     */
    public CenterEvent(Object source, float lat, float lon) {
        super(source);
        latitude = lat;
        longitude = lon;
    }

    /**
     * Get the latitude of the center.
     * 
     * @return float latitude in decimal degrees
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * Get the latitude of the center.
     * 
     * @return float latitude in decimal degrees
     */
    public float getLongitude() {
        return longitude;
    }
}