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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/DrawingToolLayer.java,v $
// $RCSfile: DrawingToolLayer.java,v $
// $Revision: 1.3 $
// $Date: 2003/02/20 02:43:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Properties;
import javax.swing.*;

/**
 * This layer can receive graphics from the OMDrawingToolLauncher, and
 * also sent it's graphics to the OMDrawingTool for editing. <P>
 *
 * The projectionChanged() and paint() methods are taken care of in
 * the OMGraphicHandlerLayer superclass.
 */
public class DrawingToolLayer extends OMGraphicHandlerLayer 
    implements MapMouseListener, DrawingToolRequestor {

    /** Get a handle on the DrawingTool. */
    protected OMDrawingTool drawingTool;

    /** For callbacks on editing... */
    protected final DrawingToolRequestor layer = this;

    /**
     * A flag to provide a tooltip over OMGraphics to click to edit.
     */
    protected boolean showHints = true;

    public final static String ShowHintsProperty = "showHints";

    public DrawingToolLayer() {
	setAddToBeanContext(true);
    }

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix,props);

	String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
	showHints = LayerUtils.booleanFromProperties(props, realPrefix + ShowHintsProperty, showHints);
    }
    
    /**
     * Overriding the OMGraphicHandlerMethod, creating a list if it's null.
     */
    public OMGraphicList getList() {
	OMGraphicList list = super.getList();
	if (list == null) {
	    list = new OMGraphicList();
	    super.setList(list);
	}
	return list;
    }
	
    public OMDrawingTool getDrawingTool() {
	return drawingTool;
    }

    public void setDrawingTool(OMDrawingTool dt) {
	drawingTool = dt;
    }

    /**
     * DrawingToolRequestor method.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
	getList(); // create a list if there isn't one.
	doAction(omg, action);
	repaint();
	Debug.message("dtl", "DrawingToolLayer: DrawingTool complete");
    }

    /**
     * Called by findAndInit(Iterator) so subclasses can find
     * objects, too.
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof OMDrawingTool) {
	    Debug.message("dtl", "DrawingToolLayer: found a drawing tool");
	    setDrawingTool((OMDrawingTool)someObj);
	}
    }
    
    /**
     * BeanContextMembershipListener method.  Called when a new object
     * is removed from the BeanContext of this object.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof DrawingTool) {
	    if (getDrawingTool() == (DrawingTool)someObj) {
		setDrawingTool(null);
	    }
	}
    }

    /**
     * Note: A layer interested in receiving amouse events should
     * implement this function .  Otherwise, return the default, which
     * is null.
     */
    public synchronized MapMouseListener getMapMouseListener() {
	return this;
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
	String[] services = {SelectMouseMode.modeID};// what are other possibilities in OpenMap
	return services;
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
	boolean ret = false;
	OMGraphic omgr = ((OMGraphicList)getList()).findClosest(e.getX(), e.getY(), 4);

	if (omgr != null) {
	    OMDrawingTool dt = getDrawingTool();
	    if (dt != null) {
		dt.setBehaviorMask(OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);
		// The first check is to find out if the tool is already busy
		// on another graphic.  If it is (null), then
		// deactivate and try again.  If it fails again, then
		// the tool can't handle the omgr.

		// This is fine for OMGraphics that are not near to each other, but not for neighbors.
		if (dt.edit(omgr, layer, e) == null) {
		    dt.deactivate();
		    dt.edit(omgr, layer, e);
		}
	    }
	    ret = true;
	}
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
     * Get the behavior mask used for the drawing tool when an
     * OMGraphic is clicked on, and editing starts.  Controls how the
     * drawing tool will behave, with respect to its GUI, etc.
     */
    public int getDrawingToolEditBehaviorMask() {
	return OMDrawingTool.DEFAULT_BEHAVIOR_MASK;
    }
    
    /**
     * Invoked when the mouse has been clicked on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) { 
	boolean ret = false;

	OMGraphic omgr = ((OMGraphicList)getList()).findClosest(e.getX(), e.getY(), 4);
	if (omgr != null) {
	    OMDrawingTool dt = getDrawingTool();
	    if (dt != null) {
		dt.setBehaviorMask(getDrawingToolEditBehaviorMask());
		// We don't seem to need to check to find out if another
		// graphic is using the drawing tool, the check in
		// mousePressed takes care of that.
		dt.edit(omgr, layer);
	    }
	    ret = true;
	}
	
	return ret;
    }
    
    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
	return;
    }
    
    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
	return;
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
	return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {  
	OMGraphic omgr = ((OMGraphicList)getList()).findClosest(e.getX(),e.getY(),4.0f);

	if (omgr != null) {
// 	    fireRequestInfoLine("Click to edit graphic.");
	    if (showHints) {
		String tt = getToolTipForOMGraphic(omgr);
		if (tt != null) {
		    fireRequestToolTip(e, tt);
		    return true;
		} else {
		    fireHideToolTip(e);
		}
	    }
	} else {
// 	    fireRequestInfoLine("");
	    if (showHints) {
		fireHideToolTip(e);
	    }
	}
	return false;
    }
    
    /**
     * Called by default in the MouseMoved method, in order to fire a
     * ToolTip for a particular OMGraphic.  Return a String if you
     * want a ToolTip displayed, null if you don't.  By default,
     * returns 'Click to Edit'.  You can override and change, and also
     * return different String for different OMGraphics.
     */
    protected String getToolTipForOMGraphic(OMGraphic omgr) {
	return "Click to Edit";
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {}

    public java.awt.Component getGUI() {

	JPanel box = PaletteHelper.createVerticalPanel("Save Layer Graphics");
	box.setLayout(new java.awt.GridLayout(0, 1));
	JButton button = new JButton("Save As Shape File");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    com.bbn.openmap.dataAccess.shape.EsriShapeExport ese = 
			new com.bbn.openmap.dataAccess.shape.EsriShapeExport(getList(), getProjection(), null);
		    ese.export();
		}
	    });
	box.add(button);

	return box;
    }

    /**
     * A flag to provide a tooltip over OMGraphics to click to edit.
     */
    public void setShowHints(boolean show) {
	showHints = show;
    }

    public boolean getShowHints() {
	return showHints;
    }
}









