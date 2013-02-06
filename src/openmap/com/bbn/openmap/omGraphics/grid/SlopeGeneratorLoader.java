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
// $RCSfile: SlopeGeneratorLoader.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/22 18:46:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import java.awt.Component;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.layer.rpf.RpfLayer;
import com.bbn.openmap.util.PaletteHelper;

public class SlopeGeneratorLoader extends ColorGeneratorLoader {

    public final static String CONTRAST_PROPERTY = "CONTRAST";

    protected int contrast = 5;

    public SlopeGeneratorLoader() {
        setPrettyName(i18n.get(SlopeGeneratorLoader.class, "name", "Greyscale Slope")); //default
    }
    
    public void setContrast(int cont) {
        int oldValue = contrast;
        contrast = cont;
        if (oldValue != contrast) {
            firePropertyChange(CONTRAST_PROPERTY,
                    new Integer(oldValue),
                    new Integer(contrast));
        }
    }

    public int getContrast() {
        return contrast;
    }

    public Component getGUI() {
        JPanel panel = new JPanel();
        // The DTED Contrast Adjuster
        JPanel contrastPanel = PaletteHelper.createPaletteJPanel("Contrast Adjustment");
        JSlider contrastSlide = new JSlider(JSlider.HORIZONTAL, 1/* min */, 10/* max */, getContrast()/* initial */);
        java.util.Hashtable dict = new java.util.Hashtable();
        dict.put(new Integer(1), new JLabel("min"));
        dict.put(new Integer(10), new JLabel("max"));
        contrastSlide.setLabelTable(dict);
        contrastSlide.setPaintLabels(true);
        contrastSlide.setMajorTickSpacing(1);
        contrastSlide.setPaintTicks(true);
        contrastSlide.setSnapToTicks(true);
        contrastSlide.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                JSlider slider = (JSlider) ce.getSource();
                if (!slider.getValueIsAdjusting()) {
                    setContrast(slider.getValue());
                }
            }
        });
        contrastPanel.add(contrastSlide);
        panel.add(contrastPanel);

        return panel;
    }

    public OMGridGenerator getGenerator() {
        SlopeGenerator gen = new SlopeGenerator();
        gen.setColors(getColors());
        gen.setContrast(contrast);
        return gen;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(CONTRAST_PROPERTY, pcl);
        super.addPropertyChangeListener(COLORS_PROPERTY, pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        super.removePropertyChangeListener(CONTRAST_PROPERTY, pcl);
        super.removePropertyChangeListener(COLORS_PROPERTY, pcl);
    }
 
}