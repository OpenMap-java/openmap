// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/AbstractEditorTool.java,v $
// $RCSfile: AbstractEditorTool.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.*;
import com.bbn.openmap.util.Debug;

public class AbstractEditorTool implements EditorTool {

    /**
     * Flag to let it's layer know when it wants control over mouse
     * events.
     */
    protected boolean wantsEvents = false;
    /**
     * The parent layer.
     */
    protected EditorLayer layer = null;
    /** Used as a placeholder if face is null. */
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
    protected AbstractEditorTool(EditorLayer eLayer) {
	layer = eLayer;
    }

    public void setEditorLayer(EditorLayer eLayer) {
	layer = eLayer;
    }

    public EditorLayer getEditorLayer() {
	return layer;
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
     * Part of the interface where the EditorLayer can provide
     * components that are available via the MapHandler/BeanContext.
     * The object is something that has been added to the MapHandler.
     */
    public void findAndInit(Object obj) {}

    /**
     * Part of the interface where the EditorLayer can provide
     * components that are available via the MapHandler/BeanContext.
     * The object is something that has been removed from the MapHandler.
     */
    public void findAndUndo(Object obj) {}

    /**
     * Method where the EditorLayer lets the tool know that the
     * editing function has come full circle, so the user interface
     * can be adjusted.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {}

    /**
     * A method that lets the EditorTool know whether its interface
     * should be visible.
     */
    public void setVisible(boolean value) {
	if (face != null) {
	    face.setVisible(value);
	}
	visible = value;
    }

    /**
     * A method that lets the EditorTool respond to queries wondering
     * whether its interface is visible.
     */
    public boolean isVisible() {
	if (face != null) {
	    return face.isVisible();
	} else {
	    return visible;  // they should be the same...
	}
    }

    ////////////////////////
    // Mouse Listener events
    ////////////////////////

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  For the EditorTool, it is going to be fed
     * events from the SelectMouseMode "Gestures".
     */
    public String[] getMouseModeServiceList() {
	return null;  // we are going to be fed mouse events.
    }
    
    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) { 
	return false;
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {      
	return false;
    }
    
    /**
     * Invoked when the mouse has been clicked on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) { 
	return false;
    }
    
    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}
    
    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}
    
    ///////////////////////////////
    // Mouse Motion Listener events
    ///////////////////////////////
    
    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.  The listener will receive these events if it
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {      
	return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {  
	return false;
    }
    
    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {}

    ///////////////////////////////
    // Tool interface methods
    ///////////////////////////////

    protected Container face = null;

    /** 
     * The tool's interface. This is added to the
     * tool bar.
     *
     * @return String The key for this tool.
     */
    public Container getFace() {
	return face;
    }
}
