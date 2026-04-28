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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMGraphicList.java,v $
// $RCSfile: EditableOMGraphicList.java,v $
// $Revision: 1.4 $
// $Date: 2006/08/09 21:08:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.omGraphics.editable.ListStateMachine;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;

/**
 * An EditableOMGraphic list encapsulates an OMGraphicList to move the
 * editable ones around when they are selected as a group.
 */
public class EditableOMGraphicList extends EditableOMGraphic {

    /**
     * For grabbing the list objects and moving them.
     */
    protected OffsetGrabPoint gpm;

    /**
     * The list of OMGraphics being selected and moved.
     */
    protected OMGraphicList list;

    /**
     * The list of editables wrapping the list contents.
     */
    protected List<EditableOMGraphic> editables;

    /**
     * Create an empty EditableOMGraphicList, ready to have OMGraphics
     * added to it.
     */
    public EditableOMGraphicList() {
        this(new OMGraphicList());
    }

    /**
     * Create the EditableOMGraphicList with an OMGraphicList already
     * defined, ready for editing.
     * 
     * @param oml OMGraphicList that should be handled.
     */
    public EditableOMGraphicList(OMGraphicList oml) {
        setGraphic(oml);
    }

    public List<EditableOMGraphic> getEditables() {
        if (editables == null) {
            editables = new LinkedList<EditableOMGraphic>();
        }
        return editables;
    }

    /**
     * Create and initialize the state machine that interprets the
     * modifying gestures/commands, as well as initialize the grab
     * points. Also allocates the grab point array needed by the
     * EditableOMGraphicList.
     */
    public void init() {
        Debug.message("eomg", "EditableOMGraphicList.init()");
        getEditables();
        setStateMachine(new ListStateMachine(this));
    }

    /**
     * Must be called on a EditableOMGraphicList that is created from
     * an OMGraphicList containing OMGraphics.
     * 
     * @param drawingTool OMDrawingTool used to create
     *        EditableOMGraphics for other OMGraphics on the list,
     *        which will in turn be managed by this
     *        EditableOMGraphicList. If this is null, nothing will get
     *        done. If this drawing tool doesn't know how to create an
     *        EditableOMGraphic for anything on the list, those things
     *        will not be managed.
     */
    public void init(OMDrawingTool drawingTool) {
        if (list != null) {
            for (OMGraphic omg: list) {
                // Do we need to handle OMGraphicLists in a special
                // way?
                if (omg.isVisible()) {
                    add(omg, drawingTool);
                }
            }
        }
    }

    public GrabPoint[] getGrabPoints() {
        return new GrabPoint[] { gpm };
    }

