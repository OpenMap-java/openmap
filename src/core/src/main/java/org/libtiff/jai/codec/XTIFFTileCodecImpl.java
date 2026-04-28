package org.libtiff.jai.codec;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;

import org.libtiff.jai.util.JaiI18N;

/**
 * Provides a base class for writing TIFF tile codecs, to be registered with the
 * XTIFFDirectory. This codec allows for both decoding and (optionally) encoding
 * of tiles, and also handles the colorspace conversion in decoding.
 * <p>
 * At the minimum you will need to implement the two methods decodeTilePixels()
 * for byte and short data, as well as the methods register() and create(). If
 * your decoder requires additional parameters from the tags, set them up in
 * initializeDecoding(), and initializeEncoding() for encoding.
 * <p>
 * To implement encoding, you must override the canEncode() method to return
 * true, and implement encodeTilePixels().
 * 
 * @author Niles Ritter
 * @see XTIFFTileCodec
 */
public abstract class XTIFFTileCodecImpl implements XTIFFTileCodec {

    // ////////////////////////////////////////////////////
    // // Implementation Section
    // // Override or implement methods here
    // ////////////////////////////////////////////////////

    /**
     * Registration method. Must be implemented by the extended class to
     * register itself with the XTIFFDirectory for all compression codes it
     * supports (e.g Fax codec supports 3 codes).
     * 
     * @see XTIFFDirectory
     */
    public abstract void register();

    /**
     * Implement this to return the corresponding empty codec object.
     */
    public abstract XTIFFTileCodec create();

    /**
     * Indicate whether this codec can encode data. Override to return true only
     * if your codec implments encoding.
     */
    public boolean canEncode() {
        return false;
    }

    /**
     * The initialization method particular to decoding. Extend for whatever
     * compression-specific information or parameters is needed. The decoding
     * parameter has already been assigned at this point, as well as the
     * XTIFFDirectory parsed from the input stream, and so all XTIFFFields are
     * available.
     */
    public void initializeDecoding() {}

    /**
     * The initialization method particular to encoding. Extend for whatever
     * compression-specific information or parameters is needed. The decoding
     * parameter has already been assigned at this point, as well as the
     * XTIFFDirectory parsed from the input stream, and so all XTIFFFields are
     * available.
     */
    public void initializeEncoding() {}

    /**
     * decode bpixel byte array of data into pixels, packed for 1,2,4 8 bit
     * pixels. Must implment this.
     * 
     * @param bpixels the byte array of compressed input data
     * @param rect the rectangular shape of the target pixels
     * @param pixels the target decompressed pixels.
     */
    public abstract void decodeTilePixels(byte[] bpixels, Rectangle rect,
                                          byte[] pixels);

    /**
     * decode bpixel byte array of data into pixels, packed for 16 bit pixels.
     * Must implment this.
     * 
     * @param bpixels the byte array of compressed input data
     * @param rect the rectangular shape of the target pixels
     * @param pixels the target decompressed pixels.
     */
    public abstract void decodeTilePixels(byte[] bpixels, Rectangle rect,
                                          short[] pixels);

    /**
     * encode the tile in pixels into bpixels and return the byte size of the
     * compressed data. Override this method if canEncode() = true;
     * 
     * @param pixels input pixels
     * @param rect the array dimensions of samples
     * @param bpixels the target array of compressed byte data
     */

    public int encodeTilePixels(int[] pixels, Rectangle rect, byte[] bpixels) {
        return 0;
    }

    // ////////////////////////////////////////////////////
    // // Common Section
    // ////////////////////////////////////////////////////

    protected XTIFFDirectory directory = null;
    protected RenderedImage image = null;
    protected int minY;
    protected int minX;
    protected int width;
    protected int length;
    protected int numBands;
    protected int tileLength;
    protected int tileWidth;
    protected int compression;
    protected SampleModel sampleModel;
    protected int[] sampleSize;
    protected char[] bitsPerSample;
    protected char[] colormap = null;

