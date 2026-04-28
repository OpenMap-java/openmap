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
import java.util.HashMap;
import java.util.Map;

import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;

public class GLOBESite {

    private float lon, lat;

    private float maxYear = -9999;
    private float minYear = 99999;
    private float maxTemp = -99999;
    private float minTemp = 99999;

    private Map<Float, Float> temperatureTable = null;

    private String name;

    private OMGraphic omg;

    public GLOBESite(float lat, float lon) {
        this.lon = lon;
        this.lat = lat;

        name = "(" + lat + ", " + lon + ")";

        //      graphic_ = new OMCircle(lat, lon, 5,5, 11, 11,
        // default_bits_);
        omg = new OMCircle(lat, lon, 5, 5);
        omg.setLinePaint(Color.red);
        omg.setFillPaint(Color.red);
        omg.setSelectPaint(Color.yellow);
        omg.putAttribute(OMGraphic.APP_OBJECT, this);

        temperatureTable = new HashMap<>();
    }

    public String hash() {
        return name;
    }

    public void addCurrentTemp(float year, float temp) {
        temperatureTable.put(new Float(year), new Float(temp));
    }

    public float getCurrentTemp(float year) {
        return ((Number) temperatureTable.get(new Float(year))).floatValue();
    }

    public float getLatitude() {
        return lat;
    }

    public float getLongitude() {
        return lon;
    }

    public final OMGraphic getGraphic() {
        return omg;
    }

    public String getName() {
        return name;
    }

    public Map<Float, Float> getAllYears() {
        return temperatureTable;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public float getMaxYear() {
        return maxYear;
    }

    public float getMinYear() {
        return minYear;
    }

    public float getValueForYear(float year) {
        if (temperatureTable.containsKey(new Float(year))) {
            return temperatureTable.get(year);
        }
        return -99;
    }

    protected void recalcLimits() {
    	for (Float year : temperatureTable.keySet()) {
            
            float temp = temperatureTable.get(year);
            if (year > maxYear)
                maxYear = year;
            if (year < minYear)
                minYear = year;

            if (temp > maxTemp)
                maxTemp = temp;
            if (temp < minTemp)
                maxTemp = temp;
        }
    }

    public String getInfo() {
        int numpoints = temperatureTable.size();
        return name + " -- " + numpoints + " datapoints available";
    }

}