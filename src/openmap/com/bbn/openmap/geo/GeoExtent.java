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
//$RCSfile: GeoExtent.java,v $
//$Revision: 1.3 $
//$Date: 2007/01/30 20:37:04 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.geo;

/**
 * An object with some geographical representation on the earth. Can
 * be a point, line or area-type thing, i.e. anything that could be
 * plotted on a map.
 */
public interface GeoExtent {
    /** compute a point and radius around the extent. */
    BoundingCircle getBoundingCircle();
    /** return an object that this extent represents. */
    Object getID();
}
