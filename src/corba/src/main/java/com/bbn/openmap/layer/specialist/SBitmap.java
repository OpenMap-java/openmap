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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SBitmap.java,v $
// $RCSfile: SBitmap.java,v $
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
import com.bbn.openmap.corba.CSpecialist.BitmapPackage.BF_update;
import com.bbn.openmap.corba.CSpecialist.BitmapPackage.EBitmap;
import com.bbn.openmap.corba.CSpecialist.CStipplePackage.EStipple;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.DeclutterType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;

/**
 * SBitmap is a specialist graphic object that creates and manipulates
 * bitmap data. It can be used for bitmap icons, or for map subframes
 * that tile together to cover the whole display.
 */
public class SBitmap extends SGraphic /* used to be _BitmapImplBase */{
    /** xy screen location of the upper left corner of the bitmap. */
    protected XYPoint p1_;
    /** lat/lon location of the upper left corner of the bitmap. */
    protected LLPoint ll1_;
    /** width of the bitmap. */
    protected short width_;
    /** height of the bitmap. */
    protected short height_;
    /** the location (left-right) of the hot spot of the bitmap. */
    protected short x_hot_;
    /** the location (up-down) of the hot spot of the bitmap. */
    protected short y_hot_;
    /** the bitmap data. */
    protected byte[] bits_;
    protected SStipple bmref_;

    /** Create empty, add parameters later. */
    public SBitmap() {
        super(GraphicType.GT_Bitmap,
              RenderType.RT_Unknown,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint((short) 0, (short) 0);
        ll1_ = new LLPoint(0f, 0f);
        width_ = 0;
        height_ = 0;
        x_hot_ = 0;
        y_hot_ = 0;
        bits_ = new byte[0];
        bmref_ = null;
    }

    /** Lat/lon placement. */
    public SBitmap(LLPoint ll1, short width, short height, byte[] bits,
            short x_hot, short y_hot) {
        super(GraphicType.GT_Bitmap,
              RenderType.RT_LatLon,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint((short) 0, (short) 0);
        ll1_ = ll1;
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        bits_ = bits;
        bmref_ = null;
    }

    /** X/Y screen placement. */
    public SBitmap(short x1, short y1, short width, short height, byte[] bits,
            short x_hot, short y_hot) {
        super(GraphicType.GT_Bitmap,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint(x1, y1);
        ll1_ = new LLPoint(0f, 0f);
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        bits_ = bits;
        bmref_ = null;
    }

    /** Lat/lon with a X/Y offset placement. */
    public SBitmap(LLPoint ll1, short offset_x1, short offset_y1, short width,
            short height, byte[] bits, short x_hot, short y_hot) {
        super(GraphicType.GT_Bitmap,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        p1_ = new XYPoint(offset_x1, offset_y1);
        ll1_ = ll1;
        width_ = width;
        height_ = height;
        x_hot_ = x_hot;
        y_hot_ = y_hot;
        bits_ = bits;
        bmref_ = null;
    }

    public void p1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
    }

    public com.bbn.openmap.corba.CSpecialist.XYPoint p1() {
        return p1_;
    }

    public void ll1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
    }

    public com.bbn.openmap.corba.CSpecialist.LLPoint ll1() {
        return ll1_;
    }

    public void width(short width) {
        width_ = width;
    }

    public short width() {
        return width_;
    }

    public void height(short height) {
        height_ = height;
    }

    public short height() {
        return height_;
    }

    public void x_hot(short x_hot) {
        x_hot_ = x_hot;
    }

    public short x_hot() {
        return x_hot_;
    }

    public void y_hot(short y_hot) {
        y_hot_ = y_hot;
    }

    public short y_hot() {
        return y_hot_;
    }

    public void bits(byte[] bits) {
        bits_ = bits;
    }

    public byte[] bits() {
        return bits_;
    }

    public void bmref(SStipple bmref) {
        bmref_ = bmref;
    }

    public SStipple bmref() {
        return bmref_;
    }

    public EBitmap fill() {
        String BMref;
        if (bmref_ == null)
            BMref = new EStipple(null, (short) 0, (short) 0, new byte[0]).toString();
        else
            BMref = new EStipple(null, bmref_.width(), bmref_.height(), bmref_.data()).toString();

        return new EBitmap(eg, p1_, ll1_, width_, height_, x_hot_, y_hot_, bits_, BMref);
    }

    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.ebit(fill());
        return ugraphic;
    }

    //  Update methods as a result of gesture impulses...
    public void changeP1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
        BF_update gupdate = new BF_update();
        gupdate.p1(p1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.bf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLl1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
        BF_update gupdate = new BF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.bf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeWidth(short width) {
        width_ = width;
        BF_update gupdate = new BF_update();
        gupdate.width(width);
        UpdateGraphic ug = new UpdateGraphic();
        ug.bf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeHeight(short height) {
        height_ = height;
        BF_update gupdate = new BF_update();
        gupdate.height(height);
        UpdateGraphic ug = new UpdateGraphic();
        ug.bf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeX_hot(short x_hot) {
        x_hot_ = x_hot;
        BF_update gupdate = new BF_update();
        gupdate.x_hot(x_hot);
        UpdateGraphic ug = new UpdateGraphic();
        ug.bf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeY_hot(short y_hot) {
        y_hot_ = y_hot;
        BF_update gupdate = new BF_update();
        gupdate.y_hot(y_hot);
        UpdateGraphic ug = new UpdateGraphic();
        ug.bf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeBits(byte[] bits) {
        bits_ = bits;
        BF_update gupdate = new BF_update();
        gupdate.bits(bits);
        UpdateGraphic ug = new UpdateGraphic();
        ug.bf_update(gupdate);
        addGraphicChange(ug);
    }
    //     public void changeBmref(SStipple bmref) {
    //      bmref_ = bmref;
    //      BF_update gupdate = new BF_update();
    //      gupdate.bmref(bmref);
    //      UpdateGraphic ug = new UpdateGraphic();
    //      ug.bf_update(gupdate);
    //      addGraphicChange(ug);
    //     }

}

