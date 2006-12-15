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

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.media.jai.RasterFactory;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFDecodeParam;
import org.libtiff.jai.codec.XTIFFDirectory;
import org.libtiff.jai.codec.XTIFFField;
import org.libtiff.jai.codec.XTIFFTileCodec;
import org.libtiff.jai.util.JaiI18N;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codecimpl.SimpleRenderedImage;

public class XTIFFImage extends SimpleRenderedImage {

    XTIFFTileCodec codec;
    XTIFFDirectory dir;
    TIFFDecodeParam param;
    int photometric_interp;
    SeekableStream stream;
    int tileSize;
    int tilesX, tilesY;
    long[] tileOffsets;
    long tileByteCounts[];
    char colormap[];
    char bitsPerSample[];
    int samplesPerPixel;
    int extraSamples;
    byte palette[];
    int bands;
    char sampleFormat[];

    boolean decodePaletteAsShorts;

    boolean isBigEndian;

    // Image types
    int image_type;
    int dataType;

    /**
     * Constructs a XTIFFImage that acquires its data from a given
     * SeekableStream and reads from a particular IFD of the stream. The index
     * of the first IFD is 0.
     * 
     * @param stream the SeekableStream to read from.
     * @param param an instance of TIFFDecodeParam, or null.
     * @param directory the index of the IFD to read from.
     */
    public XTIFFImage(SeekableStream stream, TIFFDecodeParam param,
            int directory) throws IOException {

        this.stream = stream;
        if (param == null || !(param instanceof XTIFFDecodeParam)) {
            param = new XTIFFDecodeParam(param);
        }
        this.param = param;

        decodePaletteAsShorts = param.getDecodePaletteAsShorts();

        // Read the specified directory.
        dir = XTIFFDirectory.create(stream, directory);
        properties.put("tiff.directory", dir);
        ((XTIFFDecodeParam) param).setDirectory(dir);

        // Check whether big endian or little endian format is used.
        isBigEndian = dir.isBigEndian();

        setupImageParameters();

        setupSamplesAndColor();

        dir.setImageType(image_type);

        // Calculate number of tiles and the tileSize in bytes
        tilesX = (width + tileWidth - 1) / tileWidth;
        tilesY = (height + tileHeight - 1) / tileHeight;
        tileSize = tileWidth * tileHeight * bands;

        try {
            codec = dir.createTileCodec((XTIFFDecodeParam) param);
        } catch (Exception e) {
        }
    }

