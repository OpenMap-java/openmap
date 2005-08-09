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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/meteo/IceAreaShapeDecoration.java,v $
//$RCSfile: IceAreaShapeDecoration.java,v $
//$Revision: 1.3 $
//$Date: 2005/08/09 20:11:32 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.meteo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.awt.AbstractShapeDecoration;

/**
 * A ShapeDecoration for meteorological icy conditions.
 */
public class IceAreaShapeDecoration extends AbstractShapeDecoration {

    /** We use to draw them red ! */
    public static Color COLOR = new Color(102, 51, 0);

    /**
     * Filled half circles for surface fronts, empty ones for altitude
     * fronts
     */
    private boolean filled = false;

    /**
     * Constructor.
     * 
     * @param length
     * @param width
     * @param orientation
     */
    public IceAreaShapeDecoration(int length, int width, int orientation) {
        super(length, width, orientation);
        setPaint(COLOR);
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

        g.drawLine((int) xcoord1, (int) ycoord1, (int) xcoord2, (int) ycoord2);

        if (complete) {
            int orient = getOrientation() == LEFT ? -1 : 1;

            // Compute cosinus and sinus of rotation angle
            double dx = xcoord2 - xcoord1;
            double dy = ycoord2 - ycoord1;
            double norm = Math.sqrt(dx * dx + dy * dy);
            double rcos = dx / norm;
            double rsin = dy / norm;

            // Compute vertices
            double r = getLength() / 2.0; // x radius before rotation
            double w = orient * getWidth(); // y radius before
                                            // rotation
            // rotate

            int x2 = (int) (xcoord1 + r * rcos);
            int y2 = (int) (ycoord1 + r * rsin);
            int x1 = (int) (x2 - w * rsin);
            int y1 = (int) (y2 + w * rcos);

            g.drawLine((int) x2, (int) y2, (int) x1, (int) y1);
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

