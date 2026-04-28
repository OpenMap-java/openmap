// **********************************************************************
// **********************************************************************
// 
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/RectUndefinedState.java,v
// $
// $RCSfile: RectUndefinedState.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.util.Debug;

/**
 * This is a new GraphicUndefinedState for OMGraphics that can handle being
 * defined with click or press setting the initial point, and then a mouse move
 * or drag, respectively, causing placement of the second point. Good for lines,
 * circles, rectangles.
 * 
 * @author dietrick
 */
public class ClckOrDrgUndefinedState extends GraphicUndefinedState {

    public ClckOrDrgUndefinedState(EditableOMGraphic eomg) {
        super(eomg);
    }

    Point point1 = null;
    int indexOfFirstPoint;
    int indexOfSecondPoint;

    /**
     * In this state, we need to draw a rect from scratch. So, we listen for a
     * mouse down, and set both points there, and then set the mode to rect
     * edit.
     */
    public boolean mousePressed(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|undefined state|mousePressed = "
                + graphic.getGraphic().getRenderType());

        if (point1 == null) {
            graphic.getGrabPoint(indexOfFirstPoint).set(e.getX(), e.getY());
            point1 = new Point(e.getX(), e.getY());

            if (graphic.getGraphic().getRenderType() == OMGraphic.RENDERTYPE_OFFSET) {
                graphic.getStateMachine().setOffsetNeeded(true);
                Debug.message("eomg", "GraphicStateMachine|undefined state| *offset needed*");
            }

            graphic.getGrabPoint(indexOfSecondPoint).set(e.getX(), e.getY());
            graphic.setMovingPoint(graphic.getGrabPoint(indexOfSecondPoint));
        } else {
            graphic.redraw(e);
        }

        return getMapMouseListenerResponse();
    }

    public boolean mouseReleased(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|undefined state|mouseReleased = "
                + graphic.getGraphic().getRenderType());
        // C'mon, point1 has to be set here, right?
        if (point1.distance(e.getX(), e.getY()) > 5) {

            if ((graphic.getStateMachine()).isOffsetNeeded() == true) {
                graphic.getStateMachine().setOffset();
                graphic.getStateMachine().setOffsetNeeded(false);
            } else {
                graphic.getStateMachine().setSelected();
                GrabPoint mp = graphic.getMovingPoint();

                // If right mouse button not pressed, then it's a valid end to a
                // modification.
                if (!SwingUtilities.isRightMouseButton(e)) {
                    graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_UNDO);
                }

                if (mp == null && !graphic.getCanGrabGraphic()) {
                    graphic.fireEvent(EOMGCursors.DEFAULT, "", e, EOMGEvent.EOMG_SELECTED);
                } else {
                    graphic.fireEvent(EOMGCursors.EDIT, "", e, EOMGEvent.EOMG_SELECTED);
                }
            }

            graphic.redraw(e, true);
            graphic.setMovingPoint(null);
        }

        return getMapMouseListenerResponse();
    }

    public boolean mouseMoved(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|undefined state|mouseMoved = "
                + graphic.getGraphic().getRenderType());
        if (point1 != null) {
            graphic.redraw(e);
        }
        return getMapMouseListenerResponse();
    }

    public boolean mouseDragged(MouseEvent e) {
        Debug.message("eomg", "GraphicStateMachine|undefined state|mouseDragged = "
                + graphic.getGraphic().getRenderType());
        if (point1 != null) {
            graphic.redraw(e);
        }

        return getMapMouseListenerResponse();
    }

}