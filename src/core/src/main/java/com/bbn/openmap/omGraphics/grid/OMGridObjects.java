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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/OMGridObjects.java,v
// $
// $RCSfile: OMGridObjects.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.Projection;

public interface OMGridObjects {

    public Object find(int id);

    public OMGraphic generate(int id, Projection proj);
}

