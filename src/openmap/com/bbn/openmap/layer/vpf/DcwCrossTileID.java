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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/DcwCrossTileID.java,v $
// $RCSfile: DcwCrossTileID.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 19:29:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.EOFException;
import java.io.IOException;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * Encapsulate the VPF Cross-Tile identifier primitive datatype. The
 * cross-tile identifier relates map features that cross multiple
 * tiles. <br>
 * Note: Mil-Std-2407 cross tile ids have a fourth, unused field. This
 * class will read that field, but does not use it.
 */
public class DcwCrossTileID {
    /** the 1-byte length specifier for the rest of the values... */
    //private int funkyDcwKeyValue = -1;
    /** the key for this tile */
    final public int currentTileKey;
    /** the id for the connected tile */
    final public int nextTileID;
    /** the key in the adjoining tile */
    final public int nextTileKey;

    /** unused value in VPF... */
    //private int unusedDcwKey = -1;
    /**
     * Construct a DcwCrossTileID
     * 
     * @param currentTileKey primitive ID in current tile
     * @param nextTileID tile ID
     * @param nextTileKey primitive ID in nextTileID
     */
    public DcwCrossTileID(int currentTileKey, int nextTileID, int nextTileKey) {
        this.currentTileKey = currentTileKey;
        this.nextTileID = nextTileID;
        this.nextTileKey = nextTileKey;
    }

    /**
     * Construct a DcwCrossTileID from the specified input stream.
     * 
     * @param in the filestream to construct from
     * @exception FormatException some error was detected while
     *            reading the info for the column.
     * @exception EOFException EOF was encountered before reading any
     *            data
     */
    public DcwCrossTileID(BinaryFile in) throws FormatException, EOFException {
        int format;
        try {
            format = in.read();
        } catch (IOException ioe) {
            throw new FormatException(ioe.getMessage());
        }
        if (format == -1) {
            throw new EOFException();
        }

        try {
            currentTileKey = readIntegerByKey(in, format >> 6);
            nextTileID = readIntegerByKey(in, format >> 4);
            nextTileKey = readIntegerByKey(in, format >> 2);
            /*int unusedDcwKey = */readIntegerByKey(in, format);
        } catch (EOFException e) {
            throw new FormatException("DcwCrossTileID: unexpected EOD "
                    + e.getMessage());
        }
    }

    /**
     * Reads an integer from the input stream
     * 
     * @param in the stream to read from
     * @param key specifies the number of bytes to read (based on
     *        bottom 2 bits)
     * @return the integer read. (-1 for a zero-length field)
     * @exception FormatException internal consistency failure
     * @exception EOFException hit end-of-file while reading data
     */
    private int readIntegerByKey(BinaryFile in, int key)
            throws FormatException, EOFException {
        switch (key & 0x3) {
        case 0:
            return -1;
        case 1: {
            int byteval;
            try {
                byteval = in.read();
            } catch (IOException ioe) {
                throw new FormatException(ioe.getMessage());
            }
            if (byteval == -1) {
                throw new EOFException();
            }
            return byteval;
        }
        case 2:
            return MoreMath.signedToInt(in.readShort());
        case 3:
            return in.readInteger();
        }
        throw new FormatException("This can't happen");
    }

    /**
     * produce a nice printed version of all our contained information
     * 
     * @return a nice little string
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append(currentTileKey).append("/");
        if ((nextTileID != -1) && (nextTileKey != -1)) {
            output.append(nextTileID).append(",");
            output.append(nextTileKey);
        }
        return output.toString();
    }
}