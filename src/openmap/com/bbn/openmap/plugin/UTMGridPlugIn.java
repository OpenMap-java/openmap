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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/UTMGridPlugIn.java,v $
// $RCSfile: UTMGridPlugIn.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.plugin;

import com.bbn.openmap.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.geom.*;
import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.quadtree.QuadTree;

import java.awt.Color;
import java.awt.Paint;
import java.util.Properties;
import java.util.Vector;

/**
 * The UTMGridPlugIn renders UTM Zone areas, and renders a grid
 * marking equal-distance areas around the center of the current
 * projection.  This distance grid only extends east-west for 500km in
 * both directions from the center of the current zone because that is
 * the extent of accuracy for those measurements - after that, you get
 * too far away from the central meridian for the current UTM zone. <p>
 *
 * Currently, this plugin only draws 100km distance squares.  Updates
 * on the way.  The plugin has the following properties that may be
 * set:<p>
 * <pre>
 *
 * # Turn zone area labels on when zoomed in closer than 1:33M (true
 * # is default)
 * showLabels=true
 * # Color for UTM Zone area boundaries
 * utmGridColor=hex AARRGGBB value
 * # Color for the distance area grid lines
 * distanceGridColor= hex AARRGGBB value
 * </pre>
 */
public class UTMGridPlugIn extends OMGraphicHandlerPlugIn {

    protected boolean UTM_DEBUG = false;
    protected boolean UTM_DEBUG_VERBOSE = false;

    public final static int INTERVAL_100K = 100000;

    protected boolean showLabels = true;
    protected Paint utmGridPaint = Color.black;
    protected Paint distanceGridPaint = Color.black;

    public final static String ShowLabelsProperty = "showLabels";
    public final static String UTMGridColorProperty = "utmGridColor";
    public final static String DistanceGridColorProperty = "distanceGridColor";
    

    public UTMGridPlugIn() {
	UTM_DEBUG = Debug.debugging("utmgrid");
	UTM_DEBUG_VERBOSE = Debug.debugging("utmgrid_verbose");
    }

