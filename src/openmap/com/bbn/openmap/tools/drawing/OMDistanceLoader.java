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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/OMDistanceLoader.java,v $
// $RCSfile: OMDistanceLoader.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.EditableOMDistance;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMDistance;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * Loader that knows how to create/edit OMDistance objects.
 * 
 * @author Ben Lubin
 * @version $Revision: 1.4 $ on $Date: 2004/10/14 18:06:26 $
 * @since 1/3/03
 */
public class OMDistanceLoader extends AbstractToolLoader implements
        EditToolLoader {

    protected String graphicClassName = "com.bbn.openmap.omGraphics.OMDistance";

    protected String editableClassName = "com.bbn.openmap.omGraphics.EditableOMDistance";

    public OMDistanceLoader() {
        init();
    }

    public void init() {
        EditClassWrapper ecw = new EditClassWrapper(graphicClassName, editableClassName, "distance.png", i18n.get(OMDistanceLoader.class,
                "omdistance",
                "Distance"));

        addEditClassWrapper(ecw);

        // A class wrapper isn't added here for the LabeledOMPoly
        // because currently they are only created programmatically.
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic. The GraphicAttributes
     * object lets you set some of the initial parameters of the poly,
     * like poly type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname,
                                                GraphicAttributes ga) {
        if (classname.intern() == graphicClassName) {
            return new EditableOMDistance(ga);
        }
        return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
        if (graphic instanceof OMDistance) {
            return new EditableOMDistance((OMDistance) graphic);
        }
        return null;
    }
}

