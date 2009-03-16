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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SText.java,v $
// $RCSfile: SText.java,v $
// $Revision: 1.4 $
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
import com.bbn.openmap.corba.CSpecialist.TextPackage.EText;
import com.bbn.openmap.corba.CSpecialist.TextPackage.TF_update;

/**
 * A SText graphic object is used to put text on the client's screen.
 */
public class SText extends SGraphic /* used to be _TextImplBase */{

    /** XY screen location, or offset value. */
    protected XYPoint p1_;
    /** Lat/lon location. */
    protected LLPoint ll1_;
    /** The String value of the font - see below, resolveFont(). */
    protected String font_;
    /** The Java Font. */
    protected java.awt.Font realFont_;
    /** The text to be displayed. */
    protected String data_;
    /** Right, left, or center. */
    protected short justify_;

    /** Generic, using Helvetica font. */
    public SText() {
        super(GraphicType.GT_Text,
              RenderType.RT_Unknown,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint((short) 0, (short) 0);
        data_ = "";
        realFont_ = new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 10);
        justify_ = 0;
    }

    /** Lat/lon placement. */
    public SText(LLPoint ll1, String data, java.awt.Font font, short justify) {
        super(GraphicType.GT_Text,
              RenderType.RT_LatLon,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        p1_ = new XYPoint((short) 0, (short) 0);
        data_ = data;
        realFont_ = font;
        justify_ = justify;
    }

    /** XY placement. */
    public SText(short x1, short y1, String data, java.awt.Font font,
            short justify) {
        super(GraphicType.GT_Text,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint(x1, y1);
        data_ = data;
        realFont_ = font;
        justify_ = justify;
    }

    /** Lat/lon placement with XY offset. */
    public SText(LLPoint ll1, short x1, short y1, String data,
            java.awt.Font font, short justify) {
        super(GraphicType.GT_Text,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        p1_ = new XYPoint(x1, y1);
        data_ = data;
        realFont_ = font;
        justify_ = justify;
    }

    // The SText methods
    public void p1(XYPoint p1) {
        p1_ = p1;
    }

    public XYPoint p1() {
        return p1_;
    }

    public void ll1(LLPoint ll1) {
        ll1_ = ll1;
    }

    public LLPoint ll1() {
        return ll1_;
    }

    public void data(String data) {
        data_ = data;
    }

    public String data() {
        return data_;
    }

    public void font(String font) {
        font_ = font;
    }

    public String font() {
        return resolveFont(realFont_);
    }

    public void font(java.awt.Font font) {
        realFont_ = font;
    }

    public void justify(short justify) {
        justify_ = justify;
    }

    public short justify() {
        return justify_;
    }

    public EText fill() {
        return new EText(eg, p1_, ll1_, data_, resolveFont(realFont_), justify_);
    }

    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.etext(fill());
        return ugraphic;
    }

    /**
     * In the idl, fonts are represented by a string. Traditionally,
     * with MATT, the font was a X representation of a font. That's
     * what is being done here - we're taking the Font structure, and
     * then going to XFont type text structure. Dashes need to be
     * included, line feeds are not. They are here only for
     * readability. The JText object brings this back to a java Font.
     * Like wise, doing the transformation this way makes all
     * specialists compatible with all other clients.
     */
    public String resolveFont(java.awt.Font font) {
        //-foundry(who made it)
        String ret = "-*";
        //-font family(name)
        ret = ret.concat("-" + font.getName());
        //-weight(bold, medium)
        if (font.isBold())
            ret = ret.concat("-bold");
        else
            ret = ret.concat("-normal");
        //-slant(o,i)
        if (font.isItalic())
            ret = ret.concat("-i");
        else
            ret = ret.concat("-o");
        //-set width(normal, condensed, narrow, double width)
        ret = ret.concat("-normal");
        //--pixels(height)
        ret = ret.concat("--" + font.getSize());
        //-points(in tenths of a point, related to screen)
        ret = ret.concat("-*");
        //-horizontal resolution in dpi
        ret = ret.concat("-*");
        //-vertical resolution in dpi
        ret = ret.concat("-*");
        //-spacing(m-monospace or p-proportional)
        ret = ret.concat("-*");
        //-average width(of each letter, in tenths of a pixel)
        ret = ret.concat("-*");
        //-character set(like an ISO designation.
        ret = ret.concat("-*");
        // System.out.println("SText.fontString: " + ret);
        return ret;
    }

    //  Gesture responses
    public void changeP1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
        TF_update gupdate = new TF_update();
        gupdate.p1(p1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.tf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLl1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
        TF_update gupdate = new TF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.tf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeData(String data) {
        data_ = data;
        TF_update gupdate = new TF_update();
        gupdate.data(data);
        UpdateGraphic ug = new UpdateGraphic();
        ug.tf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeFont(String font) {
        font_ = font;
        TF_update gupdate = new TF_update();
        gupdate.font(font);
        UpdateGraphic ug = new UpdateGraphic();
        ug.tf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeFont(java.awt.Font font) {
        realFont_ = font;
        TF_update gupdate = new TF_update();
        gupdate.font(resolveFont(realFont_));
        UpdateGraphic ug = new UpdateGraphic();
        ug.tf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeJustify(short justify) {
        justify_ = justify;
        TF_update gupdate = new TF_update();
        gupdate.justify(justify);
        UpdateGraphic ug = new UpdateGraphic();
        ug.tf_update(gupdate);
        addGraphicChange(ug);
    }
}