    /**
     * The empty constructor.
     */
    public XTIFFTileCodecImpl() {}

    /**
     * The method for initializing information common to both encoder and
     * decoder.
     */
    public void initialize() {
        width = (int) getLongField(XTIFF.TIFFTAG_IMAGE_WIDTH);
        length = (int) getLongField(XTIFF.TIFFTAG_IMAGE_LENGTH);
        isTiled = directory.isTiled();
        if (isTiled) {
            tileWidth = (int) getLongField(XTIFF.TIFFTAG_TILE_WIDTH);
            tileLength = (int) getLongField(XTIFF.TIFFTAG_TILE_LENGTH);
        } else {
            tileWidth = width;
            tileLength = (int) getLongField(XTIFF.TIFFTAG_ROWS_PER_STRIP);
        }
        // Figure out what compression if any, is being used.
        XTIFFField compField = directory.getField(XTIFF.TIFFTAG_COMPRESSION);
        if (compField != null) {
            compression = compField.getAsInt(0);
        } else {
            compression = XTIFF.COMPRESSION_NONE;
        }
        XTIFFField cfield = directory.getField(XTIFF.TIFFTAG_COLORMAP);
        if (cfield != null)
            colormap = cfield.getAsChars();

        // Read the TIFFTAG_BITS_PER_SAMPLE field
        XTIFFField bitsField = directory.getField(XTIFF.TIFFTAG_BITS_PER_SAMPLE);

        if (bitsField == null) {
            // Default
            bitsPerSample = new char[1];
            bitsPerSample[0] = 1;
        } else {
            bitsPerSample = bitsField.getAsChars();
        }
        image_type = directory.getImageType();
    }

    /**
     * A common utility method for accessing the XTIFFFields in the current
     * image directory.
     */
    protected long getLongField(int fld) {
        XTIFFField field = directory.getField(fld);
        if (field == null)
            return 0;
        else
            return field.getAsLong(0);
    }

    /**
     * This method may be used by the implementations register() method to
     * register itself with the XTIFFDirectory.
     * 
     * @see XTIFFDirectory
     */
    public void register(int comp) {
        XTIFFDirectory.registerTileCodec(comp, this);
    }

    /**
     * One-time common image parameter setup
     * 
     * @param img the source image that will be encoded into a TIFF formatted
     *        stream, or the TIFF image from which Raster tiles will be decoded.
     */
    protected void setupSourceImage(RenderedImage img) {
        image = img;

        // Get raster parameters
        minY = image.getMinY();
        minX = image.getMinX();
        sampleModel = image.getSampleModel();
        numBands = sampleModel.getNumBands();
        sampleSize = sampleModel.getSampleSize();

    }

    /**
     * Returns the TIFF compression type
     */
    public int getCompression() {
        return compression;
    }

    // ////////////////////////////////////////////////////
    // // Encoding Section
    // ////////////////////////////////////////////////////

    protected XTIFFEncodeParam encodeParam = null;
    private int _pixels[];
    protected boolean isTiled;

    /**
     * The method for creating an encoder from the XTIFFEncodeParam information.
     */
    public XTIFFTileCodec create(XTIFFEncodeParam param) throws IOException {
        XTIFFTileCodecImpl codec = (XTIFFTileCodecImpl) create();
        codec.initialize(param);
        return codec;
    }

    protected void initialize(XTIFFEncodeParam param) throws IOException {
        if (!canEncode())
            throw new IOException("encoding not supported");
        encodeParam = param;
        directory = param.getDirectory();
        initialize();
        initializeEncoding();
    }

    /**
     * Encode the data into buffer and return byte count Normally you will not
     * need to override this method, but instead implement the
     * <code>encodeTilePixels()</code> method.
     */
    public int encode(RenderedImage img, Rectangle rect, byte[] bpixels) {
        if (image == null) {
            setupSourceImage(img);
            setupBufferForEncoding();
        }

        // Fill tile buffer, padding right with zeroes.
        getTilePixels(rect);

        // encode and return number of bytes compressed
        return encodeTilePixels(_pixels, rect, bpixels);
    }

