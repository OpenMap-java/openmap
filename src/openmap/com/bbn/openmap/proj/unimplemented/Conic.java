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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/unimplemented/Attic/Conic.java,v $
// $RCSfile: Conic.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:14 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.proj;

import java.awt.*;
import java.util.Vector;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.util.Debug;

/**
 * Base of all conic projections.
 * @see LambertConformalConic
 * @see MassStatePlane
 */
public abstract class Conic extends Proj {
    // HACK this class needs to be stress tested quite a bit more...

    // These should be defined in any State Plane file that derives
    // from this type.  HACK but are they global enough to go here?
    protected LatLonPoint origin; // lat/lon point where the
                                  // projection is centered
    protected LatLonPoint parallel1, parallel2; // the parallels of
                                                // the projection

    // These Border parameters are to limit the range of the screen in
    // this projection, since the conic projections are really intended
    // for a limited view for any given statel plane.  Decimal Degrees.
    protected float eastBorder = Float.NaN;
    protected float westBorder = Float.NaN;
    protected float northBorder = Float.NaN;
    protected float southBorder = Float.NaN;
  
    /**
     * Construct a conic projection.
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     * @param type projection type
     */
    public Conic (LatLonPoint center, float scale, 
                  int width, int height, int type){
        super(center, scale, width, height, type);
    }

    /**
     * Return stringified description of this projection.
     * @return String
     * @see Projection#getProjectionID
     */
    public String toString() {
        return " origin(" + origin.getLatitude() + "," + 
          origin.getLongitude() + ") " + super.toString();
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" parameters
     * used in the forward() and inverse() calls.<p>
     */
    protected void computeParameters() {
        // HACK grabbed from Cylindrical.  Might need fixing.
        planetPixelRadius = planetRadius * pixelsPerMeter;
        planetPixelCircumference = MoreMath.TWO_PI*planetPixelRadius;

        // minscale is the minimum scale allowable (before integer wrapping
        // can occur)
        minscale = (float)Math.ceil(planetPixelCircumference/(int)Integer.MAX_VALUE);
        if (minscale < 1)
            minscale = 1;
        if (scale < minscale)
            scale = minscale;

        // maxscale = scale at which world circumference fits in window
        maxscale = (float) planetPixelCircumference/(float)width;
        if (maxscale < minscale) {
            maxscale = minscale;
        }
        if (scale > maxscale) {
            scale = maxscale;
        }
        scaled_radius = planetPixelRadius/scale;

        // calculate cutoff scale for XWindows workaround
        XSCALE_THRESHOLD = (int)(planetPixelCircumference/64000);//fudge it a little bit
    }

    /**
     * Sets the limits of the border parameters to make sure the coordinates
     * make sense for the version of the plane used.
     * <p>
     * If you don't want a limit on any one of these parametes, pass in the
     * vaue Float.NaN.  Otherwise, the numbers are decimal degrees.
     * @param north float latitude in decimal degrees
     * @param east float longitude in decimal degrees
     * @param south float latitude in decimal degrees
     * @param west float longitude in decimal degrees
     */
    protected void setBorders(float north, float east, 
                              float south, float west){
          northBorder = north;
          southBorder = south;
          eastBorder = east;
          westBorder = west;
    }

    /**
     * Checks the border parameters to make sure the
     * coordinates make sense for the version of the plane used.
     *
     * @param lat float latitude in decimal degrees
     * @param lon float longitude in decimal degrees
     */
    public void setCenter(float lat, float lon) {

        if(eastBorder != Float.NaN) 
          if (lon > eastBorder) lon = eastBorder;
        if(westBorder != Float.NaN) 
          if (lon < westBorder) lon = westBorder;
        if(northBorder != Float.NaN) 
          if (lat > northBorder) lat = northBorder;
        if(southBorder != Float.NaN) 
          if (lat < southBorder) lat = southBorder;

        super.setCenter(lat, lon);
    }

    /**
     * Forward project a raw array of radian points.
     * This assumes nothing about the array of coordinates.  In no way does it
     * assume the points are connected or that the composite figure is to be
     * filled.
     * <p>
     * It does populate a visible array indicating whether the points are
     * visible on the projected view of the world.
     * @param rawllpts array of lat,lon,... in radians
     * @param rawoff offset into rawllpts
     * @param xcoords x coordinates
     * @param ycoords y coordinates
     * @param visible coordinates visible?
     * @param copyoff offset into x,y,visible arrays
     * @param copylen number of coordinates (coordinate arrays should be at
     * least this long, rawllpts should be at least twice as long).
     * @return boolean true if all points visible, false if some points not
     * visible.
     */
    public boolean forwardRaw(float[] rawllpts, int rawoff,
                              int[] xcoords, int[] ycoords, boolean[] visible,
                              int copyoff, int copylen){
        
        boolean visibleTotal = false;
        // HACK grabbed from Cylindrical.  Might need fixing.
        Point temp = new Point();
        int end = copylen+copyoff;
        for (int i=copyoff, j=rawoff; i<end; i++, j+=2) {
            forward(rawllpts[j], rawllpts[j+1], temp, true);
            xcoords[i] = temp.x;
            ycoords[i] = temp.y;
//          visible[i] = true;

            visible[i] = (0 <= temp.x && temp.x <= width) &&
                (0 <= temp.y && temp.y <= height);

            if (visible[i] == true && visibleTotal == false){
                visibleTotal = true;
            }
                
        }
        // if everything is visible
        return visibleTotal;
    }


    /**
     * Forward project a lat/lon Poly.
     * Remember to specify vertices in radians!
     * @param rawllpts float[] of lat,lon,lat,lon,... in RADIANS!
     * @param ltype line type (straight, rhumbline, greatcircle)
     * @param nsegs number of segment points (only for greatcircle or
     * rhumbline line types, and if &lt; 1, this value is generated internally)
     * @param isFilled filled poly?  this is currently ignored
     * for cylindrical projections.
     * @return Vector of x[], y[], x[], y[], ... projected poly
     */
    protected Vector _forwardPoly(float[] rawllpts, 
                                  int ltype, 
                                  int nsegs, boolean isFilled){
        Debug.message("projdetail", "Conic._forwardPoly()");
        // HACK: not handling any wrapping anomalies...  Need to test this
        // alot more.
        int i, j;

        // determine length of pairs
        int len = rawllpts.length>>1;   // len/2, chop off extra
        if (len < 2)
            return new Vector(0);

        // handle complicated line in specific routines
        if (isComplicatedLineType(ltype))
            return doPolyDispatch(rawllpts, ltype, nsegs, isFilled);

        Point temp = new Point();
        int[] xs = new int[len];
        int[] ys = new int[len];

        // forward project the points
        for (i=0, j=0; i<len; i++, j+=2) {
            temp = forward(rawllpts[j], rawllpts[j+1], temp, true);
            xs[i] = temp.x;
            ys[i] = temp.y;
        }

        Vector ret_val = new Vector(2);
        ret_val.addElement(xs);
        ret_val.addElement(ys);

        return ret_val;
    }


    /**
     * Draw the background for the projection.
     * @param g Graphics
     */
    public void drawBackground(Graphics g) {
        // HACK grabbed from Cylindrical.  Might need fixing.
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    protected static java.awt.Color backgroundColor = new java.awt.Color(191,239,255);

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return "Conic";
    }

}
