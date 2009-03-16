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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JLine.java,v $
// $RCSfile: JLine.java,v $
// $Revision: 1.4 $
// $Date: 2009/02/23 22:37:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.LinePackage.ELine;
import com.bbn.openmap.omGraphics.OMLine;

/**
 * CSLine - simple lines, use CSPoly with no fillColor to get
 * polylines
 */
public class JLine extends OMLine implements JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    /** Constructor. */
    public JLine(ELine eline) {
        super();
        JGraphic.fillOMGraphicParams(this, eline.egraphic);
        double[] lls = new double[4];
        int[] pts = new int[4];
        lls[0] = eline.ll1.lat;
        lls[1] = eline.ll1.lon;
        lls[2] = eline.ll2.lat;
        lls[3] = eline.ll2.lon;

        pts[0] = eline.p1.x;
        pts[1] = eline.p1.y;
        pts[2] = eline.p2.x;
        pts[3] = eline.p2.y;
        setLL(lls);
        setPts(pts);
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

    public void update(com.bbn.openmap.corba.CSpecialist.LinePackage.LF_update update) {
        // do the updates, but don't rerender just yet

        switch (update.discriminator().value()) {
        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.LinePackage.settableFields._LF_ll1:
            LLPoint ll1 = update.ll1();
            double[] lls = getLL();
            lls[0] = ll1.lat;
            lls[1] = ll1.lon;
            break;

        case com.bbn.openmap.corba.CSpecialist.LinePackage.settableFields._LF_p1:
            XYPoint pt1 = update.p1();
            int[] pts = getPts();
            pts[0] = pt1.x;
            pts[1] = pt1.y;
            break;

        case com.bbn.openmap.corba.CSpecialist.LinePackage.settableFields._LF_ll2:
            LLPoint ll2 = update.ll2();
            lls = getLL();
            lls[2] = ll2.lat;
            lls[3] = ll2.lon;
            break;

        case com.bbn.openmap.corba.CSpecialist.LinePackage.settableFields._LF_p2:
            XYPoint pt2 = update.p2();
            pts = getPts();
            pts[2] = pt2.x;
            pts[3] = pt2.y;
            break;

        default:
            System.err.println("CSLine.update: invalid line update");
            break;
        }
    }
}