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
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JPoly.java,v
// $
// $RCSfile: JPoly.java,v $
// $Revision: 1.4 $
// $Date: 2009/02/23 22:37:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.io.Serializable;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.PolyPackage.CoordMode;
import com.bbn.openmap.corba.CSpecialist.PolyPackage.EPoly;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.ProjMath;

public class JPoly extends OMPoly implements Serializable, JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    /** Constructor. */
    public JPoly(EPoly epoly) {
        super();
        JGraphic.fillOMGraphicParams(this, epoly.egraphic);

        java.awt.Color fc = JGraphic.getColor(epoly.egraphic.fillColor);
        setIsPolygon(fc != null);

        units = OMGraphic.RADIANS;
        lat = ProjMath.degToRad(epoly.ll1.lat);
        lon = ProjMath.degToRad(epoly.ll1.lon);

        int npts = epoly.xypoints.length;
        if (npts > 0) {
            xs = new int[npts];
            ys = new int[npts];

            for (int i = 0; i < npts; i++) {
                xs[i] = epoly.xypoints[i].x;
                ys[i] = epoly.xypoints[i].y;
            }
        }

        npts = epoly.llpoints.length;
        if (npts > 0) {
            int i, j;
            rawllpts = (isPolygon) ? new double[npts * 2 + 2]//*2 for
                                                            // pairs
                                                            // +2 for
                                                            // connect
                    : new double[npts * 2];//*2 for pairs

            for (i = 0, j = 0; i < npts; i++, j += 2) {
                rawllpts[j] = ProjMath.degToRad(epoly.llpoints[i].lat);
                rawllpts[j + 1] = ProjMath.degToRad(epoly.llpoints[i].lon);
            }
            if (isPolygon) {//connect the polygon
                rawllpts[j] = rawllpts[0];
                rawllpts[j + 1] = rawllpts[1];
            }
        }

        coordMode = getCoordMode(epoly.cMode);
    }

    public static int getCoordMode(CoordMode cMode) {

        int cm;
        switch (cMode.value()) {
        case CoordMode._CModePrevious:
            cm = COORDMODE_PREVIOUS;
            break;
        default:
            cm = COORDMODE_ORIGIN;
            break;
        }
        return cm;

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

    /**
     * update() - takes a PolyPackage.PF_update and changes the fields
     * that need to be. Called as a result of a gesture.
     */
    public void update(com.bbn.openmap.corba.CSpecialist.PolyPackage.PF_update update) {
        int npts;

        switch (update.discriminator().value()) {

        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.PolyPackage.settableFields._PF_ll1:
            LLPoint ll = update.ll1();
            lat = ll.lat;
            lon = ll.lon;
            setNeedToRegenerate(true);
            break;

        // set coordinate mode

        case com.bbn.openmap.corba.CSpecialist.PolyPackage.settableFields._PF_cMode:
            int newMode = getCoordMode(update.cMode());
            if (newMode != coordMode) {
                coordMode = newMode;
                setNeedToRegenerate(true);
            }
            break;

        // set xypoints
        case com.bbn.openmap.corba.CSpecialist.PolyPackage.settableFields._PF_xypoints:

            com.bbn.openmap.corba.CSpecialist.XYPoint[] xypoints = update.xypoints();
            npts = xypoints.length;
            if (npts > 0) {
                xs = new int[npts];
                ys = new int[npts];

                for (int i = 0; i < npts; i++) {
                    xs[i] = xypoints[i].x;
                    ys[i] = xypoints[i].y;
                }
            }

            if (renderType != RENDERTYPE_LATLON)
                setNeedToRegenerate(true);
            break;

        // set llpoints
        case com.bbn.openmap.corba.CSpecialist.PolyPackage.settableFields._PF_llpoints:
            com.bbn.openmap.corba.CSpecialist.LLPoint[] llpoints = update.llpoints();
            npts = llpoints.length;
            if (npts > 0) {
                rawllpts = new double[npts * 2];

                for (int i = 0; i < npts; i += 2) {
                    rawllpts[i] = llpoints[i].lat;
                    rawllpts[i + 1] = llpoints[i].lon;
                }
            }

            if (renderType == RENDERTYPE_LATLON)
                setNeedToRegenerate(true);
            break;

        default:
            System.err.println("JPoly.update: invalid poly update");
            break;
        }
    }
}