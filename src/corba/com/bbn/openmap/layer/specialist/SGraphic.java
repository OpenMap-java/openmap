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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SGraphic.java,v $
// $RCSfile: SGraphic.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.util.Vector;

import com.bbn.openmap.corba.CSpecialist.CColor;
import com.bbn.openmap.corba.CSpecialist.CStipple;
import com.bbn.openmap.corba.CSpecialist.Comp;
import com.bbn.openmap.corba.CSpecialist.EComp;
import com.bbn.openmap.corba.CSpecialist.Graphic;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.UpdateGraphic;
import com.bbn.openmap.corba.CSpecialist.UpdateRecord;
import com.bbn.openmap.corba.CSpecialist.CColorPackage.EColor;
import com.bbn.openmap.corba.CSpecialist.CStipplePackage.EStipple;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.DeclutterType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.EGraphic;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GF_update;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;

/**
 * The SGraphic class is the base class for the specialist graphic
 * objects - it holds attributes common to most all objects. Some
 * attributes don't apply to some types of graphics.
 */
public abstract class SGraphic {

    private static long gid = 0;
    Vector graphicUpdateList_ = null;
    protected com.bbn.openmap.corba.CSpecialist.GraphicPackage.EGraphic eg;
    final private static EColor nullColor = new EColor(null, (short) 0, (short) 0, (short) 0);
    final private static EStipple nullStipple = new EStipple(null, (short) 0, (short) 0, new byte[0]);
    final private static EComp nullComp = new EComp(null, "");

    // create a raw EGraphic
    public static EGraphic createEGraphic() {
        EGraphic eg = new EGraphic();
        eg.graph = null;
        eg.obj = nullComp;
        eg.gType = GraphicType.GT_Raster;
        eg.rType = RenderType.RT_LatLon;
        eg.lType = com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType.LT_Unknown;
        eg.dcType = DeclutterType.DC_None;
        eg.lineWidth = 1;
        eg.gID = Long.toString(gid++);
        eg.color = nullColor;
        eg.fillColor = nullColor;
        eg.stipple = nullStipple;
        eg.fillStipple = nullStipple;
        return eg;
    }

    public SGraphic(Graphic g, GraphicType gType, RenderType rType,
            LineType lType, DeclutterType dcType) {
        eg = new EGraphic();
        eg.graph = g;
        eg.obj = nullComp;
        eg.gType = gType;
        eg.rType = rType;
        eg.lType = lType;
        eg.dcType = dcType;
        eg.lineWidth = 1;
        eg.gID = Long.toString(gid++);
        eg.color = nullColor;
        eg.fillColor = nullColor;
        eg.stipple = nullStipple;
        eg.fillStipple = nullStipple;
        graphicUpdateList_ = new Vector();
    }

    /**
     * construct an SGraphic without a reference to the actual CORBA
     * object.
     */
    public SGraphic(GraphicType gType, RenderType rType, LineType lType,
            DeclutterType dcType) {
        this(null, gType, rType, lType, dcType);
    }

    public java.lang.String gID() {
        return eg.gID;
    }

    public void gID(java.lang.String gID) {
        eg.gID = gID;
    }

    public GraphicType gType() {
        return eg.gType;
    }

    public void object(Comp object) {
        eg.obj = (object == null) ? nullComp : object.fill();
    }

    public Comp object() {
        return eg.obj.comp;
    }

    public SComp sobject() {
        return ((eg.obj.comp instanceof SComp) ? (SComp) eg.obj.comp : null);
    }

    public void lType(LineType lType) {
        eg.lType = lType;
    }

    public LineType lType() {
        return eg.lType;
    }

    public void rType(RenderType rType) {
        eg.rType = rType;
    }

    public RenderType rType() {
        return eg.rType;
    }

    public void color(CColor color) {
        eg.color = (color == null) ? nullColor : color.fill();
    }

    public CColor color() {
        return eg.color.color;
    }

    public SColor scolor() {
        return ((eg.color.color instanceof SColor) ? (SColor) eg.color.color
                : null);
    }

    public void fillColor(CColor fillColor) {
        eg.fillColor = (fillColor == null) ? nullColor : fillColor.fill();
    }

