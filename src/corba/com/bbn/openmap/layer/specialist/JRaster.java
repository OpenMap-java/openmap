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
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JRaster.java,v
// $
// $RCSfile: JRaster.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 20:59:19 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.awt.Color;
import java.io.Serializable;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.RasterPackage.ERaster;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.util.ColorFactory;

public class JRaster extends OMRaster implements Serializable, JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    protected int x_hot, y_hot;//UNUSED

    /** Constructor */
    public JRaster(ERaster eraster) {
        super();
        JGraphic.fillOMGraphicParams(this, eraster.egraphic);
        setColorModel(COLORMODEL_INDEXED);

        x = eraster.p1.x;
        y = eraster.p1.y;
        lat = eraster.ll1.lat;
        lon = eraster.ll1.lon;
        height = eraster.height;
        width = eraster.width;
        bits = eraster.pixels;

        // Transparency HACK for Java RPF specialist...
        transparent = eraster.transparent & 0xff;
        // scaling HACK!
        if ((eraster.transparent & 0xff00) != 0) {
            scaleTo(eraster.x_hot, eraster.y_hot, OMRaster.FAST_SCALING);
        } else {
            x_hot = eraster.x_hot;
            y_hot = eraster.y_hot;
        }

        // save the tmp colortable
        Color[] tmpColors = new Color[eraster.colorsTotal];
        for (int i = 0; i < eraster.colorsTotal; i++) {
            tmpColors[i] = ColorFactory.createColor(eraster.ct[i].red,
                    eraster.ct[i].green,
                    eraster.ct[i].blue,
                    eraster.ct[i].open,
                    true);
        }
        setColors(tmpColors);

        setNeedToRegenerate(true);
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
                       com.bbn.openmap.corba.CSpecialist.RasterPackage.RASF_update update) {
        // do the updates, but don't rerender just yet
        setNeedToRegenerate(true); // flag dirty

        switch (update.discriminator().value()) {
        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_ll1:
            LLPoint ll = update.ll1();
            lat = ll.lat;
            lon = ll.lon;
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_p1:
            XYPoint pt = update.p1();
            x = pt.x;
            y = pt.y;
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_width:
            width = update.width();
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_height:
            height = update.height();
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_x_hot:
            x_hot = update.x_hot();
            needToRegenerate = false;
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_y_hot:
            y_hot = update.y_hot();
            needToRegenerate = false;
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_pixels:
            bits = update.pixels();
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_colorsTotal:
            // No longer needed!
            // colorsTotal = update.colorsTotal();
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_ct:
            com.bbn.openmap.corba.CSpecialist.CTEntry[] ct = update.ct();
            Color[] tmpColors = new Color[ct.length];
            for (int i = 0; i < ct.length; i++) {
                tmpColors[i] = ColorFactory.createColor(ct[i].red,
                        ct[i].green,
                        ct[i].blue,
                        ct[i].open,
                        true);
            }
            setColors(tmpColors);
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_transparent:
            transparent = update.transparent();
            break;

        case com.bbn.openmap.corba.CSpecialist.RasterPackage.settableFields._RASF_openColors:
        default:
            System.err.println("JRaster.update: invalid raster update");
            setNeedToRegenerate(false);
            break;
        }
    }
}