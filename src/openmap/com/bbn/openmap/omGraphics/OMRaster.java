// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMRaster.java,v $
// $RCSfile: OMRaster.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.Serializable;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DeepCopyUtil;

/**
 * The OMRaster object lets you create multi-colored images. An image is a two
 * dimensional array of pixel values that correspond to some color values. The
 * pixels are used from the top left, across each row to the right, down to the
 * bottom row.
 * <p>
 * There are two colormodels that are implemented in OMRaster - the direct
 * colormodel and the indexed colormodel. The direct colormodel is implemented
 * when the pixel values contain the actual java.awt.Color values for the image.
 * The indexed colormodel is implemented when the pixel values are actually
 * indexes into an array of java.awt.Colors. NOTE: The direct colormodel
 * OMRaster is faster to display, because it doesn't need to take the time to
 * resolve the colortable values into pixels.
 * <P>
 * 
 * For direct colormodel images: If you pass in a null pix or a pix with a zero
 * length, the object will create the pixels for you but will not general a
 * renderable version of the object. You will need to call render before
 * generate after the pixels have been set. This feature is for cached rasters,
 * where the content may be changed later. Use this (null pix) if you are
 * building images in a cache, for tiled mapping data or something else where
 * the data is not yet known. The memory for the pixels will be allocated, and
 * then they can be set with image data later when a database is accessed.
 * <P>
 * 
 * For ImageIcon OMRasters: Using an ImageIcon to create an OMRaster gives you
 * the ability to put an image on the screen based on an ImageIcon made from
 * file or URL. The OMRaster uses this ImageIcon as is - there is no opportunity
 * to change any parameters of this image. So set the colors, transparency, etc.
 * before you create the OMRaster.
 * <P>
 * 
 * For indexed colormodel images: If you pass in an empty byte array, a byte
 * array will be created based on the width and height. You will have to resolve
 * empty colortables and set the pixels later. Use this method (null bytes) if
 * you are building images in a cache, for tiled mapping data or something else
 * where the data is not yet known. The memory for the pixels will be allocated,
 * and then they can be set with image data later when a database is accessed.
 * 
 * There is the ability to add a filter to the image, to change it's appearance
 * for rendering. The most common filter, which is included as a kind of
 * default, is the scale filter. Filtering the OMRasterObject replaces the
 * bitmap variable, which is the internal java.awt.Image used for rendering. For
 * OMRasters created with pixels, or with the colortable and the colortable
 * index, the original data is left intact, and can be recreated later, or
 * rescaled on the fly, because the internal bitmap will be recreated prior to
 * re-scaling. For OMRasters created by ImageIcons or Images, though, you'll
 * need to hold on to the original Image. The internal version is replaced by
 * the filtered version.
 * 
 * @see OMRasterObject
 */
