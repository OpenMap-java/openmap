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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMDistance.java,v $
// $RCSfile: OMDistance.java,v $
// $Revision: 1.4 $
// $Date: 2003/11/14 20:50:27 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.omGraphics;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;

import java.awt.*;
import java.awt.geom.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * OMGraphic object that represents a polyline, labeled with distances.
 */
public class OMDistance extends OMPoly {

    protected OMGraphicList labels = new OMGraphicList();
    protected OMGraphicList points = new OMGraphicList();

    protected transient Length distUnits = Length.NM;
    public DecimalFormat df = new DecimalFormat("0.#");

    /**
     * Construct a default OMDistance.
     */
    public OMDistance() {
        super();
	setRenderType(RENDERTYPE_LATLON);
    }

    /**
     * Create an OMDistance from a list of float lat/lon pairs.
     * <p>
     * NOTES:
     * <ul>
     * <li>llPoints array is converted into radians IN PLACE for more
     * efficient handling internally if it's not already in radians!
     * For even better performance, you should send us an array
     * already in radians format!
     * <li>If you want the poly to be connected (as a polygon), you
     * need to ensure that the first and last coordinate pairs are the
     * same.
     * </ul>
     *
     * @param llPoints array of lat/lon points, arranged lat, lon,
     * lat, lon, etc.
     * @param units radians or decimal degrees.  Use OMGraphic.RADIANS
     * or OMGraphic.DECIMAL_DEGREES
     * @param lType line type, from a list defined in OMGraphic.
     */
    public OMDistance(float[] llPoints, int units, int lType, Length distanceUnits) {
	this(llPoints, units, lType, -1, distanceUnits);
    }

    /**
     * Create an OMDistance from a list of float lat/lon pairs.
     * <p>
     * NOTES:
     * <ul>
     * <li>llPoints array is converted into radians IN PLACE for more
     * efficient handling internally if it's not already in radians!
     * For even better performance, you should send us an array
     * already in radians format!
     * <li>If you want the poly to be connected (as a polygon), you
     * need to ensure that the first and last coordinate pairs are the
     * same.
     * </ul>
     *
     * @param llPoints array of lat/lon points, arranged lat, lon,
     * lat, lon, etc.
     * @param units radians or decimal degrees.  Use OMGraphic.RADIANS
     * or OMGraphic.DECIMAL_DEGREES
     * @param lType line type, from a list defined in OMGraphic.
     * @param nsegs number of segment points (only for
     * LINETYPE_GREATCIRCLE or LINETYPE_RHUMB line types, and if &lt;
     * 1, this value is generated internally)
     */
    public OMDistance(float[] llPoints, int units, int lType, int nsegs, Length distanceUnits) {
	super(llPoints, units, lType, nsegs);
	setDistUnits(distanceUnits);
    }

    /**
     * Set the Length object used to represent distances.
     */
    public void setDistUnits(Length distanceUnits) {
	distUnits = distanceUnits;
    }

    /**
     * Get the Length object used to represent distances.
     */
    public Length getDistUnits() {
	return distUnits;
    }

    public void setLocation(float[] llPoints, int units) {
	this.units = OMGraphic.RADIANS;
	if (units == OMGraphic.DECIMAL_DEGREES) {
	    ProjMath.arrayDegToRad(llPoints);
	}
	rawllpts = llPoints;
	setNeedToRegenerate(true);
	setRenderType(RENDERTYPE_LATLON);
    }

    public void createLabels() {
	labels.clear();
	points.clear();

	if (rawllpts == null) {
	    return;
	}
	if (rawllpts.length < 4) {
	    return;
	}

	Geo lastGeo = Geo.createGeo(rawllpts[0], rawllpts[1]);
	points.add(new OMPoint(ProjMath.radToDeg(rawllpts[0]), ProjMath.radToDeg(rawllpts[1]), 1));

	int l = 0;
	float cumulativeDist = 0f;
	for (int p = 2 ; p< rawllpts.length; p +=2) {
	    Geo curGeo = Geo.createGeo(rawllpts[p], rawllpts[p+1]);
	    
	    float dist = getDist(lastGeo, curGeo);
	    cumulativeDist += dist;

	    labels.add(createLabel(lastGeo, curGeo, dist, cumulativeDist, distUnits));
	    points.add(new OMPoint(ProjMath.radToDeg(rawllpts[p]), ProjMath.radToDeg(rawllpts[p+1]), 1));
	    lastGeo = curGeo;
	}
    }

