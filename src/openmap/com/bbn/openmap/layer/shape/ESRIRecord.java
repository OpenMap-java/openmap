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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ESRIRecord.java,v $
// $RCSfile: ESRIRecord.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:09:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import com.bbn.openmap.dataAccess.shape.ShapeUtils;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * This is the base class for all shape record classes. It stores the
 * record number and content length of a record, also known as the
 * record header.
 * 
 * @author Ray Tomlinson
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.4 $ $Date: 2005/12/09 21:09:10 $
 */
public abstract class ESRIRecord extends ShapeUtils {

    /** The ordinal of this record. */
    public int recordNumber;

    /** The length of the data portion of this record. */
    public int contentLength;

    /**
     * Initialize an empty record. Used when constructing a record to
     * be written to a shape file.
     */
    public ESRIRecord() {
        recordNumber = 0;
        contentLength = 0;
    }

    /**
     * Initialize a record from a buffer. Reads the record header
     * fields from the given buffer at the given offset.
     * 
     * @param b the buffer
     * @param off the offset
     */
    public ESRIRecord(byte b[], int off) {
        recordNumber = readBEInt(b, off);
        contentLength = readBEInt(b, off + 4);
    }

    /**
     * Generates 2D OMGraphics and adds them to the given list. If you
     * are using jdk1.1.X, you'll have to comment out this method,
     * because jdk1.1.X doesn't know about the java.awt.Stroke and
     * java.awt.Paint interfaces.
     * 
     * @param list the graphics list
     * @param drawingAttributes Attributes that describe how to render
     *        the graphics.
     */
    public abstract void addOMGraphics(OMGraphicList list,
                                       DrawingAttributes drawingAttributes);

    /**
     * Generates OMGeometry and adds them to the given list. The list
     * takes care of DrawingAttributes.
     * 
     * @param list the geometry list
     */
    public abstract OMGeometry addOMGeometry(OMGeometryList list);

    /**
     * Gets this record's bounding box.
     * 
     * @return a bounding box
     */
    public abstract ESRIBoundingBox getBoundingBox();

    /**
     * Gets this record's shape type as an int. Shape types are
     * enumerated on the ShapeUtils class.
     * 
     * @return the shape type as an int
     */
    public abstract int getShapeType();

    /**
     * Yields the length of this record's data portion.
     * 
     * @return number of bytes equal to the size of this record's data
     */
    public abstract int getRecordLength();

    /**
     * Returns the number of bytes needed to store the record header
     * and record data.
     * 
     * @return number of bytes equal to the size of this record
     */
    public int getBinaryStoreSize() {
        return getRecordLength() + 8; // Constant for record headers
    }

    /**
     * Writes this record to the given buffer at the given offset.
     * 
     * @param b the buffer
     * @param off the offset
     * @return the number of bytes written
     */
    public int write(byte[] b, int off) {
        int nBytes = writeBEInt(b, off, recordNumber);
        nBytes += writeBEInt(b, off + nBytes, (getRecordLength() / 2));
        return nBytes;
    }

    //     public ESRIArcRecord getArcShape() {
    //      if (getShapeType() == SHAPE_TYPE_ARC) {
    //          return (ESRIArcRecord) this;
    //      } else {
    //          return null;
    //      }
    //     }

    /**
     * Accessor for record number field.
     * 
     * @return the ordinal of this record in the file
     */
    public int getRecordNumber() {
        return recordNumber;
    }

    /**
     * Accessor for the content length of this record. That's the size
     * of this record in 16 bit words.
     * 
     * @return the number of 16 bit words that this record takes up
     */
    public int getContentLength() {
        return contentLength;
    }
}