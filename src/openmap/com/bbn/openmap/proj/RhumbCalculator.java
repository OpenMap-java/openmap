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
 *          /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/RhumbCalculator.java,v
 *          1.1 2004/05/10 20:53:58 dietrick Exp $
 * @author pawkub
 * 
 * Klasa zawiera metody pozwalaj¹ce przeprowadzaæ obliczenia zwi¹zane
 * z tzw. Rhumblinem. Klasa zawiera kilka ogólnych wzorów i poza
 * OpenMap¹ nie jest z niczym zwi¹zana. Mo¿na j¹ wrzuciæ do OpenMapy i
 * u¿yæ zawartych w niej wzorów tam gdzie to bêdzie potrzebne (a jest
 * takich miejsc kilka).
 */
public class RhumbCalculator {

    /**
     * Konstruktor jest prywatny, bo (na razie) klasa zawiera tylko
     * kilka metod statycznych.
     */
    private RhumbCalculator() {}

    /*
     * Metoda pozwala pbliczyæ po³o¿enie punktu na Rhumb Line, znaj¹c
     * po³o¿enie punktu odniesienia, azymut i odleg³oœæ. @param point
     * Punkt odniesienia @param azimuth azymut w stopniach @oparam
     * dist odleg³oœæ w metrach @return punkt (obiekt klasy
     * LatLonPoint)
     */
    public static LatLonPoint calculatePointOnRhumbLine(LatLonPoint point,
                                                        float azimuth,
                                                        float dist) {
        double lat1 = point.getRadLat();
        double lon1 = point.getRadLon();
        double d = (double) dist / 1855.3 * Math.PI / 10800.0;
        double lat = 0.0;
        double lon = 0.0;
        lat = lat1 + d * Math.cos(azimuth);
        double dphi = Math.log((1 + Math.sin(lat)) / Math.cos(lat))
                - Math.log((1 + Math.sin(lat1)) / Math.cos(lat1));
        double dlon = 0.0;
        if (Math.abs(Math.cos(azimuth)) > Math.sqrt(0.00000000000001)) {
            dlon = dphi * Math.tan(azimuth);
        } else { // along parallel
            dlon = Math.sin(azimuth) * d / Math.cos(lat1);
        }
        lon = mod(lon1 - dlon + Math.PI, 2 * Math.PI) - Math.PI;
        //System.out.println("calculatePointOnRhumbLine: lat1 =
        // "+lat1+"+ lon1 = "+lon1 + " lat = "+lat+"+ lon = "+lon);
        LatLonPoint ret = (LatLonPoint) point.clone();
        ret.setLatLon(lat, lon, true);
        return ret;
    }

    /**
     * Metoda pozwala znormowaæ wartoœæ do podanego zakresu
     * 
     * @param y normaowana wartoœæ
     * @param x zakres
     * @return normowana wartoœæ
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
     * Metoda pozwala obliczyæ odleg³oœæ pomiêdzy punktami liczon¹ po
     * rhumbline.
     * 
     * @param p1 punkt geograficzny
     * @param p2 punkt geograficzny
     * @return odleg³oœæ
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
//        double dphi = Math.log((1 + Math.sin(lat2)) / Math.cos(lat2))
//                - Math.log((1 + Math.sin(lat1)) / Math.cos(lat1));
        tc = getAzimuthBetweenPoints(p1, p2);
        if (Math.abs(lat1 - lat2) < Math.sqrt(0.00000000000001)) {
            d = Math.min(dlon_W, dlon_E) * Math.cos(lat1); // distance
                                                           // along
                                                           // parallel
        } else {
            d = Math.abs((lat2 - lat1) / Math.cos(tc));
        }
        float dist = (float) (d * 10800.0 / Math.PI * 1855.3);
        //System.out.println("DWrong = " + distWrong + " DOK = " +
        // dist);
        return dist;
    }

    /**
     * Metoda pozwala obliczyæ azymut pomiêdzy punktami (namiar od
     * pierwszego do drugiego)
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
        double dphi = Math.log((1 + Math.sin(lat2)) / Math.cos(lat2))
                - Math.log((1 + Math.sin(lat1)) / Math.cos(lat1));
        if (dlon_W < dlon_E) {// West is the shortest
            tc = mod(Math.atan2(-dlon_W, dphi), 2 * Math.PI);
        } else {
            tc = mod(Math.atan2(dlon_E, dphi), 2 * Math.PI);
        }
        return (float) tc;
    }

}