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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/examples/crew/Crew.java,v $
// $RCSfile: Crew.java,v $
// $Revision: 1.5 $
// $Date: 2005/05/23 19:46:57 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.examples.crew;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFrame;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.gui.LayersPanel;
import com.bbn.openmap.gui.OMToolSet;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * A sample application incorporating the <code>MapBean</code>.
 * This program adds a MouseMode that passes events to the RouteLayer.
 * Move the mouse over the three lines, and click on them.
 */
public class Crew extends JFrame {

    /** The name of the resource file. */
    public static String crewResources = "Crew.properties";

    /**
     * The properties acquired from the resource file.
     * 
     * @see #crewResources
     */
    private Properties properties;

    /**
     * Create a default Crew instance. The instance will use the
     * default properties.
     */
    public Crew() throws MultipleSoloMapComponentException {
        this(new Properties());
    }

    /**
     * Create a Crew instance with the given properties. The
     * properties override the defaults.
     * 
     * @param props The override properties
     */
    public Crew(Properties props) throws MultipleSoloMapComponentException {

        // Initialize the parent class (JFrame)
        super("Crew Example");

        // Use a Border layout manager
        getContentPane().setLayout(new BorderLayout());

        // Call quit when the window's close box is clicked.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        // Store the given properties for later use.
        properties = props;

        // Create the MapHandler, which allows all the components to
        // find each other if they are added to it.
        MapHandler mapHandler = new MapHandler();
        // Create the MapBean.
        MapBean map = new MapBean();
        // Set the map's center property
        map.setCenter(new LatLonPoint.Double(43.0f, -95.0f));
        // Add the MapBean to the MapHandler.
        mapHandler.add(map);

        // Add the map to the JFrame
        getContentPane().add(map, BorderLayout.CENTER);

        // Create a mouse delegator to handle mouse events on the map
        mapHandler.add(new MouseDelegator());
        // Create and add the MouseMode that the RouteLayer wants
        // events from. The MouseDelegator asks all layers which
        // MouseMode they listen to, and hooks them up. When that
        // MouseMode is active, events flow to the top layer, then
        // down to lower layers if the event is not consumed along the
        // way.
        mapHandler.add(new SelectMouseMode());

        // Add a LayerHandler, which manages all layers, on or off.
        mapHandler.add(new LayerHandler());

        // Create and add a Political Background
        Layer layer = createPoliticalLayer();
        if (layer != null)
            mapHandler.add(layer);

        // Create and add a Route Layer. The LayerHandler will find
        // it via the LayerHandler, and then the LayerHandler will add
        // it to the map because layer.isVisible() == true;
        layer = createRouteLayer();
        if (layer != null) {
            layer.setName("Routes");
            mapHandler.add(layer);
        }

        // Add some navigation tools. The ToolPanel will find the
        // OMToolSet (a Tool) in the MapHandler.
        ToolPanel toolPanel = new ToolPanel();
        mapHandler.add(toolPanel);
        mapHandler.add(new OMToolSet());
        // Add the ToolPanel to the JFrame.
        getContentPane().add(toolPanel, BorderLayout.NORTH);

        // Oh, for fun, lets add a GUI to control the layers. A
        // button to launch it will get added to the ToolPanel.
        mapHandler.add(new LayersPanel());

        // You can add other components from the com.bbn.openmap.gui
        // package...
    }

    /**
     * Creates a political boundary map layer. Actually, this method
     * instantiates a DCW Layer (Digital Chart of the World), and sets
     * the layer's parameters so that it will render a political map.
     * <p>
     * Creation of the political layer is controlled by the
     * <code><bold>
     * showPolitical</bold></code> property. See the
     * resource file crew.properties for more information.
     * 
     * @return the political layer, or null if an error occurred
     */
    protected Layer createPoliticalLayer() {
        Boolean showPolitical;

        // You can add a showPolitical=false property to the
        // Crew.properties file to make this layer not be added to the
        // application.
        showPolitical = new Boolean(properties.getProperty("showPolitical",
                "true"));

        if (!showPolitical.booleanValue()) {
            return null;
        }

        ShapeLayer politicalLayer = new ShapeLayer();
        politicalLayer.setProperties("political", properties);

        return politicalLayer;
    }

    /**
     * Creates a route layer to display great circle lines on the map.
     * 
     * @return the route layer
     * @see RouteLayer
     */
    protected Layer createRouteLayer() {
        return new RouteLayer();
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
        Crew crew;
        Properties crewProps = new Properties();
        InputStream propsIn = Crew.class.getResourceAsStream(crewResources);

        if (propsIn == null) {
            System.err.println("Unable to locate resources: " + crewResources);
            System.err.println("Using default resources.");
        } else {
            try {
                crewProps.load(propsIn);
            } catch (java.io.IOException e) {
                System.err.println("Caught IOException loading resources: "
                        + crewResources);
                System.err.println("Using default resources.");
            }
        }

        try {
            crew = new Crew(crewProps);
            crew.setSize(700, 500);
            crew.pack();
            crew.setVisible(true);
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
}