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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMGeometry.java,v $
// $RCSfile: OMGeometry.java,v $
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.Map;

import com.bbn.openmap.proj.Projection;

/**
 * Base class of OpenMap OMGraphics geometry.
 * <p>
 * 
 * The geometry classes are intended to pull the object location data out of the
 * OMGraphics. If you have a bunch of OMGraphics that are all rendered with
 * common attributes, you can create a bunch of OMGeometry objects to plavce in
 * a OMGeometryList that will render them all alike.
 * 
 * @see OMGeometryList
 * @see Projection
 */
public interface OMGeometry {

    /**
     * Set the line type for the graphic, which will affect how the lines will
     * be drawn. See the definition of the lineType parameter. Accepts
     * LINETYPE_RHUMB, LINETYPE_STRAIGHT and LINETYPE_GREATCIRCLE. Any weird
     * values get set to LINETYPE_STRAIGHT.
     * 
     * @param value the line type of the graphic.
     */
    public void setLineType(int value);

    /**
     * Return the line type.
     * 
     * @return the linetype - LINETYPE_RHUMB, LINETYPE_STRAIGHT,
     *         LINETYPE_GREATCIRCLE or LINETYPE_UNKNOWN.
     */
    public int getLineType();

    /**
     * Return the render type.
     * 
     * @return the rendertype of the object - RENDERTYPE_LATLON, RENDERTYPE_XY,
     *         RENDERTYPE_OFFSET and RENDERTYPE_UNKNOWN.
     */
    public int getRenderType();

    /**
     * Sets the regenerate flag for the graphic. This flag is used to determine
     * if extra work needs to be done to prepare the object for rendering.
     * 
     * @param value boolean
     */
    public void setNeedToRegenerate(boolean value);

    /**
     * Return the regeneration status.
     * 
     * @return boolean
     */
    public boolean getNeedToRegenerate();

    /**
     * Set the visibility variable. NOTE: <br>
     * This is checked by the OMGeometryList when it iterates through its list
     * for render and gesturing. It is not checked by the internal OMGeometry
     * methods, although maybe it should be...
     * 
     * @param visible boolean
     */
    public void setVisible(boolean visible);

    /**
     * Get the visibility variable.
     * 
     * @return boolean
     */
    public boolean isVisible();

    /**
     * Let the geometry object know that it is selected. No action mandated.
     */
    public void select();

    /**
     * Let the geometry object know that it is not selected. No action mandated.
     */
    public void deselect();

    /**
     * Holds an application specific object for later access. This can be used
     * to associate an application object with an OMGeometry for later
     * retrieval. For instance, when the graphic is clicked on, the application
     * gets the OMGeometry object back from the OMGeometryList, and can then get
     * back to the application level object through this pointer.
     * 
     * @param obj Object
     */
    public void setAppObject(Object obj);

    /**
     * Gets the application's object pointer.
     * 
     * @return Object
     */
    public Object getAppObject();

    /**
     * Set an attribute in an OMGeometry.
     */
    public void putAttribute(Object key, Object value);

    /**
     * Get an attribute from an OMGeometry.
     */
    public Object getAttribute(Object key);

    /**
     * Remove an attribute from the OMGeometry.
     */
    public Object removeAttribute(Object key);

    /**
     * Clear attributes from the OMGeometry.
     */
    public void clearAttributes();

    /**
     * Set all attributes on the OMGeometry.
     */
    public void setAttributes(Map<Object, Object> attributes);

    /**
     * Get all attributes from the OMGeometry.
     */
    public Map<Object, Object> getAttributes();

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
    public boolean generate(Projection proj);

    /**
     * Self-discovery of internal Shape object used for check, or implemented if
     * a Shape object doesn't matter.
     * 
     * @return true if the OMGeometry should be rendered - it's visible and the
     *         projected shape is created and reflects the current location
     *         settings of the geometry.
     */
    public boolean isRenderable();

