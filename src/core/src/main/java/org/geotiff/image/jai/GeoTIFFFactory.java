/*
 * GeoTIFF extension to JAI.
 * 
 * Software distributed distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Permission granted to use and modify this code,
 * so long as this copyright notice appears in the derived code.
 *
 * Portions created by: Niles Ritter 
 * are Copyright (C): Niles Ritter, GeoTIFF.org, 1999,2000.
 * All Rights Reserved.
 * Contributor(s): Niles Ritter
 */

package org.geotiff.image.jai;

import java.io.IOException;

import org.libtiff.jai.codec.XTIFFDirectory;
import org.libtiff.jai.codec.XTIFFFactory;

import com.sun.media.jai.codec.SeekableStream;

/**
 * A factory object for a GeoTIFFDirectory and its corresponding XTIFFField
 * class.
 * 
 * @see XTIFFDirectory
 * @see XTIFFField
 */
public class GeoTIFFFactory extends XTIFFFactory implements
        java.io.Serializable {
    /**
     * Default constructor
     */
    public GeoTIFFFactory() {}

    /**
     * Constructs a TIFFDirectoryFactory from a SeekableStream. The directory
     * parameter specifies which directory to read from the linked list present
     * in the stream; directory 0 is normally read but it is possible to store
     * multiple images in a single TIFF file by maintaing multiple directories.
     * 
     * @param stream a SeekableStream to read from.
     * @param directory the index of the directory to read.
     */
    public XTIFFDirectory createDirectory(SeekableStream stream, int directory)
            throws IOException {
        return new GeoTIFFDirectory(stream, directory);
    }

    /**
     * Constructs a TIFFDirectory by reading a SeekableStream. The ifd_offset
     * parameter specifies the stream offset from which to begin reading; this
     * mechanism is sometimes used to store private IFDs within a TIFF file that
     * are not part of the normal sequence of IFDs.
     * 
     * @param stream a SeekableStream to read from.
     * @param ifd_offset the long byte offset of the directory.
     */
    public XTIFFDirectory createDirectory(SeekableStream stream, long ifd_offset)
            throws IOException {
        return new GeoTIFFDirectory(stream, ifd_offset);
    }

    /**
     * Constructs an empty TIFFDirectory for writing
     */
    public XTIFFDirectory createDirectory() {
        return new GeoTIFFDirectory();
    }
}
