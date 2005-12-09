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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/LatLonPoint.java,v $
// $RCSfile: LatLonPoint.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap;

import com.bbn.openmap.proj.Projection;

/**
 * Encapsulates latitude and longitude coordinates in decimal degrees.
 * Normalizes the internal representation of latitude and longitude. This class
 * now extends the com.bbn.openmap.proj.coords.LatLonPoint.Float class, and you
 * should start using that class directly. This class will be deprecated soon.
 * <p>
 * <strong>Normalized Latitude: </strong> <br>
 * &minus;90&deg; &lt;= &phi; &lt;= 90&deg;
 * <p>
 * <strong>Normalized Longitude: </strong> <br>
 * &minus;180&deg; &le; &lambda; &le; 180&deg;
 */
public class LatLonPoint extends com.bbn.openmap.proj.coords.LatLonPoint.Float {

    /**
     * Construct a default LatLonPoint.
     */
    public LatLonPoint() {}

    /**
     * Construct a LatLonPoint from raw float lat/lon in decimal degrees.
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public LatLonPoint(float lat, float lon) {
        super(lat, lon, false);
    }

    /**
     * Construct a LatLonPoint from raw float lat/lon in radians.
     * 
     * @param lat latitude in radians
     * @param lon longitude in radians
     * @param isRadian placeholder indicates radians
     */
    public LatLonPoint(float lat, float lon, boolean isRadian) {
        super(lat, lon, isRadian);
    }

    /**
     * Copy construct a LatLonPoint.
     * 
     * @param pt LatLonPoint
     */
    public LatLonPoint(LatLonPoint pt) {
        super(pt);
    }

    /**
     * Construct a LatLonPoint from raw double lat/lon.
     * 
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public LatLonPoint(double lat, double lon) {
        super((float) lat, (float) lon, false);
    }

    /**
     * Utility function that gets a LatLonPoint from a Projection.
     * 
     * @param x horizontal pixel value of map window associated with Projection.
     * @param y vertical pixel value of map window associated with Projection.
     * @param proj Projection asoociated with x, y points.
     * @return LatLonPoint for x, y value.
     */
    public static LatLonPoint getLatLon(int x, int y, Projection proj) {
        return (LatLonPoint) proj.inverse(x, y, new LatLonPoint());
    }

}