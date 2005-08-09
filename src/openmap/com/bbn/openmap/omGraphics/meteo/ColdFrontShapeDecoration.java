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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/meteo/ColdFrontShapeDecoration.java,v $
//$RCSfile: ColdFrontShapeDecoration.java,v $
//$Revision: 1.7 $
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
 * A ShapeDecoration for meteorological cold fronts.
 * 
 * @author Eric LEPICIER
 * @version 28 juil. 2002
 */
public class ColdFrontShapeDecoration extends AbstractShapeDecoration {

    /** We use to draw them blue ! */
    public static Color COLOR = Color.blue;

    /**
     * Filled half triangles for surface fronts, empty ones for
     * altitude fronts
     */
    private boolean filled = true;

    /**
     * Constructor.
     * 
     * @param length
     * @param width
     * @param orientation
     */
    public ColdFrontShapeDecoration(int length, int width, int orientation) {
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
        int xcoord[] = new int[nbpts + 2];
        int ycoord[] = new int[nbpts + 2];

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

            // Compute vertices
            double r = getLength() / 2.0; // x radius before rotation
            double w = orient * getWidth(); // y radius before
                                            // rotation
            // rotate
            xcoord[nbpts] = (int) (points[0].getX() + r * rcos - w * rsin);
            ycoord[nbpts++] = (int) (points[0].getY() + r * rsin + w * rcos);

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
     * Sets the filled (draw a polygon or a polyline ?).
     * 
     * @param filled The filled to set
     */
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

}

