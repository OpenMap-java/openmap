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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/Attic/EditingMapMouseInterpreter.java,v $
// $RCSfile: EditingMapMouseInterpreter.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/08 22:32:19 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;

public class EditingMapMouseInterpreter extends StandardMapMouseInterpreter {

    public EditingMapMouseInterpreter() {}

    public EditingMapMouseInterpreter(OMGraphicHandlerLayer l) {
	setLayer(l);
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
// 	OMGraphicList list = getList();

// 	if (list != null) {

// 	    OMGraphic omgr = list.findClosest(e.getX(), e.getY(), 4);

// 	    if (omgr != null && shouldEdit(omgr)) {
// 		OMDrawingTool dt = getDrawingTool();
// 		if (dt != null) {
// 		    dt.setBehaviorMask(OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);

// 		    MapMouseMode omdtmm = dt.getMouseMode();
// 		    if (!omdtmm.isVisible()) {
// 			dt.setBehaviorMask(OMDrawingTool.PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK |
// 					   OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);
// 		    }

// 		    if (dt.edit(omgr, layer, e) != null) {
// 			// OK, means we're editing - let's lock up the MouseMode
// 			if (e instanceof MapMouseEvent) {

// 			    // Check to see if the DrawingToolMouseMode wants to 
// 			    // be invisible.  If it does, ask the current
// 			    // active MouseMode to be the proxy for it...
// 			    if (!omdtmm.isVisible()) {
// 				MapMouseMode mmm = ((MapMouseEvent)e).getMapMouseMode();
// 				if (mmm.actAsProxyFor(omdtmm, MapMouseSupport.PROXY_DISTRIB_MOUSE_MOVED & MapMouseSupport.PROXY_DISTRIB_MOUSE_DRAGGED)) {
// 				    if (DTL_DEBUG) {
// 					Debug.output("DTL: Setting " + mmm.getID() + " as proxy for drawing tool");
// 				    }
// 				    setProxyMouseMode(mmm);
// 				} else {
// 				    // WHOA, couldn't get proxy lock - bail
// 				    if (DTL_DEBUG) {
// 					Debug.output("DTL: couldn't get proxy lock on " + mmm.getID() + " deactivating internal drawing tool");
// 				    }
// 				    dt.deactivate();
// 				}
// 			    } else {
// 				if (DTL_DEBUG) {
// 				    Debug.output("DTL: OMDTMM wants to be visible");
// 				}
// 			    }
// 			} else {
// 			    if (DTL_DEBUG) {
// 				Debug.output("DTL: MouseEvent not a MapMouseEvent");
// 			    }
// 			}

// 			fireHideToolTip(e);
// 			ret = true;
// 		    }
// 		}
// 	    } 
// 	}
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
	return false;
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
	return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {
	boolean ret = false;
// 	OMGraphic omgr = null;
// 	OMGraphicList list = getList();

// 	if (list != null) {
// 	    omgr = list.findClosest(e.getX(),e.getY(),4.0f);

// 	    if (omgr != null) {
// 		if (showHints) {
// 		    if (omgr != lastSelected) {
// 			lastToolTip = getToolTipForOMGraphic(omgr);
// 		    }

// 		    if (lastToolTip != null) {
// 			fireRequestToolTip(e, lastToolTip);
// 			ret = true;
// 		    } else {
// 			fireHideToolTip(e);
// 		    }
// 		}
// 	    } else {
// 		if (showHints && lastToolTip != null) {
// 		    fireHideToolTip(e);
// 		}
// 		lastToolTip = null;
// 	    }
// 	}

// 	lastSelected = omgr;
	return ret;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {
    }
}
