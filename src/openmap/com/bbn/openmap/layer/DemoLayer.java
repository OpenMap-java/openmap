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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/DemoLayer.java,v $
// $RCSfile: DemoLayer.java,v $
// $Revision: 1.25 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************
package com.bbn.openmap.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.FontSizer;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMArc;
import com.bbn.openmap.omGraphics.OMAreaList;
import com.bbn.openmap.omGraphics.OMArrowHead;
import com.bbn.openmap.omGraphics.OMBitmap;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMDecoratedSpline;
import com.bbn.openmap.omGraphics.OMEllipse;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMScalingIcon;
import com.bbn.openmap.omGraphics.OMShape;
import com.bbn.openmap.omGraphics.OMSpline;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.omGraphics.awt.CircleShapeDecoration;
import com.bbn.openmap.omGraphics.awt.LineShapeDecoration;
import com.bbn.openmap.omGraphics.awt.ShapeDecorator;
import com.bbn.openmap.omGraphics.labeled.LabeledOMSpline;
import com.bbn.openmap.omGraphics.meteo.IceAreaShapeDecoration;
import com.bbn.openmap.omGraphics.meteo.OMHotSurfaceFront;
import com.bbn.openmap.omGraphics.meteo.OMOcclusion;
import com.bbn.openmap.omGraphics.util.ArcCalc;
import com.bbn.openmap.omGraphics.util.RibbonMaker;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.symbology.milStd2525.SymbolPart;
import com.bbn.openmap.tools.symbology.milStd2525.SymbolReferenceLibrary;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 * This layer demonstrates interactive capabilities of OpenMap. Instantiating
 * this layer should show an icon loaded using HTTP Protocol, which represents
 * Boston, MA in USA. Above Boston it should show a square that would change
 * color when mouse is moved over it in 'Gesture' mode. Also clicking once
 * brings up a message box and more than once brings up browser.
 * <P>
 * 
 * The DemoLayer has also been modified to demonstrate the first uses of the
 * OMDrawingTool. The Palette has buttons that can be used to start the tool in
 * several different ways.
 * 
 * @see com.bbn.openmap.layer.DemoLayer
 * 
 *      Just added some decorated splines to test them. EL
 */
public class DemoLayer extends OMGraphicHandlerLayer implements DrawingToolRequestor {

    protected JPanel legend;
    /**
     * Found in the findAndInit() method, in the MapHandler.
     */
    protected DrawingTool drawingTool;
    /**
     * Found in the findAndInit() method, in the MapHandler.
     */
    protected SymbolReferenceLibrary srl;
    /**
     * Used by the internal ActionListeners for a callback, see getGUI().
     */
    protected final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer = this;
    /**
     * Used by geometries created in GUI for specify if the spatial filter is
     * for objects inside the drawn shape.
     */
    protected final static String internalKey = "ik";
    /**
     * Used by geometries created in GUI for specify if the spatial filter is
     * for objects outside the drawn shape.
     */
    protected final static String externalKey = "ek";
    protected GraphicAttributes filterGA = null;
    /**
     * This is a list to hold the non-changing OMGraphics to display on the
     * layer. It is used to load the OMGraphicList that the layer actually
     * paints.
     */
    protected OMGraphicList objects;

    public DemoLayer() {
        setName("Demo");
        // This is how to set the ProjectionChangePolicy, which
        // dictates how the layer behaves when a new projection is
        // received.
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
        // Making the setting so this layer receives events from the
        // SelectMouseMode, which has a modeID of "Gestures". Other
        // IDs can be added as needed.
        setMouseModeIDsForEvents(new String[] { "Gestures" });
    }

    public void paint(java.awt.Graphics g) {
        // Super calls the RenderPolicy that makes decisions on how to
        // paint the OMGraphicList. The only reason we have this
        // method overridden is to paint the legend if it exists.
        super.paint(g);
        if (legend != null) {
            legend.paint(g);
        }
    }

