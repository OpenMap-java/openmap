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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/SinkGenerator.java,v $
// $RCSfile: SinkGenerator.java,v $
// $Revision: 1.1 $
// $Date: 2004/01/24 03:38:44 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.SinkGraphic;
import com.bbn.openmap.proj.Projection;
import java.awt.Graphics;

public class SinkGenerator implements OMGridGenerator {
    
    public OMGraphic generate(OMGrid grid, Projection proj) {
        return SinkGraphic.getSharedInstance();
    }

    public boolean needGenerateToRender() {
        return false;
    }
}

