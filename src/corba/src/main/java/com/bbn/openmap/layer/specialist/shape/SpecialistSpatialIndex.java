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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/shape/SpecialistSpatialIndex.java,v $
// $RCSfile: SpecialistSpatialIndex.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.specialist.shape;

import java.io.IOException;

import com.bbn.openmap.layer.shape.ESRIRecord;
import com.bbn.openmap.layer.shape.SpatialIndex;

/**
 */
public class SpecialistSpatialIndex extends SpatialIndex {

    /**
     * Opens a spatial index file for its associated shape file.
     *
     * @param shpFilename the name of the shape file
     * @exception IOException if something goes wrong opening the
     *            files
     */
    public SpecialistSpatialIndex(String shpFilename)
            throws IOException {
        super(shpFilename);
    }

    /**
     * Creates a record instance from the shape file data. Calls the
     * appropriate record constructor based on the shapeType, and
     * passes the buffer and offset to that constructor.
     *
     * @param shapeType the shape file's shape type, enumerated in
     *        <code>ShapeUtils</code>
     * @param b the buffer pointing to the raw record data
     * @param off the offset of the data starting point in the buffer @
     * @exception IOException if something goes wrong reading the file
     * @see ShapeUtils
     */
    public ESRIRecord makeESRIRecord(int shapeType, byte[] b, int off)
            throws IOException {

        switch (shapeType) {

        case SHAPE_TYPE_NULL:
            return null;
        case SHAPE_TYPE_POINT:
            return new ESRISpecialistPointRecord(b, off);
        case SHAPE_TYPE_POLYGON:
        case SHAPE_TYPE_ARC:
            //      case SHAPE_TYPE_POLYLINE:
            return new ESRISpecialistPolygonRecord(b, off);
        case SHAPE_TYPE_MULTIPOINT:
            System.out.println("SpecialistSpatialIndex.makeESRIRecord: Arc NYI");
            return null;
        //          return new ESRIMultipointRecord(b, off);
        default:
            return null;
        }
    }
}