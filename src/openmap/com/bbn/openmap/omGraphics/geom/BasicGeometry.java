// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.7 $
// $Date: 2003/08/28 22:09:16 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.geom;

import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;

import java.awt.*;
import java.awt.geom.*;
import java.io.Serializable;

/**
 * Base class implementation of OpenMap OMGeometry. <p>
 *
 * The geometry classes are intended to pull the object location data
 * out of the OMGraphics.  If you have a bunch of OMGraphics that are
 * all rendered with common attributes, you can create a bunch of
 * OMGeometry objects to plavce in a OMGeometryList that will render
 * them all alike.
 *
 * @see PolygonGeometry
 * @see PolylineGeometry
 * @see com.bbn.openmap.omGraphics.OMGeometryList
 * @see Projection 
 */
public abstract class BasicGeometry
    implements OMGeometry, Serializable, OMGraphicConstants {

    /** 
     * The lineType describes the way a line will be drawn between
     * points.  LINETYPE_STRAIGHT will mean the line is drawn straight
     * between the pixels of the endpoints of the line, across the
     * window.  LINETYPE_GREATCIRCLE means the line will be drawn on the
     * window representing the shortest line along the
     * land. LINETYPE_RHUMB means a line will be drawn along a constant
     * bearing between the two points.
     */
    protected int lineType = LINETYPE_UNKNOWN;
    
    /** Flag to indicate that the object needs to be reprojected. */
    protected boolean needToRegenerate = true;

    /**
     * Space for an application to associate geometry with an
     * application object.  This object can contain attribute
     * information about the geometry.
     *
     * @see #setAppObject
     * @see #getAppObject 
     */
    protected Object appObject;

    /**
     * A flag to render this geometry visible.
     */
    protected boolean visible = true;

    /**
     * The Java 2D containing the Shape of the Graphic.  There may be
     * an array of them, in case the graphic wraps around the earth,
     * and we need to show the other edge of the graphic on the other
     * side of the earth.  
     */
    protected transient GeneralPath shape = null;

  //////////////////////////////////////////////////////////  

    /** 
     * Set the line type for the graphic, which will affect how the
     * lines will be drawn.  See the definition of the lineType
     * parameter. Accepts LINETYPE_RHUMB, LINETYPE_STRAIGHT and
     * LINETYPE_GREATCIRCLE.  Any weird values get set to
     * LINETYPE_STRAIGHT.  
     *
     * @param value the line type of the graphic.
     * */
    public void setLineType(int value) {
	if (lineType == value) return;
	setNeedToRegenerate(true); // flag dirty
	
	lineType = value;
    }

    /**
     * Return the line type. 
     *
     * @return the linetype - LINETYPE_RHUMB, LINETYPE_STRAIGHT,
     * LINETYPE_GREATCIRCLE or LINETYPE_UNKNOWN.
     */
    public int getLineType() { 
        return lineType;
    }
    
    /**
     * Return the render type. 
     *
     * @return the rendertype of the object - RENDERTYPE_LATLON,
     * RENDERTYPE_XY, RENDERTYPE_OFFSET and RENDERTYPE_UNKNOWN.
     */
    public abstract int getRenderType();
    
    /**
     * Sets the regenerate flag for the graphic.
     * This flag is used to determine if extra work needs to be done
     * to prepare the object for rendering.
     *
     * @param value boolean
     */
    public void setNeedToRegenerate(boolean value) { 
        needToRegenerate = value;
	if (value == true) {
	    shape = null;
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
     * Set the visibility variable.
     * NOTE:<br>
     * This is checked by the OMGeometryList when it iterates through its list
     * for render and gesturing.  It is not checked by the internal OMGeometry
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
     * Let the geometry object know it's selected.  No action mandated.
     */
    public void select() {}

    /**
     * Let the geometry object know it's deselected.  No action mandated.
     */
    public void deselect() {}

    /**
     * Holds an application specific object for later access.
     * This can be used to associate an application object with
     * an OMGeometry for later retrieval.  For instance, when
     * the graphic is clicked on, the application gets the OMGeometry
     * object back from the OMGeometryList, and can then get back
     * to the application level object through this pointer.
     *
     * @param obj Object
     */
    public synchronized void setAppObject(Object obj) {
	appObject = obj;
    }

    /**
     * Gets the application's object pointer.
     *
     * @return Object
     */
    public synchronized Object getAppObject() {
	return appObject;
    }

//////////////////////////////////////////////////////////////////////////

    /**
     * Prepare the geometry for rendering.
     * This must be done before calling <code>render()</code>!  If a
     * vector graphic has lat-lon components, then we project these
     * vertices into x-y space.  For raster graphics we prepare in a
     * different fashion. <p>
     * If the generate is unsuccessful, it's usually because of some
     * oversight, (for instance if <code>proj</code> is null), and if
     * debugging is enabled, a message may be output to the controlling
     * terminal. <p>
     *
     * @param proj Projection
     * @return boolean true if successful, false if not.
     * @see #regenerate
     */
    public abstract boolean generate(Projection proj);

    /**
     *
     */
    public boolean isRenderable() {
	return (!getNeedToRegenerate() &&
		isVisible() &&	shape != null);
    }

    /**
     * Paint the graphic, as a filled shape. <P>
     *
     * This paints the graphic into the Graphics context.  This is
     * similar to <code>paint()</code> function of
     * java.awt.Components.  Note that if the graphic has not been
     * generated or if it isn't visible, it will not be
     * rendered. <P>
     *
     * This method used to be abstract, but with the conversion of
     * OMGeometrys to internally represent themselves as java.awt.Shape
     * objects, it's a more generic method.  If the OMGeometry hasn't
     * been updated to use Shape objects, it should have its own
     * render method.
     *
     * @param g Graphics2D context to render into.  
     */
    public void fill(Graphics g) {
	if (isRenderable()) {
	    ((Graphics2D)g).fill(shape);
	}
    }
	    
    /**
     * Paint the graphic, as an outlined shape. <P>
     *
     * This paints the graphic into the Graphics context.  This is
     * similar to <code>paint()</code> function of
     * java.awt.Components.  Note that if the graphic has not been
     * generated or if it isn't visible, it will not be
     * rendered. <P>
     *
     * This method used to be abstract, but with the conversion of
     * OMGeometrys to internally represent themselves as java.awt.Shape
     * objects, it's a more generic method.  If the OMGeometry hasn't
     * been updated to use Shape objects, it should have its own
     * render method.
     *
     * @param g Graphics2D context to render into.  
     */
    public void draw(Graphics g) {
	if (isRenderable()) {
	    ((Graphics2D)g).draw(shape);
	}
    }

    /**
     * Return the shortest distance from the edge of a graphic to an
     * XY-point. <p>
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point.
     * Returns Float.POSITIVE_INFINITY if the graphic isn't ready
     * (ungenerated).
     */
    public float distanceToEdge(int x, int y) {
	float temp, distance = Float.POSITIVE_INFINITY;

	if (getNeedToRegenerate() || shape == null) {
	    return distance;
	}

	PathIterator pi2 = shape.getPathIterator(null);
	FlatteningPathIterator pi = new FlatteningPathIterator(pi2, .25);
	double[] coords = new double[6];
	int count = 0;
	double startPntX = 0;
	double startPntY = 0;
	double endPntX = 0;
	double endPntY = 0;

	while (!pi.isDone()) {
	    int type = pi.currentSegment(coords);
	    float dist;

	    if (type == PathIterator.SEG_LINETO) {
		startPntX = endPntX;
		startPntY = endPntY;
		endPntX = coords[0];
		endPntY = coords[1];

		dist = (float) Line2D.ptSegDist(startPntX, startPntY, endPntX, endPntY, (double)x, (double)y);

		if (dist < distance) {
		    distance = dist;
		}

		if (Debug.debugging("omgraphicdetail")) {
		    Debug.output("Type: " + type + "(" + 
				 (count++) + "), " + 
				 startPntX + ", " + startPntY + ", " +
				 endPntX + ", " + endPntY + ", " + 
				 x + ", " + y + 
				 ", distance: " + distance);
		}
		    
	    } else {

		// This should be the first and last
		// condition, SEG_MOVETO and SEG_CLOSE
		startPntX = coords[0];
		startPntY = coords[1];
		endPntX = coords[0];
		endPntY = coords[1];
	    }

	    pi.next();
	}

	return distance;
    }

    /**
     * Return the shortest distance from the graphic to an
     * XY-point. Checks to see of the point is contained within the
     * OMGraphic, which may, or may not be the right thing for clear
     * OMGraphics or lines.<p>
     *
     * This method used to be abstract, but with the conversion of
     * OMGeometrys to internally represent themselves as java.awt.Shape
     * objects, it's a more generic method.  If the OMGeometry hasn't
     * been updated to use Shape objects, it should have its own
     * distance method.<p>
     *
     * Calls _distance(x, y);
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point.
     * Returns Float.POSITIVE_INFINITY if the graphic isn't ready
     * (ungenerated).
     */
    public float distance(int x, int y) {
	return _distance(x, y);
    }

    /**
     * Return the shortest distance from the graphic to an
     * XY-point. Checks to see of the point is contained within the
     * OMGraphic, which may, or may not be the right thing for clear
     * OMGraphics or lines.<p>
     *
     * _distance was added so subclasses could make this call if their
     * geometries/attributes require this action (when fill color
     * doesn't matter).
     *
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point.
     * Returns Float.POSITIVE_INFINITY if the graphic isn't ready
     * (ungenerated).
     */
    protected float _distance(int x, int y) {
	float temp, distance = Float.POSITIVE_INFINITY;

	if (getNeedToRegenerate() || shape == null) {
	    return distance;
	}

	if (shape.contains((double)x, (double)y)) {
// 	    if (Debug.debugging("omgraphicdetail")) {
// 		Debug.output(" contains " + x + ", " + y);
// 	    }
	    return 0f;
	} else {
	    return distanceToEdge(x, y);
	}
    }

    /** 
     * Answsers the question whether or not the OMGeometry contains
     * the given pixel point.
     * <P>
     * This method used to be abstract, but with the conversion of
     * OMGeometrys to internally represent themselves as java.awt.Shape
     * objects, it's a more generic method.  If the OMGeometry hasn't
     * been updated to use Shape objects, it should have its own
     * contains method.
     * <P>
     * This method duplicates a java.awt.Shape method, with some
     * protection wrapped around it.  If you have other queries for
     * the internal Shape object, just ask for it and then ask it
     * directly.  This method is provided because it is the most
     * useful, used when determining if a mouse event is occuring over
     * an object on the map.
     *
     * @param x X pixel coordinate of the point.
     * @param y Y pixel coordinate of the point.
     * @return getShape().contains(x, y), false if the OMGraphic
     * hasn't been generated yet.  
     */
    public boolean contains(int x, int y) {
	Shape shape = getShape();
	boolean ret = false;

	if (shape != null) {
	    ret = shape.contains((double)x, (double)y);
	}

	return ret;
    }

    /**
     * Invoke this to regenerate a "dirty" graphic.
     * This method is a wrapper around the <code>generate()</code>
     * method.  It invokes <code>generate()</code> only if
     * </code>needToRegenerate()</code> on the graphic returns true.
     * To force a graphic to be generated, call
     * <code>generate()</code> directly.
     *
     * @param proj the Projection
     * @return true if generated, false if didn't do it (maybe a
     * problem).
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
     * Get the java.awt.Shape object that represents the projected
     * graphic.  The array will one Shape object even if the object
     * wraps around the earth and needs to show up in more than one
     * place on the map.  In conditions like that, the Shape will have
     * multiple parts.<p>
     *
     * The java.awt.Shape object gives you the ability to do a little
     * spatial analysis on the graphics.
     *
     * @return java.awt.geom.GeneralPath (a java.awt.Shape object), or
     * null if the graphic needs to be generated with the current map
     * projection, or null if the OMGeometry hasn't been updated to
     * use Shape objects for its internal representation.
     */
    public GeneralPath getShape() {
	return shape;
    }

    /**
     * Set the java.awt.Shape object that represents the projected
     * graphic.  This Shape object should be internally generated, but
     * this method is provided to clear out the object to save memory,
     * or to allow a little customization if your requirements
     * dictate.<p>
     *
     * The java.awt.Shape object gives you the ability to do a little
     * spatial analysis on the graphics.
     *
     * @param gp java.awt.geom.GeneralPath, or null if the graphic
     * needs to be generated with the current map projection or to
     * clear out the object being held by the OMGeometry.
     */
    public void setShape(GeneralPath gp) {
	shape = gp;
    }

    /**
     * Create a Shape object given an array of x points and y points.
     * The x points a y points should be projected.  This method is
     * used by subclasses that get projected coordinates out of the
     * projection classes, and they need to build a Shape object from
     * those coordinates.
     * @param xpoints projected x coordinates
     * @param ypoints projected y coordinates
     * @param isPolygon whether the points make up a polygon, or a
     * polyline.  If it's true, the Shape object returned is a
     * Polygon.  If false, the Shape returned is a GeneralPath object.
     * @return The Shape object for the points.  
     */
    public static GeneralPath createShape(int xpoints[], int ypoints[], boolean isPolygon) {
	return createShape(xpoints, ypoints, 0, xpoints.length, isPolygon);
    }

    /**
     * Create a Shape object given an array of x points and y points.
     * The x points a y points should be projected.  This method is
     * used by subclasses that get projected coordinates out of the
     * projection classes, and they need to build a Shape object from
     * those coordinates.
     * @param xpoints projected x coordinates
     * @param ypoints projected y coordinates
     * @param startIndex the starting coordinate index in the array.
     * @param length the number of points to use from the array for the shape.
     * @param isPolygon whether the points make up a polygon, or a
     * polyline.  If it's true, the Shape object returned is a
     * Polygon.  If false, the Shape returned is a GeneralPath object.
     * @return The Shape object for the points.  
     */
    public static GeneralPath createShape(int xpoints[], int ypoints[], 
					  int startIndex, int length, 
					  boolean isPolygon) { 
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
     * Utility method that iterates over a Shape object and prints out the points.
     */
    public static void describeShapeDetail(Shape shape) {
	describeShapeDetail(shape, .25);
    }

    /**
     * Utility method that iterates over a Shape object and prints out
     * the points.  The flattening is used for a
     * FlatteningPathIterator, controlling the scope of the path
     * traversal.
     */
    public static void describeShapeDetail(Shape shape, double flattening) {
	PathIterator pi2 = shape.getPathIterator(null);
	FlatteningPathIterator pi = new FlatteningPathIterator(pi2, flattening);
	double[] coords = new double[6];
	int pointCount = 0;

	Debug.output(" -- start describeShapeDetail with flattening[" + flattening + "]");
	while (!pi.isDone()) {
	    int type = pi.currentSegment(coords);
	    Debug.output(" Shape point [" + type + "] (" + (pointCount++) + ") " +  
			 coords[0] + ", " + coords[1]);
	    pi.next();
	}

	Debug.output(" -- end (" + pointCount + ")");
    }

    /**
     * Convenience method to add the coordinates to the given
     * GeneralPath.  You need to close the path yourself if you want
     * it to be a polygon.
     * @param toShape the GeneralPath Shape object to add the coordinates to.
     * @param xpoints horizontal pixel coordiantes.
     * @param ypoints vertical pixel coordiantes.
     * @return toShape, with coordinates appended.
     */
    public static GeneralPath appendShapeEdge(GeneralPath toShape, 
					      int xpoints[], int ypoints[]) { 
	return appendShapeEdge(toShape, xpoints, ypoints, 0, xpoints.length);
    }

    /**
     * Convenience method to add the coordinates to the given
     * GeneralPath.  You need to close the path yourself if you want
     * it to be a polygon.
     * @param toShape the GeneralPath Shape object to add the coordinates to.
     * @param xpoints horizontal pixel coordiantes.
     * @param ypoints vertical pixel coordiantes.
     * @param startIndex the index into pixel coordinate array to start reading from.
     * @param length the number of coordinates to add.
     * @return toShape, with coordinates appended.
     */
    public static GeneralPath appendShapeEdge(GeneralPath toShape, 
					      int xpoints[], int ypoints[], 
					      int startIndex, int length) { 
	return appendShapeEdge(toShape, createShape(xpoints, ypoints, startIndex, length, false));
    }

    /**
     * Convenience method to append the edge of a GeneralPath Shape to
     * another GeneralPath Shape.  A PathIterator is used to figure
     * out the points to use to add to the toShape.  You need to close
     * the path yourself if you want it to be a polygon.
     * @param toShape the GeneralPath Shape object to add the edge to.
     * @param addShape the GeneralPath Shape to add to the toShape.
     * @return toShape, with coordinates appended.  Returns addShape
     * if toShape was null.
     */
    public static GeneralPath appendShapeEdge(GeneralPath toShape, 
					      GeneralPath addShape) {

	boolean DEBUG = Debug.debugging("arealist");
	int pointCount = 0;
	boolean firstPoint = false;

	// If both null, return null.
	if (addShape == null) {
	    return toShape;
	}

	if (toShape == null) {
	    return addShape;
	}

	PathIterator pi2 = addShape.getPathIterator(null);
	FlatteningPathIterator pi = new FlatteningPathIterator(pi2, .25);
	double[] coords = new double[6];

	while (!pi.isDone()) {
	    int type = pi.currentSegment(coords);
	    if (firstPoint) {
		if (DEBUG) {
		    Debug.output("Creating new shape, first point " +
				 (float)coords[0] + ", " + (float)coords[1]);
		}
		toShape.moveTo((float)coords[0], (float)coords[1]);
		firstPoint = false;
	    } else {
		if (DEBUG) {
		    Debug.output(" adding point [" + type + "] (" + (pointCount++) + ") " +  
				 (float)coords[0] + ", " + (float)coords[1]);
		}
		toShape.lineTo((float)coords[0], (float)coords[1]);
	    }
	    pi.next();
	}

	if (DEBUG) {
	    Debug.output(" -- end point (" + pointCount + ")");
	}

	return toShape;
    }


    /**
     * Create a general path from a point plus a height and width;
     */
    public static GeneralPath createBoxShape(int x, int y, int width, int height) {
	int[] xs = new int[4];
	int[] ys = new int[4];

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
}
