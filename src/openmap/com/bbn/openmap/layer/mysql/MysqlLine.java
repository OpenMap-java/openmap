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
 * This class represents the encapsulation of a MySQL
 * GeometryLineString. Coordinate values are stored as values of
 * double precision in arrays as a sequence of Latitude/Longitude
 * pairs. This differs from the database where values are stored as
 * X/Y or Easting/Northing pairs.
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlLine extends MysqlGeometry {

    private double[] coordinateArray;

    public MysqlLine() {
        super();
        this.setType(MysqlGeometry.LINESTRINGTYPE);
    }

    /**
     * Returns the coordinateArray.
     * 
     * @return double[]
     */
    public double[] getCoordinateArray() {
        return coordinateArray;
    }

    /**
     * Sets the coordinateArray.
     * 
     * @param coordinateArray The coordinateArray to set
     */
    public void setCoordinateArray(double[] coordinateArray) {
        this.coordinateArray = coordinateArray;
    }

}