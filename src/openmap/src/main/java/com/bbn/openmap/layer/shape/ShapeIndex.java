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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ShapeIndex.java,v $
// $RCSfile: ShapeIndex.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.bbn.openmap.dataAccess.shape.ShapeUtils;
import com.bbn.openmap.util.Debug;

/**
 * A class representing a shape index file.
 * <p>
 * Currently this class has limited capabilities. It can dump the
 * index information to stdout. This is useful for verification of a
 * spatial index.
 * 
 * <H2>Usage</H2>
 * <DT>java com.bbn.openmap.shape.ShapeIndex file.shx</DT>
 * <DD><i>Dumps spatial index information, excluding bounding boxes
 * to stdout. Useful for comparing to a shape index. </i></DD>
 * 
 * <H2>To Do</H2>
 * <UL>
 * <LI>Generate an index from a shape file</LI>
 * </UL>
 * 
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.6 $ $Date: 2005/12/09 21:09:10 $
 * @see SpatialIndex
 */
public class ShapeIndex extends ShapeUtils {

    /** Size of the shape index record, value is 8 bytes. */
    public final static int SHAPE_INDEX_RECORD_LENGTH = 8;

    /** The shape index file. */
    protected RandomAccessFile shx;

    /**
     * Opens a shape index file for reading.
     * 
     * @param shxFilename the name of the spatial index file
     * @exception IOException if something goes wrong opening the file
     */
    public ShapeIndex(String shxFilename) throws IOException {
        shx = new RandomAccessFile(shxFilename, "r");
    }

    /**
     * Displays the contents of this index.
     * 
     * @exception IOException if something goes wrong reading the file
     */
    public void dumpIndex() throws IOException {
        boolean atEOF = false;
        byte ixRecord[] = new byte[SHAPE_INDEX_RECORD_LENGTH];
        int recNum = 0;

        shx.seek(100); // skip the file header
        while (!atEOF) {
            int result = shx.read(ixRecord, 0, SHAPE_INDEX_RECORD_LENGTH);
            if (result == -1) {
                atEOF = true;
                Debug.output("Processed " + recNum + " records");
            } else {
                recNum++;
                int offset = readBEInt(ixRecord, 0);
                int length = readBEInt(ixRecord, 4);
                Debug.output("Record " + recNum + ": " + offset + ", " + length);
            }
        }

    }

    /**
     * The driver for the command line interface. Reads the command
     * line arguments and executes appropriate calls.
     * <p>
     * See the file documentation for usage.
     * 
     * @param args the command line arguments
     * @exception IOException if something goes wrong reading or
     *            writing the file
     */
    public static void main(String args[]) throws IOException {
        String name = args[0];
        ShapeIndex si = new ShapeIndex(name);
        si.dumpIndex();
    }
}