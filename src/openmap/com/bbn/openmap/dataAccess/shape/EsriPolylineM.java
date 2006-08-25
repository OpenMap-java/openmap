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
// $Revision: 1.2 $
// $Date: 2006/08/25 15:36:13 $
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

    public EsriPolylineM(float[] points, int units, int lineType) {
        super(points, units, lineType);
    }

    public static EsriPolylineM convert(OMPoly ompoly) {
        if (ompoly.getRenderType() == RENDERTYPE_LATLON) {

            float[] rawLL = ompoly.getLatLonArray();
            float[] degreePoints = new float[rawLL.length];
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

    public EsriPolylineM shallowCopyPolyline() {
        return (EsriPolylineM) clone();
    }
}