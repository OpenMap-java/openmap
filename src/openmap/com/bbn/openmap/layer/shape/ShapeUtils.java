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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/ShapeUtils.java,v $
// $RCSfile: ShapeUtils.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.shape;

import java.io.*;

import com.bbn.openmap.dataAccess.shape.ShapeConstants;
import com.bbn.openmap.io.BinaryBufferedFile;

/**
 * Utilities for reading and writing the components of shape files.
 *
 * @author Tom Mitchell <tmitchell@bbn.com>
 * @version $Revision: 1.1.1.1 $ $Date: 2003/02/14 21:35:48 $
 */
public class ShapeUtils implements ShapeConstants {

//     /** A buffer for reading/writing big endian ints. */
//     private static byte beiBuffer[] = new byte[4];

    /** A buffer for reading/writing little endian ints. */
    private static byte leiBuffer[] = new byte[4];

//     /** A buffer for reading/writing little endian doubles. */
//     private static byte ledBuffer[] = new byte[8];

    /**
     * Reads a big endian integer.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     */
    public static int readBEInt(byte[] b, int off) {
	return (((b[off + 0] & 0xff) << 24) |
		((b[off + 1] & 0xff) << 16) |
		((b[off + 2] & 0xff) <<  8) |
		((b[off + 3] & 0xff)      ));
    }

    /**
     * Convenience routine to read a big endian integer.  Reads a
     * big endian int at offset zero of the given buffer.
     *
     * @param b the raw data buffer
     * @return the int read from the buffer at offset zero
     */
//     public static int readBEInt(byte[] b) {
// 	return readBEInt(b, 0);
//     }

//     public static int readBEInt(InputStream is) throws IOException {
// 	int result = is.read(beiBuffer, 0, 4);
// 	if (result < 4) {
// 	    throw new EOFException();
// 	} else {
// 	    return readBEInt(beiBuffer, 0);
// 	}
//     }

//     public static int readBEInt(RandomAccessFile in) throws IOException {
// 	int result = in.read(beiBuffer, 0, 4);
// 	if (result < 4) {
// 	    throw new EOFException();
// 	} else {
// 	    return readBEInt(beiBuffer, 0);
// 	}
//     }

    /**
     * Reads a little endian integer.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     */
    public static int readLEInt(byte[] b, int off) {
	return (((b[off + 3] & 0xff) << 24) |
		((b[off + 2] & 0xff) << 16) |
		((b[off + 1] & 0xff) <<  8) |
		((b[off + 0] & 0xff)      ));
    }

    /**
     * Convenience routine to read a little endian integer.
     * Reads a little endian int at offset zero of the given buffer.
     *
     * @param b the raw data buffer
     * @return the int read from the buffer at offset zero
     */
//     public static int readLEInt(byte[] b) {
// 	return readLEInt(b, 0);
//     }

//     public static int readLEInt(InputStream is) throws IOException {
// 	int result = is.read(leiBuffer, 0, 4);
// 	if (result < 4) {
// 	    throw new EOFException();
// 	} else {
// 	    return readLEInt(leiBuffer, 0);
// 	}
//     }

    /**
     * Reads a little endian int from the current location of the
     * given file.  Synchronized for thread-safe access to leiBuffer.
     *
     * @param in an input file
     * @return the int read from the file
     */
//      public static synchronized int readLEInt(RandomAccessFile in) 
     public static synchronized int readLEInt(BinaryBufferedFile in) 
       throws IOException {
	int result = in.read(leiBuffer, 0, 4);
	if (result < 4) {
	    throw new EOFException();
	} else {
	    return readLEInt(leiBuffer, 0);
	}
    }

    /**
     * Reads a little endian 8 byte integer.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the long resides
     * @return the long read from the buffer at the offset location
     */
    public static long readLELong(byte[] b, int off) {
	return (((b[off + 0] & 0xffL)      ) |
		((b[off + 1] & 0xffL) <<  8) |
		((b[off + 2] & 0xffL) << 16) |
		((b[off + 3] & 0xffL) << 24) |
		((b[off + 4] & 0xffL) << 32) |
		((b[off + 5] & 0xffL) << 40) |
		((b[off + 6] & 0xffL) << 48) |
		((b[off + 7] & 0xffL) << 56));
    }

    /**
     * Convenience routine to read a little endian 8 byte integer.
     * Reads a little endian long at offset zero of the given buffer.
     *
     * @param b the raw data buffer
     * @return the long read from the buffer at offset zero
     */
//     public static long readLELong(byte[] b) {
// 	return readLELong(b, 0);
//     }

//     public static long readLELong(InputStream is) throws IOException {
// 	int result = is.read(leiBuffer, 0, 8);
// 	if (result < 8) {
// 	    throw new EOFException();
// 	} else {
// 	    return readLELong(beiBuffer, 0);
// 	}
//     }

