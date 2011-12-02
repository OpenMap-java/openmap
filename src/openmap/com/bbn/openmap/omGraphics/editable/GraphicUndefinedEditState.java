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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/editable/GraphicEditState.java,v
// $
// $RCSfile: GraphicEditState.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.editable;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMGraphic;

/**
 * The GraphicUndefinedEditState is for a couple of OMGraphics that are still
 * undefined when they are created after a first click. We can have the
 * OMGraphic in an edit state, but we need to have a mouse released away from
 * the first mouse pressed in order to have an OMGraphic that makes sense. For
 * instance, circles and rectangles can't have just one point defined, so if
 * only one point is defined, the mouse dragged and mouse moved actions should
 * be the same, and then mouse release should mark the second definition point
 * of the OMGraphic.
 * 
 * @author ddietrick
 */
public class GraphicUndefinedEditState
        extends GraphicEditState
        implements EOMGEditState {

    protected boolean needAnotherPoint = false;

    public GraphicUndefinedEditState(EditableOMGraphic eomg) {
        super(eomg);
    }

    public boolean mouseDragged(MouseEvent e) {
        needAnotherPoint = false;
        return super.mouseDragged(e);
    }

    public boolean mouseReleased(MouseEvent e) {
        
        if (needAnotherPoint) {
            return false;
        }

        return super.mouseReleased(e);
    }
    
    public boolean mouseMoved(MouseEvent e) {
        needAnotherPoint = false;
        return super.mouseMoved(e);
    }
}
