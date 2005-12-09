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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/areas/AreaShapeLayer.java,v $
// $RCSfile: AreaShapeLayer.java,v $
// $Revision: 1.7 $
// $Date: 2005/12/09 21:09:12 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape.areas;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * A Layer to use an AreaHandler to display geographic regions on
 * OpenMap. See the AreaHandler for more information on how to set
 * this layer up.
 * 
 * @see com.bbn.openmap.layer.shape.areas.AreaHandler
 */
public class AreaShapeLayer extends ShapeLayer implements MapMouseListener {

    protected AreaHandler areas;

    /**
     */
    public AreaShapeLayer() {
        super();
    }

    /**
     * Initializes this layer from the given properties.
     * 
     * @param props the <code>Properties</code> holding settings for
     *        this layer
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        areas = new AreaHandler(spatialIndex, drawingAttributes);
        areas.setProperties(prefix, props);
    }

    /**
     * Set the AreaHandler.
     */
    public void setAreas(AreaHandler a) {
        areas = a;
    }

    /**
     * Get the AreaHandler.
     */
    public AreaHandler getAreas() {
        return areas;
    }

    /**
     * Gets the layer graphics.
     * 
     * @return OMGraphicList
     */
    public synchronized OMGraphicList prepare() {

        Projection projection = getProjection();
        Point2D ul = projection.getUpperLeft();
        Point2D lr = projection.getLowerRight();
        double ulLat = ul.getY();
        double ulLon = ul.getX();
        double lrLat = lr.getY();
        double lrLon = lr.getX();

        OMGraphicList list = areas.getGraphics(ulLat, ulLon, lrLat, lrLon);
        list.generate(getProjection(), true);
        return list;
    }

    public void setDrawingAttributes(DrawingAttributes da) {
        areas.setDrawingAttributes(da);
    }

    public DrawingAttributes getDrawingAttributes() {
        return areas.getDrawingAttributes();
    }

    /**
     * Find a PoliticalArea named by the abbreviation
     */
    public PoliticalArea findPoliticalArea(String area_abbrev) {
        return areas.findPoliticalArea(area_abbrev);
    }

    //----------------------------------------------------------------------
    // MapMouseListener interface
    //----------------------------------------------------------------------
    private OMGraphic selectedGraphic;

    public boolean mouseMoved(MouseEvent e) {
        OMGraphicList omgraphics = (OMGraphicList) getList();
        if (omgraphics == null)
            return false;

        OMGraphic newSelectedGraphic = omgraphics.selectClosest(e.getX(),
                e.getY(),
                2.0f);

        if (newSelectedGraphic != selectedGraphic) {
            if (selectedGraphic != null) {
                selectedGraphic.deselect();
            }

            selectedGraphic = newSelectedGraphic;
            if (newSelectedGraphic != null) {
                newSelectedGraphic.select();
                Object obj = newSelectedGraphic.getAppObject();
                if (obj instanceof String) {
                    fireRequestInfoLine((String) obj);
                } else if (obj instanceof Vector) {
                    fireRequestInfoLine(areas.getName((Vector) obj));
                } else if (obj instanceof Integer) {
                    fireRequestInfoLine(areas.getName((Integer) obj));
                } else {
                    fireRequestInfoLine("");
                }
            } else {
                fireRequestInfoLine("");
            }
            repaint();
            return true;
        }

        if (newSelectedGraphic == null) {
            return false;
        } else {
            return true;
        }
    }

    public MapMouseListener getMapMouseListener() {
        return this;
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener. You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
        String[] modes = new String[1];
        modes[0] = com.bbn.openmap.event.SelectMouseMode.modeID;

        return modes;
    }

    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) {
        return false; // did not handle the event
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged. The listener will receive these events if it
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
        return false;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {}

}

