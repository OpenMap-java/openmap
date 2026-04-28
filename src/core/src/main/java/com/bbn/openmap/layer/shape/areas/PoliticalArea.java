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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/areas/PoliticalArea.java,v $
// $RCSfile: PoliticalArea.java,v $
// $Revision: 1.5 $
// $Date: 2006/08/25 15:36:15 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape.areas;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.net.URL;

import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.Debug;

/**
 * A PoliticalArea is a region that has a name (like "Oklahoma"), an identifier
 * (like "OK"), and a list of OMGraphics that define its geography (ie: the
 * polygons that define it's borders).
 * <P>
 * NOTE: The name of this class is somewhat misleading - the graphic doesn't
 * have to represent an area - the graphic can be any graphic created from the
 * shapefile. This class just provides a way to associate an id with the
 * graphic.
 */
public class PoliticalArea {
    public final String id;

    public String name = null;
    protected OMGeometryList geometry;

    protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

    public PoliticalArea(String identifier) {
        this(null, identifier);
    }

    /**
     * Create a political area with a name, and an identifier which is used as a
     * key by the AreaHandler.
     */
    public PoliticalArea(String name, String identifier) {
        this.id = identifier;
        this.name = name;
        geometry = new OMGeometryList();
    }

    public void setDrawingAttributes(DrawingAttributes da) {
        drawingAttributes = da;
        da.setTo(geometry);
    }

    public DrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    /**
     * Set the fill-paint of all the graphics in the List
     * 
     * @param c java.awt.Paint
     */
    public void setFillPaint(Paint c) {
        drawingAttributes.setFillPaint(c);
        drawingAttributes.setTo(geometry);
    }

    /**
     * Get the paint used for the fill paint for all the graphics in the
     * political area, if one was set.
     * 
     * @return Paint if set, null if it wasn't.
     */
    public Paint getFillPaint() {
        return drawingAttributes.getFillPaint();
    }

    /**
     * Set the fill pattern of all the graphics in the List. This will override
     * the fill paint, if you've set that as well. There are sections of code in
     * this method that need to be commented out if you are not using jdk 1.2.x.
     * 
     * @param fillPatternURL url of image file to use as fill.
     */
    public void setFillPattern(URL fillPatternURL) {
        // This is kind of tricky. Look at the list, find out which
        // members are OMGraphic2D objects, and set the Paint for
        // those graphics.

        TexturePaint texture = null;
        try {

            if (fillPatternURL != null) {
                BufferedImage bi = BufferedImageHelper.getBufferedImage(fillPatternURL,
                        0,
                        0,
                        -1,
                        -1);
                texture = new TexturePaint(bi, new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
            }
        } catch (InterruptedException ie) {
            Debug.error("PoliticalArea.setFillPattern(): error getting texture image - \n"
                    + ie);
        }

        setFillPattern(texture);
    }

    /**
     * Set the fill pattern of all the graphics in the List. This will override
     * the fill paint, if you've set that as well. There are sections of code in
     * this method that need to be commented out if you are not using jdk 1.2.x.
     * 
     * @param texture TexturePaint object to use as fill.
     */
    public void setFillPattern(TexturePaint texture) {
        drawingAttributes.setFillPaint(texture);
        drawingAttributes.setTo(geometry);
    }

    /**
     * Get the TexturePaint used as fill for all the graphics in the political
     * area, if one was set.
     * 
     * @return TexturePaint if set, null if it wasn't.
     */
    public TexturePaint getFillPattern() {
        return drawingAttributes.getFillPattern();
    }

    /**
     * Set the line-paint of all the graphics in the List
     * 
     * @param c java.awt.Paint
     */
    public void setLinePaint(Paint c) {
        drawingAttributes.setLinePaint(c);
        drawingAttributes.setTo(geometry);
    }

    /**
     * Get the paint used for the line paint for all the graphics in the
     * political area, if one was set.
     * 
     * @return Paint if set, null if it wasn't.
     */
    public Paint getLinePaint() {
        return drawingAttributes.getLinePaint();
    }

    /**
     * Set the select-paint of all the graphics in the List
     * 
     * @param c java.awt.Paint
     */
    public void setSelectPaint(Paint c) {
        drawingAttributes.setSelectPaint(c);
        drawingAttributes.setTo(geometry);
    }

    /**
     * Get the paint used for the select paint for all the graphics in the
     * political area, if one was set.
     * 
     * @return Paint if set, null if it wasn't.
     */
    public Paint getSelectPaint() {
        return drawingAttributes.getSelectPaint();
    }

    /**
     * Get the value of geometry.
     * 
     * @return Value of geometry.
     */
    public OMGeometryList getGeometry() {
        return geometry;
    }

    /**
     * Set the value of geometry.
     * 
     * @param v Value to assign to geometry.
     */
    public void setGeometry(OMGeometryList v) {
        this.geometry = v;
        drawingAttributes.setTo(v);
    }

    /**
     * Add a new omgraphic to the list of graphics in this area
     */
    public void addGraphic(OMGraphic g) {
        this.geometry.add((OMGeometry) g);
        drawingAttributes.setTo(g);
    }
}