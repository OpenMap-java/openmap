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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMText.java,v $
// $RCSfile: EditableOMText.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:06:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.layer.util.stateMachine.State;
import com.bbn.openmap.omGraphics.editable.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;

import java.awt.Insets;
import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;

/**
 * Wrapper class to edit OMText objects.  This component is used by the OMDrawingTool.
 */
public class EditableOMText extends EditableOMGraphic implements ActionListener {

    protected GrabPoint gpc;
    protected OffsetGrabPoint gpo; // offset

    protected OMText text;

    public final static int CENTER_POINT_INDEX = 0;
    public final static int OFFSET_POINT_INDEX = 1;

    /**
     * Create the EditableOMText, setting the state machine to create
     * the point off of the gestures.
     */
    public EditableOMText() {
        createGraphic(null);
    }

    /**
     * Create an EditableOMText with the pointType and renderType
     * parameters in the GraphicAttributes object.
     */
    public EditableOMText(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableOMText with an OMText already defined, ready
     * for editing.
     * 
     * @param omc OMText that should be edited.
     */
    public EditableOMText(OMText omc) {
        setGraphic(omc);
    }

    /**
     * Create and initialize the state machine that interprets the
     * modifying gestures/commands, as well as ititialize the grab
     * points. Also allocates the grab point array needed by the
     * EditableOMText.
     */
    public void init() {
        setCanGrabGraphic(false);
        setStateMachine(new TextStateMachine(this));
        gPoints = new GrabPoint[2];
    }

    /**
     * Set the graphic within the state machine. If the graphic is
     * null, then one shall be created, and located off screen until
     * the gestures driving the state machine place it on the map.
     */
    public void setGraphic(OMGraphic graphic) {
        init();
        if (graphic instanceof OMText) {
            text = (OMText) graphic;
            stateMachine.setSelected();
            setGrabPoints(text);
        } else {
            createGraphic(null);
        }
    }

    /**
     * Create and set the graphic within the state machine. The
     * GraphicAttributes describe the type of point to create.
     */
    public void createGraphic(GraphicAttributes ga) {
        init();
        stateMachine.setUndefined();
        int renderType = OMGraphic.RENDERTYPE_UNKNOWN;

        if (ga != null) {
            renderType = ga.getRenderType();
        }

        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON):
            text = new OMText(90f, -180f, "Text", OMText.JUSTIFY_LEFT);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET):
            text = new OMText(90f, -180f, 0, 0, "Text", OMText.JUSTIFY_LEFT);
            break;
        default:
            text = new OMText(0, 0, "Text", OMText.JUSTIFY_LEFT);
        }

        if (ga != null) {
            ga.setTo(text);
            text.setLinePaint(ga.getLinePaint());
        }

        assertGrabPoints();
    }

    /**
     * Get the OMGraphic being created/modified by the EditableOMText.
     */
    public OMGraphic getGraphic() {
        return text;
    }

    /**
     * Attach to the Moving OffsetGrabPoint so if it moves, it will
     * move this EditableOMGraphic with it. EditableOMGraphic version
     * doesn't do anything, each subclass has to decide which of its
     * OffsetGrabPoints should be attached to it.
     */
    public void attachToMovingGrabPoint(OffsetGrabPoint gp) {
        gp.addGrabPoint(gpo);
    }

    /**
     * Detach from a Moving OffsetGrabPoint. The EditableOMGraphic
     * version doesn't do anything, each subclass should remove
     * whatever GrabPoint it would have attached to an
     * OffsetGrabPoint.
     */
    public void detachFromMovingGrabPoint(OffsetGrabPoint gp) {
        gp.removeGrabPoint(gpo);
    }

    /**
     * Set the GrabPoint that is in the middle of being modified, as a
     * result of a mouseDragged event, or other selection process.
     */
    public void setMovingPoint(GrabPoint gp) {
        super.setMovingPoint(gp);
    }

    /**
     * Given a MouseEvent, find a GrabPoint that it is touching, and
     * set the moving point to that GrabPoint.
     * 
     * @param e MouseEvent
     * @return GrabPoint that is touched by the MouseEvent, null if
     *         none are.
     */
    public GrabPoint getMovingPoint(MouseEvent e) {

        movingPoint = null;
        GrabPoint[] gb = getGrabPoints();
        int x = e.getX();
        int y = e.getY();

        for (int i = gb.length - 1; i >= 0; i--) {

            if (gb[i] != null && gb[i].distance(x, y) == 0) {

                setMovingPoint(gb[i]);
                // in case the points are on top of each other, the
                // last point in the array will take precidence.
                break;
            }
        }
        return movingPoint;
    }

    protected int lastRenderType = -1;

    /**
     * Check to make sure the grab points are not null. If they are,
     * allocate them, and them assign them to the array.
     */
    public void assertGrabPoints() {
        int rt = getGraphic().getRenderType();
        if (rt != lastRenderType) {
            clearGrabPoints();
            lastRenderType = rt;
        }

        if (gpc == null) {
            gpc = new GrabPoint(-1, -1);
            gPoints[CENTER_POINT_INDEX] = gpc;
        }

        if (gpo == null) {
            gpo = new OffsetGrabPoint(-1, -1);
            gPoints[OFFSET_POINT_INDEX] = gpo;
            gpo.addGrabPoint(gpc);
        }
    }

    protected void clearGrabPoints() {

        gpc = null;
        gpo = null;

        gPoints[CENTER_POINT_INDEX] = gpc;
        gPoints[OFFSET_POINT_INDEX] = gpo;
    }

    /**
     * Set the grab points for the graphic provided, setting them on
     * the extents of the graphic. Called when you want to set the
     * grab points off the location of the graphic.
     */
    public void setGrabPoints(OMGraphic graphic) {
        if (!(graphic instanceof OMText)) {
            return;
        }
        assertGrabPoints();

        OMText text = (OMText) graphic;
        boolean ntr = text.getNeedToRegenerate();
        int renderType = text.getRenderType();
        int lineType = text.getLineType();

        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        LatLonPoint llp;
        int latoffset = 0;
        int lonoffset = 0;

        boolean doStraight = true;

        if (ntr == false) {

            if (renderType == OMGraphic.RENDERTYPE_LATLON
                    || renderType == OMGraphic.RENDERTYPE_OFFSET) {

                if (projection != null) {
                    float lon = text.getLon();
                    float lat = text.getLat();

                    llp = new LatLonPoint(lat, lon);
                    java.awt.Point p = projection.forward(llp);
                    if (renderType == OMGraphic.RENDERTYPE_LATLON) {
                        doStraight = false;
                        gpc.set((int) p.getX(), (int) p.getY());
                    } else {
                        latoffset = (int) p.getY();
                        lonoffset = (int) p.getX();
                        gpo.set(lonoffset, latoffset);
                    }
                }
            }

            if (doStraight) {
                gpc.set(lonoffset + text.getX(), latoffset + text.getY());
            }

            if (renderType == OMGraphic.RENDERTYPE_OFFSET) {
                gpo.updateOffsets();
            }

        } else {
            System.out.println("EditableOMText.setGrabPoint: graphic needs to be regenerated");
        }
    }

    /**
     * Take the current location of the GrabPoints, and modify the
     * location parameters of the OMPoint with them. Called when you
     * want the graphic to change according to the grab points.
     */
    public void setGrabPoints() {

        int renderType = text.getRenderType();
        LatLonPoint llp1;

        Debug.message("eomt", "EditableOMText.setGrabPoints()");

        // Do center point for lat/lon or offset points
        if (renderType == OMGraphic.RENDERTYPE_LATLON) {

            if (projection != null) {
                //movingPoint == gpc
                llp1 = projection.inverse(gpc.getX(), gpc.getY());
                text.setLat(llp1.getLatitude());
                text.setLon(llp1.getLongitude());
                // text.setNeedToRegenerate set
            }
        }

        boolean settingOffset = getStateMachine().getState() instanceof GraphicSetOffsetState
                && movingPoint == gpo;

        // If the center point is moving, the offset distance changes
        if (renderType == OMGraphic.RENDERTYPE_OFFSET) {

            llp1 = projection.inverse(gpo.getX(), gpo.getY());

            text.setLat(llp1.getLatitude());
            text.setLon(llp1.getLongitude());

            if (settingOffset || movingPoint == gpc) {
                // Don't call point.setLocation because we only want
                // to
                // setNeedToRegenerate if !settingOffset.
                text.setX(gpc.getX() - gpo.getX());
                text.setY(gpc.getY() - gpo.getY());
            }

            if (!settingOffset) {
                text.setX(gpc.getX() - gpo.getX());
                text.setY(gpc.getY() - gpo.getY());
            }

            // Set Location has reset the rendertype, but provides
            // the convenience of setting the max and min values
            // for us.
            text.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        }

        // Do the point height and width for XY and OFFSET render
        // types.
        if (renderType == OMGraphic.RENDERTYPE_XY) {

            if (movingPoint == gpc) {
                text.setX(gpc.getX());
                text.setY(gpc.getY());
            }
        }

        if (projection != null) {
            regenerate(projection);
        }

    }

    /**
     * Get whether a graphic can be manipulated by its edges, rather
     * than just by its grab points.
     */
    public boolean getCanGrabGraphic() {
        return false;
    }

    /**
     * Called to set the OffsetGrabPoint to the current mouse
     * location, and update the OffsetGrabPoint with all the other
     * GrabPoint locations, so everything can shift smoothly. Should
     * also set the OffsetGrabPoint to the movingPoint. Should be
     * called only once at the beginning of the general movement, in
     * order to set the movingPoint. After that, redraw(e) should just
     * be called, and the movingPoint will make the adjustments to the
     * graphic that are needed.
     */
    public void move(java.awt.event.MouseEvent e) {}

    /**
     * Use the current projection to place the graphics on the screen.
     * Has to be called to at least assure the graphics that they are
     * ready for rendering. Called when the graphic position changes.
     * 
     * @param proj com.bbn.openmap.proj.Projection
     * @return true
     */
    public boolean generate(Projection proj) {
        if (text != null)
            text.regenerate(proj);
        for (int i = 0; i < gPoints.length; i++) {
            GrabPoint gp = gPoints[i];
            if (gp != null) {
                gp.generate(proj);
            }
        }
        return true;
    }

    /**
     * Given a new projection, the grab points may need to be
     * repositioned off the current position of the graphic. Called
     * when the projection changes.
     */
    public void regenerate(Projection proj) {
        if (text != null)
            text.regenerate(proj);

        setGrabPoints(text);
        generate(proj);
    }

    /**
     * Draw the EditableOMtext parts into the java.awt.Graphics
     * object. The grab points are only rendered if the point machine
     * state is TextSelectedState.TEXT_SELECTED.
     * 
     * @param graphics java.awt.Graphics.
     */
    public void render(java.awt.Graphics graphics) {

        State state = getStateMachine().getState();

        if (!(state instanceof GraphicUndefinedState)) {
            if (text != null) {
                text.setVisible(true);
                text.render(graphics);
                text.setVisible(false);
            } else {
                Debug.message("eomg", "EditableOMText.render: null point.");
            }

            int renderType = text.getRenderType();

            if (state instanceof GraphicSelectedState
                    || state instanceof GraphicEditState) {

                for (int i = 0; i < gPoints.length; i++) {
                    GrabPoint gp = gPoints[i];
                    if (gp != null) {
                        if ((i == OFFSET_POINT_INDEX
                                && renderType == OMGraphic.RENDERTYPE_OFFSET && movingPoint == gpo)
                                ||

                                (state instanceof GraphicSelectedState && ((i != OFFSET_POINT_INDEX && renderType != OMGraphic.RENDERTYPE_OFFSET) || (renderType == OMGraphic.RENDERTYPE_OFFSET)))

                        ) {

                            gp.setVisible(true);
                            gp.render(graphics);
                            gp.setVisible(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * If this EditableOMGraphic has parameters that can be
     * manipulated that are independent of other EditableOMGraphic
     * types, then you can provide the widgets to control those
     * parameters here. By default, returns the GraphicAttributes GUI
     * widgets. If you don't want a GUI to appear when a widget is
     * being created/edited, then don't call this method from the
     * EditableOMGraphic implementation, and return a null Component
     * from getGUI.
     * 
     * @param graphicAttributes the GraphicAttributes to use to get
     *        the GUI widget from to control those parameters for this
     *        EOMG.
     * @return java.awt.Component to use to control parameters for
     *         this EOMG.
     */
    public java.awt.Component getGUI(GraphicAttributes graphicAttributes) {
        Debug.message("eomg", "EditableOMPoly.getGUI");
        if (graphicAttributes != null) {
            Component gaGUI = graphicAttributes.getGUI();
            ((JComponent) gaGUI).add(getTextGUI());
            return gaGUI;
        } else {
            return getTextGUI();
        }
    }

    JComboBox sizesFont;
    JComboBox styleFont;

    /** Command for text string adjustments. */
    public final static String TextFieldCommand = "TextField";
    public final static String TextFontCommand = "TextFont";
    public final static String TextRotationCommand = "TextRotation";

    protected java.awt.Component getTextGUI() {
        javax.swing.Box attributeBox = javax.swing.Box.createHorizontalBox();

        attributeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        attributeBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        String textString = "Text";
        if (text != null) {
            textString = text.getData();
        }

        JTextField textField = new JTextField(textString, 25);
        textField.setActionCommand(TextFieldCommand);
        textField.addActionListener(this);
        textField.setMargin(new Insets(0, 1, 0, 1));
        textField.setMinimumSize(new java.awt.Dimension(100, 20));
        textField.setPreferredSize(new java.awt.Dimension(100, 20));
        attributeBox.add(textField);

        //         JPanel palette =
        // PaletteHelper.createHorizontalPanel("Rotation");
        javax.swing.Box palette = javax.swing.Box.createHorizontalBox();
        textField = new JTextField(Integer.toString((int) (text.getRotationAngle() * 180 / Math.PI)), 5);
        textField.setActionCommand(TextRotationCommand);
        textField.setToolTipText("Text rotation in degrees");
        textField.setMargin(new Insets(0, 1, 0, 1));
        textField.addActionListener(this);
        textField.setMinimumSize(new java.awt.Dimension(30, 20));
        textField.setPreferredSize(new java.awt.Dimension(30, 20));
        palette.add(textField);
        palette.add(new JLabel("\u00b0 "));
        attributeBox.add(palette);

        String[] sizesStrings = { "3", "5", "8", "10", "12", "14", "18", "20",
                "24", "36", "48" };
        sizesFont = new JComboBox(sizesStrings);
        sizesFont.setToolTipText("Font Size");
        sizesFont.setSelectedItem("" + (text.getFont()).getSize());
        sizesFont.setActionCommand(TextFontCommand);
        sizesFont.addActionListener(this);

        String[] styleStrings = { "Plain", "Bold", "Italic", "Bold Italic" };
        styleFont = new JComboBox(styleStrings);
        styleFont.setToolTipText("Font Style");
        if ((text.getFont().isBold()) && (text.getFont().isItalic()))
            styleFont.setSelectedIndex(3);
        else if (text.getFont().isBold())
            styleFont.setSelectedIndex(1);
        else if (text.getFont().isItalic())
            styleFont.setSelectedIndex(2);
        else
            styleFont.setSelectedIndex(0);
        styleFont.setActionCommand(TextFontCommand);
        styleFont.addActionListener(this);

        attributeBox.add(sizesFont);
        attributeBox.add(styleFont);

        return attributeBox;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String command = e.getActionCommand();

        if (command == TextFontCommand) {
            String FontString = OMText.fontToXFont(text.getFont());
            FontString = FontString.substring(0, FontString.indexOf("-", 3));
            StringBuffer ret = new StringBuffer(FontString);
            if ((styleFont.getSelectedIndex() == 1)
                    || (styleFont.getSelectedIndex() == 3))
                ret.append("-bold");
            else
                ret.append("-normal");
            if (styleFont.getSelectedIndex() > 1)
                ret.append("-i");
            else
                ret.append("-o");
            ret.append("-normal");
            ret.append("--" + sizesFont.getSelectedItem());
            ret.append("-*-*-*-*-*-*");
            ret.toString();
            text.setFont(OMText.rebuildFont(ret.toString()));
            repaint();
        } else if (command == TextFieldCommand) {
            text.setData(((JTextField) source).getText());
            text.regenerate(projection);
            repaint();
        } else if (command == TextRotationCommand) {
            Integer rotation = new Integer(((JTextField) source).getText());
            text.setRotationAngle(Math.PI / 180 * (rotation.intValue() % 360));
            text.regenerate(projection);
            repaint();
        }
    }
}