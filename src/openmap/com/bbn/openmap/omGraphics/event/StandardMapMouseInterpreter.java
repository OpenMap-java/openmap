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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/StandardMapMouseInterpreter.java,v $
// $RCSfile: StandardMapMouseInterpreter.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/08 22:32:19 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

public class StandardMapMouseInterpreter implements MapMouseInterpreter {

    protected OMGraphicHandlerLayer layer = null;
    protected String[] mouseModeServiceList = null;
    protected OMGraphic lastSelected = null;
    protected String lastToolTip = null;
    protected GestureResponsePolicy grp = null;
    protected OMGeometry omgci = null; // click interest
    protected OMGeometry omgmi = null; // movement interest

    public StandardMapMouseInterpreter() {}

    public StandardMapMouseInterpreter(OMGraphicHandlerLayer l) {
	setLayer(l);
    }

    public void setLayer(OMGraphicHandlerLayer l) {
	layer = l;
    }

    public OMGraphicHandlerLayer getLayer() {
	return layer;
    }

    public void setMouseModeServiceList(String[] list) {
	mouseModeServiceList = list;
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
	return mouseModeServiceList;
    }

    protected void setClickInterest(OMGeometry omg) {
	omgci = omg;
    }

    protected OMGeometry getClickInterest() {
	return omgci;
    }

    protected void setMovementInterest(OMGeometry omg) {
	omgmi = omg;
    }

    protected OMGeometry getMovementInterest() {
	return omgmi;
    }

    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) { 
	boolean ret = false;
	OMGraphicList list = null;

	if (layer != null) {
	    list = layer.getList();
	}

	if (list != null) {
	    OMGraphic omgr = list.findClosest(e.getX(), e.getY(), 4);
	    if (omgr != null && grp.isSelectable(omgr)) {
		setClickInterest(omgr);
	    }
	    ret = true;
	}
	return ret;
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
	boolean ret = false;
	OMGeometry omg = getClickInterest();

	if (omg != null) {
	    if (e.getButton() == MouseEvent.BUTTON2) {
		grp.rightClick(omg, e);
	    } else {
		grp.leftClick(omg, e);
	    }
	}

	return ret;
    }

    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
    }

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.  The listener will receive these events if it
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
	boolean ret = false;

	OMGeometry omg = getClickInterest();
	if (omg != null) {
	    setClickInterest(null);
	    ret = true;
	}

	ret = mouseMoved(e);

	return ret;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {
	boolean ret = false;
	OMGraphicList list = null;

	if (layer != null) {
	    list = layer.getList();
	}

 	if (list != null) {
 	    OMGeometry omg = list.findClosest(e.getX(),e.getY(),4.0f);

 	    if (omg != null) {
		if (grp.isSelectable(omg)) {
		    if (omg != lastSelected) {
			lastToolTip = grp.getToolTipTextFor(omg);
		    }

		    if (layer != null) {
			if (lastToolTip != null) {
			    layer.fireRequestToolTip(e, lastToolTip);
			    ret = true;
			} else {
			    layer.fireHideToolTip(e);
			}
		    }

		    setMovementInterest(omg);
		    grp.mouseOver(omg, e);
		}
	    } else {
		if (lastToolTip != null && layer != null) {
		    layer.fireHideToolTip(e);
		}
		lastToolTip = null;

		omg = getMovementInterest();
		if (omg != null) {
		    grp.mouseNotOver(omg);
		    setMovementInterest(null);
		}
	    }
	}

	return ret;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {
	OMGeometry omg = getMovementInterest();
	if (omg != null) {
	    grp.mouseNotOver(omg);
	    setMovementInterest(null);
	}
    }

    public void setGRP(GestureResponsePolicy grp) {
	this.grp = grp;
    }

    public GestureResponsePolicy getGRP() {
	return grp;
    }

}
