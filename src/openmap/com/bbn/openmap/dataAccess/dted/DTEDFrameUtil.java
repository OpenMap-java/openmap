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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDFrameUtil.java,v $
// $RCSfile: DTEDFrameUtil.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

/**
 * This class does the conversions that are required for converting
 * latitudes and longitudes into filenames, and from string values of
 * latitudes and longitudes from within a DTED file to float values.
 */
public class DTEDFrameUtil {

    /**
     * Conversion of string found in DTED file to float value.
     * 
     * @param text_lat the String representation of the latitude, from
     *        the file.
     * @return the latitude in decimal degrees.
     */
    public static float stringToLat(String text_lat) {
        int offset = 0;
        int hemisphere = 1;
        String s_seconds;

        if (text_lat.length() > 7 && text_lat.charAt(6) != '.')
            offset = 1;
        char[] contents = text_lat.toCharArray();
        String s_degrees = String.valueOf(contents, offset, 2);
        String s_minutes = String.valueOf(contents, 2 + offset, 2);

        if (text_lat.charAt(6 + offset) == '.') {
            s_seconds = String.valueOf(contents, 4 + offset, 4);
            if (text_lat.charAt(8 + offset) == 'S')
                hemisphere = -1;
        } else {
            s_seconds = String.valueOf(contents, 4 + offset, 2);
            if (text_lat.charAt(6 + offset) == 'S')
                hemisphere = -1;
        }

        float result = Float.valueOf(s_degrees).floatValue()
                + (Float.valueOf(s_minutes).floatValue() / 60.0F)
                + (Float.valueOf(s_seconds).floatValue() / 3600.0F);
        result *= hemisphere;
        return result;
    }

    /**
     * Conversion of a string value of a longitude to a float.
     * 
     * @param text_lon the longitude String representation, out of a
     *        DTED file.
     * @return the longitude in decimal degrees.
     */
    public static float stringToLon(String text_lon) {
        int hemisphere = 1;
        String s_seconds;

        char[] contents = text_lon.toCharArray();
        String s_degrees = String.valueOf(contents, 0, 3);
        String s_minutes = String.valueOf(contents, 3, 2);

        if (text_lon.charAt(7) == '.') {
            s_seconds = String.valueOf(contents, 5, 4);
            if (text_lon.charAt(9) == 'W')
                hemisphere = -1;
        } else {
            s_seconds = String.valueOf(contents, 5, 2);
            if (text_lon.charAt(7) == 'W')
                hemisphere = -1;
        }

        float result = Float.valueOf(s_degrees).floatValue()
                + (Float.valueOf(s_minutes).floatValue() / 60.0F)
                + (Float.valueOf(s_seconds).floatValue() / 3600.0F);
        result *= hemisphere;
        return result;
    }

    /**
     * Conversion of a float latitude value and DTED level to a DTED
     * filename. DTED filenames are based on these two factors -<n/s>
     * <value>.dt{0,1} DTED Level 0 files are named .dt0. Level 1 and
     * higher are named .dt1, rather than .dt{level}
     * 
     * @param lat latitude in decimal degrees.
     * @param level the DTED level.
     * @return part of the DTED directory path that longitude
     *         contributes to.
     */
    public static String latToFileString(float lat, int level) {
        String direction;
        if (lat >= 0) {
            direction = "n";
            lat = (float) Math.floor(lat);
        } else {
            direction = "s";
            lat = (float) Math.ceil(lat * -1);
        }

        //      int ilat = new Float(lat).intValue();
        //      result = new String(direction + ilat + ".dt" + level);

        // The two lines above wrongly manages lat < 10. It generates
        // n5.dt0 instead of n05.dt0 - sokolov@system.ecology.su.se -
        // 14
        // April 1999

        java.text.DecimalFormat fd = new java.text.DecimalFormat("00");
        String result = direction + fd.format(lat) + ".dt" + level;

        return result;
    }

    /**
     * Conversion of a float longitude value to a DTED parent
     * directory of frame files. DTED parent directories are based on
     * latitude -<w/e><value>.
     * 
     * @param lon longitude in decimal degrees.
     * @return part of the DTED directory path that longitude
     *         contributes to.
     */
    public static String lonToFileString(float lon) {
        String result, direction;
        if (lon >= 0) {
            direction = "e";
            lon = (float) Math.floor(lon);
        } else {
            direction = "w";
            lon = (float) Math.ceil(lon * -1);
        }

        //      int ilon = new Float(lon).intValue();
        //      if (ilon > 100) result = new String(direction + ilon);
        //      else result = new String(direction + "0" + ilon);

        // The three lines above wrongly manages lon < 10. It
        // generates e05 instead of e005 -
        // sokolov@system.ecology.su.se
        // - 14 April 1999

        java.text.DecimalFormat fd = new java.text.DecimalFormat("000");
        result = direction + fd.format(lon);

        return result;
    }

}

