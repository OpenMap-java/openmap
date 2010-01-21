package com.bbn.openmap.layer.test;
import java.awt.Color;

import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.geo.GeoArray;
import com.bbn.openmap.geo.Intersection;
import com.bbn.openmap.geo.OMGeo;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMTextLabeler;

//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

public class GeoTestLayer extends OMGraphicHandlerLayer {

    public GeoTestLayer() {

    }

    public OMGraphicList prepare() {
        OMGraphicList list = getList();
        if (list == null) {
            list = getGeos();
        }

        if (list != null) {
            list.generate(getProjection());
        }

        return list;
    }

    protected OMGraphicList getGeos() {
        OMGraphicList ret = new OMGraphicList();

        GeoArray thePolygon = GeoArray.Double.createFromLatLonDegrees(_Polygon);
        Geo x = new Geo(38.75, -27.6, true);
        boolean bInside = Intersection.isPointInPolygon(x, thePolygon);
        System.out.println("The point " + x.getLatitude() + ","
                + x.getLongitude() + " is "
                + ((bInside) ? "INSIDE" : "OUTSIDE") + " the polygon");

        System.out.println("self intersecting: "
                + (Intersection.isSelfIntersectingPoly(_Polygon) ? "TRUE"
                        : "FALSE"));

        OMPoly poly = new OMPoly(_Polygon, OMPoly.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE);
        poly.setFillPaint(new Color(0xaaFF0000, true));
//        ret.add(poly);

        Geo centerGeo = Intersection.center(thePolygon);
        OMPoint center = new OMPoint(centerGeo.getLatitude(), centerGeo.getLongitude());
        center.putAttribute(OMGraphic.LABEL, new OMTextLabeler("CENTER"));
        center.setLinePaint(Color.yellow);
        center.setFillPaint(Color.green);
        ret.add(center);

        x = new Geo(39, -90, true);
        bInside = Intersection.isPointInPolygon(x, thePolygon);
        System.out.println("The point " + x.getLatitude() + ","
                + x.getLongitude() + " is "
                + ((bInside) ? "INSIDE" : "OUTSIDE") + " the polygon");

        OMPoint point = new OMPoint(39.0, -90.0);
        point.putAttribute(OMGraphic.LABEL,
                new OMTextLabeler(bInside ? "1 INSIDE" : "1 OUTSIDE"));
        point.setLinePaint(Color.yellow);
        point.setFillPaint(Color.red);
        ret.add(point);

        OMGeo.Pt g1 = new OMGeo.Pt(0.0, 0.0, true);
        g1.setLinePaint(Color.green);
        OMGeo.Pt g2 = new OMGeo.Pt(10.0, 10.0, true);
        g2.setLinePaint(Color.green);
        OMGeo.Pt g3 = new OMGeo.Pt(g1.getPoint().add(g2.getPoint(), new Geo()));
        g3.setLinePaint(Color.yellow);
        
        ret.add(g1);
        ret.add(g2);
        ret.add(g3);
        
        // Just for yucks, let's reverse the winding...
        // int nLength = _Polygon.length;
        // double[] _PolygonReverse = new double[nLength];
        // for (int i = (nLength - 1), j = 0; j < nLength; --i, ++j) {
        // _PolygonReverse[j] = _Polygon[i];
        // }
        // GeoArray thePolygon2 =
        // GeoArray.Double.createFromLatLonDegrees(_Polygon);

        // OMPoly poly2 = new OMPoly(_PolygonReverse, OMPoly.DECIMAL_DEGREES,
        // OMGraphic.LINETYPE_GREATCIRCLE);
        // poly2.setFillPaint(new Color(0xaa00FF00, true));
        // ret.add(poly2);

        // x = new Geo(38.75, -27.6, true);
        // bInside = Intersection.isPointInPolygon(x, thePolygon2);
        // System.out.println("The point " + x.getLatitude() + ","
        // + x.getLongitude() + " is "
        // + ((bInside) ? "INSIDE" : "OUTSIDE") + " the polygon2");

        // Geo centerGeo = Intersection.center(thePolygon2);
        // OMPoint center = new OMPoint(centerGeo.getLatitude(),
        // centerGeo.getLongitude());
        // center.putAttribute(OMGraphic.LABEL, new OMTextLabeler("CENTER"));
        // center.setLinePaint(Color.yellow);
        // center.setFillPaint(Color.green);
        // ret.add(center);

        // OMPoint point2 = new OMPoint(38.75, -27.6);
        // point2.putAttribute(OMGraphic.LABEL, new OMTextLabeler(bInside ?
        // "2 INSIDE" : "2 OUTSIDE"));
        // point2.setLinePaint(Color.yellow);
        // point2.setFillPaint(Color.green);
        // ret.add(point2);

        // x = new Geo(39, -90, true);
        // bInside = Intersection.isPointInPolygon(x, thePolygon2);
        // System.out.println("The point " + x.getLatitude() + ","
        // + x.getLongitude() + " is "
        // + ((bInside) ? "INSIDE" : "OUTSIDE") + " the polygon2");

        // point2 = new OMPoint(39.0, -90.0);
        // point2.putAttribute(OMGraphic.LABEL, new OMTextLabeler(bInside ?
        // "3 INSIDE" : "3 OUTSIDE"));
        // point2.setLinePaint(Color.yellow);
        // point2.setFillPaint(Color.green);
        // ret.add(point2);

        return ret;
    }

    
    private static double[] _Polygon = { 73, 46, 73, 121, 73, 155, 46.4317,
        155, 39.625, 150.482, 37.2, 150.5, 35.2, 146, 32, 146, 25, 144.343,
        21, 145.2, 17.42, 146.5, 16.5367, 147.925, 14.875, 148.982, 12.215,
        148.962, 7, 155, -45.2333, 155.233, -38.3117, 75, -35, 26,
        -35.2167, 10, -18, 10, -9.66667, 11.4, -4.79833, 6.58333, 0.391667,
        -7.745, 4.57167, -12.165, 6.96, -14.72, 12.2833, -17.3667, 13, -18,
        20.9167, -18, 25, -19.8117, 35.9667, -11.45, 36.7617, -13.6733, 38,
        -15, 42, -15, 43, -13, 45, -13, 45, -9, 46, -8.75, 48, -8.75,
        48.5983, -9, 49.0667, -11.745, 49, -15, 57, -15, 57, -10, 61, -10,
        61, 0.001667, 63, 3.71667, 68.0967, 10.9583, 69.7033, 18.9967,
        70.3667, 32, 73, 46 };
    
