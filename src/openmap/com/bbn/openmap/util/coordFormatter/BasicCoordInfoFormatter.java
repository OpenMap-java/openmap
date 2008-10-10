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
//$RCSfile: BasicCoordInfoFormatter.java,v $
//$Revision: 1.2 $
//$Date: 2008/10/10 00:57:21 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.util.coordFormatter;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Properties;

import com.bbn.openmap.Layer;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.PropUtils;

/**
 * A CoordInfoFormatter that creates the default OpenMap string:<pre>
 * 
 * Lat, Lon (latitude, longitude) - x, y (x pixel loc, y pixel loc)
 * 
 * </pre>
 * 
 * @author dietrick
 */
public class BasicCoordInfoFormatter extends OMComponent implements
        CoordInfoFormatter {

    protected String prettyName = "Default";
    public static final String DEGREE_SIGN = "\u00b0";
    protected DecimalFormat df = new DecimalFormat("0.###");

    public BasicCoordInfoFormatter() {
        
    }

    public String createCoordinateInformationLine(int x, int y,
                                                  Point2D llp, Object source) {
        if (llp != null) {
            return "Lat, Lon (" + df.format(llp.getY()) + ", "
                    + df.format(llp.getX()) + ") - x, y (" + x + ","
                    + y + ")";
        } else {
            return "x, y (" + x + "," + y + ")";
        }
    }

    public String getPrettyName() {
        return prettyName;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        prettyName = props.getProperty(prefix + Layer.PrettyNameProperty,
                prettyName);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + Layer.PrettyNameProperty, prettyName);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                this.getClass(),
                Layer.PrettyNameProperty,
                "Name",
                "Name for formatter",
                null);

        return props;
    }
}