    /**
     * This method gets the image parameters from fields
     */
    protected void setupImageParameters() {

        // Set basic image layout
        minX = minY = 0;
        width = (int) dir.getFieldAsLong(XTIFF.TIFFTAG_IMAGE_WIDTH);
        height = (int) dir.getFieldAsLong(XTIFF.TIFFTAG_IMAGE_LENGTH);

        photometric_interp = (int) dir.getFieldAsLong(XTIFF.TIFFTAG_PHOTOMETRIC_INTERPRETATION);

        // Read the TIFFTAG_BITS_PER_SAMPLE field
        XTIFFField bitsField = dir.getField(XTIFF.TIFFTAG_BITS_PER_SAMPLE);

        if (bitsField == null) {
            // Default
            bitsPerSample = new char[1];
            bitsPerSample[0] = 1;
        } else {
            bitsPerSample = bitsField.getAsChars();
        }

        for (int i = 1; i < bitsPerSample.length; i++) {
            if (bitsPerSample[i] != bitsPerSample[1]) {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder19"));
            }
        }

        // Get the number of samples per pixel
        XTIFFField sfield = dir.getField(XTIFF.TIFFTAG_SAMPLES_PER_PIXEL);
        if (sfield == null) {
            samplesPerPixel = 1;
        } else {
            samplesPerPixel = (int) sfield.getAsLong(0);
        }

        // Figure out if any extra samples are present.
        XTIFFField efield = dir.getField(XTIFF.TIFFTAG_EXTRA_SAMPLES);
        if (efield == null) {
            extraSamples = 0;
        } else {
            extraSamples = (int) efield.getAsLong(0);
        }

        // Read the TIFFTAG_SAMPLE_FORMAT tag to see whether the data might be
        // signed or floating point
        XTIFFField sampleFormatField = dir.getField(XTIFF.TIFFTAG_SAMPLE_FORMAT);

        if (sampleFormatField != null) {
            sampleFormat = sampleFormatField.getAsChars();

            // Check that all the samples have the same format
            for (int l = 1; l < sampleFormat.length; l++) {
                if (sampleFormat[l] != sampleFormat[0]) {
                    throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder20"));
                }
            }

        } else {
            sampleFormat = new char[] { 1 };
        }

        if (sampleFormat[0] == 1 || sampleFormat[0] == 4) {

            // Unsigned or unknown
            if (bitsPerSample[0] == 8) {
                dataType = DataBuffer.TYPE_BYTE;
            } else if (bitsPerSample[0] == 16) {
                dataType = DataBuffer.TYPE_USHORT;
            } else if (bitsPerSample[0] == 32) {
                dataType = DataBuffer.TYPE_INT;
            }

        } else if (sampleFormat[0] == 2) {
            // Signed

            if (bitsPerSample[0] == 1 || bitsPerSample[0] == 4
                    || bitsPerSample[0] == 8) {

                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder21"));

            } else if (bitsPerSample[0] == 16) {
                dataType = DataBuffer.TYPE_SHORT;
            } else if (bitsPerSample[0] == 32) {
                dataType = DataBuffer.TYPE_INT;
            }

        } else if (sampleFormat[0] == 3) {
            // Floating point
            // dataType = DataBuffer.TYPE_FLOAT;
            throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder22"));
        }

        if (dir.getField(XTIFF.TIFFTAG_TILE_WIDTH) != null) {
            // Image is in tiled format
            tileWidth = (int) dir.getFieldAsLong(XTIFF.TIFFTAG_TILE_WIDTH);
            tileHeight = (int) dir.getFieldAsLong(XTIFF.TIFFTAG_TILE_LENGTH);
            tileOffsets = (dir.getField(XTIFF.TIFFTAG_TILE_OFFSETS)).getAsLongs();
            tileByteCounts = dir.getField(XTIFF.TIFFTAG_TILE_BYTE_COUNTS)
                    .getAsLongs();

        } else {

            // Image is in stripped format, looks like tiles to us
            tileWidth = width;
            XTIFFField field = dir.getField(XTIFF.TIFFTAG_ROWS_PER_STRIP);
            if (field == null) {
                // Default is infinity (2^32 -1), basically the entire image
                // TODO: Can do a better job of tiling here
                tileHeight = height;
            } else {
                long l = field.getAsLong(0);
                long infinity = 1;
                infinity = (infinity << 32) - 1;
                if (l == infinity) {
                    // 2^32 - 1 (effectively infinity, entire image is 1 strip)
                    tileHeight = height;
                } else {
                    tileHeight = (int) l;
                }
            }

            XTIFFField tileOffsetsField = dir.getField(XTIFF.TIFFTAG_STRIP_OFFSETS);
            if (tileOffsetsField == null) {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder11"));
            } else {
                tileOffsets = tileOffsetsField.getAsLongs();
            }

            XTIFFField tileByteCountsField = dir.getField(XTIFF.TIFFTAG_STRIP_BYTE_COUNTS);
            if (tileByteCountsField == null) {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder12"));
            } else {
                tileByteCounts = tileByteCountsField.getAsLongs();
            }
        }
    }

