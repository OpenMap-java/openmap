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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/labeled/EditableLabeledOMPoly.java,v $
// $RCSfile: EditableLabeledOMPoly.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.labeled;

import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;

/**
 * The EditableLabeledOMPoly encompasses an LabeledOMPoly, providing
 * methods for modifying or creating it.
 */
public class EditableLabeledOMPoly extends EditableOMPoly {

    /**
     * Create the EditableLabeledOMPoly, setting the state machine to
     * create the poly off of the gestures.
     */
    public EditableLabeledOMPoly() {
        createGraphic(null);
    }

    /**
     * Create an EditableLabeledOMPoly with the polyType and
     * renderType parameters in the GraphicAttributes object.
     */
    public EditableLabeledOMPoly(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableLabeledOMPoly with a LabeledOMPoly already
     * defined, ready for editing.
     * 
     * @param omp LabeledOMPoly that should be edited.
     */
    public EditableLabeledOMPoly(LabeledOMPoly omp) {
        setGraphic(omp);
    }

    /**
     * Extendable method to create specific subclasses of OMPolys.
     */
    public OMGraphic createGraphic(int renderType, int lineType) {
        OMGraphic g = null;
        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON):
            g = new LabeledOMPoly(new double[0], OMGraphic.RADIANS, lineType);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET):
            g = new LabeledOMPoly(90f, -180f, new int[0], OMPoly.COORDMODE_ORIGIN);
            break;
        default:
            g = new LabeledOMPoly(new int[0]);
        }
        ((LabeledOMPoly) g).setDoShapes(true);
        return g;
    }

    public java.net.URL getImageURL(String imageName) {
        try {
            return Class.forName("com.bbn.openmap.omGraphics.EditableOMPoly")
                    .getResource(imageName);
        } catch (ClassNotFoundException cnfe) {
        }
        return null;
    }

}