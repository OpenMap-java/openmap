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
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFDirectory;
import org.libtiff.jai.codec.XTIFFEncodeParam;
import org.libtiff.jai.codec.XTIFFField;
import org.libtiff.jai.codec.XTIFFTileCodec;
import org.libtiff.jai.util.JaiI18N;

import com.sun.media.jai.codec.ImageEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.codecimpl.TIFFImageDecoder;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

/**
 * A baseline TIFF writer. The writer outputs TIFF images in either Bilevel,
 * Greyscale, Palette color or Full Color modes.
 * 
 */
public class XTIFFImageEncoder extends TIFFImageEncoder {

    long firstIFDOffset = 0;
    XTIFFDirectory directory;
    XTIFFEncodeParam tparam;
    int width;
    int length;
    SampleModel sampleModel;
    int numBands;
    int sampleSize[];
    int dataType;
    boolean dataTypeIsShort;
    ColorModel colorModel;
    int numTiles;
    int compression;
    boolean isTiled;
    long tileLength;
    long tileWidth;
    byte[] bpixels = null;
    long stripTileByteCounts[];
    long stripTileOffsets[];
    long currentOffset = 0;

    // Image Types
    public static final int XTIFF_BILEVEL_WHITE_IS_ZERO = 0;
    public static final int XTIFF_BILEVEL_BLACK_IS_ZERO = 1;
    public static final int XTIFF_PALETTE = 2;
    public static final int XTIFF_FULLCOLOR = 3;
    public static final int XTIFF_GREYSCALE = 4;

    /**
     * Standard constructor
     */
    public XTIFFImageEncoder(OutputStream output, ImageEncodeParam param) {
        super(output, param);
        if (this.param == null || !(param instanceof XTIFFEncodeParam)) {
            this.param = new XTIFFEncodeParam((TIFFEncodeParam) param);
        }
        tparam = (XTIFFEncodeParam) this.param;
        directory = tparam.getDirectory();
    }

    private File createTemp() throws IOException {
        String tmpdir = System.getProperty("tiff.io.tmpdir");
        File file = null;
        if (tmpdir != null)
            file = File.createTempFile("libtiff.jai.", ".dat", new File(tmpdir));
        else
            file = File.createTempFile("libtiff.jai.", ".dat");
        file.deleteOnExit();
        return file;
    }

    private void copyImageData(File tmp, OutputStream out, int total)
            throws IOException {
        int bufsize = 1024;
        int bytes = 0;
        byte[] buf = new byte[bufsize];
        FileInputStream in = new FileInputStream(tmp);
        do {
            bytes = in.read(buf);
            out.write(buf, 0, bytes);
            total -= bytes;
        } while (total > 0);
        in.close();
    }

    /**
     * Encodes a RenderedImage and writes the output to the OutputStream
     * associated with this ImageEncoder.
     */
    public void encode(RenderedImage im) throws IOException {

        // Set comp into directory
        compression = tparam.getCompression();

        // see if tiled
        isTiled = ((TIFFEncodeParam) param).getWriteTiled();

        // Setup Directory fields.
        getImageFields(im);

        if (compression == XTIFF.COMPRESSION_NONE) {
            computeIFDOffset();
            writeFileHeader(firstIFDOffset);
            currentOffset = 8;
            writeImageData(im, output);
            writeDirectory(directory.getFields(), 0);
        } else {
            // We have to write compressed data out to
            // a temp file to compute the IFD offset.
            // The only alternative is to compress the
            // data twice, which is just about as bad.
            currentOffset = 8;
            File tmp = null;
            try {
                tmp = createTemp();
                OutputStream tmpOut = new FileOutputStream(tmp);
                int total = writeImageData(im, tmpOut);
                tmpOut.close();
                writeFileHeader(currentOffset + currentOffset % 2);
                copyImageData(tmp, output, total);
                writeDirectory(directory.getFields(), 0);
            } finally {
                if (tmp != null)
                    tmp.delete();
            }
        }
    }

