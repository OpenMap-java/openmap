package com.bbn.openmap.util.coordFormatter;

/**
 * Copyright NAVICON A/S
 * com@navicon.dk
 *
 * Formats a string to represent DMS for lat/lon information.
 */
import java.awt.geom.Point2D;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;

public class MGRSCoordInfoFormatter extends BasicCoordInfoFormatter {

    public MGRSCoordInfoFormatter() {
    }

    public String createCoordinateInformationLine(int x, int y,
                                                  Point2D llp, Object source) {
        if (llp != null) {
            double lat = llp.getY();
            double lon = llp.getX();
            return "MGRS (" +  MGRSPoint.LLtoMGRS(new LatLonPoint.Double(lat, lon)).getMGRS() + ")";
        } else {
            return "MGRS (" + "?" + ", " + "?" + ")";
        }
    }

}
