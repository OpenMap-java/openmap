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

import com.bbn.openmap.gui.menu.CoordsMenuItem;
import com.bbn.openmap.gui.menu.ProjectionMenu;

/**
 * Provides MenuItems that lets users control the projection. This
 * includes providing a means to call up the Coordinate Window to let
 * users enter coordinates to center the map, a projection choice
 * menu.
 */
public class NavigateMenu extends AbstractOpenMapMenu {

    public static final String DEFAULT_TEXT = "Navigate";
    public static final String DEFAULT_MNEMONIC = "N";

    /**
     * This constructor automatically configures the Menu to have
     * choices to bring up the Coordinates Window, the projection
     * choice menu, and the zoom menus.
     */
    public NavigateMenu() {
        super();
        setText(i18n.get(this, "navigate", DEFAULT_TEXT));
//        setMnemonic(i18n.get(this, "navigate", I18n.MNEMONIC, DEFAULT_MNEMONIC)
//                .charAt(0));
        add(new CoordsMenuItem());
        add(new ProjectionMenu());
    }

}