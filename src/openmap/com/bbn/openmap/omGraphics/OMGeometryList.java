// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMGeometryList.java,v $
// $RCSfile: OMGeometryList.java,v $
// $Revision: 1.9 $
// $Date: 2004/10/14 18:06:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ListIterator;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.GraphicList;
import com.bbn.openmap.util.Debug;

/**
 * This class encapsulates a List of OMGeometries. It's an OMGraphic,
 * so it contains information on how to draw them. It's also a
 * subclass to the OMGraphicList, and relies on many OMGraphicList
 * methods.
 * 
 * <p>
 * The OMGeometryList assumes that all OMGeometries on it should be
 * rendered the same - same fill color, same edge color and stroke,
 * and will create one java.awt.Shape object from all the projected
 * OMGeometries for more efficient rendering. If your individual
 * OMGeometries have independing rendering characteristics, use the
 * OMGraphicList and OMGraphics.
 * 
 * <p>
 * Because the OMGeometryList creates a single java.awt.Shape object
 * for all of its contents, it needs to be generated() if an
 * OMGeometry is added or removed from the list. If you don't
 * regenerate the OMGeometryList, the list will iterate through its
 * contents and render each piece separately.
 */
public class OMGeometryList extends OMGraphicList implements GraphicList,
        Serializable {

    /**
     * Flag to mark that the parts should be connected, making this
     * OMGeometryList a combination OMGraphic that sums disparate
     * parts. False by default.
     */
    protected boolean connectParts = false;

    /**
     * Construct an OMGeometryList.
     */
    public OMGeometryList() {
        super(10);
    };

    /**
     * Construct an OMGeometryList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public OMGeometryList(int initialCapacity) {
        super(initialCapacity);
    };

    /**
     * Construct an OMGeometryList around a List of OMGeometries. The
     * OMGeometryList assumes that all the objects on the list are
     * OMGeometries, and never does checking. Live with the
     * consequences if you put other stuff in there.
     * 
     * @param list List of OMGeometries.
     */
    public OMGeometryList(java.util.List list) {
        super(list);
    }

    /**
     * Add an OMGeometry to the GraphicList. The OMGeometry must not
     * be null.
     * 
     * @param g the non-null OMGeometry to add
     * @exception IllegalArgumentException if OMGeometry is null
     */
    public void add(OMGeometry g) {
        setNeedToRegenerate(true);
        _add(g);
    }

    /**
     * Remove the geometry from the list.
     * 
     * @param geometry the geometry to remove.
     * @return true if geometry was on the list, false if otherwise.
     */
    public boolean remove(OMGeometry geometry) {
        setNeedToRegenerate(true);
        return _remove(geometry);
    }

    /**
     * Return the index of the OMGeometry in the list.
     * 
     * @param geometry the geometry to look for
     * @return the index in the list of the geometry, -1 if the object
     *         is not found.
     */
    public int indexOf(OMGeometry geometry) {
        return _indexOf(geometry);
    }

    /**
     * Set the geometry at the specified location. The OMGeometry must
     * not be null.
     * 
     * @param geometry OMGeometry
     * @param index index of the OMGeometry to return
     * @exception ArrayIndexOutOfBoundsException if index is
     *            out-of-bounds
     */
    public void setAt(OMGeometry geometry, int index) {
        setNeedToRegenerate(true);
        _setAt(geometry, index);
    }

    /**
     * Get the geometry at the location number on the list.
     * 
     * @param location the location of the OMGeometry to return
     * @return OMGeometry or null if location &gt; list size
     * @exception ArrayIndexOutOfBoundsException if
     *            <code>location &lt; 0</code> or
     *            <code>location &gt;=
     * this.size()</code>
     */
    public OMGeometry getAt(int location) {
        return _getAt(location);
    }

    /**
     * Get the geometry with the appObject. Traverse mode doesn't
     * matter. Tests object identity first, then tries equality.
     * 
     * @param appObj appObject of the wanted geometry.
     * @return OMGeometry or null if not found
     * @see Object#equals
     * @see OMGeometry#setAppObject
     * @see OMGeometry#getAppObject
     */
    public OMGeometry getWithAppObject(Object appObj) {
        return _getWithAppObject(appObj);
    }

    /**
     * Remove the geometry at the location number.
     * 
     * @param location the location of the OMGeometry to remove
     */
    public Object removeAt(int location) {
        Object obj = _remove(location);
        if (obj != null) {
            setNeedToRegenerate(true);
        }
        return obj;
    }

    /**
     * Insert the geometry at the location number. The OMGeometry must
     * not be null.
     * 
     * @param geometry the OMGeometry to insert.
     * @param location the location of the OMGeometry to insert
     * @exception ArrayIndexOutOfBoundsException if index is
     *            out-of-bounds
     */
    public void insertAt(OMGeometry geometry, int location) {
        setNeedToRegenerate(true);
        _insert(geometry, location);
    }

    /**
     * Set the stroke for this list object. All geometries will be
     * rendered with this stroke.
     * 
     * @param s the stroke object to use.
     */
    public void setStroke(Stroke s) {
        if (s != null) {
            stroke = s;
        } else {
            stroke = new BasicStroke();
        }
    }

    /**
     * Set the fill paint for this list object. All the geometries
     * will be rendered with this fill paint.
     * 
     * @param paint java.awt.Paint
     */
    public void setFillPaint(Paint paint) {
        if (paint != null) {
            fillPaint = paint;
            if (Debug.debugging("omGraphics")) {
                Debug.output("OMGraphic.setFillPaint(): fillPaint= "
                        + fillPaint);
            }
        } else {
            fillPaint = clear;
            if (Debug.debugging("omGraphics")) {
                Debug.output("OMGraphic.setFillPaint(): fillPaint is clear");
            }
        }
        setEdgeMatchesFill();
    }

    /**
     * Set the texture mask for the OMGeometries on the list. If not
     * null, then it will be rendered on top of the fill paint. If the
     * fill paint is clear, the texture mask will not be used. If you
     * just want to render the texture mask as is, set the fill paint
     * of the graphic instead. This is really to be used to have a
     * texture added to the graphics, with the fill paint still
     * influencing appearance.
     */
    public void setTextureMask(TexturePaint texture) {
        textureMask = texture;
    }

    /**
     * Set the line paint for this list object. All the geometries
     * will be rendered with this fill paint.
     * 
     * @param paint Set the line paint for all the objects on the
     *        list.
     */
    public void setLinePaint(Paint paint) {
        if (paint != null) {
            linePaint = paint;
        } else {
            linePaint = Color.black;
        }

        if (!selected) {
            displayPaint = linePaint;
        }
        setEdgeMatchesFill();
    }

    /**
     * Set the select paint for this list object. All the geometries
     * will be rendered with this fill paint.
     * 
     * @param paint java.awt.Paint
     */
    public void setSelectPaint(Paint paint) {
        if (paint != null) {
            selectPaint = paint;
        } else {
            selectPaint = Color.black;
        }

        if (selected) {
            displayPaint = selectPaint;
        }
        setEdgeMatchesFill();
    }

    /**
     * Set the matting paint for all the objects on the list.
     * 
     * @param paint java.awt.Paint
     */
    public void setMattingPaint(Paint paint) {
        if (paint != null) {
            mattingPaint = paint;
        } else {
            mattingPaint = Color.black;
        }
    }

    /**
     * Set the matting flag for all the list.
     */
    public void setMatted(boolean value) {
        matted = value;
    }

    /**
     * Renders all the objects in the list a geometries context. This
     * is the same as <code>paint()</code> for AWT components. The
     * geometries are rendered in the order of traverseMode. Any
     * geometries where <code>isVisible()</code> returns false are
     * not rendered.
     * 
     * @param gr the AWT Graphics context
     */
    public synchronized void render(Graphics gr) {
        Shape shp = getShape();
        if (shp != null) {

            if (matted) {
                if (gr instanceof Graphics2D && stroke instanceof BasicStroke) {
                    ((Graphics2D) gr).setStroke(new BasicStroke(((BasicStroke) stroke).getLineWidth() + 2f));
                    setGraphicsColor(gr, mattingPaint);
                    draw(gr);
                }
            }

            setGraphicsForFill(gr);
            ((Graphics2D) gr).fill(shp);
            setGraphicsForEdge(gr);
            ((Graphics2D) gr).draw(shp);

        } else {
            ListIterator iterator;
            java.util.List targets = getTargets();
            OMGeometry geometry;

            if (traverseMode == FIRST_ADDED_ON_TOP) {
                iterator = targets.listIterator(targets.size());
                while (iterator.hasPrevious()) {
                    geometry = (OMGeometry) iterator.previous();

                    if (geometry.isVisible()) {
                        renderGeometry(geometry, gr);
                    }

                }
            } else {
                iterator = targets.listIterator();
                while (iterator.hasNext()) {
                    geometry = (OMGeometry) iterator.next();

                    if (geometry.isVisible()) {
                        renderGeometry(geometry, gr);
                    }
                }
            }
        }
    }

    protected void renderGeometry(OMGeometry geometry, Graphics gr) {
        if (matted) {
            if (gr instanceof Graphics2D && stroke instanceof BasicStroke) {
                ((Graphics2D) gr).setStroke(new BasicStroke(((BasicStroke) stroke).getLineWidth() + 2f));
                setGraphicsColor(gr, mattingPaint);
                geometry.draw(gr);
            }
        }

        setGraphicsForFill(gr);
        geometry.fill(gr);
        setGraphicsForEdge(gr);
        geometry.draw(gr);
    }

    /**
     * Renders all the objects in the list a geometry's context, in
     * their 'selected' mode. This is the same as <code>paint()</code>
     * for AWT components. The geometries are rendered in the order of
     * traverseMode. Any geometries where <code>isVisible()</code>
     * returns false are not rendered. All of the geometries on the
     * list are returned to their deselected state.
     * 
     * @param gr the AWT Graphics context
     */
    public void renderAllAsSelected(Graphics gr) {
        if (shape != null) {

            setGraphicsForFill(gr);
            ((Graphics2D) gr).fill(shape);
            select();
            setGraphicsForEdge(gr);
            ((Graphics2D) gr).draw(shape);
            deselect();

        }
    }

    /**
     * Prepare the geometries for rendering. This must be done before
     * calling <code>render()</code>! This recursively calls
     * generate() on the OMGeometries on the list.
     * 
     * @param p a <code>Projection</code>
     * @param forceProjectAll if true, all the geometries on the list
     *        are generated with the new projection. If false they are
     *        only generated if getNeedToRegenerate() returns true
     * @see OMGeometry#generate
     * @see OMGeometry#regenerate
     */
    public synchronized void generate(Projection p, boolean forceProjectAll) {

        // Important! Resets the shape.
        shape = null;

        // Create a shape object out of all of the shape objects.
        ListIterator iterator;

        if (traverseMode == FIRST_ADDED_ON_TOP) {
            iterator = graphics.listIterator(graphics.size());
            while (iterator.hasPrevious()) {
                updateShape((OMGeometry) iterator.previous(),
                        p,
                        forceProjectAll);
            }
        } else {
            iterator = graphics.listIterator();
            while (iterator.hasNext()) {
                updateShape((OMGeometry) iterator.next(), p, forceProjectAll);
            }
        }
        setNeedToRegenerate(false);
    }

    /**
     * Given a OMGeometry, it calls generate/regenerate on it, and
     * then adds the GeneralPath shape within it to the OMGeometryList
     * shape object.
     */
    protected void updateShape(OMGeometry geometry, Projection p,
                               boolean forceProject) {

        if (forceProject) {
            geometry.generate(p);
        } else {
            geometry.regenerate(p);
        }

        if (geometry.isVisible()) {
            GeneralPath gp = (GeneralPath) geometry.getShape();

            if (gp == null) {
                return;
            }

            if (shape == null) {
                shape = gp;
            } else {
                ((GeneralPath) shape).append(gp, connectParts);
            }
        }
    }

    /**
     * Return the shortest distance from the graphic to an XY-point.
     * Checks to see of the point is contained within the OMGraphic,
     * which may, or may not be the right thing for clear OMGraphics
     * or lines.
     * <p>
     * 
     * _distance was added so subclasses could make this call if their
     * geometries/attributes require this action (when fill color
     * doesn't matter).
     * 
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point.
     *         Returns Float.POSITIVE_INFINITY if the graphic isn't
     *         ready (ungenerated).
     */
    protected float _distance(int x, int y) {
        float temp, distance = Float.POSITIVE_INFINITY;

        if (isVague()) {

            if (getNeedToRegenerate() || shape == null) {
                return distance;
            }

            if (shape.contains((double) x, (double) y)) {
                //          if (Debug.debugging("omgraphicdetail")) {
                //              Debug.output(" contains " + x + ", " + y);
                //          }
                distance = 0f;
            } else {
                distance = distanceToEdge(x, y);
            }
        } else {
            distance = super._distance(x, y);
        }

        return distance;
    }

    protected synchronized OMDist _findClosest(int x, int y, float limit,
                                               boolean resetSelect) {

        if (shape != null) {
            float currentDistance = _distance(x, y);
            OMDist omd = new OMDist();

            if (currentDistance < limit) {
                omd.omg = this;
                omd.d = currentDistance;
            }
            return omd;
        } else {
            return super._findClosest(x, y, limit, resetSelect);
        }
    }

    /**
     * Finds the object located the closest to the point, if the
     * object distance away is within the limit. The search is always
     * conducted from the topmost geometry to the bottommost,
     * depending on the traverseMode. Any geometries where
     * <code>isVisible()</code> returns false are not considered.
     * 
     * @param x the x coordinate on the component the geometries are
     *        displayed on.
     * @param y the y coordinate on the component the geometries are
     *        displayed on.
     * @param limit the max distance that a geometry has to be within
     *        to be returned, in pixels.
     * @return OMGeometry the closest on the list within the limit, or
     *         null if not found.
     */
    public OMGeometry findClosestGeometry(int x, int y, float limit) {
        return _findClosest(x, y, limit).omg;
    }

    /**
     * This method returns an OMGraphic if the thing that is found
     * closest to the coordinates is an OMGraphic. It mose likely is
     * an OMGeometry, so it can return null if it found something
     * close to the coordinates that isn't an OMGraphic.
     */
    public OMGraphic findClosest(int x, int y, float limit) {
        return objectToOMGraphic(_findClosest(x, y, limit).omg);
    }

    /**
     * This method returns an OMGraphic if the thing that is found
     * closest to the coordinates is an OMGraphic. It mose likely is
     * an OMGeometry, so it can return null if it found something
     * close to the coordinates that isn't an OMGraphic. It will tell
     * anything it finds to be selected, however, whether it is an
     * OMGraphic or OMGeometry.
     */
    public OMGraphic selectClosest(int x, int y, float limit) {
        return objectToOMGraphic(_selectClosest(x, y, limit));
    }

    /**
     * Finds the object located the closest to the point, regardless
     * of how far away it is. This method returns null if the list is
     * not valid. The search starts at the first-added geometry. <br>
     * This is the same as calling
     * <code>findClosest(x, y, Float.MAX_VALUE)</code>.
     * 
     * @param x the horizontal pixel position of the window, from the
     *        left of the window.
     * @param y the vertical pixel position of the window, from the
     *        top of the window.
     * @return the closest geometry to the xy window point.
     * @see #findClosest(int, int, float)
     */
    public OMGeometry findClosestGeometry(int x, int y) {
        return _findClosest(x, y, Float.MAX_VALUE).omg;
    }

    /**
     * Finds the object located the closest to the coordinates,
     * regardless of how far away it is. Sets the select paint of that
     * object, and resets the paint of all the other objects. The
     * search starts at the first-added graphic.
     * 
     * @param x the x coordinate on the component the graphics are
     *        displayed on.
     * @param y the y coordinate on the component the graphics are
     *        displayed on.
     * @return the closest OMGraphic on the list.
     */
    public OMGeometry selectClosestGeometry(int x, int y) {
        return _selectClosest(x, y, Float.MAX_VALUE);
    }

    /**
     * Finds the object located the closest to the point, if the
     * object distance away is within the limit, and sets the paint of
     * that geometry to its select paint. It sets the paints to all
     * the other objects to the regular paint. The search starts at
     * the first-added geometry. Any geometries where
     * <code>isVisible()</code> returns false are not considered.
     * 
     * @param x the horizontal pixel position of the window, from the
     *        left of the window.
     * @param y the vertical pixel position of the window, from the
     *        top of the window.
     * @param limit the max distance that a geometry has to be within
     *        to be returned, in pixels.
     * @return the closest OMGeometry on the list, within the limit or
     *         null if none found.
     */
    public OMGeometry selectClosestGeometry(int x, int y, float limit) {
        return _selectClosest(x, y, limit);
    }

    /**
     * Finds the first OMGeometry (the one on top) that is under this
     * pixel. This method will return the particular OMGeometry that
     * may fall around the pixel location. If you want to know if the
     * pixel touches any part of this list, call contains(x, y)
     * instead.
     * 
     * @param x the horizontal pixel position of the window, from the
     *        left of the window.
     * @param y the vertical pixel position of the window, from the
     *        top of the window.
     * @return the geometry that contains the pixel, NONE (null) if
     *         none are found.
     */
    public OMGeometry getContains(int x, int y) {
        if (shape != null && isVague() && shape.contains(x, y)) {
            return this;
        } else {
            return _getContains(x, y);
        }
    }

    /**
     * Returns this list if x, y is inside the bounds of the contents
     * of this list.
     */
    public OMGraphic getOMGraphicThatContains(int x, int y) {
        return objectToOMGraphic(getContains(x, y));
    }

    /**
     * Perform an action on the provided geometry. If the geometry is
     * not currently on the list, it is added (if the action doesn't
     * say to delete it). If the geometry is null, the list checks for
     * an action to take on the list (deselectAll).
     */
    public void doAction(OMGeometry geometry, OMAction action) {
        _doAction(geometry, action);
    }

    /**
     * Read a cache of OMGeometries, given a ObjectInputStream.
     * 
     * @param objstream ObjectInputStream of geometry list.
     */
    public void readGraphics(ObjectInputStream objstream) throws IOException {

        Debug.message("omgraphics", "OMGeometryList: Reading cached geometries");

        try {
            while (true) {
                try {
                    OMGeometry omg = (OMGeometry) objstream.readObject();
                    this.add(omg);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (OptionalDataException ode) {
                    ode.printStackTrace();
                }
            }
        } catch (EOFException e) {
        }
    }

    /**
     * Set whether the OMGeometries on the list should be connected to
     * make a one-part shape object (if true), or a multi-part shape
     * object (if false).
     */
    public void setConnectParts(boolean value) {
        connectParts = value;
    }

    /**
     * Get whether the OMGeometries on the list should be connected to
     * make a one-part shape object (if true), or a multi-part shape
     * object (if false).
     */
    public boolean getConnectParts() {
        return connectParts;
    }

}