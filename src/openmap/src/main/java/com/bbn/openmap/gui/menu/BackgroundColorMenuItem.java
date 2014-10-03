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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/BackgroundColorMenuItem.java,v
// $
// $RCSfile: BackgroundColorMenuItem.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.bbn.openmap.Environment;
import com.bbn.openmap.LightMapHandlerChild;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.omGraphics.OMColorChooser;

public class BackgroundColorMenuItem extends JMenuItem implements
        ActionListener, LightMapHandlerChild {

    protected MapBean map = null;

    public BackgroundColorMenuItem() {
        this("Set Map Background Color");
    }

    public BackgroundColorMenuItem(String title) {
        super(title);
        addActionListener(this);
    }

    public void setMap(MapBean mapbean) {
        map = mapbean;
    }

    public MapBean getMap() {
        return map;
    }

    public void actionPerformed(ActionEvent ae) {
        if (map != null) {
            Paint newPaint = OMColorChooser.showDialog(this,
                    getText(),
                    map.getBackground());
            if (newPaint != null) {

                String colorString = Integer.toString(((java.awt.Color) newPaint).getRGB());
                Environment.set(Environment.BackgroundColor, colorString);
                map.setBackground((java.awt.Color) newPaint);
                map.setBckgrnd(newPaint);
            }
        }
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof MapBean) {
            setMap((MapBean) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapBean) {
            setMap(null);
        }
    }

}