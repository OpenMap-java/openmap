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
// $Revision: 1.10 $
// $Date: 2005/08/09 20:01:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * This class encapsulates a List of OMGeometries. It's an OMGraphic, so it
 * contains information on how to draw them. It's also a subclass to the
 * OMGraphicList, and relies on many OMGraphicList methods.
 * 
 * <p>
 * The OMGeometryList assumes that all OMGeometries on it should be rendered the
 * same - same fill color, same edge color and stroke, and will create one
 * java.awt.Shape object from all the projected OMGeometries for more efficient
 * rendering. If your individual OMGeometries have independent rendering
 * characteristics, use the OMGraphicList and OMGraphics.
 * 
 * <p>
 * Because the OMGeometryList creates a single java.awt.Shape object for all of
 * its contents, it needs to be generated() if an OMGeometry is added or removed
 * from the list. If you don't regenerate the OMGeometryList, the list will
 * iterate through its contents and render each piece separately.
 */
public class OMGeometryList extends OMList<OMGeometry> implements Serializable {

    /**
     * Flag to mark that the parts should be connected, making this
     * OMGeometryList a combination OMGraphic that sums disparate parts. False
     * by default.
     */
    protected boolean connectParts = false;

    /**
     * Construct an OMGeometryList.
     */
    public OMGeometryList() {
    }

    /**
     * Construct an OMGeometryList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public OMGeometryList(int initialCapacity) {
        super(initialCapacity);
    }

    public OMGeometryList(Collection<OMGeometry> c) {
        graphics.addAll(c);
    }

    /**
     * Add an OMGeometry to the GraphicList. The OMGeometry must not be null.
     * 
     * @param g the non-null OMGeometry to add
     * @exception IllegalArgumentException if OMGeometry is null
     */
    public boolean add(OMGeometry g) {
        setNeedToRegenerate(true);
        return super.add(g);
    }

    /**
     * For backward compatibility.
     * 
     * @param omg
     * @return true if add was successful
     */
    public boolean addOMGraphic(OMGraphic omg) {
        return super.add(omg);
    }

    /**
     * Remove the geometry from the list.
     * 
     * @param geometry the geometry to remove.
     * @return true if geometry was on the list, false if otherwise.
     */
    public boolean remove(OMGeometry geometry) {
        setNeedToRegenerate(true);
        return super.remove(geometry);
    }

    /**
     * Set the geometry at the specified location. The OMGeometry must not be
     * null.
     * 
     * @param geometry OMGeometry
     * @param index index location of the OMGeometry placement.
     * @exception ArrayIndexOutOfBoundsException if index is out-of-bounds
     */
    public OMGeometry set(int index, OMGeometry geometry) {
        setNeedToRegenerate(true);
        synchronized (graphics) {
            return graphics.set(index, geometry);
        }
    }

    /**
     * Remove the geometry at the location number.
     * 
     * @param location the location of the OMGeometry to remove
     */
    public OMGeometry remove(int location) {
        OMGeometry obj = super.remove(location);
        if (obj != null) {
            setNeedToRegenerate(true);
        }
        return obj;
    }

    /**
     * Renders all the objects in the list a geometries context. This is the
     * same as <code>paint()</code> for AWT components. The geometries are
     * rendered in the order of traverseMode. Any geometries where
     * <code>isVisible()</code> returns false are not rendered.
     * 
     * @param gr the AWT Graphics context
     */
    public void render(Graphics gr) {
        
        if (isVague() && !isVisible())
            return;
        
        Shape shp = getShape();
        if (shp != null) {

            if (matted) {
                if (gr instanceof Graphics2D && stroke instanceof BasicStroke) {
                    ((Graphics2D) gr).setStroke(new BasicStroke(((BasicStroke) stroke).getLineWidth() + 2f));
                    setGraphicsColor(gr, mattingPaint);
                    ((Graphics2D) gr).draw(shp);
                }
            }

            setGraphicsForFill(gr);
            ((Graphics2D) gr).fill(shp);
            setGraphicsForEdge(gr);
            ((Graphics2D) gr).draw(shp);

        } else {

            synchronized (graphics) {
                if (traverseMode == FIRST_ADDED_ON_TOP) {
                    ListIterator<OMGeometry> iterator = graphics.listIterator(graphics.size());
                    while (iterator.hasPrevious()) {
                        renderGeometry(iterator.previous(), gr);
                    }
                } else {
                    ListIterator<OMGeometry> iterator = graphics.listIterator();
                    while (iterator.hasNext()) {
                        renderGeometry(iterator.next(), gr);
                    }
                }
            }
        }

        renderLabel(gr);
    }

    protected void renderGeometry(OMGeometry geometry, Graphics gr) {

        Shape shp = geometry.getShape();

        boolean isRenderable = !geometry.getNeedToRegenerate() && geometry.isVisible()
                && shp != null;

        if (isRenderable) {
            if (matted) {
                if (gr instanceof Graphics2D && stroke instanceof BasicStroke) {
                    ((Graphics2D) gr).setStroke(new BasicStroke(((BasicStroke) stroke).getLineWidth() + 2f));
                    setGraphicsColor(gr, mattingPaint);
                    draw(gr, shp);
                }
            }

            setGraphicsForFill(gr);
            fill(gr, shp);
            setGraphicsForEdge(gr);
            draw(gr, shp);
        }
    }

