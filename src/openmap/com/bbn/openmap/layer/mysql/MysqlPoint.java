/* ***********************************************************************
 * This is used by the MysqlGeometryLayer.
 * This program is distributed freely and in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Copyright 2003 by the Author
 *
 * Author name: Uwe Baier uwe.baier@gmx.net
 * Version 1.0
 * ***********************************************************************
 */
package com.bbn.openmap.layer.mysql;

/**
 * This class represents the encapsulation of a MySQL Point Geometry.
 * Coordinate values are stored as values of double precision in
 * arrays as a sequence of Latitude/Longitude pairs. This differs from
 * the database where values are stored as X/Y or Easting/Northing
 * pairs.
 * 
 * <p>
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlPoint extends MysqlGeometry {

    private double Eastings;
    private double Northings;

    public MysqlPoint() {
        super();
        this.setType(MysqlGeometry.POINTTYPE);
    }

    /**
     * Returns the eastings.
     * 
     * @return double
     */
    public double getEastings() {
        return Eastings;
    }

    /**
     * Returns the northings.
     * 
     * @return double
     */
    public double getNorthings() {
        return Northings;
    }

    /**
     * Sets the eastings.
     * 
     * @param eastings The eastings to set
     */
    public void setEastings(double eastings) {
        Eastings = eastings;
    }

    /**
     * Sets the northings.
     * 
     * @param northings The northings to set
     */
    public void setNorthings(double northings) {
        Northings = northings;
    }

}