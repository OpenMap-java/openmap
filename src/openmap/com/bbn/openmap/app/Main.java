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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/app/OpenMap.java,v $
// $RCSfile: OpenMap.java,v $
// $Revision: 1.16 $
// $Date: 2009/02/26 21:16:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.app;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.OverlayMapPanel;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * The OpenMap application framework. This class creates a PropertyHandler that
 * searches the classpath, config directory and user's home directory for an
 * openmap.properties file, and creates the application based on the contents of
 * the properties files. It also creates an MapPanel and an OpenMapFrame to be
 * used for the application and adds them to the MapHandler contained in the
 * MapPanel. All other components are added to that MapHandler as well, and they
 * use the MapHandler to locate, connect and communicate with each other.
 */
public class Main extends OpenMap {

    /**
     * Create a new OpenMap framework object - creates a MapPanel, OpenMapFrame,
     * and brings up the layer palettes that are being told to be open at
     * startup. The MapPanel will create a PropertiesHandler that will search
     * for an openmap.properties file.
     */
    public Main() {
        this(null);
    }
    
    /**
     * Create a new OpenMap framework object - creates a MapPanel, OpenMapFrame,
     * and brings up the layer palettes that are being told to be open at
     * startup. The properties in the PropertyHandler will be used to configure
     * the application. PropertyHandler may be null.
     */
    public Main(PropertyHandler propertyHandler) {
       super(propertyHandler);
    }
    
    protected void configureMapPanel(PropertyHandler propertyHandler) {
        OverlayMapPanel basicMapPanel = new OverlayMapPanel(propertyHandler, true);
        // Creates the components in the main application thread. If any of
        // these components need to update their GUI, they should hand a
        // Runnable object to the SwingUtilities.invokeLater(Runnable) method,
        // and it will be updated accordingly.
        basicMapPanel.create();
        mapPanel = basicMapPanel;
    }

    /**
     * Create and return an OpenMap object that uses a standard PropertyHandler
     * to configure itself. The OpenMap object has a MapHandler that you can use
     * to gain access to all the components.
     * 
     * @return OpenMap
     * @see #getMapHandler
     */
    public static Main create() {
        return new Main(null);
    }

    /**
     * Create and return an OpenMap object that uses a standard PropertyHandler
     * to configure itself. The OpenMap object has a MapHandler that you can use
     * to gain access to all the components.
     * 
     * @return OpenMap
     * @see #getMapHandler
     */
    public static Main create(String propertiesFile) {
        Debug.init();
        return new Main(configurePropertyHandler(propertiesFile));
    }

    /**
     * The main OpenMap application.
     */
    static public void main(String args[]) {

        ArgParser ap = new ArgParser("OpenMap");
        String propArgs = null;
        ap.add("properties",
                "A resource, file path or URL to properties file\n Ex: http://myhost.com/xyz.props or file:/myhome/abc.pro\n See Java Documentation for java.net.URL class for more details",
                1);

        ap.parse(args);

        String[] arg = ap.getArgValues("properties");
        if (arg != null) {
            propArgs = arg[0];
        }

        Main.create(propArgs);
    }
}