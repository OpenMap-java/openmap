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
 * This class represents the encapsulation of a MySQL MultiLineString.
 * Coordinate values are stored as values of double precision in
 * arrays as a sequence of Latitude/Longitude pairs. This differs from
 * the database where values are stored as X/Y or Easting/Northing
 * pairs.
 * <p>
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlMultiLineString extends MysqlMulti {

    public MysqlMultiLineString() {
        super();
        this.setType(MysqlGeometry.MULTILINESTRINGTYPE);
    }

    /**
     * @see com.bbn.openmap.layer.mysql.MysqlMulti#addElement(MysqlGeometry)
     */
    public void addElement(MysqlGeometry l) {
        if (l.getType().equals(MysqlGeometry.LINESTRINGTYPE)) {
            super.elements.add((MysqlLine) l);
        }
    }

    public MysqlGeometry getElementByIndex(int i) {
        return (MysqlLine) super.elements.elementAt(i);
    }

}