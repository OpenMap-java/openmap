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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMPolyLoader.java,v $
// $RCSfile: OMPolyLoader.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.labeled.*;
import com.bbn.openmap.util.Debug;

import java.awt.Component;

/**
 * Loader that knows how to create/edit OMPoly objects.
 */
public class OMPolyLoader extends AbstractToolLoader 
    implements EditToolLoader {

    protected String graphicClassName = "com.bbn.openmap.omGraphics.OMPoly";
    protected String labeledClassName = "com.bbn.openmap.omGraphics.labeled.LabeledOMPoly";

    public OMPolyLoader() {
	init();
    }

    public void init() {
	EditClassWrapper ecw = 
	    new EditClassWrapper(graphicClassName,
				 "com.bbn.openmap.omGraphics.EditableOMPoly",
				 "editablepoly.gif",
				 "Polygons/Polylines");
	addEditClassWrapper(ecw);

	// A class wrapper isn't added here for the LabeledOMPoly
	// because currently they are only created programmatically.
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.  The GraphicAttributes
     * object lets you set some of the initial parameters of the poly,
     * like poly type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname, 
						GraphicAttributes ga) {
	if (classname.intern() == graphicClassName) {
	    return new EditableOMPoly(ga);
	}
	if (classname.intern() == labeledClassName) {
	    return new EditableLabeledOMPoly(ga);
	}
	return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
	if (graphic instanceof OMPoly) {
	    return new EditableOMPoly((OMPoly)graphic);
	}
	return null;
    }
}
