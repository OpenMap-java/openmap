package com.bbn.openmap.app;
import java.awt.Component;

import javax.swing.JMenuBar;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.gui.HotwashPanel;
import com.bbn.openmap.gui.OpenMapFrame;
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

public class TimeFrameApp extends OpenMap {

    public TimeFrameApp() {}

    public TimeFrameApp(PropertyHandler propertyHandler) {
        super(propertyHandler);
    }

    protected void showInFrame() {
        OpenMapFrame omf = (OpenMapFrame) getMapHandler().get(OpenMapFrame.class);

        if (omf == null) {
            omf = new OpenMapFrame() {
                public void considerForContent(Object someObj) {
                    if (someObj instanceof HotwashPanel) {
                        setContent((Component) someObj);
                    }

                    if (someObj instanceof BasicMapPanel) {
                        JMenuBar jmb = ((BasicMapPanel)someObj).getMapMenuBar();
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