    /**
     * Renders all the objects in the list a geometry's context, in their
     * 'selected' mode. This is the same as <code>paint()</code> for AWT
     * components. The geometries are rendered in the order of traverseMode. Any
     * geometries where <code>isVisible()</code> returns false are not rendered.
     * All of the geometries on the list are returned to their deselected state.
     * 
     * @param gr the AWT Graphics context
     */
    public void renderAllAsSelected(Graphics gr) {
        Shape shape = getShape();
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
     * Prepare the geometries for rendering. This must be done before calling
     * <code>render()</code>! This recursively calls generate() on the
     * OMGeometries on the list.
     * 
     * @param p a <code>Projection</code>
     * @param forceProjectAll if true, all the geometries on the list are
     *        generated with the new projection. If false they are only
     *        generated if getNeedToRegenerate() returns true
     * @see OMGeometry#generate
     * @see OMGeometry#regenerate
     */
    public boolean generate(Projection p, boolean forceProjectAll) {

        setNeedToRegenerate(true);

        GeneralPath projectedShape = null;

        synchronized (graphics) {

            if (traverseMode == FIRST_ADDED_ON_TOP) {
                ListIterator<OMGeometry> iterator = graphics.listIterator(size());
                while (iterator.hasPrevious()) {
                    projectedShape = updateShape(projectedShape, (OMGeometry) iterator.previous(), p, forceProjectAll);
                }
            } else {
                ListIterator<OMGeometry> iterator = graphics.listIterator();
                while (iterator.hasNext()) {
                    projectedShape = updateShape(projectedShape, (OMGeometry) iterator.next(), p, forceProjectAll);
                }
            }
        }

        setShape(projectedShape);
        setNeedToRegenerate(false);

        return projectedShape != null;
    }

    /**
     * Given a OMGeometry, it calls generate/regenerate on it, and then adds the
     * GeneralPath shape within it to the OMGeometryList shape object. Calls
     * setShape() with the new current shape, which is a synchronized method.
     * 
     * @param geometry the geometry to append
     * @param p the current projection
     * @param forceProject flag to force re-generation
     * @deprecated use the new paradigm from the other updateShape
     */
    protected void updateShape(OMGeometry geometry, Projection p, boolean forceProject) {

        if (geometry.isVisible()) {
            if (forceProject) {
                geometry.generate(p);
            } else {
                geometry.regenerate(p);
            }

            setShape(appendShapeEdge(getShape(), geometry.getShape(), connectParts));
        }
    }

    /**
     * Given an OMGeometry, check its visibility and if visible, generate it if
     * required and add the result to the provided current shape. Does not call
     * setShape().
     * 
     * @param currentShape the current shape
     * @param geometry the geometry to test
     * @param p the current projection
     * @param forceProject flag to force regeneration
     * @return the newly combined shape.
     */
    protected GeneralPath updateShape(GeneralPath currentShape, OMGeometry geometry, Projection p,
                                      boolean forceProject) {

        GeneralPath newShapePart = null;
        if (geometry != null && geometry.isVisible()) {
            if (forceProject) {
                geometry.generate(p);
            } else {
                geometry.regenerate(p);
            }

            newShapePart = geometry.getShape();
        }

        return appendShapeEdge(currentShape, newShapePart, connectParts);
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
     * Set whether the OMGeometries on the list should be connected to make a
     * one-part shape object (if true), or a multi-part shape object (if false).
     */
    public void setConnectParts(boolean value) {
        connectParts = value;
    }

    /**
     * Get whether the OMGeometries on the list should be connected to make a
     * one-part shape object (if true), or a multi-part shape object (if false).
     */
    public boolean getConnectParts() {
        return connectParts;
    }

    /**
     * Returns a iterator of a shallow copy of the current list, to avoid
     * concurrent modification exceptions if the list is being generated or
     * rendered while the list is being reviewed for other reasons.
     */
    public Iterator<OMGeometry> iteratorCopy() {
        return new OMGeometryList(graphics).iterator();
    }

    /**
     * Returns a iterator of a shallow copy of the current list, to avoid
     * concurrent modification exceptions if the list is being generated or
     * rendered while the list is being reviewed for other reasons.
     */
    public ListIterator<OMGeometry> listIteratorCopy() {
        return new OMGeometryList(graphics).listIterator();
    }

    /**
     * Returns a iterator of a shallow copy of the current list, to avoid
     * concurrent modification exceptions if the list is being generated or
     * rendered while the list is being reviewed for other reasons.
     */
    public ListIterator<OMGeometry> listIteratorCopy(int size) {
        return new OMGeometryList(graphics).listIterator(size);
    }

    @Override
    public OMList<OMGeometry> create() {
        return new OMGeometryList();
    }

    @Override
    protected com.bbn.openmap.omGraphics.OMList.OMDist<OMGeometry> createDist() {
        return new OMDist<OMGeometry>();
    }

    public void clear() {
        setNeedToRegenerate(true);
        super.clear();
    }

    public void add(int index, OMGeometry element) {
        setNeedToRegenerate(true);
        super.add(index, element);
    }

    public boolean addAll(Collection<? extends OMGeometry> c) {
        setNeedToRegenerate(true);
        synchronized (graphics) {
            return graphics.addAll(c);
        }
    }

    public boolean addAll(int index, Collection<? extends OMGeometry> c) {
        setNeedToRegenerate(true);
        synchronized (graphics) {
            return graphics.addAll(index, c);
        }
    }

    public boolean removeAll(Collection<?> c) {
        setNeedToRegenerate(true);
        return super.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        setNeedToRegenerate(true);
        return super.retainAll(c);
    }

    public OMGeometry get(int index) {
        synchronized (graphics) {
            return graphics.get(index);
        }
    }

}