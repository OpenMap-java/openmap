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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMLineLoader.java,v $
// $RCSfile: OMLineLoader.java,v $
// $Revision: 1.3 $
// $Date: 2004/02/04 17:26:04 $
// $Author: dietrick $
// 
// **********************************************************************




package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.*;

/**
 * Loader that knows how to create/edit OMLine objects.
 */
public class OMLineLoader extends AbstractToolLoader
    implements EditToolLoader {

    protected String graphicClassName = "com.bbn.openmap.omGraphics.OMLine";

    public OMLineLoader() {
        init();
    }

    public void init() {
        EditClassWrapper ecw = 
            new EditClassWrapper(graphicClassName,
                                 "com.bbn.openmap.omGraphics.EditableOMLine",
                                 "editableline.gif",
                                 i18n.get(OMLineLoader.class,"omline","Line"));                          
        addEditClassWrapper(ecw);
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.  The GraphicAttributes
     * object lets you set some of the initial parameters of the line,
     * like line type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname, 
                                                GraphicAttributes ga) {
        if (classname.intern() == graphicClassName) {
            return new EditableOMLine(ga);
        }
        return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
        if (graphic instanceof OMLine) {
            return new EditableOMLine((OMLine)graphic);
        }
        return null;
    }
}
