//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: CoordInfoFormatterHandler.java,v $
//$Revision: 1.2 $
//$Date: 2008/10/10 00:57:21 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.util.coordFormatter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The CoordInfoFormatterHandler manages CoordInfoFormatters for all of the
 * CoordMouseModes, in order to provide a consistent coordinate display across
 * mouse modes that display coordinate info. If you want different coordinate
 * information for each CoordMouseMode, set the CoordInfoFormatter on each mouse
 * mode and don't use this class in the openmap.components property or
 * MapHandler. Only use this if you want to provide a choice to your users and
 * you want that choice used over all of your mouse modes.
 * <P>
 * This class should be placed in the MapHandler, which you can do via the
 * properties by adding it's marker name to the openmap.components property
 * list. The properties look like this (for a marker name
 * coordFormatterHandler):
 * 
 * <pre>
 * 
 * coordFormatterHandler.class=com.bbn.openmap.util.coordFormatter.CoordInfoFormatterHandler
 * coordFormatterHandler.formatters=dmsFormatter basicFormatter
 * coordFormatterHandler.dmsFormatter.class=com.bbn.openmap.util.coordFormatter.DMSCoordInfoFormatter
 * coordFormatterHandler.basicFormatter.class=com.bbn.openmap.util.coordFormatter.BasicCoordInfoFormatter
 * 
 * </pre>
 * 
 * @author dietrick
 */
public class CoordInfoFormatterHandler extends OMComponent {

    protected List<CoordInfoFormatter> formatters = new ArrayList<CoordInfoFormatter>();
    protected CoordInfoFormatter activeFormatter = null;
    public final static String FORMATTER_PROPERTY = "formatters";

    public CoordInfoFormatterHandler() {

    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        // Create the formatters from the property settings.
        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
        String markerList = props.getProperty(realPrefix + FORMATTER_PROPERTY);
        if (markerList != null) {
            Vector<String> formatterV = PropUtils.parseSpacedMarkers(markerList);
            Vector<?> formatters = ComponentFactory.create(formatterV,
                    prefix,
                    props);

            for (Object obj : formatters) {
                if (obj instanceof CoordInfoFormatter) {
                    CoordInfoFormatter cif = (CoordInfoFormatter) obj;
                    if (activeFormatter == null) {
                        activeFormatter = cif;
                    }
                    this.formatters.add(cif);
                }
            }
        }
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        StringBuffer markerList = new StringBuffer();
        for (Iterator<CoordInfoFormatter> it = formatters.iterator(); it.hasNext();) {
            CoordInfoFormatter cif = it.next();
            cif.getProperties(props);
            markerList.append(cif.getPropertyPrefix()).append(" ");
            props.put(PropUtils.getScopedPropertyPrefix(cif), cif.getClass()
                    .getName());
        }

        props.put(prefix + FORMATTER_PROPERTY, markerList.toString().trim());
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        return props;
    }

    public void setActiveFormatter(CoordInfoFormatter formatter) {
        CoordInfoFormatter oldFormatter = activeFormatter;
        if (oldFormatter == formatter) {
            return;
        }

        activeFormatter = formatter;
        firePropertyChange(FORMATTER_PROPERTY, oldFormatter, activeFormatter);
    }

    public void addPropertyChangeListener(String property,
                                          PropertyChangeListener pcl) {
        super.addPropertyChangeListener(property, pcl);
        pcl.propertyChange(new PropertyChangeEvent(this, FORMATTER_PROPERTY, null, activeFormatter));
    }

}
