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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMDistance.java,v $
// $RCSfile: EditableOMDistance.java,v $
// $Revision: 1.6 $
// $Date: 2003/12/23 22:55:25 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.layer.util.stateMachine.State;
import com.bbn.openmap.omGraphics.editable.*;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.omGraphics.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.*;

/**
 * The EditableOMDistance encompasses an OMDistance, providing methods for
 * modifying or creating it.
 */
public class EditableOMDistance extends EditableOMPoly {

    /**
     * Create the EditableOMDistance, setting the state machine to create
     * the poly off of the gestures.  
     */
    public EditableOMDistance() {
	super();
    }

    /**
     * Create an EditableOMDistance with the polyType and renderType
     * parameters in the GraphicAttributes object.
     */
    public EditableOMDistance(GraphicAttributes ga) {
	super(ga);
    }

    /**
     * Create the EditableOMDistance with an OMDistance already defined, ready
     * for editing.
     *
     * @param omp OMDistance that should be edited.
     */
    public EditableOMDistance(OMDistance omp) {
	super(omp);
    }

    /**
     * Create and set the graphic within the state machine.  The
     * GraphicAttributes describe the type of poly to create. 
     */
    public void createGraphic(GraphicAttributes ga) {
	init();
	stateMachine.setUndefined();
	int renderType = OMGraphic.RENDERTYPE_LATLON;
	int lineType = OMGraphic.LINETYPE_GREATCIRCLE;

	if (ga != null) {
	    renderType = ga.getRenderType();
	    lineType = ga.getLineType();
	}

	if (Debug.debugging("eomg")) {
	    Debug.output("EditableOMDistance.createGraphic(): rendertype = " +
			 renderType);
	}

	if (lineType == OMGraphic.LINETYPE_UNKNOWN) {
	    lineType = OMGraphic.LINETYPE_GREATCIRCLE;
	    ga.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
	}

	this.poly = (OMDistance)createGraphic(renderType, lineType);

	if (ga != null) {
	    ga.setRenderType(poly.getRenderType());
	    ga.setTo(poly);
	}
    }

    /**
     *  Extendable method to create specific subclasses of OMDistances.
     */
    public OMGraphic createGraphic(int renderType, int lineType) {
	OMGraphic g = null;
	switch (renderType) {
	case (OMGraphic.RENDERTYPE_OFFSET):
	    System.err.println("Offset type not supported for OMDistance");
	}
	g = new OMDistance(new float[0], OMGraphic.RADIANS, lineType, Length.NM);
	((OMDistance)g).setDoShapes(true);
	return g;
    }

    /**
     * A convenience method that gives an EditableOMGraphic a chance
     * to modify the OMGraphic so it can be drawn quickly, by turning
     * off labels, etc, right before the XORpainting happens.  The
     * OMGraphic should be configured so that the render method does
     * the least amount of painting possible.  Note that the
     * DrawingAttributes for the OMGraphic have already been set to
     * DrawingAttributes.DEFAULT (black line, clear fill).
     */
    protected void modifyOMGraphicForEditRender() {
	((OMDistance)getGraphic()).paintOnlyPoly = true;
    }

    /**
     * A convenience method that gives an EditableOMGraphic a chance
     * to reset the OMGraphic so it can be rendered normally, after it
     * has been modified for quick paints.  The DrawingAttributes for
     * the OMGraphic have already been reset to their normal settings,
     * from the DrawingAttributes.DEFAULT settings that were used for
     * the quick paint.
     */
    protected void resetOMGraphicAfterEditRender() {
	((OMDistance)getGraphic()).paintOnlyPoly = false;
    }
}