    /**
     * Reads a little endian double.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the double resides
     * @return the double read from the buffer at the offset location
     */
    public static double readLEDouble(byte[] b, int off) {
	double result = Double.longBitsToDouble(readLELong(b, off));
 	return result;
    }

//     public static double readLEDouble(byte[] b) {
// 	return readLEDouble(b, 0);
//     }

//     public static double readLEDouble(InputStream is) throws IOException {
// 	double result = Double.longBitsToDouble(readLELong(is));
// 	return result;
//     }

    /**
     * Reads a point record. A point record is a double representing the
     * x value and a double representing a y value.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the int resides
     * @return the point read from the buffer at the offset location
     */
    public static ESRIPoint readPoint(byte[] b, int off) {
	ESRIPoint point = new ESRIPoint();
	point.x = readLEDouble(b, off);
	point.y = readLEDouble(b, off + 8);
	return point;
    }

//     public static ESRIPoint readPoint(byte[] b) {
// 	return readPoint(b, 0);
//     }

//     public static ESRIPoint readPoint(InputStream is) throws IOException {
// 	ESRIPoint point = new ESRIPoint();
// 	point.x = readLEDouble(is);
// 	point.y = readLEDouble(is);
// 	return point;
//     }

    /**
     * Reads a bounding box record.  A bounding box is four double
     * representing, in order, xmin, ymin, xmax, ymax.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the int resides
     * @return the point read from the buffer at the offset location
     */
    public static ESRIBoundingBox readBox(byte[] b, int off)
    {
	ESRIBoundingBox bb = new ESRIBoundingBox();
	bb.min = readPoint(b, off);
	bb.max = readPoint(b, off + 16);
	return bb;
    }

//     public static ESRIBoundingBox readBox(byte[] b) {
// 	return readBox(b, 0);
//     }

//     public static ESRIBoundingBox readBox(InputStream is) throws IOException {
// 	ESRIBoundingBox bb = new ESRIBoundingBox();
// 	bb.min = readPoint(is);
// 	bb.max = readPoint(is);
// 	return bb;
//     }

    /**
     * Writes the given integer to the given buffer at the given location
     * in big endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the integer to write
     * @return the number of bytes written
     */
    public static int writeBEInt(byte[] b, int off, int val)
    {
	b[off + 0] = (byte) ((val >> 24) & 0xff);
	b[off + 1] = (byte) ((val >> 16) & 0xff);
	b[off + 2] = (byte) ((val >>  8) & 0xff);
	b[off + 3] = (byte) ((val      ) & 0xff);
	return 4;
    }

    /**
     * Writes the given integer to the given buffer at the given location
     * in little endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the integer to write
     * @return the number of bytes written
     */
    public static int writeLEInt(byte[] b, int off, int val)
    {
	b[off + 0] = (byte) ((val      ) & 0xff);
	b[off + 1] = (byte) ((val >>  8) & 0xff);
	b[off + 2] = (byte) ((val >> 16) & 0xff);
	b[off + 3] = (byte) ((val >> 24) & 0xff);
	return 4;
    }

    /**
     * Writes the given long to the given buffer at the given location
     * in little endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the long to write
     * @return the number of bytes written
     */
    public static int writeLELong(byte[]  b, int off, long val)
    {
	b[off + 0] = (byte) ((val      ) & 0xff);
	b[off + 1] = (byte) ((val >>  8) & 0xff);
	b[off + 2] = (byte) ((val >> 16) & 0xff);
	b[off + 3] = (byte) ((val >> 24) & 0xff);
	b[off + 4] = (byte) ((val >> 32) & 0xff);
	b[off + 5] = (byte) ((val >> 40) & 0xff);
	b[off + 6] = (byte) ((val >> 48) & 0xff);
	b[off + 7] = (byte) ((val >> 56) & 0xff);
	return 8;
    }

    /**
     * Writes the given double to the given buffer at the given location
     * in little endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the double to write
     * @return the number of bytes written
     */
    public static int writeLEDouble(byte[] b, int off, double val)
    {
	return writeLELong(b, off, Double.doubleToLongBits(val));
    }

    /**
     * Writes the given point to the given buffer at the given location.
     * The point is written as a double representing x followed by a
     * double representing y.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the point to write
     * @return the number of bytes written
     */
    public static int writePoint(byte[] b, int off, ESRIPoint point)
    {
	int nBytes = writeLEDouble(b, off, point.x);
	nBytes += writeLEDouble(b, off + nBytes, point.y);
	return nBytes;
    }

    /**
     * Writes the given bounding box  to the given buffer at the
     * given location.  The bounding box is written as four doubles
     * representing, in order, xmin, ymin, xmax, ymax.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the bounding box to write
     * @return the number of bytes written
     */
    public static int writeBox(byte[] b, int off, ESRIBoundingBox box)
    {
	int nBytes = writePoint(b, off, box.min);
	nBytes += writePoint(b, off + nBytes, box.max);
	return nBytes;
    }
}
