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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/shape/ESRILinkPolygonRecord.java,v $
// $RCSfile: ESRILinkPolygonRecord.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:09 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link.shape;

import java.io.IOException;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.layer.link.*;
import com.bbn.openmap.layer.shape.*;

/**
 */
public class ESRILinkPolygonRecord extends ESRIPolygonRecord 
    implements ESRILinkRecord {

    public ESRILinkPolygonRecord() {
        super();
    }

    public ESRILinkPolygonRecord(byte b[], int off)
        throws IOException{
        super(b, off);
    }

    /**
     * Generates OMGraphics and adds them to the given list.
     * <p>
     * Copy the poly points array because the OMPoly converts from
     * degrees to radians in place, trashing the shape.
     *
     * @param lgl the graphics response to write the graphic to.
     * @param properties the semantic description of how the graphic should be drawn.
     */
    public void writeLinkGraphics (LinkGraphicList lgl,
                                   LinkProperties properties) 
        throws IOException {
        int nPolys = polygons.length;
        if (nPolys <= 0) return;
        OMPoly p=null;
        float[] pts;
        boolean ispolyg = isPolygon();

        for (int i=0, j, k; i<nPolys; i++) {
            // these points are already in RADIAN lat,lon order!...
            pts = ((ESRIPoly.ESRIFloatPoly)polygons[i]).getRadians();

            lgl.addPoly(pts, OMGraphic.RADIANS, 
                       OMGraphic.LINETYPE_STRAIGHT, properties); 

        }
    }
}
