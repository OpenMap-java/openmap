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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/geom/BasicGeometry.java,v $
// $RCSfile: BasicGeometry.java,v $
// $Revision: 1.19 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.omGraphics.geom;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * Base class implementation of OpenMap OMGeometry, the super class for all
 * OMGraphics.
 * <p>
 *
 * The geometry classes are intended to pull the object location data out of the
 * OMGraphics. If you have a bunch of OMGraphics that are all rendered with
 * common attributes, you can create a bunch of OMGeometry objects to plavce in
 * a OMGeometryList that will render them all alike.
 * <p>
 *
 * The BasicGeometry can hold attributes. Traditionally, there has been an
 * appObject (Application Object) that could be set in the OMGeometry/OMGraphic
 * to maintain a pointer for additional information about the shape. This has
 * been modified so that an attribute Map can be maintained for the
 * BasicGeometry to let it hold on to a bunch of organized attributes. To
 * maintain backward compatibility, the setAppObject() and getAppObject()
 * methods have been modified to manage a java.util.Map along with any Objects
 * stored in the appObject. Using the setAppObject() and getAppObject() methods
 * in conjunction with other attributes will cause that object to be stored in
 * the attribute Map under the APP_OBJECT_KEY Map key.
 *
 * @see PolygonGeometry
 * @see PolylineGeometry
 * @see com.bbn.openmap.omGraphics.OMGeometryList
 * @see Projection
 */
public abstract class BasicGeometry implements OMGeometry, Serializable, OMGraphicConstants {

    /**
     * The lineType describes the way a line will be drawn between points.
     * LINETYPE_STRAIGHT will mean the line is drawn straight between the pixels
     * of the endpoints of the line, across the window. LINETYPE_GREATCIRCLE
     * means the line will be drawn on the window representing the shortest line
     * along the land. LINETYPE_RHUMB means a line will be drawn along a
     * constant bearing between the two points.
     */
    protected int lineType = LINETYPE_UNKNOWN;

    /** Flag to indicate that the object needs to be re-projected. */
    protected transient boolean needToRegenerate = true;

    /**
     * Attribute Map for this Geometry/OMGraphic.
     */
    protected Map<Object, Object> attributes;

    /**
     * A flag to render this geometry visible.
     */
    protected boolean visible = true;

    /**
     * The Java 2D containing the Shape of the Graphic. There may be several
     * paths appended to each other, in case the graphic wraps around the earth,
     * and we need to show the other edge of the graphic on the other side of
     * the earth.
     */
    protected transient GeneralPath shape = null;

    // ////////////////////////////////////////////////////////

    /**
     * Set the line type for the graphic, which will affect how the lines will
     * be drawn. See the definition of the lineType parameter. Accepts
     * LINETYPE_RHUMB, LINETYPE_STRAIGHT and LINETYPE_GREATCIRCLE. Any weird
     * values get set to LINETYPE_STRAIGHT.
     *
     * @param value the line type of the graphic.
     */
    public void setLineType(int value) {
        if (lineType == value)
            return;
        setNeedToRegenerate(true); // flag dirty

        lineType = value;
    }

    /**
     * Return the line type.
     *
     * @return the linetype - LINETYPE_RHUMB, LINETYPE_STRAIGHT,
     *         LINETYPE_GREATCIRCLE or LINETYPE_UNKNOWN.
     */
    public int getLineType() {
        return lineType;
    }

    /**
     * Return the render type.
     *
     * @return the rendertype of the object - RENDERTYPE_LATLON, RENDERTYPE_XY,
     *         RENDERTYPE_OFFSET and RENDERTYPE_UNKNOWN.
     */
    public abstract int getRenderType();

    /**
     * Sets the regenerate flag for the graphic. This flag is used to determine
     * if extra work needs to be done to prepare the object for rendering. This
     * also sets the shape to null;
     *
     * @param value boolean
     */
    public void setNeedToRegenerate(boolean value) {
        needToRegenerate = value;
        if (value == true) {
            setShape(null);
        }
    }

    /**
     * Return the regeneration status.
     *
     * @return boolean
     */
    public boolean getNeedToRegenerate() {
        return needToRegenerate;
    }

