// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/CoordsMenuItem.java,v $
// $RCSfile: CoordsMenuItem.java,v $
// $Revision: 1.4 $
// $Date: 2004/02/02 22:52:25 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.menu;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.CombinedCoordPanel;
import com.bbn.openmap.gui.ScrollPaneWindowSupport;
import com.bbn.openmap.gui.WindowSupport;

/**
 * A menu item that will bring up a frame with a CombinedCoordPanel,
 * showing different coordinate panels in a tabbed pane.  This menu
 * item forwards all objects received from the MapHandler to the
 * CombinedCoordPanel.
 */
public class CoordsMenuItem extends MapHandlerMenuItem 
    implements ActionListener {
    /**
     * The WindowSupport for the CombinedCoordPanel.
     */
    protected WindowSupport ws;
    /**
     * The coordinate content.
     */
    protected CombinedCoordPanel ccp;

    public CoordsMenuItem() {
        super("Coordinates...");
        addActionListener(this);
        ccp = new CombinedCoordPanel(this);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand() == CombinedCoordPanel.CloseCmd &&
            ws != null) {
            ws.killWindow();
        } else {
            if (ws == null) {
                ws = new ScrollPaneWindowSupport(ccp, "Go To Coordinates...");
            } 

            MapHandler mh = getMapHandler();
            Frame frame = null;
            if (mh != null) {
                frame = (Frame)mh.get(java.awt.Frame.class);
            }

            ws.displayInWindow(frame);
        }
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof MapHandler) {
            setMapHandler((MapHandler)someObj);
        }
        ccp.findAndInit(someObj);
    }

    public void findAndUndo(Object someObj) {
        if (someObj == getMapHandler()) {
            setMapHandler(null);
        }
        ccp.findAndUndo(someObj);
    }
}
