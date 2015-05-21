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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/MapBeanKeyListener.java,v
// $
// $RCSfile: MapBeanKeyListener.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.OMComponent;

public class MapBeanKeyListener extends OMComponent implements KeyListener {

    protected MapBean mapBean;

    public MapBeanKeyListener() {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    public void setMapBean(MapBean map) {
        if (mapBean != null) {
            mapBean.removeKeyListener(this);
        }

        mapBean = map;

        if (mapBean != null) {
            ((MapBean) map).addKeyListener(this);
        }
    }

    public MapBean getMapBean() {
        return mapBean;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof MapBean) {
            setMapBean((MapBean) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj == getMapBean()) {
            setMapBean(null);
        }
    }
}