package com.bbn.openmap.omGraphics.awt;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.Point2D;

/**
 * A ShapeDecoration that draws a circle on the path.
 */
public class CircleShapeDecoration extends AbstractShapeDecoration {

    /**
     */
    private boolean filled = true;

    /**
     * Constructor.
     * 
     * @param length number of pixels of segment to draw circle in.
     * @param radius number of pixels for radius of circle.
     */
    public CircleShapeDecoration(int length, int radius) {
        super(length, radius, RIGHT);
    }

    /**
     * Constructor.
     * 
     * @param length number of pixels of segment to draw circle in.
     * @param radius number of pixels for radius of circle.
     * @param paint the Paint to use for the circle.
     */
    public CircleShapeDecoration(int length, int radius, Paint paint) {
        super(length, radius, RIGHT);
        setPaint(paint);
    }

    /**
     * @see com.bbn.openmap.omGraphics.awt.ShapeDecoration#draw(Graphics,
     *      Point2D[], boolean)
     */
    public void draw(Graphics g, Point2D[] points, boolean complete) {
        setGraphics(g);

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
            double w = getWidth();
            // rotate

            int x = (int) (xcoord1 + r * rcos);
            int y = (int) (ycoord1 + r * rsin);

            if (filled) {
                g.fillOval((int) (x - w / 2),
                        (int) (y - w / 2),
                        (int) (w),
                        (int) (w));
            } else {
                g.drawOval((int) (x - w / 2),
                        (int) (y - w / 2),
                        (int) (w),
                        (int) (w));
            }
        }

        restoreGraphics(g);
    }

    /**
     * Returns the filled boolean.
     * 
     * @return boolean
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * Sets the filled (draw a half disk or a half circle ?).
     * 
     * @param filled The filled to set
     */
    public void setFilled(boolean filled) {
        this.filled = filled;
    }
}

