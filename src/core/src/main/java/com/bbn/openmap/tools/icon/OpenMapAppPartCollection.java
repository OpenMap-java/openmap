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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/OpenMapAppPartCollection.java,v
// $
// $RCSfile: OpenMapAppPartCollection.java,v $
// $Revision: 1.5 $
// $Date: 2006/08/29 23:07:53 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class OpenMapAppPartCollection extends IconPartCollection {

    static OpenMapAppPartCollection omparts;

    protected OpenMapAppPartCollection() {
        super("OpenMap", "Common parts used in OpenMap Application Icons");
        init();
    }

    public synchronized static OpenMapAppPartCollection getInstance() {
        if (omparts == null) {
            omparts = new OpenMapAppPartCollection();
        }
        return omparts;
    }

    protected void init() {
        add(BIG_BOX);
        add(SMALL_BOX);
        add(FILL_BOX);
        add(UL_TRI);
        add(LR_TRI);
        add(LL_UR_LINE);
        add(UL_LR_LINE);
        add(BIG_ARROW);
        add(MED_ARROW);
        add(SMALL_ARROW);
        add(CORNER_TRI);
        add(OPP_CORNER_TRI);
        add(CIRCLE);
    }

    public final static OpenMapAppPart BIG_BOX = new OpenMapAppPart.Poly("BIG_BOX", "BIG_BOX", new int[] {
            10, 10, 90, 90, 10 }, new int[] { 10, 90, 90, 10, 10 });
    public final static OpenMapAppPart SMALL_BOX = new OpenMapAppPart.Poly("SMALL_BOX", "SMALL_BOX", new int[] {
            30, 30, 70, 70, 30 }, new int[] { 30, 70, 70, 30, 30 });
    public final static OpenMapAppPart FILL_BOX = new OpenMapAppPart.Poly("FILL_BOX", "FILL_BOX", new int[] {
            10, 10, 50, 50, 30, 30, 70, 70, 50, 50, 90, 90, 10 }, new int[] {
            10, 90, 90, 70, 70, 30, 30, 70, 70, 90, 90, 10, 10 });
    public final static OpenMapAppPart UL_TRI = new OpenMapAppPart.Poly("UL_TRI", "UL_TRI", new int[] {
            10, 10, 75, 10 }, new int[] { 10, 75, 10, 10 });
    public final static OpenMapAppPart LR_TRI = new OpenMapAppPart.Poly("LR_TRI", "LR_TRI", new int[] {
            25, 90, 90, 25 }, new int[] { 90, 90, 25, 90 });
    public final static OpenMapAppPart LL_UR_LINE = new OpenMapAppPart.Poly("LL_UR_LINE", "LL_UR_LINE", new int[] {
            10, 90 }, new int[] { 90, 10 });
    public final static OpenMapAppPart UL_LR_LINE = new OpenMapAppPart.Poly("UL_LR_LINE", "UL_LR_LINE", new int[] {
            10, 90 }, new int[] { 10, 90 });
    public final static OpenMapAppPart BIG_ARROW = new OpenMapAppPart.Poly("BIG_ARROW", "BIG_ARROW", new int[] {50,90,80,80,20,20,10,50}, new int[] {10,40,40,90,90,40,40,10});
    public final static OpenMapAppPart MED_ARROW = new OpenMapAppPart.Poly("MED_ARROW", "MED_ARROW", new int[] {50,90,70,70,30,30,10,50}, new int[] {10,50,50,90,90,50,50,10});
    public final static OpenMapAppPart SMALL_ARROW = new OpenMapAppPart.Poly("SMALL_ARROW", "SMALL_ARROW", new int[] {50,80,60,60,40,40,20,50}, new int[] {10,50,50,90,90,50,50,10});
    public final static OpenMapAppPart CORNER_TRI = new OpenMapAppPart.Poly("CORNER_TRI", "CORNER_TRI", new int[] {10,50,10}, new int[] {10,10,50});
    public final static OpenMapAppPart OPP_CORNER_TRI = new OpenMapAppPart.Poly("OPP_CORNER_TRI", "OPP_CORNER_TRI", new int[] {50,90,50}, new int[] {50,50,90});
    public final static OpenMapAppPart CIRCLE = new OpenMapAppPart.Circle("CIRCLE", "CIRCLE", 50, 50, 46);
    public final static OpenMapAppPart DOT = new OpenMapAppPart.Circle("DOT", "DOT", 50, 50, 6);
    public final static OpenMapAppPart PLUS = new OpenMapAppPart.Poly("PLUS", "PLUS", new int[] {25,50,50,50,50,75,75,50,50,50,50,25,25}, new int[] {50,50,25,25,50,50,50,50,75,75,50,50,50});
    public final static OpenMapAppPart ADD_PLUS = new OpenMapAppPart.Poly("ADD_PLUS", "ADD_PLUS", new int[] {70,80,80,80,80,90}, new int[] {20,20,10,30,20,20});
    public final static OpenMapAppPart MINUS = new OpenMapAppPart.Poly("MINUS", "MINUS", new int[] {25,75}, new int[] {50,50});
    public final static OpenMapAppPart MAP_PIN_HEAD = new OpenMapAppPart.Circle("MAP_PIN_HEAD", "MAP_PIN_HEAD", 50, 33, 30);
    public final static OpenMapAppPart MAP_PIN_BOTTOM = new OpenMapAppPart.Poly("MAP_PIN_BOTTOM", "MAP_PIN_BOTTOM", new int[] {50,30,70,50}, new int[] {90,50,50,90});
    
    public static class OpenMapAppPart extends IconPartCollectionEntry {

        public OpenMapAppPart(String n, String d, Shape shape) {
            this(n, d, shape, (AffineTransform) null);
        }

        public OpenMapAppPart(String n, String d, Shape shape,
                AffineTransform affTrans) {
            super(n, d, new BasicIconPart(shape, affTrans));
        }

        public static class Poly extends OpenMapAppPart {
            public Poly(String n, String d, int[] xp, int[] yp) {
                this(n, d, xp, yp, (AffineTransform) null);
            }

            public Poly(String n, String d, int[] xp, int[] yp,
                    AffineTransform af) {
                super(n, d, new Polygon(xp, yp, xp.length), af);
            }
        }

        public static class Circle extends OpenMapAppPart {
            public Circle(String n, String d, double x, double y, double radius) {
                this(n, d, x, y, radius, (AffineTransform) null);
            }

            public Circle(String n, String d, double x, double y,
                    double radius, AffineTransform af) {
                super(n,
                      d,
                      new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2),
                      af);
            }
        }
    }

}