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
// $Source:
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JBitmap.java,v
// $
// $RCSfile: JBitmap.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 20:59:19 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

// import netscape.application.*;
import java.io.Serializable;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.BitmapPackage.EBitmap;
import com.bbn.openmap.omGraphics.OMBitmap;

public class JBitmap extends OMBitmap implements Serializable, JObjectHolder {

    public java.lang.String bmref;
    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    protected int x_hot, y_hot;//UNUSED

    /** Constructor. Creates an OMBitmap out of a EBitmap. */
    public JBitmap(EBitmap ebit) {
        super();
        JGraphic.fillOMGraphicParams(this, ebit.egraphic);

        x = ebit.p1.x;
        y = ebit.p1.y;
        lat = ebit.ll1.lat;
        lon = ebit.ll1.lon;
        height = ebit.height;
        width = ebit.width;
        bits = ebit.bits;
        x_hot = ebit.x_hot;
        y_hot = ebit.y_hot;
        bmref = ebit.bmref;
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
                       com.bbn.openmap.corba.CSpecialist.BitmapPackage.BF_update update) {
        // do the updates, but don't rerender just yet

        switch (update.discriminator().value()) {
        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_ll1:
            LLPoint ll = update.ll1();
            setLat(ll.lat);
            setLon(ll.lon);
            break;

        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_p1:
            XYPoint pt = update.p1();
            setX(pt.x);
            setY(pt.y);
            break;

        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_width:
            setWidth(update.width());
            break;

        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_height:
            setHeight(update.height());
            break;

        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_x_hot:
            //            setX_hot(update.x_hot());
            break;

        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_y_hot:
            //            setY_hot(update.y_hot());
            break;

        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_bits:
            setBits(update.bits());
            break;

        case com.bbn.openmap.corba.CSpecialist.BitmapPackage.settableFields._BF_bmref:
            System.err.println("CSBitmap.update: bmref update not supported/necessary.");
            break;

        default:
            System.err.println("CSBitmap.update: invalid bitmap update");
            break;
        }
    }
}