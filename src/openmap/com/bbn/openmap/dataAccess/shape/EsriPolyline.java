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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPolyline.java,v $
// $RCSfile: EsriPolyline.java,v $
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.ProjMath;

/**
 * An extension to OMPoly for polylines that typecasts a specific Esri
 * graphic type.  Used to ensure that all OMGraphics added to a
 * EsriGraphicList is of the same type. 
 *
 * @author Doug Van Auken
 */
public class EsriPolyline extends OMPoly implements EsriGraphic, Cloneable {

    float[] extents;

    public EsriPolyline(float[] points, int units, int lineType) {
        super(points, units, lineType);
        float[] degreePoints = new float[points.length];
        System.arraycopy(points, 0, degreePoints, 0, points.length);
        addExtents(ProjMath.arrayRadToDeg(degreePoints));
    }

    /**
     * The lat/lon extent of the EsriGraphic, assumed to contain miny,
     * minx, maxy maxx in order of the array.  
     */
    public void setExtents(float[] extents) {
        this.extents = extents;
    }
 
   /**
     * The lat/lon extent of the EsriGraphic, returned as miny, minx,
     * maxy maxx in order of the array.  
     */
    public float[] getExtents() {
        if (extents == null) {
            // These are set to their opposites to guarantee some
            // movement.
            extents = new float[] { 90f, 180f, -90f, -180f };
        }
        return extents;
    }

    public void addExtents(float[] graphicExtents) {
        float[] ex = getExtents();

        // Check both graphic extents in case they are inadvertently
        // switched.
        for (int i = 0; i < graphicExtents.length; i+=2) {
            if (ex[0] > graphicExtents[i]) ex[0] = graphicExtents[i];
            if (ex[1] > graphicExtents[i+1]) ex[1] = graphicExtents[i+1];
            if (ex[2] < graphicExtents[i]) ex[2] = graphicExtents[i];
            if (ex[3] < graphicExtents[i+1]) ex[3] = graphicExtents[i+1];
        }

//      System.out.println("extents of Polyline: " +
//                         ex[1] + ", " +
//                         ex[0] + ", " +
//                         ex[3] + ", " +
//                         ex[2]);
    }

    public static EsriPolyline convert(OMPoly ompoly) {
        if (ompoly.getRenderType() == RENDERTYPE_LATLON) {

            float[] rawLL = ompoly.getLatLonArray();
            float[] degreePoints = new float[rawLL.length];
            System.arraycopy(rawLL, 0, degreePoints, 0, rawLL.length);

            EsriPolyline ePoly = new EsriPolyline(degreePoints, OMGraphic.RADIANS, ompoly.getLineType());
            ePoly.setAppObject(ompoly.getAppObject());
            DrawingAttributes attributes = new DrawingAttributes();
            attributes.setFrom(ompoly);
            attributes.setTo(ePoly);

            return ePoly;
        } else {
            return null;
        }
    }

    public EsriGraphic shallowCopy() {
        return shallowCopyPolyline();
    }

    public EsriPolyline shallowCopyPolyline() {
        try {
            return (EsriPolyline) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