public class OMRaster
        extends OMRasterObject
        implements Serializable {

    /**
     * The integer colors that are needed in a java colortable. The Color[] that
     * gets passed into some of the constructors goes to build this, but this
     * array is really used to build the image pixel array.
     */
    protected int[] colors = null;

    /**
     * The transparency of the image. If this is set to anything less than 255,
     * this value is used for all colors in the image. If it is set to 255, then
     * the alpha value in each Color regulates the transparency of the image.
     * The value of this variable should stay in the range:
     * <code>0 &lt;= transparent &lt;= 255</code>
     */
    protected int transparent = 255;

    /**
     * Construct a blank OMRaster, to be filled in with setX calls.
     */
    public OMRaster() {
        super(RENDERTYPE_UNKNOWN, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
    }

    // /////////////////////////////////// INT PIXELS - DIRECT
    // COLORMODEL

    /**
     * Creates an OMRaster images, Lat/Lon placement with a direct colormodel.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @see #setPixel
     */
    public OMRaster(double lt, double ln, int w, int h, int[] pix) {

        super(RENDERTYPE_LATLON, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_DIRECT);
        lat = lt;
        lon = ln;
        width = w;
        height = h;
        pixels = pix;

        if (pixels == null || pixels.length == 0)
            pixels = new int[height * width];
    }

    /**
     * Create an OMRaster image, XY placement with a direct colormodel.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @see #setPixel
     */
    public OMRaster(int x1, int y1, int w, int h, int[] pix) {

        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_DIRECT);
        x = x1;
        y = y1;
        width = w;
        height = h;
        pixels = pix;

        if (pixels == null || pixels.length == 0)
            pixels = new int[height * width];
    }

    /**
     * Create an OMRaster, Lat/lon placement with XY offset with a direct
     * colormodel.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param pix color values for the pixels.
     * @see #setPixel
     */
    public OMRaster(double lt, double ln, int offset_x1, int offset_y1, int w, int h, int[] pix) {

        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_DIRECT);
        lat = lt;
        lon = ln;
        x = offset_x1;
        y = offset_y1;
        width = w;
        height = h;
        pixels = pix;

        if (pixels == null || pixels.length == 0) {
            pixels = new int[height * width];
        }
    }

    // //////////////////////////////////// IMAGEICON

    /**
     * Create an OMRaster, Lat/Lon placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param ii ImageIcon used for the image.
     */
    public OMRaster(double lt, double ln, ImageIcon ii) {
        this(lt, ln, ii.getImage());
    }

    /**
     * Create an OMRaster, Lat/Lon placement with an Image.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param ii Image used for the image.
     */
    public OMRaster(double lt, double ln, Image ii) {
        super(RENDERTYPE_LATLON, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_IMAGEICON);
        lat = lt;
        lon = ln;
        setImage(ii);
    }

    /**
     * Create an OMRaster image, X/Y placement with an ImageIcon.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param ii ImageIcon used for the image.
     */
    public OMRaster(int x1, int y1, ImageIcon ii) {
        this(x1, y1, ii.getImage());
    }

    /**
     * Create an OMRaster image, X/Y placement with an Image.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param ii Image used for the image.
     */
    public OMRaster(int x1, int y1, Image ii) {
        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_IMAGEICON);
        x = x1;
        y = y1;
        setImage(ii);
    }

    /**
     * Create an OMRaster, Lat/Lon with X/Y placement with an ImageIcon.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param ii ImageIcon used for the image.
     */
    public OMRaster(double lt, double ln, int offset_x1, int offset_y1, ImageIcon ii) {
        this(lt, ln, offset_x1, offset_y1, ii.getImage());
    }

    /**
     * Create an OMRaster, Lat/Lon with X/Y placement with an Image. Make sure
     * that the Image is complete( if being loaded over the internet) and ready
     * to be drawn. Otherwise, you have to figure out when the Image is
     * complete, so that you can get the layer to paint it! Use the ImageIcon
     * constructor if you don't mind blocking to wait for the pixels to arrive.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param ii Image used for the image.
     */
    public OMRaster(double lt, double ln, int offset_x1, int offset_y1, Image ii) {

        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_IMAGEICON);
        lat = lt;
        lon = ln;
        x = offset_x1;
        y = offset_y1;
        setImage(ii);
    }

    // //////////////////////////////////// BYTE PIXELS with
    // COLORTABLE

    /**
     * Lat/Lon placement with a indexed colormodel, which is using a colortable
     * and a byte array to construct the int[] pixels.
     * 
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @see #setPixel
     */
    public OMRaster(double lt, double ln, int w, int h, byte[] bytes, Color[] colorTable, int trans) {

        super(RENDERTYPE_LATLON, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_INDEXED);
        lat = lt;
        lon = ln;
        width = w;
        height = h;
        bits = bytes;
        transparent = trans;
        if (colorTable != null) {
            setColors(colorTable);
        }

        if (bits != null && bits.length != 0) {
            if (colorTable != null && colors.length != 0) {
                computePixels();
            }
        } else {
            bits = new byte[height * width];
        }
    }

    /**
     * XY placement with a indexed colormodel, which is using a colortable and a
     * byte array to construct the int[] pixels.
     * 
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @see #setPixel
     */
    public OMRaster(int x1, int y1, int w, int h, byte[] bytes, Color[] colorTable, int trans) {

        super(RENDERTYPE_XY, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_INDEXED);
        x = x1;
        y = y1;
        width = w;
        height = h;
        bits = bytes;
        transparent = trans;
        if (colorTable != null) {
            setColors(colorTable);
        }

        if (bits != null && bits.length != 0) {
            if (colorTable != null && colors.length != 0) {
                computePixels();
            }
        } else {
            bits = new byte[height * width];
        }
    }

    /**
     * Lat/lon placement with XY offset with a indexed colormodel, which is
     * using a colortable and a byte array to construct the int[] pixels.
     * 
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes colortable index values for the pixels.
     * @param colorTable color array corresponding to bytes
     * @param trans transparency of image.
     * @see #setPixel
     */
    public OMRaster(double lt, double ln, int offset_x1, int offset_y1, int w, int h, byte[] bytes, Color[] colorTable, int trans) {

        super(RENDERTYPE_OFFSET, LINETYPE_UNKNOWN, DECLUTTERTYPE_NONE);
        setColorModel(COLORMODEL_INDEXED);
        lat = lt;
        lon = ln;
        x = offset_x1;
        y = offset_y1;
        width = w;
        height = h;
        transparent = trans;
        bits = bytes;
        if (colorTable != null) {
            setColors(colorTable);
        }

        if (bits != null && bits.length != 0) {
            if (colorTable != null && colors.length != 0) {
                computePixels();
            }
        } else {
            bits = new byte[height * width];
        }
    }

    // ////////////////////////////////////////////////////
    /**
     * Just a simple check to see if the x, y pair actually fits into the pixel
     * array.
     * 
     * @param x x location of pixel, from the left side of image.
     * @param y y location of pixel, from the top of image.
     * @return true if location within pixel array.
     */
    private boolean boundsSafe(int x, int y) {
        if ((y < 0) || (y >= height) || (x < 0) || (x >= width)) {
            return false;
        }
        return true;
    }

    /**
     * Set the ImageIcon.
     * 
     * @param img ImageIcon
     */
    public void setImageIcon(ImageIcon img) {
        setImage(img.getImage());
    }

    /**
     * Set the image pixel value at a location.
     * 
     * @param x Horizontal location of pixel from left.
     * @param y Vertical location of pixel from top.
     * @param colorValue the color value of the pixel.
     * @return true if x, y location valid.
     */
    public boolean setPixel(int x, int y, int colorValue) {

        if (boundsSafe(x, y)) {
            pixels[(y * width) + x] = colorValue;
            setNeedToRegenerate(true);
            return true;
        }
        return false; // fail
    }

    /**
     * Get the image pixel value at a location.
     * 
     * @param x Horizontal location of pixel from left.
     * @param y Vertical location of pixel from top.
     * @return the integer color value of the image at x, y
     */
    public int getPixel(int x, int y) {
        if (boundsSafe(x, y)) {
            return pixels[(y * width) + x];
        }
        return 0; // fail - but also the ct[0] - hmmmmm.
    }

    /**
     * Set image byte data, for index frame using colortable.
     * 
     * @param x Horizontal location of pixel from left.
     * @param y Vertical location of pixel from top.
     * @param ctIndex The array index of the applicable color in the color
     *        table.
     * @return true if x, y location valid.
     */
    public boolean setByte(int x, int y, byte ctIndex) {
        if (boundsSafe(x, y) && bits != null) {
            bits[(y * width) + x] = ctIndex;
            setNeedToRegenerate(true);
            return true;
        }
        return false; // fail
    }

    /**
     * Get image byte data, which the index to a colortable for indexed images.
     * 
     * @param x Horizontal location of pixel from left.
     * @param y Vertical location of pixel from top.
     * @return byte value of bytes(x, y)
     */
    public byte getByte(int x, int y) {
        if (boundsSafe(x, y) && bits != null) {
            return bits[(y * width) + x];
        }
        return 0; // fail - but also the ct[0] - hmmmmm.
    }

    /**
     * Set the bytes used to create the pixels used to create the image. Checks
     * to see of the length matches the height * width, but doesn't do anything
     * if they don't match, except print out a warning. Make sure it does.
     * 
     * @param values byte values containing bit pixel values.
     */
    public void setBits(byte[] values) {
        super.setBits(values);
        if ((values.length) != (height * width)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("OMBitmap: new byte[] size (" + +values.length + ") doesn't" + " match [height*width (" + height
                        * width + ")]");
            }
        }
    }

    /**
     * Set the transparency of the index type images. For the Direct Colormodel
     * the pixel data needs to be reconstructed, so this is an O(pixels.length)
     * operation. For an indexed colormodel, the data still needs to be
     * reconstructed, but it will cost you the time in generate(). The
     * transparency value should be a number between 0-255.
     * 
     * @param value New value of the alpha value for the image.
     */
    public void setTransparent(int value) {
        value &= 0x000000ff;
        if (transparent == value)
            return;
        transparent = value;
        setNeedToRegenerate(true);
        if (bits != null) {
            pixels = null;
            computePixels();
        } else {
            value <<= 24;// move to alpha position
            // direct color model, touch each pixel in the image
            for (int i = 0; i < pixels.length; i++) {

                // Do this if we want to support images that have
                // transparent pixels, and we want each pixel to have
                // the most transparent pixel.

                // Why don't we want to do this??? DFD

                // int pixAlpha = 0xFF000000 & pixels[i];
                // pixAlpha = (pixAlpha < value)?pixAlpha:value;
                // pixels[i] = (0x00ffffff & pixels[i]) | pixAlpha;

                pixels[i] = (0x00ffffff & pixels[i]) | value;
            }
        }
    }

    /**
     * Get the transparent setting of the image.
     * 
     * @return the transparent value (0-255) of the image.
     */
    public int getTransparent() {
        return transparent;
    }

    /**
     * Set the color table to the int RGB values passed in. Valid for the
     * indexed colormodel only. The pixels will be colored according to these
     * values.
     * 
     * @param values array of color RGB values.
     */
    public void setColors(int[] values) {
        if (colorModel != COLORMODEL_INDEXED) {
            logger.fine("OMRaster: Setting colors for final colortable when a colortable isn't needed!");
        } else {
            colors = values;
            setNeedToRegenerate(true);
        }
    }

    /**
     * Set the color table according to the java.awt.Color array passed in.
     * Valid for the indexed colormodel only. The pixels will be colored
     * according to these values. The transparency values of these colors will
     * only take effect of they are less than the transparency value of the
     * images' value.
     * 
     * @param values array of java.awt.Color colors.
     */
    public void setColors(Color[] values) {

        if (colorModel != COLORMODEL_INDEXED) {
            logger.fine("Setting colors for final colortable when a colortable isn't needed!");
            return;

        } else if (values == null || values.length == 0) {
            colors = new int[0];
            logger.fine("What are you trying to do to me?!? The colortables gots to have values!");
            return;

        } else {

            if (values.length > 0) {
                colors = new int[values.length];
                boolean allTransparent = true;
                int trans = (transparent << 24) & 0xff000000;

                // Turn the color table into a table using the
                // default OMava color model.
                for (int i = 0; i < values.length; i++) {

                    // The transparent field can be set for the whole
                    // image, while the open part of the colortable
                    // entry
                    // structure is the transparent setting for that
                    // particular color.
                    if (transparent < 255) {
                        int argb = values[i].getRGB();

                        if (values[i].getAlpha() > transparent) {
                            // If the transparent value of the pixel
                            // is
                            // lower than the transparency value, keep
                            // that instead - don't make things more
                            // visible then they were.
                            colors[i] = (0x00ffffff & argb) | trans;
                        } else {
                            colors[i] = argb;
                        }

                    } else {
                        colors[i] = values[i].getRGB();
                    }

                    // Just check if all the colors are transparent -
                    // this is a pain to figure out if you are
                    // getting colors from some server that doesn't
                    // know about alpha values.
                    if (allTransparent && ((colors[i] >>> 24) != 0)) {
                        allTransparent = false;
                    }
                }
                if (DEBUG && allTransparent) {
                    logger.fine("OMRaster: **Whasamatta?** Image created with all transparent pixels!");
                }
            }

            // This is wrong - we do need to force a computePixels,
            // but in generate...
            // computePixels();
            // This will do it....
            pixels = null;
            setNeedToRegenerate(true);
        }
    }

    /**
     * Get the array of colors used in the indexed color model. If the image is
     * not a indexed colormodel, the int[] will be null.
     * 
     * @return color int[] if index colormodel, null otherwise.
     */
    public int[] getColors() {
        return colors;
    }

    // ///////////////////////////////////////////////////////
    /**
     * Compute pixels is the function that resolves the color table into pixel
     * integer values used in the Image. It uses the bits as indexes into the
     * color table, and builds a big array of ints to use in the bitmap image.
     * If the bits are null, then the object was created in the direct color
     * model where the colors are already built into the pixels. SO, if you call
     * this, the pixels have to be null and the bits good indexes into the
     * colortable.
     * 
     * @return true if the image is OK to draw after this function.
     */
    protected boolean computePixels() {

        if (DEBUG)
            logger.fine("OMRaster.compute pixels!");

        int i;
        if (colorModel != COLORMODEL_INDEXED) {
            return true;
        }

        if (colors == null || colors.length == 0) {
            logger.fine("OMRaster: attempting to compute pixels without color table!");
            return false;
        }

        int nPixels = width * height;
        if (DEBUG) {
            logger.fine("Computing pixels for image size:" + width + ", " + height);
        }
        // pixels are the image pixels
        pixels = new int[nPixels];

        // Now, using the new constructed color table, build a set of
        // pixels.
        // alpha is a ready, shifted version of the overall
        // transparency value;
        int alpha = (transparent << 24) & 0xff000000;
        // numColors is the number of colors.
        int numColors = colors.length;

        for (i = 0; i < nPixels; i++) {
            byte b = bits[i];
            int color;

            // make the alpha for this color the lessor of what the
            // colortable is, versus the transparent value
            // int pixAlpha;

            try {
                if (b >= numColors) {
                    if (DEBUG)
                        logger.fine("OMRaster:.computePixels() problem!: " + b);
                    color = clear.getRGB();

                } else if (b < 0) {
                    color = colors[MoreMath.signedToInt(b)];
                } else {
                    color = colors[b];
                }
            } catch (ArrayIndexOutOfBoundsException aiiobe) {
                // If the color can't be found, don't paint it.
                if (DEBUG) {
                    logger.fine("OMRaster.computePixels() problem, can't find color for index: " + aiiobe.getMessage());
                }

                color = clear.getRGB();
            }

            // OK, got an int value, argb, for the color to be put on
            // the pixel. Now we need to straighten out the
            // transparency.
            if (transparent < 255 && ((color >> 24) > transparent)) {
                // this means that the overall transparency should be
                // more (lower number, more transparent) than the
                // pixel color.
                color = alpha | (0x00FFFFFF & color);
            } // Otherwise, just go with the alpha value set on the
              // color...

            pixels[i] = color;

        }

        return true;
    }

    /**
     * Prepare the graphics for rendering. For all image types, it positions the
     * image relative to the projection. For direct and indexed colormodel
     * images, it creates the ImageIcon used for drawing to the window (internal
     * to object). For indexed colormodel images, it also calls computePixels,
     * to resolve the colortable and the bytes to create the image pixels.
     * 
     * @param proj Projection used to position the image on the window.
     * @return true if the image is ready to paint.
     */
    public boolean generate(Projection proj) {

        // Position sets the position for the OMRaster!!!!
        if (!position(proj)) {
            if (DEBUG) {
                logger.fine("OMRaster.generate(): positioning failed!");
            }
            return false;
        }

        // We used to just return here if the OMRaster didn't need to
        // be regenerated, but that didn't create the shape properly.

        if (getNeedToRegenerate()) {
            bitmap = getBitmapFromInternalData();
            if (bitmap == null) {
                return false;
            }
        }

        // generate shape that is a boundary of the generated image.
        // We'll make it a GeneralPath rectangle.
        setShape();

        setNeedToRegenerate(false);
        return true;
    }

    protected Image getBitmapFromInternalData() {
        Image bi = bitmap;

        if (colorModel != COLORMODEL_IMAGEICON) {

            // This section is for the indexed color model rasters
            // that need to resolve the color map to the bit array
            // indexes.
            boolean allsWell = true;
            // If pixels == null, then computePixels has not been
            // called
            if (pixels == null) {
                allsWell = false;
                if (bits != null)
                    allsWell = computePixels();
                if (!allsWell) {
                    logger.fine("attempted to generate without pixels defined!");
                    return null;
                }
            }

            if (width <= 0 || height <= 0) {
                // NeedToRegenerate should still be true, so it won't
                // render.
                return null;
            }

            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            /**
             * Looking at the standard BufferedImage code, an int[0] is
             * allocated for every pixel. Maybe the memory usage is optimized
             * for that, but it goes through a call stack for every pixel to do
             * it. Let's just cycle through the data and write the pixels
             * directly into the raster.
             */
            WritableRaster raster = (WritableRaster) ((BufferedImage) bi).getRaster();
            raster.setDataElements(0, 0, width, height, pixels);
        } 

        /*
         * REPLACING bitmap with the filtered version - keep a copy yourself
         * if you need the original!!! i.e. for COLORMODEL_IMAGEICON
         */
        if (imageFilter != null) {
            bi = filterImage(bi);
        }
        
        return bi;
    }
    
    public void restore(OMGeometry source) {
       super.restore(source);
       if (source instanceof OMRaster) {
          OMRaster raster = (OMRaster) source;
          this.colors = DeepCopyUtil.deepCopy(raster.colors);
          this.transparent = raster.transparent;
       }
    }
}