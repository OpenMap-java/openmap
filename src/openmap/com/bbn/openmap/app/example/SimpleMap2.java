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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/examples/simple/SimpleMap2.java,v $
// $RCSfile: SimpleMap2.java,v $
// $Revision: 1.5 $
// $Date: 2005/05/23 19:46:57 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.app.example;

import java.util.Properties;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.event.OMMouseMode;
import com.bbn.openmap.gui.EmbeddedNavPanel;
import com.bbn.openmap.gui.EmbeddedScaleDisplayPanel;
import com.bbn.openmap.gui.MapPanel;
import com.bbn.openmap.gui.OpenMapFrame;
import com.bbn.openmap.gui.OverlayMapPanel;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.learn.BasicLayer;
import com.bbn.openmap.layer.shape.BufferedShapeLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * This is a simple application that uses the OpenMap MapBean to show a map.
 * <p>
 * This example shows:
 * <ul>
 * <li>MapBean
 * <li>MapHandler
 * <li>LayerHandler
 * <li>ShapeLayer with political data
 * <li>GraticuleLayer
 * <li>BasicLayer with some random data
 * <li>Tools to navigate around on the map
 * </ul>
 */
public class SimpleMap2 {

    public SimpleMap2() {

        try {

            /*
             * The BasicMapPanel automatically creates many default components,
             * including the MapBean and the MapHandler. You can extend the
             * BasicMapPanel class if you like to add different functionality or
             * different types of objects.
             */
            MapPanel mapPanel = new OverlayMapPanel();

            // Get the default MapHandler the BasicMapPanel created.
            MapHandler mapHandler = mapPanel.getMapHandler();

            // Get the default MapBean that the BasicMapPanel created.
            MapBean mapBean = mapPanel.getMapBean();

            // Set the map's center
            mapBean.setCenter(new LatLonPoint.Double(43.0, -95.0));

            // Set the map's scale 1:120 million
            mapBean.setScale(120000000f);

            /*
             * Create and add a LayerHandler to the MapHandler. The LayerHandler
             * manages Layers, whether they are part of the map or not.
             * layer.setVisible(true) will add it to the map. The LayerHandler
             * has methods to do this, too. The LayerHandler will find the
             * MapBean in the MapHandler.
             */
            mapHandler.add(new LayerHandler());
            // Add navigation tools over the map
            mapHandler.add(new EmbeddedNavPanel());
            // Add scale display widget over the map
            mapHandler.add(new EmbeddedScaleDisplayPanel());
            // Add MouseDelegator, which handles mouse modes (managing mouse
            // events)
            mapHandler.add(new MouseDelegator());
            // Add OMMouseMode, which handles how the map reacts to mouse
            // movements
            mapHandler.add(new OMMouseMode());
            // Add a ToolPanel for widgets on the north side of the map.
            mapHandler.add(new ToolPanel());

            // Create a Swing frame. The OpenMapFrame knows how to use
            // the MapHandler to locate and place certain objects.
            OpenMapFrame frame = new OpenMapFrame("Simple Map 2");
            // Size the frame appropriately
            frame.setSize(640, 480);

            mapHandler.add(frame);
            // Display the frame
            frame.setVisible(true);

            /*
             * Create a ShapeLayer to show world political boundaries. Set the
             * properties of the layer. This assumes that the datafile
             * "cntry02.shp" is in a path specified in the CLASSPATH variable.
             * These files are distributed with OpenMap and reside in the
             * top level "share" sub-directory.
             */
            ShapeLayer shapeLayer = new BufferedShapeLayer();

            // Since this Properties object is being used just for
            // this layer, the properties do not have to be scoped
            // with marker name.
            Properties shapeLayerProps = new Properties();
            shapeLayerProps.put("prettyName", "Political Solid");
            shapeLayerProps.put("lineColor", "000000");
            shapeLayerProps.put("fillColor", "BDDE83");
            shapeLayerProps.put("shapeFile", "data/shape/cntry02/cntry02.shp");
            shapeLayer.setProperties(shapeLayerProps);
            shapeLayer.setVisible(true);

            // Last on top.
            mapHandler.add(shapeLayer);
            mapHandler.add(new GraticuleLayer());

            mapHandler.add(new BasicLayer());
            
        } catch (MultipleSoloMapComponentException msmce) {
            // The MapHandler is only allowed to have one of certain
            // items. These items implement the SoloMapComponent
            // interface. The MapHandler can have a policy that
            // determines what to do when duplicate instances of the
            // same type of object are added - replace or ignore.

            // In this example, this will never happen, since we are
            // controlling that one MapBean, LayerHandler,
            // MouseDelegator, etc is being added to the MapHandler.
        }
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SimpleMap2();
            }
        });
    }
}