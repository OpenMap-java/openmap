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
//$RCSfile: AbstractGCT.java,v $
//$Revision: 1.2 $
//$Date: 2008/01/29 22:04:13 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.proj.coords;

import java.awt.geom.Point2D;

import com.bbn.openmap.OMComponent;

public abstract class AbstractGCT extends OMComponent implements GeoCoordTransformation {

    public Point2D forward(double lat, double lon) {
        return forward(lat, lon, new Point2D.Double());
    }

    public abstract Point2D forward(double lat, double lon, Point2D ret);

    public LatLonPoint inverse(double x, double y) {
        return inverse(x, y, new LatLonPoint.Double());
    }

    public abstract LatLonPoint inverse(double x, double y, LatLonPoint ret);

}
