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
// $Revision: 1.3 $
// $Date: 2006/04/07 15:38:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import com.bbn.openmap.proj.Projection;

/**
 * The OMShape object is an OMGraphic intended to be used with non-GeoProj
 * projections, defining projected map object to be modified for different
 * views. You can use it to provide OMGraphic functionality, with respect to
 * colors and strokes and OMGraphicLists, to java.awt.Shape objects.
 * <P>
 * GeoProj projections will be able to display them, but they will be rendered
 * as OMGraphic.RENDERTYPE_LATLON with OMGraphic.LINETYPE_STRAIGHT settings.
 * Rendering can be unpredictable for large coordinate values.
 */
public class OMShape extends OMGraphicAdapter implements OMGraphic {
	private static final long serialVersionUID = 1L;
	protected Shape origShape = null;

	protected OMShape() {
	}

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
            setShape(new GeneralPath(proj.forwardShape(origShape)));
            setLabelLocation(getShape(), proj);
            setNeedToRegenerate(false);
            return true;
        }

		return false;
	}

	public void restore(OMGeometry source) {
		super.restore(source);
		if (source instanceof OMShape) {
			OMShape shape = (OMShape) source;

			this.origShape = new GeneralPath(shape.origShape);
		}
	}

	/**
	 * This is a subclass that uses the provided shape as the generated shape.
	 * Takes advantage of the rendering mechanism of OMGraphics. Mainly used for
	 * rendering features already projected for vector tiles.
	 * 
	 * @author dietrick
	 *
	 */
	public static class PROJECTED extends OMShape {
		private static final long serialVersionUID = 1L;

		public PROJECTED(Shape s) {
			super(s);
			setShape(new GeneralPath(origShape));
			setNeedToRegenerate(false);
		}

		public boolean generate(Projection proj) {
			// NOOP
			return true;
		}
	}
}