package com.bbn.openmap.omGraphics.awt;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.logging.Logger;

/**
 * A decoration that attempts to draw a line on either side of a source line.
 * The stroke width is the overall sideways coverage of the two lines, and the
 * gap width is the space between them.
 * 
 * <pre>
 * sd.addDecoration(new GapDecoration(8f, 5f, Color.gray));
 * </pre>
 * 
 * TODO - seems a little shakey, but kinda provides the desired effect.  Just saving work...
 * 
 * @author dietrick
 */
public class GapDecoration extends AbstractShapeDecoration {
	float gapWidth;

	public GapDecoration(float strokeWidth, float gapWidth, Paint color) {
		super(2, strokeWidth, RIGHT);
		this.gapWidth = gapWidth;
		setStroke(new BasicStroke((strokeWidth - gapWidth) / 2));
		setPaint(color);
	}

	public void draw(Graphics g, Point2D[] points, boolean complete) {
		setGraphics(g);
		((Graphics2D) g).setStroke(getStroke());

		int nbpts = points.length;

		double xcoord1 = points[0].getX();
		double ycoord1 = points[0].getY();
		double xcoord2 = points[nbpts - 1].getX();
		double ycoord2 = points[nbpts - 1].getY();

		if (complete) {

			// Compute cosinus and sinus of rotation angle
			double dx = xcoord2 - xcoord1;
			double dy = ycoord2 - ycoord1;
			double norm = Math.sqrt(dx * dx + dy * dy);
			double rcos = dx / norm;
			double rsin = dy / norm;

			// Compute vertices
			double r = getLength() / 2.0; // x radius before rotation
			double w = -getWidth(); // y radius before rotation

			// rotate
			// This is the middle point of the line that goes across.
			int x2 = (int) (xcoord2 + r * rcos);
			int y2 = (int) (ycoord2 + r * rsin);

			int x1 = (int) (xcoord1 + r * rcos);
			int y1 = (int) (ycoord1 + r * rsin);

			g.drawLine((int) (x1 - w * rsin), (int) (y1 + w * rcos), (int) (x2 - w * rsin), (int) (y2 + w * rcos));

			w = getWidth(); // y radius before rotation
			g.drawLine((int) (x1 - w * rsin), (int) (y1 + w * rcos), (int) (x2 - w * rsin), (int) (y2 + w * rcos));
		}

		restoreGraphics(g);

	}

	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(GapDecoration.class.getName());

		/**
		 * Prevent instantiation
		 */
		private LoggerHolder() {
			throw new AssertionError("The LoggerHolder should never be instantiated");
		}
	}

	/**
	 * Get the logger for this class.
	 *
	 * @return logger for this class
	 */
	private static Logger getLogger() {
		return LoggerHolder.LOGGER;
	}
	// </editor-fold>

}
