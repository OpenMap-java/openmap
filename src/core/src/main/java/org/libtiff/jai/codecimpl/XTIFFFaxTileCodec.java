package org.libtiff.jai.codecimpl;

import java.awt.Rectangle;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFField;
import org.libtiff.jai.codec.XTIFFTileCodec;
import org.libtiff.jai.codec.XTIFFTileCodecImpl;

/**
 * This codec encapsulates all the logic for the TIFF "lzw" decoding codec
 * algorithm.
 */
public class XTIFFFaxTileCodec extends XTIFFTileCodecImpl {

    private XTIFFFaxDecoder decoder = null;

    // Fax compression related variables
    long tiffT4Options;
    long tiffT6Options;
    int fillOrder;

    /**
     * Public constructor
     */
    public XTIFFFaxTileCodec() {}

    /**
     * Creation method
     */
    public XTIFFTileCodec create() {
        return new XTIFFFaxTileCodec();
    }

    public boolean canEncode() {
        return false;
    }

    /**
     * Registration method
     */
    public void register() {
        register(XTIFF.COMPRESSION_FAX_G3_1D);
        register(XTIFF.COMPRESSION_FAX_G3_2D);
        register(XTIFF.COMPRESSION_FAX_G4_2D);
    }

    /**
     * The initialization method particular to Fax decoding.
     */
    public void initializeDecoding() {

        XTIFFField fillOrderField = directory.getField(XTIFF.TIFFTAG_FILL_ORDER);
        if (fillOrderField != null) {
            fillOrder = fillOrderField.getAsInt(0);
        } else {
            // Default Fill Order
            fillOrder = 1;
        }
        // Fax T.4 compression options
        if (compression == 3) {
            XTIFFField t4OptionsField = directory.getField(XTIFF.TIFFTAG_T4_OPTIONS);
            if (t4OptionsField != null) {
                tiffT4Options = t4OptionsField.getAsLong(0);
            } else {
                // Use default value
                tiffT4Options = 0;
            }
        }

        // Fax T.6 compression options
        if (compression == 4) {
            XTIFFField t6OptionsField = directory.getField(XTIFF.TIFFTAG_T6_OPTIONS);
            if (t6OptionsField != null) {
                tiffT6Options = t6OptionsField.getAsLong(0);
            } else {
                // Use default value
                tiffT6Options = 0;
            }
        }

        decoder = new XTIFFFaxDecoder(fillOrder, tileWidth, tileLength);

    }

    /**
     * Decode a rectangle of pixels
     */
    public void decodeTilePixels(byte[] input, Rectangle newRect, byte[] bdata) {
        if (compression == XTIFF.COMPRESSION_FAX_G3_1D) {
            decoder.decode1D(bdata, input, newRect.x, newRect.height);
        } else if (compression == XTIFF.COMPRESSION_FAX_G3_2D) {
            decoder.decode2D(bdata,
                    input,
                    newRect.x,
                    newRect.height,
                    tiffT4Options);
        } else if (compression == XTIFF.COMPRESSION_FAX_G4_2D) {
            decoder.decodeT6(bdata,
                    input,
                    newRect.x,
                    newRect.height,
                    tiffT6Options);
        }
    }

    /**
     * Decode a rectangle of pixels
     */
    public void decodeTilePixels(byte[] input, Rectangle newRect, short[] sdata) {
    // not used for fax.
    }
}
