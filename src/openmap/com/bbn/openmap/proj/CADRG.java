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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/CADRG.java,v $
// $RCSfile: CADRG.java,v $
// $Revision: 1.2 $
// $Date: 2003/11/14 20:56:43 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.Point;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;

/**
 * Implements the CADRG projection.
 * This is really an Equal Arc Projection with pixel spacings as dictated by
 * the RPF specification.
 */
public class CADRG extends Cylindrical implements EqualArc {

    /**
     * The CADRG name.
     */
    public final static transient String CADRGName = "CADRG";

    /**
     * The CADRG type of projection.
     */
    public final static transient int CADRGType = 42;

    public final static transient float epsilon = 0.0001f;

    // HACK -degrees
    private static final float NORTH_LIMIT = ProjMath.degToRad(80.0f);
    private static final float SOUTH_LIMIT = -NORTH_LIMIT;

    private double spps_x, spps_y; // scaled pixels per SCoord
    private static final int CADRG_ARC_A[] = { 369664, 302592, 245760, 199168,
					       163328, 137216, 110080, 82432 };
    private static final double CADRG_SCALE_LIMIT = 200.0;
    private static final int CADRG_get_zone_old_extents[] = {32, 48, 56, 64,
							     68, 72, 76, 80, 90};
    private int /*ox,*/ oy;
    private double x_pix_constant, y_pix_constant;
    private Point ul;//upper left

    private float[] lower_zone_extents;
    private float[] upper_zone_extents;
    
    private int zone;

    /**
     * Construct a CADRG projection.
     * <p>
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param w width of screen
     * @param h height of screen
     *
     */
    public CADRG(LatLonPoint center, float scale, int width, int height) {
	super(center, scale, width, height, CADRGType);
	minscale = (float)1000000/(float)CADRG_SCALE_LIMIT;
    }

 
    /**
     * Sets radian latitude to something sane.  This is an abstract
     * function since some projections don't deal well with extreme
     * latitudes.<p>
     *
     * @param lat float latitude in radians
     * @return float latitude (-PI/2 &lt;= y &lt;= PI/2)
     * @see com.bbn.openmap.LatLonPoint#normalize_latitude(float)
     *
     */
    public float normalize_latitude(float lat) {
	if (lat > NORTH_LIMIT) {
	    lat = NORTH_LIMIT;
	} else if (lat < SOUTH_LIMIT) {
	    lat = SOUTH_LIMIT;
	}
	return lat;
    }


//    protected void finalize() {
//	Debug.message("proj", "CADRG finialized");
//    }


    /**
     * Return stringified description of this projection.
     * <p>
     * @return String
     * @see Projection#getProjectionID
     *
     */
    public String toString() {
	return "CADRG[ spps_x=" + spps_x + " spps_y=" + spps_y +
	    " x_pix=" + x_pix_constant + " y_pix=" + y_pix_constant +
	    /*" ox=" + ox +*/ " oy=" + oy + " ul(" +
	    ul.x + "," + ul.y + ")" +
	    super.toString();
    }

    /** Returns the current zone of the projection.  Zone number
     * starts at 1, goes to 8, per the RPF specification.  We don't
     * handle zone 9 (polar).
     *
     * @return the zone of the projection.
     * */
    public int getZone() {
	return zone;
    }

    /**
     * Given a letter for a zone, return the CADRG zone equivalent,
     */
    public static int getProjZone(char asciiZone) {
	int z = (int) asciiZone;
	
	if (z == 74) z--; // Fix J to a zone.
	if (z > 64) z -= 64; // Below the equator
	else z -= 48; // Above the equator

	// Now we should have a number, of a zone 1-9
	return z;
    }