    /**
     * Precomputes the IFD Offset for uncompressed data.
     */
    private void computeIFDOffset() {

        long bytesPerRow = (long) Math.ceil((sampleSize[0] / 8.0) * tileWidth
                * numBands);
        long bytesPerTile = bytesPerRow * tileLength;
        long lastTile = bytesPerTile;

        if (!isTiled) {
            // Last strip may have lesser rows
            long lastStripRows = length - (tileLength * (numTiles - 1));
            lastTile = lastStripRows * bytesPerRow;
        }

        long totalBytesOfData = bytesPerTile * (numTiles - 1) + lastTile;

        // File header always occupies 8 bytes and we write the image data
        // after that.
        firstIFDOffset = 8 + totalBytesOfData;
        // Must begin on a word boundary
        if ((firstIFDOffset % 2) != 0) {
            firstIFDOffset++;
        }
    }

    private void writeFileHeader(long firstIFDOffset) throws IOException {
        // 8 byte image file header

        // Byte order used within the file - Big Endian
        output.write('M');
        output.write('M');

        // Magic value
        output.write(0);
        output.write(42);

        // Offset in bytes of the first IFD, must begin on a word boundary
        writeLong(firstIFDOffset);

    }

    // method for adding tags that haven't been set by user
    private void addIfAbsent(int tag, int type, int count, Object obj) {
        if (directory.getField(tag) == null)
            directory.addField(tag, type, count, obj);
    }

