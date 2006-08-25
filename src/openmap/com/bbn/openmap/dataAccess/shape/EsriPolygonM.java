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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolygonM.java,v $
// $RCSfile: EsriPolygonM.java,v $
// $Revision: 1.2 $
// $Date: 2006/08/25 15:36:12 $
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
public class EsriPolygonM extends EsriPolygon implements Cloneable, EsriGraphic {

    protected float[] extents;

    public EsriPolygonM(float[] points, int units, int lineType) {
        super(points, units, lineType);
    }

    public static EsriPolygonM convert(OMPoly ompoly) {
        if (ompoly.getRenderType() == RENDERTYPE_LATLON) {

            float[] rawLL = ompoly.getLatLonArray();
            float[] degreePoints = new float[rawLL.length];
            System.arraycopy(rawLL, 0, degreePoints, 0, rawLL.length);

            EsriPolygonM ePoly = new EsriPolygonM(degreePoints, OMGraphic.RADIANS, ompoly.getLineType());
            DrawingAttributes attributes = new DrawingAttributes();
            attributes.setFrom(ompoly);
            attributes.setTo(ePoly);
            ePoly.setAttributes(ompoly.getAttributes());
            ePoly.setIsPolygon(true);
            return ePoly;
        } else {
            return null;
        }
    }

    public int getType() {
        return SHAPE_TYPE_POLYGONM;
    }

    public EsriPolygonM shallowCopyPolygon() {
        return (EsriPolygonM) clone();
    }
}