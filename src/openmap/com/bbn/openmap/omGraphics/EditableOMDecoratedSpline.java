package com.bbn.openmap.omGraphics;

import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * EditableOMDecoratedSpline.
 * Nothing created, code adapted from EditableOMPoly
 * We just need to edit the generating points as an OMPoly
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
     * @param ga
     */
    public EditableOMDecoratedSpline(GraphicAttributes ga) {
        super(ga);
    }

    /**
     * Constructor.
     * @param omp
     */
    public EditableOMDecoratedSpline(OMSpline omp) {
        super(omp);
    }

    /**
     *  Extendable method to create specific subclasses of OMPolys.
     */
    public OMGraphic createGraphic(int renderType, int lineType) {
        OMGraphic g = null;
        switch (renderType) {
        case (OMGraphic.RENDERTYPE_LATLON) :
            g = new OMDecoratedSpline(new float[0], OMGraphic.RADIANS, lineType);
            break;
        case (OMGraphic.RENDERTYPE_OFFSET) :
            g = new OMSpline(90f, -180f, new int[0],
                             OMDecoratedSpline.COORDMODE_ORIGIN);
            break;
        default :
            g = new OMDecoratedSpline(new int[0]);
        }
        ((OMSpline) g).setDoShapes(true);
        return g;
    }

    public java.net.URL getImageURL(String imageName) {
        try {
            return Class.forName(
                "com.bbn.openmap.omGraphics.EditableOMPoly").getResource(
                    imageName);
        }
        catch (ClassNotFoundException cnfe) {}
        return null;
    }

}
