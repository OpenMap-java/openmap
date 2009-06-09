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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/examples/crew/RouteLayer.java,v $
// $RCSfile: RouteLayer.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.examples.crew;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.bbn.openmap.Layer;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;

/**
 * A sample Layer implementation. It demonstrates how to write a Layer
 * that can be added to the MapBean.
 * <p>
 * The key elements are the <code>paint</code> and
 * <code>projectionChanged</code> methods.
 * <p>
 * <code>paint</code> renders the <code>Layer</code> on the map.
 * <p>
 * <code>projectionChanged</code> is called whenever the map's
 * projection changes. The Layer should update its internal state to
 * reflect the new map view. This may mean acquiring new data from a
 * data source, such as a database, or it may be as simple as
 * re-projecting existing graphics. The <code>RouteLayer</code> does
 * the latter. A Layer that simply displays a legend on top of the map
 * might not do anything in the <code>projectionChanged</code>
 * method since its display may be independent of the current
 * projection.
 */
public class RouteLayer extends Layer implements MapMouseListener {

    /** A list of graphics to be painted on the map. */
    private OMGraphicList omgraphics;

    /** The current projection. */
    private Projection projection;

    /** The currently selected graphic. */
    private OMGraphic selectedGraphic;

    /**
     * Construct a default route layer. Initializes omgraphics to a
     * new OMGraphicList, and invokes createGraphics to create the
     * canned list of routes.
     */
    public RouteLayer() {
        omgraphics = new OMGraphicList();
        createGraphics(omgraphics);
    }

    /**
     * Creates an OMLine from the given parameters.
     * 
     * @param lat1 The line's starting latitude
     * @param lng1 The line's starting longitude
     * @param lat2 The line's ending latitude
     * @param lng2 The line's ending longitude
     * @param lineType The line's type
     * @param color The line's color
     * @param selColor The line's selected color
     * 
     * @return An OMLine with the given properties
     */
    public OMLine createLine(float lat1, float lng1, float lat2, float lng2,
                             int lineType, Color color, Color selColor) {
        OMLine line = new OMLine(lat1, lng1, lat2, lng2, lineType);
        line.setLinePaint(color);
        line.setSelectPaint(selColor);
        return line;
    }

    /**
     * Clears and then fills the given OMGraphicList. Creates three
     * lines for display on the map.
     * 
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     */
    public OMGraphicList createGraphics(OMGraphicList graphics) {

        graphics.clear();

        graphics.add(createLine(42.0f,
                -71.0f,
                35.5f,
                -120.5f,
                OMGraphic.LINETYPE_GREATCIRCLE,
                Color.red,
                Color.yellow));
        graphics.add(createLine(28.0f,
                -81.0f,
                47.0f,
                -122.0f,
                OMGraphic.LINETYPE_GREATCIRCLE,
                Color.green,
                Color.yellow));
        graphics.add(createLine(22.6f,
                -101.0f,
                44.0f,
                -70.0f,
                OMGraphic.LINETYPE_GREATCIRCLE,
                Color.blue,
                Color.yellow));

        return graphics;
    }

    //----------------------------------------------------------------------
    // Layer overrides
    //----------------------------------------------------------------------

    /**
     * Renders the graphics list. It is important to make this routine
     * as fast as possible since it is called frequently by Swing, and
     * the User Interface blocks while painting is done.
     */
    public void paint(Graphics g) {
        omgraphics.render(g);
    }

    /**
     * Returns self as the <code>MapMouseListener</code> in order to
     * receive <code>MapMouseEvent</code>s. If the implementation
     * would prefer to delegate <code>MapMouseEvent</code>s, it
     * could return the delegate from this method instead.
     * 
     * @return The object to receive <code>MapMouseEvent</code> s or
     *         null if this layer isn't interested in
     *         <code>MapMouseEvent</code> s
     */
    public MapMouseListener getMapMouseListener() {
        return this;
    }

    //----------------------------------------------------------------------
    // ProjectionListener interface implementation
    //----------------------------------------------------------------------

    /**
     * Handler for <code>ProjectionEvent</code>s. This function is
     * invoked when the <code>MapBean</code> projection changes. The
     * projection is stored, the graphics are reprojected and the
     * Layer is repainted.
     * <p>
     * It is important to store a <bold>clone </bold> of the
     * <code>Projection</code> and not the given
     * <code>Projection</code> since the <code>Projection</code>
     * given is the actual
     * <code>MapBean</code> <code>Projection</code>. This will
     * ensure that no other objects will be able to manipulate the
     * <code>Projection</code> held by this <code>Layer</code>.
     * 
     * @param e the projection event
     */
    public void projectionChanged(ProjectionEvent e) {
        projection = (Projection) e.getProjection().makeClone();
        omgraphics.project(projection, true);
        repaint();
    }

    //----------------------------------------------------------------------
    // MapMouseListener interface implementation
    //----------------------------------------------------------------------

    /**
     * Indicates which mouse modes should send events to this
     * <code>Layer</code>.
     * 
     * @return An array mouse mode names
     * 
     * @see com.bbn.openmap.event.MapMouseListener
     * @see com.bbn.openmap.MouseDelegator
     */
    public String[] getMouseModeServiceList() {
        String[] ret = new String[1];
        ret[0] = SelectMouseMode.modeID; // "Gestures"
        return ret;
    }

    /**
     * Called whenever the mouse is pressed by the user and one of the
     * requested mouse modes is active.
     * 
     * @param e the press event
     * @return true if event was consumed(handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mousePressed(MouseEvent e) {
        return false;
    }

    /**
     * Called whenever the mouse is released by the user and one of
     * the requested mouse modes is active.
     * 
     * @param e the release event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseReleased(MouseEvent e) {
        return false;
    }

    /**
     * Called whenever the mouse is clicked by the user and one of the
     * requested mouse modes is active.
     * 
     * @param e the click event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseClicked(MouseEvent e) {
        if (selectedGraphic != null) {
            switch (e.getClickCount()) {
            case 1:
                System.out.println("Show Info: " + selectedGraphic);
                break;
            case 2:
                System.out.println("Request URL: " + selectedGraphic);
                break;
            default:
                break;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called whenever the mouse enters this layer and one of the
     * requested mouse modes is active.
     * 
     * @param e the enter event
     * @see #getMouseModeServiceList
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Called whenever the mouse exits this layer and one of the
     * requested mouse modes is active.
     * 
     * @param e the exit event
     * @see #getMouseModeServiceList
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Called whenever the mouse is dragged on this layer and one of
     * the requested mouse modes is active.
     * 
     * @param e the drag event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseDragged(MouseEvent e) {
        return false;
    }

    /**
     * Called whenever the mouse is moved on this layer and one of the
     * requested mouse modes is active.
     * <p>
     * Tries to locate a graphic near the mouse, and if it is found,
     * it is highlighted and the Layer is repainted to show the
     * highlighting.
     * 
     * @param e the move event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseMoved(MouseEvent e) {
        OMGraphic newSelectedGraphic = omgraphics.selectClosest(e.getX(),
                e.getY(),
                2.0f);
        if (newSelectedGraphic != selectedGraphic) {
            selectedGraphic = newSelectedGraphic;
            repaint();
        }
        return true;
    }

    /**
     * Called whenever the mouse is moved on this layer and one of the
     * requested mouse modes is active, and the gesture is consumed by
     * another active layer. We need to deselect anything that may be
     * selected.
     * 
     * @see #getMouseModeServiceList
     */
    public void mouseMoved() {
        omgraphics.deselect();
        repaint();
    }

}