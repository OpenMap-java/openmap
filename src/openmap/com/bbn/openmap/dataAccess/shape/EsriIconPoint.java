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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriIconPoint.java,v $
// $RCSfile: EsriIconPoint.java,v $
// $Revision: 1.2 $
// $Date: 2006/08/25 15:36:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.awt.Image;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMScalingIcon;

/**
 * An extension to OMPoint that typecasts a specific Esri graphic type. Used to
 * ensure that all OMGraphics added to a EsriGraphicList is of the same type.
 * 
 * @author Doug Van Auken
 */
public class EsriIconPoint extends OMScalingIcon implements Cloneable,
        EsriGraphic, OMGraphicConstants {
    
    protected int type = SHAPE_TYPE_POINT;

    public EsriIconPoint(float lat, float lon, ImageIcon imageIcon) {
        super(lat, lon, imageIcon);
    }

    public EsriIconPoint(float lat, float lon, Image image) {
        super(lat, lon, image);
    }

    /**
     * The lat/lon extent of the EsriGraphic, assumed to contain miny, minx,
     * maxy maxx in order of the array.
     */
    public void setExtents(float[] extents) {
    // we know what it is.
    }

    /**
     * The lat/lon extent of the EsriGraphic, returned as miny, minx, maxy maxx
     * in order of the array.
     */
    public float[] getExtents() {
        float[] ex = new float[4];
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

    public static EsriIconPoint convert(OMScalingIcon omscalingicon) {
        if (omscalingicon.getRenderType() == RENDERTYPE_LATLON) {
            EsriIconPoint eip = new EsriIconPoint(omscalingicon.getLat(), omscalingicon.getLon(), omscalingicon.getImage());
            eip.setBaseScale(omscalingicon.getBaseScale());
            eip.setMaxScale(omscalingicon.getMaxScale());
            eip.setMinScale(omscalingicon.getMinScale());
            eip.setAttributes(omscalingicon.getAttributes());
            DrawingAttributes attributes = new DrawingAttributes();
            attributes.setFrom(omscalingicon);
            attributes.setTo(eip);

            return eip;
        } else {
            return null;
        }
    }

    public EsriGraphic shallowCopy() {
        return shallowCopyPoint();
    }

    public EsriIconPoint shallowCopyPoint() {
        return (EsriIconPoint) super.clone();
    }

}