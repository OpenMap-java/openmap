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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/PathGLPoint.java,v $
// $RCSfile: PathGLPoint.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:07 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.graphicLoader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.bbn.openmap.Environment;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;

/**
 * The PathGLPoint is a GLPoint that follows a certain path, as
 * opposed to just wandering around randomly.
 */
public class PathGLPoint extends GLPoint {

    float[] pathPoints = null;
    OMPoly poly = null;
    int pathIndex = 0;
    float currentSegDist = 0f;
    float nextSegOffset = 0f;
    float rate = Length.METER.toRadians(10000); 

    public PathGLPoint(OMPoly path, int radius, boolean isOval) {
        super(0f, 0f, radius, isOval);
        setPoly(path);
    }

    public void move(float factor) {
        if (!stationary) {
            moveAlong();
        }
    }

    public float[] getSegmentCoordinates(int currentPathIndex) {
        float[] latlons = new float[4];
        
        if (pathIndex > pathPoints.length - 2 || pathIndex < 0) {
            pathIndex = 0;
        }
        
        if (pathPoints != null && pathPoints.length >= 4) {
            
            int la1 = pathIndex;
            int lo1 = pathIndex + 1;
            
            int la2 = pathIndex + 2;
            int lo2 = pathIndex + 3;
            
            if (lo2 >= pathPoints.length) {
                if (poly.isPolygon()) {
                    Debug.message("graphicloader", "PathGLPoint.moveAlong(): index to big, wrapping... ");
                    la2 = 0;
                    lo2 = 1;
                } else {
                    pathIndex = 0;
                    Debug.message("graphicloader", "PathGLPoint.moveAlong(): index to big, no wrapping, starting over... ");
                    return getSegmentCoordinates(pathIndex);
                }
            }
            
            latlons[0] = pathPoints[la1];
            latlons[1] = pathPoints[lo1];
            latlons[2] = pathPoints[la2];
            latlons[3] = pathPoints[lo2];
        } 
        
        return latlons;
    }

    public void moveAlong() {
        if (Debug.debugging("graphicloader")) {
            Debug.output("PathGLPoint.moveAlong(): segment " + 
                         (pathIndex/2) + " of " + 
                         (pathPoints.length/2));
        }
        float azimuth;
        LatLonPoint newPoint;

        float[] latlons = getSegmentCoordinates(pathIndex);

        float segLength = GreatCircle.spherical_distance(latlons[0], 
                                                         latlons[1], 
                                                         latlons[2],
                                                         latlons[3]);
        if (Debug.debugging("graphicloader")) {
            Debug.output("PathGLPoint.moveAlong(): segment Length " + segLength + ", and already have " + currentSegDist + " of it.");
        }
        float needToTravel = rate;
        int originalPathIndex = pathIndex;
        int loopingTimes = 0;
        while (needToTravel >= segLength - currentSegDist) {

            needToTravel -= (segLength - currentSegDist);
            currentSegDist = 0f;

            pathIndex += 2; // Move to the next segment of the poly
            
            if (Debug.debugging("graphicloader")) {
                Debug.output("PathGLPoint to next segment(" +
                             (pathIndex/2) + "), need to travel " + 
                             needToTravel);
            }
            latlons = getSegmentCoordinates(pathIndex);

            if (pathIndex == originalPathIndex) {
                loopingTimes++;
                if (loopingTimes > 1) {
                    Debug.output("PathGLPoint looping on itself, setting to stationary");
                    setStationary(true);
                    return;
                }
            }
            
            segLength = GreatCircle.spherical_distance(latlons[0], 
                                                       latlons[1], 
                                                       latlons[2],
                                                       latlons[3]);
        }
        
        if (Debug.debugging("graphicloader")) {
            Debug.output("Moving PathGLPoint within current(" +
                         (pathIndex/2) + ") segment, segLength: " + 
                         segLength + ", ntt: " + needToTravel);
        }
        
        // Staying on this segment, just calculate where the
        // next point on the segment is.
        azimuth = GreatCircle.spherical_azimuth(latlons[0], 
                                                latlons[1], 
                                                latlons[2],
                                                latlons[3]);

        newPoint = GreatCircle.spherical_between(
            latlons[0], latlons[1], 
            currentSegDist + needToTravel, azimuth);
        
        setLat(newPoint.getLatitude());
        setLon(newPoint.getLongitude());
        
        currentSegDist = GreatCircle.spherical_distance(
            latlons[0], latlons[1], 
            newPoint.radlat_, newPoint.radlon_);
    }

    public boolean generate(Projection p) {
        boolean ret = super.generate(p);
        if (poly != null) {
            poly.generate(p);
        }
        return ret;
    }

    public void render(Graphics g) {
        if (poly != null) {
            poly.render(g);
        }
        super.render(g);
    }

    public void setPoly(OMPoly p) {
        poly = p;

        if (poly.getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
            pathPoints = poly.getLatLonArray();
            setLat(ProjMath.radToDeg(pathPoints[0]));
            setLon(ProjMath.radToDeg(pathPoints[1]));
            setStationary(false);
        } else {
            setStationary(true);
        }
    }

    public OMPoly getPoly() {
        return poly;
    }
}
