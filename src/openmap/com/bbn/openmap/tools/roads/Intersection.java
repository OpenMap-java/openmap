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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/Intersection.java,v
// $
// $RCSfile: Intersection.java,v $
// $Revision: 1.3 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class Intersection extends Waypoint implements RoadObject {
    /**
     * The list of roads at this intersection.
     */
    Vector roads = new Vector(4);

    /**
     * The "name" of this intersection. Intersections are named after
     * their coordinates. Coordinates (in degrees) are multiplied by
     * 10000000 and truncated to integers.
     */
    private String name;

    private boolean displayAsTerminal = false;

    public static int GRID = 100000;//was 100000

    /**
     * Create an Intersection at a given location.
     * 
     * @param loc the location of the intersection.
     */
    public Intersection(LatLonPoint loc, RoadLayer layer) {
        this(loc, getLatLonPointName(loc), layer);
    }

    public Intersection(LatLonPoint loc, String name, RoadLayer layer) {
        super(loc, layer);
        this.name = name;
    }

    public static Class getGraphicClass() {
        return Graphic.class;
    }

    public static String getLatLonPointName(LatLonPoint loc) {
        StringBuffer buf = new StringBuffer(24);
        buf.append((int) (loc.getY() * GRID));
        buf.append(",");
        buf.append((int) (loc.getX() * GRID));
        return new String(buf.toString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Add a road to this intersection.
     * 
     * @param road The road to be added.
     */
    public void addRoad(Road road) {
        roads.addElement(road);
    }

    public void removeRoad(Road road) {
        roads.removeElement(road);
    }

    public int getRoadCount() {
        return roads.size();
    }

    /**
     * Merge another intersection with this one. All the roads of the
     * other intersection are removed and added onto this.
     */
    public void merge(Intersection other) {
        for (Enumeration e = other.getRoads(); e.hasMoreElements();) {
            Road road = (Road) e.nextElement();
            road.changeIntersection(other, this);
            addRoad(road);
        }
        other.clear();
        setModified(true);
    }

    public void clear() {
        roads.setSize(0);
    }

    /**
     * Get an enumeration of the roads at this intersection.
     */
    public Enumeration getRoads() {
        return roads.elements();
    }

    public Road getRoad(int ix) {
        return (Road) roads.elementAt(ix);
    }

    public void setTerminalStatus(boolean yes) {
        displayAsTerminal = yes;
        update();
    }

    public boolean getTerminalStatus() {
        return displayAsTerminal;
    }

    /**
     * Render the graphics for this intersection.
     */
    public void render(OMGraphicList gl, boolean forceNew) {
        RoadGraphic visual = getVisual();
        if (visual == null || forceNew) {
            visual = new Graphic(displayAsTerminal);
            setVisual(visual);
        }
        gl.add((Graphic) visual);
    }

    /**
     * Render the graphics for the roads leaving this intersection.
     */
    public void renderRoads(OMGraphicList gl, boolean forceNew) {
        for (Enumeration e = roads.elements(); e.hasMoreElements();) {
            Road road = (Road) e.nextElement();
            if (road.getFirstIntersection() == this)
                road.render(gl, forceNew);
        }
    }

    /**
     * Override equals so that two Intersections at the same location
     * are the same Intersection.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Intersection other = (Intersection)obj;
        return other.getLocation().equals(getLocation());
    }

    /**
     * Override hashCode so that two Intersections at the same
     * location have the same hashcode.
     */
    public int hashCode() {
        LatLonPoint llp = getLocation();
        int hc1 = Float.floatToIntBits(llp.getLatitude());
        int hc2 = Float.floatToIntBits(llp.getLongitude());
        return hc1 ^ (hc2 << 5) ^ (hc2 >>> 27);
    }

    public Intersection startMove(boolean shifted) {
        if (shifted) {
            RoadLayer layer = getRoadLayer();
            Road road = layer.createRoad(this);
            return road.getOtherIntersection(this);
        } else {
            return this;
        }
    }

    public void update() {
        super.update();
        for (Enumeration e = roads.elements(); e.hasMoreElements();) {
            ((Road) e.nextElement()).updateLines();
        }
    }

    public String toString() {
        return super.toString() + "[" + getName() + ","
                + getLocation().toString() + "] " + getRoadCount() + " roads";
    }

    /**
     * Inner class for the visual representation of an Intersection.
     * The visual representation of the the Waypoint base class is
     * extended so that the move method can also update the roads
     * connected to the intersection.
     */

    public class Graphic extends Waypoint.Graphic implements RoadGraphic {
        Graphic(boolean displayAsTerminal) {
            super(displayAsTerminal ? 5 : 3);
            if (displayAsTerminal)
                setLinePaint(Color.red);
        }

        public RoadObject getRoadObject() {
            return Intersection.this;
        }

        public Intersection getIntersection() {
            return Intersection.this;
        }

        public String toString() {
            return super.toString() + "[" + Intersection.this.toString() + "]";
        }
    }
}