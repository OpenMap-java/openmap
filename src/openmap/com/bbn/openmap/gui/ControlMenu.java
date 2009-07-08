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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ControlMenu.java,v $
// $RCSfile: ControlMenu.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import com.bbn.openmap.gui.menu.BackgroundColorMenuItem;
import com.bbn.openmap.gui.menu.MouseModeMenu;
import com.bbn.openmap.gui.menu.ToolPanelToggleMenuItem;

/**
 * This Menu, by default, holds a menu option to control the mouse
 * modes, change the background color of the map, and a button to
 * toggle the ToolPanel on and off.
 */
public class ControlMenu extends AbstractOpenMapMenu {

    private String defaultText = "Control";
    private int defaultMnemonic = 'C';

    public ControlMenu() {
        super();
        setText(defaultText);
//        setMnemonic(defaultMnemonic);

        add(new MouseModeMenu());
        add(new BackgroundColorMenuItem());
        add(new ToolPanelToggleMenuItem());
    }
}