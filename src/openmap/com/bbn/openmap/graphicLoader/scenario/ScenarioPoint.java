// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.1 $
// $Date: 2003/06/25 20:38:09 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.graphicLoader.scenario;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.layer.location.URLRasterLocation;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A ScenarioPoint is a ScenarioGraphic representing an object on the
 * map.  It takes TimeStamps and works out its location for a specific
 * time, or it can also represent its plan for the entire scenario.  If
 * the URL provided for the point is not available, it will describe
 * itself as an OMPoint.  ScenarioPoint is a Location, however, so any
 * location marker OMGraphic can be used.  Make sure
 * setGraphicLocations is updated if the marker OMGraphic is something
 * other than OMPoint or OMRaster.
 */
public class ScenarioPoint extends URLRasterLocation implements ScenarioGraphic {

    /**
     * A list of points where this point should be.
     */
    List timeStamps;

    /**
     * A list of graphics to be used to render this point, and any
     * other symbology it needs during the scenario.
     */
    OMGraphicList renderList;

    /**
     * The radius of OMPoints, if icons are not found.
     */
    protected int radius = 5;

    /**
     * The resolution of segments to use for interpolating between
     * activity points. Default is 100.
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
     * Create a ScenarioLocation at a latitude/longtude location.
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
	timeStamps = new LinkedList();
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
    public void addTimeStamp(TimeStamp timeStamp) {
	synchronized (timeStamps) {
	    int count = 0;

	    Iterator it = timeStamps.iterator();
	    while (it.hasNext()) {
		TimeStamp ts = (TimeStamp)it.next();
		if (timeStamp.time < ts.time) {
		    break;
		}
		count++;
	    }
	    timeStamps.add(count, timeStamp);
	    if (DEBUG) {
		Debug.output("ScenarioPoint (" + getName() + ") adding " + timeStamp);
	    }
	}
	setNeedToRegenerate(true);
    }

    public boolean removeTimeStamp(TimeStamp timeStamp) {
	boolean result = false;
	synchronized (timeStamps) {
	    result = timeStamps.remove(timeStamp);
	}
	return result;
    }

    public void clearTimeStamps() {
	synchronized (timeStamps) {
	    timeStamps.clear();
	}
    }

    /**
     * Given a time, figure out the location.  If the time is before
     * the earliest time or after the latest time, the location will
     * be set to the first or last known location, but the marker will
     * made invisible.  If the time is in between the first and last
     * time, the position will be interpolated.
     */
    public void setPosition(long time) {
	TimeStamp previous = null;
	TimeStamp next = null;
	boolean POSITION_DEBUG = DEBUG;

	synchronized (timeStamps) {
	    Iterator it = timeStamps.iterator();
	    while (it.hasNext()) {
		TimeStamp ts = (TimeStamp) it.next();

		if (ts.time < time) {
		    previous = ts;
		} else if (ts.time > time) {
		    next = ts;
		    break;
		} else {
		    // Hit a time right at a position.

		    if (POSITION_DEBUG) {
			Debug.output("ScenarioPoint (" + getName() + 
				     ").setPosition(): " + 
				     ts.latitude + ", " + ts.longitude + 
				     " at time " + time);
		    }

		    setLocation(ts.latitude, ts.longitude);
		    setVisible(true);
		    return;
		}
	    }
	}

	if (previous == null) {
	    // time is before ScenarioPoint is placed.
	    setVisible(false);
	    if (next != null) {
		// Just for fun, put location where it will be.
		if (POSITION_DEBUG) {
		    Debug.output("ScenarioPoint (" + getName() + 
				 ").setPosition(): premature time, invisible at " + 
				 next.latitude + ", " + next.longitude);
		}
		setLocation(next.latitude, next.longitude);
	    } else {
		// no timestamps
		if (POSITION_DEBUG) {
		    Debug.output("ScenarioPoint (" + getName() + 
				 ").setPosition(): no TimeStamps ");
		}

		return;
	    }
	} else if (next == null) {
	    // time is after last ScenarioPoint TimeStamp
	    setVisible(false);
	    // Just for fun, place location at last timestamp.
	    if (POSITION_DEBUG) {
		Debug.output("ScenarioPoint (" + getName() + 
			     ").setPosition(): expired time, invisible at " + 
			     previous.latitude + ", " + previous.longitude);
	    }
	    setLocation(previous.latitude, previous.longitude);
	} else {
	    // Need to interpolate between the two.
	    float[] points = GreatCircle.great_circle(
		ProjMath.degToRad(previous.latitude),
		ProjMath.degToRad(previous.longitude),
		ProjMath.degToRad(next.latitude),
		ProjMath.degToRad(next.longitude), 
		pathStep, true);
	    float numSegsCovered = (float)pathStep * 
		(time - previous.time)/(float)(next.time - previous.time);
	    int index = Math.round(numSegsCovered) * 2; 
	    float la = ProjMath.radToDeg(points[index]);
	    float lo = ProjMath.radToDeg(points[index + 1]); 

	    if (POSITION_DEBUG) {
		Debug.output("ScenarioPoint (" + getName() + 
			     ").setPosition(): good time, " + 
			     (100f*numSegsCovered/pathStep) + 
			     "% to the next location, visible at " + 
			     la + ", " + lo);
	    }

	    setLocation(la, lo);
	    points = null;
	    setVisible(true);
	}
    }