    /**
     * Get the planet pixel circumference.
     * @return float circumference of planet in pixels
     */
    public float getPlanetPixelCircumference() {
	// Why this algorithm?  Well, the CADRG_ARC_A is a pixel count
	// that needs to be multiplied by 1000000 to normalize it
	// against the 1:1M factor reflected in the array values.  The
	// 1.5 factor was tossed in there because it was showing up in
	// other calculations as that 100/150 thing.  It works in tests.

  	return (1000000 * (float)CADRG_ARC_A[zone - 1])/1.5f;
	// These are the same things...
// 	return (float)getXPixConstant() * scale;

	// This is what the default return value is from the super
	// class.
// 	return planetPixelCircumference; // the standard return for projections...
    }

    /**
     * Returns the zone based on the y_pix_constant and a latitude.
     * <p>
     * HACK: latitude in decimal degrees DO THE CONSTANTS
     * DEPEND ON THIS?!!
     * <p>
     * @param lat latitude
     * @param y_pix_constant pixel constant
     *
     */
    private int getZone(double lat, double y_pix_constant) {
	int NOT_SET = -1;
	int ret = NOT_SET;

	double delta;
	double upper_lat, lower_lat;
	int x;
	double pivot;
	/** Pixels per degree */
	double ppd = y_pix_constant/90;
	
	if (upper_zone_extents == null) {
	    upper_zone_extents = new float[CADRG_get_zone_old_extents.length];
	}
	if (lower_zone_extents == null) {
	    lower_zone_extents = new float[CADRG_get_zone_old_extents.length + 1];
	}

	/** Delta*2 is the number of degrees for the height of the
	 *  projection. */
	if (y_pix_constant == 0)
	    delta = 0;
	else
	    delta = height/2.0*90.0/y_pix_constant;
	Debug.message("proj", "height = " + height);

	upper_lat = Math.abs(Math.abs(lat) + delta);
	lower_lat = Math.abs(Math.abs(lat) - delta);

	Debug.message("proj", "upper_lat = " + upper_lat);
	Debug.message("proj", "lower_lat = " + lower_lat);

	lower_zone_extents[0] = 0f;
	lower_zone_extents[8] = 80f;
	upper_zone_extents[8] = 90f;
	
	// figure out new extents - from CADRG spec
	for (x = 0; x < CADRG_get_zone_old_extents.length - 1/*8*/; x++) {
	    pivot = ppd * CADRG_get_zone_old_extents[x] / 1536.0;
	    pivot = Math.floor(pivot);
	    Debug.message("proj", "pivot = " + pivot);
	    lower_zone_extents[x+1] = (float) (pivot * 1536.0 / ppd);
	    // Can't go further than the equator.
// 	    if (x == 0) lower_zone_extents[x] = 0;
	    pivot++;
	    upper_zone_extents[x] = (float) (pivot * 1536.0 / ppd);
	    Debug.message("proj", "lower_zone_extents[" + x + "] = " +
			  lower_zone_extents[x]);
	    Debug.message("proj", "upper_zone_extents[" + x + "] = " +
			  upper_zone_extents[x]);

	    if ((lower_lat <= (double)upper_zone_extents[x]) &&
		(upper_lat <= (double)upper_zone_extents[x]) && ret == NOT_SET)
		ret = x+1;
	}
	if (ret == NOT_SET) 
	    ret = CADRG_get_zone_old_extents.length - 1;

	return ret;
    }

    /**
     * Returns the x pixel constant of the projection. This was
     * calcuated when the projection was created.  Represents the
     * number of pixels around the earth (360 degrees).
     */
    public double getXPixConstant() {
	return x_pix_constant;
    }

    /**
     * Returns the y pixel constant of the projection. This was
     * calcuated when the projection was created.  Represents the
     * number of pixels from 0 to 90 degrees.
     */
    public double getYPixConstant() {
	return y_pix_constant;
    }

    /**
     * Returns the upper zone extent for the given zone at the
     * current scale. This only makes sense if the projection is at
     * the same scale as the chart data you are interested in.
     */
    public float getUpperZoneExtent(int zone) {
	if (zone < 1) zone = 1;
	if (zone > 8) zone = 9;
	return upper_zone_extents[zone-1];
    }

