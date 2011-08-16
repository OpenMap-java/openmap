package com.bbn.openmap.omGraphics.awt;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * A ShapeDecoration that draws a text along a path
 * 
 * @author Eric LEPICIER
 * @version 16 aot 2002
 */
public class TextShapeDecoration
        extends AbstractShapeDecoration {
    private String text;
    private Font font = null;
    private int verticalAlignment = BASELINE;

    /** Baseline vertical alignment */
    public final static int BASELINE = 1;
    /** Center vertical alignment */
    public final static int CENTER = 2;
    /** Top vertical alignment */
    public final static int TOP = 3;
    /** Bottom vertical alignment */
    public final static int BOTTOM = 4;

    /** Orientation for the shape text decoration: use poly direction */
    public final static int FORWARD = LEFT;
    /**
     * Orientation for the shape text decoration: use reverse poly direction
     */
    public final static int BACKWARD = RIGHT;
    /** Orientation for the shape text decoration: force left to right */
    public final static int LEFT_TO_RIGHT = 3;
    /** Orientation for the shape text decoration: force right to left */
    public final static int RIGHT_TO_LEFT = 4;
    /** Orientation for the shape text decoration: force top to bottom */
    public final static int TOP_TO_BOTTOM = 5;
    /** Orientation for the shape text decoration: force bottom to top */
    public final static int BOTTOM_TO_TOP = 6;
    /**
     * Orientation for the shape text decoration: occidental reading use
     */
    public final static int MOST_READABLE = 7;

    /**
     * Text will follow the poly instead of being written on the segment from
     * begin to end of the poly, also allow text to be uncomplete. You may add
     * this one to the other constants
     */
    public final static int FOLLOW_POLY = 16;

    private transient FontMetrics metrics;
    private transient Graphics2D g2D = null;

    /**
     * Constructor.
     * 
     * @param text
     * @param font
     * @param orientation
     * @param verticalAlignment
     */
    public TextShapeDecoration(String text, Font font, int orientation, int verticalAlignment) {
        super(0.0f, 0.0f, orientation);
        setVerticalAlignment(verticalAlignment);
        setFont(font);
        setText(text);
        initMetrics();
    }

    /**
     * Constructor.
     * 
     * @param text
     */
    public TextShapeDecoration(String text) {
        super(0.0f, 0.0f, FORWARD);
        setVerticalAlignment(BASELINE);
        setText(text);
        initMetrics();
    }

    /**
     * init Metrics used to get string graphic length.
     * 
     * @param object
     */
    private void initMetrics() {
        if (g2D == null)
            g2D = (Graphics2D) new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).getGraphics();
        if (font == null)
            font = g2D.getFont();
        metrics = g2D.getFontMetrics(font);
    }

    /**
     * Draws the text along the polyline
     * 
     * @see com.bbn.openmap.omGraphics.awt.ShapeDecoration#draw(Graphics,
     *      Point2D[], boolean)
     */
    public void draw(Graphics g, Point2D[] points, boolean complete) {

        g2D = (Graphics2D) g;
        initMetrics();
        g.setFont(font);

        int x1 = (int) points[0].getX();
        int y1 = (int) points[0].getY();
        int x2 = (int) points[points.length - 1].getX();
        int y2 = (int) points[points.length - 1].getY();

        boolean reverse = needToReverse(x1, y1, x2, y2);

        // draw the text exactly on the curve, even uncomplete
        if ((getOrientation() & FOLLOW_POLY) > 0) {
            drawFollow(g, points, reverse);
            return;
        }

        // if there's not enough room, do nothing
        if (!complete)
            return;

        // else plot the text straight on the half segment
        // from start to end point and away
        int x, y;
        double angle;
        if (reverse) {
            x = x2;
            y = y2;
            angle = Math.atan2(y1 - y2, x1 - x2);
        } else {
            x = x1;
            y = y1;
            angle = Math.atan2(y2 - y1, x2 - x1);
        }

        // adjust vertical alignment
        switch (verticalAlignment) {
            case TOP:
                y += metrics.getAscent();
                break;
            case BOTTOM:
                y -= metrics.getDescent();
                break;
            case CENTER:
                y += metrics.getAscent() - (metrics.getHeight()) / 2;
                break;
            case BASELINE:
                break;
        }

        drawAngledString(g, text, x, y, angle);
    }

    /**
     * Returns true if the polyline need to be reverted for the text to be drawn
     * with the specified orientation.
     * 
     * @param x1 starting point x coordinate
     * @param y1 starting point y coordinate
     * @param x2 ending point x coordinate
     * @param y2 ending point y coordinate
     * @return boolean
     */
    protected boolean needToReverse(int x1, int y1, int x2, int y2) {
        boolean reverse = false;
        switch (getOrientation() & ~FOLLOW_POLY) {
            case FORWARD:
                break;
            case BACKWARD:
                reverse = true;
                break;
            case LEFT_TO_RIGHT:
                reverse = x1 > x2;
                break;
            case RIGHT_TO_LEFT:
                reverse = x1 < x2;
                break;
            case TOP_TO_BOTTOM:
                reverse = y1 > y2;
                break;
            case BOTTOM_TO_TOP:
                reverse = y1 < y2;
                break;
            case MOST_READABLE:
                reverse = x2 < x1 || y1 > y2;
                break;
        }
        return reverse;
    }

    /**
     * Draws the text character per character to follow the polyline
     * 
     * @param g
     * @param pts
     * @param reverse
     */
    protected void drawFollow(Graphics g, Point2D[] pts, boolean reverse) {
        LinkedList points = new LinkedList();
        if (reverse) {
            for (int i = pts.length - 1; i >= 0; i--)
                points.add(pts[i]);
        } else {
            for (int i = 0; i < pts.length; i++)
                points.add(pts[i]);
        }

        LinkedList polysegment = new LinkedList();
        int l, x1, y1, x2, y2;
        String c;
        Point2D p1, p2;
        double angle;
        for (int i = 0; i < text.length(); i++) {
            c = text.substring(i, i + 1);
            l = metrics.stringWidth(c);
            if (points.isEmpty())
                break;
            LineUtil.retrievePoints(l, points, polysegment);

            p1 = (Point2D) polysegment.getFirst();
            x1 = (int) p1.getX();
            y1 = (int) p1.getY();
            p2 = (Point2D) polysegment.getLast();
            x2 = (int) p2.getX();
            y2 = (int) p2.getY();

            angle = Math.atan2(y2 - y1, x2 - x1);
            drawAngledString(g, c, x1, y1, angle);
        }
    }

    /**
     * Draws the text from a starting point with an angle
     * 
     * @param g
     * @param text
     * @param x
     * @param y
     * @param angle
     */
    public static void drawAngledString(Graphics g, String text, int x, int y, double angle) {

        Graphics2D g2D = (Graphics2D) g;
        AffineTransform oldAt = g2D.getTransform();
        AffineTransform at = new AffineTransform(oldAt);
        at.rotate(angle, x, y);
        g2D.setTransform(at);
        g2D.drawString(text, x, y);
        g2D.setTransform(oldAt);
    }

    /**
     * Returns the font.
     * 
     * @return Font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Returns the text.
     * 
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the font.
     * 
     * @param font The font to set
     */
    public void setFont(Font font) {
        this.font = font;
        initMetrics();
    }

    /**
     * Sets the text.
     * 
     * @param text The text to set
     */
    public void setText(String text) {
        this.text = text;
        initMetrics();
        setLength(metrics.stringWidth(text));
        setWidth(metrics.getHeight());
    }

    /**
     * Returns the verticalAlignment.
     * 
     * @return int
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the verticalAlignment.
     * 
     * @param verticalAlignment The verticalAlignment to set (TOP, CENTER,
     *        BASELINE, BOTTOM)
     */
    public void setVerticalAlignment(int verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

}
