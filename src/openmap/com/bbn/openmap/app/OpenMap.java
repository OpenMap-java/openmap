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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.gui.MapPanel;
import com.bbn.openmap.gui.OpenMapFrame;
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
public class OpenMap {

    protected MapPanel mapPanel;

    /**
     * Create a new OpenMap framework object - creates a MapPanel, OpenMapFrame,
     * and brings up the layer palettes that are being told to be open at
     * startup. The MapPanel will create a PropertiesHandler that will search
     * for an openmap.properties file.
     */
    public OpenMap() {
        this(null);
    }

    /**
     * Create a new OpenMap framework object - creates a MapPanel, OpenMapFrame,
     * and brings up the layer palettes that are being told to be open at
     * startup. The properties in the PropertyHandler will be used to configure
     * the application. PropertyHandler may be null.
     */
    public OpenMap(PropertyHandler propertyHandler) {
        configureMapPanel(propertyHandler);

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // TODO There's something going on here with the progress
                // reporter and the swing thread that is causing the app to hang
                // in Leopard.
                showInFrame();
            }
        });
    }

    protected void configureMapPanel(PropertyHandler propertyHandler) {
        BasicMapPanel basicMapPanel = new BasicMapPanel(propertyHandler, true);
        // Creates the components in the main application thread. If any of
        // these components need to update their GUI, they should hand a
        // Runnable object to the SwingUtilities.invokeLater(Runnable) method,
        // and it will be updated accordingly.
        basicMapPanel.create();
        mapPanel = basicMapPanel;
    }

    protected void showInFrame() {
        OpenMapFrame omf = (OpenMapFrame) getMapHandler().get(com.bbn.openmap.gui.OpenMapFrame.class);

        if (omf == null) {
            omf = new OpenMapFrame(Environment.get(Environment.Title));
            getMapHandler().add(omf);
        }

        setWindowListenerOnFrame(omf);

        omf.setVisible(true);
        mapPanel.getMapBean().showLayerPalettes();
        Debug.message("basic", "OpenMap: READY");
    }

    /**
     * A method called internally to set the WindowListener behavior on an
     * OpenMapFrame used for the OpenMap application. By default, this method
     * adds a WindowAdapter that calls System.exit(0), killing java. You can
     * extend this to add a WindowListener to the OpenMapFrame that does nothing
     * or something else.
     */
    protected void setWindowListenerOnFrame(OpenMapFrame omf) {
        omf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * Get the MapHandler used for the OpenMap object.
     */
    public MapHandler getMapHandler() {
        return mapPanel.getMapHandler();
    }

    /**
     * Get the MapPanel, the container for the OpenMap components.
     */
    public MapPanel getMapPanel() {
        return mapPanel;
    }

    /**
     * Create and return an OpenMap object that uses a standard PropertyHandler
     * to configure itself. The OpenMap object has a MapHandler that you can use
     * to gain access to all the components.
     * 
     * @return OpenMap
     * @see #getMapHandler
     */
    public static OpenMap create() {
        return new OpenMap(null);
    }

    /**
     * Create and return an OpenMap object that uses a standard PropertyHandler
     * to configure itself. The OpenMap object has a MapHandler that you can use
     * to gain access to all the components.
     * 
     * @return OpenMap
     * @see #getMapHandler
     */
    public static OpenMap create(String propertiesFile) {
        Debug.init();
        return new OpenMap(configurePropertyHandler(propertiesFile));
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
            PropertyHandler.Builder builder = new PropertyHandler.Builder().setPropertiesFile(propertiesFile);
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

        OpenMap.create(propArgs);
    }
}