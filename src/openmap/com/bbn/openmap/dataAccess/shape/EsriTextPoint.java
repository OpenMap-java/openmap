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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/EsriTextPoint.java,v $
// $RCSfile: EsriTextPoint.java,v $
// $Revision: 1.2 $
// $Date: 2006/08/25 15:36:12 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMText;

/**
 * An extension to OMPoint that typecasts a specific Esri graphic
 * type. Used to ensure that all OMGraphics added to a EsriGraphicList
 * is of the same type.
 * 
 * @author Doug Van Auken
 */
public class EsriTextPoint extends OMText  implements Cloneable, EsriGraphic,
        OMGraphicConstants {

    protected int type = SHAPE_TYPE_POINT;
    
    public EsriTextPoint(float lat, float lon, String stuff, int justification) {
        super(lat, lon, stuff, justification);
    }

    /**
     * The lat/lon extent of the EsriGraphic, assumed to contain miny,
     * minx, maxy maxx in order of the array.
     */
    public void setExtents(float[] extents) {
    // we know what it is.
    }

    /**
     * The lat/lon extent of the EsriGraphic, returned as miny, minx,
     * maxy maxx in order of the array.
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
    
    public static EsriTextPoint convert(OMText omtext) {
        if (omtext.getRenderType() == RENDERTYPE_LATLON) {
            EsriTextPoint etp = new EsriTextPoint(omtext.getLat(), omtext.getLon(), omtext.getData(), omtext.getJustify());
            etp.setAttributes(omtext.getAttributes());
            DrawingAttributes attributes = new DrawingAttributes();
            attributes.setFrom(omtext);
            attributes.setTo(etp);

            return etp;
        } else {
            return null;
        }
    }

    public EsriGraphic shallowCopy() {
        return shallowCopyPoint();
    }

    public EsriTextPoint shallowCopyPoint() {
        return (EsriTextPoint) super.clone();
    }

}