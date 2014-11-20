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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/event/NavMouseMode.java,v $
// $RCSfile: NavMouseMode.java,v $
// $Revision: 1.12 $
// $Date: 2007/02/12 17:36:26 $
// $Author: dietrick $
// 
// **********************************************************************
package com.bbn.openmap.event;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Properties;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The Navigation Mouse Mode interprets mouse clicks and mouse drags to recenter
 * and rescale the map. The map is centered on the location where a click
 * occurs. If a box is drawn by clicking down and dragging the mouse, the map is
 * centered on the dot in the center of the box, and the scale is adjusted so
 * the screen fills the area designated by the box.
 * <p>
 * You MUST add this MouseMode as a ProjectionListener to the MapBean to get it
 * to work. If you use a MouseDelegator with the bean, it will take care of that
 * for you.
 */
public class NavMouseMode extends CoordMouseMode {

    /**
     * Mouse Mode identifier, which is "Navigation".
     */
    public final static transient String modeID = "Navigation";
    protected Point point1, point2;
    protected boolean autoZoom = false;
    MapBean theMap = null;

    /**
     * DrawingAttributes to use for drawn rectangle. Fill paint will be used for
     * XOR color, line paint will be used for paint color.
     */
    protected DrawingAttributes rectAttributes = DrawingAttributes.getDefaultClone();

    /**
     * Construct a NavMouseMode. Sets the ID of the mode to the modeID, the
     * consume mode to true, and the cursor to the crosshair.
     */
    public NavMouseMode() {
        this(true);
        rectAttributes.setLinePaint(Color.GRAY);
        rectAttributes.setMattingPaint(Color.LIGHT_GRAY);
        rectAttributes.setMatted(true);
    }

    /**
     * Construct a NavMouseMode. Lets you set the consume mode. If the events
     * are consumed, then a MouseEvent is sent only to the first
     * MapMouseListener that successfully processes the event. If they are not
     * consumed, then all of the listeners get a chance to act on the event.
     * 
     * @param shouldConsumeEvents the mode setting.
     */
    public NavMouseMode(boolean shouldConsumeEvents) {
        super(modeID, shouldConsumeEvents);
        // override the default cursor
        setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        rectAttributes.setLinePaint(Color.GRAY);
        rectAttributes.setMattingPaint(Color.LIGHT_GRAY);
        rectAttributes.setMatted(true);
    }

    /**
     * Handle a mousePressed MouseListener event. Erases the old navigation
     * rectangle if there is one, and then keeps the press point for reference
     * later.
     * 
     * @param e MouseEvent to be handled
     */
    public void mousePressed(MouseEvent e) {
        if (Debug.debugging("mousemode")) {
            Debug.output(getID() + "|NavMouseMode.mousePressed()");
        }
        e.getComponent().requestFocus();

        if (!mouseSupport.fireMapMousePressed(e) && e.getSource() instanceof MapBean) {
            // set the new first point
            point1 = e.getPoint();
            // ensure the second point isn't set.
            point2 = null;
            autoZoom = true;
        }
    }

    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();

        super.mouseClicked(e);

        if (!(obj instanceof MapBean) || point1 == null) {
            return;
        }

        MapBean map = (MapBean) obj;
        Projection projection = map.getProjection();
        Proj p = (Proj) projection;

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

    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        if (theMap != null) {
            cleanUp();
        }
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
    public void mouseReleased(MouseEvent e) {
        if (Debug.debugging("mousemode")) {
            Debug.output(getID() + "|NavMouseMode.mouseReleased()");
        }

        if (!mouseSupport.fireMapMouseReleased(e)) {
            handleMouseReleased(e);
        }
    }

