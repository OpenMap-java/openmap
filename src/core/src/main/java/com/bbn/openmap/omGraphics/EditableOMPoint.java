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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMPoint.java,v $
// $RCSfile: EditableOMPoint.java,v $
// $Revision: 1.16 $
// $Date: 2009/02/25 22:34:03 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import com.bbn.openmap.gui.GridBagToolBar;
import com.bbn.openmap.omGraphics.editable.GraphicEditState;
import com.bbn.openmap.omGraphics.editable.GraphicSelectedState;
import com.bbn.openmap.omGraphics.editable.GraphicSetOffsetState;
import com.bbn.openmap.omGraphics.editable.GraphicUndefinedState;
import com.bbn.openmap.omGraphics.editable.PointStateMachine;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

/**
 * An EditableOMGraphic that encapsulates an OMPoint.
 */
public class EditableOMPoint extends EditableOMGraphic {

    protected GrabPoint gpc;
    protected OffsetGrabPoint gpo; // offset

    protected OMPoint point;

    public final static String OffsetResetCmd = "OffsetResetCmd";
    public final static int CENTER_POINT_INDEX = 0;
    public final static int OFFSET_POINT_INDEX = 1;

    /**
     * Create the EditableOMPoint, setting the state machine to create the point
     * off of the gestures.
     */
    public EditableOMPoint() {
        createGraphic(null);
    }

    /**
     * Create an EditableOMPoint with the pointType and renderType parameters in
     * the GraphicAttributes object.
     */
    public EditableOMPoint(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableOMPoint with an OMPoint already defined, ready for
     * editing.
     * 
     * @param omc OMPoint that should be edited.
     */
    public EditableOMPoint(OMPoint omc) {
        setGraphic(omc);
    }

    /**
     * Create and initialize the state machine that interprets the modifying
     * gestures/commands, as well as initialize the grab points. Also allocates
     * the grab point array needed by the EditableOMPoint.
     */
    public void init() {
        Debug.message("eomg", "EditableOMPoint.init()");
        setCanGrabGraphic(false);
        setStateMachine(new PointStateMachine(this));
        gPoints = new GrabPoint[2];
    }

    /**
     * Set the graphic within the state machine. If the graphic is null, then
     * one shall be created, and located off screen until the gestures driving
     * the state machine place it on the map.
     */
    public void setGraphic(OMGraphic graphic) {
        init();
        if (graphic instanceof OMPoint) {
            point = (OMPoint) graphic;
            stateMachine.setSelected();
            setGrabPoints(point);
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

        if (Debug.debugging("eomg")) {
            Debug.output("EditableOMPoint.createGraphic(): rendertype = " + renderType);
        }

        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON):
            point = new OMPoint(90f, -180f);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET):
            point = new OMPoint(90f, -180f, 0, 0);
            break;
        default:
            point = new OMPoint(-1, -1);
        }

        if (ga != null) {
            ga.setTo(point);
        }

