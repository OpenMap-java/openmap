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
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.*;
import javax.swing.*;

import com.bbn.openmap.util.PaletteHelper;

/**
 * A class extended from a JFileChooser that adds fields for
 * specifying the image size.
 */
public class SaveAsImageFileChooser extends JFileChooser {

    SaveAsImageFileChooser.DimensionQueryPanel dqp = new SaveAsImageFileChooser.DimensionQueryPanel();

    /**
     * Create file chooser with the image size fields filled in.
     */
    public SaveAsImageFileChooser(int width, int height) {
        super();
        dqp.setImageHeight(height);
        dqp.setImageWidth(width);
        JPanel imageSizePanel = PaletteHelper.createPaletteJPanel(" Set Image Size ");
        imageSizePanel.setLayout(new BorderLayout());
        imageSizePanel.add(dqp, BorderLayout.CENTER);
        setAccessory(imageSizePanel);
    }

    /**
     * Set the value of the image width setting from the GUI.
     */
    public void setImageWidth(int w) {
        dqp.setImageWidth(w);
    }

    /**
     * Get the value of the image width setting from the GUI.
     */
    public int getImageWidth() {
        return dqp.getImageWidth();
    }

    /**
     * Set the value of the image height setting from the GUI.
     */
    public void setImageHeight(int h) {
        dqp.setImageHeight(h);
    }

    /**
     * Get the value of the image height setting from the GUI.
     */
    public int getImageHeight() {
        return dqp.getImageHeight();
    }

    public class DimensionQueryPanel extends JPanel {

        private JTextField hfield;
        private JTextField vfield;
        private JLabel htext;
        private JLabel vtext;
        private JLabel ptext1;
        private JLabel ptext2;

        public DimensionQueryPanel() {
            this(0, 0);
        }

        public DimensionQueryPanel(int width, int height) {

            htext = new JLabel("Width: ");
            htext.setHorizontalAlignment(SwingConstants.RIGHT);
            vtext = new JLabel("Height: ");
            vtext.setHorizontalAlignment(SwingConstants.RIGHT);
            hfield = new JTextField(Integer.toString(width), 5);
            vfield = new JTextField(Integer.toString(height), 5);
            ptext1 = new JLabel(" pixels");
            ptext2 = new JLabel(" pixels");
            layoutPanel();
        }

        public void setImageWidth(int width) {
            hfield.setText(Integer.toString(width));
        }

        public int getImageWidth() {
            return Integer.parseInt(hfield.getText());
        }

        public void setImageHeight(int height) {
            vfield.setText(Integer.toString(height));
        }

        public int getImageHeight() {
            return Integer.parseInt(vfield.getText());
        }

        public void layoutPanel() {

            GridBagLayout gb = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            setLayout(gb);

            c.insets = new Insets(3, 3, 3, 3);
            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.anchor = GridBagConstraints.EAST;

            gb.setConstraints(htext, c);
            add(htext);

            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1f;
            c.anchor = GridBagConstraints.WEST;

            gb.setConstraints(hfield, c);
            add(hfield);

            c.gridx = 2;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            gb.setConstraints(ptext1, c);
            add(ptext1);

            // Next row

            c.gridx = 0;
            c.gridy = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.anchor = GridBagConstraints.EAST;

            gb.setConstraints(vtext, c);
            add(vtext);

            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1f;
            c.anchor = GridBagConstraints.WEST;

            gb.setConstraints(vfield, c);
            add(vfield);

            c.gridx = 2;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            gb.setConstraints(ptext2, c);
            add(ptext2);
        }
    }
}