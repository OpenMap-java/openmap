package com.bbn.openmap.omGraphics.awt;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;

/**
 * A decoration which is just a line.
 * 
 * @author Eric LEPICIER
 * @version 28 juil. 2002
 */
public class LineShapeDecoration extends SpacingShapeDecoration {

    /**
     * Constructor.
     * 
     * @param length
     */
    public LineShapeDecoration(float length) {
        super(length);
    }

    /**
     * Constructor.
     * 
     * @param length
     * @param paint
     */
    public LineShapeDecoration(float length, Paint paint) {
        super(length);
        setPaint(paint);
    }

    /**
     * Constructor.
     * 
     * @param length
     * @param paint
     * @param stroke
     */
    public LineShapeDecoration(float length, Paint paint, Stroke stroke) {
        super(length);
        setPaint(paint);
        setStroke(stroke);
    }

    /**
     * @see com.bbn.openmap.omGraphics.awt.ShapeDecoration#draw(Graphics,
     *      Point2D[], boolean)
     */
    public void draw(Graphics g, Point2D[] points, boolean complete) {
        Graphics2D g2D = (Graphics2D) g;
        setGraphics(g2D);
        // we just need to draw the poly line
        for (int i = 0; i < points.length - 1; i++)
            g2D.drawLine((int) points[i].getX(),
                    (int) points[i].getY(),
                    (int) points[i + 1].getX(),
                    (int) points[i + 1].getY());
        restoreGraphics(g2D);
    }
}