    /**
     * Override this method to change what happens when the mouse is released.
     * 
     * @param e MouseEvent
     */
    protected void handleMouseReleased(MouseEvent e) {
        Object obj = e.getSource();

        MapBean map = (MapBean) theMap;
        Point firstPoint = this.point1;
        Point secondPoint = this.point2;

        // point2 is always going to be null for a click.
        if (!(obj == map) || !autoZoom || firstPoint == null || secondPoint == null) {
            return;
        }

        Projection projection = map.getProjection();
        Proj p = (Proj) projection;

        synchronized (this) {

            point2 = getRatioPoint((MapBean) e.getSource(), firstPoint, e.getPoint());
            secondPoint = point2;
            int dx = Math.abs(secondPoint.x - firstPoint.x);
            int dy = Math.abs(secondPoint.y - firstPoint.y);

            // Don't bother redrawing if the rectangle is too small
            if ((dx < 5) || (dy < 5)) {

                // If rectangle is too small in both x and y then
                // recenter the map
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
                } else {
                    cleanUp();
                    map.repaint();
                }
                return;
            }

            // Figure out the new scale
            float newScale = com.bbn.openmap.proj.ProjMath.getScale(firstPoint, secondPoint, projection);

            // Figure out the center of the rectangle
            int centerx = Math.min(firstPoint.x, secondPoint.x) + dx / 2;
            int centery = Math.min(firstPoint.y, secondPoint.y) + dy / 2;
            Point2D center = map.inverse(centerx, centery, null);

            // Fire events on main map to change view to match rect1
            // Debug.output("point1: " +point1);
            // Debug.output("point2: " +point2);
            // Debug.output("Centerx: " +centerx +
            // " Centery: " + centery);
            // Debug.output("New Scale: " + newScale);
            // Debug.output("New Center: " +center);

            // Set the parameters of the projection and then set
            // the projection of the map. This way we save having
            // the MapBean fire two ProjectionEvents.
            p.setScale(newScale);
            p.setCenter(center);

            cleanUp();

            map.setProjection(p);
        }
    }

    /**
     * Handle a mouseEntered MouseListener event. The boolean autoZoom is set to
     * true, which will make the delegate ask the map to zoom in to a box that
     * is drawn.
     * 
     * @param e MouseEvent to be handled
     */
    public void mouseEntered(MouseEvent e) {
        if (Debug.debugging("mousemodedetail")) {
            Debug.output(getID() + "|NavMouseMode.mouseEntered()");
        }
        super.mouseEntered(e);
        autoZoom = true;
    }

    /**
     * Handle a mouseExited MouseListener event. The boolean autoZoom is set to
     * false, which will cause the delegate to NOT ask the map to zoom in on a
     * box. If a box is being drawn, it will be erased. The point1 is kept in
     * case the mouse comes back on the screen with the button still down. Then,
     * a new box will be drawn with the original mouse press position.
     * 
     * @param e MouseEvent to be handled
     */
    public void mouseExited(MouseEvent e) {
        if (Debug.debugging("mousemodedetail")) {
            Debug.output(getID() + "|NavMouseMode.mouseExited()");
        }

        super.mouseExited(e);

        if (theMap == e.getSource()) {
            // don't zoom in, because the mouse is off the window.
            autoZoom = false;
            // set the second point to null so that a new box will be
            // drawn if the mouse comes back, and the box will use the
            // old starting point, if the mouse button is still down.
            point2 = null;
            theMap.repaint();

        }
    }

    // Mouse Motion Listener events
    // /////////////////////////////
    /**
     * Handle a mouseDragged MouseMotionListener event. A rectangle is drawn
     * from the mousePressed point, since I'm assuming that I'm drawing a box to
     * zoom the map to. If a previous box was drawn, it is erased.
     * 
     * @param e MouseEvent to be handled
     */
    public void mouseDragged(MouseEvent e) {
        if (Debug.debugging("mousemodedetail")) {
            Debug.output(getID() + "|NavMouseMode.mouseDragged()");
        }

        super.mouseDragged(e);

        Object obj = e.getSource();

        if (obj instanceof MapBean && theMap == null) {
            theMap = (MapBean) obj;
            theMap.addPaintListener(this);
        }

        MapBean map = this.theMap;
        Point firstPoint = this.point1;

        if (map != null) {
            if (!autoZoom) {
                return;
            }

            point2 = getRatioPoint(map, firstPoint, e.getPoint());
            map.repaint();
        }
    }

    protected void cleanUp() {
        if (theMap != null) {
            theMap.removePaintListener(this);
            theMap = null;
        }
        point1 = null;
        point2 = null;
    }

    /**
     * Given a MapBean, which provides the projection, and the starting point of
     * a box (pt1), look at pt2 to see if it represents the ratio of the
     * projection map size. If it doesn't, provide a point that does. This
     * method signature is provided for backwards compatibility.
     */
    protected Point getRatioPoint(MapBean map, Point pt1, Point pt2) {
        return ProjMath.getRatioPoint(map.getProjection(), pt1, pt2);
    }

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

            if (width == 0) {
                width++;
            }
            if (height == 0) {
                height++;
            }

            Rectangle2D rect1 = new Rectangle2D.Double(pt1.x < pt2.x ? pt1.x : pt2.x, pt1.y < pt2.y ? pt1.y
                    : pt2.y, width, height);
            Rectangle2D rect2 = new Rectangle2D.Double(pt1.x < pt2.x ? pt1.x + (pt2.x - pt1.x) / 2
                    - 1 : pt2.x + (pt1.x - pt2.x) / 2 - 1, pt1.y < pt2.y ? pt1.y + (pt2.y - pt1.y)
                    / 2 - 1 : pt2.y + (pt1.y - pt2.y) / 2 - 1, 2, 2);

            if (theMap != null) {
                rectAttributes.render((Graphics2D) g, theMap.getNonRotatedShape(rect1));
                rectAttributes.render((Graphics2D) g, theMap.getNonRotatedShape(rect2));
            }

        }
    }

    /**
     * Called by the MapBean when it repaints, to let the MouseMode know when to
     * update itself on the map. PaintListener interface.
     */
    public void listenerPaint(Object obj, java.awt.Graphics g) {

        if (theMap == null && obj instanceof MapBean) {
            ((MapBean) obj).removePaintListener(this);
            return;
        }

        Graphics2D graphics = (Graphics2D) g.create();
        if (point1 != null && point2 != null) {
            paintRectangle(graphics, point1, point2);
        }
        graphics.dispose();
    }

    public DrawingAttributes getRectAttributes() {
        return rectAttributes;
    }

    public void setRectAttributes(DrawingAttributes rectAttributes) {
        this.rectAttributes = rectAttributes;
    }

    /**
     * PropertyConsumer interface method.
     */
    public void setProperties(String prefix, Properties setList) {
        super.setProperties(prefix, setList);

        rectAttributes.setProperties(prefix, setList);
    }

    /**
     * PropertyConsumer interface method.
     */
    public Properties getProperties(Properties getList) {
        return rectAttributes.getProperties(getList);
    }

    /**
     * PropertyConsumer interface method.
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        rectAttributes.getPropertyInfo(list);

        list.put(initPropertiesProperty, DrawingAttributes.linePaintProperty + " "
                + DrawingAttributes.mattingPaintProperty + " " + DrawingAttributes.mattedProperty);
        return list;
    }

}
