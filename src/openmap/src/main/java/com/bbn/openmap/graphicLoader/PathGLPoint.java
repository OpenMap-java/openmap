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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/PathGLPoint.java,v $
// $RCSfile: PathGLPoint.java,v $
// $Revision: 1.6 $
// $Date: 2009/02/25 22:34:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader;

import java.awt.Graphics;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * The PathGLPoint is a GLPoint that follows a certain path, as
 * opposed to just wandering around randomly.
 */
public class PathGLPoint extends GLPoint {

    double[] pathPoints = null;
    OMPoly poly = null;
    int pathIndex = 0;
    double currentSegDist = 0;
    double nextSegOffset = 0f;
    double rate = Length.METER.toRadians(10000);

    public PathGLPoint(OMPoly path, int radius, boolean isOval) {
        super(0f, 0f, radius, isOval);
        setPoly(path);
    }

    public void move(float factor) {
        if (!stationary) {
            moveAlong();
        }
    }

    public double[] getSegmentCoordinates(int currentPathIndex) {
        double[] latlons = new double[4];

        if (pathIndex > pathPoints.length - 2 || pathIndex < 0) {
            pathIndex = 0;
        }

        if (pathPoints.length >= 4) {

            int la1 = pathIndex;
            int lo1 = pathIndex + 1;

            int la2 = pathIndex + 2;
            int lo2 = pathIndex + 3;

            if (lo2 >= pathPoints.length) {
                if (poly.isPolygon()) {
                    Debug.message("graphicloader",
                            "PathGLPoint.moveAlong(): index to big, wrapping... ");
                    la2 = 0;
                    lo2 = 1;
                } else {
                    pathIndex = 0;
                    Debug.message("graphicloader",
                            "PathGLPoint.moveAlong(): index to big, no wrapping, starting over... ");
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
            Debug.output("PathGLPoint.moveAlong(): segment " + (pathIndex / 2)
                    + " of " + (pathPoints.length / 2));
        }
        double azimuth;
        LatLonPoint newPoint;

        double[] latlons = getSegmentCoordinates(pathIndex);

        double segLength = GreatCircle.sphericalDistance(latlons[0],
                latlons[1],
                latlons[2],
                latlons[3]);
        if (Debug.debugging("graphicloader")) {
            Debug.output("PathGLPoint.moveAlong(): segment Length " + segLength
                    + ", and already have " + currentSegDist + " of it.");
        }
        double needToTravel = rate;
        int originalPathIndex = pathIndex;
        int loopingTimes = 0;
        while (needToTravel >= segLength - currentSegDist) {

            needToTravel -= (segLength - currentSegDist);
            currentSegDist = 0f;

            pathIndex += 2; // Move to the next segment of the poly

            if (Debug.debugging("graphicloader")) {
                Debug.output("PathGLPoint to next segment(" + (pathIndex / 2)
                        + "), need to travel " + needToTravel);
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

            segLength = GreatCircle.sphericalDistance(latlons[0],
                    latlons[1],
                    latlons[2],
                    latlons[3]);
        }

        if (Debug.debugging("graphicloader")) {
            Debug.output("Moving PathGLPoint within current(" + (pathIndex / 2)
                    + ") segment, segLength: " + segLength + ", ntt: "
                    + needToTravel);
        }

        // Staying on this segment, just calculate where the
        // next point on the segment is.
        azimuth = GreatCircle.sphericalAzimuth(latlons[0],
                latlons[1],
                latlons[2],
                latlons[3]);

        newPoint = GreatCircle.sphericalBetween(latlons[0],
                latlons[1],
                currentSegDist + needToTravel,
                azimuth);

        setLat(newPoint.getY());
        setLon(newPoint.getX());

        currentSegDist = GreatCircle.sphericalDistance(latlons[0],
                latlons[1],
                (float)newPoint.getRadLat(),
                (float)newPoint.getRadLon());
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