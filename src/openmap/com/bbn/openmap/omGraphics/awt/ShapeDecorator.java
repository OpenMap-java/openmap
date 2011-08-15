package com.bbn.openmap.omGraphics.awt;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The ShapeDecorator class : in charge of drawing repeatedly several
 * stored decorations along a path (a polyline or a complex shape)
 * 
 * @author Eric LEPICIER
 * @version 26 juil. 2002
 */
public class ShapeDecorator implements Revertable {

    /**
     * The list of decorations (instances of ShapeDecoration)
     */
    private List<ShapeDecoration> decorations = new ArrayList<ShapeDecoration>();

    /**
     * The flatness used for Shape.getPathIterator()
     */
    public static double FLATNESS = 0.1;

    /**
     * Default constructor
     */
    public ShapeDecorator() {}

    /**
     * Returns the decorations.
     * 
     * @return List the list of decorations
     */
    public List<ShapeDecoration> getDecorations() {
        return decorations;
    }

    /**
     * Inserts a Decoration.
     * 
     * @param index the index where to insert the new decoration
     * @param decoration the new decoration
     */
    public void insertDecoration(int index, ShapeDecoration decoration) {
        decorations.add(index, decoration);
    }

    /**
     * Adds a Decoration at the end of the list.
     * 
     * @param decoration the new decoration
     */
    public void addDecoration(ShapeDecoration decoration) {
        decorations.add(decoration);
    }

    /**
     * Removes a Decoration.
     * 
     * @param index the index of the Decoration to be removed
     * @return ShapeDecoration the removed Decoration
     */
    public ShapeDecoration removeDecoration(int index) {
        return decorations.remove(index);
    }

    /**
     * Removes a Decoration.
     * 
     * @param decoration the decoration to remove
     * @return boolean true if it was removed
     */
    public boolean removeDecoration(ShapeDecoration decoration) {
        return decorations.remove(decoration);
    }

    /**
     * Reverts all the decorations
     * 
     * @see com.bbn.openmap.omGraphics.awt.Revertable#revert()
     */
    public void revert() {
        for (ShapeDecoration shapeDecoration : decorations) {
            shapeDecoration.revert();
        }
    }

    /**
     * Draws a decorated shape
     * 
     * @param g the Graphics to use
     * @param s the shape to render
     */
    public void draw(Graphics g, Shape s) {

        if (decorations.isEmpty())
            return;

        PathIterator pi = s.getPathIterator(null, FLATNESS);
        int segType;
        double[] segCoords = new double[6];

        LinkedList points = new LinkedList();
        Point2D firstPoint = null;
        Point2D point;

        // split path in polylines
        do {
            segType = pi.currentSegment(segCoords);
            point = new Point2D.Double(segCoords[0], segCoords[1]);

            switch (segType) {
            case PathIterator.SEG_MOVETO:
                if (firstPoint == null)
                    firstPoint = point;

                if (!points.isEmpty()) {
                    // draw decorations for the previous polyline
                    draw(g, points);
                }
                // init a new polyline
                points.clear();
                points.add(point);
                break;
            case PathIterator.SEG_LINETO:
                points.add(point);
                break;
            case PathIterator.SEG_CLOSE:
                points.add(firstPoint);
                break;
            }
            pi.next();
        } while (!pi.isDone());

        // draw decorations for the last poly
        if (!points.isEmpty()) {
            draw(g, points);
        }
    }

    /**
     * Draws a decorated polyline
     * 
     * @param g the Graphics to use
     * @param xcoords array of x floating coordinates
     * @param ycoords array of y floating coordinates
     */
    public void draw(Graphics g, float xcoords[], float[] ycoords) {
        LinkedList points = new LinkedList();
        for (int i = 0; i < xcoords.length; i++)
            points.add(new Point2D.Double(xcoords[i], ycoords[i]));
        draw(g, points);
    }

    /**
     * Draws a decorated polyline
     * 
     * @param g the Graphics to use
     * @param xcoords array of x integer coordinates
     * @param ycoords array of y integer coordinates
     */
    public void draw(Graphics g, int xcoords[], int[] ycoords) {
        LinkedList points = new LinkedList();
        for (int i = 0; i < xcoords.length; i++)
            points.add(new Point2D.Double(xcoords[i], ycoords[i]));
        draw(g, points);
    }

    /**
     * Draws a decorated polyline
     * 
     * @param g the Graphics to use
     * @param points array of points
     */
    public void draw(Graphics g, Point2D[] points) {
        LinkedList pointlist = new LinkedList();
        for (int i = 0; i < points.length; i++)
            pointlist.add(points[i]);
        draw(g, pointlist);
    }

    /**
     * Draws a decorated polyline Calls ShapeDecoration.draw(...) for
     * each decoration on an subsetted polyline with the same length
     * than the decoration, cycling until all the path is consumed.
     * 
     * @param g the Graphics to use
     * @param points array of points (instances of Point2D)
     */
    protected void draw(Graphics g, LinkedList points) {

        if (decorations.isEmpty())
            throw new NullPointerException("No decorations");

        Iterator decorationIterator = decorations.listIterator();
        LinkedList polysegment = new LinkedList();
        Point2D[] point2DArrayType = new Point2D.Double[1];

        while (!points.isEmpty()) {
            if (!decorationIterator.hasNext())
                decorationIterator = decorations.listIterator();
            ShapeDecoration decor = (ShapeDecoration) decorationIterator.next();

            boolean complete = LineUtil.retrievePoints(decor.getLength(),
                    points,
                    polysegment);
            // drawing is delegated to the decoration
            decor.draw(g,
                    (Point2D[]) polysegment.toArray(point2DArrayType),
                    complete);
        }
    }

}