    /**
     * Set the visibility variable. NOTE: <br>
     * This is checked by the OMGeometryList when it iterates through its list
     * for render and gesturing. It is not checked by the internal OMGeometry
     * methods, although maybe it should be...
     *
     * @param visible boolean
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Get the visibility variable.
     *
     * @return boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Let the geometry object know it's selected. No action mandated.
     */
    public void select() {
    }

    /**
     * Let the geometry object know it's deselected. No action mandated.
     */
    public void deselect() {
    }

    /**
     * Method to extend if you don't like Hashtables used for attribute table.
     */
    protected Map<Object, Object> createAttributeMap() {
        return Collections.synchronizedMap(new LinkedHashMap<Object, Object>());
    }

    /**
     * Adds a key-value pair to the attribute Map. The Map will be created if it
     * doesn't exist.
     */
    public void putAttribute(Object key, Object value) {
        if (key != null) {
            getAttributes().put(key, value);
        }
    }

    /**
     * Returns the object stored in a Map stored in the appObject. If the
     * appObject is a Map, the key will be passed to it even if the Map isn't
     * considered to be the 'official' attribute Map.
     */
    public Object getAttribute(Object key) {
        if (key != null) {
            return getAttributes().get(key);
        }
        return null;
    }

    /**
     * Removes the object stored in a Map stored in the appObject. If the
     * appObject is a Map, the key will be passed to it even if the Map isn't
     * considered to be the 'official' attribute Map. Returns the removed value
     * from the Map, or null if there wasn't a value for the given key.
     */
    public Object removeAttribute(Object key) {
        if (key != null) {
            return getAttributes().remove(key);
        }
        return null;
    }

    /**
     * Removes all of the objects stored in a Map stored in the appObject. If
     * the appObject is a Map, the clear command will be passed to it even if
     * the Map isn't considered to be the 'official' attribute Map.
     */
    public void clearAttributes() {
        getAttributes().clear();
    }

    /**
     * Returns the 'official' attribute Map.
     */
    public Map<Object, Object> getAttributes() {
        if (attributes == null) {
            attributes = createAttributeMap();
        }
        return attributes;
    }

    /**
     * Sets the 'official' attribute Map, moving any appObject that isn't
     * currently the 'official' attribute Map into the map under the
     * APP_OBJECT_KEY.
     */
    public void setAttributes(Map<Object, Object> atts) {
        attributes = atts;
    }

    /**
     * OMGeometry method for returning a simple description of the OMGraphic.
     */
    public String getDescription() {
        String cname = getClass().getName();
        int lastPeriod = cname.lastIndexOf('.');
        if (lastPeriod != -1) {
            cname = cname.substring(lastPeriod + 1);
        }
        return cname;
    }

    // ////////////////////////////////////////////////////////////////////////

    /**
     * Prepare the geometry for rendering. This must be done before calling
     * <code>render()</code>! If a vector graphic has lat-lon components, then
     * we project these vertices into x-y space. For raster graphics we prepare
     * in a different fashion.
     * <p>
     * If the generate is unsuccessful, it's usually because of some oversight,
     * (for instance if <code>proj</code> is null), and if debugging is enabled,
     * a message may be output to the controlling terminal.
     * <p>
     *
     * @param proj Projection
     * @return boolean true if successful, false if not.
     * @see #regenerate
     */
    public abstract boolean generate(Projection proj);

    /**
     * A check to see if the OMGeometry is ready for rendering. Calls getShape()
     * to pass to other isRenderable() method.
     */
    public boolean isRenderable() {
        return isRenderable(getShape());
    }

    /**
     * A check to see if the OMGeometry is ready for rendering. This is the
     * method you should call, with a the handle to the shape object you're
     * interested in rendering.
     *
     * @param shape the projected shape of the OMGraphic
     * @return true if draw/fill should be called.
     */
    public boolean isRenderable(Shape shape) {
        return (!getNeedToRegenerate() && isVisible() && shape != null);
    }

    /**
     * Paints the graphic, as a filled shape. The Graphics object should be set
     * for rendering. Calls the getShape() method, which is synchronized, before
     * calling the other fill method. It's better to call the other one after
     * doing renderable check, and then call draw, with a single handle to the
     * current shape object.
     * <P>
     *
     * This paints the graphic into the Graphics context. This is similar to
     * <code>paint()</code> function of java.awt.Components. Note that if the
     * graphic has not been generated or if it isn't visible, it will not be
     * rendered.
     * <P>
     *
     * This method used to be abstract, but with the conversion of OMGeometrys
     * to internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGeometry hasn't been updated to use Shape
     * objects, it should have its own render method.
     *
     * @param g Graphics2D context to render into.
     */
    public void fill(Graphics g) {
        fill(g, getShape());
    }

