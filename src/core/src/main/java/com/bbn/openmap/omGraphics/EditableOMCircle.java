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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMCircle.java,v $
// $RCSfile: EditableOMCircle.java,v $
// $Revision: 1.12 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.editable.CircleStateMachine;
import com.bbn.openmap.omGraphics.editable.GraphicEditState;
import com.bbn.openmap.omGraphics.editable.GraphicSelectedState;
import com.bbn.openmap.omGraphics.editable.GraphicSetOffsetState;
import com.bbn.openmap.proj.DrawUtil;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

/**
 * The EditableOMCircle encompasses an OMCircle, providing methods for modifying
 * or creating it. This class only modifies circles in lat/lon space
 * (RENDERTYPE_LATLON) - and ellipses in screen space (RENDERTYPE_XY or
 * RENDERTYPE_OFFSET). When you grab at the circle, you change the radius of the
 * entire circle. Grabbing the center point moves the circle. If there is an
 * offset point, moving the center point changes the circle's position in
 * relation to the offset point. Moving the offset point moves the circle,
 * keeping the distance to the center point constant.
 */
public class EditableOMCircle extends EditableOMGraphic {

    protected VerticalGrabPoint gpn;
    protected HorizontalGrabPoint gpw;
    protected VerticalGrabPoint gps;
    protected HorizontalGrabPoint gpe;
    protected GrabPoint gpnw;
    protected GrabPoint gpne;
    protected GrabPoint gpsw;
    protected GrabPoint gpse;
    protected OffsetGrabPoint gpc;
    protected GrabPoint gpr;
    protected OffsetGrabPoint gpo; // offset
    protected OffsetGrabPoint gpm; // for grabbing the circle and
    // changing the radius during creation.

    protected OMCircle circle;

    public final static int CENTER_POINT_INDEX = 0;
    public final static int NW_POINT_INDEX = 1;
    public final static int N_POINT_INDEX = 2;
    public final static int NE_POINT_INDEX = 3;
    public final static int W_POINT_INDEX = 4;
    public final static int E_POINT_INDEX = 5;
    public final static int SW_POINT_INDEX = 6;
    public final static int S_POINT_INDEX = 7;
    public final static int SE_POINT_INDEX = 8;
    public final static int RADIUS_POINT_INDEX = 9;
    public final static int OFFSET_POINT_INDEX = 10;

    /**
     * Create the EditableOMCircle, setting the state machine to create the
     * circle off of the gestures.
     */
    public EditableOMCircle() {
        createGraphic(null);
    }