    /**
     * Returns the lower zone extent for the given zone at the
     * current scale. This only makes sense if the projection is at
     * the same scale as the chart data you are interested in. 
     */
    public float getLowerZoneExtent(int zone) {
	if (zone < 1) zone = 1;
	if (zone > 8) zone = 9;
	return lower_zone_extents[zone-1];
    }

    /** 
     * Return the number of horizontal frame files that will fit
     * around the world in the current zone.  This only makes sense if
     * the projection is at the same scale as the chart data you are
     * interested in.
     *
     * @return number of frame columes in the current zone, to go
     * around the world.  
     */
    public int numHorizontalFrames() {
	return (int) Math.ceil(x_pix_constant/(1536.0));
    }

    /** 
     * Return the number of vertical frame files that will fit within
     * the current zone, overlaps included.  This only makes sense if
     * the projection is at the same scale as the chart data you are
     * interested in.
     *
     * @return number of frame rows in the current zone.
     */
    public int numVerticalFrames() {
	return (int) Math.round((upper_zone_extents[zone-1] - 
				 lower_zone_extents[zone-1]) *
				(y_pix_constant/90.0)/(1536.0));
    }
    
    /** Figures out the number of pixels around the earth, for 360
     * degrees.
     * <p>
     * @param adrgscale The scale adjusted to 1:1M (1M/real scale)
     * @param zone ADRG zone
     * @return The number of pixels around the equator (360 degrees)
     * */
    private double CADRG_x_pix_constant(double adrgscale, int zone) {
	// E-W pixel constant
	double tempx = 0;
	double x_pix = (double) adrgscale * CADRG_ARC_A[zone-1] / 512.0;
      
        // Increase, if necessary, to the next highest integer value
	x_pix = Math.ceil(x_pix);
	x_pix = x_pix * 1.33333;//(512*100)/(150*256);

        // Round the final result.
	x_pix = Math.round(x_pix);

	return x_pix*256.0;
    }


    /**
     * Calculate the maximum allowable scale.
     * <p>
     * @return float maxscale
     *
     */
    private float CADRG_calc_maxscale() {
	// Why 1.5?  It was 150/100?  Why?
	return (1000000 * (float)CADRG_ARC_A[0]) / (width * 1.5f);
    }


    /**
     * Returns the number of pixels from the equator to a pole.
     * <p>
     * @param adrgscale scale adjusted to 1:1M (1M/real scale)
     * @return number of pixels from 0 to 90 degrees
     *
     */
    private double CADRG_y_pix_constant(double adrgscale) {
	double tempy = 0;
	final int CADRG_ARC_B = 400384;

	double y_pix = (double) adrgscale * CADRG_ARC_B / 512.0;
      
	// Increase, if necessary, to the next highest integer value
	y_pix = Math.ceil(y_pix);

	y_pix = y_pix * 0.33333;//(512*100)/(4*150*256);

	// Round the final result.
	y_pix = Math.round(y_pix);
    
	return y_pix*256.0;
    }


    /**
     * Checks if a LatLonPoint is plot-able.
     * <p>
     * A point is plot-able in the CADRG projection if it is within the North
     * and South zone limits.
     * <p>
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(float lat, float lon) {
	lat = normalize_latitude(ProjMath.degToRad(lat));
	return ((lat-epsilon < NORTH_LIMIT) &&
		(lat+epsilon > SOUTH_LIMIT));
    }


    /**
     * Projects a point from Lat/Lon space to X/Y space.
     * <p>
     * @param pt LatLonPoint
     * @param ret_val Point retval
     * @return Point ret_val
     */
    public Point forward (LatLonPoint pt, Point ret_val) {
	float lon_ = wrap_longitude(pt.radlon_ - ctrLon);
	float lat_ = normalize_latitude(pt.radlat_);

	ret_val.x = (int) ProjMath.roundAdjust(spps_x * lon_) - ul.x;
	ret_val.y = (int) ProjMath.roundAdjust(-spps_y * lat_) + ul.y + oy;

	return ret_val;
    }
 

