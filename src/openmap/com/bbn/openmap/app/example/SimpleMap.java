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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/examples/simple/SimpleMap.java,v $
// $RCSfile: SimpleMap.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.app.example;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFrame;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.layer.shape.ShapeLayer;

/**
 * This is a simple application that uses the OpenMap MapBean to show
 * a map.
 * <p>
 * This example shows:
 * <ul>
 * <li>MapBean
 * <li>ShapeLayer with political data
 * </ul>
 */
public class SimpleMap {

    public static void main(String args[]) {

        // Create a Swing frame
        JFrame frame = new JFrame("Simple Map");

        // Size the frame appropriately
        frame.setSize(640, 480);

        // Create a MapBean
        MapBean mapBean = new MapBean();

        // Create a ShapeLayer to show world political boundaries.
        // Set the properties of the layer. This assumes that the
        // datafiles "dcwpo-browse.shp" and "dcwpo-browse.ssx" are in
        // a path specified in the CLASSPATH variable. These files
        // are distributed with OpenMap and reside in the toplevel
        // "share" subdirectory.
        ShapeLayer shapeLayer = new ShapeLayer();
        Properties shapeLayerProps = new Properties();
        shapeLayerProps.put("prettyName", "Political Solid");
        shapeLayerProps.put("lineColor", "000000");
        shapeLayerProps.put("fillColor", "BDDE83");
        shapeLayerProps.put("shapeFile", "data/shape/dcwpo-browse.shp");
        shapeLayerProps.put("spatialIndex", "data/shape/dcwpo-browse.ssx");
        shapeLayer.setProperties(shapeLayerProps);

        // Add the political layer to the map
        mapBean.add(shapeLayer);

        // Add the map to the frame
        frame.getContentPane().add(mapBean);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // Display the frame
        frame.setVisible(true);
    }
}