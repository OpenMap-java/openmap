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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMCircleLoader.java,v $
// $RCSfile: OMCircleLoader.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:15 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.*;

/**
 * Loader that knows how to create/edit OMCircle and OMRangeRings objects.
 */
public class OMCircleLoader extends AbstractToolLoader 
    implements EditToolLoader {

    protected String circleClassName = "com.bbn.openmap.omGraphics.OMCircle";
    protected String rangeRingsClassName = "com.bbn.openmap.omGraphics.OMRangeRings";

    public OMCircleLoader() {
        init();
    }

    public void init() {
        EditClassWrapper ecw = 
            new EditClassWrapper(circleClassName,
                                 "com.bbn.openmap.omGraphics.EditableOMCircle",
                                 "editablecircle.gif",
                                 "Circle");
        addEditClassWrapper(ecw);

        ecw = 
            new EditClassWrapper(rangeRingsClassName,
                                 "com.bbn.openmap.omGraphics.EditableOMRangeRings",
                                 "editablerangering.gif",
                                 "Range Rings");
        addEditClassWrapper(ecw);
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.  The GraphicAttributes
     * object lets you set some of the initial parameters of the circle,
     * like circle type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname, 
                                                GraphicAttributes ga) {
        String name = classname.intern();
        if (name == circleClassName) {
            return new EditableOMCircle(ga);
        }
        if (name == rangeRingsClassName) {
            return new EditableOMRangeRings(ga);
        }
        return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
        // Range rings have to go first, they subclass Circles.
        if (graphic instanceof OMRangeRings) {
            return new EditableOMRangeRings((OMRangeRings)graphic);
        }
        if (graphic instanceof OMCircle) {
            return new EditableOMCircle((OMCircle)graphic);
        }
        return null;
    }
}