    /**
     * Forward projects lat,lon into XY space and returns a Point.
     * <p>
     * @param lat float latitude in radians
     * @param lon float longitude in radians
     * @param ret_val Resulting XY Point
     * @param isRadian bogus argument indicating that lat,lon
     * arguments are in radians
     * @return Point ret_val
     */
    public Point forward (float lat, float lon, Point ret_val, boolean b) {
	float lon_ = wrap_longitude(lon - ctrLon);
	float lat_ = normalize_latitude(lat);

	ret_val.x = (int) ProjMath.roundAdjust(spps_x * lon_) - ul.x;
	ret_val.y = (int) ProjMath.roundAdjust(-spps_y * lat_) + ul.y + oy;
	return ret_val;
    }


    /**
     * Forward projects lat,lon coordinates.
     * <p>
     * @param lat raw latitude in decimal degrees
     * @param lon raw longitude in decimal degrees
     * @param ret_val Resulting XY Point
     * @return Point ret_val
     */
    public Point forward (float lat, float lon, Point ret_val) {
	float lon_ = wrap_longitude(ProjMath.degToRad(lon) - ctrLon);
	float lat_ = normalize_latitude(ProjMath.degToRad(lat));

	ret_val.x = (int) ProjMath.roundAdjust(spps_x * lon_) - ul.x;
	ret_val.y = (int) ProjMath.roundAdjust(-spps_y * lat_) + ul.y + oy;
	return ret_val;
    }


