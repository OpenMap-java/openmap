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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/AziDist.java,v $
// $RCSfile: AziDist.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

/**
 * Structure contains azimuth and distance values. Distance units are
 * determined by the operation.
 * 
 * @see GreatCircle#ellipsoidalAziDist
 */
public class AziDist {
    public double faz;// forward azimuth
    public double baz;// backward azimuth
    public double distance;// distance
}