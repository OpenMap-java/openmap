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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/BasicIconPart.java,v $
// $RCSfile: BasicIconPart.java,v $
// $Revision: 1.5 $
// $Date: 2006/08/29 23:07:54 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import com.bbn.openmap.omGraphics.DrawingAttributes;

/**
 * The BasicIconPart is an implementation of the IconPart. In addition
 * to the geometry and DrawingAttributes adjustments that can be done
 * on an IconPart, the BasicIconPart also lets you use an
 * AffineTransform to rotate, translate or scale the geometrym and
 * will create GradientPaints for the Colors from the
 * DrawingAttribtues if desired.
 */
public class BasicIconPart implements IconPart, Cloneable {

    /**
     * AffineTransform to adjust geometry if needed.
     */
    protected AffineTransform baseTransform;
    /**
     * Shape geometry for this IconPart.
     */
    protected Shape geometry;
    /**
     * Shape clipping area for this IconPart.
     */
    protected Shape clip;
    /**
     * DrawingAttributes for this IconPart.
     */
    protected DrawingAttributes renderingAttributes = null;
    /**
     * Flag to modifying DrawingAttributes Colors into GradientPaints,
     * for that 3D lighting look.
     */
    protected boolean gradient = false;

    /**
     * Create a BasicIconPart with a java.awt.Shape object for a geometry.
     */
    public BasicIconPart(Shape shape) {
        this(shape, null, DrawingAttributes.DEFAULT);
    }

    /**
     * Create a BasicIconPart with a java.awt.Shape object for a geometry, along
     * with an AffineTransform that may be applied to the geometry at
     * rendertime.
     */
    public BasicIconPart(Shape shape, AffineTransform transform) {
        this(shape, transform, DrawingAttributes.DEFAULT);
    }
    
    /**
     * Create a BasicIconPart with a java.awt.Shape object for a geometry.
     */
    public BasicIconPart(Shape shape, DrawingAttributes da) {
        this(shape, (AffineTransform) null, da);
    }

    /**
     * Create a BasicIconPart with a java.awt.Shape object for a geometry, along
     * with an AffineTransform that may be applied to the geometry at
     * rendertime.
     */
    public BasicIconPart(Shape shape, AffineTransform transform, DrawingAttributes da) {
        geometry = shape;

        if (transform == null) {
            transform = new AffineTransform();
        }

        baseTransform = transform;
        setRenderingAttributes(da);
    }

    /**
     * Get the DrawingAttributes that should be used for rendering.
     * 
     * @param da DrawingAttributes passed in that may affect rendering
     *        choices. Can be null, and the IconPart may decide to
     *        ignore it.
     * @return DrawingAttribute for this part.
     */
    protected DrawingAttributes getAttributesForRendering(DrawingAttributes da) {
        return getRenderingAttributes();
    }

    /**
     * @param g a java.awt.Graphics object to render into.
     * @param width pixel width of icon, used to scale geometry.
     * @param height pixel height of icon, used to scale geometry.
     */
    public void render(Graphics g, int width, int height) {
        render(g, width, height, null);
    }

    /**
     * @param g a java.awt.Graphics object to render into.
     * @param width pixel width of icon, used to scale geometry.
     * @param height pixel height of icon, used to scale geometry.
     * @param appDA drawing attributes to use under certain
     *        conditions. Certain IconParts on this list may use these
     *        drawing attributes if they want/should. May be null.
     */
    public void render(Graphics g, int width, int height,
                       DrawingAttributes appDA) {

        AffineTransform transform = AffineTransform.getScaleInstance((double) width / 100.0,
                (double) height / 100.0);
        transform.concatenate(baseTransform);

        // Handle clip area in Graphics, first
        Shape clip = getClip();
        if (clip != null) {
            g.setClip(new GeneralPath(clip).createTransformedShape(transform));
        }

        Shape shape = new GeneralPath(geometry).createTransformedShape(transform);
        getAttributesForRendering(appDA).render((Graphics2D) g, shape, gradient);
    }

    /**
     * Set whether colors should be replaced by GradientPaints.
     */
    public void setGradient(boolean value) {
        gradient = value;
    }

    /**
     * Get whether colors should be replaced by GradientPaints.
     */
    public boolean isGradient() {
        return gradient;
    }

    public void setClip(Shape clipArea) {
        clip = clipArea;
    }

    public Shape getClip() {
        return clip;
    }

    public void setGeometry(Shape shape) {
        geometry = shape;
    }

    public Shape getGeometry() {
        return geometry;
    }

    public void setTransform(AffineTransform af) {
        baseTransform = af;
    }

    public AffineTransform getTransform() {
        return baseTransform;
    }

    public void setRenderingAttributes(DrawingAttributes da) {
        renderingAttributes = da;
    }

    public DrawingAttributes getRenderingAttributes() {
        if (renderingAttributes == null) {
            return DrawingAttributes.DEFAULT;
        } else {
            return renderingAttributes;
        }
    }

    public Object clone() {
        BasicIconPart clone = null;
        try {
            clone = (BasicIconPart) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }
}