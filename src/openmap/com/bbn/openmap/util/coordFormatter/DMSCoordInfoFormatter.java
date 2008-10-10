package com.bbn.openmap.util.coordFormatter;

/**
 * Copyright NAVICON A/S
 * com@navicon.dk
 *
 * Formats a string to represent DMS for lat/lon information.
 */
import java.awt.geom.Point2D;
import java.text.NumberFormat;

public class DMSCoordInfoFormatter extends BasicCoordInfoFormatter {

    public DMSCoordInfoFormatter() {}

    public String createCoordinateInformationLine(int x, int y,
                                                  Point2D llp, Object source) {
        if (llp != null) {
            return "Cursor Position (" + formatLatitude(llp.getY())
                    + ", " + formatLongitude(llp.getX()) + ")";
        } else {
            return "Lat, Lon (" + "?" + ", " + "?" + ")";
        }
    }

    public static String formatLatitude(double latitude) {
        return formatDegreesMinutes(latitude, 2, latitude < 0 ? "S" : "N");
    }

    public static String formatLongitude(double longitude) {
        return formatDegreesMinutes(longitude, 3, longitude < 0 ? "W" : "E");
    }

    public static String formatDegreesMinutes(double value, int integerDigits,
                                              String semisphere) {
        double valueAbs = Math.abs(value);
        int degrees = (int) valueAbs;
        double minutes = (valueAbs - degrees) * 60.0;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(integerDigits);
        nf.setMaximumIntegerDigits(integerDigits);
        String strDegrees = nf.format(degrees);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        String strMinutes = nf.format(minutes);
        return strDegrees + DEGREE_SIGN + strMinutes + "'" + semisphere;
    }
}
