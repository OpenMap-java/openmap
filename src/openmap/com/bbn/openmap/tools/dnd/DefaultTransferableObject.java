//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/dnd/DefaultTransferableObject.java,v $
//$RCSfile: DefaultTransferableObject.java,v $
//$Revision: 1.2 $
//$Date: 2004/10/14 18:06:25 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;

/**
 * Transferable object class with the default data flavor of
 * DataFlavor.javaJVMLocalObjectMimeType.
 */

public class DefaultTransferableObject implements Transferable {

    public static final DataFlavor OBJECT_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Object/JavaBean");

    private Object obj;

    private DataFlavor[] flavors = { OBJECT_FLAVOR };

    public DefaultTransferableObject(Object data) {
        obj = data;
    }

    /**
     * Adds another supported data flavor to the array.
     */

    public void addTransferDataFlavor(DataFlavor flavor) {
        Arrays.asList(flavors).add(flavor);
    }

    /**
     * Returns an object which represents the data to be transferred.
     * The class of the object returned is defined by the
     * representation class of the flavor.
     * 
     * @param flavor the requested flavor for the data
     * @see DataFlavor#getRepresentationClass
     * @exception IOException if the data is no longer available in
     *            the requested flavor.
     * @exception UnsupportedFlavorException if the requested data
     *            flavor is not supported.
     */

    public synchronized Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {

        for (int i = 0; i < flavors.length; i++)
            if (flavor == flavors[i]) {
                return obj;
            }

        throw new UnsupportedFlavorException(flavor);
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors
     * the data can be provided in. The array should be ordered
     * according to preference for providing the data (from most
     * richly descriptive to least descriptive).
     * 
     * @return an array of data flavors in which this data can be
     *         transferred
     */

    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    /**
     * Returns whether or not the specified data flavor is supported
     * for this object.
     * 
     * @param flavor the requested flavor for the data
     * @return boolean indicating wjether or not the data flavor is
     *         supported
     */

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Arrays.asList(flavors).contains(flavor);
    }
}