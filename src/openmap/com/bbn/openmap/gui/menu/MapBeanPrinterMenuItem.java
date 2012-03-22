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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/MapBeanPrinterMenuItem.java,v $
// $RCSfile: MapBeanPrinterMenuItem.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.image.MapBeanPrinter;

/**
 * A MapBeanPrinterMenuItem is a MapHandlerMenuItem that looks for the
 * MapBean in the MapHandler and prints it when it is called.
 */
public class MapBeanPrinterMenuItem extends MapHandlerMenuItem implements
        ActionListener {

    protected MapBean mapBean = null;

    public MapBeanPrinterMenuItem(String title) {
        super(title);
        addActionListener(this);
        setEnabled(false);
    }

    public MapBeanPrinterMenuItem() {
        super("Print");
        addActionListener(this);
        setEnabled(false);
    }

    public void setMapBean(MapBean mb) {
        setEnabled(mb != null);
        mapBean = mb;
    }

    public MapBean getMapBean() {
        return mapBean;
    }

    public void actionPerformed(ActionEvent ae) {
        MapBean mb = getMapBean();
        if (mb != null) {
            MapBeanPrinter.printMap(mb);
        }
    }

    public void findAndInit(Object obj) {
        super.findAndInit(obj);
        if (obj instanceof MapBean) {
            setMapBean((MapBean) obj);
        }
    }

    public void findAndUndo(Object obj) {
        super.findAndUndo(obj);
        if (obj instanceof MapBean && obj == getMapBean()) {
            setMapBean(null);
        }
    }
}