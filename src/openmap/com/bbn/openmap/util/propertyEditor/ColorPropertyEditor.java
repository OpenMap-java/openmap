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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/ColorPropertyEditor.java,v $
// $RCSfile: ColorPropertyEditor.java,v $
// $Revision: 1.4 $
// $Date: 2004/03/23 18:51:55 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.propertyEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMColorChooser;
import com.bbn.openmap.util.ColorFactory;

/** 
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * file. 
 */
public class ColorPropertyEditor extends PropertyEditorSupport {
    
    /** The Component returned by getCustomEditor(). */
    JButton button;

    public final static String title = "Select color...";


    /** Create FilePropertyEditor.  */
    public ColorPropertyEditor() {
        button = new JButton(title);
    }

    //
    //  PropertyEditor interface
    //
    
    /** PropertyEditor interface.
     *  @return true 
     */
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /**
     * Returns a JButton that will bring up a JFileChooser dialog.
     * @return JButton button
     */
    public Component getCustomEditor() {
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    Color startingColor;
                    try {
                        startingColor = ColorFactory.parseColor(getAsText(), true);
                    } catch (NumberFormatException nfe) {
                        startingColor = OMColor.clear;
                    }

                    Color color = OMColorChooser.showDialog(button, title, startingColor);

                    ColorPropertyEditor.this.setValue(color);
                }
            });

        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);
        gridbag.setConstraints(button, c);
        panel.add(button);

        return panel;
    }

    /** Implement PropertyEditor interface. */
    public void setValue(Object someObj) {
        if (someObj instanceof String) {
            button.setText((String)someObj);
        } else if (someObj instanceof Color) {
            button.setText(Integer.toHexString(((Color)someObj).getRGB()));
        }
    }
    
    /** Implement PropertyEditor interface. */
    public String getAsText() {
        return button.getText();
    }
    
    //
    //  ActionListener interface
    //
    
    /** Implement ActionListener interface. */
    public void actionPerformed(ActionEvent e) {
    }
}