    /**
     * Paint the graphic, as a filled shape. The Graphics object should be set
     * for rendering.
     * <P>
     *
     * This paints the graphic into the Graphics context. This is similar to
     * <code>paint()</code> function of java.awt.Components. Note that if the
     * graphic has not been generated or if it isn't visible, it will not be
     * rendered.
     * <P>
     *
     * This method used to be abstract, but with the conversion of OMGeometrys
     * to internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGeometry hasn't been updated to use Shape
     * objects, it should have its own render method.
     *
     * @param g Graphics2D context to render into.
     * @param s Shape object to fill for rendering.
     */
    public void fill(Graphics g, Shape s) {
        if (s != null) {
            ((Graphics2D) g).fill(s);
        }
    }

    /**
     * Paint the graphic, as an outlined shape. The Graphics object should be
     * ready for rendering (paint, stroke).
     * <P>
     *
     * This paints the graphic into the Graphics context. This is similar to
     * <code>paint()</code> function of java.awt.Components. Note that if the
     * graphic has not been generated or if it isn't visible, it will not be
     * rendered.
     * <P>
     *
     * This method used to be abstract, but with the conversion of OMGeometrys
     * to internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGeometry hasn't been updated to use Shape
     * objects, it should have its own render method.
     *
     * @param g Graphics2D context to render into.
     * @param s Shape object to render.
     */
    public void draw(Graphics g, Shape s) {
        if (s != null) {
            ((Graphics2D) g).draw(s);
        }
    }

    /**
     * Paint the graphic, as an outlined shape. The Graphics object should be
     * ready for rendering (paint, stroke). Calls the getShape() method, which
     * is synchronized, before calling the other fill method. It's better to
     * call the other one after doing renderable check and fill, with a single
     * handle to the current shape object.
     * <P>
     *
     * This paints the graphic into the Graphics context. This is similar to
     * <code>paint()</code> function of java.awt.Components. Note that if the
     * graphic has not been generated or if it isn't visible, it will not be
     * rendered.
     * <P>
     *
     * This method used to be abstract, but with the conversion of OMGeometrys
     * to internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGeometry hasn't been updated to use Shape
     * objects, it should have its own render method.
     *
     * @param g Graphics2D context to render into.
     */
    public void draw(Graphics g) {
        draw(g, getShape());
    }

    /**
     * Sure it renders, but not with any controllable rendering attributes - the
     * colors and strokes are what are set in the Graphics. The OMGeometryList
     * controls this better, this method shouldn't really be called directly.
     */
    public void render(Graphics g) {
        Shape s = getShape();
        if (isRenderable(s)) {
            fill(g, s);
            draw(g, s);
        }
    }

    /**
     * Return the shortest distance from the edge of a graphic to an XY-point.
     * <p>
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point. Returns
     *         Float.POSITIVE_INFINITY if the graphic isn't ready (ungenerated).
     */
    public float distanceToEdge(double x, double y) {
        float distance = Float.POSITIVE_INFINITY;

        GeneralPath shape = getShape();
        if (!getNeedToRegenerate() && shape != null) {
            distance = BasicGeometry.distanceToEdge(x, y, shape);
        }

        return distance;
    }

