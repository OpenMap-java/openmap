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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/GridData.java,v $
// $RCSfile: GridData.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

/**
 * The GridData interface is a wrapper class for data stored and used
 * in an OMGrid object. It holds a two-dimensional array of data. They
 * type of array is determined my the type of GridData used.
 */
public interface GridData {

    public Object get(int x, int y);

    public int getNumColumns();

    public int getNumRows();

    public void setMajor(boolean value);

    public boolean getMajor();
    
    public GridData deepCopy();

    public interface Boolean extends GridData {
        public boolean getBooleanValue(int x, int y);

        public boolean[][] getData();
    }

    public interface Byte extends GridData {
        public byte getByteValue(int x, int y);

        public byte[][] getData();
    }

    public interface Char extends GridData {
        public char getCharValue(int x, int y);

        public char[][] getData();
    }

    public interface Short extends GridData {
        public short getShortValue(int x, int y);

        public short[][] getData();
    }

    public interface Int extends GridData {
        public int getIntValue(int x, int y);

        public int[][] getData();
    }

    public interface Float extends GridData {
        public float getFloatValue(int x, int y);

        public float[][] getData();
    }

    public interface Double extends GridData {
        public double getDoubleValue(int x, int y);

        public double[][] getData();
    }
}