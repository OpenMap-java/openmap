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
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Length;

public class RotTool extends OMToolComponent {

    protected MapBean map;

    public RotTool() {
        JSlider slider = new JSlider(-180, 180, 0);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                int value = slider.getValue();
                
                setRotation(Length.DECIMAL_DEGREE.toRadians(value));
            }
        });
        this.add(slider);
    }

    public void setRotation(double rot) {
        if (map != null) {
            map.setRotation(rot);
        }
    }

    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);

        if (someObj instanceof MapBean) {
            setMapBean((MapBean) someObj);
        }

    }

    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        if (getMapBean() == someObj) {
            setMapBean(null);
        }
    }

    public void setMapBean(MapBean mb) {
        map = mb;
    }

    public MapBean getMapBean() {
        return map;
    }
}
