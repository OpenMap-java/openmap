package org.libtiff.jai.codec;

/*
 * XTIFF: eXtensible TIFF libraries for JAI.
 * 
 * The contents of this file are subject to the  JAVA ADVANCED IMAGING
 * SAMPLE INPUT-OUTPUT CODECS AND WIDGET HANDLING SOURCE CODE  License
 * Version 1.0 (the "License"); You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.sun.com/software/imaging/JAI/index.html
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License. 
 *
 * The Original Code is JAVA ADVANCED IMAGING SAMPLE INPUT-OUTPUT CODECS
 * AND WIDGET HANDLING SOURCE CODE. 
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 * Portions created by: Niles Ritter 
 * are Copyright (C): Niles Ritter, GeoTIFF.org, 1999,2000.
 * All Rights Reserved.
 * Contributor(s): Niles Ritter
 */

import java.io.IOException;

import com.sun.media.jai.codec.SeekableStream;

/**
 * A class representing the factory for constructing a XTIFFDirectory, and the
 * corresponding XTIFFFields. If you are creating extensions to those classes,
 * extend this class as well. See the GeoTIFF package for an example of how to
 * do this.
 * 
 * @see org.libtiff.jai.TIFFDescriptor
 * @see XTIFFField
 * @see XTIFFDirectory
 */
public class XTIFFFactory extends Object implements java.io.Serializable {
    /**
     * Default constructor
     */
    public XTIFFFactory() {}

    /**
     * Constructs an XTIFFDirectoryFactory from a SeekableStream. The directory
     * parameter specifies which directory to read from the linked list present
     * in the stream; directory 0 is normally read but it is possible to store
     * multiple images in a single TIFF file by maintaing multiple directories.
     * 
     * @param stream a SeekableStream to read from.
     * @param directory the index of the directory to read.
     */
    public XTIFFDirectory createDirectory(SeekableStream stream, int directory)
            throws IOException {
        return new XTIFFDirectory(stream, directory);
    }

    /**
     * Constructs a XTIFFDirectory by reading a SeekableStream. The ifd_offset
     * parameter specifies the stream offset from which to begin reading; this
     * mechanism is sometimes used to store private IFDs within a TIFF file that
     * are not part of the normal sequence of IFDs.
     * 
     * @param stream a SeekableStream to read from.
     * @param ifd_offset the long byte offset of the directory.
     */
    public XTIFFDirectory createDirectory(SeekableStream stream, long ifd_offset)
            throws IOException {
        return new XTIFFDirectory(stream, ifd_offset);
    }

    /**
     * Constructs an empty XTIFFDirectory for encoding
     */
    public XTIFFDirectory createDirectory() {
        return new XTIFFDirectory();
    }

    /**
     * Constructs an XTIFFField from values
     * 
     * @param tag the TIFF tag listed in XTIFF
     * @param type the TIFF field type listed in XTIFFField
     * @param count the number of values in array obj
     * @param obj the array of values
     * @see XTIFFField
     */
    public XTIFFField createField(int tag, int type, int count, Object obj) {
        return new XTIFFField(tag, type, count, obj);
    }
}
