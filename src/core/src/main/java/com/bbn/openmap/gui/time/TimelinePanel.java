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
//$RCSfile: TimelinePanel.java,v $
//$Revision: 1.1 $
//$Date: 2007/09/25 17:31:26 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui.time;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.proj.Cartesian;

public class TimelinePanel extends BasicMapPanel {

    private static final long serialVersionUID = 1L;
    protected TimelineLayer timelineLayer;

    public TimelinePanel() {
        super(true);
        Cartesian cartesian = new Cartesian(new Point2D.Double(), 300000f, 600, 20);
        setLayout(new BorderLayout());
        MapBean mapBean = getMapBean();
        mapBean.setProjection(cartesian);
        // mapBean.setPreferredSize(new Dimension(10, 70));
        mapBean.setBorder(BorderFactory.createLineBorder(TimelineLayer.tint));
        mapBean.setBckgrnd(Color.white);

        MapHandler mh = getMapHandler();
        mh.add(new com.bbn.openmap.LayerHandler());
        mh.add(new com.bbn.openmap.MouseDelegator());
        mh.add(new com.bbn.openmap.event.SelectMouseMode() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            /**
             * Invoked from the MouseWheelListener interface.
             */
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rot = e.getWheelRotation();
                timelineLayer.adjustZoomFromMouseWheel(rot);
            }

        });

        timelineLayer = new TimelineLayer();
        mh.add(timelineLayer);
    }

    public TimelineLayer getTimelineLayer() {
        return timelineLayer;
    }

    public void TimelineLayer(TimelineLayer timelineLayer) {
        this.timelineLayer = timelineLayer;
    }

    public Wrapper getWrapper() {
        return new Wrapper(this);
    }

    public static class Wrapper {
        TimelinePanel panel;

        public Wrapper(TimelinePanel tlp) {
            panel = tlp;
        }

        public TimelinePanel getTimelinePanel() {
            return panel;
        }
    }

    public void setRealTimeMode(boolean realTimeMode) {
        timelineLayer.setRealTimeMode(realTimeMode);
    }
}
