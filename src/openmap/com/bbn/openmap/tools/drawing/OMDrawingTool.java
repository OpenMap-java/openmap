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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMDrawingTool.java,v $
// $RCSfile: OMDrawingTool.java,v $
// $Revision: 1.12 $
// $Date: 2003/09/23 22:47:16 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.Environment;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.*;
import com.bbn.openmap.gui.OMToolComponent;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.event.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;


/**
 * The OMDrawingTool implements the DrawingTool interface, and can be
 * used to adjust the drawing parameters of OMGraphics.  It is
 * basically a manager for directing MouseEvents and MouseMotionEvents
 * from a Component to a EditableOMGraphic.  The EditableOMGraphic is
 * responsible for interpreting the events and making the adjustments
 * to the OMGraphic it has wrapped within.  The OMDrawingTool also
 * tries to keep the presentation of the OMGraphic up to date, by
 * managing the repaints of the Component to include the graphic being
 * modified. <P>
 *
 * The OMDrawingTool uses a behavior mask to give control over how it
 * behaves.  You can control if the attribute palette appears, if a
 * popup gui appears by default when the editing is complete, or
 * appear when the alt+mouse key or right mouse key is pressed.  You
 * should set this mask if you are not sure about the values that
 * other components may have set on the OMDrawingTool. <P>
 *
 * The OMDrawingTool uses EditToolLoaders to determine what
 * EditableOMGraphic can be used for a particular class name or
 * OMGraphic type.  If a loader for an OMGraphic type is not found,
 * then that OMGraphic type won't be handled, and the tool will react
 * to a create() or edit() call with a null object pointer.  If a
 * loader is found, and the OMgraphic can be edited or modified, then
 * the create() or edit() methods will return a pointer to the
 * OMGraphic being modified. <P>
 *
 * The GUI for the OMDrawingTool is multi-layered.  The OMDrawingTool
 * contains a GraphicsAttributes object, which is an extension of the
 * GraphicAttributes object.  The GraphicAttributes GUI within the
 * tool lets you change the colors, line width and line dash pattern
 * of the current OMGraphic.  The GraphicAttributes conttribution to
 * the GUI is not yet implemented, but will let you change the render
 * type and line type of the OMGraphic.  Finally, the
 * EditableOMGraphic is given an opportunity to change and set
 * parameters of the OMGraphic that is knows about - for instance, the
 * EditableOMLine object will soon provide an interface to set
 * arrowheads on the lines, as well as set the amount of arc a line
 * has (it's currently not implemented). <P>
 */
