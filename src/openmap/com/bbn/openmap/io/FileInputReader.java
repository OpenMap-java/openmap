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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/FileInputReader.java,v $
// $RCSfile: FileInputReader.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.io;

import com.bbn.openmap.util.Debug;
import java.io.*;

/**
 * This class wraps a java.io.RandomAccessFile to allow us to choose
 * the byte-order of the underlying file.  It has a user-settable
 * byte-ordering, which is then used to properly implement the various
 * multibyte reading members.  Used for reading local files.
 *
 * @see java.io.RandomAccessFile 
 * @see com.bbn.openmap.io.InputReader 
 * @see com.bbn.openmap.io.BinaryFile
 */
public class FileInputReader implements InputReader {
    /** The underlying file input */
    protected RandomAccessFile inputFile = null;
    protected String name = null;

    /**
     * Constructs a new BinaryFile with the specified file as the
     * input.  The default byte-order is LSB first.  Reads start at
     * the first byte of the file.
     *
     * @param f the file to be opened for reading
     * @exception IOException pass-through errors from opening a
     * RandomAccessFile with f
     * @see java.io.RandomAccessFile 
     */
    public FileInputReader(File f) throws IOException {
	if (Debug.debugging("binaryfile")) {
	    Debug.output("FileInputReader created from " + f.getAbsolutePath());
	}
	init(f);
    }

    /**
     * Constructs a new BinaryFile with the specified file as the
     * input.  The default byte-order is LSB first.  Reads start at
     * the first byte of the file.
     *
     * @param f the path to the file to be opened for reading.
     * @exception IOException pass-through errors from opening a
     * RandomAccessFile with f
     * @see java.io.RandomAccessFile 
     */
    public FileInputReader(String f) throws IOException {
	if (Debug.debugging("binaryfile")) {
	    Debug.output("FileInputReader created from " + f);
	}
	init(new File(f));
    }

    /**
     * Get the file name.
     */
    public String getName() {
	return name;
    }

    /**
     * Initialize the underlying RandomAccessFile.  If it's found, but
     * there are too many files open, it calls
     * BinaryFile.closeClosable to try to get an open file pointer
     * from the system, and then tries again.
     * @param f a java.io.File
     * @throws IOException
     */
    protected void init(File f) throws IOException {
	try {
	    name = f.getName();
	    inputFile = new RandomAccessFile(f, "r");
	} catch (FileNotFoundException fnfe) {
	    throw fnfe;
	} catch (IOException i) {
	    if (f.canRead()) {
		BinaryFile.closeClosable();
		inputFile = new RandomAccessFile(f, "r");
	    } else {
		throw i;
	    }
	}	
    }

    /**
     * Get the RandomAccessFile, for quering purposes only.  Don't use
     * it to get data!
     */
    public RandomAccessFile getInputFile() {
	return inputFile;
    }

    /**
     * Skip over n bytes in the input file
     *
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped.  annoying, isn't it?
     * @exception IOException Any IO errors that occur in skipping bytes
     * in the underlying file
     */
    public long skipBytes(long n) throws IOException {
	return inputFile.skipBytes((int)n);
    }

    /**
     * Get the index of the next character to be read
     *
     * @return the index
     * @exception IOException Any IO errors that occur in accessing
     * the underlying file
     */
    public long getFilePointer() throws IOException {
	return inputFile.getFilePointer();
    }

    /**
     * Set the index of the next character to be read.
     *
     * @param pos the position to seek to.
     * @exception IOException Any IO Errors that occur in seeking the
     * underlying file.
     */
    public void seek(long pos) throws IOException {
	inputFile.seek(pos);
    }

    /**
     * Local files only.  Retrieve the length of the file being accessed.
     *
     * @return the length of the file (counted in bytes)
     * @exception IOException Any IO errors that occur in accessing the
     * underlying file.
     */
    public long length() throws IOException {
	return inputFile.length();
    }

    /**
     * Return how many bytes left to be read in the file.
     *
     * @return the number of bytes remaining to be read (counted in bytes)
     * @exception IOException Any IO errors encountered in accessing the file
     */
    public long available() throws IOException {
	return(length() - getFilePointer());
    }

    /** 
     * Closes the underlying file
     *
     * @exception IOException Any IO errors encountered in accessing the file
     */
    public void close() throws IOException {
	if (Debug.debugging("binaryfile")) {
	    Debug.output("FileInputReader.close()");
	}
	try {
	    if (inputFile != null) inputFile.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	inputFile = null;
    }

    /**
     * Read from the file.
     *
     * @return one byte from the file.  -1 for EOF
     * @exception IOException Any IO errors encountered in reading from the file
     */
    public int read() throws IOException {
	return inputFile.read();
    }

    /**
     * Read from the file
     *
     * @param b The byte array to read into
     * @param off the first array position to read into
     * @param len the number of bytes to read
     * @return the number of bytes read
     * @exception IOException Any IO errors encountered in reading from the file
     */
    public int read(byte b[], int off, int len) throws IOException {
	return inputFile.read(b, off, len);
    }

    /** 
     * Read from the file.
     *
     * @param b the byte array to read into.  Equivelent to 
     * <code>read(b, 0, b.length)</code>
     * @return the number of bytes read
     * @exception IOException Any IO errors encountered in reading from the file
     * @see java.io.RandomAccessFile#read(byte[])
     */
    public int read(byte b[]) throws IOException {
	return inputFile.read(b);
    }

    /** 
     * Read from the file.
     *
     * @param howmany the number of bytes to read
     * @param allowless if we can return fewer bytes than requested
     * @return the array of bytes read.
     * @exception FormatException Any IO Exceptions, plus an end-of-file
     * encountered after reading some, but now enough, bytes when allowless
     * was <code>false</code>
     * @exception EOFException Encountered an end-of-file while allowless 
     * was <code>false</code>, but NO bytes had been read.
     */
    public byte[] readBytes(int howmany, boolean allowless) 
	throws EOFException, FormatException {

	byte foo[] = new byte[howmany];
	int gotsofar = 0;
	int err = 0;
	try {
	    while (gotsofar < howmany) {
		err = inputFile.read(foo, gotsofar, howmany - gotsofar);

		if (err == -1) {
		    if (allowless) {
			//return a smaller array, so the caller can tell how much
			//they really got
			byte retval[] = new byte[gotsofar];
			System.arraycopy(foo, 0, retval, 0, gotsofar);
			return retval;
		    } else { //some kind of failure...
			if (gotsofar > 0) {
			    throw new FormatException("EOF while reading data");
			} else {
			    throw new EOFException();
			}
		    }
		}
		gotsofar += err;
	    }
	} catch (IOException i) {
	    throw new FormatException("FileInputReader: readBytes IOException: " + i.getMessage());
	}
	return foo;
    }

}
