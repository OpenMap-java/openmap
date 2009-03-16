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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SRect.java,v $
// $RCSfile: SRect.java,v $
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
import com.bbn.openmap.corba.CSpecialist.RectanglePackage.ERectangle;
import com.bbn.openmap.corba.CSpecialist.RectanglePackage.RF_update;

/**
 * SRect is a specialist rectangle graphic object
 * 
 * <h4>ASSUMPTIONS</h4>
 * 
 * OpenMap assumes the following about rects:
 * 
 * <ul>
 * 
 * <li>The edges of a LatLon rect must be less than 180 degrees of
 * arc, (one hemisphere). We draw lines the short way around the
 * world.
 * <p>
 * 
 * <li>The vertices of the LatLon rect should be specified in
 * clockwise order, we suggest `llNW' then `llSE' (Northwest LatLon,
 * Southeast LatLon box). <br>
 * 
 * </ul>
 * 
 * Not following these assumptions may result in unpredictable
 * behavior!
 * <p>
 * 
 * These assumptions are virtually the same as those on the more
 * generic Poly and Line graphic types.
 * <p>
 * 
 * @see SPoly
 * @see SLine
 */

public class SRect extends SGraphic /* used to be _RectangleImplBase */{

    /** Upper left corner xy screen point, or upper left corner offset. */
    protected XYPoint p1_;
    /**
     * Lower right corner xy screen point, or lower right corner
     * offset.
     */
    protected XYPoint p2_;
    /** Upper left corner coordinate, or coordinate for offset */
    protected LLPoint ll1_;
    /** Lower right corner coordinate */
    protected LLPoint ll2_;

    // Need several constructors
    public SRect() {
        super(GraphicType.GT_Rectangle,
              RenderType.RT_Unknown,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint((short) 0, (short) 0);
        ll2_ = new LLPoint(0f, 0f);
        p2_ = new XYPoint((short) 0, (short) 0);
    }

    public SRect(LLPoint ll1, LLPoint ll2, LineType lType) {
        super(GraphicType.GT_Rectangle,
              RenderType.RT_LatLon,
              lType,
              DeclutterType.DC_None);
        ll1_ = ll1;
        ll2_ = ll2;
        p1_ = new XYPoint((short) 0, (short) 0);
        p2_ = new XYPoint((short) 0, (short) 0);
    }

    public SRect(short x1, short y1, short x2, short y2) {
        super(GraphicType.GT_Rectangle,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        ll2_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint(x1, y1);
        p2_ = new XYPoint(x2, y2);
    }

    public SRect(LLPoint ll1, short x1, short y1, short x2, short y2) {
        super(GraphicType.GT_Rectangle,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        ll2_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint(x1, y1);
        p2_ = new XYPoint(x2, y2);
    }

    // The SRect methods
    public void p1(XYPoint p1) {
        p1_ = p1;
    }

    public XYPoint p1() {
        return p1_;
    }

    public void p2(XYPoint p2) {
        p2_ = p2;
    }

    public XYPoint p2() {
        return p2_;
    }

    public void ll1(LLPoint ll1) {
        ll1_ = ll1;
    }

    public LLPoint ll1() {
        return ll1_;
    }

    public void ll2(LLPoint ll2) {
        ll2_ = ll2;
    }

    public LLPoint ll2() {
        return ll2_;
    }

    public ERectangle fill() {
        return new ERectangle(eg, p1_, p2_, ll1_, ll2_);
    }

    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.erect(fill());
        return ugraphic;
    }

    //  Update methods as a result of gesture impulses...
    public void changeLl1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
        RF_update gupdate = new RF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLl2(com.bbn.openmap.corba.CSpecialist.LLPoint ll2) {
        ll2_ = ll2;
        RF_update gupdate = new RF_update();
        gupdate.ll2(ll2);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeP1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
        RF_update gupdate = new RF_update();
        gupdate.p1(p1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeP2(com.bbn.openmap.corba.CSpecialist.XYPoint p2) {
        p2_ = p2;
        RF_update gupdate = new RF_update();
        gupdate.p2(p2);
        UpdateGraphic ug = new UpdateGraphic();
        ug.rf_update(gupdate);
        addGraphicChange(ug);
    }
}