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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadLine.java,v
// $
// $RCSfile: RoadLine.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:09:12 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class RoadLine extends Visual implements RoadObject {

    private Road road;

    private int index;

    public RoadLine(Road road, int index) {
        this.road = road;
        this.index = index;
    }

    public static Class getGraphicClass() {
        return Graphic.class;
    }

    public void moveTo(Point loc) {}

    public RoadPoint addRoadPoint(int x, int y) {
        RoadLayer layer = road.getRoadLayer();
        Projection p = layer.getProjection();
        RoadPoint rp = new RoadPoint(road, p.inverse(x, y, new LatLonPoint.Double()), layer);
        road.insertRoadPointAt(rp, index + 1);
        return rp;
    }

    public Road getRoad() {
        return road;
    }

    public synchronized void render(OMGraphicList gl, boolean forceNew) {
        RoadGraphic visual = getVisual();
        if (visual == null || forceNew) {
            LatLonPoint p1 = road.getWaypoint(index).getLocation();
            LatLonPoint p2 = road.getWaypoint(index + 1).getLocation();
            visual = new Graphic(p1, p2);
            setVisual(visual);
        }
        gl.add((Graphic) visual);
    }

    public class Graphic extends OMLine implements RoadGraphic {
        Graphic(LatLonPoint p1, LatLonPoint p2) {
            super(p1.getLatitude(),
                  p1.getLongitude(),
                  p2.getLatitude(),
                  p2.getLongitude(),
                  OMLine.LINETYPE_STRAIGHT);
            if (road.isBlocked()) {
                setLinePaint(Color.white);
                //setLineWidth(road.getRoadClass().getWidth() * 2 +
                // 2);
            } else {
                setLinePaint(road.getRoadClass().getColor());
                /*
                 * if (road.isRoute()) {
                 * setLineWidth(road.getRoadClass().getWidth() * 2 +
                 * 2); } else {
                 * setLineWidth(road.getRoadClass().getWidth()); }
                 */
            }
        }

        public void blink(boolean newState) {
            blinkState = newState;
        }

        public void render(Graphics g) {
            if (blinkState)
                return;
            super.render(g);
            /*
             * if (false && lineWidth > 1) { g.setColor(Color.black);
             * int[] x = null, y = null;
             * 
             * int size = fatLines.size(); for (int i = 0; i < size;
             * i+=2) { x = (int[])(fatLines.elementAt(i)); y =
             * (int[])(fatLines.elementAt(i+1)); g.drawPolygon(x, y,
             * x.length); } }
             */
        }

        public RoadObject getRoadObject() {
            return RoadLine.this;
        }

        public RoadLine getRoadLine() {
            return RoadLine.this;
        }
    }
}