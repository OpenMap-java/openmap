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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/editor/Attic/AbstractDrawingEditorTool.java,v $
// $RCSfile: AbstractDrawingEditorTool.java,v $
// $Revision: 1.5 $
// $Date: 2003/06/02 18:35:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.gui.MouseModeButtonPanel;
import com.bbn.openmap.gui.OMGraphicDeleteTool;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.tools.drawing.*;
import com.bbn.openmap.util.Debug;

/**
 * The AbstractDrawingEditorTool is a EditorTool for the EditorLayer
 * that will use a custom OMDrawingTool to create OMGraphics as needed
 * by the EditorTool.  This is an abstract super class that doesn't
 * provide the actual DrawingToolLoaders that the OMDrawingTool should
 * use.  It does provide the OMDrawingTool and all the button
 * mechanisms organized for smooth behavior integrated with the
 * regular OpenMap mouse modes.
 */
public abstract class AbstractDrawingEditorTool extends AbstractEditorTool 
    implements ActionListener, PropertyChangeListener {

    /**
     * OMDrawingTool handling OMGraphic modifications and creations.
     */
    protected OMDrawingTool drawingTool = null;
    /**
     * A handler on the OMDrawingToolMouseMode that the OMDrawingTool
     * is using, for convenience.  If this handle is not null, then
     * that's an internal signal for this EditorTool to know that it's
     * active and interpreting MouseEvents.  If this is null, and the
     * EditorTool wants events, that's a signal to create a new
     * OMGraphic (see mousePressed).
     */
    protected OMDrawingToolMouseMode omdtmm = null;
    /**
     * The class name of the next thing to create.  Used as a signal
     * to this EditorTool that when the next appropriate MouseEvent
     * comes in, this "thing" should be created.
     */
    protected String thingToCreate = null;
    /**
     * The ButtonGroup to use for the face.
     */
    protected ButtonGroup bg = null;

    /**
     * The button that unpicks all the rest of the tool buttons.  It
     * is kept invisible, but a member of all the other button's
     * ButtonGroup.  When selected, all of the other buttons are
     * deselected.
     */
    protected JToggleButton unpickBtn = null;

    protected GraphicAttributes ga = null;

    /**
     * The MouseDelegator that is controlling the MouseModes.  We need
     * to keep track of what's going on so we can adjust our tools
     * accordingly.
     */
    protected MouseDelegator mouseDelegator;

    /**
     * The ArrayList containing the EditToolLoaders for the drawing
     * tool.
     */
    protected ArrayList loaderList = new ArrayList();

    public final static String RESET_CMD = "RESET_CMD";

    /**
     * The general constructor that can be called from subclasses to
     * initialize the drawing tool and interface.  All that is left to
     * do for subclasses is to add EditToolLoaders to the
     * DrawingEditorTool subclass.
     */
    protected AbstractDrawingEditorTool(EditorLayer layer) {
	super(layer);

	initDrawingTool();

	// Ensures that the drawing tool used by super classes fits 
	// the OMGraphics created by the EditorTool
	layer.setDrawingTool(drawingTool);
    }

    /**
     * Method called in the AbstractDrawingEditorTool constructor.
     */
    public void initDrawingTool() {
	drawingTool = createDrawingTool();
	drawingTool.getMouseMode().setVisible(false);
	ga = drawingTool.getAttributes();
 	ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
 	ga.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
    }

    protected OMDrawingTool createDrawingTool() {
	return new OMDrawingTool();	
    }

    public void addEditToolLoader(EditToolLoader loader) {
	loaderList.add(loader);
	drawingTool.addLoader(loader);
    }

    public void removeEditToolLoader(EditToolLoader loader) {
	loaderList.remove(loader);
	drawingTool.removeLoader(loader);
    }

    public void clearEditToolLoaders() {
	loaderList.clear();
	drawingTool.setLoaders(null);
    }

    /**
     * Add the default (line, poly, rectangle, circle/range rings,
     * point) capabilities to the tool.
     */
    public void initDefaultDrawingToolLoaders() {

	EditToolLoader etl = new OMLineLoader();
	loaderList.add(etl);
	drawingTool.addLoader(etl);

	etl = new OMPolyLoader();
	loaderList.add(etl);
	drawingTool.addLoader(etl);

	etl = new OMRectLoader();
	loaderList.add(etl);
	drawingTool.addLoader(etl);

	etl = new OMCircleLoader();
	loaderList.add(etl);
	drawingTool.addLoader(etl);

	etl = new OMPointLoader();
	loaderList.add(etl);
	drawingTool.addLoader(etl);
    }

    /**
     * The EditorTool method, with the added bonus of resetting the
     * tool if it doesn't want events.
     */
    public void setWantsEvents(boolean value) {
	super.setWantsEvents(value);
	if (!value) {
	    resetForNewGraphic();
	}
    }

    /**
     * Called by findAndInit(Iterator) so subclasses can find
     * objects, too.
     */
    public void findAndInit(Object someObj) {
	super.findAndInit(someObj);

	if (someObj instanceof MapBean ||
	    someObj instanceof InformationDelegator) {
	    drawingTool.findAndInit(someObj);
	}

	if (someObj instanceof MouseDelegator) {
	    setMouseDelegator((MouseDelegator)someObj);
	    // I think we want to handle this differently.  The
	    // EditorToolLayer should get the Gestures MouseMode to
	    // act as a proxy for the drawing tool mouse mode when a
	    // tool is not being used.
 	    drawingTool.findAndInit(someObj);
	}

	if (someObj instanceof OMGraphicDeleteTool) {
	    ((OMGraphicDeleteTool)someObj).findAndInit(getDrawingTool());
	}
	
    }

    public void findAndUndo(Object someObj) {
	super.findAndUndo(someObj);

	if (someObj instanceof MouseDelegator && 
	    someObj == mouseDelegator) {
	    setMouseDelegator(null);
	}

	if (someObj instanceof MapBean ||
	    someObj instanceof InformationDelegator) {
	    drawingTool.findAndUndo(someObj);
	}

	if (someObj instanceof OMGraphicDeleteTool) {
	    ((OMGraphicDeleteTool)someObj).findAndUndo(getDrawingTool());
	}
    }

    /**
     * When a graphic is complete, the drawing tool gets ready to make
     * another.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
	resetForNewGraphic();
    }

    /**
     * Called when the Tool should be reset to draw a new graphic.
     * Currently sets the OMDrawingToolMouseMode to null, which is a
     * signal to the DrawingEditorTool that if an appropriate
     * MouseEvent is provided, that the DrawingTool should be
     * configured to create a new OMGraphic.  If the
     * OMDrawingToolMouseMode is not null, then the MouseEvent is just
     * given to it.
     */
    public void resetForNewGraphic() {
	omdtmm = null;
    }

    /**
     * Does everything to make the DrawingEditorTool go to sleep, and
     * disable all buttons.
     */
    public void totalReset() {
	// Need to check if the tool wants events before just 
	// deactivating the drawing tool - that can mess up a edit
	// session that is unrelated to the tool but still related to
	// the DrawingToolLayer.
	if (wantsEvents()) {
	    thingToCreate = null;
	    setWantsEvents(false);
	    drawingTool.deactivate();
	    if (unpickBtn != null) {
		unpickBtn.doClick();
	    }
	}
    }

    /**
     * Set the OMDrawingTool to use.  It's created internally, though.
     */
    public void setDrawingTool(OMDrawingTool omdt) {
	drawingTool = omdt;
    }

    /**
     * Get the OMDrawingTool to use with this DrawingEditorTool.
     */
    public OMDrawingTool getDrawingTool() {
	return drawingTool;
    }

    /**
     * actionPerformed - Handle the mouse clicks on the button(s)
     */
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	if (command == RESET_CMD) {
	    thingToCreate = null;
	    setWantsEvents(false);
	} else if (command != thingToCreate) {

	    layer.releaseProxyMouseMode();

	    if (thingToCreate != null) {
		// the tool was active with a different thing.
		drawingTool.deactivate();
		resetForNewGraphic();
	    } else if (mouseDelegator != null) {
		mouseDelegator.setActiveMouseModeWithID(getEditorLayer().getMouseMode().getID());
		setWantsEvents(true);
	    }

	    thingToCreate = command;
	}
    }

    /**
     * Method to set up the drawing tool with default behavior in
     * order to create a new OMGraphic.  Will try to deactivate the
     * OMDrawingTool if it thinks it's busy.
     * @param ttc thingToCreate, classname of thing to create
     * @return OMDrawingToolMouseMode of DrawingTool if all goes well,
     * null if the drawing tool can't create the new thingy.
     */
    protected OMDrawingToolMouseMode activateDrawingTool(String ttc) {
	if (drawingTool != null) {
	    drawingTool.setBehaviorMask(OMDrawingTool.GUI_VIA_POPUP_BEHAVIOR_MASK |
					OMDrawingTool.ALT_POPUP_BEHAVIOR_MASK |
					OMDrawingTool.PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK);
	    if (drawingTool.create(ttc, ga, layer) == null) {
		// Something bad happened, might as well try to clean up.
		drawingTool.deactivate();
		return null;
	    }
	    return drawingTool.getMouseMode();
	} else {
	    return null;
	}
    }

    ////////////////////////
    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) { 
	if (omdtmm != null) {
	    omdtmm.mousePressed(e);
	} else if (thingToCreate != null && drawingTool != null) {
	    omdtmm = activateDrawingTool(thingToCreate);
	    // omdtmm is set in activateDrawingTool.
	    if (omdtmm != null) {
		omdtmm.mousePressed(e);
	    }
	}

	return true;
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {      
	if (omdtmm != null) {
	    omdtmm.mouseReleased(e);
	    return true;
	} else {
	    return false;
	}
    }
    
    /**
     * Invoked when the mouse has been clicked on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) { 
	if (omdtmm != null) {
	    omdtmm.mouseClicked(e);
	    return true;
	} else {
	    return false;
	}
    }
    
    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
	if (omdtmm != null) {
	    omdtmm.mouseEntered(e);
	}
    }
    
    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
	if (omdtmm != null) {
	    omdtmm.mouseExited(e);
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
	if (omdtmm != null) {
	    omdtmm.mouseDragged(e);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {  
	if (omdtmm != null) {
	    omdtmm.mouseMoved(e);
	    return true;
	} else {
	    return false;
	}
    }
    
    ///////////////////////////////
    // Tool interface methods
    ///////////////////////////////

    public void setVisible(boolean value) {
	super.setVisible(value);
	if (!value) {
	    totalReset();
	}
    }

    /** 
     * The tool's interface. This is added to the
     * tool bar.
     *
     * @return String The key for this tool.
     */
    public Container getFace() {

	ImageIcon icon = null;

	if (face == null) {

	    JToggleButton btn;
	    face = new JToolBar();
	    ((JToolBar)face).setFloatable(false);

	    Iterator it = loaderList.iterator();
	    if (bg == null) {
		bg = new ButtonGroup();
	    }

	    while (it.hasNext()) {
		EditToolLoader loader = (EditToolLoader)it.next();
		String[] classnames = loader.getEditableClasses();

		for (int i = 0; i < classnames.length; i++) {
		    icon = loader.getIcon(classnames[i]);
		    btn = new JToggleButton(icon, false);
		    btn.setToolTipText(loader.getPrettyName(classnames[i]));
		    btn.setActionCommand(classnames[i]);
		    btn.addActionListener(this);
		    bg.add(btn);

		    face.add(btn);
		}

		unpickBtn = new JToggleButton("", false);
		unpickBtn.setActionCommand(RESET_CMD);
		unpickBtn.addActionListener(this);
		unpickBtn.setVisible(false);
		bg.add(unpickBtn);
		face.add(unpickBtn);
	    }
	    
// 	    face.add(ga.getGUI());

	    face.setVisible(visible);
	}

	return face;
    }
    
    /**
     * Set the MouseDelegator used to hold the different MouseModes
     * available to the map.  
     */
    public void setMouseDelegator(MouseDelegator md) {
	if (mouseDelegator != null) {
	    mouseDelegator.removePropertyChangeListener(this);
	}

	mouseDelegator=md;

	mouseDelegator.addMouseMode(getEditorLayer().getMouseMode());

	if (mouseDelegator == null) {
	    return;
	}

	mouseDelegator.addPropertyChangeListener(this);
    }

    /**
     * Get the MouseDelegator used to control mouse gestures over the
     * map.  
     */
    public MouseDelegator getMouseDelegator() {
	return mouseDelegator;
    }

    /**
     *  Listen for changes to the active mouse mode and for any changes
     *  to the list of available mouse modes
     */
    public void propertyChange(PropertyChangeEvent evt) {
	if (evt.getPropertyName() == MouseDelegator.ActiveModeProperty) {
	    /* If the mouse mode changes, we want to reset ourselves
	     * to be ready to just adjust what's on our layer. */
	    String mmID = ((MapMouseMode)evt.getNewValue()).getID();

	    if (Debug.debugging("editortool")) {
		Debug.output("DTE.propertyChange: mousemode changed to " + mmID);
	    }

	    if (mmID != getEditorLayer().getMouseMode().getID()) {
		totalReset();
	    }
	}
    }
    
}
