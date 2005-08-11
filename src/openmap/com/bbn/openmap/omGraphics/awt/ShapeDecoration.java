package com.bbn.openmap.omGraphics.awt;

import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 * A ShapeDecoration is a basic element to be drawn along a path by a
 * ShapeDecorator. The decoration length is measured along the path to
 * decorate,</LI>
 * the width, on a normal to the path.</LI>
 * The decoration may be on the left or on the right of the path.
 * </LI>
 * 
 * @author Eric LEPICIER
 * @version 26 juil. 2002
 */
public interface ShapeDecoration extends Revertable {

    /**
     * The right orientation constant
     */
    public static final int RIGHT = 1;
    /**
     * The left orientation constant
     */
    public static final int LEFT = 2;

    /**
     * Returns the length.
     * 
     * @return float
     */
    public float getLength();

    /**
     * Returns the width.
     * 
     * @return float
     */
    public float getWidth();

    /**
     * Returns the orientation.
     * 
     * @return int
     */
    public int getOrientation();

    /**
     * Sets the orientation.
     * 
     * @param orientation The orientation to set
     */
    public void setOrientation(int orientation);

    /**
     * reverts the orientation
     */
    public void revert();

    /**
     * Draws itself along the specified polyline Called by
     * ShapeDecorator
     * 
     * @see com.bbn.openmap.omGraphics.awt.ShapeDecorator#draw(Graphics, Point2D[])
     * @param g
     * @param points the polyline
     * @param complete true if the polyline length equals the
     *        decoration length
     */
    public void draw(Graphics g, Point2D[] points, boolean complete);
}

