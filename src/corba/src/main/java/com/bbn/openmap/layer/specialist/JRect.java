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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JRect.java,v $
// $RCSfile: JRect.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.io.Serializable;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.RectanglePackage.ERectangle;
import com.bbn.openmap.omGraphics.OMRect;

/** JRect - rectangles */
public class JRect extends OMRect implements Serializable, JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    /** Constructor. */
    public JRect(ERectangle erect) {
        super();
        JGraphic.fillOMGraphicParams(this, erect.egraphic);

        x1 = erect.p1.x;
        y1 = erect.p1.y;
        x2 = erect.p2.x;
        y2 = erect.p2.y;
        lat1 = erect.ll1.lat;
        lon1 = erect.ll1.lon;
        lat2 = erect.ll2.lat;
        lon2 = erect.ll2.lon;
    }

    public void setObject(com.bbn.openmap.corba.CSpecialist.EComp aObject) {
        object = aObject;
    }

    public com.bbn.openmap.corba.CSpecialist.EComp getObject() {
        return object;
    }

    public void update(
                       com.bbn.openmap.corba.CSpecialist.GraphicPackage.GF_update update) {
        JGraphic.update((JObjectHolder) this, update);
    }

    public void update(
                       com.bbn.openmap.corba.CSpecialist.RectanglePackage.RF_update update) {
        // do the updates, but don't rerender just yet

        switch (update.discriminator().value()) {
        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.RectanglePackage.settableFields._RF_ll1:
            LLPoint ll1 = update.ll1();
            lat1 = ll1.lat;
            lon1 = ll1.lon;
            setNeedToRegenerate(true);
            break;

        case com.bbn.openmap.corba.CSpecialist.RectanglePackage.settableFields._RF_p1:
            XYPoint pt1 = update.p1();
            x1 = pt1.x;
            y1 = pt1.y;
            if (renderType != RENDERTYPE_LATLON)
                setNeedToRegenerate(true);
            break;

        case com.bbn.openmap.corba.CSpecialist.RectanglePackage.settableFields._RF_ll2:
            LLPoint ll2 = update.ll2();
            lat2 = ll2.lat;
            lon2 = ll2.lon;
            if (renderType == RENDERTYPE_LATLON)
                setNeedToRegenerate(true);
            break;

        case com.bbn.openmap.corba.CSpecialist.RectanglePackage.settableFields._RF_p2:
            XYPoint pt2 = update.p2();
            x2 = pt2.x;
            y2 = pt2.y;
            if (renderType != RENDERTYPE_LATLON)
                setNeedToRegenerate(true);
            break;

        default:
            System.err.println("JRect.update: invalid rect update");
            break;
        }
    }
}