    /**
     * One-time setup for encoding
     */
    protected void setupBufferForEncoding() {
        // Set up input tile/strip buffer
        _pixels = new int[tileWidth * tileLength * numBands];

        // if padding necessary do it now.
        int padRight = (tileWidth - (width % tileWidth)) % tileWidth;
        int padBottom = (tileLength - (length % tileLength)) % tileLength;
        if (!isTiled)
            padBottom = 0;
        if (padRight > 0 || padBottom > 0) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(null)
                    .add(padRight)
                    .add(null)
                    .add(padBottom)
                    .add(null)
                    .add(null);
            image = JAI.create("border", pb);
        }
    }

    /**
     * Get the portion of tile fitting into buffer. You probably won't need to
     * override this.
     * 
     * @param rect the region to extract from image.
     */

    protected void getTilePixels(Rectangle rect) {
        // Grab the pixels
        Raster src = image.getData(rect);
        int col = (int) rect.getX();
        int row = (int) rect.getY();
        int rows = (int) rect.getHeight();
        int cols = (int) rect.getWidth();
        src.getPixels(col, row, cols, rows, _pixels);
    }

    /**
     * If derived classes can make a better estimate for the maximum size of a
     * compressed tile, they should override this, which assumes conservatively
     * that it won't be worse than twice the original size.
     * 
     * @param im the rendered image containing the image data
     */
    public int getCompressedTileSize(RenderedImage im) {
        sampleModel = im.getSampleModel();
        numBands = sampleModel.getNumBands();
        sampleSize = sampleModel.getSampleSize();
        return (int) Math.ceil(2 * tileWidth * tileLength * numBands
                * (sampleSize[0] / 8.0));
    }

    // ////////////////////////////////////////////////////
    // // Decoding Section
    // ////////////////////////////////////////////////////

    protected XTIFFDecodeParam decodeParam = null;
    protected boolean decodePaletteAsShorts = false;
    protected int unitsInThisTile;
    protected byte _bdata[] = null;
    protected short _sdata[] = null;
    protected byte[] bpixvals = null;
    protected short[] spixvals = null;
    protected DataBuffer buffer = null;
    protected int dataType;
    protected int image_type;

    /**
     * The standard decoder creation method
     */
    public XTIFFTileCodec create(XTIFFDecodeParam param) throws IOException {
        XTIFFTileCodecImpl codec = (XTIFFTileCodecImpl) create();
        codec.initialize(param);
        return codec;
    }

    protected void initialize(XTIFFDecodeParam param) throws IOException {
        decodeParam = param;
        decodePaletteAsShorts = param.getDecodePaletteAsShorts();
        directory = param.getDirectory();
        initialize();
        initializeDecoding();
    }

    /**
     * One-time setup for encoding. Some configurations require a temp array for
     * unpacking 16-bit palette data.
     */
    protected void setupBufferForDecoding() {

        // int length;
        buffer = sampleModel.createDataBuffer();
        dataType = sampleModel.getDataType();

        if (dataType == DataBuffer.TYPE_BYTE) {
            _bdata = ((DataBufferByte) buffer).getData();
            bpixvals = _bdata;
        } else if (dataType == DataBuffer.TYPE_USHORT) {
            _sdata = ((DataBufferUShort) buffer).getData();
            if (!decodePaletteAsShorts)
                spixvals = _sdata;
        } else if (dataType == DataBuffer.TYPE_SHORT) {
            _sdata = ((DataBufferShort) buffer).getData();
            if (!decodePaletteAsShorts)
                spixvals = _sdata;
        }
        if (decodePaletteAsShorts) {
            int len = _sdata.length;
            if (bitsPerSample[0] == 16)
                spixvals = new short[len];
            else
                bpixvals = new byte[len];
        }
    }

    /**
     * Decode a rectangle of data stored in bpixels into a raster tile. Usually
     * you will not need to override this, but instead implement the
     * decodeTilePixels methods.
     */
    public WritableRaster decode(RenderedImage img, Rectangle newRect,
                                 byte[] bpixels) {
        if (image == null) {
            setupSourceImage(img);
        }

        setupBufferForDecoding(); // set up every time

        unitsInThisTile = newRect.width * newRect.height * numBands;

        // uncompress data
        decodeTilePixels(bpixels, newRect);

        // post-processing of color data
        decodeColor(newRect);

        // put buffer into a tile
        return setTilePixels(newRect);
    }

    /**
     * Postprocess the uncompressed color data into the appropriate display
     * color model. This implementation Does a number of things:
     * <ul>
     * <li> For RGB color, reverse to BGR which apparently is faster for Java 2D
     * display
     * <li> For one-bit WHITE_IS_ZERO data, flip the values so that they will
     * look correct
     * <li> If the decodePaletteAsShorts flag is true then unpack the bits and
     * apply the lookup table, as 16-bit lookup is not supported in JAI.
     * </ul>
     * Override this if you have other color types.
     * 
     * @see XTIFFDecodeParam
     */
    protected void decodeColor(Rectangle newRect) {
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            decodeColor(bpixvals, _bdata, newRect);
            break;
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
            if (bpixvals != null)
                decodeColor(bpixvals, _sdata, newRect);
            else
                decodeColor(spixvals, _sdata, newRect);
        }
    }

    /**
     * Decode a tile of data into either byte or short pixel buffers. Override
     * this if you have other buffer types (e.g. int)
     */
    protected void decodeTilePixels(byte[] bpixels, Rectangle newRect) {

        // decodeTilePixels into the appropriate buffer
        if (bpixvals != null)
            decodeTilePixels(bpixels, newRect, bpixvals);
        else
            decodeTilePixels(bpixels, newRect, spixvals);
    }

    /**
     * Take the values from the buffer and store them in a WritableRaster
     * object.
     */
    protected WritableRaster setTilePixels(Rectangle rect) {
        return (WritableRaster) RasterFactory.createWritableRaster(sampleModel,
                buffer,
                new Point((int) rect.getX(), (int) rect.getY()));
    }

    /**
     * A useful Method to interpret a byte array as shorts. Method depends on
     * whether the bytes are stored in a big endian or little endian format.
     */

    protected void unpackShorts(byte byteArray[], short output[], int shortCount) {

        int j;
        int firstByte, secondByte;

        if (directory.isBigEndian()) {

            for (int i = 0; i < shortCount; i++) {
                j = 2 * i;
                firstByte = byteArray[j] & 0xff;
                secondByte = byteArray[j + 1] & 0xff;
                output[i] = (short) ((firstByte << 8) + secondByte);
            }

        } else {

            for (int i = 0; i < shortCount; i++) {
                j = 2 * i;
                firstByte = byteArray[j] & 0xff;
                secondByte = byteArray[j + 1] & 0xff;
                output[i] = (short) ((secondByte << 8) + firstByte);
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // /// Color decoding section
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Decode short pixel data, or interpret palette data as short from byte.
     */
    protected void decodeColor(byte[] bpix, short[] sdata, Rectangle newRect) {
        // short sswap;

        switch (image_type) {
        case XTIFF.TYPE_PALETTE:
            if (bitsPerSample[0] == 8) {

                // At this point the data is 1 banded and will
                // become 3 banded only after we've done the palette
                // lookup, since unitsInThisTile was calculated with
                // 3 bands, we need to divide this by 3.
                int unitsBeforeLookup = unitsInThisTile / 3;

                // Expand the palette image into an rgb image with ushort
                // data type.
                int cmapValue;
                int count = 0, lookup, len = colormap.length / 3;
                int len2 = len * 2;
                for (int i = 0; i < unitsBeforeLookup; i++) {
                    // Get the index into the colormap
                    lookup = bpix[i] & 0xff;
                    // Get the blue value
                    cmapValue = colormap[lookup + len2];
                    sdata[count++] = (short) (cmapValue & 0xffff);
                    // Get the green value
                    cmapValue = colormap[lookup + len];
                    sdata[count++] = (short) (cmapValue & 0xffff);
                    // Get the red value
                    cmapValue = colormap[lookup];
                    sdata[count++] = (short) (cmapValue & 0xffff);
                }

            } else if (bitsPerSample[0] == 4) {

                int padding = newRect.width % 2;
//                int bytesPostDecoding = ((newRect.width + 1) / 2)
//                        * newRect.height;

                int bytes = unitsInThisTile / 3;

                // Unpack the 2 pixels packed into each byte.
                byte[] data = new byte[bytes];

                int srcCount = 0, dstCount = 0;
                for (int j = 0; j < newRect.height; j++) {
                    for (int i = 0; i < newRect.width / 2; i++) {
                        data[dstCount++] = (byte) ((bpix[srcCount] & 0xf0) >> 4);
                        data[dstCount++] = (byte) (bpix[srcCount++] & 0x0f);
                    }

                    if (padding == 1) {
                        data[dstCount++] = (byte) ((bpix[srcCount++] & 0xf0) >> 4);
                    }
                }

                int len = colormap.length / 3;
                int len2 = len * 2;
                int cmapValue, lookup;
                int count = 0;
                for (int i = 0; i < bytes; i++) {
                    lookup = data[i] & 0xff;
                    cmapValue = colormap[lookup + len2];
                    sdata[count++] = (short) (cmapValue & 0xffff);
                    cmapValue = colormap[lookup + len];
                    sdata[count++] = (short) (cmapValue & 0xffff);
                    cmapValue = colormap[lookup];
                    sdata[count++] = (short) (cmapValue & 0xffff);
                }
            } else {
                throw new RuntimeException(JaiI18N.getString("XTIFFImageDecoder7"));
            }
            break;
        }
    }

    /**
     * Decode short color data, or interpret palette data as short.
     */
    protected void decodeColor(short[] spix, short[] sdata, Rectangle newRect) {
        short sswap;

        switch (image_type) {
        case XTIFF.TYPE_GREYSCALE_WHITE_IS_ZERO:
        case XTIFF.TYPE_GREYSCALE_BLACK_IS_ZERO:
            // Since we are using a ComponentColorModel with this image,
            // we need to change the WhiteIsZero data to BlackIsZero data
            // so it will display properly.
            if (image_type == XTIFF.TYPE_GREYSCALE_WHITE_IS_ZERO) {

                if (dataType == DataBuffer.TYPE_USHORT) {

                    for (int l = 0; l < sdata.length; l++) {
                        sdata[l] = (short) (65535 - spix[l]);
                    }

                } else if (dataType == DataBuffer.TYPE_SHORT) {

                    for (int l = 0; l < sdata.length; l++) {
                        sdata[l] = (short) (~spix[l]);
                    }
                }
            }

            break;

        case XTIFF.TYPE_RGB:
            // Change to BGR order, as Java2D displays that faster
            for (int i = 0; i < unitsInThisTile; i += 3) {
                sswap = spix[i];
                sdata[i] = spix[i + 2];
                sdata[i + 2] = sswap;
            }
            break;

        case XTIFF.TYPE_ORGB:
        case XTIFF.TYPE_ARGB_PRE:
        case XTIFF.TYPE_ARGB:
            // Change from RGBA to ABGR for Java2D's faster special cases
            for (int i = 0; i < unitsInThisTile; i += 4) {
                // Swap R and A
                sswap = spix[i];
                sdata[i] = spix[i + 3];
                sdata[i + 3] = sswap;

                // Swap G and B
                sswap = spix[i + 1];
                sdata[i + 1] = spix[i + 2];
                sdata[i + 2] = sswap;
            }
            break;

        case XTIFF.TYPE_RGB_EXTRA:
            break;

        case XTIFF.TYPE_PALETTE:
            if (decodePaletteAsShorts) {

                // At this point the data is 1 banded and will
                // become 3 banded only after we've done the palette
                // lookup, since unitsInThisTile was calculated with
                // 3 bands, we need to divide this by 3.
                int unitsBeforeLookup = unitsInThisTile / 3;

                // Since unitsBeforeLookup is the number of shorts,
                // but we do our decompression in terms of bytes, we
                // need to multiply it by 2 in order to figure out
                // how many bytes we'll get after decompression.
//                int entries = unitsBeforeLookup * 2;

                if (dataType == DataBuffer.TYPE_USHORT) {

                    // Expand the palette image into an rgb image with ushort
                    // data type.
                    int cmapValue;
                    int count = 0, lookup, len = colormap.length / 3;
                    int len2 = len * 2;
                    for (int i = 0; i < unitsBeforeLookup; i++) {
                        // Get the index into the colormap
                        lookup = spix[i] & 0xffff;
                        // Get the blue value
                        cmapValue = colormap[lookup + len2];
                        sdata[count++] = (short) (cmapValue & 0xffff);
                        // Get the green value
                        cmapValue = colormap[lookup + len];
                        sdata[count++] = (short) (cmapValue & 0xffff);
                        // Get the red value
                        cmapValue = colormap[lookup];
                        sdata[count++] = (short) (cmapValue & 0xffff);
                    }

                } else if (dataType == DataBuffer.TYPE_SHORT) {

                    // Expand the palette image into an rgb image with
                    // short data type.
                    int cmapValue;
                    int count = 0, lookup, len = colormap.length / 3;
                    int len2 = len * 2;
                    for (int i = 0; i < unitsBeforeLookup; i++) {
                        // Get the index into the colormap
                        lookup = spix[i] & 0xffff;
                        // Get the blue value
                        cmapValue = colormap[lookup + len2];
                        sdata[count++] = (short) cmapValue;
                        // Get the green value
                        cmapValue = colormap[lookup + len];
                        sdata[count++] = (short) cmapValue;
                        // Get the red value
                        cmapValue = colormap[lookup];
                        sdata[count++] = (short) cmapValue;
                    }
                }// dataType
            }// decodePaletteAsShorts
            break;

        case XTIFF.TYPE_TRANS:
            break;
        }
    }

    /**
     * Decode byte color data
     */
    protected void decodeColor(byte[] bpix, byte[] bdata, Rectangle newRect) {
        byte bswap;

        switch (image_type) {
        case XTIFF.TYPE_BILEVEL_WHITE_IS_ZERO:
        case XTIFF.TYPE_BILEVEL_BLACK_IS_ZERO:
        case XTIFF.TYPE_GREYSCALE_WHITE_IS_ZERO:
        case XTIFF.TYPE_GREYSCALE_BLACK_IS_ZERO:
        case XTIFF.TYPE_RGB_EXTRA:
        case XTIFF.TYPE_TRANS:
            // nothing
            break;

        case XTIFF.TYPE_RGB:
            if (bitsPerSample[0] == 8) {

                // Change to BGR order, as Java2D displays that faster
                for (int i = 0; i < unitsInThisTile; i += 3) {
                    bswap = bpix[i];
                    bdata[i] = bpix[i + 2];
                    bdata[i + 2] = bswap;
                }

            }
            break;

        case XTIFF.TYPE_ORGB:
        case XTIFF.TYPE_ARGB_PRE:
        case XTIFF.TYPE_ARGB:
            if (bitsPerSample[0] == 8) {
                // Convert from RGBA to ABGR for Java2D
                for (int i = 0; i < unitsInThisTile; i += 4) {
                    // Swap R and A
                    bswap = bpix[i];
                    bdata[i] = bpix[i + 3];
                    bdata[i + 3] = bswap;

                    // Swap G and B
                    bswap = bpix[i + 1];
                    bdata[i + 1] = bpix[i + 2];
                    bdata[i + 2] = bswap;
                }
            }
            break;

        case XTIFF.TYPE_PALETTE:
            // 
            break;

        }// switch
    }// decodeColor

}
