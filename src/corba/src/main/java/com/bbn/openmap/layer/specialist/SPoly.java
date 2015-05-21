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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SPoly.java,v $
// $RCSfile: SPoly.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.UpdateGraphic;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.DeclutterType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;
import com.bbn.openmap.corba.CSpecialist.PolyPackage.CoordMode;
import com.bbn.openmap.corba.CSpecialist.PolyPackage.EPoly;
import com.bbn.openmap.corba.CSpecialist.PolyPackage.PF_update;

/**
 * An SPoly is a specialist graphic object that represents a polygon
 * or polyline (multi-sided object or line).
 * <p>
 * 
 * <h4>ASSUMPTIONS</h4>
 * 
 * OpenMap assumes the following about polys:
 * 
 * <ul>
 * 
 * <li>LatLon polygons should enclose an area less than one
 * hemisphere.
 * <p>
 * 
 * <li>The vertices of the LatLon polygon should be specified in
 * clockwise order. In some projections we deal with an ambiguous
 * polygon by drawing the fill along the righthand side as we traverse
 * the vertices in order, assuming a clockwise orientation. <br>
 * 
 * <li>To be safe, never enclose extreme polar points within a LatLon
 * polygon. Certain projections, (the cylindrical family including
 * Mercator), cannot handle drawing these types of polygons. However
 * it's ok to have a pole as a point along the edge of the poly. This
 * isn't a restriction if you will be viewing the polygon using a good
 * polar projection. <br>
 * 
 * </ul>
 * 
 * Not following these assumptions may result in unpredictable
 * behavior!
 * <p>
 * 
 * Similar assumptions apply to the other vector graphics that we
 * define: circles, ellipses, rects, lines.
 * <p>
 */
public class SPoly extends SGraphic /* used to be _PolyImplBase */{

    /** Lat/lon coordinate of the starting point of the polygon. */
    protected LLPoint ll1_;
    /** The xy pixel points of the polygon. */
    protected XYPoint[] xypoints_;
    /** The lat/lon points of the polygon. */
    protected LLPoint[] llpoints_;
    /**
     * Specifies how the xy points relate to each other on the screen. -
     * CModeOrigin (the xy points relate to the first point), and
     * CModePrevious (the xy point relates to the previous point).
     */
    protected CoordMode cMode_;

    // Need several constructors
    public SPoly() {
        super(GraphicType.GT_Poly,
              RenderType.RT_Unknown,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        xypoints_ = new XYPoint[0];
        llpoints_ = new LLPoint[0];
        cMode_ = CoordMode.CModeOrigin;
    }

    public SPoly(LLPoint[] llpoints, LineType lType) {
        super(GraphicType.GT_Poly,
              RenderType.RT_LatLon,
              lType,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        llpoints_ = llpoints;
        xypoints_ = new XYPoint[0];
        cMode_ = CoordMode.CModeOrigin;
    }

    public SPoly(XYPoint[] xypoints, CoordMode cMode) {
        super(GraphicType.GT_Poly,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        llpoints_ = new LLPoint[0];
        xypoints_ = xypoints;
        cMode_ = cMode;
    }

    public SPoly(LLPoint ll1, XYPoint[] xypoints, CoordMode cMode) {
        super(GraphicType.GT_Poly,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        llpoints_ = new LLPoint[0];
        xypoints_ = xypoints;
        cMode_ = cMode;
    }

    // The SPoly methods
    public void ll1(LLPoint ll1) {
        ll1_ = ll1;
    }

    public LLPoint ll1() {
        return ll1_;
    }

    public void cMode(CoordMode cMode) {
        cMode_ = cMode;
    }

    public CoordMode cMode() {
        return cMode_;
    }

    public void xypoints(XYPoint[] xypoints) {
        xypoints_ = xypoints;
    }

    public XYPoint[] xypoints() {
        return xypoints_;
    }

    public void llpoints(LLPoint[] llpoints) {
        llpoints_ = llpoints;
    }

    public LLPoint[] llpoints() {
        return llpoints_;
    }

    public EPoly fill() {
        return new EPoly(eg, ll1_, cMode_, xypoints_, llpoints_);
    }

    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.epoly(fill());
        return ugraphic;
    }

    //  Update methods as a result of gesture impulses...
    public void changeLl1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
        PF_update gupdate = new PF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.pf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeCMode(CoordMode cmode) {
        cMode_ = cmode;
        PF_update gupdate = new PF_update();
        gupdate.cMode(cmode);
        UpdateGraphic ug = new UpdateGraphic();
        ug.pf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeXypoints(XYPoint[] xypoints) {
        xypoints_ = xypoints;
        PF_update gupdate = new PF_update();
        gupdate.xypoints(xypoints);
        UpdateGraphic ug = new UpdateGraphic();
        ug.pf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLlpoints(LLPoint[] llpoints) {
        llpoints_ = llpoints;
        PF_update gupdate = new PF_update();
        gupdate.llpoints(llpoints);
        UpdateGraphic ug = new UpdateGraphic();
        ug.pf_update(gupdate);
        addGraphicChange(ug);
    }
}