    private void getImageFields(RenderedImage im)
    /* throws IOException */{

        width = im.getWidth();
        length = im.getHeight(); // TIFF calls it length

        sampleModel = im.getSampleModel();
        numBands = sampleModel.getNumBands();
        sampleSize = sampleModel.getSampleSize();

        dataType = sampleModel.getDataType();
        if (dataType != DataBuffer.TYPE_BYTE
                && dataType != DataBuffer.TYPE_SHORT
                && dataType != DataBuffer.TYPE_USHORT) {
            // Support only byte and (unsigned) short.
            throw new Error(JaiI18N.getString("TIFFImageEncoder0"));
        }

        dataTypeIsShort = dataType == DataBuffer.TYPE_SHORT
                || dataType == DataBuffer.TYPE_USHORT;

        colorModel = im.getColorModel();
        if (colorModel != null && colorModel instanceof IndexColorModel
                && dataTypeIsShort) {
            // Don't support (unsigned) short palette-color images.
            throw new Error(JaiI18N.getString("TIFFImageEncoder2"));
        }
        IndexColorModel icm = null;
        int sizeOfColormap = 0;
        char colormap[] = null;

        // Basic fields - have to be in increasing numerical order BILEVEL
        // ImageWidth 256
        // ImageLength 257
        // BitsPerSample 258
        // Compression 259
        // PhotoMetricInterpretation 262
        // StripOffsets 273
        // RowsPerStrip 278
        // StripByteCounts 279
        // XResolution 282
        // YResolution 283
        // ResolutionUnit 296

        int photometricInterpretation = XTIFF.PHOTOMETRIC_RGB;
        int imageType = XTIFF_FULLCOLOR;

        // IMAGE TYPES POSSIBLE

        // Bilevel
        // BitsPerSample = 1
        // Compression = 1, 2, or 32773
        // PhotometricInterpretation either 0 or 1

        // Greyscale
        // BitsPerSample = 4 or 8
        // Compression = 1, 32773
        // PhotometricInterpretation either 0 or 1

        // Palette
        // ColorMap 320
        // BitsPerSample = 4 or 8
        // PhotometrciInterpretation = 3

        // Full color
        // BitsPerSample = 8, 8, 8
        // SamplesPerPixel = 3 or more 277
        // Compression = 1, 32773
        // PhotometricInterpretation = 2

        if (colorModel instanceof IndexColorModel) {

            icm = (IndexColorModel) colorModel;
            int mapSize = icm.getMapSize();

            if (sampleSize[0] == 1) {
                // Bilevel image

                if (mapSize != 2) {
                    throw new IllegalArgumentException(JaiI18N.getString("TIFFImageEncoder1"));
                }

                byte r[] = new byte[mapSize];
                icm.getReds(r);
                byte g[] = new byte[mapSize];
                icm.getGreens(g);
                byte b[] = new byte[mapSize];
                icm.getBlues(b);

                if ((r[0] & 0xff) == 0 && (r[1] & 0xff) == 255
                        && (g[0] & 0xff) == 0 && (g[1] & 0xff) == 255
                        && (b[0] & 0xff) == 0 && (b[1] & 0xff) == 255) {

                    imageType = XTIFF_BILEVEL_BLACK_IS_ZERO;

                } else if ((r[0] & 0xff) == 255 && (r[1] & 0xff) == 0
                        && (g[0] & 0xff) == 255 && (g[1] & 0xff) == 0
                        && (b[0] & 0xff) == 255 && (b[1] & 0xff) == 0) {

                    imageType = XTIFF_BILEVEL_WHITE_IS_ZERO;

                } else {
                    imageType = XTIFF_PALETTE;
                }

            } else {
                // Palette color image.
                imageType = XTIFF_PALETTE;
            }
        } else {

            // If it is not an IndexColorModel, it can either be a greyscale
            // image or a full color image

            if ((colorModel == null || colorModel.getColorSpace().getType() == ColorSpace.TYPE_GRAY)
                    && numBands == 1) {
                // Greyscale image
                imageType = XTIFF_GREYSCALE;
            } else {
                // Full color image
                imageType = XTIFF_FULLCOLOR;
            }
        }

        switch (imageType) {

        case XTIFF_BILEVEL_WHITE_IS_ZERO:
            photometricInterpretation = XTIFF.PHOTOMETRIC_WHITE_IS_ZERO;
            break;

        case XTIFF_BILEVEL_BLACK_IS_ZERO:
            photometricInterpretation = XTIFF.PHOTOMETRIC_BLACK_IS_ZERO;
            break;

        case XTIFF_GREYSCALE:
            // Since the CS_GRAY colorspace is always of type black_is_zero
            photometricInterpretation = XTIFF.PHOTOMETRIC_BLACK_IS_ZERO;
            break;

        case XTIFF_PALETTE:
            photometricInterpretation = XTIFF.PHOTOMETRIC_PALETTE;

            icm = (IndexColorModel) colorModel;
            sizeOfColormap = icm.getMapSize();

            byte r[] = new byte[sizeOfColormap];
            icm.getReds(r);
            byte g[] = new byte[sizeOfColormap];
            icm.getGreens(g);
            byte b[] = new byte[sizeOfColormap];
            icm.getBlues(b);

            int redIndex = 0,
            greenIndex = sizeOfColormap;
            int blueIndex = 2 * sizeOfColormap;
            colormap = new char[sizeOfColormap * 3];
            for (int i = 0; i < sizeOfColormap; i++) {
                colormap[redIndex++] = (char) (r[i] << 8);
                colormap[greenIndex++] = (char) (g[i] << 8);
                colormap[blueIndex++] = (char) (b[i] << 8);
            }

            sizeOfColormap *= 3;

            // Since we will be writing the colormap field.
            break;

        case XTIFF_FULLCOLOR:
            photometricInterpretation = XTIFF.PHOTOMETRIC_RGB;
            break;

        }

        if (isTiled) {
            tileWidth = 16L;
            tileLength = 16L;
            XTIFFField fld = directory.getField(XTIFF.TIFFTAG_TILE_WIDTH);
            if (fld != null)
                tileWidth = (int) fld.getAsLong(0);
            fld = directory.getField(XTIFF.TIFFTAG_TILE_LENGTH);
            if (fld != null)
                tileLength = (int) fld.getAsLong(0);
        } else {
            // Default strip is 8 rows.
            tileLength = 8L;
            // tileWidth of strip is width

            tileWidth = width;
            XTIFFField fld = directory.getField(TIFFImageDecoder.TIFF_ROWS_PER_STRIP);
            if (fld != null)
                tileLength = fld.getAsLong(0);
        }

        numTiles = (int) Math.ceil((double) length / (double) tileLength)
                * (int) Math.ceil((double) width / (double) tileWidth);

        stripTileByteCounts = new long[numTiles];
        stripTileOffsets = new long[numTiles];

        // Image Width
        directory.addField(XTIFF.TIFFTAG_IMAGE_WIDTH,
                TIFFField.TIFF_LONG,
                1,
                (Object) (new long[] { width }));

        // Image Length
        directory.addField(XTIFF.TIFFTAG_IMAGE_LENGTH,
                TIFFField.TIFF_LONG,
                1,
                new long[] { length });

        directory.addField(XTIFF.TIFFTAG_BITS_PER_SAMPLE,
                TIFFField.TIFF_SHORT,
                numBands,
                convertToChars(sampleSize));

        directory.addField(XTIFF.TIFFTAG_COMPRESSION,
                TIFFField.TIFF_SHORT,
                1,
                new char[] { (char) compression });

        directory.addField(XTIFF.TIFFTAG_PHOTOMETRIC_INTERPRETATION,
                TIFFField.TIFF_SHORT,
                1,
                new char[] { (char) photometricInterpretation });

        directory.addField(XTIFF.TIFFTAG_SAMPLES_PER_PIXEL,
                TIFFField.TIFF_SHORT,
                1,
                new char[] { (char) numBands });

        if (isTiled) {
            directory.addField(XTIFF.TIFFTAG_TILE_WIDTH,
                    TIFFField.TIFF_LONG,
                    1,
                    new long[] { tileWidth });

            directory.addField(XTIFF.TIFFTAG_TILE_LENGTH,
                    TIFFField.TIFF_LONG,
                    1,
                    new long[] { tileLength });

            directory.addField(XTIFF.TIFFTAG_TILE_OFFSETS,
                    TIFFField.TIFF_LONG,
                    numTiles,
                    stripTileOffsets);

            directory.addField(XTIFF.TIFFTAG_TILE_BYTE_COUNTS,
                    TIFFField.TIFF_LONG,
                    numTiles,
                    stripTileByteCounts);
        } else {
            directory.addField(XTIFF.TIFFTAG_STRIP_OFFSETS,
                    TIFFField.TIFF_LONG,
                    numTiles,
                    stripTileOffsets);

            directory.addField(XTIFF.TIFFTAG_ROWS_PER_STRIP,
                    TIFFField.TIFF_LONG,
                    1,
                    new long[] { tileLength });

            directory.addField(XTIFF.TIFFTAG_STRIP_BYTE_COUNTS,
                    TIFFField.TIFF_LONG,
                    numTiles,
                    stripTileByteCounts);
        }

        addIfAbsent(XTIFF.TIFFTAG_X_RESOLUTION,
                TIFFField.TIFF_RATIONAL,
                1,
                new long[][] { { 72, 1 } });

        addIfAbsent(XTIFF.TIFFTAG_Y_RESOLUTION,
                TIFFField.TIFF_RATIONAL,
                1,
                new long[][] { { 72, 1 } });

        addIfAbsent(XTIFF.TIFFTAG_RESOLUTION_UNIT,
                TIFFField.TIFF_SHORT,
                1,
                new char[] { (char) 2 });

        if (colormap != null) {
            directory.addField(XTIFF.TIFFTAG_COLORMAP,
                    TIFFField.TIFF_SHORT,
                    sizeOfColormap,
                    colormap);
        }

        // Data Sample Format Extension fields.
        if (dataTypeIsShort) {
            // SampleFormat
            int[] sampleFormat = new int[numBands];
            sampleFormat[0] = dataType == DataBuffer.TYPE_USHORT ? 1 : 2;
            for (int b = 1; b < numBands; b++) {
                sampleFormat[b] = sampleFormat[0];
            }
            directory.addField(XTIFF.TIFFTAG_SAMPLE_FORMAT,
                    TIFFField.TIFF_SHORT,
                    numBands,
                    convertToChars(sampleFormat));

            // SMinSampleValue: set to data type minimum.
            int[] minValue = new int[numBands];
            minValue[0] = dataType == DataBuffer.TYPE_USHORT ? 0
                    : Short.MIN_VALUE;
            for (int b = 1; b < numBands; b++) {
                minValue[b] = minValue[0];
            }
            directory.addField(XTIFF.TIFFTAG_S_MIN_SAMPLE_VALUE,
                    TIFFField.TIFF_SHORT,
                    numBands,
                    convertToChars(minValue));

            // SMaxSampleValue: set to data type maximum.
            int[] maxValue = new int[numBands];
            maxValue[0] = dataType == DataBuffer.TYPE_USHORT ? 65535
                    : Short.MAX_VALUE;
            for (int b = 1; b < numBands; b++) {
                maxValue[b] = maxValue[0];
            }
            directory.addField(XTIFF.TIFFTAG_S_MAX_SAMPLE_VALUE,
                    TIFFField.TIFF_SHORT,
                    numBands,
                    convertToChars(maxValue));
        }

    }

