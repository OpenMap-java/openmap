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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ESRIMultiPointRecord.java,v $
// $RCSfile: ESRIMultiPointRecord.java,v $
// $Revision: 1.4 $
// $Date: 2005/12/09 21:09:09 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGeometry;
import com.bbn.openmap.omGraphics.OMGeometryList;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

/**
 * This is a skeleton of the MultiPoint record type. No methods are
 * implemented.
 * 
 * <H2>To Do</H2>
 * <UL>
 * <LI>Implement the methods of this class.</LI>
 * </UL>
 * 
 * @author Ray Tomlinson
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.4 $ $Date: 2005/12/09 21:09:09 $
 */
public class ESRIMultiPointRecord extends ESRIRecord {

    /**
     * Constructor skeleton.
     * 
     * @param b the buffer
     * @param off the offset
     */
    public ESRIMultiPointRecord(byte b[], int off) {}

    /**
     * Generates 2D OMGraphics and adds them to the given list. If you
     * are using jdk1.1.X, you'll have to comment out this method,
     * because jdk1.1.X doesn't know about the java.awt.Stroke and
     * java.awt.Paint interfaces.
     * 
     * @param list the graphics list
     * @param drawingAttributes Attributes for rendering.
     */
    public void addOMGraphics(OMGraphicList list,
                              DrawingAttributes drawingAttributes) {

    }

    /**
     * Generates OMGeometry and adds it to the list.
     * 
     * @param list the graphics list
     */
    public OMGeometry addOMGeometry(OMGeometryList list) {
        return null;
    }

    /**
     * Gets this record's bounding box.
     * 
     * @return a bounding box
     */
    public ESRIBoundingBox getBoundingBox() {
        return null;
    }

    /**
     * Gets this record's shape type as an int. Shape types are
     * enumerated on the ShapeUtils class.
     * 
     * @return the shape type as an int
     */
    public int getShapeType() {
        return SHAPE_TYPE_MULTIPOINT;
    }

    /**
     * Yields the length of this record's data portion.
     * 
     * @return number of bytes equal to the size of this record's data
     */
    public int getRecordLength() {
        Debug.output("HACK: ESIRMultiPointRecord.getRecordLength: NYI");
        return 0;
    }

    /**
     * Writes this multipoint record to the given buffer at the given
     * offset.
     * 
     * @param b the buffer
     * @param off the offset
     * @return the number of bytes written
     */
    public int write(byte[] b, int off) {
        return 0;
    }
}