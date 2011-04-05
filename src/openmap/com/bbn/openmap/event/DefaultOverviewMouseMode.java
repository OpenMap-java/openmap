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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/DefaultOverviewMouseMode.java,v $
// $RCSfile: DefaultOverviewMouseMode.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:09:07 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.gui.OverviewMapHandler;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * A MouseMode that handles drawing a box, or clicking on a point, but directs
 * the updates to the ControlledMapSupport of the overview map handler, instead
 * of the overview MapBean, which would have been the normal behavior.
 */
public class DefaultOverviewMouseMode
      extends NavMouseMode2 {

   OverviewMapHandler overviewMapHandler;

   /**
    * Construct a OverviewMouseMode. Sets the ID of the mode to the modeID, the
    * consume mode to true, and the cursor to the crosshair.
    */
   public DefaultOverviewMouseMode(OverviewMapHandler omh) {
      super(true);
      overviewMapHandler = omh;
   }

   /**
    * Handle a mouseReleased MouseListener event. If there was no drag events,
    * or if there was only a small amount of dragging between the occurrence of
    * the mousePressed and this event, then recenter the source map. Otherwise
    * we get the second corner of the navigation rectangle and try to figure out
    * the best scale and location to zoom in to based on that rectangle.
    * 
    * @param e MouseEvent to be handled
    */
   public void mouseReleased(MouseEvent e) {
      if (Debug.debugging("mousemode")) {
         System.out.println(getID() + "|DefaultOverviewMouseMode.mouseReleased()");
      }
      Object obj = e.getSource();
      if (!mouseSupport.fireMapMouseReleased(e)) {
         if (!(obj instanceof MapBean) || !autoZoom || point1 == null)
            return;
         MapBean map = (MapBean) obj;
         Projection projection = map.getProjection();

         synchronized (this) {
            point2 = getRatioPoint(map, point1, e.getPoint());
            int dx = Math.abs(point2.x - point1.x);
            int dy = Math.abs(point2.y - point1.y);

            // Don't bother redrawing if the rectangle is too small
            if ((dx < 5) || (dy < 5)) {
               // clean up the rectangle, since point2 has the
               // old value.
               paintRectangle(map, point1, point2);

               // If rectangle is too small in both x and y then
               // recenter the map
               if ((dx < 5) && (dy < 5)) {
                  Point2D llp = projection.inverse(e.getPoint());
                  overviewMapHandler.getControlledMapListeners().setCenter(llp);
               }
               return;
            }

            // Figure out the new scale
            Point2D ll1 = projection.inverse(point1);
            Point2D ll2 = projection.inverse(point2);

            float deltaDegrees;
            int deltaPix;
            dx = Math.abs(point2.x - point1.x);
            dy = Math.abs(point2.y - point1.y);

            if (dx < dy) {
               float dlat = (float) Math.abs(ll1.getY() - ll2.getY());
               deltaDegrees = dlat * 2;
               deltaPix = overviewMapHandler.getSourceMap().getProjection().getHeight();
            } else {
               float dlon;
               float lat1, lon1, lon2;

               // point1 is to the right of point2. switch the
               // LatLonPoints so that ll1 is west (left) of ll2.
               if (point1.x > point2.x) {
                  lat1 = (float) ll1.getY();
                  lon1 = (float) ll1.getX();
                  ll1.setLocation(ll2);
                  ll2.setLocation(lon1, lat1);
               }

               lon1 = (float) ll1.getX();
               lon2 = (float) ll2.getX();

               // allow for crossing dateline
               if (lon1 > lon2) {
                  dlon = (180 - lon1) + (180 + lon2);
               } else {
                  dlon = lon2 - lon1;
               }

               deltaDegrees = dlon * 2;
               deltaPix = overviewMapHandler.getSourceMap().getProjection().getWidth();
            }

            if (projection instanceof GeoProj) {
               double pixPerDegree = ((GeoProj) projection).getPlanetPixelCircumference() / 360;
               double newScale = pixPerDegree / (deltaPix / deltaDegrees);
               overviewMapHandler.getControlledMapListeners().setScale((float) newScale);
            } // else what??? TODO

            // Figure out the center of the rectangle
            Point2D center = projection.inverse(point1.x, point1.y);

            // Set the parameters of the projection and then set
            // the projection of the map. This way we save having
            // the MapBean fire two ProjectionEvents.

            overviewMapHandler.getControlledMapListeners().setCenter(center);
         }
         // reset the points
         point1 = null;
         point2 = null;
      }
   }

   /**
    * Get the projection of the source map and the starting point of
    * a box (pt1), look at pt2 to see if it represents the ratio of the
    * projection map size. If it doesn't, provide a point that does.
    */
   protected Point getRatioPoint(MapBean map, Point pt1, Point pt2) {
      return ProjMath.getRatioPoint(overviewMapHandler.getSourceMap().getProjection(), pt1, pt2);
   }
}