    private char[] convertToChars(int[] shorts) {
        char[] out = new char[shorts.length];
        for (int i = 0; i < shorts.length; i++)
            out[i] = (char) shorts[i];
        return out;
    }

    protected int getSampleSize() {
        if (dataType == DataBuffer.TYPE_BYTE)
            return 1;
        else if (dataTypeIsShort)
            return 2;
        return 1; // what should go here?
    }

    protected int getTileSize() {
        return (int) (tileLength * tileWidth * numBands);
    }

    private int writeImageData(RenderedImage im, OutputStream out)
            throws IOException {
        int total = 0;

        // Get the encoder
        XTIFFTileCodec codec = directory.createTileCodec(tparam);

        // Create a buffer to hold the data
        // to be written to the file, so we can use array writes.
        int tsize = codec.getCompressedTileSize(im);
        bpixels = new byte[tsize];

        // Encode one tile at a time
        Rectangle rect = new Rectangle();
        float minX = (float) im.getMinX();
        float minY = (float) im.getMinY();
        float rows = (float) tileLength;
        float cols = (float) tileWidth;
        int i = 0;
        for (int row = 0; row < length; row += tileLength) {
            for (int col = 0; col < width; col += tileWidth) {
                if (!isTiled)
                    rows = Math.min(tileLength, length - row);
                rect.setRect(minX + col, minY + row, cols, rows);
                int tileSize = codec.encode(im, rect, bpixels);
                out.write(bpixels, 0, tileSize);
                stripTileOffsets[i] = currentOffset;
                stripTileByteCounts[i++] = tileSize;
                currentOffset += tileSize;
                total += tileSize;
            }
        }
        return total;
    }

