package com.bbn.openmap.omGraphics;

import java.util.Iterator;

import com.bbn.openmap.layer.util.stateMachine.State;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.editable.GraphicEditState;
import com.bbn.openmap.omGraphics.editable.GraphicSelectedState;
import com.bbn.openmap.omGraphics.editable.PolyAddNodeState;
import com.bbn.openmap.omGraphics.editable.PolyDeleteNodeState;
import com.bbn.openmap.omGraphics.editable.PolyUndefinedState;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * EditableOMSpline.
 * Nothing created, code adapted from EditableOMPoly
 * We just need to edit the generating points as an OMPoly
 * 
 * @author Eric LEPICIER
 * @version 22 juil. 2002
 */
public class EditableOMSpline extends EditableOMPoly {

    /**
     * Constructor.
     */
    public EditableOMSpline() {
	super();
    }

    /**
     * Constructor.
     * @param ga
     */
    public EditableOMSpline(GraphicAttributes ga) {
	super(ga);
    }

    /**
     * Constructor.
     * @param omp
     */
    public EditableOMSpline(OMSpline omp) {
	super(omp);
    }

    /**
     *  Extendable method to create specific subclasses of OMPolys.
     */
    public OMGraphic createGraphic(int renderType, int lineType) {
	OMGraphic g = null;
	switch (renderType) {
	case (OMGraphic.RENDERTYPE_LATLON) :
	    g = new OMSpline(new float[0], OMGraphic.RADIANS, lineType);
	    break;
	case (OMGraphic.RENDERTYPE_OFFSET) :
	    g =	new OMSpline(90f, -180f, new int[0],
			     OMSpline.COORDMODE_ORIGIN);
	    break;
	default :
	    g = new OMSpline(new int[0]);
	}
	((OMSpline) g).setDoShapes(true);
	return g;
    }

    /**
     * Overridden to overcome some repainting unpleasantness that
     * occurs when a point is added.  Slows things down, however.
     */
    public int addMovingPoint(int x, int y) {
	int position = super.addMovingPoint(x, y);
	redraw(null, true);
	return position;
    }

    public java.net.URL getImageURL(String imageName) {
	try {
	    return Class.forName(
		"com.bbn.openmap.omGraphics.EditableOMPoly").getResource(
		    imageName);
	}
	catch (ClassNotFoundException cnfe) {}
	return null;
    }

}
