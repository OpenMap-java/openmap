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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/Attic/EditorLayerInterpreter.java,v $
// $RCSfile: EditorLayerInterpreter.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/23 22:53:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.editor;

import java.awt.event.MouseEvent;
import com.bbn.openmap.layer.DrawingToolLayerInterpreter;

public class EditorLayerInterpreter extends DrawingToolLayerInterpreter {

    public EditorLayerInterpreter(EditorLayer l) {
	super(l);
    }
 
    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) { 
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mousePressed(e);
	    return consumeEvents;
	} else {
	    return super.mousePressed(e);
	}
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {      
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseReleased(e);
	    return true;
	} else {
	    return super.mouseReleased(e);
	}
    }
    
    /**
     * Invoked when the mouse has been clicked on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) { 
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseClicked(e);
	    return true;
	} else {
	    return super.mouseClicked(e);
	}
    }
    
    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseEntered(e);
	} else {
	    super.mouseEntered(e);
	}
    }
    
    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseExited(e);
	} else {
	    super.mouseExited(e);
	}
    }
    
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
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseDragged(e);
 	    return consumeEvents;
	} else {
 	    return super.mouseDragged(e) && consumeEvents;
	}
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {  
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseMoved(e);
 	    return consumeEvents;
	} else {
 	    return super.mouseMoved(e) && consumeEvents;
	}
    }
    
    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {
	EditorTool editorTool = ((EditorLayer)layer).editorTool;
	if (editorTool != null && editorTool.wantsEvents()) {
 	    editorTool.mouseMoved();
	} else {
	    super.mouseMoved();
	}
    }

}