public class OMDrawingTool extends OMToolComponent
    implements DrawingTool, Serializable, PropertyChangeListener,
    ProjectionListener, EOMGListener, PaintListener, SelectionProvider {

    /**
     * A GraphicAttributes object that describes the current coloring
     * parameters for the current graphic. 
     */
    protected GraphicAttributes graphicAttributes = new GraphicAttributes();
    /** The current graphic being modified. */
    protected EditableOMGraphic currentEditable;
    /**
     * The MouseDelegator to use to get mouse events directed to the
     * DrawingTool.  
     */
    protected MouseDelegator mouseDelegator;
    /**
     * A placeholder for the last mouse mode active before the
     * drawing tool took over.  
     */
    protected MapMouseMode formerMouseMode = null;
    /** The JComponent the drawing tool is servicing, usually the MapBean. */
    protected JComponent canvas;
    /**
     * The objects that know how to create a EditableOMGraphic for a
     * particular class name or OMGraphic.  
     */
    protected Hashtable loaders = new Hashtable();
    /**
     * The MouseMode used for the drawing tool.
     */
    protected OMDrawingToolMouseMode dtmm;

//     // palette variables
//     protected transient JInternalFrame paletteWindow = null;
//     protected transient JFrame paletteWindow2 = null;

    protected DrawingToolRequestor requestor = null;
    /** The current projection. */
    protected Projection projection = null;

    protected SelectionSupport selectionSupport = null;
    
    /**
     * A behavior mask to show the GUI for the OMDrawingTool.
     */
    public final static int SHOW_GUI_BEHAVIOR_MASK = 1 << 0; // + 1
    /**
     * A behavior mask to add a menu item to the popup that will allow
     * the GUI to appear.
     */
    public final static int GUI_VIA_POPUP_BEHAVIOR_MASK = 1 << 1; // + 2
    /**
     * Flag to tell the OMDrawingTool to display a popup when
     * gesturing/modifications appear to be over.  Was the default
     * action of the tool, but was moved to only happening when the
     * ctrl key or right mouse button is pressed.  You can force the
     * old behavior by setting this.
     */
    public final static int USE_POPUP_BEHAVIOR_MASK = 1 << 2; // + 4
    /**
     * Allow a GUI popup to appear over the map when the
     * gesturing/modifications appear to be over, and when the ctrl
     * key or right mouse button is pressed.
     */
    public final static int ALT_POPUP_BEHAVIOR_MASK = 1 << 3; // + 8
    /**
     * Set the flag for the behavior that will tell the OMDrawingTool
     * to *NOT* add the OMDrawingToolMouseMode to the MouseDelegator
     * as the active mouse mode when activated.  Should be called
     * before create/edit is called, and then you have to make sure
     * that you provide MouseEvents to the OMDrawingToolMouseMode or
     * EditableOMGraphic in order to modify the OMGraphic.  Don't call
     * this if you have already started using the tool, it won't do
     * anything unless nothing is currently being modified.
     */
    public final static int PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK = 1 << 4; // + 16
    /**
     * This behavior is used internally, when the OMDrawingTool should
     * be told to clean up as soon as it is safe.
     */
    public final static int DEACTIVATE_ASAP_BEHAVIOR_MASK = 1 << 5; // + 32
    /**
     * A convenience value that tells the OMDrawingTool to show the
     * GUI, and to only display the popup with the ctrl key or right
     * mouse button.  A combination of SHOW_GUI, GUI_VIA_POPUP and
     * ALT_POPUP.
     */
    public final static int DEFAULT_BEHAVIOR_MASK = 11;
    /**
     * A convenience value that tells the OMDrawingTool to not show
     * the GUI, but show the popup with the alt key, and the popup has
     * the ability to bring up the GUI. A combination of GUI_VIA_POPUP
     * and ALT_POPUP.
     */
    public final static int QUICK_CHANGE_BEHAVIOR_MASK = 10;

    /**
     * A integer that is looked at, bitwise, to determine different
     * behaviors.
     */
    protected int behaviorMask = DEFAULT_BEHAVIOR_MASK;

    public final static String LoadersProperty = "OMDrawingTool.loaders";

    protected boolean DEBUG = false;

    protected InformationDelegator informationDelegator = null;

    /**
     * Create a OpenMap Drawing Tool.
     */
    public OMDrawingTool() {
	super();
 	setBorder(BorderFactory.createEmptyBorder());
	DEBUG = Debug.debugging("drawingtool");
	selectionSupport = new SelectionSupport(this);
	setAttributes(new GraphicAttributes());
	setMouseMode(createMouseMode());
    }

    /**
     * Create the mouse mode used with the drawing tool.  Called in
     * the default empty constructor, returns a OMDrawingToolMouseMode
     * by default.
     */
    protected OMDrawingToolMouseMode createMouseMode() {
	return new OMDrawingToolMouseMode(this);
    }

    /**
     * Create a new OMGraphic, encased in a new EditableOMGraphic that
     * can modify it.  If a loader cannot be found that can handle a
     * graphic with the given classname, this method will return a
     * null object.  If you aren't sure of the behavior mask set in
     * the tool, and you want a particular behavior, set it before
     * calling this method.
     *
     * @param classname the classname of the graphic to create.
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic of the classname given, null if the
     * DrawingTool can't create it.  
     */
    public OMGraphic create(String classname, DrawingToolRequestor requestor) {
	return create(classname, null, requestor);
    }

    /**
     * Create a new OMGraphic, encased in a new EditableOMGraphic that
     * can modify it.  If a loader cannot be found that can handle a
     * graphic with the given classname, this method will return a
     * null object.  If you aren't sure of the behavior mask set in
     * the tool, and you want a particular behavior, set it before
     * calling this method.
     *
     * @param classname the classname of the graphic to create.
     * @param ga GraphicAttributes object that contains more
     * information about the type of line to be created.
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic of the classname given, null if the
     * DrawingTool can't create it.  
     */
    public OMGraphic create(String classname, GraphicAttributes ga, 
			    DrawingToolRequestor requestor) {
	return create(classname, ga, requestor, isMask(SHOW_GUI_BEHAVIOR_MASK));
    }

    /**
     * Create a new OMGraphic, encased in a new EditableOMGraphic that
     * can modify it.  If a loader cannot be found that can handle a
     * graphic with the given classname, this method will return a
     * null object.  This method gives you the option of supressing
     * the GUI for the EditableOMGraphic.  If you aren't sure of the
     * behavior mask set in the tool, and you want a particular
     * behavior, set it before calling this method.
     *
     * @param classname the classname of the graphic to create.
     * @param ga GraphicAttributes object that contains more
     * information about the type of line to be created.
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @param showGUI set to true (default) if a GUI showing attribute
     * controls should be displayed.  The behaviorMask will be
     * adjusted accordingly.
     * @return OMGraphic of the classname given, null if the
     * DrawingTool can't create it.  
     */
    public OMGraphic create(String classname, GraphicAttributes ga, 
			    DrawingToolRequestor requestor,
			    boolean showGUI) {

	if (getCurrentEditable() != null) {
	    if (DEBUG) {
		Debug.output("OMDrawingTool.edit(): can't create " + classname + ", drawing tool busy with another graphic.");
	    }
	    return null;
	}

	if (DEBUG) {
	    Debug.output("OMDrawingTool.create(" + classname + ")");
	}

	if (showGUI) {
	    if (DEBUG) Debug.output("OMDrawingTool.create(): showing GUI per request");
	    setMask(SHOW_GUI_BEHAVIOR_MASK);
	} else {
	    if (DEBUG) Debug.output("OMDrawingTool.create(): NOT showing GUI per request");
	    unsetMask(SHOW_GUI_BEHAVIOR_MASK);
	}

	EditableOMGraphic eomg = null;
	EditToolLoader loader = (EditToolLoader)loaders.get(classname);

	if (loader == null) {

	    if (DEBUG) {
		Debug.output("OMDrawingTool.create(" + classname + ") - rechecking loaders");
	    }

	    // The loaders may be able to instantiate objects they
	    // don't want in the GUI - check to see if they can..
	    Iterator things = loaders.values().iterator();
	    while (things.hasNext()) {
		EditToolLoader ldr = (EditToolLoader)things.next();
		
		eomg = ldr.getEditableGraphic(classname, ga);
		if (eomg != null) {
		    break;
		}
	    }

	    if (eomg == null) {
		return null;
		// well, we tried...
	    }

	} else {
	    eomg = loader.getEditableGraphic(classname, ga);
	}
	setAttributes(ga);

	eomg.setShowGUI(isMask(SHOW_GUI_BEHAVIOR_MASK));
	
	eomg.setActionMask(OMGraphic.ADD_GRAPHIC_MASK);

	return edit(eomg, requestor);
    }

    /**
     * Given an OMGraphic, wrap it in the applicable
     * EditableOMGraphic, allow the user to make modifications, and
     * then call requestor.drawingComplete().  If you aren't sure of
     * the behavior mask set in the tool, and you want a particular
     * behavior, set it before calling this method.
     *
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic being modified, null if the OMDrawingTool
     * can't figure out what to use for the modifications. 
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor) {
	return edit(g, requestor, g.getShowEditablePalette());
    }

    /** 
     * Given an OMGraphic, wrap it in the applicable
     * EditableOMGraphic, allow the user to make modifications, and
     * then call requestor.drawingComplete().  This methods gives you
     * the option to supress the GUI from the EditableOMGraphic.  If
     * you aren't sure of the behavior mask set in the tool, and you
     * want a particular behavior, set it before calling this method.
     *
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @param showGUI set to true (default) if a GUI showing attribute
     * controls should be displayed. The behaviorMask will be
     * adjusted accordingly.
     * @return OMGraphic being modified, null if the OMDrawingTool
     * can't figure out what to use for the modifications.  
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor,
			  boolean showGUI) {

	if (getCurrentEditable() != null) {
	    if (DEBUG) {
		Debug.output("OMDrawingTool.edit(): can't edit " + g.getClass().getName() + ", drawing tool busy with another graphic.");
	    }
	    return null;
	}

	Set keys = loaders.keySet();
	this.requestor = requestor;

	paletteTitle = DefaultPaletteTitle;

	if (showGUI) {
	    if (DEBUG) Debug.output("OMDrawingTool.edit(): showing GUI per request");
	    setMask(SHOW_GUI_BEHAVIOR_MASK);
	} else {
	    if (DEBUG) Debug.output("OMDrawingTool.edit(): NOT showing GUI per request");
	    unsetMask(SHOW_GUI_BEHAVIOR_MASK);
	}

	Iterator iterator = keys.iterator();
	while (iterator.hasNext()) {
	    if (DEBUG) Debug.output("OMDrawingTool: looking for loader.");
	    String key = (String)iterator.next();

	    try {
		Class kc = Class.forName(key);
		Class gc = g.getClass();
		if (kc == gc || kc.isAssignableFrom(gc)) {
		    EditToolLoader loader = (EditToolLoader)loaders.get(key);
		    
		    if (loader == null) {
			return null;
		    }
		    
		    // There is a reason why the generation of the
		    // graphic is done here.  I think it has to do
		    // with something with the creation of the
		    // EditableOMGraphic and its display with the
		    // GrabPoints.
		    generateOMGraphic(g);

		    EditableOMGraphic eomg = loader.getEditableGraphic(g);

		    paletteTitle = loader.getPrettyName(key);

		    eomg.setShowGUI(isMask(SHOW_GUI_BEHAVIOR_MASK));
		    eomg.setActionMask(OMGraphic.UPDATE_GRAPHIC_MASK);

		    return edit(eomg, requestor);
		}
	    } catch (ClassNotFoundException cnfe) {
		if (DEBUG) {
		    Debug.output("OMDrawingTool.edit(" + g.getClass().getName() + ") comparision couldn't find class for " + key);
		}
	    }
	} 
	return null;
    }

    /**
     * Given an EditableOMGraphic, use it to make modifications, and
     * then call requestor.drawingComplete().  The requestor is
     * responsible for setting up the correct initial state of the
     * EditableOMGraphic.  The requestor will be given the action mask
     * that is set in the EditableOMGraphic at this point, if no other
     * external modifications to it are made.  If you aren't sure of
     * the behavior mask set in the tool, and you want a particular
     * behavior, set it before calling this method.
     *
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic being modified contained within the
     * EditableOMGraphic.  
     */
    public OMGraphic edit(EditableOMGraphic eomg, 
			  DrawingToolRequestor requestor) {

	if (setCurrentEditable(eomg)) {

	    getGUI();// reset GUI for current EOMG

	    if (DEBUG) {
		Debug.output("OMDrawingTool.edit success");
	    }
	    this.requestor = requestor;
	    if (currentEditable != null) {
		graphicAttributes.setFrom(currentEditable.getGraphic());
		activate();
		if (currentEditable == null) {
		    // In case activating caused something 
		    // strange to happen, most likely with activating
		    // the MouseModes.
		    return null;
		}
		return currentEditable.getGraphic();
	    }
	}

	if (DEBUG) {
	    Debug.output("OMDrawingTool.edit(): can't edit " + eomg.getClass().getName() + ", drawing tool busy with another graphic.");
	}

	return null;
    }

    /**
     * A slightly different edit method, where the EditableOMGraphic
     * is put directly into edit mode, and the mouse events
     * immediately start making modifications to the OMGraphic.  The
     * palette is not shown, but if you set the
     * GUI_VIA_POPUP_BEHAVIOR_MASK on the OMDrawingTool, the option to
     * bring up the drawing tool palette will be presented to the
     * user.  If you aren't sure of the behavior mask set in
     * the tool, and you want a particular behavior, set it before
     * calling this method.
     *
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @param e MouseEvent to use to start editing with.
     * @return OMGraphic being modified.
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor, 
			  MouseEvent e) {

	OMGraphic ret = null;

	if (getCurrentEditable() == null) {
	    ret = edit(g, requestor, false);
	    if (ret != null) {
		currentEditable.getStateMachine().setEdit();
		if (e != null) {
		    GrabPoint gp = currentEditable.getMovingPoint(e);
		    if (gp == null) {
			currentEditable.move(e);
		    } else {
			currentEditable.getStateMachine().setSelected();
		    }
		}
	    }
	} else {
	    if (DEBUG) {
		Debug.output("OMDrawingTool.edit(mouseEvent): can't edit " + g.getClass().getName() + ", drawing tool busy with another graphic.");
	    }
	}

	return ret;
    }

    /**
     * A slightly different edit method, where the EditableOMGraphic
     * is put directly into edit mode, and the mouse events
     * immediately start making modifications to the OMGraphic.  If
     * you aren't sure of the behavior mask set in the tool, and you
     * want a particular behavior, set it before calling this method.
     *
     * @param eomg EditableOMGraphic to modify
     * @param requestor the Component that is requesting the
     * OMGraphic.  The requestor gets notified when the user is
     * finished with the DrawingTool and the graphic is ready.
     * @param e MouseEvent to use to start editing with.
     * @return OMGraphic being modified contained within the
     * EditableOMGraphic.  
     */
    public OMGraphic edit(EditableOMGraphic eomg, 
			  DrawingToolRequestor requestor,
			  MouseEvent e) {
	OMGraphic ret = null;

	if (currentEditable != null) {
	    ret = edit(eomg, requestor);
	    currentEditable.getStateMachine().setEdit();
	    GrabPoint gp = currentEditable.getMovingPoint(e);
	    if (gp == null) {
		currentEditable.move(e);
	    }
	} else {
	    if (DEBUG) {
		Debug.output("OMDrawingTool.edit(): can't edit " + eomg.getClass().getName() + ", drawing tool busy with another graphic.");
	    }
	}

	return ret;
    }

    /**
     * A Vector of Classes that can be handled by the OMDrawingTool.
     * Constructed the first time canEdit() is called after an
     * EditToolLoader is added or removed.
     */
    protected Vector possibleEditableClasses = null;

    /**
     * Return true if the OMDrawingTool can edit the OMGraphic.  Meant
     * to be a low-cost check, with a minimal allocation of memory.
     */
    public boolean canEdit(Class omgc) {
	Iterator iterator;
	if (possibleEditableClasses == null) {
	    Set keys = loaders.keySet();
	    possibleEditableClasses = new Vector(keys.size());
	    iterator = keys.iterator();
	    while (iterator.hasNext()) {
		String key = (String)iterator.next();
		try {
		    possibleEditableClasses.add(Class.forName(key));
		} catch (ClassNotFoundException cnfe) {
		    // Don't worry about this now...
		}
	    }
	}

	iterator = possibleEditableClasses.iterator();
	while (iterator.hasNext()) {
	    Class kc = (Class) iterator.next();
	    if (kc == omgc || kc.isAssignableFrom(omgc)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Set the EditableOMGraphic being used, if it hasn't already been
     * set.  You can set it to null all the time.
     */
    public synchronized boolean setCurrentEditable(EditableOMGraphic eomg) {

	if (currentEditable == null || eomg == null) {

	    if (selectionSupport != null) {
		if (eomg == null && currentEditable != null) {
		    // No longer being edited.
		    selectionSupport.fireSelection(currentEditable.getGraphic(), requestor, false);
		} else if (eomg != null) {
		    // Starting to be edited.
		    selectionSupport.fireSelection(eomg.getGraphic(), requestor, true);
		} // else all is null, ignore...
	    }

	    currentEditable = eomg;
	    if (currentEditable != null) {
		return true;
	    }	
	}

	return false;
    }

    /**
     * Get the current EditableOMGraphic being used by the drawing
     * tool.  Could be null if nothing valid is happening, i.e. if the
     * OMDrawingTool isn't actively editing something.
     */
    public EditableOMGraphic getCurrentEditable() {
	return currentEditable;
    }

    /**
     * If you need your OMDrawingToolMouseMode to do something a
     * little different, you can substitude your subclass here.  Don't
     * set this to null.
     */
    public void setMouseMode(OMDrawingToolMouseMode adtmm) {
	dtmm = adtmm;
    }

    /**
     * If you want to run the drawing tool in passive mode, you'll
     * need a handle on the mouseMode to feed events to.
     */
    public OMDrawingToolMouseMode getMouseMode() {
	return dtmm;
    }

    /**
     * Add an EditToolLoader to the Hashtable of loaders that the
     * OMDrawingTool can use to create/modify OMGraphics.
     */
    public void addLoader(EditToolLoader loader) {
	String[] classnames = loader.getEditableClasses();

	// Add the loader to the hashtable, with the classnames as
	// keys.  Then, when we get a request for a classname, we do
	// a lookup and get the proper loader for the key.
	if (classnames != null) {
	    for (int i = 0; i < classnames.length; i++) {
		loaders.put(classnames[i].intern(), loader);
	    }
	    firePropertyChange(LoadersProperty, null, loaders);
	    possibleEditableClasses = null;
	}
    }

    /**
     * Remove an EditToolLoader from the Hashtable of loaders that the
     * OMDrawingTool can use to create/modify OMGraphics.
     */
    public void removeLoader(EditToolLoader loader) {
	String[] classnames = loader.getEditableClasses();

	// Remove the loader to the hashtable, with the classnames as
	// keys. 
	if (classnames != null) {
	    for (int i = 0; i < classnames.length; i++) {
		EditToolLoader etl = (EditToolLoader)loaders.get(classnames[i].intern());
		if (etl == loader) {
		    loaders.remove(classnames[i]);
		} else {
		    if (DEBUG) {
			Debug.output("DrawingTool.removeLoader: loader to be removed isn't the current loader for " + classnames[i] + ", ignored.");
		    }
		}
	    }
	    firePropertyChange(LoadersProperty, null, loaders);
	    possibleEditableClasses = null;
	}
    }

    /**
     * Get all the loaders the OMDrawingTool has access to.
     */
    public EditToolLoader[] getLoaders() {

	Set keys = loaders.keySet();
	EditToolLoader[] etls = new EditToolLoader[keys.size()];
	
	Iterator iterator = keys.iterator();
	int count = 0;
	while (iterator.hasNext()) {
	    etls[count++] = (EditToolLoader)loaders.get(iterator.next());
	}
	return etls;
    }

    /**
     * Set the loaders that the OMDrawingTool has access to.
     */
    public void setLoaders(EditToolLoader[] etls) {
	loaders.clear();
	for (int i = 0; i < etls.length; i++) {
	    addLoader(etls[i]);
	}
    }

    /**
     * Get the GUI that dictates what the OMDrawingTool has control
     * over.  This should include a section on controlling the
     * GraphicAttributes, a section for controls provided by the
     * current EditableOMGraphic for parameters unique to the EOMG,
     * and any other controls that the tool may need.  <P>
     *
     * To create different types of graphics, the
     * OMDrawingToolMouseMode can be used, to attach to a layer to
     * make it a drawing layer.  The Loaders can be queried to get
     * their trigger graphics so you can load the drawing tool with a
     * particular loader to create a particular graphic.  But here, we
     * just deal with the actual controls over the particular graphic
     * loaded and being modified.<P>
     *
     * @return null if no GUI is to be used - parameters for the EOMG
     * are not to be modified.  Otherwise, return the GUI from the
     * EOMG with a Dismiss button and a remarks text window.
     */
    public Component getGUI() {
	removeAll();
	Component eomgc = null;

	if (currentEditable != null) {
	    // GUI specific to a particular EditableOMGraphic type.
	    eomgc = currentEditable.getGUI(graphicAttributes);
	    if (eomgc != null) {
		add(eomgc);
	    }
	}

	// Basic, generic GUI if eomgc wasn't set.
	if (eomgc == null) {
	    add(graphicAttributes.getGUI());
	}

	return this;
    }

    public void setInformationDelegator(InformationDelegator id) {
	informationDelegator = id;
    }

    public InformationDelegator getInformationDelegator() {
	return informationDelegator;
    }

    /**
     * Put the message in a display line that the OMDrawingTool is using.
     */
    public void setRemarks(String message) {
	if (informationDelegator != null) {
	    informationDelegator.displayInfoLine(message, InformationDelegator.MAP_OBJECT_INFO_LINE);
	}
    }

    /**
     * Just a helper flag to reduce work caused by unnecessary
     * deactivate calls.  Set internally in activate() and deactivate().
     */
    protected boolean activated = false;

    /**
     * Convenience function to tell if the OMDrawingTool is currently
     * working on an OMGraphic.
     */
    public boolean isActivated() {
	return activated;
    }

    /**
     * Turn the OMDrawingTool on, attaching it to the MouseDelegator
     * or the canvas component it is assigned to.  Also brings up the
     * drawing palette.  Called automatically from the create/edit
     * methods.
     */
    protected synchronized void activate() {
	activated = true;

	if (DEBUG) Debug.output("OMDrawingTool: activate()");
	if (currentEditable != null && graphicAttributes != null) {
	    graphicAttributes.setTo(currentEditable.getGraphic());
	    currentEditable.getGraphic().setVisible(false);
	    currentEditable.addEOMGListener(this);
	}

	if (!isMask(PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK)) {
	    if (mouseDelegator != null) {
		if (Debug.debugging("drawingtooldetail")) {
		    Debug.output("OMDrawingTool.activate() mousemode connecting to MouseDelegator");
		}
		formerMouseMode = mouseDelegator.getActiveMouseMode();
		mouseDelegator.setActiveMouseMode(dtmm);

	    } else if (canvas != null) {
		// If a MouseDelegator is not being used, go directly to
		// the MapBean.
		if (Debug.debugging("drawingtooldetail")) {
		    Debug.output("OMDrawingTool.activate() mousemode connecting directly to canvas");
		}
		canvas.addMouseListener(dtmm);
		canvas.addMouseMotionListener(dtmm);
	    } else {
		Debug.error("Drawing Tool can't find a map to work with");
	    }
	}

	// The Drawing tool is added as a projection listener so that
	// it can properly update the current graphic if the map
	// projection changes during graphic creation/edit.

	if (canvas != null) {
	    if (canvas instanceof MapBean) {
  		((MapBean)canvas).addPaintListener(this);
		((MapBean)canvas).addProjectionListener(this);
	    }
	    // Gets the graphic highlighted on the map, if needed.
	    canvas.repaint();
	}

	// Show the gui.
	showPalette();
    }

    /**
     * Turn the drawing tool off, disconnecting it from the
     * MouseDelegator or canvas component, and removing the palette.
     * Called automatically from the mouse mode an GUI when
     * appropriate, although you can force a cleanup if needed by
     * calling this method.  Calling this version of deactivate() just
     * uses the action mask stored in the EditableOMGraphic, which
     * knows if the graphic is being updated or created.
     */
    public void deactivate() {
	int actionMask = 0;
	if (currentEditable != null) {
	    actionMask = currentEditable.getActionMask();
	}
	deactivate(actionMask);
    }

    /**
     * Turn the drawing tool off, disconnecting it from the
     * MouseDelegator or canvas component, and removing the palette.
     * This version can called when you want to control what action is
     * taken by the receiver.
     *
     * @param actionToDoWithOMGraphic a masked int from
     * OMGraphicConstants that describes an OMAction to take on the
     * current editable.
     * @see com.bbn.openmap.omGraphics.OMGraphicConstants
     */
    public synchronized void deactivate(int actionToDoWithOMGraphic) {
	if (DEBUG) {
	    Debug.output("OMDrawingTool: deactivate(" + 
			 (activated?"while active":"while inactive") + ")");
	}

	// Don't waste effort;
	if (!activated) return;

	if (!isMask(PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK)) {
	    if (mouseDelegator != null) {
		mouseDelegator.setActiveMouseMode(formerMouseMode);
		mouseDelegator.removeMouseMode(dtmm);
	    } else if (canvas != null) {
		// If a MouseDelegator is not being used, go directly to
		// the canvas.
		canvas.removeMouseListener(dtmm);
		canvas.removeMouseMotionListener(dtmm);
	    }
	}

	if (canvas != null) {
	    if (canvas instanceof MapBean) {
		((MapBean)canvas).removeProjectionListener(this);
  		((MapBean)canvas).removePaintListener(this);
	    }
	}

	// hide the gui.
	hidePalette();

	OMGraphic g = null;

	if (currentEditable != null) {
	    g = currentEditable.getGraphic();
	    currentEditable.removeEOMGListener(this);
	    if (g != null && requestor != null) {
		g.setVisible(true);
		OMAction action = new OMAction();
		action.setMask(actionToDoWithOMGraphic);
		generateOMGraphic(g);
		notifyListener(g, action);
	    }
	}
	setCurrentEditable(null);
	unsetMask(DEACTIVATE_ASAP_BEHAVIOR_MASK);
	popup = null;
	activated = false;
    }

    /**
     * If the projection is not null, generate the OMGraphic.
     */
    protected void generateOMGraphic(OMGraphic g) {
	if (g != null && g.getNeedToRegenerate()) {
	    Projection proj = getProjection();
	    if (proj != null) {
		g.generate(proj);
	    } else if (DEBUG) {
		Debug.output("OMDrawingTool: graphic needs generation: " + g.getNeedToRegenerate());
	    }
	}
    }

    /**
     * Notify the listener of an action to a graphic.
     * @param graphic the graphic being created/modified
     * @param action the OMAction telling the listener what to do with
     * the graphic.
     */
    public void notifyListener(OMGraphic graphic, OMAction action) {
	if (requestor != null) {
	    if (DEBUG) Debug.output("OMDrawingTool: notifying requestor, graphic with action");
	    requestor.drawingComplete(graphic, action);
	}

	// in case the requestor is a layer that is not visible
	if (canvas != null) {
	    canvas.repaint();
	}
    }

    /**
     * ProjectionListener method.  Helps if the currentEditable is set.
     */
    public void projectionChanged(ProjectionEvent e) {
	setProjection((Projection)e.getProjection().makeClone());
    }

    /**
     * Set the current projection.  Tells the currentEditable what it
     * is too.
     */
    public void setProjection(Projection proj) {
	projection = proj;
	if (currentEditable != null) {
	    currentEditable.setProjection(projection);
	}
    }

    /**
     * Get the current projection, if one has been provided.  If one
     * has not been provided, then the canvas is checked to see if it
     * is a MapBean.  If it is, then that projection is returned.  If
     * that doesn't work, it will finally return null.
     */
    public Projection getProjection() {
	if (projection == null && canvas instanceof MapBean) {
	    projection = ((MapBean)canvas).getProjection();
	}
	return projection;
    }

    /**
     * Set the GraphicAttributes object used to fill the OMGraphic
     * java.awt.Graphics parameters.
     */
    public void setAttributes(GraphicAttributes da) {
	if (graphicAttributes != null) {
	    graphicAttributes.getPropertyChangeSupport().removePropertyChangeListener(this);
	}

	if (da == null) {
	    graphicAttributes = GraphicAttributes.DEFAULT;
	} else {
	    graphicAttributes = da;
	}

	graphicAttributes.getPropertyChangeSupport().addPropertyChangeListener(this);

	if (currentEditable != null) {
	    graphicAttributes.setTo(currentEditable.getGraphic());
	}
    }

    /**
     * Get the DrawingAttributes driving the parameters of the current
     * graphic.
     */
    public GraphicAttributes getAttributes() {
	return graphicAttributes;
    }

    /**
     * PaintListener interface.  We want to know when the canvas is
     * repainted.
     * 
     * @param g the Graphics to draw into.
     */
    public void listenerPaint(Graphics g) {
	// Call repaintRender here because if the graphic is in the
	// middle of being moved, we'll draw it in the mouse event
	// thread.  Otherwise, it gets set in the image for the
	// background, which looks bad.
	if (currentEditable != null) {
	    // do g.create() to prevent Stroke remnants from affecting
	    // the Border of the canvas.
	    currentEditable.repaintRender(g.create());
	}
    }

    /**
     * Set the MouseDelegator used to receive mouse events.
     */
    public void setMouseDelegator(MouseDelegator md) {
	mouseDelegator = md;
    }

    /**
     * Get the MouseDelegator used to receive mouse events.
     */
    public MouseDelegator getMouseDelegator() {
	return mouseDelegator;
    }

    public void setCursor(java.awt.Cursor cursor) {
	if (canvas != null) {
	    canvas.setCursor(cursor);
	}
    }

    public java.awt.Cursor getCursor() {
	if (canvas != null) {
	    return canvas.getCursor();
	} else {
	    return null;
	}
    }

    /**
     * Set the JComponent this thing is directing events for.  If the
     * MouseDelegator is not set, the Canvas is contacted to get
     * MouseEvents from.  Within the BeanContext, the OMDrawingTool
     * looks for MapBeans to use as canvases.
     */
    public void setCanvas(JComponent can) {
	canvas = can;
    }

    /**
     * Get the JComponent this thing is directing events for.
     */
    public JComponent getCanvas() {
	return canvas;
    }

    /**
     * Set whether the Tool's face should be used.  The subclasses to
     * this class should either remove all components from its face,
     * or make its face invisible if this is set to false.
     */
    public void setUseAsTool(boolean value) {
	super.setUseAsTool(value);
    }

    /**
     * Called from the findAndInit(Iterator) method, when objects are
     * added to the MapHandler. so the OMDrawingTool can hook up with
     * what it needs.  An InformationDelegator is used to provide map
     * coordinates of the mouse movements.  The MouseDelegator is used
     * to intercept MouseEvents when the OMDrawingTool is activated.
     * The MapBean is used to get mouse events if the MouseDelegator
     * isn't loaded, and is also used to help out with smooth
     * repaints() in general.  EditToolLoaders are looked for to load
     * into the OMDrawingTool to handler different graphic requests.
     */
    public void findAndInit(Object someObj) {

	if (someObj instanceof InformationDelegator) {
	    if (DEBUG) Debug.output("DrawingTool: found InformationDelegator");
	    if (dtmm != null) {
		dtmm.setInfoDelegator((InformationDelegator)someObj);
	    }
	    setInformationDelegator((InformationDelegator)someObj);
	}
	if (someObj instanceof MouseDelegator) {
	    if (DEBUG) Debug.output("DrawingTool: found MouseDelegator.");
	    setMouseDelegator((MouseDelegator)someObj);
	}
	if (someObj instanceof MapBean) {
	    if (DEBUG) Debug.output("DrawingTool: found MapBean.");
	    setCanvas((JComponent)someObj);
	}
	if (someObj instanceof EditToolLoader) {
	    if (DEBUG) Debug.output("DrawingTool: found EditToolLoader.");
	    addLoader((EditToolLoader)someObj);		
	}
    }

    /**
     * Called by childrenRemoved, it provides a good method for
     * handling any object you may want to take away from the
     * OMDrawingTool.  The OMDrawingTool figures out if it should
     * disconnect itseld from the object.
     */
    public void findAndUndo(Object someObj) {
	if (someObj == getInformationDelegator()) {
	    if (dtmm != null && dtmm.getInfoDelegator() == (InformationDelegator)someObj) {
		dtmm.setInfoDelegator(null);
	    }
	    setInformationDelegator(null);
	}
	if (someObj == getMouseDelegator()) {
	    setMouseDelegator(null);
	}
	if (someObj == getCanvas()) {
	    setCanvas(null);
	}
	if (someObj instanceof EditToolLoader) {
	    removeLoader((EditToolLoader)someObj);
	}
    }

    ////////////////  end BeanContext stuff

    /**
     *  The palette title.
     */
    public final static String DefaultPaletteTitle = "Drawing Tool";
    public static String paletteTitle = DefaultPaletteTitle;

    /**
     * Display the palette.
     */
    public void showPalette() {
	Debug.message("drawingtool", "OMDrawingTool.showPalette()");

	getGUI(); // resets the gui.
	setVisible(true); // just to make sure.

// 	if (!getUseAsTool() && getWindowSupport() == null) {
// 	    Debug.output("Setting window support in OMDrawingTool");
// 	    setWindowSupport(new WindowSupport(getGUI(), "Drawing Tool"));
// 	}

// 	WindowSupport ws = getWindowSupport();

// 	if (ws != null && !getUseAsTool()) {
// 	    Debug.output("OMDrawingTool.showPalette(): showing palette");
// 	    int w = getWidth();
// 	    int h = getHeight();
// 	    Dimension dim = ws.getComponentSize();
// 	    if (dim != null) {
// 		w = (int)dim.getWidth();
// 		h = (int)dim.getHeight();
// 	    }

// 	    int x = 10;
// 	    int y = 10;
	    
// 	    Point loc = ws.getComponentLocation();
// 	    if (loc != null) {
// 		x = (int) loc.getX();
// 		y = (int) loc.getY();
// 	    }

// 	    ws.displayInWindow(x, y, w, h);
// 	} else {
// 	    Debug.output("OMDrawingTool.showPalette(): NOT showing palette, ws == null:" + 
// 			 (ws == null) + ", used as tool:" + getUseAsTool());
// 	}

    }
    
    /**
     * Hide the OMDrawingTool palette.
     */
    public void hidePalette() {
	Debug.message("drawingtool", "OMDrawingTool.hidePalette()");
	getGUI();

// 	WindowSupport ws = getWindowSupport();
// 	if (ws != null) {
// 	    ws.killWindow();
// 	}

    }

    /**
     * A integer that is looked at internally, bitwise, to determine
     * different behaviors.  If you care about specific behavior of
     * the DrawingTool, you should set this to what you want to make
     * sure the tool acts the way you want.
     */
    public void setBehaviorMask(int mask) {
	behaviorMask = mask;
    }

    /**
     * A integer that is looked at internally, bitwise, to determine
     * different behaviors.
     */
    public int getBehaviorMask() {
	return behaviorMask;
    }

    /**
     * Set a particular mask bit in the internal value.
     * @param mask an OMDrawingTool behavior mask.
     * @return the changed integer value.
     */
    public int setMask(int mask){
	behaviorMask = OMAction.setMask(behaviorMask, mask);
	return behaviorMask;
    }

    /** 
     * Unset a particular mask bit in the internal value.
     * @param mask an OMDrawingTool behavior mask.
     * @return the changed integer value.
     */
    public int unsetMask(int mask){
	behaviorMask = OMAction.unsetMask(behaviorMask, mask);
	return behaviorMask;
    }

    /** 
     * Return whether a mask value is set in the internal value.
     * @param mask an OMDrawingTool behavior mask.
     * @return whether the value bit is set on the internal value.
     */
    public boolean isMask(int mask){
	return OMAction.isMask(behaviorMask, mask);
    }

    /**
     * PropertyChangeListener method.  If DrawingAttribute parameters
     * change, this method is called, and we update the OMGraphic
     * parameters.  
     */
    public void propertyChange(PropertyChangeEvent pce) {
	Object source = pce.getSource();
	if (source instanceof DrawingAttributes &&
	    currentEditable != null) {
	    graphicAttributes.setTo(currentEditable.getGraphic());
	    
	    if (projection != null) {
		currentEditable.regenerate(projection);
	    }

	    if (canvas != null) {
		canvas.repaint();
	    }
	}
    }

    /**
     * Used to hold the last thing displayed to the remarks window.
     */
    String lastRemarks = "";
    JPopupMenu popup = null;

    /**
     * This is a EOMGListener method, and gets called by the
     * EditableOMGraphic when something changes.
     */
    public void eomgChanged(EOMGEvent event) {
	Cursor cursor = event.getCursor();
	if (cursor != null) {
	    setCursor(cursor);
	}

	// We might have used the InformationDelgator to put the comments
	// in the info line, but that can't work, because we are
	// already putting the lat/lon info on the info line.

	// Updated, 4.6 - now that the InformationDelegator has new
	// places for coordinate information and map object
	// information, we can sent the info there, and it looks OK.

	String message = event.getMessage().intern();
	if (message != null && message != lastRemarks) {
	    lastRemarks = message;
	    setRemarks(message);
	}
	EditableOMGraphic source = event.getSource();

	// EOMGDefined means unselected.
	if (source.getStateMachine().getState()
	    instanceof com.bbn.openmap.omGraphics.editable.EOMGDefinedState) {
	    if (DEBUG) Debug.output("OMDrawingTool.eomgChanged(): try for menu.");
	    if (currentEditable != null) {
		GrabPoint gp = source.getMovingPoint();
		if (gp != null) {

		    /** Let's see if we should bring up pop-up menu
		     * with all sorts of lovely options - if the right
		     * mouse key was pressed, or if the ctrl key was
		     * pressed with the mouse button being released,
		     * display the option menu.  Otherwise, just get
		     * ready to end. */

		    MouseEvent me = event.getMouseEvent();
		    boolean showPopup = false;

		    boolean theCorrectMouseKeys = (me != null && (me.isControlDown() || (me.getModifiers() & InputEvent.BUTTON3_MASK) > 0));

		    if (popup == null && (theCorrectMouseKeys || isMask(USE_POPUP_BEHAVIOR_MASK)) && !getUseAsTool()) {
			popup = createPopupMenu();
			showPopup = true;
		    } else {
			setMask(DEACTIVATE_ASAP_BEHAVIOR_MASK);
		    }

		    JComponent map = null;
		    if (mouseDelegator != null) {
			map = mouseDelegator.getMap();
		    } else if (canvas != null) {
			// If a MouseDelegator is not being used, go
			// directly to the MapBean.
			map = canvas;
		    }

		    if (showPopup) {
			if (map != null) {
			    popup.show(map, gp.getX(), gp.getY());
			} else {
			    Debug.error("OMDrawingTool: no component to show popup on!");
			}
		    }
		} else {
		    if (DEBUG) Debug.output("OMDrawingTool.eomgChanged(). no valid location for popup supplied.");
		}
	    } else {
		if (DEBUG) Debug.output("OMDrawingTool.eomgChanged(). no currentEditable to deal with.");
	    }
	}
    }

    public JPopupMenu createPopupMenu() {
	JPopupMenu pum = new JPopupMenu();
	JMenuItem delete = new JMenuItem("Delete");
	delete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    OMAction action = new OMAction();
		    action.setMask(OMGraphic.DELETE_GRAPHIC_MASK);
		    EditableOMGraphic eomg = getCurrentEditable();
		    if (eomg != null) {
			OMGraphic g = eomg.getGraphic();
			if (g != null) {
			    notifyListener(g, action);
			}
		    }
		    setCurrentEditable(null);
		    deactivate();
		}
	    });

// 	JMenuItem done = new JMenuItem("Done");
// 	done.addActionListener(new ActionListener() {
// 		public void actionPerformed(ActionEvent ae) {
// 		    deactivate();
// 		}
// 	    });

	JMenu gui = new JMenu("Change Appearance ");
	gui.add(this);

// 	JMenuItem gui = new JMenuItem("Change Appearance ");
// 	gui.addActionListener(new ActionListener() {
// 		public void actionPerformed(ActionEvent ae) {
// 		    EditableOMGraphic eomg = getCurrentEditable();
// 		    if (eomg != null) {
// 			boolean previous = eomg.getShowGUI();
// 			eomg.setShowGUI(true);
// 			showPalette();
// 			eomg.setShowGUI(previous);
// 			eomg.getStateMachine().setSelected();
// 		    }
// 		}
// 	    });

// 	JMenuItem cancel = new JMenuItem("Continue Making Changes");
// 	cancel.addActionListener(new ActionListener() {
// 		public void actionPerformed(ActionEvent ae) {
// 		}
// 	    });

	JMenuItem reset = new JMenuItem("Undo Changes");
	reset.setEnabled(false);
	reset.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    if (currentEditable != null) {
			currentEditable.reset();
		    }
		}
	    });

// 	pum.add(done);
// 	pum.addSeparator();
	if (isMask(GUI_VIA_POPUP_BEHAVIOR_MASK)) {
	    pum.add(this);
	    pum.addSeparator();
	}
//  	pum.add(reset);
	pum.add(delete);
// 	pum.addSeparator();
// 	pum.add(cancel);
	return pum;
    }

    //////////// SelectionListener support
    public void addSelectionListener(SelectionListener list) {
	if (selectionSupport != null) selectionSupport.addSelectionListener(list);
    }

    public void removeSelectionListener(SelectionListener list) {
	if (selectionSupport != null) selectionSupport.removeSelectionListener(list);
    }

    public void clearSelectionListeners() {
	if (selectionSupport != null) selectionSupport.clearSelectionListeners();
    }
    //////////// SelectionListener support ends

    public static void main(String[] args) {
	OMDrawingTool omdt = new OMDrawingTool();
	omdt.showPalette();
    }
}