    private void writeDirectory(XTIFFField fields[], int nextIFDOffset)
            throws IOException {

        if (currentOffset % 2 == 1) {
            output.write(0);
            currentOffset++;
        }

        // 2 byte count of number of directory entries (fields)
        int numEntries = fields.length;

        long offsetBeyondIFD = currentOffset + 12 * numEntries + 4 + 2;
        Vector tooBig = new Vector();

        XTIFFField field;
        int tag;
        int type;
        int count;

        // Write number of fields in the IFD
        writeUnsignedShort(numEntries);

        for (int i = 0; i < numEntries; i++) {

            field = fields[i];

            // 12 byte field entry TIFFField

            // byte 0-1 Tag that identifies a field
            tag = field.getTag();
            writeUnsignedShort(tag);

            // byte 2-3 The field type
            type = field.getType();
            writeUnsignedShort(type);

            // bytes 4-7 the number of values of the indicated type
            count = field.getCount();
            writeLong(count);

            // bytes 8 - 11 the value offset
            if (count * sizeOfType[type] > 4) {

                // We need an offset as data won't fit into 4 bytes
                writeLong(offsetBeyondIFD);
                offsetBeyondIFD += (count * sizeOfType[type]);
                tooBig.add(new Integer(i));

            } else {

                writeValuesAsFourBytes(field);
            }

        }

        // Address of next IFD
        writeLong(nextIFDOffset);

        int index;
        // Write the tag values that did not fit into 4 bytes

        for (int i = 0; i < tooBig.size(); i++) {
            index = ((Integer) tooBig.elementAt(i)).intValue();
            writeValues(fields[index]);
        }
    }

