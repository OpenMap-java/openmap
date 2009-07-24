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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/examples/hello/HelloWorld.java,v $
// $RCSfile: HelloWorld.java,v $
// $Revision: 1.5 $
// $Date: 2006/02/27 15:11:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.examples.hello;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.gui.OMToolSet;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * A sample application incorporating the <code>MapHandler</code>
 * and <code>MapBean</code>.
 * <p>
 * Uses a properties file to configure the layers.
 */
public class HelloWorld extends JFrame {

    /** Property for space separated layers. "hello.layers" */
    public static final String layersProperty = "hello.layers";

    /** The name of the resource file. "HelloWorld.properties" */
    public static String helloResources = "HelloWorld.properties";

    /** The message we wish to announce to the user. */
    public static String message = "Hello, World!";

    /**
     * Create a default HelloWorld instance. The instance will use the
     * default properties.
     */
    public HelloWorld() throws MultipleSoloMapComponentException {
        this(new Properties());
    }

    /**
     * Create a HelloWorld instance with the given properties. The
     * properties override the defaults.
     * 
     * @param props The override properties
     */
    public HelloWorld(Properties props)
            throws MultipleSoloMapComponentException {

        // Initialize the parent class (JFrame)
        super("HelloWorld Example");

        // Use a Border layout manager
        getContentPane().setLayout(new BorderLayout());

        // Call quit when the window's close box is clicked.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        System.out.println("Creating MapHandler");
        // Create the BeanContext, known as the MapHandler.
        MapHandler mapHandler = new MapHandler();
        System.out.println("Creating MapBean");
        // Create a MapBean, and add it to the MapHandler.
        MapBean map = new MapBean();

        // Set the map's center property...
        map.setCenter(new LatLonPoint.Double(43.0, -95.0));
        // and scale
        map.setScale(80000000f);

        mapHandler.add(map);
        // Add the map to the JFrame
        getContentPane().add(map, BorderLayout.CENTER);

        System.out.println("Adding MouseEvent support...");
        // Add Mouse handling objects. The MouseDelegator manages the
        // MouseModes, controlling which one receives events from the
        // MapBean. The active MouseMode sends events to the layers
        // that want to receive events from it. The MouseDelegator
        // will find the MapBean in the MapHandler, and hook itself up
        // to it.
        mapHandler.add(new MouseDelegator());

        // Add MouseMode. The MouseDelegator will find it via the
        // MapHandler.

        //  Adding NavMouseMode first makes it active.
        mapHandler.add(new NavMouseMode());

        System.out.println("Creating ToolPanel...");
        // Add the standard panning and zoom GUI to the JFrame.
        // Create the tool...
        mapHandler.add(new OMToolSet());
        // Create the ToolPanel. It will find the OMToolSet in the
        // MapHandler.
        ToolPanel toolPanel = new ToolPanel();
        mapHandler.add(toolPanel);

        // Add the ToolPanel to the right place in this JFrame.
        getContentPane().add(toolPanel, BorderLayout.NORTH);

        System.out.println("Creating Layers...");
        Layer[] layers = getLayers(props);

        // Use the LayerHandler to manage all layers, whether they are
        // on the map or not. You can add a layer to the map by
        // setting layer.setVisible(true).
        LayerHandler layerHandler = new LayerHandler();
        for (int i = 0; i < layers.length; i++) {
            layers[i].setVisible(true);
            layerHandler.addLayer(layers[i]);
        }

        mapHandler.add(layerHandler);
        System.out.println("Done creating...");
    }

    /**
     * Exits the application.
     */
    protected void quit() {
        System.exit(0);
    }

    /**
     * Launches the application. Reads the resource file, instantiates
     * a application, sizes it and displays it.
     * 
     * @param args command line arguments -- ignored
     */
    public static void main(String[] args) {
        Properties helloProps = new Properties();

        loadResource(helloResources, helloProps);
        try {
            HelloWorld hello = new HelloWorld(helloProps);
            hello.setSize(700, 500);
            hello.pack();
            hello.setVisible(true);
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

    /**
     * This method, called from main(), bundles functionality that
     * once was being called twice, because there were two resource
     * files being loaded, not just one, as is currently the case.
     * Rather than put this code back into main(), it's been kept as a
     * separate method in case we use more than one resource file
     * again.
     */
    private static void loadResource(String resources, Properties props) {
        InputStream in = HelloWorld.class.getResourceAsStream(resources);
        if (props == null) {
            System.err.println("Unable to locate resources: " + resources);
            System.err.println("Using default resources.");
        } else {
            try {
                props.load(in);
            } catch (java.io.IOException e) {
                System.err.println("Caught IOException loading resources: "
                        + resources);
                System.err.println("Using default resources.");
            }
        }
    }

    /**
     * Gets the names of the Layers to be loaded from the properties
     * passed in, initializes them, and returns them.
     * 
     * @param p the properties, among them the property represented by
     *        the String layersProperty above, which will tell us
     *        which Layers need to be loaded
     * @return an array of Layers ready to be added to the map bean
     * @see #layersProperty
     */
    private Layer[] getLayers(Properties p) {

        // Get the contents of the hello.layers property, which is a
        // space-separated list of marker names...
        String layersValue = p.getProperty(layersProperty);

        // Didn't find it if it's null.
        if (layersValue == null) {
            System.err.println("No property \"" + layersProperty
                    + "\" found in application properties.");
            return null;
        }
        // OK, parse the list
        StringTokenizer tokens = new StringTokenizer(layersValue, " ");
        Vector layerNames = new Vector();
        while (tokens.hasMoreTokens()) {
            layerNames.addElement(tokens.nextToken());
        }
        int nLayerNames = layerNames.size();
        Vector layers = new Vector(nLayerNames);

        // For each layer marker name, find that layer's properties.
        // The marker name is used to scope those properties that
        // apply to a particular layer. If you parse the layers'
        // properties from a file, you can add/remove layers from the
        // application without re-compiling. You could hard-code all
        // the properties being set if you'd rather...

        for (int i = 0; i < nLayerNames; i++) {
            String layerName = (String) layerNames.elementAt(i);

            // Find the .class property to know what kind of layer to
            // create.
            String classProperty = layerName + ".class";
            String className = p.getProperty(classProperty);
            if (className == null) {
                // Skip it if you don't find it.
                System.err.println("Failed to locate property \""
                        + classProperty + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
                continue;
            }
            try {
                // Create it if you do...
                Object obj = java.beans.Beans.instantiate(null, className);
                if (obj instanceof Layer) {
                    Layer l = (Layer) obj;
                    // All layers have a setProperties method, and
                    // should initialize themselves with proper
                    // settings here. If a property is not set, a
                    // default should be used, or a big, graceful
                    // complaint should be issued.
                    l.setProperties(layerName, p);
                    layers.addElement(l);
                }
            } catch (java.lang.ClassNotFoundException e) {
                System.err.println("Layer class not found: \"" + className
                        + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
            } catch (java.io.IOException e) {
                System.err.println("IO Exception instantiating class \""
                        + className + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
            }
        }
        int nLayers = layers.size();
        if (nLayers == 0) {
            return null;
        } else {
            Layer[] value = new Layer[nLayers];
            layers.copyInto(value);
            return value;
        }
    }
}