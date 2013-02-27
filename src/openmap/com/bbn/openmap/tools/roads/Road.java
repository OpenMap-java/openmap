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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/Road.java,v
// $
// $RCSfile: Road.java,v $
// $Revision: 1.5 $
// $Date: 2005/12/09 21:09:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.roads;

import java.awt.Point;
import java.io.Serializable;
import java.util.logging.Logger;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.coords.LatLonPoint;

public class Road
      implements RoadObject, Serializable {
   
    transient Logger logger = Logger.getLogger(this.getClass().getName());

   /**
    * The points along the road. The first and last points are always
    * Intersections.
    */
   private Waypoint[] points;

   /**
    * The lines between the points.
    */
   private RoadLine[] lines;

   /**
    * The class of this road. A Road's class implies what kind of road it is.
    * For now this governs its visual appearance of color and width.
    */
   private RoadClass roadClass;

   private boolean isRoute = false;

   private boolean blocked = false;

   /**
    * The id of this road.
    */
   private int id;

   /**
    * The name of this road.
    */
   private String name;

   /**
    * True if this road has been modified (points added or removed).
    */
   private boolean modified = false;

   /**
    * The RoadLayer we belong to so we can invoke its services.
    */
   private transient RoadLayer roadLayer;

   /**
    * Selection flag for this road.
    */
   private boolean selected = false;

   /**
    * Blink flag for this road.
    */
   private boolean blinkState = false;

   /**
    * Create a road between two Intersections. The detailed segments between the
    * intersections may be filled in later.
    * 
    * @param id a unique, integer identifier of this road.
    * @param name a name for this road.
    * @param from the intersection from which this road leaves.
    * @param to the intersection to which this road goes.
    * @param roadClass the class of this road.
    * @param roadLayer the RoadLayer we belong to.
    */
   public Road(int id, String name, Intersection from, Intersection to, RoadClass roadClass, RoadLayer roadLayer) {
      this.id = id;
      this.name = name;
      this.roadLayer = roadLayer;
      points = new Waypoint[2];
      setIntersections(from, to);
      this.roadClass = roadClass;
      createLines();
      modified = false;
   }

   public float getLengthInKilometers() {
      float kilometers = 0.0f;
      LatLonPoint prevPoint = points[0].getLocation();
      // logger.warning ("" + this + " pt 0 " + points[0] + " pt 1 "
      // + points[1] + " getSecondInter " + getSecondIntersection
      // ());
      for (int i = 1; i < points.length; i++) {
         LatLonPoint thisPoint = points[i].getLocation();
         kilometers +=
               GreatCircle.sphericalDistance(prevPoint.getLatitude(), prevPoint.getLongitude(), thisPoint.getLatitude(),
                                             thisPoint.getLongitude());
         prevPoint = thisPoint;
      }
      return kilometers;
   }

   public float getTraverseHours() {
      if (isBlocked())
         return Float.MAX_VALUE;
      return getLengthInKilometers() / getRoadClass().getConvoySpeed();
   }

   public LatLonPoint getLocationAtKilometer(float kilometers) {
      LatLonPoint prevPoint = points[0].getLocation();
      float prevLat = prevPoint.getLatitude();
      float prevLon = prevPoint.getLongitude();
      for (int i = 1; i < points.length; i++) {
         LatLonPoint thisPoint = points[i].getLocation();
         float thisLat = thisPoint.getLatitude();
         float thisLon = thisPoint.getLongitude();
         float thisLength = GreatCircle.sphericalDistance(prevLat, prevLon, thisLat, thisLon);
         if (thisLength >= kilometers) {
            float fraction = kilometers / thisLength;
            float deltaLat = thisLat - prevLat;
            float deltaLon = thisLon - prevLon;
            if (deltaLon < -180f)
               deltaLon += 360f;
            else if (deltaLon > 180f)
               deltaLon -= 360f;
            return new LatLonPoint.Double(prevLat + fraction * deltaLat, prevLon + fraction * deltaLon);
         }
         kilometers -= thisLength;
         prevPoint = thisPoint;
         prevLat = thisLat;
         prevLon = thisLon;
      }
      return prevPoint;
   }

   private void createLines() {
      lines = new RoadLine[points.length - 1];
      for (int i = 0; i < lines.length; i++) {
         lines[i] = new RoadLine(this, i);
      }
      blinkLines();
   }

   private void blinkLines() {
      for (int i = 0; i < lines.length; i++)
         lines[i].blink(blinkState);
   }

   /**
    * Set the state of the modified flag. Setting the modified flag to false
    * also sets the modified flag of all the points to false as well.
    * 
    * @param newValue the new setting.
    */
   public void setModified(boolean newValue) {
      modified = newValue;
      if (newValue == false) {
         for (int i = 0; i < points.length; i++)
            points[i].setModified(false);
      }
   }

   /**
    * Get the state of the modified flag.
    * 
    * @return true if the road or its points have been modified.
    */
   public boolean getModified() {
      if (modified)
         return true;
      for (int i = 0; i < points.length; i++)
         if (points[i].getModified())
            return true;
      return false;
   }

   public void block() {
      blocked = true;
      updateLines();
   }

   public void unblock() {
      blocked = false;
      updateLines();
   }

   public boolean isBlocked() {
      return blocked;
   }

   public void blink(boolean newState) {
      blinkState = newState;
      if (lines != null)
         blinkLines();
      blinkPoints();
   }

   /**
    * Accessor for the ID property.
    * 
    * @return the road ID.
    */
   public int getID() {
      return id;
   }

   public String getName() {
      return name;
   }

   public void setName(String newName) {
      name = newName;
   }

   public RoadClass getRoadClass() {
      return roadClass;
   }

   public void setRoadClass(RoadClass newClass) {
      roadClass = newClass;
      setModified(true);
      updateLines();
   }

   public String getRoadClassName() {
      return roadClass.getName().toString();
   }

   public void isRoute(boolean yes) {
      isRoute = yes;
      updateLines();
   }

   public boolean isRoute() {
      return isRoute;
   }

   public RoadLayer getRoadLayer() {
      return roadLayer;
   }

   public void setIntersections(Intersection from, Intersection to) {
      if (from == null) {
         logger.warning("from is null.");
         Thread.dumpStack();
      }
      if (to == null) {
         logger.warning("to is null.");
         Thread.dumpStack();
      }

      points[0] = from;
      points[points.length - 1] = to;
      checkPoints();
      createLines();
      setModified(true);
   }

   public void setRoadPoints(RoadPoint[] innerPoints) {
      Waypoint[] oldPoints = points;
      points = new Waypoint[2 + innerPoints.length];
      points[0] = oldPoints[0];
      System.arraycopy(innerPoints, 0, points, 1, innerPoints.length);
      points[points.length - 1] = oldPoints[oldPoints.length - 1];

      if (points[points.length - 1] == null) {
         logger.warning("to is null.");
         Thread.dumpStack();
      }

      checkPoints();

      createLines();
      // blinkPoints();
      setModified(true);
   }

   public void checkPoints() {
      for (int i = 0; i < points.length; i++) {
         if (points[i] == null) {
            logger.warning("found null point at " + i);
            Thread.dumpStack();
         }
      }
   }

   public RoadPoint[] getRoadPoints() {
      RoadPoint[] innerPoints = new RoadPoint[points.length - 2];
      System.arraycopy(points, 1, innerPoints, 0, innerPoints.length);
      return innerPoints;
   }

   public void insertRoadPointAt(RoadPoint wp, int ix) {
      Waypoint[] oldPoints = points;
      points = new Waypoint[1 + oldPoints.length];
      System.arraycopy(oldPoints, 0, points, 0, ix);
      points[ix] = wp;
      if (wp == null) {
         logger.warning("wp is null.");
         Thread.dumpStack();
      }
      checkPoints();
      System.arraycopy(oldPoints, ix, points, ix + 1, oldPoints.length - ix);
      createLines();
      // blinkPoints();
      setModified(true);
   }

   public void deleteRoadPoint(RoadPoint rp) {
      for (int ix = 1; ix < points.length - 1; ix++) {
         if (points[ix] == rp) {
            Waypoint[] oldPoints = points;
            points = new Waypoint[oldPoints.length - 1];
            System.arraycopy(oldPoints, 0, points, 0, ix);
            System.arraycopy(oldPoints, ix + 1, points, ix, oldPoints.length - ix - 1);
            createLines();
            setModified(true);
            return;
         }
      }
      checkPoints();

   }

   public Intersection getFirstIntersection() {
      return (Intersection) points[0];
   }

   public Intersection getSecondIntersection() {
      return (Intersection) points[points.length - 1];
   }

   public Intersection getOtherIntersection(Intersection intersection) {
      if (intersection == points[0])
         return (Intersection) points[points.length - 1];
      return (Intersection) points[0];
   }

   public void changeIntersection(Intersection oldIntersection, Intersection newIntersection) {
      if (oldIntersection == points[0]) {
         setIntersections(newIntersection, getSecondIntersection());
      } else if (oldIntersection == points[points.length - 1]) {
         setIntersections(getFirstIntersection(), newIntersection);
      }
      checkPoints();

   }

   public Waypoint getWaypoint(int ix) {
      return points[ix];
   }

   public Waypoint[] getPoints() {
      return points;
   }

   public RoadPoint[] getPointsBefore(RoadPoint wp) {
      for (int i = 1; i < points.length - 1; i++) {
         if (points[i] == wp) {
            RoadPoint[] answer = new RoadPoint[i - 1];
            System.arraycopy(points, 1, answer, 0, answer.length);
            return answer;
         }
      }
      return new RoadPoint[0];
   }

   public RoadPoint[] getPointsAfter(RoadPoint wp) {
      for (int i = 1; i < points.length - 1; i++) {
         if (points[i] == wp) {
            RoadPoint[] answer = new RoadPoint[points.length - i - 2];
            System.arraycopy(points, i + 1, answer, 0, answer.length);
            return answer;
         }
      }
      RoadPoint[] answer = new RoadPoint[points.length - 2];
      System.arraycopy(points, 1, answer, 0, answer.length);
      return answer;
   }

   private void blinkPoints() {
      for (int i = 1; i < points.length - 1; i++) {
         points[i].blink(blinkState);
      }
   }

   /**
    * Mark this Road as needing a new visual representation.
    */
   public synchronized void updateLines() {
      for (int i = 0; i < lines.length; i++)
         lines[i].update();
   }

   public void moveTo(Point loc) {
   }

   public synchronized void render(OMGraphicList gl, boolean projectionIsNew) {
      if (roadLayer.isEditing()) {
         for (int i = 1; i < points.length - 1; i++) {
            points[i].render(gl, projectionIsNew);
         }
      }
      for (int i = 0; i < lines.length; i++)
         lines[i].render(gl, projectionIsNew);
   }

   public String toString() {
      return name + " from " + getFirstIntersection() + " to " + getSecondIntersection() + " " + points.length + " points.";
   }

   public boolean isSelected() {
      return selected;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }
}