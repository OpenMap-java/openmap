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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/OMGridObjects.java,v $
// $RCSfile: OMGridObjects.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.Projection;
import java.awt.Graphics;

public interface OMGridObjects {
    
    public Object find(int id); 
    public OMGraphic generate(int id, Projection proj);
}

