// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMTextLoader.java,v $
// $RCSfile: OMTextLoader.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.EditableOMText;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMText;

/**
 * Loader that knows how to create/edit OMText objects.
 */
public class OMTextLoader extends AbstractToolLoader implements EditToolLoader {

    protected String textClassName = "com.bbn.openmap.omGraphics.OMText";

    public OMTextLoader() {
        init();
    }

    public void init() {
        EditClassWrapper ecw = new EditClassWrapper(textClassName, "com.bbn.openmap.omGraphics.EditableOMText", "editabletext.gif", i18n.get(OMTextLoader.class,
                "omtext",
                "Text"));

        addEditClassWrapper(ecw);

    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic. The GraphicAttributes
     * object lets you set some of the initial parameters of the text,
     * like font and size.
     */
    public EditableOMGraphic getEditableGraphic(String classname,
                                                GraphicAttributes ga) {
        if (classname.intern() == textClassName) {
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
            return new EditableOMText((OMText) graphic);
        }
        return null;
    }
}