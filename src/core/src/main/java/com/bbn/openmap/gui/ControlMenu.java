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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import com.bbn.openmap.gui.menu.BackgroundColorMenuItem;
import com.bbn.openmap.gui.menu.MouseModeMenu;
import com.bbn.openmap.gui.menu.ToolPanelToggleMenuItem;

/**
 * This Menu, by default, holds a menu option to control the mouse modes, change
 * the background color of the map, and a button to toggle the ToolPanel on and
 * off.
 */
public class ControlMenu extends AbstractOpenMapMenu {

    private String defaultText = "Control";

    public ControlMenu() {
        super();
        setText(defaultText);
        // setMnemonic(defaultMnemonic);

        add(new MouseModeMenu());
        add(new BackgroundColorMenuItem());
    }

    public void findAndInit(Object obj) {
        super.findAndInit(obj);

        if (obj instanceof ToolPanel) {
            add(((ToolPanel) obj).getToggleMenu());
        }
    }

    public void findAndUndo(Object obj) {
        super.findAndUndo(obj);

        if (obj instanceof ToolPanel) {
            ToolPanel tp = (ToolPanel) obj;
            List<ToolPanelToggleMenuItem> removedItems = new ArrayList<ToolPanelToggleMenuItem>();
            int numItems = this.getItemCount();
            for (int i = 0; i < numItems; i++) {
                JMenuItem jmi = this.getItem(i);

                if (jmi instanceof ToolPanelToggleMenuItem
                        && tp.checkToolPanelToggleMenuItem(((ToolPanelToggleMenuItem) jmi))) {
                    removedItems.add(((ToolPanelToggleMenuItem) jmi));
                }
            }

            if (!removedItems.isEmpty()) {
                for (ToolPanelToggleMenuItem jmi : removedItems) {
                    remove(jmi);
                    jmi.dispose();
                }
            }

        }
    }

}