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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolylineM.java,v $
// $RCSfile: EsriPolylineM.java,v $
// $Revision: 1.4 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;

/**
 * An extension to OMPoly for polylines that typecasts a specific Esri graphic
 * type. Used to ensure that all OMGraphics added to a EsriGraphicList is of the
 * same type.
 * 
 * @author Doug Van Auken
 */
public class EsriPolylineM extends EsriPolyline implements EsriGraphic,
        Cloneable {

    public EsriPolylineM(double[] points, int units, int lineType) {
        super(points, units, lineType);
    }

    public static EsriPolyline convert(OMPoly ompoly) {
        if (ompoly.getRenderType() == RENDERTYPE_LATLON) {

            double[] rawLL = ompoly.getLatLonArray();
            double[] degreePoints = new double[rawLL.length];
            System.arraycopy(rawLL, 0, degreePoints, 0, rawLL.length);

            EsriPolylineM ePoly = new EsriPolylineM(degreePoints, OMGraphic.RADIANS, ompoly.getLineType());
            ePoly.setAttributes(ompoly.getAttributes());
            DrawingAttributes attributes = new DrawingAttributes();
            attributes.setFrom(ompoly);
            attributes.setTo(ePoly);

            return ePoly;
        } else {
            return null;
        }
    }

    public int getType() {
        return SHAPE_TYPE_POLYLINEM;
    }

    public EsriPolyline shallowCopyPolyline() {
        return (EsriPolylineM) clone();
    }
}