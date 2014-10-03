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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/plotLayer/GLOBESite.java,v
// $
// $RCSfile: GLOBESite.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 18:44:25 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.plotLayer;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;

public class GLOBESite {

    private float longitude_, latitude_;

    private float max_year_ = -9999;
    private float min_year_ = 99999;
    private float max_temp_ = -99999;
    private float min_temp_ = 99999;

    private Hashtable temp_table_ = null;
    //     private Hashtable max_temp_table_ = null;
    //     private Hashtable min_temp_table_ = null;

    private String name_;

//    private static final byte[] default_bits_ = { (byte) 0x00, (byte) 0x00,
//            (byte) 0x00, (byte) 0x00, (byte) 0x70, (byte) 0x00, (byte) 0xf8,
//            (byte) 0x00, (byte) 0xfc, (byte) 0x01, (byte) 0xfc, (byte) 0x01,
//            (byte) 0xfc, (byte) 0x01, (byte) 0xf8, (byte) 0x00, (byte) 0x70,
//            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    private OMGraphic graphic_;

    public GLOBESite(float lat, float lon) {
        longitude_ = lon;
        latitude_ = lat;

        name_ = "(" + latitude_ + ", " + longitude_ + ")";

        //      graphic_ = new OMCircle(lat, lon, 5,5, 11, 11,
        // default_bits_);
        graphic_ = new OMCircle(lat, lon, 5, 5);
        graphic_.setLinePaint(Color.red);
        graphic_.setFillPaint(Color.red);
        graphic_.setSelectPaint(Color.yellow);
        graphic_.setAppObject(this);

        temp_table_ = new Hashtable();
        //      max_temp_table_ = new Hashtable();
        //      min_temp_table_ = new Hashtable();
    }

    public String hash() {
        return name_;
    }

    public void addCurrentTemp(float year, float temp) {
        temp_table_.put(new Float(year), new Float(temp));
    }

    public float getCurrentTemp(float year) {
        return ((Number) temp_table_.get(new Float(year))).floatValue();
    }

    //     public void addMaxTemp(float year, float temp)
    //     {max_temp_table_.put(new Float(year), new Float(temp));}
    //     public float getMaxTemp(float year)
    //     {return ((Number)max_temp_table_.get(new
    // Float(year))).floatValue();}

    //     public void addMinTemp(float year, float temp)
    //     {min_temp_table_.put(new Float(year), new Float(temp));}
    //     public float getMinTemp(float year)
    //     {return ((Number)min_temp_table_.get(new
    // Float(year))).floatValue();}

    public float getLatitude() {
        return latitude_;
    }

    public float getLongitude() {
        return longitude_;
    }

    public final OMGraphic getGraphic() {
        return graphic_;
    }

    public String getName() {
        return name_;
    }

    public Enumeration getAllYears() {
        return temp_table_.keys();
    }

    public float getMaxTemp() {
        return max_temp_;
    }

    public float getMinTemp() {
        return min_temp_;
    }

    public float getMaxYear() {
        return max_year_;
    }

    public float getMinYear() {
        return min_year_;
    }

    public float getValueForYear(float year) {
        if (temp_table_.containsKey(new Float(year))) {
            return ((Float) temp_table_.get(new Float(year))).floatValue();
        }
        return -99;
    }

    protected void recalcLimits() {
        Enumeration all_years = temp_table_.keys();
        while (all_years.hasMoreElements()) {
            float year = ((Float) all_years.nextElement()).floatValue();
            float temp = ((Float) temp_table_.get(new Float(year))).floatValue();
            if (year > max_year_)
                max_year_ = year;
            if (year < min_year_)
                min_year_ = year;

            if (temp > max_temp_)
                max_temp_ = temp;
            if (temp < min_temp_)
                max_temp_ = temp;
        }
    }

    public String getInfo() {
        int numpoints = temp_table_.size();
        return name_ + " -- " + numpoints + " datapoints available";
    }

}