    public OMGraphicList init() {

        // This layer keeps a pointer to an OMGraphicList that it uses
        // for painting. It's initially set to null, which is used as
        // a flag in prepare() to signal that the OMGraphcs need to be
        // created. The list returned from prepare() gets set in the
        // layer.
        // This layer uses the StandardPCPolicy for new
        // projections, which keeps the list intact and simply calls
        // generate() on it with the new projection, and repaint()
        // which calls paint().

        OMGraphicList omList = new OMGraphicList();

        // Location loc = new
        // URLRasterLocation(42.3583f,-71.06f,"Boston,Massachusetts,USA","http://javamap.bbn.com:4711/appletimages/city.gif");
        // //loc.setLocationColor(Color.blue);
        // loc.setShowLocation(true);
        // loc.setShowName(true);
        // //loc.setDetails("Details");
        // omList.add(loc);

        int bytearrsize = (16 * 16) / 8;
        byte[] bytearr = new byte[bytearrsize];

        for (int i = 0; i < bytearr.length; i++) {
            bytearr[i] = (byte) 0xffffffff;
        }

        OMBitmap omb = new OMBitmap(45.3583f, -71.06f, 16, 16, bytearr);
        omb.setLinePaint(Color.red);
        omb.setFillPaint(null);
        omb.setSelectPaint(Color.blue);
        omb.setRotationAngle(Math.PI / 2);
        omb.putAttribute(RCT, "bitmap");
        omList.add(omb);

        OMPoint point = new OMPoint(42f, -72f, 14);
        point.setFillPaint(Color.green);
        point.setOval(true);
        omList.add(point);

        OMCircle circle = new OMCircle(40f, -70f, 50, 200);
        circle.setRotationAngle(com.bbn.openmap.MoreMath.HALF_PI / 2f);
        circle.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler("Circle Label", OMText.JUSTIFY_CENTER));
        circle.putAttribute(RCT, "circle");
        omList.add(circle);

        int[] llPointsx = new int[5];
        int[] llPointsy = new int[5];
        llPointsy[0] = 10;
        llPointsx[0] = 170;
        llPointsy[1] = 42;
        llPointsx[1] = 273;
        llPointsy[2] = 38;
        llPointsx[2] = 374;
        llPointsy[3] = 78;
        llPointsx[3] = 468;
        llPointsy[4] = 84;
        llPointsx[4] = 369;

        LabeledOMSpline spline = new LabeledOMSpline(40f, -72, llPointsx, llPointsy, OMPoly.COORDMODE_ORIGIN);
        spline.setText("Testing");
        spline.setLocateAtCenter(true);
        spline.putAttribute(RCT, "spline 1");
        // spline.setIndex(2);
        omList.add(spline);

