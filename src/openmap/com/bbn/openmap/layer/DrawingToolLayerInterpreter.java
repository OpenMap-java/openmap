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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/Attic/DrawingToolLayerInterpreter.java,v $
// $RCSfile: DrawingToolLayerInterpreter.java,v $
// $Revision: 1.1 $
// $Date: 2003/09/23 22:53:08 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer;

import java.awt.event.MouseEvent;

import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.MapMouseSupport;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;

public class DrawingToolLayerInterpreter extends StandardMapMouseInterpreter {

    public DrawingToolLayerInterpreter() {
	super();
    }

    public DrawingToolLayerInterpreter(DrawingToolLayer l) {
	setLayer(l);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return true if pressed on OMGraphics and consumeEvents == true.
     */
    public boolean mousePressed(MouseEvent e) { 
	boolean ret = false;

	GeometryOfInterest goi = getClickInterest();
	OMGraphic omg = getGeometryUnder(e);

	if (goi != null && !goi.appliesTo(omg, e)) {
	    // If the click doesn't match the geometry or button
	    // of the geometry of interest, need to tell the goi
	    // that is was clicked off, and set goi to null.
	    if (goi.isLeftButton()) {
		leftClickOff(goi.getGeometry(), e);
	    } else {
		rightClickOff(goi.getGeometry(), e);
	    }
	    setClickInterest(null);
	}

	if (omg != null && grp != null && grp.isSelectable(omg)) {
	    if (layer instanceof DrawingToolLayer) {
		OMDrawingTool dt = ((DrawingToolLayer)layer).getDrawingTool();

		if (dt != null && dt.canEdit(omg.getClass())) {
		    if (!dt.getUseAsTool()) {
			dt.setBehaviorMask(OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);
		    }

		    MapMouseMode omdtmm = dt.getMouseMode();
		    if (!omdtmm.isVisible()) {
			int behaviorMask = OMDrawingTool.PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK;
			if (!dt.getUseAsTool()) {
			    behaviorMask |= OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK; 
			}
			dt.setBehaviorMask(behaviorMask);
		    }

		    if (dt.edit(omg, (DrawingToolLayer)layer, e) != null) {
			// OK, means we're editing - let's lock up the MouseMode
			if (DEBUG) {
			    Debug.output("DTLI: leftClick starting editing of OMGraphic...");
			}

			if (e instanceof MapMouseEvent) {

			    // Check to see if the DrawingToolMouseMode wants to 
			    // be invisible.  If it does, ask the current
			    // active MouseMode to be the proxy for it...
			    if (!omdtmm.isVisible()) {
				MapMouseMode mmm = ((MapMouseEvent)e).getMapMouseMode();
				if (mmm.actAsProxyFor(omdtmm, MapMouseSupport.PROXY_DISTRIB_MOUSE_MOVED & MapMouseSupport.PROXY_DISTRIB_MOUSE_DRAGGED)) {
				    if (DEBUG) {
					Debug.output("DTLI: Setting " + mmm.getID() + " as proxy for drawing tool");
				    }
				    ((DrawingToolLayer)layer).setProxyMouseMode(mmm);
				} else {
				    // WHOA, couldn't get proxy lock - bail
				    if (DEBUG) {
					Debug.output("DTLI: couldn't get proxy lock on " + mmm.getID() + " deactivating internal drawing tool");
				    }
				    dt.deactivate();
				}
			    } else {
				if (DEBUG) {
				    Debug.output("DTLI: OMDTMM wants to be visible");
				}
			    }
			} else {
			    if (DEBUG) {
				Debug.output("DTLI: MouseEvent not a MapMouseEvent");
			    }
			}
		    }
		}
	    }

	    setClickInterest(new GeometryOfInterest(omg, e));
	    ret = true;
	}

	return ret && consumeEvents;
    }
}
