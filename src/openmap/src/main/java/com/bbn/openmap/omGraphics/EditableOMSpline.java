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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMSpline.java,v $
//$RCSfile: EditableOMSpline.java,v $
//$Revision: 1.6 $
//$Date: 2009/01/21 01:24:41 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics;

/**
 * EditableOMSpline. Nothing created, code adapted from EditableOMPoly
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
     * 
     * @param ga
     */
    public EditableOMSpline(GraphicAttributes ga) {
        super(ga);
    }

    /**
     * Constructor.
     * 
     * @param omp
     */
    public EditableOMSpline(OMSpline omp) {
        super(omp);
    }

    /**
     * Extendable method to create specific subclasses of OMPolys.
     */
    public OMGraphic createGraphic(int renderType, int lineType) {
        OMGraphic g = null;
        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON):
            g = new OMSpline(new double[0], OMGraphic.RADIANS, lineType);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET):
            g = new OMSpline(90f, -180f, new int[0], OMSpline.COORDMODE_ORIGIN);
            break;
        default:
            g = new OMSpline(new int[0]);
        }
        ((OMSpline) g).setDoShapes(true);
        return g;
    }

    /**
     * Overridden to overcome some repainting unpleasantness that
     * occurs when a point is added. Slows things down, however.
     */
    public int addMovingPoint(int x, int y) {
        int position = super.addMovingPoint(x, y);
        redraw(null, true);
        return position;
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