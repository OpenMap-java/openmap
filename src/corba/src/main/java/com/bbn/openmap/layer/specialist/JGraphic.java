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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JGraphic.java,v $
// $RCSfile: JGraphic.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.Serializable;

import com.bbn.openmap.corba.CSpecialist.CColorPackage.EColor;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.DeclutterType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.EGraphic;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.LineType;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.RenderType;
import com.bbn.openmap.omGraphics.OMGraphic;

/** class JGraphic */
public class JGraphic implements Serializable {

    public final static int GRAPHICTYPE_UNITSYMBOL = 8;
    public final static int GRAPHICTYPE_2525SYMBOL = 9;
    public final static int GRAPHICTYPE_FORCEARROW = 10;
    public final static int GRAPHICTYPE_NEW = 11;
    public final static int GRAPHICTYPE_REORDER = 12;

    private JGraphic() {}

    public static void fillOMGraphicParams(OMGraphic newbie, EGraphic egraphic) {

        //      newbie.setGraphicID(egraphic.gID);
        newbie.setLineType(getOMGraphicLineType(egraphic.lType.value()));
        newbie.setRenderType(getOMGraphicRenderType(egraphic.rType.value()));
        //      newbie.setGraphicType(getOMGraphicType(egraphic.gType.value()));
        newbie.setStroke(new BasicStroke(egraphic.lineWidth));
        newbie.setDeclutterType(egraphic.dcType.value());
        newbie.setLinePaint(getColor(egraphic.color));
        newbie.setFillPaint(getColor(egraphic.fillColor));

        if (newbie instanceof JObjectHolder)
            ((JObjectHolder) newbie).setObject(egraphic.obj);
    }

    //////////////////////////////////////////////////////////////////////////

    /** getRenderType() */
    public static int getOMGraphicRenderType(int cRenderType) {
        int rType;
        switch (cRenderType) {
        case RenderType._RT_LatLon:
            rType = OMGraphic.RENDERTYPE_LATLON;
            break;
        case RenderType._RT_Offset:
            rType = OMGraphic.RENDERTYPE_OFFSET;
            break;
        default: // default to XY
            rType = OMGraphic.RENDERTYPE_XY;
            break;
        }
        return rType;
    }

    /** getLineType() */
    public static int getOMGraphicLineType(int cLineType) {
        int lType;

        switch (cLineType) {
        case LineType._LT_Rhumb:
            lType = OMGraphic.LINETYPE_RHUMB;
            break;
        case LineType._LT_GreatCircle:
            lType = OMGraphic.LINETYPE_GREATCIRCLE;
            break;
        default: // default to STRAIGHT
            lType = OMGraphic.LINETYPE_STRAIGHT;
            break;
        }
        return lType;
    }

    /** getDeclutterType() */
    public static int getOMGraphicDeclutterType(int cDeclutterType) {
        int dcType;

        switch (cDeclutterType) {
        case DeclutterType._DC_Space:
            dcType = OMGraphic.DECLUTTERTYPE_SPACE;
            break;
        case DeclutterType._DC_Move:
            dcType = OMGraphic.DECLUTTERTYPE_MOVE;
            break;
        case DeclutterType._DC_Line:
            dcType = OMGraphic.DECLUTTERTYPE_LINE;
            break;
        default:
            dcType = OMGraphic.DECLUTTERTYPE_NONE;
            break;
        }
        return dcType;
    }

    /** getGraphicType() */
    public static int getOMGraphicType(int cGraphicType) {
        int gtype;
        switch (cGraphicType) {
        case GraphicType._GT_Bitmap:
            gtype = OMGraphic.GRAPHICTYPE_BITMAP;
            break;
        case GraphicType._GT_Text:
            gtype = OMGraphic.GRAPHICTYPE_TEXT;
            break;
        case GraphicType._GT_Poly:
            gtype = OMGraphic.GRAPHICTYPE_POLY;
            break;
        case GraphicType._GT_Line:
            gtype = OMGraphic.GRAPHICTYPE_LINE;
            break;
        case GraphicType._GT_UnitSymbol:
            gtype = GRAPHICTYPE_UNITSYMBOL;
            break;
        case GraphicType._GT_2525Symbol:
            gtype = GRAPHICTYPE_2525SYMBOL;
            break;
        case GraphicType._GT_Rectangle:
            gtype = OMGraphic.GRAPHICTYPE_RECTANGLE;
            break;
        case GraphicType._GT_Circle:
            gtype = OMGraphic.GRAPHICTYPE_CIRCLE;
            break;
        case GraphicType._GT_Raster:
            gtype = OMGraphic.GRAPHICTYPE_RASTER;
            break;
        case GraphicType._GT_ForceArrow:
            gtype = GRAPHICTYPE_FORCEARROW;
            break;
        case GraphicType._GT_NewGraphic:
            gtype = GRAPHICTYPE_NEW;
            break;
        case GraphicType._GT_ReorderGraphic:
            gtype = GRAPHICTYPE_REORDER;
            break;
        default:
            gtype = OMGraphic.GRAPHICTYPE_GRAPHIC;
            break;
        }
        return gtype;
    }

