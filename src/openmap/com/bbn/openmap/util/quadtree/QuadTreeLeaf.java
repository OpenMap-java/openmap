// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/quadtree/QuadTreeLeaf.java,v $
// $RCSfile: QuadTreeLeaf.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.util.quadtree;

import java.util.*;
import java.io.Serializable;

public class QuadTreeLeaf implements Serializable{

    static final long serialVersionUID = 7885745536157252519L;

    public float latitude;
    public float longitude;
    public Object object;

    public QuadTreeLeaf (float lat, float lon, Object obj){
	latitude = lat;
	longitude = lon;
	object = obj;
    }
}
