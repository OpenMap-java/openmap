package org.libtiff.jai.codecimpl;

import java.awt.Rectangle;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFTileCodec;
import org.libtiff.jai.codec.XTIFFTileCodecImpl;
import org.libtiff.jai.util.JaiI18N;

/**
 * This codec encapsulates all the logic for the default TIFF "packbits"
 * bit-packing codec algorithm.
 */
public class XTIFFPackTileCodec extends XTIFFTileCodecImpl {

    public XTIFFPackTileCodec() {}

    public XTIFFTileCodec create() {
        return new XTIFFPackTileCodec();
    }

    public boolean canEncode() {
        return false;
    }

    public void register() {
        register(XTIFF.COMPRESSION_PACKBITS);
    }

    /**
     * encode the tile into bpixels and return the byte size (uncompressed
     * packing algorithm). The padding has already been done, so we may safely
     * assume that pixels is exactly rows by cols by numBands ints.
     */

    public int encodeTilePixels(int[] pixels, Rectangle newRect, byte[] bpixels) {
        return 0;
    }

    /**
     * Decode a rectangle of pixels
     */
    public void decodeTilePixels(byte[] input, Rectangle newRect, byte[] bdata) {

        if (bitsPerSample[0] == 8) {
            decodePackbits(input, unitsInThisTile, bdata);
        } else if (bitsPerSample[0] == 4) {
            // Since the decompressed data will still be packed
            // 2 pixels into 1 byte, calculate bytesInThisTile
            int bytesInThisTile;
            if ((newRect.width % 8) == 0) {
                bytesInThisTile = (newRect.width / 2) * newRect.height;
            } else {
                bytesInThisTile = (newRect.width / 2 + 1) * newRect.height;
            }

            decodePackbits(input, bytesInThisTile, bdata);
        }
    }

    /**
     * Decode a rectangle of pixels
     */
    public void decodeTilePixels(byte[] input, Rectangle newRect, short[] sdata) {
        int bytesInThisTile = unitsInThisTile * 2;
        byte byteArray[] = new byte[bytesInThisTile];
        decodePackbits(input, bytesInThisTile, byteArray);
        unpackShorts(byteArray, sdata, unitsInThisTile);
    }

    // Uncompress packbits compressed image data.
    private byte[] decodePackbits(byte data[], int arraySize, byte[] dst) {

        if (dst == null) {
            dst = new byte[arraySize];
        }

        int srcCount = 0, dstCount = 0;
        byte repeat, b;

        try {

            while (dstCount < arraySize) {

                b = data[srcCount++];

                if (b >= 0 && b <= 127) {

                    // literal run packet
                    for (int i = 0; i < (b + 1); i++) {
                        dst[dstCount++] = data[srcCount++];
                    }

                } else if (b <= -1 && b >= -127) {

                    // 2 byte encoded run packet
                    repeat = data[srcCount++];
                    for (int i = 0; i < (-b + 1); i++) {
                        dst[dstCount++] = repeat;
                    }

                } else {
                    // no-op packet. Do nothing
                    srcCount++;
                }
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException ae) {
            throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder10"));
        }

        return dst;
    }
}