    /**
     * Shape object provided for Shape-readiness check.
     * 
     * @param s Shape to check if ready for rendering
     * @return true if the OMGeometry should be rendered - it's visible and the
     *         projected shape is created and reflects the current location
     *         settings of the geometry.
     */
    public boolean isRenderable(Shape s);

    /**
     * Paint the graphic. This paints the graphic into the Graphics context.
     * This is similar to <code>paint()</code> function of java.awt.Components.
     * Note that if the graphic has not been generated, it should not be
     * rendered.
     * <P>
     * 
     * It's expected that this method will call fill and draw, respectively.
     * 
     * @param g Graphics2D context to render into.
     */
    public void render(Graphics g);

    /**
     * Paint the graphic, as a filled shape. This method has been broken out of
     * render as a way to allow OMGeometries to fine-tune their rendering
     * process.
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
     * @param s Shape object to use for fill.
     */
    public void fill(Graphics g, Shape s);

    /**
     * Paint the graphic. This method has been broken out of render as a way to
     * allow OMGeometries to fine-tune their rendering process.
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
    public void fill(Graphics g);

    /**
     * Paint the graphic, as an outlined shape. This method has been broken out
     * of render as a way to allow OMGeometries to fine-tune their rendering
     * process.
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
     * @param s Shape object to use for drawing.
     */
    public void draw(Graphics g, Shape s);

    /**
     * Paint the graphic. This method has been broken out of render as a way to
     * allow OMGeometries to fine-tune their rendering process.
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
    public void draw(Graphics g);

    /**
     * Return the shortest distance from the graphic to an XY-point.
     * <p>
     * 
     * This method used to be abstract, but with the conversion of OMGeometrys
     * to internally represent themselves as java.awt.Shape objects, it's a more
     * generic method. If the OMGeometry hasn't been updated to use Shape
     * objects, it should have its own distance method.
     * 
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point. Returns
     *         Float.POSITIVE_INFINITY if the graphic isn't ready (ungenerated).
     */
    public float distance(double x, double y);

    /**
     * Return the shortest distance from the edge of a geometry to an XY-point.
     * <p>
     * 
     * @param x X coordinate of the point.
     * @param y Y coordinate of the point.
     * @return float distance, in pixels, from graphic to the point. Returns
     *         Float.POSITIVE_INFINITY if the geometry isn't ready
     *         (ungenerated).
     */
    public float distanceToEdge(double x, double y);

    /**
     * Answsers the question whether or not the OMGeometry contains the given
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
    public boolean contains(double x, double y);

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
    public boolean regenerate(Projection proj);

    /**
     * Get the java.awt.Shape object that represents the projected graphic.
     * <p>
     * 
     * The java.awt.Shape object gives you the ability to do a little spatial
     * analysis on the graphics.
     * 
     * @return java.awt.geom.GeneralPath (Shape), or null if the graphic needs
     *         to be generated with the current map projection, or null if the
     *         OMGeometry hasn't been updated to use Shape objects for its
     *         internal representation.
     */
    public GeneralPath getShape();

    /**
     * Set the java.awt.Shape object that represents the projected graphic.
     * Ideally, the OMGeometry will set this internally. This method is provided
     * to clear out the object to save memory, or to allow manipulations if the
     * situation dictates.
     * <p>
     * 
     * The java.awt.Shape object gives you the ability to do a little spatial
     * analysis on the graphics.
     * 
     * @param gp java.awt.geom.GeneralPath (Shape), or null if the graphic needs
     *        to be cleared or regenerated.
     */
    public void setShape(GeneralPath gp);

    /**
     * OMGeometry method for returning a simple description of itself, for
     * debugging purposes.
     */
    public String getDescription();

    /**
     * Replace the member variables of this OMGraphic with copies of member
     * variables from another one.
     * 
     * @param source
     */
    void restore(OMGeometry source);
}