    protected OMGeometryList createUTMZoneVerticalLines() {

	OMGeometryList verticalList = new OMGeometryList();
	float[] points = null;

	for (int lon = -180; lon < 180; lon += 6) {
	    if (lon == 6) {
		points = new float[] {56f, lon, -80f, lon};
	    } else if (lon > 6 && lon < 42) {
		points = new float[] {72f, lon, -80f, lon};
	    } else {
		points = new float[] {84f, lon, -80f, lon};
	    }
	    verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));
	}

	points = new float[] {72f, 6f, 64f, 6f};
	verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

	points = new float[] {64f, 3f, 56f, 3f};
	verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

	points = new float[] {84f, 9f, 72f, 9f};
	verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

	points = new float[] {84f, 21f, 72f, 21f};
	verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

	points = new float[] {84f, 33f, 72f, 33f};
	verticalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE));

	verticalList.setLinePaint(utmGridPaint);
	
	return verticalList;
    }

    protected OMGeometryList createUTMZoneHorizontalLines() {
	OMGeometryList horizontalList = new OMGeometryList();
	float[] points = null;

	for (int lat = -80; lat <= 72; lat += 8) {
	    points = new float[] {lat, -180f, lat, -90f, lat, 0f, lat, 90f, lat, 180f};
	    horizontalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB));
	}

	points = new float[] {84f, -180f, 84f, -90f, 84f, 0f, 84f, 90f, 84f, 180f};
	horizontalList.add(new PolylineGeometry.LL(points, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB));

	horizontalList.setLinePaint(utmGridPaint);

	return horizontalList;
    }

    protected QuadTree createUTMZoneLabels() {

	QuadTree labelTree = new QuadTree();

	UTMPoint utm = new UTMPoint();
	LatLonPoint llp = new LatLonPoint();
	float latitude;
	float longitude;

	for (int lat = -80; lat <= 72; lat += 8) {
	    for (int lon = -180; lon < 180; lon += 6) {

		latitude = (float) lat;
		longitude = (float) lon;

		if (lat == 56 && lon == 6) {
		    longitude = 3f;
		} else if (lat == 72 && (lon > 0 && lon < 42)) {
		    continue;
		}
		llp.setLatLon(latitude, longitude);
		addLabel(llp, UTMPoint.LLtoUTM(llp,utm), labelTree);
	    }
	}
    
	latitude = 72f;
	llp.setLatLon(latitude, 9f);
	addLabel(llp, UTMPoint.LLtoUTM(llp,utm), labelTree);
	llp.setLongitude(21f);
	addLabel(llp, UTMPoint.LLtoUTM(llp,utm), labelTree);
	llp.setLongitude(33f);
	addLabel(llp, UTMPoint.LLtoUTM(llp,utm), labelTree);

	return labelTree;
    }

    protected void addLabel(LatLonPoint llp, UTMPoint utm, QuadTree labelTree) {
	float latitude = llp.getLatitude();
	float longitude = llp.getLongitude();
	labelTree.put(latitude, longitude, new OMText(latitude, longitude, 2, -2, new String(utm.zone_number + "" + utm.zone_letter), OMText.JUSTIFY_LEFT));
    }

    protected OMGraphicList createEquiDistanceLines(UTMPoint utm, int gridLineInterval) {

	OMGraphicList list = new OMGraphicList();

	// Used to calculate the endpoints of the horizontal lines.
	UTMPoint utm1 = new UTMPoint(utm);
	UTMPoint utm2 = new UTMPoint(utm);
	LatLonPoint point1 = new LatLonPoint();
	LatLonPoint point2 = new LatLonPoint();

	// Used to calculate the pieces of the vertical lines.
	UTMPoint utmp = new UTMPoint(utm);
	LatLonPoint llp = new LatLonPoint();

	int i;
	OMLine line;
	OMPoly poly;

	float lat2;
 	int endNorthing = (int) Math.floor(utm.northing/INTERVAL_100K) + 10;
 	int startNorthing = (int) Math.floor(utm.northing/INTERVAL_100K) - 10;

	int numVertLines = 9;
	int numHorLines = endNorthing - startNorthing;

	float[][] vertPoints = new float[numVertLines][numHorLines * 2];

	if (UTM_DEBUG_VERBOSE) {
	    Debug.output("Array is [" + vertPoints.length + "][" + vertPoints[0].length + "]");
	}

	int coordCount = 0;
	boolean doPolys = true;

	utm1.easting = INTERVAL_100K;
	utm2.easting = 9 * INTERVAL_100K;

	// Horizontal lines
	for (i = startNorthing; i < endNorthing; i++) {
	    utm1.northing = (float) i * gridLineInterval;
	    utm2.northing = utm1.northing;
	    utmp.northing = utm1.northing;

	    if (doPolys) {
		for (int j = 0; j < numVertLines; j++) {
		    utmp.easting = (float) (j+1) * gridLineInterval;
		    llp = utmp.toLatLonPoint(Ellipsoid.WGS_84, llp);

		    vertPoints[j][coordCount] = llp.getLatitude();
		    vertPoints[j][coordCount+1] = llp.getLongitude();

		    if (UTM_DEBUG_VERBOSE) {
			Debug.output("for vline " + j + ", point " + i +
				     ", easting: " + utmp.easting + 
				     ", northing: " + utmp.northing + 
				     ", lat:" + vertPoints[j][coordCount] + 
				     ", lon:" + vertPoints[j][coordCount+1] );
		    }
		}
		coordCount+=2;
	    }

	    point1 = utm1.toLatLonPoint(Ellipsoid.WGS_84, point1);
	    point2 = utm2.toLatLonPoint(Ellipsoid.WGS_84, point2);

	    lat2 = point1.getLatitude();

	    if (lat2 < 84f) {
		line = new OMLine(point1.getLatitude(), point1.getLongitude(),
				  point2.getLatitude(), point2.getLongitude(),
				  OMGraphic.LINETYPE_GREATCIRCLE);
		line.setLinePaint(distanceGridPaint);
  		list.add(line);
	    }
	}


	if (doPolys) {
	    for (i = 0; i < vertPoints.length; i++) {
		if (UTM_DEBUG_VERBOSE) {
		    for (int k = 0; k < vertPoints[i].length; k += 2) {
			System.out.println(" for poly " + i + ": lat = " + 
					   vertPoints[i][k] + ", lon = " +
					   vertPoints[i][k+1]);
		    }
		}
		poly = new OMPoly(vertPoints[i], OMGraphic.DECIMAL_DEGREES, 
				  OMGraphic.LINETYPE_GREATCIRCLE);
		poly.setLinePaint(distanceGridPaint);
		list.add(poly);
	    } 
	} else {

	    // This doesn't seem to calculate the right 
	    // lines, although it looks like it should.

	    if (UTM_DEBUG) {
		Debug.output("Doing vertical lines");
	    }

	    utm1.northing = startNorthing;
	    utm2.northing = endNorthing;

	    // Vertical lines
	    for (i = 1; i <= 9; i++) {
		utm1.easting = i * 100000f;
		utm2.easting = i * 100000f;

		point1 = utm1.toLatLonPoint(Ellipsoid.WGS_84, point1);
		point2 = utm2.toLatLonPoint(Ellipsoid.WGS_84, point2);

		line = new OMLine(point1.getLatitude(), point1.getLongitude(),
				  point2.getLatitude(), point2.getLongitude(),
				  OMGraphic.LINETYPE_GREATCIRCLE);
		line.setLinePaint(distanceGridPaint);
		list.add(line);
	    }
	}

	return list;
    }

    protected QuadTree labelTree;
    protected OMGraphicList labelList;
    protected OMGraphicList verticalList;
    protected OMGraphicList horizontalList;

    /**
     * The getRectangle call is the main call into the PlugIn module.
     * The module is expected to fill the graphics list with objects
     * that are within the screen parameters passed.
     *
     * @param p projection of the screen, holding scale, center
     * coords, height, width.
     */
    public OMGraphicList getRectangle(Projection p) {

	OMGraphicList list = getList();

	if (verticalList == null) {
	    verticalList = createUTMZoneVerticalLines();
	    horizontalList = createUTMZoneHorizontalLines();
	    labelTree = createUTMZoneLabels();
	}

	list.clear();

	list.add(verticalList);
	list.add(horizontalList);

	UTMPoint utm = new UTMPoint(p.getCenter());

	OMGraphicList hunKLines = createEquiDistanceLines(utm, 100000);
	list.add(hunKLines);

	if (labelList != null) {
	    labelList.clear();
	} else {
	    labelList = new OMGraphicList();
	}

	if (showLabels && p.getScale() < 33000000f) {
	    Debug.message("utmgrid", "Creating labels for map...");
	    LatLonPoint ul = p.getUpperLeft();
	    LatLonPoint lr = p.getLowerRight();

	    Vector labels = labelTree.get(ul.getLatitude(), ul.getLongitude(),
					  lr.getLatitude(), lr.getLongitude());

	    labelList.setTargets(labels);
	    list.add(labelList);
	}

	list.generate(p);
        return list;
    } //end getRectangle

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);
	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	showLabels = LayerUtils.booleanFromProperties(props, prefix + ShowLabelsProperty, showLabels);
	utmGridPaint = LayerUtils.parseColorFromProperties(props, prefix + UTMGridColorProperty, utmGridPaint);
	distanceGridPaint = LayerUtils.parseColorFromProperties(props, prefix + DistanceGridColorProperty, distanceGridPaint);
    }

    public Properties getProperties(Properties props) {
	props = super.getProperties(props);

	String prefix = PropUtils.getScopedPropertyPrefix(this);
	props.put(prefix + ShowLabelsProperty, new Boolean(showLabels).toString());
	props.put(prefix + UTMGridColorProperty, Integer.toHexString(((Color)utmGridPaint).getRGB()));
	props.put(prefix + DistanceGridColorProperty, Integer.toHexString(((Color)distanceGridPaint).getRGB()));

	return props;
    }

    public Properties getPropertyInfo(Properties props) {
	props = super.getPropertyInfo(props);

	props.put(ShowLabelsProperty, "Show Labels for Grid Lines");
	props.put(ShowLabelsProperty + ScopedEditorProperty, 
		  "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

	props.put(UTMGridColorProperty, "Color for UTM Grid lines.");
	props.put(UTMGridColorProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

	props.put(DistanceGridColorProperty, "Color for Equal-Distance Grid lines.");
	props.put(DistanceGridColorProperty + ScopedEditorProperty, 
		 "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");

	return props;
    }
}
