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
 * This abstract class represents the base class of compound Mysql
 * Geometry Objects.
 * <p>
 * 
 * Copyright 2003 by the Author <br>
 * <p>
 * 
 * @author Uwe Baier uwe.baier@gmx.net <br>
 * @version 1.0 <br>
 */
abstract public class MysqlMulti extends MysqlGeometry {

    protected Vector elements = new Vector();

    public int countElements() {
        return elements.size();
    }

    abstract public void addElement(MysqlGeometry mg);

    abstract public MysqlGeometry getElementByIndex(int i);
}