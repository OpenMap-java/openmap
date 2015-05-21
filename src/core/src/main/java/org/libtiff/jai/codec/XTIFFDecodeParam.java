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

import com.sun.media.jai.codec.TIFFDecodeParam;

/**
 * An extension of <code>TIFFDecodeParam</code> for decoding images in the
 * TIFF format. In addition to the inherited properties, this object also
 * retains a reference to the parsed XTIFFDirectory that is constructed from the
 * file. For encoding, an empty XTIFFDirectory is constructed, into which the
 * user may provide any additional tags for controlling the contents of the
 * data. For example, if the image is to be compressed, some additional tags may
 * be needed to specify parameters for the particular compression scheme.
 * 
 * @see XTIFFDirectory
 * @see XTIFFDecodeParam
 */

public class XTIFFDecodeParam extends TIFFDecodeParam {

    protected XTIFFDirectory directory = null;

    /**
     * Promotes an XTIFFEncodeParam object from simpler one
     */
    public XTIFFDecodeParam(TIFFDecodeParam param) {
        if (param == null)
            return;
        setDecodePaletteAsShorts(param.getDecodePaletteAsShorts());
    }

    /** Constructs a default instance of <code>XTIFFDecodeParam</code>. */
    public XTIFFDecodeParam() {}

    /** returns the current XTIFFDirectory */
    public XTIFFDirectory getDirectory() {
        return directory;
    }

    /** sets the current XTIFFDirectory */
    public void setDirectory(XTIFFDirectory dir) {
        directory = dir;
    }
}
