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
 * GeometryCollection.
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
public class MysqlCollection extends MysqlMulti {

    public MysqlCollection() {
        super();
        this.setType(MysqlGeometry.GEOMETRYCOLLECTIONTYPE);
    }

    public void addElement(MysqlGeometry mg) {
        super.elements.add(mg);
    }

    public MysqlGeometry getElementByIndex(int i) {
        return (MysqlGeometry) super.elements.elementAt(i);
    }

}