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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/quadtree/QuadTreeLeaf.java,v
// $
// $RCSfile: QuadTreeLeaf.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.quadtree;

import java.io.Serializable;

public class QuadTreeLeaf implements Serializable {

    static final long serialVersionUID = 7885745536157252519L;

    public float latitude;
    public float longitude;
    public Object object;

    public QuadTreeLeaf(float lat, float lon, Object obj) {
        latitude = lat;
        longitude = lon;
        object = obj;
    }
}