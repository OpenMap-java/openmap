/* 
 * <copyright>
 *  Copyright 2011 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

import com.bbn.openmap.omGraphics.OMGeometry;

/**
 * A set of convenience functions for deep copying data structures.
 * 
 * @author ddietrick
 */
public class DeepCopyUtil {

    public static double[] deepCopy(double[] source) {
        if (source == null) {
            return null;
        }
        double[] ds = new double[source.length];
        System.arraycopy(source, 0, ds, 0, source.length);
        return ds;
    }

    public static int[] deepCopy(int[] source) {
        if (source == null) {
            return null;
        }
        int[] ints = new int[source.length];
        System.arraycopy(source, 0, ints, 0, source.length);
        return ints;
    }

    public static boolean[] deepCopy(boolean[] source) {
        if (source == null) {
            return null;
        }
        boolean[] bools = new boolean[source.length];
        System.arraycopy(source, 0, bools, 0, source.length);
        return bools;
    }

    public static float[] deepCopy(float[] source) {
        if (source == null) {
            return null;
        }
        float[] floats = new float[source.length];
        System.arraycopy(source, 0, floats, 0, source.length);
        return floats;
    }

    public static char[] deepCopy(char[] source) {
        if (source == null) {
            return null;
        }
        char[] chars = new char[source.length];
        System.arraycopy(source, 0, chars, 0, source.length);
        return chars;
    }

    public static short[] deepCopy(short[] source) {
        if (source == null) {
            return null;
        }
        short[] shorts = new short[source.length];
        System.arraycopy(source, 0, shorts, 0, source.length);
        return shorts;
    }

    public static long[] deepCopy(long[] source) {
        if (source == null) {
            return null;
        }
        long[] longs = new long[source.length];
        System.arraycopy(source, 0, longs, 0, source.length);
        return longs;
    }

    public static byte[] deepCopy(byte[] source) {
        if (source == null) {
            return null;
        }
        byte[] bytes = new byte[source.length];
        System.arraycopy(source, 0, bytes, 0, source.length);
        return bytes;
    }

    public static <T extends OMGeometry> T deepCopy(T source) {
        T list = (T) ComponentFactory.create(source.getClass().getName());
        list.restore(source);
        return list;
    }

    public static <T extends OMGeometry> T[] deepCopy(T[] source) {
        if (source == null) {
            return null;
        }

        // This is a shallow copy, clone objects are same as source objects
        T[] clone = source.clone();
        
        // JDK 1.6 required
        //T[] clone = Arrays.copyOfRange(source, 0, source.length);

        for (int i = 0; i < source.length; i++) {
            T subclone = null;
            subclone = (T) ComponentFactory.create(source[i].getClass().getName());
            if (subclone != null) {
                subclone.restore(source[i]);
            }
            clone[i] = subclone;
        }

        return clone;
    }

    public static double[][] deepCopy(double[][] source) {
        if (source == null) {
            return null;
        }

        double[][] ret = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new double[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static boolean[][] deepCopy(boolean[][] source) {
        if (source == null) {
            return null;
        }

        boolean[][] ret = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new boolean[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static byte[][] deepCopy(byte[][] source) {
        if (source == null) {
            return null;
        }

        byte[][] ret = new byte[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new byte[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static char[][] deepCopy(char[][] source) {
        if (source == null) {
            return null;
        }

        char[][] ret = new char[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new char[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static int[][] deepCopy(int[][] source) {
        if (source == null) {
            return null;
        }

        int[][] ret = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new int[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static float[][] deepCopy(float[][] source) {
        if (source == null) {
            return null;
        }

        float[][] ret = new float[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new float[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static long[][] deepCopy(long[][] source) {
        if (source == null) {
            return null;
        }

        long[][] ret = new long[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new long[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }

    public static short[][] deepCopy(short[][] source) {
        if (source == null) {
            return null;
        }

        short[][] ret = new short[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                ret[i] = new short[source[i].length];
                System.arraycopy(source[0], 0, ret[i], 0, source[i].length);
            }
        }
        return ret;
    }
}
