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
// $Revision: 1.6 $
// $Date: 2003/09/23 22:53:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.editor;

import com.bbn.openmap.InformationDelegator;
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
	    String ln = getName();
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

	if (someObj instanceof InformationDelegator || 
	    someObj instanceof SelectMouseMode) {
	    getMouseMode().findAndInit(someObj);
	}
    }

    public void findAndUndo(Object someObj) {
	super.findAndUndo(someObj);
	if (editorTool != null) {
	    editorTool.findAndUndo(someObj);
	}

	if (someObj instanceof InformationDelegator || 
	    someObj instanceof SelectMouseMode) {
	    getMouseMode().findAndUndo(someObj);
	}
    }

    public void setMouseModeIDsForEvents(String[] modes) {
	if (elmm == null) {
	    getMouseMode(); // creates the MouseMode...
	}
	String[] newModes = new String[modes.length + 1];
	System.arraycopy(modes, 0, newModes, 0, modes.length);
	newModes[modes.length] = elmm.getID();
	super.setMouseModeIDsForEvents(newModes);
    }

    public String[] getMouseModeIDsForEvents() {
	String[] modes = super.getMouseModeIDsForEvents();
	if (modes == null) {
	    // Set the internal mouse mode as valid, since it hasn't been set yet.
	    setMouseModeIDsForEvents(new String[0]);
	    // Since it's set now, return it.
	    return super.getMouseModeIDsForEvents();
	} else {
	    return modes;
	}
    }

    /**
     * Overriding the OMGraphicHandlerLayer method that sets the
     * StandardMapMouseInterpreter, and setting a
     * DrawingToolLayerInterpreter instead, which allows movement of
     * edited OMGraphics on the downclick.  The mouseModes property
     * needs to be set in the properties file if you want this layer
     * to respond to something different than the SelectMouseMode.
     */
    public synchronized MapMouseListener getMapMouseListener() {
	String[] modeList = getMouseModeIDsForEvents();
	if (modeList != null) {
	    EditorLayerInterpreter interpreter = 
		new EditorLayerInterpreter(this);
	    interpreter.setMouseModeServiceList(modeList);
	    interpreter.setConsumeEvents(getConsumeEvents());
	    interpreter.setGRP(this);
	    return interpreter;
	} else {
	    return null;
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
     * The retrieval key for this tool.  We use the property prefix
     * for the key.
     *
     * @return String The key for this tool.
     */
    public String getKey() {
	return getPropertyPrefix();
    }
    
    /** 
     * Set the retrieval key for this tool.  This call sets the key
     * used for the Tool interface method, which is generally the
     * property prefix used for this layer.  Do not use this lightly,
     * since the ToolPanel may be expecting to find a key that is
     * reflected in the openmap.properties file.
     *
     * @param key The key for this tool.
     */
    public void setKey(String aKey) {
	setPropertyPrefix(aKey);
    }
}

