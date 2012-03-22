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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/DcwSpatialIndex.java,v $
// $RCSfile: DcwSpatialIndex.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.EOFException;
import java.io.IOException;
import java.util.BitSet;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

/** Read a VPF spatial index file. (VPF *.?si files) */
public class DcwSpatialIndex {
    /** the vpf file with the data */
    private BinaryFile inputFile = null;
    /** the number of primitive features that are indexed */
    final private int numberOfPrimitives;
    /** the boundaries of the spatial index */
    final private float boundingRectx1, boundingRecty1, boundingRectx2,
            boundingRecty2;
    /** the number of splits in the spatial index */
    final private int nodesInTree;
    /** node info */
    int nodeinfo[][];

    /**
     * Construct an index for a filename. Prints a bunch of
     * information about what it read.
     */
    public DcwSpatialIndex(String filename, boolean border)
            throws FormatException {
        try {
            inputFile = new BinaryBufferedFile(filename);
        } catch (IOException e) {
            throw new FormatException("Can't open file " + filename + ": "
                    + e.getMessage());
        }
        inputFile.byteOrder(border);

        try {
            numberOfPrimitives = inputFile.readInteger();
            boundingRectx1 = inputFile.readFloat();
            boundingRecty1 = inputFile.readFloat();
            boundingRectx2 = inputFile.readFloat();
            boundingRecty2 = inputFile.readFloat();
            nodesInTree = inputFile.readInteger();

            if (Debug.debugging("vpfserver")) {
                System.out.println("NumberOfPrimitives = " + numberOfPrimitives);
                System.out.println("Bounding Rect = (" + boundingRectx1 + ", "
                        + boundingRecty1 + ") - (" + boundingRectx2 + ", "
                        + boundingRecty2 + ")");
                System.out.println("Nodes in Tree = " + nodesInTree);
            }

            nodeinfo = new int[nodesInTree][2]; //offset, count

            for (int i = 0; i < nodesInTree; i++) {
                inputFile.readIntegerArray(nodeinfo[i], 0, 2);
            }

            if (Debug.debugging("vpfserver")) {
                int baseOffset = 24 + nodesInTree * 8;
                BitSet b = new BitSet(nodesInTree);
                int actprimcnt = 0;
                b.set(0);
                for (int i = 0; i < nodesInTree; i++) {
                    if ((baseOffset + nodeinfo[i][0]) != inputFile.getFilePointer()) {
                        throw new FormatException("SI Input appears to be out-of-sync");
                    }
                    StringBuffer pr = new StringBuffer("i=").append(i + 1);
                    pr.append(" offset=").append(nodeinfo[i][0]);
                    pr.append(" count=").append(nodeinfo[i][1]);
                    for (int j = 0; j < nodeinfo[i][1]; j++) {
                        actprimcnt++;
                        PrimitiveRecord prim = new PrimitiveRecord(inputFile);
                        pr.append("\n\t").append(prim.toString());
                    }
                    if (nodeinfo[i][1] != 0) {
                        if ((i < 15) || ((i + 1) == nodesInTree)) {
                            System.out.println(pr);
                        }
                        b.set(i + 1);
                        if (!b.get((i + 1) / 2)) {
                            throw new FormatException("condition failed");
                        }
                    }
                }
                if (actprimcnt == numberOfPrimitives) {
                    System.out.println("Got the right number of primitives");
                } else {
                    System.out.println("!!Got the wrong number of primitives");
                }
                if (inputFile.available() != 0) {
                    throw new FormatException("Bytes left at end of file "
                            + inputFile.available());
                }
            }
        } catch (EOFException e) {
            throw new FormatException("Hit Premature EOF in thematic index");
        } catch (IOException i) {
            throw new FormatException("Encountered IO Exception: "
                    + i.getMessage());
        }
    }

    /**
     * Returns the number of primitives included in the spatial index
     * 
     * @return the number of primitives included in the spatial index
     */
    public int getNumberOfPrimitives() {
        return numberOfPrimitives;
    }

    /**
     * Returns the west boundary
     * 
     * @return the west boundary
     */
    public float getBoundingX1() {
        return boundingRectx1;
    }

    /**
     * Returns the east boundary
     * 
     * @return the east boundary
     */
    public float getBoundingX2() {
        return boundingRectx2;
    }

    /**
     * Returns the south boundary
     * 
     * @return the south boundary
     */
    public float getBoundingY1() {
        return boundingRecty1;
    }

    /**
     * Returns the north boundary
     * 
     * @return the north boundary
     */
    public float getBoundingY2() {
        return boundingRecty2;
    }

    /**
     * Returns the number of nodes in the spatial index
     * 
     * @return the number of nodes in the spatial index
     */
    public int getNodesInTree() {
        return nodesInTree;
    }

    /**
     * Returns the number of primitives listed in the node
     * 
     * @param node the node index
     * @return the number of primitives listed in the node
     */
    public int getPrimitiveCount(int node) {
        return nodeinfo[node][1];
    }

    /**
     * Returns the relative byte offset of the node primitive list
     * from the header
     * 
     * @param node the node index
     * @return the byte offset of the record in the file
     */
    public int getPrimitiveOffset(int node) {
        return nodeinfo[node][0];
    }

    /**
     * Returns an array of primitive records
     * 
     * @param node the node index
     * @exception FormatException an error was encountered reading the
     *            file
     * @return the array of primitive records
     */
    public PrimitiveRecord[] getPrimitiveRecords(int node)
            throws FormatException {
        int count = getPrimitiveCount(node);
        int offset = getPrimitiveOffset(node);
        PrimitiveRecord[] ret = new PrimitiveRecord[count];
        try {
            //offset measures from the end of the header
            inputFile.seek(offset + 24 + nodesInTree * 8);
            for (int i = 0; i < count; i++) {
                ret[i] = new PrimitiveRecord(inputFile);
            }
        } catch (IOException ioe) {
            throw new FormatException("Error reading spatial index file");
        }
        return ret;
    }

    /**
     * A class that wraps an entry in the spatial index.
     */
    public static class PrimitiveRecord {
        /** see the VPF spec for what these mean */
        final public short x1, y1, x2, y2;
        /** the id of the primitive this record is for */
        final public int primId;

        /**
         * construct a new primitive record
         * 
         * @param inputFile the file to read the record from
         * @exception FormatException an error was encountered reading
         *            the record
         * @exception EOFException an error was encountered reading
         *            the record
         */
        public PrimitiveRecord(BinaryFile inputFile) throws FormatException,
                EOFException {
            x1 = (short) (inputFile.readChar() & 0xff);
            y1 = (short) (inputFile.readChar() & 0xff);
            x2 = (short) (inputFile.readChar() & 0xff);
            y2 = (short) (inputFile.readChar() & 0xff);
            //foo[] = inputFile.readBytes(4, false); //x1 y1 x2 y2
            primId = inputFile.readInteger();
        }

        /**
         * Returns a pretty string representation of the record
         * 
         * @return a string version of the record
         */
        public String toString() {
            return ("(" + primId + ": \t" + x1 + " \t" + x2 + " \t" + y1
                    + " \t" + y2 + ")");
        }
    }

    /**
     * Closes the files associated with the spatial index
     */
    public void close() {
        try {
            inputFile.close();
        } catch (IOException i) {
            System.out.println("Caught ioexception " + i.getClass() + " "
                    + i.getMessage());
        }
    }
}