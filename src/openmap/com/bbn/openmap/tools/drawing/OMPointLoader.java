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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMPointLoader.java,v $
// $RCSfile: OMPointLoader.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.*;

/**
 * Loader that knows how to create/edit OMPoint objects.
 */
public class OMPointLoader extends AbstractToolLoader
    implements EditToolLoader {

    protected String graphicClassName = "com.bbn.openmap.omGraphics.OMPoint";

    public OMPointLoader() {
	init();
    }
    
    public void init() {
	EditClassWrapper ecw = 
	    new EditClassWrapper(graphicClassName,
				 "com.bbn.openmap.omGraphics.EditableOMPoint",
				 "editablepoint.gif",
				 "Point");
	addEditClassWrapper(ecw);
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.  The GraphicAttributes
     * object lets you set some of the initial parameters of the point,
     * like point type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname, 
						GraphicAttributes ga) {
	if (classname.intern() == graphicClassName) {
	    return new EditableOMPoint(ga);
	}
	return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
	if (graphic instanceof OMPoint) {
	    return new EditableOMPoint((OMPoint)graphic);
	}
	return null;
    }
}
