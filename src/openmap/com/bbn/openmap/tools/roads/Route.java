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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/Route.java,v
// $
// $RCSfile: Route.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class Route implements Cloneable, Serializable {

    private static float MSEC_PER_HOUR = 3600000.0f;

    private String name = null;

    Road[] roads;

    private boolean startWithFirstIntersection;
//    private boolean endWithFirstIntersection;

    transient Logger logger = Logger.getLogger(this.getClass().getName());

    static class NodeInfo {
        Intersection intersection;
        Road bestRoad = null;
        float time;
        float crowsPathHours;

        NodeInfo(Intersection intersection, Road road, float time,
                float crowsPathHours) {
            this.intersection = intersection;
            this.bestRoad = road;
            this.time = time;
            this.crowsPathHours = crowsPathHours;
        }
    }

    public synchronized static Route getBestRoute(Intersection from,
                                                  Intersection to,
                                                  float bestConvoySpeed,
                                                  float worstConvoySpeed) {
        Hashtable marks = new Hashtable();
        boolean haveRoute = false;
        float toLat;
        float toLon;
        LatLonPoint toLoc = to.getLocation();
        toLat = toLoc.getLatitude();
        toLon = toLoc.getLongitude();
        LatLonPoint fromLoc = from.getLocation();
        float fromLat = fromLoc.getLatitude();
        float fromLon = fromLoc.getLongitude();
        float timeLimitBase = GreatCircle.sphericalDistance(toLat,
                toLon,
                fromLat,
                fromLon)
                / worstConvoySpeed;
        float bestTime = Float.MAX_VALUE;
        for (float snakeFactor = 1.0f; snakeFactor < 40f; snakeFactor *= 2f) {
            //	  if (logger.isLoggable(Level.INFO))
            //	    logger.info ("Snake factor " + snakeFactor);

            float timeLimit = timeLimitBase * snakeFactor;
            Vector toDo = new Vector();
            toDo.addElement(from);
            marks.clear();
            marks.put(from, new NodeInfo(from, null, 0.0f, 0.0f));
            while (!toDo.isEmpty()) {
                Vector newToDo = new Vector();
                for (Enumeration e = toDo.elements(); e.hasMoreElements();) {
                    Intersection thisIntersection = (Intersection) e.nextElement();
                    NodeInfo thisInfo = (NodeInfo) marks.get(thisIntersection);
                    //  System.out.println ("examining " +
                    // thisIntersection);
                    for (Enumeration e2 = thisIntersection.getRoads(); e2.hasMoreElements();) {
                        Road road = (Road) e2.nextElement();
                        //			System.out.println (" - road " + road);
                        float roadTime = road.getTraverseHours();
                        float newTime = thisInfo.time + roadTime;
                        if (newTime > timeLimit)
                            continue;
                        Intersection nextIntersection = road.getOtherIntersection(thisIntersection);
                        //			System.out.println (" - next inter " +
                        // nextIntersection);

                        NodeInfo nextInfo = (NodeInfo) marks.get(nextIntersection);
                        if (nextInfo == null) {
                            LatLonPoint nextLoc = nextIntersection.getLocation();
                            float crowsPathDistance = GreatCircle.sphericalDistance(toLat,
                                    toLon,
                                    nextLoc.getLatitude(),
                                    nextLoc.getLongitude());
                            float crowsPathHours = crowsPathDistance
                                    / bestConvoySpeed;
                            nextInfo = new NodeInfo(nextIntersection, road, newTime, crowsPathHours);
                            marks.put(nextIntersection, nextInfo);
                            if (newTime + nextInfo.crowsPathHours > bestTime)
                                continue;
                            newToDo.addElement(nextIntersection);
                            //    System.out.println (" - best road for "
                            // + nextIntersection + " is " + road);
                        } else if (nextInfo.time > newTime) {
                            if (!nextInfo.intersection.equals(nextIntersection)) {
                                System.err.println("huh?  lookup of "
                                        + nextIntersection
                                        + " gets node info with inter "
                                        + nextInfo.intersection);
                            }
                            nextInfo.time = newTime;
                            nextInfo.bestRoad = road;
                            //System.out.println (" - (redo) best
                            // road for " + nextIntersection + " is "
                            // + road);
                            if (newTime + nextInfo.crowsPathHours > bestTime)
                                continue;
                            newToDo.addElement(nextIntersection);
                        } else {
                            continue;
                        }
                        if (nextIntersection == to) {
                            //System.err.println ("found end " + to);
                            bestTime = nextInfo.time;
                            haveRoute = true;
                        }
                    }
                }
                toDo = newToDo;
            }
            if (haveRoute)
                break;
        }
        Vector roadVector = new Vector();
        Route result = null;
        if (haveRoute) {
            for (NodeInfo info = (NodeInfo) marks.get(to); result == null;) {
                roadVector.addElement(info.bestRoad);
                // System.err.println ("adding Road #" + (i++) + " - "
                // + info.bestRoad);
                Intersection prevIntersection = info.bestRoad.getOtherIntersection(info.intersection);
                if (prevIntersection == from)
                    result = new Route(roadVector, info.bestRoad.getFirstIntersection() == prevIntersection);
                else
                    info = (NodeInfo) marks.get(prevIntersection);
            }
        }
        marks = null;
        return result;
    }

    public Route(String name, Road[] roads, boolean startWithFirstIntersection) {
        this.name = name;
        this.roads = roads;
        this.startWithFirstIntersection = startWithFirstIntersection;
    }

    private Route(Vector roadVector, boolean startWithFirstIntersection) {
        int nRoads = roadVector.size();
        roads = new Road[nRoads];
        for (int i = 0; i < nRoads; i++)
            roads[i] = (Road) roadVector.elementAt(nRoads - 1 - i);
        this.startWithFirstIntersection = startWithFirstIntersection;
    }

    public Object clone() {
        return new Route(null, roads, startWithFirstIntersection);
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public Road[] getRoads() {
        return roads;
    }

    public int getBlockedRoadCount() {
        int blockedRoadCount = 0;
        for (int i = 0; i < roads.length; i++) {
            if (roads[i].isBlocked())
                blockedRoadCount++;
        }
        return blockedRoadCount;
    }

    public void unblockBlockedRoads() {
        for (int i = 0; i < roads.length; i++) {
            if (roads[i].isBlocked())
                roads[i].unblock();
        }
    }

    public Intersection getOriginIntersection() {
        if (startWithFirstIntersection)
            return roads[0].getFirstIntersection();
        else
            return roads[0].getSecondIntersection();
    }

    public Intersection getDestinationIntersection() {
        Intersection x = getOriginIntersection();
        for (int i = 0; i < roads.length; i++) {
            x = roads[i].getOtherIntersection(x);
        }
        return x;
    }

    public long getTravelTime() {
        float hours = 0.0f;
        for (int i = 0; i < roads.length; i++) {
            Road road = roads[i];
            float roadLength = road.getLengthInKilometers();
            float convoySpeed = road.getRoadClass().getConvoySpeed();
            float timeToTraverse = roadLength / convoySpeed;
            hours += timeToTraverse;
        }
        return (long) (hours * MSEC_PER_HOUR);
    }

    public LatLonPoint location(long time) {
        float hours = time / MSEC_PER_HOUR;
        Intersection from = (startWithFirstIntersection ? roads[0].getFirstIntersection()
                : roads[0].getSecondIntersection());

        for (int i = 0; i < roads.length; i++) {
            Road road = roads[i];
            boolean forward = road.getFirstIntersection() == from;
            float roadLength = road.getLengthInKilometers();
            float convoySpeed = road.getRoadClass().getConvoySpeed();
            float timeToTraverse = roadLength / convoySpeed;
            if (timeToTraverse > hours) {
                float fraction = (float) hours / (float) timeToTraverse;
                if (!forward)
                    fraction = 1.0f - fraction;
                return road.getLocationAtKilometer(roadLength * fraction);
            }
            hours -= timeToTraverse;
            if (forward)
                from = road.getSecondIntersection();
            else
                from = road.getFirstIntersection();
        }
        return from.getLocation();
    }
}