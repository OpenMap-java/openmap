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
//$RCSfile: TimeSliderPanel.java,v $
//$Revision: 1.1 $
//$Date: 2007/09/25 17:31:26 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui.time;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.proj.Cartesian;

public class TimeSliderPanel extends BasicMapPanel {
    protected TimeSliderLayer timeSliderLayer;

    public TimeSliderPanel(boolean realTimeMode) {
        super(true);
        setLayout(new BorderLayout());
        Cartesian cartesian = new Cartesian(new Point2D.Double(), 300000f, 600, 20);
        MapBean mapBean = getMapBean();
        mapBean.setProjection(cartesian);
        mapBean.setMinimumSize(new Dimension(10, 20));
        mapBean.setBorder(BorderFactory.createEmptyBorder());
        mapBean.setBckgrnd(getBackground());
        
        MapHandler mh = getMapHandler();
        mh.add(new com.bbn.openmap.LayerHandler());
        mh.add(new com.bbn.openmap.MouseDelegator());
        mh.add(new TimeSliderMouseMode());

        timeSliderLayer = new TimeSliderLayer(realTimeMode);
        mh.add(timeSliderLayer);
        mh.add(timeSliderLayer.getTimeLabels());
    }

    public TimeSliderLayer getTimeSliderLayer() {
        return timeSliderLayer;
    }

    public void setTimeSliderLayer(TimeSliderLayer timeSliderLayer) {
        this.timeSliderLayer = timeSliderLayer;
    }

    public static class TimeSliderMouseMode extends
            com.bbn.openmap.event.SelectMouseMode {
        public TimeSliderMouseMode() {
            super();
        }

        public void mouseWheelMoved(MouseWheelEvent e) {}

    }

    public void addTimeBoundsUserActionsListener(
            ITimeBoundsUserActionsListener timeBoundsUserActionsListener) {
        timeSliderLayer.addTimeBoundsUserActionsListener(timeBoundsUserActionsListener);
    }
    
    public void removeTimeBoundsUserActionsListener(
            ITimeBoundsUserActionsListener timeBoundsUserActionsListener) {
        timeSliderLayer.removeTimeBoundsUserActionsListener(timeBoundsUserActionsListener);
    }

    public void setUserHasChangedScale(boolean userHasChangedScale) {
        timeSliderLayer.setUserHasChangedScale(userHasChangedScale);
    }
}
