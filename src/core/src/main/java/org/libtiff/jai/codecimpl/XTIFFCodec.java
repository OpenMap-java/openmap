package org.libtiff.jai.codecimpl;

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

import java.awt.image.RenderedImage;
import java.io.OutputStream;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecodeParam;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;

/**
 * TIFFCodec is declared final so we can't extend it
 */
public class XTIFFCodec extends ImageCodec {

    public XTIFFCodec() {}

    static {
        // All built-in codec support should be here
        (new XTIFFUncompTileCodec()).register();
        (new XTIFFLZWTileCodec()).register();
        (new XTIFFPackTileCodec()).register();
        (new XTIFFFaxTileCodec()).register();
    }

    public String getFormatName() {
        return "tiff";
    }

    public Class getEncodeParamClass() {
        return TIFFEncodeParam.class;
    }

    public Class getDecodeParamClass() {
        return TIFFDecodeParam.class;
    }

    public boolean canEncodeImage(RenderedImage im, ImageEncodeParam param) {
        return true;
    }

    protected ImageEncoder createImageEncoder(OutputStream dst,
                                              ImageEncodeParam param) {
        return new XTIFFImageEncoder(dst, param);
    }

    protected ImageDecoder createImageDecoder(SeekableStream src,
                                              ImageDecodeParam param) {
        return new XTIFFImageDecoder(src, param);
    }

    public int getNumHeaderBytes() {
        return 4;
    }

    public boolean isFormatRecognized(byte[] header) {
        if ((header[0] == 0x49) && (header[1] == 0x49) && (header[2] == 0x2a)
                && (header[3] == 0x00)) {
            return true;
        }

        if ((header[0] == 0x4d) && (header[1] == 0x4d) && (header[2] == 0x00)
                && (header[3] == 0x2a)) {
            return true;
        }

        return false;
    }
}
