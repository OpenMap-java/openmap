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
// $RCSfile: OMColorChooser.java,v $
// $Revision: 1.10 $
// $Date: 2006/02/27 15:11:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;

/**
 * A wrapper class that pops up a modified JColorChooser class. The
 * modification involves replacing the preview panel with a slider
 * that modifies the alpha transparency part of the color.
 * 
 * @author dietrick
 * @author Oliver Hinds added preview panel to see color with
 *         transparency.
 */
public class OMColorChooser {
    /**
     * Displays a dialog that lets you change a color. Locks up the
     * application until a choice is made, returning the chosen color,
     * or null if nothing was chosen.
     * 
     * @param component the source component.
     * @param title the String title for the window.
     * @param startingColor the initial color.
     */
    public static Color showDialog(Component component, String title,
                                   Color startingColor) {
        Color initColor = startingColor != null ? startingColor : Color.white;

        final JColorChooser jcc = new JColorChooser(initColor);
        ColorTracker ok = new ColorTracker(jcc);

        jcc.getSelectionModel().addChangeListener(ok);
        /* WORKAROUND for Java bug #5029286 and #6199676 */
        //        jcc.setPreviewPanel(ok.getTransparancyAdjustment(initColor.getAlpha()));
        JComponent previewPanel = ok.getTransparancyAdjustment(initColor.getAlpha());
        previewPanel.setSize(previewPanel.getPreferredSize());
        previewPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        jcc.setPreviewPanel(previewPanel);
        
        JDialog colorDialog = JColorChooser.createDialog(component,
                title,
                true,
                jcc,
                ok,
                null);
        colorDialog.setVisible(true);
        return ok.getColor();
    }

    public static void main(String[] argv) {
        Color testColor = showDialog(null, "Choose a Color", Color.white);
        System.out.println("Color: " + testColor + ", hex value: "
                + Integer.toHexString(testColor.getRGB()));
        System.exit(0);
    }
}

/**
 * A modified ActionListener used by the JColorChooser. Based on the
 * one used in javax.swing.JColorChooser, but with the extended
 * capability to handle transparancy.
 */

class ColorTracker implements ActionListener, ChangeListener, Serializable {

    ColorRect preview;
    JColorChooser chooser;
    Color color;
    int transparency;
    private I18n i18n = Environment.getI18n();
    boolean isOK = false;//added because method <code>getColor</code> does not return null if action was not performed

    public ColorTracker(JColorChooser c) {
        chooser = c;
        preview = new ColorRect(chooser.getColor());
    }

    /**
     * ActionListener interface. Sets the color from the
     * JColorChooser.
     */
    public void actionPerformed(ActionEvent e) {
        color = chooser.getColor();
        setPreviewColor(color);
        isOK = true;
    }

    /**
     * ChangeListener interface. Called when the color changes
     */
    public void stateChanged(ChangeEvent e) {
        if (!(e.getSource() instanceof ColorSelectionModel))
            return;

        setPreviewColor(((ColorSelectionModel) e.getSource()).getSelectedColor());
    }

    /**
     * sets the preview color
     */
    public void setPreviewColor(Color c) {
        c = new Color(c.getRed(), c.getGreen(), c.getBlue(), transparency);
        preview.setColor(c);
        preview.repaint();
        color = c;
    }

    /**
     * Get the Color set in the JColorChooser, and set the alpha value
     * based on the transparency slider.
     */
    public Color getColor() {
        if (!isOK) {
            return null;
        }
        if (color != null) {
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), transparency);
        }
        return color;
    }

    /**
     * Create the Swing components that let you change the
     * transparency of the color received from the JColorChooser.
     * 
     * @param initialValue the starting alpha value for the color.
     * @return JComponent to adjust the transparency value.
     */
    public JComponent getTransparancyAdjustment(int initialValue) {
        transparency = initialValue;
        // This sets initial transparency effect in preview...
        setPreviewColor(preview.getColor());
        JPanel slidePanel = new JPanel();
        Box slideBox = Box.createHorizontalBox();

        JSlider opaqueSlide = new JSlider(JSlider.HORIZONTAL, 0/* min */, 255/* max */, initialValue/* initial */);
        java.util.Hashtable<Integer, JLabel> dict = new java.util.Hashtable<Integer, JLabel>();
        String opaqueLabel = i18n.get(ColorTracker.class, "opaque", "opaque");
        String clearLabel = i18n.get(ColorTracker.class, "clear", "clear");
        if (opaqueLabel == null || opaqueLabel.length() == 0) {
            // translations are too long :(
            dict.put(new Integer(126), new JLabel(clearLabel));            
        } else {        
            dict.put(new Integer(50), new JLabel(clearLabel));
            dict.put(new Integer(200), new JLabel(opaqueLabel));
        }
        //commented because polish translations are too long
        opaqueSlide.setLabelTable(dict);
        opaqueSlide.setPaintLabels(true);
        opaqueSlide.setMajorTickSpacing(50);
        opaqueSlide.setPaintTicks(true);
        opaqueSlide.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                JSlider slider = (JSlider) ce.getSource();
                if (slider.getValueIsAdjusting()) {
                    transparency = slider.getValue();
                }
                // This sets transparency in preview...
                setPreviewColor(preview.getColor());
            }
        });

        preview.setPreferredSize(new Dimension(100, slideBox.getHeight()));
        slideBox.add(preview);
        slideBox.add(Box.createGlue());
        slideBox.add(opaqueSlide);
        slideBox.add(Box.createGlue());
        slidePanel.add(slideBox);
        // You know what, it just has to be something, so the
        // UIManager will think it's valid. It will get resized as
        // appropriate when the JDialog gets packed.
        slidePanel.setSize(new Dimension(50, 50));
        return slidePanel;
    }
       
}

// class to display the currently selected color

class ColorRect extends JPanel {
    Color c; // color to display

    /**
     * constructor
     */
    public ColorRect(Color _c) {
        setBackground(Color.white);
        c = _c;
    }

    /**
     * set the color to tht specified
     * 
     * @param _c color to paint
     */
    public void setColor(Color _c) {
        c = _c;
    }

    /**
     * get the color
     */
    public Color getColor() {
        return c;
    }

    /**
     * paints this panel
     */
    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(c);
        ((Graphics2D) g).fill(g.getClip());
    }
}