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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMLine.java,v $
// $RCSfile: EditableOMLine.java,v $
// $Revision: 1.13 $
// $Date: 2009/02/25 22:34:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.editable.GraphicEditState;
import com.bbn.openmap.omGraphics.editable.GraphicSelectedState;
import com.bbn.openmap.omGraphics.editable.LineStateMachine;
import com.bbn.openmap.omGraphics.geom.NonRegional;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

/**
 * The EditableOMLine encompasses an OMLine, providing methods for modifying or
 * creating it.
 */
public class EditableOMLine extends EditableOMAbstractLine implements NonRegional {

    protected GrabPoint gp1;
    protected GrabPoint gp2;
    protected OffsetGrabPoint gpo; // offset
    protected OffsetGrabPoint gpm; // for grabbing the line and moving
    // it.

    protected OMLine line;

    public final static int STARTING_POINT_INDEX = 0;
    public final static int ENDING_POINT_INDEX = 1;
    public final static int OFFSET_POINT_INDEX = 2;

    /**
     * Create the EditableOMLine, setting the state machine to create the line
     * off of the gestures.
     */
    public EditableOMLine() {
        createGraphic(null);
    }

    /**
     * Create an EditableOMLine with the lineType and renderType parameters in
     * the GraphicAttributes object.
     */
    public EditableOMLine(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableOMLine with an OMLine already defined, ready for
     * editing.
     * 
     * @param oml OMLine that should be edited.
     */
    public EditableOMLine(OMLine oml) {
        setGraphic(oml);
    }

    /**
     * Create and initialize the state machine that interprets the modifying
     * gestures/commands, as well as initialize the grab points. Also allocates
     * the grab point array needed by the EditableOMLine.
     */
    public void init() {
        Debug.message("eomg", "EditableOMLine.init()");
        setStateMachine(new LineStateMachine(this));
        gPoints = new GrabPoint[3];
    }

    /**
     * Set the graphic within the state machine. If the graphic is null, then
     * one shall be created, and located off screen until the gestures driving
     * the state machine place it on the map.
     */
    public void setGraphic(OMGraphic graphic) {
        init();
        if (graphic instanceof OMLine) {
            line = (OMLine) graphic;
            stateMachine.setSelected();
            setGrabPoints(line);
        } else {
            createGraphic(null);
        }
    }

    /**
     * Create and set the graphic within the state machine. The
     * GraphicAttributes describe the type of line to create.
     */
    public void createGraphic(GraphicAttributes ga) {
        init();
        stateMachine.setUndefined();
        int renderType = OMGraphic.RENDERTYPE_UNKNOWN;
        int lineType = OMGraphic.LINETYPE_GREATCIRCLE;

        if (ga != null) {
            renderType = ga.getRenderType();
            lineType = ga.getLineType();
        }

        if (Debug.debugging("eoml")) {
            Debug.output("EditableOMLine.createGraphic(): rendertype = " + renderType);
        }

        if (lineType == OMGraphic.LINETYPE_UNKNOWN) {
            lineType = OMGraphic.LINETYPE_GREATCIRCLE;
            if (ga != null)
                ga.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
        }

        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON):
            line = new OMLine(90f, -180f, 90f, -180f, lineType);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET):
            line = new OMLine(90d, -180d, 0, 0, 0, 0);
            break;
        default:
            line = new OMLine(-1, -1, -1, -1);
        }

        if (ga != null) {
            ga.setTo(line, true);
        }
    }

    /**
     * Get the OMGraphic being created/modified by the EditableOMLine.
     */
    public OMGraphic getGraphic() {
        return line;
    }

    /**
     * Set the GrabPoint that is in the middle of being modified, as a result of
     * a mouseDragged event, or other selection process.
     */
    public void setMovingPoint(GrabPoint gp) {
        super.setMovingPoint(gp);
        gpm = null;
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
     * Check to make sure the grab points are not null. If they are, allocate
     * them, and them assign them to the array.
     */
    public void assertGrabPoints() {
        if (gp1 == null) {
            gp1 = new GrabPoint(-1, -1);
            gPoints[STARTING_POINT_INDEX] = gp1;
        }
        if (gp2 == null) {
            gp2 = new GrabPoint(-1, -1);
            gPoints[ENDING_POINT_INDEX] = gp2;
        }

        if (gpo == null) {
            gpo = new OffsetGrabPoint(-1, -1);
            gPoints[OFFSET_POINT_INDEX] = gpo;
            gpo.addGrabPoint(gp1);
            gpo.addGrabPoint(gp2);
        }
    }

    /**
     * Set the grab points for the graphic provided, setting them on the extents
     * of the graphic. Called when you want to set the grab points off the
     * location of the graphic.
     */
    public void setGrabPoints(OMGraphic graphic) {
        if (!(graphic instanceof OMLine)) {
            return;
        }

        assertGrabPoints();

        OMLine line = (OMLine) graphic;
        boolean ntr = line.getNeedToRegenerate();
        int renderType = line.getRenderType();
        if (ntr == false) {
            if (renderType == OMGraphic.RENDERTYPE_LATLON) {
                Debug.message("eomg", "EditableOMLine: modifying lat/lon line");

                // Complicated lines!!!! Need to grab the end points
                // that are on the map! See, for very large lines
                // that go around the earth, they are actually drawn
                // in OpenMap as an array of lines that are clipped as
                // they go offscreen. Eventually, one of the points
                // should appear on the map somewhere. If they don't,
                // then then the end points may not be on the screen.
                if (projection != null) {

                    double[] ll = line.getLL();
                    Point2D p = projection.forward(ll[0], ll[1]);
                    gp1.set((int) p.getX(), (int) p.getY());

                    projection.forward(ll[2], ll[3], p);
                    gp2.set((int) p.getX(), (int) p.getY());
                }

            } else {
                // Grab the projected endpoints
                Debug.message("eomg", "EditableOMLine: modifying x/y or offset standard line");
                gp1.set(line.xpoints[0][0], line.ypoints[0][0]);

                int last = line.xpoints[0].length - 1;
                gp2.set(line.xpoints[0][last], line.ypoints[0][last]);
            }

            // Check to see if the line is a offset line, and set the
            // offset grab point accordingly.
            if (line.getRenderType() == OMGraphic.RENDERTYPE_OFFSET && projection != null) {

                double[] ll = line.getLL();
                Point2D p = projection.forward(ll[0], ll[1]);

                gpo.set((int) p.getX(), (int) p.getY());
                gpo.updateOffsets();
            }
        } else {
            Debug.message("eomg", "EditableOMLine.setGrabPoints: graphic needs to be regenerated");
        }
    }

    /**
     * Take the current location of the GrabPoints, and modify the location
     * parameters of the OMLine with them. Called when you want the graphic to
     * change according to the grab points.
     */
    public void setGrabPoints() {

        int renderType = line.getRenderType();
        if (renderType == OMGraphic.RENDERTYPE_LATLON) {
            if (projection != null) {
                double[] coords = new double[4];
                LatLonPoint llp = (LatLonPoint) projection.inverse(gp1.getX(), gp1.getY(), new LatLonPoint.Double());

                coords[0] = llp.getY();
                coords[1] = llp.getX();

                projection.inverse(gp2.getX(), gp2.getY(), llp);
                coords[2] = llp.getY();
                coords[3] = llp.getX();
                line.setLL(coords);
            } else {
                Debug.message("eomg", "EditableOMLine.setGrabPoints: projection is null, can't figure out LATLON points for line.");
            }
        } else if (renderType == OMGraphic.RENDERTYPE_OFFSET) {
            // Do the offset point.
            if (projection != null) {
                double[] coords = new double[4];
                LatLonPoint llp = (LatLonPoint) projection.inverse(gpo.getX(), gpo.getY(), new LatLonPoint.Double());

                coords[0] = llp.getY();
                coords[1] = llp.getX();
                coords[2] = 0;// not used
                coords[3] = 0;// not used
                line.setLL(coords);
            } else {
                Debug.message("eomg", "EditableOMLine.setGrabPoints: projection is null, can't figure out LATLON points for line offset.");
            }
        }

        if (renderType == OMGraphic.RENDERTYPE_XY || renderType == OMGraphic.RENDERTYPE_OFFSET) {

            int[] ints = new int[4];
            if (renderType == OMGraphic.RENDERTYPE_OFFSET && gpo != null) {
                // If offset rendertype, the x-y have to be offset
                // distances, not screen pixel locations.
                ints[0] = gp1.getX() - gpo.getX();
                ints[1] = gp1.getY() - gpo.getY();
                ints[2] = gp2.getX() - gpo.getX();
                ints[3] = gp2.getY() - gpo.getY();
            } else {
                ints[0] = gp1.getX();
                ints[1] = gp1.getY();
                ints[2] = gp2.getX();
                ints[3] = gp2.getY();
            }
            line.setPts(ints);
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
        // Need to check to see if the OffsetGrabPoint is currently
        // being used. If not, just use it, otherwise, will need to
        // create a special one for the move.

        Point2D pnt = getProjectionPoint(e);
        int x = (int) pnt.getX();
        int y = (int) pnt.getY();

        if (line.getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
            gpm = new OffsetGrabPoint(x, y);

            gpm.addGrabPoint(gp1);
            gpm.addGrabPoint(gp2);

        } else {
            gpm = gpo;
            gpm.set(x, y);
            gpm.updateOffsets();
        }
        movingPoint = gpm;
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
        Debug.message("eomg", "EditableOMLine.generate()");
        if (line != null)
            line.generate(proj);

        if (gp1 != null)
            gp1.generate(proj);
        if (gp2 != null)
            gp2.generate(proj);
        if (gpo != null) {
            gpo.generate(proj);
            gpo.updateOffsets();
        }
        return true;
    }

    /**
     * Given a new projection, the grab points may need to be repositioned off
     * the current position of the graphic. Called when the projection changes.
     */
    public void regenerate(Projection proj) {
        Debug.message("eomg", "EditableOMLine.regenerate()");
        if (line != null)
            line.generate(proj);
        setGrabPoints(line);

        if (gp1 != null)
            gp1.generate(proj);
        if (gp2 != null)
            gp2.generate(proj);
        if (gpo != null) {
            gpo.generate(proj);
            gpo.updateOffsets();
        }
    }

    /**
     * Draw the EditableOMLine parts into the java.awt.Graphics object. The grab
     * points are only rendered if the line machine state is
     * LineSelectedState.LINE_SELECTED.
     * 
     * @param graphics java.awt.Graphics.
     */
    public void render(java.awt.Graphics graphics) {
        Debug.message("eomg", "EditableOMLine.render()");

        State state = getStateMachine().getState();

        if (line != null) {
            line.setVisible(true);
            line.render(graphics);
            line.setVisible(false);
        } else {
            Debug.message("eomg", "EditableOMLine.render: null line.");
        }

        if (state instanceof GraphicSelectedState) {
            if (gp1 != null) {
                gp1.setVisible(true);
                gp1.render(graphics);
                gp1.setVisible(false);
            }

            if (gp2 != null) {
                gp2.setVisible(true);
                gp2.render(graphics);
                gp2.setVisible(false);
            }
        }

        if (state instanceof GraphicSelectedState || state instanceof GraphicEditState /*
                                                                                        * ||
                                                                                        * state
                                                                                        * instanceof
                                                                                        * LineSetOffsetState
                                                                                        */) {
            if (gpo != null && line.getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
                gpo.setVisible(true);
                gpo.render(graphics);
                gpo.setVisible(false);
            }
        }
    }

}