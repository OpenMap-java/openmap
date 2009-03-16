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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SCirc.java,v $
// $RCSfile: SCirc.java,v $
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
import com.bbn.openmap.corba.CSpecialist.CirclePackage.CF_update;
import com.bbn.openmap.corba.CSpecialist.CirclePackage.ECircle;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.DeclutterType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;
import com.bbn.openmap.proj.Planet;

/**
 * A SCirc is a specialist graphic object that represents a circle or
 * an ellipse.
 * <p>
 * 
 * <h4>ASSUMPTIONS</h4>
 * 
 * OpenMap assumes the following about circles and ellipses:
 * 
 * <ul>
 * 
 * <li>LatLon circles and ellipses should enclose an area less than
 * one hemisphere.
 * <p>
 * 
 * <li>To be safe, never enclose extreme polar points within a LatLon
 * circle or ellipse. Certain projections, (the cylindrical family
 * including Mercator), cannot handle drawing these types of polygons.
 * However it's ok to have a pole as a point along the edge of the
 * circle or ellipse. This isn't a restriction if you will be viewing
 * the graphic using a good polar projection. <br>
 * 
 * </ul>
 * 
 * Not following these assumptions may result in unpredictable
 * behavior!
 * <p>
 * 
 * These assumptions are virtually the same as those on the more
 * generic Poly graphic type.
 * <p>
 * 
 * @see SPoly
 *  
 */
public class SCirc extends SGraphic /* used to be _CircleImplBase */{

    /** p1 - xy screen position of the circle, or the offset value. */
    protected XYPoint p1_;
    /** ll1 - the lat/lon position of the circle. */
    protected LLPoint ll1_;
    /** major - the height in decimal degrees. */
    protected float major_;
    /** minor - the width in decimal degreen. */
    protected float minor_;
    /** height - the height in pixels. */
    protected short height_;
    /** width - the width in pixels. */
    protected short width_;

    final public static int KM = 0;
    final public static int MILES = 1;
    final public static int NMILES = 2;

    public SCirc() {
        super(GraphicType.GT_Circle,
              RenderType.RT_Unknown,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        p1_ = new XYPoint((short) 0, (short) 0);
        width_ = (short) 0;
        height_ = (short) 0;
        major_ = 0f;
        minor_ = 0f;
    }

    /** Lat-lon center with lat-lon axis. */
    public SCirc(LLPoint ll1, float major, float minor) {
        super(GraphicType.GT_Circle,
              RenderType.RT_LatLon,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        major_ = major;
        minor_ = minor;
        p1_ = new XYPoint((short) 0, (short) 0);
        height_ = 0;
        width_ = 0;
    }

    /** Lat-lon center with x-y axis. */
    public SCirc(LLPoint ll1, short width, short height) {
        super(GraphicType.GT_Circle,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        major_ = 0f;
        minor_ = 0f;
        p1_ = new XYPoint((short) 0, (short) 0);
        height_ = height;
        width_ = width;
    }

    /** x-y center with x-y axis. */
    public SCirc(short x1, short y1, short width, short height) {
        super(GraphicType.GT_Circle,
              RenderType.RT_XY,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = new LLPoint(0f, 0f);
        major_ = 0;
        minor_ = 0;
        p1_ = new XYPoint(x1, y1);
        width_ = width;
        height_ = height;
    }

    /** Lat-lon location, x-y offset, x-y axis. */
    public SCirc(LLPoint ll1, short offset_x1, short offset_y1, short width,
            short height) {
        super(GraphicType.GT_Circle,
              RenderType.RT_Offset,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        ll1_ = ll1;
        major_ = 0;
        minor_ = 0;
        p1_ = new XYPoint(offset_x1, offset_y1);
        width_ = width;
        height_ = height;
    }

    /**
     * Constructor that let you make center/physical radius setting:
     * <BR>
     * Units can be: KM, MILES, NMILES
     */
    public SCirc(LLPoint ll1, float distance, int units) {
        super(GraphicType.GT_Circle,
              RenderType.RT_LatLon,
              LineType.LT_Unknown,
              DeclutterType.DC_None);
        float upd = units_per_degree(units);
        major_ = distance / upd;
        minor_ = major_;
        ll1_ = ll1;
        width_ = 0;
        height_ = 0;
        p1_ = new XYPoint((short) 0, (short) 0);
    }

    //HACK check what OMCircle does. This should be changed.
    protected float units_per_degree(int UNITS) {
        float kmpdeg = (Planet.wgs84_earthEquatorialRadiusMeters * 2f * (float) Math.PI)
                / (1000f * 360f);
        switch (UNITS) {
        case MILES:
            return kmpdeg * .6213712f; // miles/km
        case NMILES:
            return kmpdeg * .5399568f; // nmiles/km
        default:
            return kmpdeg;
        }
    }

    // The SCirc methods
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

    public void major(float major) {
        major_ = major;
    }

    public float major() {
        return major_;
    }

    public void minor(float minor) {
        minor_ = minor;
    }

    public float minor() {
        return minor_;
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

    public ECircle fill() {
        return new ECircle(eg, p1_, ll1_, major_, minor_, width_, height_);
    }

    public UGraphic ufill() {
        UGraphic ugraphic = new UGraphic();
        ugraphic.ecirc(fill());
        return ugraphic;
    }

    public void changeP1(XYPoint p1) {
        p1_ = p1;
        CF_update gupdate = new CF_update();
        gupdate.p1(p1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.cf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeLl1(LLPoint ll1) {
        ll1_ = ll1;
        CF_update gupdate = new CF_update();
        gupdate.ll1(ll1);
        UpdateGraphic ug = new UpdateGraphic();
        ug.cf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeMajor(float major) {
        major_ = major;
        CF_update gupdate = new CF_update();
        gupdate.major(major);
        UpdateGraphic ug = new UpdateGraphic();
        ug.cf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeMinor(float minor) {
        minor_ = minor;
        CF_update gupdate = new CF_update();
        gupdate.minor(minor);
        UpdateGraphic ug = new UpdateGraphic();
        ug.cf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeWidth(short width) {
        width_ = width;
        CF_update gupdate = new CF_update();
        gupdate.width(width);
        UpdateGraphic ug = new UpdateGraphic();
        ug.cf_update(gupdate);
        addGraphicChange(ug);
    }

    public void changeHeight(short height) {
        height_ = height;
        CF_update gupdate = new CF_update();
        gupdate.height(height);
        UpdateGraphic ug = new UpdateGraphic();
        ug.cf_update(gupdate);
        addGraphicChange(ug);
    }
}