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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/examples/simple/RouteLayer.java,v $
// $RCSfile: RouteLayer.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.examples.simple;

import java.awt.Color;

import com.bbn.openmap.Layer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.event.ProjectionEvent;

/**
 * A Layer to display hypothetical transportation routes.
 * <p>
 * This example shows:
 * <ul>
 * <li>OMGraphicList use
 * <li>OMLine use
 * </ul>
 */
public class RouteLayer extends Layer {

    /**
     * A list of graphics to be painted on the map.
     */
    private OMGraphicList omgraphics;

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
     * @param lon1 The line's starting longitude
     * @param lat2 The line's ending latitude
     * @param lon2 The line's ending longitude
     * @param color The line's color
     * 
     * @return An OMLine with the given properties
     */
    public OMLine createLine(float lat1, float lon1, float lat2, float lon2,
                             Color color) {
        OMLine line = new OMLine(lat1, lon1, lat2, lon2, OMGraphic.LINETYPE_GREATCIRCLE);
        line.setLinePaint(color);
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
                Color.red));
        graphics.add(createLine(28.0f,
                -81.0f,
                47.0f,
                -122.0f,
                Color.green));
        graphics.add(createLine(22.6f,
                -101.0f,
                44.0f,
                -70.0f,
                Color.blue));

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
    public void paint(java.awt.Graphics g) {
        omgraphics.render(g);
    }

    //----------------------------------------------------------------------
    // ProjectionListener interface implementation
    //----------------------------------------------------------------------

    /**
     * Handler for <code>ProjectionEvent</code>s. This function is
     * invoked when the <code>MapBean</code> projection changes. The
     * graphics are reprojected and then the Layer is repainted.
     * <p>
     * 
     * @param e the projection event
     */
    public void projectionChanged(ProjectionEvent e) {
        omgraphics.project(e.getProjection(), true);
        repaint();
    }
}