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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/BasicLocation.java,v $
// $RCSfile: BasicLocation.java,v $
// $Revision: 1.4 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  OpenMap  */
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMRect;

/**
 * A BasicLocation the standard Location - dot for the marker, text to the right
 * of the dot. Other graphics can be substituted for the dot, but you should
 * modify the setGraphicLocations methods accordingly.
 */
public class BasicLocation
        extends Location {

    /**
     * A plain constructor if you are planning on setting everything yourself.
     */
    public BasicLocation() {
    }

    /**
     * Create a location at a latitude/longitude. If the locationMarker is null,
     * a small rectangle (dot) will be created to mark the location.
     * 
     * @param latitude the latitude, in decimal degrees, of the location.
     * @param longitude the longitude, in decimal degrees, of the location.
     * @param name the name of the location, also used in the label.
     * @param locationMarker the OMGraphic to use for the location mark.
     */
    public BasicLocation(double latitude, double longitude, String name, OMGraphic locationMarker) {
        super(latitude, longitude, name, locationMarker);
    }

    /**
     * Create a location at a map location. If the locationMarker is null, a
     * small rectangle (dot) will be created to mark the location.
     * 
     * @param x the pixel location of the object from the let of the map.
     * @param y the pixel location of the object from the top of the map
     * @param name the name of the location, also used in the label.
     * @param locationMarker the OMGraphic to use for the location mark.
     */
    public BasicLocation(int x, int y, String name, OMGraphic locationMarker) {
        super(x, y, name, locationMarker);
    }

    /**
     * Create a location at a pixel offset from a latitude/longitude. If the
     * locationMarker is null, a small rectangle (dot) will be created to mark
     * the location.
     * 
     * @param latitude the latitude, in decimal degrees, of the location.
     * @param longitude the longitude, in decimal degrees, of the location.
     * @param xOffset the pixel location of the object from the longitude.
     * @param yOffset the pixel location of the object from the latitude.
     * @param name the name of the location, also used in the label.
     * @param locationMarker the OMGraphic to use for the location mark.
     */
    public BasicLocation(double latitude, double longitude, int xOffset, int yOffset, String name, OMGraphic locationMarker) {
        super(latitude, longitude, xOffset, yOffset, name, locationMarker);
    }

    /**
     * Called by setLocations(). Assumes the dot for the location marker, and a
     * text object as the label, stored to the right.
     */
    public void setGraphicLocations(double latitude, double longitude) {
        if (location instanceof OMRect) {
            ((OMRect) location).setLocation(latitude, longitude, -1, -1, 1, 1);
        }
        label.setLat(latitude);
        label.setLon(longitude);
    }

    /**
     * Called by setLocations(). Assumes the dot for the location marker, and a
     * text object as the label, stored to the right.
     */
    public void setGraphicLocations(int x, int y) {
        if (location instanceof OMRect) {
            ((OMRect) location).setLocation(x - 1, y - 1, x + 1, y + 1);
        }
        label.setX(x);
        label.setY(y);
    }

    /**
     * Called by setLocations(). Assumes the dot for the location marker, and a
     * text object as the label, stored to the right.
     */
    public void setGraphicLocations(double latitude, double longitude, int offsetX, int offsetY) {
        if (location instanceof OMRect) {
            ((OMRect) location).setLocation(latitude, longitude, offsetX - 1, offsetY - 1, offsetX + 1, offsetY + 1);
        }

        label.setLat(latitude);
        label.setLon(longitude);
        label.setX(offsetX);
        label.setY(offsetY);
    }
}