    private static final int[] sizeOfType = { 0, // 0 = n/a
            1, // 1 = byte
            1, // 2 = ascii
            2, // 3 = short
            4, // 4 = long
            8, // 5 = rational
            1, // 6 = sbyte
            1, // 7 = undefined
            2, // 8 = sshort
            4, // 9 = slong
            8, // 10 = srational
            4, // 11 = float
            8 // 12 = double
    };

    private void writeValuesAsFourBytes(XTIFFField field) throws IOException {

        int dataType = field.getType();
        int count = field.getCount();

        switch (dataType) {

        // unsigned 8 bits
        case TIFFField.TIFF_BYTE:
            byte bytes[] = field.getAsBytes();

            for (int i = 0; i < count; i++) {
                output.write(bytes[i]);
            }

            for (int i = 0; i < (4 - count); i++) {
                output.write(0);
            }

            break;

        // unsigned 16 bits
        case TIFFField.TIFF_SHORT:
            char shorts[] = field.getAsChars();

            for (int i = 0; i < count; i++) {
                writeUnsignedShort((int) shorts[i]);
            }

            for (int i = 0; i < (2 - count); i++) {
                writeUnsignedShort(0);
            }

            break;

        // unsigned 32 bits
        case TIFFField.TIFF_LONG:
            long longs[] = field.getAsLongs();

            for (int i = 0; i < count; i++) {
                writeLong(longs[i]);
            }
            break;
        }

    }

    private void writeValues(XTIFFField field) throws IOException {

        int dataType = field.getType();
        int count = field.getCount();

        switch (dataType) {

        // character data with NULL termination
        case TIFFField.TIFF_ASCII:
            String strings[] = field.getAsStrings();
            for (int i = 0; i < strings.length; i++) {
                byte bytes[] = strings[i].getBytes();
                for (int j = 0; j < bytes.length; j++) {
                    output.write(bytes[j]);
                }
                if ((i + 1) < count)
                    output.write(0);
            }
            break;

        // unsigned 8 bits
        case TIFFField.TIFF_BYTE:
            byte bytes[] = field.getAsBytes();
            for (int i = 0; i < count; i++) {
                output.write(bytes[i]);
            }
            break;

        // unsigned 16 bits
        case TIFFField.TIFF_SHORT:
            char shorts[] = field.getAsChars();
            for (int i = 0; i < count; i++) {
                writeUnsignedShort((int) shorts[i]);
            }
            break;

        // unsigned 32 bits
        case TIFFField.TIFF_LONG:
            long longs[] = field.getAsLongs();
            for (int i = 0; i < count; i++) {
                writeLong(longs[i]);
            }
            break;

        // IEEE 8-byte double
        case TIFFField.TIFF_DOUBLE:
            double doubles[] = field.getAsDoubles();
            for (int i = 0; i < count; i++) {
                writeDouble(doubles[i]);
            }
            break;

        case TIFFField.TIFF_RATIONAL:
            long rationals[][] = field.getAsRationals();
            for (int i = 0; i < count; i++) {
                writeLong(rationals[i][0]);
                writeLong(rationals[i][1]);
            }
            break;

        }

    }

    // Here s is never expected to have value greater than what can be
    // stored in 2 bytes.
    private void writeUnsignedShort(int s) throws IOException {
        output.write((s & 0xff00) >>> 8);
        output.write(s & 0x00ff);
    }

    private void writeLong(long l) throws IOException {
        output.write((int) ((l & 0xff000000) >>> 24));
        output.write((int) ((l & 0x00ff0000) >>> 16));
        output.write((int) ((l & 0x0000ff00) >>> 8));
        output.write(((int) l & 0x000000ff));
    }

    // write 8-byte IEEE double
    private void writeDouble(double d) throws IOException {
        long lval = Double.doubleToLongBits(d);
        writeLong(lval >>> 32);
        writeLong((lval & 0xffffffff));
    }

}