    /**
     * Return the shortest distance from the edge of a Shape object to an
     * XY-point.
     * <p>
     * Method taken and adapted from
     * {@link BasicGeometry#distanceToEdge(double, double)}
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @param shape Shape object to test.
     * @return float distance, in pixels, from Shape to the point. Returns
     *         Float.POSITIVE_INFINITY if the Shape is null.
     */
    public static float distanceToEdge(final double x, final double y, final Shape shape) {
        float distance = Float.POSITIVE_INFINITY;

        if (shape == null) {
            return distance;
        }

        final PathIterator pi2 = shape.getPathIterator(null);
        final FlatteningPathIterator pathIt = new FlatteningPathIterator(pi2, .25);
        final double[] coords = new double[6];
        double endPntX = Double.NaN;
        double endPntY = Double.NaN;

        double lastMovedToPntX = Double.NaN;
        double lastMovedToPntY = Double.NaN;

        while (!pathIt.isDone()) {
            final int type = pathIt.currentSegment(coords);

            if (type == PathIterator.SEG_LINETO) {
                final double startPntX = endPntX;
                final double startPntY = endPntY;
                endPntX = coords[0];
                endPntY = coords[1];

                final float dist = (float) Line2D.ptSegDist(startPntX, startPntY, endPntX, endPntY, x, y);

                if (dist < distance) {
                    distance = dist;
                }
            } else if (type == PathIterator.SEG_MOVETO) {
                endPntX = coords[0];
                endPntY = coords[1];
                lastMovedToPntX = coords[0];
                lastMovedToPntY = coords[1];
            } else if (type == PathIterator.SEG_CLOSE) {
                final double startPntX = lastMovedToPntX;
                final double startPntY = lastMovedToPntY;
                endPntX = coords[0];
                endPntY = coords[1];

                final float dist = (float) Line2D.ptSegDist(startPntX, startPntY, endPntX, endPntY, x, y);

                if (dist < distance) {
                    distance = dist;
                }
            }

            pathIt.next();
        }

        return distance;
    }

    /**
     * Return the shortest distance from the graphic to an XY-point. Checks to
     * see of the point is contained within the OMGraphic, which may, or may not
     * be the right thing for clear OMGraphics or lines.
     * <p>
     *
     * This method used to be abstract, but with the conversion of OMGeometrys
     * to internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGeometry hasn't been updated to use Shape
     * objects, it should have its own distance method.
     * <p>
     *
     * Calls _distance(x, y);
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point. Returns
     *         Float.POSITIVE_INFINITY if the graphic isn't ready (ungenerated).
     */
    public float distance(double x, double y) {
        return _distance(x, y);
    }

    /**
     * Return the shortest distance from the graphic to an XY-point. Checks to
     * see of the point is contained within the OMGraphic, which may, or may not
     * be the right thing for clear OMGraphics or lines.
     * <p>
     *
     * _distance was added so subclasses could make this call if their
     * geometries/attributes require this action (when fill color doesn't
     * matter).
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point. Returns
     *         Float.POSITIVE_INFINITY if the graphic isn't ready (ungenerated).
     */
    protected float _distance(double x, double y) {
        float distance = Float.POSITIVE_INFINITY;

        GeneralPath shape = getShape();

        if (getNeedToRegenerate() || shape == null) {
            return distance;
        }

        if (shape.contains(x, y)) {
            // if (Debug.debugging("omgraphicdetail")) {
            // Debug.output(" contains " + x + ", " + y);
            // }
            return 0f;
        } else {
            return distanceToEdge(x, y);
        }
    }

    /**
     * Answers the question whether or not the OMGeometry contains the given
     * pixel point.
     * <P>
     * This method used to be abstract, but with the conversion of OMGeometrys
     * to internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGeometry hasn't been updated to use Shape
     * objects, it should have its own contains method.
     * <P>
     * This method duplicates a java.awt.Shape method, with some protection
     * wrapped around it. If you have other queries for the internal Shape
     * object, just ask for it and then ask it directly. This method is provided
     * because it is the most useful, used when determining if a mouse event is
     * occurring over an object on the map.
     *
     * @param x X pixel coordinate of the point.
     * @param y Y pixel coordinate of the point.
     * @return getShape().contains(x, y), false if the OMGraphic hasn't been
     *         generated yet.
     */
    public boolean contains(double x, double y) {
        Shape shape = getShape();
        boolean ret = false;

        if (shape != null) {
            ret = shape.contains(x, y);
        }

        return ret;
    }

    /**
     * Invoke this to regenerate a "dirty" graphic. This method is a wrapper
     * around the <code>generate()</code> method. It invokes
     * <code>generate()</code> only if</code> needToRegenerate() </code> on the
     * graphic returns true. To force a graphic to be generated, call
     * <code>generate()</code> directly.
     *
     * @param proj the Projection
     * @return true if generated, false if didn't do it (maybe a problem).
     * @see #generate
     */
    public boolean regenerate(Projection proj) {
        if (proj == null) {
            return false;
        }

        if (getNeedToRegenerate()) {
            return generate(proj);
        }

        return false;
    }

