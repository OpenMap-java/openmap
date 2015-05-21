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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SUnitSymbol.java,v $
// $RCSfile: SUnitSymbol.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:37 $
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
import com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.EUnitSymbol;
import com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.USF_update;

/** FM101 Unit Symbols are part of a Goverment standard for icons. */

public class SUnitSymbol extends SGraphic /*used to be _UnitSymbolImplBase*/ {
    protected XYPoint p1_;
    protected LLPoint ll1_;
    protected String group_ = new String();
    protected String symbol_ = new String();
    protected String echelon_ = new String();
    protected String left1_ = new String();
    protected String left2_ = new String();
    protected String left3_ = new String();
    protected String left4_ = new String();
    protected String right1_ = new String();
    protected String right2_ = new String();
    protected String right3_ = new String();
    protected String right4_ = new String();
    protected String top1_ = new String();
    protected String bottom1_ = new String();
    protected short nom_size_ = 30;     // nominal size is in pixels
    protected short min_size_ = 5;      // minimal size is in pixels
    protected short max_size_ = 1000;   // maximum size is in pixels
    protected int scale_ = 3200;        // scale at which size is nom_size
    protected boolean is_hq_ = false;   // Headquarters mark display
    protected float rotate_ = (float)0.0;               // rotate unit 0.0 -> 180.0 degrees

    public SUnitSymbol() {
        super(GraphicType.GT_UnitSymbol, RenderType.RT_Unknown, 
              LineType.LT_Unknown, DeclutterType.DC_None);
        p1_ = new XYPoint((short)0, (short)0);
        ll1_ = new LLPoint(0f, 0f);
    }


    public SUnitSymbol(LLPoint ll1,
                       String group,
                       String symbol,
                       String echelon,
                       String bottom) {
        super(GraphicType.GT_UnitSymbol, RenderType.RT_LatLon, 
              LineType.LT_Unknown, DeclutterType.DC_None);
        p1_ = new XYPoint((short)0, (short)0);
        ll1_ = ll1;
        group_ = group;
        symbol_ = symbol;
        echelon_ = echelon;
        bottom1_ = bottom;
    }

    public SUnitSymbol(short x1, short y1,
                       String group,
                       String symbol,
                       String echelon,
                       String bottom1) {
        super(GraphicType.GT_UnitSymbol, RenderType.RT_XY, 
              LineType.LT_Unknown, DeclutterType.DC_None);
        p1_ = new XYPoint(x1, y1);
        ll1_ = new LLPoint(0f, 0f);
        group_ = group;
        symbol_ = symbol;
        echelon_ = echelon;
        bottom1_ = bottom1;
    }

    public SUnitSymbol(LLPoint  ll1,            
                       String   group,          
                       String   symbol,         
                       String   echelon,        
                       String   left1,          
                       String   left2,          
                       String   left3,          
                       String   left4,          
                       String   right1,         
                       String   right2,         
                       String   right3,         
                       String   right4,         
                       String   top1,           
                       String   bottom1,        
                       short    nom_size,   
                       short    min_size,   
                       short    max_size,   
                       int      scale,      
                       boolean  is_hq,          
                       float    rotate) {
        super(GraphicType.GT_UnitSymbol, RenderType.RT_LatLon, 
              LineType.LT_Unknown, DeclutterType.DC_None);
        p1_ = new XYPoint((short)0, (short)0);
        ll1_     = ll1;
        group_   = group;
        symbol_  = symbol;
        echelon_ = echelon;
        left1_   = left1;
        left2_   = left2;
        left3_   = left3;
        left4_   = left4;
        right1_  = right1;
        right2_  = right2;
        right3_  = right3;
        right4_  = right4;
        top1_    = top1;
        bottom1_ = bottom1;
        nom_size_= nom_size;
        min_size_= min_size;
        max_size_= max_size;
        scale_   = scale;
        is_hq_   = is_hq;
        rotate_  = rotate;
    }



    public SUnitSymbol(short x1, short y1,
                       String   group,          
                       String   symbol,         
                       String   echelon,        
                       String   left1,          
                       String   left2,          
                       String   left3,          
                       String   left4,          
                       String   right1,         
                       String   right2,         
                       String   right3,         
                       String   right4,         
                       String   top1,           
                       String   bottom1,        
                       short    nom_size, 
                       short    min_size, 
                       short    max_size, 
                       int      scale,     
                       boolean  is_hq,          
                       float    rotate) {
        super(GraphicType.GT_UnitSymbol, RenderType.RT_XY, 
              LineType.LT_Unknown, DeclutterType.DC_None);
        p1_ = new XYPoint(x1, y1);
        ll1_ = new LLPoint(0f, 0f);
        group_   = group;
        symbol_  = symbol;
        echelon_ = echelon;
        left1_   = left1;
        left2_   = left2;
        left3_   = left3;
        left4_   = left4;
        right1_  = right1;
        right2_  = right2;
        right3_  = right3;
        right4_  = right4;
        top1_    = top1;
        bottom1_ = bottom1;
        nom_size_= nom_size;
        min_size_= min_size;
        max_size_= max_size;
        scale_   = scale;
        is_hq_   = is_hq;
        rotate_  = rotate;
    }
  

    // Object methods
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

    public void group(String group) { group_ = group; }
    public String group() { return group_; }

    public void symbol(String symbol) { symbol_ = symbol; }
    public String symbol() { return symbol_;}

