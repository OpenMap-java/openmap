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
// $RCSfile: ColorGeneratorLoader.java,v $
// $Revision: 1.2 $
// $Date: 2005/12/22 18:46:22 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import java.beans.PropertyChangeListener;
import java.util.Properties;

import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

public class ColorGeneratorLoader extends GeneratorLoader {

    public final static String ColorsClassProperty = "colorsClass";
    public final static String COLORS_PROPERTY = "COLORS";

    public final static String DEFAULT_COLORS_CLASS = "com.bbn.openmap.omGraphics.grid.GreyscaleSlopeColors";

    protected ElevationColors colors;
    
    public ColorGeneratorLoader() {
        setPrettyName(i18n.get(ColorGeneratorLoader.class, "name", "Color Shading")); //default
    }

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
                colors = (ElevationColors) Class.forName(DEFAULT_COLORS_CLASS)
                        .newInstance();
            } catch (InstantiationException ie) {
            } catch (IllegalAccessException iae) {
            } catch (ClassNotFoundException cnfe) {
            }
        }
        return colors;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        String colorsClassProperty = props.getProperty(prefix
                + ColorsClassProperty);
        if (colorsClassProperty != null) {
            try {
                setColors((ElevationColors) ComponentFactory.create(colorsClassProperty));
            } catch (ClassCastException cce) {
                Debug.output("SlopeGeneratorLoader created a "
                        + colorsClassProperty
                        + ", but it's not a ElevationColors object");
            }
        }
    }
 
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(COLORS_PROPERTY, pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        super.removePropertyChangeListener(COLORS_PROPERTY, pcl);
    }
}