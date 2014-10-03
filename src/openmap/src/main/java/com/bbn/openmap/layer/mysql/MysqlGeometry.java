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
 * This class represents the base class of MySQL Geometry. Coordinate
 * values are stored as values of double precision in arrays as a
 * sequence of Latitude/Longitude pairs. This differs from the
 * database where values are stored as X/Y or Easting/Northing pairs.
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlGeometry {

    private String type;

    public final static String POINTTYPE = "POINT";
    public final static String LINESTRINGTYPE = "LINESTRING";
    public final static String POLYGONTTYPE = "POLYGON";
    public final static String MULTIPOINTTYPE = "MULTIPOINT";
    public final static String MULTILINESTRINGTYPE = "MULTILINESTRING";
    public final static String MULTIPOLYGONTYPE = "MULTIPOLYGON";
    public final static String GEOMETRYCOLLECTIONTYPE = "GEOMETRYCOLLECTION";

    /**
     * Returns the type.
     * 
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * 
     * @param type The type to set
     */
    public void setType(String type) {
        this.type = type;
    }

}