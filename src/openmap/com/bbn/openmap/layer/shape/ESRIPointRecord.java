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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ESRIPointRecord.java,v $
// $RCSfile: ESRIPointRecord.java,v $
// $Revision: 1.9 $
// $Date: 2006/08/09 21:08:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.io.IOException;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMRaster;

/**
 * An ESRI Point record.
 * 
 * @author Ray Tomlinson
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.9 $ $Date: 2006/08/09 21:08:41 $
 */
public class ESRIPointRecord extends ESRIRecord {

    /** The x coordinate. */
    protected double x;

    /** The y coordinate. */
    protected double y;

    /** A BufferedImage to use at the point. */
    protected ImageIcon ii;

    /**
     * Initializes this point from the given point.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public ESRIPointRecord(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Initialize a point record from the given buffer.
     * 
     * @param b the buffer
     * @param off the offset into the buffer where the data starts
     */
    public ESRIPointRecord(byte b[], int off) throws IOException {
        super(b, off);

        int ptr = off + 8;

        int shapeType = readLEInt(b, ptr);
        ptr += 4;
        if (shapeType != SHAPE_TYPE_POINT) {
            throw new IOException("Invalid point record.  Expected shape "
                    + "type " + SHAPE_TYPE_POINT + " but found " + shapeType);
        }

        x = readLEDouble(b, ptr);
        ptr += 8;

        y = readLEDouble(b, ptr);
        ptr += 8;
    }

    /**
     * Initialize a point record from the given buffer.
     * 
     * @param b the buffer
     * @param off the offset into the buffer where the data starts
     */
    public ESRIPointRecord(byte b[], int off, ImageIcon imageIcon)
            throws IOException {
        super(b, off);

        int ptr = off + 8;

        int shapeType = readLEInt(b, ptr);
        ptr += 4;
        if (shapeType != SHAPE_TYPE_POINT && shapeType != SHAPE_TYPE_NULL) {
            throw new IOException("Invalid point record.  Expected shape "
                    + "type " + SHAPE_TYPE_POINT + " but found " + shapeType);
        }

        x = readLEDouble(b, ptr);
        ptr += 8;

        y = readLEDouble(b, ptr);
        ptr += 8;

        ii = imageIcon;
    }

    /**
     * Gets this record's bounding box.
     * 
     * @return a bounding box
     */
    public ESRIBoundingBox getBoundingBox() {
        return new ESRIBoundingBox(x, y);
    }

    /**
     * Yields the length of this record's data portion. Always 20.
     * 
     * @return number of bytes equal to the size of this record's data
     */
    public int getRecordLength() {
        return 20;
    }

    /**
     * Writes this point to the given buffer at the given offset.
     * 
     * @param b the buffer
     * @param off the offset
     * @return the number of bytes written
     */
    public int write(byte[] b, int off) {
        int nBytes = super.write(b, off);
        nBytes += writeLEInt(b, off + nBytes, SHAPE_TYPE_POINT);
        nBytes += writeLEDouble(b, off + nBytes, x);
        nBytes += writeLEDouble(b, off + nBytes, y);
        return nBytes;
    }

    /**
     * Generates Points and adds them to the given list.
     * 
     * @param list the graphics list
     * @param drawingAttributes DrawingAttributes to describe how to
     *        represent the points, if an ImageIcon is not defined.
     */
    public void addOMGraphics(OMGraphicList list,
                              DrawingAttributes drawingAttributes) {
        if (ii == null) {
            OMPoint r = new OMPoint((float) y, (float) x);
            if (drawingAttributes != null) {
                drawingAttributes.setTo(r);
            }
            list.add(r);
            r.setAppObject(new Integer(getRecordNumber())); //added by DGK
        } else {
            list.add(new OMRaster((float) y, (float) x, -ii.getIconWidth() / 2, -ii.getIconHeight() / 2, ii));
        }
    }

    /**
     * Generates Points and adds them to the given list.
     * 
     * @param list the graphics list
     */
    public OMGeometry addOMGeometry(OMGeometryList list) {
        // Don't have a point geometry yet.
        return null;
    }

    /**
     * Gets this record's shape type as an int. Shape types are
     * enumerated on the ShapeUtils class.
     * 
     * @return the shape type as an int
     */
    public int getShapeType() {
        return SHAPE_TYPE_POINT;
    }

    /**
     * Get the x coordinate for this record.
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y coordinate for this record.
     */
    public double getY() {
        return y;
    }
}