/* **********************************************************************
 *
 *    Use, duplication, or disclosure by the Government is subject to
 * 	     restricted rights as set forth in the DFARS.
 *
 * 			   BBN Technologies
 * 			    A Division of
 * 			   BBN Corporation
 * 			  10 Moulton Street
 * 			 Cambridge, MA 02138
 * 			    (617) 873-3000
 *
 * 	  Copyright 1998 by BBN Technologies, A Division of
 * 		BBN Corporation, all rights reserved.
 *
 * **********************************************************************
 *
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/roads/RoadFinder.java,v $
 * $RCSfile: RoadFinder.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/02/16 16:22:48 $
 * $Author: dietrick $
 *
 * **********************************************************************
 */

package com.bbn.openmap.tools.roads;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.quadtree.QuadTree;

/**
 * Gives road access to a shape or vpf layer.
 */
public class RoadFinder
      implements RoadServices, ProjectionListener, RoadLayer {

   protected RoadClasses roadClasses = new RoadClasses();

   protected RoadClass defaultRoadClass;

   protected LayerView layer;

   protected Intersections intersections = new Intersections();

   protected RoadVector roads = new RoadVector();

   protected Vector removedRoads = new Vector();

   /**
    * how far (in lat-lon space) from lat,lon point to look in quad tree for
    * nearest road *
    */
   protected float halo;

   Logger logger = Logger.getLogger(this.getClass().getName());
   boolean drawIntersections = false;
   boolean drawResults = false;
   boolean doLoopCheck = false;
   protected QuadTree interQuadTree;
   protected Map graphicToRoad;
   boolean doInterp = true;

   /** list of extra graphics to display */
   List toDraw = new ArrayList();
   boolean showLines = true;
   int roadsMade = 0;

   public RoadFinder(LayerView layer, boolean drawIntersections, boolean drawResults) {
      initRoadClasses();

      this.drawIntersections = drawIntersections;
      this.drawResults = drawResults;
      logger.info("drawIntersections is " + drawIntersections);
      logger.info("drawResults is " + drawResults);
      this.layer = layer;
   }

   protected void initRoadClasses() {
      roadClasses.put(new RoadClass("1", Color.magenta, 2, 25.0f));
      defaultRoadClass = findRoadClass("1");
   }

   /**
    * Implemented for ProtectionListener
    */
   public void projectionChanged(ProjectionEvent e) {
      try {
         getData();
      } catch (Exception ee) {
         logger.warning("Got exception " + ee);
         ee.printStackTrace();
      }
   }

   /**
    * Take the shape data on the layer and use it to populate our roads and
    * intersections.
    * 
    * Clears lists of roads and intersections first, and after calculating the
    * roads, tells the RoadLayer what extra graphics to display, if any.
    */
   protected synchronized void getData()
         throws Exception {
      logger.info("get Data called.");
      intersections.clear();
      removedRoads.setSize(0);
      roads.clear();
      toDraw.clear();
      getRoads();
      checkIntegrity();
      logger.info("showing " + toDraw.size() + " extra graphics.");
      layer.setExtraGraphics(toDraw);
      halo = 0.05f * (getProjection().getScale() / 20000f);
   }

   /**
    * Take the shape data on the layer and use it to populate our roads and
    * intersections.
    * 
    */
   protected void getRoads()
         throws Exception {
      roadsMade = 0;
      List rectangle = layer.getGraphicList();
      int[] xPoints = new int[1024];
      int[] yPoints = new int[1024];
      interQuadTree = new QuadTree();
      graphicToRoad = new HashMap();

      int height = getProjection().getHeight();
      int width = getProjection().getWidth();
      int skipped = 0;

      synchronized (rectangle) {
         double[] points = new double[6];
         if (logger.isLoggable(Level.INFO))
            logger.info("iterating over rectangle contents.");

         int num = 0;
         int made = 0;
         for (Iterator iter = rectangle.iterator(); iter.hasNext();) {
            double lastXOff = 0;
            double lastYOff = 0;

            num++;
            OMGeometry graphic = (OMGeometry) iter.next();

            if (logger.isLoggable(Level.FINE))
               logger.fine("examining " + graphic);

            Shape shape = graphic.getShape();
            if (shape == null)
               continue;

            PathIterator path = shape.getPathIterator(new AffineTransform());
            int segment = 0;
            int itemsInPath = 0;
            boolean pathValid = true;

            for (; !path.isDone() && pathValid; path.next()) {
               int type = path.currentSegment(points);
               itemsInPath++;
               boolean offScreen = false;
               if (points[0] < 0 || points[0] >= width) {
                  // logger.warning("skipping x point " +
                  // points[0] + " b/c it's off the map.");
                  offScreen = true;
               }
               if (points[1] < 0 || points[1] >= height) {
                  // logger.warning("skipping y point " +
                  // points[1] + " b/c it's off the map.");
                  offScreen = true;
               }

               switch (type) {
                  case PathIterator.SEG_CLOSE:
                     logger.warning("got close");
                     break;
                  case PathIterator.SEG_CUBICTO:
                     logger.warning("got cubic to");
                     break;
                  case PathIterator.SEG_LINETO:
                     if (offScreen) {
                        if (segment > 0) {
                           // BOZO
                           // should reexamine whether this is
                           // legal - there should be
                           // a one-to-one mapping between
                           // graphic and road object,
                           // but this will throw away the
                           // original entry

                           if (doInterp) {
                              Point interpPt =
                                    interp(xPoints[segment - 1], yPoints[segment - 1], points[0], points[1], width, height);
                              xPoints[segment] = interpPt.x;
                              yPoints[segment++] = interpPt.y;

                              makeRoad(shape, graphic, made++, xPoints, yPoints, segment);
                              lastXOff = 0;
                              lastYOff = 0;
                              segment = 0;
                           }
                        } else {
                           lastXOff = points[0];
                           lastYOff = points[1];
                        }
                     } else { // onscreen
                        if (lastXOff != 0 || lastYOff != 0) {
                           Point interpPt = interp(points[0], points[1], lastXOff, lastYOff, width, height);
                           xPoints[segment] = interpPt.x;
                           yPoints[segment++] = interpPt.y;
                        }

                        xPoints[segment] = (int) points[0];
                        yPoints[segment++] = (int) points[1];
                        lastXOff = 0;
                        lastYOff = 0;
                     }

                     if (logger.isLoggable(Level.FINE))
                        logger.fine(" line to " + points[0] + ", " + points[1]);

                     break;
                  case PathIterator.SEG_MOVETO:
                     if (offScreen) {
                        lastXOff = points[0];
                        lastYOff = points[1];
                     } else {
                        if (segment == 0) {
                           xPoints[segment] = (int) points[0];
                           yPoints[segment++] = (int) points[1];
                        } else {
                           // we got a second move to in the list
                           // - this is not valid
                           pathValid = false;
                           logger.info("got invalid path.");
                        }

                        lastXOff = 0;
                        lastYOff = 0;
                     }

                     if (logger.isLoggable(Level.FINE))
                        logger.fine(" moving to " + points[0] + ", " + points[1]);

                     break;
                  case PathIterator.SEG_QUADTO:
                     logger.warning("got quad to");
                     break;
                  default:
                     logger.warning("got another type : " + type);
                     break;
               }
            }

            if (segment < 2) {
               skipped++;
               logger.fine("Skipping line that doesn't have an end point");
            } else {
               if (logger.isLoggable(Level.INFO))
                  logger.info("items in path " + itemsInPath);

               makeRoad(shape, graphic, made++, xPoints, yPoints, segment);
            }
            segment = 0;
         }

         if (logger.isLoggable(Level.INFO))
            logger.info("num items " + num + " skipped " + skipped);
      }
   }

   /**
    * find a point between x1,y1 and x2, y2 that is within the visible map
    * 
    * @param width of visible map
    * @param height of visible map
    * @return Point between x1,y1 and x2, y2
    */
   protected Point interp(double x1, double y1, double x2, double y2, int width, int height) {
      double deltaY = y2 - y1;
      double deltaX = x2 - x1;
      double slope = deltaY / deltaX;
      double newX = x2;
      double newY = y2;

      if (newX < 0) {
         newX = 0;
         newY = Math.round(slope * (newX - x1) + y1);
      } else if (newX >= width) {
         newX = width - 1;
         newY = Math.round(slope * (newX - x1) + y1);
      }

      if (newY < 0) {
         newY = 0;
         newX = Math.round(x1 + (newY - y1) / slope);
      } else if (newY >= height) {
         newY = height - 1;
         newX = Math.round(x1 + (newY - y1) / slope);
      }

      int intX = (int) newX;
      int intY = (int) newY;

      if (intX < 0) {
         logger.warning("new x is " + intX);
         intX = 0;
      }
      if (intX >= width) {
         logger.warning("new x is " + intX);
         intX = width - 1;
      }
      if (intY < 0) {
         logger.warning("new y is " + intY);
         intY = 0;
      }
      if (intY >= height) {
         logger.warning("new y is " + intY);
         intY = height - 1;
      }

      if (logger.isLoggable(Level.INFO)) {
         logger.info("from " + x1 + "," + y1 + " to " + x2 + "," + y2 + "w " + width + " h " + height + " interp " + intX + ","
               + intY);
      }

      return new Point(intX, intY);
   }

   /**
    * Makes a road object given the points on the shape that are within the
    * visible box
    * 
    * Stores it in a quadTree
    */
   protected void makeRoad(Shape shape, OMGeometry graphic, int num, int[] xPoints, int[] yPoints, int segment) {
      createRoadFromPoints(num, xPoints, yPoints, segment);
   }

   /**
    * Makes a road object given the points on the shape that are within the
    * visible box
    * 
    * @param nPoints in the xpoints and ypoints arrays
    */
   protected RoadObject createRoadFromPoints(int id, int[] xpoints, int[] ypoints, int nPoints) {
      RoadPoint[] roadPoints = new RoadPoint[nPoints - 2];
      Intersection from = findIntersection(xpoints[0], ypoints[0]);
      int fromBefore = from.getRoadCount();
      Intersection to = findIntersection(xpoints[nPoints - 1], ypoints[nPoints - 1]);
      int toBefore = to.getRoadCount();

      if (from == null) {
         logger.warning("no from intersection for " + xpoints[0] + ", " + ypoints[0]);
      }
      if (to == null) {
         logger.warning("no to intersection for " + xpoints[nPoints - 1] + ", " + ypoints[nPoints - 1]);
      }

      String name = "road";
      Road road = createRoad(id, name + "-" + id, from, to, defaultRoadClass);
      if (fromBefore + 1 != from.getRoadCount())
         logger.severe("huh? " + from + " had " + fromBefore + " roads before and now " + from.getRoadCount());
      if (toBefore + 1 != to.getRoadCount())
         logger.severe("huh? " + to + " had " + toBefore + " roads before and now " + to.getRoadCount());
      int width = roadsMade % 5;
      roadsMade++;

      if (logger.isLoggable(Level.INFO)) {
         logger.info("road # " + roadsMade + " " + road + " has " + nPoints + " points");
      }

      if (!showLines && drawIntersections) {
         OMPoint point = new YellowPoint(xpoints[0], ypoints[0], 10);
         toDraw.add(point);
      }

      for (int i = 1; i < nPoints - 1; i++) {
         roadPoints[i - 1] = new RoadPoint(road, createLatLonPoint(xpoints[i], ypoints[i]), this);
         if (drawIntersections) {
            if (showLines) {
               OMLine line = new YellowLine(xpoints[i - 1], ypoints[i - 1], xpoints[i], ypoints[i], width);
               toDraw.add(line);
               toDraw.add(new OMText((xpoints[i - 1] - xpoints[i]) / 2 + xpoints[i - 1], (ypoints[i - 1] - ypoints[i]) / 2
                     + ypoints[i - 1] - 5, "" + roadsMade, 0));
            } else {
               OMPoint point = new YellowPoint(xpoints[i], ypoints[i], 10);
               toDraw.add(point);
            }
         }
      }

      if (drawIntersections) {
         if (showLines) {
            OMLine line =
                  new YellowLine(xpoints[nPoints - 2], ypoints[nPoints - 2], xpoints[nPoints - 1], ypoints[nPoints - 1], width);
            toDraw.add(line);
            toDraw.add(new OMText((xpoints[nPoints - 2] - xpoints[nPoints - 1]) / 2 + xpoints[nPoints - 2],
                                  (ypoints[nPoints - 2] - ypoints[nPoints - 1]) / 2 + ypoints[nPoints - 2] - 5, "" + roadsMade, 0));
            line.addArrowHead(true);
         } else {
            OMPoint point = new YellowPoint(xpoints[nPoints - 1], ypoints[nPoints - 1], 10);
            toDraw.add(point);
         }
      }

      if (to == from && nPoints == 2) {
         deleteRoad(road);
         return null;
      }

      road.setRoadPoints(roadPoints);

      if (!road.getFirstIntersection().equals(from))
         logger.severe("huh? " + road + " first inter " + road.getFirstIntersection() + " not " + from);

      if (!road.getSecondIntersection().equals(to))
         logger.severe("huh? " + road + " second inter " + road.getSecondIntersection() + " not " + to);

      if (road.getPoints().length < 2)
         logger.warning("Error : somehow made a road " + road + " with too few points.");
      else if (logger.isLoggable(Level.INFO)) {
         // logger.info("made " + road);
      }

      return road;
   }

   /** a yellow point for displaying intersections */
   protected class YellowPoint
         extends OMPoint {
      public YellowPoint(int x, int y, int radius) {
         super(x, y, radius);
      }

      public void render(Graphics g) {
         setGraphicsColor(g, Color.YELLOW);
         draw(g, getShape());
      }
   }

   /** a yellow line for display routes between intersections */
   protected class YellowLine
         extends OMLine {
      int width;

      public YellowLine(int x, int y, int x2, int y2, int width) {
         super(x, y, x2, y2);
         this.width = width;
      }

      public void render(Graphics g) {
         float[] dash1 = new float[width + 1];
         dash1[0] = 10.f;

         for (int i = 1; i < width; i++) {
            dash1[i] = 2.0f;
         }

         BasicStroke dashed = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
         ((Graphics2D) g).setStroke(dashed);
         setGraphicsColor(g, Color.YELLOW);
         draw(g, getShape());
      }
   }

   protected LatLonPoint createLatLonPoint(int x, int y) {
      return getProjection().inverse(x, y, new LatLonPoint.Double());
   }

   protected Intersection findIntersection(LatLonPoint loc, String name) {
      if (name == null)
         return findIntersection(loc);
      Intersection intersection = intersections.get(name);
      if (intersection != null) {
         LatLonPoint foundLoc = intersection.getLocation();
         float distance =
               (Math.abs(foundLoc.getLatitude() - loc.getLatitude()) + Math.abs(foundLoc.getLongitude() - loc.getLongitude()));
         if (distance * Intersection.GRID > 0.1f) {
            intersection = findIntersection(loc); // Ignore the
            // name, it's
            // too far away.
            System.out.println("Using " + intersection.getName() + " instead of " + name + " distance = " + distance);
            return intersection;
         }
      } else {
         intersection = new Intersection(loc, name, this);
         intersections.put(intersection);
         interQuadTree.put(intersection.getLatitude(), intersection.getLongitude(), intersection);
      }
      return intersection;
   }

   protected Intersection findIntersection(int x, int y) {
      LatLonPoint fromLoc = createLatLonPoint(x, y);
      Intersection from = findIntersection(fromLoc);
      return from;
   }

   protected Intersection findIntersection(LatLonPoint loc) {
      String name = Intersection.getLatLonPointName(loc);
      Intersection intersection = intersections.get(name);
      if (intersection == null) {
         if (logger.isLoggable(Level.FINE))
            logger.fine("making new intersection for " + loc);
         intersection = new Intersection(loc, name, this);
         interQuadTree.put(intersection.getLatitude(), intersection.getLongitude(), intersection);
         intersections.put(intersection);
      } else {
         if (logger.isLoggable(Level.FINE))
            logger.fine("found existing intersection for " + loc + " with " + intersection.getRoadCount()
                  + " roads coming out of it.");
      }
      return intersection;
   }

   protected void deleteIntersection(Intersection intersection) {
      if (intersection.getRoadCount() > 0)
         throw new IllegalArgumentException("Attempt to delete connected intersection");
      intersections.remove(intersection);
   }

   /**
    * called from Intersection Implemented for RoadLayer interface
    */
   public Road createRoad(Intersection from) {
      return createRoad(-1, null, from, null, null);
   }

   protected Road createRoad(int id, String name, Intersection from, Intersection to, RoadClass cl_ss) {
      if (id < 0)
         id = findUnusedRoadID();
      if (name == null)
         name = "Road_" + id;
      if (from == null)
         from = findIntersection(to.getLocation(), to.getName() + ".drag");
      if (to == null)
         to = findIntersection(from.getLocation(), from.getName() + ".drag");
      if (cl_ss == null)
         cl_ss = defaultRoadClass;
      Road road = new Road(id, name, from, to, cl_ss, this);
      road.setModified(true);
      from.addRoad(road);
      to.addRoad(road);
      roads.add(road);
      return road;
   }

   public void deleteRoad(Road road) {
      Intersection intersection1 = road.getFirstIntersection();
      Intersection intersection2 = road.getSecondIntersection();
      intersection1.removeRoad(road);
      intersection2.removeRoad(road);
      if (intersection1.getRoadCount() == 0)
         deleteIntersection(intersection1);
      if (intersection2.getRoadCount() == 0)
         deleteIntersection(intersection2);
      if (intersection1.getRoadCount() == 2 && intersection1.getRoad(0).getRoadClass() == intersection1.getRoad(1).getRoadClass())
         joinRoads(intersection1);
      if (intersection2.getRoadCount() == 2 && intersection2.getRoad(0).getRoadClass() == intersection2.getRoad(1).getRoadClass())
         joinRoads(intersection2);
      removedRoads.addElement(road);
      roads.remove(road);
   }

   /**
    * Split a road into two roads at one of its corners. An intersection is
    * created where the corner was and the segments before the corner become the
    * segments of the original road. The segments after the corner become the
    * segments of a new road between the new intersection and the
    */
   public Intersection splitRoad(Road road, RoadPoint rp) {
      RoadPoint[] pointsBefore = road.getPointsBefore(rp);
      RoadPoint[] pointsAfter = road.getPointsAfter(rp);
      Intersection newIntersection = findIntersection(rp.getLocation(), null);
      Intersection firstIntersection = road.getFirstIntersection();
      Intersection secondIntersection = road.getSecondIntersection();
      road.setIntersections(firstIntersection, newIntersection);
      road.setRoadPoints(pointsBefore);
      secondIntersection.removeRoad(road);
      newIntersection.addRoad(road);
      Road newRoad = createRoad(-1, null, newIntersection, secondIntersection, road.getRoadClass());
      newRoad.setRoadPoints(pointsAfter);
      return newIntersection;
   }

   /**
    * Join two roads into one. The roads must be the only two roads at the
    * intersection and must be of the same class. If the roads are not distinct,
    * then we quietly delete the road and remove the intersection. The roads
    * might not be distinct if they form an isolated loop (such as a racetrack).
    * Thus situation is particularly problematic if the road has no inner
    * points. The RoadPoints of both roads are concatenated with a new RoadPoint
    * where the intersection was between them. This code is a little complicated
    * because the RoadPoints must be assembled in a valid order. The order used
    * is to start from the other intersection of the first road to the given
    * intersection and from the given intersection of the second road to the
    * other intersection of the second road.
    */
   public void joinRoads(Intersection intersection) {
      if (intersection.getRoadCount() != 2)
         throw new IllegalArgumentException("Illegal intersection conversion");
      Road road0 = intersection.getRoad(0);
      Road road1 = intersection.getRoad(1);
      if (road0 == road1) {
         roads.remove(road1);
         intersections.remove(intersection);
         return;
      }
      if (road0.getRoadClass() != road1.getRoadClass())
         throw new IllegalArgumentException("Illegal intersection conversion");
      intersections.remove(intersection);
      roads.remove(road1);
      RoadPoint[] road0Points = road0.getRoadPoints();
      RoadPoint[] road1Points = road1.getRoadPoints();
      RoadPoint[] innerPoints = new RoadPoint[road0Points.length + road1Points.length + 1];
      int j = 0;
      Intersection firstIntersection;
      if (intersection == road0.getFirstIntersection()) {
         firstIntersection = road0.getSecondIntersection();
         for (int i = road0Points.length; --i >= 0;)
            innerPoints[j++] = road0Points[i];
      } else {
         firstIntersection = road0.getFirstIntersection();
         System.arraycopy(road0Points, 0, innerPoints, j, road0Points.length);
         j += road0Points.length;
      }
      Intersection otherIntersection = road1.getOtherIntersection(intersection);
      otherIntersection.removeRoad(road1);
      road0.setIntersections(firstIntersection, otherIntersection);
      otherIntersection.addRoad(road0);
      innerPoints[j++] = new RoadPoint(road0, intersection.getLocation(), this);
      if (intersection == road1.getFirstIntersection()) {
         System.arraycopy(road1Points, 0, innerPoints, j, road1Points.length);
         j += road1Points.length;
      } else {
         for (int i = road1Points.length; --i >= 0;)
            innerPoints[j++] = road1Points[i];
      }
      road0.setRoadPoints(innerPoints);
      road0.setName(mergeRoadNames(road0.getName(), road1.getName()));
   }

   protected String mergeRoadNames(String name0, String name1) {
      return name0 + "+" + name1;
   }

   public RoadClass findRoadClass(Object className) {
      RoadClass cl_ss = (RoadClass) roadClasses.get(className);
      if (cl_ss == null)
         return defaultRoadClass;
      return cl_ss;
   }

   public int findUnusedRoadID() {
      return roads.findUnusedID();
   }

   /**
    * Displays a Route between two points on the map.
    * <p>
    * 
    * @param start start from start point on map
    * @param end to end point on map
    * @param route the Route to travel from start to end
    * @param segments as side effect, populated with PathSegments between
    *        returned WayPoints
    * @return List of WayPoints
    */
   public List displayPathOnRoad(Point start, Point end, Route route, List segments) {
      List newPoints;
      try {
         if (route == null) {
            OMPoint point = new RedPoint(start.x, start.y, 5);
            toDraw.add(point);
            point = new RedPoint(end.x, end.y, 5);
            toDraw.add(point);

            return null;
         }

         if (drawResults) {
            OMPoint point = new YellowPoint(start.x, start.y, 10);
            toDraw.add(point);
            point = new YellowPoint(end.x, end.y, 10);
            toDraw.add(point);
         }

         newPoints = new ArrayList();
         populatePointsAndSegments(route, newPoints, segments);

         if (drawResults) {
            Point last = null;
            Point first = null;
            for (Iterator iter = newPoints.iterator(); iter.hasNext();) {
               Point pt = (Point) iter.next();
               if (last != null) {
                  OMLine line = new BlueLine(last.x, last.y, pt.x, pt.y);
                  toDraw.add(line);
               }
               if (first == null)
                  first = pt;
               last = pt;
            }

            if (first != null && last != null) {
               // draw line from start to beginning intersection
               OMLine line = new YellowLine(start.x, start.y, first.x, first.y, 10);
               toDraw.add(line);
               line = new YellowLine(last.x, last.y, end.x, end.y, 10);
               toDraw.add(line);
            }
         }
      } catch (Exception e) {
         logger.warning("Got exception " + e);
         e.printStackTrace();
         return null;
      }

      return newPoints;

   }

   /**
    * Finds closest intersection to start and end find path from start
    * intersection to end intersection
    * <p>
    * 
    * This method works on screen coordinates.
    * 
    * @param start from start point on map
    * @param end to end point on map
    * @param segments as side effect, populated with PathSegments between
    *        returned WayPoints
    * @return List of WayPoints
    */
   public List getPathOnRoad(Point start, Point end, List segments) {
      List newPoints;
      try {
         Route bestRoute = getRouteBetweenPoints(start, end);
         newPoints = displayPathOnRoad(start, end, bestRoute, segments);
      } catch (Exception e) {
         logger.warning("Got exception " + e);
         e.printStackTrace();
         return null;
      }

      return newPoints;
   }

   /**
    * a red point for displaying when we can't find a route between two points
    */
   protected class RedPoint
         extends OMPoint {
      public RedPoint(int x, int y, int radius) {
         super(x, y, radius);
      }

      public void render(Graphics g) {
         setGraphicsColor(g, Color.RED);
         draw(g, getShape());
      }
   }

   /** a blue line to indicate the found route */
   protected class BlueLine
         extends OMLine {
      int width;

      public BlueLine(int x, int y, int x2, int y2) {
         super(x, y, x2, y2);
         this.width = 5;
      }

      public void render(Graphics g) {
         float[] dash1 = new float[width + 1];
         dash1[0] = 10.f;

         for (int i = 1; i < width; i++) {
            dash1[i] = 2.0f;
         }

         BasicStroke dashed = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
         ((Graphics2D) g).setStroke(dashed);
         setGraphicsColor(g, Color.BLUE);
         draw(g, getShape());
      }
   }

   /**
    * Returns best Route between two points specified by latitude and longitude.
    * <p>
    * 
    * This method works on latitude/longitude coordinates.
    * 
    * @return Route between two points
    */
   public Route getPathOnRoad(LatLonPoint start, LatLonPoint end) {
      Intersection startTemp = findClosestIntersection(start);
      Intersection endTemp = findClosestIntersection(end);

      Route bestRoute = null;

      if (startTemp != null && endTemp != null) {
         if (roadClasses == null) {
            logger.warning("huh? road classes is null???");
            return bestRoute;
         }

         bestRoute = Route.getBestRoute(startTemp, endTemp, roadClasses.getBestConvoySpeed(), roadClasses.getWorstConvoySpeed());
      }

      if (bestRoute == null) {
         if (logger.isLoggable(Level.INFO))
            logger.info("no route from " + startTemp + " to " + endTemp);
      } else {
         if (logger.isLoggable(Level.INFO))
            logger.info("route from " + startTemp + " to " + endTemp + " is " + bestRoute);
      }

      // post condition check
      if (logger.isLoggable(Level.INFO) && bestRoute != null) {
         float length = 0;
         for (int i = 0; i < bestRoute.getRoads().length; i++) {
            Road road = bestRoute.getRoads()[i];
            length += road.getLengthInKilometers();
         }

         logger.info("best route from " + bestRoute.getOriginIntersection() + " - start " + start + " to "
               + bestRoute.getDestinationIntersection() + " - end " + end + " was " + length + " kilometers.");
      }

      return bestRoute;
   }

   /**
    * Returns best Route between two points
    * 
    * This method works on latitude/longitude coordinates.
    * 
    * @return Route between two points
    */
   public Route getRouteBetweenPoints(Point start, Point end) {
      return getPathOnRoad(createLatLonPoint(start.x, start.y), createLatLonPoint(end.x, end.y));
   }

   /**
    * Look in intersection Quad Tree for closest intersection to point x,y
    * 
    * @return Intersection closest
    */
   protected Intersection findClosestIntersection(int x, int y) {
      return findClosestIntersection(createLatLonPoint(x, y));
   }

   /**
    * Look in intersection Quad Tree for closest intersection to point at
    * specified latitude and longitude.
    * <p>
    * 
    * @return Intersection closest
    */
   protected Intersection findClosestIntersection(LatLonPoint latLon) {
      Intersection inter = (Intersection) interQuadTree.get(latLon.getLatitude(), latLon.getLongitude());
      if (inter == null)
         logger.warning("no intersection at " + latLon);

      return inter;
   }

   /**
    * Iterates over route, populating points and segments lists. Worries about
    * sequence order of from and to points, i.e. end of one road should be the
    * start of the next. This is not guaranteed by the route, so we have to
    * check.
    * 
    * @param bestRoute route to iterate over.
    * @param newPoints populated with points on the route.
    * @param segments populated with Segments.
    */
   protected void populatePointsAndSegments(Route bestRoute, List newPoints, List segments) {
      Projection proj = getProjection();

      Intersection origin = bestRoute.getOriginIntersection();
      // Intersection dest = bestRoute.getDestinationIntersection();

      if (logger.isLoggable(Level.INFO))
         logger.info("adding " + bestRoute.roads.length + " new roads.");

      Road road = null;

      Intersection from = origin, to = null;

      Set loopSet = new HashSet();
      if (doLoopCheck)
         loopSet.add(origin);
      Set ptSet = new HashSet();
      for (int i = 0; i < bestRoute.roads.length; i++) {
         road = bestRoute.roads[i];

         if (!from.equals(road.getFirstIntersection()) && !from.equals(road.getSecondIntersection())) {
            logger.severe("huh? " + from + " is not an intersection on road " + road);
         }

         Point pt = createPoint((Point) proj.forward(from.getLocation(), new Point()));

         if (doLoopCheck) {
            if (ptSet.contains(pt)) {
               logger.warning("pt set has duplicate at " + pt);
            }
            ptSet.add(pt);
         }

         newPoints.add(pt);
         to = road.getOtherIntersection(from);
         if (doLoopCheck) {
            if (loopSet.contains(to)) {
               logger.warning("road has a cycle at " + to);
            }
            loopSet.add(to);
         }

         // check to see if we need to reverse the order of the
         // road points,
         // which may not be ordered the same as the previous road

         boolean reverse = from.equals(road.getSecondIntersection());
         Segment path = getPathSegment(proj, road, reverse);

         if (logger.isLoggable(Level.INFO))
            logger.info("created path " + path);

         segments.add(path);

         from = to;
      }

      if (to != null) {
         Point pt = createPoint((Point) proj.forward(to.getLocation(), new Point()));
         if (ptSet.contains(pt)) {
            logger.warning("pt set has duplicate at " + pt);
         }

         newPoints.add(pt);

         if (logger.isLoggable(Level.INFO))
            logger.info(" now " + newPoints.size() + " points and " + segments.size() + " segments.");
      }
   }

   /**
    * Converts a road into a path segment - reverse parameter guarantees the
    * ordering of the points is consistent across multiple path segments in the
    * whole route.
    * 
    * @return PathSegment converted from a road
    */
   protected Segment getPathSegment(Projection proj, Road road, boolean reverse) {
      RoadPoint[] roadPoints = road.getRoadPoints();

      List newPoints = new ArrayList();
      if (reverse) {
         for (int i = roadPoints.length - 1; i > 0; i--) {
            newPoints.add(createPoint((Point) proj.forward(roadPoints[i].getLocation(), new Point())));
         }
      } else {
         for (int i = 0; i < roadPoints.length; i++) {
            newPoints.add(createPoint((Point) proj.forward(roadPoints[i].getLocation(), new Point())));
         }
      }

      return createSegment(newPoints);
   }

   /**
    * Allows subclasses to redefine segments
    */
   protected Segment createSegment(List newPoints) {
      return new Segment(newPoints);
   }

   /**
    * Allows subclasses to redefine points returned
    */
   protected Point createPoint(Point pt) {
      return new Point(pt);
   }

   public Projection getProjection() {
      return layer.getProjection();
   }

   /**
    * Check the integrity of our data structures.
    * 
    * Scan the known intersections. Note intersections with no roads. Scan the
    * roads of the intersection: Each road has two intersections. If the road
    * has already been encountered, then we recorded its "other" intersection
    * and that must match this intersection. If it doesn't match, record an
    * error. If it does match reset its recorded other intersection to be a
    * special marker indicating that both ends of the road have been accounted
    * for. If the road has not already been encountered, then record its "other"
    * intersection. Scan the known roads. Every road should accounted for in the
    * "other" intersection table and should be marked as having both
    * intersections accounted for. Note the roads which were not found in the
    * first scan and the roads which were found, but for which both
    * intersections were not found. Remark every road. Finally scan the other
    * intersection table for entries which were not marked as being in the roads
    * vector.
    */
   protected void checkIntegrity() {
      // CharArrayWriter errorWriter = new CharArrayWriter();
      // PrintWriter errors = new PrintWriter(errorWriter);
      PrintStream errors = System.err;
      Hashtable otherIntersections = new Hashtable();
      Object bothIntersections = new Object();
      Object inRoadsVector = new Object();
      for (Enumeration e = intersections.elements(); e.hasMoreElements();) {
         Intersection intersection = (Intersection) e.nextElement();
         int nRoads = intersection.getRoadCount();
         if (nRoads == 0) {
            errors.println("Dangling intersection");
            errors.println("  Intersection = " + intersection);
            continue;
         }
         for (int i = 0; i < nRoads; i++) {
            Road road = intersection.getRoad(i);
            Object other = otherIntersections.get(road);
            if (other == null) {
               otherIntersections.put(road, road.getOtherIntersection(intersection));
            } else if (other == intersection) {
               otherIntersections.put(road, bothIntersections);
            } else {
               errors.println("Misconnected");
               errors.println("          Road = " + road);
               errors.println("    Road.Other = " + other);
               errors.println("  Intersection = " + intersection);
            }
         }
      }

      for (Enumeration e = roads.elements(); e.hasMoreElements();) {
         Road road = (Road) e.nextElement();
         Object other = otherIntersections.get(road);
         if (other == null) {
            errors.println("Road not found in intersections");
            errors.println("          Road = " + road);
         } else if (other != bothIntersections) {
            errors.println("Road incompletely connected");
            errors.println("          Road = " + road);
            errors.println("    Road.Other = " + other);
         } else if (other == inRoadsVector) {
            errors.println("Road doubly listed");
            errors.println("          Road = " + road);
         }
         otherIntersections.put(road, inRoadsVector);
      }
      for (Enumeration e = otherIntersections.keys(); e.hasMoreElements();) {
         Road road = (Road) e.nextElement();
         Object other = otherIntersections.get(road);
         if (other != inRoadsVector) {
            errors.println("Road not listed");
            errors.println("          Road = " + road);
         }
      }
      // String errString = errorWriter.toString();
      // if (errString.isEmpty())
      // return;
      // JTextArea text = new JTextArea(errString);
      // JScrollPane scrollPane = new JScrollPane(text);
      // final JFrame dialog = new JFrame("Errors");
      // JButton ok = new JButton("OK");
      // ok.addActionListener(new ActionListener() {
      // public void actionPerformed(ActionEvent e) {
      // dialog.dispose();
      // }
      // });
      // dialog.getContentPane().add(scrollPane,
      // BorderLayout.CENTER);
      // dialog.getContentPane().add(ok, BorderLayout.SOUTH);
      // dialog.setSize(new java.awt.Dimension(640, 480));
      // dialog.setVisible(true);
   }

   static class RoadVector {
      Road[] roads = new Road[0];
      private int look = 0;
      private int roadCount = 0;

      public void clear() {
         for (int i = 0; i < roads.length; i++)
            roads[i] = null;
         look = 0;
         roadCount = 0;
      }

      public void add(Road r) {
         int id = r.getID();
         if (id >= roads.length) {
            Road[] oldRoads = roads;
            roads = new Road[id + 100 + roads.length];
            System.arraycopy(oldRoads, 0, roads, 0, oldRoads.length);
            for (int i = oldRoads.length; i < roads.length; i++)
               roads[i] = null;
         }
         if (roads[id] == null)
            roadCount++;
         roads[id] = r;
      }

      public void remove(Road r) {
         int id = r.getID();
         if (roads[id] != null) {
            roads[id] = null;
            if (id < look)
               look = id;
            --roadCount;
         }
      }

      public int findUnusedID() {
         while (look < roads.length && roads[look] != null) {
            look++;
         }
         return look;
      }

      public Road elementAt(int n) {
         return roads[n];
      }

      public Enumeration elements() {
         return new Enumeration() {
            private int i = 0;

            public boolean hasMoreElements() {
               for (; i < roads.length; i++) {
                  if (roads[i] != null)
                     return true;
               }
               return false;
            }

            public Object nextElement() {
               return roads[i++];
            }
         };
      }

      public int size() {
         return roadCount;
      }
   }

   public static class Intersections {
      private Hashtable intersections = new Hashtable();

      public void put(Intersection intersection) {
         int suffix = 0;
         String name = intersection.getName();
         while (intersections.containsKey(name)) {
            suffix++;
            name = intersection.getName() + "," + suffix;
         }
         intersection.setName(name);
         intersections.put(name, intersection);
      }

      public void remove(Intersection intersection) {
         intersections.remove(intersection.getName());
      }

      public Intersection get(String name) {
         return (Intersection) intersections.get(name);
      }

      public Enumeration elements() {
         return intersections.elements();
      }

      public boolean contains(Intersection intersection) {
         return intersections.get(intersection.getName()) == intersection;
      }

      public void clear() {
         intersections.clear();
      }

      public int size() {
         return intersections.size();
      }
   }

   public static class RoadClasses
         extends Hashtable {
      float bestConvoySpeed = 0.0f;
      float worstConvoySpeed = Float.MAX_VALUE;

      public void put(RoadClass roadClass) {
         put(roadClass.getName(), roadClass);
         if (roadClass.getConvoySpeed() > bestConvoySpeed)
            bestConvoySpeed = roadClass.getConvoySpeed();
         if (roadClass.getConvoySpeed() < worstConvoySpeed)
            worstConvoySpeed = roadClass.getConvoySpeed();
      }

      public float getBestConvoySpeed() {
         return bestConvoySpeed;
      }

      public float getWorstConvoySpeed() {
         return worstConvoySpeed;
      }
   }

   /** BOZO remove me */
   public boolean isEditing() {
      return false;
   }
}