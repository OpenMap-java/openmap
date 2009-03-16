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
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JText.java,v
// $
// $RCSfile: JText.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.io.Serializable;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.TextPackage.EText;
import com.bbn.openmap.omGraphics.OMText;

public class JText extends OMText implements Serializable, JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    /**
     * Construct a JText object.
     */
    public JText(EText etext) {
        super();
        JGraphic.fillOMGraphicParams(this, etext.egraphic);

        setX(etext.p1.x);
        setY(etext.p1.y);
        setLat(etext.ll1.lat);
        setLon(etext.ll1.lon);
        setData(etext.data);
        if (!etext.font.equals(""))
            setFont(rebuildFont(etext.font));
        setJustify(etext.justify);
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

    public void update(com.bbn.openmap.corba.CSpecialist.TextPackage.TF_update update) {
        // do the updates, but don't rerender just yet

        switch (update.discriminator().value()) {
        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.TextPackage.settableFields._TF_ll1:
            LLPoint ll = update.ll1();
            setLat(ll.lat);
            setLon(ll.lon);
            break;

        case com.bbn.openmap.corba.CSpecialist.TextPackage.settableFields._TF_p1:
            XYPoint pt = update.p1();
            setX(pt.x);
            setY(pt.y);
            break;

        case com.bbn.openmap.corba.CSpecialist.TextPackage.settableFields._TF_data:
            setData(update.data());
            break;

        case com.bbn.openmap.corba.CSpecialist.TextPackage.settableFields._TF_font:
            setFont(rebuildFont(update.font()));
            break;

        case com.bbn.openmap.corba.CSpecialist.TextPackage.settableFields._TF_justify:
            setJustify(update.justify());
            break;

        default:
            System.err.println("JText.update: invalid text update");
            needToRegenerate = false;
            break;
        }
    }
}