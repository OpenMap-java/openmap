//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMDecoratedSpline.java,v $
//$RCSfile: EditableOMDecoratedSpline.java,v $
//$Revision: 1.5 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

/**
 * EditableOMDecoratedSpline. Nothing created, code adapted from
 * EditableOMPoly We just need to edit the generating points as an
 * OMPoly
 * 
 * @author Eric LEPICIER
 * @version 29 juil. 2002
 */
public class EditableOMDecoratedSpline extends EditableOMSpline {

    /**
     * Constructor.
     */
    public EditableOMDecoratedSpline() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param ga
     */
    public EditableOMDecoratedSpline(GraphicAttributes ga) {
        super(ga);
    }

    /**
     * Constructor.
     * 
     * @param omp
     */
    public EditableOMDecoratedSpline(OMSpline omp) {
        super(omp);
    }

    /**
     * Extendable method to create specific subclasses of OMPolys.
     */
    public OMGraphic createGraphic(int renderType, int lineType) {
        OMGraphic g = null;
        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON):
            g = new OMDecoratedSpline(new double[0], OMGraphic.RADIANS, lineType);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET):
            g = new OMSpline(90f, -180f, new int[0], OMDecoratedSpline.COORDMODE_ORIGIN);
            break;
        default:
            g = new OMDecoratedSpline(new int[0]);
        }
        ((OMSpline) g).setDoShapes(true);
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