    // Trimmed
//    private static double[] _Polygon = { 73, 46, 73, 121, 73, 155, 46.4317,
//            155, 39.625, 150.482, 37.2, 150.5, 35.2, 146, 32, 146, 25, 144.343,
//            21, 145.2, 17.42, 146.5, 16.5367, 147.925, 14.875, 148.982, 12.215,
//            148.962, 7, 155, -45.2333, 155.233, -38.3117, 75, -35, 26,
//            -35.2167, 10, -18, 10, -9.66667, 11.4, -4.79833, 6.58333, 0.391667,
//            -7.745, 4.57167, -12.165, 6.96, -14.72, 12.2833, -17.3667, 13, -18,
//            20.9167, -18, 25, -19.8117, 35.9667, -11.45, 36.7617, -13.6733, 38,
//            -15, 42, -15, 43, -13, 45, -13, 45, -9, 46, -8.75, 48, -8.75,
//            48.5983, -9, 49.0667, -11.745, 49, -15, 57, -15, 57, -10, 61, -10,
//            61, 0.001667, 63, 3.71667, 68.0967, 10.9583, 69.7033, 18.9967,
//            70.3667, 32, 73, 46 };

    // Non-intersecting
    // private static double[] _Polygon = { 73, 46, 73, 121, 73, -169, 72, -141,
    // 69.2, -141, 60.9, -141, 59.5, -139.6, 57.9, -141.6, 57.75, -152.33,
    // 56.95, -158.62, 55.2667, -162.775, 53.905, -166.55, 51.9167,
    // -176.567, 50.515, 175.945, 46.4317, 162.31, 39.625, 150.482, 37.2,
    // 150.5, 35.2, 146, 32, 146, 25, 144.343, 21, 145.2, 17.42, 146.5,
    // 16.5367, 147.925, 14.875, 148.982, 12.215, 148.962, 7, 158.2, 8.7,
    // 167.7, 7.1, 171.3, 11.7083, -180, 21.1567, -164, 22.8, -162.617,
    // 25.1617, -154.563, 30, -146.15, 34.6, -135.7, 37.8483, -127,
    // 37.8333, -125.835, 37, -125, 36.1817, -124.76, 34.2, -123.1, 33.5,
    // -122.6, 32.7, -122, 31.6, -121.4, 25, -120, 14.535, -132.312, -5,
    // -142.515, -14.9483, -147.683, -17.5483, -149.602, -33.0917, -175,
    // -37, 178.967, -39.8167, 178.933, -45.2333, 172.233, -46.4667,
    // 168.483, -38.3117, 75, -35, 26, -35.2167, 10, -18, 10, -9.66667,
    // 11.4, -4.79833, 6.58333, 0.391667, -7.745, 4.57167, -12.165, 6.96,
    // -14.72, 12.2833, -17.3667, 13, -18, 20.9167, -18, 25, -19.8117,
    // 35.9667, -11.45, 36.7617, -13.6733, 38, -15, 42, -15, 43, -13, 45,
    // -13, 45, -9, 46, -8.75, 48, -8.75, 48.5983, -9, 49.0667, -11.745,
    // 49, -15, 57, -15, 57, -10, 61, -10, 61, 0.001667, 63, 3.71667,
    // 68.0967, 10.9583, 69.7033, 18.9967, 70.3667, 32, 73, 46 };

