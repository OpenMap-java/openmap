/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/RhumbCalculator.java,v 1.5 2005/12/09 21:09:01 dietrick Exp $
 *
 * Copyright 2004 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.proj;

import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * 
 * @version $Header:
 *          /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/RhumbCalculator
 *          .java,v 1.1 2004/05/10 20:53:58 dietrick Exp $
 * @author pawkub
 * 
 *         This class contains methods that helps you make calculations
 *         performed ASSOCIATED The so-called. Rhumblinem. This class contains
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

    /*
     * This method allows public location point on the Rhumb Line, znajc
     * Location reference point, the azimuth and Distance.
     * 
     * @param point
     * 
     * @param azimuth in degrees
     * 
     * @param dist Distance in meters
     * 
     * @return point (an object of class LatLonPoint)
     */
    public static LatLonPoint calculatePointOnRhumbLine(LatLonPoint point, float azimuth, float dist) {
        double lat1 = point.getRadLat();
        double lon1 = point.getRadLon();
        double d = (double) dist / 1855.3 * Math.PI / 10800.0;
        double lat = 0.0;
        double lon = 0.0;
        lat = lat1 + d * Math.cos(azimuth);
        double dphi = Math.log((1 + Math.sin(lat)) / Math.cos(lat)) - Math.log((1 + Math.sin(lat1)) / Math.cos(lat1));
        double dlon = 0.0;
        if (Math.abs(Math.cos(azimuth)) > Math.sqrt(0.00000000000001)) {
            dlon = dphi * Math.tan(azimuth);
        } else { // along parallel
            dlon = Math.sin(azimuth) * d / Math.cos(lat1);
        }
        lon = mod(lon1 - dlon + Math.PI, 2 * Math.PI) - Math.PI;
        // System.out.println("calculatePointOnRhumbLine: lat1 =
        // "+lat1+"+ lon1 = "+lon1 + " lat = "+lat+"+ lon = "+lon);
        LatLonPoint ret = (LatLonPoint) point.clone();
        ret.setLatLon(lat, lon, true);
        return ret;
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
     * @param p1 punkt geograficzny
     * @param p2 punkt geograficzny
     * @return distance
     */
    public static float getDistanceBetweenPoints(LatLonPoint p1, LatLonPoint p2) {
        double lat1 = p1.getRadLat();
        double lon1 = p1.getRadLon();
        double lat2 = p2.getRadLat();
        double lon2 = p2.getRadLon();
        double d = 0.0;
        double tc = 0.0;
        double dlon_W = mod(lon2 - lon1, 2 * Math.PI);
        double dlon_E = mod(lon1 - lon2, 2 * Math.PI);
        // double dphi = Math.log((1 + Math.sin(lat2)) / Math.cos(lat2))
        // - Math.log((1 + Math.sin(lat1)) / Math.cos(lat1));
        tc = getAzimuthBetweenPoints(p1, p2);
        if (Math.abs(lat1 - lat2) < Math.sqrt(0.00000000000001)) {
            d = Math.min(dlon_W, dlon_E) * Math.cos(lat1); // distance
            // along
            // parallel
        } else {
            d = Math.abs((lat2 - lat1) / Math.cos(tc));
        }
        float dist = (float) (d * 10800.0 / Math.PI * 1855.3);
        // System.out.println("DWrong = " + distWrong + " DOK = " +
        // dist);
        return dist;
    }

    /**
     * This method allows you to calculate the azimuth between the points
     * (bearing the first to the second)
     * 
     * @param p1 pierwszy punkt geograficzny
     * @param p2 drugi punkt geograficzny
     * @return namiar w radianach
     */
    public static float getAzimuthBetweenPoints(LatLonPoint p1, LatLonPoint p2) {
        double lat1 = p1.getRadLat();
        double lon1 = p1.getRadLon();
        double lat2 = p2.getRadLat();
        double lon2 = p2.getRadLon();

        double tc = 0.0;
        double dlon_W = mod(lon2 - lon1, 2 * Math.PI);
        double dlon_E = mod(lon1 - lon2, 2 * Math.PI);
        double dphi = Math.log((1 + Math.sin(lat2)) / Math.cos(lat2)) - Math.log((1 + Math.sin(lat1)) / Math.cos(lat1));
        if (dlon_W < dlon_E) {// West is the shortest
            tc = mod(Math.atan2(-dlon_W, dphi), 2 * Math.PI);
        } else {
            tc = mod(Math.atan2(dlon_E, dphi), 2 * Math.PI);
        }
        return (float) tc;
    }
}