    /**
     * Prepare the ScenarioPoint to be rendered in its position at a
     * certain time.
     */
    public void generateSnapshot(Projection p, long time) {
	renderList.clear();
	if (DEBUG) {
	    Debug.output("ScenarioPoint (" + getName() + 
			 ") calculating snapshot location.");
	}
	
	setPosition(time);

	if (location.isVisible() && isShowLocation()) {
	    if (DEBUG) {
		Debug.output("ScenarioPoint (" + getName() + 
			     ") adding location (" +
			     location.getClass().getName() + ")");
	    }
	    renderList.add(location);
	}

	if (label.isVisible() && isShowName()) {
	    if (DEBUG) {
		Debug.output("ScenarioPoint (" + getName() + 
			     ") adding label (" +
			     label.getData() + ")");
	    }
	    renderList.add(label);
	}

	super.generate(p);
	setNeedToRegenerate(false);
    }

    /**
     * Given a new latitude/longitude, reposition the graphic and
     * label.
     */
    public void setGraphicLocations(float latitude, float longitude) {
	if (location instanceof OMPoint) {
	    OMPoint point = (OMPoint)location;
	    point.set(latitude, longitude);
	    point.setOval(true);
	    point.setRadius(radius);
	    point.setFillPaint(getFillPaint());
	    point.setLinePaint(getLinePaint());

	    label.setLat(latitude);
	    label.setLon(longitude);
	    setHorizontalLabelBuffer(((OMPoint)location).getRadius() + SPACING);
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
	    image = ((OMRaster)location).getImage();
	}

	float lastLat = 0f;
	float lastLon = 0f;
	boolean firstLine = true;
	synchronized (timeStamps) {
	    Iterator it = timeStamps.iterator();
	    while (it.hasNext()) {
		TimeStamp ts = (TimeStamp)it.next();

		if (image != null) {
		    if (DEBUG) {
			Debug.output("ScenarioPoint (" + getName() + 
				     ") represented by image");
		    }
		    OMRaster icon = new OMRaster(ts.latitude, ts.longitude, 
						 -((OMRaster)location).getWidth()/2,
						 -((OMRaster)location).getHeight()/2, 
						 image);
		    icon.generate(p);
		    icons.add(icon);
		} else {
		    if (DEBUG) {
			Debug.output("ScenarioPoint (" + getName() + 
				     ") represented by OMPoint");
		    }
		    OMPoint point = new OMPoint(ts.latitude, ts.longitude,
						radius);
		    point.setOval(true);
		    point.setFillPaint(getFillPaint());
		    point.setLinePaint(getLinePaint());
		    point.generate(p);
		    icons.add(point);
		}

		if (firstLine) {
		    lastLat = ts.latitude;
		    lastLon = ts.longitude;
		    firstLine = false;
		} else {
		    float currentLat = ts.latitude;
		    float currentLon = ts.longitude;

		    OMLine path = new OMLine(lastLat, lastLon, 
					     currentLat, currentLon, 
					     OMGraphic.LINETYPE_GREATCIRCLE);
		    path.addArrowHead(OMArrowHead.ARROWHEAD_DIRECTION_FORWARD, 
				      95, 2, 8);
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
	    Debug.output("ScenarioPoint (" + getName() + ") " + (isVisible()?"is ":"is not ") + "visible, " + (getNeedToRegenerate()?"needs regeneration.":"all set"));
	}
	if (isVisible() && !getNeedToRegenerate()) {
	    renderList.render(g);
	}
    }
}
