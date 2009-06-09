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

package com.bbn.openmap.graphicLoader.scenario;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Iterator;

import com.bbn.openmap.layer.location.URLRasterLocation;
import com.bbn.openmap.omGraphics.OMArrowHead;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.time.TemporalPoint;
import com.bbn.openmap.omGraphics.time.TemporalPointSupport;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A ScenarioPoint is a ScenarioGraphic representing an object on the map. It
 * takes TimeStamps and works out its location for a specific time, or it can
 * also represent its plan for the entire scenario. If the URL provided for the
 * point is not available, it will describe itself as an OMPoint. ScenarioPoint
 * is a Location, however, so any location marker OMGraphic can be used. Make
 * sure setGraphicLocations is updated if the marker OMGraphic is something
 * other than OMPoint or OMRaster.
 */
public class ScenarioPoint extends URLRasterLocation implements ScenarioGraphic {

    /**
     * A list of points where this point should be.
     */
    protected TemporalPointSupport timeStamps;

    /**
     * A list of graphics to be used to render this point, and any other
     * symbology it needs during the scenario.
     */
    protected OMGraphicList renderList;

    /**
     * The radius of OMPoints, if icons are not found.
     */
    protected int radius = 5;

    /**
     * The resolution of segments to use for interpolating between activity
     * points. Default is 100.
     */
    protected int pathStep = 100;

    protected boolean DEBUG = Debug.debugging("scenariopoint");

    /**
     * Create a ScenarioLocation at a latitude/longtude location.
     * 
     * @param name the identifier for the location.
     * @param iconURL a string to a URL for an image
     */
    public ScenarioPoint(String name, String iconURL) {
        super(90f, -180f, name.intern(), iconURL);
        init();
    }

    /**
     * Create a ScenarioLocation at a latitude/longitude location.
     * 
     * @param name the identifier for the location.
     * @param iconURL a URL for an image
     */
    public ScenarioPoint(String name, URL iconURL) {
        // The latitude/longitude location is ignored,
        // the timestamp locations will be used.
        super(90f, -180f, name.intern(), iconURL);
        init();
    }

    protected void init() {
        timeStamps = new TemporalPointSupport();
        showName = false;
        renderList = new OMGraphicList();
    }

    /**
     * Checks the internal name against the given string.
     */
    public boolean thisIsYou(String n) {
        return name == n.intern();
    }

    /**
     * Add a TimeStamp to the point.
     */
    public void addTimeStamp(TemporalPoint timeStamp) {
        timeStamps.add(timeStamp);
        setNeedToRegenerate(true);
    }

    public boolean removeTimeStamp(TemporalPoint timeStamp) {
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
        return timeStamps.getPosition(time, false);
    }

    /**
     * Prepare the ScenarioPoint to be rendered in its position at a certain
     * time.
     */
    public void generate(Projection p, long time, boolean showScenario) {

        if (renderList.isEmpty()) {
            generateTotalScenario(p);
        }

        renderList.setVisible(showScenario);

        if (DEBUG) {
            Debug.output("ScenarioPoint (" + getName()
                    + ") calculating snapshot location.");
        }

        setPosition(time);

        generate(p);
        setNeedToRegenerate(false);
    }

    /**
     * Given a new latitude/longitude, reposition the graphic and label.
     */
    public void setGraphicLocations(float latitude, float longitude) {
        if (location instanceof OMPoint) {
            OMPoint point = (OMPoint) location;
            point.set(latitude, longitude);
            point.setOval(true);
            point.setRadius(radius);
            point.setFillPaint(getFillPaint());
            point.setLinePaint(getLinePaint());

            label.setLat(latitude);
            label.setLon(longitude);
            setHorizontalLabelBuffer(((OMPoint) location).getRadius() + SPACING);
        } else {
            super.setGraphicLocations(latitude, longitude);
        }
    }

    /**
     * Prepare the ScenarioPoint to render its entire scenario performance.
     */
    public void generateTotalScenario(Projection p) {
        renderList.clear();
        OMGraphicList icons = new OMGraphicList();

        Image image = null;
        if (location instanceof OMRaster) {
            image = ((OMRaster) location).getImage();
        }

        double lastLat = 0f;
        double lastLon = 0f;
        boolean firstLine = true;
        synchronized (timeStamps) {
            Iterator<TemporalPoint> it = timeStamps.iterator();
            while (it.hasNext()) {
                TemporalPoint ts = it.next();
                Point2D pnt = ts.getLocation();

                if (false && image != null) {
                    if (DEBUG) {
                        Debug.output("ScenarioPoint (" + getName()
                                + ") represented by image");
                    }
                    OMRaster icon = new OMRaster(pnt.getY(), pnt.getX(), -((OMRaster) location).getWidth() / 2, -((OMRaster) location).getHeight() / 2, image);
                    icon.generate(p);
                    icons.add(icon);
                } else {
                    if (DEBUG) {
                        Debug.output("ScenarioPoint (" + getName()
                                + ") represented by OMPoint");
                    }
                    OMPoint point = new OMPoint(pnt.getY(), pnt.getX(), 2);
                    point.setOval(true);
                    point.setFillPaint(getSelectPaint());
                    point.setLinePaint(getSelectPaint());
                    point.generate(p);
                    icons.add(point);
                }

                if (firstLine) {
                    lastLat = pnt.getY();
                    lastLon = pnt.getX();
                    firstLine = false;
                } else {
                    double currentLat = pnt.getY();
                    double currentLon = pnt.getX();

                    OMLine path = new OMLine(lastLat, lastLon, currentLat, currentLon, OMGraphic.LINETYPE_GREATCIRCLE);
                    path.addArrowHead(OMArrowHead.ARROWHEAD_DIRECTION_FORWARD,
                            95,
                            2,
                            8);
                    path.setLinePaint(getSelectPaint());
                    path.generate(p);
                    renderList.add(path);

                    lastLat = currentLat;
                    lastLon = currentLon;
                }
            }
        }

        renderList.add(icons);

        setVisible(true);
        setNeedToRegenerate(false);
    }

    public boolean generate(Projection p) {
        super.generate(p);
        return renderList.generate(p);
    }

    public void render(Graphics g) {
        if (DEBUG) {
            Debug.output("ScenarioPoint ("
                    + getName()
                    + ") "
                    + (isVisible() ? "is " : "is not ")
                    + "visible, "
                    + (getNeedToRegenerate() ? "needs regeneration."
                            : "all set"));
        }
        if (isVisible() && !getNeedToRegenerate()) {
            renderList.render(g);
            super.render(g);
        }
    }
}