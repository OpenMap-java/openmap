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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/shape/ESRILinkPolygonRecord.java,v $
// $RCSfile: ESRILinkPolygonRecord.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 18:09:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.shape;

import java.io.IOException;

import com.bbn.openmap.layer.link.LinkGraphicList;
import com.bbn.openmap.layer.link.LinkProperties;
import com.bbn.openmap.layer.shape.ESRIPoly;
import com.bbn.openmap.layer.shape.ESRIPolygonRecord;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 */
public class ESRILinkPolygonRecord extends ESRIPolygonRecord implements
        ESRILinkRecord {

    public ESRILinkPolygonRecord() {
        super();
    }

    public ESRILinkPolygonRecord(byte b[], int off) throws IOException {
        super(b, off);
    }

    /**
     * Generates OMGraphics and adds them to the given list.
     * <p>
     * Copy the poly points array because the OMPoly converts from
     * degrees to radians in place, trashing the shape.
     * 
     * @param lgl the graphics response to write the graphic to.
     * @param properties the semantic description of how the graphic
     *        should be drawn.
     */
    public void writeLinkGraphics(LinkGraphicList lgl, LinkProperties properties)
            throws IOException {
        int nPolys = polygons.length;
        if (nPolys <= 0)
            return;

        for (int i = 0; i < nPolys; i++) {
            // these points are already in RADIAN lat,lon order!...
            lgl.addPoly(((ESRIPoly.ESRIFloatPoly) polygons[i]).getRadians(),
                    OMGraphic.RADIANS,
                    OMGraphic.LINETYPE_STRAIGHT,
                    properties);

        }
    }
}