    /**
     * Get the java.awt.Shape object that represents the projected graphic. The
     * array will one Shape object even if the object wraps around the earth and
     * needs to show up in more than one place on the map. In conditions like
     * that, the Shape will have multiple parts.
     * <p>
     *
     * The java.awt.Shape object gives you the ability to do a little spatial
     * analysis on the graphics.
     *
     * @return java.awt.geom.GeneralPath (a java.awt.Shape object), or null if
     *         the graphic needs to be generated with the current map
     *         projection, or null if the OMGeometry hasn't been updated to use
     *         Shape objects for its internal representation.
     */
    public synchronized GeneralPath getShape() {
        return shape;
    }

    /**
     * Set the java.awt.Shape object that represents the projected graphic. This
     * Shape object should be internally generated, but this method is provided
     * to clear out the object to save memory, or to allow a little
     * customization if your requirements dictate.
     * <p>
     *
     * The java.awt.Shape object gives you the ability to do a little spatial
     * analysis on the graphics.
     *
     * @param gp java.awt.geom.GeneralPath, or null if the graphic needs to be
     *        generated with the current map projection or to clear out the
     *        object being held by the OMGeometry.
     */
    public synchronized void setShape(GeneralPath gp) {
        shape = gp;
    }

    /**
     * Create a Shape object given an array of x points and y points. The x
     * points a y points should be projected. This method is used by subclasses
     * that get projected coordinates out of the projection classes, and they
     * need to build a Shape object from those coordinates.
     *
     * @param xpoints projected x coordinates
     * @param ypoints projected y coordinates
     * @param isPolygon whether the points make up a polygon, or a polyline. If
     *        it's true, the Shape object returned is a Polygon. If false, the
     *        Shape returned is a GeneralPath object.
     * @return The Shape object for the points.
     */
    public static GeneralPath createShape(float[] xpoints, float[] ypoints, boolean isPolygon) {
        return createShape(xpoints, ypoints, 0, xpoints.length, isPolygon);
    }

    /**
     * Create a Shape object given an array of x points and y points. The x
     * points a y points should be projected. This method is used by subclasses
     * that get projected coordinates out of the projection classes, and they
     * need to build a Shape object from those coordinates.
     *
     * @param xpoints projected x coordinates
     * @param ypoints projected y coordinates
     * @param startIndex the starting coordinate index in the array.
     * @param length the number of points to use from the array for the shape.
     * @param isPolygon whether the points make up a polygon, or a polyline. If
     *        it's true, the Shape object returned is a Polygon. If false, the
     *        Shape returned is a GeneralPath object.
     * @return The Shape object for the points.
     */
    public static GeneralPath createShape(float[] xpoints, float[] ypoints, int startIndex,
                                          int length, boolean isPolygon) {
        // used to return a Shape

        if (xpoints == null || ypoints == null) {
            return null;
        }

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (length > xpoints.length - startIndex) {
            // Do as much as you can...
            length = xpoints.length - startIndex - 1;
        }

        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, length);

        if (length > startIndex) {
            path.moveTo(xpoints[startIndex], ypoints[startIndex]);
            for (int j = startIndex + 1; j < length; j++) {
                path.lineTo(xpoints[j], ypoints[j]);
            }

            if (isPolygon) {
                path.closePath();
            }
        }

