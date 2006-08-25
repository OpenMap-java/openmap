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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolygonZ.java,v $
// $RCSfile: EsriPolygonZ.java,v $
// $Revision: 1.2 $
// $Date: 2006/08/25 15:36:14 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;

/**
 * An extension to OMPoly for polygons that typecasts a specific Esri graphic
 * type. Used to ensure that all OMGraphics added to a EsriGraphicList is of the
 * same type.
 * 
 * @author Doug Van Auken
 */
public class EsriPolygonZ extends EsriPolygon implements Cloneable, EsriGraphic {

    protected float[] extents;

    public EsriPolygonZ(float[] points, int units, int lineType) {
        super(points, units, lineType);
    }

    public static EsriPolygonZ convert(OMPoly ompoly) {
        if (ompoly.getRenderType() == RENDERTYPE_LATLON) {

            float[] rawLL = ompoly.getLatLonArray();
            float[] degreePoints = new float[rawLL.length];
            System.arraycopy(rawLL, 0, degreePoints, 0, rawLL.length);

            EsriPolygonZ ePoly = new EsriPolygonZ(degreePoints, OMGraphic.RADIANS, ompoly.getLineType());
            DrawingAttributes attributes = new DrawingAttributes();
            attributes.setFrom(ompoly);
            attributes.setTo(ePoly);
            ePoly.setAttributes(ompoly.getAttributes());
            return ePoly;
        } else {
            return null;
        }
    }

    public int getType() {
        return SHAPE_TYPE_POLYGONZ;
    }

    public EsriPolygonZ shallowCopyPolygon() {
        return (EsriPolygonZ) clone();
    }
}