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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/GeneratorLoader.java,v $
// $RCSfile: GeneratorLoader.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.JPanel;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.PropUtils;

/**
 * A GeneratorLoader is a component that can provide an
 * OMGridGenerator. It can provide a GUI to control the settings on
 * the next generator it provides. This is a basic GeneratorLoader
 * that doesn't do anything.
 */
public class GeneratorLoader extends OMComponent {

    protected String prettyName = "";

    public String getPrettyName() {
        return prettyName;
    }

    public void setPrettyName(String name) {
        prettyName = name;
    }

    public Component getGUI() {
        return new JPanel();
    }

    public OMGridGenerator getGenerator() {
        return new SinkGenerator();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        setPrettyName(props.getProperty(prefix
                + com.bbn.openmap.Layer.PrettyNameProperty, prettyName));
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + com.bbn.openmap.Layer.PrettyNameProperty,
                PropUtils.unnull(getPrettyName()));

        return props;
    }

    /**
     * You should override this so the listener receives any
     * particular events that the GeneratorLoader sends out.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {}

}