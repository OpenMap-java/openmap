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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/output/ShxOutputStream.java,v $
// $RCSfile: ShxOutputStream.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape.output;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

/**
 * Writes the .shx file
 * 
 * @author Doug Van Auken
 */
public class ShxOutputStream {
    /**
     * An outputstream that writes primitive data types in little endian or big
     * endian
     */
    private LittleEndianOutputStream _leos = null;

    /**
     * Chains an output stream to a LittleEndianOutputStream
     * 
     * @param os An outputstream to chain a LittleEndianOutputStream to
     */
    public ShxOutputStream(OutputStream os) {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        _leos = new LittleEndianOutputStream(bos);
    }

    /**
     * Writes the index, with the default extents of the graphics being the
     * entire earth.
     * 
     * @param indexData The index data to write
     * @param layerType Tye type of layer being written
     * @return True if no exceptions occur
     */
    public boolean writeIndex(int[][] indexData, int layerType) {
        return writeIndex(indexData, layerType, new double[] { -90, -180, 90,
                180 });
    }

    /**
     * Writes the index, with the default extents of the graphics being the
     * entire earth.
     * 
     * @param indexData The index data to write
     * @param layerType Tye type of layer being written
     * @param extents an array of floats describing, in order, miny, minx, maxy,
     *        maxx for the area that the graphics in the shape file cover.
     * @return True if no exceptions occur
     */
    public boolean writeIndex(int[][] indexData, int layerType, double[] extents) {

        try {
            _leos.writeInt(9994);
            _leos.writeInt(0);
            _leos.writeInt(0);
            _leos.writeInt(0);
            _leos.writeInt(0);
            _leos.writeInt(0);
            _leos.writeInt(indexData[0].length * 4 + 50);
            _leos.writeLEInt(1000);
            _leos.writeLEInt(layerType);

            if (extents[0] == 90f && extents[1] == 180f && extents[2] == -90f
                    && extents[3] == -180f) {

                // Whoa! not set from defaults correctly!
                // use old, hardcoded way.
                _leos.writeLEDouble(-180.0); // Hard-coding extents.
                _leos.writeLEDouble(-90.0); // When viewed through
                // ArcView, this will
                _leos.writeLEDouble(180.0); // cause window to zoom to
                // world extents
                _leos.writeLEDouble(90.0); // instead of layer
                // extents.

            } else {
                _leos.writeLEDouble((float) extents[1]);
                _leos.writeLEDouble((float) extents[0]);
                _leos.writeLEDouble((float) extents[3]);
                _leos.writeLEDouble((float) extents[2]);
            }

            _leos.writeLEDouble(0.0);
            _leos.writeLEDouble(0.0);
            _leos.writeLEDouble(0.0);
            _leos.writeLEDouble(0.0);

            for (int i = 0; i <= indexData[0].length - 1; i++) {
                _leos.writeInt(indexData[0][i]);
                _leos.writeInt(indexData[1][i]);
            }
            _leos.flush();
            _leos.close();
        } catch (Exception e) {
            System.out.println("exception=" + e.toString());
            return false;
        }
        return true;
    }

}