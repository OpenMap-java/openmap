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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/InputStreamSplitter.java,v $
// $RCSfile: InputStreamSplitter.java,v $
// $Revision: 1.4 $
// $Date: 2007/06/21 21:39:02 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The <code>InputStreamSplitter</code> is a
 * <code>FilterInputStream</code> that reads and returns data from
 * an input stream, while also sending the data to an output stream.
 * Thus, the <code>InputStreamSplitter</code> can provide a handy
 * sniffing/logging mechanism. In addition, the output stream could be
 * coupled with a <code>PipedInputStream</code> to create two input
 * sources out of a single source.
 */
public class InputStreamSplitter extends FilterInputStream {

    /**
     * The OutputStream to route the data to.
     */
    protected OutputStream out;

    /**
     * Creates an <code>InputStreamSplitter</code>.
     * 
     * @param in the underlying input stream
     * @param out the output stream
     */
    public InputStreamSplitter(InputStream in, OutputStream out) {
        super(in);
        this.out = out;
    }

    /**
     * Reads the next byte of data from the input stream and writes it
     * to the output stream. The value byte is returned as an
     * <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of
     * the stream has been reached, the value <code>-1</code> is
     * returned. This method blocks until input data is available, the
     * end of the stream is detected, or an exception is thrown.
     * 
     * @return the next byte of data, or <code>-1</code> if the end
     *         of the stream is reached.
     * @exception IOException if an I/O error occurs.
     */
    public int read() throws IOException {
        final int i = super.read();
        if (i >= 0)
            out.write(i);
        return i;
    }

    /**
     * Reads up to <code>b.length</code> bytes of data from this
     * input stream into an array of bytes, and writes those bytes to
     * the output stream. This method blocks until some input is
     * available.
     * 
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the
     *         end of the stream has been reached.
     * @exception IOException if an I/O error occurs.
     * @see #read(byte[], int, int)
     */
    public int read(byte b[]) throws IOException {
        final int i = super.read(b);
        if (i >= 0)
            out.write(b, 0, i);
        return i;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input
     * stream into an array of bytes, and writes those bytes to the
     * output stream. This method blocks until some input is
     * available.
     * 
     * @param b the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the
     *         end of the stream has been reached.
     * @exception IOException if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException {
        final int i = super.read(b, off, len);
        if (i >= 0)
            out.write(b, off, i);
        return i;
    }

    /**
     * Closes the input and output streams and releases any system
     * resources associated with those streams.
     * 
     * @exception IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        super.close();
        out.close();
    }

    /**
     * Flushes the output stream.
     * 
     * @exception IOException if an I/O error occurs.
     */
    public void flush() throws IOException {
        out.flush();
    }
}