    public CColor fillColor() {
        return eg.fillColor.color;
    }

    public SColor sfillColor() {
        return ((eg.fillColor.color instanceof SColor) ? (SColor) eg.fillColor.color
                : null);
    }

    public void lineWidth(short lineWidth) {
        eg.lineWidth = lineWidth;
    }

    public short lineWidth() {
        return eg.lineWidth;
    }

    public void stipple(CStipple stipple) {
        eg.stipple = (stipple == null) ? nullStipple : stipple.fill();
    }

    public CStipple stipple() {
        return eg.stipple.stipple;
    }

    public SStipple sstipple() {
        return ((eg.stipple.stipple instanceof SStipple) ? (SStipple) eg.stipple.stipple
                : null);
    }

    public void fillStipple(CStipple fillStipple) {
        eg.fillStipple = (fillStipple == null) ? nullStipple
                : fillStipple.fill();
    }

    public CStipple fillStipple() {
        return eg.fillStipple.stipple;
    }

    public SStipple sfillStipple() {
        return ((eg.fillStipple.stipple instanceof SStipple) ? (SStipple) eg.fillStipple.stipple
                : null);
    }

    public void dcType(DeclutterType dcType) {
        eg.dcType = dcType;
    }

    public DeclutterType dcType() {
        return eg.dcType;
    }

    public EGraphic gfill() {
        return eg;
    }

    abstract public UGraphic ufill();

    public void changeObject(Comp obj) {
        object(obj);
        GF_update gupdate = new GF_update();
        gupdate.obj(eg.obj);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLType(LineType lType) {
        eg.lType = lType;
        GF_update gupdate = new GF_update();
        gupdate.lType(lType);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeRType(RenderType rType) {
        eg.rType = rType;
        GF_update gupdate = new GF_update();
        gupdate.rType(rType);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeColor(CColor color) {
        color(color);
        GF_update gupdate = new GF_update();
        gupdate.color(eg.color);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeFillColor(SColor fillColor) {
        fillColor(fillColor);
        GF_update gupdate = new GF_update();
        gupdate.fillColor(eg.fillColor);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLineWidth(short lineWidth) {
        eg.lineWidth = lineWidth;
        GF_update gupdate = new GF_update();
        gupdate.lineWidth(lineWidth);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeStipple(SStipple stipple) {
        stipple(stipple);
        GF_update gupdate = new GF_update();
        gupdate.stipple(eg.stipple);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeFillStipple(SStipple fillStipple) {
        fillStipple(fillStipple);
        GF_update gupdate = new GF_update();
        gupdate.fillStipple(eg.fillStipple);
        UpdateGraphic ug = new UpdateGraphic();
        ug.gf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeDcType(DeclutterType dct) {
    //       dcType_ = dct;
    //       GF_update gupdate = new GF_update();
    //       gupdate.dcType(dct);
    //       UpdateGraphic ug = new UpdateGraphic();
    //       ug.gf_update(gupdate);
    //       addGraphicChange(ug);
    }

    /**
     * <b>addGraphicChange </b> is called from within each graphic
     * object type (SCirc, SBitmap, etc. to add it to the change list.
     * Don't call this directly, the object will take care of it. It
     * adds the change to the specific parameter of the object to a
     * total list of changes that the object is tracking.
     */
    protected void addGraphicChange(UpdateGraphic ug) {
        graphicUpdateList_.addElement(ug);
    }

    /**
     * <b>getGraphicUpdates </b> are called from within the graphic
     * objects to get the list of updates that have been made to the
     * object. The list is converted to an <b>UpdateRecord </b>. Call
     * this method to obtain the <b>UpdateRecord </b> needed for the
     * graphic updates as a result of a gesture action.
     */
    public UpdateRecord getGraphicUpdates() {
        UpdateRecord ur = new UpdateRecord();
        ur.gID = eg.gID;
        ur.objectUpdates = new UpdateGraphic[graphicUpdateList_.size()];
        for (int i = 0; i < graphicUpdateList_.size(); i++)
            ur.objectUpdates[i] = (UpdateGraphic) graphicUpdateList_.elementAt(i);
        return ur;
    }
}

