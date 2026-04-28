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

/**
 * An extension of <code>TIFFEncodeParam</code> for encoding images in 
 * the TIFF format using the XTIFF machinery. The encode object creates
 * an empty XTIFFDirectory, which the user may may access and add 
 * whatever XTIFFFields they need for annotation, compression, etc.
 * It shouldn't be necessary to extend this class further, as all
 * parameters will be passed through the XTIFFDirectory.
 */

import com.sun.media.jai.codec.TIFFEncodeParam;

public class XTIFFEncodeParam extends TIFFEncodeParam {

    /* inherited: compression, tiled */
    private int _compression;
    private boolean _isTiled;

    /** For XTIFF everything is stored in the directory */
    private XTIFFDirectory directory;

    /**
     * Promotes an XTIFFEncodeParam object from simpler one
     */
    public XTIFFEncodeParam(TIFFEncodeParam param) {
        initialize();
        if (param == null)
            return;
        setCompression(param.getCompression());
        setWriteTiled(param.getWriteTiled());
    }

    /**
     * Constructs an XTIFFEncodeParam object with default values for parameters.
     */
    public XTIFFEncodeParam() {
        initialize();
    }

    /**
     * Initializes an XTIFFEncodeParam with default values for parameters.
     */
    public void initialize() {
        directory = XTIFFDirectory.create();
        setCompression(COMPRESSION_NONE);
        setWriteTiled(false);
    }

    public XTIFFDirectory getDirectory() {
        return directory;
    }

    /**
     * Specifies the type of compression to be used. The compression type
     * specified will be honored only if it is compatible with the image being
     * written out.
     * 
     * @param compression The compression type.
     */
    public void setCompression(int compression) {
        directory.setCompression(compression);
        _compression = compression;
    }

    /**
     * Specifies the type of compression to be used. The compression type
     * specified will be honored only if it is compatible with the image being
     * written out.
     * 
     */
    public int getCompression() {
        return _compression;
    }

    /**
     * If set, the data is in tiled format, instead of in strips.
     */
    public boolean getWriteTiled() {
        return _isTiled;
    }

    /**
     * If set, the data will be written out in tiled format, instead of in
     * strips.
     * 
     * @param writeTiled Specifies whether the image data should be wriiten out
     *        in tiled format.
     */
    public void setWriteTiled(boolean writeTiled) {
        directory.setIsTiled(writeTiled);
        _isTiled = writeTiled;
    }
}
