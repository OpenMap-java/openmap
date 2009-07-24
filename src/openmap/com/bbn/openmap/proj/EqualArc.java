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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/EqualArc.java,v $
// $RCSfile: EqualArc.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:22 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

/**
 * A designator interface to let layers know that a Projection is an
 * Equal Arc projection, meaning that the variations in latitude and
 * longitude are constant.
 */
public interface EqualArc extends Projection {

    /**
     * Returns the x pixel constant of the projection. This was
     * calculated when the projection was created. Represents the
     * number of pixels around the earth (360 degrees).
     */
    public double getXPixConstant();

    /**
     * Returns the y pixel constant of the projection. This was
     * calculated when the projection was created. Represents the
     * number of pixels from 0 to 90 degrees.
     */
    public double getYPixConstant();
}