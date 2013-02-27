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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/plotLayer/GLOBETempData.java,v
// $
// $RCSfile: GLOBETempData.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 18:44:25 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.plotLayer;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class GLOBETempData extends GLOBEData {

    private static float NO_VALUE = -99;

    private Hashtable site_table = new Hashtable();

    public float overall_min_year_ = Float.NaN;
    public float overall_max_year_ = Float.NaN;
    public float overall_min_temp_ = Float.NaN;
    public float overall_max_temp_ = Float.NaN;

    public GLOBETempData() {
        super();
    }

    /*
     * Data Format:
     * <pre>
     * Air Temperature:
     * Field 1 = AT 
     * Field 2 = GLOBE phase number (1 or 2)
     * Field 3 = Site number
     * Field 4 = Time the data was reported, YYYYMMDD where YYYY = Calendar year MM = Calendar month (starting with 1 = January) DD = Day of month (starting with 1) 
     * Field 5 = Time the data was sampled, YYYYMMDDHH where YYYY = Calendar year MM = Calendar month (starting with 1 = January) DD = Day of month (starting with 1) HH = Hour of day (00-23)
     * Field 6 = Decimal year
     * Field 7 = Measurement location latitude
     * Field 8 = Measurement location longitude (+ = East, - = West)
     * Field 9 = Measurement location elevation (meters above sea level)
     * Field 10 = Current air temperature (degrees Celsius) [Missing value = -99.0]
     * Field 11 = Daily maximum air temperature (degrees Celsius) [Missing value = -99.0]
     * Field 12 = Daily minimum air temperature (degrees Celsius) [Missing value = -99.0]
     * 
     * Example: AT 1 1 19961218 1996121814 1996.96334 44.1281 -68.8747 14 18 22 -2
     *</pre>
     *  
     */
    protected void parseDataFromStream(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        DecimalFormat dec_format = new DecimalFormat();

        try {

            for (int i = 0; i < 2; i++)
                tokenizer.nextToken();
            /*int site_number = */dec_format.parse(tokenizer.nextToken())
                    .intValue();

            for (int i = 0; i < 2; i++)
                tokenizer.nextToken();
            float time = dec_format.parse(tokenizer.nextToken()).floatValue();

            float latitude = dec_format.parse(tokenizer.nextToken())
                    .floatValue();
            float longitude = dec_format.parse(tokenizer.nextToken())
                    .floatValue();

            tokenizer.nextToken(); // ignore Field 9

            float current_temp = dec_format.parse(tokenizer.nextToken())
                    .floatValue();

            //          float max_temp =
            //              dec_format.parse( tokenizer.nextToken() ).floatValue();
            //          float min_temp =
            //              dec_format.parse( tokenizer.nextToken() ).floatValue();

            // Now process the data we just parsed.

            GLOBESite site = findSite(latitude, longitude);

            if (current_temp != NO_VALUE) {
                site.addCurrentTemp(time, current_temp);
                checkLimits(current_temp, time);
            }
            //          if ( max_temp != NO_VALUE )
            //              { site.addMinTemp(time, max_temp); }
            //          if ( min_temp != NO_VALUE )
            //              { site.addMinTemp(time, min_temp); }

            //          System.out.println(" site: " + site_number
            //                             + " time: " + time
            //                             + " lat: " + latitude
            //                             + " lon: " + longitude
            //                             + " t1-3: " + current_temp
            //                             + " (" + max_temp
            //                             + "," + min_temp + ")" );

        } catch (NoSuchElementException e) {
            System.err.println(e + ": " + e.getMessage());
        } catch (ParseException e) {
            System.err.println(e);
        }
    }

    /**
     * See if these values for temp and year are outside of our
     * current notion of how large our data space is.
     * <p>
     * 
     * @param temp
     * @param year
     */

    private void checkLimits(float temp, float year) {
        if (Float.isNaN(overall_max_temp_) || temp > overall_max_temp_)
            overall_max_temp_ = temp;
        if (Float.isNaN(overall_min_temp_) || temp < overall_min_temp_)
            overall_min_temp_ = temp;

        if (Float.isNaN(overall_max_year_) || year > overall_max_year_)
            overall_max_year_ = year;
        if (Float.isNaN(overall_min_year_) || year < overall_min_year_)
            overall_min_year_ = year;
    }

    private GLOBESite findSite(float latitude, float longitude) {
        GLOBESite site = new GLOBESite(latitude, longitude);
        GLOBESite hashed_site = (GLOBESite) site_table.get(site.hash());

        if (hashed_site == null) {
            //      System.out.println("hash_miss: " + latitude + " " +
            // longitude);
            site_table.put(site.hash(), site);
            hashed_site = site;
        }
        return hashed_site;
    }

    public Enumeration getAllSites() {
        return site_table.elements();
    }

    /*
     * public static void main (String argv[]) { try {
     * System.out.println("Getting URL: " + temp_data_source_);
     * GLOBETempData datafile = new GLOBETempData();
     * datafile.loadData(); } catch (IOException e) {
     * System.err.println(e); } }
     */
}