package com.bbn.openmap.omGraphics.awt;

import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 * The most simple decoration : space.
 * 
 * @author Eric LEPICIER
 * @version 27 juil. 2002
 */
public class SpacingShapeDecoration extends AbstractShapeDecoration {

    /**
     * Constructor.
     * 
     * @param length
     */
    public SpacingShapeDecoration(float length) {
        super(length, 0.0f, LEFT);
    }

    /**
     * @see com.bbn.openmap.omGraphics.awt.ShapeDecoration#draw(Graphics,
     *      Point2D[], boolean)
     */
    public void draw(Graphics g, Point2D[] points, boolean complete) {
    // nothing to do, it's space !
    }
}

