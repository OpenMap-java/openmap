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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/ScenarioPoint.java,v $
// $RCSfile: ScenarioPoint.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.time;

import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.Projection;

/**
 * A ScenarioPoint is a ScenarioGraphic representing an object on the map. It
 * takes TimeStamps and works out its location for a specific time, or it can
 * also represent its plan for the entire scenario. If the URL provided for the
 * point is not available, it will describe itself as an OMPoint. ScenarioPoint
 * is a Location, however, so any location marker OMGraphic can be used. Make
 * sure setGraphicLocations is updated if the marker OMGraphic is something
 * other than OMPoint or OMRaster.
 */
public class TemporalOMPoint extends OMPoint implements TemporalOMGraphic {

    protected Object id;

    /**
     * A list of points where this point should be.
     */
    protected TemporalPointSupport timeStamps;

    /**
     * Flag to indicate that intermediate positions between locations should be
     * interpolated.
     */
    protected boolean interpolate = false;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public boolean isInterpolate() {
        return interpolate;
    }

    public void setInterpolate(boolean interpolate) {
        this.interpolate = interpolate;
    }

    /**
     * Create a ScenarioLocation at a latitude/longitude location.
     * 
     * @param id the identifier for the location.
     */
    public TemporalOMPoint(Object id, int renderType, boolean interpolate) {
        this.id = id;
        this.interpolate = interpolate;
        setRenderType(renderType);
        timeStamps = new TemporalPointSupport(renderType);
    }

    /**
     * Checks the internal id against the given one.
     */
    public boolean thisIsYou(Object n) {
        return id.equals(n);
    }

    /**
     * Add a TimeStamp to the point.
     */
    public void addTimeStamp(TemporalRecord timeStamp) {
        timeStamps.add(timeStamp);
        setNeedToRegenerate(true);
    }

    public boolean removeTimeStamp(TemporalRecord timeStamp) {
        return timeStamps.remove(timeStamp);
    }

    public void clearTimeStamps() {
        timeStamps.clear();
    }

    /**
     * Given a time, figure out the location. If the time is before the earliest
     * time or after the latest time, the location will be set to the first or
     * last known location, but the marker will made invisible. If the time is
     * in between the first and last time, the position will be interpolated.
     */
    public TemporalPoint setPosition(long time) {
        return timeStamps.getPosition(time, interpolate);
    }

    /**
     * Prepare the ScenarioPoint to be rendered in its position at a certain
     * time.
     */
    public void generate(Projection p, long time) {

        TemporalPoint tp = setPosition(time);

        if (tp == null) {
            return;
        }

        Point2D pt = tp.getLocation();
        switch (renderType) {
        case RENDERTYPE_XY:
            set((int) pt.getX(), (int) pt.getY());
            break;
        default:
            set(pt.getY(), pt.getX());
        }

        super.generate(p);
    }
}