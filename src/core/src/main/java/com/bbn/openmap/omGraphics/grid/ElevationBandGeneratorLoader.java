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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/SlopeGeneratorLoader.java,v
// $
// $RCSfile: ElevationBandGeneratorLoader.java,v $
// $Revision: 1.2 $
// $Date: 2005/12/22 18:46:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.proj.Length;
import com.bbn.openmap.util.PaletteHelper;

public class ElevationBandGeneratorLoader extends ColorGeneratorLoader {

    public final static String DISPLAY_UNITS_PROPERTY = "DISPLAY_UNITS";
    public final static String BANDHEIGHT_PROPERTY = "BANDHEIGHT";

    protected Length displayUnits = Length.METER;
    protected int bandHeight = ElevationBandGenerator.DEFAULT_BANDHEIGHT;

    public void setDisplayUnits(Length units) {
        Length oldValue = displayUnits;
        displayUnits = units;
        if (oldValue != displayUnits) {
            firePropertyChange(DISPLAY_UNITS_PROPERTY, oldValue, displayUnits);
        }
    }

    public Length getDisplayUnits() {
        return displayUnits;
    }

    public int getBandHeight() {
        return bandHeight;
    }

    public void setBandHeight(int bh) {
        int oldValue = bandHeight;
        bandHeight = bh;
        if (oldValue != bandHeight) {
            firePropertyChange(BANDHEIGHT_PROPERTY,
                    new Integer(oldValue),
                    new Integer(bandHeight));
        }
    }

    private final static Integer sliderLabelKey = new Integer(250);
    
    protected String getUnitAdjustmentTitle() {
        return "Band Height Units";
    }

    protected String getValueAdjustmentTitle() {
        return "Band Height Value";
    }
    
    public Component getGUI() {
        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1f;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.setLayout(gridbag);

        JPanel unitPanel = PaletteHelper.createPaletteJPanel(getUnitAdjustmentTitle());
        Object[] units = new Object[] { Length.METER, Length.FEET };
        JComboBox unitBox = new JComboBox(units);
        unitBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JComboBox unitBox = (JComboBox) ae.getSource();
                setDisplayUnits((Length) unitBox.getSelectedItem());
            }
        });
        unitPanel.add(unitBox);
        gridbag.setConstraints(unitPanel, c);
        panel.add(unitPanel);

        // The DTED Bandheight Adjuster
        JPanel contrastPanel = PaletteHelper.createPaletteJPanel(getValueAdjustmentTitle());
        JSlider contrastSlide = new JSlider(JSlider.HORIZONTAL, 1/* min */, 500/* max */, getBandHeight()/* initial */);
        java.util.Hashtable dict = new java.util.Hashtable();
        dict.put(new Integer(1), new JLabel("1"));
        dict.put(new Integer(500), new JLabel("500"));
        dict.put(sliderLabelKey, new JLabel("(" + getBandHeight() + ")"));
        contrastSlide.setLabelTable(dict);
        contrastSlide.setPaintLabels(true);
        contrastSlide.setMajorTickSpacing(50);
        contrastSlide.setPaintTicks(true);
        contrastSlide.setSnapToTicks(false);
        contrastSlide.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                JSlider slider = (JSlider) ce.getSource();
                if (!slider.getValueIsAdjusting()) {
                    int value = slider.getValue();
                    JLabel label = (JLabel) slider.getLabelTable()
                            .get(sliderLabelKey);
                    label.setText("(" + value + ")");
                    setBandHeight(value);
                }
            }
        });
        contrastPanel.add(contrastSlide);
        gridbag.setConstraints(contrastPanel, c);
        panel.add(contrastPanel);

        return panel;
    }

    public OMGridGenerator getGenerator() {
        ElevationBandGenerator gen = new ElevationBandGenerator();
        gen.setColors(getColors());
        gen.setDisplayUnits(displayUnits);
        gen.setBandHeight(getBandHeight());
        return gen;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(DISPLAY_UNITS_PROPERTY, pcl);
        super.addPropertyChangeListener(BANDHEIGHT_PROPERTY, pcl);
        super.addPropertyChangeListener(COLORS_PROPERTY, pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        super.removePropertyChangeListener(DISPLAY_UNITS_PROPERTY, pcl);
        super.removePropertyChangeListener(BANDHEIGHT_PROPERTY, pcl);
        super.removePropertyChangeListener(COLORS_PROPERTY, pcl);
    }

}