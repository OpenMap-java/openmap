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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/IconPartList.java,v $
// $RCSfile: IconPartList.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.omGraphics.DrawingAttributes;

/**
 * An IconPartList is a group of IconParts that can be rendered
 * together. If you ask an IconPartList for it's geometry, it will
 * combine all its parts into one geometry, and use its
 * DrawingAttributes to render that combined shape. The IconPartList
 * is itself an IconPart, so the recursive possibilities are endless.
 */
public class IconPartList implements IconPart, Iterable<IconPart>, Cloneable {

    protected List<IconPart> parts;
    protected DrawingAttributes renderingAttributes = null;
    protected Shape clip = null;

    public IconPartList() {}

    protected List<IconPart> getList() {
        if (parts == null) {
            parts = new LinkedList<IconPart>();
        }
        return parts;
    }

    public Iterator<IconPart> iterator() {
        return parts.iterator();
    }

    /**
     * First in drawn on bottom. Last in on top.
     */
    public void add(IconPart part) {
        getList().add(part);
    }

    public boolean remove(IconPart part) {
        return getList().remove(part);
    }

    public void clear() {
        getList().clear();
    }

    public void render(Graphics g, int width, int height) {
        render(g, width, height, null);
    }

    /**
     * @param appDA drawing attributes to use under certain
     *        conditions. Certain IconParts on this list may use these
     *        drawing attributes if they want/should. May be null.
     */
    public void render(Graphics g, int width, int height,
                       DrawingAttributes appDA) {

        // Handle clip area in Graphics, first
        Shape clip = getClip();
        if (clip != null) {
            g.setClip(clip);
        }

        DrawingAttributes da = getRenderingAttributes();
        DrawingAttributes tmpDA = null;

        for (IconPart part : this) {

            if (da != null) {
                tmpDA = part.getRenderingAttributes();
                part.setRenderingAttributes(da);
            }

            Graphics2D g2 = (Graphics2D) g.create();
            part.render(g2, width, height);
            g2.dispose();

            if (da != null) {
                part.setRenderingAttributes(tmpDA);
                tmpDA = null;
            }
        }
    }

    public void setClip(Shape clipArea) {
        clip = clipArea;
    }

    public Shape getClip() {
        return clip;
    }

    public void setGeometry(Shape shape) {
        // dump the list, create a generic IconPart to hold the shape.
        List<IconPart> list = getList();
        list.clear();
        list.add(new BasicIconPart(shape));
    }

    /**
     * If you ask a IconPartList for its geometry, it will combine all
     * its parts to make one Shape object. All the rendering
     * attributes from the individual parts will be ignored. The
     * contributions will be kept geometrically separate
     * (disconnected) and their clipping areas will be ignored.
     */
    public Shape getGeometry() {
        GeneralPath geometry = null;
        for (IconPart part : this) {

            Shape shp = part.getGeometry();

            if (shp == null) {
                continue;
            }

            if (geometry == null) {
                geometry = new GeneralPath(shp);
            } else {
                geometry.append(shp, false);
            }
        }
        return geometry;
    }

    public void setRenderingAttributes(DrawingAttributes da) {
        renderingAttributes = da;
    }

    public DrawingAttributes getRenderingAttributes() {
        return renderingAttributes;
    }

    public Object clone() {
        IconPartList clone = new IconPartList();
        for (IconPart part : this) {
            clone.add((IconPart)part.clone());
        }

        clone.setRenderingAttributes(getRenderingAttributes());
        clone.setClip(getClip());
        return clone;
    }

}