    // Original
    // private static double[] _Polygon = { 73, 46, 73, 121, 73, -169, 72, -141,
    // 69.2, -141, 60.9, -141, 59.5, -139.6, 57.9, -141.6, 57.75, -152.33,
    // 56.95, -158.62, 55.2667, -162.775, 53.905, -166.55, 51.9167,
    // -176.567, 50.515, 175.945, 46.4317, 162.31, 39.625, 150.482, 37.2,
    // 150.5, 35.2, 146, 32, 146, 25, 144.343, 21, 145.2, 17.42, 146.5,
    // 16.5367, 147.925, 14.875, 148.982, 12.215, 148.962, 7, 158.2, 8.7,
    // 167.7, 7.1, 171.3, 11.7083, -180, 21.1567, -164, 22.8, -162.617,
    // 25.1617, -154.563, 30, -146.15, 34.6, -135.7, 37.8483, -127,
    // 37.8333, -125.835, 37, -125, 36.1817, -124.76, 34.2, -123.1, 33.5,
    // -122.6, 32.7, -122, 31.6, -121.4, 25, -120, 85.9999, 176, 14.535,
    // -132.312, -5, -142.515, -14.9483, -147.683, -17.5483, -149.602,
    // -33.0917, -175, -37, 178.967, -39.8167, 178.933, -45.2333, 172.233,
    // -46.4667, 168.483, -38.3117, 75, -35, 26, -35.2167, 10, -18, 10,
    // -9.66667, 11.4, -4.79833, 6.58333, 0.391667, -7.745, 4.57167,
    // -12.165, 6.96, -14.72, 12.2833, -17.3667, 13, -18, 20.9167, -18,
    // 25, -19.8117, 35.9667, -11.45, 36.7617, -13.6733, 38, -15, 42, -15,
    // 43, -13, 45, -13, 45, -9, 46, -8.75, 48, -8.75, 48.5983, -9,
    // 49.0667, -11.745, 49, -15, 57, -15, 57, -10, 61, -10, 61, 0.001667,
    // 63, 3.71667, 68.0967, 10.9583, 75, 75, 69.7033, 18.9967, 70.3667,
    // 32, 73, 46 };

}
