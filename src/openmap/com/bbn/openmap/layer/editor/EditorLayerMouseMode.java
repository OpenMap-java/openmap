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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/EditorLayerMouseMode.java,v $
// $RCSfile: EditorLayerMouseMode.java,v $
// $Revision: 1.6 $
// $Date: 2004/10/14 18:05:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.event.MouseEvent;

import com.bbn.openmap.event.CoordMouseMode;
import com.bbn.openmap.event.SelectMouseMode;

/**
 * The EditorLayerMouseMode is a mouse mode that is made active when
 * the EditorTool needs events. It serves to ensure that the
 * EditorLayer is the only layer receiving events from the MapBean
 * when the EditorTool needs them, with the addition bonus of being
 * invisible. The EditorTool should take care of indicating what it is
 * doing with the MouseEvents, by creating certain OMGraphics,
 * selecting certain types of objects, etc.
 */
public class EditorLayerMouseMode extends CoordMouseMode {

    /**
     * Mouse Mode identifier, which is "EditorLayer". This is returned
     * on getID(). This mouse mode is invisible, so id doesn't have to
     * be pretty for the GUI.
     */
    public transient String modeID = null;

    /**
     * Construct a EditorLayerMouseMode. Default constructor. Sets the
     * ID to the modeID, and the consume mode to true.
     * 
     * @param idToUse a uniqueID to use just for a particular layer.
     */
    public EditorLayerMouseMode(String idToUse) {
        this(idToUse, true);
    }

    /**
     * Construct a EditorLayerMouseMode. The constructor that lets you
     * set the consume mode.
     * 
     * @param idToUse a uniqueID to use just for a particular layer.
     * @param consumeEvents the consume mode setting.
     */
    public EditorLayerMouseMode(String idToUse, boolean consumeEvents) {
        super(idToUse, consumeEvents);
        modeID = idToUse;
        setVisible(false);
    }

    SelectMouseMode gestures = null;

    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof SelectMouseMode
                && ((SelectMouseMode) someObj).getID() == SelectMouseMode.modeID) {
            gestures = (SelectMouseMode) someObj;
        }
    }

    public void findAndUndo(Object someObj) {
        super.findAndInit(someObj);
        if (someObj == gestures) {
            gestures = null;
        }
    }

    /**
     * Fires the MapMouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        mouseSupport.fireMapMouseClicked(e);
        fireMouseLocation(e);
    }

    /**
     * Fires the MapMouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mousePressed(MouseEvent e) {
        mouseSupport.fireMapMousePressed(e);
        fireMouseLocation(e);
    }

    /**
     * Fires the MapMouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mouseReleased(MouseEvent e) {
        mouseSupport.fireMapMouseReleased(e);
        fireMouseLocation(e);
    }

    /**
     * Fires the MapMouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mouseEntered(MouseEvent e) {
        mouseSupport.fireMapMouseEntered(e);
    }

    /**
     * Fires the MapMouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mouseExited(MouseEvent e) {
        mouseSupport.fireMapMouseExited(e);
    }

    /**
     * Fires the MapMouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mouseDragged(MouseEvent e) {
        if (gestures != null) {
            gestures.mouseDragged(e);
        }
        mouseSupport.fireMapMouseDragged(e);
        fireMouseLocation(e);
    }

    /**
     * Fires the MapMouseSupport method.
     * 
     * @param e mouse event.
     */
    public void mouseMoved(MouseEvent e) {
        if (gestures != null) {
            gestures.mouseMoved(e);
        }
        mouseSupport.fireMapMouseMoved(e);
        fireMouseLocation(e);
    }
}