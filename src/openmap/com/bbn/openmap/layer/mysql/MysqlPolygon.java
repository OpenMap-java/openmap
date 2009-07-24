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

import java.util.Vector;

/**
 * This class represents the encapsulation of a MySQL Polygon
 * Geometry. Coordinate values are stored as values of double
 * precision in arrays as a sequence of Latitude/Longitude pairs. This
 * differs from the database where values are stored as X/Y or
 * Easting/Northing pairs.
 * 
 * <p>
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlPolygon extends MysqlGeometry {

    private Vector rings;

    public MysqlPolygon() {
        super();
        this.setType(MysqlGeometry.POLYGONTTYPE);
    }

    /**
     * Returns the rings.
     * 
     * @return Vector
     */
    public Vector getRings() {
        return rings;
    }

    /**
     * Sets the rings.
     * 
     * @param rings The rings to set
     */
    public void setRings(Vector rings) {
        this.rings = rings;
    }

}