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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/InputReader.java,v $
// $RCSfile: InputReader.java,v $
// $Revision: 1.4 $
// $Date: 2007/06/21 21:39:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.io;

import java.io.EOFException;
import java.io.IOException;

/**
 * The InputReader is an interface that isolates the data file source
 * type from the BinaryFile. Represents an InputStream setup, and
 * basic read functions.
 */
public interface InputReader {

    /**
     * Get the name of the source.
     */
    public String getName();

    /**
     * Skip over n bytes in the input file
     * 
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped. annoying, isn't it?
     * @exception IOException Any IO errors that occur in skipping
     *            bytes in the underlying file
     */
    public long skipBytes(long n) throws IOException;

    /**
     * Get the index of the next character to be read
     * 
     * @return the index
     * @exception IOException Any IO errors that occur in accessing
     *            the underlying file
     */
    public long getFilePointer() throws IOException;

    /**
     * Set the index of the next character to be read.
     * 
     * @param pos the position to seek to.
     * @exception IOException Any IO Errors that occur in seeking the
     *            underlying file.
     */
    public void seek(long pos) throws IOException;

    /**
     * Return the total byte length of the source. May not be accurate
     * for StreamInputReaders.
     * 
     * @return the number of bytes remaining to be read (counted in
     *         bytes)
     * @exception IOException Any IO errors encountered in accessing
     *            the file
     */
    public long length() throws IOException;

    /**
     * Return how many bytes left to be read in the file.
     * 
     * @return the number of bytes remaining to be read (counted in
     *         bytes)
     * @exception IOException Any IO errors encountered in accessing
     *            the file
     */
    public long available() throws IOException;

    /**
     * Closes the underlying file.
     * 
     * @exception IOException Any IO errors encountered in accessing
     *            the file
     */
    public void close() throws IOException;

    /**
     * Read from the file.
     * 
     * @return one byte from the file. -1 for EOF
     * @exception IOException Any IO errors encountered in reading
     *            from the file
     */
    public int read() throws IOException;

    /**
     * Read from the file
     * 
     * @param b The byte array to read into
     * @param off the first array position to read into
     * @param len the number of bytes to read
     * @return the number of bytes read
     * @exception IOException Any IO errors encountered in reading
     *            from the file
     */
    public int read(byte b[], int off, int len) throws IOException;

    /**
     * Read from the file.
     * 
     * @param b the byte array to read into. Equivalent to
     *        <code>read(b, 0, b.length)</code>
     * @return the number of bytes read
     * @exception IOException Any IO errors encountered in reading
     *            from the file
     * @see java.io.RandomAccessFile#read(byte[])
     */
    public int read(byte b[]) throws IOException;

    /**
     * Read from the file.
     * 
     * @param howmany the number of bytes to read
     * @param allowless if we can return fewer bytes than requested
     * @return the array of bytes read.
     * @exception FormatException Any IO Exceptions, plus an
     *            end-of-file encountered after reading some, but now
     *            enough, bytes when allowless was <code>false</code>
     * @exception EOFException Encountered an end-of-file while
     *            allowless was <code>false</code>, but NO bytes
     *            had been read.
     */
    public byte[] readBytes(int howmany, boolean allowless)
            throws EOFException, FormatException;

}