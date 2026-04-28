package com.bbn.openmap.omGraphics.awt;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * A minimal implementation of ShapeDecoration, adds members and
 * accessors for graphic attributes :
 * <UL>
 * <LI>length : the decoration length, along the path to decorate
 * </LI>
 * <LI>width : the decoration width, normal to the path to decorate
 * </LI>
 * <LI>orientation : the decoration is on the left or on the right of
 * the path</LI>
 * <LI>stroke, paint : the stroke and paint to be used</LI>
 * </UL>
 * 
 * @author Eric LEPICIER
 * @version 27 juil. 2002
 */
public abstract class AbstractShapeDecoration implements ShapeDecoration {

    private float width;
    private float length;
    private int orientation;
    private Stroke stroke;
    private Paint paint;

    private Stroke saveStroke;
    private Paint savePaint;

    /**
     * Constructor.
     * 
     * @param length
     * @param width
     * @param orientation
     */
    public AbstractShapeDecoration(float length, float width, int orientation) {
        this.length = length;
        this.width = width;
        this.orientation = orientation;
    }

    /**
     * Returns the length.
     * 
     * @return float
     */
    public float getLength() {
        return length;
    }

    /**
     * Sets the length.
     * 
     * @param length The length to set
     */
    public void setLength(float length) {
        this.length = length;
    }

    /**
     * Returns the width.
     * 
     * @return float
     */
    public float getWidth() {
        return width;
    }

    /**
     * Sets the width.
     * 
     * @param width The width to set
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * Returns the orientation.
     * 
     * @return int
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation.
     * 
     * @param orientation The orientation to set
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * Reverts the orientation
     * 
     * @see com.bbn.openmap.omGraphics.awt.ShapeDecoration#revert()
     */
    public void revert() {
        orientation = orientation == RIGHT ? LEFT : RIGHT;
    }

    /**
     * Returns the paint.
     * 
     * @return Paint
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Returns the stroke.
     * 
     * @return Stroke
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the paint.
     * 
     * @param paint The paint to set
     */
    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    /**
     * Sets the stroke.
     * 
     * @param stroke The stroke to set
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    /**
     * Applies stroke and paint to the Graphics, saving previous
     * settings
     * 
     * @param g
     */
    protected void setGraphics(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        saveStroke = g2D.getStroke();
        savePaint = g2D.getPaint();
        if (stroke != null)
            g2D.setStroke(stroke);
        if (paint != null)
            g2D.setPaint(paint);
    }

    /**
     * Restores previous settings to the Graphics. Beware : no
     * verification is made to be sure that it is the same Graphics
     * ...
     * 
     * @param g
     */
    protected void restoreGraphics(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setStroke(saveStroke);
        g2D.setPaint(savePaint);
    }
}

