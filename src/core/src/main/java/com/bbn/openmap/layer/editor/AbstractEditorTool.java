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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/AbstractEditorTool.java,v
// $
// $RCSfile: AbstractEditorTool.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.Container;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter;

public class AbstractEditorTool extends StandardMapMouseInterpreter implements
        EditorTool {

    /**
     * Flag to let it's layer know when it wants control over mouse events.
     */
    protected boolean wantsEvents = false;
    /**
     * Used as a placeholder if face is null.
     */
    protected boolean visible = false; // until we are told otherwise.

    /**
     * Make sure you set the EditorLayer at some point.
     */
    protected AbstractEditorTool() {
    // Set the layer later.
    }

    /**
     * The preferred constructor.
     */
    public AbstractEditorTool(EditorLayer eLayer) {
        setLayer(eLayer);
    }

    public void setLayer(OMGraphicHandlerLayer eLayer) {
        super.setLayer(eLayer);
        if (eLayer instanceof EditorLayer) {
            ((EditorLayer) eLayer).setMouseEventInterpreter(this);
        }
    }

    /**
     * Set whether the tool should want MouseEvents.
     */
    public void setWantsEvents(boolean value) {
        wantsEvents = value;
    }

    /**
     * Whether the Tool is expecting to be fed MouseEvents.
     */
    public boolean wantsEvents() {
        return wantsEvents;
    }

    /**
     * Part of the interface where the EditorLayer can provide components that
     * are available via the MapHandler/BeanContext. The object is something
     * that has been added to the MapHandler.
     */
    public void findAndInit(Object obj) {}

    /**
     * Part of the interface where the EditorLayer can provide components that
     * are available via the MapHandler/BeanContext. The object is something
     * that has been removed from the MapHandler.
     */
    public void findAndUndo(Object obj) {}

    /**
     * Method where the EditorLayer lets the tool know that the editing function
     * has come full circle, so the user interface can be adjusted.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {}

    /**
     * A method that lets the EditorTool know whether its interface should be
     * visible.
     */
    public void setVisible(boolean value) {
        if (face != null) {
            face.setVisible(value);
        }
        visible = value;
    }

    /**
     * A method that lets the EditorTool respond to queries wondering whether
     * its interface is visible.
     */
    public boolean isVisible() {
        if (face != null) {
            return face.isVisible();
        } else {
            return visible; // they should be the same...
        }
    }

    // /////////////////////////////
    // Tool interface methods
    // /////////////////////////////

    protected Container face = null;

    /**
     * The tool's interface. This is added to the tool bar.
     * 
     * @return String The key for this tool.
     */
    public Container getFace() {
        return face;
    }

    /**
     * Called when the parent layer detects that it has been removed from the
     * application.
     */
    public void dispose() {

    }
}