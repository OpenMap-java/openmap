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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/SlopeGeneratorLoader.java,v $
// $RCSfile: SlopeGeneratorLoader.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:13 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.grid;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

public class SlopeGeneratorLoader extends GeneratorLoader {

    public final static String ColorsClassProperty = "colorsClass";
    public final static String COLORS_PROPERTY = "COLORS";
    public final static String CONTRAST_PROPERTY = "CONTRAST";

    public final static String DEFAULT_COLORS_CLASS = "com.bbn.openmap.omGraphics.grid.ColoredShadingColors";

    protected ElevationColors colors;
    protected int contrast = 5;

    public void setColors(ElevationColors cols) {
        ElevationColors oldColors = colors;
        colors = cols;
        if (oldColors != colors) {
            firePropertyChange(COLORS_PROPERTY, oldColors, colors);
        }
    }

    public ElevationColors getColors() {
        if (colors == null) {
            try {
                colors = (ElevationColors)Class.forName(DEFAULT_COLORS_CLASS).newInstance();
            } catch (InstantiationException ie) {
            } catch (IllegalAccessException iae) {
            } catch (ClassNotFoundException cnfe) {
            }
        }
        return colors;
    }

    public void setContrast(int cont) {
        int oldValue = contrast;
        contrast = cont;
        if (oldValue != contrast) {
            firePropertyChange(CONTRAST_PROPERTY, new Integer(oldValue), new Integer(contrast));
        }
    }

    public int getContrast() {
        return contrast;
    }

    public Component getGUI() {
        JPanel panel = new JPanel();
        // The DTED Contrast Adjuster
        JPanel contrastPanel = PaletteHelper.createPaletteJPanel("Contrast Adjustment");
        JSlider contrastSlide = new JSlider(JSlider.HORIZONTAL, 1/*min*/, 10/*max*/, getContrast()/*inital*/);
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
                    if (slider.getValueIsAdjusting()) {
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

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        String colorsClassProperty = props.getProperty(prefix + ColorsClassProperty);
        if (colorsClassProperty != null) {
            try {
                setColors((ElevationColors)ComponentFactory.create(colorsClassProperty));
            } catch (ClassCastException cce) {
                Debug.output("SlopeGeneratorLoader created a " + colorsClassProperty +
                             ", but it's not a ElevationColors object");
            }
        }


    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(CONTRAST_PROPERTY, pcl);
        super.addPropertyChangeListener(COLORS_PROPERTY, pcl);
    }

}
