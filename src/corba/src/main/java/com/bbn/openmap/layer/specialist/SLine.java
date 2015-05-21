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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SLine.java,v $
// $RCSfile: SLine.java,v $
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
import com.bbn.openmap.corba.CSpecialist.LinePackage.ELine;
import com.bbn.openmap.corba.CSpecialist.LinePackage.LF_update;

/**
 * A SLine is a specialist graphic object that represents a line.
 * 
 * <h4>ASSUMPTIONS</h4>
 * 
 * OpenMap assumes the following about lines:
 * 
 * <ul>
 * 
 * <li>A LatLon line must be less than 180 degrees of arc, (one
 * hemisphere). We draw lines the short way around the world.
 * <p>
 * 
 * </ul>
 * 
 * Not following these assumptions may result in unpredictable
 * behavior!
 * <p>
 *  
 */

public class SLine extends SGraphic /* used to be _LineImplBase */{

    /** p1 - xy screen point of the end point of the line. */
    protected XYPoint p1_;
    /** p2 - xy screen point of the end point of the line. */
    protected XYPoint p2_;
    /** ll1 - lat/lon coordinate of the starting point of the line. */
    protected LLPoint ll1_;
    /** ll2 - lat/lon coordinate of the end point of the line. */
    protected LLPoint ll2_;

    // Need several constructors
    public SLine() {
        super(GraphicType.GT_Line,
              RenderType.RT_Unknown,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        ll2_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint((short) 0, (short) 0);
        p2_ = new XYPoint((short) 0, (short) 0);
    }

    public SLine(LLPoint ll1, LLPoint ll2, LineType lType) {
        super(GraphicType.GT_Line,
              RenderType.RT_LatLon,
              lType,
              DeclutterType.DC_None);
        ll1_ = ll1;
        ll2_ = ll2;
        p1_ = new XYPoint((short) 0, (short) 0);
        p2_ = new XYPoint((short) 0, (short) 0);
    }

    public SLine(short x1, short y1, short x2, short y2) {
        super(GraphicType.GT_Line,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        ll2_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint(x1, y1);
        p2_ = new XYPoint(x2, y2);
    }

    public SLine(LLPoint ll1, short x1, short y1, short x2, short y2) {
        super(GraphicType.GT_Line,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        ll2_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint(x1, y1);
        p2_ = new XYPoint(x2, y2);
    }

    // The SLine methods
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

    public ELine fill() {
        return new ELine(eg, p1_, p2_, ll1_, ll2_);
    }

    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.eline(fill());
        return ugraphic;
    }

    //  Update methods as a result of gesture impulses...
    public void changeLl1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
        LF_update gupdate = new LF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.lf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLl2(com.bbn.openmap.corba.CSpecialist.LLPoint ll2) {
        ll2_ = ll2;
        LF_update gupdate = new LF_update();
        gupdate.ll2(ll2);
        UpdateGraphic ug = new UpdateGraphic();
        ug.lf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeP1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
        LF_update gupdate = new LF_update();
        gupdate.p1(p1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.lf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeP2(com.bbn.openmap.corba.CSpecialist.XYPoint p2) {
        p2_ = p2;
        LF_update gupdate = new LF_update();
        gupdate.p2(p2);
        UpdateGraphic ug = new UpdateGraphic();
        ug.lf_update(gupdate);
        addGraphicChange(ug);
    }
}