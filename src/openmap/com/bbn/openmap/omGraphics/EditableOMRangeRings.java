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
// $Revision: 1.12 $
// $Date: 2007/10/01 21:55:41 $
// $Author: epgordon $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.gui.GridBagToolBar;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.util.Debug;

/**
 */
public class EditableOMRangeRings extends EditableOMCircle {

    protected static int lastInterval;
    protected static Length lastUnit;
    protected static boolean snapToInterval = false;

    /**
     * Create the EditableOMRangeRings, setting the state machine to create the
     * circle off of the gestures.
     */
    public EditableOMRangeRings() {
        createGraphic(null);
    }

    /**
     * Create an EditableOMRangeRings with the circleType and renderType
     * parameters in the GraphicAttributes object.
     */
    public EditableOMRangeRings(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableOMRangeRings with an OMCircle already defined, ready
     * for editing.
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
     * Modifies the gui to not include line type adjustments, and adds widgets
     * to control range ring settings.
     * 
     * @param graphicAttributes the GraphicAttributes to use to get the GUI
     *        widget from to control those parameters for this EOMG.
     * @return java.awt.Component to use to control parameters for this EOMG.
     */
    public Component getGUI(GraphicAttributes graphicAttributes) {
        Debug.message("eomg", "EditableOMRangeRings.getGUI");
        if (graphicAttributes != null) {
            JComponent panel = graphicAttributes.getColorAndLineGUI();
            panel.add(getRangeRingGUI());
            return panel;
        } else {
            return getRangeRingGUI();
        }
    }

    public void updateInterval(int val) {
        ((OMRangeRings) circle).setInterval(val);
        lastInterval = val;
        if (intervalField != null) {
            intervalField.setText(Integer.toString(val));
        }

        if (snapToInterval) {
            setRadius(circle.getRadius());
        }
        redraw(null, true);
    }

    public void updateInterval(String intervalStr) {
        int oldValue = ((OMRangeRings) circle).getInterval();
        int value = interpretValue(intervalStr);

        if (value <= 0) {
            value = oldValue;
        }

        updateInterval(value);
    }

    public int interpretValue(String intervalStr) {
        int value = -1;
        try {
            if (intervalStr.toLowerCase().endsWith("m")) {
                intervalStr = intervalStr.substring(0, intervalStr.length() - 1);
                value = (int) df.parse(intervalStr).intValue() * 1000000;
            } else if (intervalStr.toLowerCase().endsWith("k")) {
                intervalStr = intervalStr.substring(0, intervalStr.length() - 1);
                value = df.parse(intervalStr).intValue() * 1000;
            } else if (intervalStr.trim().equals("")) {
                // do nothing
            } else {
                value = df.parse(intervalStr).intValue();
            }
        } catch (java.text.ParseException e) {
            Debug.error("RangeRing interval value not valid: " + intervalStr);
        } catch (NumberFormatException e) {
            Debug.error("RangeRing interval value not valid: " + intervalStr);
        }
        return value;
    }

    protected JTextField intervalField = null;
    protected JToolBar rrToolBar = null;
    protected transient DecimalFormat df = new DecimalFormat();
    protected I18n i18n = Environment.getI18n();

    protected JToolBar getRangeRingGUI() {
        if (rrToolBar == null) {
            rrToolBar = new GridBagToolBar();
            rrToolBar.setFloatable(false);
            rrToolBar.setMargin(new Insets(0, 1, 0, 1));

            configureRangeRings();
            intervalField = makeIntervalField();
            rrToolBar.add(intervalField);

            // JSlider intervalSlide = new JSlider(
            // JSlider.HORIZONTAL, 1/*min*/, 200/*max*/,
            // ((OMRangeRings)circle).getInterval()/*inital*/);
            // java.util.Hashtable dict = new java.util.Hashtable();
            // dict.put(new Integer(1), new JLabel("1"));
            // dict.put(new Integer(50), new JLabel("50"));
            // dict.put(new Integer(100), new JLabel("100"));
            // dict.put(new Integer(150), new JLabel("150"));
            // dict.put(new Integer(200), new JLabel("200"));
            // intervalSlide.setLabelTable(dict);
            // intervalSlide.setPaintLabels(true);
            // intervalSlide.setMajorTickSpacing(10);
            // intervalSlide.setPaintTicks(true);
            // intervalSlide.setSnapToTicks(false);
            // intervalSlide.addChangeListener(new ChangeListener() {
            // public void stateChanged(ChangeEvent ce) {
            // JSlider slider = (JSlider) ce.getSource();
            // if (slider.getValueIsAdjusting()) {
            // ((OMRangeRings)circle).setInterval(slider.getValue());
            // }
            // }
            // });

            rrToolBar.add(makeUnitsCombo());
            rrToolBar.add(makeSnapCheckBox());
        }
        return rrToolBar;
    }

    private void configureRangeRings() {
        ((OMRangeRings)circle).setInterval(getInterval());
        ((OMRangeRings)circle).setIntervalUnits(getUnits());
    }

    private int getInterval() {
        return (! isNewRing())
            ? ((OMRangeRings)circle).getInterval()
            : haveUserSpecifiedValue()
            ? lastInterval
            : OMRangeRings.DEFAULT_INTERVAL;
    }

    private Length getUnits() {
        return (! isNewRing())
            ? ((OMRangeRings)circle).getIntervalUnits()
            : haveUserSpecifiedValue()
            ? lastUnit
            : null;
    }

    private boolean isNewRing() {
        // we rely on interval units not being initialized during construction
        return (((OMRangeRings)circle).getIntervalUnits() == null);
    }

    private boolean haveUserSpecifiedValue() {
        // lastUnit is not null if the user made a selection with the comboBox
        return (lastUnit != null);
    }

    private JTextField makeIntervalField() {
        JTextField field = new JTextField(
                Integer.toString(((OMRangeRings) circle).getInterval()), 5);
        field.setMargin(new Insets(0, 1, 0, 1));
        // without minimum size set, field can be too small to use
        field.setMinimumSize(new Dimension(40,  18));
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setToolTipText(i18n.get(this,
                "intervalField.tooltip",
                "Value for interval between rings."));
        field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                updateInterval(((JTextField) (ae.getSource())).getText());
            }
        });
        // Users forget to hit Enter, which is required for an action event,
        // then wonder why the rings they draw don't have the desired value.
        // Adding a focus listener addresses this issue.
        field.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent event) {
                if (! event.isTemporary()) {
                    updateInterval(
                            ((JTextField) (event.getSource())).getText());
                }
            }
        });
        return field;
    }

    private JComboBox makeUnitsCombo() {
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
        unitStrings[unitStrings.length - 1] = i18n.get(this,
                "unitStrings.concentric",
                "concentric");

        JComboBox combo = new JComboBox(unitStrings);
        combo.setBorder(new EmptyBorder(0, 1, 0, 1));
        combo.setSelectedIndex(currentIndex);
        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcb = (JComboBox) e.getSource();
                OMRangeRings rr = (OMRangeRings) circle;
                Length newLength = Length.get((String) jcb.getSelectedItem());
                Length oldLength = rr.getIntervalUnits();

                /*
                 * If newLength is not null and oldLength is not null, just
                 * translate the distance that is current specified. If
                 * newLength is null, then find out how many rings are on
                 * the range ring and set the interval to that. If oldLength
                 * is null, find out the radius and divide it by the number
                 * of rings - 1.
                 */

                int value = interpretValue(intervalField.getText());
                if (value <= 0) {
                    value = 4;
                }

                if (newLength != null && oldLength != null) {
                    value = (int) newLength.fromRadians(oldLength.toRadians(value));
                } else {
                    int numSubCircles;
                    if (rr.subCircles == null || rr.subCircles.length == 0) {
                        numSubCircles = 1;
                    } else {
                        numSubCircles = rr.subCircles.length;
                    }

                    if (newLength == null) {
                        value = numSubCircles;
                    } else if (oldLength == null) {
                        value = (int) Math.ceil(newLength.fromRadians(Length.DECIMAL_DEGREE.toRadians(rr.getRadius()))
                                / numSubCircles);
                    }
                }

                ((OMRangeRings) circle).setIntervalUnits(newLength);
                lastUnit = newLength;
                updateInterval(value);
            }
        });
        return combo;
    }

    private JCheckBox makeSnapCheckBox() {
        String snapText = i18n.get(this, "snapToInterval", "Snap");
        JCheckBox snapBox = new JCheckBox(snapText, isSnapToInterval());
        snapText = i18n.get(this,
                "snapToInterval",
                I18n.TOOLTIP,
                "Round radius to nearest interval value.");
        snapBox.setToolTipText(snapText);
        snapBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                setSnapToInterval(((JCheckBox) ae.getSource()).isSelected());
                if (snapToInterval) {
                    setRadius(circle.getRadius());
                }
                redraw(null, true);
            }
        });
        return snapBox;
    }

    protected boolean drawLabelsHolder = true;

    /**
     * A convenience method that gives an EditableOMGraphic a chance to modify
     * the OMGraphic so it can be drawn quickly, by turning off labels, etc,
     * right before the XORpainting happens. The OMGraphic should be configured
     * so that the render method does the least amount of painting possible.
     * Note that the DrawingAttributes for the OMGraphic have already been set
     * to DrawingAttributes.DEFAULT (black line, clear fill).
     */
    protected void modifyOMGraphicForEditRender() {
        OMRangeRings omrr = (OMRangeRings) getGraphic();
        drawLabelsHolder = omrr.getDrawLabels();
        omrr.setDrawLabels(false);
    }

    /**
     * A convenience method that gives an EditableOMGraphic a chance to reset
     * the OMGraphic so it can be rendered normally, after it has been modified
     * for quick paints. The DrawingAttributes for the OMGraphic have already
     * been reset to their normal settings, from the DrawingAttributes.DEFAULT
     * settings that were used for the quick paint.
     */
    protected void resetOMGraphicAfterEditRender() {
        ((OMRangeRings) getGraphic()).setDrawLabels(drawLabelsHolder);
    }

    public boolean isSnapToInterval() {
        return snapToInterval;
    }

    public void setSnapToInterval(boolean snapToInterval) {
        this.snapToInterval = snapToInterval;
    }

    protected void setRadius(float radius) {
        if (circle != null) {
            if (snapToInterval) {
                OMRangeRings rr = (OMRangeRings) circle;
                Length units = rr.getIntervalUnits();
                if (units != null) {
                    float rds = units.fromRadians(Length.DECIMAL_DEGREE.toRadians(radius));
                    radius = Math.round(rds / rr.getInterval())
                            * rr.getInterval();
                    radius = Length.DECIMAL_DEGREE.fromRadians(units.toRadians(radius));
                }
            }
            circle.setRadius(radius);
        }
    }
}