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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/test/HelloWorldLayer.java,v $
// $RCSfile: HelloWorldLayer.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.test;

import java.awt.Color;
import java.awt.Graphics;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;

/**
 * Layer objects are components which can be added to the MapBean to
 * make a map.
 * <p>
 * Layers implement the ProjectionListener interface to listen for
 * ProjectionEvents. When the projection changes, they may need to
 * refetch, regenerate their graphics, and then repaint themselves
 * into the new view.
 */
public class HelloWorldLayer extends Layer {

    protected OMGraphicList graphics;

    /**
     * Construct the layer.
     */
    public HelloWorldLayer() {
        super();
        graphics = new OMGraphicList(10);
        createGraphics(graphics);
    }

    /**
     * Sets the properties for the <code>Layer</code>. This allows
     * <code>Layer</code> s to get a richer set of parameters than
     * the <code>setArgs</code> method.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, java.util.Properties props) {
        super.setProperties(prefix, props);
    }

    /**
     * Invoked when the projection has changed or this Layer has been
     * added to the MapBean.
     * 
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
        graphics.generate(e.getProjection());
        repaint();
    }

    /**
     * Paints the layer.
     * 
     * @param g the Graphics context for painting
     */
    public void paint(Graphics g) {
        graphics.render(g);
    }

    /**
     * Create graphics.
     */
    protected void createGraphics(OMGraphicList list) {
        // NOTE: all this is very non-optimized...

        OMPoly poly;

        // H
        poly = new OMPoly(new double[] { 10f, -150f, 35f, -150f, 35f, -145f,
                25f, -145f, 25f, -135f, 35f, -135f, 35f, -130f, 10f, -130f,
                10f, -135f, 20f, -135f, 20f, -145f, 10f, -145f, 10f, -150f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // E
        poly = new OMPoly(new double[] { 10f, -120f, 35f, -120f, 35f, -100f,
                30f, -100f, 30f, -115f, 25f, -115f, 25f, -105f, 20f, -105f,
                20f, -115f, 15f, -115f, 15f, -100f, 10f, -100f, 10f, -120f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // L
        poly = new OMPoly(new double[] { 10f, -90f, 35f, -90f, 35f, -85f, 15f,
                -85f, 15f, -75f, 10f, -75f, 10f, -90f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // L
        poly = new OMPoly(new double[] { 10f, -70f, 35f, -70f, 35f, -65f, 15f,
                -65f, 15f, -55f, 10f, -55f, 10f, -70f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // O
        poly = new OMPoly(new double[] { 10f, -50f, 35f, -50f, 35f, -30f, 10f,
                -30f, 10f, -50f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { 15f, -45f, 30f, -45f, 30f, -35f, 15f,
                -35f, 15f, -45f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { 10f, -50f, 35f, -50f, 35f, -30f, 10f,
                -30f, 10f, -45f, 15f, -45f, 15f, -35f, 30f, -35f, 30f, -45f,
                10f, -45f, 10f, -50f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(OMGraphic.clear);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // W
        poly = new OMPoly(new double[] { -35f, -5f, -10f, -5f, -10f, 0f, -25f,
                0f, -25f, 5f, -20f, 5f, -20f, 10f, -25f, 10f, -25f, 15f, -10f,
                15f, -10f, 20f, -35f, 20f, -35f, 10f, -30f, 10f, -30f, 5f,
                -35f, 5f, -35f, -5f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // O
        poly = new OMPoly(new double[] { -35f, 30f, -10f, 30f, -10f, 50f, -35f,
                50f, -35f, 30f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { -30f, 35f, -15f, 35f, -15f, 45f, -30f,
                45f, -30f, 35f, }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { -35f, 30f, -10f, 30f, -10f, 50f, -35f,
                50f, -35f, 35f, -30f, 35f, -30f, 45f, -15f, 45f, -15f, 35f,
                -35f, 35f, -35f, 30f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(OMGraphic.clear);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // R
        poly = new OMPoly(new double[] { -35f, 60f, -10f, 60f, -10f, 75f, -20f,
                75f, -25f, 70f, -30f, 80f, -35f, 80f, -35f, 75f, -30f, 70f,
                -30f, 65f, -35f, 65f, -35f, 60f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { -20f, 65f, -15f, 65f, -15f, 70f, -20f,
                70f, -20f, 65f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { -35f, 60f, -10f, 60f, -10f, 75f, -20f,
                75f, -25f, 70f, -30f, 80f, -35f, 80f, -35f, 75f, -30f, 70f,
                -30f, 65f, -20f, 65f, -20f, 70f, -15f, 70f, -15f, 65f, -35f,
                65f, -35f, 60f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(OMGraphic.clear);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // L
        poly = new OMPoly(new double[] { -35f, 90f, -10f, 90f, -10f, 95f, -30f,
                95f, -30f, 105f, -35f, 105f, -35f, 90f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(Color.green);
        list.add(poly);

        // D
        poly = new OMPoly(new double[] { -35f, 110f, -10f, 110f, -10f, 125f,
                -15f, 130f, -30f, 130f, -35f, 125f, -35f, 110f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { -30f, 115f, -15f, 115f, -15f, 120f,
                -20f, 125f, -25f, 125f, -30f, 120f, -30f, 115f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(Color.black);
        poly.setFillPaint(OMGraphic.clear);
        list.add(poly);
        poly = new OMPoly(new double[] { -35f, 110f, -10f, 110f, -10f, 125f,
                -15f, 130f, -30f, 130f, -35f, 125f, -35f, 115f, -30f, 115f,
                -30f, 120f, -25f, 125f, -20f, 125f, -15f, 120f, -15f, 115f,
                -35f, 115f, -35f, 110f }, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB, 32);
        poly.setLinePaint(OMGraphic.clear);
        poly.setFillPaint(Color.green);
        list.add(poly);
    }
}