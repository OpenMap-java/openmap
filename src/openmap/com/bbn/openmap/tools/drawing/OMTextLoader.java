/* **********************************************************************
 * 
 *  BBNT Solutions LLC, A part of GTE
 *  10 Moulton St.
 *  Cambridge, MA 02138
 *  (617) 873-2000
 * 
 *  Copyright (C) 1998, 2000
 *  This software is subject to copyright protection under the laws of 
 *  the United States and other countries.
 * 
 * **********************************************************************
 * 
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMTextLoader.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003/02/14 21:35:49 $
 * $Author: dietrick $
 * 
 * **********************************************************************
 */

package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.*;

/**
 * Loader that knows how to create/edit OMText objects.
 */
public class OMTextLoader extends AbstractToolLoader
    implements EditToolLoader {

    protected String graphicClassName = "com.bbn.openmap.omGraphics.OMText";

    public OMTextLoader() {
	init();
    }
    
    public void init() {
	EditClassWrapper ecw = 
	    new EditClassWrapper(graphicClassName,
				 "com.bbn.openmap.omGraphics.EditableOMText",
				 "editabletext.gif",
				 "Text");
	addEditClassWrapper(ecw);
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.  The GraphicAttributes
     * object lets you set some of the initial parameters of the text,
     * like text type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname, 
						GraphicAttributes ga) {
	if (classname.intern() == graphicClassName) {
	    return new EditableOMText(ga);
	}
	return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
	if (graphic instanceof OMText) {
	    return new EditableOMText((OMText)graphic);
	}
	return null;
    }
}
