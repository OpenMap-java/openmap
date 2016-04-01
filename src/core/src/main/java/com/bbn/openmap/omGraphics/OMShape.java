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

	public static class GAPPED extends OMShape {
		private static final long serialVersionUID = 2L;

		float strokeWidth;
		float gapWidth;

		public GAPPED(Shape s, float strokeWidth, float gapWidth) {
			super(s);
			this.strokeWidth = strokeWidth;
			this.gapWidth = gapWidth;
			
			this.setStroke(new BasicStroke(strokeWidth));
			setShape(getGappedShape(origShape));
			setNeedToRegenerate(false);
		}

		public boolean generate(Projection proj) {
			// NOOP
			return true;
		}

		protected GeneralPath getGappedShape(Shape s) {
			GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			GeneralPath inside = null;
			GeneralPath outside = null;
			//GeneralPath middle = null;

			PathIterator pi2 = s.getPathIterator(null);
			FlatteningPathIterator pi = new FlatteningPathIterator(pi2, .25);
			double[] coords = new double[6];

			double xcoord1 = 0;
			double ycoord1 = 0;
			double xcoord2 = 0;
			double ycoord2 = 0;

			while (!pi.isDone()) {
				int type = pi.currentSegment(coords);

				if (inside == null) {
					// Need to set up the first point. We can't calculate the
					// position of the perpendicular points until we know the
					// location of the second point.
					inside = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
					outside = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
					//middle = new GeneralPath(GeneralPath.WIND_EVEN_ODD);					
					xcoord1 = coords[0];
					ycoord1 = coords[1];

					continue;
				}

				xcoord2 = coords[0];
				ycoord2 = coords[1];

				// Compute cosinus and sinus of rotation angle
				double dx = xcoord2 - xcoord1;
				double dy = ycoord2 - ycoord1;
				double norm = Math.sqrt(dx * dx + dy * dy);
				double rcos = dx / norm;
				double rsin = dy / norm;

				// Compute vertices
				double r = .25;//getLength() / 2.0; // x radius before rotation
				double w = (gapWidth + strokeWidth) / 2;


				double x1 = xcoord1 + r * rcos;
				double y1 = ycoord1 + r * rsin;

				switch (type) {
				case PathIterator.SEG_LINETO:
					inside.lineTo(x1 - w * rsin, y1 + w * rcos);
					outside.lineTo(x1 + w * rsin, y1 - w * rcos);
					//middle.lineTo(xcoord1, ycoord1);
					break;
				case PathIterator.SEG_MOVETO:
					inside.moveTo(x1 - w * rsin, y1 + w * rcos);
					outside.moveTo(x1 + w * rsin, y1 - w * rcos);
					//middle.moveTo(xcoord1, ycoord1);					
					break;
				default:
				}

				xcoord1 = xcoord2;
				ycoord1 = ycoord2;
				
				pi.next();
			}

			gp.append(inside, false);
			//gp.append(middle, false);
			gp.append(outside, false);
			return gp;
		}
	}

}