//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
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
//$RCSfile: SphereBox.java,v $
//$Revision: 1.1 $
//$Date: 2006/04/07 17:44:36 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.tools.j3d.geometry;

import javax.vecmath.Point3d;

import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.tools.j3d.J3DGeo;

public class SphereBox {

    LatLonPoint upper;
    LatLonPoint lower;
    double lowerElevation;
    double upperElevation;

    public SphereBox() {
        lowerElevation = Double.POSITIVE_INFINITY;
        upperElevation = Double.NEGATIVE_INFINITY;
    }

    public SphereBox(Point3d lower, Point3d upper) {
        init(new J3DGeo(lower.x, lower.y, lower.z));
        add(upper);
    }

    public SphereBox(J3DGeo lower, J3DGeo upper) {
        init(lower);
        add(upper);
    }

    public SphereBox(SphereBox sBox) {
        this.upper = sBox.upper;
        this.lower = sBox.lower;
        lowerElevation = sBox.lowerElevation;
        upperElevation = sBox.upperElevation;
    }

    public void init(J3DGeo lower) {
        this.lower = new LatLonPoint.Double(lower.getLatitudeRadians(), lower.getLongitudeRadians(), true);
        this.upper = new LatLonPoint.Double(lower.getLatitudeRadians(), lower.getLongitudeRadians(), true);
        lowerElevation = lower.length();
        upperElevation = lowerElevation;
    }

    public J3DGeo getLower() {
        if (lower != null) {
            return new J3DGeo(lower.getRadLat(), lower.getRadLon(), false, lowerElevation);
        }

        return null;
    }

    public J3DGeo getUpper() {
        if (upper != null) {
            return new J3DGeo(upper.getRadLat(), upper.getRadLon(), false, upperElevation);
        }

        return null;
    }

    public void add(Point3d pt) {
        add(new J3DGeo(pt.x, pt.y, pt.z));
    }

    public void add(J3DGeo pt) {

        if (lower == null) {
            init(pt);
            return;
        }

        double llat = lower.getRadLat();
        double llon = lower.getRadLon();
        double ulat = upper.getRadLat();
        double ulon = upper.getRadLon();

        double lat = pt.getLatitudeRadians();
        double lon = pt.getLongitudeRadians();
        double elev = pt.length();

        if (lat < llat) {
            llat = lat;
        }

        if (lat > ulat) {
            ulat = lat;
        }

        if (lon < llon) {
            llon = lon;
        }

        if (lon > ulon) {
            ulon = lon;
        }

        if (elev < lowerElevation) {
            lowerElevation = elev;
        }

        if (elev > upperElevation) {
            upperElevation = elev;
        }
        
        upper.setLatLon(ulat, ulon, true);
        lower.setLatLon(llat, llon, true);
    }

    public void add(SphereBox sBox) {
        J3DGeo lg = sBox.getLower();
        J3DGeo ug = sBox.getUpper();
        if (lg != null)
            add(lg);
        if (ug != null)
            add(ug);
    }

}
