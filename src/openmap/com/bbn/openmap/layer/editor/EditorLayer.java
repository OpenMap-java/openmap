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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/EditorLayer.java,v $
// $RCSfile: EditorLayer.java,v $
// $Revision: 1.3 $
// $Date: 2003/08/22 16:14:16 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.editor;

import com.bbn.openmap.event.*;
import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.layer.DrawingToolLayer;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.tools.drawing.*;
import com.bbn.openmap.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Properties;
import javax.swing.*;

/**
 * The EditorLayer is a layer that provides a specific set of tools to
 * modify a set of OMGraphics that represent specific types of
 * objects.  It has an EditorTool that controls what the interface
 * looks like, and controls reception of the mouse events to direct
 * their interpretation usefully.  The EditorLayer can use the
 * following property:
 * <pre>
 * # could be com.bbn.openmap.layer.editor.DrawingEditorTool, for instance
 * editorLayer.editor=EditorTool class
 * </pre>
 */
public class EditorLayer extends DrawingToolLayer implements Tool {

    /**
     * The EditorTool controls the interface, and how OMGraphics are
     * managed.
     */
    protected EditorTool editorTool = null;

    protected EditorLayerMouseMode elmm = null;

    /**
     * The property to use of the EditorLayer doesn't really know what
     * EditorTool it will have.  This property is used in
     * setProperties if the EditorTool isn't already set.  If you
     * extend the EditorLayer and specifically set the EditorTool in
     * the constructor, this property will be ignored.
     */
    public final static String EditorToolProperty = "editor";

    public EditorLayer() {}

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);

	if (editorTool == null) {
	    String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
	    String editorClassName = props.getProperty(realPrefix + EditorToolProperty);
	    if (editorClassName != null) {
		// Try to create with this layer as an argument.
		Object[] objArgs = {this};

		editorTool = (EditorTool)ComponentFactory.create(editorClassName, objArgs, prefix, props);
		
		if (editorTool == null) {
		    // OK, try to create with an empty constructor.
		    editorTool = (EditorTool)ComponentFactory.create(editorClassName, prefix, props);
		}

		if (editorTool == null) {
		    String additionalInfo = ".";
		    if (editorClassName != null) {
			additionalInfo = ", although an editor tool class (" +
			    editorClassName + ") was defined.";
		    }
		    Debug.error(getName() + 
				" doesn't have a EditorTool defined" + 
				additionalInfo);
		}
	    }
	}
    }

    /**
     * Get and/or create the EditorLayerMouseMode that can be used
     * specifically for this layer, used to capture the MapBean's
     * MouseEvents when an EditorTool is invoked.  The
     * EditorLayerMouseMode is invisible, meaning it won't show up in
     * standard OpenMap GUI widgets as a viable MouseMode.  It is
     * expected that the EditorTool will compensate for displaying
     * what is going on.
     */
    public EditorLayerMouseMode getMouseMode() {
	if (elmm == null) {
	    String ln = layer.getName();
	    if (ln == null) {
		// Try something unique
		ln = this.getClass().getName() + System.currentTimeMillis();
	    }
	    elmm = new EditorLayerMouseMode(ln.intern(), true);
	}
	return elmm;
    }

    /**
     * DrawingToolRequestor method.  It's actually pretty important to
     * call EditorTool.drawingComplete() from here, too, if you create
     * a subclass to EditorLayer.  The EditorTool needs to know this
     * to reset the drawing tool mouse mode, to get ready for another
     * new OMGraphic if necessary.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
	super.drawingComplete(omg, action);
	if (editorTool != null) {
	    editorTool.drawingComplete(omg, action);
	}
    }

    /**
     * Called by findAndInit(Iterator) so subclasses can find
     * objects, too.
     */
    public void findAndInit(Object someObj) {
	// We don't want the EditorLayer to find the DrawingTool
	// in the MapHandler.  The EditorTool should set its own.
	if (!(someObj instanceof DrawingTool)) {
	    super.findAndInit(someObj);
	}

	if (editorTool != null) {
	    editorTool.findAndInit(someObj);
	}
    }

    public void findAndUndo(Object someObj) {
	super.findAndUndo(someObj);
	if (editorTool != null) {
	    editorTool.findAndUndo(someObj);
	}
    }

    ////////////////////////
    // Map Mouse Listener events and methods
    ////////////////////////

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
	if (elmm == null) {
	    getMouseMode(); // creates the MouseMode...
	}
	String[] services = { SelectMouseMode.modeID, elmm.getID() };
	return services;
    }
    
    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) { 
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mousePressed(e);
	    return true;
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
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseDragged(e);
	    return true;
	} else {
	    return super.mouseDragged(e);
	}
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {  
	if (editorTool != null && editorTool.wantsEvents()) {
	    editorTool.mouseMoved(e);
	    return true;
	} else {
	    return super.mouseMoved(e);
	}
    }
    
    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {
	if (editorTool != null && editorTool.wantsEvents()) {
 	    editorTool.mouseMoved();
	} else {
	    super.mouseMoved();
	}
    }

    /**
     * Part of a layer hack to notify the component listener when the
     * component is hidden.  These components don't receive the
     * ComponentHidden notification.  Remove when it works.
     */
    public void setVisible(boolean show) {
	if (editorTool != null) {
	    editorTool.setVisible(show);
	}
	super.setVisible(show);
    }	

    ///////////////////////////////
    // Tool interface methods
    ///////////////////////////////

    /** 
     * The tool's interface. This is added to the
     * tool bar.
     *
     * @return String The key for this tool.
     */
    public Container getFace() {
	if (editorTool != null) {
	    return editorTool.getFace();
	} else {
	    return new JPanel();
	}
    }
    
    /** 
     * The retrieval key for this tool.  We use the layer name in this
     * case.
     *
     * @return String The key for this tool.
     **/
    public String getKey() {
	return getName();
    }
    
    /** 
     * Set the retrieval key for this tool.  This call sets the
     * layer's name, so it doesn't need to be called if that is
     * already done.
     *
     * @param key The key for this tool.
     */
    public void setKey(String aKey) {
	setName(aKey);
    }

}

