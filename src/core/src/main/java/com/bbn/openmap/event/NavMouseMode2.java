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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/NavMouseMode2.java,v $
// $RCSfile: NavMouseMode2.java,v $
// $Revision: 1.10 $
// $Date: 2006/12/18 20:39:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.event;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The Navigation Mouse Mode interprets mouse clicks and mouse drags to recenter
 * and rescale the map. The map is centered on the location where a click
 * occurs. The difference between this MouseMode and the original NavMouseMode
 * is that the box drawn is interpreted differently. The point where the mouse
 * is pressed is interpreted to be the center of the new zoom area (instead of
 * one of the corners), and the dragged mouse point is the edge of the box,
 * reflected equally on the other side of the center point.
 * 
 * <p>
 * You MUST add this MouseMode as a ProjectionListener to the MapBean to get it
 * to work. If you use a MouseDelegator with the bean, it will take care of that
 * for you.
 */
public class NavMouseMode2 extends NavMouseMode {

    /**
     * Construct a NavMouseMode2. Sets the ID of the mode to the modeID, the
     * consume mode to true, and the cursor to the crosshair.
     */
    public NavMouseMode2() {
        this(true);
    }

    /**
     * Construct a NavMouseMode2. Lets you set the consume mode. If the events
     * are consumed, then a MouseEvent is sent only to the first
     * MapMouseListener that successfully processes the event. If they are not
     * consumed, then all of the listeners get a chance to act on the event.
     * 
     * @param shouldConsumeEvents the mode setting.
     */
    public NavMouseMode2(boolean shouldConsumeEvents) {
        super(shouldConsumeEvents);
    }

    /**
     * Handle a mouseReleased MouseListener event. If there was no drag events,
     * or if there was only a small amount of dragging between the occurrence of
     * the mousePressed and this event, then recenter the map. Otherwise we get
     * the second corner of the navigation rectangle and try to figure out the
     * best scale and location to zoom in to based on that rectangle.
     * 
     * @param e MouseEvent to be handled
     */
    public void handleMouseReleased(MouseEvent e) {
        if (Debug.debugging("mousemode")) {
            Debug.output(getID() + "|NavMouseMode2.mouseReleased()");
        }

        Object obj = e.getSource();

        Point firstPoint = this.point1;
        Point secondPoint = this.point2;
        MapBean map = this.theMap;

        if (!(map == obj) || !autoZoom || firstPoint == null || secondPoint == null) {
            return;
        }

        Projection projection = map.getProjection();
        Proj p = (Proj) projection;

        synchronized (this) {
            point2 = getRatioPoint(map, firstPoint, e.getPoint());
            secondPoint = point2;

            int dx = Math.abs(secondPoint.x - firstPoint.x);
            int dy = Math.abs(secondPoint.y - firstPoint.y);

            // Don't bother redrawing if the rectangle is too small
            if ((dx < 5) || (dy < 5)) {

                // If rectangle is too small in both x and y then
                // re-center the map
                if ((dx < 5) && (dy < 5)) {
                    Point2D llp = map.getCoordinates(e);

                    boolean shift = e.isShiftDown();
                    boolean control = e.isControlDown();

                    if (control) {
                        if (shift) {
                            p.setScale(p.getScale() * 2.0f);
                        } else {
                            p.setScale(p.getScale() / 2.0f);
                        }
                    }

                    cleanUp();

                    p.setCenter(llp);
                    map.setProjection(p);
                }
                return;
            }

            // Figure out the new scale
            dx = Math.abs(secondPoint.x - firstPoint.x);
            dy = Math.abs(secondPoint.y - firstPoint.y);

            // cornerPoint 1 should be the upper left.
            Point cornerPoint1 = new Point(secondPoint.x < firstPoint.x ? secondPoint.x
                    : firstPoint.x, secondPoint.y < firstPoint.y ? secondPoint.y : firstPoint.y);
            Point cornerPoint2 = new Point(cornerPoint1.x + 2 * dx, cornerPoint1.y + 2 * dy);

            float newScale = com.bbn.openmap.proj.ProjMath.getScale(cornerPoint1, cornerPoint2, projection);

            // Figure out the center of the rectangle
            Point2D center = map.inverse(firstPoint.x, firstPoint.y, null);

            // Set the parameters of the projection and then set
            // the projection of the map. This way we save having
            // the MapBean fire two ProjectionEvents.
            p.setScale(newScale);
            p.setCenter(center);
            cleanUp();
            map.setProjection(p);
        }

    }

    // Mouse Motion Listener events
    // /////////////////////////////

    /**
     * Draws or erases boxes between two screen pixel points. The graphics from
     * the map is set to XOR mode, and this method uses two colors to make the
     * box disappear if on has been drawn at these coordinates, and the box to
     * appear if it hasn't.
     * 
     * @param pt1 one corner of the box to drawn, in window pixel coordinates.
     * @param pt2 the opposite corner of the box.
     */
    protected void paintRectangle(Graphics g, Point pt1, Point pt2) {

        if (pt1 != null && pt2 != null) {

            int width = Math.abs(pt2.x - pt1.x);
            int height = Math.abs(pt2.y - pt1.y);

            if (width == 0)
                width++;
            if (height == 0)
                height++;

            Rectangle2D rect1 = new Rectangle2D.Double(pt1.x - width, pt1.y - height, width * 2, height * 2);
            Rectangle2D rect2 = new Rectangle2D.Double(pt1.x - 1, pt1.y - 1, 3, 3);

            if (theMap != null) {
                rectAttributes.render((Graphics2D) g, theMap.getNonRotatedShape(rect1));
                rectAttributes.render((Graphics2D) g, theMap.getNonRotatedShape(rect2));
            }
        }
    }
}