    public void echelon(String echelon) { echelon_ = echelon;}
    public String echelon() { return echelon_;}

    public void left1(String left1) {left1_ = left1; }
    public String left1() { return left1_ ; }

    public void left2(String left2) {left2_ = left2; }
    public String left2() { return left2_ ; }

    public void left3(String left3) {left3_ = left3; }
    public String left3() { return left3_ ; }

    public void left4(String left4) {left4_ = left4; }
    public String left4() { return left4_ ; }

    public void right1(String right1) {right1_ = right1; }
    public String right1() { return right1_ ; }

    public void right2(String right2) {right2_ = right2; }
    public String right2() { return right2_ ; }

    public void right3(String right3) {right3_ = right3; }
    public String right3() { return right3_ ; }

    public void right4(String right4) {right4_ = right4; }
    public String right4() { return right4_ ; }

    public String top1() { return top1_;}
    public void top1(String top1) { top1_ = top1; }

    public String bottom1() { return bottom1_; }
    public void bottom1(String bottom1) { bottom1_ = bottom1; }

    public short nom_size() { return nom_size_; }
    public void nom_size(short nom_size ) { nom_size_ = nom_size;}

    public short min_size() { return min_size_; }
    public void min_size(short min_size ) { min_size_ = min_size;}

    public short max_size() { return max_size_; }
    public void max_size(short max_size ) { max_size_ = max_size;}

    public int scale() { return scale_;}
    public void scale(int scale) { scale_ = scale; }

    public boolean is_hq() { return is_hq_; }
    public void is_hq(boolean is_hq) { is_hq_ = is_hq; }

    public float rotate() { return rotate_; }
    public void rotate(float rotate) { rotate_ = rotate; }

    public EUnitSymbol fill() {

        return new EUnitSymbol(eg,
                               p1_, ll1_,
                               group_,symbol_, echelon_,    
                               left1_, left2_, left3_, left4_,      
                               right1_, right2_, right3_, right4_,     
                               top1_, bottom1_,    
                               nom_size_, min_size_, max_size_,   
                               scale_,      
                               is_hq_,      
                               rotate_);
    }


    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.eunit(fill());
        return ugraphic;
    }

    //  Update methods as a result of gesture impulses...
    public void changeP1(com.bbn.openmap.corba.CSpecialist.XYPoint p1) {
        p1_ = p1;
        USF_update gupdate = new USF_update();
        gupdate.p1(p1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeLl1(com.bbn.openmap.corba.CSpecialist.LLPoint ll1) {
        ll1_ = ll1;
        USF_update gupdate = new USF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeGroup(String group) {
        group_ = group;
        USF_update gupdate = new USF_update();
        gupdate.group(group);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeSymbol(String symbol) {
        symbol_ = symbol;
        USF_update gupdate = new USF_update();
        gupdate.symbol(symbol);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeEchelon_(String echelon) {
        echelon_ = echelon;
        USF_update gupdate = new USF_update();
        gupdate.echelon(echelon);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeLeft1(String left1) {
        left1_ = left1;
        USF_update gupdate = new USF_update();
        gupdate.left1(left1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeLeft2(String left2) {
        left2_ = left2;
        USF_update gupdate = new USF_update();
        gupdate.left2(left2);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeLeft3(String left3) {
        left3_ = left3;
        USF_update gupdate = new USF_update();
        gupdate.left3(left3);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeLeft4(String left4) {
        left4_ = left4;
        USF_update gupdate = new USF_update();
        gupdate.left4(left4);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeRight1(String right1) {
        right1_ = right1;
        USF_update gupdate = new USF_update();
        gupdate.right1(right1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeRight2(String right2) {
        right2_ = right2;
        USF_update gupdate = new USF_update();
        gupdate.right2(right2);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeRight3(String right3) {
        right3_ = right3;
        USF_update gupdate = new USF_update();
        gupdate.right3(right3);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeRight4(String right4) {
        right4_ = right4;
        USF_update gupdate = new USF_update();
        gupdate.right4(right4);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeTop1(String top1) {
        top1_ = top1;
        USF_update gupdate = new USF_update();
        gupdate.top1(top1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeBottom1(String bottom1) {
        bottom1_ = bottom1;
        USF_update gupdate = new USF_update();
        gupdate.bottom1(bottom1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeNom_size(short nom_size) {
        nom_size_ = nom_size;
        USF_update gupdate = new USF_update();
        gupdate.nom_size(nom_size);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeMin_size(short min_size) {
        min_size_ = min_size;
        USF_update gupdate = new USF_update();
        gupdate.min_size(min_size);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeMax_size(short max_size) {
        max_size_ = max_size;
        USF_update gupdate = new USF_update();
        gupdate.max_size(max_size);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeScale(int scale) {
        scale_ = scale;
        USF_update gupdate = new USF_update();
        gupdate.scale(scale);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeIs_hq(boolean is_hq) {
        is_hq_ = is_hq;
        USF_update gupdate = new USF_update();
        gupdate.is_hq(is_hq);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
    public void changeRotate(float rotate) {
        rotate_ = rotate;
        USF_update gupdate = new USF_update();
        gupdate.rotate(rotate);
        UpdateGraphic ug = new UpdateGraphic();
        ug.usf_update(gupdate);
        addGraphicChange(ug);
    }
}