    /** Get a Color from an EColor. */
    public static Color getColor(EColor color) {
        Color ret;
        if (color != null) {
            if ((color.red == 0) && (color.green == 0) && (color.blue == 0)
                    && (color.color == null)) {
                ret = null;
            } else {
                ret = new Color(((color.red >> 8) & 0x00FF), ((color.green >> 8) & 0x00FF), ((color.blue >> 8) & 0x00FF));
            }
        } else {
            ret = null;
        }
        return ret;
    }

    //    public static OMStipple getOMStipple(
    //      com.bbn.openmap.corba.CSpecialist.CStipplePackage.EStipple estip)
    //    {
    //      System.err.println("EGraphic.getOMStipple: unimplemented");
    //      return (OMStipple) null;
    //    }

    //////////////////////////////////////////////////////////////////////////

    /**
     * constructGesture() - constructs a CSpecialist.MouseEvent from a
     * MapGesture
     */
    protected static com.bbn.openmap.corba.CSpecialist.MouseEvent constructGesture(
                                                                             MapGesture gest) {

        com.bbn.openmap.corba.CSpecialist.Mouse mouse = new com.bbn.openmap.corba.CSpecialist.Mouse();

        // set the mouse parameters
        mouse.point = new com.bbn.openmap.corba.CSpecialist.XYPoint((short) gest.point.x, (short) gest.point.y);
        mouse.llpoint = new com.bbn.openmap.corba.CSpecialist.LLPoint(gest.llpoint.getLatitude(), gest.llpoint.getLongitude());
        mouse.press = gest.press;
        mouse.mousebutton = gest.mousebutton;
        mouse.modifiers = new com.bbn.openmap.corba.CSpecialist.key_modifiers(gest.alt, gest.shift, gest.control);
        //      mouse.modifiers.meta = gest.meta;

        // construct the CSpecialist.MouseEvent
        com.bbn.openmap.corba.CSpecialist.MouseEvent event = new com.bbn.openmap.corba.CSpecialist.MouseEvent();
        switch (gest.event_type) {
        case com.bbn.openmap.corba.CSpecialist.MouseType._ClickEvent:
            event.click(mouse);
            break;
        case com.bbn.openmap.corba.CSpecialist.MouseType._MotionEvent:
            event.motion(mouse);
            break;
        case com.bbn.openmap.corba.CSpecialist.MouseType._KeyEvent:
            event.keypress(new com.bbn.openmap.corba.CSpecialist.Keypress(mouse.point, gest.key, mouse.modifiers));
            break;
        default:
            System.err.println("JGraphic.constructGesture() - invalid type");
        }
        return event;
    }

    public static void update(
                              JObjectHolder graphic,
                              com.bbn.openmap.corba.CSpecialist.GraphicPackage.GF_update update) {

        // do the updates, but don't rerender just yet
        switch (update.discriminator().value()) {

        // mousable object changed
        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_object:
            graphic.setObject(update.obj());
            break;

        // line type changed
        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_lType:
            ((OMGraphic) graphic).setLineType(update.lType().value());
            break;

        // render type changed
        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_rType:
            ((OMGraphic) graphic).setRenderType(update.rType().value());
            break;

        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_color:
            ((OMGraphic) graphic).setLinePaint(getColor(update.color()));
            break;

        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_fillColor:
            ((OMGraphic) graphic).setFillPaint(getColor(update.fillColor()));
            break;

        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_lineWidth:
            ((OMGraphic) graphic).setStroke(new BasicStroke(update.lineWidth()));
            break;

        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_stipple:
            System.err.println("ignoring stipple");
            //            ((OMGraphic)graphic).setStipple(getOMStipple(update.stipple()));
            break;

        case com.bbn.openmap.corba.CSpecialist.GraphicPackage.settableFields._GF_fillStipple:
            System.err.println("ignoring fill stipple");
            //            ((OMGraphic)graphic).setFillStipple(getOMStipple(update.fillStipple()));
            break;

        default:
            System.err.println("JGraphic.update: invalid graphic update");
            break;
        }
    }
}