        return path;
    }

    /**
     * Utility method that iterates over a Shape object and prints out the
     * points.
     */
    public static void describeShapeDetail(Shape shape) {
        describeShapeDetail(shape, .25);
    }

    /**
     * Utility method that iterates over a Shape object and prints out the
     * points. The flattening is used for a FlatteningPathIterator, controlling
     * the scope of the path traversal.
     */
    public static void describeShapeDetail(Shape shape, double flattening) {
        PathIterator pi2 = shape.getPathIterator(null);
        FlatteningPathIterator pi = new FlatteningPathIterator(pi2, flattening);
        double[] coords = new double[6];
        int pointCount = 0;

        Debug.output(" -- start describeShapeDetail with flattening[" + flattening + "]");
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            Debug.output(" Shape point [" + type + "] (" + (pointCount++) + ") " + coords[0] + ", "
                    + coords[1]);
            pi.next();
        }

        Debug.output(" -- end (" + pointCount + ")");
    }

    /**
     * Convenience method to add the coordinates to the given GeneralPath. You
     * need to close the path yourself if you want it to be a polygon.
     *
     * @param toShape the GeneralPath Shape object to add the coordinates to.
     * @param xpoints horizontal pixel coordinates.
     * @param ypoints vertical pixel coordinates.
     * @return toShape, with coordinates appended.
     */
    public static GeneralPath appendShapeEdge(GeneralPath toShape, float[] xpoints, float[] ypoints) {
        return appendShapeEdge(toShape, xpoints, ypoints, 0, xpoints.length);
    }

    /**
     * Convenience method to add the coordinates to the given GeneralPath. You
     * need to close the path yourself if you want it to be a polygon.
     *
     * @param toShape the GeneralPath Shape object to add the coordinates to.
     * @param xpoints horizontal pixel coordinates.
     * @param ypoints vertical pixel coordinates.
     * @param startIndex the index into pixel coordinate array to start reading
     *        from.
     * @param length the number of coordinates to add.
     * @return toShape, with coordinates appended.
     */
    public static GeneralPath appendShapeEdge(GeneralPath toShape, float[] xpoints,
                                              float[] ypoints, int startIndex, int length) {
        return appendShapeEdge(toShape, createShape(xpoints, ypoints, startIndex, length, false));
    }

    /**
     * Convenience method to append the edge of a GeneralPath Shape to another
     * GeneralPath Shape. A PathIterator is used to figure out the points to use
     * to add to the toShape. You need to close the path yourself if you want it
     * to be a polygon. Assumes that the two paths should be connected.
     *
     * @param toShape the GeneralPath Shape object to add the edge to.
     * @param addShape the GeneralPath Shape to add to the toShape.
     * @return toShape, with coordinates appended. Returns addShape if toShape
     *         was null.
     */
    public static GeneralPath appendShapeEdge(GeneralPath toShape, GeneralPath addShape) {
        return appendShapeEdge(toShape, addShape, true);
    }

    /**
     * Convenience method to append the edge of a GeneralPath Shape to another
     * GeneralPath Shape. A PathIterator is used to figure out the points to use
     * to add to the toShape. You need to close the path yourself if you want it
     * to be a polygon.
     *
     * @param toShape the GeneralPath Shape object to add the edge to.
     * @param addShape the GeneralPath Shape to add to the toShape.
     * @param lineTo specify whether the first point of the appended path is
     *        connected to the original path. True to connect.
     * @return toShape, with coordinates appended. Returns addShape if toShape
     *         was null.
     */
    public static GeneralPath appendShapeEdge(GeneralPath toShape, GeneralPath addShape,
                                              boolean lineTo) {

        boolean DEBUG = Debug.debugging("arealist");
        int pointCount = 0;

        // If both null, return null.
        if (addShape == null) {
            return toShape;
        }

        if (toShape == null) {
            return addShape;
        }

        toShape.append(addShape, lineTo);
        return toShape;

        /*
         * 
         * PathIterator pi2 = addShape.getPathIterator(null);
         * FlatteningPathIterator pi = new FlatteningPathIterator(pi2, .25);
         * double[] coords = new double[6];
         * 
         * while (!pi.isDone()) { int type = pi.currentSegment(coords); if
         * (lineTo) { if (DEBUG) { Debug.output(" adding point [" + type + "] ("
         * + (pointCount++) + ") " + (float) coords[0] + ", " + (float)
         * coords[1]); } toShape.lineTo((float) coords[0], (float) coords[1]);
         * 
         * } else { if (DEBUG) { Debug.output("Creating new shape, first point "
         * + (float) coords[0] + ", " + (float) coords[1]); }
         * toShape.moveTo((float) coords[0], (float) coords[1]); lineTo = true;
         * } pi.next(); }
         * 
         * if (DEBUG) { Debug.output(" -- end point (" + pointCount + ")"); }
         * 
         * return toShape;
         */
    }

    /**
     * Create a general path from a point plus a height and width;
     */
    public static GeneralPath createBoxShape(float x, float y, int width, int height) {
        float[] xs = new float[4];
        float[] ys = new float[4];

        xs[0] = x;
        ys[0] = y;
        xs[1] = x + width;
        ys[1] = y;
        xs[2] = x + width;
        ys[2] = y + height;
        xs[3] = x;
        ys[3] = y + height;

        return createShape(xs, ys, true);
    }

    public void restore(OMGeometry source) {
        this.lineType = source.getLineType();
        this.visible = source.isVisible();
        this.attributes = source.getAttributes();
        this.needToRegenerate = true;
    }
}
