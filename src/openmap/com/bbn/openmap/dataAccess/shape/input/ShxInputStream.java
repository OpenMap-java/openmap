// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/input/ShxInputStream.java,v $
// $RCSfile: ShxInputStream.java,v $
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.shape.input;

import java.io.*;

/**
 * Reads index data from a .shx file
 * @author Doug Van Auken
 */
public class ShxInputStream {

    /**
     * An integer specifying the type of layer, as defined by Esri's
     * shape file format specifications 
     */
    private int _type = -1;
    
    /** An input stream to process primitives in Little Endian or Big Endian */
    private LittleEndianInputStream _leis = null;
    
    /**
     * Chains an input stream with a Little EndianInputStream
     */
    public ShxInputStream(InputStream is) {
        BufferedInputStream bis = new BufferedInputStream(is);
        _leis = new LittleEndianInputStream(bis);
    }
    
    /**
     * Processes the SHX file to obtain a list of offsets, which classes
     * derived from AbstractSupport will use to iterate through the
     * associated SHP file
     *
     * @return an array of offsets, which will be passed into the
     * open method of classes which extend AbstractSupport.  
     */
    public int[][] getIndex() {
        int[][] indexData = null;
        try {
            int fileCode, fileLength, contentLength, Offset, numShapes;
            fileCode = _leis.readInt();
            _leis.skipBytes(20);
            fileLength = _leis.readInt();
            numShapes = (fileLength - 50) / 4;
            indexData = new int[2][numShapes];
            _leis.skipBytes(4);
            byte[] intBytes = new byte[4];
            _type = _leis.readLEInt();
            _leis.skip(64);
            for (int i = 0; i <= numShapes - 1; i++) {
                indexData[0][i] = _leis.readInt();
                indexData[1][i] = _leis.readInt();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return indexData;
    }
    
    public void close() throws IOException{
        _leis.close();
    }
}

