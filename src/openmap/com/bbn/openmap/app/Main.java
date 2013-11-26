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

import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.UIManager;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.OverlayMapPanel;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * A Main application class using the OpenMap framework. This class is like the
 * OpenMap application class except it uses the new OverlayMapPanel instead of
 * the BasicMapPanel. The property prefix used for the properties is "main", so
 * this application is configured based on the main.components property list.
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
        OverlayMapPanel mapPanel = new OverlayMapPanel(propertyHandler, true);
        // Creates the components in the main application thread. If any of
        // these components need to update their GUI, they should hand a
        // Runnable object to the SwingUtilities.invokeLater(Runnable) method,
        // and it will be updated accordingly.
        mapPanel.create();
        this.mapPanel = mapPanel;
    }

    /**
     * Given a path to a properties file, try to configure a PropertyHandler
     * with it. If the properties file is not valid, the returned
     * PropertyHandler will look for the openmap.properties file in the
     * classpath and the user's home directory.
     */
    public static PropertyHandler configurePropertyHandler(String propertiesFile) {
        PropertyHandler propertyHandler = null;

        try {
            PropertyHandler.Builder builder = new PropertyHandler.Builder().setPropertiesFile(propertiesFile).setPropertyPrefix("main");
            propertyHandler = new PropertyHandler(builder);
        } catch (MalformedURLException murle) {
            Debug.error(murle.getMessage());
            murle.printStackTrace();
            propertyHandler = new PropertyHandler();
        } catch (IOException ioe) {
            Debug.error(ioe.getMessage());
            ioe.printStackTrace();
            propertyHandler = new PropertyHandler();
        }

        return propertyHandler;
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
     * A Main OpenMap application.
     */
    static public void main(String args[]) {

        ArgParser ap = new ArgParser("Main");
        String propArgs = null;
        ap.add("properties", "A resource, file path or URL to properties file\n Ex: http://myhost.com/xyz.props or file:/myhome/abc.pro\n See Java Documentation for java.net.URL class for more details", 1);

        ap.parse(args);

        String[] arg = ap.getArgValues("properties");
        if (arg != null) {
            propArgs = arg[0];
        }

        Main.create(propArgs);
    }
}