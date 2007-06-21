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
//$RCSfile: UTMGCT.java,v $
//$Revision: 1.1 $
//$Date: 2007/06/21 21:39:03 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.proj.coords;

import java.awt.Point;
import java.util.Properties;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.util.PropUtils;

public class UTMGCT extends OMComponent implements GeoCoordTransformation {

    public final static String NorthingProperty = "northing";
    public final static String EastingProperty = "easting";
    public final static String ZoneProperty = "zone";
    public final static String HemiProperty = "hemi";
    public final static String ElliposoidProperty = "ellipsoid";

    protected UTMPoint utm;
    protected Ellipsoid ellipsoid = Ellipsoid.WGS_84;
    protected LatLonPoint tmpLL = new LatLonPoint.Double();

    /**
     * Make sure you call setProperties() or set the UTM before trying to use
     * this object.
     * 
     */
    public UTMGCT() {}

    public UTMGCT(UTMPoint utmPoint) {
        utm = utmPoint;
    }

    public UTMPoint getUtm() {
        return utm;
    }

    public void setUtm(UTMPoint utm) {
        this.utm = utm;
    }

    public Point forward(double lat, double lon) {
        return forward(lat, lon, new Point());
    }

    public Point forward(double lat, double lon, Point ret) {
        if (utm == null) {
            return null;
        }

        tmpLL.setLatLon((float) lat, (float) lon);
        UTMPoint.LLtoUTM(tmpLL, ellipsoid, utm);
        ret.setLocation(utm.easting, utm.northing);
        return ret;
    }

    public LatLonPoint inverse(double x, double y) {
        return inverse(x, y, new LatLonPoint.Double());
    }

    public LatLonPoint inverse(double x, double y, LatLonPoint ret) {
        if (utm == null) {
            return null;
        }

        utm.easting = (float) x;
        utm.northing = (float) y;
        UTMPoint.UTMtoLL(utm, ellipsoid, ret);
        return ret;
    }

    public void setProperties(String propertyPrefix, Properties props) {
        super.setProperties(propertyPrefix, props);

        propertyPrefix = PropUtils.getScopedPropertyPrefix(propertyPrefix);
        float northing = PropUtils.floatFromProperties(props, propertyPrefix
                + NorthingProperty, 0.0f);
        float easting = PropUtils.floatFromProperties(props, propertyPrefix
                + EastingProperty, 0.0f);
        int zone = PropUtils.intFromProperties(props, propertyPrefix
                + ZoneProperty, 0);
        String hString = props.getProperty(propertyPrefix + HemiProperty, "N");
        char hemi = hString.charAt(0);

        utm = new UTMPoint(northing, easting, zone, hemi);

        String eName = props.getProperty(propertyPrefix + ElliposoidProperty,
                "WGS_84");
        ellipsoid = Ellipsoid.getByName(eName);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        if (utm != null) {
            props.put(prefix + NorthingProperty, Float.toString(utm.northing));
            props.put(prefix + EastingProperty, Float.toString(utm.easting));
            props.put(prefix + ZoneProperty, Integer.toString(utm.zone_number));
            props.put(prefix + HemiProperty,
                    Character.toString(utm.zone_letter));

            props.put(prefix + ElliposoidProperty, ellipsoid.name.toUpperCase()
                    .replace(" ", "_"));
        }
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        return props;
    }
}
