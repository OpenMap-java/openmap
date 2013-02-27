package org.libtiff.jai.codecimpl;

import java.awt.Rectangle;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFTileCodec;
import org.libtiff.jai.codec.XTIFFTileCodecImpl;

/**
 * This codec encapsulates all the logic for the default TIFF "uncompressed"
 * bit-packing codec algorithm.
 */
public class XTIFFUncompTileCodec extends XTIFFTileCodecImpl {

    public XTIFFUncompTileCodec() {}

    public XTIFFTileCodec create() {
        return new XTIFFUncompTileCodec();
    }

    public boolean canEncode() {
        return true;
    }

    public void register() {
        register(XTIFF.COMPRESSION_NONE);
    }

    /**
     * encode the tile into bpixels and return the byte size (uncompressed
     * packing algorithm). The padding has already been done, so we may safely
     * assume that pixels is exactly rows by cols by numBands ints.
     */

    public int encodeTilePixels(int[] pixels, Rectangle rect, byte[] bpixels) {

        int rows = (int) rect.getHeight();
        int cols = (int) rect.getWidth();
        int index/*, remainder*/;
        int pixel = 0;
        int k = 0;
        int rowBytes = 0;

        switch (sampleSize[0]) {

        case 1:

            index = 0;
            rowBytes = (cols + 7) / 8;

            // For each of the rows in a tile
            for (int i = 0; i < rows; i++) {

                // Write out the number of pixels exactly
                // divisible by 8 (bits per byte)
                for (int j = 0; j < cols / 8; j++) {

                    pixel = (pixels[index++] << 7) | (pixels[index++] << 6)
                            | (pixels[index++] << 5) | (pixels[index++] << 4)
                            | (pixels[index++] << 3) | (pixels[index++] << 2)
                            | (pixels[index++] << 1) | pixels[index++];
                    bpixels[k++] = (byte) pixel;
                }

                // Write out the pixels remaining after division by 8
                if (cols % 8 > 0) {
                    pixel = 0;
                    for (int j = 0; j < cols % 8; j++) {
                        pixel |= (pixels[index++] << (7 - j));
                    }
                    bpixels[k++] = (byte) pixel;
                }
            } // row loop
            break;

        case 4:

            index = 0;
            rowBytes = (cols + 3) / 4;

            // For each of the rows in a strip
            for (int i = 0; i < rows; i++) {

                // Write out the number of pixels that will fit into an
                // even number of nibbles.
                for (int j = 0; j < cols / 2; j++) {
                    pixel = (pixels[index++] << 4) | pixels[index++];
                    bpixels[k++] = (byte) pixel;
                }

                // Last pixel for odd-length lines
                if ((cols % 2) != 0) {
                    pixel = pixels[index++] << 4;
                    bpixels[k++] = (byte) pixel;
                }
            }
            break;

        case 8:

            index = 0;
            rowBytes = cols * numBands;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols * numBands; j++) {
                    bpixels[k++] = (byte) pixels[index++];
                }
            }
            break;

        case 16:

            index = 0;
            rowBytes = cols * 2;
            int l = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    short value = (short) pixels[index++];
                    bpixels[l++] = (byte) ((value & 0xff00) >> 8);
                    bpixels[l++] = (byte) (value & 0x00ff);
                }
            }
            break;

        }
        return rows * rowBytes;
    }

    /**
     * Decompress data packed bytes into packed bytes
     */
    public void decodeTilePixels(byte[] input, Rectangle rect, byte[] bpixels) {
        for (int i = 0; i < unitsInThisTile; i++) {
            bpixels[i] = input[i];
        }
    }

    /**
     * Decompress data packed bytes into short
     */
    public void decodeTilePixels(byte[] input, Rectangle rect, short[] spixels) {
        unpackShorts(input, spixels, unitsInThisTile);
    }

}
