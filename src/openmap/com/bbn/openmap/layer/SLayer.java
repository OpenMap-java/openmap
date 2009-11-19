//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: SLayer.java,v $
//$Revision: 1.1 $
//$Date: 2005/12/09 21:09:08 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMShape;
import com.bbn.openmap.proj.Projection;

public class SLayer extends OMGraphicHandlerLayer {

    public SLayer() {
        super();
    }

    public synchronized OMGraphicList prepare() {
        OMGraphicList list = getList();

        if (list == null) {
            list = new OMGraphicList();

            createGraphics(list, getProjection());
        } else {
            list.generate(getProjection());
        }
        return list;
    }

    public void createGraphics(OMGraphicList toAddTo, Projection proj) {
        int[] xs = new int[] { -20, 20, 20, -20, -20 };
        int[] ys = new int[] { 20, 20, 30, 30, 20 };

        Polygon poly = new Polygon(xs, ys, 5);
        OMShape s1 = new OMShape(poly);
        s1.setFillPaint(Color.red);
        s1.generate(proj);
        toAddTo.add(s1);

        Line2D line = new Line2D.Double(-70, 30, -100, 50);
        OMShape s2 = new OMShape(line);
        s2.setLinePaint(Color.green);
        s2.generate(proj);
        toAddTo.add(s2);

        Arc2D arc1 = new Arc2D.Float(-40f, -60f, 20f, 20f, 90f, 120f, Arc2D.PIE);
        OMShape s3 = new OMShape(arc1);
        s3.setFillPaint(Color.CYAN);
        s3.generate(proj);
        toAddTo.add(s3);
    }

}