    /**
     * Create an EditableOMCircle with the circleType and renderType parameters
     * in the GraphicAttributes object.
     */
    public EditableOMCircle(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableOMCircle with an OMCircle already defined, ready for
     * editing.
     * 
     * @param omc OMCircle that should be edited.
     */
    public EditableOMCircle(OMCircle omc) {
        setGraphic(omc);
    }

    /**
     * Create and initialize the state machine that interprets the modifying
     * gestures/commands, as well as initialize the grab points. Also allocates
     * the grab point array needed by the EditableOMCircle.
     */
    public void init() {
        setCanGrabGraphic(false);
        Debug.message("eomg", "EditableOMCircle.init()");
        setStateMachine(new CircleStateMachine(this));
        gPoints = new GrabPoint[11];
    }

    /**
     * Set the graphic within the state machine. If the graphic is null, then
     * one shall be created, and located off screen until the gestures driving
     * the state machine place it on the map.
     */
    public void setGraphic(OMGraphic graphic) {
        init();
        if (graphic instanceof OMCircle) {
            circle = (OMCircle) graphic;
            stateMachine.setSelected();
            setGrabPoints(circle);
        } else {
            createGraphic(null);
        }
    }

    /**
     * Create and set the graphic within the state machine. The
     * GraphicAttributes describe the type of circle to create.
     */
    public void createGraphic(GraphicAttributes ga) {
        init();
        stateMachine.setUndefined();
        int renderType = OMGraphic.RENDERTYPE_UNKNOWN;

        if (ga != null) {
            renderType = ga.getRenderType();
        }

        if (Debug.debugging("eomc")) {
            Debug.output("EditableOMCircle.createGraphic(): rendertype = " + renderType);
        }

        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON):
            circle = new OMCircle(90f, -180f, 0f);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET):
            circle = new OMCircle(90f, -180f, 0, 0, 1, 1);
            break;
        default:
            circle = new OMCircle(-1, -1, 1, 1);
        }

        if (ga != null) {
            ga.setTo(circle, true);
        }
    }

    /**
     * Get whether a graphic can be manipulated by its edges, rather than just
     * by its grab points.
     */
    public boolean getCanGrabGraphic() {
        return canGrabGraphic || circle.renderType == OMGraphic.RENDERTYPE_LATLON;
    }

    /**
     * Get the OMGraphic being created/modified by the EditableOMCircle.
     */
    public OMGraphic getGraphic() {
        return circle;
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

    /**
     * Set the GrabPoint that is in the middle of being modified, as a result of
     * a mouseDragged event, or other selection process.
     */
    public void setMovingPoint(GrabPoint gp) {
        super.setMovingPoint(gp);
        // set as a flag that the graphic is being moved, and it's
        // parameters should not be adjusted.
        gpm = null;
    }

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
        int x = (int) pnt.getX();
        int y = (int) pnt.getY();

        for (int i = gb.length - 1; i >= 0; i--) {

            if (i != RADIUS_POINT_INDEX && gb[i] != null && gb[i].distance(x, y) == 0) {

                setMovingPoint(gb[i]);
                // in case the points are on top of each other, the
                // last point in the array will take precedence.
                break;
            }
        }
        return movingPoint;
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

        if (gpr == null) {
            gpr = new GrabPoint(-1, -1);
            gPoints[RADIUS_POINT_INDEX] = gpr;
        }
        if (gpnw == null) {
            gpnw = new GrabPoint(-1, -1);
            gPoints[NW_POINT_INDEX] = gpnw;
        }
        if (gpn == null) {
            gpn = new VerticalGrabPoint(-1, -1);
            gPoints[N_POINT_INDEX] = gpn;
        }
        if (gpne == null) {
            gpne = new GrabPoint(-1, -1);
            gPoints[NE_POINT_INDEX] = gpne;
        }
        if (gpw == null) {
            gpw = new HorizontalGrabPoint(-1, -1);
            gPoints[W_POINT_INDEX] = gpw;
        }
        if (gpe == null) {
            gpe = new HorizontalGrabPoint(-1, -1);
            gPoints[E_POINT_INDEX] = gpe;
        }
        if (gpsw == null) {
            gpsw = new GrabPoint(-1, -1);
            gPoints[SW_POINT_INDEX] = gpsw;
        }
        if (gps == null) {
            gps = new VerticalGrabPoint(-1, -1);
            gPoints[S_POINT_INDEX] = gps;
        }
        if (gpse == null) {
            gpse = new GrabPoint(-1, -1);
            gPoints[SE_POINT_INDEX] = gpse;
        }

        if (gpc == null) {
            gpc = new OffsetGrabPoint(-1, -1);
            gPoints[CENTER_POINT_INDEX] = gpc;
            if (getGraphic().getRenderType() != OMGraphic.RENDERTYPE_LATLON) {
                gpc.addGrabPoint(gpnw);
                gpc.addGrabPoint(gpn);
                gpc.addGrabPoint(gpne);
                gpc.addGrabPoint(gpw);
                gpc.addGrabPoint(gpe);
                gpc.addGrabPoint(gpsw);
                gpc.addGrabPoint(gps);
                gpc.addGrabPoint(gpse);
            }
        }

        if (gpo == null) {
            gpo = new OffsetGrabPoint(-1, -1);
            gPoints[OFFSET_POINT_INDEX] = gpo;
            gpo.addGrabPoint(gpc);
        }
    }

    protected void clearGrabPoints() {

        gpc = null;
        gpr = null;
        gpnw = null;
        gpn = null;
        gpne = null;
        gpw = null;
        gpe = null;
        gpsw = null;
        gps = null;
        gpse = null;
        gpo = null;

        gPoints[CENTER_POINT_INDEX] = gpc;
        gPoints[RADIUS_POINT_INDEX] = gpr;
        gPoints[NW_POINT_INDEX] = gpnw;
        gPoints[N_POINT_INDEX] = gpn;
        gPoints[NE_POINT_INDEX] = gpne;
        gPoints[W_POINT_INDEX] = gpw;
        gPoints[E_POINT_INDEX] = gpe;
        gPoints[SW_POINT_INDEX] = gpsw;
        gPoints[S_POINT_INDEX] = gps;
        gPoints[SE_POINT_INDEX] = gpse;
        gPoints[OFFSET_POINT_INDEX] = gpo;

    }

    /**
     * Set the grab points for the graphic provided, setting them on the extents
     * of the graphic. Called when you want to set the grab points off the
     * location of the graphic.
     */
    public void setGrabPoints(OMGraphic graphic) {
        if (!(graphic instanceof OMCircle)) {
            return;
        }

        assertGrabPoints();

        OMCircle circle = (OMCircle) graphic;
        boolean ntr = circle.getNeedToRegenerate();
        int renderType = circle.getRenderType();

        int centerx = 0;
        int centery = 0;

        if (ntr == false) {

            if (renderType == OMGraphic.RENDERTYPE_LATLON
                    || renderType == OMGraphic.RENDERTYPE_OFFSET) {

                if (projection != null) {
                    LatLonPoint center = circle.getLatLon();
                    Point2D p = projection.forward(center);
                    centerx = (int) p.getX();
                    centery = (int) p.getY();
                }
                if (renderType == OMGraphic.RENDERTYPE_OFFSET) {
                    gpo.setX(centerx);
                    gpo.setY(centery);

                    centerx += circle.getOffX();
                    centery += circle.getOffY();
                }

            } else if (renderType == OMGraphic.RENDERTYPE_XY) {
                centerx = circle.getX();
                centery = circle.getY();
            }

            if (renderType == OMGraphic.RENDERTYPE_LATLON) {
                Debug.message("eomg", "EditableOMCircle: modifying lat/lon circle");
                if (projection != null) {
                    gpc.set(centerx, centery);
                }

                // Note that we really don't handle this situation
                // well, if there isn't a projection. We kind of
                // assume later that there is one, and although there
                // shouldn't be massive meltdowns, data could look
                // funky on the screen if the projection is
                // unavailable.

            } else {
                // Grab the projected endpoints
                Debug.message("eomg", "EditableOMCircle: modifying x/y or offset standard circle");
                int height = circle.getHeight() / 2;
                int width = circle.getWidth() / 2;
                gpc.set(centerx, centery);

                gpe.set(centerx + width, centery, true);
                gps.set(centerx, centery + height, true);
                gpw.set(centerx - width, centery, true);
                gpn.set(centerx, centery - height, true);

                gpne.set(gpe.getX(), gpn.getY());
                gpnw.set(gpw.getX(), gpn.getY());
                gpse.set(gpe.getX(), gps.getY());
                gpsw.set(gpw.getX(), gps.getY());
                gpc.updateOffsets();

                // Debug.output("***\nheight:" + height + ", width:" +
                // width +
                // "\n EditableOMCircle: east at x: " + gpe.getX() +
                // ", y:" + gpe.getY());
                // Debug.output(" EditableOMCircle: north at x: " +
                // gpn.getX() +
                // ", y:" + gpn.getY());
                // Debug.output(" EditableOMCircle: northeast at x: "
                // + gpne.getX() +
                // ", y:" + gpne.getY());
                // Debug.output(" EditableOMCircle: center at x: " +
                // centerx +
                // ", y:" + centery);
            }

            // Check to see if the circle is a offset circle, and set
            // the
            // offset grab point offsets accordingly.
            if (circle.getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
                gpo.updateOffsets();
            }

        } else {
            Debug.message("eomg", "EditableOMCircle.setGrabPoints: graphic needs to be regenerated");
        }

    }

    /**
     * Take the current location of the GrabPoints, and modify the location
     * parameters of the OMCircle with them. Called when you want the graphic to
     * change according to the grab points.
     */
    public void setGrabPoints() {

        int renderType = circle.getRenderType();

        // If the gpm is a movement GP or the gpc, then we need to
        // update all the grab points relative location to the gpm,
        // and then use the code below.

        // If the gpm is the gpo, then the circle moves, too. That's
        // OK, because if the gpo is moving, then the other points
        // have been moved already.

        // If the gpm is one of the other GPs, then we need to update
        // the radius or height and width based on that point, as well
        // as the other GP locations.

        Debug.message("eomg", "EditableOMCircle.setGrabPoints()");

        // Do center point for lat/lon or offset circles
        if (renderType == OMGraphic.RENDERTYPE_LATLON || renderType == OMGraphic.RENDERTYPE_OFFSET) {

            GrabPoint llgp;
            // OK, to set the center, if the rendertype is offset,
            // then the center lat/lon has to be set to the offset
            // point. When the circle is initially defined, this is
            // OK because the offset x and y are 0, and the render
            // method knows not to render the point yet.
            if (// movingPoint == gpo ||
            renderType == OMGraphic.RENDERTYPE_OFFSET) {
                llgp = gpo;
            } else {
                // If the moving point is the radius, the the center
                // grab point is the lat/lon point.
                llgp = gpc;
            }

            if (projection != null) {

                LatLonPoint llp = (LatLonPoint) projection.inverse(llgp.getX(), llgp.getY(), new LatLonPoint.Double());

                circle.setCenter(llp);

                // Do the radius for LATLON circles.
                if (renderType == OMGraphic.RENDERTYPE_LATLON && movingPoint == gpr) {

                    LatLonPoint llpm = (LatLonPoint) projection.inverse(gpr.getX(), gpr.getY(), new LatLonPoint.Double());

                    double radius;

                    if (projection instanceof GeoProj) {
                        radius = Length.DECIMAL_DEGREE.fromRadians(llpm.distance(llp));
                    } else {
                        radius = DrawUtil.distance(llpm.getX(), llpm.getY(), llp.getX(), llp.getY());
                    }

                    setRadius(radius);

                }

            } else {
                Debug.message("eomg", "EditableOMCircle.setGrabPoints: projection is null, can't figure out LATLON points for circle.");
            }
        }

        boolean settingOffset = getStateMachine().getState() instanceof GraphicSetOffsetState
                && movingPoint == gpo;

        // If the center point is moving, the offset distance changes
        if (renderType == OMGraphic.RENDERTYPE_OFFSET && (settingOffset || movingPoint == gpc)) {
            // Do the offset point.
            circle.setOffX(gpc.getX() - gpo.getX());
            circle.setOffY(gpc.getY() - gpo.getY());
            Debug.message("eomg", "EditableOMCircle: updating offset distance, ox:"
                    + circle.getOffX() + ", oy:" + circle.getOffY());
        }

        // Do the circle height and width for XY and OFFSET render
        // types.
        if (renderType == OMGraphic.RENDERTYPE_XY
                || (renderType == OMGraphic.RENDERTYPE_OFFSET && !settingOffset)) {

            if (renderType == OMGraphic.RENDERTYPE_XY) {
                circle.setX(gpc.getX());
                circle.setY(gpc.getY());
            }

            if (movingPoint instanceof HorizontalGrabPoint) {
                // Must be the left or right...
                circle.setWidth(Math.abs(movingPoint.getX() - gpc.getX()) * 2);

            } else if (movingPoint instanceof VerticalGrabPoint) {
                // Must be the top or bottom...
                circle.setHeight(Math.abs(movingPoint.getY() - gpc.getY()) * 2);

            } else if (movingPoint != gpo && movingPoint != gpc) {
                // Must be one of the corners...
                circle.setWidth(Math.abs(movingPoint.getX() - gpc.getX()) * 2);
                circle.setHeight(Math.abs(movingPoint.getY() - gpc.getY()) * 2);
            }

            // Debug.output("EditableOMCircle.setGrabPoints():
            // movingPoint x:" +
            // movingPoint.getX() + ", y:" + movingPoint.getY() +
            // ", gpc.x:" + gpc.getX() + ", gpc.y:" + gpc.getY());
        }

        if (projection != null) {
            regenerate(projection);
        }
    }

    /**
     * To be overloaded if needed when setting circle's radius.
     * 
     * @param radius in DECIMAL_DEGREES
     */
    protected void setRadius(double radius) {
        if (circle != null) {
            circle.setRadius(radius);
        }
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
    public void move(MouseEvent e) {
        if (getGraphic().getRenderType() == OMGraphic.RENDERTYPE_LATLON
                && isMouseEventTouchingTheEdge(e)) {
            if (gpr == null) {
                gpr = new GrabPoint(-1, -1);
            }
            gpr.set(e.getX(), e.getY());
            movingPoint = gpr;
        }
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
        Debug.message("eomg", "EditableOMCircle.generate()");
        if (circle != null)
            circle.generate(proj);

        for (int i = 0; i < gPoints.length; i++) {
            GrabPoint gp = gPoints[i];
            if (gp != null) {
                gp.generate(proj);

                // Why is this here???
                // if (gp instanceof OffsetGrabPoint) {
                // ((OffsetGrabPoint)gpo).updateOffsets();
                // }
            }
        }
        return true;
    }

    /**
     * Given a new projection, the grab points may need to be repositioned off
     * the current position of the graphic. Called when the projection changes.
     */
    public void regenerate(Projection proj) {
        Debug.message("eomg", "EditableOMCircle.regenerate()");
        if (circle != null)
            circle.regenerate(proj);

        setGrabPoints(circle);
        generate(proj);
    }

    /**
     * Draw the EditableOMCircle parts into the java.awt.Graphics object. The
     * grab points are only rendered if the circle machine state is
     * CircleSelectedState.CIRCLE_SELECTED.
     * 
     * @param graphics java.awt.Graphics.
     */
    public void render(java.awt.Graphics graphics) {
        Debug.message("eomg", "EditableOMCircle.render()");

        State state = getStateMachine().getState();

        // All the rotation stuff isn't ready for primetime, yet.
        // Need to translate the mouse events, too.

        // graphics = graphics.create();
        // double rotationAngle = OMGraphic.DEFAULT_ROTATIONANGLE;

        if (circle == null) {
            Debug.message("eomg", "EditableOMCircle.render: null circle.");
            return;
        }

        circle.setVisible(true);
        circle.render(graphics);
        circle.setVisible(false);

        // rotationAngle = circle.getRotationAngle();

        int renderType = circle.getRenderType();

        // if (rotationAngle != OMGraphic.DEFAULT_ROTATIONANGLE) {
        // ((java.awt.Graphics2D)graphics).rotate(rotationAngle,
        // circle.getX(), circle.getY());
        // }

        if (state instanceof GraphicSelectedState || state instanceof GraphicEditState) {

            for (int i = 0; i < gPoints.length; i++) {
                GrabPoint gp = gPoints[i];
                if (gp != null) {
                    if (i == RADIUS_POINT_INDEX)
                        continue;

                    if (renderType == OMGraphic.RENDERTYPE_LATLON && i != CENTER_POINT_INDEX)
                        continue;

                    if ((i == OFFSET_POINT_INDEX && renderType == OMGraphic.RENDERTYPE_OFFSET && movingPoint == gpo)
                            || (state instanceof GraphicSelectedState && ((i != OFFSET_POINT_INDEX && renderType != OMGraphic.RENDERTYPE_OFFSET) || (renderType == OMGraphic.RENDERTYPE_OFFSET)))) {

                        gp.setVisible(true);
                        gp.render(graphics);
                        gp.setVisible(false);
                    }
                }
            }
        }
    }

    /**
     * If this EditableOMGraphic has parameters that can be manipulated that are
     * independent of other EditableOMGraphic types, then you can provide the
     * widgets to control those parameters here. By default, returns the
     * GraphicAttributes GUI widgets. If you don't want a GUI to appear when a
     * widget is being created/edited, then don't call this method from the
     * EditableOMGraphic implementation, and return a null Component from
     * getGUI.
     * 
     * @param graphicAttributes the GraphicAttributes to use to get the GUI
     *        widget from to control those parameters for this EOMG.
     * @return java.awt.Component to use to control parameters for this EOMG.
     */
    public java.awt.Component getGUI(GraphicAttributes graphicAttributes) {
        Debug.message("eomg", "EditableOMCircle.getGUI");
        // if (graphicAttributes != null) {
        // return graphicAttributes.getColorAndLineGUI();
        // } else {
        return null;
        // }
    }

}
