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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMRangeRings.java,v $
// $RCSfile: EditableOMRangeRings.java,v $
// $Revision: 1.7 $
// $Date: 2004/10/14 18:06:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import com.bbn.openmap.gui.GridBagToolBar;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.*;
import javax.swing.*;

/**
 */
public class EditableOMRangeRings extends EditableOMCircle {

    /**
     * Create the EditableOMRangeRings, setting the state machine to
     * create the circle off of the gestures.
     */
    public EditableOMRangeRings() {
        createGraphic(null);
    }

    /**
     * Create an EditableOMRangeRings with the circleType and
     * renderType parameters in the GraphicAttributes object.
     */
    public EditableOMRangeRings(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableOMRangeRings with an OMCircle already
     * defined, ready for editing.
     * 
     * @param omc OMCircle that should be edited.
     */
    public EditableOMRangeRings(OMRangeRings omc) {
        setGraphic(omc);
    }

    /**
     * Create and set the graphic within the state machine. The
     * GraphicAttributes describe the type of circle to create.
     */
    public void createGraphic(GraphicAttributes ga) {
        init();
        stateMachine.setUndefined();
        int renderType = OMGraphic.RENDERTYPE_LATLON;

        if (ga != null) {
            renderType = ga.getRenderType();
        }

        if (Debug.debugging("eomc")) {
            Debug.output("EditableOMRangeRings.createGraphic(): rendertype = "
                    + renderType);
        }

        circle = new OMRangeRings(90f, -180f, 0f);

        if (ga != null) {
            ga.setTo(circle);
        }
    }

    /**
     * Modifies the gui to not include line type adjustments, and adds
     * widgets to control range ring settings.
     * 
     * @param graphicAttributes the GraphicAttributes to use to get
     *        the GUI widget from to control those parameters for this
     *        EOMG.
     * @return java.awt.Component to use to control parameters for
     *         this EOMG.
     */
    public Component getGUI(GraphicAttributes graphicAttributes) {
        Debug.message("eomg", "EditableOMRangeRings.getGUI");
        if (graphicAttributes != null) {
            JPanel panel = graphicAttributes.getColorAndLineGUI();
            panel.add(getRangeRingGUI());
            return panel;
        } else {
            return getRangeRingGUI();
        }
    }

    public void updateInterval(int val) {
        ((OMRangeRings) circle).setInterval(val);
        if (intervalField != null) {
            intervalField.setText(Integer.toString(val));
        }
        redraw(null, true);
    }

    public void updateInterval(String intervalStr) {
        int oldValue = ((OMRangeRings) circle).getInterval();
        int value = oldValue;

        try {
            if (intervalStr.toLowerCase().endsWith("m")) {
                intervalStr = intervalStr.substring(0, intervalStr.length() - 1);
                value = (int) df.parse(intervalStr).intValue() * 1000000;
            } else if (intervalStr.toLowerCase().endsWith("k")) {
                intervalStr = intervalStr.substring(0, intervalStr.length() - 1);
                value = df.parse(intervalStr).intValue() * 1000;
            } else if (intervalStr.trim().equals("")) {
                value = oldValue;
            } else {
                value = df.parse(intervalStr).intValue();
            }
        } catch (java.text.ParseException e) {
            Debug.error("RangeRing interval value not valid: " + intervalStr);
        } catch (NumberFormatException e) {
            Debug.error("RangeRing interval value not valid: " + intervalStr);
        }

        if (value <= 0) {
            value = oldValue;
        }

        updateInterval(value);

    }

    protected JTextField intervalField = null;
    protected JToolBar rrToolBar = null;
    protected transient java.text.DecimalFormat df = new java.text.DecimalFormat();

    protected JToolBar getRangeRingGUI() {
        if (rrToolBar == null) {
            rrToolBar = new GridBagToolBar();
            rrToolBar.setFloatable(false);
            rrToolBar.setMargin(new Insets(0, 1, 0, 1));

            //  JPanel intervalPanel =
            // PaletteHelper.createPaletteJPanel("Interval");
            intervalField = new JTextField(Integer.toString(((OMRangeRings) circle).getInterval()), 5);
            intervalField.setMargin(new Insets(0, 1, 0, 1));
            intervalField.setHorizontalAlignment(JTextField.RIGHT);
            intervalField.setToolTipText("Value for interval between rings.");
            intervalField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    updateInterval(((JTextField) (ae.getSource())).getText());
                }
            });

