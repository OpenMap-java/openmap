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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/FileMenu.java,v $
// $RCSfile: FileMenu.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.gui.menu.*;

/**
 * FileMenu creates AboutMenuItem, SavePropertiesMenuItem,
 * SaveImageMenuItem, ExitMenuItem. It only adds AboutMenuItem if
 * runing as an Applet, all otherwise. These menu items are added by
 * default.
 */
public class FileMenu extends AbstractOpenMapMenu {

    private String defaultText = "File";
    private int defaultMnemonic = 'F';

    /**
     * Create and add menuitems(About, SaveProperties, SaveAsImage and
     * Exit)
     */
    public FileMenu() {
        super();
        setText(defaultText);
//        setMnemonic(defaultMnemonic);

        add(new AboutMenuItem());

        if (!Environment.isApplet()) {
            add(new JSeparator());
            add(new SavePropertiesMenuItem());
            add(new LoadPropertiesMenuItem());
            add(new JSeparator());
            add(new SaveAsMenu());
            add(new MapBeanPrinterMenuItem());
            add(new JSeparator());
            add(new QuitMenuItem());
        }
    }
}