        OMSpline spline2 = new OMSpline(llPointsx, llPointsy);
        spline2.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler("Spline Label"));
        spline2.setLinePaint(Color.green);
        spline2.putAttribute(RCT, "spline 2");
        omList.add(spline2);

        double[] llPoints = { 55.0f, -10.0f, 50.0f, -5.0f, 45.0f, -7.0f, 43.0f, -12.0f, 55.0f,
                -10.0f };
        OMDecoratedSpline omds = new OMDecoratedSpline(llPoints, OMSpline.DECIMAL_DEGREES, OMSpline.LINETYPE_STRAIGHT);
        ShapeDecorator sd = new ShapeDecorator();
        sd.addDecoration(new LineShapeDecoration(5, com.bbn.openmap.omGraphics.OMColor.clear));
        sd.addDecoration(new IceAreaShapeDecoration(7, 7, IceAreaShapeDecoration.RIGHT));
        omds.setDecorator(sd);
        omList.add(omds);

        llPoints = new double[] { 56.0f, -11.0f, 51.0f, -6.0f, 46.0f, -8.0f, 44.0f, -13.0f, 56.0f,
                -11.0f };
        omds = new OMDecoratedSpline(llPoints, OMSpline.DECIMAL_DEGREES, OMSpline.LINETYPE_STRAIGHT);
        sd = new ShapeDecorator();
        sd.addDecoration(new LineShapeDecoration(3, com.bbn.openmap.omGraphics.OMColor.clear));
        sd.addDecoration(new CircleShapeDecoration(5, 5, Color.blue));
        omds.setDecorator(sd);
        omList.add(omds);

        llPoints = new double[] { 57.0f, -12.0f, 52.0f, -7.0f, 47.0f, -9.0f, 45.0f, -14.0f, 57.0f,
                -12.0f };
        omds = new OMDecoratedSpline(llPoints, OMSpline.DECIMAL_DEGREES, OMSpline.LINETYPE_STRAIGHT);
        sd = new ShapeDecorator();
        sd.addDecoration(new LineShapeDecoration(2, com.bbn.openmap.omGraphics.OMColor.clear));
        sd.addDecoration(new CircleShapeDecoration(5, 5, Color.red));
        sd.addDecoration(new LineShapeDecoration(2, com.bbn.openmap.omGraphics.OMColor.clear));
        sd.addDecoration(new LineShapeDecoration(15, Color.red));
        omds.setDecorator(sd);
        omList.add(omds);

        double[] llPoints2 = { 55.0f, -12.0f, 50.0f, -7.0f, 45.0f, -9.0f, 43.0f, -14.0f };
        OMHotSurfaceFront hf = new OMHotSurfaceFront(llPoints2, OMSpline.DECIMAL_DEGREES, OMSpline.LINETYPE_STRAIGHT);
        omList.add(hf);
        double[] llPoints3 = { 55.0f, -14.0f, 50.0f, -9.0f, 45.0f, -11.0f, 43.0f, -16.0f };
        OMOcclusion oc = new OMOcclusion(llPoints3, OMSpline.DECIMAL_DEGREES, OMSpline.LINETYPE_STRAIGHT);
        omList.add(oc);

        // float[] llPoints4 = { 55.0f, -16.0f, 50.0f, -11.0f, 45.0f,
        // -13.0f,
        // 43.0f, -18.0f };
        // OMSpline spline3 = new OMDecoratedSpline(llPoints4,
        // OMSpline.DECIMAL_DEGREES, OMSpline.LINETYPE_STRAIGHT) {
        // protected void initDecorations() {
        //
        // getDecorator().addDecoration(new TextShapeDecoration(" This
        // one has a text ", new Font("arial", Font.PLAIN, 10),
        // TextShapeDecoration.LEFT_TO_RIGHT
        // + TextShapeDecoration.FOLLOW_POLY,
        // TextShapeDecoration.CENTER));
        // }
        // };
        // omList.add(spline3);

        OMLine line = new OMLine(40f, -75f, 42f, -70f, OMGraphic.LINETYPE_GREATCIRCLE);
        // line.addArrowHead(true);
        line.addArrowHead(OMArrowHead.ARROWHEAD_DIRECTION_BOTH);
        line.setStroke(new BasicStroke(2));
        line.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler("Line Label"));

        omList.add(line);

        OMLine arcLine = new OMLine(0d, 0d, -20d, 30d, OMGraphic.LINETYPE_GREATCIRCLE);
        arcLine.setLinePaint(Color.green);
        arcLine.setArc(new ArcCalc(Math.PI, true));
        omList.add(arcLine);

        OMGraphicList pointList = new OMGraphicList();
        for (int i = 0; i < 100; i++) {
            point = new OMPoint((float) (Math.random() * 89f), (float) (Math.random() * -179f), 3);
            point.setSelectPaint(Color.yellow);
            point.putAttribute(RCT, "Point " + i);
            pointList.add(point);
        }
        omList.add(pointList);

        OMEllipse ell = new OMEllipse(new LatLonPoint.Double(60, -110), 1000, 300, Length.NM, com.bbn.openmap.MoreMath.HALF_PI / 2.0);

        ell.setLinePaint(Color.blue);
        // ell.setFillPaint(Color.yellow);
        omList.add(ell);

        ell = new OMEllipse(new LatLonPoint.Double(40, -75), 800, 250, Length.MILE, 0);

        ell.setFillPaint(Color.yellow);
        omList.add(ell);

        double[] llp2 = new double[] { 0.41789755f, -1.435303f, 0.41813868f, -1.3967744f };

        OMPoly p2 = new OMPoly(llp2, OMGraphic.RADIANS, OMGraphic.LINETYPE_RHUMB);
        p2.setLinePaint(Color.yellow);
        omList.add(p2);

        // OMArc arc = new OMArc(40f, 65f, 750f, Length.MILE, 20f,
        // 95f);
        OMArc arc = new OMArc((float) 40.0, (float) 65.0, (float) 750.0, Length.MILE, (float) 20.0, (float) 95.0);
        arc.setLinePaint(Color.red);
        arc.setFillPaint(new Color(120, 0, 0, 128));
        arc.setArcType(java.awt.geom.Arc2D.OPEN);
        arc.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler("Arc Label", OMText.JUSTIFY_CENTER));
        omList.add(arc);

        OMAreaList combo = new OMAreaList();

        combo.addOMGraphic(new OMLine((float) 50.453333, (float) 5.223889, (float) 50.375278, (float) 4.873889, 2));
        combo.addOMGraphic(new OMLine((float) 50.375278, (float) 4.873889, (float) 50.436944, (float) 4.860556, 2));
        // combo.addOMGraphic(new OMLine((float) 50.436944, (float)
        // 4.860556, (float) 50.436667, (float) 4.860833, 2));
        // combo.addOMGraphic(new OMLine((float) 50.436667, (float)
        // 4.860833, (float) 50.490833, (float) 4.847778, 2));
        // combo.addOMGraphic(new OMLine((float) 50.491269, (float)
        // 4.704239, (float) 50.490833, (float) 4.847778, 3));
        combo.addOMGraphic(new OMArc((float) 50.491269, (float) 4.704239, (float) 0.09168520552327833, (float) (28.201865385183652 + 90.21758717585848), (float) -90.21758717585848));
        combo.addOMGraphic(new OMLine((float) 50.534167, (float) 4.831111, (float) 50.640833, (float) 4.832222, 2));
        combo.addOMGraphic(new OMLine((float) 50.640833, (float) 4.832222, (float) 50.547778, (float) 5.223889, 2));
        combo.addOMGraphic(new OMLine((float) 50.547778, (float) 5.223889, (float) 50.453333, (float) 5.223889, 2));

        // combo.setConnectParts(true);
        // combo.addOMGraphic(new OMLine(30f, -125f, 30f, -100f,
        // OMGraphic.LINETYPE_RHUMB));
        // combo.addOMGraphic(new OMLine(30f, -100f, 40f, -95f,
        // OMGraphic.LINETYPE_GREATCIRCLE));
        // combo.addOMGraphic(new OMLine(40f, -95f, 50f, -145f,
        // OMGraphic.LINETYPE_GREATCIRCLE));
        // combo.addOMGraphic(new OMLine(50f, -145f, 30f, -125f,
        // OMGraphic.LINETYPE_STRAIGHT));
        combo.setLinePaint(Color.blue);
        combo.setFillPaint(Color.green);
        omList.add(combo);

        OMAreaList combo1 = new OMAreaList();
        combo1.addOMGraphic(new OMLine(66.618519f, 141.563497f, 66.028244f, 140.193964f, OMGraphic.LINETYPE_GREATCIRCLE));
        combo1.addOMGraphic(new OMLine(66.028244f, 140.193964f, 66.968058f, 137.611042f, OMGraphic.LINETYPE_RHUMB));
        combo1.addOMGraphic(new OMLine(66.968058f, 137.611042f, 67.558261f, 139.033958f, OMGraphic.LINETYPE_GREATCIRCLE));
        combo1.addOMGraphic(new OMLine(67.558261f, 139.033958f, 66.618519f, 141.563497f, OMGraphic.LINETYPE_RHUMB));
        combo1.setLinePaint(Color.red);
        combo1.setFillPaint(Color.blue);
        omList.add(combo1);

        combo1 = new OMAreaList();
        combo1.addOMGraphic(new OMLine(65.495278f, 55.488889f, 65.022778f, 55.749167f, OMGraphic.LINETYPE_GREATCIRCLE));
        combo1.addOMGraphic(new OMLine(65.022778f, 55.749167f, 64.970278f, 55.208611f, OMGraphic.LINETYPE_RHUMB));
        combo1.addOMGraphic(new OMLine(64.970278f, 55.208611f, 65.442778f, 54.948889f, OMGraphic.LINETYPE_GREATCIRCLE));
        combo1.addOMGraphic(new OMLine(65.442778f, 54.948889f, 65.495278f, 55.488889f, OMGraphic.LINETYPE_RHUMB));
        combo1.setLinePaint(Color.blue);
        combo1.setFillPaint(Color.red);
        omList.add(combo1);

        // OMArc arc1 = new OMArc(100, 100, 200, 200, 0f, -45f);
        // arc1.setLinePaint(Color.blue);
        // arc1.setFillPaint(Color.yellow);
        // arc1.setArcType(java.awt.geom.Arc2D.PIE);
        // omList.add(arc1);

        OMText text = new OMText(30f, 80f, "Testing FontSizer", OMText.JUSTIFY_CENTER);
        text.setFontSizer(new FontSizer(30000000f, 1, 5, 40));
        omList.add(text);

        if (srl != null) {
            ImageIcon ii = srl.getIcon("SFPPV-----*****", new Dimension(200, 200));
            if (ii != null) {
                OMScalingIcon omsi = new OMScalingIcon(20f, -50f, ii);
                omsi.setBaseScale(4000000);
                omsi.setMinScale(1000000);
                omsi.setMaxScale(6000000);
                omsi.setRotationAngle(Math.PI / 4);
                omsi.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler("SFPPV-----*****", OMText.JUSTIFY_LEFT, OMTextLabeler.ANCHOR_RIGHT));

                omList.add(omsi);

                SymbolPart sp = srl.getSymbolPartForCode("SFPPV-----*****");
                if (sp != null) {
                    omsi.putAttribute(OMGraphic.TOOLTIP, sp.getDescription());
                }
            } else {
                Debug.output("DemoLayer: couldn't create symbol from SymbolReferenceLibrary");
            }
        }

        GeneralPath gp = new GeneralPath();
        gp.moveTo(20, 20);
        gp.lineTo(20, -20);
        gp.lineTo(-20, -20);
        gp.lineTo(-20, 20);
        gp.lineTo(20, 20);
        gp.moveTo(10, 10);
        gp.lineTo(10, -10);
        gp.lineTo(-10, -10);
        gp.lineTo(-10, 10);
        gp.lineTo(10, 10);
        OMShape oms = new OMShape(gp);
        oms.setFillPaint(Color.orange);

        omList.add(oms);

        OMGraphicList geoTest = new OMGraphicList();
        LatLonPoint pnt1 = new LatLonPoint.Double(42.0, -71.0);
        LatLonPoint pnt2 = new LatLonPoint.Double(42.3, -70.678);
        double gspacing = Length.MILE.toRadians(5);

        OMCircle ompoint1 = new OMCircle(pnt1.getLatitude(), pnt1.getLongitude(), gspacing, Length.RADIAN);
        OMCircle ompoint2 = new OMCircle(pnt2.getLatitude(), pnt2.getLongitude(), gspacing, Length.RADIAN);

        LatLonPoint int1 = GreatCircle.pointAtDistanceBetweenPoints(pnt1.getRadLat(), pnt1.getRadLon(), pnt2.getRadLat(), pnt2.getRadLon(), gspacing, -1);
        LatLonPoint int2 = GreatCircle.pointAtDistanceBetweenPoints(pnt2.getRadLat(), pnt2.getRadLon(), pnt1.getRadLat(), pnt1.getRadLon(), gspacing, -1);

        OMLine geoline = new OMLine(int1.getLatitude(), int1.getLongitude(), int2.getLatitude(), int2.getLongitude(), OMGraphic.LINETYPE_GREATCIRCLE);
        ompoint1.setLinePaint(Color.red);
        ompoint2.setLinePaint(Color.red);
        geoline.setLinePaint(Color.red);
        geoTest.add(ompoint1);
        geoTest.add(ompoint2);
        geoTest.add(geoline);

        omList.add(geoTest);

        OMText omtest = new OMText(42.0, -71.0, "Testing how this looks\nwhen doing multiple lines", OMText.JUSTIFY_LEFT);
        omtest.setBaseline(OMText.BASELINE_TOP);
        omtest.setFillPaint(Color.red);

        omList.add(omtest);

        llPoints = new double[] { -5.856972964554054E-4, 7.181106520146243E-5,
                -5.856972964554055E-4, -7.181106520146255E-5, -2.9284864843698565E-4,
                -7.18110652014625E-5, 5.856972969587945E-4, -5.756442432493538E-4,
                5.856972969587943E-4, 5.756442432493541E-4, -2.9284864843698565E-4,
                7.181106520146243E-5, -5.856972964554054E-4, 7.181106520146243E-5, };

        double buffer = 0.1645788336933045;

        for (int i = 0; i < llPoints.length - 1; i += 2) {
            double lat = Math.toDegrees(llPoints[i]);
            double lon = Math.toDegrees(llPoints[i + 1]);

            OMText txt = new OMText(lat, lon, Integer.toString(i / 2), OMText.JUSTIFY_LEFT);
            omList.add(txt);
        }

        RibbonMaker ribbonMaker = RibbonMaker.createFromRadians(llPoints);

        OMGraphic omg = ribbonMaker.getOuterRing(Length.NM.toRadians(buffer));
        omg.setLinePaint(Color.red);
        omList.add(omg);

        OMPoly ribbonPoly = new OMPoly(llPoints, OMPoly.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE);
        ribbonPoly.setLinePaint(Color.orange);
        omList.add(ribbonPoly);

        OMEllipse ome1 = new OMEllipse(new LatLonPoint.Double(20.0, -110.0), 600, 300, Length.MILE, 0);
        ome1.setLinePaint(Color.cyan);
        omList.add(ome1);

        OMEllipse ome4 = new OMEllipse(new LatLonPoint.Double(20.0, -110.0), 600, 300, Length.MILE, Math.toRadians(45));
        ome4.setLinePaint(Color.red);
        omList.add(ome4);

        OMEllipse ome2 = new OMEllipse(new LatLonPoint.Double(20.0, -110.0), 600, 300, Length.MILE, Math.toRadians(60));
        ome2.setLinePaint(Color.orange);
        omList.add(ome2);

        OMEllipse ome3 = new OMEllipse(new LatLonPoint.Double(20.0, -110.0), 600, 300, Length.MILE, Math.toRadians(115));
        ome3.setLinePaint(Color.green);
        omList.add(ome3);

        OMEllipse ome5 = new OMEllipse(new LatLonPoint.Double(20.0, -110.0), 600, 300, Length.MILE, Math.toRadians(135));
        ome5.setLinePaint(Color.MAGENTA);
        omList.add(ome5);

        return omList;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        setAddToBeanContext(true);
    }

    /**
     * This is an important Layer method to override. The prepare method gets
     * called when the layer is added to the map, or when the map projection
     * changes. We need to make sure the OMGraphicList returned from this method
     * is what we want painted on the map. The OMGraphics need to be generated
     * with the current projection. We test for a null OMGraphicList in the
     * layer to see if we need to create the OMGraphics. This layer doesn't
     * change it's OMGraphics for different projections, if your layer does, you
     * need to clear out the OMGraphicList and add the OMGraphics you want for
     * the current projection.
     */
    public synchronized OMGraphicList prepare() {
        if (objects == null) {
            objects = init();
        }
        OMGraphicList list = new OMGraphicList();
        // Return new list of the objects to mange for the projection change.
        list.addAll(objects);
        list.generate(getProjection());
        return list;
    }

    protected GraphicAttributes getFilterGA() {
        if (filterGA == null) {
            filterGA = new GraphicAttributes();
            filterGA.setLinePaint(Color.red);
            filterGA.setRenderType(OMGraphic.RENDERTYPE_LATLON);
            filterGA.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
            BasicStroke filterStroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, new float[] {
                    3, 3 }, 0f);
            filterGA.setStroke(filterStroke);
        }
        return (GraphicAttributes) filterGA.clone();
    }

    public java.awt.Component getGUI() {

        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);

        JPanel box = PaletteHelper.createVerticalPanel(" Create Filters for Map ");
        box.setLayout(new java.awt.GridLayout(0, 1));

        // JButton button = new JButton("Add and Edit Offset Line");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // if (dt != null) {

        // OMLine line = new OMLine(42f, -72f, -50, -70, 200, 200);
        // line.setStroke(new java.awt.BasicStroke(5));
        // line.setLinePaint(java.awt.Color.red);
        // line.setFillPaint(java.awt.Color.green);

        // line = (OMLine) getDrawingTool().edit(line, layer);
        // if (line != null) {
        // getList().add(line);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMLine");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Add and Edit XY Line");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // if (dt != null) {

        // OMLine line = new OMLine(200, 200, 420, 520);
        // line.setLinePaint(java.awt.Color.blue);
        // line.setFillPaint(java.awt.Color.green);

        // line = (OMLine) getDrawingTool().edit(line, layer);
        // if (line != null) {
        // getList().add(line);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMLine");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Add and Edit LatLon Line, no GUI");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // if (dt != null) {
        // OMLine line = new OMLine(30f, -60f, 42f, -72f,
        // OMGraphic.LINETYPE_GREATCIRCLE);
        // line.setStroke(new java.awt.BasicStroke(5));
        // line.setLinePaint(java.awt.Color.red);
        // line.setFillPaint(java.awt.Color.green);

        // line = (OMLine) getDrawingTool().edit(line, layer, false);
        // if (line != null) {
        // getList().add(line);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMLine");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create XY Line");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // if (dt != null) {
        // OMLine line = (OMLine)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMLine",
        // layer);
        // if (line != null) {
        // getList().add(line);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMLine");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create Offset Line");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        // if (dt != null) {
        // OMLine line = (OMLine)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMLine",
        // ga, layer);
        // if (line != null) {
        // getList().add(line);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMLine");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create Lat/Lon Circle");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        // if (dt != null) {
        // OMCircle circle = (OMCircle)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle",
        // ga, layer);
        // if (circle != null) {
        // getList().add(circle);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create
        // OMCircle");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create XY Circle");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_XY);
        // if (dt != null) {
        // OMCircle circle = (OMCircle)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle",
        // ga, layer);
        // if (circle != null) {
        // getList().add(circle);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create
        // OMCircle");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create Offset Circle");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        // ga.setFillPaint(Color.red);
        // if (dt != null) {
        // OMCircle circle = (OMCircle)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle",
        // ga, layer);
        // if (circle != null) {
        // getList().add(circle);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create
        // OMCircle");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        JButton button = new JButton("Create Containing Rectangle Filter");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                DrawingTool dt = getDrawingTool();
                if (dt != null) {
                    GraphicAttributes fga = getFilterGA();
                    fga.setFillPaint(new OMColor(0x0c0a0a0a));

                    OMRect rect = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", fga, layer, false);
                    if (rect != null) {
                        rect.setAppObject(internalKey);
                    } else {
                        Debug.error("DemoLayer: Drawing tool can't create OMRect");
                    }
                } else {
                    Debug.output("DemoLayer can't find a drawing tool");
                }
            }
        });
        box.add(button);

        button = new JButton("Create Containing Polygon Filter");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                DrawingTool dt = getDrawingTool();
                if (dt != null) {
                    GraphicAttributes fga = getFilterGA();
                    fga.setFillPaint(OMColor.clear);

                    EditableOMPoly eomp = new EditableOMPoly(fga);
                    eomp.setEnclosed(true);
                    eomp.setShowGUI(false);

                    dt.setBehaviorMask(OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);
                    OMPoly poly = (OMPoly) getDrawingTool().edit(eomp, layer);

                    if (poly != null) {
                        poly.setIsPolygon(true);
                        poly.setAppObject(internalKey);
                    } else {
                        Debug.error("DemoLayer: Drawing tool can't create OMPoly");
                    }
                } else {
                    Debug.output("DemoLayer can't find a drawing tool");
                }
            }
        });
        box.add(button);

        button = new JButton("Create Excluding Rectangle Filter");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                DrawingTool dt = getDrawingTool();
                if (dt != null) {
                    GraphicAttributes fga = getFilterGA();
                    fga.setFillPaint(OMColor.clear);

                    OMRect rect = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", fga, layer, false);
                    if (rect != null) {
                        rect.setAppObject(externalKey);
                    } else {
                        Debug.error("DemoLayer: Drawing tool can't create OMRect");
                    }
                } else {
                    Debug.output("DemoLayer can't find a drawing tool");
                }
            }
        });
        box.add(button);

        button = new JButton("Reset filter");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                resetFiltering();
                repaint();
            }
        });
        box.add(button);

        // button = new JButton("Create XY Rect");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_XY);
        // if (dt != null) {
        // OMRect rect = (OMRect)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect",
        // ga, layer);
        // if (rect != null) {
        // getList().add(rect);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMRect");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create Offset Rect");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        // ga.setFillPaint(Color.red);
        // if (dt != null) {
        // OMRect rect = (OMRect)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect",
        // ga, layer);
        // if (rect != null) {
        // getList().add(rect);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMRect");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create RangeRings");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setLinePaint(Color.yellow);
        // if (dt != null) {
        // OMRangeRings rr = (OMRangeRings)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMRangeRings",
        // ga, layer);
        // if (rr != null) {
        // // rr.setInterval(25, Length.MILE);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create
        // OMRangeRings");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create XY Poly");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_XY);
        // ga.setLinePaint(Color.red);
        // ga.setFillPaint(Color.red);
        // if (dt != null) {
        // OMPoly point = (OMPoly)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMPoly",
        // ga, layer);
        // if (point != null) {
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMPoly");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create LatLon Labeled Poly");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        // ga.setLinePaint(Color.green);
        // ga.setFillPaint(Color.green);
        // if (dt != null) {

        // LabeledOMPoly point = (LabeledOMPoly)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.labeled.LabeledOMPoly",
        // ga, layer);

        // if (point != null) {
        // // point.setOval(true);
        // // point.setRadius(8);
        // point.setText("Active Testing");
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMPoly");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        // button = new JButton("Create LatLon Offset Poly");
        // button.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent event) {
        // DrawingTool dt = getDrawingTool();
        // GraphicAttributes ga = new GraphicAttributes();
        // ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        // ga.setLinePaint(Color.green);
        // ga.setFillPaint(Color.green);
        // if (dt != null) {
        // OMPoly point = (OMPoly)
        // getDrawingTool().create("com.bbn.openmap.omGraphics.OMPoly",
        // ga, layer);
        // if (point != null) {
        // // rr.setInterval(25, Length.MILE);
        // } else {
        // Debug.error("DemoLayer: Drawing tool can't create OMPoly");
        // }
        // } else {
        // Debug.output("DemoLayer can't find a drawing tool");
        // }
        // }
        // });
        // box.add(button);

        gridbag.setConstraints(box, c);
        panel.add(box);
        return panel;
    }

    public DrawingTool getDrawingTool() {
        // Usually set in the findAndInit() method.
        return drawingTool;
    }

    public void setDrawingTool(DrawingTool dt) {
        // Called by the findAndInit method.
        drawingTool = dt;
    }

    /**
     * Called when the DrawingTool is complete, providing the layer with the
     * modified OMGraphic.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
        Debug.message("demo", "DemoLayer: DrawingTool complete");

        Object obj = omg.getAppObject();

        if (obj != null && (obj == internalKey || obj == externalKey)
                && !action.isMask(OMGraphicConstants.DELETE_GRAPHIC_MASK)) {

            java.awt.Shape filterShape = omg.getShape();
            OMGraphicList filteredList = filter(filterShape, (omg.getAppObject() == internalKey));
            if (Debug.debugging("demo")) {
                Debug.output("DemoLayer filter: " + filteredList.getDescription());
            }
        } else {
            if (!doAction(omg, action)) {
                // null OMGraphicList on failure, should only occur if
                // OMGraphic is added to layer before it's ever been
                // on the map.
                setList(new OMGraphicList());
                doAction(omg, action);
            }
        }

        repaint();
    }

    /**
     * Called when a component that is needed, and not available with an
     * appropriate iterator from the BeanContext. This lets this object hook up
     * with what it needs. For Layers, this method doesn't do anything by
     * default. If you need your layer to get ahold of another object, then you
     * can use the Iterator to go through the objects to look for the one you
     * need.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof DrawingTool) {
            Debug.message("demo", "DemoLayer: found a drawing tool");
            setDrawingTool((DrawingTool) someObj);
        }

        if (someObj instanceof SymbolReferenceLibrary) {
            setSymbolReferenceLibrary((SymbolReferenceLibrary) someObj);
        }
    }

    /**
     * Set the MilStd2525 SymbolReferenceLibrary object used to create symbols.
     * 
     * @param library
     */
    public void setSymbolReferenceLibrary(SymbolReferenceLibrary library) {
        srl = library;
    }

    public SymbolReferenceLibrary getSymbolReferenceLibrary() {
        return srl;
    }

    /**
     * BeanContextMembershipListener method. Called when a new object is removed
     * from the BeanContext of this object. For the Layer, this method doesn't
     * do anything. If your layer does something with the childrenAdded method,
     * or findAndInit, you should take steps in this method to unhook the layer
     * from the object used in those methods.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof DrawingTool) {
            if (getDrawingTool() == (DrawingTool) someObj) {
                setDrawingTool(null);
            }
        }
    }

    /**
     * Query that an OMGraphic can be highlighted when the mouse moves over it.
     * If the answer is true, then highlight with this OMGraphics will be
     * called.
     */
    public boolean isHighlightable(OMGraphic omg) {
        return true;
    }

    /**
     * Query that an OMGraphic is selectable.
     */
    public boolean isSelectable(OMGraphic omg) {
        DrawingTool dt = getDrawingTool();
        return (dt != null && dt.canEdit(omg.getClass()));
    }

    /**
     * Query for what text should be placed over the information bar when the
     * mouse is over a particular OMGraphic.
     */
    public String getInfoText(OMGraphic omg) {
        DrawingTool dt = getDrawingTool();
        if (dt != null && dt.canEdit(omg.getClass())) {
            return "Click to edit graphic.";
        } else {
            return null;
        }
    }

    /**
     * Query for what tooltip to display for an OMGraphic when the mouse is over
     * it.
     */
    public String getToolTipTextFor(OMGraphic omg) {
        Object tt = omg.getAttribute(OMGraphic.TOOLTIP);
        if (tt instanceof String) {
            return (String) tt;
        }

        String classname = omg.getClass().getName();
        int lio = classname.lastIndexOf('.');
        if (lio != -1) {
            classname = classname.substring(lio + 1);
        }

        return "Demo Layer Object: " + classname;
    }

    /**
     * Called if isSelectable(OMGraphic) was true, so the list has the
     * OMGraphic. A list is used in case underlying code is written to handle
     * more than one OMGraphic being selected at a time.
     */
    public void select(OMGraphicList list) {
        if (list != null && !list.isEmpty()) {
            OMGraphic omg = list.getOMGraphicAt(0);
            DrawingTool dt = getDrawingTool();

            if (dt != null && dt.canEdit(omg.getClass())) {
                dt.setBehaviorMask(OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);
                if (dt.edit(omg, this) == null) {
                    // Shouldn't see this because we checked, but ...
                    fireRequestInfoLine("Can't figure out how to modify this object.");
                }
            }
        }
    }

    public List<Component> getItemsForMapMenu(MapMouseEvent me) {
        List<Component> l = new ArrayList<Component>();
        l.add(new JMenuItem("When"));
        l.add(new JMenuItem("Where"));
        l.add(new JMenuItem("How"));

        return l;
    }

    private String RCT = "rightClickTest";

    public List<Component> getItemsForOMGraphicMenu(OMGraphic omg) {

        String rightClickTest = (String) omg.getAttribute(RCT);

        logger.info("right click test: " + rightClickTest);

        List<Component> l = new ArrayList<Component>();
        l.add(new JMenuItem("Which"));
        l.add(new JMenuItem("Why"));
        l.add(new JSeparator());
        l.add(new JMenuItem(rightClickTest));
        return l;
    }
}
