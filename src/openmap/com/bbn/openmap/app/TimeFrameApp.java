package com.bbn.openmap.app;

import java.awt.Component;

import javax.swing.JMenuBar;

import com.bbn.openmap.MapHandler;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.event.OMEventSelectionCoordinator;
import com.bbn.openmap.gui.HotwashPanel;
import com.bbn.openmap.gui.MapPanel;
import com.bbn.openmap.gui.OpenMapFrame;
import com.bbn.openmap.gui.event.EventListPresenter;
import com.bbn.openmap.gui.event.EventPanel;
import com.bbn.openmap.gui.time.TimePanel;
import com.bbn.openmap.time.Clock;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

/**
 * An application that demonstrates the use of temporal GUI widgets and
 * infrastructure components. The OpenMapFrame contains a HotwashPanel, which
 * itself contains slider panes for various components. The standard OpenMap
 * MapPanel is in the center of the application. The TimePanel, for controlling
 * the current time displayed, goes on the bottom, and the event panel showing a
 * list of events goes to the west.
 */
public class TimeFrameApp extends Main {

    public TimeFrameApp() {}

    public TimeFrameApp(PropertyHandler propertyHandler) {
        super(propertyHandler);
    }

    /**
     * A method that lets you control what gets added to the application
     * programmatically. These components are required for handling an
     * OMEventHandler, which would be added to the MapHandler. If you wanted to
     * use the standard OpenMap application, you could add these components to
     * the MapHandler, instead.
     */
    protected void configureMapPanel(PropertyHandler propertyHandler) {
        super.configureMapPanel(propertyHandler);
        MapHandler mapHandler = mapPanel.getMapHandler();

        HotwashPanel hotwashPanel = new HotwashPanel();
        String hotwash = "hotwash";
        // This is important - the property prefix is checked against parent
        // names of MapPanelChildren, so the HotwashPanel can figure out what
        // goes to itself vs. the BasicMapPanel holding the map.
        hotwashPanel.setPropertyPrefix(hotwash);
        mapHandler.add(hotwashPanel);

        mapHandler.add(new OMEventSelectionCoordinator());

        TimePanel timePanel = new TimePanel();
        timePanel.setParentName(hotwash);
        mapHandler.add(timePanel);

        EventPanel eventPanel = new EventPanel();
        eventPanel.setParentName(hotwash);
        mapHandler.add(eventPanel);

        EventListPresenter eventListPresenter = new EventListPresenter();
        mapHandler.add(eventListPresenter);

        mapHandler.add(new Clock());
    }

    @SuppressWarnings("serial")
   protected void showInFrame() {
        OpenMapFrame omf = (OpenMapFrame) getMapHandler().get(OpenMapFrame.class);

        if (omf == null) {
            omf = new OpenMapFrame() {
                public void considerForContent(Object someObj) {
                    if (someObj instanceof HotwashPanel) {
                        setContent((Component) someObj);
                    }

                    if (someObj instanceof MapPanel) {
                        JMenuBar jmb = ((MapPanel) someObj).getMapMenuBar();
                        if (jmb != null) {
                            getRootPane().setJMenuBar(jmb);
                        }
                    }
                }
            };
            omf.setTitle("TimeFrame");
            getMapHandler().add(omf);
        }

        setWindowListenerOnFrame(omf);

        omf.setVisible(true);
        mapPanel.getMapBean().showLayerPalettes();
        Debug.message("basic", "OpenMap: READY");
    }

    /**
     * The main OpenMap application.
     */
    static public void main(String args[]) {
        Debug.init();
        ArgParser ap = new ArgParser("TimeFrameApp");
        ap.add("properties",
                "A resource, file path or URL to properties file\n Ex: http://myhost.com/xyz.props or file:/myhome/abc.pro\n See Java Documentation for java.net.URL class for more details",
                1);

        ap.parse(args);

        String propArgs = null;
        String[] arg = ap.getArgValues("properties");
        if (arg != null) {
            propArgs = arg[0];
        }

        new TimeFrameApp(configurePropertyHandler(propArgs));
    }
}
