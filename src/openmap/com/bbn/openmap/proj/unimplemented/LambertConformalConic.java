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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/unimplemented/Attic/LambertConformalConic.java,v $
// $RCSfile: LambertConformalConic.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:24 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.Point;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;


/**
 * Implements the LambertConformalConic projection.
 *
 * @author Don Dietrick
 */
public class LambertConformalConic extends Conic {

    /**
     * The LambertCC name.
     */
    public final static transient String LambertCCName = "LambertCC";

    /**
     * The LambertCC type of projection.
     */
    public final static transient int LambertCCType = 99;

    protected int hy, wx;
    protected double n;
    protected double F;
    protected double Po;
    protected double RF;
    protected double quarterPI = Math.PI/(double)4.0;
    protected double halfPI = Math.PI/(double)2.0;

    /**
     * Construct a Lambert projection.
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     */
    public LambertConformalConic(LatLonPoint center, float scale, 
                                 int width, int height) {
        super(center, scale, width, height, LambertCCType);
    }


    //  Should never be called, except for testing.  All values are
    //  MSP defined.
    /*
    protected void computeLCCParameters() {
      Debug.message("proj", "LambertConformalConic.computeLCCParameters() with: h-" + 
                    height + "|w-" + width);

        //HACK - hardcoded MASS State Plane values - These should be
        //defined in all State Plane files that are created, and the
        //computeParameters function should call it in that State
        //Plane File.
        origin = new LatLonPoint(41.0f, -71.30f); 
        parallel1 = new LatLonPoint(41.43f, 0);
        parallel2 = new LatLonPoint(42.41f, 0);

        double p1tan = Math.tan((parallel1.radlat_/2.0) + quarterPI);
        double p2tan = Math.tan((parallel2.radlat_/2.0) + quarterPI);
        double lp2p1tan = Math.log(p2tan/p1tan);
        double p1cos = Math.cos(parallel1.radlat_);
        double p2cos = Math.cos(parallel2.radlat_);
        double lp1p2cos = Math.log(p1cos/p2cos);
        n = lp1p2cos/lp2p1tan;

        F = Math.cos(parallel1.radlat_)*
          Math.pow(Math.tan(quarterPI + (parallel1.radlat_/2.0)), n)/ n;

        RF = F*EARTH_RADIUS * PPM/scale;
        Po = RF / Math.pow(Math.tan(quarterPI + (origin.radlat_/2.0)), n);

        hy = (int)forward_y(ctrLat, ctrLon) + height/2;
        wx = width/2 - (int)forward_x(ctrLat, ctrLon);

        Debug.message("proj", "computeLCCParameters():\n     Origin = " + 
                      origin.getLatitude() + "," + origin.getLongitude() + 
                      "\n     scale = " + scale +
                      "\n     center = " + ProjMath.radToDeg(ctrLat) + 
                      ", " + ProjMath.radToDeg(ctrLon) +
                      "\n     parallel1 = " + parallel1 + 
                      "\n     parallel2 = " + parallel2 + 
                      "\n     n = " + (double)n + 
                      "\n     F = " + (double)F + 
                      "\n     RF = " + (double)RF + 
                      "\n     Po = " + (double)Po +
                      "\n     hy = " + hy + 
                      "\n     wx = " + wx);

    }
    */

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
        if (lat > NORTH_POLE) {
            return NORTH_POLE;
        } else if (lat < SOUTH_POLE) {
            return SOUTH_POLE;
        }
        return lat;
    }

    /**
     * forward_x() - arguments in radians (-DATELINE <= lambda <
     * DATELINE), (SOUTH_POLE <= phi <= NORTH_POLE), returns a raw
     * float in world coordinates. 
     */
    protected float forward_x(float phi, float lambda) {
        double diff = (double)(lambda-origin.radlon_);
        if (diff > halfPI) diff -= Math.PI;
        if (diff < -halfPI) diff += Math.PI;
        double theta = n*(double)(diff);
        double sintheta = Math.sin(theta);
        double rho = RF/(Math.pow(Math.tan(quarterPI + (double)(phi/2.0f)), n));

        if (Debug.debugging("proj")){
            Debug.output("LambertConformalConic.forward_x:\n   phi = " + 
                         ProjMath.radToDeg(phi) + 
                         "\n   lambda = " +  ProjMath.radToDeg(lambda) + 
                         "\n   origin = " + origin.getLatitude() +
                         "," + origin.getLongitude() + 
                         "\n   rho = " + rho + 
                         "\n   theta = " + ProjMath.radToDeg(theta));
        }
        return (float)(rho * sintheta);
    }

    /**
     * forward_y() - arguments in radians (-DATELINE <= lambda <
     * DATELINE), (SOUTH_POLE <= phi <= NORTH_POLE), returns a raw
     * float value in world coordinates. 
     */
    protected float forward_y(float phi, float lambda) {
        double diff = (double)(lambda-origin.radlon_);
        if (diff > halfPI) diff -= Math.PI;
        if (diff < -halfPI) diff += Math.PI;
        double theta = n*(double)(diff);
        double costheta = Math.cos(theta);
        double rho = RF/Math.pow(Math.tan(quarterPI + (double)(phi/2.0f)), n);

        if (Debug.debugging("proj")){
            Debug.output("LambertConformalConic.forward_y:\n   phi = " +  
                         ProjMath.radToDeg(phi) + 
                         "\n   lambda = " +  ProjMath.radToDeg(lambda) + 
                         "\n   origin = " + origin.getLatitude() +
                         "," + origin.getLongitude() + 
                         "\n   rho = " + rho + 
                         "\n   theta = " + ProjMath.radToDeg(theta));
        }

        return (float)(Po - (rho * costheta));
    }

    /**
     * inverse_lat(x, y) - assumes raw float values in world
     * coordinates. 
     */
    protected float inverse_lat(float x, float y) {
        double diff = Po - (double)y;
        double rho = Math.sqrt((double)(x*x) + (diff * diff));
        return (float)(2.0 * Math.atan(Math.pow((RF/rho), 1/n)) - halfPI);
    }

    /** 
     * inverse_lon(x, y) - assumes raw float values in world
     * coordinates. 
     */
    protected float inverse_lon(float x, float y) {
        double theta = Math.atan((double)x/(Po - (double)y));
        return (float)(theta/n) + origin.radlon_;
    }

    /**
     * Checks if a LatLonPoint is plot-able.
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     * @return boolean
     */
    public boolean isPlotable(float lat, float lon) {
        return true;// not really sure about this...
    }

    /**
     * Projects a point from Lat/Lon space to X/Y space.
     * @param pt LatLonPoint
     * @param p Point retval
     * @return Point p
     */
    public Point forward(LatLonPoint pt, Point p) {
        return forward(pt.radlat_, pt.radlon_, p, true);
    }

    /**
     * Forward projects a lat,lon coordinates.
     * @param lat raw latitude in decimal degrees
     * @param lon raw longitude in decimal degrees
     * @param p Resulting XY Point
     * @return Point p
     */
    public Point forward(float lat, float lon, Point p) {
        return forward(
                ProjMath.degToRad(lat), ProjMath.degToRad(lon), p, true);
    }

    /**
     * Forward projects lat,lon into XY space and returns a Point.
     * @param phi float latitude in radians
     * @param lambda float longitude in radians
     * @param p Resulting XY Point
     * @param isRadian bogus argument indicating that lat,lon
     * arguments are in radians
     * @return Point p
     */
    public Point forward(float phi, float lambda, Point p, boolean isRadian) {

        // Figure out the point for screen coordinates.  Need to take
        // into account that the origin point of the projection may be
        // off screen, so we need to take the calculated world
        // coordinates of the center of the screen and subtract the
        // screen offset from that.

        double diff = (double)(lambda-origin.radlon_);
        if (diff > halfPI) diff -= Math.PI;
        if (diff < -halfPI) diff += Math.PI;
        double theta = n*(double)(diff);

        // x
        double sintheta = Math.sin(theta);
        double rho = RF/Math.pow(Math.tan(quarterPI + (double)(phi/2.0f)), n);
        p.x = (int)((float)(rho * sintheta) + wx);
        
        //y
        double costheta = Math.cos(theta);
        p.y = hy - (int)((float)(Po - (rho * costheta)));

        return p;
    }


    /**
     * Inverse project x,y coordinates into a LatLonPoint.
     * @param x integer x coordinate
     * @param y integer y coordinate
     * @param llp LatLonPoint
     * @return LatLonPoint llp
     * @see Proj#inverse(Point)
     */
    public LatLonPoint inverse(int x, int y, LatLonPoint llp) {
        // convert from screen to world coordinates
        x = x - wx;
        y = hy - y;

        //lat
        double diff = Po - (double) y;
        double rho = Math.sqrt((double)(x*x) + (diff * diff));
        float lat = (float)(2.0 * Math.atan(Math.pow((RF/rho), 1/n)) - 
                            Math.PI/2.0); // scale is in RF
        //lon
        double theta;
        if (n < 0){
            theta = Math.atan(-1.0*((double)x/diff));
        }
        else theta = Math.atan((double)x/diff);
        float lon = (float)(theta/n) + origin.radlon_;

        llp.setLatLon(lat, lon, true);
        return llp;
    }

    /**
     * Inverse project a Point.
     * @param pt x,y Point
     * @param llp resulting LatLonPoint
     * @return LatLonPoint llp
     */
    public LatLonPoint inverse(Point pt, LatLonPoint llp) {
        return inverse (pt.x, pt.y, llp);
    }


    /**
     * Get the upper left (northwest) point of the projection.
     * <p>
     * Returns the upper left point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * @return LatLonPoint
     */
    public LatLonPoint getUpperLeft() {
        LatLonPoint tmp;
        float lat, lon, newLat, newLon;

        //  This shouldn't make a difference of northern or southern
        //  hemisphere.  Need to check all three points across the top
        //  of the screen (left, middle, right), and the topmost will
        //  be there.  Need to check the top left and bottom left to
        //  get the leftmost point, too.

//      if(true) return inverse(0, 0);
                        
        // Get screen coordinate of center for reference:
        //left
        lat = inverse_lat(-wx, hy);
        // middle
        newLat = inverse_lat((width/2)-wx, hy);
        if(newLat > lat) lat = newLat;
        // right
        newLat = inverse_lat(width-wx, hy);
        if(newLat > lat) lat = newLat;

        lon = inverse_lon(-wx, hy);
        newLon = inverse_lon(-wx, hy - height);
        if (newLon < lon) lon = newLon;
        else if (lon < 0 && newLon > 0) lon = newLon;  // dateline

        Debug.message("proj", "LambertConformalConic.getUpperLeft(): " +
                      "lat,lon=" + ProjMath.radToDeg(lat) + "," + ProjMath.radToDeg(lon));

        return new LatLonPoint(ProjMath.radToDeg(lat), ProjMath.radToDeg(lon));
    }


    /**
     * Get the lower right (southeast) point of the projection.
     * <p>
     * Returns the lower right point (or closest equivalent) of the
     * projection based on the center point and height and width of
     * screen.
     * @return LatLonPoint
     */
    public LatLonPoint getLowerRight() {
        LatLonPoint tmp;
        float lat, lon, newLat, newLon;

        //  This shouldn't make a difference of northern or southern
        //  hemisphere.  Need to check all three points across the
        //  bottom of the screen (left, middle, right), and the
        //  topmost will be there.  Need to check the top right and
        //  bottom right to get the rightmost point, too.

//      if(true) return inverse(width, height);

        //left
        lat = inverse_lat(-wx, hy - height);
        // middle
        newLat = inverse_lat((width/2)-wx, hy - height);
        if(newLat < lat) lat = newLat;
        // right
        newLat = inverse_lat(width - wx, hy - height);
        if(newLat < lat) lat = newLat;

        lon = inverse_lon(width - wx, hy);
        newLon = inverse_lon(width - wx, hy - height);
        if (newLon > lon) lon = newLon;
        else if (lon > 0 && newLon < 0) lon = newLon;  // dateline

        Debug.message("proj", "LambertConformalConic.getLowerRight(): " +
                      "lat,lon=" + ProjMath.radToDeg(lat) + "," + ProjMath.radToDeg(lon));

        return new LatLonPoint(ProjMath.radToDeg(lat), ProjMath.radToDeg(lon));
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return LambertCCName;
    }

    /*
    public void testPoint(float lat, float lon) {
        float x, y;
        lon = wrap_longitude(ProjMath.degToRad(lon));
        lat = normalize_latitude(ProjMath.degToRad(lat));
        x = forward_x(lat, lon);
        y = forward_y(lat, lon);

        System.out.println("(lon="+ProjMath.radToDeg(lon)+",lat="+ProjMath.radToDeg(lat)+
                           ") = (x="+x+",y="+y+")");
        lat = inverse_lat(x, y);
        lon = wrap_longitude(inverse_lon(x, y));
        System.out.println("(x="+x+",y="+y+") = (lon="+
                           ProjMath.radToDeg(lon)+",lat="+ProjMath.radToDeg(lat)+")");
    }

    public static void main (String argv[]) {
        LambertConformalConic proj=null;
        // defaults for MassStatePanel
        proj = new LambertConformalConic(new LatLonPoint(41.0f, -71.30f), 1.0f, 
                                         620, 480);

        System.out.println("testing");
        proj.setEarthRadius(1.0f);
        System.out.println("setEarthRadius("+proj.getEarthRadius()+")");
        proj.setPPM(1);
        System.out.println("setPPM("+proj.getPPM()+")");
        proj.setMinScale(1.0f);
        System.out.println("setMinScale("+proj.getMinScale()+")");
        try {
            proj.setScale(1.0f);
        } catch (java.beans.PropertyVetoException e) {
        }
        System.out.println("setScale("+proj.getScale()+")");
        System.out.println(proj);
        System.out.println();

        System.out.println("---testing latitude");
        proj.testPoint(0.0f, 0.0f);
        proj.testPoint(10.0f, 0.0f);
        proj.testPoint(40.0f, 0.0f);
        proj.testPoint(-80.0f, 0.0f);
        proj.testPoint(-90.0f, 0.0f);
        proj.testPoint(100.0f, 0.0f);
        proj.testPoint(-3272.0f, 0.0f);
        System.out.println("---testing longitude");
        proj.testPoint(0.0f, 10.0f);
        proj.testPoint(0.0f, -10.0f);
        proj.testPoint(0.0f, 90.0f);
        proj.testPoint(0.0f, -90.0f);
        proj.testPoint(0.0f, 170.0f);
        proj.testPoint(0.0f, -170.0f);
        proj.testPoint(0.0f, 180.0f);
        proj.testPoint(0.0f, -180.0f);
        proj.testPoint(0.0f, 190.0f);
        proj.testPoint(0.0f, -190.0f);
        System.out.println("---testing lat&lon");
        proj.testPoint(100.0f, 370.0f);
        proj.testPoint(-30.0f, -370.0f);
        proj.testPoint(-80.0f, 550.0f);
        proj.testPoint(0.0f, -550.0f);
    }
    */
}
