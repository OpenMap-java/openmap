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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/unimplemented/Attic/MassStatePlane.java,v $
// $RCSfile: MassStatePlane.java,v $
// $Revision: 1.3 $
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
 * Implements the MassStatePlane projection.
 *
 * @author Don Dietrick
 */
public class MassStatePlane extends LambertConformalConic {

    /**
     * The MassStatePlane name.
     */
    public final static transient String MassStatePlaneName = "MassStatePlane";

    /**
     * The MassStatePlane type of projection.
     */
    public final static transient int MassStatePlaneType = 43;

    /**
     * Construct a MassStatePlane projection.
     * <p>
     * @param center LatLonPoint center of projection
     * @param scale float scale of projection
     * @param width width of screen
     * @param height height of screen
     *
     */
    public MassStatePlane(
        LatLonPoint center, float scale, int width, int height)
    {
        super(center, scale, width, height);
        type = MassStatePlaneType;
        setMinScale(1000.0f);
        setMaxScale(2000000f);
        setBorders(43.0f, -69.0f, 41.0f, -73.0f);
    }


    /**
     * Called when some fundamental parameters change.
     * <p>
     * Each projection will decide how to respond to this change.
     * For instance, they may need to recalculate "constant" parameters
     * used in the forward() and inverse() calls.<p>
     *
     */
    public void computeParameters() {
        super.computeParameters();
        computeMSPParameters();
    }


    protected void computeMSPParameters() {
        if (Debug.debugging("proj")){
            Debug.output("MassStatePlane.computeMSPParameters() with: h-" + 
                         height + "|w-" + width);
        }

        origin = new LatLonPoint(41.0f, -71.50f); 
        parallel1 = new LatLonPoint(41.716667f, 0);
        parallel2 = new LatLonPoint(42.683333f, 0);

//      Book Example Parameters for testing
//      origin = new LatLonPoint(23.0f, -96.00f); 
//      parallel1 = new LatLonPoint(33.00f, 0);
//      parallel2 = new LatLonPoint(45.00f, 0);

        double p1tan = Math.tan((parallel1.radlat_/2.0) + quarterPI);
        double p2tan = Math.tan((parallel2.radlat_/2.0) + quarterPI);
        double lp2p1tan = Math.log(p2tan/p1tan);
        double p1cos = Math.cos(parallel1.radlat_);
        double p2cos = Math.cos(parallel2.radlat_);
        double lp1p2cos = Math.log(p1cos/p2cos);
        n = lp1p2cos/lp2p1tan;

        F = Math.cos(parallel1.radlat_)*
          Math.pow(Math.tan(quarterPI + (parallel1.radlat_/2.0)), n)/ n;

//      RF = F*EARTH_RADIUS * PPM/scale;
        RF = F*planetRadius * pixelsPerMeter/scale;
        Po = RF / Math.pow(Math.tan(quarterPI + (origin.radlat_/2.0)), n);

        hy = (int)forward_y(ctrLat, ctrLon) + height/2;
        wx = width/2 - (int)forward_x(ctrLat, ctrLon);

        if (Debug.debugging("proj")){
            Debug.output("MassStatePlane:computeMSPParameters():\n     Origin = " + 
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
    }
  
    public double x_meter_coord(float lat, float lon){
        lon = wrap_longitude(ProjMath.degToRad(lon));
        lat = normalize_latitude(ProjMath.degToRad(lat));
        float x = forward_x(lat, lon);

        // 200000 = False Northing
        return (double)((x/(3266.68f/getScale())) + 200000); 
    }

    public double y_meter_coord(float lat, float lon){
        lon = wrap_longitude(ProjMath.degToRad(lon));
        lat = normalize_latitude(ProjMath.degToRad(lat));
        float y = forward_y(lat, lon);

        // 750000 = False Easting
        return (double)((y/(3279.4f/getScale())) + 750000);
    }

    /**
     * Get the name string of the projection.
     */
    public String getName() {
        return MassStatePlaneName;
    }

    /*
    public static void main (String argv[]) {
        MassStatePlane proj=null;
        proj = new MassStatePlane(new LatLonPoint(42.329994f, -71.01998f), 
                                  100000f, 593, 310);
//      proj = new MassStatePlane(new LatLonPoint(35.0f, -75.00f), 
//                                1.0f, 620, 480);

        System.out.println("testing");
        System.out.println("EarthRadius("+proj.getEarthRadius()+")");
        System.out.println("PPM("+proj.getPPM()+")");
        proj.setMinScale(1000.0f);
        System.out.println("MinScale("+proj.getMinScale()+")");
        try {
            proj.setScale(26160.0f);// Full scale - 1635.0f, then forth 6540. then 16th 26160
        } catch (java.beans.PropertyVetoException e) {
            System.err.println("setScale errored!");
        }
        System.out.println("Scale("+proj.getScale()+")");
        System.out.println(proj);
        System.out.println();

        System.out.println("---testing point");
        // Frame 237898
        proj.testPoint(42.3316f, -71.0511f);
        System.out.println("Frame 237898 coordinate calcuated to " + 
                           proj.DOQ_frame_name(42.3316f, -71.0511f));

//      proj.testPoint(42.3678f, -71.0994f);
//      System.out.println("Frame coordinate x = " + 
//                         proj.x_meter_coord(42.3678f, -71.0994f) +
//                         ", y = " + proj.y_meter_coord(42.3678f, -71.0994f));

        // Frame 237902
        proj.testPoint(42.3676f, -71.0508f);
        System.out.println("Frame 237902 coordinate calcuated to " + 
                           proj.DOQ_frame_name(42.3676f, -71.0508f));

        // Frame 241902
        proj.testPoint(42.3674f, -71.0022f);
        System.out.println("Frame 241902 coordinate calcuated to " + 
                           proj.DOQ_frame_name(42.3674f, -71.0022f));


//      proj.testPoint(41.0f, -71.30f);
//      proj.testPoint(41.1f, -71.30f);
//      proj.testPoint(41.2f, -71.30f);
//      proj.testPoint(41.0f, -71.30f);
//      proj.testPoint(41.0f, -71.40f);
//      proj.testPoint(41.0f, -71.50f);
//      proj.testPoint(35.0f, -80.0f);
//      proj.testPoint(35.0f, -70.0f);
//      System.out.println("---testing longitude");
//      proj.testPoint(30.0f, -75.0f);
//      proj.testPoint(35.0f, -75.0f);
//      proj.testPoint(40.0f, -75.0f);
//      System.out.println("---testing lat&lon");
//      proj.testPoint(100.0f, 370.0f);
//      proj.testPoint(-30.0f, -370.0f);
//      proj.testPoint(-80.0f, 550.0f);
//      proj.testPoint(0.0f, -550.0f);
    }
    */
}
