package org.libtiff.jai.codecimpl;

import java.awt.Rectangle;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFField;
import org.libtiff.jai.codec.XTIFFTileCodec;
import org.libtiff.jai.codec.XTIFFTileCodecImpl;
import org.libtiff.jai.util.JaiI18N;

import com.sun.media.jai.codecimpl.TIFFLZWDecoder;

/**
 * This codec encapsulates all the logic for the TIFF "lzw" decoding codec
 * algorithm.
 */
public class XTIFFLZWTileCodec extends XTIFFTileCodecImpl {

    private TIFFLZWDecoder lzwDecoder = null;

    // LZW compression related variable
    int predictor;
    int samplesPerPixel;

    /**
     * Public constructor
     */
    public XTIFFLZWTileCodec() {}

    /**
     * Creation method
     */
    public XTIFFTileCodec create() {
        return new XTIFFLZWTileCodec();
    }

    // public boolean canEncode() {return true;}

    /**
     * Registration method
     */
    public void register() {
        register(XTIFF.COMPRESSION_LZW);
    }

    /**
     * The initialization method particular to LZW decoding.
     */
    public void initializeDecoding() {
        // Get the number of samples per pixel
        XTIFFField sfield = directory.getField(XTIFF.TIFFTAG_SAMPLES_PER_PIXEL);
        if (sfield == null) {
            samplesPerPixel = 1;
        } else {
            samplesPerPixel = (int) sfield.getAsLong(0);
        }
        XTIFFField predictorField = directory.getField(XTIFF.TIFFTAG_PREDICTOR);

        if (predictorField == null) {
            predictor = 1;
        } else {
            predictor = predictorField.getAsInt(0);
            if (predictor != 1 && predictor != 2) {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder16"));
            }
            if (predictor == 2 && bitsPerSample[0] != 8) {
                throw new RuntimeException(bitsPerSample[0]
                        + JaiI18N.getString("XTIFFImageDecoder17"));
            }
        }
        lzwDecoder = new TIFFLZWDecoder(tileWidth, predictor, samplesPerPixel);
    }

    /**
     * Decode a rectangle of pixels
     */
    public void decodeTilePixels(byte[] input, Rectangle newRect, byte[] bdata) {
        lzwDecoder.decode(input, bdata, newRect.height);
    }

    /**
     * Decode a rectangle of pixels
     */
    public void decodeTilePixels(byte[] input, Rectangle newRect, short[] sdata) {

        // Since unitsInThisTile is the number of shorts,
        // but we do our decompression in terms of bytes, we
        // need to multiply unitsInThisTile by 2 in order to
        // figure out how many bytes we'll get after
        // decompression.
        byte byteArray[] = new byte[unitsInThisTile * 2];
        lzwDecoder.decode(input, byteArray, newRect.height);
        unpackShorts(byteArray, sdata, unitsInThisTile);
    }
}
