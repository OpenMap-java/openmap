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
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/JUnit.java,v
// $
// $RCSfile: JUnit.java,v $
// $Revision: 1.4 $
// $Date: 2006/02/16 16:22:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.XYPoint;
import com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.EUnitSymbol;
import com.bbn.openmap.omGraphics.OMGraphicAdapter;
import com.bbn.openmap.proj.Projection;

public class JUnit extends OMGraphicAdapter implements ImageObserver, JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    EUnitSymbol eunit = null;
    Point point1;
    ImageIcon symbol_;
    ImageIcon echelon_;
    float total_height = 0f;
    float total_width = 0f;

    public JUnit(EUnitSymbol eunit) {
        super();
        JGraphic.fillOMGraphicParams(this, eunit.egraphic);
        this.eunit = eunit;
    }

    public void setObject(com.bbn.openmap.corba.CSpecialist.EComp aObject) {
        object = aObject;
    }

    public com.bbn.openmap.corba.CSpecialist.EComp getObject() {
        return object;
    }

    /** generate() - prepares the graphics for rendering. */
    public boolean generate(Projection proj) {
        needToRegenerate = false;

        switch (renderType) {
        case RENDERTYPE_XY:
            point1 = new Point(eunit.p1.x, eunit.p1.y);
            break;
        case RENDERTYPE_OFFSET:
            point1 = (Point) proj.forward(eunit.ll1.lat, eunit.ll1.lon, new Point());
            point1.x += eunit.p1.x;
            point1.y += eunit.p1.y;
            break;
        case RENDERTYPE_LATLON:
            point1 = (Point) proj.forward(eunit.ll1.lat, eunit.ll1.lon, new Point());
            break;
        case RENDERTYPE_UNKNOWN:
            System.err.println("JUnit.generate: invalid RenderType");
            return false;
        }

        String image_path = "fmsymbols/";
        image_path += eunit.group;
        image_path += "/";
        image_path += eunit.symbol;
        image_path += ".xbm";
        symbol_ = new ImageIcon(image_path.toLowerCase());

        image_path = "fmsymbols/echelons/";
        image_path += eunit.echelon;
        image_path += ".xbm";
        echelon_ = new ImageIcon(image_path.toLowerCase());
        return true;
    }

    /** render() - renders the unit. */
    public void render(Graphics g) {
        if (g == null) {
            System.err.println("JUnit.render: can't render null Graphics");
            return;
        }
        g.setColor(getDisplayColor());

        // I'm cheating and drawing in the most-used annotation
        // (bottom1),
        // and ignoring all the others.

        // Call Hack functions to pre-create the Images
        //     if (!eunit.symbol.equals(""))
        //        g.createImage(symbol_);
        //     if (!eunit.echelon.equals(""))
        //        g.createImage(echelon_);

        // First, figure out where to draw the echelon
        int ech_x = point1.x + (int) ((symbol_.getIconWidth() / 2))
                - (int) ((echelon_.getIconWidth() / 2));

        if (!eunit.echelon.equals("")) {
            g.drawImage(echelon_.getImage(), ech_x, point1.y, this);
            total_height = echelon_.getIconHeight();
            total_width = echelon_.getIconWidth();
        }

        // Next, draw the symbol
        int sym_y = point1.y + (int) (echelon_.getIconHeight());
        if (!eunit.symbol.equals("")) {
            g.drawImage(symbol_.getImage(), point1.x, sym_y, this);
            total_height += symbol_.getIconHeight();
            if (symbol_.getIconWidth() > total_width)
                total_width = symbol_.getIconWidth();
        }

        // Finally, put the label string underneath

        if (!eunit.bottom1.equals("")) {
            int x = point1.x + (symbol_.getIconWidth() / 2);
            int y = point1.y + symbol_.getIconHeight()
                    + echelon_.getIconHeight();
            Font font = new Font("Helvetica", java.awt.Font.PLAIN, 10);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(eunit.bottom1);
            int h = fm.getHeight();
            total_height += h;
            if (w > total_width)
                total_width = w;
            x -= w / 2;
            y += h;

            g.drawString(eunit.bottom1, x, y);
        }

    }

    /**
     * distance() - returns the shortest distance from the unit to an
     * XY-point
     */
    public float distance(int x, int y) {
        float distance = Float.POSITIVE_INFINITY;
        if (getNeedToRegenerate()) {
            System.err.println("JUnit.distance(): not projected!");
            return distance;
        }

        if (point1 == null) {
            System.err.println("JUnit.distance(): invalid" + " Raster location");
            return distance;
        }

        if ((x >= point1.x && x <= point1.x + total_width)
                && (y >= point1.y && y <= point1.y + total_height)) {
            // The point is within my boundaries.
            return 0f;
        } else {
            // The point is outside my boundaries. Don't calculate the
            // hypotenuse distance since precision doesn't matter much
            distance = Math.max(Math.max(point1.x - x, x
                    - (point1.x + total_width)), Math.max(point1.y - y, y
                    - (point1.y + total_height)));
        }
        return distance;

    }

    public void update(
                       com.bbn.openmap.corba.CSpecialist.GraphicPackage.GF_update update) {
        JGraphic.update((JObjectHolder) this, update);
    }

    public void update(
                       com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.USF_update update) {
        // do the updates, but don't rerender just yet
        setNeedToRegenerate(true); // flag dirty

        switch (update.discriminator().value()) {
        // set fixed point
        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_ll1:
            LLPoint ll = update.ll1();
            eunit.ll1.lat = ll.lat;
            eunit.ll1.lon = ll.lon;
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_p1:
            XYPoint pt = update.p1();
            eunit.p1.x = pt.x;
            eunit.p1.y = pt.y;
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_group:
            eunit.group = update.group();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_symbol:
            eunit.symbol = update.symbol();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_echelon:
            eunit.echelon = update.echelon();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_left1:
            eunit.left1 = update.left1();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_left2:
            eunit.left2 = update.left2();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_left3:
            eunit.left3 = update.left3();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_left4:
            eunit.left4 = update.left4();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_right1:
            eunit.right1 = update.right1();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_right2:
            eunit.right2 = update.right2();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_right3:
            eunit.right3 = update.right3();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_right4:
            eunit.right4 = update.right4();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_top1:
            eunit.top1 = update.top1();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_bottom1:
            String bottom1 = update.bottom1();
            eunit.bottom1 = bottom1;
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_nom_size:
            eunit.nom_size = update.nom_size();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_min_size:
            eunit.min_size = update.min_size();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_max_size:
            short max_size = update.max_size();
            eunit.max_size = max_size;
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_scale:
            eunit.scale = update.scale();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_is_hq:
            eunit.is_hq = update.is_hq();
            break;

        case com.bbn.openmap.corba.CSpecialist.UnitSymbolPackage.settableFields._USF_rotate:
            eunit.rotate = update.rotate();
            break;

        default:
            System.err.println("JUnit.update: invalid unit symbol update");
            setNeedToRegenerate(false);

            break;
        }
    }

    // From the Image Observer Interface
    public boolean imageUpdate(Image img, int infoflags, int x, int y,
                               int width, int height) {
        return false;
    }

} // class JUnit

