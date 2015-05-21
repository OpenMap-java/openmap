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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadPoint.java,v
// $
// $RCSfile: RoadPoint.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class RoadPoint extends Waypoint implements RoadObject {

    private Road road;

    public RoadPoint(Road road, LatLonPoint loc, RoadLayer layer) {
        super(loc, layer);
        this.road = road;
    }

    public RoadPoint(Road road, Intersection intersection) {
        super(intersection.location, intersection.layer);
        this.road = road;
    }

    public static Class getGraphicClass() {
        return Graphic.class;
    }

    public Road getRoad() {
        return road;
    }

    public void delete() {
        road.deleteRoadPoint(this);
    }

    public void update() {
        super.update();
        road.updateLines();
    }

    public void render(OMGraphicList gl, boolean forceNew) {
        RoadGraphic visual = getVisual();
        if (visual == null || forceNew) {
            visual = new Graphic();
            setVisual(visual);
        }
        gl.add((Graphic) visual);
    }

    public class Graphic extends Waypoint.Graphic {
        public Graphic() {
            super(2);
        }

        public RoadObject getRoadObject() {
            return getRoadPoint();
        }

        public RoadPoint getRoadPoint() {
            return RoadPoint.this;
        }
    }

    public String toString() {
        return "RoadPoint on road " + road + ", " + super.toString();
    }
}

