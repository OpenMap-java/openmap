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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class Route implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private static float MSEC_PER_HOUR = 3600000.0f;

	private String name = null;

	Road[] roads;

	private boolean startWithFirstIntersection;
	// private boolean endWithFirstIntersection;

	transient Logger logger = Logger.getLogger(this.getClass().getName());

	static class NodeInfo {
		Intersection intersection;
		Road bestRoad = null;
		double time;
		double crowsPathHours;

		NodeInfo(Intersection intersection, Road road, double time, double crowsPathHours) {
			this.intersection = intersection;
			this.bestRoad = road;
			this.time = time;
			this.crowsPathHours = crowsPathHours;
		}
	}

	public synchronized static Route getBestRoute(Intersection from, Intersection to, double bestConvoySpeed,
			double worstConvoySpeed) {
		Map<Intersection, NodeInfo> marks = new HashMap<>();
		boolean haveRoute = false;
		LatLonPoint toLoc = to.getLocation();
		double toLat = toLoc.getY();
		double toLon = toLoc.getX();
		LatLonPoint fromLoc = from.getLocation();
		double fromLat = fromLoc.getY();
		double fromLon = fromLoc.getX();
		double timeLimitBase = GreatCircle.sphericalDistance(toLat, toLon, fromLat, fromLon) / worstConvoySpeed;
		double bestTime = Double.MAX_VALUE;
		for (float snakeFactor = 1.0f; snakeFactor < 40f; snakeFactor *= 2f) {
			// if (logger.isLoggable(Level.INFO))
			// logger.info ("Snake factor " + snakeFactor);

			double timeLimit = timeLimitBase * snakeFactor;
			List<Intersection> toDo = new ArrayList<>();
			toDo.add(from);
			marks.clear();
			marks.put(from, new NodeInfo(from, null, 0.0f, 0.0f));
			while (!toDo.isEmpty()) {
				List<Intersection> newToDo = new ArrayList<>();
				for (Intersection thisIntersection : toDo) {

					NodeInfo thisInfo = (NodeInfo) marks.get(thisIntersection);
					// System.out.println ("examining " +
					// thisIntersection);
					for (Road road : thisIntersection.getRoads()) {

						// System.out.println (" - road " + road);
						double roadTime = road.getTraverseHours();
						double newTime = thisInfo.time + roadTime;

						if (newTime > timeLimit) {
							continue;
						}
						Intersection nextIntersection = road.getOtherIntersection(thisIntersection);
						// System.out.println (" - next inter " + nextIntersection);

						NodeInfo nextInfo = marks.get(nextIntersection);
						if (nextInfo == null) {
							LatLonPoint nextLoc = nextIntersection.getLocation();
							double crowsPathDistance = GreatCircle.sphericalDistance(toLat, toLon, nextLoc.getY(),
									nextLoc.getX());
							double crowsPathHours = crowsPathDistance / bestConvoySpeed;
							nextInfo = new NodeInfo(nextIntersection, road, newTime, crowsPathHours);
							marks.put(nextIntersection, nextInfo);
							if (newTime + nextInfo.crowsPathHours > bestTime)
								continue;
							newToDo.add(nextIntersection);

							// System.out.println (" - best road for " + nextIntersection + " is " + road);

						} else if (nextInfo.time > newTime) {
							if (!nextInfo.intersection.equals(nextIntersection)) {
								System.err.println("huh?  lookup of " + nextIntersection + " gets node info with inter "
										+ nextInfo.intersection);
							}
							nextInfo.time = newTime;
							nextInfo.bestRoad = road;
							// System.out.println (" - (redo) best
							// road for " + nextIntersection + " is "
							// + road);
							if (newTime + nextInfo.crowsPathHours > bestTime)
								continue;
							newToDo.add(nextIntersection);
						} else {
							continue;
						}
						if (nextIntersection == to) {
							// System.err.println ("found end " + to);
							bestTime = nextInfo.time;
							haveRoute = true;
						}
					}
				}
				toDo = newToDo;
			}

			if (haveRoute) {
				break;
			}
		}

		List<Road> roadVector = new ArrayList<>();
		Route result = null;
		if (haveRoute) {
			for (NodeInfo info = (NodeInfo) marks.get(to); result == null;) {
				roadVector.add(info.bestRoad);
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

	private Route(List<Road> roadVector, boolean startWithFirstIntersection) {
		int nRoads = roadVector.size();
		roads = new Road[nRoads];
		for (int i = 0; i < nRoads; i++) {
			roads[i] = roadVector.get(nRoads - 1 - i);
		}
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
			if (roads[i].isBlocked()) {
				blockedRoadCount++;
			}
		}
		return blockedRoadCount;
	}

	public void unblockBlockedRoads() {
		for (int i = 0; i < roads.length; i++) {
			if (roads[i].isBlocked()) {
				roads[i].unblock();
			}
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
		double hours = 0.0f;
		for (int i = 0; i < roads.length; i++) {
			Road road = roads[i];
			double roadLength = road.getLengthInKilometers();
			double convoySpeed = road.getRoadClass().getConvoySpeed();
			double timeToTraverse = roadLength / convoySpeed;
			hours += timeToTraverse;
		}
		return (long) (hours * MSEC_PER_HOUR);
	}

	public LatLonPoint location(long time) {
		double hours = time / MSEC_PER_HOUR;
		Intersection from = (startWithFirstIntersection ? roads[0].getFirstIntersection()
				: roads[0].getSecondIntersection());

		for (int i = 0; i < roads.length; i++) {
			Road road = roads[i];
			boolean forward = road.getFirstIntersection() == from;
			double roadLength = road.getLengthInKilometers();
			double convoySpeed = road.getRoadClass().getConvoySpeed();
			double timeToTraverse = roadLength / convoySpeed;
			if (timeToTraverse > hours) {
				float fraction = (float) hours / (float) timeToTraverse;
				if (!forward) {
					fraction = 1.0f - fraction;
				}
				return road.getLocationAtKilometer(roadLength * fraction);
			}
			hours -= timeToTraverse;
			if (forward) {
				from = road.getSecondIntersection();
			} else {
				from = road.getFirstIntersection();
			}
		}
		return from.getLocation();
	}
}