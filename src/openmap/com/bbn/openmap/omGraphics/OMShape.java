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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMShape.java,v $
// $RCSfile: OMShape.java,v $
// $Revision: 1.1 $
// $Date: 2005/08/09 20:01:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.Serializable;

import com.bbn.openmap.proj.Projection;

/**
 * TODO - This is set up for LINETYPE_STRAIGHT. Need to think about
 * preparing the shape for generation better, perhaps doing the
 * conversion to radians, accepting choices for linetype. Might
 * require the shape be converted to GeneralPath for
 * LINETYPE_GREATCIRCLE/RHUMB. We ought to think about having
 * RENDERTYPE_XY being based on percentages relative to width/height
 * of window (ABSOLUTE/RELATIVE).
 * 
 * @author dietrick
 */
public class OMShape extends OMGraphic implements Serializable {

    protected Shape origShape = null;

    public OMShape(Shape shapeIn) {
        origShape = shapeIn;
    }

    public Shape getOrigShape() {
        return origShape;
    }

    public void setOrigShape(Shape origShape) {
        this.origShape = origShape;
        setNeedToRegenerate(true);
    }

    public boolean generate(Projection proj) {
        setNeedToRegenerate(true);

        if (origShape != null) {
            PathIterator pi = shape.getPathIterator(null);
            double[] coords = new double[6];
            Point screen = new Point();

            GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

            while (!pi.isDone()) {
                int type = pi.currentSegment(coords);

                proj.forward((float) coords[0], (float) coords[1], screen);

                if (type == PathIterator.SEG_MOVETO) {
                    path.moveTo((int) screen.getX(), (int) screen.getY());
                } else if (type == PathIterator.SEG_LINETO) {
                    path.lineTo((int) screen.getX(), (int) screen.getY());
                } else if (type == PathIterator.SEG_CLOSE) {
                    path.closePath();
                }

                pi.next();
            }
            setNeedToRegenerate(false);
            return true;
        }

        return false;
    }

}