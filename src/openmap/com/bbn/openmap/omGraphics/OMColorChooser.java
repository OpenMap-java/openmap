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
// $RCSfile: OMColorChooser.java,v $
// $Revision: 1.3 $
// $Date: 2004/02/10 00:12:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.colorchooser.*;
import javax.swing.event.*;

/**
 * A wrapper class that pops up a modified JColorChooser class.  The
 * modification involves replacing the preview panel with a slider
 * that modifies the alpha transparency part of the color.
 * @author dietrick
 * @author Oliver Hinds added preview panel to see color with transparency.
 */
public class OMColorChooser {

    /**
     * Displays a dialog that lets you change a color.  Locks up the
     * application until a choice is made, returning the chosen color,
     * or null if nothing was chosen.
     *
     * @param component the source component.
     * @param title the String title for the window.
     * @param startingColor the initial color.
     */
    public static Color showDialog(Component component,
                                   String title, 
                                   Color startingColor) {
        Color initColor = startingColor != null ? startingColor : Color.white;

        final JColorChooser jcc = new JColorChooser(initColor);
        ColorTracker ok = new ColorTracker(jcc);

        jcc.getSelectionModel().addChangeListener(ok);
//         jcc.setPreviewPanel(ok.getTransparancyAdjustment(initColor.getAlpha()));
        jcc.setPreviewPanel(new JPanel());

        JDialog colorDialog = JColorChooser.createDialog(component, title,
                                                         true, jcc,
                                                         ok, null);

        // For some reason, in jdk 1.4.2, the custom transparency
        // adjustment panel stopped showing up in the preview panel.
        // This seems to work around the problem.
        JComponent preview = ok.getTransparancyAdjustment(initColor.getAlpha());
        colorDialog.getContentPane().remove(jcc);

        JPanel content = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        content.setLayout(gridBag);
        gridBag.setConstraints(jcc, c);
        gridBag.setConstraints(preview, c);
        content.add(jcc);
        content.add(preview);

        colorDialog.getContentPane().add(content, BorderLayout.CENTER);
        colorDialog.pack();
        colorDialog.show();
        return ok.getColor();
    }

    public static void main(String[] argv) {
        Color testColor = showDialog(null, "Test of OMColorChooser", Color.red);
        System.out.println("OMColorChooser ending with " + testColor);
        System.exit(0);
    }
}

/**
 * A modified ActionListener used by the JColorChooser.  Based on
 * the one used in javax.swing.JColorChooser, but with the
 * extended capability to handle transparancy.
 */
class ColorTracker 
    implements ActionListener, ChangeListener, Serializable {
    
    ColorRect preview;
    JColorChooser chooser;
    Color color;
    int transparency;
    
    public ColorTracker(JColorChooser c) {
        chooser = c;
        preview = new ColorRect(chooser.getColor());
    }

    /** 
     * ActionListener interface.  Sets the color from the JColorChooser.
     */
    public void actionPerformed(ActionEvent e) {
        color = chooser.getColor();
        setPreviewColor(color);
    }
    
    /** 
     * ChangeListener interface. Called when the color changes
     */
    public void stateChanged(ChangeEvent e) {
        if(!(e.getSource() instanceof ColorSelectionModel)) return;

        setPreviewColor(((ColorSelectionModel) e.getSource()).getSelectedColor());
    }
    
    /**
     * sets the preview color
     */
    public void setPreviewColor(Color c) {
        c = new Color(c.getRed(), c.getGreen(), c.getBlue(), transparency);
        preview.setColor(c);
        preview.repaint();
    }

    /**
     * Get the Color set in the JColorChooser, and set the alpha
     * value based on the transparency slider.
     */
    public Color getColor() {
        if (color != null) {
            color =  new Color(color.getRed(),
                               color.getGreen(),
                               color.getBlue(),
                               transparency);
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
    
        JSlider opaqueSlide = new JSlider(
            JSlider.HORIZONTAL, 0/*min*/, 255/*max*/, 
            initialValue/*inital*/);
        java.util.Hashtable dict = new java.util.Hashtable();
        dict.put(new Integer(50), new JLabel("clear"));
        dict.put(new Integer(200), new JLabel("opaque"));
        opaqueSlide.setLabelTable(dict);
        opaqueSlide.setPaintLabels(true);
        opaqueSlide.setMajorTickSpacing(50);
        opaqueSlide.setPaintTicks(true);
        opaqueSlide.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent ce){
                    JSlider slider = (JSlider) ce.getSource();
                    if (slider.getValueIsAdjusting()){
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
     * @param c color to paint
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
        ((Graphics2D)g).fill(g.getClip());
    }
}
