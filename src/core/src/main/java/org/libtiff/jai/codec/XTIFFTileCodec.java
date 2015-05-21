package org.libtiff.jai.codec;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * The XTIFFTileCodec is the common interface used by all registered
 * implementations of TIFF data compression. Unlike other file formats, TIFF has
 * no fixed set of data compressions schemes, but allows for new and
 * user-defined compression types.
 * <p>
 * To use a new codec with the XTIFFDirectory you must do the following things:
 * <ul>
 * <li> register XTIFF methods with JAI through the XTIFFDescriptor
 * <li> implement the methods below; it is recommended to use the
 * XTIFFTileCodecImpl class for this purpose, as it reduces the problem to one
 * of defining the actual data compression and decompression algorithms. If you
 * do not support encoding (e.g LZW), be sure the canEncode() methods returns
 * false.
 * <li> register the implemented code with the XTIFFDirectory, indicating in the
 * register method all TIFF compression codes that this codec can handle.
 * </ul>
 * 
 * @see XTIFFTileCodecImpl
 */
public interface XTIFFTileCodec {

    /**
     * Create a codec for encoding data.
     * 
     * @param param the encoding parameter. It is the responsibility of the
     *        codec to initialize itself from this parameter.
     */
    public XTIFFTileCodec create(XTIFFEncodeParam param) throws IOException;

    /**
     * Create a codec for decoding
     * 
     * @param param the decoding parameter. It is the responsibility of the
     *        codec to initialize itself from this parameter.
     */
    public XTIFFTileCodec create(XTIFFDecodeParam param) throws IOException;

    /**
     * Encode some data from RenderedImage, and return the actual number of
     * bytes stored in output buffer.
     */
    public int encode(RenderedImage im, Rectangle rect, byte[] output);

    /**
     * Decode input byte data into a new WritableRaster, using information from
     * underlying RenderedImage
     */
    public WritableRaster decode(RenderedImage im, Rectangle rect, byte[] input);

    /**
     * Return the associated TIFF compression code
     */
    public int getCompression();

    /**
     * Return the largest possible compressed buffer size for this image in
     * bytes. This is used by the XTIFFImage constructor to allocate a decoding
     * buffer.
     */
    public int getCompressedTileSize(RenderedImage im);

    /**
     * Register this codec with the XTIFFDirectory. The method may register
     * itself with multiple TIFF compression codes, if it supports more than
     * one.
     * 
     * @see XTIFFDirectory
     */
    public void register();
}
