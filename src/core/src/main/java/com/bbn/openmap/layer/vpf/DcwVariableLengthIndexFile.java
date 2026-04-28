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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/DcwVariableLengthIndexFile.java,v $
// $RCSfile: DcwVariableLengthIndexFile.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 19:29:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.IOException;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * Read in a VPF variable length index file, and allows access to
 * individual records.
 */
public class DcwVariableLengthIndexFile {
    /** holds all the offset,size pairs */
    final private int offsettable[];
    /** the number of records */
    final private int recordCount;
    /** the end of the file offset */
    final private int endOfFileOffset;

    /**
     * Construct a new index file.
     * 
     * @param filename the name of the file to read in.
     * @param msbfirst the byte order of the file to be read.
     * @exception FormatException some error was encountered in trying
     *            to read the file.
     */
    public DcwVariableLengthIndexFile(String filename, boolean msbfirst)
            throws FormatException, IOException {
        this(new BinaryBufferedFile(filename), msbfirst);
    }

    /**
     * Construct a new index file.
     * 
     * @param inputstream BinaryBufferedFile handle.
     * @param msbfirst the byte order of the file to be read.
     * @exception FormatException some error was encountered in trying
     *            to read the file.
     */
    public DcwVariableLengthIndexFile(BinaryFile inputstream, boolean msbfirst)
            throws FormatException {

        try {
            inputstream.byteOrder(msbfirst);

            recordCount = inputstream.readInteger();
            /*int HeaderLength = */inputstream.readInteger();
            offsettable = new int[recordCount * 2];
            inputstream.readIntegerArray(offsettable, 0, recordCount * 2);
            endOfFileOffset = offsettable[offsettable.length - 2]
                    + offsettable[offsettable.length - 1];
            inputstream.close();
            //          com.bbn.openmap.util.Debug.output("VLI.init(): number
            // of entries(" + recordCount +
            //                                            ") HeaderLength(" + HeaderLength + ")");

        } catch (IOException i) {
            throw new FormatException("IOException with "
                    + inputstream.getName() + ": " + i.getMessage());
        }
    }

    /**
     * get the offset byte offset of the record in the associated
     * table file. If recordNumber is greater than the number of
     * records, this returns the offset of the end-of-file.
     * 
     * @param recordNumber the record to retrieve the offset for
     */
    public int recordOffset(int recordNumber) {
        return (recordCount < recordNumber) ? endOfFileOffset
                : offsettable[(recordNumber - 1) * 2];
    }

    /**
     * get the size of the record in the associated table file If
     * recordNumber is greater than the number of records, this
     * returns a record size of 0.
     * 
     * @param recordNumber the record to retrieve the offset for
     */
    public int recordSize(int recordNumber) {
        return (recordCount < recordNumber) ? 0
                : offsettable[(recordNumber - 1) * 2 + 1];
    }

    /** get the number of records in the index file */
    public int getRecordCount() {
        return recordCount;
    }

    /** close the associated input file */
    public void close() {}
}