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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/db/LocationData.java,v $
// $RCSfile: LocationData.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:00 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class is responsible for retrieving Latitude and Longitude Data from a
 * table in a Database given a City/Town name, State. Also it retrieves
 * identifier of the object that would be used to represent this City/Town on
 * Map. For instance, identifier can be either a url or a name that can be
 * looked up somewhere else.
 * <P>
 * This class needs the RecordSet to be called with the following query: <BR>
 * select CITY (string), STATE (string), GRAPHIC (string, url or graphic name),
 * LATITUDE (float), LONGITUDE (float) from LOCATION_TABLE (tablename where data
 * is stored) <BR>
 * The class is expecting the results in this order.
 */
public class LocationData {

    /* variables that would hold current values of record set */
    protected String cityName, stateName, graphicName;
    protected float latitude, longitude;
    protected String queryString = null;


    public LocationData(RecordSet drs)
            throws SQLException {

        ResultSet rset = drs.getResultSet();

        cityName = rset.getString(1);
        stateName = rset.getString(2);
        graphicName = rset.getString(3);
        latitude = rset.getFloat(4);
        longitude = rset.getFloat(5);
    }

    /**
     * @return city name of current record
     */
    public String getCityName() {
        return cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public String getGraphicName() {
        return graphicName;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String inQueryString) {
        queryString = inQueryString;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Location Data values:\n");
        s.append(" City Name = ").append(cityName).append("\n");
        s.append(" State Name = ").append(stateName).append("\n");
        s.append(" Graphic = ").append(graphicName).append("\n");
        s.append(" Latitude = ").append(latitude).append("\n");
        s.append(" Longitude = ").append(longitude).append("\n");
        return s.toString();
    }

    public static void main(String[] args) {
        System.out.println("\n*** LocationData handles results from the following query: ***\n");
        System.out.println(" select CITY (string), STATE (string), GRAPHIC (string, url or graphic name), LATITUDE (float), LONGITUDE (float) from LOCATION_TABLE (tablename where data is stored)\n");
        System.out.println("Note: Column names and tablename should reflect whatever is stored in the database.  Data types should match what this class is expecting, in the order listed here.\n");
    }

}