    /**
     * Set the graphic within the state machine. If the graphic is
     * null, then one shall be created, and located off screen until
     * the gestures driving the state machine place it on the map.
     */
    public void setGraphic(OMGraphic graphic) {
        init();
        if (graphic instanceof OMGraphicList) {
            list = (OMGraphicList) graphic;
            list.setProcessAllGeometries(true);
            stateMachine.setSelected();
            gpm = new OffsetGrabPoint(-10, -10);
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

        OMGraphicList tmpList = new OMGraphicList();

        if (ga != null) {
            ga.setTo(tmpList);
        }

        setGraphic(tmpList);
    }

    /**
     * Get the OMGraphic being created/modified by the
     * EditableOMGraphicList.
     */
    public OMGraphic getGraphic() {
        return list;
    }

    public void add(OMGraphicList list, OMDrawingTool drawingTool) {
        for (OMGraphic omg : list) {
            add(omg, drawingTool);
        }
    }

    /**
     * Create an EditableOMGraphic and add it to the list.
     * 
     * @param omg OMGraphic to add.
     * @param drawingTool to use to figure out what EditableOMGraphic
     *        to use for the OMGraphic.
     * @return EditableOMGraphic if successful, null if not.
     */
    public EditableOMGraphic add(OMGraphic omg, OMDrawingTool drawingTool) {
        EditableOMGraphic editable = null;

        if (omg instanceof OMGraphicList) {
            add((OMGraphicList) omg, drawingTool);
            return editable;
        }

        if (omg != null && drawingTool != null) {
            // The OMDrawingTool knows how to create an
            // EditableOMGraphic for the omg
            editable = drawingTool.getEditableGraphic(omg);
            if (editable != null) {
                add(editable);
            } else {
                if (Debug.debugging("eomg")) {
                    Debug.output("EditableOMGraphicList can't handle "
                            + omg.getClass().getName());
                }
            }
        } else {
            if (Debug.debugging("eomg")) {
                Debug.output("EditableOMGraphicList told to add null OMGraphic or null OMDrawingTool");
            }
        }

        return editable;
    }

    /**
     * Add the EditableOMGraphic to the list.
     */
    public void add(EditableOMGraphic editable) {
        if (editable == null) {
            if (Debug.debugging("eomg")) {
                Debug.output("EditableOMGraphicList adding null EditableOMGraphic");
            }
            return;
        }

        if (Debug.debugging("eomg")) {
            Debug.output("EditableOMGraphicList adding "
                    + editable.getClass().getName() + " " + editable);
        }

        OMGraphic graphic = editable.getGraphic();

        if (!list.contains(graphic)) {
            getEditables().add(editable);
            editable.setProjection(getProjection());

            // Need this for distance measurements.
            list.add(graphic);
            editable.attachToMovingGrabPoint(gpm);
        } else {
            if (Debug.debugging("eomg")) {
                Debug.output("EditableOMGraphicList.add("
                        + editable.getClass().getName()
                        + ") not added, duplicate");
            }
        }
    }

    /**
     * Remove an OMGraphic from being moved.
     */
    public void remove(OMGraphic omg) {
        EditableOMGraphic eomg = null;
        for (EditableOMGraphic eomgraphic : getEditables()) {
            eomg = eomgraphic;
            if (eomg.getGraphic() == omg) {
                break;
            }
            eomg = null;
        }

        // If we found the eomg for the omg, we broke out of the loop above and
        // eomg is set to something.
        if (eomg != null) {
            remove(eomg);
            list.remove(omg);
        }
    }

    /**
     * Remove the EditableOMGraphic from the list.
     */
    public boolean remove(EditableOMGraphic editable) {
        if (editable == null) {
            if (Debug.debugging("eomg")) {
                Debug.output("EditableOMGraphicList removing null EditableOMGraphic");
            }
            return false;
        }

        if (Debug.debugging("eomg")) {
            Debug.output("EditableOMGraphicList removing "
                    + editable.getClass().getName());
        }

        editable.setProjection(null);
        editable.detachFromMovingGrabPoint(gpm);
        boolean ret = getEditables().remove(editable);
        return ret;
    }

    /**
     * Remove all EditableOMGraphics and clear out.
     */
    public void clear() {
        //      list.processAllGeometries(false);
        //      list.clear();
        //      list = null;
        getEditables().clear();
        gpm.clear();
    }

    /**
     * Set the current projection.
     */
    public void setProjection(Projection proj) {
        if (Debug.debugging("eomg")) {
            Debug.output("EOMGL: setProjection(" + proj + ")");
        }
        super.setProjection(proj);
        for (EditableOMGraphic eomg : getEditables()) {
            eomg.setProjection(proj);
        }
    }

    /**
     * Take the current location of the GrabPoints, and modify the
     * location parameters of the OMLine with them. Called when you
     * want the graphic to change according to the grab points.
     */
    public void setGrabPoints() {
        for (EditableOMGraphic eomg : getEditables()) {
            eomg.setGrabPoints();
            //          if (Debug.debugging("eomg")) {
            //              Debug.output(" -- setting GrabPoints on " +
            // editable.getClass().getName());
            //          }
        }
    }

    public GrabPoint getMovingPoint(MouseEvent me) {
        // For the EdtiableOMGraphicList, this should just go ahead
        // and test for contact for anything on the OMGraphicList, and
        // return the gpm for that point.
        if (list != null) {
            float distance = list.distance(me.getX(), me.getY());
            if (distance <= 4) {
                // will set movingPoint
                move(me);
            } else {
                //              int count = 0;
                //              for (Iterator it = list.iterator(); it.hasNext();)
                // {
                //                  OMGraphic omg = (OMGraphic)it.next();
                //                  Debug.output(" graphic " + (count++) + " distance("
                // +
                //                               omg.distance(me.getX(), me.getY()) + ") ntbr: " +
                //                               omg.getNeedToRegenerate());
                //              }
                movingPoint = null;
            }
        } else {
            //          Debug.output("EOMGL.getMovingPoint() null list");
            movingPoint = null;
        }

        return movingPoint;
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
    public void move(MouseEvent e) {
        if (gpm != null) {
            gpm.set(e.getX(), e.getY());
            gpm.updateOffsets();
            movingPoint = gpm;
        }
    }

    /**
     * Use the current projection to place the graphics on the screen.
     * Has to be called to at least assure the graphics that they are
     * ready for rendering. Called when the graphic position changes.
     * 
     * @param proj com.bbn.openmap.proj.Projection
     * @return true
     */
    public boolean generate(Projection proj) {
        Debug.message("eomg", "EditableOMGraphicList.generate()");

        for (EditableOMGraphic eomg : getEditables()) {
            eomg.generate(proj);
        }

        if (gpm != null)
            gpm.generate(proj);

        return true;
    }

    /**
     * Given a new projection, the grab points may need to be
     * repositioned off the current position of the graphic. Called
     * when the projection changes.
     */
    public void regenerate(Projection proj) {
        Debug.message("eomg", "EditableOMGraphicList.regenerate()");

        for (EditableOMGraphic eomg : getEditables()) {
            eomg.regenerate(proj);
        }

        if (gpm != null)
            gpm.generate(proj);
    }

    /**
     * Draw the EditableOMGraphicList parts into the java.awt.Graphics
     * object. The grab points are only rendered if the line machine
     * state is LineSelectedState.LINE_SELECTED.
     * 
     * @param graphics java.awt.Graphics.
     */
    public void render(java.awt.Graphics graphics) {
        for (EditableOMGraphic eomg : getEditables()) {
            eomg.render(graphics);
        }
    }
}