    /**
     * This method constructs the sampleModel, colorModel, determines the
     * image_type and the bands parameter.
     */
    protected void setupSamplesAndColor() {

        // Figure out which kind of image we are dealing with.
        switch (photometric_interp) {

        case XTIFF.PHOTOMETRIC_WHITE_IS_ZERO:

            bands = 1;

            // Bilevel or Grayscale - WhiteIsZero
            if (bitsPerSample[0] == 1) {

                image_type = XTIFF.TYPE_BILEVEL_WHITE_IS_ZERO;

                // Keep pixels packed, use IndexColorModel
                sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, tileWidth, tileHeight, 1);

                // Set up the palette
                byte r[] = new byte[] { (byte) 255, (byte) 0 };
                byte g[] = new byte[] { (byte) 255, (byte) 0 };
                byte b[] = new byte[] { (byte) 255, (byte) 0 };

                colorModel = new IndexColorModel(1, 2, r, g, b);

            } else {

                image_type = XTIFF.TYPE_GREYSCALE_WHITE_IS_ZERO;

                if (bitsPerSample[0] == 4) {
                    sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, tileWidth, tileHeight, 4);

                    colorModel = ImageCodec.createGrayIndexColorModel(sampleModel,
                            false);

                } else if (bitsPerSample[0] == 8) {
                    sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,
                            tileWidth,
                            tileHeight,
                            bands);

                    colorModel = ImageCodec.createGrayIndexColorModel(sampleModel,
                            false);

                } else if (bitsPerSample[0] == 16) {

                    sampleModel = RasterFactory.createPixelInterleavedSampleModel(dataType,
                            tileWidth,
                            tileHeight,
                            bands);

                    colorModel = ImageCodec.createComponentColorModel(sampleModel);

                } else {
                    throw new IllegalArgumentException(JaiI18N.getString("XTIFFImageDecoder14"));
                }
            }

            break;

        case XTIFF.PHOTOMETRIC_BLACK_IS_ZERO:

            bands = 1;

            // Bilevel or Grayscale - BlackIsZero
            if (bitsPerSample[0] == 1) {

                image_type = XTIFF.TYPE_BILEVEL_BLACK_IS_ZERO;

                // Keep pixels packed, use IndexColorModel
                sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, tileWidth, tileHeight, 1);

                // Set up the palette
                byte r[] = new byte[] { (byte) 0, (byte) 255 };
                byte g[] = new byte[] { (byte) 0, (byte) 255 };
                byte b[] = new byte[] { (byte) 0, (byte) 255 };

                // 1 Bit pixels packed into a byte, use IndexColorModel
                colorModel = new IndexColorModel(1, 2, r, g, b);

            } else {

                image_type = XTIFF.TYPE_GREYSCALE_BLACK_IS_ZERO;

                if (bitsPerSample[0] == 4) {
                    sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, tileWidth, tileHeight, 4);
                    colorModel = ImageCodec.createGrayIndexColorModel(sampleModel,
                            true);
                } else if (bitsPerSample[0] == 8) {
                    sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,
                            tileWidth,
                            tileHeight,
                            bands);
                    colorModel = ImageCodec.createComponentColorModel(sampleModel);

                } else if (bitsPerSample[0] == 16) {

                    sampleModel = RasterFactory.createPixelInterleavedSampleModel(dataType,
                            tileWidth,
                            tileHeight,
                            bands);
                    colorModel = ImageCodec.createComponentColorModel(sampleModel);

                } else {
                    throw new IllegalArgumentException(JaiI18N.getString("XTIFFImageDecoder14"));
                }
            }

            break;

        case XTIFF.PHOTOMETRIC_RGB:

            bands = samplesPerPixel;

            // RGB full color image
            if (bitsPerSample[0] == 8) {

                sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,
                        tileWidth,
                        tileHeight,
                        bands);
            } else if (bitsPerSample[0] == 16) {

                sampleModel = RasterFactory.createPixelInterleavedSampleModel(dataType,
                        tileWidth,
                        tileHeight,
                        bands);
            } else {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder15"));
            }

            if (samplesPerPixel < 3) {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder1"));

            } else if (samplesPerPixel == 3) {

                image_type = XTIFF.TYPE_RGB;
                // No alpha
                colorModel = ImageCodec.createComponentColorModel(sampleModel);

            } else if (samplesPerPixel == 4) {

                if (extraSamples == 0) {

                    image_type = XTIFF.TYPE_ORGB;
                    // Transparency.OPAQUE signifies image data that is
                    // completely opaque, meaning that all pixels have an alpha
                    // value of 1.0. So the extra band gets ignored, which is
                    // what we want.
                    colorModel = createAlphaComponentColorModel(dataType,
                            true,
                            false,
                            Transparency.OPAQUE);

                } else if (extraSamples == 1) {

                    image_type = XTIFF.TYPE_ARGB_PRE;
                    // Pre multiplied alpha.
                    colorModel = createAlphaComponentColorModel(dataType,
                            true,
                            true,
                            Transparency.TRANSLUCENT);

                } else if (extraSamples == 2) {

                    image_type = XTIFF.TYPE_ARGB;
                    // The extra sample here is unassociated alpha, usually a
                    // transparency mask, also called soft matte.
                    colorModel = createAlphaComponentColorModel(dataType,
                            true,
                            false,
                            Transparency.BITMASK);
                }

            } else {
                image_type = XTIFF.TYPE_RGB_EXTRA;

                // For this case we can't display the image, so there is no
                // point in trying to reformat the data to be BGR followed by
                // the ExtraSamples, the way Java2D would like it, because
                // Java2D can't display it anyway. Therefore create a sample
                // model with increasing bandOffsets, and keep the colorModel
                // as null, as there is no appropriate ColorModel.

                int bandOffsets[] = new int[bands];
                for (int i = 0; i < bands; i++) {
                    bandOffsets[i] = i;
                }

                if (bitsPerSample[0] == 8) {

                    sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, tileWidth, tileHeight, bands, bands
                            * tileWidth, bandOffsets);
                    colorModel = null;

                } else if (bitsPerSample[0] == 16) {

                    sampleModel = new PixelInterleavedSampleModel(dataType, tileWidth, tileHeight, bands, bands
                            * tileWidth, bandOffsets);
                    colorModel = null;
                }
            }

            break;

        case XTIFF.PHOTOMETRIC_PALETTE:

            image_type = XTIFF.TYPE_PALETTE;

            // Get the colormap
            XTIFFField cfield = dir.getField(XTIFF.TIFFTAG_COLORMAP);
            if (cfield == null) {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder2"));
            } else {
                colormap = cfield.getAsChars();
            }

            // Could be either 1 or 3 bands depending on whether we use
            // IndexColorModel or not.
            if (decodePaletteAsShorts) {
                bands = 3;

                if (bitsPerSample[0] != 4 && bitsPerSample[0] != 8
                        && bitsPerSample[0] != 16) {
                    throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder13"));
                }

                // If no SampleFormat tag was specified and if the
                // bitsPerSample are less than or equal to 8, then the
                // dataType was initially set to byte, but now we want to
                // expand the palette as shorts, so the dataType should
                // be ushort.
                if (dataType == DataBuffer.TYPE_BYTE) {
                    dataType = DataBuffer.TYPE_USHORT;
                }

                // Data will have to be unpacked into a 3 band short image
                // as we do not have a IndexColorModel that can deal with
                // a colormodel whose entries are of short data type.
                sampleModel = RasterFactory.createPixelInterleavedSampleModel(dataType,
                        tileWidth,
                        tileHeight,
                        bands);
                colorModel = ImageCodec.createComponentColorModel(sampleModel);

            } else {

                bands = 1;

                if (bitsPerSample[0] == 4) {
                    // Pixel data will not be unpacked, will use MPPSM to store
                    // packed data and IndexColorModel to do the unpacking.
                    sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, tileWidth, tileHeight, bitsPerSample[0]);
                } else if (bitsPerSample[0] == 8) {
                    sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,
                            tileWidth,
                            tileHeight,
                            bands);
                } else if (bitsPerSample[0] == 16) {

                    // Here datatype has to be unsigned since we are storing
                    // indices into the IndexColorModel palette. Ofcourse
                    // the actual palette entries are allowed to be negative.
                    sampleModel = RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_USHORT,
                            tileWidth,
                            tileHeight,
                            bands);
                } else {
                    throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder13"));
                }

                int bandLength = colormap.length / 3;
                byte r[] = new byte[bandLength];
                byte g[] = new byte[bandLength];
                byte b[] = new byte[bandLength];

                int gIndex = bandLength;
                int bIndex = bandLength * 2;

                if (dataType == DataBuffer.TYPE_SHORT) {

                    for (int i = 0; i < bandLength; i++) {
                        r[i] = param.decodeSigned16BitsTo8Bits((short) colormap[i]);
                        g[i] = param.decodeSigned16BitsTo8Bits((short) colormap[gIndex
                                + i]);
                        b[i] = param.decodeSigned16BitsTo8Bits((short) colormap[bIndex
                                + i]);
                    }

                } else {

                    for (int i = 0; i < bandLength; i++) {
                        r[i] = param.decode16BitsTo8Bits(colormap[i] & 0xffff);
                        g[i] = param.decode16BitsTo8Bits(colormap[gIndex + i] & 0xffff);
                        b[i] = param.decode16BitsTo8Bits(colormap[bIndex + i] & 0xffff);
                    }

                }

                colorModel = new IndexColorModel(bitsPerSample[0], bandLength, r, g, b);
            }

            break;

        case XTIFF.PHOTOMETRIC_TRANSPARENCY:

            image_type = XTIFF.TYPE_TRANS;

            // Transparency Mask
            throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder3"));
            // break;

        default:
            throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder4"));
        }

    }

    /**
     * Reads a private IFD from a given offset in the stream. This method may be
     * used to obtain IFDs that are referenced only by private tag values.
     */
    public XTIFFDirectory getPrivateIFD(long offset) throws IOException {
        return XTIFFDirectory.create(stream, offset);
    }

    private WritableRaster tile00 = null;

    /**
     * Returns tile (tileX, tileY) as a Raster.
     */
    public synchronized Raster getTile(int tileX, int tileY) {
        if (tileX == 0 && tileY == 0 && tile00 != null) {
            return tile00;
        }

        if ((tileX < 0) || (tileX >= tilesX) || (tileY < 0)
                || (tileY >= tilesY)) {
            throw new IllegalArgumentException(JaiI18N.getString("XTIFFImageDecoder5"));
        }

        // file setup1

        // Save original file pointer position and seek to tile data location.
        long save_offset = 0;
        try {
            save_offset = stream.getFilePointer();
            stream.seek(tileOffsets[tileY * tilesX + tileX]);
        } catch (IOException ioe) {
            throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder8"));
        }

        // Number of bytes in this tile (strip) after compression.
        int byteCount = (int) tileByteCounts[tileY * tilesX + tileX];

        // Find out the number of bytes in the current tile
        Rectangle tileRect = new Rectangle(tileXToX(tileX), tileYToY(tileY), tileWidth, tileHeight);
        Rectangle newRect = tileRect.intersection(getBounds());

        // file setup2

        byte data[] = new byte[byteCount];
        WritableRaster tile = null;
        try {
            stream.readFully(data, 0, byteCount);
            tile = codec.decode(this, newRect, data);
            stream.seek(save_offset);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read raw tile data:" + e);
        }

        if (tileX == 0 && tileY == 0) {
            tile00 = tile;
        }
        return tile;
    }

    // Create ComponentColorModel for TYPE_RGB images
    private ComponentColorModel createAlphaComponentColorModel(
                                                               int dataType,
                                                               boolean hasAlpha,
                                                               boolean isAlphaPremultiplied,
                                                               int transparency) {

        ComponentColorModel ccm = null;
        int RGBBits[][] = new int[3][];

        RGBBits[0] = new int[] { 8, 8, 8, 8 }; // Byte
        RGBBits[1] = new int[] { 16, 16, 16, 16 }; // Short
        RGBBits[2] = new int[] { 16, 16, 16, 16 }; // UShort
        RGBBits[2] = new int[] { 32, 32, 32, 32 }; // Int

        ccm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), RGBBits[dataType], hasAlpha, isAlphaPremultiplied, transparency, dataType);
        return ccm;
    }
}
