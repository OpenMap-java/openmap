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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriPoint.java,v $
// $RCSfile: EsriPoint.java,v $
// $Revision: 1.10 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMPoint;

/**
 * An extension to OMPoint that typecasts a specific Esri graphic
 * type. Used to ensure that all OMGraphics added to a EsriGraphicList
 * is of the same type.
 * 
 * @author Doug Van Auken
 */
public class EsriPoint extends OMPoint implements Cloneable, EsriGraphic,
        OMGraphicConstants {

    protected int type = SHAPE_TYPE_POINT; 
    
    public EsriPoint(double lat, double lon) {
        super(lat, lon);
    }

    /**
     * The lat/lon extent of the EsriGraphic, assumed to contain miny,
     * minx, maxy maxx in order of the array.
     */
    public void setExtents(double[] extents) {
    // we know what it is.
    }

    /**
     * The lat/lon extent of the EsriGraphic, returned as miny, minx,
     * maxy maxx in order of the array.
     */
    public double[] getExtents() {
        double[] ex = new double[4];
        ex[0] = getLat();
        ex[1] = getLon();
        ex[2] = getLat();
        ex[3] = getLon();
        return ex;
    }
    
    public void setType(int t) {
        type = t;
    }
    
    public int getType() {
        return type;
    }

    public static EsriPoint convert(OMPoint ompoint) {
        if (ompoint.getRenderType() == RENDERTYPE_LATLON) {
            EsriPoint ePoint = new EsriPoint(ompoint.getLat(), ompoint.getLon());
            ePoint.setAttributes(ompoint.getAttributes());
            DrawingAttributes attributes = new DrawingAttributes();
            attributes.setFrom(ompoint);
            attributes.setTo(ePoint);

            // Hmmm. looses drawing information, like Oval, etc.
            // That's not even kept in the shape file, so it might be
            // something for the drawing attributes. Better save it
            // in case someone looks for it later.
            ePoint.setOval(ompoint.isOval());
            ePoint.setRadius(ompoint.getRadius());
            return ePoint;
        } else {
            return null;
        }
    }

    public EsriGraphic shallowCopy() {
        return shallowCopyPoint();
    }

    public EsriPoint shallowCopyPoint() {
        return (EsriPoint) super.clone();
    }

}