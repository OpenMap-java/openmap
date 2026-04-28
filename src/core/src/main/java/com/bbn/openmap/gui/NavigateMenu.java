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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/NavigateMenu.java,v $
// $RCSfile: NavigateMenu.java,v $
// $Revision: 1.10 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.event.ZoomListener;
import com.bbn.openmap.event.ZoomSupport;
import com.bbn.openmap.gui.menu.CoordsMenuItem;
import com.bbn.openmap.gui.menu.ProjectionMenu;
import com.bbn.openmap.util.Debug;

/**
 * Provides MenuItems that lets users control the projection. This
 * includes providing a means to call up the Coordinate Window to let
 * users enter coordinates to center the map, a projection choice
 * menu, and zooming choices.
 */
public class NavigateMenu extends AbstractOpenMapMenu implements ActionListener {

    public static final String defaultText = "Navigate";
    public static final String defaultMnemonic = "N";

    protected ZoomSupport zoomSupport = new ZoomSupport(this);
    public final static transient String zoomIn2Cmd = "zoomIn2Cmd";
    public final static transient String zoomIn4Cmd = "zoomIn4Cmd";
    public final static transient String zoomOut2Cmd = "zoomOut2Cmd";
    public final static transient String zoomOut4Cmd = "zoomOut4Cmd";

    /**
     * This constructor automatically configures the Menu to have
     * choices to bring up the Coordinates Window, the projection
     * choice menu, and the zoom menus.
     */
    public NavigateMenu() {
        super();
        setText(i18n.get(this, "navigate", defaultText));
//        setMnemonic(i18n.get(this, "navigate", I18n.MNEMONIC, defaultMnemonic)
//                .charAt(0));
        add(new CoordsMenuItem());

        JMenuItem mi;
        JMenu submenu = (JMenu) add(new JMenu(i18n.get(this,
                "zoomIn",
                "Zoom In")));
        mi = (JMenuItem) submenu.add(new JMenuItem(i18n.get(this,
                "zoomIn2X",
                "2X")));
        mi.setActionCommand(zoomIn2Cmd);
        mi.addActionListener(this);
        mi = (JMenuItem) submenu.add(new JMenuItem(i18n.get(this,
                "zoomIn4X",
                "4X")));
        mi.setActionCommand(zoomIn4Cmd);
        mi.addActionListener(this);

        submenu = (JMenu) add(new JMenu(i18n.get(this, "zoomOut", "Zoom Out")));
        mi = (JMenuItem) submenu.add(new JMenuItem(i18n.get(this,
                "zoomOut2X",
                "2X")));
        mi.setActionCommand(zoomOut2Cmd);
        mi.addActionListener(this);
        mi = (JMenuItem) submenu.add(new JMenuItem(i18n.get(this,
                "zoomOut4X",
                "4X")));
        mi.setActionCommand(zoomOut4Cmd);
        mi.addActionListener(this);

        add(new ProjectionMenu());
    }

    /**
     * ActionListener interface, lets the Menu act on the actions of
     * the MenuItems.
     */
    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();

        Debug.message("navigatemenu", "NavigateMenu.actionPerformed(): "
                + command);

        if (command.equals(zoomIn2Cmd)) {
            fireZoom(ZoomEvent.RELATIVE, 0.5f);
        } else if (command.equals(zoomIn4Cmd)) {
            fireZoom(ZoomEvent.RELATIVE, 0.25f);
        } else if (command.equals(zoomOut2Cmd)) {
            fireZoom(ZoomEvent.RELATIVE, 2.0f);
        } else if (command.equals(zoomOut4Cmd)) {
            fireZoom(ZoomEvent.RELATIVE, 4.0f);
        }
    }

    /*----------------------------------------------------------------------
     * Zoom Support - for broadcasting zoom events
     *----------------------------------------------------------------------
     */

    /**
     *  
     */
    public synchronized void addZoomListener(ZoomListener l) {
        zoomSupport.add(l);
    }

    /**
     *  
     */
    public synchronized void removeZoomListener(ZoomListener l) {
        zoomSupport.remove(l);
    }

    /**
     *  
     */
    public void fireZoom(int zoomType, float amount) {
        zoomSupport.fireZoom(zoomType, amount);
    }

    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof MapBean) {
            addZoomListener((MapBean) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        if (someObj instanceof MapBean) {
            removeZoomListener((MapBean) someObj);
        }
    }

}