    /** 
     * Get an OMText label for a segments between the given
     * lat/lon points whose given distance and
     * cumulative distance is specified.
     */
    public OMText createLabel(Geo g1, Geo g2,
			      float dist, float cumulativeDist,
			      Length distanceUnits) {
	Geo mid;
	switch (getLineType()) {
	case LINETYPE_STRAIGHT:
	    float lat = (float)(g1.getLatitude() + g2.getLatitude()) / 2f;
	    float lon = (float)(g1.getLongitude() + g2.getLongitude()) / 2f;
	    mid = new Geo(lat, lon);
	    break;
	case LINETYPE_RHUMB:
	    System.err.println("Rhumb distance calculation not implemented.");
	case LINETYPE_GREATCIRCLE:
	case LINETYPE_UNKNOWN:
	default:	
	    mid = g1.midPoint(g2);
	}
	
// 	String text = ((int)dist) + " (" + ((int)cumulativeDist) + ")";

	String text = (df.format(distanceUnits.fromRadians(dist))) + " (" + 
	    (df.format(distanceUnits.fromRadians(cumulativeDist))) + ") " +
	    distanceUnits.getAbbr();
	OMText omtext = new OMText((float)mid.getLatitude(), 
				   (float)mid.getLongitude(),
				   text,
				   OMText.JUSTIFY_LEFT);
// 	omtext.setLinePaint(new Color(200, 200, 255));
	return omtext;
    }

    /**
     * Return the distance between that lat/lons defined in radians.
     * The returned value is in radians.
     */
    public float getDist(Geo g1, Geo g2) {
	switch (getLineType()) {
	case LINETYPE_STRAIGHT:
	    float lonDist = ProjMath.lonDistance((float)g2.getLongitude(),
						 (float)g1.getLongitude());
	    float latDist = (float)g2.getLatitude() - (float)g1.getLatitude();
	    return (float)Math.sqrt(lonDist*lonDist + latDist*latDist);
	case LINETYPE_RHUMB:
	    Debug.error("Rhumb distance calculation not implemented.");
	case LINETYPE_GREATCIRCLE:
	case LINETYPE_UNKNOWN:
	default:
	    return (float)g1.distance(g2);
	}
    }

    /**
     * Prepare the poly for rendering.
     *
     * @param proj Projection
     * @return true if generate was successful
     */
    public boolean generate(Projection proj) {
	boolean ret = super.generate(proj);
	createLabels();
	labels.generate(proj);
	points.generate(proj);
	return ret;
    }

    /**
     * Flag used by the EditableOMDistance to do quick movement paints
     * in a cleaner way.
     */
    protected boolean paintOnlyPoly = false;

    /**
     * Paint the poly. 
     * This works if generate() has been successful.
     *
     * @param g java.awt.Graphics to paint the poly onto.
     */
    public void render(Graphics g) {
	super.render(g);

	if (!paintOnlyPoly) {
	    labels.setLinePaint(getLinePaint());
	    if (isMatted()) {
		labels.setFillPaint(getMattingPaint());
	    }

	    points.setLinePaint(getLinePaint());
	    points.setFillPaint(getLinePaint());

	    labels.render(g);
	    points.render(g);
	}
    }

    private void writeObject(java.io.ObjectOutputStream stream) 
	throws java.io.IOException {
        stream.defaultWriteObject();
        stream.writeObject(distUnits.getAbbr());
     }

    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        distUnits = Length.get((String)stream.readObject());
    }

}
