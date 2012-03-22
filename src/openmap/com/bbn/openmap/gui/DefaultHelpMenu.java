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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/DefaultHelpMenu.java,v $
// $RCSfile: DefaultHelpMenu.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.util.Iterator;

import javax.swing.JMenuItem;

import com.bbn.openmap.util.Debug;

/**
 * HelpMenu is instance of JMenu. When added to beancontext it looks
 * for objects that implements HelpMenuItem interface. When objects
 * implementing HelpMenuItems interface are found, it simply retrieves
 * the menu items and adds them to itself. Note: It is the
 * responsibility of the MenuItems themselves to respond to clicks on
 * them.
 */
public class DefaultHelpMenu extends AbstractOpenMapMenu implements HelpMenu {

    private String defaultText = "Help";
    private int defaultMnemonic = 'H';

    public DefaultHelpMenu() {
        setText(defaultText);
//        setMnemonic(defaultMnemonic);
    }

    public DefaultHelpMenu(String in_text) {
        super();
        setText(in_text);
    }

    /**
     * Look for objects that implement HelpMenuItems interface and add
     * them to itself.
     */
    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof HelpMenuItems) {
            if (Debug.debugging("menu")) {
                Debug.output("DefaultHelpMenu found HelpMenuItems");
            }
            Iterator hmiit = ((HelpMenuItems) someObj).iterator();
            while (hmiit.hasNext()) {
                add((JMenuItem) hmiit.next());
            }
        }
    }

    /**
     * If an object implementing helpMenuItemsI is found, remove it.
     */
    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        if (someObj instanceof HelpMenuItems) {
            Iterator hmiit = ((HelpMenuItems) someObj).iterator();
            while (hmiit.hasNext()) {
                remove((JMenuItem) hmiit.next());
            }
        }
    }
}