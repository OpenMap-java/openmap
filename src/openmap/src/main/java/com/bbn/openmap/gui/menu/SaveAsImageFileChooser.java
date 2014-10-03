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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/SaveAsImageFileChooser.java,v $
// $RCSfile: SaveAsImageFileChooser.java,v $
// $Revision: 1.6 $
// $Date: 2006/11/14 22:41:05 $
// $Author: kratkiew $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.bbn.openmap.gui.DimensionQueryPanel;
import com.bbn.openmap.util.PaletteHelper;

/**
 * A class extended from a JFileChooser that adds fields for
 * specifying the image size.
 */
public class SaveAsImageFileChooser extends JFileChooser {

    DimensionQueryPanel dqp = new DimensionQueryPanel();

    /**
     * Create file chooser with the image size fields filled in.
     */
    public SaveAsImageFileChooser(int width, int height) {
        super();
        dqp.setFieldHeight(height);
        dqp.setFieldWidth(width);
        JPanel imageSizePanel = PaletteHelper.createPaletteJPanel(" Set Image Size ");
        imageSizePanel.setLayout(new BorderLayout());
        imageSizePanel.add(dqp, BorderLayout.CENTER);
        setAccessory(imageSizePanel);
    }

    /**
     * Set the value of the image width setting from the GUI.
     */
    public void setImageWidth(int w) {
        dqp.setFieldWidth(w);
    }

    /**
     * Get the value of the image width setting from the GUI.
     */
    public int getImageWidth() {
        return dqp.getFieldWidth();
    }

    /**
     * Set the value of the image height setting from the GUI.
     */
    public void setImageHeight(int h) {
        dqp.setFieldHeight(h);
    }

    /**
     * Get the value of the image height setting from the GUI.
     */
    public int getImageHeight() {
        return dqp.getFieldHeight();
    }

}