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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/SaveAsMenu.java,v
// $
// $RCSfile: SaveAsMenu.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 17:51:43 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import javax.swing.JMenu;

import com.bbn.openmap.gui.AbstractOpenMapMenu;
import com.bbn.openmap.image.AbstractImageFormatter;
import com.bbn.openmap.util.Debug;

public class SaveAsMenu extends AbstractOpenMapMenu {

    public SaveAsMenu() {
        this("Save Map As");
    }

    public SaveAsMenu(String title) {
        super(title);
        add(new SaveAsJpegMenuItem());
        add(new SaveAsGifMenuItem());
        addSVGMenuItem(this);
    }

    /**
     * Method checks to see if the SVGFormatter can be created, and if
     * it can, adds it to the FileMenu->Save As menu. The SVGFormatter
     * needs the right Batik jars in the classpath to compile.
     */
    public void addSVGMenuItem(JMenu menu) {
        try {
            Object obj = com.bbn.openmap.util.ComponentFactory.create("com.bbn.openmap.image.SVGFormatter");

            if (obj != null) {
                // This is a test to see if the batik package is
                // available. If it isn't, this statement should
                // throw an exception, and the SVG option will not be
                // added to the SaveAs Menu item.
                Class.forName("org.apache.batik.swing.JSVGCanvas")
                        .newInstance();
                menu.add(new SaveAsImageMenuItem("SVG", (AbstractImageFormatter) obj));
                return;
            }
        } catch (ClassNotFoundException cnfe) {
        } catch (InstantiationException ie) {
        } catch (IllegalAccessException iae) {
        } catch (NoClassDefFoundError ncdfe) {
        }

        if (Debug.debugging("basic")) {
            Debug.output("SVG not added to the Save As options, because Batik was not found in classpath.");
        }
    }
}