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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MapWindow.java,v $
// $RCSfile: MapWindow.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.Environment;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The MapWindow is a window with the basics - MapBean,
 * MouseDelegator, SelectMouseMode, LayerHandler. It can be used to
 * draw stuff into if you add a layer to it that handles what you want
 * to render. The SelectMouseMode will automatically direct mouse
 * events to the layer if it is set up to receive "Gestures".
 * <P>
 * 
 * If you want to add an object that needs to set itself to a
 * particular location on the frame, i.e. the ToolPanel or
 * InformationDelegator, you have to do the layout management yourself
 * with the MapWindow. The OpenMapFrame has an idea of where it wants
 * those components to go, but the MapWindow doesn't.
 */
public class MapWindow extends JFrame {

    /** The MapHandler BeanContext. */
    protected MapHandler mapHandler = new MapHandler();
    /** The MapBean. */
    protected MapBean map;

    public MapWindow() {
        super();
    }

    public MapWindow(String windowTitle) {
        super(windowTitle);
        try {
            map = new BufferedMapBean();
            mapHandler.add(new MouseDelegator());
            mapHandler.add(new LayerHandler());
            mapHandler.add(map);
            getContentPane().add(map, BorderLayout.CENTER);
        } catch (MultipleSoloMapComponentException msmce) {
        }
    }

    /** Add an object to the internal MapHandler. */
    public void addTo(Object obj) throws MultipleSoloMapComponentException {
        mapHandler.add(obj);
    }

    /** Remove an object from the internal MapHandler. */
    public void removeFrom(Object obj) {
        mapHandler.remove(obj);
    }

    /** Get the MapHandler. */
    public MapHandler getMapHandler() {
        return mapHandler;
    }

    /** Get the map. */
    public MapBean getMap() {
        return map;
    }

    public void setProjection(Projection proj) {
        map.setProjection(proj);
    }

    public Projection getProjection() {
        return map.getProjection();
    }

    public static void main(String[] argv) {
        Environment.init();
        Debug.init();

        MapBean.suppressCopyright = true;
        MapWindow mw = new MapWindow("OpenMap's MapWindow");

        mw.setSize(500, 500);

        try {
            mw.addTo(new com.bbn.openmap.layer.shape.ShapeLayer("share/data/shape/dcwpo-browse.shp"));
            mw.addTo(new com.bbn.openmap.event.NavMouseMode2());
            mw.addTo(new com.bbn.openmap.gui.ToolPanel());
            mw.addTo(new com.bbn.openmap.gui.OMToolSet());

            // If you want to add a ToolPanel, or an
            // InformationDelegator, or anything that needs to know
            // about the layout of the MapWindow frame, then you have
            // to do the layout management yourself. For example:

            //          ToolPanel toolPanel = new ToolPanel();
            //          toolPanel.setFloatable(false);
            //          mw.getContentPane().add(toolPanel, BorderLayout.NORTH);

        } catch (MultipleSoloMapComponentException memce) {

        }

        mw.setVisible(true);
    }

}