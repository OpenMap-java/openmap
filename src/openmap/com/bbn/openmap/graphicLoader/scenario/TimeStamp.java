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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/TimeStamp.java,v $
// $RCSfile: TimeStamp.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.scenario;

import java.util.Comparator;

import com.bbn.openmap.proj.coords.LatLonPoint;


/**
 * A TimeStamp is a latitude and longitude associated with a time. The
 * time is relative.
 */
public class TimeStamp implements Comparator {

    protected float latitude;
    protected float longitude;
    protected long time;

    /**
     * A constructor for a TimeStamp created internally by the
     * ScenarioPoint to be used by its TreeSet as a Comparator.
     */
    protected TimeStamp() {}

    /**
     * Create a TimeStamp to be used as a position by ScenarioPoints.
     */
    public TimeStamp(float lat, float lon, long t) {
        latitude = lat;
        longitude = lon;
        time = t;
    }

    public String toString() {
        return "TimeStamp [ lat=" + latitude + " lon=" + longitude + " time="
                + time + " ]";
    }

    public void setTime(long t) {
        time = t;
    }

    public long getTime() {
        return time;
    }

    public void setLocation(LatLonPoint llp) {
        setLocation(llp.getLatitude(), llp.getLongitude());
    }

    public void setLocation(float lat, float lon) {
        latitude = lat;
        longitude = lon;
    }

    public LatLonPoint getLocation() {
        return getLocation(new LatLonPoint.Double());
    }

    public LatLonPoint getLocation(LatLonPoint llp) {
        llp.setLatLon(latitude, longitude);
        return llp;
    }

    /**
     * Compares its two arguments for order. Returns a negative
     * integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second. Assumes that
     * the objects are TimeStamps. Make the comparison based on time
     * fields.
     */
    public int compare(Object obj1, Object obj2) {
        long time1 = ((TimeStamp) obj1).time;
        long time2 = ((TimeStamp) obj2).time;
        if (time1 < time2) {
            return -1;
        } else if (time1 > time2) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Indicates whether some other object is "equal to" this
     * Comparator. Assumes that the other object is a TimeStamp
     * object. Compares time fields.
     */
    public boolean equals(Object obj) {
        return (((TimeStamp) obj).time == time);
    }

}