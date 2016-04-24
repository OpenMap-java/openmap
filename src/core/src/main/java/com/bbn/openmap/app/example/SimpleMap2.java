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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.gui.LayersPanel;
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
 * This is the model you should follow if you are starting to use OpenMap and
 * want to create your own application. Use a MapPanel! Use the MapHandler
 * inside it!
 * <p>
 * This example shows:
 * <ul>
 * <li>MapBean
 * <li>MapHandler
 * <li>LayerHandler
 * <li>LayersPanel
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
			 * The OverlayMapPanel automatically creates many default
			 * components, including the MapBean and the MapHandler. You can
			 * extend the BasicMapPanel/Overlay class if you like to add
			 * different functionality or different types of objects. The
			 * OverlayMapPanel has navigation widgets built on top of the map.
			 * Using the standard configuration adds a LayerHandler,
			 * MouseDelegator and OMMouseMode to the OverlayMapPanel.
			 */
			MapPanel mapPanel = OverlayMapPanel.standardConfig();

			/*
			 * The MapHandler is central to this application, although you never
			 * really see it. It's in the MapPanel. Calling addMapComponent(obj)
			 * is the same as calling mapPanel.getMapHandler().add(obj).
			 */

			// Add a ToolPanel for widgets on the north side of the map.
			mapPanel.addMapComponent(new ToolPanel());
			// Add a LayersPanel, which lets you control layers.  addLayerControls() 
			// returns the LayersPanel.
			mapPanel.addMapComponent(new LayersPanel().addLayerControls());

			/*
			 * Create a ShapeLayer to show world political boundaries. Set the
			 * properties of the layer. This assumes that the datafile
			 * "cntry02.shp" is in a path specified in the CLASSPATH variable.
			 * These files are distributed with OpenMap and reside in the top
			 * level "share" sub-directory.
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
			mapPanel.addMapComponent(shapeLayer);
			mapPanel.addMapComponent(new GraticuleLayer());
			mapPanel.addMapComponent(new BasicLayer());

			// Get the default MapBean that the BasicMapPanel created.
			MapBean mapBean = mapPanel.getMapBean();
			// Set the map's center
			mapBean.setCenter(new LatLonPoint.Double(43.0, -95.0));
			// Set the map's scale 1:120 million
			mapBean.setScale(120000000f);

			// Create a Swing frame. The OpenMapFrame knows how to use
			// the MapHandler to locate and place certain objects.
			OpenMapFrame frame = new OpenMapFrame("Simple Map 2");
			// Size the frame appropriately
			frame.setSize(640, 480);

			mapPanel.addMapComponent(frame);

			// If you close the frame, exit the app
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

			// Display the frame
			frame.setVisible(true);

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
