// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.12 $
// $Date: 2004/05/10 21:10:44 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.omGraphics.awt.TextShapeDecoration;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.event.*;
import com.bbn.openmap.omGraphics.geom.*;
import com.bbn.openmap.omGraphics.labeled.LabeledOMSpline;
import com.bbn.openmap.omGraphics.meteo.OMColdSurfaceFront;
import com.bbn.openmap.omGraphics.meteo.OMHotSurfaceFront;
import com.bbn.openmap.omGraphics.meteo.OMOcclusion;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 * This layer demonstrates interactive capabilities of OpenMap.
 * Instantiating this layer should show an icon loaded using HTTP
 * Protocol, which represents Boston, MA in USA.  Above Boston it
 * should show a square that would change color when mouse is moved
 * over it in 'Gesture' mode.  Also clicking once brings up a message
 * box and more than once brings up browser.  <P>
 *
 * The DemoLayer has also been modified to demonstrate the first uses
 * of the OMDrawingTool.  The Palette has buttons that can be used to
 * start the tool in several different ways.
 * 
 * @see com.bbn.openmap.layer.DemoLayer
 * 
 * Just added some decorated splines to test them. EL
 */
public class DemoLayer extends OMGraphicHandlerLayer
        implements DrawingToolRequestor {

    protected JPanel legend;

    public DemoLayer() {
        // This is how to set the ProjectionChangePolicy, which
        // dictates how the layer behaves when a new projection is
        // received.
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this, true));
        // Making the setting so this layer receives events from the
        // SelectMouseMode, which has a modeID of "Gestures".  Other
        // IDs can be added as needed.
        setMouseModeIDsForEvents(new String[] {"Gestures"});
    }

    public void paint(java.awt.Graphics g) {
        // Super calls the RenderPolicy that makes decisions on how to
        // paint the OMGraphicList.  The only reason we have this
        // method overridden is to paint the legend if it exists.
        super.paint(g);
        if (legend != null) {
            legend.paint(g);
        }
    }

    public void init() {

        // This layer has a set OMGraphicList, created when the layer
        // is created.  It uses the StandardPCPolicy for new
        // projections, which keeps the list intact and simply calls
        // generate() on it with the new projection, and repaint()
        // which calls paint().  If you want a more dynamic layer,
        // don't bother creating your list right away - Override the
        // prepare() method, which gets called by the
        // ProjectionChangePolicy when the projection changes.  See
        // OMGraphicHandlerLayer.

        OMGraphicList omList = (OMGraphicList) getList();

        //      Location loc = new URLRasterLocation(42.3583f,-71.06f,"Boston,Massachusetts,USA","http://javamap.bbn.com:4711/appletimages/city.gif");
        //      //loc.setLocationColor(Color.blue);
        //      loc.setShowLocation(true);
        //      loc.setShowName(true);
        //      //loc.setDetails("Details");
        //      omList.add(loc);

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
        omList.add(omb);

        OMPoint point = new OMPoint(42f, -72f, 14);
        point.setFillPaint(Color.green);
        point.setOval(true);
        omList.add(point);

        OMCircle circle = new OMCircle(40f, -70f, 50, 200);
        circle.setRotationAngle(com.bbn.openmap.MoreMath.HALF_PI / 2f);
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

        LabeledOMSpline spline =
            new LabeledOMSpline(
                40f,
                -72,
                llPointsx,
                llPointsy,
                OMPoly.COORDMODE_ORIGIN);
        spline.setText("Testing");
        spline.setLocateAtCenter(true);
//      spline.setIndex(2);
        omList.add(spline);

        OMSpline spline2 = new OMSpline(llPointsx, llPointsy);
        omList.add(spline2);

        float[] llPoints =
            { 55.0f, -10.0f, 50.0f, -5.0f, 45.0f, -7.0f, 43.0f, -12.0f };
        OMColdSurfaceFront cf =
            new OMColdSurfaceFront(
                llPoints,
                OMSpline.DECIMAL_DEGREES,
                OMSpline.LINETYPE_STRAIGHT);
        omList.add(cf);
        float[] llPoints2 =
            { 55.0f, -12.0f, 50.0f, -7.0f, 45.0f, -9.0f, 43.0f, -14.0f };
        OMHotSurfaceFront hf =
            new OMHotSurfaceFront(
                llPoints2,
                OMSpline.DECIMAL_DEGREES,
                OMSpline.LINETYPE_STRAIGHT);
        omList.add(hf);
        float[] llPoints3 =
            { 55.0f, -14.0f, 50.0f, -9.0f, 45.0f, -11.0f, 43.0f, -16.0f };
        OMOcclusion oc =
            new OMOcclusion(
                llPoints3,
                OMSpline.DECIMAL_DEGREES,
                OMSpline.LINETYPE_STRAIGHT);
        omList.add(oc);

        float[] llPoints4 =
            { 55.0f, -16.0f, 50.0f, -11.0f, 45.0f, -13.0f, 43.0f, -18.0f };
        OMSpline spline3 =
            new OMDecoratedSpline(
                llPoints4,
                OMSpline.DECIMAL_DEGREES,
                OMSpline.LINETYPE_STRAIGHT) {
                protected void initDecorations() {

                    getDecorator().addDecoration(
                        new TextShapeDecoration(
                            " This one has a text ",
                            new Font("arial", Font.PLAIN, 10),
                            TextShapeDecoration.LEFT_TO_RIGHT
                            + TextShapeDecoration.FOLLOW_POLY,
                            TextShapeDecoration.CENTER));
                }
            };
//      omList.add(spline3);

        OMLine line =
            new OMLine(40f, -75f, 42f, -70f, OMGraphic.LINETYPE_STRAIGHT);
//      line.addArrowHead(true);
        line.addArrowHead(OMArrowHead.ARROWHEAD_DIRECTION_BOTH);
        line.setStroke(new BasicStroke(2));
        omList.add(line);

        OMGraphicList pointList = new OMGraphicList();
        for (int i = 0; i < 100; i++) {
            point =
                new OMPoint(
                    (float) (Math.random() * 89f),
                    (float) (Math.random() * -179f),
                    3);
            point.setSelectPaint(Color.yellow);
            pointList.add(point);
        }
        omList.add(pointList);

        OMEllipse ell = new OMEllipse(new LatLonPoint(60f, -110), 
                                      1000, 300, Length.NM,
                                      com.bbn.openmap.MoreMath.HALF_PI/2.0);

        ell.setLinePaint(Color.blue);
        //      ell.setFillPaint(Color.yellow);
        omList.add(ell);

        ell = new OMEllipse(new LatLonPoint(40f, -75), 
                            800, 250, Length.MILE, 0);

        ell.setFillPaint(Color.yellow);
        omList.add(ell);

        float[] llp2 = new float[] {0.41789755f, -1.435303f, 0.41813868f, -1.3967744f};

        OMPoly p2 = new OMPoly(llp2, OMGraphic.RADIANS, OMGraphic.LINETYPE_RHUMB);
        p2.setLinePaint(Color.yellow);
        omList.add(p2);

//      OMArc arc = new OMArc(40f, 65f, 750f, Length.MILE, 20f, 95f);
        OMArc arc = new OMArc((float)40.0, (float)65.0, 
                              (float)750.0, Length.MILE, (float)20.0, (float)95.0);
        arc.setLinePaint(Color.red);
        arc.setFillPaint(new Color(120, 0, 0, 128));
        arc.setArcType(java.awt.geom.Arc2D.PIE);
        omList.add(arc);

        OMAreaList combo = new OMAreaList();

        combo.addOMGraphic(new OMLine((float) 50.453333, (float) 5.223889, (float) 50.375278, (float) 4.873889, 2));
        combo.addOMGraphic(new OMLine((float) 50.375278, (float) 4.873889, (float) 50.436944, (float) 4.860556, 2));
//      combo.addOMGraphic(new OMLine((float) 50.436944, (float) 4.860556, (float) 50.436667, (float) 4.860833, 2));
//      combo.addOMGraphic(new OMLine((float) 50.436667, (float) 4.860833, (float) 50.490833, (float) 4.847778, 2));
//      combo.addOMGraphic(new OMLine((float) 50.491269, (float) 4.704239, (float) 50.490833, (float) 4.847778, 3));
        combo.addOMGraphic(new OMArc((float) 50.491269, (float) 4.704239, (float) 0.09168520552327833, 
                                     (float) (28.201865385183652 + 90.21758717585848), 
                                     (float) -90.21758717585848));
        combo.addOMGraphic(new OMLine((float) 50.534167, (float) 4.831111, (float) 50.640833, (float) 4.832222, 2));
        combo.addOMGraphic(new OMLine((float) 50.640833, (float) 4.832222, (float) 50.547778, (float) 5.223889, 2));
        combo.addOMGraphic(new OMLine((float) 50.547778, (float) 5.223889, (float) 50.453333, (float) 5.223889, 2));

//      combo.setConnectParts(true);
//      combo.addOMGraphic(new OMLine(30f, -125f, 30f, -100f, OMGraphic.LINETYPE_RHUMB));
//      combo.addOMGraphic(new OMLine(30f, -100f, 40f, -95f, OMGraphic.LINETYPE_GREATCIRCLE));
//      combo.addOMGraphic(new OMLine(40f, -95f, 50f, -145f, OMGraphic.LINETYPE_GREATCIRCLE));
//      combo.addOMGraphic(new OMLine(50f, -145f, 30f, -125f, OMGraphic.LINETYPE_STRAIGHT));
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

        OMArc arc1 = new OMArc(100, 100, 200, 200, 0f, -45f); 
        arc1.setLinePaint(Color.blue);
        arc1.setFillPaint(Color.yellow);
        arc1.setArcType(java.awt.geom.Arc2D.PIE);
        omList.add(arc1);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        init();
        setAddToBeanContext(true);
    }

    /**
     * Overriding the OMGraphicHandlerMethod, creating a list if it's null.
     */
    public OMGraphicList getList() {
        // This isn't the default behavior of the
        // OMGraphicHandlerLayer.  Normally, if the list is null, we
        // leave it null because a null list can be easily used as a
        // flag that work has to be done in the prepare() method to
        // contact the data source and create OMGraphics.
        OMGraphicList list = super.getList();
        if (list == null) {
            list = new OMGraphicList();
            super.setList(list);
        }
        return list;
    }

    protected final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer =
        this;

    protected final static String internalKey = "ik";
    protected final static String externalKey = "ek";
    protected GraphicAttributes filterGA = null;

    protected GraphicAttributes getFilterGA() {
        if (filterGA == null) {
            filterGA = new GraphicAttributes();
            filterGA.setLinePaint(Color.red);
            filterGA.setRenderType(OMGraphic.RENDERTYPE_LATLON);
            filterGA.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
            BasicStroke filterStroke =
                new BasicStroke(
                    1f,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10f,
                    new float[] { 3, 3 },
                    0f);
            filterGA.setStroke(filterStroke);
        }
        return (GraphicAttributes)filterGA.clone();
    }

    public java.awt.Component getGUI() {

        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);
        
        JPanel box = PaletteHelper.createVerticalPanel(" Create Filters for Map ");
        box.setLayout(new java.awt.GridLayout(0, 1));

        //      JButton button = new JButton("Add and Edit Offset Line");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  if (dt != null) {

        //                      OMLine line = new OMLine(42f, -72f, -50, -70, 200, 200);
        //                      line.setStroke(new java.awt.BasicStroke(5));
        //                      line.setLinePaint(java.awt.Color.red);
        //                      line.setFillPaint(java.awt.Color.green);

        //                      line  = (OMLine) getDrawingTool().edit(line, layer);
        //                      if (line != null) {
        //                          getList().add(line);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMLine");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Add and Edit XY Line");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  if (dt != null) {

        //                      OMLine line = new OMLine(200, 200, 420, 520);
        //                      line.setLinePaint(java.awt.Color.blue);
        //                      line.setFillPaint(java.awt.Color.green);

        //                      line  = (OMLine) getDrawingTool().edit(line, layer);
        //                      if (line != null) {
        //                          getList().add(line);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMLine");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Add and Edit LatLon Line, no GUI");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  if (dt != null) {
        //                      OMLine line = new OMLine(30f, -60f, 42f, -72f, 
        //                                               OMGraphic.LINETYPE_GREATCIRCLE);
        //                      line.setStroke(new java.awt.BasicStroke(5));
        //                      line.setLinePaint(java.awt.Color.red);
        //                      line.setFillPaint(java.awt.Color.green);

        //                      line  = (OMLine) getDrawingTool().edit(line, layer, false);
        //                      if (line != null) {
        //                          getList().add(line);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMLine");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create XY Line");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  if (dt != null) {
        //                      OMLine line  = (OMLine) getDrawingTool().create("com.bbn.openmap.omGraphics.OMLine", layer);
        //                      if (line != null) {
        //                          getList().add(line);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMLine");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create Offset Line");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        //                  if (dt != null) {
        //                      OMLine line  = (OMLine) getDrawingTool().create("com.bbn.openmap.omGraphics.OMLine", ga, layer);
        //                      if (line != null) {
        //                          getList().add(line);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMLine");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create Lat/Lon Circle");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        //                  if (dt != null) {
        //                      OMCircle circle  = (OMCircle) getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle", ga, layer);
        //                      if (circle != null) {
        //                          getList().add(circle);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMCircle");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create XY Circle");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_XY);
        //                  if (dt != null) {
        //                      OMCircle circle  = (OMCircle) getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle", ga, layer);
        //                      if (circle != null) {
        //                          getList().add(circle);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMCircle");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create Offset Circle");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        //                  ga.setFillPaint(Color.red);
        //                  if (dt != null) {
        //                      OMCircle circle  = (OMCircle) getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle", ga, layer);
        //                      if (circle != null) {
        //                          getList().add(circle);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMCircle");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        JButton button = new JButton("Create Containing Rectangle Filter");
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    DrawingTool dt = getDrawingTool();
                    if (dt != null) {
                        GraphicAttributes fga = getFilterGA();
                        fga.setFillPaint(new OMColor(0x0c0a0a0a));

                        OMRect rect =
                            (OMRect) getDrawingTool().create(
                                "com.bbn.openmap.omGraphics.OMRect",
                                fga,
                                layer,
                                false);
                        if (rect != null) {
                            rect.setAppObject(internalKey);
                        }
                        else {
                            Debug.error(
                                "DemoLayer: Drawing tool can't create OMRect");
                        }
                    }
                    else {
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
                        }
                        else {
                            Debug.error(
                                "DemoLayer: Drawing tool can't create OMPoly");
                        }
                    }
                    else {
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

                        OMRect rect =
                            (OMRect) getDrawingTool().create(
                                "com.bbn.openmap.omGraphics.OMRect",
                                fga,
                                layer,
                                false);
                        if (rect != null) {
                            rect.setAppObject(externalKey);
                        }
                        else {
                            Debug.error(
                                "DemoLayer: Drawing tool can't create OMRect");
                        }
                    }
                    else {
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

        //      button = new JButton("Create XY Rect");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_XY);
        //                  if (dt != null) {
        //                      OMRect rect  = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", ga, layer);
        //                      if (rect != null) {
        //                          getList().add(rect);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMRect");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create Offset Rect");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        //                  ga.setFillPaint(Color.red);
        //                  if (dt != null) {
        //                      OMRect rect  = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", ga, layer);
        //                      if (rect != null) {
        //                          getList().add(rect);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMRect");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create RangeRings");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setLinePaint(Color.yellow);
        //                  if (dt != null) {
        //                      OMRangeRings rr  = (OMRangeRings) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRangeRings", ga, layer);
        //                      if (rr != null) {
        //  //                              rr.setInterval(25, Length.MILE);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMRangeRings");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create XY Poly");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_XY);
        //                  ga.setLinePaint(Color.red);
        //                  ga.setFillPaint(Color.red);
        //                  if (dt != null) {
        //                      OMPoly point  = (OMPoly) getDrawingTool().create("com.bbn.openmap.omGraphics.OMPoly", ga, layer);
        //                      if (point != null) {
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMPoly");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create LatLon Labeled Poly");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
        //                  ga.setLinePaint(Color.green);
        //                  ga.setFillPaint(Color.green);
        //                  if (dt != null) {

        //                      LabeledOMPoly point  = (LabeledOMPoly) getDrawingTool().create("com.bbn.openmap.omGraphics.labeled.LabeledOMPoly", ga, layer);

        //                      if (point != null) {
        //  //                              point.setOval(true);
        //  //                              point.setRadius(8);
        //                          point.setText("Active Testing");
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMPoly");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        //      button = new JButton("Create LatLon Offset Poly");
        //      button.addActionListener(new ActionListener() {
        //              public void actionPerformed(ActionEvent event) {
        //                  DrawingTool dt = getDrawingTool();
        //                  GraphicAttributes ga = new GraphicAttributes();
        //                  ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        //                  ga.setLinePaint(Color.green);
        //                  ga.setFillPaint(Color.green);
        //                  if (dt != null) {
        //                      OMPoly point  = (OMPoly) getDrawingTool().create("com.bbn.openmap.omGraphics.OMPoly", ga, layer);
        //                      if (point != null) {
        //  //                              rr.setInterval(25, Length.MILE);
        //                      } else {
        //                          Debug.error("DemoLayer: Drawing tool can't create OMPoly");
        //                      } 
        //                  } else {
        //                      Debug.output("DemoLayer can't find a drawing tool");
        //                  }
        //              }
        //          });
        //      box.add(button);

        gridbag.setConstraints(box, c);
        panel.add(box);
        return panel;
    }

    protected DrawingTool drawingTool;

    public DrawingTool getDrawingTool() {
        // Usually set in the findAndInit() method.
        return drawingTool;
    }

    public void setDrawingTool(DrawingTool dt) {
        // Called by the findAndInit method.
        drawingTool = dt;
    }

    /**
     * Called when the DrawingTool is complete, providing the layer
     * with the modified OMGraphic.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
        Debug.message("demo", "DemoLayer: DrawingTool complete");

        Object obj = omg.getAppObject();

        if (obj != null
            && (obj == internalKey || obj == externalKey)
            && !action.isMask(OMGraphicConstants.DELETE_GRAPHIC_MASK)) {

            java.awt.Shape filterShape = omg.getShape();
            OMGraphicList filteredList = filter(filterShape, (omg.getAppObject() == internalKey));
            if (Debug.debugging("demo")) {
                Debug.output("DemoLayer filter: " + filteredList.getDescription());
            }
        } else {
            getList().doAction(omg, action);
        }

        repaint();
    }

    /**
     * Called when a component that is needed, and not available with
     * an appropriate interator from the BeanContext.  This lets this
     * object hook up with what it needs.  For Layers, this method
     * doesn't do anything by default.  If you need your layer to get
     * ahold of another object, then you can use the Iterator to go
     * through the objects to look for the one you need.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof DrawingTool) {
            Debug.message("demo", "DemoLayer: found a drawing tool");
            setDrawingTool((DrawingTool) someObj);
        }
    }

    /**
     * BeanContextMembershipListener method.  Called when a new object
     * is removed from the BeanContext of this object.  For the Layer,
     * this method doesn't do anything.  If your layer does something
     * with the childrenAdded method, or findAndInit, you should take
     * steps in this method to unhook the layer from the object used
     * in those methods.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof DrawingTool) {
            if (getDrawingTool() == (DrawingTool) someObj) {
                setDrawingTool(null);
            }
        }
    }

    /**
     * Query that an OMGraphic can be highlighted when the mouse moves
     * over it.  If the answer is true, then highlight with this
     * OMGraphics will be called.
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
     * Query for what text should be placed over the information bar
     * when the mouse is over a particular OMGraphic.
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
     * Query for what tooltip to display for an OMGraphic when the
     * mouse is over it.
     */
    public String getToolTipTextFor(OMGraphic omg) {
        return "Demo Layer Object";
    }

    /**
     * Called if isSelectable(OMGraphic) was true, so the list has the
     * OMGraphic.  A list is used in case underlying code is written
     * to handle more than one OMGraphic being selected at a time.
     */
    public void select(OMGraphicList list) {
        if (list != null && list.size() > 0) {
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

}
