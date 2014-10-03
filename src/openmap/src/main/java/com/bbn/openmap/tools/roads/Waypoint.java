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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/Waypoint.java,v
// $
// $RCSfile: Waypoint.java,v $
// $Revision: 1.5 $
// $Date: 2006/02/16 16:22:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.coords.LatLonPoint;

public abstract class Waypoint extends Visual implements Serializable {

    /**
     * The location of this waypoint.
     */
    protected LatLonPoint location;

    protected Point screenLocation = null;

    /**
     * True if this waypoint has modified since it was created or written.
     */
    private boolean modified = false;

    /**
     * create a Waypoint at a given location.
     * 
     * @param loc the location of the Waypoint
     */
    public Waypoint(LatLonPoint loc, RoadLayer layer) {
        location = loc;
        this.layer = layer;
    }

    public static Class getGraphicClass() {
        return Graphic.class;
    }

    /**
     * Set the modified flag
     */
    public void setModified(boolean newValue) {
        modified = newValue;
    }

    /**
     * Get the state of the modified flag.
     */
    public boolean getModified() {
        return modified;
    }

    /**
     * Get the location of this Waypoint.
     * 
     * @return the location of this Waypoint.
     */
    public LatLonPoint getLocation() {
        return location;
    }

    /**
     * Set the location of this Waypoint.
     * 
     * @param loc the new location.
     */
    public void setLocation(LatLonPoint loc) {
        location = loc;
        update();
    }

    public Point getScreenLocation() {
        if (screenLocation == null)
            screenLocation = (Point) getRoadLayer().getProjection()
                    .forward(location, new Point());
        return screenLocation;
    }

    public void setScreenLocation(Point loc) {
        setLocation(getRoadLayer().getProjection().inverse(loc.x,
                loc.y,
                new LatLonPoint.Double()));
    }

    public void update() {
        super.update();
        screenLocation = null;
    }

    // /**
    // * Get the OMGraphic for this Waypoint.
    // * @return the visual OMGraphic. Create it if necessary
    // */
    // public OMGraphic getOMGraphic(Projection p) {
    // if (visual == null) {
    // OMGraphicList gl = new OMGraphicList(1);
    // render(gl, p, true);
    // }
    // return visual;
    // }

    /**
     * Get the RoadLayer of which this is a part.
     * 
     * @return the RoadLayer.
     */
    public RoadLayer getRoadLayer() {
        return layer;
    }

    /**
     * Move this Intersection a distance on the screen.
     */
    public void moveTo(Point loc) {
        setScreenLocation(loc);
    }

    public float getLatitude() {
        return getLocation().getLatitude();
    }

    public float getLongitude() {
        return getLocation().getLongitude();
    }

    /**
     * Add the visual representation of this Waypoint to the graphics list. Our
     * visual representation is just a rectangle with radius 2.
     */
    public abstract void render(OMGraphicList gl, boolean projectionIsNew);

    public abstract class Graphic extends OMRect implements RoadGraphic {
        Logger logger = Logger.getLogger(this.getClass().getName());
        private boolean blinkState = false;

        protected Graphic(int radius) {
            super(Waypoint.this.getLocation().getLatitude(),
                  Waypoint.this.getLocation().getLongitude(),
                  -radius,
                  -radius,
                  radius,
                  radius);
            setLinePaint(Color.black);
        }

        public void blink(boolean newState) {
            blinkState = newState;
        }

        public void render(java.awt.Graphics g) {
            if (!blinkState)
                super.render(g);
        }

        public abstract RoadObject getRoadObject();
    }

    public String toString() {
        return "Waypoint : location " + location;
    }
}
