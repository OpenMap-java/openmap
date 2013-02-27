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
// /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/J2525.java,v
// $
// $RCSfile: J2525.java,v $
// $Revision: 1.4 $
// $Date: 2006/02/16 16:22:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

import com.bbn.openmap.corba.CSpecialist.U2525SymbolPackage.E2525Symbol;
import com.bbn.openmap.omGraphics.OMGraphicAdapter;
import com.bbn.openmap.proj.Projection;

public class J2525 extends OMGraphicAdapter implements JObjectHolder {

    protected transient com.bbn.openmap.corba.CSpecialist.EComp object = null;

    E2525Symbol e2525 = null;
    Point point1;
    ImageIcon bitmap;
    ImageObserver observer;

    public J2525(E2525Symbol e2525) {
        super();
        JGraphic.fillOMGraphicParams(this, e2525.egraphic);
        this.e2525 = e2525;
    }

    public void setObject(com.bbn.openmap.corba.CSpecialist.EComp aObject) {
        object = aObject;
    }

    public com.bbn.openmap.corba.CSpecialist.EComp getObject() {
        return object;
    }

    /** generate() - prepares the graphics for rendering. */
    public boolean generate(Projection proj) {
        setNeedToRegenerate(false);

        switch (renderType) {
        case RENDERTYPE_XY:
            break;
        case RENDERTYPE_OFFSET:
            break;
        case RENDERTYPE_LATLON:
            point1 = (Point) proj.forward(e2525.ll1.lat, e2525.ll1.lon, new Point());

            // I'm cheating and forcing all 2525 symbols to be
            // represented by a
            // single bitmap. Ideally, we'd use a ported version of
            // the GSD
            // library to generate an appropriate bitmap for us.
            bitmap = new ImageIcon("plus.bm");
            break;
        case RENDERTYPE_UNKNOWN:
            System.err.println("J2525.generate: invalid RenderType");
            return false;
        }
        return true;
    }

    /** render() - renders the 2525 symbol. */
    public void render(Graphics g) {
        if (g == null) {
            System.err.println("J2525.render: can't render null Graphics");
            return;
        }
        g.setColor(getDisplayColor());

        if (bitmap != null) {
            System.out.println("\n\nHelp!!!!: J2525 can't draw an image!");
            // Need to update this to JFC, but need an
            // ImageObserver...
            // g.drawImage(bitmap, point1.x, point1.y);

            // I'm cheating and drawing in the most-used annotation
            // (bottom1),
            // and ignoring all the others.

            // Determine proper positioning of the bottom1 label.
            // netscape.application.Font f =
            // netscape.application.Font.defaultFont();
            // netscape.application.FontMetrics fm = f.fontMetrics();
            // netscape.application.Size sz =
            // fm.stringSize(e2525.bottom1);
            // int x,y;
            // x = point1.x + (bitmap.width() / 2);
            // y = point1.y + bitmap.height();
            // x -= sz.width/2;
            // y += sz.height;

            // g.setFont(f);
            // g.drawString(e2525.bottom1, x, y);
            int x = point1.x + (bitmap.getImage().getWidth(observer) / 2);
            int y = point1.y + bitmap.getImage().getHeight(observer);
            int w = g.getFontMetrics().stringWidth(e2525.bottom1);
            int h = g.getFontMetrics().getHeight();
            x -= w / 2;
            y += h;

            g.drawString(e2525.bottom1, x, y);
        } else {
            System.err.println("J2525.render: ignoring null bitmap");
        }
    }

    /**
     * distance() - returns the shortest distance from the 2525 symbol to an
     * XY-point
     */
    public float distance(int x, int y) {
        float distance = Float.POSITIVE_INFINITY;
        return distance;
    }

    public void update(com.bbn.openmap.corba.CSpecialist.GraphicPackage.GF_update update) {
        JGraphic.update((JObjectHolder) this, update);
    }

} // class J2525

