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
//$RCSfile: CoordInfoFormatter.java,v $
//$Revision: 1.2 $
//$Date: 2008/10/10 00:57:21 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.util.coordFormatter;

import java.awt.geom.Point2D;

import com.bbn.openmap.PropertyConsumer;

/**
 * A CoordInfoFormatter is an object that can create a String for display from coordinate information.
 * 
 * @author dietrick
 */
public interface CoordInfoFormatter extends PropertyConsumer {

    /**
     * The main function of this class
     * @param x the x pixel coordinate of the map, 0 is left side.
     * @param y the y pixel coordinate of the map, 0 is top.
     * @param llp the LatLonPoint represented by the pixel location.
     * @param source the object calling this method.
     * @return String with coordinate info on it.
     */
    public String createCoordinateInformationLine(int x, int y,
                                                  Point2D llp, Object source);

    /**
     * A name, suitable for GUI display, of this formatter.
     * @return String for pretty name for formatter
     */
    public String getPrettyName();
}
