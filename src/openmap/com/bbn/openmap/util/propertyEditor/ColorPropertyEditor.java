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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/propertyEditor/ColorPropertyEditor.java,v $
// $RCSfile: ColorPropertyEditor.java,v $
// $Revision: 1.7 $
// $Date: 2005/05/24 17:55:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.propertyEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMColorChooser;
import com.bbn.openmap.tools.icon.IconPartList;
import com.bbn.openmap.tools.icon.OMIconFactory;
import com.bbn.openmap.tools.icon.OpenMapAppPartCollection;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

/**
 * A PropertyEditor that brings up a JFileChooser panel to select a
 * file.
 */
public class ColorPropertyEditor extends PropertyEditorSupport {

    /** The Component returned by getCustomEditor(). */
    JButton button;

    public final static String title = "Select color...";
    protected int icon_width = 20;
    protected int icon_height = 20;

    /** Create FilePropertyEditor. */
    public ColorPropertyEditor() {
        button = new JButton(title);
    }

    //
    //  PropertyEditor interface
    //

    /**
     * PropertyEditor interface.
     * 
     * @return true
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns a JButton that will bring up a JFileChooser dialog.
     * 
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

                Color color = OMColorChooser.showDialog(button,
                        title,
                        startingColor);

                ColorPropertyEditor.this.setValue(color);
            }
        });

        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        panel.setLayout(gridbag);
        gridbag.setConstraints(button, c);
        panel.add(button);

        return panel;
    }

    public ImageIcon getIconForPaint(Paint paint, boolean fill) {

        if (paint == null)
            paint = Color.black;

        DrawingAttributes da = new DrawingAttributes();
        da.setLinePaint(paint);
        da.setStroke(new BasicStroke(2));
        if (fill) {
            da.setFillPaint(paint);
        }

        OpenMapAppPartCollection collection = OpenMapAppPartCollection.getInstance();
        IconPartList parts = new IconPartList();

        if (paint instanceof Color || paint == OMColor.clear) {
            Color color = (Color) paint;
            Color opaqueColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
            DrawingAttributes opaqueDA = new DrawingAttributes();
            opaqueDA.setLinePaint(opaqueColor);
            opaqueDA.setStroke(new BasicStroke(2));

            if (fill) {
                opaqueDA.setFillPaint(opaqueColor);
            }

            parts.add(collection.get("LR_TRI", opaqueDA));
            parts.add(collection.get("UL_TRI", da));
        } else {
            parts.add(collection.get("BIG_BOX", da));
        }

        return OMIconFactory.getIcon(icon_width, icon_height, parts);
    }

    /** Implement PropertyEditor interface. */
    public void setValue(Object someObj) {

        if (someObj == null) {
            setButtonForColor(Color.black);
        } else if (someObj instanceof Color) {
            setButtonForColor((Color) someObj);
        } else if (someObj instanceof String) {
            Color color = OMColor.clear;
            try {
                color = ColorFactory.parseColor((String) someObj, true);
            } catch (NumberFormatException nfe) {
                Debug.output("ColorPropertyEditor.setValue problem with color: " + someObj + "\n"
                        + nfe.getMessage());
            }

            setButtonForColor(color);
        }
    }

    protected void setButtonForColor(Color color) {
        button.setIcon(getIconForPaint(color, true));
        String val = Integer.toHexString(color.getRGB());
        if (val.equals("0")) {
            val = "00000000";
        }

        button.setText(val);
    }

    /** Implement PropertyEditor interface. */
    public String getAsText() {
        return button.getText();
    }

    //
    //  ActionListener interface
    //

    /** Implement ActionListener interface. */
    public void actionPerformed(ActionEvent e) {}
}