        assertGrabPoints();
    }

    /**
     * Get the OMGraphic being created/modified by the EditableOMPoint.
     */
    public OMGraphic getGraphic() {
        return point;
    }

    /**
     * Set the GrabPoint that is in the middle of being modified, as a result of
     * a mouseDragged event, or other selection process.
     */
    // public void setMovingPoint(GrabPoint gp) {
    // super.setMovingPoint(gp);
    // }

    /**
     * Given a MouseEvent, find a GrabPoint that it is touching, and set the
     * moving point to that GrabPoint.
     * 
     * @param e MouseEvent
     * @return GrabPoint that is touched by the MouseEvent, null if none are.
     */
    public GrabPoint getMovingPoint(MouseEvent e) {

        movingPoint = null;
        GrabPoint[] gb = getGrabPoints();

        Point2D pnt = getProjectionPoint(e);
        double x = pnt.getX();
        double y = pnt.getY();

        for (int i = gb.length - 1; i >= 0; i--) {

            if (gb[i] != null && gb[i].distance(x, y) == 0) {

                setMovingPoint(gb[i]);
                // in case the points are on top of each other, the
                // last point in the array will take precedence.
                break;
            }
        }
        return movingPoint;
    }

    /**
     * Attach to the Moving OffsetGrabPoint so if it moves, it will move this
     * EditableOMGraphic with it. EditableOMGraphic version doesn't do anything,
     * each subclass has to decide which of its OffsetGrabPoints should be
     * attached to it.
     */
    public void attachToMovingGrabPoint(OffsetGrabPoint gp) {
        gp.addGrabPoint(gpo);
    }

    /**
     * Detach from a Moving OffsetGrabPoint. The EditableOMGraphic version
     * doesn't do anything, each subclass should remove whatever GrabPoint it
     * would have attached to an OffsetGrabPoint.
     */
    public void detachFromMovingGrabPoint(OffsetGrabPoint gp) {
        gp.removeGrabPoint(gpo);
    }

    protected int lastRenderType = -1;

    /**
     * Check to make sure the grab points are not null. If they are, allocate
     * them, and them assign them to the array.
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
     * Set the grab points for the graphic provided, setting them on the extents
     * of the graphic. Called when you want to set the grab points off the
     * location of the graphic.
     */
    public void setGrabPoints(OMGraphic graphic) {
        Debug.message("eomg", "EditableOMPoint.setGrabPoints(graphic)");
        if (!(graphic instanceof OMPoint)) {
            return;
        }

        assertGrabPoints();

        OMPoint point = (OMPoint) graphic;
        boolean ntr = point.getNeedToRegenerate();
        int renderType = point.getRenderType();

        LatLonPoint llp;
        int latoffset = 0;
        int lonoffset = 0;

        boolean doStraight = true;

        if (ntr == false) {

            if (renderType == OMGraphic.RENDERTYPE_LATLON
                    || renderType == OMGraphic.RENDERTYPE_OFFSET) {

                if (projection != null) {
                    double lon = point.getLon();
                    double lat = point.getLat();

                    llp = new LatLonPoint.Double(lat, lon);
                    Point2D p = projection.forward(llp);
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
                gpc.set(lonoffset + point.getX(), latoffset + point.getY());
            }

            if (renderType == OMGraphic.RENDERTYPE_OFFSET) {
                gpo.updateOffsets();
            }

        } else {
            Debug.message("eomg", "EditableOMPoint.setGrabPoints: graphic needs to be regenerated");
        }
    }

    /**
     * Take the current location of the GrabPoints, and modify the location
     * parameters of the OMPoint with them. Called when you want the graphic to
     * change according to the grab points.
     */
    public void setGrabPoints() {

        int renderType = point.getRenderType();
        LatLonPoint llp1;

        Debug.message("eomg", "EditableOMPoint.setGrabPoints()");

        // Do center point for lat/lon or offset points
        if (renderType == OMGraphic.RENDERTYPE_LATLON) {

            if (projection != null) {
                // movingPoint == gpc
                llp1 = (LatLonPoint) projection.inverse(gpc.getX(), gpc.getY(), new LatLonPoint.Double());
                point.set(llp1.getY(), llp1.getX());
                // point.setNeedToRegenerate set
            }
        }

        boolean settingOffset = getStateMachine().getState() instanceof GraphicSetOffsetState
                && movingPoint == gpo;

        // If the center point is moving, the offset distance changes
        if (renderType == OMGraphic.RENDERTYPE_OFFSET) {

            llp1 = (LatLonPoint) projection.inverse(gpo.getX(), gpo.getY(), new LatLonPoint.Double());

            point.setLat(llp1.getY());
            point.setLon(llp1.getX());

            if (settingOffset || movingPoint == gpc) {
                // Don't call point.setLocation because we only want
                // to
                // setNeedToRegenerate if !settingOffset.
                point.setX(gpc.getX() - gpo.getX());
                point.setY(gpc.getY() - gpo.getY());
            }

            if (!settingOffset) {
                Debug.message("eomg", "EditableOMPoint: updating offset point");
                point.set(gpc.getX() - gpo.getX(), gpc.getY() - gpo.getY());
            }

            // Set Location has reset the rendertype, but provides
            // the convenience of setting the max and min values
            // for us.
            point.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        }

        // Do the point height and width for XY and OFFSET render
        // types.
        if (renderType == OMGraphic.RENDERTYPE_XY) {
            Debug.message("eomg", "EditableOMPoint: updating x/y point");

            if (movingPoint == gpc) {
                point.set(gpc.getX(), gpc.getY());
            }
        }

        if (projection != null) {
            regenerate(projection);
        }
    }

    /**
     * Get whether a graphic can be manipulated by its edges, rather than just
     * by its grab points.
     */
    public boolean getCanGrabGraphic() {
        return false;
    }

    /**
     * Called to set the OffsetGrabPoint to the current mouse location, and
     * update the OffsetGrabPoint with all the other GrabPoint locations, so
     * everything can shift smoothly. Should also set the OffsetGrabPoint to the
     * movingPoint. Should be called only once at the beginning of the general
     * movement, in order to set the movingPoint. After that, redraw(e) should
     * just be called, and the movingPoint will make the adjustments to the
     * graphic that are needed.
     */
    public void move(java.awt.event.MouseEvent e) {
    }

    /**
     * Use the current projection to place the graphics on the screen. Has to be
     * called to at least assure the graphics that they are ready for rendering.
     * Called when the graphic position changes.
     * 
     * @param proj com.bbn.openmap.proj.Projection
     * @return true
     */
    public boolean generate(Projection proj) {
        Debug.message("eomgdetail", "EditableOMPoint.generate()");
        if (point != null)
            point.generate(proj);

        for (int i = 0; i < gPoints.length; i++) {
            GrabPoint gp = gPoints[i];
            if (gp != null) {
                gp.generate(proj);
            }
        }
        return true;
    }

    /**
     * Given a new projection, the grab points may need to be repositioned off
     * the current position of the graphic. Called when the projection changes.
     */
    public void regenerate(Projection proj) {
        Debug.message("eomg", "EditableOMPoint.regenerate()");
        if (point != null)
            point.generate(proj);

        setGrabPoints(point);
        generate(proj);
    }

    /**
     * Draw the EditableOMPoint parts into the java.awt.Graphics object. The
     * grab points are only rendered if the point machine state is
     * PointSelectedState.POINT_SELECTED.
     * 
     * @param graphics java.awt.Graphics.
     */
    public void render(java.awt.Graphics graphics) {
        Debug.message("eomgdetail", "EditableOMPoint.render()");

        if (point == null) {
            Debug.message("eomg", "EditableOMPoint.render: null point.");
            return;
        }

        State state = getStateMachine().getState();

        if (!(state instanceof GraphicUndefinedState)) {
            point.setVisible(true);
            point.render(graphics);
            point.setVisible(false);

            int renderType = point.getRenderType();

            if (state instanceof GraphicSelectedState || state instanceof GraphicEditState) {

                for (int i = 0; i < gPoints.length; i++) {

                    GrabPoint gp = gPoints[i];
                    if (gp != null) {
                        if ((i == OFFSET_POINT_INDEX && renderType == OMGraphic.RENDERTYPE_OFFSET && movingPoint == gpo)
                                || (state instanceof GraphicSelectedState && ((i != OFFSET_POINT_INDEX && renderType != OMGraphic.RENDERTYPE_OFFSET) || (renderType == OMGraphic.RENDERTYPE_OFFSET)))

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
     * Modifies the gui to not include line type adjustments, and adds widgets
     * to control point settings.
     * 
     * @param graphicAttributes the GraphicAttributes to use to get the GUI
     *        widget from to control those parameters for this EOMG.
     * @return java.awt.Component to use to control parameters for this EOMG.
     */
    public Component getGUI(GraphicAttributes graphicAttributes) {
        Debug.message("eomg", "EditableOMPoint.getGUI");
        if (graphicAttributes != null) {
            // JComponent panel = graphicAttributes.getColorAndLineGUI();
            JComponent panel = createAttributePanel(graphicAttributes);
            panel.add(getPointGUI());
            return panel;
        } else {
            return getPointGUI();
        }
    }

    protected JToolBar pToolBar = null;

    protected JToolBar getPointGUI() {
        if (pToolBar == null) {
            pToolBar = new GridBagToolBar();
            // Add buttons to toggle oval/rect, radius of point.
        }

        return pToolBar;
    }
}
