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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/test/TestLayer.java,v $
// $RCSfile: TestLayer.java,v $
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMArrowHead;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 * A Layer for testing different types of graphics. The GUI code is
 * very large and ugly. Maybe break this off into several classes.
 * <p>
 * This layer responds to the following properties: <code><pre>
 * 
 *  # initial visibility settings:
 *  test.line.visible=true
 *  test.circ.visible=true
 *  test.rect.visible=true
 *  test.text.visible=true
 *  test.poly.visible=true
 *  # latlon vertices of the poly
 *  #test.poly.vertices=80 -180 80 -90 80 0 80 90 80 180 70 180 70 90 70 0 70 -90 70 -180
 *  
 * </pre></code> In addition, you can get this layer to work with the
 * OpenMap viewer by editing your openmap.properties file: <code><pre>
 * 
 *  # layers
 *  openmap.layers=test ...
 *  # class
 *  test.class=com.bbn.openmap.layer.TestLayer
 *  # name
 *  test.prettyName=Graticule
 *  
 * </pre></code>
 */
public class TestLayer extends OMGraphicHandlerLayer implements
        MapMouseListener {

    public final static transient String LineVisibleProperty = ".line.visible";
    public final static transient String CircVisibleProperty = ".circ.visible";
    public final static transient String RectVisibleProperty = ".rect.visible";
    public final static transient String TextVisibleProperty = ".text.visible";
    public final static transient String PolyVisibleProperty = ".poly.visible";
    public final static transient String PolyVertsProperty = ".poly.vertices";

    // colors
    protected final static transient String[] colorNames = new String[] {
            "white", "lightGray", "gray", "darkGray", "black", "red", "pink",
            "orange", "yellow", "green", "magenta", "cyan", "blue", "clear" };
    protected final static transient Color[] colors = new Color[] {
            Color.white, Color.lightGray, Color.gray, Color.darkGray,
            Color.black, Color.red, Color.pink, Color.orange, Color.yellow,
            Color.green, Color.magenta, Color.cyan, Color.blue, OMGraphic.clear };
    protected final static transient int NCOLORS = colors.length;

    // graphics and peers
    protected OMCircle omcircle = new OMCircle();
    protected Circle circle = new Circle();

    protected OMLine omline = new OMLine();
    protected Line line = new Line();

    protected OMRect omrect = new OMRect();
    protected Rect rect = new Rect();

    protected OMText omtext = new OMText();
    protected Text text = new Text();

    protected OMPoly ompoly = new OMPoly();
    protected Poly poly = new Poly();

    protected JPanel gui = null;// the GUI

    /**
     * Construct the TestLayer.
     */
    public TestLayer() {}

    /**
     * The properties and prefix are managed and decoded here, for the
     * standard uses of the GraticuleLayer.
     * 
     * @param prefix string prefix used in the properties file for
     *        this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);

        line.visible = Boolean.valueOf(properties.getProperty(prefix
                + LineVisibleProperty, "true")).booleanValue();

        circle.visible = Boolean.valueOf(properties.getProperty(prefix
                + CircVisibleProperty, "true")).booleanValue();

        rect.visible = Boolean.valueOf(properties.getProperty(prefix
                + RectVisibleProperty, "true")).booleanValue();

        text.visible = Boolean.valueOf(properties.getProperty(prefix
                + TextVisibleProperty, "true")).booleanValue();

        poly.visible = Boolean.valueOf(properties.getProperty(prefix
                + PolyVisibleProperty, "true")).booleanValue();

        String verts = properties.getProperty(prefix + PolyVertsProperty);
        if (verts != null) {
            poly.setVertices(verts);
        }
    }

    public synchronized OMGraphicList prepare() {
        if (getList() == null) {
            setList(generateGraphics());
        }
        return super.prepare();
    }

    /**
     * Create and project the graphics.
     */
    protected OMGraphicList generateGraphics() {
        OMGraphicList graphics = new OMGraphicList();

        // create OMLine from internal line representation
        switch (line.rt) {
        case OMGraphic.RENDERTYPE_LATLON:
            omline = new OMLine(line.llpts[0], line.llpts[1], line.llpts[2], line.llpts[3], line.type, line.nsegs);
            break;
        case OMGraphic.RENDERTYPE_XY:
            omline = new OMLine(line.xypts[0], line.xypts[1], line.xypts[2], line.xypts[3]);
            break;
        case OMGraphic.RENDERTYPE_OFFSET:
            omline = new OMLine(line.llpts[0], line.llpts[1], line.xypts[0], line.xypts[1], line.xypts[2], line.xypts[3]);
            break;
        default:
            System.err.println("ARRRR!");
            break;
        }
        if (line.arrowhead) {
            omline.addArrowHead(line.arrowtype);
        }

        // create OMCircle from internal circle representation
        switch (circle.rt) {
        case OMGraphic.RENDERTYPE_LATLON:
            omcircle = new OMCircle(circle.llpts[0], circle.llpts[1], circle.radius, Length.KM, circle.nsegs);
            omcircle.setPolarCorrection(true);
            break;
        case OMGraphic.RENDERTYPE_XY:
            omcircle = new OMCircle(circle.xypts[0], circle.xypts[1], circle.width, circle.height);
            break;
        case OMGraphic.RENDERTYPE_OFFSET:
            omcircle = new OMCircle(circle.llpts[0], circle.llpts[1], circle.xypts[0], circle.xypts[1], circle.width, circle.height);
            break;
        default:
            System.err.println("ARRRR!");
            break;
        }

        // create OMRect from internal rect representation
        switch (rect.rt) {
        case OMGraphic.RENDERTYPE_LATLON:
            omrect = new OMRect(rect.llpts[0], rect.llpts[1], rect.llpts[2], rect.llpts[3], rect.type, rect.nsegs);
            break;
        case OMGraphic.RENDERTYPE_XY:
            omrect = new OMRect(rect.xypts[0], rect.xypts[1], rect.xypts[2], rect.xypts[3]);
            break;
        case OMGraphic.RENDERTYPE_OFFSET:
            omrect = new OMRect(rect.llpts[0], rect.llpts[1], rect.xypts[0], rect.xypts[1], rect.xypts[2], rect.xypts[3]);
            break;
        default:
            System.err.println("ARRRR!");
            break;
        }

        // create OMText from internal text representation
        switch (text.rt) {
        case OMGraphic.RENDERTYPE_LATLON:
            omtext = new OMText(text.llpts[0], text.llpts[1], text.data, Font.decode(text.font), text.just);
            break;
        case OMGraphic.RENDERTYPE_XY:
            omtext = new OMText(text.xypts[0], text.xypts[1], text.data, Font.decode(text.font), text.just);
            break;
        case OMGraphic.RENDERTYPE_OFFSET:
            omtext = new OMText(text.llpts[0], text.llpts[1], text.xypts[0], text.xypts[1], text.data, Font.decode(text.font), text.just);
            break;
        default:
            System.err.println("ARRRR!");
            break;
        }

        // create OMPoly from internal poly representation
        switch (poly.rt) {
        case OMGraphic.RENDERTYPE_LATLON:
            int len = poly.llpts.length;
            double[] llpts = new double[len];
            System.arraycopy(poly.llpts, 0, llpts, 0, len);
            ompoly = new OMPoly(llpts, OMPoly.DECIMAL_DEGREES, poly.type, poly.nsegs);
            break;
        case OMGraphic.RENDERTYPE_XY:
            ompoly = new OMPoly(poly.xypts);
            break;
        case OMGraphic.RENDERTYPE_OFFSET:
            ompoly = new OMPoly(poly.lat, poly.lon, poly.xypts, poly.cMode);
            break;
        default:
            System.err.println("ARRRR!");
            break;
        }

        // generic
        omline.setVisible(line.visible);
        omline.setLinePaint(colors[line.lineColor]);
        omcircle.setVisible(circle.visible);
        omcircle.setLinePaint(colors[circle.lineColor]);
        omrect.setVisible(rect.visible);
        omrect.setLinePaint(colors[rect.lineColor]);
        ompoly.setVisible(poly.visible);
        ompoly.setLinePaint(colors[poly.lineColor]);
        omtext.setVisible(text.visible);
        omtext.setLinePaint(colors[text.lineColor]);
        if (circle.isFilled)
            omcircle.setFillPaint(colors[circle.fillColor]);
        if (rect.isFilled)
            omrect.setFillPaint(colors[rect.fillColor]);
        if (poly.isFilled)
            ompoly.setFillPaint(colors[poly.fillColor]);

        graphics.add(omline);
        graphics.add(omcircle);
        graphics.add(omrect);
        graphics.add(omtext);
        graphics.add(ompoly);
        graphics.generate(getProjection());

        return graphics;
    }

    /**
     * Gets the palette associated with the layer.
     * <p>
     * 
     * @return Component or null
     */
    public Component getGUI() {
        if (gui == null) {
            JPanel pal;

            gui = PaletteHelper.createPaletteJPanel("Test");
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            gui.setLayout(gridbag);
            constraints.fill = GridBagConstraints.HORIZONTAL; // fill
                                                              // horizontally
            constraints.gridwidth = GridBagConstraints.REMAINDER; //another
                                                                  // row
            constraints.anchor = GridBagConstraints.EAST; // tack to
                                                          // the left
                                                          // edge
            //          constraints.weightx = 0.0;

            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = Integer.parseInt(e.getActionCommand(), 10);
                    switch (index) {
                    case 0:
                        line.visible = !line.visible;
                        omline.setVisible(line.visible);
                        repaint();
                        break;
                    case 1:
                        circle.visible = !circle.visible;
                        omcircle.setVisible(circle.visible);
                        repaint();
                        break;
                    case 2:
                        rect.visible = !rect.visible;
                        omrect.setVisible(rect.visible);
                        repaint();
                        break;
                    case 3:
                        text.visible = !text.visible;
                        omtext.setVisible(text.visible);
                        repaint();
                        break;
                    case 4:
                        poly.visible = !poly.visible;
                        ompoly.setVisible(poly.visible);
                        repaint();
                        break;
                    default:
                        System.out.println("TestLayer: Unimplemented...");
                    }
                }
            };
            pal = PaletteHelper.createCheckbox("Graphics", new String[] {
                    "Line", "Circle", "Rect", "Text", "Poly" }, new boolean[] {
                    line.visible, circle.visible, rect.visible, text.visible,
                    poly.visible }, al);
            gridbag.setConstraints(pal, constraints);
            gui.add(pal);

            // line controls
            pal = getGraphicPalette(line, "Line");
            gridbag.setConstraints(pal, constraints);
            gui.add(pal);

            // circle controls
            pal = getGraphicPalette(circle, "Circle");
            gridbag.setConstraints(pal, constraints);
            gui.add(pal);

            // rect controls
            pal = getGraphicPalette(rect, "Rect");
            gridbag.setConstraints(pal, constraints);
            gui.add(pal);

            // text controls
            pal = getGraphicPalette(text, "Text");
            gridbag.setConstraints(pal, constraints);
            gui.add(pal);

            // poly controls
            pal = getGraphicPalette(poly, "Poly");
            gridbag.setConstraints(pal, constraints);
            gui.add(pal);
        }
        return gui;
    }

    /**
     * Create the sub-palette for a particular graphic type.
     * 
     * @param obj GraphicObj
     * @param title panel title
     * @return JPanel sub-palette
     */
    protected JPanel getGraphicPalette(final GraphicBase obj, final String title) {

        final JComboBox jcb;
        final JFrame jframe;
        final JRootPane main;
        final JPanel parent;

        parent = PaletteHelper.createVerticalPanel(title);
        jframe = new JFrame();
        main = jframe.getRootPane();

        // different controls for different render types
        jcb = new JComboBox();
        jcb.addItem("LatLon");// indices correspond to LineType.java
        jcb.addItem("XY");
        jcb.addItem("Offset");
        jcb.setSelectedIndex(obj.rt - 1);
        jcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                obj.setRender(jcb);
                switch (obj.rt) {
                case OMGraphic.RENDERTYPE_LATLON:
                    jframe.setTitle(title + " - LatLon");
                    main.getContentPane().removeAll();
                    main.getContentPane().add(obj.getGUI());
                    jframe.pack();
                    jframe.setVisible(true);
                    break;
                case OMGraphic.RENDERTYPE_XY:
                    jframe.setTitle(title + " - XY");
                    main.getContentPane().removeAll();
                    main.getContentPane().add(obj.getGUI());
                    jframe.pack();
                    jframe.setVisible(true);
                    break;
                case OMGraphic.RENDERTYPE_OFFSET:
                    jframe.setTitle(title + " - XY Offset");
                    main.getContentPane().removeAll();
                    main.getContentPane().add(obj.getGUI());
                    jframe.pack();
                    jframe.setVisible(true);
                    break;
                default:
                    System.err.println("ARRRR!");
                    break;
                }
            }
        });
        parent.add(jcb);

        return parent;
    }

    /**
     * Returns self as the <code>MapMouseListener</code> in order to
     * receive <code>MapMouseEvent</code>s. If the implementation
     * would prefer to delegate <code>MapMouseEvent</code>s, it
     * could return the delegate from this method instead.
     * 
     * @return MapMouseListener this
     */
    public MapMouseListener getMapMouseListener() {
        return this;
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener. The source MouseEvents will only get sent to
     * the MapMouseListener if the mode is set to one that the
     * listener is interested in. Layers interested in receiving
     * events should register for receiving events in "select" mode.
     * <code>
     * <pre>
     * return new String[1] { SelectMouseMode.modeID };
     * </pre>
     * <code>
     * @see com.bbn.openmap.event.NavMouseMode#modeID
     * @see com.bbn.openmap.event.SelectMouseMode#modeID
     * @see com.bbn.openmap.event.NullMouseMode#modeID
     */
    public String[] getMouseModeServiceList() {
        return new String[] { SelectMouseMode.modeID };
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mousePressed(MouseEvent e) {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mousePressed()");
        }
        return true;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseReleased(MouseEvent e) {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mouseReleased()");
        }
        return true;
    }

    /**
     * Invoked when the mouse has been clicked on a component. The
     * listener will receive this event if it successfully processed
     * <code>mousePressed()</code>, or if no other listener
     * processes the event. If the listener successfully processes
     * mouseClicked(), then it will receive the next mouseClicked()
     * notifications that have a click count greater than one.
     * 
     * @param e MouseListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mouseClicked(MouseEvent e) {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mouseClicked()");
        }
        return true;
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseListener MouseEvent to handle.
     */
    public void mouseEntered(MouseEvent e) {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mouseEntered()");
        }
    }

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseListener MouseEvent to handle.
     */
    public void mouseExited(MouseEvent e) {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mouseExited()");
        }
    }

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged. The listener will receive these events if it
     * successfully processes mousePressed(), or if no other listener
     * processes the event.
     * 
     * @param e MouseMotionListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mouseDragged(MouseEvent e) {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mouseDragged()");
        }
        return true;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons no down).
     * 
     * @param e MouseListener MouseEvent to handle.
     * @return true if the listener was able to process the event.
     */
    public boolean mouseMoved(MouseEvent e) {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mouseMoved()");
        }
        return true;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * This event is intended to tell the listener that there was a
     * mouse movement, but that the event was consumed by another
     * layer. This will allow a mouse listener to clean up actions
     * that might have happened because of another motion event
     * response.
     */
    public void mouseMoved() {
        if (Debug.debugging("TestLayer")) {
            System.out.println("TestLayer.mouseMoved()[alt]");
        }
    }

    //////////////////////////////////////////////////////////////////

    /*
     * The GUI code is implemented here.
     */
    protected abstract class GraphicBase {
        // ll data
        protected double[] llpts = new double[4];
        protected float radius = 4000f;
        protected int type = OMGraphic.LINETYPE_GREATCIRCLE;
        protected int nsegs = 360;

        // xy data
        protected int[] xypts = new int[4];
        protected int width, height;

        // generic
        protected int lineColor = NCOLORS - 2;
        protected int fillColor = NCOLORS - 1;
        protected boolean visible = true;
        protected boolean isFilled = false;
        protected int rt = OMGraphic.RENDERTYPE_LATLON;

        // GUI code
        protected abstract JPanel getGUI();

        protected void setXYCoordinate(JTextField jtf, int i) {
            try {
                xypts[i] = Integer.parseInt(jtf.getText().trim());
            } catch (NumberFormatException ex) {
                return;
            }
        }

        protected void setLLCoordinate(JTextField jtf, int i) {
            try {
                llpts[i] = Double.valueOf(jtf.getText().trim()).doubleValue();
            } catch (NumberFormatException ex) {
                return;
            }
        }

        protected void setType(JComboBox jcb) {
            type = jcb.getSelectedIndex() + 1;
            setList(generateGraphics());
            repaint();
        }

        protected void setRender(JComboBox jcb) {
            rt = jcb.getSelectedIndex() + 1;
            setList(generateGraphics());
            repaint();
        }

        protected void setSegs(JTextField jtf) {
            try {
                nsegs = Integer.parseInt(jtf.getText().trim());
            } catch (NumberFormatException ex) {
                return;
            }
        }

        protected void makeFillCheckBox(JComponent parent) {
            JPanel pal = PaletteHelper.createCheckbox(null,
                    new String[] { "Filled" },
                    new boolean[] { isFilled },
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            isFilled = !isFilled;
                            setList(generateGraphics());
                            repaint();
                        }
                    });
            parent.add(pal);
        }

        protected void makeColorBox(JComponent parent, String title,
                                    final boolean isFill) {
            JPanel pal = PaletteHelper.createVerticalPanel(title);
            final JComboBox jcb = new JComboBox();
            for (int i = 0; i < NCOLORS; i++) {
                jcb.addItem(colorNames[i]);
            }
            jcb.setSelectedIndex(isFill ? fillColor : lineColor);
            jcb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isFill)
                        fillColor = jcb.getSelectedIndex();
                    else
                        lineColor = jcb.getSelectedIndex();
                    setList(generateGraphics());
                    repaint();
                }
            });
            pal.add(jcb);
            parent.add(pal);
        }

        // get an OK button which refreshes the display.
        protected JButton getOKButton() {
            // add reset button
            JButton jb = new JButton("OK");
            jb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setList(generateGraphics());
                    repaint();
                }
            });
            return jb;
        }
    }

    protected class Line extends GraphicBase {

        protected int arrowtype = -1;
        protected boolean arrowhead = false;

        public Line() {
            llpts[0] = 45.0;
            llpts[1] = -90.0;
            llpts[2] = 0.0;
            llpts[3] = -180.0;
            xypts[0] = 45;
            xypts[1] = 90;
            xypts[2] = 0;
            xypts[3] = 180;
            lineColor = 5;
        }

        // makes arrow head selection
        protected void makeArrowHeadGUI(JComponent parent) {
            JPanel pal;
            pal = PaletteHelper.createVerticalPanel(null);
            final JComboBox jcb = new JComboBox();
            jcb.addItem("None");
            jcb.addItem("Arrow Forward");
            jcb.addItem("Arrow Back");
            jcb.addItem("Arrow Both");
            jcb.setSelectedIndex(arrowtype + 1);
            jcb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (jcb.getSelectedIndex()) {
                    case 0:
                        System.out.println("TestLayer: no arrows");
                        omline.addArrowHead(false);
                        arrowhead = false;
                        arrowtype = -1;
                        break;
                    case 1:
                        System.out.println("TestLayer: arrows forward");
                        arrowhead = true;
                        arrowtype = OMArrowHead.ARROWHEAD_DIRECTION_FORWARD;
                        break;
                    case 2:
                        System.out.println("TestLayer: arrows backward");
                        arrowhead = true;
                        arrowtype = OMArrowHead.ARROWHEAD_DIRECTION_BACKWARD;
                        break;
                    case 3:
                        System.out.println("TestLayer: arrows both");
                        arrowhead = true;
                        arrowtype = OMArrowHead.ARROWHEAD_DIRECTION_BOTH;
                        break;
                    }
                    setList(generateGraphics());
                    repaint();
                }
            });
            pal.add(jcb);
            parent.add(pal);
        }

        public JPanel getGUI() {
            // request focus
            requestFocus();

            JTextField tf;
            JPanel pal;
            final JPanel pop;
            pop = PaletteHelper.createVerticalPanel(null);

            // add arrowheads
            if (!(this instanceof Rect))
                makeArrowHeadGUI(pop);

            if (rt == OMGraphic.RENDERTYPE_LATLON) {
                pal = PaletteHelper.createVerticalPanel(null);
                JComboBox jcb = new JComboBox();
                jcb.addItem("Straight");// indices correspond to
                                        // LineType.java
                jcb.addItem("Rhumb");
                jcb.addItem("Great Circle");
                jcb.setSelectedIndex(type - 1);
                jcb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setType((JComboBox) e.getSource());
                    }
                });
                pal.add(jcb);
                pop.add(pal);
                tf = PaletteHelper.createTextEntry("nsegs", "" + nsegs, pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setSegs((JTextField) e.getSource());
                    }
                });
            }
            if (rt != OMGraphic.RENDERTYPE_XY) {
                tf = PaletteHelper.createTextEntry("lat1", "" + llpts[0], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 0);
                    }
                });
                tf = PaletteHelper.createTextEntry("lon1", "" + llpts[1], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 1);
                    }
                });
            }

            if (rt == OMGraphic.RENDERTYPE_LATLON) {
                tf = PaletteHelper.createTextEntry("lat2", "" + llpts[2], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 2);
                    }
                });
                tf = PaletteHelper.createTextEntry("lon2", "" + llpts[3], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 3);
                    }
                });
            } else {
                tf = PaletteHelper.createTextEntry("x1", "" + xypts[0], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 0);
                    }
                });
                tf = PaletteHelper.createTextEntry("y1", "" + xypts[1], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 1);
                    }
                });
                tf = PaletteHelper.createTextEntry("x2", "" + xypts[2], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 2);
                    }
                });
                tf = PaletteHelper.createTextEntry("y2", "" + xypts[3], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 3);
                    }
                });
            }

            // line color
            makeColorBox(pop, "line", false);

            // fill color
            if (this instanceof Rect)
                makeColorBox(pop, "fill", true);

            // filled?
            if (this instanceof Rect)
                makeFillCheckBox(pop);

            // add ok button
            pop.add(getOKButton());

            return pop;
        }
    }

    protected class Rect extends Line {
        public Rect() {
            llpts[0] = -80.0;
            llpts[1] = 0.0;
            llpts[2] = 10.0;
            llpts[3] = 45.0;
            xypts[0] = 250;
            xypts[1] = 100;
            xypts[2] = 150;
            xypts[3] = 380;
            lineColor = 0;
            fillColor = 9;
            type = OMGraphic.LINETYPE_RHUMB;
        }
    }

    protected class Circle extends GraphicBase {

        public Circle() {
            xypts[0] = 10;
            xypts[1] = 20;
            width = 10;
            height = 20;
            fillColor = 0;
        }

        public JPanel getGUI() {
            // request focus
            requestFocus();

            final JPanel pop;
            JTextField tf;
            pop = PaletteHelper.createVerticalPanel(null);

            if (rt != OMGraphic.RENDERTYPE_XY) {
                tf = PaletteHelper.createTextEntry("lat", "" + llpts[0], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 0);
                    }
                });
                tf = PaletteHelper.createTextEntry("lon", "" + llpts[1], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 1);
                    }
                });
            } else {
                tf = PaletteHelper.createTextEntry("x", "" + xypts[0], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 0);
                    }
                });
                tf = PaletteHelper.createTextEntry("y", "" + xypts[1], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 1);
                    }
                });
            }

            if (rt == OMGraphic.RENDERTYPE_OFFSET) {
                tf = PaletteHelper.createTextEntry("off_x", "" + xypts[0], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 0);
                    }
                });
                tf = PaletteHelper.createTextEntry("off_y", "" + xypts[1], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 1);
                    }
                });
            } else if (rt == OMGraphic.RENDERTYPE_LATLON) {
                tf = PaletteHelper.createTextEntry("R (km)", "" + radius, pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        JTextField jtf = (JTextField) e.getSource();
                        float f = radius;
                        try {
                            f = Float.valueOf(jtf.getText().trim())
                                    .floatValue();
                        } catch (NumberFormatException ex) {
                            return;
                        }
                        radius = f;
                        System.out.println("TestLayer: radius=" + radius);
                    }
                });
                tf = PaletteHelper.createTextEntry("nverts", "" + nsegs, pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setSegs((JTextField) e.getSource());
                    }
                });
            }

            if (rt != OMGraphic.RENDERTYPE_LATLON) {
                tf = PaletteHelper.createTextEntry("width", "" + width, pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        JTextField jtf = (JTextField) e.getSource();
                        try {
                            width = Integer.parseInt(jtf.getText().trim());
                        } catch (NumberFormatException ex) {
                            return;
                        }
                    }
                });
                tf = PaletteHelper.createTextEntry("height", "" + height, pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        JTextField jtf = (JTextField) e.getSource();
                        try {
                            height = Integer.parseInt(jtf.getText().trim());
                        } catch (NumberFormatException ex) {
                            return;
                        }
                    }
                });
            }

            // line color
            makeColorBox(pop, "line", false);

            // fill color
            makeColorBox(pop, "fill", true);

            // filled?
            makeFillCheckBox(pop);

            // add ok button
            pop.add(getOKButton());

            return pop;
        }
    }

    protected class Poly extends GraphicBase {

        protected float lat = 0f;
        protected float lon = 0f;
        protected int cMode = OMPoly.COORDMODE_ORIGIN;

        public Poly() {
            llpts = new double[8];
            xypts = new int[6];
            llpts[0] = 10.0;
            llpts[1] = -20.0;
            llpts[2] = 45.0;
            llpts[3] = -70.0;
            llpts[4] = 0.0;
            llpts[5] = -90.0;
            llpts[6] = -15.0;
            llpts[7] = -40.0;
            xypts[0] = 145;
            xypts[1] = 190;
            xypts[2] = 160;
            xypts[3] = 210;
            xypts[2] = 135;
            xypts[3] = 215;
            lineColor = 4;
            fillColor = 9;
            type = OMGraphic.LINETYPE_GREATCIRCLE;
        }

        // set latlon vertices
        protected void setVertices(String verts) {
            try {
                String str;
                StringTokenizer tok = new StringTokenizer(verts, "\n\r");

                // clean out comments
                StringBuilder sb = new StringBuilder();
                while (tok.hasMoreTokens()) {
                    str = tok.nextToken().trim();
                    if (str.charAt(0) != '#') {
                        sb.append(str).append(" ");
                    }
                }

                // extract vertices
                tok = new StringTokenizer(sb.toString());
                int size = tok.countTokens();
                System.out.println("ll size=" + size);
                llpts = new double[size];
                for (int i = 0; i < size; i += 2) {
                    str = tok.nextToken();
                    System.out.print("lat=" + str);
                    llpts[i] = Double.valueOf(str).doubleValue();
                    str = tok.nextToken();
                    System.out.println(" lon=" + str);
                    llpts[i + 1] = Double.valueOf(str).doubleValue();
                }
            } catch (NumberFormatException ex) {
                return;
            }
        }

        protected void setXY(JTextArea jta) {
            try {
                if (false)
                    throw new NumberFormatException("foo");
            } catch (NumberFormatException ex) {
                return;
            }
        }

        protected void setLL(JTextArea jta) {
            setVertices(jta.getText().trim());
        }

        protected void setLLCoordinate(JTextField jtf, int i) {
            try {
                if (i == 0)
                    lat = Float.valueOf(jtf.getText().trim()).floatValue();
                else
                    lon = Float.valueOf(jtf.getText().trim()).floatValue();
            } catch (NumberFormatException ex) {
                return;
            }
        }

        public JPanel getGUI() {
            // request focus
            requestFocus();

            final JPanel pop;
            JPanel pal;
            JTextField tf;
            JTextArea ta;
            pop = PaletteHelper.createVerticalPanel(null);

            if (rt == OMGraphic.RENDERTYPE_LATLON) {
                pal = PaletteHelper.createVerticalPanel(null);
                JComboBox jcb = new JComboBox();
                jcb.addItem("Straight");// indices correspond to
                                        // LineType.java
                jcb.addItem("Rhumb");
                jcb.addItem("Great Circle");
                jcb.setSelectedIndex(type - 1);
                jcb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setType((JComboBox) e.getSource());
                    }
                });
                pal.add(jcb);
                pop.add(pal);
                tf = PaletteHelper.createTextEntry("nsegs", "" + nsegs, pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setSegs((JTextField) e.getSource());
                    }
                });
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < llpts.length; i += 2) {
                    sb.append(llpts[i]).append(" ").append(llpts[i + 1]).append("\n");
                }
                ta = PaletteHelper.createTextArea("llpts", sb.toString(), pop, 5, 8);
                ta.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLL((JTextArea) e.getSource());
                    }
                });
            } else {
                if (rt == OMGraphic.RENDERTYPE_OFFSET) {
                    tf = PaletteHelper.createTextEntry("lat",
                            "" + llpts[0],
                            pop);
                    tf.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent e) {
                            setLLCoordinate((JTextField) e.getSource(), 0);
                        }
                    });
                    tf = PaletteHelper.createTextEntry("lon",
                            "" + llpts[1],
                            pop);
                    tf.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent e) {
                            setLLCoordinate((JTextField) e.getSource(), 1);
                        }
                    });
                }
                StringBuilder entry = new StringBuilder();
                for (int i = 0; i < xypts.length; i += 2) {
                    entry.append(xypts[i]).append(" ").append(xypts[i + 1]).append("\n");
                }
                ta = PaletteHelper.createTextArea("xypts", entry.toString(), pop, 0, 0);
                ta.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXY((JTextArea) e.getSource());
                    }
                });
            }

            // line color
            makeColorBox(pop, "line", false);

            // fill color
            makeColorBox(pop, "fill", true);

            // filled?
            makeFillCheckBox(pop);

            // add ok button
            pop.add(getOKButton());

            return pop;
        }
    }

    protected class Text extends GraphicBase {
        protected String data;
        protected String font = "SansSerif-Bold-18";
        protected int just = OMText.JUSTIFY_CENTER;

        public Text() {
            llpts[0] = 42.35;
            llpts[1] = -70.5;
            xypts[0] = 20;
            xypts[1] = 10;
            lineColor = 10;
            data = "Boston";
        }

        public JPanel getGUI() {
            // request focus
            requestFocus();

            final JPanel pop;
            JPanel pal;
            JTextField tf;
            final JComboBox jcb;
            pop = PaletteHelper.createVerticalPanel(null);

            tf = PaletteHelper.createTextEntry("text", data, pop);
            tf.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    data = ((JTextField) (e.getSource())).getText().trim();
                }
            });

            tf = PaletteHelper.createTextEntry("font", font, pop);
            tf.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    font = ((JTextField) (e.getSource())).getText().trim();
                }
            });

            pal = PaletteHelper.createVerticalPanel(null);
            jcb = new JComboBox();
            jcb.addItem("right");// indices correspond to values in
                                 // OMText
            jcb.addItem("center");
            jcb.addItem("left");
            jcb.setSelectedIndex(just);
            jcb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    just = jcb.getSelectedIndex();
                    setList(generateGraphics());
                    repaint();
                }
            });
            pal.add(jcb);
            pop.add(pal);

            if (rt != OMGraphic.RENDERTYPE_XY) {
                tf = PaletteHelper.createTextEntry("lat", "" + llpts[0], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 0);
                    }
                });
                tf = PaletteHelper.createTextEntry("lon", "" + llpts[1], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setLLCoordinate((JTextField) e.getSource(), 1);
                    }
                });
            }

            if (rt != OMGraphic.RENDERTYPE_LATLON) {
                tf = PaletteHelper.createTextEntry("off_x", "" + xypts[0], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 0);
                    }
                });
                tf = PaletteHelper.createTextEntry("off_y", "" + xypts[1], pop);
                tf.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent e) {
                        setXYCoordinate((JTextField) e.getSource(), 1);
                    }
                });
            }

            // line color
            makeColorBox(pop, "text", false);

            // add ok button
            pop.add(getOKButton());

            return pop;
        }
    }
}