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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/grid/OMGridData.java,v
// $
// $RCSfile: OMGridData.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:18 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.grid;

import com.bbn.openmap.omGraphics.OMGrid;
import com.bbn.openmap.util.DeepCopyUtil;

public abstract class OMGridData
        implements GridData {

    /**
     * Keep track of which dimension different parts of the double array
     * represent. COLUMN_MAJOR is the default, meaning that the first dimension
     * of the array represents the vertical location in the array, and the
     * second is the horizontal location in the array.
     */
    protected boolean major = OMGrid.COLUMN_MAJOR;

    public void setMajor(boolean value) {
        major = value;
    }

    public boolean getMajor() {
        return major;
    }

    /**
     * Boolean is a GridData object that contains booleans.
     */
    public static class Boolean
            extends OMGridData
            implements GridData.Boolean {

        protected boolean[][] data;

        public Boolean(boolean[][] d) {
            setData(d);
        }

        public void setData(boolean[][] d) {
            data = d;
        }

        public boolean[][] getData() {
            return data;
        }

        public Object get(int x, int y) {
            return new java.lang.Boolean(getBooleanValue(x, y));
        }

        public boolean getBooleanValue(int x, int y) {
            return data[x][y];
        }

        public int getNumColumns() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data.length;
            } else {
                return data[0].length;
            }
        }

        public int getNumRows() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data[0].length;
            } else {
                return data.length;
            }
        }

        public GridData deepCopy() {
            GridData copy = new OMGridData.Boolean(DeepCopyUtil.deepCopy(data));
            copy.setMajor(major);
            return copy;
        }
    }

    /**
     * Byte is a GridData object that contains bytes.
     */
    public static class Byte
            extends OMGridData
            implements GridData.Byte {

        protected byte[][] data;

        public Byte(byte[][] d) {
            setData(d);
        }

        public void setData(byte[][] d) {
            data = d;
        }

        public byte[][] getData() {
            return data;
        }

        public Object get(int x, int y) {
            return new java.lang.Byte(getByteValue(x, y));
        }

        public byte getByteValue(int x, int y) {
            return data[x][y];
        }

        public int getNumColumns() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data.length;
            } else {
                return data[0].length;
            }
        }

        public int getNumRows() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data[0].length;
            } else {
                return data.length;
            }
        }

        public GridData deepCopy() {
            GridData copy = new OMGridData.Byte(DeepCopyUtil.deepCopy(data));
            copy.setMajor(major);
            return copy;
        }
    }

    /**
     * Char is a GridData object that contains chars.
     */
    public static class Char
            extends OMGridData
            implements GridData.Char {

        protected char[][] data;

        public Char(char[][] d) {
            setData(d);
        }

        public void setData(char[][] d) {
            data = d;
        }

        public char[][] getData() {
            return data;
        }

        public Object get(int x, int y) {
            return new Character(getCharValue(x, y));
        }

        public char getCharValue(int x, int y) {
            return data[x][y];
        }

        public int getNumColumns() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data.length;
            } else {
                return data[0].length;
            }
        }

        public int getNumRows() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data[0].length;
            } else {
                return data.length;
            }
        }

        public GridData deepCopy() {
            GridData copy = new OMGridData.Char(DeepCopyUtil.deepCopy(data));
            copy.setMajor(major);
            return copy;
        }
    }

    /**
     * Float is a GridData object that contains floats.
     */
    public static class Float
            extends OMGridData
            implements GridData.Float {

        protected float[][] data;

        public Float(float[][] d) {
            setData(d);
        }

        public void setData(float[][] d) {
            data = d;
        }

        public float[][] getData() {
            return data;
        }

        public Object get(int x, int y) {
            return new java.lang.Float(getFloatValue(x, y));
        }

        public float getFloatValue(int x, int y) {
            return data[x][y];
        }

        public int getNumColumns() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data.length;
            } else {
                return data[0].length;
            }
        }

        public int getNumRows() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data[0].length;
            } else {
                return data.length;
            }
        }

        public GridData deepCopy() {
            GridData copy = new OMGridData.Float(DeepCopyUtil.deepCopy(data));
            copy.setMajor(major);
            return copy;
        }
    }

    /**
     * Int is a GridData object that contains ints.
     */
    public static class Int
            extends OMGridData
            implements GridData.Int {

        protected int[][] data;

        public Int(int[][] d) {
            setData(d);
        }

        public void setData(int[][] d) {
            data = d;
        }

        public int[][] getData() {
            return data;
        }

        public Object get(int x, int y) {
            return new Integer(getIntValue(x, y));
        }

        public int getIntValue(int x, int y) {
            return data[x][y];
        }

        public int getNumColumns() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data.length;
            } else {
                return data[0].length;
            }
        }

        public int getNumRows() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data[0].length;
            } else {
                return data.length;
            }
        }

        public GridData deepCopy() {
            GridData copy = new OMGridData.Int(DeepCopyUtil.deepCopy(data));
            copy.setMajor(major);
            return copy;
        }
    }

    /**
     * Short is a GridData object that contains shorts.
     */
    public static class Short
            extends OMGridData
            implements GridData.Short {

        protected short[][] data;

        public Short(short[][] d) {
            setData(d);
        }

        public void setData(short[][] d) {
            data = d;
        }

        public short[][] getData() {
            return data;
        }

        public Object get(int x, int y) {
            return new java.lang.Short(getShortValue(x, y));
        }

        public short getShortValue(int x, int y) {
            return data[x][y];
        }

        public int getNumColumns() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data.length;
            } else {
                return data[0].length;
            }
        }

        public int getNumRows() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data[0].length;
            } else {
                return data.length;
            }
        }

        public GridData deepCopy() {
            GridData copy = new OMGridData.Short(DeepCopyUtil.deepCopy(data));
            copy.setMajor(major);
            return copy;
        }
    }

    /**
     * Double is a GridData object that contains doubles.
     */
    public static class Double
            extends OMGridData
            implements GridData.Double {

        protected double[][] data;

        public Double(double[][] d) {
            setData(d);
        }

        public void setData(double[][] d) {
            data = d;
        }

        public double[][] getData() {
            return data;
        }

        public Object get(int x, int y) {
            return new java.lang.Double(getDoubleValue(x, y));
        }

        public double getDoubleValue(int x, int y) {
            return data[x][y];
        }

        public int getNumColumns() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data.length;
            } else {
                return data[0].length;
            }
        }

        public int getNumRows() {
            if (major == OMGrid.COLUMN_MAJOR) {
                return data[0].length;
            } else {
                return data.length;
            }
        }

        public GridData deepCopy() {
            GridData copy = new OMGridData.Double(DeepCopyUtil.deepCopy(data));
            copy.setMajor(major);
            return copy;
        }
    }
}