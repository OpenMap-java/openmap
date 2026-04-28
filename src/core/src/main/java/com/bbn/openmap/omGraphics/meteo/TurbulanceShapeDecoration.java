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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/meteo/TurbulanceShapeDecoration.java,v $
//$RCSfile: TurbulanceShapeDecoration.java,v $
//$Revision: 1.4 $
//$Date: 2005/08/11 20:39:14 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.omGraphics.meteo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

import com.bbn.openmap.omGraphics.awt.AbstractShapeDecoration;

/**
 * A ShapeDecoration for meteorological turbulance conditions.
 */
public class TurbulanceShapeDecoration extends AbstractShapeDecoration {

    /** We use to draw them red ! */
    public static Color COLOR = Color.blue;

    /**
     * Filled half circles for surface fronts, empty ones for altitude
     * fronts
     */
    private boolean filled = true;

    /**
     * Constructor.
     * 
     * @param length
     * @param radius
     */
    public TurbulanceShapeDecoration(int length, int radius) {
        super(length, radius, 1);
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

            g.fillOval((int) (x - w / 2),
                    (int) (y - w / 2),
                    (int) (w),
                    (int) (w));
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