            rrToolBar.add(intervalField);

            //  JSlider intervalSlide = new JSlider(
            //      JSlider.HORIZONTAL, 1/*min*/, 200/*max*/,
            //      ((OMRangeRings)circle).getInterval()/*inital*/);
            //  java.util.Hashtable dict = new java.util.Hashtable();
            //  dict.put(new Integer(1), new JLabel("1"));
            //  dict.put(new Integer(50), new JLabel("50"));
            //  dict.put(new Integer(100), new JLabel("100"));
            //  dict.put(new Integer(150), new JLabel("150"));
            //  dict.put(new Integer(200), new JLabel("200"));
            //  intervalSlide.setLabelTable(dict);
            //  intervalSlide.setPaintLabels(true);
            //  intervalSlide.setMajorTickSpacing(10);
            //  intervalSlide.setPaintTicks(true);
            //  intervalSlide.setSnapToTicks(false);
            //  intervalSlide.addChangeListener(new ChangeListener() {
            //          public void stateChanged(ChangeEvent ce) {
            //              JSlider slider = (JSlider) ce.getSource();
            //              if (slider.getValueIsAdjusting()) {
            //                  ((OMRangeRings)circle).setInterval(slider.getValue());
            //              }
            //          }
            //      });

            Length[] available = Length.getAvailable();
            String[] unitStrings = new String[available.length + 1];

            String current = null;
            Length l = ((OMRangeRings) circle).getIntervalUnits();
            if (l != null) {
                current = l.toString();
            }

            int currentIndex = unitStrings.length - 1;

            for (int i = 0; i < available.length; i++) {
                unitStrings[i] = available[i].toString();
                if (current != null && unitStrings[i].equals(current)) {
                    currentIndex = i;
                }
            }
            unitStrings[unitStrings.length - 1] = "concentric";

            JComboBox unitList = new JComboBox(unitStrings);
            unitList.setBorder(new javax.swing.border.EmptyBorder(0, 1, 0, 1));

            unitList.setSelectedIndex(currentIndex);
            unitList.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox jcb = (JComboBox) e.getSource();
                    ((OMRangeRings) circle).setIntervalUnits(Length.get((String) jcb.getSelectedItem()));
                    updateInterval(intervalField.getText());
                }
            });

            rrToolBar.add(unitList);
        }
        return rrToolBar;
    }

    protected boolean drawLabelsHolder = true;

    /**
     * A convenience method that gives an EditableOMGraphic a chance
     * to modify the OMGraphic so it can be drawn quickly, by turning
     * off labels, etc, right before the XORpainting happens. The
     * OMGraphic should be configured so that the render method does
     * the least amount of painting possible. Note that the
     * DrawingAttributes for the OMGraphic have already been set to
     * DrawingAttributes.DEFAULT (black line, clear fill).
     */
    protected void modifyOMGraphicForEditRender() {
        OMRangeRings omrr = (OMRangeRings) getGraphic();
        drawLabelsHolder = omrr.getDrawLabels();
        omrr.setDrawLabels(false);
    }

    /**
     * A convenience method that gives an EditableOMGraphic a chance
     * to reset the OMGraphic so it can be rendered normally, after it
     * has been modified for quick paints. The DrawingAttributes for
     * the OMGraphic have already been reset to their normal settings,
     * from the DrawingAttributes.DEFAULT settings that were used for
     * the quick paint.
     */
    protected void resetOMGraphicAfterEditRender() {
        ((OMRangeRings) getGraphic()).setDrawLabels(drawLabelsHolder);
    }

}