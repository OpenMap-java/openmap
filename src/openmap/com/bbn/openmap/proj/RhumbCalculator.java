/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/RhumbCalculator.java,v 1.5 2005/12/09 21:09:01 dietrick Exp $
 *
 * Copyright 2004 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.proj;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * 
 * @version $Header:
 *          /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/RhumbCalculator
 *          .java,v 1.1 2004/05/10 20:53:58 dietrick Exp $
 * @author pawkub
 * 
 *         This class contains methods that helps you make calculations
 *         performed ASSOCIATED The so-called. Rhumbline. This class contains
 *         several oglnych wzorw and beyond OpenMap is ASSOCIATED with nothing.
 *         You can drop into OpenMap and contained in it, where will you need
 *         (and it is places a few).
 */
public class RhumbCalculator {

    /**
     * The constructor is private, because (for now) class contains only A few
     * static methods.
     */
    private RhumbCalculator() {
    }

    /**
     * This method allows public location point on the Rhumb Line, znajc
     * Location reference point, the azimuth and Distance.
     * 
     * @param point LatLonPoint the staring point.
     * @param azimuth in radians, clockwise from North (0.0).
     * @param dist distance in radians.
     * @return LatLonPoint on the rhumb line at the azimuth and distance
     *         specified.
     */
    public static LatLonPoint calculatePointOnRhumbLine(LatLonPoint point, double azimuth,
                                                        double dist) {
        double az = MoreMath.TWO_PI_D - azimuth;
        double lat1 = point.getRadLat();
        double lon1 = point.getRadLon();

        double lat = lat1 + dist * Math.cos(az);
        double dphi = Math.log(Math.abs((1 + Math.sin(lat)) / Math.cos(lat)))
                - Math.log(Math.abs((1 + Math.sin(lat1)) / Math.cos(lat1)));
        double dlon = 0.0;

        if (Math.abs(Math.cos(az)) > Math.sqrt(0.00000000000001)) {
            dlon = dphi * Math.tan(az);
        } else { // along parallel
            dlon = Math.sin(az) * dist / Math.cos(lat1);
        }

        double lon = mod(lon1 - dlon + Math.PI, 2 * Math.PI) - Math.PI;

        return new LatLonPoint.Double(lat, lon, true);
    }

    /**
     * This method allows znormowa worth to the given
     * 
     * @param y
     * @param x
     * @return regulated worth
     */
    private static double mod(double y, double x) {
        double ret;
        if (y >= 0) {
            ret = y - x * (int) (y / x);
        } else {
            ret = y + x * ((int) (-y / x) + 1);
        }
        return ret;
    }

    /**
     * 
     * @param p1 LatLonPoint the first point.
     * @param p2 LatLonPoint to the second point.
     * @return distance in radians
     */
    public static double getDistanceBetweenPoints(LatLonPoint p1, LatLonPoint p2) {
        double lat1 = p1.getRadLat();
        double lon1 = p1.getRadLon();
        double lat2 = p2.getRadLat();
        double lon2 = p2.getRadLon();
        double d = 0.0;
        double tc = 0.0;
        double dlon_W = mod(lon2 - lon1, 2 * Math.PI);
        double dlon_E = mod(lon1 - lon2, 2 * Math.PI);

        tc = getAzimuthBetweenPoints(p1, p2);
        if (Math.abs(lat1 - lat2) < Math.sqrt(0.00000000000001)) {
            // distance along parallel
            d = Math.min(dlon_W, dlon_E) * Math.cos(lat1);
        } else {
            d = Math.abs((lat2 - lat1) / Math.cos(tc));
        }
        return d;
    }

    /**
     * This method allows you to calculate the azimuth between the points.
     * 
     * @param p1 LatLonPoint the first point.
     * @param p2 LatLonPoint to the second point.
     * @return azimuth in radians of the bearing from the first point to the
     *         second point.
     */
    public static double getAzimuthBetweenPoints(LatLonPoint p1, LatLonPoint p2) {
        double lat1 = p1.getRadLat();
        double lon1 = p1.getRadLon();
        double lat2 = p2.getRadLat();
        double lon2 = p2.getRadLon();

        double tc = 0.0;
        double dlon_W = mod(lon2 - lon1, 2.0 * Math.PI);
        double dlon_E = mod(lon1 - lon2, 2.0 * Math.PI);
        double dphi = Math.log(Math.abs((1 + Math.sin(lat2)) / Math.cos(lat2)))
                - Math.log(Math.abs((1 + Math.sin(lat1)) / Math.cos(lat1)));
        if (dlon_W < dlon_E) {// West is the shortest
            tc = mod(Math.atan2(-dlon_W, dphi), 2.0 * Math.PI);
        } else {
            tc = mod(Math.atan2(dlon_E, dphi), 2.0 * Math.PI);
        }
        return MoreMath.TWO_PI_D - tc;
    }

}