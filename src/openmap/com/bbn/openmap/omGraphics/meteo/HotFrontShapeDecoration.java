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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/meteo/HotFrontShapeDecoration.java,v $
//$RCSfile: HotFrontShapeDecoration.java,v $
//$Revision: 1.6 $
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
 * A ShapeDecoration for meteorological hot fronts.
 * 
 * @author Eric LEPICIER
 * @version 28 juil. 2002
 */
public class HotFrontShapeDecoration extends AbstractShapeDecoration {

    /** We use to draw them red ! */
    public static Color COLOR = Color.red;

    /**
     * Filled half circles for surface fronts, empty ones for altitude
     * fronts
     */
    private boolean filled = true;

    /**
     * Constructor.
     * 
     * @param length
     * @param width
     * @param orientation
     */
    public HotFrontShapeDecoration(int length, int width, int orientation) {
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
        int xcoord[] = new int[nbpts + 6];
        int ycoord[] = new int[nbpts + 6];

        for (int i = 0; i < nbpts; i++) {
            xcoord[i] = (int) points[i].getX();
            ycoord[i] = (int) points[i].getY();
        }

        if (complete) {
            int orient = getOrientation() == LEFT ? -1 : 1;

            // Compute cosinus and sinus of rotation angle
            double dx = points[nbpts - 1].getX() - points[0].getX();
            double dy = points[nbpts - 1].getY() - points[0].getY();
            double norm = Math.sqrt(dx * dx + dy * dy);
            double rcos = dx / norm;
            double rsin = dy / norm;

            // Compute vertices (6 lines to approximate)
            double ll = getLength(); // en x avant rotation
            double ww = orient * getWidth(); // en y avant rotation
            double l, w;
            l = 0.9330127 * ll; // i.e. (2 + sqrt(3))/4
            w = 0.5 * ww;
            xcoord[nbpts] = (int) (points[0].getX() + l * rcos - w * rsin);
            ycoord[nbpts++] = (int) (points[0].getY() + l * rsin + w * rcos);
            l = 0.85355339 * ll; // i.e. (2 + sqrt(2))/4
            w = 0.70710678 * ww;
            xcoord[nbpts] = (int) (points[0].getX() + l * rcos - w * rsin);
            ycoord[nbpts++] = (int) (points[0].getY() + l * rsin + w * rcos);
            l = 0.5 * ll;
            w = ww;
            xcoord[nbpts] = (int) (points[0].getX() + l * rcos - w * rsin);
            ycoord[nbpts++] = (int) (points[0].getY() + l * rsin + w * rcos);
            l = 0.14644661 * ll; // i.e. (2 - sqrt(2))/4
            w = 0.70710678 * ww;
            xcoord[nbpts] = (int) (points[0].getX() + l * rcos - w * rsin);
            ycoord[nbpts++] = (int) (points[0].getY() + l * rsin + w * rcos);
            l = 0.066987298 * ll; // i.e. (2 - sqrt(3))/4
            w = 0.5 * ww;
            xcoord[nbpts] = (int) (points[0].getX() + l * rcos - w * rsin);
            ycoord[nbpts++] = (int) (points[0].getY() + l * rsin + w * rcos);

            // link to start
            xcoord[nbpts] = (int) points[0].getX();
            ycoord[nbpts++] = (int) points[0].getY();

            if (filled)
                g.fillPolygon(xcoord, ycoord, nbpts);
        }
        g.drawPolyline(xcoord, ycoord, nbpts);
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