    /**
     * Inverse project a Point.
     * <p>
     * @param point x,y Point
     * @param ret_val resulting LatLonPoint
     * @return LatLonPoint ret_val
     *
     */
    public LatLonPoint inverse (Point pt, LatLonPoint ret_val) {
	//Debug.output("CADRG.inverse");
	Point pixpoint = new Point(0,0);
    
	/* offset back into pixel space from Drawable space */
	pixpoint.x = pt.x + ul.x/* - ox*/;
	pixpoint.y = -pt.y + ul.y + oy;

	// Check bounds on the call (P Space). Mutate if needed.
	if (pixpoint.x > (int) ProjMath.roundAdjust(world.x/2.0)) {
	    pixpoint.x = (int) ProjMath.roundAdjust(world.x/2.0);
	}
	else if (pixpoint.x < (int) ProjMath.roundAdjust(-world.x/2.0)) {
	    pixpoint.x = (int) ProjMath.roundAdjust(-world.x/2.0);
	}
	if (pixpoint.y > (int) ProjMath.roundAdjust(world.y/2.0)) {
	    pixpoint.y = (int) ProjMath.roundAdjust(world.y/2.0);
	}
	else if (pixpoint.y < (int) ProjMath.roundAdjust(-world.y/2.0)) {
	    pixpoint.y = (int) ProjMath.roundAdjust(-world.y/2.0);
	}

	// normalize_latitude on the way out.
	float lat_ = normalize_latitude(
	    (float)((double)pixpoint.y/(double)spps_y));
	ret_val.setLatitude(ProjMath.radToDeg(lat_));
	// longitude is wrapped as usual.
	ret_val.setLongitude(ProjMath.radToDeg((float)(
	    (double)pixpoint.x/(double)spps_x) + ctrLon));

// 	// normalize_latitude on the way out.
// 	float lat_ = normalize_latitude(degToRad(((float)MoreMath.SC_TO_DEG(
// 	    (int)(ProjMath.roundAdjust((double)pixpoint.y/(double)spps_y))))));
// 	ret_val.setLatitude((float)ProjMath.radToDeg(lat_));

// 	// longitude is wrapped as usual.
// 	ret_val.setLongitude((float)MoreMath.SC_TO_DEG(
// 	    (int)(ProjMath.roundAdjust((double)pixpoint.x/(double)spps_x) +
// 		  MoreMath.DEG_TO_SC(ProjMath.radToDeg(ctrLon)))));
	
	return ret_val;
    }


    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * <p>
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param ret_val LatLonPoint
     * @return LatLonPoint ret_val
     * @see Proj#inverse(Point)
     *
     */
    public LatLonPoint inverse (int x, int y, LatLonPoint ret_val) {
	//Debug.output("CADRG.inverse");
	Point pixpoint = new Point(0,0);
    
	/* offset back into pixel space from Drawable space */
	pixpoint.x = x + ul.x/* - ox*/;
	pixpoint.y = -y + ul.y + oy;

	// Check bounds on the call (P Space). Mutate if needed.
	if (pixpoint.x > (int) ProjMath.roundAdjust(world.x/2.0)) {
	    pixpoint.x = (int) ProjMath.roundAdjust(world.x/2.0);
	}
	else if (pixpoint.x < (int) ProjMath.roundAdjust(-world.x/2.0)) {
	    pixpoint.x = (int) ProjMath.roundAdjust(-world.x/2.0);
	}
	if (pixpoint.y > (int) ProjMath.roundAdjust(world.y/2.0)) {
	    pixpoint.y = (int) ProjMath.roundAdjust(world.y/2.0);
	}
	else if (pixpoint.y < (int) ProjMath.roundAdjust(-world.y/2.0)) {
	    pixpoint.y = (int) ProjMath.roundAdjust(-world.y/2.0);
	}

	// normalize_latitude on the way out.
	float lat_ = normalize_latitude(
	    (float)((double)pixpoint.y/(double)spps_y));
	ret_val.setLatitude(ProjMath.radToDeg(lat_));
	// longitude is wrapped as usual.
	ret_val.setLongitude(ProjMath.radToDeg((float)(
	    (double)pixpoint.x/(double)spps_x) + ctrLon));
	
	return ret_val;
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" paramters
     * used in the forward() and inverse() calls.<p>
     *
     */
    protected void computeParameters() {
	int w, h;

	if (ul == null)
	    ul = new Point(0,0);	//HACK

	// quick calculate the maxscale
	maxscale = CADRG_calc_maxscale();
	if (scale > maxscale)
	    scale = maxscale;

	// Compute the "ADRG" scale, which gets used below.
	double adrgscale = 1000000.0 / scale;  // 1 million (from ADRG spec)
	if (adrgscale > CADRG_SCALE_LIMIT) {
	    Debug.message("proj", "CADRG: adrgscale > CADRG_SCALE_LIMIT");
	    adrgscale = CADRG_SCALE_LIMIT;
	}

	// Compute the y pixel constant based on scale.
	y_pix_constant = CADRG_y_pix_constant(adrgscale);
	if (Debug.debugging("proj")) {
	    Debug.output("Y pix constant = " + y_pix_constant);
	}
	// What zone are we in?
	zone = getZone(ProjMath.radToDeg(ctrLat), y_pix_constant);
	if (Debug.debugging("proj")) {
	    Debug.output("Zone = " + zone);
	}

	// Compute the x pixel constant, based on scale and zone.
	x_pix_constant = CADRG_x_pix_constant(adrgscale, zone);
	if (Debug.debugging("proj")) {
	    Debug.output("x_pix_constant = " + x_pix_constant);
	}
	// Now I can compute the world coordinate.
	if (world == null)
	    world = new Point(0,0);
	world.x = (int) ProjMath.roundAdjust(x_pix_constant);
	world.y = (int) ProjMath.roundAdjust(y_pix_constant * 4.0 / 2.0);
	Debug.message("proj", "world = " + world.x + "," + world.y);

	// Compute scaled pixels per RADIAN, not SCOORD
	spps_x = (double) x_pix_constant / MoreMath.TWO_PI/*MoreMath.DEG_TO_SC(360)*/;
	spps_y = (double) y_pix_constant / MoreMath.HALF_PI/*MoreMath.DEG_TO_SC(90)*/;
	Debug.message("proj", "spps = " + spps_x + "," + spps_y);

	// Fix the "small world" situation, computing ox, oy.
	if (width > world.x) {
	    Debug.message("proj", "CADRG: fixing small world");
	    w = world.x;
// 	    ox = (int) ProjMath.roundAdjust((width - w) / 2.0);
	}
	else {
	    w = width;
// 	    ox = 0;
	}
	if (height > world.y) {
	    h = (int) world.y;
	    oy = (int) ProjMath.roundAdjust((height - h) / 2.0);
	}
	else {
	    h = height;
	    oy = 0;
	}

	// compute the "upper left" adjustment.
	long temp = (long) ProjMath.roundAdjust(spps_y * ctrLat);
	if (Debug.debugging("proj")) {
	    Debug.output("CADRG.temp = " + temp);
	}
	if (ul == null)
	    ul = new Point(0,0);
	ul.x = (int) ProjMath.roundAdjust(-w/2.0);
	if ((temp != 0) && (oy != 0)) { 
	    ul.y = (int) ProjMath.roundAdjust(h/2.0);
	}
	else {
	    ul.y = (int) temp + (int) ProjMath.roundAdjust(h/2.0);
	}

	if (Debug.debugging("proj")) {
	    Debug.output("CADRG: ul = " + ul.x + "," + ul.y);
	    Debug.output(/*"ox = " + ox +*/ " oy = " + oy);
	}

	// Finally compute some useful cylindrical projection parameters
// 	maxscale = (CADRG_ARC_A[0] * (1000000/width));// HACK!!!
	half_world = world.x/2;
	if (scale > maxscale) {
	    scale = maxscale;
	}
// 	scaled_radius = planetPixelRadius/scale;
	Debug.message("proj",
	    "CADRG.computeParameters(): maxscale: " + maxscale);
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
	return CADRGName;
    }

    /**
     * Given a couple of points representing a bounding box, find out
     * what the scale should be in order to make those points appear
     * at the corners of the projection.
     *
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.Point reflecting a pixel spot on the
     * projection that matches the ll1 coordinate, the upper left
     * corner of the area of interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the
     * projection that matches the ll2 coordinate, usually the lower
     * right corner of the area of interest.
     */
    public float getScale(LatLonPoint ll1, LatLonPoint ll2, Point point1, Point point2) {
	return getScale(ll1, ll2, point1, point2, 0);
    }

    /**
     * Given a couple of points representing a bounding box, find out
     * what the scale should be in order to make those points appear
     * at the corners of the projection.
     *
     * @param ll1 the upper left coordinates of the bounding box.
     * @param ll2 the lower right coordinates of the bounding box.
     * @param point1 a java.awt.Point reflecting a pixel spot on the
     * projection that matches the ll1 coordinate, the upper left
     * corner of the area of interest.
     * @param point2 a java.awt.Point reflecting a pixel spot on the
     * projection that matches the ll2 coordinate, usually the lower
     * right corner of the area of interest.
     * @param recursiveCount a protective count to keep this method
     * from getting in a recursive death spiral.
     */
    private float getScale(LatLonPoint ll1, LatLonPoint ll2, Point point1, Point point2, int recursiveCount) {

	try {
	    
	    float deltaDegrees;
	    float pixPerDegree;
	    int deltaPix;
	    float ret;
	    float dx = Math.abs(point2.x - point1.x);
	    float dy = Math.abs(point2.y - point1.y);

	    float nCenterLat = Math.min(ll1.getLatitude(), ll2.getLatitude()) + Math.abs(ll1.getLatitude() - ll2.getLatitude())/2f;
	    float nCenterLon = Math.min(ll1.getLongitude(), ll2.getLongitude()) + Math.abs(ll1.getLongitude() - ll2.getLongitude())/2f;
	
	    if (dx < dy) {
		float dlat = Math.abs(ll1.getLatitude() - ll2.getLatitude());
		deltaDegrees = dlat;
		deltaPix = getHeight();
		pixPerDegree = getScale() * (float)getYPixConstant()/90f;
	    } else {
		float dlon;
		float lat1, lon1, lon2;
		
		// point1 is to the right of point2. switch the
		// LatLonPoints so that ll1 is west (left) of ll2.
		if (point1.x > point2.x) {
		    lat1 = ll1.getLatitude();
		    lon1 = ll1.getLongitude();
		    ll1.setLatLon(ll2);
		    ll2.setLatLon(lat1, lon1);
		}
		
		lon1 = ll1.getLongitude();
		lon2 = ll2.getLongitude();
		
		// allow for crossing dateline
		if (lon1 > lon2) {
		    dlon = (180-lon1) + (180+lon2);
		} else {
		    dlon = lon2-lon1;
		}
		
		deltaDegrees = dlon;
		deltaPix = getWidth();
		pixPerDegree = getPlanetPixelCircumference()/360f;
	    }

	    // The new scale...
	    ret = pixPerDegree/(deltaPix/deltaDegrees);

	    //OK, now given the new scale at the apparent new center
	    //location, we need to test if the zone changes, because
	    //if it does, the values don't work out right because the
	    //pixel spacings are different.  If the zones are
	    //different, we need to recalculate the scale based on the
	    //new zone.
	    CADRG newcadrg = new CADRG(new LatLonPoint(nCenterLat, nCenterLon), ret, getWidth(), getHeight());

	    // Use the recursiveCount to prevent extended recalls.  A couple rounds should suffice.
	    if (newcadrg.getZone() != zone && recursiveCount < 2) {
		ret = newcadrg.getScale(ll1, ll2, newcadrg.forward(ll1), newcadrg.forward(ll2), recursiveCount +1);
	    }
	    
	    return ret;
	} catch (NullPointerException npe) {
	    Debug.error("ProjMath.getScale(): caught null pointer exception.");
	    return Float.MAX_VALUE;
	}
    }

    /*
    public static void main (String argv[]) {
	CADRG proj=
	    new CADRG(new LatLonPoint(42.0f, 0.0f), 18000000.0f, 620,480);
	
	Debug.output("---testing latitude");
	proj.testPoint(0.0f, 0.0f);
	proj.testPoint(10.0f, 0.0f);
	proj.testPoint(-10.0f, 0.0f);
	proj.testPoint(23.1234f, 0.0f);
	proj.testPoint(-23.1234f, 0.0f);
	proj.testPoint(90.0f, 0.0f);
	proj.testPoint(-100.0f, 0.0f);

	Debug.output("---testing longitude");
	proj.testPoint(0.0f, 10.0f);
	proj.testPoint(0.0f, -10.0f);
	proj.testPoint(0.0f, 86.45f);
	proj.testPoint(0.0f, -86.45f);
	proj.testPoint(0.0f, 375.0f);
	proj.testPoint(0.0f, -375.0f);
    }
    
    private void testPoint(float lat, float lon) {
	LatLonPoint llpoint =
	    new LatLonPoint(ProjMath.radToDeg(
		    normalize_latitude(ProjMath.degToRad(lat))), lon);
	Point point = forward(llpoint);

	Debug.output("(lon="+llpoint.getLongitude()+
			   ",lat="+llpoint.getLatitude()+
			   ") = (x="+point.x+",y="+point.y+")");

	llpoint = inverse(point);

	Debug.output("(x="+point.x+",y="+point.y+") = (lon="+
			   llpoint.getLongitude()+",lat="+
			   llpoint.getLatitude()+")");
    }
    */
}
